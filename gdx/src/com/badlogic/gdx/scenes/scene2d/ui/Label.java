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
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.NumberUtils;

/** A label.
 * 
 * <h2>Functionality</h2> A Label displays multi-line text.
 * 
 * <h2>Layout</h2> The (preferred) width and height of a Label are derrived from the bounding box of the text contained in it. Use
 * {@link #setPrefSize(int, int)} to change this programmatically. If the text doesn't fit into the specified area artifacts will
 * be visible.
 * 
 * <h3>Style</h3> A label is a {@link Widget} displaying text via a {@link BitmapFont} and a {@link Color}. The style is defined
 * via an instance of {@link LabelStyle}, which can be either done programmatically or via a {@link Skin}. A label's text will
 * always be rendered starting from the top edge of it's bouding box, define by its width and height, not the text's bounding box.
 * 
 * A Label's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <label name="styleName" 
 *        font="fontName" 
 *        color="colorName"
 *                    />
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newLabel(String, String, String)}.</li>
 * <li>The <code>font</code> attribute references a {@link BitmapFont} by name, to be used to render the label's text.</li>
 * <li>The <code>fontColor</code> attribute references a {@link Color} by name, to be used to render the label's text.</li>
 * </ul>
 * 
 * @author mzechner */
public class Label extends Widget {
	final LabelStyle style;
	final BitmapFontCache cache;
	String text;

	public Label (String name, String text, LabelStyle style) {
		super(name, 0, 0);
		this.style = style;

		cache = new BitmapFontCache(style.font);
		cache.setColor(style.fontColor);
		setText(text);

		width = prefWidth;
		height = prefHeight;
		touchable = false;
	}

	public void setText (String text) {
		this.text = text;
		TextBounds bounds = style.font.getMultiLineBounds(text);
		cache.setMultiLineText(text, 0, bounds.height);
		prefWidth = bounds.width;
		prefHeight = bounds.height - style.font.getDescent(); // Centers text in the label.
		invalidateHierarchy();
	}

	public String getText () {
		return text;
	}

	public void setColor (float color) {
		cache.setColor(color);
	}

	public void setColor (Color tint) {
		cache.setColor(tint);
	}

	public void setColor (float r, float g, float b, float a) {
		cache.setColor(r, g, b, a);
	}

	public Color getColor () {
		return cache.getColor();
	}

	@Override
	public void layout () {
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		cache.setPosition(x, y);
		cache.draw(batch, parentAlpha);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		return false;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {

	}

	@Override
	public Actor hit (float x, float y) {
		return null;
	}

	/** Defines a label's style, see {@link Label}
	 * @author mzechner */
	public static class LabelStyle {
		public BitmapFont font;
		public Color fontColor;

		public LabelStyle () {
		}

		public LabelStyle (BitmapFont font, Color fontColor) {
			this.font = font;
			this.fontColor = fontColor;
		}
	}
}
