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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;

/** A slider is a horizontal indicator that allows a user to set a value. The slider has a range (min, max) and a stepping between
 * each value the slider represents.
 * <p>
 * {@link ChangeEvent} is fired when the slider knob is moved. Canceling the event will move the knob to where it was previously.
 * <p>
 * The preferred height of a slider is determined by the larger of the knob and background. The preferred width of a slider is
 * 140, a relatively arbitrary size.
 * @author mzechner
 * @author Nathan Sweet */
public class Slider extends ProgressBar {
	int draggingPointer = -1;

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
		super(min, max, stepSize, vertical, style);

		shiftIgnoresSnap = true;

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
		if (style == null) throw new NullPointerException("style cannot be null");
		if (!(style instanceof SliderStyle)) throw new IllegalArgumentException("style must be a SliderStyle.");
		super.setStyle(style);
	}

	/** Returns the slider's style. Modifying the returned style may not have an effect until {@link #setStyle(SliderStyle)} is
	 * called. */
	public SliderStyle getStyle () {
		return (SliderStyle)super.getStyle();
	}

	boolean calculatePositionAndValue (float x, float y) {
		final SliderStyle style = getStyle();
		final Drawable knob = (disabled && style.disabledKnob != null) ? style.disabledKnob : style.knob;
		final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;

		float value;
		float oldPosition = position;

		final float min = getMinValue();
		final float max = getMaxValue();

		if (vertical) {
			float height = getHeight() - bg.getTopHeight() - bg.getBottomHeight();
			float knobHeight = knob == null ? 0 : knob.getMinHeight();
			position = y - bg.getBottomHeight() - knobHeight * 0.5f;
			value = min + (max - min) * (position / (height - knobHeight));
			position = Math.max(0, position);
			position = Math.min(height - knobHeight, position);
		} else {
			float width = getWidth() - bg.getLeftWidth() - bg.getRightWidth();
			float knobWidth = knob == null ? 0 : knob.getMinWidth();
			position = x - bg.getLeftWidth() - knobWidth * 0.5f;
			value = min + (max - min) * (position / (width - knobWidth));
			position = Math.max(0, position);
			position = Math.min(width - knobWidth, position);
		}

		float oldValue = value;
		boolean valueSet = setValue(value);
		if (value == oldValue) position = oldPosition;
		return valueSet;
	}

	/** Returns true if the slider is being dragged. */
	public boolean isDragging () {
		return draggingPointer != -1;
	}

	/** The style for a slider, see {@link Slider}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class SliderStyle extends ProgressBarStyle {
		public SliderStyle () {
		}

		public SliderStyle (Drawable background, Drawable knob) {
			super(background, knob);
		}

		public SliderStyle (SliderStyle style) {
			super(style);
		}
	}
}
