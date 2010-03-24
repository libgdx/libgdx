package com.badlogic.gdx.bsp.collision;

import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector;

/**
 * Class holding various static methods to perform collision detection
 * and response.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class CollisionDetection 
{
	/**
	 * Collides the ellipsoid moving form start to end having xRadius in the x/z plane
	 * and yRadius on the y-axis with the given {@link CollisionMesh}.
	 * 
	 * @param mesh the CollisionMesh
	 * @param start the start position of the ellipsoid
	 * @param end the end position of the ellipsoid
	 * @param xRadius the radius in the x/z plane
	 * @param yRadius the radius on the y-axis.
	 * @return whether a collision happened or not
	 */
	public static boolean collide( CollisionMesh mesh, CollisionPacket packet )
	{
		return false;
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
	public static void collideTriangle( CollisionPacket packet, Vector p1, Vector p2, Vector p3, Plane plane )
	{
		// we ignore back facing triangles
		if( !plane.isFrontFacing( packet.normalizedVelocity ) )
			return;
			
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
				return;
			}
			else
			{
				// sphere is embedded in triangle
				embeddedInPlane = true;
				t0 = 0;
				t1 = 1;
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
				return;
			
			// clamp
			if( t0 < 0 ) t0 = 0;
			if( t1 < 0 ) t1 = 0;
			if( t0 > 1 ) t0 = 1;
			if( t1 > 1 ) t1 = 1;
		}
		
		//
		// now we know the collision interval, let's do some magic...
		//
		Vector collisionPoint = new Vector( );
		boolean foundCollision = false;
		float t = 1.0f;
		
		//
		// first we test the case that the sphere collides with the
		// inside of the triangle. This happens at t0 but only if the
		// sphere is not embedded
		//
		if( !embeddedInPlane )
		{
			Vector planeIntersectionPoint = new Vector( packet.position ).sub( plane.normal );
			planeIntersectionPoint.add( packet.velocity.x * t0, packet.velocity.y * t0, packet.velocity.z * t0 );
			
			if( Intersector.isPointInTriangle( planeIntersectionPoint, p1, p2, p3 ) )
			{
				foundCollision = true;
				t = t0;
				collisionPoint = planeIntersectionPoint;
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
			float newT;
			
			// first check against each vertex
			a = velocitySquaredLength;
			
			// P1
			b = 2 * (velocity.dot( base.tmp().sub(p1) ) );
			c = p1.tmp().sub(base).len2() - 1;
			float root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN )			
				t = root;
			
			
		}
	}
	
	public static void main( String[] argv )
	{
		FloatMesh mesh = new FloatMesh( 4, 3, false, false, false, 0, 0, true, 6 );
		mesh.setVertices( new float[] { -1, 0, 1, 1, 0, 1, 1, 0, -1, -1, 0, -1 } );
		mesh.setIndices( new short[] { 0, 1, 2, 2, 3, 1 } );
		CollisionMesh cmesh = new CollisionMesh( mesh, false );
		System.out.println( cmesh );
	}
}
