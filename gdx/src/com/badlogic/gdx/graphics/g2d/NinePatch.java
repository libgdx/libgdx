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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A 3x3 grid of texture regions. Any of the regions may be omitted. Padding may be set as a hint on how to inset content on top
 * of the ninepatch (by default the eight "edge" textures of the ninepatch define the padding). When drawn, the four corner
 * patches will not be scaled, the interior patch will be scaled in both directions, and the middle patch for each edge will be
 * scaled in only one direction.
 * <p>
 * Note this class does not accept ".9.png" textures that include the metadata border pixels describing the splits (and padding)
 * for the ninepatch. That information is either passed to a constructor or defined implicitly by the size of the individual patch
 * textures. {@link TextureAtlas} is one way to generate a postprocessed ninepatch texture regions from ".9.png" files. */
public class NinePatch {
	static public final int TOP_LEFT = 0;
	static public final int TOP_CENTER = 1;
	static public final int TOP_RIGHT = 2;
	static public final int MIDDLE_LEFT = 3;
	static public final int MIDDLE_CENTER = 4;
	static public final int MIDDLE_RIGHT = 5;
	static public final int BOTTOM_LEFT = 6;
	/** Indices for the {@link #NinePatch(TextureRegion...)} constructor. */
	static public final int BOTTOM_CENTER = 7; // This field has the javadoc comment because it appears first in the javadocs.
	static public final int BOTTOM_RIGHT = 8;

	static private final Color tmpDrawColor = new Color();

	private Texture texture;
	private int bottomLeft, bottomCenter, bottomRight;
	private int middleLeft, middleCenter, middleRight;
	private int topLeft, topCenter, topRight;
	private float leftWidth, rightWidth, middleWidth, middleHeight, topHeight, bottomHeight;
	private float[] vertices = new float[9 * 4 * 5];
	private int idx;
	private final Color color = new Color(Color.WHITE);
	private float padLeft = -1, padRight = -1, padTop = -1, padBottom = -1;

	/** Create a ninepatch by cutting up the given texture into nine patches. The subsequent parameters define the 4 lines that
	 * will cut the texture region into 9 pieces.
	 * @param left Pixels from left edge.
	 * @param right Pixels from right edge.
	 * @param top Pixels from top edge.
	 * @param bottom Pixels from bottom edge. */
	public NinePatch (Texture texture, int left, int right, int top, int bottom) {
		this(new TextureRegion(texture), left, right, top, bottom);
	}

	/** Create a ninepatch by cutting up the given texture region into nine patches. The subsequent parameters define the 4 lines
	 * that will cut the texture region into 9 pieces.
	 * @param left Pixels from left edge.
	 * @param right Pixels from right edge.
	 * @param top Pixels from top edge.
	 * @param bottom Pixels from bottom edge. */
	public NinePatch (TextureRegion region, int left, int right, int top, int bottom) {
		if (region == null) throw new IllegalArgumentException("region cannot be null.");
		final int middleWidth = region.getRegionWidth() - left - right;
		final int middleHeight = region.getRegionHeight() - top - bottom;

		TextureRegion[] patches = new TextureRegion[9];
		if (top > 0) {
			if (left > 0) patches[TOP_LEFT] = new TextureRegion(region, 0, 0, left, top);
			if (middleWidth > 0) patches[TOP_CENTER] = new TextureRegion(region, left, 0, middleWidth, top);
			if (right > 0) patches[TOP_RIGHT] = new TextureRegion(region, left + middleWidth, 0, right, top);
		}
		if (middleHeight > 0) {
			if (left > 0) patches[MIDDLE_LEFT] = new TextureRegion(region, 0, top, left, middleHeight);
			if (middleWidth > 0) patches[MIDDLE_CENTER] = new TextureRegion(region, left, top, middleWidth, middleHeight);
			if (right > 0) patches[MIDDLE_RIGHT] = new TextureRegion(region, left + middleWidth, top, right, middleHeight);
		}
		if (bottom > 0) {
			if (left > 0) patches[BOTTOM_LEFT] = new TextureRegion(region, 0, top + middleHeight, left, bottom);
			if (middleWidth > 0) patches[BOTTOM_CENTER] = new TextureRegion(region, left, top + middleHeight, middleWidth, bottom);
			if (right > 0) patches[BOTTOM_RIGHT] = new TextureRegion(region, left + middleWidth, top + middleHeight, right, bottom);
		}

		// If split only vertical, move splits from right to center.
		if (left == 0 && middleWidth == 0) {
			patches[TOP_CENTER] = patches[TOP_RIGHT];
			patches[MIDDLE_CENTER] = patches[MIDDLE_RIGHT];
			patches[BOTTOM_CENTER] = patches[BOTTOM_RIGHT];
			patches[TOP_RIGHT] = null;
			patches[MIDDLE_RIGHT] = null;
			patches[BOTTOM_RIGHT] = null;
		}
		// If split only horizontal, move splits from bottom to center.
		if (top == 0 && middleHeight == 0) {
			patches[MIDDLE_LEFT] = patches[BOTTOM_LEFT];
			patches[MIDDLE_CENTER] = patches[BOTTOM_CENTER];
			patches[MIDDLE_RIGHT] = patches[BOTTOM_RIGHT];
			patches[BOTTOM_LEFT] = null;
			patches[BOTTOM_CENTER] = null;
			patches[BOTTOM_RIGHT] = null;
		}

		load(patches);
	}

	/** Construct a degenerate "nine" patch with only a center component. */
	public NinePatch (Texture texture, Color color) {
		this(texture);
		setColor(color);
	}

	/** Construct a degenerate "nine" patch with only a center component. */
	public NinePatch (Texture texture) {
		this(new TextureRegion(texture));
	}

	/** Construct a degenerate "nine" patch with only a center component. */
	public NinePatch (TextureRegion region, Color color) {
		this(region);
		setColor(color);
	}

	/** Construct a degenerate "nine" patch with only a center component. */
	public NinePatch (TextureRegion region) {
		load(new TextureRegion[] {
			//
			null, null, null, //
			null, region, null, //
			null, null, null //
		});
	}

	/** Construct a nine patch from the given nine texture regions. The provided patches must be consistently sized (e.g., any left
	 * edge textures must have the same width, etc). Patches may be <code>null</code>. Patch indices are specified via the public
	 * members {@link #TOP_LEFT}, {@link #TOP_CENTER}, etc. */
	public NinePatch (TextureRegion... patches) {
		if (patches == null || patches.length != 9) throw new IllegalArgumentException("NinePatch needs nine TextureRegions");

		load(patches);

		if ((patches[TOP_LEFT] != null && patches[TOP_LEFT].getRegionWidth() != leftWidth)
			|| (patches[MIDDLE_LEFT] != null && patches[MIDDLE_LEFT].getRegionWidth() != leftWidth)
			|| (patches[BOTTOM_LEFT] != null && patches[BOTTOM_LEFT].getRegionWidth() != leftWidth)) {
			throw new GdxRuntimeException("Left side patches must have the same width");
		}
		if ((patches[TOP_RIGHT] != null && patches[TOP_RIGHT].getRegionWidth() != rightWidth)
			|| (patches[MIDDLE_RIGHT] != null && patches[MIDDLE_RIGHT].getRegionWidth() != rightWidth)
			|| (patches[BOTTOM_RIGHT] != null && patches[BOTTOM_RIGHT].getRegionWidth() != rightWidth)) {
			throw new GdxRuntimeException("Right side patches must have the same width");
		}
		if ((patches[BOTTOM_LEFT] != null && patches[BOTTOM_LEFT].getRegionHeight() != bottomHeight)
			|| (patches[BOTTOM_CENTER] != null && patches[BOTTOM_CENTER].getRegionHeight() != bottomHeight)
			|| (patches[BOTTOM_RIGHT] != null && patches[BOTTOM_RIGHT].getRegionHeight() != bottomHeight)) {
			throw new GdxRuntimeException("Bottom side patches must have the same height");
		}
		if ((patches[TOP_LEFT] != null && patches[TOP_LEFT].getRegionHeight() != topHeight)
			|| (patches[TOP_CENTER] != null && patches[TOP_CENTER].getRegionHeight() != topHeight)
			|| (patches[TOP_RIGHT] != null && patches[TOP_RIGHT].getRegionHeight() != topHeight)) {
			throw new GdxRuntimeException("Top side patches must have the same height");
		}
	}

	public NinePatch (NinePatch ninePatch) {
		this(ninePatch, ninePatch.color);
	}

	public NinePatch (NinePatch ninePatch, Color color) {
		texture = ninePatch.texture;

		bottomLeft = ninePatch.bottomLeft;
		bottomCenter = ninePatch.bottomCenter;
		bottomRight = ninePatch.bottomRight;
		middleLeft = ninePatch.middleLeft;
		middleCenter = ninePatch.middleCenter;
		middleRight = ninePatch.middleRight;
		topLeft = ninePatch.topLeft;
		topCenter = ninePatch.topCenter;
		topRight = ninePatch.topRight;

		leftWidth = ninePatch.leftWidth;
		rightWidth = ninePatch.rightWidth;
		middleWidth = ninePatch.middleWidth;
		middleHeight = ninePatch.middleHeight;
		topHeight = ninePatch.topHeight;
		bottomHeight = ninePatch.bottomHeight;

		padLeft = ninePatch.padLeft;
		padTop = ninePatch.padTop;
		padBottom = ninePatch.padBottom;
		padRight = ninePatch.padRight;

		vertices = new float[ninePatch.vertices.length];
		System.arraycopy(ninePatch.vertices, 0, vertices, 0, ninePatch.vertices.length);
		idx = ninePatch.idx;
		this.color.set(color);
	}

	private void load (TextureRegion[] patches) {
		if (patches[BOTTOM_LEFT] != null) {
			bottomLeft = add(patches[BOTTOM_LEFT], false, false);
			leftWidth = patches[BOTTOM_LEFT].getRegionWidth();
			bottomHeight = patches[BOTTOM_LEFT].getRegionHeight();
		} else
			bottomLeft = -1;
		if (patches[BOTTOM_CENTER] != null) {
			bottomCenter = add(patches[BOTTOM_CENTER], patches[BOTTOM_LEFT] != null || patches[BOTTOM_RIGHT] != null, false);
			middleWidth = Math.max(middleWidth, patches[BOTTOM_CENTER].getRegionWidth());
			bottomHeight = Math.max(bottomHeight, patches[BOTTOM_CENTER].getRegionHeight());
		} else
			bottomCenter = -1;
		if (patches[BOTTOM_RIGHT] != null) {
			bottomRight = add(patches[BOTTOM_RIGHT], false, false);
			rightWidth = Math.max(rightWidth, patches[BOTTOM_RIGHT].getRegionWidth());
			bottomHeight = Math.max(bottomHeight, patches[BOTTOM_RIGHT].getRegionHeight());
		} else
			bottomRight = -1;
		if (patches[MIDDLE_LEFT] != null) {
			middleLeft = add(patches[MIDDLE_LEFT], false, patches[TOP_LEFT] != null || patches[BOTTOM_LEFT] != null);
			leftWidth = Math.max(leftWidth, patches[MIDDLE_LEFT].getRegionWidth());
			middleHeight = Math.max(middleHeight, patches[MIDDLE_LEFT].getRegionHeight());
		} else
			middleLeft = -1;
		if (patches[MIDDLE_CENTER] != null) {
			middleCenter = add(patches[MIDDLE_CENTER], patches[MIDDLE_LEFT] != null || patches[MIDDLE_RIGHT] != null,
				patches[TOP_CENTER] != null || patches[BOTTOM_CENTER] != null);
			middleWidth = Math.max(middleWidth, patches[MIDDLE_CENTER].getRegionWidth());
			middleHeight = Math.max(middleHeight, patches[MIDDLE_CENTER].getRegionHeight());
		} else
			middleCenter = -1;
		if (patches[MIDDLE_RIGHT] != null) {
			middleRight = add(patches[MIDDLE_RIGHT], false, patches[TOP_RIGHT] != null || patches[BOTTOM_RIGHT] != null);
			rightWidth = Math.max(rightWidth, patches[MIDDLE_RIGHT].getRegionWidth());
			middleHeight = Math.max(middleHeight, patches[MIDDLE_RIGHT].getRegionHeight());
		} else
			middleRight = -1;
		if (patches[TOP_LEFT] != null) {
			topLeft = add(patches[TOP_LEFT], false, false);
			leftWidth = Math.max(leftWidth, patches[TOP_LEFT].getRegionWidth());
			topHeight = Math.max(topHeight, patches[TOP_LEFT].getRegionHeight());
		} else
			topLeft = -1;
		if (patches[TOP_CENTER] != null) {
			topCenter = add(patches[TOP_CENTER], patches[TOP_LEFT] != null || patches[TOP_RIGHT] != null, false);
			middleWidth = Math.max(middleWidth, patches[TOP_CENTER].getRegionWidth());
			topHeight = Math.max(topHeight, patches[TOP_CENTER].getRegionHeight());
		} else
			topCenter = -1;
		if (patches[TOP_RIGHT] != null) {
			topRight = add(patches[TOP_RIGHT], false, false);
			rightWidth = Math.max(rightWidth, patches[TOP_RIGHT].getRegionWidth());
			topHeight = Math.max(topHeight, patches[TOP_RIGHT].getRegionHeight());
		} else
			topRight = -1;
		if (idx < vertices.length) {
			float[] newVertices = new float[idx];
			System.arraycopy(vertices, 0, newVertices, 0, idx);
			vertices = newVertices;
		}
	}

	private int add (TextureRegion region, boolean isStretchW, boolean isStretchH) {
		if (texture == null)
			texture = region.getTexture();
		else if (texture != region.getTexture()) //
			throw new IllegalArgumentException("All regions must be from the same texture.");

		// Add half pixel offsets on stretchable dimensions to avoid color bleeding when GL_LINEAR
		// filtering is used for the texture. This nudges the texture coordinate to the center
		// of the texel where the neighboring pixel has 0% contribution in linear blending mode.
		float u = region.u, v = region.v2, u2 = region.u2, v2 = region.v;
		if (texture.getMagFilter() == TextureFilter.Linear || texture.getMinFilter() == TextureFilter.Linear) {
			if (isStretchW) {
				float halfTexelWidth = 0.5f * 1f / texture.getWidth();
				u += halfTexelWidth;
				u2 -= halfTexelWidth;
			}
			if (isStretchH) {
				float halfTexelHeight = 0.5f * 1f / texture.getHeight();
				v -= halfTexelHeight;
				v2 += halfTexelHeight;
			}
		}

		float[] vertices = this.vertices;
		int i = idx;
		vertices[i + 3] = u;
		vertices[i + 4] = v;

		vertices[i + 8] = u;
		vertices[i + 9] = v2;

		vertices[i + 13] = u2;
		vertices[i + 14] = v2;

		vertices[i + 18] = u2;
		vertices[i + 19] = v;
		idx += 20;
		return i;
	}

	/** Set the coordinates and color of a ninth of the patch. */
	private void set (int idx, float x, float y, float width, float height, float color) {
		final float fx2 = x + width;
		final float fy2 = y + height;
		final float[] vertices = this.vertices;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = color;

		vertices[idx + 5] = x;
		vertices[idx + 6] = fy2;
		vertices[idx + 7] = color;

		vertices[idx + 10] = fx2;
		vertices[idx + 11] = fy2;
		vertices[idx + 12] = color;

		vertices[idx + 15] = fx2;
		vertices[idx + 16] = y;
		vertices[idx + 17] = color;
	}

	private void prepareVertices (Batch batch, float x, float y, float width, float height) {
		final float centerX = x + leftWidth;
		final float centerY = y + bottomHeight;
		final float centerWidth = width - rightWidth - leftWidth;
		final float centerHeight = height - topHeight - bottomHeight;
		final float rightX = x + width - rightWidth;
		final float topY = y + height - topHeight;
		final float c = tmpDrawColor.set(color).mul(batch.getColor()).toFloatBits();
		if (bottomLeft != -1) set(bottomLeft, x, y, leftWidth, bottomHeight, c);
		if (bottomCenter != -1) set(bottomCenter, centerX, y, centerWidth, bottomHeight, c);
		if (bottomRight != -1) set(bottomRight, rightX, y, rightWidth, bottomHeight, c);
		if (middleLeft != -1) set(middleLeft, x, centerY, leftWidth, centerHeight, c);
		if (middleCenter != -1) set(middleCenter, centerX, centerY, centerWidth, centerHeight, c);
		if (middleRight != -1) set(middleRight, rightX, centerY, rightWidth, centerHeight, c);
		if (topLeft != -1) set(topLeft, x, topY, leftWidth, topHeight, c);
		if (topCenter != -1) set(topCenter, centerX, topY, centerWidth, topHeight, c);
		if (topRight != -1) set(topRight, rightX, topY, rightWidth, topHeight, c);
	}

	public void draw (Batch batch, float x, float y, float width, float height) {
		prepareVertices(batch, x, y, width, height);
		batch.draw(texture, vertices, 0, idx);
	}

	public void draw (Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation) {
		prepareVertices(batch, x, y, width, height);
		float worldOriginX = x + originX, worldOriginY = y + originY;
		int n = this.idx;
		float[] vertices = this.vertices;
		if (rotation != 0) {
			for (int i = 0; i < n; i += 5) {
				float vx = (vertices[i] - worldOriginX) * scaleX, vy = (vertices[i + 1] - worldOriginY) * scaleY;
				float cos = MathUtils.cosDeg(rotation), sin = MathUtils.sinDeg(rotation);
				vertices[i] = cos * vx - sin * vy + worldOriginX;
				vertices[i + 1] = sin * vx + cos * vy + worldOriginY;
			}
		} else if (scaleX != 1 || scaleY != 1) {
			for (int i = 0; i < n; i += 5) {
				vertices[i] = (vertices[i] - worldOriginX) * scaleX + worldOriginX;
				vertices[i + 1] = (vertices[i + 1] - worldOriginY) * scaleY + worldOriginY;
			}
		}
		batch.draw(texture, vertices, 0, n);
	}

	/** Copy given color. The color will be blended with the batch color, then combined with the texture colors at
	 * {@link NinePatch#draw(Batch, float, float, float, float) draw} time. Default is {@link Color#WHITE}. */
	public void setColor (Color color) {
		this.color.set(color);
	}

	public Color getColor () {
		return color;
	}

	public float getLeftWidth () {
		return leftWidth;
	}

	/** Set the draw-time width of the three left edge patches */
	public void setLeftWidth (float leftWidth) {
		this.leftWidth = leftWidth;
	}

	public float getRightWidth () {
		return rightWidth;
	}

	/** Set the draw-time width of the three right edge patches */
	public void setRightWidth (float rightWidth) {
		this.rightWidth = rightWidth;
	}

	public float getTopHeight () {
		return topHeight;
	}

	/** Set the draw-time height of the three top edge patches */
	public void setTopHeight (float topHeight) {
		this.topHeight = topHeight;
	}

	public float getBottomHeight () {
		return bottomHeight;
	}

	/** Set the draw-time height of the three bottom edge patches */
	public void setBottomHeight (float bottomHeight) {
		this.bottomHeight = bottomHeight;
	}

	public float getMiddleWidth () {
		return middleWidth;
	}

	/** Set the width of the middle column of the patch. At render time, this is implicitly the requested render-width of the
	 * entire nine patch, minus the left and right width. This value is only used for computing the {@link #getTotalWidth() default
	 * total width}. */
	public void setMiddleWidth (float middleWidth) {
		this.middleWidth = middleWidth;
	}

	public float getMiddleHeight () {
		return middleHeight;
	}

	/** Set the height of the middle row of the patch. At render time, this is implicitly the requested render-height of the entire
	 * nine patch, minus the top and bottom height. This value is only used for computing the {@link #getTotalHeight() default
	 * total height}. */
	public void setMiddleHeight (float middleHeight) {
		this.middleHeight = middleHeight;
	}

	public float getTotalWidth () {
		return leftWidth + middleWidth + rightWidth;
	}

	public float getTotalHeight () {
		return topHeight + middleHeight + bottomHeight;
	}

	/** Set the padding for content inside this ninepatch. By default the padding is set to match the exterior of the ninepatch, so
	 * the content should fit exactly within the middle patch. */
	public void setPadding (float left, float right, float top, float bottom) {
		this.padLeft = left;
		this.padRight = right;
		this.padTop = top;
		this.padBottom = bottom;
	}

	/** Returns the left padding if set, else returns {@link #getLeftWidth()}. */
	public float getPadLeft () {
		if (padLeft == -1) return getLeftWidth();
		return padLeft;
	}

	/** See {@link #setPadding(float, float, float, float)} */
	public void setPadLeft (float left) {
		this.padLeft = left;
	}

	/** Returns the right padding if set, else returns {@link #getRightWidth()}. */
	public float getPadRight () {
		if (padRight == -1) return getRightWidth();
		return padRight;
	}

	/** See {@link #setPadding(float, float, float, float)} */
	public void setPadRight (float right) {
		this.padRight = right;
	}

	/** Returns the top padding if set, else returns {@link #getTopHeight()}. */
	public float getPadTop () {
		if (padTop == -1) return getTopHeight();
		return padTop;
	}

	/** See {@link #setPadding(float, float, float, float)} */
	public void setPadTop (float top) {
		this.padTop = top;
	}

	/** Returns the bottom padding if set, else returns {@link #getBottomHeight()}. */
	public float getPadBottom () {
		if (padBottom == -1) return getBottomHeight();
		return padBottom;
	}

	/** See {@link #setPadding(float, float, float, float)} */
	public void setPadBottom (float bottom) {
		this.padBottom = bottom;
	}

	/** Multiplies the top/left/bottom/right sizes and padding by the specified amount. */
	public void scale (float scaleX, float scaleY) {
		leftWidth *= scaleX;
		rightWidth *= scaleX;
		topHeight *= scaleY;
		bottomHeight *= scaleY;
		middleWidth *= scaleX;
		middleHeight *= scaleY;
		if (padLeft != -1) padLeft *= scaleX;
		if (padRight != -1) padRight *= scaleX;
		if (padTop != -1) padTop *= scaleY;
		if (padBottom != -1) padBottom *= scaleY;
	}

	public Texture getTexture () {
		return texture;
	}
}
