/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Disposable;

/** SpriteDebugger will draw a rectangle with a specified color around the bounding box of a sprite
 * @author Daniel Christopher
 * @version 4/26/15 */
public class SpriteDebugRenderer implements Disposable {
	/** The shape renderer in which bounding rectangles will be drawn **/
	private ShapeRenderer renderer;

	private Color tempColor;

	/** Specifies if the rectangles drawn should be filled or outlined **/
	private boolean fillMode;

	/** Constructs a new SpriteDebugger with a brand new shape renderer. */
	public SpriteDebugRenderer () {
		renderer = new ShapeRenderer();
	}

	/** Constructs a new SpriteDebugger with an existing shape renderer
	 * @param renderer The shape renderer to use when drawing */
	public SpriteDebugRenderer (ShapeRenderer renderer) {
		this.renderer = renderer;
	}

	/** Must be called before {@link #debugRender(Sprite sprite, Color color)} is called */
	public void begin () {
		renderer.begin(fillMode ? ShapeType.Filled : ShapeType.Line);
	}

	/** Must be called after {@link #debugRender(Sprite sprite, Color color)} is called */
	public void end () {
		renderer.end();
	}

	/** Draws a box around a sprite's bounds
	 * @param sprite The sprite to draw a debug box around
	 * @param color The color of which the debug box should be drawn */
	public void debugRender (Sprite sprite, Color color) {
		tempColor = renderer.getColor();

		renderer.setColor(color);

		renderer.rect(sprite.getBoundingRectangle().x, sprite.getBoundingRectangle().y, sprite.getBoundingRectangle().width,
			sprite.getBoundingRectangle().height);

		renderer.setColor(tempColor);
	}

	/** @return The shape renderer being used for debug rendering */
	public ShapeRenderer getRenderer () {
		return renderer;
	}

	/** Sets the shape renderer used for drawing debug lines
	 * @param renderer Shape renderer to use */
	public void setRenderer (ShapeRenderer renderer) {
		this.renderer = renderer;
	}

	/** Sets fill to either true or false. If fill is true, rectangles will be filled. If fill is false, rectangles will be
	 * outlined.
	 * @param fill Fill the rectangles or leave them outlined? */
	public void enableFillMode (boolean fill) {
		this.fillMode = fill;
	}

	/** @return Boolean representing if fill mode is enabled or not */
	public boolean isFillModeEnabled () {
		return fillMode;
	}

	/** Disposes the shape renderer used by this SpriteDebugger */
	@Override
	public void dispose () {
		renderer.dispose();
	}
}
