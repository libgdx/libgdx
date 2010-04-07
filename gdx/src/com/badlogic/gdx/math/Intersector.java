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
package com.badlogic.gdx.math;

import java.util.List;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Class offering various static methods for intersection testing between
 * different geometric objects.
 * @author badlogicgames@gmail.com
 *
 */
public final class Intersector 
{	
	/**
	 * Returns the lowest positive root of the quadric equation given
	 * by a* x * x + b * x + c = 0. If no solution is given Float.Nan
	 * is returned.
	 * 
	 * @param a the first coefficient of the quadric equation
	 * @param b the second coefficient of the quadric equation
	 * @param c the third coefficient of the quadric equation
	 * @return the lowest positive root or Float.Nan
	 */
	public static float getLowestPositiveRoot( float a, float b, float c )
	{
		float det = b * b - 4 * a * c;
		if( det < 0 )
			return Float.NaN;
		
		float sqrtD = (float)Math.sqrt( det );
		float invA = 1 / (2 * a);
		float r1 = (-b - sqrtD) * invA;
		float r2 = (-b + sqrtD) * invA;
		
		if( r1 > r2 )
		{
			float tmp = r2;
			r2 = r1;
			r1 = tmp;
		}
		
		if( r1 > 0 )
			return r1;
		
		if( r2 > 0 )
			return r2;
		
		return Float.NaN;
	}
	
	/**
	 * Returns whether the given point is inside the triangle. This assumes
	 * that the point is on the plane of the triangle. No check is performed
	 * that this is the case.
	 * 
	 * @param point the point
	 * @param t1 the first vertex of the triangle
	 * @param t2 the second vertex of the triangle
	 * @param t3 the third vertex of the triangle
	 * @return whether the point is in the triangle
	 */
	private final static Vector3 v0 = new Vector3( );
	private final static Vector3 v1 = new Vector3( );
	private final static Vector3 v2 = new Vector3( );
		
	public static boolean isPointInTriangle( Vector3 point, Vector3 t1, Vector3 t2, Vector3 t3 )
	{
//		v0.set( t3 ).sub( t1 );
//		v1.set( t2 ).sub( t1 );
//		v2.set( point ).sub( t1 );
//
//		float dot00 = v0.dot( v0 );
//		float dot01 = v0.dot( v1 );
//		float dot02 = v0.dot( v2 );
//		float dot11 = v1.dot( v1 );
//		float dot12 = v1.dot( v2 );
//
//		float denom = dot00 * dot11 - dot01 * dot01;
//		if( denom == 0 )
//			return false;
//
//		float u = (dot11 * dot02 - dot01 * dot12) / denom;
//		float v = (dot00 * dot12 - dot01 * dot02) / denom;
//
//		if( u >= 0 && v >= 0 && u + v <= 1 )		
//			return true;		
//		else		
//			return false;	
		
		v0.set(t1).sub(point);
		v1.set(t2).sub(point);
		v2.set(t3).sub(point);
		
		float ab = v0.dot(v1);
		float ac = v0.dot(v2);
		float bc = v1.dot(v2);
		float cc = v2.dot(v2);
		
		if( bc * ac - cc * ab < 0 ) return false;
		float bb = v1.dot(v1);
		if( ab * bc - ac * bb < 0 ) return false;
		return true;
	}
	
	public static boolean intersectSegmentPlane( Vector3 start, Vector3 end, Plane plane, Vector3 intersection )
	{	
		Vector3 dir = end.tmp().sub(start);
		float denom = dir.dot( plane.getNormal() );		
		float t = -( start.dot(plane.getNormal()) + plane.getD() ) / denom;
		if( t < 0 || t > 1 )
			return false;

		intersection.set( start ).add( dir.mul(t) );
		return true;		
	}
	
	/**
	 * Checks wheter the given point is in the polygon. Only the
	 * x and y coordinates of the provided {@link Vector3}s are used.
	 * 
	 * @param polygon The polygon vertices
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @return Wheter the point is in the polygon
	 */
	public static boolean isPointInPolygon( List<Vector2> polygon, Vector2 point )
	{
		
		int j = polygon.size() - 1;
		boolean oddNodes = false;
		for( int i = 0; i < polygon.size(); i++ )
		{
			if( (polygon.get(i).y < point.y && polygon.get(j).y >= point.y ) ||
				(polygon.get(j).y < point.y && polygon.get(i).y >= point.y ) )
				{
					if( polygon.get(i).x + (point.y - polygon.get(i).y)/(polygon.get(j).y - polygon.get(i).y)*(polygon.get(j).x-polygon.get(i).x)<point.x )
					{
						oddNodes = !oddNodes;
					}
				}
			j = i;
		}
		
		return oddNodes;
	}
	
	/**
	 * Returns the distance between the given line segment and point.
	 * @param start The line start point
	 * @param end The line end point
	 * @param point The point
	 * 
	 * @return The distance between the line segment and the point.
	 */	
	public static float distanceLinePoint( Vector2 start, Vector2 end, Vector2 point )
	{
		tmp.set( end.x, end.y, 0 ).sub(start.x, start.y, 0);
		float l = tmp.len();
		tmp2.set(start.x, start.y, 0).sub(point.x, point.y, 0);
		return tmp.crs(tmp2).len() / l;
	}
	
	/**
	 * Returns wheter the given line segment intersects the given
	 * circle. 
	 * 
	 * @param start The start point of the line segment
	 * @param end The end point of the line segment
	 * @param center The center of the circle
	 * @param squareRadius The squared radius of the circle
	 * @return Wheter the line segment and the circle intersect
	 */
	public static boolean intersectSegmentCircle( Vector2 start, Vector2 end, Vector2 center, float squareRadius )
	{
		float u = (center.x - start.x) * ( end.x - start.x ) + ( center.y - start.y ) * ( end.y - start.y );
		float d = start.dst( end );
		u /= ( d * d );
		if( u < 0 || u > 1 )
			return false;
		tmp.set( end.x, end.y, 0 ).sub(start.x, start.y, 0);
		tmp2.set(start.x, start.y, 0).add( tmp.mul(u) );
		if( tmp2.dst2( center.x, center.y, 0 ) < squareRadius )
			return true;
		else
			return false;
	}
	
	/**
	 * Checks wheter the line segment and the circle intersect and returns by 
	 * how much and in what direction the line has to move away from the circle to not intersect.
	 * 
	 * @param start The line segment starting point
	 * @param end The line segment end point
	 * @param point The center of the circle
	 * @param radius The radius of the circle
	 * @param displacement The displacement vector set by the method having unit length
	 * @return The displacement or Float.POSITIVE_INFINITY if no intersection is present 
	 */
	public static float intersectSegmentCircleDisplace( Vector2 start, Vector2 end, Vector2 point, float radius, Vector2 displacement )
	{
		float u = (point.x - start.x) * ( end.x - start.x ) + ( point.y - start.y ) * ( end.y - start.y );
		float d = start.dst( end );
		u /= ( d * d );
		if( u < 0 || u > 1 )
			return Float.POSITIVE_INFINITY;
		tmp.set( end.x, end.y, 0 ).sub(start.x, start.y, 0);
		tmp2.set(start.x, start.y, 0).add( tmp.mul(u) );
		d = tmp2.dst(point.x, point.y, 0 );
		if( d < radius )
		{
			displacement.set(point).sub(tmp2.x, tmp2.y).nor();
			return d;
		}
		else
			return Float.POSITIVE_INFINITY;
	}
	
	/**
	 * Intersects a {@link Ray} and a {@link Plane}. The intersection point
	 * is stored in intersection in case an intersection is present.
	 * 
	 * @param ray The ray
	 * @param plane The plane
	 * @param intersection The vector the intersection point is written to
	 * @return Wheter an intersection is present.
	 */
	public static boolean intersectRayPlane( Ray ray, Plane plane, Vector3 intersection )
	{
		float denom = ray.direction.dot( plane.getNormal() );
		if( denom != 0 )
		{
			float t = -( ray.origin.dot(plane.getNormal()) + plane.getD() ) / denom;
			if( t < 0 )
				return false;

			intersection.set( ray.origin ).add( ray.direction.tmp().mul(t) );
			return true;
		}
		else		
			if( plane.testPoint( ray.origin ) == Plane.PlaneSide.OnPlane )
			{
				intersection.set( ray.origin );
				return true;
			}
			else
				return false;		
	}

	/**
	 * Intersect a {@link Ray} and a triangle, returning the intersection point
	 * in intersection.
	 * 
	 * @param ray The ray
	 * @param t1 The first vertex of the triangle
	 * @param t2 The second vertex of the triangle
	 * @param t3 The third vertex of the triangle
	 * @param intersection The intersection point
	 * @return True in case an intersection is present.
	 */
	public static boolean intersectRayTriangle( Ray ray, Vector3 t1, Vector3 t2, Vector3 t3, Vector3 intersection )
	{       
		Plane p = new Plane( t1, t2, t3 );		
		Vector3 i = new Vector3();
		if( !intersectRayPlane( ray, p, i ) )
			return false;

		v0.set( t3 ).sub( t1 );
		v1.set( t2 ).sub( t1 );
		v2.set( i ).sub( t1 );

		float dot00 = v0.dot( v0 );
		float dot01 = v0.dot( v1 );
		float dot02 = v0.dot( v2 );
		float dot11 = v1.dot( v1 );
		float dot12 = v1.dot( v2 );

		float denom = dot00 * dot11 - dot01 * dot01;
		if( denom == 0 )
			return false;

		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;

		if( u >= 0 && v >= 0 && u + v <= 1 )
		{
			intersection.set( i );
			return true;
		}
		else
		{
			return false;
		}

	}

	/**
	 * Intersects a {@link Ray} and a sphere, returning the intersection
	 * point in intersection.
	 * 
	 * @param ray The ray
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @param intersection The intersection point
	 * @return Wheter an interesection is present.
	 */
	public static boolean intersectRaySphere( Ray ray, Vector3 center, float radius, Vector3 intersection )
	{
		Vector3 dir = ray.direction.cpy().nor();
		Vector3 start = ray.origin.cpy();
		float b = 2 * ( dir.dot( start.tmp().sub( center ) ) );
		float c = start.dist2( center ) - radius * radius;
		float disc = b * b - 4 * c;
		if( disc < 0 )
			return false;

		// compute q as described above
		float distSqrt = (float)Math.sqrt(disc);
		float q;
		if (b < 0)
			q = (-b - distSqrt)/2.0f;
		else
			q = (-b + distSqrt)/2.0f;

		// compute t0 and t1
		float t0 = q / 1;
		float t1 = c / q;

		// make sure t0 is smaller than t1
		if (t0 > t1)
		{
			// if t0 is bigger than t1 swap them around
			float temp = t0;
			t0 = t1;
			t1 = temp;
		}

		// if t1 is less than zero, the object is in the ray's negative direction
		// and consequently the ray misses the sphere
		if (t1 < 0)
			return false;

		// if t0 is less than zero, the intersection point is at t1
		if (t0 < 0)
		{
			if( intersection != null )
				intersection.set( start ).add( dir.tmp().mul( t1 ) );
			return true;
		}
		// else the intersection point is at t0
		else
		{
			if( intersection != null )
				intersection.set( start ).add( dir.tmp().mul( t0 ) );
			return true;
		}
	}

	/**
	 * Quick check wheter the given {@link Ray} and {@link BoundingBox}
	 * intersect.
	 * 
	 * @param ray The ray
	 * @param bounds The bounding box
	 * @return Wheter the ray and the bounding box intersect.
	 */
	public static boolean intersectRayBoundsFast( Ray ray, BoundingBox bounds )
	{
		float t_x_min, t_x_max;
		float t_y_min, t_y_max;
		float t_z_min, t_z_max;
		float div_x, div_y, div_z;

		div_x = 1 / ray.direction.x;
		div_y = 1 / ray.direction.y;
		div_z = 1 / ray.direction.z;

		if (div_x >= 0)
		{
			t_x_min = (bounds.getMin().x - ray.origin.x) * div_x;
			t_x_max = (bounds.getMax().x - ray.origin.x) * div_x;
		}
		else
		{
			t_x_min = (bounds.getMax().x - ray.origin.x) * div_x;
			t_x_max = (bounds.getMin().x - ray.origin.x) * div_x;
		}

		if (div_y >= 0)
		{
			t_y_min = (bounds.getMin().y - ray.origin.y) * div_y;
			t_y_max = (bounds.getMax().y - ray.origin.y) * div_y;
		}
		else
		{
			t_y_min = (bounds.getMax().y - ray.origin.y) * div_y;
			t_y_max = (bounds.getMin().y - ray.origin.y) * div_y;
		}

		if (t_x_min > t_y_max || (t_y_min > t_x_max))
			return false;

		if (t_y_min > t_x_min)
			t_x_min = t_y_min;
		if (t_y_max < t_x_max)
			t_x_max = t_y_max;

		if (div_z >= 0)
		{
			t_z_min = (bounds.getMin().z - ray.origin.z) * div_z;
			t_z_max = (bounds.getMax().z - ray.origin.z) * div_z;
		}
		else
		{
			t_z_min = (bounds.getMax().z - ray.origin.z) * div_z;
			t_z_max = (bounds.getMin().z - ray.origin.z) * div_z;
		}

		if ((t_x_min > t_z_max) || (t_z_min > t_x_max))
			return false;
		if (t_z_min > t_x_min)
			t_x_min = t_z_min;
		if (t_z_max < t_x_max)
			t_x_max = t_z_max;

		return ((t_x_min < 1) && (t_x_max > 0));
	}

	static Vector3 tmp = new Vector3();
	static Vector3 best = new Vector3();
	static Vector3 tmp1 = new Vector3();
	static Vector3 tmp2 = new Vector3();
	static Vector3 tmp3 = new Vector3();

	/**
	 * Intersects the given ray with list of triangles. Returns the nearest
	 * intersection point in intersection
	 *  
	 * @param ray The ray
	 * @param triangles The triangles, each succesive 3 elements from a vertex
	 * @param intersection The nearest intersection point
	 * @return Wheter the ray and the triangles intersect.
	 */
	public static boolean intersectRayTriangles( Ray ray, float[] triangles, Vector3 intersection )
	{				
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if( ( triangles.length / 3 ) % 3 != 0 )
			throw new RuntimeException( "triangle list size is not a multiple of 3" );

		for( int i = 0; i < triangles.length - 6; i+=9 )
		{
			boolean result = intersectRayTriangle( ray, 
					tmp1.set( triangles[i], triangles[i+1], triangles[i+2] ),
					tmp2.set( triangles[i+3], triangles[i+4], triangles[i+5] ),
					tmp3.set( triangles[i+6], triangles[i+7], triangles[i+8] ),												   
					tmp);

			if( result == true )
			{
				float dist = ray.origin.tmp().sub( tmp ).len();
				if( dist < min_dist )
				{
					min_dist = dist;					
					best.set( tmp );
					hit = true;
				}
			}
		}

		if( hit == false )
			return false;
		else
		{
			if( intersection != null )
				intersection.set( best );
			return true;
		}
	}

	/**
	 * Intersects the given ray with list of triangles. Returns the nearest
	 * intersection point in intersection
	 *  
	 * @param ray The ray
	 * @param triangles The triangles
	 * @param intersection The nearest intersection point
	 * @return Wheter the ray and the triangles intersect.
	 */
	public static boolean intersectRayTriangles( Ray ray, List<Vector3> triangles, Vector3 intersection )
	{
		Vector3 tmp = new Vector3();
		Vector3 best = null;
		float min_dist = Float.MAX_VALUE;

		if( triangles.size() % 3 != 0 )
			throw new RuntimeException( "triangle list size is not a multiple of 3" );

		for( int i = 0; i < triangles.size() - 2; i+=3 )
		{
			boolean result = intersectRayTriangle( ray, 
					triangles.get(i), 
					triangles.get(i+1),
					triangles.get(i+2), 
					tmp);

			if( result == true )
			{
				float dist = ray.origin.tmp().sub( tmp ).len();
				if( dist < min_dist )
				{
					min_dist = dist;
					if( best == null )
						best = new Vector3();
					best.set( tmp );
				}
			}
		}

		if( best == null )
			return false;
		else
		{
			if( intersection != null )
				intersection.set( best );
			return true;
		}
	}

	/**
	 * Returns wheter the two rectangles intersect
	 * @param a The first rectangle
	 * @param b The second rectangle
	 * @return Wheter the two rectangles intersect
	 */
	public static boolean intersectRectangles(Rectangle a, Rectangle b)
	{		
		return !(a.getX() > b.getX() + b.getWidth() || a.getX() + a.getWidth() < b.getX() ||
				a.getY() > b.getY() + b.getHeight() || a.getY() + a.getHeight() < b.getY());
	}	

	/**
	 * Intersects the two lines and returns the intersection point
	 * in intersection.
	 * 
	 * @param p1 The first point of the first line
	 * @param p2 The second point of the first line
	 * @param p3 The first point of the second line
	 * @param p4 The second point of the second line
	 * @param intersection The intersection point
	 * @return Wheter the two lines intersect
	 */
	public static boolean intersectLines( Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, Vector2 intersection )
	{
		float  x1 = p1.x, y1 = p1.y,
		x2 = p2.x, y2 = p2.y,
		x3 = p3.x, y3 = p3.y,
		x4 = p4.x, y4 = p4.y;

    	float det1 = det(x1, y1, x2, y2);
    	float det2 = det(x3, y3, x4, y4);
    	float det3 = det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
    	
		float x = det(det1, x1 - x2,
				det2, x3 - x4)/
				det3;
		float y = det(det1, y1 - y2,
				det2, y3 - y4)/
				det3;
		
		intersection.x = x;
		intersection.y = y;
		
		return true;
	}
	

	/**
	 * Intersects the two line segments and returns the intersection point
	 * in intersection.
	 * 
	 * @param p1 The first point of the first line segment
	 * @param p2 The second point of the first line segment
	 * @param p3 The first point of the second line segment
	 * @param p4 The second point of the second line segment 
	 * @param intersection The intersection point
	 * @return Wheter the two line segments intersect
	 */
	public static boolean intersectSegments( Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, Vector2 intersection )
	{
		float  x1 = p1.x, y1 = p1.y,
		x2 = p2.x, y2 = p2.y,
		x3 = p3.x, y3 = p3.y,
		x4 = p4.x, y4 = p4.y;
		
		float d = (y4-y3)*(x2-x1) - (x4-x3)*(y2-y1);
		if( d == 0 )
			return false;
		
		float ua = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / d;
		float ub = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / d;
		
		if( ua < 0 || ua > 1 )
			return false;
		if( ub < 0 || ub > 1 )
			return false;
		
		intersection.set( x1 + (x2-x1)*ua, y1 + (y2-y1)*ua );
		return true;
	}
	
	static float det(float a, float b, float c, float d)
	{
		return a * d - b * c;
	}
	
	static double detd(double a, double b, double c, double d)
	{
		return a * d - b * c;
	}
}
