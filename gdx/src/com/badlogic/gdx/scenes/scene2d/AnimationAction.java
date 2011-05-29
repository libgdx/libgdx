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
package com.badlogic.gdx.scenes.scene2d;

/**
 * An {@link AnimationAction} performs a transformation on its target {@link Actor}. These transformations physically change the
 * Actor itself.
 * 
 * @author Moritz Post <moritzpost@gmail.com>
 */
public abstract class AnimationAction extends Action {

	protected float duration;
	protected float invDuration;
	protected float taken;
	protected Actor target;
	protected boolean done;

	protected Interpolator interpolator;

	@Override public boolean isDone () {
		return done;
	}

	@Override public void finish () {
		super.finish();
		if (interpolator != null) {
			interpolator.finished();
		}
	}

	/**
	 * Sets an {@link Interpolator} to modify the progression of the animations.
	 * 
	 * @param interpolator the interpolator to use during the animation
	 * @return an instance of self so that the call can be easily chained
	 */
	public AnimationAction setInterpolator (Interpolator interpolator) {
		this.interpolator = interpolator;
		return this;
	}

	protected float createInterpolatedAlpha (float delta) {
		taken += delta;
		if (taken >= duration) {
			taken = duration;
			done = true;
			return taken;
		} else if (interpolator == null) {
			return taken * invDuration;
		} else {
			float interpolatedTime = interpolator.getInterpolation(taken / duration) * duration;
			return interpolatedTime * invDuration;
		}
	}
	
	public Actor getTarget () {
		return target;
	}

	@Override public void reset () {
		super.reset();
		interpolator = null;
	}
}
