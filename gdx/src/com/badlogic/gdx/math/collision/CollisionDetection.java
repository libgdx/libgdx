package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.collision.CollisionPacket.CollisionType;

/**
 * Class holding various static methods to perform collision detection,
 * nearest point queries and intersection tests.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class CollisionDetection 
{
	/** the global epsilon value used for some of the routines in here. set at your own risk **/
	public static float EPSILON = 0.00001f;
	
	/** temporary vectors **/
	static final Vector p1 = new Vector();
	static final Vector p2 = new Vector();
	static final Vector p3 = new Vector();
	
	/** temporary plane **/
	static final Plane plane = new Plane( new Vector(), 0 );
	
	/** number of triangles processed during the last call to collide **/
	private static int processedTriangles;
	
	/** number of early out triangles **/
	private static int earlyOutTriangles;
	
	/** number of backface culled triangles **/
	private static int culledTriangles;
	
	/**
	 * Calculates the closest point on the plain to the given point.
	 * 
	 * @param plane the plane
	 * @param p the point
	 * @param o the closest point on the plain to p (output)
	 */
	public static void closestPointToPlane( Plane plane, Vector p, Vector o )
	{
		float t = plane.normal.dot(p) + plane.d;
		o.set(p).sub(plane.normal.tmp().mul(t) );
	}
	
	/**
	 * Calculates the signed distance from the given point
	 * to the plane. 
	 * 
	 * @param plane the plane
	 * @param p the point
 	 * @return the signed distance
	 */
	public static float signedDistanceToPlane( Plane plane, Vector p )
	{
		return plane.normal.dot(p) + plane.d;
	}
	
	/**
	 * Calculates the closest point on a {@link Segment} to the
	 * given point.
	 * 
	 * @param s the segment
	 * @param p the point
	 * @param o the closest point (output)
	 */
	public static void closestPointToSegment( Segment s, Vector p, Vector o )
	{
		Vector ab = s.b.tmp().sub(s.a);
		float t = ab.dot( p.tmp2().sub(s.a) );
		if( t <= 0 )
		{
			o.set(s.a);
		}
		else
		{
			float denom = ab.dot(ab);
			if( t >= denom )
			{
				o.set( s.b );
			}
			else
			{
				t = t / denom;
				o.set( s.a ).add( ab.mul(t) ) ;
			}
		}
	}
	
	/**
	 * Calculates the closest point on a {@link Ray} to the
	 * given point.
	 * 
	 * @param r the ray
	 * @param p the point
	 * @param o the closest point (output)
	 */
	public static void closestPointToRay( Ray r, Vector p, Vector o )
	{
		Vector ab = r.direction;
		float t = ab.dot( p.tmp2().sub(r.origin) );
		if( t <= 0 )
		{
			o.set(r.origin);
		}
		else
		{
			float denom = ab.dot(ab);
			t = t / denom;
			o.set( r.origin ).add( ab.tmp().mul(t) ) ;
		}
	}
	
	/**
	 * Calculates the closest point on a line to the given point
	 * 
	 * @param a the first point of the line
	 * @param b the second point of the line
	 * @param p the point
 	 * @param o the closest point on the line to p
	 */
	public static void closestPointToLine( Vector a, Vector b, Vector p, Vector o )
	{
		Vector ab = b.tmp().sub(a);
		float t = ab.dot( p.tmp2().sub(a) );
		float denom = ab.dot(ab);
		t = t / denom;
		o.set( a ).add( ab.mul(t) ) ;
	}
	
	/**
	 * Returns the closest point in and on the bounding box to the given point.
	 * If the given point is inside the bounding box the given point is returned.
	 * 
	 * @param b the bounding box
	 * @param p the point
	 * @param o the closest point
	 */
	public static void closestPointToBoundingBox( BoundingBox b, Vector p, Vector o )
	{
		o.set( p );
		if( p.x < b.min.x ) o.x = b.min.x;
		if( p.x > b.max.x ) o.x = b.max.x;
		if( p.y < b.min.y ) o.y = b.min.y;
		if( p.y > b.max.y ) o.y = b.max.y;
		if( p.z < b.min.z ) o.z = b.min.z;
		if( p.z > b.max.z ) o.z = b.max.z;
	}
	
	/**
	 * Returns the squared distance from the point to the bounding box. If the
	 * point is inside the bounding box 0 is returned.
	 * 
	 * @param b the bounding box
	 * @param p the point 
	 * @return the squared distance to the bounding box.
	 */
	public static float squaredDistanceToBoundingBox( BoundingBox b, Vector p )
	{
		float sqDist = 0;
		if( p.x < b.min.x ) sqDist += (b.min.x - p.x) * (b.min.x - p.x);
		if( p.x > b.max.x ) sqDist += (p.x - b.max.x ) * ( p.x - b.max.x );
		
		if( p.y < b.min.y ) sqDist += (b.min.y - p.y) * (b.min.y - p.y);
		if( p.y > b.max.y ) sqDist += (p.y - b.max.y ) * ( p.y - b.max.y );
		
		if( p.z < b.min.z ) sqDist += (b.min.z - p.z) * (b.min.z - p.z);
		if( p.z > b.max.z ) sqDist += (p.z - b.max.z ) * ( p.z - b.max.z );
		
		return sqDist;
	}
	
	/**
	 * Returns whether the given sphere and plane intersect
	 * 
	 * @param s the sphere
	 * @param p the plane
	 * @return whether the sphere and the plane intersect.
	 */
	public static boolean testSpherePlane( Sphere s, Plane p )
	{
		float dist = s.center.dot( p.normal ) - p.d;
		return Math.abs( dist ) <= s.radius;
	}
	
	/**
	 * Returns whether the given bounding box and plane intersect
	 * 
	 * @param b the bounding box
	 * @param p the plane
	 * @return whether the bounding box and the plane intersect
	 */
	static final Vector c = new Vector( );
	static final Vector e = new Vector( );
	public static boolean testBoundingBoxPlane( BoundingBox b, Plane p )
	{
		c.set( b.max ).add( b.min ).mul(0.5f );
		e.set( b.max ).sub( c );
		
		float r = e.x * Math.abs( p.normal.x ) + e.y * Math.abs( p.normal.y ) + e.z * Math.abs( p.normal.z );
		float s = p.normal.dot( c ) - p.d;
		return Math.abs( s ) <= r;
	}
	
	/**
	 * Returns whether the given bounding box and sphere intersect
	 * @param b the bounding box
	 * @param s the sphere
	 * @return whether the bounding box and sphere intersect
	 */
	public static boolean testBoundingBoxSphere( BoundingBox b, Sphere s )
	{
		float sqDist = squaredDistanceToBoundingBox( b, s.center );
		return sqDist <= s.radius * s.radius;
	}
	
	/**
	 * Tests whether the given ray and bounding box intersect
	 * @param b the bounding box
	 * @param r the ray
	 * @return whether the bounding box and ray intersect
	 */
	public static boolean testBoundingBoxRay( BoundingBox b, Ray r )
	{
		float t_x_min, t_x_max;
		float t_y_min, t_y_max;
		float t_z_min, t_z_max;
		float div_x, div_y, div_z;

		div_x = 1 / r.direction.x;
		div_y = 1 / r.direction.y;
		div_z = 1 / r.direction.z;

		if (div_x >= 0)
		{
			t_x_min = (b.getMin().x - r.origin.x) * div_x;
			t_x_max = (b.getMax().x - r.origin.x) * div_x;
		}
		else
		{
			t_x_min = (b.getMax().x - r.origin.x) * div_x;
			t_x_max = (b.getMin().x - r.origin.x) * div_x;
		}

		if (div_y >= 0)
		{
			t_y_min = (b.getMin().y - r.origin.y) * div_y;
			t_y_max = (b.getMax().y - r.origin.y) * div_y;
		}
		else
		{
			t_y_min = (b.getMax().y - r.origin.y) * div_y;
			t_y_max = (b.getMin().y - r.origin.y) * div_y;
		}

		if (t_x_min > t_y_max || (t_y_min > t_x_max))
			return false;

		if (t_y_min > t_x_min)
			t_x_min = t_y_min;
		if (t_y_max < t_x_max)
			t_x_max = t_y_max;

		if (div_z >= 0)
		{
			t_z_min = (b.getMin().z - r.origin.z) * div_z;
			t_z_max = (b.getMax().z - r.origin.z) * div_z;
		}
		else
		{
			t_z_min = (b.getMax().z - r.origin.z) * div_z;
			t_z_max = (b.getMin().z - r.origin.z) * div_z;
		}

		if ((t_x_min > t_z_max) || (t_z_min > t_x_max))
			return false;
		if (t_z_min > t_x_min)
			t_x_min = t_z_min;
		if (t_z_max < t_x_max)
			t_x_max = t_z_max;

		return ((t_x_min < 1) && (t_x_max > 0));
	}
	
	/**
	 * Returns whether the given bounding box and segment intersect
	 * @param b the bounding box
	 * @param s the segment
	 * @return whether the bounding box and segment intersect
	 */
	final static Vector d = new Vector( );
	public static boolean testBoundingBoxSegment( BoundingBox b, Segment s )
	{
		c.set( b.min ).add( b.max ).mul( 0.5f );
		e.set( b.max ).sub( c );
		m .set( s.a ).add( s.b ).mul(0.5f);
		d.set( s.a ).sub( m );
		m.sub( c );
		float adx = Math.abs( d.x );
		if( Math.abs( m.x ) > e.x + adx ) return false;
		float ady = Math.abs( d.y );
		if( Math.abs( m.y ) > e.y + ady ) return false;
		float adz = Math.abs( d.z );
		if( Math.abs( m.z ) > e.z + adz ) return false;
		
		adx += EPSILON; ady += EPSILON; adz += EPSILON;
		if( Math.abs( m.y * d.z - m.z * d.y ) > e.y * adz + e.z * ady ) return false;
		if( Math.abs( m.z * d.x - m.x * d.z ) > e.x * adz + e.z * adx ) return false;
		if( Math.abs( m.x * d.y - m.y * d.x ) > e.x * ady + e.y * adx ) return false;
		
		return true;
	}
	
	/**
	 * Returns whether the given sphere and ray intersect
	 * @param s the sphere
	 * @param r the ray
	 * @return whether the sphere and ray intersect
	 */
	public static boolean testSphereRay( Sphere s, Ray r )
	{
		m.set( r.origin ).sub( s.center );
		float c = m.dot(m) - s.radius*s.radius;
		if( c < 0 ) return false;
		float b = m.dot(r.direction);
		if( b > 0 ) return false;
		float disc = b*b - c;
		if( disc < 0 ) return false;
		return true;
	}
	
	/**
	 * Tests whether the moving sphere intersects the plane
	 * @param s the sphere 
	 * @param v the spheres velocity, encoding the distance and direction it travels
	 * @param p the plane
	 * @return whether the sphere and the plane intersect
	 */
	public static boolean testMovingSpherePlane( Sphere s, Vector v, Plane p )
	{
		float adist = s.center.dot( p.normal ) - p.d;
		float bdist = s.center.tmp().add( v ).dot( p.normal ) - p.d;
		
		if( adist * bdist < 0 ) return true;
		
		if( Math.abs( adist ) <= s.radius || Math.abs( bdist ) <= s.radius ) return true;
		return false;
	}
	
	/**
	 * Intersects the given segment and plane, returning the intersection point in o
	 * 
	 * @param s the segment
	 * @param p the plane
	 * @param i the intersection point (output)
	 * @return whether the segment and the plane intersect
	 */
	public static boolean intersectSegmentPlane( Segment s, Plane p, Vector i )
	{
		Vector ab = s.b.tmp().sub(s.a);
		float denom = ab.dot( p.getNormal() );		
		float t = -( s.a.dot(p.getNormal()) + p.getD() ) / denom;
		if( t < 0 || t > 1 )
			return false;

		intersection.set( s.a ).add( ab.mul(t) );
		return true;
	}
	
	/**
	 * Intersects the given ray and plane and returns the intersection point
	 * 
	 * @param r the ray
	 * @param p the plane
	 * @param i the intersection point
	 * @return whether the ray and the plane intersected
	 */
	public static boolean intersectRayPlane( Ray r, Plane p, Vector i )
	{
		Vector ab = r.direction.tmp();
		float denom = ab.dot( p.getNormal() );
		float t = -( r.origin.dot(p.getNormal()) + p.getD() ) / denom;
		if( t >= 0 )
		{
			i.set(r.origin).add( ab.mul(t) );
			return true;
		}
		
		return false;
	}
	
	public static float intersectRayPlane( Ray r, Plane p )
	{
		Vector ab = r.direction.tmp();
		float denom = ab.dot( p.getNormal() );
		float t = -( r.origin.dot(p.getNormal()) + p.getD() ) / denom;
		return t;
	}
	
	/**
	 * Intersects the given ray and sphere and returns the intersection point
	 * @param r the ray
	 * @param sp the sphere
	 * @param i the intersection point
	 * @return whether the ray and the sphere intersect
	 */
	final static Vector m = new Vector( );
	public static boolean intersectRaySphere( Ray r, Sphere sp, Vector i )
	{
		m.set( r.origin ).sub(sp.center);
		float b = m.dot( r.direction );
		float c = m.dot(m) - sp.radius * sp.radius;
		if( c > 0 && b > 0 ) return false;
		float discr = b*b - c;
		if( discr < 0 ) return false;
		float t = -b - (float)Math.sqrt( discr );
		if( t < 0 ) t = 0;
		i.set( r.origin ).add( r.direction.tmp().mul( t ) );
		return true;
	}
	
	/**
	 * Returns whether the given point is inside the given triangle. It is assumed
	 * that the point lies on the same plane as the triangle.
	 * 
	 * @param p the point
	 * @param t1 the first point of the triangle
	 * @param t2 the second point of the triangle
	 * @param t3 the third point of the triangle
	 * @return whether the point lies in the triangle
	 */
	static final Vector v0 = new Vector( );
	static final Vector v1 = new Vector( );
	static final Vector v2 = new Vector( );
	public static boolean isPointInTriangle( Vector p, Vector t1, Vector t2, Vector t3 )
	{
		v0.set( t3 ).sub( t1 );
		v1.set( t2 ).sub( t1 );
		v2.set( p ).sub( t1 );

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
			return true;		
		else		
			return false;			
	}
	
	/** Intersects the given ray and triangle
	 * 
	 * @param r the ray
	 * @param t1 the first point of the triangle
	 * @param t2 the second point of the triangle
	 * @param t3 the third point of the triangle
	 * @param i the intersection point (output)
	 * @return whether the ray and the triangle intersect
	 */
	public static boolean intersectRayTriangle( Ray r, Vector t1, Vector t2, Vector t3, Vector i )
	{
		plane.set( t1, t2, t3 );
		if( !intersectRayPlane( r, plane, i) )
			return false;
		if( isPointInTriangle( i, t1, t2, t3) )
			return true;
		else
			return false;
	}
	
	/** Intersects the given ray and triangle
	 * 
	 * @param r the ray
	 * @param t1 the first point of the triangle
	 * @param t2 the second point of the triangle
	 * @param t3 the third point of the triangle
	 * @param p the plane of the triangle
	 * @param i the intersection point (output)
	 * @return whether the ray and the triangle intersect
	 */
	public static boolean intersectRayTriangle( Ray r, Vector t1, Vector t2, Vector t3, Plane p, Vector i )
	{
		if( !intersectRayPlane( r, p, i) )
			return false;
		if( isPointInTriangle( i, t1, t2, t3) )
			return true;
		else
			return false;
	}
	
	/**
	 * Intersects the given segment and triangle intersect
	 * 
	 * @param s the segment
	 * @param t1 the first point of the triangle
	 * @param t2 the second point of the triangle
	 * @param t3 the third point of the triangle
	 * @param i the intersection point
	 * @return whether the given segment and triangle intersect
	 */
	public static boolean intersectSegmentTriangle( Segment s, Vector t1, Vector t2, Vector t3, Vector i )
	{
		plane.set( t1, t2, t3 );
		if( !intersectSegmentPlane( s, plane, i) )
			return false;
		if( isPointInTriangle( i, t1, t2, t3) )
			return true;
		else return false;
	}
	
	/**
	 * Intersects the given segment and triangle 
	 * @param s the segment
	 * @param t1 the first point of the triangle
	 * @param t2 the second point of the triangle
	 * @param t3 the third point of the triangle
	 * @param p the plane of the triangle
	 * @param i the intersection point
	 * @return whether the segment and the triangle intersect
	 */
	public static boolean intersectSegmentTriangle( Segment s, Vector t1, Vector t2, Vector t3, Plane p, Vector i )
	{
		if( !intersectSegmentPlane( s, p, i) )
			return false;
		if( isPointInTriangle( i, t1, t2, t3) )
			return true;
		else return false;	
	}
	
	/**
	 * Intersects the moving sphere and plane
	 * @param s the sphere
	 * @param v the velocity of the sphere, encoding the distance and direction it travels
	 * @param p the plane
	 * @param i the intersection point
	 * @return whether the sphere and plane intersect
	 */
	public static boolean intersectMovingSpherePlane( Sphere s, Vector v, Plane p, Vector i )
	{
		float dist = p.normal.dot( s.center ) - p.d;
		if( Math.abs( dist ) <= s.radius )
		{
			i.set( s.center );
			return true;
		}
		else
		{
			float denom = p.normal.dot(v);
			if( denom * dist >= 0 )
			{
				return false;
			}
			else
			{
				float r = dist > 0 ? s.radius: -s.radius;
				float t = (r - dist) / denom;
				if( t < 0 || t > 1 )
					return false;
				i.set( s.center ).add( v.tmp().mul(t) ).sub( p.normal.tmp().mul(r) );
				return true;
			}
		}
	}
	
	/**
	 * Returns whether the segment intersects with the given collision mesh
	 * @param mesh the mesh
	 * @param segment the segment
	 * @return whether the mesh and segment intersect
	 */
	final static Vector intersection = new Vector( );
	public static boolean testMeshSegment( CollisionMesh mesh, Segment segment )
	{
		float[] planes = mesh.getPlanes();
		float[] triangles = mesh.getTriangles();
		int numTriangles = mesh.getNumTriangles();
		int idx = 0;
		int idxt = 0;
		for( int i = 0; i < numTriangles; i++ )
		{
			p1.set( triangles[idxt++], triangles[idxt++], triangles[idxt++] );
			p2.set( triangles[idxt++], triangles[idxt++], triangles[idxt++] );
			p3.set( triangles[idxt++], triangles[idxt++], triangles[idxt++] );
			plane.set( planes[idx++], planes[idx++], planes[idx++], planes[idx++] );					
			
			if( intersectSegmentTriangle( segment, p1, p2, p3, plane, intersection ) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns whether the ray and the given collision mesh intersect
	 * @param mesh the mesh
	 * @param ray the ray
	 * @return whether the mesh and ray intersect
	 */
	public static boolean testMeshRay( CollisionMesh mesh, Ray ray )
	{
		float[] planes = mesh.getPlanes();
		float[] triangles = mesh.getTriangles();
		int numTriangles = mesh.getNumTriangles();
		int idx = 0;
		int idxt = 0;
		for( int i = 0; i < numTriangles; i++ )
		{
			p1.set( triangles[idxt++], triangles[idxt++], triangles[idxt++] );
			p2.set( triangles[idxt++], triangles[idxt++], triangles[idxt++] );
			p3.set( triangles[idxt++], triangles[idxt++], triangles[idxt++] );
			plane.set( planes[idx++], planes[idx++], planes[idx++], planes[idx++] );					
			
			if( intersectRayTriangle( ray, p1, p2, p3, plane, intersection ) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Collides the ellipsoid moving form start to end having xRadius in the x/z plane
	 * and yRadius on the y-axis with the given {@link CollisionMesh}.
	 * 
	 * @param mesh the CollisionMesh
	 * @param origin the start position of the ellipsoid
	 * @param end the end position of the ellipsoid
	 * @param xRadius the radius in the x/z plane
	 * @param yRadius the radius on the y-axis.
	 * @return whether a collision happened or not
	 */
	public static boolean collide( CollisionMesh mesh, CollisionPacket packet )
	{						
		processedTriangles = 0;
		earlyOutTriangles = 0;
		culledTriangles = 0;
		
		float[] triangles = mesh.getTriangles();
		int numTriangles = mesh.getNumTriangles();
		int idx = 0;
		for( int i = 0; i < numTriangles; i++ )
		{
			p1.set( triangles[idx++], triangles[idx++], triangles[idx++] ).scale( packet.invRadiusX, packet.invRadiusY, packet.invRadiusZ );
			p2.set( triangles[idx++], triangles[idx++], triangles[idx++] ).scale( packet.invRadiusX, packet.invRadiusY, packet.invRadiusZ );
			p3.set( triangles[idx++], triangles[idx++], triangles[idx++] ).scale( packet.invRadiusX, packet.invRadiusY, packet.invRadiusZ );
			
			if( mesh.isClockWise() )
				plane.set( p3, p2, p1 );
			else
				plane.set( p1, p2, p3 );
			
			collideTriangle( packet, p1, p2, p3, plane );
			
			processedTriangles++;
		}
		
		return packet.isColliding();
	}
	
	/**
	 * Collides the ellipsoid with the triangle in ellipsoid space. The
	 * triangle points and plane must be given in ellipsoid space!
	 * 
	 * @param packet
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param plane
	 */
	static Vector planeIntersectionPoint = new Vector( );
	static Vector edge = new Vector( );
	static Vector baseToVertex = new Vector( );
	public static void collideTriangle( CollisionPacket packet, Vector p1, Vector p2, Vector p3, Plane plane )
	{
		CollisionType type = CollisionType.Vertex;
		
		// we ignore back facing triangles
		if( !plane.isFrontFacing( packet.normalizedVelocity ) )
		{
			culledTriangles++;
			return;
		}
			
		// calculate the interval of plane intersection
		float t0, t1;
		boolean embeddedInPlane = false;
		
		// calculate the signed distance from the sphere to the triangle
		float signedDistanceToTrianglePlane = plane.distance( packet.position );
		float normalDotVelocity = plane.normal.dot(packet.velocity);
		
		// sphere is parallel to plane
		if( normalDotVelocity == 0 )
		{
			// no intersection
			if( Math.abs( signedDistanceToTrianglePlane ) >= 1.0f )
			{
				earlyOutTriangles++;
				return;
			}
			else
			{
				// sphere is embedded in triangle
				embeddedInPlane = true;
				t0 = 0;
				t1 = 1;
				type = CollisionType.Embedded;
			}
		}
		else
		{
			t0 = (-1 - signedDistanceToTrianglePlane ) / normalDotVelocity;
			t1 = (1 - signedDistanceToTrianglePlane ) / normalDotVelocity;
			
			if( t0 > t1 )
			{
				float tmp = t1;
				t1 = t0;
				t0 = tmp;
			}
			
			// no collision
			if( t0 > 1 || t1 < 0 )
			{
				earlyOutTriangles++;
				return;
			}
			
			// clamp
			if( t0 < 0 ) t0 = 0;
			if( t1 < 0 ) t1 = 0;
			if( t0 > 1 ) t0 = 1;
			if( t1 > 1 ) t1 = 1;
		}
		
		//
		// now we know the collision interval, let's do some magic...
		//
		Vector collisionPoint = null;
		boolean foundCollision = false;
		float t = 1.0f;
		
		//
		// first we test the case that the sphere collides with the
		// inside of the triangle. This happens at t0 but only if the
		// sphere is not embedded
		//
		if( !embeddedInPlane )
		{
			planeIntersectionPoint.set( packet.position ).sub( plane.normal );
			planeIntersectionPoint.add( packet.velocity.x * t0, packet.velocity.y * t0, packet.velocity.z * t0 );
			
			earlyOutTriangles++;
			
			// FIXME maybe isPointInTriangle is wrong?
			if( Intersector.isPointInTriangle( planeIntersectionPoint, p1, p2, p3 ) )
			{
				foundCollision = true;
				t = t0;
				collisionPoint = planeIntersectionPoint;
				type = CollisionType.Plane;
			}						
		}
		
		//
		// otherwise we have to check the vertices and edges of the triangle against the swept sphere
		// oh well, here we go...
		//
		if( foundCollision == false )
		{
			Vector velocity = packet.velocity;
			Vector base = packet.position;
			float velocitySquaredLength = velocity.len2();
			float a, b, c;			
			
			// first check against each vertex
			a = velocitySquaredLength;
			
			// P1
			b = 2 * (velocity.dot( base.tmp().sub(p1) ) );
			c = p1.tmp().sub(base).len2() - 1;
			float root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN && root < t )
			{
				t = root;
				foundCollision = true;
				collisionPoint = p1;
				type = CollisionType.Vertex;
			}
			
			// P2
			if( !foundCollision )
			{
				b = 2 * (velocity.dot(base.tmp().sub(p2)));
				c = p2.tmp().sub(base).len2() - 1;
				root = Intersector.getLowestPositiveRoot(a, b, c);
				if( root != Float.NaN && root < t )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p2;
					type = CollisionType.Vertex;					
				}
			}
			
			// P2
			if( !foundCollision )
			{
				b = 2 * (velocity.dot(base.tmp().sub(p3)));
				c = p3.tmp().sub(base).len2() - 1;
				root = Intersector.getLowestPositiveRoot(a, b, c);
				if( root != Float.NaN && root < t )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p3;
					type = CollisionType.Vertex;
				}
			}
			
			// now check against edges...						
			
			// p1 -> p2
			edge.set(p2).sub(p1);
			baseToVertex.set(p1).sub(base);
			float edgeSquaredLength = edge.len2();
			float edgeDotVelocity = edge.dot(velocity);
			float edgeDotBaseToVertex = edge.dot(baseToVertex);
			
			a = edgeSquaredLength * - velocitySquaredLength + 
				edgeDotVelocity * edgeDotVelocity;
			b = edgeSquaredLength*(2*velocity.dot(baseToVertex)) - 
				2 * edgeDotVelocity * edgeDotBaseToVertex;
			c = edgeSquaredLength * (1-baseToVertex.len2()) + 
				edgeDotBaseToVertex*edgeDotBaseToVertex;
			
			root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN && root < t )
			{
				float f = (edgeDotVelocity*root - edgeDotBaseToVertex)/edgeSquaredLength;
				if( f >= 0 && f <= 1 )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p1.tmp2().add( edge.tmp().mul(f) );
					type = CollisionType.Edge;
				}
			}
			
			// p2 -> p3
			edge.set(p3).sub(p2);
			baseToVertex.set(p2).sub(base);
			edgeSquaredLength = edge.len2();
			edgeDotVelocity = edge.dot(velocity);
			edgeDotBaseToVertex = edge.dot(baseToVertex);
			
			a = edgeSquaredLength * - velocitySquaredLength + 
				edgeDotVelocity * edgeDotVelocity;
			b = edgeSquaredLength*(2*velocity.dot(baseToVertex)) - 
				2 * edgeDotVelocity * edgeDotBaseToVertex;
			c = edgeSquaredLength * (1-baseToVertex.len2()) + 
				edgeDotBaseToVertex*edgeDotBaseToVertex;
			
			root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN && root < t )
			{
				float f = (edgeDotVelocity*root - edgeDotBaseToVertex)/edgeSquaredLength;
				if( f >= 0 && f <= 1 )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p2.tmp2().add( edge.tmp().mul(f) );
					type = CollisionType.Edge;
				}
			}
			
			// p3 -> p1
			edge.set(p1).sub(p3);
			baseToVertex.set(p3).sub(base);
			edgeSquaredLength = edge.len2();
			edgeDotVelocity = edge.dot(velocity);
			edgeDotBaseToVertex = edge.dot(baseToVertex);
			
			a = edgeSquaredLength * - velocitySquaredLength + 
				edgeDotVelocity * edgeDotVelocity;
			b = edgeSquaredLength*(2*velocity.dot(baseToVertex)) - 
				2 * edgeDotVelocity * edgeDotBaseToVertex;
			c = edgeSquaredLength * (1-baseToVertex.len2()) + 
				edgeDotBaseToVertex*edgeDotBaseToVertex;
			
			root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN && root < t )
			{
				float f = (edgeDotVelocity*root - edgeDotBaseToVertex)/edgeSquaredLength;
				if( f >= 0 && f <= 1 )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p3.tmp2().add( edge.tmp().mul(f) );
					type = CollisionType.Edge;
				}
			}		
		}
		
		// compose the final result
		if( foundCollision == true )
		{
			float distToCollision = t*packet.velocity.len();
			if( packet.foundCollision == false || distToCollision < packet.nearestDistance )
			{
				packet.nearestDistance = distToCollision;
				packet.intersectionPoint.set(collisionPoint);
				packet.foundCollision = true;
				packet.type = type;
				packet.plane.set(plane);
			}
		}
	}
	
	/**
	 * @return the number of processed triangles by the last call to collide();
	 */
	public static int getNumProcessedTriangles( )
	{
		return processedTriangles;
	}
	
	/**
	 * @return the number of culled triangles by the last call to collide();
	 */
	public static int getNumCulledTriangles( )
	{
		return culledTriangles;
	}
	
	/**
	 * @return the number of non colliding triangles by the last call to collide();
	 */
	public static int getNumEarlyOutTriangles( )
	{
		return earlyOutTriangles;
	}
	
	/**
	 * @return the number of triangles that actually collided by the last call to collide();
	 */
	public static int getNumCollidedTriangles( )
	{
		return processedTriangles - culledTriangles - earlyOutTriangles;
	}	

	/**
	 * Calculates the closest point on the perimeter of the triangle to the given
	 * point. The given point is assumed to be on the plane of the triangle
	 * @param p1 the first point of the triangle
	 * @param p2 the second point of the triangle
	 * @param p3 the third point of the triangle
	 * @param p the point
	 * @param i the closest point
	 */
	static final Vector ab = new Vector( );
	static final Vector ac = new Vector( );
	static final Vector ap = new Vector( );
	static final Vector bp = new Vector( );
	static final Vector cp = new Vector( );
	public static void closestPointToTriangle(Vector a, Vector b, Vector c, Vector p, Vector i) 
	{	
		ab.set(b).sub(a);
		ac.set(c).sub(a);
		ap.set(p).sub(a);
		float d1 = ab.dot(ap);
		float d2 = ac.dot(ap);
		
		if( d1 <= 0 && d2 <= 0 ) 
		{
			i.set(a);
			return;
		}
		
		bp.set(p).sub(b);
		float d3 = ab.dot(bp);
		float d4 = ac.dot(bp);
		if( d3 >= 0 && d4 <= d3 )
		{
			i.set(b);
			return;
		}
		
		float vc = d1 * d4 - d3 * d2;
		if( vc <= 0 && d1 >= 0 && d3 <= 0 )
		{
			float v = d1 / (d1 - d3);
			i.set(a).add( ab.mul(v) );
			return;
		}
		
		cp.set(p).sub(c);
		float d5 = ab.dot(cp);
		float d6 = ac.dot(cp);
		if( d6 >= 0 && d5 <= d6 )
		{
			i.set(c);
			return;
		}
		
		float vb = d5 * d2 - d1 * d6;
		if( vb <= 0 && d2 >= 0 && d6 <= 0 )
		{
			float w = d2 / (d2-d6);
			i.set(a).add(ac.mul(w));
			return;
		}
		
		float va = d3 * d6 - d5 * d4;
		if( va <= 0 && (d4 - d3) >= 0 && (d5 - d6) >= 0 )
		{
			float w = (d4 - d3) / ((d4-d3) + (d5 - d6));
			i.set(b).add( c.tmp().sub(b).mul(w) );
			return;
		}
		
		float denom = 1.0f / (va + vb + vc );
		float v = vb * denom;
		float w = vc * denom;
		i.set(a).add( ab.mul(v) ).add( ac.mul(w) );
	}
	
	public static void main( String[] argv ) throws Exception
	{
		BoundingBox aabb = new BoundingBox();
		Plane p = new Plane( new Vector(), new Vector() );
		Vector v = new Vector( );
		Vector i = new Vector();
		
		aabb.set( new Vector( -2, -2, -2 ), new Vector( 2, 2, 2 ) );
		v.set( -1, 1, -1 );
		CollisionDetection.closestPointToBoundingBox(aabb, v, i);
		check(i.x == -1 && i.y == 1 && i.z == -1);
		
		v.set( -3, -3, -3 );
		CollisionDetection.closestPointToBoundingBox(aabb, v, i);
		check(i.x == -2 && i.y == -2 && i.z == -2);
		
		CollisionDetection.closestPointToLine(new Vector( ), new Vector( 1, 1, 1 ), new Vector( -1, 1, -1 ), i );
		check(i.x == -1 / 3.0f && i.y == -1 / 3.0f && i.z == -1 / 3.0f );
		
		p.set( new Vector(1, 1, 1), new Vector( 1, 1, 1 ).nor() );		
		CollisionDetection.closestPointToPlane( p, new Vector( 3, 3, 3 ), i );
		check(eeq(i.x, 1) && eeq(i.y, 1) && eeq(i.z, 1) );
		
		CollisionDetection.closestPointToRay( new Ray( new Vector( 1, 1, 1 ), new Vector( 1, 1, 1 ) ), new Vector(), i );
		check(eeq(i.x, 1) && eeq(i.y,1) && eeq(i.z, 1 ) );
		
		CollisionDetection.closestPointToRay( new Ray( new Vector( 1, 1, 0 ), new Vector( 1, 1, 0 ) ), new Vector( 1, 3, 0 ), i );
		check( i.x == 2 && i.y == 2 && i.z == 0 );
		
		CollisionDetection.closestPointToSegment( new Segment( new Vector( 1, 1, 1 ), new Vector( 2, 2, 2 ) ), new Vector( ), i );
		check(eeq(i.x, 1) && eeq(i.y,1) && eeq(i.z, 1 ) );
		
		CollisionDetection.closestPointToSegment( new Segment( new Vector( 1, 1, 1 ), new Vector( 2, 2, 2 ) ), new Vector( 3, 3, 3 ), i );
		check(eeq(i.x, 2) && eeq(i.y,2) && eeq(i.z, 2 ) );
		
		CollisionDetection.closestPointToSegment( new Segment( new Vector( 1, 1, 0 ), new Vector( 2, 2, 0 ) ), new Vector( 1, 3, 0 ), i );
		check(eeq(i.x, 2) && eeq(i.y,2) && eeq(i.z, 0 ) );
		
		CollisionDetection.closestPointToTriangle( new Vector( ), new Vector( 1, 0, 0), new Vector(0, 0.5f, -1 ), new Vector( 0, -1, 0 ), i );
		check( i.x == 0 && i.y == 0 && i.z == 0 );
		
		CollisionDetection.closestPointToTriangle( new Vector( ), new Vector( 1, 0, 0), new Vector(0, 0.5f, -1 ), new Vector( 0.5f, -1, 0 ), i );
		check( i.x == 0.5f && i.y == 0 && i.z == 0 );
		
		CollisionDetection.closestPointToTriangle( new Vector( ), new Vector( 1, 0, 0), new Vector(0.5f, 1, -1 ), new Vector( 0.5f, 1, -0.5f ), i );
		check( i.x == 0.5f && i.y == 0.75f && i.z == -0.75f );
		
		check( CollisionDetection.testBoundingBoxPlane( aabb, new Plane( new Vector( 1, 1, 1 ), new Vector() ) ) );
		check( !CollisionDetection.testBoundingBoxPlane( aabb, new Plane( new Vector( 1, 1, 1 ), new Vector(-3, -3, -3) ) ) );
		check( !CollisionDetection.testBoundingBoxPlane( aabb, new Plane( new Vector( 1, 1, 1 ), new Vector(3, 3, 3) ) ) );
		
		// FIXME this is essentially a aabb segment test!
//		check( CollisionDetection.testBoundingBoxRay( aabb, new Ray( new Vector( -3, -3, -3 ), new Vector( 1, 1, 1 ) ) ) );
		
		CollisionDetection.testBoundingBoxSegment(b, s)
	}
	
	private static boolean eeq( float a, float b )
	{
		return ( Math.abs( a - b) < 0.000001 );
	}
	
	private static void check( boolean expr ) throws Exception
	{
		if( !expr )
			throw new Exception( "d'oh" );		
	}
}
