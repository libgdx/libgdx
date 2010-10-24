/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics;

/**
 * A Pixmap represents a plattform specific bitmap. On Android it encapsulates a Bitmap on the desktop it encapsulates a
 * BufferedImage. Various methods are available to manipulate the Pixmap. This uses the common bitmap coordinate system with the
 * origin being in the top left corner and the y-axis pointing downwards. Bitmaps can be disposed to free all related resources.
 * Use after disposing will result in nasty exceptions.
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public interface Pixmap {
	/**
	 * Different pixel formats.
	 * 
	 * @author mzechner
	 * 
	 */
	public enum Format {
		Alpha, RGB565, RGBA4444, RGBA8888
	}

	/**
	 * Sets the color for the following drawing operations.
	 * 
	 * @param r The red component.
	 * @param g The green component.
	 * @param b The blue component.
	 * @param a The alpha component.
	 */
	public void setColor (float r, float g, float b, float a);

	/**
	 * Fills the complete bitmap with the currently set color.
	 */
	public void fill ();

	/**
	 * Sets the width in pixels of strokes.
	 * 
	 * @param width The stroke width in pixels.
	 */
	public void setStrokeWidth (int width);

	/**
	 * Draws a line between the given coordinates using the currently set color and stroke width.
	 * @param x The x-coodinate of the first point
	 * @param y The y-coordinate of the first point
	 * @param x2 The x-coordinate of the first point
	 * @param y2 The y-coordinate of the first point
	 */
	public void drawLine (int x, int y, int x2, int y2);

	/**
	 * Draws a rectangle outline starting at x, y extending by width to the right and by height downwards (y-axis points downwards)
	 * using the current color and stroke width.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels
	 */
	public void drawRectangle (int x, int y, int width, int height);

	/**
	 * Draws an area form another Pixmap to this Pixmap.
	 * 
	 * @param Pixmap The other Pixmap
	 * @param x The target x-coordinate
	 * @param y The target y-coordinate
	 * @param srcx The source x-coordinate
	 * @param srcy The source y-coordinate
	 * @param width The width of the area form the other Pixmap in pixels
	 * @param height The height of the area form the other Pixmap in pixles
	 */
	public void drawPixmap (Pixmap Pixmap, int x, int y, int srcx, int srcy, int width, int height);

	/**
	 * Fills a rectangle starting at x, y extending by width to the right and by height downwards (y-axis points downwards) using
	 * the current color.
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels
	 */
	public void fillRectangle (int x, int y, int width, int height);

	/**
	 * Draws a circle outline with the center at x,y and a radius using the current color and stroke width.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels
	 */
	public void drawCircle (int x, int y, int radius);

	/**
	 * Fills a circle with the center at x,y and a radius using the current color.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels
	 */
	public void fillCircle (int x, int y, int radius);

	/**
	 * Returns the 32-bit argb value of the pixel at x, y
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @return The pixel color in 32-bit argb format.
	 */
	public int getPixel (int x, int y);

	/**
	 * Returns the yth row of pixels form the Pixmap in the provided pixels array. The pixels array must have at least
	 * Pixmap.getWidth() elements. The values are returned as 32-bit argb values.
	 * 
	 * @param pixels The array to store the pixels in.
	 * @param y The y-coordinate of the row.
	 */
	public void getPixelRow (int[] pixels, int y);

	/**
	 * @return The native bitmap object that backs this interface, Bitmap on Android, BufferedImage on PC
	 */
	public Object getNativePixmap ();

	/**
	 * @return The width of the Pixmap in pixels.
	 */
	public int getWidth ();

	/**
	 * @return The height of the Pixmap in pixels.
	 */
	public int getHeight ();

	/**
	 * Releases all resources associated with this Pixmap.
	 */
	public void dispose ();
}
