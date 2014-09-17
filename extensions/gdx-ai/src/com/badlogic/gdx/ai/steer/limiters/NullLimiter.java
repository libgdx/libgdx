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

/** A {@code NullLimiter} always throws {@link UnsupportedOperationException}. Typically it's used as the base class of partial
 * or immutable limiters.
 * 
 * @author davebaol */
public class NullLimiter implements Limiter {

	/** An immutable limiter whose getters return {@link Float#POSITIVE_INFINITY} and setters throw
	 * {@link UnsupportedOperationException}. */
	public static final NullLimiter NEUTRAL_LIMITER = new NullLimiter() {

		@Override
		public float getMaxLinearSpeed () {
			return Float.POSITIVE_INFINITY;
		}

		@Override
		public float getMaxLinearAcceleration () {
			return Float.POSITIVE_INFINITY;
		}

		@Override
		public float getMaxAngularSpeed () {
			return Float.POSITIVE_INFINITY;
		}

		@Override
		public float getMaxAngularAcceleration () {
			return Float.POSITIVE_INFINITY;
		}

	};

	/** Creates a {@code NullLimiter}. */
	public NullLimiter () {
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public float getMaxLinearSpeed () {
		throw new UnsupportedOperationException();
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public void setMaxLinearSpeed (float maxLinearSpeed) {
		throw new UnsupportedOperationException();
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public float getMaxLinearAcceleration () {
		throw new UnsupportedOperationException();
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		throw new UnsupportedOperationException();
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public float getMaxAngularSpeed () {
		throw new UnsupportedOperationException();
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public void setMaxAngularSpeed (float maxAngularSpeed) {
		throw new UnsupportedOperationException();
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public float getMaxAngularAcceleration () {
		throw new UnsupportedOperationException();
	}

	/** Guaranteed to throw UnsupportedOperationException.
	 * @throws UnsupportedOperationException always */
	@Override
	public void setMaxAngularAcceleration (float maxAngularAcceleration) {
		throw new UnsupportedOperationException();
	}
}
