package com.badlogic.gdx.bsp.collision;

import com.badlogic.gdx.math.Vector;

/**
 * An EllipsoidCollider is a class that encapsulates the property of
 * an ellipsoid bounding volume. It has methods that allow you to
 * collide the ellipsoid with a {@link CollisionMesh}. The response
 * to the collision can be set at construction time and will alter
 * the position and velocity of the collider accordingly.
 * 
 * @author mzechner
 *
 */
public class EllipsoidCollider 
{
	/** the internal CollisionPacket used to track collision states **/
	private final CollisionPacket packet;
	/** the response to use in case a collision occured **/
	private final CollisionResponse response;
	
	/**
	 * Constructs a new EllipsoidCollider with the given radii that uses
	 * the given {@link CollisionResponse} in case a collision was detected.
	 * 
	 * @param xRadius the radius on the x-Axis
	 * @param yRadius the radius on the y-Axis
	 * @param zRadius the radius on the z-Axis
	 * @param response the response
	 */
	public EllipsoidCollider( float xRadius, float yRadius, float zRadius, CollisionResponse response )
	{
		if( response == null )
			throw new IllegalArgumentException( "response must be != null" );
		
		packet = new CollisionPacket( new Vector(), new Vector(), xRadius, yRadius, zRadius );
		this.response = response;
	}
	
	/**
	 * Collides the ellipsoid collider with the given mesh using the 
	 * given position and velocity. If a collision occured the position
	 * and velocity given will be modified according to the {@link CollisionResponse}
	 * set for this collider. The displacementDistance defines by how much a collider
	 * will be displaced from the intersecting plane so that it does not touch
	 * the plane anymore after correction of the position. This value depends on the
	 * scale of your world and is usually very small (e.g. 0.0001 for a world units of 1m).
	 * 
	 * @param mesh the CollisionMesh
	 * @param position the position
	 * @param velocity the velocity
	 * @param displacementDistance the distance by which to displace a collider
	 */
	public boolean collide( CollisionMesh mesh, Vector position, Vector velocity, float displacementDistance )
	{
		boolean collided = false;
		int iterations = 0;
		while( true )
		{
			packet.set( position, velocity );
			CollisionDetection.collide( mesh, packet );
			
			if( packet.isColliding() && iterations < 5 )
			{
				collided = true;
				response.respond( packet, position, velocity, displacementDistance );
				if( velocity.len() < displacementDistance )
					break;
			}
			else
				break;
			iterations++;			
		}
		
		return collided;
	}
}
