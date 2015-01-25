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

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;

/** A progress bar is a widget that visually displays the progress of some activity or a value within given range. The progress bar
 * has a range (min, max) and a stepping between each value it represents. The percentage of completeness typically starts out as
 * an empty progress bar and gradually becomes filled in as the task or variable value progresses.
 * <p>
 * {@link ChangeEvent} is fired when the progress bar knob is moved. Cancelling the event will move the knob to where it was
 * previously.
 * <p>
 * The preferred height of a progress bar is determined by the larger of the knob and background. The preferred width of progress
 * bar is 140, a relatively arbitrary size.
 * @author mzechner
 * @author Nathan Sweet */
public class ProgressBar extends Widget implements Disableable {
	private ProgressBarStyle style;
	private float min, max, stepSize;
	private float value, animateFromValue;
	float position;
	final boolean vertical;
	private float animateDuration, animateTime;
	private Interpolation animateInterpolation = Interpolation.linear;
	private float[] snapValues;
	private float threshold;
	boolean disabled;
	boolean shiftIgnoresSnap;

	public ProgressBar (float min, float max, float stepSize, boolean vertical, Skin skin) {
		this(min, max, stepSize, vertical, skin.get("default-" + (vertical ? "vertical" : "horizontal"), ProgressBarStyle.class));
	}

	public ProgressBar (float min, float max, float stepSize, boolean vertical, Skin skin, String styleName) {
		this(min, max, stepSize, vertical, skin.get(styleName, ProgressBarStyle.class));
	}

	/** Creates a new progress bar. It's width is determined by the given prefWidth parameter, its height is determined by the
	 * maximum of the height of either the progress bar {@link NinePatch} or progress bar handle {@link TextureRegion}. The min and
	 * max values determine the range the values of this progress bar can take on, the stepSize parameter specifies the distance
	 * between individual values.
	 * <p>
	 * E.g. min could be 4, max could be 10 and stepSize could be 0.2, giving you a total of 30 values, 4.0 4.2, 4.4 and so on.
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param stepSize the step size between values
	 * @param style the {@link ProgressBarStyle} */
	public ProgressBar (float min, float max, float stepSize, boolean vertical, ProgressBarStyle style) {
		if (min > max) throw new IllegalArgumentException("max must be > min. min,max: " + min + ", " + max);
		if (stepSize <= 0) throw new IllegalArgumentException("stepSize must be > 0: " + stepSize);
		setStyle(style);
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.vertical = vertical;
		this.value = min;
		setSize(getPrefWidth(), getPrefHeight());
	}

	public void setStyle (ProgressBarStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/** Returns the progress bar's style. Modifying the returned style may not have an effect until
	 * {@link #setStyle(ProgressBarStyle)} is called. */
	public ProgressBarStyle getStyle () {
		return style;
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (animateTime > 0) {
			animateTime -= delta;
			Stage stage = getStage();
			if (stage != null && stage.getActionsRequestRendering()) Gdx.graphics.requestRendering();
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		ProgressBarStyle style = this.style;
		boolean disabled = this.disabled;
		final Drawable knob = (disabled && style.disabledKnob != null) ? style.disabledKnob : style.knob;
		final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
		final Drawable knobBefore = (disabled && style.disabledKnobBefore != null) ? style.disabledKnobBefore : style.knobBefore;
		final Drawable knobAfter = (disabled && style.disabledKnobAfter != null) ? style.disabledKnobAfter : style.knobAfter;

		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();
		float knobHeight = knob == null ? 0 : knob.getMinHeight();
		float knobWidth = knob == null ? 0 : knob.getMinWidth();
		float value = getVisualValue();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		if (vertical) {
			bg.draw(batch, x + (int)((width - bg.getMinWidth()) * 0.5f), y, bg.getMinWidth(), height);

			float positionHeight = height - (bg.getTopHeight() + bg.getBottomHeight());
			float knobHeightHalf = 0;
			if (min != max) {
				if (knob == null) {
					knobHeightHalf = knobBefore == null ? 0 : knobBefore.getMinHeight() * 0.5f;
					position = (value - min) / (max - min) * (positionHeight - knobHeightHalf);
					position = Math.min(positionHeight - knobHeightHalf, position);
				} else {
					knobHeightHalf = knobHeight * 0.5f;
					position = (value - min) / (max - min) * (positionHeight - knobHeight);
					position = Math.min(positionHeight - knobHeight, position) + bg.getBottomHeight();
				}
				position = Math.max(0, position);
			}

			if (knobBefore != null) {
				float offset = 0;
				if (bg != null) offset = bg.getTopHeight();
				knobBefore.draw(batch, x + (int)((width - knobBefore.getMinWidth()) * 0.5f), y + offset, knobBefore.getMinWidth(),
					(int)(position + knobHeightHalf));
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + (int)((width - knobAfter.getMinWidth()) * 0.5f), y + (int)(position + knobHeightHalf),
					knobAfter.getMinWidth(), height - (int)(position + knobHeightHalf));
			}
			if (knob != null) knob.draw(batch, x + (int)((width - knobWidth) * 0.5f), (int)(y + position), knobWidth, knobHeight);
		} else {
			bg.draw(batch, x, y + (int)((height - bg.getMinHeight()) * 0.5f), width, bg.getMinHeight());

			float positionWidth = width - (bg.getLeftWidth() + bg.getRightWidth());
			float knobWidthHalf = 0;
			if (min != max) {
				if (knob == null) {
					knobWidthHalf = knobBefore == null ? 0 : knobBefore.getMinWidth() * 0.5f;
					position = (value - min) / (max - min) * (positionWidth - knobWidthHalf);
					position = Math.min(positionWidth - knobWidthHalf, position);
				} else {
					knobWidthHalf = knobWidth * 0.5f;
					position = (value - min) / (max - min) * (positionWidth - knobWidth);
					position = Math.min(positionWidth - knobWidth, position) + bg.getLeftWidth();
				}
				position = Math.max(0, position);
			}

			if (knobBefore != null) {
				float offset = 0;
				if (bg != null) offset = bg.getLeftWidth();
				knobBefore.draw(batch, x + offset, y + (int)((height - knobBefore.getMinHeight()) * 0.5f),
					(int)(position + knobWidthHalf), knobBefore.getMinHeight());
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + (int)(position + knobWidthHalf), y + (int)((height - knobAfter.getMinHeight()) * 0.5f),
					width - (int)(position + knobWidthHalf), knobAfter.getMinHeight());
			}
			if (knob != null) knob.draw(batch, (int)(x + position), (int)(y + (height - knobHeight) * 0.5f), knobWidth, knobHeight);
		}
	}

	public float getValue () {
		return value;
	}

	/** If {@link #setAnimateDuration(float) animating} the progress bar value, this returns the value current displayed. */
	public float getVisualValue () {
		if (animateTime > 0) return animateInterpolation.apply(animateFromValue, value, 1 - animateTime / animateDuration);
		return value;
	}

	/** Returns progress bar visual position within the range. */
	protected float getKnobPosition () {
		return this.position;
	}

	/** Sets the progress bar position, rounded to the nearest step size and clamped to the minimum and maximum values.
	 * {@link #clamp(float)} can be overridden to allow values outside of the progress bar's min/max range.
	 * @return false if the value was not changed because the progress bar already had the value or it was canceled by a listener. */
	public boolean setValue (float value) {
		value = clamp(Math.round(value / stepSize) * stepSize);
		if (!shiftIgnoresSnap || (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)))
			value = snap(value);
		float oldValue = this.value;
		if (value == oldValue) return false;
		float oldVisualValue = getVisualValue();
		this.value = value;
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		boolean cancelled = fire(changeEvent);
		if (cancelled)
			this.value = oldValue;
		else if (animateDuration > 0) {
			animateFromValue = oldVisualValue;
			animateTime = animateDuration;
		}
		Pools.free(changeEvent);
		return !cancelled;
	}

	/** Clamps the value to the progress bar's min/max range. This can be overridden to allow a range different from the progress
	 * bar knob's range. */
	protected float clamp (float value) {
		return MathUtils.clamp(value, min, max);
	}

	/** Sets the range of this progress bar. The progress bar's current value is clamped to the range. */
	public void setRange (float min, float max) {
		if (min > max) throw new IllegalArgumentException("min must be <= max");
		this.min = min;
		this.max = max;
		if (value < min)
			setValue(min);
		else if (value > max) setValue(max);
	}

	public void setStepSize (float stepSize) {
		if (stepSize <= 0) throw new IllegalArgumentException("steps must be > 0: " + stepSize);
		this.stepSize = stepSize;
	}

	public float getPrefWidth () {
		if (vertical) {
			final Drawable knob = (disabled && style.disabledKnob != null) ? style.disabledKnob : style.knob;
			final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
			return Math.max(knob == null ? 0 : knob.getMinWidth(), bg.getMinWidth());
		} else
			return 140;
	}

	public float getPrefHeight () {
		if (vertical)
			return 140;
		else {
			final Drawable knob = (disabled && style.disabledKnob != null) ? style.disabledKnob : style.knob;
			final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
			return Math.max(knob == null ? 0 : knob.getMinHeight(), bg.getMinHeight());
		}
	}

	public float getMinValue () {
		return this.min;
	}

	public float getMaxValue () {
		return this.max;
	}

	public float getStepSize () {
		return this.stepSize;
	}

	/** If > 0, changes to the progress bar value via {@link #setValue(float)} will happen over this duration in seconds. */
	public void setAnimateDuration (float duration) {
		this.animateDuration = duration;
	}

	/** Sets the interpolation to use for {@link #setAnimateDuration(float)}. */
	public void setAnimateInterpolation (Interpolation animateInterpolation) {
		if (animateInterpolation == null) throw new IllegalArgumentException("animateInterpolation cannot be null.");
		this.animateInterpolation = animateInterpolation;
	}

	/** Will make this progress bar snap to the specified values, if the knob is within the threshold. */
	public void setSnapToValues (float[] values, float threshold) {
		this.snapValues = values;
		this.threshold = threshold;
	}

	/** Returns a snapped value. */
	private float snap (float value) {
		if (snapValues == null) return value;
		for (int i = 0; i < snapValues.length; i++) {
			if (Math.abs(value - snapValues[i]) <= threshold) return snapValues[i];
		}
		return value;
	}

	public void setDisabled (boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled () {
		return disabled;
	}

	/** The style for a progress bar, see {@link ProgressBar}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class ProgressBarStyle {
		/** The progress bar background, stretched only in one direction. */
		public Drawable background;
		/** Optional. **/
		public Drawable disabledBackground;
		/** Optional, centered on the background. */
		public Drawable knob, disabledKnob;
		/** Optional. */
		public Drawable knobBefore, knobAfter, disabledKnobBefore, disabledKnobAfter;

		public ProgressBarStyle () {
		}

		public ProgressBarStyle (Drawable background, Drawable knob) {
			this.background = background;
			this.knob = knob;
		}

		public ProgressBarStyle (ProgressBarStyle style) {
			this.background = style.background;
			this.disabledBackground = style.disabledBackground;
			this.knob = style.knob;
			this.disabledKnob = style.disabledKnob;
			this.knobBefore = style.knobBefore;
			this.knobAfter = style.knobAfter;
			this.disabledKnobBefore = style.disabledKnobBefore;
			this.disabledKnobAfter = style.disabledKnobAfter;
		}
	}
}
