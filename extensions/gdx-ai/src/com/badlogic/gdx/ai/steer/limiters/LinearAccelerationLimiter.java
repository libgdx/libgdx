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

/** A {@code LinearAccelerationLimiter} provides the maximum magnitude of linear acceleration. All other methods throw an
 * {@link UnsupportedOperationException}.
 * 
 * @author davebaol */
public class LinearAccelerationLimiter extends NullLimiter {

	private float maxLinearAcceleration;

	/** Creates a {@code LinearAccelerationLimiter}.
	 * @param maxLinearAcceleration the maximum linear acceleration */
	public LinearAccelerationLimiter (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
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
