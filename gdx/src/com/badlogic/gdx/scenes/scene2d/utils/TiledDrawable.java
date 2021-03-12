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

package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Draws a {@link TextureRegion} repeatedly to fill the area, instead of stretching it.
 * @author Nathan Sweet */
public class TiledDrawable extends TextureRegionDrawable {
	private final Color color = new Color(1, 1, 1, 1);
	private float scale = 1;

	public TiledDrawable () {
		super();
	}

	public TiledDrawable (TextureRegion region) {
		super(region);
	}

	public TiledDrawable (TextureRegionDrawable drawable) {
		super(drawable);
	}

	public void draw (Batch batch, float x, float y, float width, float height) {
		float oldColor = batch.getPackedColor();
		batch.setColor(batch.getColor().mul(color));

		TextureRegion region = getRegion();
		float regionWidth = region.getRegionWidth() * scale, regionHeight = region.getRegionHeight() * scale;
		int fullX = (int)(width / regionWidth), fullY = (int)(height / regionHeight);
		float remainingX = width - regionWidth * fullX, remainingY = height - regionHeight * fullY;
		float startX = x, startY = y;
		float endX = x + width - remainingX, endY = y + height - remainingY;
		for (int i = 0; i < fullX; i++) {
			y = startY;
			for (int ii = 0; ii < fullY; ii++) {
				batch.draw(region, x, y, regionWidth, regionHeight);
				y += regionHeight;
			}
			x += regionWidth;
		}
		Texture texture = region.getTexture();
		float u = region.getU();
		float v2 = region.getV2();
		if (remainingX > 0) {
			// Right edge.
			float u2 = u + remainingX / (texture.getWidth() * scale);
			float v = region.getV();
			y = startY;
			for (int ii = 0; ii < fullY; ii++) {
				batch.draw(texture, x, y, remainingX, regionHeight, u, v2, u2, v);
				y += regionHeight;
			}
			// Upper right corner.
			if (remainingY > 0) {
				v = v2 - remainingY / (texture.getHeight() * scale);
				batch.draw(texture, x, y, remainingX, remainingY, u, v2, u2, v);
			}
		}
		if (remainingY > 0) {
			// Top edge.
			float u2 = region.getU2();
			float v = v2 - remainingY / (texture.getHeight() * scale);
			x = startX;
			for (int i = 0; i < fullX; i++) {
				batch.draw(texture, x, y, regionWidth, remainingY, u, v2, u2, v);
				x += regionWidth;
			}
		}

		batch.setPackedColor(oldColor);
	}

	public void draw (Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation) {
		throw new UnsupportedOperationException();
	}

	public Color getColor () {
		return color;
	}

	public void setScale (float scale) {
		this.scale = scale;
	}

	public float getScale () {
		return scale;
	}

	public TiledDrawable tint (Color tint) {
		TiledDrawable drawable = new TiledDrawable(this);
		drawable.color.set(tint);
		drawable.setLeftWidth(getLeftWidth());
		drawable.setRightWidth(getRightWidth());
		drawable.setTopHeight(getTopHeight());
		drawable.setBottomHeight(getBottomHeight());
		return drawable;
	}
}
