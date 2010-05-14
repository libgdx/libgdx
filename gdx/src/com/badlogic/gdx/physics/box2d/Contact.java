package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

/**
 * The class manages contact between two shapes. A contact exists for each overlapping
 * AABB in the broad-phase (except if filtered). Therefore a contact object may exist
 * that has no contact points.
 * @author mzechner
 *
 */
public class Contact 
{	
	/** the address **/
	protected long addr;
	
	/** the world **/
	protected World world;
	
	/** the world manifold **/
	protected final WorldManifold worldManifold = new WorldManifold( );
	
	protected Contact( World world, long addr )
	{
		this.addr = addr;
		this.world = world;		
	}
	
	/**
	 * Get the world manifold.
	 */	
	private final float[] tmp = new float[6];
	public WorldManifold GetWorldManifold()
	{
		int numContactPoints = jniGetWorldManifold( addr, tmp );
		
		worldManifold.numContactPoints = numContactPoints;
		worldManifold.normal.set( tmp[0], tmp[1] );
		for( int i = 0; i < numContactPoints; i++ )
		{
			Vector2 point = worldManifold.points[i];
			point.x = tmp[2+i*2];
			point.y = tmp[2+i*2 + 1];
		}
		
		return worldManifold;
	}
	
	private native int jniGetWorldManifold( long addr, float[] manifold );
	
	public boolean isTouching( )
	{
		return jniIsTouching( addr );
	}
	
	private native boolean jniIsTouching( long addr );
	
	/**
	 *  Enable/disable this contact. This can be used inside the pre-solve
	 * contact listener. The contact is only disabled for the current
	 * time step (or sub-step in continuous collisions).
	 */
	public void setEnabled(boolean flag)
	{
		jniSetEnabled( addr, flag );
	}
	
	private native void jniSetEnabled( long addr, boolean flag );

	/**
	 *  Has this contact been disabled?
	 */
	public boolean isEnabled()
	{
		return jniIsEnabled( addr );
	}
	
	private native boolean jniIsEnabled( long addr );

	/**
	 *  Get the first fixture in this contact.
	 */
	public Fixture getFixtureA()
	{
		return world.fixtures.get( jniGetFixtureA( addr ) );		
	}	
	
	private native long jniGetFixtureA( long addr );

	/**
	 *  Get the second fixture in this contact.
	 */
	public Fixture getFixtureB()
	{
		return world.fixtures.get( jniGetFixtureB( addr ) );
	}	
	
	private native long jniGetFixtureB( long addr );
}
