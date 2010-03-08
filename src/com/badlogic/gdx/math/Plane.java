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

import java.io.Serializable;

public class Plane implements Serializable 
{	
	private static final long serialVersionUID = 7471550250795948609L;

	enum Intersection
	{
		OnPlane,
		Back,
		Front
	}
	
	protected final Vector normal = new Vector();
	protected float d = 0;
	
	public Plane( )
	{
		
	}
	
	public Plane( Vector normal, float d )
	{
		this.normal.set( normal ).nor();
		this.d = d;
	}
	
	public Plane( Vector normal, Vector point )
	{
		this.normal.set(normal).nor();
		this.d = -this.normal.dot( point );
	}
	
	public Plane( Vector a, Vector b, Vector c )
	{
		Vector l = a.tmp().sub(b);
		Vector r = b.tmp2().sub(c);
		Vector nor = l.crs( r ).nor();
		normal.set( nor );
		d = -a.dot( nor );
	}
	
	public void set( Vector a, Vector b, Vector c )
	{
		Vector l = a.tmp().sub(b);
		Vector r = b.tmp2().sub(c);
		Vector nor = l.crs( r ).nor();
		normal.set( nor );
		d = -a.dot( nor );
	}
	
	public float distance( Vector p )
	{
		return normal.dot( p ) + d;
	}
	
	public Intersection testPoint( Vector p )
	{
		float dist = normal.dot( p ) + d;
		
		if( dist == 0 )
			return Intersection.OnPlane;
		else
			if( dist < 0 )
				return Intersection.Back;
			else
				return Intersection.Front;
	}
	
	public Vector getNormal( )
	{
		return normal;
	}
	
	public float getD( )
	{
		return d;
	}
	
	public static void main( String[] argv )
	{
		Vector v1 = new Vector( 0, 0, 1 );
		Vector v2 = new Vector( 1, 0, 0 );
		Vector v3 = new Vector( 1, 0, 1 );		
		
		Vector a = new Vector( v2 ).sub( v1 );
		Vector b = new Vector( v3 ).sub( v1 );
		System.out.println( a.crs( b ).nor() );					
		
//		Plane p1 = new Plane( new Vector( 0, 1, 0 ), 0 );
//		System.out.println( p1.testPoint( new Vector( 0, 0, 0 ) ));
//		System.out.println( p1.testPoint( new Vector( 1, 230, 23 ) ) );
//		System.out.println( p1.testPoint( new Vector( 0, -239, 34 ) ) );
	}
}
