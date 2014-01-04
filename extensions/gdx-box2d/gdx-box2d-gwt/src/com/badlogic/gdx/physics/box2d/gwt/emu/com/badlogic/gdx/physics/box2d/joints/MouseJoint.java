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

/** A mouse joint is used to make a point on a body track a specified world point. This a soft constraint with a maximum force.
 * This allows the constraint to stretch and without applying huge forces. NOTE: this joint is not documented in the manual
 * because it was developed to be used in the testbed. If you want to learn how to use the mouse joint, look at the testbed. */
public class MouseJoint extends Joint {
	org.jbox2d.dynamics.joints.MouseJoint joint;
	final Vec2 tmp = new Vec2();

	public MouseJoint (World world, org.jbox2d.dynamics.joints.MouseJoint joint) {
		super(world, joint);
		this.joint = joint;
	}

	/** Use this to update the target point. */
	public void setTarget (Vector2 target) {
		tmp.set(target.x, target.y);
		joint.setTarget(tmp);
	}

	/** Use this to update the target point. */
	private final Vector2 target = new Vector2();

	public Vector2 getTarget () {
		Vec2 t = joint.getTarget();
		return target.set(t.x, t.y);
	}

	/** Set/get the maximum force in Newtons. */
	public void setMaxForce (float force) {
		joint.setMaxForce(force);
	}

	/** Set/get the maximum force in Newtons. */
	public float getMaxForce () {
		return joint.getMaxForce();
	}

	/** Set/get the frequency in Hertz. */
	public void setFrequency (float hz) {
		joint.setFrequency(hz);
	}

	/** Set/get the frequency in Hertz. */
	public float getFrequency () {
		return joint.getFrequency();
	}

	/** Set/get the damping ratio (dimensionless). */
	public void setDampingRatio (float ratio) {
		joint.setDampingRatio(ratio);
	}

	/** Set/get the damping ratio (dimensionless). */
	public float getDampingRatio () {
		return joint.getDampingRatio();
	}
}
