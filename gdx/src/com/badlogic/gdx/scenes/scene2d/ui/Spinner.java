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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/** A spinner allows to select a value according to a specified model, which defines the underlying data set. The appearance of
 * the spinner is given by its SpinnerStyle. To change the value displayed by the spinner, it is possible to use the scroll button
 * on the mouse, or tap the "next" (up) and "previous" (down) portions of the background. A SpinnerStyle is made of 3 drawables,
 * two for when the next or previous buttons are pressed, and one by default.
 * 
 * @author Jeremy Gillespie-Cloutier */
public class Spinner extends Widget {

	private SpinnerStyle style;
	private GlyphLayout layout;
	private SpinnerModel model;
	private Drawable currentBackground; // The current background drawable, either default, next button pressed or previous button
													// pressed

	public Spinner (Skin skin, SpinnerModel model) {
		this(skin.get(SpinnerStyle.class), model);
	}

	public Spinner (Skin skin, String styleName, SpinnerModel model) {
		this(skin.get(styleName, SpinnerStyle.class), model);
	}

	public Spinner (SpinnerStyle style, SpinnerModel model) {
		this.style = style;
		this.model = model;
		currentBackground = style.background;
		setSize(getPrefWidth(), getPrefHeight());

		this.addListener(new InputListener() {

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				setCurrentBackground(getBackgroundForClick(x, y));
				invalidate();
				return true;
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				// Update spinner value, and fire change events if needed
				if (getCurrentBackground() == getStyle().backgroundNext) {
					if (getModel().next()) Spinner.this.fire(new ChangeListener.ChangeEvent());
				} else if (getCurrentBackground() == getStyle().backgroundPrev) {
					if (getModel().previous()) Spinner.this.fire(new ChangeListener.ChangeEvent());
				}
				setCurrentBackground(getStyle().background);
				invalidate();
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				setCurrentBackground(getBackgroundForClick(x, y));
				invalidate();
			}

			@Override
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				// Get scroll focus for mouse scroll button
				if (getStage() != null) getStage().setScrollFocus(Spinner.this);
			}

			@Override
			public boolean scrolled (InputEvent event, float x, float y, int amount) {
				// Update spinner value, and fire change events if needed
				if (x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight()) {
					if (amount > 0) {
						if (getModel().previous()) Spinner.this.fire(new ChangeListener.ChangeEvent());
					} else if (amount < 0) {
						if (getModel().next()) Spinner.this.fire(new ChangeListener.ChangeEvent());
					}
					invalidate();
				}
				return false;
			}
		});
	}

	protected Drawable getCurrentBackground () {
		return this.currentBackground;
	}

	protected void setCurrentBackground (Drawable currentBackground) {
		this.currentBackground = currentBackground;
	}

	/** Retrieves the correct background given the location an event occured.
	 * @param x X coordinate of the fired event
	 * @param y Y coordinate of the fired event
	 * @return the background */
	protected Drawable getBackgroundForClick (float x, float y) {
		float diffX = getWidth() - x;
		float diffY = y;
		if (diffX < getStyle().background.getRightWidth() && diffX >= 0) {
			if (diffY <= getHeight() && diffY > getHeight() / 2)
				return getStyle().backgroundNext;
			else if (diffY >= 0 && diffY <= getHeight() / 2) return getStyle().backgroundPrev;
		}
		return getStyle().background;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		currentBackground.draw(batch, getX(), getY(), getWidth(), getHeight());
		// Center the text
		layout = new GlyphLayout(style.font, model.getValue().toString());
		style.font.draw(batch, model.getValue().toString() + "", getX() + style.background.getLeftWidth(),
			getY() + (getHeight() + layout.height) / 2);
	}

	@Override
	public float getPrefWidth () {
		// Min of borders + text width or drawable width
		layout = new GlyphLayout(style.font, model.getValue().toString());
		return Float.max(style.background.getMinWidth(),
			style.background.getLeftWidth() + layout.width + style.background.getRightWidth());
	}

	@Override
	public float getPrefHeight () {
		// Min of borders + text height or drawable height
		layout = new GlyphLayout(style.font, model.getValue().toString());
		return Float.max(style.background.getMinHeight(),
			style.background.getTopHeight() + style.background.getBottomHeight() + layout.height);
	}

	public void setStyle (SpinnerStyle style) {
		this.style = style;
		invalidate();
	}

	public SpinnerStyle getStyle () {
		return this.style;
	}

	public void setModel (SpinnerModel model) {
		this.model = model;
		invalidate();
	}

	public SpinnerModel getModel () {
		return this.model;
	}

	/** This class defines the appearance of a Spinner. it includes the font used and the drawables for idle state, next button
	 * pressed and previous button pressed. (See gdx-tests SpinnerTest) */
	public static class SpinnerStyle {

		public Drawable background, backgroundNext, backgroundPrev; // Backgrounds for the spinner states
		public BitmapFont font;

		public SpinnerStyle (Drawable background, Drawable backgroundNext, Drawable backgroundPrev, BitmapFont font) {
			this.background = background;
			this.backgroundNext = backgroundNext;
			this.backgroundPrev = backgroundPrev;
			this.font = font;
		}

		public SpinnerStyle () {
		}
	}

	/** Defines the behavior of the spinner
	 * @param <T> The data type of the spinner elements */
	public static abstract class SpinnerModel<T> {

		/** Called when the "next" (up) button of the spinner is pressed
		 * @return True if current element was changed */
		public abstract boolean next ();

		/** Called when the"previous" (down) button of the spinner is pressed
		 * @return True if the current element was changed */
		public abstract boolean previous ();

		/** Returns the current element
		 * @return current element */
		public abstract T getValue ();

	}

}
