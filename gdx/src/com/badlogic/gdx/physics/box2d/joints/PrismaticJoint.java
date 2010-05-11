package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/**
 *  A prismatic joint. This joint provides one degree of freedom: translation
 * along an axis fixed in body1. Relative rotation is prevented. You can
 * use a joint limit to restrict the range of motion and a joint motor to
 * drive the motion or to model joint friction.
 */
public class PrismaticJoint extends Joint 
{
	public PrismaticJoint( World world, long addr) 
	{
		super(world, addr);	
	}

	/**
	 * Get the current joint translation, usually in meters.
	 */
	public float getJointTranslation() 
	{
		return jniGetJointTranslation( addr );
	}
	
	private native float jniGetJointTranslation( long addr );

	/**
	 *  Get the current joint translation speed, usually in meters per second.
	 */
	public float getJointSpeed()
	{
		return jniGetJointSpeed( addr );
	}
	
	private native float jniGetJointSpeed( long addr );

	/**
	 * Is the joint limit enabled?
	 */
	public boolean isLimitEnabled()
	{
		return jniIsLimitEnabled( addr );
	}
	
	private native boolean jniIsLimitEnabled( long addr );

	/**
	 *  Enable/disable the joint limit.
	 */
	public void enableLimit(boolean flag)
	{
		jniEnableLimit( addr, flag );
	}
	
	private native void jniEnableLimit( long addr, boolean flag );

	/**
	 * Get the lower joint limit, usually in meters.
	 */
	public float getLowerLimit()
	{
		return jniGetLowerLimit( addr );
	}

	private native float jniGetLowerLimit( long addr );
	
	/**
	 *  Get the upper joint limit, usually in meters.
	 */
	public float getUpperLimit()
	{
		return jniGetUpperLimit( addr );
	}
	
	private native float jniGetUpperLimit( long addr );

	/**
	 *  Set the joint limits, usually in meters.
	 */
	public void setLimits(float lower, float upper)
	{
		jniSetLimits( addr, lower, upper );
	}
	
	private native void jniSetLimits( long addr, float lower, float upper );

	/**
	 *  Is the joint motor enabled?
	 */
	public boolean isMotorEnabled()
	{
		return jniIsMotorEnabled( addr );
	}

	private native boolean jniIsMotorEnabled( long addr );
	
	/**
	 * 	Enable/disable the joint motor.
	 */
	public void enableMotor(boolean flag)
	{
		jniEnableMotor( addr, flag );
	}

	private native void jniEnableMotor( long addr, boolean flag );
	
	/** 
	 * Set the motor speed, usually in meters per second.
	 */
	public void setMotorSpeed(float speed)
	{
		jniSetMotorSpeed( addr, speed );
	}

	private native void jniSetMotorSpeed( long addr, float speed );
	
	/**
	 *  Get the motor speed, usually in meters per second.
	 */
	public float getMotorSpeed()
	{
		return jniGetMotorSpeed( addr );
	}
	
	private native float jniGetMotorSpeed( long addr );

	/**
	 *  Set the maximum motor force, usually in N.
	 */
	public void setMaxMotorForce(float force)
	{
		jniSetMaxMotorForce( addr, force );
	}
	
	private native void jniSetMaxMotorForce( long addr, float force );

	/**
	 *  Get the current motor force, usually in N.
	 */
	public float getMotorForce()
	{
		return jniGetMotorForce( addr );
	}
	
	private native float jniGetMotorForce( long addr );

	
}
