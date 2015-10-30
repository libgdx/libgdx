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

/** Scales an actor's scale to a relative size.
 * @author Nathan Sweet */
public class ScaleByAction extends RelativeTemporalAction implements TemporalAction.ScaleAction {
	private float amountX, amountY;
	private float worldStartSpeedX, worldStartSpeedY;
	private boolean blending = false;

	protected void begin () {
		super.begin();
		if (blending) {
			setStartSpeed(2, worldStartSpeedX * getDuration() / amountX, worldStartSpeedY * getDuration() / amountY, 0, 0);
		}
	}

	protected void updateRelative (float percentDelta) {
		target.scaleBy(amountX * percentDelta, amountY * percentDelta);
	}

	protected void updateRelativeIndependently (float percentDelta0, float percentDelta1, float percentDelta2,
		float precentDelta3) {
		target.scaleBy(amountX * percentDelta0, amountY * percentDelta1);
	}

	public void reset () {
		super.reset();
		blending = false;
	}

	public void setAmount (float x, float y) {
		amountX = x;
		amountY = y;
	}

	public void setAmount (float scale) {
		amountX = scale;
		amountY = scale;
	}

	public float getAmountX () {
		return amountX;
	}

	public void setAmountX (float x) {
		this.amountX = x;
	}

	public float getAmountY () {
		return amountY;
	}

	public void setAmountY (float y) {
		this.amountY = y;
	}

	public float getWorldSpeedX () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * amountX / getDuration();
	}

	public float getWorldSpeedY () {
		if (getDuration() <= 0) return 0;
		return getSpeed1() * amountY / getDuration();
	}

	/** Set this interpolation to begin at a speed matching the current speed of an action that it is interrupting, so the
	 * transition will appear smooth. This must be called before removing the interrupted action from the actor. The interrupted
	 * action should be removed from the actor after calling this method. A {@linkplain SplineInterpolation} must be used. */
	public void setBlendFrom (ScaleAction interruptedAction, SplineInterpolation interpolation) {
		setInterpolation(interpolation);
		worldStartSpeedX = interruptedAction.getWorldSpeedX();
		worldStartSpeedY = interruptedAction.getWorldSpeedY();
		blending = true;
	}

	public void cancelBlendFrom () {
		blending = false;
	}
}
