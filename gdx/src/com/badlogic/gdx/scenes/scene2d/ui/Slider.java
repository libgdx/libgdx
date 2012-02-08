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

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// BOZO - Add snapping to the knob.

/** A slider is a horizontal indicator that allows a user to set a value. The slider his a range (min, max) and a stepping between
 * each value the slider represents.
 * <p>
 * The preferred height of a slider is determined by the larger of the knob and background. The preferred width of a slider is
 * 140, a relatively arbitrary size.
 * @author mzechner */
public class Slider extends Widget {
	private SliderStyle style;
	private float min, max, steps;
	private float value;
	private float sliderPos;
	private ValueChangedListener listener = null;
	private boolean isDragging;

	public Slider (Skin skin) {
		this(0, 100, 100, skin);
	}

	public Slider (float min, float max, float steps, Skin skin) {
		this(min, max, steps, skin.getStyle(SliderStyle.class), null);
	}

	public Slider (float min, float max, float steps, SliderStyle style) {
		this(min, max, steps, style, null);
	}

	/** Creates a new slider. It's width is determined by the given prefWidth parameter, its height is determined by the maximum of
	 * the height of either the slider {@link NinePatch} or slider handle {@link TextureRegion}. The min and max values determine
	 * the range the values of this slider can take on, the steps parameter specifies the distance between individual values. E.g.
	 * min could be 4, max could be 10 and steps could be 0.2, giving you a total of 30 values, 4.0 4.2, 4.4 and so on.
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param steps the step size between values
	 * @param style the {@link SliderStyle}
	 * @param name the name */
	public Slider (float min, float max, float steps, SliderStyle style, String name) {
		super(name);
		if (min > max) throw new IllegalArgumentException("min must be > max: " + min + " > " + max);
		if (steps < 0) throw new IllegalArgumentException("steps must be > 0: " + steps);
		setStyle(style);
		this.min = min;
		this.max = max;
		this.steps = steps;
		this.value = min;
		width = getPrefWidth();
		height = getPrefHeight();
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

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		final TextureRegion knob = style.knob;
		final NinePatch slider = style.slider;

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		sliderPos = (value - min) / (max - min) * (width - knob.getRegionWidth());
		sliderPos = Math.max(0, sliderPos);
		sliderPos = Math.min(width - knob.getRegionWidth(), sliderPos);

		slider.draw(batch, x, y + (int)((height - slider.getTotalHeight()) * 0.5f), width, slider.getTotalHeight());
		batch.draw(knob, x + sliderPos, y + (int)((height - knob.getRegionHeight()) * 0.5f));
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		isDragging = true;
		calculatePositionAndValue(x);
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		isDragging = false;
		calculatePositionAndValue(x);
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		calculatePositionAndValue(x);
	}

	private void calculatePositionAndValue (float x) {
		final TextureRegion knob = style.knob;

		sliderPos = x - knob.getRegionWidth() / 2;
		sliderPos = Math.max(0, sliderPos);
		sliderPos = Math.min(width - knob.getRegionWidth(), sliderPos);
		value = min + (max - min) * (sliderPos / (width - knob.getRegionWidth()));
		if (listener != null) listener.changed(this, getValue());
	}

	/** Returns true if the slider is being dragged. */
	public boolean isDragging () {
		return isDragging;
	}

	/** @param listener May be null. */
	public void setValueChangedListener (ValueChangedListener listener) {
		this.listener = listener;
	}

	public float getValue () {
		return (float)Math.floor(value / steps) * steps;
	}

	public void setValue (float value) {
		if (value < min || value > max) throw new IllegalArgumentException("value must be >= min and <= max: " + value);
		this.value = value;
	}

	/** Sets the range of this slider. The slider's current value is reset to min. */
	public void setRange (float min, float max) {
		if (min >= max) throw new IllegalArgumentException("min must be < max");
		this.min = min;
		this.max = max;
		this.value = min;
		if (listener != null) listener.changed(this, getValue());
	}

	public float getPrefWidth () {
		return 140;
	}

	public float getPrefHeight () {
		return Math.max(style.knob.getRegionHeight(), style.slider.getTotalHeight());
	}

	/** Interface to listen for changes to the value of the slider.
	 * @author mzechner */
	static public interface ValueChangedListener {
		public void changed (Slider slider, float value);
	}

	/** The style for a slider, see {@link Slider}.
	 * @author mzechner */
	static public class SliderStyle {
		/** The slider background, stretched only in the x direction. */
		NinePatch slider;
		/** Centered vertically on the background. */
		TextureRegion knob;

		public SliderStyle () {
		}

		public SliderStyle (NinePatch sliderPatch, TextureRegion knobRegion) {
			this.slider = sliderPatch;
			this.knob = knobRegion;
		}
	}
}
