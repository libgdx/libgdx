package com.badlogic.gdx.audio;

/**
 * A Music instance represents a streamed audio file which is played back. The interface
 * supports setting the playback position, pausing and resuming and so on. When you are
 * done with using the Music instance you have to dispose it via the {@link Music.dispose()}
 * method.
 * @author mzechner
 *
 */
public interface Music 
{
	
	
	/**
	 * Needs to be called when the Music 
	 */
	public void dispose( );	
}
