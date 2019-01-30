package com.flickshot.util;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LinkedNodeList<T extends LinkedNodeInterface<T>> implements List<T>{
	private T head;
	private T tail;
	private int size = 0;

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		for(T x: arg0){
			add(x);
		}
		return false;
	}

	@Override
	public void clear() {
		head=null;
		tail=null;
		size = 0;
	}
	
	public T getHead(){
		return head;
	}
	
	public T getTail(){
		return tail;
	}
	
	public T pop(){
		T temp = head;
		head = head.next();
		head.prev(null);
		temp.next(null);
		return temp;
	}
	
	public boolean push(T node){
		if(head!=null)head.prev(node);
		node.prev(null);
		node.next(head);
		head = node;
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for(Object x: arg0){
			if(!contains(x)) return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return size==0;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean changed = false;
		T curr = head;
		while(curr!=null){
			T temp = curr;
			curr = curr.next();
			if(!arg0.contains(temp)){
				remove(temp);
				changed = true;
				size--;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean changed = false;
		T curr = head;
		while(curr!=null){
			T temp = curr;
			curr = curr.next();
			if(!arg0.contains(temp)){
				remove(temp);
				changed = true;
				size--;
			}
		}
		return changed;
	}

	@Override
	public Object[] toArray() {
		Object[] objs = new Object[size];
		T curr = head;
		int i = 0;
		while(curr!=null){
			objs[i++] = curr;
			curr = curr.next();
		}
		return objs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> A[] toArray(A[] arg0) {
		if(arg0.length<size) arg0 = (A[])(new Object[size]);
		T curr = head;
		int i = 0;
		while(curr!=null){
			arg0[i++] = (A)curr;
			curr = curr.next();
		}
		return null;
	}

	@Override
	public boolean add(T arg0) {
		if(tail!=null) tail.next(arg0);
		arg0.prev(tail);
		tail = arg0;
		if(head==null)head=arg0;
		size++;
		return true;
	}
	
	@Override
	public boolean contains(Object arg0) {
		T curr = head;
		while(curr!=null){
			if(curr == arg0) return true;
			curr = curr.next();
		}
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return new NodeIterator(head);
	}

	/**
	 * assumes node is a part of the list
	 * @param arg0
	 * @return
	 */
	public boolean remove(Object arg0) {
		if(arg0 instanceof LinkedNodeInterface<?>){
			LinkedNodeInterface<?> node = (LinkedNodeInterface<?>)arg0;
			return remove(node);
		}
		return false;
	}
	
	/**
	 * assumes node is a part of the list
	 * @param arg0
	 * @return
	 */
	public final boolean remove(LinkedNodeInterface<?> node){
		if(node==head){
			head=head.next();
		}
		if(node==tail){
			tail=tail.prev();
		}
		node.remove();
		node.next(null);
		node.prev(null);
		return true;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void add(int arg0, T arg1) {
		if(arg0>=size) throw new IndexOutOfBoundsException();
		int i = 0;
		T curr = head;
		T prev = null;
		while(curr != null && i<arg0){
			prev = curr;
			curr = curr.next();
			i++;
		}
		insert(prev,curr,arg1);
	}
	
	public void add(T link, T node){
		insert(link,link.next(),node);
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		if(arg0>=size) throw new IndexOutOfBoundsException();
		int i = 0;
		T curr = head;
		T prev = null;
		while(curr != null && i<arg0){
			prev = curr;
			curr = curr.next();
			i++;
		}
		for(T obj: arg1){
			insert(prev,curr,obj);
			prev = obj;
		}
		return false;
	}

	@Override
	public T get(int arg0) {
		if(arg0>=size) throw new IndexOutOfBoundsException();
		int i = 0;
		T curr = head;
		while(curr != null && i<arg0){
			curr = curr.next();
			i++;
		}
		return curr;
	}

	@Override
	public int indexOf(Object arg0) {
		int i = 0;
		T curr = head;
		while(curr != null){
			if(curr==arg0)return i;
			curr = curr.next();
			i++;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object arg0) {
		int i = size-1;
		T curr = tail;
		while(curr != null){
			if(curr==arg0)return i;
			curr = curr.prev();
			i--;
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return new NodeIterator(head);
	}

	@Override
	public ListIterator<T> listIterator(int arg0) {
		return new NodeIterator(get(arg0));
	}

	@Override
	public T remove(int arg0) {
		if(arg0>=size) throw new IndexOutOfBoundsException();
		int i = 0;
		T curr = head;
		while(curr != null && i<arg0){
			curr = curr.next();
			i++;
		}
		remove(curr);
		return curr;
	}

	@Override
	public T set(int arg0, T arg1) {
		setNode(get(arg0),arg1);
		return null;
	}

	@Override
	public List<T> subList(int arg0, int arg1) {
		throw new UnsupportedOperationException();
	}
	
	private final void insert(T prev, T next, T node){
		node.prev(prev);
		node.next(next);
		if(prev!=null)prev.next(node);
		if(next!=null)next.prev(node);
	}
	
	public final void setNode(T a, T b){
		T next = a.next();
		T prev = a.prev();
		b.next(next);
		b.prev(prev);
		if(next!=null)next.prev(b);
		if(prev!=null)prev.next(b);
		if(a==head)head=b;
		if(a==tail)tail=b;
	}
	
	private class NodeIterator implements ListIterator<T>{
		T prev;
		T current;
		T last;
		
		int i = 0;
		
		NodeIterator(T head){
			current = head;
		}
		
		@Override
		public void add(T arg0) {
			insert(prev,current,arg0);
			prev = arg0;
			i++;
			last=null;
		}

		@Override
		public boolean hasNext() {
			return current!=null;
		}

		@Override
		public boolean hasPrevious() {
			return prev!=null;
		}

		@Override
		public T next() {
			T temp = current;
			prev = current;
			current = current.next();
			i++;
			last = temp;
			return temp;
		}

		@Override
		public int nextIndex() {
			return i+1;
		}

		@Override
		public T previous() {
			T temp = prev;
			current = prev;
			prev = prev.prev();
			i--;
			last = temp;
			return prev;
		}

		@Override
		public int previousIndex() {
			return i-1;
		}

		@Override
		public void remove() {
			if(last!=null){
				last.remove();
				if(last==head){
					head=head.next();
				}
				if(last==tail){
					tail=tail.next();
				}
				last.next(null);
				last.prev(null);
				last=null;
			}
		}

		@Override
		public void set(T arg0) {
			if(last!=null){
				setNode(last,arg0);
				last=null;
			}
		}
		
	}
}
