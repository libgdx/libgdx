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

/** A weld joint essentially glues two bodies together. A weld joint may distort somewhat because the island constraint solver is
 * approximate. */
public class WeldJoint extends Joint {
	org.jbox2d.dynamics.joints.WeldJoint joint;

	Vector2 localAnchorA = new Vector2();
	Vector2 localAnchorB = new Vector2();

	public WeldJoint (World world, org.jbox2d.dynamics.joints.WeldJoint joint) {
		super(world, joint);
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

	public float getReferenceAngle () {
		return joint.getReferenceAngle();
	}
	
	public float getFrequency () {
		return joint.getFrequency();
	}
	
	public void setFrequency (float frequencyHz) {
		joint.setFrequency(frequencyHz);
	}
	
	public float getDampingRatio () {
		return joint.getDampingRatio();
	}
	
	public void setDampingRatio (float dampingRatio) {
		joint.setDampingRatio(dampingRatio);
	}
	
}
