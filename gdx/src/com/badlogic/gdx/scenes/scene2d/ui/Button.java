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

/** A button with text on it.
 * 
 * <h2>Functionality</h2> A button can be either in a pressed or unpressed state. A {@link ClickListener} can be registered with
 * the Button which will be called in case the button was clicked/touched.
 * 
 * <h2>Layout</h2> The (preferred) width and height of a Button are derrived from the border patches in the background
 * {@link NinePatch} as well as the bounding box around the multi-line text displayed on the Button. Use
 * {@link Button#setPrefSize(int, int)} to programmatically change the size to your liking. In case the width and height you set
 * are to small for the contained text you will see artifacts.
 * 
 * <h2>Style</h2> A Button is a {@link Widget} displaying a background {@link NinePatch} as well as multi-line text with a
 * specific font and color. The style is defined via an instance of {@link ButtonStyle}, which can be either done programmatically
 * or via a {@link Skin}.</p>
 * 
 * A Button's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <button name="styleName" 
 *         down="downNinePatch" 
 *         up="upNinePatch" 
 *         font="fontName" 
 *         fontColor="colorName"/>/>
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newButton(String, String, String)}.</li>
 * <li>The <code>down</code> attribute references a {@link NinePatch} by name, to be used as the button's background when it is
 * pressed</li>
 * <li>The <code>up</code> attribute references a {@link NinePatch} by name, to be used as the button's background when it is not
 * pressed</li>
 * <li>The <code>font</code> attribute references a {@link BitmapFont} by name, to be used to render the text on the button</li>
 * <li>The <code>fontColor</code> attribute references a {@link Color} by name, to be used to render the text on the button</li>
 * </ul>
 * 
 * @author mzechner */
public class Button extends Widget {
	final ButtonStyle style;
	String text;
	final TextBounds bounds = new TextBounds();
	boolean isPressed = false;
	ClickListener listener = null;

	/** Creates a new Button. The width and height of the Button are determined by its label test and style.
	 * @param name the namen
	 * @param label the label
	 * @param style the {@link ButtonStyle} */
	public Button (String name, String label, ButtonStyle style) {
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

		float textY = (int)(height * 0.5f) + (int)(bounds.height * 0.5f);
		font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
		font.drawMultiLine(batch, text, x + (int)(width * 0.5f), y + textY, 0, HAlignment.CENTER);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		isPressed = true;
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		if (listener != null && hit(x, y) != null) listener.click(this);
		isPressed = false;
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
	}

	/** Defines a button style, see {@link Button}
	 * @author mzechner */
	public static class ButtonStyle {
		public NinePatch down;
		public NinePatch up;
		public BitmapFont font;
		public Color fontColor;

		public ButtonStyle () {
		}

		public ButtonStyle (BitmapFont font, Color fontColor, NinePatch down, NinePatch up) {
			this.font = font;
			this.fontColor = fontColor;
			this.down = down;
			this.up = up;
		}
	}

	/** Interface for listening to click events of a button.
	 * @author mzechner */
	public interface ClickListener {
		public void click (Button button);
	}

	/** Sets the multi-line label text of this button. Causes invalidation of all parents.
	 * @param text */
	public void setText (String text) {
		this.text = text;
		invalidateHierarchy();
	}

	/** @return the label text of this button */
	public String getText () {
		return text;
	}

	/** Sets the {@link ClickListener} of this button
	 * @param listener the listener or null
	 * @return this Button for chaining */
	public Button setClickListener (ClickListener listener) {
		this.listener = listener;
		return this;
	}
}
