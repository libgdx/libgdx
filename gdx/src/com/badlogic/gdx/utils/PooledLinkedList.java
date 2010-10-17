package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.Pool.PoolObjectFactory;


/**
 * A simple linked list that pools its nodes. This is a highly
 * specialized class used in a couple of 2D scene graph classes. 
 * I wouldn't use it if i was you :)
 * 
 * @author mzechner
 *
 */
public class PooledLinkedList<T>
{	
	static final class Item<T>
	{
		public T payload;
		public Item<T> next;
		public Item<T> prev;
	}
	
	private Item<T> head;
	private Item<T> tail;
	private Item<T> iter;
	private Item<T> curr;
	private int size = 0;
	
	private final Pool<Item<T>> pool;
	
	public PooledLinkedList( int maxPoolSize )
	{
		this.pool = new Pool<PooledLinkedList.Item<T>>( new PoolObjectFactory<PooledLinkedList.Item<T>>() {

			@Override
			public PooledLinkedList.Item<T> createObject() {
				return new Item<T>( );
			}
		} , maxPoolSize );
	}
	
	public void add( T object )
	{
		Item<T> item = pool.newObject();
		item.payload = object;
		item.next = null;
		item.prev = null;
		
		if( head == null )
		{
			head = item;
			tail = item;
			size++;
			return;
		}
		
		item.prev = tail;
		tail.next = item;
		tail = item;
		size++;
	}
	
	/**
	 * Starts iterating over the lists items
	 */
	public void iter( )
	{
		iter = head;
	}
	
	/**
	 * Gets the next item in the list
	 * 
	 * @return the next item in the list or null if there are no more items
	 */
	public T next( )
	{
		if( iter == null )
			return null;
		
		T payload = iter.payload;
		curr = iter;
		iter = iter.next;
		return payload;
	}
	
	/**
	 * Removs the current list item based on the
	 * iterator position.
	 */
	public void remove( )
	{
		if( curr == null )
			return;
		
		size--;
		pool.free( curr );
		
		Item<T> c = curr;
		Item<T> n = curr.next;
		Item<T> p = curr.prev;
		curr = null;
		
		if( size == 0 )
		{
			head = null;
			tail = null;
			return;
		}
		
		if( c == head )
		{
			n.prev = null;
			head = n;
			return;
		}
		
		if( c == tail )
		{
			p.next = null;
			tail = p;
			return;
		}
		
		p.next = n;
		n.prev = p;
	}
	
	public static void main( String[] argv )
	{
		PooledLinkedList<Integer> list = new PooledLinkedList<Integer>( 10 );
		
		list.add( 1 );
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );
		list.iter();
		list.next();
		list.next();
		list.remove();
		list.next();
		list.next();
		list.remove();
		
		list.iter();
		Integer v = null;
		while( ( v = list.next() ) != null )
			System.out.println( v );
		
		list.iter();
		list.next();
		list.next();
		list.remove();
		
		list.iter();
		list.next();
		list.remove();
	}
}
