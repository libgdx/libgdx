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
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Sets the actor's color (or a specified color), from the current to the new color. Note this action transitions from the color
 * at the time the action starts to the specified color.
 * @author Nathan Sweet */
public class ColorAction extends TemporalAction {
	private float startR, startG, startB, startA;
	private Color color;
	private final Color end = new Color();
	private float worldStartSpeedR, worldStartSpeedG, worldStartSpeedB, worldStartSpeedA;
	private boolean blending = false;

	protected void begin () {
		if (color == null) color = target.getColor();
		startR = color.r;
		startG = color.g;
		startB = color.b;
		startA = color.a;
		if (blending) {
			setStartSpeed(4, worldStartSpeedR * getDuration() / (end.r - startR),
				worldStartSpeedG * getDuration() / (end.g - startG), worldStartSpeedB * getDuration() / (end.b - startB),
				worldStartSpeedA * getDuration() / (end.a - startA));
		}
	}

	protected void update (float percent) {
		float r = startR + (end.r - startR) * percent;
		float g = startG + (end.g - startG) * percent;
		float b = startB + (end.b - startB) * percent;
		float a = startA + (end.a - startA) * percent;
		color.set(r, g, b, a);
	}

	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3) {
		float r = startR + (end.r - startR) * percent0;
		float g = startG + (end.g - startG) * percent1;
		float b = startB + (end.b - startB) * percent2;
		float a = startA + (end.a - startA) * percent3;
		color.set(r, g, b, a);
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

	public Color getEndColor () {
		return end;
	}

	/** Sets the color to transition to. Required. */
	public void setEndColor (Color color) {
		end.set(color);
	}

	public float getWorldSpeedRed () {
		if (getDuration() <= 0) return 0;
		return getSpeed0() * (end.r - startR) / getDuration();
	}

	public float getWorldSpeedGreen () {
		if (getDuration() <= 0) return 0;
		return getSpeed1() * (end.g - startG) / getDuration();
	}

	public float getWorldSpeedBlue () {
		if (getDuration() <= 0) return 0;
		return getSpeed2() * (end.b - startB) / getDuration();
	}

	public float getWorldSpeedAlpha () {
		if (getDuration() <= 0) return 0;
		return getSpeed3() * (end.a - startA) / getDuration();
	}

	/** Set this interpolation to begin at a speed matching the current speed of an action that it is interrupting, so the
	 * transition will appear smooth. This must be called before removing the interrupted action from the actor. The interrupted
	 * action should be removed from the actor after calling this method. A {@linkplain SplineInterpolation} must be used. */
	public void setBlendFrom (ColorAction interruptedAction, SplineInterpolation interpolation) {
		setInterpolation(interpolation);
		worldStartSpeedR = interruptedAction.getWorldSpeedRed();
		worldStartSpeedG = interruptedAction.getWorldSpeedGreen();
		worldStartSpeedB = interruptedAction.getWorldSpeedBlue();
		worldStartSpeedA = interruptedAction.getWorldSpeedAlpha();
		blending = true;
	}

	public void cancelBlendFrom () {
		blending = false;
	}
}
