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

/** {@code NeutralConstantLimiter}'s getters always return {@link Float#POSITIVE_INFINITY} while its setters throw
 * {@link UnsupportedOperationException}.
 * 
 * @author davebaol */
public class NeutralConstantLimiter extends NullLimiter {
	
	public static final NeutralConstantLimiter LIMITER = new NeutralConstantLimiter();

	/** Creates a {@code NullLimiter}. */
	public NeutralConstantLimiter () {
	}

	/** Returns {@link Float#POSITIVE_INFINITY}. */
	@Override
	public float getMaxLinearSpeed () {
		return Float.POSITIVE_INFINITY;
	}

	/** Returns {@link Float#POSITIVE_INFINITY}. */
	@Override
	public float getMaxLinearAcceleration () {
		return Float.POSITIVE_INFINITY;
	}

	/** Returns {@link Float#POSITIVE_INFINITY}. */
	@Override
	public float getMaxAngularSpeed () {
		return Float.POSITIVE_INFINITY;
	}

	/** Returns {@link Float#POSITIVE_INFINITY}. */
	@Override
	public float getMaxAngularAcceleration () {
		return Float.POSITIVE_INFINITY;
	}

}
