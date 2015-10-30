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

/** Sets the actor's rotation from its current value to a relative value.
 * @author Nathan Sweet */
public class RotateByAction extends RelativeTemporalAction implements TemporalAction.RotateAction {
	private float amount;
	private float worldStartSpeed;
	private boolean blending = false;

	protected void begin () {
		super.begin();
		if (blending) {
			setStartSpeed(2, worldStartSpeed * getDuration() / amount, 0, 0, 0);
		}
	}

	protected void updateRelative (float percentDelta) {
		target.rotateBy(amount * percentDelta);
	}

	protected void updateRelativeIndependently (float percentDelta0, float percentDelta1, float percentDelta2,
		float precentDelta3) {
		target.rotateBy(amount * percentDelta0);
	}

	public float getAmount () {
		return amount;
	}

	public void setAmount (float rotationAmount) {
		amount = rotationAmount;
	}

	public float getWorldSpeed () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * amount / getDuration();
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
