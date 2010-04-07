/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Vector3;

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
	public final Vector3 a = new Vector3( );
	
	/** the ending position **/
	public final Vector3 b = new Vector3( );
	
	/**
	 * Constructs a new Segment from the two points
	 * given.
	 * 
	 * @param a the first point
	 * @param b the second point
	 */
	public Segment( Vector3 a, Vector3 b )
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
