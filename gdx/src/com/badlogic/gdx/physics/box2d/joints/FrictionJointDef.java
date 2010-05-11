package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.JointDef;

/**
 * Friction joint definition. 
 */
public class FrictionJointDef extends JointDef{
	
	public FrictionJointDef( )
	{
		type = JointType.FrictionJoint;
	}
	
	/**
	 *  Initialize the bodies, anchors, axis, and reference angle using the world
	 * anchor and world axis.
	 */
	public void initialize(Body bodyA, Body bodyB, Vector2 anchor)
	{
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set( bodyA.getLocalPoint( anchor ) );
		localAnchorB.set( bodyB.getLocalPoint( anchor ) );
	}

	/**
	 *  The local anchor point relative to bodyA's origin.
	 */
	public final Vector2 localAnchorA = new Vector2();

	/**
	 *  The local anchor point relative to bodyB's origin.
	 */
	public final Vector2 localAnchorB = new Vector2();

	/**
	 *  The maximum friction force in N.
	 */
	public float maxForce = 0;

	/**
	 *  The maximum friction torque in N-m.
	 */
	public float maxTorque = 0;
}
