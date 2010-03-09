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
 * Encapsulates a 3D sphere with a center and a radius
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class Sphere 
{
	private float radius;
	private final Vector center;
	
	/**
	 * Constructs a sphere with the given center and radius
	 * @param center The center
	 * @param radius The radius
	 */
	public Sphere( Vector center, float radius )
	{
		this.center = new Vector( center );
		this.radius = radius;		
	}
	
	/**
	 * @return the center of the sphere
	 */
	public Vector getCenter( )
	{
		return center;
	}
	
	/**
	 * @return the radius of the sphere
	 */
	public float getRadius( )
	{
		return radius;
	}
	
	/**
	 * Sets the radius of the sphere
	 * 
	 * @param radius the radius
	 */
	public void setRadius( float radius )
	{
		this.radius = radius;
	}
}
