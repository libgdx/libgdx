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
import com.badlogic.gdx.physics.box2d.JointDef;

/** Mouse joint definition. This requires a world target point, tuning parameters, and the time step. */
public class MouseJointDef extends JointDef {
	public MouseJointDef () {
		type = JointType.MouseJoint;
	}

	/** The initial world target point. This is assumed to coincide with the body anchor initially. */
	public final Vector2 target = new Vector2();

	/** The maximum constraint force that can be exerted to move the candidate body. Usually you will express as some multiple of
	 * the weight (multiplier * mass * gravity). */
	public float maxForce = 0;

	/** The response speed. */
	public float frequencyHz = 5.0f;

	/** The damping ratio. 0 = no damping, 1 = critical damping. */
	public float dampingRatio = 0.7f;
}
