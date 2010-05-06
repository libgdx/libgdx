package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

/**
 * This holds the mass data computed for a shape.
 * @author mzechner
 *
 */
public class MassData 
{
	/** The mass of the shape, usually in kilograms. **/
	public float mass;

	/** The position of the shape's centroid relative to the shape's origin. **/
	public final Vector2 center = new Vector2();

	/** The rotational inertia of the shape about the local origin. **/
	public float I;
}
