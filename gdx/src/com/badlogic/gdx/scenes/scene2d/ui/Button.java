
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

/** A button is a {@link Table} with a checked state and additional {@link ButtonStyle style} fields for pressed, unpressed, and
 * checked. Being a table, a button can contain any other actors.
 * <p>
 * The preferred size of the button is determined by the background ninepatch and the button contents.
 * @author Nathan Sweet */
public class Button extends Table {
	private ButtonStyle style;
	ClickListener listener;
	boolean isChecked;
	ButtonGroup buttonGroup;

	public Button (Skin skin) {
		this(skin.getStyle(ButtonStyle.class), null);
	}

	public Button (ButtonStyle style) {
		this(style, null);
	}

	public Button (Actor child, Skin skin) {
		this(child, skin.getStyle(ButtonStyle.class));
	}

	public Button (Actor child, ButtonStyle style) {
		this(style, null);
		add(child);
		pack();
	}

	public Button (ButtonStyle style, String name) {
		super(null, null, null, name);
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		setStyle(style);

		super.setClickListener(new ClickListener() {
			public void click (Actor actor, float x, float y) {
				boolean newChecked = !isChecked;
				setChecked(newChecked);
				// Don't fire listener if the button group reverted the change to isChecked.
				if (newChecked == isChecked && listener != null) listener.click(actor, x, y);
			}
		});
	}

	public void setChecked (boolean isChecked) {
		if (buttonGroup != null && !buttonGroup.canCheck(this, isChecked)) return;
		this.isChecked = isChecked;
	}

	public boolean isChecked () {
		return isChecked;
	}

	public void setStyle (ButtonStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		setBackground(isPressed ? style.down : style.up);
		invalidateHierarchy();
	}

	/** Returns the button's style. Modifying the returned style may not have an effect until {@link #setStyle(ButtonStyle)} is
	 * called. */
	public ButtonStyle getStyle () {
		return style;
	}

	/** @param listener May be null. */
	public void setClickListener (ClickListener listener) {
		this.listener = listener;
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		float offsetX = 0, offsetY = 0;
		if (isPressed) {
			setBackground(style.down == null ? style.up : style.down);
			offsetX = style.pressedOffsetX;
			offsetY = style.pressedOffsetY;
		} else {
			if (style.checked == null)
				setBackground(style.up);
			else
				setBackground(isChecked ? style.checked : style.up);
			offsetX = style.unpressedOffsetX;
			offsetY = style.unpressedOffsetY;
		}
		for (int i = 0; i < children.size(); i++) {
			Actor child = children.get(i);
			child.x += offsetX;
			child.y += offsetY;
		}
		super.draw(batch, parentAlpha);
		for (int i = 0; i < children.size(); i++) {
			Actor child = children.get(i);
			child.x -= offsetX;
			child.y -= offsetY;
		}
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
		public NinePatch down, up, checked;
		/** Optional. */
		public float pressedOffsetX, pressedOffsetY;
		/** Optional. */
		public float unpressedOffsetX, unpressedOffsetY;

		public ButtonStyle () {
		}

		public ButtonStyle (NinePatch down, NinePatch up, NinePatch checked, float pressedOffsetX, float pressedOffsetY,
			float unpressedOffsetX, float unpressedOffsetY) {
			this.down = down;
			this.up = up;
			this.checked = checked;
			this.pressedOffsetX = pressedOffsetX;
			this.pressedOffsetY = pressedOffsetY;
			this.unpressedOffsetX = unpressedOffsetX;
			this.unpressedOffsetY = unpressedOffsetY;
		}
	}
}
