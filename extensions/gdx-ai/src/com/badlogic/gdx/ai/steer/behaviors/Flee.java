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

package com.badlogic.gdx.ai.steer.behaviors;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector;

/** {@code Flee} behavior does the opposite of {@link Seek}. It produces a linear steering force that moves the agent away from a
 * target position.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Flee<T extends Vector<T>> extends Seek<T> {

	/** Creates a {@code Flee} behavior for the specified owner, target and maximum linear acceleration.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used to flee from the target. */
	public Flee (Steerable<T> owner, Steerable<T> target, float maxLinearAcceleration) {
		super(owner, target, maxLinearAcceleration);
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// Calculate the desired velocity using a vector pointing in the opposite direction of seek,
		// i.e. (owner.getPosition() - targetPosition) instead of (targetPosition - owner.getPosition())
		steering.linear.set(owner.getPosition()).sub(target.getPosition()).nor().scl(maxLinearAcceleration);

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

}
