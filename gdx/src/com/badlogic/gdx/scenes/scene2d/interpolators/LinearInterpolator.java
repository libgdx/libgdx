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
 * A very simple {@link Interpolator} which provides a linear progression by just returning the current input.
 * 
 * @author Moritz Post <moritzpost@gmail.com>
 */
public class LinearInterpolator implements Interpolator {

	private static final Pool<LinearInterpolator> pool = new Pool<LinearInterpolator>(4, 100) {
		@Override protected LinearInterpolator newObject () {
			return new LinearInterpolator();
		}
	};

	LinearInterpolator () {
		// hide constructor
	}

	/**
	 * Gets a new {@link LinearInterpolator} from a maintained pool of {@link Interpolator}s.
	 * 
	 * @return the obtained {@link LinearInterpolator}
	 */
	public static LinearInterpolator $ () {
		return pool.obtain();
	}

	@Override public void finished () {
		pool.free(this);
	}

	@Override public float getInterpolation (float input) {
		return input;
	}

	@Override public Interpolator copy () {
		return $();
	}
}
