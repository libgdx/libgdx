package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.JointDef;

/**
 * Distance joint definition. This requires defining an 
 * anchor point on both bodies and the non-zero length of the
 * distance joint. The definition uses local anchor points
 * so that the initial configuration can violate the constraint
 * slightly. This helps when saving and loading a game.
 * @warning Do not use a zero or short length.
 */
public class DistanceJointDef extends JointDef
{
	public DistanceJointDef( )
	{
		type = JointType.DistanceJoint;
	}
	
	/**
	 * Initialize the bodies, anchors, and length using the world
	 * anchors.
	 */
	public void initialize( Body bodyA, Body bodyB, Vector2 anchorA, Vector2 anchorB )
	{
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.localAnchorA.set( bodyA.getLocalPoint( anchorA ) );
		this.localAnchorB.set( bodyB.getLocalPoint( anchorB ) );
		this.length = anchorA.dst(anchorB);
	}
	
	/**
	 *  The local anchor point relative to body1's origin.
	 */
	public final Vector2 localAnchorA = new Vector2();
	
	/**
	 *  The local anchor point relative to body2's origin.
	 */
	public final Vector2 localAnchorB = new Vector2();
	
	/**
	 *  The natural length between the anchor points.
	 */
	public float length = 1;
	
	/**
	 *  The mass-spring-damper frequency in Hertz.
	 */
	public float frequencyHz = 0;
	
	/**
	 * The damping ratio. 0 = no damping, 1 = critical damping.
	 */
	public float dampingRatio = 0;
}
