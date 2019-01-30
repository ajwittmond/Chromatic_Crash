package com.flickshot.util;

import java.util.ArrayList;
import java.util.List;

public class ListWrapper<T>{
	List<T> list;
	private int step;
	private final ArrayList<ListAssignable<T>> assignables = new ArrayList<ListAssignable<T>>();
	private final ArrayList<T[]> arrays = new ArrayList<T[]>();
	
	private int pos=0;
	
	ListWrapper(List<T> list,int step){
		set(list,step);
	}
	
	public final void set(List<T> list, int step){
		if(list==null || step<1) throw new IllegalArgumentException();
		this.list = list;
		this.step = step;
		setPosition(0);
	}
	
	public final int size(){
		return list.size()/step;
	}
	
	public final int getPosition(){
		return pos;
	}
	
	public final void setPosition(int index){
		if(index<0 || index>=list.size()/step) throw new IndexOutOfBoundsException();
		pos = index;
		int offset = index*step;
		for(int i = 0; i<assignables.size();i++) assignables.get(i).assign(offset,list);
		for(int i = 0; i<arrays.size();i++){
			T[] a = arrays.get(i);
			for(int j = 0 ;j<a.length && j+offset<list.size(); j++){
				a[j]=list.get(j+offset);
			}
		}
	}
	
	public final boolean hasNext(){
		return pos<(list.size()/step)-1;
	}
	
	public final void next(){
		setPosition(pos+1);
	}
	
	public final boolean hasPrev(){
		return pos>0;
	}
	
	public final void prev(){
		setPosition(pos+1);
	}

	public final void addAssignable(ListAssignable<T> a){
		if(a==null) throw new IllegalArgumentException();
		assignables.add(a);
	}
	
	public final void addAssignable(T[] a){
		if(a==null) throw new IllegalArgumentException();
		arrays.add(a);
	}
	
	public final boolean removeAssignable(ListAssignable<T> a){
		return assignables.remove(a);
	}
	
	public final boolean removeAssignable(T[] a){
		return arrays.remove(a);
	}
	
	public ListAssignable<T> setToCurrent(ListAssignable<T> a){
		a.assign(pos*step,list);
		return a;
	}
	
	public T[] setToCurrent(T[] a){
		int offset = pos*step;
		for(int i = 0; i<a.length && i+offset<list.size();i++){
			a[i]=list.get(i+offset);
		}
		return a;
	}
}
