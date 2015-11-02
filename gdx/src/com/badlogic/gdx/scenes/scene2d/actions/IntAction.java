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

/** An action that has an int, whose value is transitioned over time.
 * @author Nathan Sweet */
public class IntAction extends TemporalAction {
	private int start, end;
	private int value;
	private float worldStartSpeed;
	private boolean blending = false;

	/** Creates an IntAction that transitions from 0 to 1. */
	public IntAction () {
		start = 0;
		end = 1;
	}

	/** Creates an IntAction that transitions from start to end. */
	public IntAction (int start, int end) {
		this.start = start;
		this.end = end;
	}

	protected void begin () {
		value = start;
		if (blending) {
			setStartSpeed(1, worldStartSpeed * getDuration() / (end - start), 0, 0, 0);
		}
	}

	protected void update (float percent) {
		value = (int)(start + (end - start) * percent);
	}

	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3) {
		value = (int)(start + (end - start) * percent0);
	}

	/** Gets the current int value. */
	public int getValue () {
		return value;
	}

	/** Sets the current int value. */
	public void setValue (int value) {
		this.value = value;
	}

	public int getStart () {
		return start;
	}

	/** Sets the value to transition from. */
	public void setStart (int start) {
		this.start = start;
	}

	public int getEnd () {
		return end;
	}

	/** Sets the value to transition to. */
	public void setEnd (int end) {
		this.end = end;
	}

	public float getWorldSpeed () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * (end - start) / getDuration();
	}

	/** Set this interpolation to begin at a speed matching the current speed of an action that it is interrupting, so the
	 * transition will appear smooth. This must be called before removing the interrupted action from the actor. The interrupted
	 * action should be removed from the actor after calling this method. A {@linkplain SplineInterpolation} must be used. */
	public void setBlendFrom (IntAction interruptedAction, SplineInterpolation interpolation) {
		setInterpolation(interpolation);
		worldStartSpeed = interruptedAction.getWorldSpeed();
		blending = true;
	}

	public void cancelBlendFrom () {
		blending = false;
	}
}
