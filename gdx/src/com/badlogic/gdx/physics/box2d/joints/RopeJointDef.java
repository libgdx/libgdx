
package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointDef;

/** Rope joint definition. This requires two body anchor points and a maximum lengths. Note: by default the connected objects will
 * not collide. see collideConnected in b2JointDef.
 * @author mzechner */
public class RopeJointDef extends JointDef {
	public RopeJointDef () {
		type = JointType.RopeJoint;
	}

	/** The local anchor point relative to bodyA's origin. **/
	public final Vector2 localAnchorA = new Vector2(-1, 0);

	/** The local anchor point relative to bodyB's origin. **/
	public final Vector2 localAnchorB = new Vector2(1, 0);

	/** The maximum length of the rope. Warning: this must be larger than b2_linearSlop or the joint will have no effect. */
	public float maxLength = 0;
}
