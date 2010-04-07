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
package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

/**
 * Implementation of a sliding {@link CollisionResponse}
 * 
 * @author mzechner
 *
 */
public class SlideResponse implements CollisionResponse 
{
	/** the original destination of the collider **/
	private final Vector3 destination = new Vector3( );
	/** the new position of the collider **/	
	private final Vector3 newPosition = new Vector3( );
	/** the new destination **/
	private final Vector3 newDestination = new Vector3( );
	/** the new velocity **/
	private final Vector3 newVelocity = new Vector3( );
	/** the sliding plane origin **/
	private final Vector3 slidingPlaneOrigin = new Vector3( );
	/** the sliding plane normal **/
	private final Vector3 slidingPlaneNormal = new Vector3( );
	/** the sliding plane **/
	private final Plane slidingPlane = new Plane( new Vector3(), 0 );		
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void respond(CollisionPacket packet, float displacementDistance) 
	{	
		if( packet.getNearestDistance() == 0 )
		{			
//			System.out.println( "embedded: " + packet.position.dst(packet.intersectionPoint) + ", " + packet.plane );
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
