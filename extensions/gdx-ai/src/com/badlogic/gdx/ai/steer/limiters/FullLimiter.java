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

package com.badlogic.gdx.ai.steer.limiters;

import com.badlogic.gdx.ai.steer.Limiter;

/** A {@code FullLimiter} provides the maximum magnitudes of speed and acceleration for both linear and angular components.
 * 
 * @author davebaol */
public class FullLimiter implements Limiter {

	private float maxLinearAcceleration;
	private float maxLinearSpeed;
	private float maxAngularAcceleration;
	private float maxAngularSpeed;

	/** Creates a {@code FullLimiter}.
	 * @param maxLinearAcceleration the maximum linear acceleration
	 * @param maxLinearSpeed the maximum linear speed
	 * @param maxAngularAcceleration the maximum angular acceleration
	 * @param maxAngularSpeed the maximum angular speed */
	public FullLimiter (float maxLinearAcceleration, float maxLinearSpeed, float maxAngularAcceleration, float maxAngularSpeed) {
		this.maxLinearAcceleration = maxLinearAcceleration;
		this.maxLinearSpeed = maxLinearSpeed;
		this.maxAngularAcceleration = maxAngularAcceleration;
		this.maxAngularSpeed = maxAngularSpeed;
	}

	@Override
	public float getMaxLinearSpeed () {
		return maxLinearSpeed;
	}

	@Override
	public void setMaxLinearSpeed (float maxLinearSpeed) {
		this.maxLinearSpeed = maxLinearSpeed;
	}

	@Override
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	@Override
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	@Override
	public float getMaxAngularSpeed () {
		return maxAngularSpeed;
	}

	@Override
	public void setMaxAngularSpeed (float maxAngularSpeed) {
		this.maxAngularSpeed = maxAngularSpeed;
	}

	@Override
	public float getMaxAngularAcceleration () {
		return maxAngularAcceleration;
	}

	@Override
	public void setMaxAngularAcceleration (float maxAngularAcceleration) {
		this.maxAngularAcceleration = maxAngularAcceleration;
	}
}
