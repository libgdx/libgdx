package com.badlogic.gdx.bsp.collision;

import com.badlogic.gdx.math.Vector;

/**
 * A collision package contains an ellipsoids starting position
 * its velocity as well as its extends in x, y and z. The starting
 * position and velocity are stored twice, once in normal 3-space and
 * once in ellipsoid space, that is scaled by (1/xRadius,1/yRadius,1/zRadius).
 * 
 * An instance of this class is passed to {@link CollisionDetector} and returns
 * whether a collision occured and if so where it occured.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class CollisionPacket 
{
	/** the ellipsoid radii **/
	protected final float radiusX, radiusY, radiusZ;
	
	/** the inverse ellipsoid radii **/
	protected final float invRadiusX, invRadiusY, invRadiusZ;
	
	/** the position and velocity of the ellipsoid in 3-space **/
	protected final Vector r3Velocity;
	protected final Vector r3Position;
	
	/** the position and velocity of the ellipsoid in ellipsoid-space **/
	protected final Vector velocity;
	protected final Vector normalizedVelocity;
	protected final Vector position;
	
	protected boolean foundCollision;
	protected float nearestDistance;
	protected Vector intersectionPoint;
	
	public CollisionPacket( Vector position, Vector velocity, float radiusX, float radiusY, float radiusZ )
	{		
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.radiusZ = radiusZ;
		this.invRadiusX = 1 / radiusX;
		this.invRadiusY = 1 / radiusY;
		this.invRadiusZ = 1 / radiusZ;
		
		r3Position = new Vector( position );
		r3Velocity = new Vector( velocity );
		
		this.velocity = new Vector( velocity.x * invRadiusX, velocity.y * invRadiusY, velocity.z * invRadiusZ );
		this.normalizedVelocity = new Vector( velocity ).nor();
		this.position = new Vector( position.x * invRadiusX, position.y * invRadiusY, position.z * invRadiusZ );
		
		this.intersectionPoint = new Vector( );
	}
	
	/**
	 * @return whether the ellipsoid is colliding
	 */
	public boolean isColliding( )
	{
		return foundCollision;
	}
	
	/**
	 * @return the point of intersection
	 */
	public Vector getIntersectionPoint( )
	{
		return intersectionPoint;
	}
	
	/**
	 * @return the nearest distance to the colliding plane
	 */
	public float getNearestDistance( )
	{
		return nearestDistance;
	}

	/**
	 * Sets the position and velocity of the swept ellipsoid. Resets
	 * the old collision flag and distance.
	 * 
	 * @param position the position of the sphere
	 * @param velocity the velocity of the ellipsoid
	 */
	public void set(Vector position, Vector velocity) 
	{	
		r3Position.set( position );
		r3Velocity.set( velocity );
		
		this.velocity.set( velocity.x * invRadiusX, velocity.y * invRadiusY, velocity.z * invRadiusZ );
		this.normalizedVelocity.set( velocity ).nor();
		this.position.set( position.x * invRadiusX, position.y * invRadiusY, position.z * invRadiusZ );
		
		foundCollision = false;
		nearestDistance = 0;
		intersectionPoint.set(0,0,0);
	}
}
