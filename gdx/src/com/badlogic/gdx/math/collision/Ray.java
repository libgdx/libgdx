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

import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Vector3;

/**
 * Encapsulates a ray having a starting position and a unit length direction.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class Ray 
{
	public final Vector3 origin = new Vector3();
	public final Vector3 direction = new Vector3();	
	
	/**
	 * Constructor, sets the starting position of the ray
	 * and the direction. 
	 * 
	 * @param origin The starting position
	 * @param direction The direction
	 */
	public Ray( Vector3 origin, Vector3 direction )
	{
		this.origin.set(origin);
		this.direction.set(direction).nor();
	}
	
	/**
	 * @return a copy of this ray.
	 */
	public Ray cpy()
	{
		return new Ray( this.origin, this.direction );
	}
	
	/**
	 * Returns and endpoint given the distance. This is 
	 * calculated as startpoint + distance * direction.
	 * 
	 * @param distance The distance from the end point to the start point.
	 * @return The end point
	 */
	public Vector3 getEndPoint( float distance )
	{
		return new Vector3( origin ).add( direction.tmp().mul( distance ) );
	}
	
	static Vector3 tmp = new Vector3( );
	
	/**
	 * Multiplies the ray by the given matrix. Use
	 * this to transform a ray into another coordinate
	 * system.
	 * 
	 * @param matrix The matrix 
	 * @return This ray for chaining.
	 */
	public Ray mul( Matrix matrix )
	{
		tmp.set( origin ).add( direction );
		tmp.mul( matrix );
		origin.mul( matrix );
		direction.set( tmp.sub( origin ) );
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		return "ray [" + origin + ":" + direction + "]";
	}

	/**
	 * Sets the starting position and the direction 
	 * of this ray.
	 * 
	 * @param origin The starting position
	 * @param direction The direction
	 * @return this ray for chaining
	 */
	public Ray set( Vector3 origin, Vector3 direction )
	{
		this.origin.set(origin);
		this.direction.set(direction);
		return this;
	}
	
	/**
	 * Sets this ray from the given starting position
	 * and direction.
	 * 
	 * @param x The x-component of the starting position
	 * @param y The y-component of the starting position
	 * @param z The z-component of the starting position
	 * @param dx The x-component of the direction
	 * @param dy The y-component of the direction
	 * @param dz The z-component of the direction
	 * @return this ray for chaining
	 */
	public Ray set( float x, float y, float z, float dx, float dy, float dz )
	{
		this.origin.set( x, y, z );
		this.direction.set( dx, dy, dz );
		return this;
	}
	
	/**
	 * Sets the starting position and direction from the given ray
	 * 
	 * @param ray The ray
	 * @return This ray for chaining
	 */
	public Ray set(Ray ray) {

		this.origin.set(ray.origin);
		this.direction.set(ray.direction);
		return this;
	}
}
