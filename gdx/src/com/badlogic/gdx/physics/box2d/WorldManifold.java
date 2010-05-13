package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

/**
 * This is used to compute the current state of a contact manifold.
 */
public class WorldManifold 
{
	protected long addr; 
	protected long contactAddr;
	
	private final float[] tmp = new float[4];
	private final Vector2 normal = new Vector2();
	private final Vector2[] points = { new Vector2(), new Vector2() }; 
	
	protected WorldManifold( long addr, long contactAddr )
	{
		this.addr = addr;
	}
	
	/**
	 * Returns the normal of this manifold
	 */
	public Vector2 getNormal( )
	{
		jniGetNormal( addr, tmp );
		normal.set( tmp[0], tmp[1] );
		return normal;
	}
	
	private native void jniGetNormal( long addr, float[] normal );
	
	/**
	 * Returns the contact points of this manifold. Use getNumberOfContactPoints
	 * to determine how many contact points there are (0,1 or 2)
	 */
	public Vector2[] getPoints( )
	{
		int numPoints = jniGetPoints( addr, contactAddr, tmp );
		for( int i = 0; i < numPoints; i++ )		
			points[i].set( tmp[i*2], tmp[i*2+1] );				
		return points;
	}
	
	private native int jniGetPoints( long addr, long contactAddr, float[] points );
	
	public int getNumberOfContactPoints( )
	{
		return jniGetNumberOfContactPoints( contactAddr );
	}
	
	private native int jniGetNumberOfContactPoints( long contactAddr );
}
