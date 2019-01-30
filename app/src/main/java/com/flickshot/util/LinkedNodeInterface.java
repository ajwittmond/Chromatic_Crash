package com.flickshot.util;

public interface LinkedNodeInterface<T extends LinkedNodeInterface>{
	public T next();
	public T next(T next);
	public T prev();
	public T prev(T prev);
	public void remove();
	public void add(T next);
	public void push(T next);
}
