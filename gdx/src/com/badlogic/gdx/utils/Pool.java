package com.badlogic.gdx.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A pool implementation used for object instances
 * that should get reused instead of being collected
 * by the garbage collector. To increase the speed of 
 * this class no method is provided to free individual
 * object instances. Note that you should not hold on 
 * to the references of objects from this Pool once
 * they've been marked as free by calling Pool.freeAll().
 * 
 * @author mzechner
 *
 * @param <T> the type
 */
public class Pool <T> 
{	
	/**
	 * Interface for an Object Factory to be used
	 * with this Pool 
	 * 
	 * @author mzechner
	 *
	 * @param <T> the type
	 */
	public interface PoolObjectFactory<T>
	{
		public T createObject( );
	}
	
	/** the list of free objects **/
	private final List<T> freeObjects = new ArrayList( );
	/** the list of used objects **/
	private final List<T> usedObjects = new ArrayList( );
	/** the factory **/
	private final PoolObjectFactory<T> factory;
	
	public Pool( PoolObjectFactory<T> factory )
	{
		this.factory = factory;
	}
	
	/**
	 * Creates a new object either by taking it from the
	 * free object pool or by creating a new one if there
	 * are no free objects yet. 
	 * @return the object
	 */
	public T newObject( )
	{
		T object = null;
		
		if( freeObjects.size() == 0 )		
			object = factory.createObject( );
		else
			object = freeObjects.remove( freeObjects.size() - 1 );
		
		usedObjects.add( object );
		return object;		
	}
	
	/**
	 * Frees all objects created by this Pool
	 */
	public void freeAll( )
	{
		freeObjects.addAll( usedObjects );
		usedObjects.clear();
	}
}
