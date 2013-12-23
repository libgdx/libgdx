/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.JointDef;

/** Distance joint definition. This requires defining an anchor point on both bodies and the non-zero length of the distance joint.
 * The definition uses local anchor points so that the initial configuration can violate the constraint slightly. This helps when
 * saving and loading a game.
 * @warning Do not use a zero or short length. */
public class DistanceJointDef extends JointDef {
	public DistanceJointDef () {
		type = JointType.DistanceJoint;
	}

	/** Initialize the bodies, anchors, and length using the world anchors. */
	public void initialize (Body bodyA, Body bodyB, Vector2 anchorA, Vector2 anchorB) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.localAnchorA.set(bodyA.getLocalPoint(anchorA));
		this.localAnchorB.set(bodyB.getLocalPoint(anchorB));
		this.length = anchorA.dst(anchorB);
	}

	/** The local anchor point relative to body1's origin. */
	public final Vector2 localAnchorA = new Vector2();

	/** The local anchor point relative to body2's origin. */
	public final Vector2 localAnchorB = new Vector2();

	/** The natural length between the anchor points. */
	public float length = 1;

	/** The mass-spring-damper frequency in Hertz. */
	public float frequencyHz = 0;

	/** The damping ratio. 0 = no damping, 1 = critical damping. */
	public float dampingRatio = 0;

	public org.jbox2d.dynamics.joints.DistanceJointDef toJBox2d () {
		org.jbox2d.dynamics.joints.DistanceJointDef fd = new org.jbox2d.dynamics.joints.DistanceJointDef();
		fd.bodyA = bodyA.body;
		fd.bodyB = bodyB.body;
		fd.collideConnected = collideConnected;
		fd.dampingRatio = dampingRatio;
		fd.frequencyHz = frequencyHz;
		fd.length = length;
		fd.localAnchorA.set(localAnchorA.x, localAnchorA.y);
		fd.localAnchorB.set(localAnchorB.x, localAnchorB.y);
		fd.type = org.jbox2d.dynamics.joints.JointType.DISTANCE;
		return fd;
	}
}
