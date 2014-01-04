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

import org.jbox2d.common.Vec2;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/** The pulley joint is connected to two bodies and two fixed ground points. The pulley supports a ratio such that: length1 + ratio
 * * length2 <= constant Yes, the force transmitted is scaled by the ratio. The pulley also enforces a maximum length limit on
 * both sides. This is useful to prevent one side of the pulley hitting the top. */
public class PulleyJoint extends Joint {
	org.jbox2d.dynamics.joints.PulleyJoint joint;

	public PulleyJoint (World world, org.jbox2d.dynamics.joints.PulleyJoint joint) {
		super(world, joint);
		this.joint = joint;
	}

	/** Get the first ground anchor. */
	private final Vector2 groundAnchorA = new Vector2();

	public Vector2 getGroundAnchorA () {
		Vec2 g = joint.getGroundAnchorA();
		return groundAnchorA.set(g.x, g.y);
	}

	/** Get the second ground anchor. */
	private final Vector2 groundAnchorB = new Vector2();

	public Vector2 getGroundAnchorB () {
		Vec2 g = joint.getGroundAnchorB();
		return groundAnchorB.set(g.x, g.y);
	}

	/** Get the current length of the segment attached to body1. */
	public float getLength1 () {
		return joint.getLength1();
	}

	/** Get the current length of the segment attached to body2. */
	public float getLength2 () {
		return joint.getLength2();
	}

	/** Get the pulley ratio. */
	public float getRatio () {
		return joint.getRatio();
	}
}
