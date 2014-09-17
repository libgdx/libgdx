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

/** A {@code LinearLimiter} provides the maximum magnitudes of linear speed and linear acceleration. Angular methods throw an
 * {@link UnsupportedOperationException}.
 * 
 * @author davebaol */
public class LinearLimiter extends NullLimiter {

	private float maxLinearAcceleration;
	private float maxLinearSpeed;

	/** Creates a {@code LinearLimiter}.
	 * @param maxLinearAcceleration the maximum linear acceleration
	 * @param maxLinearSpeed the maximum linear speed */
	public LinearLimiter (float maxLinearAcceleration, float maxLinearSpeed) {
		this.maxLinearAcceleration = maxLinearAcceleration;
		this.maxLinearSpeed = maxLinearSpeed;
	}

	/** Returns the maximum linear speed. */
	@Override
	public float getMaxLinearSpeed () {
		return maxLinearSpeed;
	}

	/** Sets the maximum linear speed. */
	@Override
	public void setMaxLinearSpeed (float maxLinearSpeed) {
		this.maxLinearSpeed = maxLinearSpeed;
	}

	/** Returns the maximum linear acceleration. */
	@Override
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	/** Sets the maximum linear acceleration. */
	@Override
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

}
