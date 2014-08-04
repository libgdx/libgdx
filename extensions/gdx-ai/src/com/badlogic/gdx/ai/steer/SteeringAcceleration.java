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

package com.badlogic.gdx.ai.steer;

import com.badlogic.gdx.math.Vector;

/** {@code SteeringAcceleration} is a movement requested by the steering system. It is made up of two components, linear and angular
 * acceleration.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class SteeringAcceleration<T extends Vector<T>> {
	/** The linear component of the steering acceleration. */
	public T linear;

	/** The angular component of the steering acceleration. */
	public float angular;

	/** Creates a {@code SteeringAcceleration} with the given linear acceleration and zero angular acceleration.
	 * 
	 * @param linear The initial linear acceleration to give this SteeringAcceleration. */
	public SteeringAcceleration (T linear) {
		this(linear, 0f);
	}

	/** Creates a {@code SteeringAcceleration} with the given linear and angular components.
	 * 
	 * @param linear The initial linear acceleration to give this SteeringAcceleration.
	 * @param angular The initial angular acceleration to give this SteeringAcceleration. */
	public SteeringAcceleration (T linear, float angular) {
		if (linear == null) throw new IllegalArgumentException("Linear acceleration cannot be null");
		this.linear = linear;
		this.angular = angular;
	}

	/** Zeros the linear and angular components of this steering acceleration.
	 * @return this steering acceleration for chaining */
	public SteeringAcceleration<T> setZero () {
		linear.setZero();
		angular = 0f;
		return this;
	}

	/** Adds the given steering acceleration to this steering acceleration.
	 * 
	 * @param steering the steering acceleration
	 * @return this steering acceleration for chaining */
	public SteeringAcceleration<T> add (SteeringAcceleration<T> steering) {
		linear.add(steering.linear);
		angular += steering.angular;
		return this;
	}

	/** Scales this steering acceleration by the specified scalar.
	 * 
	 * @param scalar the scalar
	 * @return this steering acceleration for chaining */
	public SteeringAcceleration<T> scl (float scalar) {
		linear.scl(scalar);
		angular *= scalar;
		return this;
	}

	/** First scale a supplied steering acceleration, then add it to this steering acceleration.
	 * 
	 * @param steering the steering acceleration
	 * @param scalar the scalar
	 * @return this steering acceleration for chaining */
	public SteeringAcceleration<T> mulAdd (SteeringAcceleration<T> steering, float scalar) {
		linear.mulAdd(steering.linear, scalar);
		angular += steering.angular * scalar;
		return this;
	}

	/** Returns the square of the magnitude of this steering acceleration. This includes the angular component. */
	public float calculateSquareMagnitude () {
		return linear.len2() + angular * angular;
	}

	/** Returns the magnitude of this steering acceleration. This includes the angular component. */
	public float calculateMagnitude () {
		return (float)Math.sqrt(calculateSquareMagnitude());
	}
}
