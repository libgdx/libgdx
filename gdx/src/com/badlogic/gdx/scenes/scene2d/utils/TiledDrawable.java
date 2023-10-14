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
import com.badlogic.gdx.utils.Align;

/** Draws a {@link TextureRegion} repeatedly to fill the area, instead of stretching it.
 * @author Nathan Sweet
 * @author Thomas Creutzenberg */
public class TiledDrawable extends TextureRegionDrawable {
	private final Color color = new Color(1, 1, 1, 1);
	private float scale = 1;
	private int align = Align.bottomLeft;

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

		draw(batch, getRegion(), x, y, width, height, scale, align);

		batch.setPackedColor(oldColor);
	}

	public static void draw (Batch batch, TextureRegion textureRegion, float x, float y, float width, float height, float scale,
		int align) {
		final float regionWidth = textureRegion.getRegionWidth() * scale;
		final float regionHeight = textureRegion.getRegionHeight() * scale;

		final Texture texture = textureRegion.getTexture();
		final float textureWidth = texture.getWidth() * scale;
		final float textureHeight = texture.getHeight() * scale;
		final float u = textureRegion.getU();
		final float v = textureRegion.getV();
		final float u2 = textureRegion.getU2();
		final float v2 = textureRegion.getV2();

		int fullX = (int)(width / regionWidth);
		final float leftPartialWidth;
		final float rightPartialWidth;
		if (Align.isLeft(align)) {
			leftPartialWidth = 0f;
			rightPartialWidth = width - (regionWidth * fullX);
		} else if (Align.isRight(align)) {
			leftPartialWidth = width - (regionWidth * fullX);
			rightPartialWidth = 0f;
		} else {
			if (fullX != 0) {
				fullX = fullX % 2 == 1 ? fullX : fullX - 1;
				final float leftRight = 0.5f * (width - (regionWidth * fullX));
				leftPartialWidth = leftRight;
				rightPartialWidth = leftRight;
			} else {
				leftPartialWidth = 0f;
				rightPartialWidth = 0f;
			}
		}
		int fullY = (int)(height / regionHeight);
		final float topPartialHeight;
		final float bottomPartialHeight;
		if (Align.isTop(align)) {
			topPartialHeight = 0f;
			bottomPartialHeight = height - (regionHeight * fullY);
		} else if (Align.isBottom(align)) {
			topPartialHeight = height - (regionHeight * fullY);
			bottomPartialHeight = 0f;
		} else {
			if (fullY != 0) {
				fullY = fullY % 2 == 1 ? fullY : fullY - 1;
				final float topBottom = 0.5f * (height - (regionHeight * fullY));
				topPartialHeight = topBottom;
				bottomPartialHeight = topBottom;
			} else {
				topPartialHeight = 0f;
				bottomPartialHeight = 0f;
			}
		}

		float drawX = x;
		float drawY = y;

		// Left edge
		if (leftPartialWidth > 0f) {
			final float leftEdgeU = u2 - (leftPartialWidth / textureWidth);

			// Left bottom partial
			if (bottomPartialHeight > 0f) {
				final float leftBottomV = v + (bottomPartialHeight / textureHeight);
				batch.draw(texture, drawX, drawY, leftPartialWidth, bottomPartialHeight, leftEdgeU, leftBottomV, u2, v);
				drawY += bottomPartialHeight;
			}

			// Left center partials
			if (fullY == 0 && Align.isCenterVertical(align)) {
				final float vOffset = 0.5f * (v2 - v) * (1f - (height / regionHeight));
				final float leftCenterV = v2 - vOffset;
				final float leftCenterV2 = v + vOffset;
				batch.draw(texture, drawX, drawY, leftPartialWidth, height, leftEdgeU, leftCenterV, u2, leftCenterV2);
				drawY += height;
			} else {
				for (int i = 0; i < fullY; i++) {
					batch.draw(texture, drawX, drawY, leftPartialWidth, regionHeight, leftEdgeU, v2, u2, v);
					drawY += regionHeight;
				}
			}

			// Left top partial
			if (topPartialHeight > 0f) {
				final float leftTopV = v2 - (topPartialHeight / textureHeight);
				batch.draw(texture, drawX, drawY, leftPartialWidth, topPartialHeight, leftEdgeU, v2, u2, leftTopV);
			}
		}

		// Center full texture regions
		{
			// Center bottom partials
			if (bottomPartialHeight > 0f) {
				drawX = x + leftPartialWidth;
				drawY = y;

				final float centerBottomV = v + (bottomPartialHeight / textureHeight);

				if (fullX == 0 && Align.isCenterHorizontal(align)) {
					final float uOffset = 0.5f * (u2 - u) * (1f - (width / regionWidth));
					final float centerBottomU = u + uOffset;
					final float centerBottomU2 = u2 - uOffset;
					batch.draw(texture, drawX, drawY, width, bottomPartialHeight, centerBottomU, centerBottomV, centerBottomU2, v);
					drawX += width;
				} else {
					for (int i = 0; i < fullX; i++) {
						batch.draw(texture, drawX, drawY, regionWidth, bottomPartialHeight, u, centerBottomV, u2, v);
						drawX += regionWidth;
					}
				}
			}

			// Center full texture regions
			{
				drawX = x + leftPartialWidth;

				final int originalFullX = fullX;
				final int originalFullY = fullY;

				float centerCenterDrawWidth = regionWidth;
				float centerCenterDrawHeight = regionHeight;
				float centerCenterU = u;
				float centerCenterU2 = u2;
				float centerCenterV = v2;
				float centerCenterV2 = v;
				if (fullX == 0 && Align.isCenterHorizontal(align)) {
					fullX = 1;
					centerCenterDrawWidth = width;
					final float uOffset = 0.5f * (u2 - u) * (1f - (width / regionWidth));
					centerCenterU = u + uOffset;
					centerCenterU2 = u2 - uOffset;
				}
				if (fullY == 0 && Align.isCenterVertical(align)) {
					fullY = 1;
					centerCenterDrawHeight = height;
					final float vOffset = 0.5f * (v2 - v) * (1f - (height / regionHeight));
					centerCenterV = v2 - vOffset;
					centerCenterV2 = v + vOffset;
				}
				for (int i = 0; i < fullX; i++) {
					drawY = y + bottomPartialHeight;
					for (int ii = 0; ii < fullY; ii++) {
						batch.draw(texture, drawX, drawY, centerCenterDrawWidth, centerCenterDrawHeight, centerCenterU, centerCenterV,
							centerCenterU2, centerCenterV2);
						drawY += centerCenterDrawHeight;
					}
					drawX += centerCenterDrawWidth;
				}

				fullX = originalFullX;
				fullY = originalFullY;
			}

			// Center top partials
			if (topPartialHeight > 0f) {
				drawX = x + leftPartialWidth;

				final float centerTopV = v2 - (topPartialHeight / textureHeight);

				if (fullX == 0 && Align.isCenterHorizontal(align)) {
					final float uOffset = 0.5f * (u2 - u) * (1f - (width / regionWidth));
					final float centerTopU = u + uOffset;
					final float centerTopU2 = u2 - uOffset;
					batch.draw(texture, drawX, drawY, width, topPartialHeight, centerTopU, v2, centerTopU2, centerTopV);
					drawX += width;
				} else {
					for (int i = 0; i < fullX; i++) {
						batch.draw(texture, drawX, drawY, regionWidth, topPartialHeight, u, v2, u2, centerTopV);
						drawX += regionWidth;
					}
				}
			}
		}

		// Right edge
		if (rightPartialWidth > 0f) {
			drawY = y;

			final float rightEdgeU2 = u + (rightPartialWidth / textureWidth);

			// Right bottom partial
			if (bottomPartialHeight > 0f) {
				final float rightBottomV = v + (bottomPartialHeight / textureHeight);
				batch.draw(texture, drawX, drawY, rightPartialWidth, bottomPartialHeight, u, rightBottomV, rightEdgeU2, v);
				drawY += bottomPartialHeight;
			}

			// Right center partials
			if (fullY == 0 && Align.isCenterVertical(align)) {
				final float vOffset = 0.5f * (v2 - v) * (1f - (height / regionHeight));
				final float rightCenterV = v2 - vOffset;
				final float rightCenterV2 = v + vOffset;
				batch.draw(texture, drawX, drawY, rightPartialWidth, height, u, rightCenterV, rightEdgeU2, rightCenterV2);
				drawY += height;
			} else {
				for (int i = 0; i < fullY; i++) {
					batch.draw(texture, drawX, drawY, rightPartialWidth, regionHeight, u, v2, rightEdgeU2, v);
					drawY += regionHeight;
				}
			}

			// Right top partial
			if (topPartialHeight > 0f) {
				final float rightTopV = v2 - (topPartialHeight / textureHeight);
				batch.draw(texture, drawX, drawY, rightPartialWidth, topPartialHeight, u, v2, rightEdgeU2, rightTopV);
			}
		}
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

	public int getAlign () {
		return align;
	}

	public void setAlign (int align) {
		this.align = align;
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
