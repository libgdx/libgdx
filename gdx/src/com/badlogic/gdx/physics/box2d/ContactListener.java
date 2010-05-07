package com.badlogic.gdx.physics.box2d;

public interface ContactListener 
{
	/**
	 * Called when two fixtures begin to touch.
	 */
	public void beginContact( Contact contact );
	
	/**
	 * Called when two fixtures cease to touch.
	 */
	public void endContact( Contact contact );	
}
