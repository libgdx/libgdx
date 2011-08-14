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

public class WeldJointDef extends JointDef {
	public WeldJointDef () {
		type = JointType.WeldJoint;
	}

	// / Initialize the bodies, anchors, and reference angle using a world
	// / anchor point.
	public void initialize (Body body1, Body body2, Vector2 anchor) {
		this.bodyA = body1;
		this.bodyB = body2;
		this.localAnchorA.set(body1.getLocalPoint(anchor));
		this.localAnchorB.set(body2.getLocalPoint(anchor));
		referenceAngle = body2.getAngle() - body1.getAngle();
	}

	// / The local anchor point relative to body1's origin.
	public final Vector2 localAnchorA = new Vector2();

	// / The local anchor point relative to body2's origin.
	public final Vector2 localAnchorB = new Vector2();

	// / The body2 angle minus body1 angle in the reference state (radians).
	public float referenceAngle = 0;
}
