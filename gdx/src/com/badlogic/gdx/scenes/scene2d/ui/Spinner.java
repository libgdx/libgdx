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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Pools;

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
	private SpinnerState state = SpinnerState.IDLE; // The current state of the spinner

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
		layout = new GlyphLayout(style.font, model.getDisplayValue());
		setSize(getPrefWidth(), getPrefHeight());

		this.addListener(new InputListener() {

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				updateBackgroundForCoords(x, y);
				return true;
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				// Update spinner value if mouse is hovering over next or previous button
				if (getState() == SpinnerState.NEXT)
					updateValue(true);
				else if (getState() == SpinnerState.PREVIOUS) updateValue(false);
				updateBackgroundForCoords(x, y);
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				updateBackgroundForCoords(x, y);
			}

			@Override
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				// Get scroll focus and keyboard focus to allow for update of spinner with mouse and keyboard
				if (getStage() != null) {
					getStage().setScrollFocus(Spinner.this);
					getStage().setKeyboardFocus(Spinner.this);
				}
				updateBackgroundForCoords(x, y);
			}

			@Override
			public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				updateBackgroundForCoords(x, y);
			}

			@Override
			public boolean scrolled (InputEvent event, float x, float y, int amount) {
				// Update spinner value if mouse is hovering over widget
				if (getState() != SpinnerState.IDLE) updateValue(amount < 0);
				return false;
			}

			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				// Update spinner value if mouse is hovering over widget
				if (getState() != SpinnerState.IDLE) {
					if (keycode == Input.Keys.UP)
						updateValue(true);
					else if (keycode == Input.Keys.DOWN) updateValue(false);
				}
				return false;
			}

		});
	}

	/** This method is called by any event that triggers a modification to the spinner's current value.
	 * @param increase True for spinner's next value and false for spinner's previous value */
	protected void updateValue (boolean increase) {
		// Update value, and if change occurred, dispatch a change event
		if ((increase && getModel().next()) || (!increase && getModel().previous())) {
			layout.setText(style.font, model.getDisplayValue()); // Update layout for new current value
			ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
			fire(changeEvent);
			Pools.free(changeEvent);
		}
		invalidate();
	}

	protected SpinnerState getState () {
		return state;
	}

	protected void setState (SpinnerState state) {
		this.state = state;
	}

	/** Updates the spinner's background and state for the given coordinates
	 * @param x Current x coordinate of mouse
	 * @param y Y Current y coordinate of mouse */
	protected void updateBackgroundForCoords (float x, float y) {
		float diffX = getWidth() - x;
		float diffY = y;
		state = SpinnerState.IDLE;
		currentBackground = getStyle().background;
		if (Gdx.input.isButtonPressed(Buttons.LEFT) && diffX < getStyle().background.getRightWidth() && diffX >= 0) {
			if (diffY <= getHeight() && diffY > getHeight() / 2) {
				state = SpinnerState.NEXT;
				currentBackground = getStyle().backgroundNext;
			} else if (diffY >= 0 && diffY <= getHeight() / 2) {
				state = SpinnerState.PREVIOUS;
				currentBackground = getStyle().backgroundPrev;
			}
		} else if (diffX >= 0 && diffX <= getWidth() && diffY >= 0 && diffY <= getHeight()) {
			state = SpinnerState.HOVER;
			currentBackground = getStyle().backgroundHover;
		}
		invalidate();
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		currentBackground.draw(batch, getX(), getY(), getWidth(), getHeight());
		// Center the text
		style.font.draw(batch, model.getDisplayValue() + "", getX() + style.background.getLeftWidth(),
			getY() + (getHeight() + layout.height) / 2);
	}

	@Override
	public float getPrefWidth () {
		// Max of borders + text width or drawable width
		return Math.max(style.background.getMinWidth(),
			style.background.getLeftWidth() + layout.width + style.background.getRightWidth());
	}

	@Override
	public float getPrefHeight () {
		// Max of borders + text height or drawable height
		return Math.max(style.background.getMinHeight(),
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

	/** A spinner has four state. When the mouse is not over it, it is IDLE. When the mouse is over it, it is either over the NEXT
	 * button, the PREVIOUS button or otherwise simply in a HOVER state (mouse is over the widget, but not over the buttons) */
	enum SpinnerState {
		IDLE, HOVER, NEXT, PREVIOUS;
	}

	/** This class defines the appearance of a Spinner. it includes the font used and the drawables for idle, hover, next button
	 * pressed and previous button pressed states. (See gdx-tests SpinnerTest) */
	public static class SpinnerStyle {

		public Drawable background, backgroundHover, backgroundNext, backgroundPrev; // Backgrounds for the spinner states
		public BitmapFont font;

		/** Constructor
		 * @param background Drawable for the idle state
		 * @param backgroundHover Drawable for the general hover state
		 * @param backgroundNext Drawable for the next button hover state
		 * @param backgroundPrev Drawable for the previous button hover state
		 * @param font Font used for display */
		public SpinnerStyle (Drawable background, Drawable backgroundHover, Drawable backgroundNext, Drawable backgroundPrev,
			BitmapFont font) {
			this.background = background;
			this.backgroundHover = backgroundHover;
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

		/** The text that will be displayed in the spinner for it's current value.
		 * @return text displayed */
		public abstract CharSequence getDisplayValue ();

	}

}
