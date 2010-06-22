package com.badlogic.gdxinvaders.screens;

import com.badlogic.gdx.Application;

/**
 * Interface for a game screen, e.g. main menu, game loop,
 * game over screen and so on.
 * @author mzechner
 *
 */
public interface Screen 
{
	/**
	 * Called when the screen should update itself, e.g. continue a
	 * simulation etc.
	 * 
	 * @param app the Application
	 */
	public void update( Application app );
	
	/**
	 * Called when a screen should render itself
	 * @param app
	 */
	public void render( Application app );
	
	/**
	 * Called by GdxInvaders to check whether the screen is done.
	 * 
	 * @return true when the screen is done, false otherwise
	 */
	public boolean isDone( );
	
	/**
	 * Cleans up all resources of the screen, e.g. meshes, textures etc.
	 */
	public void dispose( );
}
