package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Friction joint. This is used for top-down friction.
 * It provides 2D translational friction and angular friction.
 */
public class FrictionJoint extends Joint
{
	public FrictionJoint( World world, long addr) 
	{
		super(world, addr);	
	}
	
	/**
	 * Set the maximum friction force in N.
	 */
	public void setMaxForce(float force)
	{
		jniSetMaxForce( addr, force );
	}
	
	private native void jniSetMaxForce( long ddr, float force );

	/**
	 *  Get the maximum friction force in N.
	 */
	public float getMaxForce()
	{
		return jniGetMaxForce( addr );
	}
	
	private native float jniGetMaxForce( long addr );

	/**
	 * Set the maximum friction torque in N*m.
	 */
	public void setMaxTorque(float torque)
	{
		jniSetMaxTorque( addr, torque );
	}
	
	private native void jniSetMaxTorque( long addr, float torque );

	/**
	 *  Get the maximum friction torque in N*m.
	 */
	public float getMaxTorque()
	{
		return jniGetMaxTorque( addr );
	}
	
	private native float jniGetMaxTorque( long addr );
}
