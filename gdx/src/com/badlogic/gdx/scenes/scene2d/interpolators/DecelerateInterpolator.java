/*
 * Copyright 2011 Moritz Post (moritzpost@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.scenes.scene2d.interpolators;

import com.badlogic.gdx.scenes.scene2d.Interpolator;
import com.badlogic.gdx.utils.Pool;

/**
 * An interpolator where the rate of change starts out fast and then decelerates over time.
 * 
 * @author Moritz Post <moritzpost@gmail.com>
 */
public class DecelerateInterpolator implements Interpolator {

	private static final float DEFAULT_FACTOR = 1.0f;

	private static final Pool<DecelerateInterpolator> pool = new Pool<DecelerateInterpolator>(4, 100) {
		@Override protected DecelerateInterpolator newObject () {
			return new DecelerateInterpolator();
		}
	};

	private float factor;

	private double doubledFactor;

	DecelerateInterpolator () {
		// hide constructor
	}

	/**
	 * Gets a new {@link DecelerateInterpolator} from a maintained pool of {@link Interpolator}s.
	 * 
	 * @param factor the factor controlling the rate of change
	 * @return the obtained {@link DecelerateInterpolator}
	 */
	public static DecelerateInterpolator $ (float factor) {
		DecelerateInterpolator inter = pool.obtain();
		inter.factor = factor;
		inter.doubledFactor = factor * 2;
		return inter;
	}

	/**
	 * Gets a new {@link DecelerateInterpolator} from a maintained pool of {@link Interpolator}s.
	 * <p>
	 * The initial factor is set to <code>{@value DecelerateInterpolator#DEFAULT_FACTOR}</code>.
	 * 
	 * @return the obtained {@link DecelerateInterpolator}
	 */
	public static DecelerateInterpolator $ () {
		return $(DEFAULT_FACTOR);
	}

	@Override public void finished () {
		pool.free(this);
	}

	public float getInterpolation (float input) {
		if (factor == 1.0f) {
			return 1.0f - (1.0f - input) * (1.0f - input);
		} else {
			return (float)(1.0f - Math.pow((1.0f - input), doubledFactor));
		}
	}

	@Override public Interpolator copy () {
		return $(factor);
	}
}
