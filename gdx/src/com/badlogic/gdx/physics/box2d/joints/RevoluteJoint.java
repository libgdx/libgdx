package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/**
 *  A revolute joint constrains two bodies to share a common point while they
 * are free to rotate about the point. The relative rotation about the shared
 * point is the joint angle. You can limit the relative rotation with
 * a joint limit that specifies a lower and upper angle. You can use a motor
 * to drive the relative rotation about the shared point. A maximum motor torque
 * is provided so that infinite forces are not generated.
 */
public class RevoluteJoint extends Joint
{
	public RevoluteJoint( World world, long addr) 
	{
		super(world,addr);	
	}

	/**
	 *  Get the current joint angle in radians.
	 */
	public float getJointAngle()
	{
		return jniGetJointAngle( addr );
	}

	private native float jniGetJointAngle( long addr );

	/**
	 *  Get the current joint angle speed in radians per second.
	 */
	public float getJointSpeed()
	{
		return jniGetJointSpeed( addr );
	}
	
	private native float jniGetJointSpeed( long addr );

	/**
	 *  Is the joint limit enabled?
	 */
	public boolean isLimitEnabled()
	{
		return jniIsLimitEnabled( addr );
	}
	
	private native boolean jniIsLimitEnabled( long addr );

	/**
	 * Enable/disable the joint limit.
	 */
	public void enableLimit(boolean flag)
	{
		jniEnableLimit( addr, flag );
	}
	
	private native void jniEnableLimit( long addr, boolean flag );

	/**
	 *  Get the lower joint limit in radians.
	 */
	public float getLowerLimit()
	{
		return jniGetLowerLimit( addr );
	}

	private native float jniGetLowerLimit( long addr );
	
	/**
	 * Get the upper joint limit in radians.
	 */
	public float getUpperLimit()
	{
		return jniGetUpperLimit( addr );
	}

	private native float jniGetUpperLimit( long addr );
	
	/**
	 *  Set the joint limits in radians.
	 * @param upper
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
	 *  Enable/disable the joint motor.
	 */
	public void enableMotor(boolean flag)
	{
		jniEnableMotor( addr, flag );
	}
	
	private native void jniEnableMotor( long addr, boolean flag );

	/**
	 *  Set the motor speed in radians per second.
	 */
	public void setMotorSpeed(float speed)
	{
		jniSetMotorSpeed( addr, speed );
	}
	
	private native void jniSetMotorSpeed( long addr, float speed );

	/**
	 *  Get the motor speed in radians per second.
	 */
	public float getMotorSpeed()
	{
		return jniGetMotorSpeed( addr );
	}
	
	private native float jniGetMotorSpeed(long addr );

	/**
	 *  Set the maximum motor torque, usually in N-m.
	 */
	public void setMaxMotorTorque(float torque)
	{
		jniSetMaxMotorTorque( addr, torque );
	}
	
	private native void jniSetMaxMotorTorque( long addr, float torque );

	/**
	 *  Get the current motor torque, usually in N-m.
	 */
	public float getMotorTorque()
	{
		return jniGetMotorTorque( addr );
	}
	
	private native float jniGetMotorTorque( long addr );
}
