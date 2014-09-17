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

/** An {@code AngularSpeedLimiter} provides the maximum magnitudes of angular speed. All other methods throw an
 * {@link UnsupportedOperationException}.
 * 
 * @author davebaol */
public class AngularSpeedLimiter extends NullLimiter {

	private float maxAngularSpeed;

	/** Creates an {@code AngularSpeedLimiter}.
	 * @param maxAngularSpeed the maximum angular speed */
	public AngularSpeedLimiter (float maxAngularSpeed) {
		this.maxAngularSpeed = maxAngularSpeed;
	}

	/** Returns the maximum angular speed. */
	@Override
	public float getMaxAngularSpeed () {
		return maxAngularSpeed;
	}

	/** Sets the maximum angular speed. */
	@Override
	public void setMaxAngularSpeed (float maxAngularSpeed) {
		this.maxAngularSpeed = maxAngularSpeed;
	}

}
