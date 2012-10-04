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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;

/** A slider is a horizontal indicator that allows a user to set a value. The slider his a range (min, max) and a stepping between
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
	private float min, max, steps;
	private float value;
	private float sliderPos;
	private boolean vertical;
	int draggingPointer = -1;

	public Slider (float min, float max, float steps, boolean vertical, Skin skin) {
		this(min, max, steps, vertical, skin.get("default-" + (vertical ? "vertical" : "horizontal"), SliderStyle.class));
	}

	public Slider (float min, float max, float steps, boolean vertical, Skin skin, String styleName) {
		this(min, max, steps, vertical, skin.get(styleName, SliderStyle.class));
	}

	/** Creates a new slider. It's width is determined by the given prefWidth parameter, its height is determined by the maximum of
	 * the height of either the slider {@link NinePatch} or slider handle {@link TextureRegion}. The min and max values determine
	 * the range the values of this slider can take on, the steps parameter specifies the distance between individual values. E.g.
	 * min could be 4, max could be 10 and steps could be 0.2, giving you a total of 30 values, 4.0 4.2, 4.4 and so on.
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param steps the step size between values
	 * @param style the {@link SliderStyle} */
	public Slider (float min, float max, float steps, boolean vertical, SliderStyle style) {
		if (min > max) throw new IllegalArgumentException("min must be > max: " + min + " > " + max);
		if (steps <= 0) throw new IllegalArgumentException("steps must be > 0: " + steps);
		setStyle(style);
		this.min = min;
		this.max = max;
		this.steps = steps;
		this.vertical = vertical;
		this.value = min;
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());

		addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (draggingPointer != -1) return false;
				draggingPointer = pointer;
				calculatePositionAndValue(x, y);
				return true;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != draggingPointer) return;
				draggingPointer = -1;
				calculatePositionAndValue(x, y);
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

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		final Drawable knob = style.knob;
		final Drawable bg = style.background;

		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		if (vertical) {
			bg.draw(batch, x + (int)((width - bg.getMinWidth()) * 0.5f), y, bg.getMinWidth(), height);

			height -= bg.getTopHeight() + bg.getBottomHeight();
			sliderPos = (value - min) / (max - min) * (height - knob.getMinHeight());
			sliderPos = Math.max(0, sliderPos);
			sliderPos = Math.min(height - knob.getMinHeight(), sliderPos) + bg.getBottomHeight();

			knob.draw(batch, x + (int)((width - knob.getMinWidth()) * 0.5f), y + sliderPos, knob.getMinWidth(), knob.getMinHeight());
		} else {
			bg.draw(batch, x, y + (int)((height - bg.getMinHeight()) * 0.5f), width, bg.getMinHeight());

			width -= bg.getLeftWidth() + bg.getRightWidth();
			sliderPos = (value - min) / (max - min) * (width - knob.getMinWidth());
			sliderPos = Math.max(0, sliderPos);
			sliderPos = Math.min(width - knob.getMinWidth(), sliderPos) + bg.getLeftWidth();

			knob.draw(batch, x + sliderPos, y + (int)((height - knob.getMinHeight()) * 0.5f), knob.getMinWidth(),
				knob.getMinHeight());
		}
	}

	void calculatePositionAndValue (float x, float y) {
		final Drawable knob = style.knob;
		final Drawable bg = style.background;

		float value;
		float oldPosition = sliderPos;

		if (vertical) {
			float height = getHeight() - bg.getTopHeight() - bg.getBottomHeight();
			sliderPos = y- bg.getBottomHeight() - knob.getMinHeight() * 0.5f;
			sliderPos = Math.max(0, sliderPos);
			sliderPos = Math.min(height - knob.getMinHeight(), sliderPos);
			value = min + (max - min) * (sliderPos / (height - knob.getMinHeight()));
		} else {
			float width = getWidth() - bg.getLeftWidth() - bg.getRightWidth();
			sliderPos = x - bg.getLeftWidth() - knob.getMinWidth() * 0.5f;
			sliderPos = Math.max(0, sliderPos);
			sliderPos = Math.min(width - knob.getMinWidth(), sliderPos);
			value = min + (max - min) * (sliderPos / (width - knob.getMinWidth()));
		}

		float oldValue = value;
		setValue(value);
		if (value == oldValue) sliderPos = oldPosition;
	}

	/** Returns true if the slider is being dragged. */
	public boolean isDragging () {
		return draggingPointer != -1;
	}

	public float getValue () {
		return value;
	}

	/** Sets the slider position, rounded to the nearest step size and clamped to the minumum and maximim values. */
	public void setValue (float value) {
		if (value < min || value > max) throw new IllegalArgumentException("value must be >= min and <= max: " + value);
		value = MathUtils.clamp(Math.round(value / steps) * steps, min, max);
		float oldValue = this.value;
		if (value == oldValue) return;
		this.value = value;
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		if (fire(changeEvent)) this.value = oldValue;
		Pools.free(changeEvent);
	}

	/** Sets the range of this slider. The slider's current value is reset to min. */
	public void setRange (float min, float max) {
		if (min >= max) throw new IllegalArgumentException("min must be < max");
		this.min = min;
		this.max = max;
		setValue(min);
	}

	public float getPrefWidth () {
		if (vertical)
			return Math.max(style.knob.getMinWidth(), style.background.getMinWidth());
		else
			return 140;
	}

	public float getPrefHeight () {
		if (vertical)
			return 140;
		else
			return Math.max(style.knob.getMinHeight(), style.background.getMinHeight());
	}

	public float getMinValue () {
		return this.min;
	}

	public float getMaxValue () {
		return this.max;
	}

	/** The style for a slider, see {@link Slider}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class SliderStyle {
		/** The slider background, stretched only in one direction. */
		public Drawable background;
		/** Centered on the background. */
		public Drawable knob;

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
