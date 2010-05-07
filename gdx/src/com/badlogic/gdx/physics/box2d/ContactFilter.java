package com.badlogic.gdx.physics.box2d;

/**
 * Implement this class to provide collision filtering. In other words, you can implement
 * this class if you want finer control over contact creation.
 * @author mzechner
 *
 */
public interface ContactFilter 
{
	boolean shouldCollide( Fixture fixtureA, Fixture fixtureB );
}
