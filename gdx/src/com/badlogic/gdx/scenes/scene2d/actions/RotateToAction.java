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

package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.math.Interpolation.SplineInterpolation;

/** Sets the actor's rotation from its current value to a specific value.
 * @author Nathan Sweet */
public class RotateToAction extends TemporalAction implements TemporalAction.RotateAction {
	private float start, end;
	private float worldStartSpeed;
	private boolean blending = false;

	protected void begin () {
		start = target.getRotation();
		if (blending) {
			setStartSpeed(2, worldStartSpeed * getDuration() / (end - start), 0, 0, 0);
		}
	}

	protected void update (float percent) {
		target.setRotation(start + (end - start) * percent);
	}

	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3) {
		target.setRotation(start + (end - start) * percent0);
	};

	public void reset () {
		super.reset();
		blending = false;
	}

	public float getRotation () {
		return end;
	}

	public void setRotation (float rotation) {
		this.end = rotation;
	}

	public float getWorldSpeed () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * (end - start) / getDuration();
	}

	/** Set this interpolation to begin at a speed matching the current speed of an action that it is interrupting, so the
	 * transition will appear smooth. This must be called before removing the interrupted action from the actor. The interrupted
	 * action should be removed from the actor after calling this method. A {@linkplain SplineInterpolation} must be used. */
	public void setBlendFrom (RotateAction interruptedAction, SplineInterpolation interpolation) {
		setInterpolation(interpolation);
		worldStartSpeed = interruptedAction.getWorldSpeed();
		blending = true;
	}

	public void cancelBlendFrom () {
		blending = false;
	}
}
