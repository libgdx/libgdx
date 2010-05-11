package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;

/**
 *  Gear joint definition. This definition requires two existing
 * revolute or prismatic joints (any combination will work).
 * The provided joints must attach a dynamic body to a static body.
 */
public class GearJointDef extends JointDef
{
	public GearJointDef( )
	{
		type = JointType.GearJoint;
	}
	
	/**
	 *  The first revolute/prismatic joint attached to the gear joint.
	 */
	public Joint joint1 = null;

	/**
	 *  The second revolute/prismatic joint attached to the gear joint.
	 */
	public Joint joint2 = null;

	/**
	 * The gear ratio.	 
	 * @see GearJoint for explanation.
	 */
	public float ratio = 1;
}
