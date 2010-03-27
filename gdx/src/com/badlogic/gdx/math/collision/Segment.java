package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Vector;

/**
 * A Segment is a line in 3-space having a staring
 * and an ending position.
 * 
 * @author mzechner
 *
 */
public class Segment 
{
	/** the starting position **/
	public final Vector a = new Vector( );
	
	/** the ending position **/
	public final Vector b = new Vector( );
	
	/**
	 * Constructs a new Segment from the two points
	 * given.
	 * 
	 * @param a the first point
	 * @param b the second point
	 */
	public Segment( Vector a, Vector b )
	{
		this.a.set(a);
		this.b.set(b);
	}

	/**
	 * Constructs a new Segment from the two points given.
	 * @param aX the x-coordinate of the first point
	 * @param aY the y-coordinate of the first point
	 * @param aZ the z-coordinate of the first point
	 * @param bX the x-coordinate of the second point
	 * @param bY the y-coordinate of the second point
	 * @param bZ the z-coordinate of the second point
	 */
	public Segment( float aX, float aY, float aZ, float bX, float bY, float bZ )
	{
		this.a.set( aX, aY, aZ );
		this.b.set( bX, bY, bZ );
	}
}
