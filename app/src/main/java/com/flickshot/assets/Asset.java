package com.flickshot.assets;

public class Asset {
	public final String id;
	public final String fileId;
	public final String content;
	public final boolean global;
	
	public Asset(String id,String fileId,String content,boolean global){
		this.id = id;
		this.fileId = fileId;
		this.content = content;
		this.global = global;
	}
	
	@Override
	public String toString(){
		return "{id:"+id+", fileId:"+fileId+"}";
	}
	
	@Override
	public int hashCode(){
		return id.hashCode()+fileId.hashCode()+content.hashCode()+(global ? 1 : 0);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Asset){
			Asset a = (Asset)o;
			return a.global==global && a.id.equals(id) && a.fileId.equals(fileId) && a.content.equals(content);
		}
		return false;
	}
}
