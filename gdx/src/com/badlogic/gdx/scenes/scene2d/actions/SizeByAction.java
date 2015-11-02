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

/** Moves an actor from its current size to a relative size.
 * @author Nathan Sweet */
public class SizeByAction extends RelativeTemporalAction implements TemporalAction.SizeAction {
	private float amountWidth, amountHeight;
	private float worldStartSpeedWidth, worldStartSpeedHeight;
	private boolean blending = false;

	protected void begin () {
		super.begin();
		if (blending) {
			setStartSpeed(2, worldStartSpeedWidth * getDuration() / amountWidth,
				worldStartSpeedHeight * getDuration() / amountHeight, 0, 0);
		}
	}

	protected void updateRelative (float percentDelta) {
		target.sizeBy(amountWidth * percentDelta, amountHeight * percentDelta);
	}

	protected void updateRelativeIndependently (float percentDelta0, float percentDelta1, float percentDelta2,
		float precentDelta3) {
		target.sizeBy(amountWidth * percentDelta0, amountHeight * percentDelta1);
	}

	public void reset () {
		super.reset();
		blending = false;
	}

	public void setAmount (float width, float height) {
		amountWidth = width;
		amountHeight = height;
	}

	public float getAmountWidth () {
		return amountWidth;
	}

	public void setAmountWidth (float width) {
		amountWidth = width;
	}

	public float getAmountHeight () {
		return amountHeight;
	}

	public void setAmountHeight (float height) {
		amountHeight = height;
	}

	public float getWorldSpeedWidth () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * amountWidth / getDuration();
	}

	public float getWorldSpeedHeight () {
		if (getDuration() <= 0) return 0;
		return getSpeed1() * amountHeight / getDuration();
	}

	/** Set this interpolation to begin at a speed matching the current speed of an action that it is interrupting, so the
	 * transition will appear smooth. This must be called before removing the interrupted action from the actor. The interrupted
	 * action should be removed from the actor after calling this method. A {@linkplain SplineInterpolation} must be used. */
	public void setBlendFrom (SizeAction interruptedAction, SplineInterpolation interpolation) {
		setInterpolation(interpolation);
		worldStartSpeedWidth = interruptedAction.getWorldSpeedWidth();
		worldStartSpeedHeight = interruptedAction.getWorldSpeedHeight();
		blending = true;
	}

	public void cancelBlendFrom () {
		blending = false;
	}
}
