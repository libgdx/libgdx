package com.badlogic.gdx.physics.box2d;

/**
 * Callback class for AABB queries. 
 */
public interface QueryCallback 
{
	/**
	 *  Called for each fixture found in the query AABB.
	 * @return false to terminate the query.
	 */
	public boolean reportFixture(Fixture fixture);
}
