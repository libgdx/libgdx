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
package com.badlogic.gdx.scenes.scene2d.interpolators;

import com.badlogic.gdx.scenes.scene2d.Interpolator;
import com.badlogic.gdx.utils.Pool;

/**
 * An interpolator where the rate of change starts out slowly, grows over time and ends slowly.
 * 
 * @author Moritz Post <moritzpost@gmail.com>
 */
public class AccelerateDecelerateInterpolator implements Interpolator {

	private static final float DEFAULT_FACTOR = 1.0f;

	private static final Pool<AccelerateDecelerateInterpolator> pool = new Pool<AccelerateDecelerateInterpolator>(4, 100) {
		@Override protected AccelerateDecelerateInterpolator newObject () {
			return new AccelerateDecelerateInterpolator();
		}
	};

	private float factor;

	private double doubledFactor;

	AccelerateDecelerateInterpolator () {
		// hide constructor
	}

	/**
	 * Gets a new {@link AccelerateDecelerateInterpolator} from a maintained pool of {@link Interpolator}s.
	 * 
	 * @param factor the factor controlling the rate of speed change
	 * @return the obtained {@link AccelerateDecelerateInterpolator}
	 */
	public static AccelerateDecelerateInterpolator $ (float factor) {
		AccelerateDecelerateInterpolator inter = pool.obtain();
		inter.factor = factor;
		inter.doubledFactor = factor * 2;
		return inter;
	}

	/**
	 * Gets a new {@link AccelerateDecelerateInterpolator} from a maintained pool of {@link Interpolator}s.
	 * <p>
	 * The initial factor is set to <code>{@value AccelerateDecelerateInterpolator#DEFAULT_FACTOR}</code>.
	 * 
	 * @return the obtained {@link AccelerateDecelerateInterpolator}
	 */
	public static AccelerateDecelerateInterpolator $ () {
		return $(DEFAULT_FACTOR);
	}

	@Override public void finished () {
		pool.free(this);
	}

	public float getInterpolation (float input) {
		return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
	}

	@Override public Interpolator copy () {
		return $(factor);
	}
}
