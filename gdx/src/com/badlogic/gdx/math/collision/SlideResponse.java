package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector;

/**
 * Implementation of a sliding {@link CollisionResponse}
 * 
 * @author mzechner
 *
 */
public class SlideResponse implements CollisionResponse 
{
	/** the original destination of the collider **/
	private final Vector destination = new Vector( );
	/** the new position of the collider **/	
	private final Vector newPosition = new Vector( );
	/** the new destination **/
	private final Vector newDestination = new Vector( );
	/** the new velocity **/
	private final Vector newVelocity = new Vector( );
	/** the sliding plane origin **/
	private final Vector slidingPlaneOrigin = new Vector( );
	/** the sliding plane normal **/
	private final Vector slidingPlaneNormal = new Vector( );
	/** the sliding plane **/
	private final Plane slidingPlane = new Plane( new Vector(), 0 );		
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void respond(CollisionPacket packet, float displacementDistance) 
	{	
		if( packet.getNearestDistance() == 0 )
		{			
			System.out.println( "embedded: " + packet.position.dst(packet.intersectionPoint) + ", " + packet.plane );
			float distance = packet.plane.distance(packet.position);
			packet.position.add(packet.plane.normal.tmp().mul(1 - distance + displacementDistance ));
			packet.nearestDistance = displacementDistance;			
		}
		
		destination.set( packet.position ).add( packet.velocity );
		newPosition.set( packet.position );		
		
		if( packet.getNearestDistance() >= displacementDistance )
		{
			newVelocity.set( packet.velocity ).nor().mul(packet.getNearestDistance() - displacementDistance);
			newPosition.add( newVelocity );
			
			newVelocity.nor();
			packet.getIntersectionPoint().sub( newVelocity.mul( displacementDistance ) );
		}			
		
		slidingPlaneOrigin.set( packet.getIntersectionPoint() );
		slidingPlaneNormal.set( newPosition ).sub( packet.getIntersectionPoint() ).nor();		
		slidingPlane.set( slidingPlaneOrigin, slidingPlaneNormal );
		
		newDestination.set( destination ).sub( slidingPlane.normal.mul(slidingPlane.distance( destination )) );
		newVelocity.set( newDestination ).sub( packet.getIntersectionPoint() );
		packet.velocity.set( newVelocity );
		packet.position.set( newPosition );
	}
	
}
