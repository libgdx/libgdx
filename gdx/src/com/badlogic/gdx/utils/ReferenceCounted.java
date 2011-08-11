package com.badlogic.gdx.utils;

/**
 * Marker interface for reference counted objects. An object implementing
 * this interface also implements the {@link Disposable} interface. The
 * {@link #dispose()} method has to adhere to the reference count in the
 * following way:
 * 
 * <ul>
 * <li>decrease the reference count by 1</li>
 * <li>if the reference count is <= 0 actually dispose the resource</li>
 * <li>otherwise ignore the request for disposal.</li>
 * </ul>
 * @author mzechner
 *
 */
public interface ReferenceCounted extends Disposable {
	/**
	 * Increases the reference count by one. The {@link #dispose()} method
	 * will not do anything unless the reference count is <= 0.
	 */
	public void incRefCount();
	
	/**
	 * Decreases the reference count. Usually you want to call dispose
	 * instead.
	 */
	public void decRefCount();
	
	/**
	 * @return the reference count.
	 */
	public int getRefCount();
}
