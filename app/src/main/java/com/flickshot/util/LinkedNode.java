package com.flickshot.util;

public abstract class LinkedNode<T extends LinkedNodeInterface<T>> implements LinkedNodeInterface<T>{
	public T prev;
	public T next;
	
	@Override
	public T next() {
		return next;
	}
	@Override
	public T next(T next) {
		this.next = next;
		return next;
	}
	@Override
	public T prev() {
		return prev;
	}
	@Override
	public T prev(T prev) {
		this.prev = prev;
		return prev;
	}
	@Override
	public void remove(){
		if(prev!=null){
			prev.next(next);
		}
		if(next!=null){
			next.prev(prev);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void add(T next) {
		if(this.next==null){
			this.next = next;
			next.prev((T)this);
		}else{
			this.next.add(next);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void push(T next){
		if(prev==null){
			prev = next;
			next.next((T)this);
		}else{
			prev.push(next);
		}
	}
	
}
