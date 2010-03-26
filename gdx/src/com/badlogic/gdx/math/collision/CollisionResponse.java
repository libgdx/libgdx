package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Vector;

/**
 * A collision response is responsible for altering the 
 * position and velocity of an {@link EllipsoidCollider}
 * in case a collision occured. This can implement sliding
 * collision response or a bouncing collision response for
 * example.
 * 
 * @author mzechner
 *
 */
public interface CollisionResponse 
{
	/**
	 * Responds to a collision defined by the {@link CollisionPacket}, modifying
	 * the given position and velocity accordingly. The displacementDistance is the
	 * distance a colliding object is displaced from the colliding plane. This value
	 * dependent on the scale of your world and is usually very small (e.g. 0.0001 for
	 * 1m per unit).
	 * 
	 * @param position the position
	 * @param velocity the velocity
	 * @param displacementDistance the displacement distance
	 */
	public void respond( CollisionPacket packet, Vector position, Vector velocity, float displacementDistance );
}
