package com.flickshot.assets.physMaterials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.assets.Asset;
import com.flickshot.assets.AssetManager;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.physics.PhysMaterial;

public class PhysMaterialManager extends AssetManager{
	private static final String NAME = "phys_material";
	private static final HashSet<Asset> assets = new HashSet<Asset>();
	private static final HashMap<String,PhysMaterial> materials = new HashMap<String,PhysMaterial>();

	@Override
	public void swap(Collection<Asset> c) {
		final ArrayList<Asset> rids = new ArrayList<Asset>();
		for(Asset curr: assets){
			if(curr.global || c.contains(curr)) continue;
			rids.add(curr);
		}
		for(Asset asset : rids){
			unload(asset);
		}
		load(c);
	}

	@Override
	public void load(Asset asset) {
		if(materials.containsKey(asset.id)) throw new IllegalStateException("key already taken "+asset);
		String content = asset.content;
		double elasticity=-1;
		double density=-1;
		double staticFriction=-1;
		double dynamicFriction=-1;
		try{
			String[] values = content.split(";");
			for(String v: values){
				String[] pair = v.split(" ");
				if(pair[0].equals("elasticity")){
					elasticity = Double.parseDouble(values[1]);
				}else if(pair[0].equals("density")){
					density = Double.parseDouble(values[1]);
				}else if(pair[0].equals("staticFriction")){
					staticFriction = Double.parseDouble(values[1]);
				}else if(pair[0].equals("dynamicFriction")){
					dynamicFriction = Double.parseDouble(values[1]);
				}else{
					throw new IllegalStateException("unidentified value: "+values[0]);
				}
			}
		}catch(Exception ex){
			throw new IllegalStateException("failed to parse material data:"+content,ex);
		}
		if(elasticity<0 || density<0 || staticFriction<0 || dynamicFriction<0) throw new IllegalStateException("failed to parse material data:"+content);
		materials.put(asset.id,new PhysMaterial(density,elasticity,staticFriction,dynamicFriction));
		assets.add(asset);
		Log.d("materials",asset+" loaded");
	}

	@Override
	public void load(Collection<Asset> c) {
		for(Asset a: c){
			if(!materials.containsKey(a.id)){
				load(a);
			}
		}
	}

	@Override
	public void unload(Asset asset) {
		if(!assets.contains(asset.id)) throw new IllegalStateException("material not loaded");
		materials.remove(asset.id);
		assets.remove(asset);
		Log.d("materials",asset+" unloaded");
	}

	@Override
	public void unload(Collection<Asset> c) {
		for(Asset a: c){
			if(assets.contains(a)){
				unload(a);
			}
		}
	}

	@Override
	public Object get(String id) {
		return materials.get(id);
	}
	
	public boolean contains(String id){
		return materials.containsKey(id);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
}
