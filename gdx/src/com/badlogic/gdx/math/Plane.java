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
 * A plane defined via a unit length normal and the distance from the
 * origin, as you learned in your math class.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class Plane 
{		
	/**
	 * Enum specifying on which side a point lies respective
	 * to the plane and it's normal. {@link PlaneSide.Front}
	 * is the side to which the normal points.
	 * 
	 * @author mzechner
	 *
	 */
	enum PlaneSide
	{
		OnPlane,
		Back,
		Front
	}
	
	protected final Vector normal = new Vector();
	protected float d = 0;
	
	/**
	 * Constructs a new plane based on the normal and distance
	 * to the origin.
	 * 
	 * @param normal The plane normal
	 * @param d The distance to the origin
	 */
	public Plane( Vector normal, float d )
	{
		this.normal.set( normal ).nor();
		this.d = d;
	}
	
	/**
	 * Constructs a new plane based on the normal and a 
	 * point on the plane.
	 * 
	 * @param normal The normal
	 * @param point The point on the plane
	 */
	public Plane( Vector normal, Vector point )
	{
		this.normal.set(normal).nor();
		this.d = -this.normal.dot( point );
	}
	
	/**
	 * Constructs a new plane out of the three given points
	 * that are considered to be on the plane. The normal
	 * is calculated via a cross product between (point1-point2)x(point2-point3)
	 * 
	 * @param point1 The first point 
	 * @param point2 The second point
	 * @param point3 The third point
	 */
	public Plane( Vector point1, Vector point2, Vector point3 )
	{
		set( point1, point2, point3 );
	}
	
	/**
	 * Sets the plane normal and distance to the origin based
	 * on the three given points which are considered to be
	 * on the plane. The normal is calculated via a cross product
	 * between (point1-point2)x(point2-point3)
	 * 
	 * @param point1
	 * @param point2
	 * @param point3
	 */
	public void set( Vector point1, Vector point2, Vector point3 )
	{
		Vector l = point1.tmp().sub(point2);
		Vector r = point2.tmp2().sub(point3);
		Vector nor = l.crs( r ).nor();
		normal.set( nor );
		d = -point1.dot( nor );
	}
	
	/**
	 * Calculates the shortest distance between the plane and the
	 * given point.
	 * 
	 * @param point The point
	 * @return the shortest distance between the plane and the point
	 */
	public float distance( Vector point )
	{
		return normal.dot( point ) + d;
	}
	
	/**
	 * Returns on which side the given point lies relative to the 
	 * plane and its normal. PlaneSide.Front refers to the side
	 * the plane normal points to.
	 * 
	 * @param point The point
	 * @return The side the point lies relative to the plane
	 */
	public PlaneSide testPoint( Vector point )
	{
		float dist = normal.dot( point ) + d;
		
		if( dist == 0 )
			return PlaneSide.OnPlane;
		else
			if( dist < 0 )
				return PlaneSide.Back;
			else
				return PlaneSide.Front;
	}
	
	/**
	 * @return The normal
	 */
	public Vector getNormal( )
	{
		return normal;
	}
	
	/**
	 * @return The distance to the origin
	 */
	public float getD( )
	{
		return d;
	}	
}
