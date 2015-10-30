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

/** Sets the actor's scale from its current value to a specific value.
 * @author Nathan Sweet */
public class ScaleToAction extends TemporalAction implements TemporalAction.ScaleAction {
	private float startX, startY;
	private float endX, endY;
	private float worldStartSpeedX, worldStartSpeedY;
	private boolean blending = false;

	protected void begin () {
		startX = target.getScaleX();
		startY = target.getScaleY();
		if (blending) {
			setStartSpeed(2, worldStartSpeedX * getDuration() / (endX - startX), worldStartSpeedY * getDuration() / (endY - startY),
				0, 0);
		}
	}

	protected void update (float percent) {
		target.setScale(startX + (endX - startX) * percent, startY + (endY - startY) * percent);
	}

	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3) {
		target.setScale(startX + (endX - startX) * percent0, startY + (endY - startY) * percent1);
	};

	public void reset () {
		super.reset();
		blending = false;
	}

	public void setScale (float x, float y) {
		endX = x;
		endY = y;
	}

	public void setScale (float scale) {
		endX = scale;
		endY = scale;
	}

	public float getX () {
		return endX;
	}

	public void setX (float x) {
		this.endX = x;
	}

	public float getY () {
		return endY;
	}

	public void setY (float y) {
		this.endY = y;
	}

	public float getWorldSpeedX () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * (endX - startX) / getDuration();
	}

	public float getWorldSpeedY () {
		if (getDuration() <= 0) return 0;
		return getSpeed1() * (endY - startY) / getDuration();
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
