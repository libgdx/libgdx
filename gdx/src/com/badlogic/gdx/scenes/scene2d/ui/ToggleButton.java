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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** A toggle button.
 * 
 * <h2>Functionality</h2> A toggle button can be either in a pressed or unpressed state. A {@link ClickListener} can be registered
 * with the ToggleButton which will be called in case the button changed its state.
 * 
 * <h2>Layout</h2> The (preferred) width and height of an ImageToggleButton are derived from the border patches in the background
 * {@link NinePatch} as well as the bounding box of the multi-line label displayed inside the ToggleButton. Use
 * {@link #setPrefSize(int, int)} to programmatically change the size to your liking. In case the width and height you set are to
 * small for the contained label you will see artifacts.
 * 
 * <h2>Style</h2> A ToggleButton is a {@link Widget} displaying a background {@link NinePatch} as well as multi-line text with a
 * specific font and color. The style is defined via an instance of {@link ToggleButtonStyle}, which can be either done
 * programmatically or via a {@link Skin}.</p>
 * 
 * A Button's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <togglebutton name="styleName" 
 *               down="downNinePatch" 
 *               up="upNinePatch" 
 *               font="fontName" 
 *               fontColor="colorName"/>/>
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newToggleButton(String, String, String)}.</li>
 * <li>The <code>down</code> attribute references a {@link NinePatch} by name, to be used as the toggle button's background when
 * it is pressed</li>
 * <li>The <code>up</code> attribute references a {@link NinePatch} by name, to be used as the toggle button's background when it
 * is not pressed</li>
 * <li>The <code>font</code> attribute references a {@link BitmapFont} by name, to be used to render the text on the toggle button
 * </li>
 * <li>The <code>fontColor</code> attribute references a {@link Color} by name, to be used to render the text on the toggle button
 * </li>
 * </ul>
 * 
 * @author mzechner */
public class ToggleButton extends Widget {
	final ToggleButtonStyle style;
	String text;
	final TextBounds bounds = new TextBounds();
	boolean isPressed = false;
	ClickListener listener = null;

	/** Creates a new ToggleButton. The width and height are determined by the background {@link NinePatch} border patches as well
	 * as the bounding box around the contained text label.
	 * @param name the name
	 * @param label the multi-line label
	 * @param style the {@link ToggleButtonStyle} */
	public ToggleButton (String name, String label, ToggleButtonStyle style) {
		super(name, 0, 0);
		this.style = style;
		this.text = label;
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}

	@Override
	public void layout () {
		final BitmapFont font = style.font;
		final NinePatch downPatch = style.down;
		bounds.set(font.getMultiLineBounds(text));

		prefHeight = downPatch.getBottomHeight() + downPatch.getTopHeight() + bounds.height + -font.getDescent() * 2;
		prefWidth = downPatch.getLeftWidth() + downPatch.getRightWidth() + bounds.width;
		invalidated = false;
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;
		final NinePatch downPatch = style.down;
		final NinePatch upPatch = style.up;

		if (invalidated) layout();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (isPressed)
			downPatch.draw(batch, x, y, width, height);
		else
			upPatch.draw(batch, x, y, width, height);

		float textY = (int)(height / 2) + (int)(bounds.height / 2);
		font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
		font.drawMultiLine(batch, text, x + (int)(width / 2), y + textY, 0, HAlignment.CENTER);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (hit(x, y) != null) {
			isPressed = !isPressed;
			if (listener != null) listener.click(this, isPressed);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp (float x, float y, int pointer) {
		return false;
	}

	@Override
	public boolean touchDragged (float x, float y, int pointer) {
		return false;
	}

	@Override
	public Actor hit (float x, float y) {
		return x > 0 && x < width && y > 0 && y < height ? this : null;
	}

	/** Defines the style of a toggle button, see {@link ToggleButton}
	 * @author mzechner */
	public static class ToggleButtonStyle {
		public final NinePatch down;
		public final NinePatch up;
		public final BitmapFont font;
		public final Color fontColor;

		public ToggleButtonStyle (BitmapFont font, Color fontColor, NinePatch down, NinePatch up) {
			this.font = font;
			this.fontColor = fontColor;
			this.down = down;
			this.up = up;
		}
	}

	/** Interface to listen for button state changes.
	 * @author mzechner */
	public interface ClickListener {
		public void click (ToggleButton button, boolean isPressed);
	}

	/** Sets the text label of this button. Invalidates all parents.
	 * @param text the text */
	public void setText (String text) {
		this.text = text;
		invalidateHierarchy();
	}

	/** @return the text label of this button */
	public String getText () {
		return text;
	}

	/** Sets the {@link ClickListener} of this button.
	 * @param listener the listener or null
	 * @return this {@link ToggleButton} for chaining */
	public ToggleButton setClickListener (ClickListener listener) {
		this.listener = listener;
		return this;
	}

	/** @return whether this button is pressed or not. */
	public boolean isPressed () {
		return isPressed;
	}

	/** Sets whether this button is pressed or not.
	 * @param isPressed whether the button is pressed or not */
	public void setPressed (boolean isPressed) {
		this.isPressed = isPressed;
	}
}
