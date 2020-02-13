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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pools;

/** A button is a {@link Table} with a checked state and additional {@link ButtonStyle style} fields for pressed, unpressed, and
 * checked. Each time a button is clicked, the checked state is toggled. Being a table, a button can contain any other actors.<br>
 * <br>
 * The button's padding is set to the background drawable's padding when the background changes, overwriting any padding set
 * manually. Padding can still be set on the button's table cells.
 * <p>
 * {@link ChangeEvent} is fired when the button is clicked. Cancelling the event will restore the checked button state to what is
 * was previously.
 * <p>
 * The preferred size of the button is determined by the background and the button contents.
 * @author Nathan Sweet */
public class Button extends Table implements Disableable {
	private ButtonStyle style;
	boolean isChecked, isDisabled;
	boolean focused;
	ButtonGroup buttonGroup;
	private ClickListener clickListener;
	private boolean programmaticChangeEvents = true;

	public Button (Skin skin) {
		super(skin);
		initialize();
		setStyle(skin.get(ButtonStyle.class));
		setSize(getPrefWidth(), getPrefHeight());
	}

	public Button (Skin skin, String styleName) {
		super(skin);
		initialize();
		setStyle(skin.get(styleName, ButtonStyle.class));
		setSize(getPrefWidth(), getPrefHeight());
	}

	public Button (Actor child, Skin skin, String styleName) {
		this(child, skin.get(styleName, ButtonStyle.class));
		setSkin(skin);
	}

	public Button (Actor child, ButtonStyle style) {
		initialize();
		add(child);
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
	}

	public Button (ButtonStyle style) {
		initialize();
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
	}

	/** Creates a button without setting the style or size. At least a style must be set before using this button. */
	public Button () {
		initialize();
	}

	private void initialize () {
		setTouchable(Touchable.enabled);
		addListener(clickListener = new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				if (isDisabled()) return;
				setChecked(!isChecked, true);
			}
		});
		addListener(new FocusListener() {
			public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
				Button.this.focused = focused;
			}
		});
	}

	public Button (@Null Drawable up) {
		this(new ButtonStyle(up, null, null));
	}

	public Button (@Null Drawable up, @Null Drawable down) {
		this(new ButtonStyle(up, down, null));
	}

	public Button (@Null Drawable up, @Null Drawable down, @Null Drawable checked) {
		this(new ButtonStyle(up, down, checked));
	}

	public Button (Actor child, Skin skin) {
		this(child, skin.get(ButtonStyle.class));
	}

	public void setChecked (boolean isChecked) {
		setChecked(isChecked, programmaticChangeEvents);
	}

	void setChecked (boolean isChecked, boolean fireEvent) {
		if (this.isChecked == isChecked) return;
		if (buttonGroup != null && !buttonGroup.canCheck(this, isChecked)) return;
		this.isChecked = isChecked;

		if (fireEvent) {
			ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
			if (fire(changeEvent)) this.isChecked = !isChecked;
			Pools.free(changeEvent);
		}
	}

	/** Toggles the checked state. This method changes the checked state, which fires a {@link ChangeEvent} (if programmatic change
	 * events are enabled), so can be used to simulate a button click. */
	public void toggle () {
		setChecked(!isChecked);
	}

	public boolean isChecked () {
		return isChecked;
	}

	public boolean isPressed () {
		return clickListener.isVisualPressed();
	}

	public boolean isOver () {
		return clickListener.isOver();
	}

	public ClickListener getClickListener () {
		return clickListener;
	}

	public boolean isDisabled () {
		return isDisabled;
	}

	/** When true, the button will not toggle {@link #isChecked()} when clicked and will not fire a {@link ChangeEvent}. */
	public void setDisabled (boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	/** If false, {@link #setChecked(boolean)} and {@link #toggle()} will not fire {@link ChangeEvent}, event will be fired only
	 * when user clicked the button */
	public void setProgrammaticChangeEvents (boolean programmaticChangeEvents) {
		this.programmaticChangeEvents = programmaticChangeEvents;
	}

	public void setStyle (ButtonStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;

		Drawable background = null;
		if (isPressed() && !isDisabled())
			background = style.down == null ? style.up : style.down;
		else {
			if (isDisabled() && style.disabled != null)
				background = style.disabled;
			else if (isChecked && style.checked != null) {
				if (isOver() && style.checkedOver != null)
					background = style.checkedOver;
				else if (focused && style.checkedFocused != null)
					background = style.checkedFocused;
				else
					background = style.checked;
			} else if (isOver() && style.over != null)
				background = style.over;
			else if (focused && style.focused != null)
				background = style.focused;
			else
				background = style.up;
		}
		setBackground(background);
	}

	/** Returns the button's style. Modifying the returned style may not have an effect until {@link #setStyle(ButtonStyle)} is
	 * called. */
	public ButtonStyle getStyle () {
		return style;
	}

	/** @return May be null. */
	@Null
	public ButtonGroup getButtonGroup () {
		return buttonGroup;
	}

	public void draw (Batch batch, float parentAlpha) {
		validate();

		boolean isDisabled = isDisabled();
		boolean isPressed = isPressed();
		boolean isChecked = isChecked();
		boolean isOver = isOver();

		Drawable background = null;
		if (isDisabled && style.disabled != null) {
			background = style.disabled;
		} else if (isPressed && style.down != null) {
			background = style.down;
		} else if (isChecked && style.checked != null) {
			if (style.checkedOver != null && isOver) {
				background = style.checkedOver;
			} else if (style.checkedFocused != null && focused) {
				background = style.checkedFocused;
			} else {
				background = style.checked;
			}
		} else if (isOver && style.over != null) {
			background = style.over;
		} else if (focused && style.focused != null) {
			background = style.focused;
		} else if (style.up != null) {
			background = style.up;
		}
		setBackground(background);

		float offsetX = 0, offsetY = 0;
		if (isPressed && !isDisabled) {
			offsetX = style.pressedOffsetX;
			offsetY = style.pressedOffsetY;
		} else if (isChecked && !isDisabled) {
			offsetX = style.checkedOffsetX;
			offsetY = style.checkedOffsetY;
		} else {
			offsetX = style.unpressedOffsetX;
			offsetY = style.unpressedOffsetY;
		}

		Array<Actor> children = getChildren();
		for (int i = 0; i < children.size; i++)
			children.get(i).moveBy(offsetX, offsetY);
		super.draw(batch, parentAlpha);
		for (int i = 0; i < children.size; i++)
			children.get(i).moveBy(-offsetX, -offsetY);

		Stage stage = getStage();
		if (stage != null && stage.getActionsRequestRendering() && isPressed != clickListener.isPressed())
			Gdx.graphics.requestRendering();
	}

	public float getPrefWidth () {
		float width = super.getPrefWidth();
		if (style.up != null) width = Math.max(width, style.up.getMinWidth());
		if (style.down != null) width = Math.max(width, style.down.getMinWidth());
		if (style.checked != null) width = Math.max(width, style.checked.getMinWidth());
		return width;
	}

	public float getPrefHeight () {
		float height = super.getPrefHeight();
		if (style.up != null) height = Math.max(height, style.up.getMinHeight());
		if (style.down != null) height = Math.max(height, style.down.getMinHeight());
		if (style.checked != null) height = Math.max(height, style.checked.getMinHeight());
		return height;
	}

	public float getMinWidth () {
		return getPrefWidth();
	}

	public float getMinHeight () {
		return getPrefHeight();
	}

	/** The style for a button, see {@link Button}.
	 * @author mzechner */
	static public class ButtonStyle {
		/** Optional. */
		@Null public Drawable up, down, over, focused, checked, checkedOver, checkedFocused, disabled;
		/** Optional. */
		public float pressedOffsetX, pressedOffsetY, unpressedOffsetX, unpressedOffsetY, checkedOffsetX, checkedOffsetY;

		public ButtonStyle () {
		}

		public ButtonStyle (@Null Drawable up, @Null Drawable down, @Null Drawable checked) {
			this.up = up;
			this.down = down;
			this.checked = checked;
		}

		public ButtonStyle (ButtonStyle style) {
			this.up = style.up;
			this.down = style.down;
			this.over = style.over;
			this.focused = style.focused;
			this.checked = style.checked;
			this.checkedOver = style.checkedOver;
			this.checkedFocused = style.checkedFocused;
			this.disabled = style.disabled;
			this.pressedOffsetX = style.pressedOffsetX;
			this.pressedOffsetY = style.pressedOffsetY;
			this.unpressedOffsetX = style.unpressedOffsetX;
			this.unpressedOffsetY = style.unpressedOffsetY;
			this.checkedOffsetX = style.checkedOffsetX;
			this.checkedOffsetY = style.checkedOffsetY;
		}
	}
}
