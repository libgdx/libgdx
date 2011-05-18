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
 * An {@link Interpolator} where the changes start backwards and than spring forward as the time progresses.
 * 
 * @author Moritz Post <moritzpost@gmail.com>
 */
public class AnticipateInterpolator implements Interpolator {

	private static final float DEFAULT_TENSION = 2.0f;

	private static final Pool<AnticipateInterpolator> pool = new Pool<AnticipateInterpolator>(4, 100) {
		@Override protected AnticipateInterpolator newObject () {
			return new AnticipateInterpolator();
		}
	};

	private float tension;

	AnticipateInterpolator () {
		// hide from the world
	}

	/**
	 * Gets a new {@link AnticipateInterpolator} from a maintained pool of {@link Interpolator}s.
	 * 
	 * @param tension the tension controlling the rate spring effect of the animation
	 * @return the obtained {@link AccelerateInterpolator}
	 */
	public static AnticipateInterpolator $ (float tension) {
		AnticipateInterpolator inter = pool.obtain();
		inter.tension = tension;
		return inter;
	}

	/**
	 * Gets a new {@link AnticipateInterpolator} from a maintained pool of {@link Interpolator}s.
	 * <p>
	 * The initial tension is set to <code>{@value AnticipateInterpolator#DEFAULT_TENSION}</code>.
	 * 
	 * @return the obtained {@link AnticipateInterpolator}
	 */
	public static AnticipateInterpolator $ () {
		return $(DEFAULT_TENSION);
	}

	@Override public void finished () {
		pool.free(this);
	}

	public float getInterpolation (float t) {
		return t * t * ((tension + 1) * t - tension);
	}

	@Override public Interpolator copy () {
		return $(tension);
	}
}
