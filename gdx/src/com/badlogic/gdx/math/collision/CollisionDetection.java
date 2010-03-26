package com.badlogic.gdx.math.collision;

import android.text.style.BackgroundColorSpan;

import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector;

/**
 * Class holding various static methods to perform collision detection.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class CollisionDetection 
{
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
			}
			
			// P2
			b = 2 * (velocity.dot(base.tmp().sub(p2)));
			c = p2.tmp().sub(base).len2() - 1;
			root = Intersector.getLowestPositiveRoot(a, b, c);
			if( root != Float.NaN && root < t )
			{
				t = root;
				foundCollision = true;
				collisionPoint = p2;
			}
			
			// P2
			b = 2 * (velocity.dot(base.tmp().sub(p3)));
			c = p3.tmp().sub(base).len2() - 1;
			root = Intersector.getLowestPositiveRoot(a, b, c);
			if( root != Float.NaN && root < t )
			{
				t = root;
				foundCollision = true;
				collisionPoint = p3;
			}
			
			// now check against edges...						
			
			// p1 -> p2
			edge.set(p2).sub(p1);
			baseToVertex.set(p1).sub(base);
			float edgeSquaredLength = edge.len2();
			float edgeDotVelocity = edge.dot(velocity);
			float edgeDotBaseToVertex = edge.dot(baseToVertex);
			
			a = edgeSquaredLength * - velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
			b = edgeSquaredLength*(2*velocity.dot(baseToVertex)) - 2 * edgeDotVelocity * edgeDotBaseToVertex;
			c = edgeSquaredLength * (1-baseToVertex.len2()) + edgeDotBaseToVertex*edgeDotBaseToVertex;
			
			root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN && root < t )
			{
				float f = (edgeDotVelocity*root - edgeDotBaseToVertex)/edgeSquaredLength;
				if( f > 0 && f < 1 )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p1.tmp2().add( edge.tmp().mul(f) );
				}
			}
			
			// p2 -> p3
			edge.set(p3).sub(p2);
			baseToVertex.set(p2).sub(base);
			edgeSquaredLength = edge.len2();
			edgeDotVelocity = edge.dot(velocity);
			edgeDotBaseToVertex = edge.dot(baseToVertex);
			
			a = edgeSquaredLength * - velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
			b = edgeSquaredLength*(2*velocity.dot(baseToVertex)) - 2 * edgeDotVelocity * edgeDotBaseToVertex;
			c = edgeSquaredLength * (1-baseToVertex.len2()) + edgeDotBaseToVertex*edgeDotBaseToVertex;
			
			root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN && root < t )
			{
				float f = (edgeDotVelocity*root - edgeDotBaseToVertex)/edgeSquaredLength;
				if( f > 0 && f < 1 )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p2.tmp2().add( edge.tmp().mul(f) );
				}
			}
			
			// p3 -> p1
			edge.set(p1).sub(p3);
			baseToVertex.set(p3).sub(base);
			edgeSquaredLength = edge.len2();
			edgeDotVelocity = edge.dot(velocity);
			edgeDotBaseToVertex = edge.dot(baseToVertex);
			
			a = edgeSquaredLength * - velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
			b = edgeSquaredLength*(2*velocity.dot(baseToVertex)) - 2 * edgeDotVelocity * edgeDotBaseToVertex;
			c = edgeSquaredLength * (1-baseToVertex.len2()) + edgeDotBaseToVertex*edgeDotBaseToVertex;
			
			root = Intersector.getLowestPositiveRoot( a, b, c );
			if( root != Float.NaN && root < t )
			{
				float f = (edgeDotVelocity*root - edgeDotBaseToVertex)/edgeSquaredLength;
				if( f > 0 && f < 1 )
				{
					t = root;
					foundCollision = true;
					collisionPoint = p3.tmp2().add( edge.tmp().mul(f) );
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
	
	public static void main( String[] argv )
	{
		FloatMesh mesh = new FloatMesh( 4, 3, false, false, false, 0, 0, true, 6 );
		mesh.setVertices( new float[] { -0.5f, 0, 0.5f, 0.5f, 0, 0.5f, 0, 0, -0.5f, -0.5f, 0, -0.5f } );
		mesh.setIndices( new short[] { 0, 1, 2, 2, 3, 1} );
		CollisionMesh cmesh = new CollisionMesh( mesh, false );
		
		CollisionPacket packet = new CollisionPacket( new Vector( 0, 1f, 0 ), new Vector( 0, -2, 0 ), 1, 1, 1 );				
		System.out.println(CollisionDetection.collide( cmesh, packet ));
		System.out.println(packet.getIntersectionPoint());
		System.out.println(packet.getNearestDistance());
	}
}
