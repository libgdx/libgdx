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
import com.badlogic.gdx.scenes.scene2d.Actor;

/** A value slider.
 * 
 * <h2>Functionality</h2> A slider lets you select a value within a range (min, max), with stepping between each value the slider
 * represents. To listen for changes of the slider value one can register a {@link ValueChangedListener} with the slider.
 * 
 * <h2>Layout</h2> A slider's (preferred) width and height are determined by the parameter past to its constructor as well as the
 * maximum height of the {@link NinePatch} and {@link TextureRegion} involved in the display of the slider. Use
 * {@link #setPrefSize(int, int)} to programmatically change the size to your liking. In case the width and height you set are to
 * small you will see artifacts.</p>
 * 
 * The slider background will only be stretched in the x-axis. The slider handle will be centered on the background vertically.
 * 
 * <h2>Style</h2> A slider is a {@link Widget} displaying a horizontal background {@link NinePatch}, stretched on the x-axis and
 * using the total height of the NinePatch on the y-axis, as well as a TextureRegion for the slider handle. The style is defined
 * via an instance of {@link SliderStyle}, which can be either done programmatically or via a {@link Skin}.</p>
 * 
 * A Slider's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <slider name="styleName" 
 *         slider="sliderPatch" 
 *         knob="knobRegion"/>
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newSlider(String, float, float, float, float, String)}.</li>
 * <li>The <code>slider</code> attribute references a {@link NinePatch} by name, to be used as the slider's background</li>
 * <li>The <code>knob</code> attribute references a {@link TextureRegion} by name, to be used as the slider's handle</li> *
 * </ul>
 * 
 * @author mzechner */
public class Slider extends Widget {
	SliderStyle style;
	float min;
	float max;
	float steps;
	float value;
	float sliderPos;
	ValueChangedListener listener = null;

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
	 * @param prefWidth the (preferred) width
	 * @param name the name */
	public Slider (float min, float max, float steps, SliderStyle style, String name) {
		super(name);
		setStyle(style);
		if (min > max) throw new IllegalArgumentException("min must be > max");
		if (steps < 0) throw new IllegalArgumentException("unit must be > 0");
		this.min = min;
		this.max = max;
		this.steps = steps;
		this.value = min;
	}

	/** Sets the style of this widget.
	 * @param style */
	public void setStyle (SliderStyle style) {
		this.style = style;
	}

	@Override
	public void layout () {
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		final TextureRegion knob = style.knob;
		final NinePatch slider = style.slider;

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		sliderPos = (value - min) / (max - min) * (width - knob.getRegionWidth());
		sliderPos = Math.max(0, sliderPos);
		sliderPos = Math.min(width - knob.getRegionWidth(), sliderPos);

		float maxHeight = Math.max(knob.getRegionHeight(), slider.getTotalHeight());
		slider.draw(batch, x, y + (int)((maxHeight - slider.getTotalHeight()) * 0.5f), width, slider.getTotalHeight());
		batch.draw(knob, x + sliderPos, y + (int)((maxHeight - knob.getRegionHeight()) * 0.5f));
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		calculateSliderPosAndValue(x);
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		calculateSliderPosAndValue(x);
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		calculateSliderPosAndValue(x);
	}

	private void calculateSliderPosAndValue (float x) {
		final TextureRegion knob = style.knob;

		sliderPos = x - knob.getRegionWidth() / 2;
		sliderPos = Math.max(0, sliderPos);
		sliderPos = Math.min(width - knob.getRegionWidth(), sliderPos);
		value = min + (max - min) * (sliderPos / (width - knob.getRegionWidth()));
		if (listener != null) listener.changed(this, getValue());
	}

	@Override
	public Actor hit (float x, float y) {
		return x > 0 && x < width && y > 0 && y < height ? this : null;
	}

	/** Defines the style of a slider, see {@link Slider}.
	 * @author mzechner */
	public static class SliderStyle {
		NinePatch slider;
		TextureRegion knob;

		public SliderStyle () {
		}

		public SliderStyle (NinePatch sliderPatch, TextureRegion knobRegion) {
			this.slider = sliderPatch;
			this.knob = knobRegion;
		}
	}

	/** Interface to listen for changes of the value of the slider.
	 * @author mzechner */
	public interface ValueChangedListener {
		public void changed (Slider slider, float value);
	}

	/** Sets the {@link ValueChangedListener} of this slider.
	 * @param listener the listener or null
	 * @return this Slider for chaining */
	public Slider setValueChangedListener (ValueChangedListener listener) {
		this.listener = listener;
		return this;
	}

	/** @return the current value of the slider */
	public float getValue () {
		return (float)Math.floor(value / steps) * steps;
	}

	/** Sets the value of this slider
	 * @param value the value */
	public void setValue (float value) {
		if (value < min || value > max) throw new IllegalArgumentException("value must be >= min && <= max");
		this.value = value;
		if (listener != null) listener.changed(this, getValue());
	}

	/** Sets the range of this slider. The slider's current value is reset to min.
	 * @param min the minimum value
	 * @param max the maximum value */
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
}
