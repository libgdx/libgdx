package com.badlogic.gdx.scenes.scene2d;

public interface Action 
{
	/**
	 * Apply the action.
	 * 
	 * @param delta delta time in seconds
	 */
	public void act( float delta );
	
	/**
	 * @return whether the action is done or not
	 */
	public boolean isDone( );
}
