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

/** A distance joint constrains two points on two bodies to remain at a fixed distance from each other. You can view this as a
 * massless, rigid rod. */
public class DistanceJoint extends Joint {
	org.jbox2d.dynamics.joints.DistanceJoint joint;

	Vector2 localAnchorA = new Vector2();
	Vector2 localAnchorB = new Vector2();

	public DistanceJoint (World world, org.jbox2d.dynamics.joints.DistanceJoint joint) {
		super(world, joint);
		this.joint = joint;
	}

	public Vector2 getLocalAnchorA () {
		Vec2 localAnchor = joint.getLocalAnchorA();
		localAnchorA.set(localAnchor.x, localAnchor.y);
		return localAnchorA;
	}

	public Vector2 getLocalAnchorB () {
		Vec2 localAnchor = joint.getLocalAnchorB();
		localAnchorB.set(localAnchor.x, localAnchor.y);
		return localAnchorB;
	}

	/** Set/get the natural length. Manipulating the length can lead to non-physical behavior when the frequency is zero. */
	public void setLength (float length) {
		joint.setLength(length);
	}

	/** Set/get the natural length. Manipulating the length can lead to non-physical behavior when the frequency is zero. */
	public float getLength () {
		return joint.getLength();
	}

	/** Set/get frequency in Hz. */
	public void setFrequency (float hz) {
		joint.setFrequency(hz);
	}

	/** Set/get frequency in Hz. */
	public float getFrequency () {
		return joint.getFrequency();
	}

	/** Set/get damping ratio. */
	public void setDampingRatio (float ratio) {
		joint.setDampingRatio(ratio);
	}

	/** Set/get damping ratio. */
	public float getDampingRatio () {
		return joint.getDampingRatio();
	}
}
