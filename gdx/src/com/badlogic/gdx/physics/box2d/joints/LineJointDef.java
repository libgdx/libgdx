package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.JointDef;

/**
 *  Line joint definition. This requires defining a line of
 * motion using an axis and an anchor point. The definition uses local
 * anchor points and a local axis so that the initial configuration
 * can violate the constraint slightly. The joint translation is zero
 * when the local anchor points coincide in world space. Using local
 * anchors and a local axis helps when saving and loading a game.
 */
public class LineJointDef extends JointDef
{
	public LineJointDef( )
	{
		type = JointType.LineJoint;
	}
	
	/**
	 *  Initialize the bodies, anchors, axis, and reference angle using the world
	 * anchor and world axis.
	 */
	public void initialize(Body bodyA, Body bodyB, Vector2 anchor, Vector2 axis)
	{
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set( bodyA.getLocalPoint( anchor ) );
		localAnchorB.set( bodyB.getLocalPoint( anchor ) );
		localAxisA.set( bodyA.getLocalVector(axis) );
	}

	/**
	 *  The local anchor point relative to body1's origin.
	 */
	public final Vector2 localAnchorA = new Vector2( );

	/**
	 *  The local anchor point relative to body2's origin.
	 */
	public final Vector2 localAnchorB = new Vector2( );

	/**
	 *  The local translation axis in body1.
	 */
	public final Vector2 localAxisA = new Vector2( 1.0f, 0 );

	/**
	 *  Enable/disable the joint limit.
	 */
	public boolean enableLimit = false;

	/**
	 *  The lower translation limit, usually in meters.
	 */
	public float lowerTranslation = 0;

	/**
	 *  The upper translation limit, usually in meters.
	 */
	public float upperTranslation = 0;

	/**
	 *  Enable/disable the joint motor.
	 */
	public boolean enableMotor = false;

	/**
	 *  The maximum motor torque, usually in N-m.
	 */
	public float maxMotorForce = 0;

	/**
	 *  The desired motor speed in radians per second.
	 */
	public float motorSpeed = 0;
}
