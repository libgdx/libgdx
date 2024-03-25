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
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pools;

/** A progress bar is a widget that visually displays the progress of some activity or a value within given range. The progress
 * bar has a range (min, max) and a stepping between each value it represents. The percentage of completeness typically starts out
 * as an empty progress bar and gradually becomes filled in as the task or variable value progresses.
 * <p>
 * {@link ChangeEvent} is fired when the progress bar knob is moved. Cancelling the event will move the knob to where it was
 * previously.
 * <p>
 * For a horizontal progress bar, its preferred height is determined by the larger of the knob and background, and the preferred
 * width is 140, a relatively arbitrary size. These parameters are reversed for a vertical progress bar.
 * @author mzechner
 * @author Nathan Sweet */
public class ProgressBar extends Widget implements Disableable {
	private ProgressBarStyle style;
	float min, max, stepSize;
	private float value, animateFromValue;
	float position;
	final boolean vertical;
	private float animateDuration, animateTime;
	private Interpolation animateInterpolation = Interpolation.linear, visualInterpolation = Interpolation.linear;
	boolean disabled;
	private boolean round = true, programmaticChangeEvents = true;

	public ProgressBar (float min, float max, float stepSize, boolean vertical, Skin skin) {
		this(min, max, stepSize, vertical, skin.get("default-" + (vertical ? "vertical" : "horizontal"), ProgressBarStyle.class));
	}

	public ProgressBar (float min, float max, float stepSize, boolean vertical, Skin skin, String styleName) {
		this(min, max, stepSize, vertical, skin.get(styleName, ProgressBarStyle.class));
	}

	/** Creates a new progress bar. If horizontal, its width is determined by the prefWidth parameter, and its height is determined
	 * by the maximum of the height of either the progress bar {@link NinePatch} or progress bar handle {@link TextureRegion}. The
	 * min and max values determine the range the values of this progress bar can take on, the stepSize parameter specifies the
	 * distance between individual values.
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

	public void act (float delta) {
		super.act(delta);
		if (animateTime > 0) {
			animateTime -= delta;
			Stage stage = getStage();
			if (stage != null && stage.getActionsRequestRendering()) Gdx.graphics.requestRendering();
		}
	}

	public void draw (Batch batch, float parentAlpha) {
		ProgressBarStyle style = this.style;
		boolean disabled = this.disabled;
		Drawable knob = style.knob, currentKnob = getKnobDrawable();
		Drawable bg = getBackgroundDrawable();
		Drawable knobBefore = getKnobBeforeDrawable();
		Drawable knobAfter = getKnobAfterDrawable();

		Color color = getColor();
		float x = getX(), y = getY();
		float width = getWidth(), height = getHeight();
		float knobHeight = knob == null ? 0 : knob.getMinHeight();
		float knobWidth = knob == null ? 0 : knob.getMinWidth();
		float percent = getVisualPercent();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		if (vertical) {
			float bgTopHeight = 0, bgBottomHeight = 0;
			if (bg != null) {
				drawRound(batch, bg, x + (width - bg.getMinWidth()) * 0.5f, y, bg.getMinWidth(), height);
				bgTopHeight = bg.getTopHeight();
				bgBottomHeight = bg.getBottomHeight();
				height -= bgTopHeight + bgBottomHeight;
			}

			float total = height - knobHeight;
			float beforeHeight = MathUtils.clamp(total * percent, 0, total);
			position = bgBottomHeight + beforeHeight;

			float knobHeightHalf = knobHeight * 0.5f;
			if (knobBefore != null) {
				drawRound(batch, knobBefore, //
					x + (width - knobBefore.getMinWidth()) * 0.5f, //
					y + bgBottomHeight, //
					knobBefore.getMinWidth(), beforeHeight + knobHeightHalf);
			}
			if (knobAfter != null) {
				drawRound(batch, knobAfter, //
					x + (width - knobAfter.getMinWidth()) * 0.5f, //
					y + position + knobHeightHalf, //
					knobAfter.getMinWidth(),
					total - (round ? (float)Math.ceil(beforeHeight - knobHeightHalf) : beforeHeight - knobHeightHalf));
			}
			if (currentKnob != null) {
				float w = currentKnob.getMinWidth(), h = currentKnob.getMinHeight();
				drawRound(batch, currentKnob, //
					x + (width - w) * 0.5f, //
					y + position + (knobHeight - h) * 0.5f, //
					w, h);
			}
		} else {
			float bgLeftWidth = 0, bgRightWidth = 0;
			if (bg != null) {
				drawRound(batch, bg, x, Math.round(y + (height - bg.getMinHeight()) * 0.5f), width, Math.round(bg.getMinHeight()));
				bgLeftWidth = bg.getLeftWidth();
				bgRightWidth = bg.getRightWidth();
				width -= bgLeftWidth + bgRightWidth;
			}

			float total = width - knobWidth;
			float beforeWidth = MathUtils.clamp(total * percent, 0, total);
			position = bgLeftWidth + beforeWidth;

			float knobWidthHalf = knobWidth * 0.5f;
			if (knobBefore != null) {
				drawRound(batch, knobBefore, //
					x + bgLeftWidth, //
					y + (height - knobBefore.getMinHeight()) * 0.5f, //
					beforeWidth + knobWidthHalf, knobBefore.getMinHeight());
			}
			if (knobAfter != null) {
				drawRound(batch, knobAfter, //
					x + position + knobWidthHalf, //
					y + (height - knobAfter.getMinHeight()) * 0.5f, //
					total - (round ? (float)Math.ceil(beforeWidth - knobWidthHalf) : beforeWidth - knobWidthHalf),
					knobAfter.getMinHeight());
			}
			if (currentKnob != null) {
				float w = currentKnob.getMinWidth(), h = currentKnob.getMinHeight();
				drawRound(batch, currentKnob, //
					x + position + (knobWidth - w) * 0.5f, //
					y + (height - h) * 0.5f, //
					w, h);
			}
		}
	}

	private void drawRound (Batch batch, Drawable drawable, float x, float y, float w, float h) {
		if (round) {
			x = (float)Math.floor(x);
			y = (float)Math.floor(y);
			w = (float)Math.ceil(w);
			h = (float)Math.ceil(h);
		}
		drawable.draw(batch, x, y, w, h);
	}

	public float getValue () {
		return value;
	}

	/** If {@link #setAnimateDuration(float) animating} the progress bar value, this returns the value current displayed. */
	public float getVisualValue () {
		if (animateTime > 0) return animateInterpolation.apply(animateFromValue, value, 1 - animateTime / animateDuration);
		return value;
	}

	/** Sets the visual value equal to the actual value. This can be used to set the value without animating. */
	public void updateVisualValue () {
		animateTime = 0;
	}

	public float getPercent () {
		if (min == max) return 0;
		return (value - min) / (max - min);
	}

	public float getVisualPercent () {
		if (min == max) return 0;
		return visualInterpolation.apply((getVisualValue() - min) / (max - min));
	}

	protected @Null Drawable getBackgroundDrawable () {
		if (disabled && style.disabledBackground != null) return style.disabledBackground;
		return style.background;
	}

	protected @Null Drawable getKnobDrawable () {
		if (disabled && style.disabledKnob != null) return style.disabledKnob;
		return style.knob;
	}

	protected Drawable getKnobBeforeDrawable () {
		if (disabled && style.disabledKnobBefore != null) return style.disabledKnobBefore;
		return style.knobBefore;
	}

	protected Drawable getKnobAfterDrawable () {
		if (disabled && style.disabledKnobAfter != null) return style.disabledKnobAfter;
		return style.knobAfter;
	}

	/** Returns progress bar visual position within the range (as it was last calculated in {@link #draw(Batch, float)}). */
	protected float getKnobPosition () {
		return this.position;
	}

	/** Sets the progress bar position, rounded to the nearest step size and clamped to the minimum and maximum values.
	 * {@link #clamp(float)} can be overridden to allow values outside of the progress bar's min/max range.
	 * @return false if the value was not changed because the progress bar already had the value or it was canceled by a
	 *         listener. */
	public boolean setValue (float value) {
		value = clamp(round(value));
		float oldValue = this.value;
		if (value == oldValue) return false;
		float oldVisualValue = getVisualValue();
		this.value = value;

		if (programmaticChangeEvents) {
			ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
			boolean cancelled = fire(changeEvent);
			Pools.free(changeEvent);
			if (cancelled) {
				this.value = oldValue;
				return false;
			}
		}

		if (animateDuration > 0) {
			animateFromValue = oldVisualValue;
			animateTime = animateDuration;
		}
		return true;
	}

	/** Rouinds the value using the progress bar's step size. This can be overridden to customize or disable rounding. */
	protected float round (float value) {
		return Math.round(value / stepSize) * stepSize;
	}

	/** Clamps the value to the progress bar's min/max range. This can be overridden to allow a range different from the progress
	 * bar knob's range. */
	protected float clamp (float value) {
		return MathUtils.clamp(value, min, max);
	}

	/** Sets the range of this progress bar. The progress bar's current value is clamped to the range. */
	public void setRange (float min, float max) {
		if (min > max) throw new IllegalArgumentException("min must be <= max: " + min + " <= " + max);
		this.min = min;
		this.max = max;
		if (value < min)
			setValue(min);
		else if (value > max) //
			setValue(max);
	}

	public void setStepSize (float stepSize) {
		if (stepSize <= 0) throw new IllegalArgumentException("steps must be > 0: " + stepSize);
		this.stepSize = stepSize;
	}

	public float getPrefWidth () {
		if (vertical) {
			Drawable knob = style.knob, bg = getBackgroundDrawable();
			return Math.max(knob == null ? 0 : knob.getMinWidth(), bg == null ? 0 : bg.getMinWidth());
		} else
			return 140;
	}

	public float getPrefHeight () {
		if (vertical)
			return 140;
		else {
			Drawable knob = style.knob, bg = getBackgroundDrawable();
			return Math.max(knob == null ? 0 : knob.getMinHeight(), bg == null ? 0 : bg.getMinHeight());
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

	/** Sets the interpolation to use for display. */
	public void setVisualInterpolation (Interpolation interpolation) {
		this.visualInterpolation = interpolation;
	}

	/** If true (the default), inner Drawable positions and sizes are rounded to integers. */
	public void setRound (boolean round) {
		this.round = round;
	}

	public void setDisabled (boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isAnimating () {
		return animateTime > 0;
	}

	public boolean isDisabled () {
		return disabled;
	}

	/** True if the progress bar is vertical, false if it is horizontal. **/
	public boolean isVertical () {
		return vertical;
	}

	/** If false, {@link #setValue(float)} will not fire {@link ChangeEvent}. The event will only be fired when the user changes
	 * the slider. */
	public void setProgrammaticChangeEvents (boolean programmaticChangeEvents) {
		this.programmaticChangeEvents = programmaticChangeEvents;
	}

	/** The style for a progress bar, see {@link ProgressBar}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class ProgressBarStyle {
		/** The progress bar background, stretched only in one direction. */
		public @Null Drawable background, disabledBackground;
		public @Null Drawable knob, disabledKnob;
		public @Null Drawable knobBefore, disabledKnobBefore;
		public @Null Drawable knobAfter, disabledKnobAfter;

		public ProgressBarStyle () {
		}

		public ProgressBarStyle (@Null Drawable background, @Null Drawable knob) {
			this.background = background;
			this.knob = knob;
		}

		public ProgressBarStyle (ProgressBarStyle style) {
			background = style.background;
			disabledBackground = style.disabledBackground;

			knob = style.knob;
			disabledKnob = style.disabledKnob;

			knobBefore = style.knobBefore;
			disabledKnobBefore = style.disabledKnobBefore;

			knobAfter = style.knobAfter;
			disabledKnobAfter = style.disabledKnobAfter;
		}
	}
}
