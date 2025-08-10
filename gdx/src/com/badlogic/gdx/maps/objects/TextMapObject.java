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

package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;

/** @brief Represents a text map object */
public class TextMapObject extends MapObject {

	// defaults set based on tiled docs
	private float rotation = 0.0f;
	private String text = "";
	private int pixelSize = 16;
	private String fontFamily = "";
	private boolean bold = false;
	private boolean italic = false;
	private boolean underline = false;
	private boolean strikeout = false;
	private boolean kerning = true;
	private boolean wrap = true;

	// possible values: "left", "center", "right", "justify" (default: "left")
	private String horizontalAlign = "left";
	// possible values: "top", "center", "bottom" (default: "top")
	private String verticalAlign = "top";

	// Rectangle shape representing the object's bounds
	private Rectangle rectangle;

	/** Creates an empty text object with bounds starting in the lower left corner at (0, 0) with width=1 and height=1 */
	public TextMapObject () {
		this(0.0f, 0.0f, 1.0f, 1.0f, "");
	}

	/** Creates a TextMapObject, represents a text map object
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param width width of the object bounds
	 * @param height height of the object bounds
	 * @param text a String representing the text for display */
	public TextMapObject (float x, float y, float width, float height, String text) {
		super();
		rectangle = new Rectangle(x, y, width, height);
		this.text = text;
	}

	/** @return rectangle representing object bounds */
	public Rectangle getRectangle () {
		return rectangle;
	}

	/** @return object's X coordinate */
	public float getX () {
		return rectangle.getX();
	}

	/** @return object's Y coordinate */
	public float getY () {
		return rectangle.getY();
	}

	/** @return object's bounds height */
	public float getWidth () {
		return rectangle.getWidth();
	}

	/** @return object's bounds height */
	public float getHeight () {
		return rectangle.getHeight();
	}

	/** @return object's rotation */
	public float getRotation () {
		return rotation;
	}

	/** @param rotation new rotation value for the object */
	public void setRotation (float rotation) {
		this.rotation = rotation;
	}

	/** @return object's text */
	public String getText () {
		return text;
	}

	/** @param text new text to display */
	public void setText (String text) {
		this.text = text;
	}

	/** @return A String describing object's horizontal alignment */
	public String getHorizontalAlign () {
		return horizontalAlign;
	}

	/** @param horizontalAlign the horizontal alignment string from Tiled */
	public void setHorizontalAlign (String horizontalAlign) {
		this.horizontalAlign = horizontalAlign;
	}

	/** @return String describing object's vertical alignment */
	public String getVerticalAlign () {
		return verticalAlign;
	}

	/** @param verticalAlign the vertical alignment string from Tiled */
	public void setVerticalAlign (String verticalAlign) {
		this.verticalAlign = verticalAlign;
	}

	/** @return font pixel size */
	public int getPixelSize () {
		return pixelSize;
	}

	/** @param pixelSize the pixel size for the font */
	public void setPixelSize (int pixelSize) {
		this.pixelSize = pixelSize;
	}

	/** @return font family */
	public String getFontFamily () {
		return fontFamily;
	}

	/** @param fontFamily new font family */
	public void setFontFamily (String fontFamily) {
		this.fontFamily = fontFamily;
	}

	/** @return true if the font is bold */
	public boolean isBold () {
		return bold;
	}

	/** @param bold set font to bold or not */
	public void setBold (boolean bold) {
		this.bold = bold;
	}

	/** @return true if the font is italic */
	public boolean isItalic () {
		return italic;
	}

	/** @param italic set font to italic or not */
	public void setItalic (boolean italic) {
		this.italic = italic;
	}

	/** @return true if the font is underlined */
	public boolean isUnderline () {
		return underline;
	}

	/** @param underline set font to underline or not */
	public void setUnderline (boolean underline) {
		this.underline = underline;
	}

	/** @return true if the font is strikeout */
	public boolean isStrikeout () {
		return strikeout;
	}

	/** @param strikeout set font to strikeout or not */
	public void setStrikeout (boolean strikeout) {
		this.strikeout = strikeout;
	}

	/** @return true if kerning is enabled */
	public boolean isKerning () {
		return kerning;
	}

	/** @param kerning enable or disable kerning */
	public void setKerning (boolean kerning) {
		this.kerning = kerning;
	}

	/** @return true if text wrapping is enabled */
	public boolean isWrap () {
		return wrap;
	}

	/** @param wrap enable or disable text wrapping */
	public void setWrap (boolean wrap) {
		this.wrap = wrap;
	}
}
