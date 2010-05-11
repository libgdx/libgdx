package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/**
 *  A gear joint is used to connect two joints together. Either joint
 * can be a revolute or prismatic joint. You specify a gear ratio
 * to bind the motions together:
 * coordinate1 + ratio * coordinate2 = constant
 * The ratio can be negative or positive. If one joint is a revolute joint
 * and the other joint is a prismatic joint, then the ratio will have units
 * of length or units of 1/length.
 * @warning The revolute and prismatic joints must be attached to
 * fixed bodies (which must be body1 on those joints).
 */
public class GearJoint extends Joint
{
	public GearJoint(World world, long addr) 
	{
		super(world, addr);	
	}
	
	/**
	 *  Set/Get the gear ratio.
	 */
	public void setRatio(float ratio)
	{
		jniSetRatio( addr, ratio );
	}
	
	private native void jniSetRatio( long addr, float ratio );
	
	/**
	 *  Set/Get the gear ratio.
	 */
	public float getRatio()
	{
		return jniGetRatio( addr );
	}
	
	private native float jniGetRatio( long addr );
}
