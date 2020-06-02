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
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pools;

/** A slider is a horizontal indicator that allows a user to set a value. The slider has a range (min, max) and a stepping between
 * each value the slider represents.
 * <p>
 * {@link ChangeEvent} is fired when the slider knob is moved. Canceling the event will move the knob to where it was previously.
 * <p>
 * For a horizontal progress bar, its preferred height is determined by the larger of the knob and background, and the preferred
 * width is 140, a relatively arbitrary size. These parameters are reversed for a vertical progress bar.
 * @author mzechner
 * @author Nathan Sweet */
public class Slider extends ProgressBar {
	int draggingPointer = -1;
	boolean mouseOver;
	private Interpolation visualInterpolationInverse = Interpolation.linear;
	private float[] snapValues;
	private float threshold;

	public Slider (float min, float max, float stepSize, boolean vertical, Skin skin) {
		this(min, max, stepSize, vertical, skin.get("default-" + (vertical ? "vertical" : "horizontal"), SliderStyle.class));
	}

	public Slider (float min, float max, float stepSize, boolean vertical, Skin skin, String styleName) {
		this(min, max, stepSize, vertical, skin.get(styleName, SliderStyle.class));
	}

	/** Creates a new slider. If horizontal, its width is determined by the prefWidth parameter, its height is determined by the
	 * maximum of the height of either the slider {@link NinePatch} or slider handle {@link TextureRegion}. The min and max values
	 * determine the range the values of this slider can take on, the stepSize parameter specifies the distance between individual
	 * values. E.g. min could be 4, max could be 10 and stepSize could be 0.2, giving you a total of 30 values, 4.0 4.2, 4.4 and so
	 * on.
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param stepSize the step size between values
	 * @param style the {@link SliderStyle} */
	public Slider (float min, float max, float stepSize, boolean vertical, SliderStyle style) {
		super(min, max, stepSize, vertical, style);

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
				// The position is invalid when focus is cancelled
				if (event.isTouchFocusCancel() || !calculatePositionAndValue(x, y)) {
					// Fire an event on touchUp even if the value didn't change, so listeners can see when a drag ends via isDragging.
					ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
					fire(changeEvent);
					Pools.free(changeEvent);
				}
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				calculatePositionAndValue(x, y);
			}

			public void enter (InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
				if (pointer == -1) mouseOver = true;
			}

			public void exit (InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
				if (pointer == -1) mouseOver = false;
			}
		});
	}

	/** Returns the slider's style. Modifying the returned style may not have an effect until {@link #setStyle(ProgressBarStyle)}
	 * is called. */
	public SliderStyle getStyle () {
		return (SliderStyle)super.getStyle();
	}

	@Null
	protected Drawable getKnobDrawable () {
		SliderStyle style = getStyle();
		return (disabled && style.disabledKnob != null) ? style.disabledKnob
			: (isDragging() && style.knobDown != null) ? style.knobDown
				: ((mouseOver && style.knobOver != null) ? style.knobOver : style.knob);
	}

	boolean calculatePositionAndValue (float x, float y) {
		SliderStyle style = getStyle();
		Drawable knob = style.knob;
		Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;

		float value;
		float oldPosition = position;

		float min = getMinValue();
		float max = getMaxValue();

		if (vertical) {
			float height = getHeight() - bg.getTopHeight() - bg.getBottomHeight();
			float knobHeight = knob == null ? 0 : knob.getMinHeight();
			position = y - bg.getBottomHeight() - knobHeight * 0.5f;
			value = min + (max - min) * visualInterpolationInverse.apply(position / (height - knobHeight));
			position = Math.max(Math.min(0, bg.getBottomHeight()), position);
			position = Math.min(height - knobHeight, position);
		} else {
			float width = getWidth() - bg.getLeftWidth() - bg.getRightWidth();
			float knobWidth = knob == null ? 0 : knob.getMinWidth();
			position = x - bg.getLeftWidth() - knobWidth * 0.5f;
			value = min + (max - min) * visualInterpolationInverse.apply(position / (width - knobWidth));
			position = Math.max(Math.min(0, bg.getLeftWidth()), position);
			position = Math.min(width - knobWidth, position);
		}

		float oldValue = value;
		if (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) value = snap(value);
		boolean valueSet = setValue(value);
		if (value == oldValue) position = oldPosition;
		return valueSet;
	}

	/** Returns a snapped value. */
	protected float snap (float value) {
		if (snapValues == null || snapValues.length == 0) return value;
		float bestDiff = -1, bestValue = 0;
		for (int i = 0; i < snapValues.length; i++) {
			float snapValue = snapValues[i];
			float diff = Math.abs(value - snapValue);
			if (diff <= threshold) {
				if (bestDiff == -1 || diff < bestDiff) {
					bestDiff = diff;
					bestValue = snapValue;
				}
			}
		}
		return bestDiff == -1 ? value : bestValue;
	}

	/** Will make this progress bar snap to the specified values, if the knob is within the threshold.
	 * @param values May be null. */
	public void setSnapToValues (@Null float[] values, float threshold) {
		this.snapValues = values;
		this.threshold = threshold;
	}

	/** Returns true if the slider is being dragged. */
	public boolean isDragging () {
		return draggingPointer != -1;
	}

	/** Sets the inverse interpolation to use for display. This should perform the inverse of the
	 * {@link #setVisualInterpolation(Interpolation) visual interpolation}. */
	public void setVisualInterpolationInverse (Interpolation interpolation) {
		this.visualInterpolationInverse = interpolation;
	}

	/** The style for a slider, see {@link Slider}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class SliderStyle extends ProgressBarStyle {
		/** Optional. */
		@Null public Drawable knobOver, knobDown;

		public SliderStyle () {
		}

		public SliderStyle (@Null Drawable background, @Null Drawable knob) {
			super(background, knob);
		}

		public SliderStyle (SliderStyle style) {
			super(style);
			this.knobOver = style.knobOver;
			this.knobDown = style.knobDown;
		}
	}
}
