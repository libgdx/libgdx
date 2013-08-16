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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;

/** A slider is a horizontal indicator that allows a user to set a value. The slider has a range (min, max) and a stepping between
 * each value the slider represents.
 * <p>
 * {@link ChangeEvent} is fired when the slider knob is moved. Cancelling the event will move the knob to where it was previously.
 * <p>
 * The preferred height of a slider is determined by the larger of the knob and background. The preferred width of a slider is
 * 140, a relatively arbitrary size.
 * @author mzechner
 * @author Nathan Sweet */
public class Slider extends Widget {
	private SliderStyle style;
	private float min, max, stepSize;
	private float value, animateFromValue;
	private float sliderPos;
	private final boolean vertical;
	int draggingPointer = -1;
	private float animateDuration, animateTime;
	private Interpolation animateInterpolation = Interpolation.linear;
	private float[] snapValues;
	private float threshold;
	boolean disabled;

	public Slider (float min, float max, float stepSize, boolean vertical, Skin skin) {
		this(min, max, stepSize, vertical, skin.get("default-" + (vertical ? "vertical" : "horizontal"), SliderStyle.class));
	}

	public Slider (float min, float max, float stepSize, boolean vertical, Skin skin, String styleName) {
		this(min, max, stepSize, vertical, skin.get(styleName, SliderStyle.class));
	}

	/** Creates a new slider. It's width is determined by the given prefWidth parameter, its height is determined by the maximum of
	 * the height of either the slider {@link NinePatch} or slider handle {@link TextureRegion}. The min and max values determine
	 * the range the values of this slider can take on, the stepSize parameter specifies the distance between individual values.
	 * E.g. min could be 4, max could be 10 and stepSize could be 0.2, giving you a total of 30 values, 4.0 4.2, 4.4 and so on.
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param stepSize the step size between values
	 * @param style the {@link SliderStyle} */
	public Slider (float min, float max, float stepSize, boolean vertical, SliderStyle style) {
		if (min > max) throw new IllegalArgumentException("min must be > max: " + min + " > " + max);
		if (stepSize <= 0) throw new IllegalArgumentException("stepSize must be > 0: " + stepSize);
		setStyle(style);
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.vertical = vertical;
		this.value = min;
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());

		addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (disabled) return false;
				if (draggingPointer != -1) return false;
				draggingPointer = pointer;
				calculatePositionAndValue(x, y);
				return true;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != draggingPointer) return;
				draggingPointer = -1;
				if (!calculatePositionAndValue(x, y)) {
					// Fire an event on touchUp even if the value didn't change, so listeners can see when a drag ends via isDragging.
					ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
					fire(changeEvent);
					Pools.free(changeEvent);
				}
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				calculatePositionAndValue(x, y);
			}
		});
	}

	public void setStyle (SliderStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/** Returns the slider's style. Modifying the returned style may not have an effect until {@link #setStyle(SliderStyle)} is
	 * called. */
	public SliderStyle getStyle () {
		return style;
	}

	public void act (float delta) {
		super.act(delta);
		animateTime -= delta;
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		SliderStyle style = this.style;
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

			float sliderPosHeight = height - (bg.getTopHeight() + bg.getBottomHeight());
			if (min != max) {
				sliderPos = (value - min) / (max - min) * (sliderPosHeight - knobHeight);
				sliderPos = Math.max(0, sliderPos);
				sliderPos = Math.min(sliderPosHeight - knobHeight, sliderPos) + bg.getBottomHeight();
			}

			float knobHeightHalf = knobHeight * 0.5f;
			if (knobBefore != null) {
				knobBefore.draw(batch, x + (int)((width - knobBefore.getMinWidth()) * 0.5f), y, knobBefore.getMinWidth(),
					(int)(sliderPos + knobHeightHalf));
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + (int)((width - knobAfter.getMinWidth()) * 0.5f), y + (int)(sliderPos + knobHeightHalf),
					knobAfter.getMinWidth(), height - (int)(sliderPos + knobHeightHalf));
			}
			if (knob != null) knob.draw(batch, x + (int)((width - knobWidth) * 0.5f), (int)(y + sliderPos), knobWidth, knobHeight);
		} else {
			bg.draw(batch, x, y + (int)((height - bg.getMinHeight()) * 0.5f), width, bg.getMinHeight());

			float sliderPosWidth = width - (bg.getLeftWidth() + bg.getRightWidth());
			if (min != max) {
				sliderPos = (value - min) / (max - min) * (sliderPosWidth - knobWidth);
				sliderPos = Math.max(0, sliderPos);
				sliderPos = Math.min(sliderPosWidth - knobWidth, sliderPos) + bg.getLeftWidth();
			}

			float knobHeightHalf = knobHeight * 0.5f;
			if (knobBefore != null) {
				knobBefore.draw(batch, x, y + (int)((height - knobBefore.getMinHeight()) * 0.5f), (int)(sliderPos + knobHeightHalf),
					knobBefore.getMinHeight());
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + (int)(sliderPos + knobHeightHalf), y + (int)((height - knobAfter.getMinHeight()) * 0.5f),
					width - (int)(sliderPos + knobHeightHalf), knobAfter.getMinHeight());
			}
			if (knob != null)
				knob.draw(batch, (int)(x + sliderPos), (int)(y + (height - knobHeight) * 0.5f), knobWidth, knobHeight);
		}
	}

	boolean calculatePositionAndValue (float x, float y) {
		final Drawable knob = (disabled && style.disabledKnob != null) ? style.disabledKnob : style.knob;
		final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;

		float value;
		float oldPosition = sliderPos;

		if (vertical) {
			float height = getHeight() - bg.getTopHeight() - bg.getBottomHeight();
			float knobHeight = knob == null ? 0 : knob.getMinHeight();
			sliderPos = y - bg.getBottomHeight() - knobHeight * 0.5f;
			value = min + (max - min) * (sliderPos / (height - knobHeight));
			sliderPos = Math.max(0, sliderPos);
			sliderPos = Math.min(height - knobHeight, sliderPos);
		} else {
			float width = getWidth() - bg.getLeftWidth() - bg.getRightWidth();
			float knobWidth = knob == null ? 0 : knob.getMinWidth();
			sliderPos = x - bg.getLeftWidth() - knobWidth * 0.5f;
			value = min + (max - min) * (sliderPos / (width - knobWidth));
			sliderPos = Math.max(0, sliderPos);
			sliderPos = Math.min(width - knobWidth, sliderPos);
		}

		float oldValue = value;
		boolean valueSet = setValue(value);
		if (value == oldValue) sliderPos = oldPosition;
		return valueSet;
	}

	/** Returns true if the slider is being dragged. */
	public boolean isDragging () {
		return draggingPointer != -1;
	}

	public float getValue () {
		return value;
	}

	/** If {@link #setAnimateDuration(float) animating} the slider value, this returns the value current displayed. */
	public float getVisualValue () {
		if (animateTime > 0) return animateInterpolation.apply(animateFromValue, value, 1 - animateTime / animateDuration);
		return value;
	}

	/** Sets the slider position, rounded to the nearest step size and clamped to the minumum and maximim values.
	 * {@link #clamp(float)} can be overidden to allow values outside of the sliders min/max range.
	 * @return false if the value was not changed because the slider already had the value or it was canceled by a listener. */
	public boolean setValue (float value) {
		value = snap(clamp(Math.round(value / stepSize) * stepSize));
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

	/** Clamps the value to the sliders min/max range. This can be overidden to allow a range different from the slider knob's
	 * range. */
	protected float clamp (float value) {
		return MathUtils.clamp(value, min, max);
	}

	/** Sets the range of this slider. The slider's current value is reset to min. */
	public void setRange (float min, float max) {
		if (min > max) throw new IllegalArgumentException("min must be <= max");
		this.min = min;
		this.max = max;
		if (value < min)
			setValue(min);
		else if (value > max) setValue(max);
	}

	/** Sets the step size of the slider */
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

	/** If > 0, changes to the slider value via {@link #setValue(float)} will happen over this duration in seconds. */
	public void setAnimateDuration (float duration) {
		this.animateDuration = duration;
	}

	/** Sets the interpolation to use for {@link #setAnimateDuration(float)}. */
	public void setAnimateInterpolation (Interpolation animateInterpolation) {
		if (animateInterpolation == null) throw new IllegalArgumentException("animateInterpolation cannot be null.");
		this.animateInterpolation = animateInterpolation;
	}

	/** Will make this slider snap to the specified values, if the knob is within the threshold */
	public void setSnapToValues (float[] values, float threshold) {
		this.snapValues = values;
		this.threshold = threshold;
	}

	/** Returns a snapped value, or the original value */
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

	/** The style for a slider, see {@link Slider}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class SliderStyle {
		/** The slider background, stretched only in one direction. */
		public Drawable background;
		/** Optional. **/
		public Drawable disabledBackground;
		/** Optional, centered on the background. */
		public Drawable knob, disabledKnob;
		/** Optional. */
		public Drawable knobBefore, knobAfter, disabledKnobBefore, disabledKnobAfter;

		public SliderStyle () {
		}

		public SliderStyle (Drawable background, Drawable knob) {
			this.background = background;
			this.knob = knob;
		}

		public SliderStyle (SliderStyle style) {
			this.background = style.background;
			this.knob = style.knob;
		}
	}
}
