package com.badlogic.gdx;

/**
 * A DestroyListener is called when the {@link Application}
 * is closing. This allows you to save any states you want
 * to save. The method is potentially not called from within
 * the rendering thread so care has to be taken.
 * 
 * @author mzechner
 *
 */
public interface DestroyListener 
{
	/**
	 * Called when the {@link Application} is destroyed
	 * @param app the Application
	 */
	public void destroy( Application app );
}
