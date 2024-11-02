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
import com.badlogic.gdx.utils.Align;

/** @brief Represents a text map object */
public class TextMapObject extends MapObject {

	 private float x = 0.0f;
	 private float y = 0.0f;
	 private float width = 0.0f;
	 private float height = 0.0f;
	 private float rotation = 0.0f;
	 private String text;
	 //Alignment values as represented via libGDX Align
	 private int horizontalAlign = Align.left;
	 private int verticalAlign = Align.top;
	 //String alignment values
	 private String tiledHorizontalAlign;
	 private String tiledVerticalAlign;
	 private int pixelSize;
	 private String fontFamily;
	 private boolean bold;
	 private boolean italic;
	 private boolean underline;
	 private boolean strikeout;
	 private boolean kerning;
	 private boolean wrap;


	 /**
	  * Creates a TextMapObject, represents a Text map object
	  *
	  * @param x X coordinate
	  * @param y Y coordinate
	  * @param width width of the object bounds
	  * @param height height of the object bounds
	  * @param text a String representing the text for display
	  */
	 public TextMapObject (float x, float y, float width, float height, String text) {
		  super();
		  this.x = x;
		  this.y = y;
		  this.width = width;
		  this.height = height;
		  this.text = text;
	 }


	 private int parseHorizontalAlign(String horizontalAlign) {
		  if (horizontalAlign == null) return Align.left;
		  switch (horizontalAlign.toLowerCase()) {
		  case "left":
				return Align.left;
		  case "center":
				return Align.center;
		  case "right":
				return Align.right;
		  case "justify":
				// Since libGDX doesn't have 'justify'
				// For now, we can default to 'left'.
				return Align.left;
		  default:
				return Align.left;
		  }
	 }

	 private int parseVerticalAlign(String verticalAlign) {
		  if (verticalAlign == null) return Align.top;
		  switch (verticalAlign.toLowerCase()) {
		  case "top":
				return Align.top;
		  case "center":
				return Align.center;
		  case "bottom":
				return Align.bottom;
		  default:
				return Align.top;
		  }
	 }

	 /** @return object's X coordinate */
	 public float getX () {
		  return x;
	 }

	 /** @param x new X coordinate for the object */
	 public void setX (float x) {
		  this.x = x;
	 }

	 /** @return object's Y coordinate */
	 public float getY() {
		  return y;
	 }

	 /** @param y new Y coordinate for the object */
	 public void setY(float y) {
		  this.y = y;
	 }

	 /** @return object's width */
	 public float getWidth() {
		  return width;
	 }

	 /** @param width new width of the object */
	 public void setWidth(float width) {
		  this.width = width;
	 }

	 /** @return object's height */
	 public float getHeight() {
		  return height;
	 }

	 /** @param height new height of the object */
	 public void setHeight(float height) {
		  this.height = height;
	 }

	 /** @return object's rotation */
	 public float getRotation() {
		  return rotation;
	 }

	 /** @param rotation new rotation value for the object */
	 public void setRotation(float rotation) {
		  this.rotation = rotation;
	 }

	 /** @return object's text */
	 public String getText() {
		  return text;
	 }

	 /** @param text new text to display */
	 public void setText(String text) {
		  this.text = text;
	 }

	 /** @return A String describing object's horizontal alignment */
	 public String getTiledHorizontalAlign () {
		  return tiledHorizontalAlign;
	 }

	 /** @return int The Horizontal Align constant */
	 public int getHorizontalAlign () {
		  return horizontalAlign;
	 }

	 /**
	  * Sets the horizontal alignment of the text.
	  * Valid values are "left", "center", "right", or "justify" as specified in Tiled.
	  *
	  * @param horizontalAlign the horizontal alignment string from Tiled
	  */
	 public void setHorizontalAlign(String horizontalAlign) {
		  this.tiledHorizontalAlign = horizontalAlign;
		  this.horizontalAlign = parseHorizontalAlign(horizontalAlign);
	 }

	 /** @return String describing object's vertical alignment */
	 public String getTiledVerticalAlign () {
		  return tiledVerticalAlign;
	 }

	 /** @return int The Vertical Align constant */
	 public int getVerticalAlign () {
		  return verticalAlign;
	 }

	 /**
	  * Sets the vertical alignment of the text.
	  * Valid values are "top", "center" or "bottom" as specified in Tiled.
	  *
	  * @param verticalAlign the vertical alignment string from Tiled
	  */
	 public void setVerticalAlign (String verticalAlign) {
		  this.tiledVerticalAlign = verticalAlign;
		  this.verticalAlign = parseVerticalAlign(verticalAlign);
	 }

	 /** @return font pixel size */
	 public int getPixelSize() {
		  return pixelSize;
	 }

	 /** @param pixelSize the pixel size for the font */
	 public void setPixelSize(int pixelSize) {
		  this.pixelSize = pixelSize;
	 }

	 /** @return font family */
	 public String getFontFamily() {
		  return fontFamily;
	 }

	 /** @param fontFamily new font family */
	 public void setFontFamily(String fontFamily) {
		  this.fontFamily = fontFamily;
	 }

	 /** @return true if the font is bold */
	 public boolean isBold() {
		  return bold;
	 }

	 /** @param bold set font to bold or not */
	 public void setBold(boolean bold) {
		  this.bold = bold;
	 }

	 /** @return true if the font is italic */
	 public boolean isItalic() {
		  return italic;
	 }

	 /** @param italic set font to italic or not */
	 public void setItalic(boolean italic) {
		  this.italic = italic;
	 }

	 /** @return true if the font is underlined */
	 public boolean isUnderline() {
		  return underline;
	 }

	 /** @param underline set font to underline or not */
	 public void setUnderline(boolean underline) {
		  this.underline = underline;
	 }

	 /** @return true if the font is strikeout */
	 public boolean isStrikeout() {
		  return strikeout;
	 }

	 /** @param strikeout set font to strikeout or not */
	 public void setStrikeout(boolean strikeout) {
		  this.strikeout = strikeout;
	 }

	 /** @return true if kerning is enabled */
	 public boolean isKerning() {
		  return kerning;
	 }

	 /** @param kerning enable or disable kerning */
	 public void setKerning(boolean kerning) {
		  this.kerning = kerning;
	 }

	 /** @return true if text wrapping is enabled */
	 public boolean isWrap() {
		  return wrap;
	 }

	 /** @param wrap enable or disable text wrapping */
	 public void setWrap(boolean wrap) {
		  this.wrap = wrap;
	 }
}
