package com.badlogic.gdx;

/**
 * A sound is a short audio clip that can be played numerous times.
 * it's completely loaded into memory.
 * 
 * @author mzechner
 *
 */
public interface Sound {
	public void play( );
	
	public void dispose( );
}
