package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/**
 * A distance joint constrains two points on two bodies
 * to remain at a fixed distance from each other. You can view
 * this as a massless, rigid rod.
 */
public class DistanceJoint extends Joint
{
	public DistanceJoint(World world, long addr) 
	{
		super( world, addr);	
	}

	/**
	 *  Set/get the natural length.
	 * Manipulating the length can lead to non-physical behavior when the frequency is zero.
	 */
	public void setLength(float length)
	{
		jniSetLength( addr, length );
	}
	
	private native void jniSetLength( long addr, float length );	
	
	/**
	 * Set/get the natural length.
	 * Manipulating the length can lead to non-physical behavior when the frequency is zero.
	 */
	public float getLength()
	{
		return jniGetLength( addr );
	}
	
	private native float jniGetLength( long addr );

	/**
	 * Set/get frequency in Hz.
	 */
	public void setFrequency(float hz)
	{
		jniSetFrequency( addr, hz );
	}
	
	private native void jniSetFrequency( long addr, float hz );
	
	/**
	 * Set/get frequency in Hz.
	 */
	public float getFrequency()
	{
		return jniGetFrequency( addr );
	}
	
	private native float jniGetFrequency( long addr );

	/**
	 *  Set/get damping ratio.
	 */
	public void setDampingRatio(float ratio)
	{
		jniSetDampingRatio( addr, ratio );
	}
	
	private native void jniSetDampingRatio( long addr, float ratio );
	
	/**
	 *  Set/get damping ratio.
	 */
	public float getDampingRatio()
	{
		return jniGetDampingRatio( addr );
	}
	
	private native float jniGetDampingRatio( long addr );
}
