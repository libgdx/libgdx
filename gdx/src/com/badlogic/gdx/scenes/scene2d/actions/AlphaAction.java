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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation.SplineInterpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Sets the alpha for an actor's color (or a specified color), from the current alpha to the new alpha. Note this action
 * transitions from the alpha at the time the action starts to the specified alpha.
 * @author Nathan Sweet */
public class AlphaAction extends TemporalAction {
	private float start, end;
	private Color color;
	private float worldStartSpeed;
	private boolean blending;

	protected void begin () {
		if (color == null) color = target.getColor();
		start = color.a;
		if (blending) {
			setStartSpeed(1, worldStartSpeed * getDuration() / (end - start), 0, 0, 0);
		}
	}

	protected void update (float percent) {
		color.a = start + (end - start) * percent;
	}

	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3) {
		color.a = MathUtils.clamp(start + (end - start) * percent0, 0, 1);
	};

	public void reset () {
		super.reset();
		color = null;
		blending = false;
	}

	public Color getColor () {
		return color;
	}

	/** Sets the color to modify. If null (the default), the {@link #getActor() actor's} {@link Actor#getColor() color} will be
	 * used. */
	public void setColor (Color color) {
		this.color = color;
	}

	public float getAlpha () {
		return end;
	}

	public void setAlpha (float alpha) {
		this.end = alpha;
	}

	public float getWorldSpeed () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * (end - start) / getDuration();
	}

	/** Set this interpolation to begin at a speed matching the current speed of an action that it is interrupting, so the
	 * transition will appear smooth. This must be called before removing the interrupted action from the actor. The interrupted
	 * action should be removed from the actor after calling this method. A {@linkplain SplineInterpolation} must be used. */
	public void setBlendFrom (AlphaAction interruptedAction, SplineInterpolation interpolation) {
		setInterpolation(interpolation);
		worldStartSpeed = interruptedAction.getWorldSpeed();
		blending = true;
	}

	public void cancelBlendFrom () {
		blending = false;
	}
}
