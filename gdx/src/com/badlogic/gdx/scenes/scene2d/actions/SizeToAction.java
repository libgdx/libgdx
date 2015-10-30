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
import com.badlogic.gdx.utils.Align;

/** Moves an actor from its current size to a specific size.
 * @author Nathan Sweet */
public class SizeToAction extends TemporalAction implements TemporalAction.SizeAction {
	private float startWidth, startHeight;
	private float endWidth, endHeight;
	private float worldStartSpeedWidth, worldStartSpeedHeight;
	private boolean blending = false;

	protected void begin () {
		startWidth = target.getWidth();
		startHeight = target.getHeight();
		if (blending) {
			setStartSpeed(2, worldStartSpeedWidth * getDuration() / (endWidth - startWidth),
				worldStartSpeedHeight * getDuration() / (endHeight - startHeight), 0, 0);
		}
	}

	protected void update (float percent) {
		target.setSize(startWidth + (endWidth - startWidth) * percent, startHeight + (endHeight - startHeight) * percent);
	}

	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3) {
		target.setSize(startWidth + (endWidth - startWidth) * percent0, startHeight + (endHeight - startHeight) * percent1);
	};

	public void reset () {
		super.reset();
		blending = false;
	}

	public void setSize (float width, float height) {
		endWidth = width;
		endHeight = height;
	}

	public float getWidth () {
		return endWidth;
	}

	public void setWidth (float width) {
		endWidth = width;
	}

	public float getHeight () {
		return endHeight;
	}

	public void setHeight (float height) {
		endHeight = height;
	}

	public float getWorldSpeedWidth () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * (endWidth - startWidth) / getDuration();
	}

	public float getWorldSpeedHeight () {
		if (getDuration() <= 0) return 0;
		return getSpeed1() * (endHeight - startHeight) / getDuration();
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
