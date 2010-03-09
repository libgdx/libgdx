/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.math;

/**
 * Encapsulates a ray having a starting position and a unit length direction.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class Ray 
{
	protected Vector start = new Vector();
	protected Vector dir = new Vector();	
	
	/**
	 * Constructor, sets the starting position of the ray
	 * and the direction. 
	 * 
	 * @param start The starting position
	 * @param dir The direction
	 */
	public Ray( Vector start, Vector dir )
	{
		this.start.set(start);
		this.dir.set(dir).nor();
	}
	
	/**
	 * @return a copy of this ray.
	 */
	public Ray cpy()
	{
		return new Ray( this.start, this.dir );
	}
	
	/**
	 * Returns and endpoint given the distance. This is 
	 * calculated as startpoint + distance * direction.
	 * 
	 * @param distance The distance from the end point to the start point.
	 * @return The end point
	 */
	public Vector getEndPoint( float distance )
	{
		return new Vector( start ).add( dir.tmp().mul( distance ) );
	}
	
	/**
	 * @return the start point
	 */
	public Vector getStartPoint( )
	{
		return start;
	}
	
	/**
	 * @return the direction
	 */
	public Vector getDirection( )
	{
		return dir;
	}
	
	static Vector tmp = new Vector( );
	
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
		tmp.set( start ).add( dir );
		tmp.mul( matrix );
		start.mul( matrix );
		dir.set( tmp.sub( start ) );
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		return "ray [" + start + ":" + dir + "]";
	}

	/**
	 * Sets the starting position and the direction 
	 * of this ray.
	 * 
	 * @param start The starting position
	 * @param dir The direction
	 * @return this ray for chaining
	 */
	public Ray set( Vector start, Vector dir )
	{
		this.start.set(start);
		this.dir.set(dir);
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
		this.start.set( x, y, z );
		this.dir.set( dx, dy, dz );
		return this;
	}
	
	/**
	 * Sets the starting position and direction from the given ray
	 * 
	 * @param ray The ray
	 * @return This ray for chaining
	 */
	public Ray set(Ray ray) {

		this.start.set(ray.start);
		this.dir.set(ray.dir);
		return this;
	}
}
