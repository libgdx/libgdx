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
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A 3x3 grid of texture regions. Any of the regions may be omitted. Padding may be set as a hint on how to inset content on top
 * of the ninepatch (by default the eight "edge" textures of the nine-patch define the padding). When drawn the eight "edge"
 * patches will not be scaled, only the interior patch will be scaled.
 * 
 * <p>
 * <b>NOTE</b>: This class expects a "post-processed" nine-patch, and not a raw ".9.png" texture. That is, the textures given to
 * this class should <em>not</em> include the meta-data pixels from a ".9.png" that describe the layout of the ninepatch over the
 * interior of the graphic. That information should be passed into the constructor either implicitly as the size of the individual
 * patch textures, or via the <code>left, right, top, bottom</code> parameters to {@link #NinePatch(Texture, int, int, int, int)}
 * or {@link #NinePatch(TextureRegion, int, int, int, int)}.
 * 
 * <p>
 * A correctly created {@link TextureAtlas} is one way to generate a post-processed nine-patch from a ".9.png" file. */
public class NinePatch {
	public static final int TOP_LEFT = 0;
	public static final int TOP_CENTER = 1;
	public static final int TOP_RIGHT = 2;
	public static final int MIDDLE_LEFT = 3;
	public static final int MIDDLE_CENTER = 4;
	public static final int MIDDLE_RIGHT = 5;
	public static final int BOTTOM_LEFT = 6;
	/** Indices for {@link #NinePatch(TextureRegion...)} constructor */
	// alphabetically first in javadoc
	public static final int BOTTOM_CENTER = 7;
	public static final int BOTTOM_RIGHT = 8;

	static private final Color tmpDrawColor = new Color();

	private Texture texture;
	private int bottomLeft = -1, bottomCenter = -1, bottomRight = -1;
	private int middleLeft = -1, middleCenter = -1, middleRight = -1;
	private int topLeft = -1, topCenter = -1, topRight = -1;
	private float leftWidth, rightWidth, middleWidth, middleHeight, topHeight, bottomHeight;
	private float[] vertices = new float[9 * 4 * 5];
	private int idx;
	private final Color color = new Color(Color.WHITE);
	private int padLeft = -1, padRight = -1, padTop = -1, padBottom = -1;
	private float scaleX = 1f;
	private float scaleY = 1f;

	/** Create a ninepatch by cutting up the given texture into nine patches. The subsequent parameters define the 4 lines that will
	 * cut the texture region into 9 pieces.
	 * 
	 * @param left Pixels from left edge.
	 * @param right Pixels from right edge.
	 * @param top Pixels from top edge.
	 * @param bottom Pixels from bottom edge. */
	public NinePatch (Texture texture, int left, int right, int top, int bottom) {
		this(new TextureRegion(texture), left, right, top, bottom);
	}

	/** Create a ninepatch by cutting up the given texture region into nine patches. The subsequent parameters define the 4 lines
	 * that will cut the texture region into 9 pieces.
	 * 
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

		float leftWidth = getLeftWidth();
		if ((patches[TOP_LEFT] != null && patches[TOP_LEFT].getRegionWidth() != leftWidth)
			|| (patches[MIDDLE_LEFT] != null && patches[MIDDLE_LEFT].getRegionWidth() != leftWidth)
			|| (patches[BOTTOM_LEFT] != null && patches[BOTTOM_LEFT].getRegionWidth() != leftWidth)) {
			throw new GdxRuntimeException("Left side patches must have the same width");
		}

		float rightWidth = getRightWidth();
		if ((patches[TOP_RIGHT] != null && patches[TOP_RIGHT].getRegionWidth() != rightWidth)
			|| (patches[MIDDLE_RIGHT] != null && patches[MIDDLE_RIGHT].getRegionWidth() != rightWidth)
			|| (patches[BOTTOM_RIGHT] != null && patches[BOTTOM_RIGHT].getRegionWidth() != rightWidth)) {
			throw new GdxRuntimeException("Right side patches must have the same width");
		}

		float bottomHeight = getBottomHeight();
		if ((patches[BOTTOM_LEFT] != null && patches[BOTTOM_LEFT].getRegionHeight() != bottomHeight)
			|| (patches[BOTTOM_CENTER] != null && patches[BOTTOM_CENTER].getRegionHeight() != bottomHeight)
			|| (patches[BOTTOM_RIGHT] != null && patches[BOTTOM_RIGHT].getRegionHeight() != bottomHeight)) {
			throw new GdxRuntimeException("Bottom side patches must have the same height");
		}

		float topHeight = getTopHeight();
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
		final float color = Color.WHITE.toFloatBits(); // placeholder color, overwritten at draw time

		if (patches[BOTTOM_LEFT] != null) {
			bottomLeft = add(patches[BOTTOM_LEFT], color, false, false);
			leftWidth = patches[BOTTOM_LEFT].getRegionWidth();
			bottomHeight = patches[BOTTOM_LEFT].getRegionHeight();
		}
		if (patches[BOTTOM_CENTER] != null) {
			bottomCenter = add(patches[BOTTOM_CENTER], color, true, false);
			middleWidth = Math.max(middleWidth, patches[BOTTOM_CENTER].getRegionWidth());
			bottomHeight = Math.max(bottomHeight, patches[BOTTOM_CENTER].getRegionHeight());
		}
		if (patches[BOTTOM_RIGHT] != null) {
			bottomRight = add(patches[BOTTOM_RIGHT], color, false, false);
			rightWidth = Math.max(rightWidth, patches[BOTTOM_RIGHT].getRegionWidth());
			bottomHeight = Math.max(bottomHeight, patches[BOTTOM_RIGHT].getRegionHeight());
		}
		if (patches[MIDDLE_LEFT] != null) {
			middleLeft = add(patches[MIDDLE_LEFT], color, false, true);
			leftWidth = Math.max(leftWidth, patches[MIDDLE_LEFT].getRegionWidth());
			middleHeight = Math.max(middleHeight, patches[MIDDLE_LEFT].getRegionHeight());
		}
		if (patches[MIDDLE_CENTER] != null) {
			middleCenter = add(patches[MIDDLE_CENTER], color, true, true);
			middleWidth = Math.max(middleWidth, patches[MIDDLE_CENTER].getRegionWidth());
			middleHeight = Math.max(middleHeight, patches[MIDDLE_CENTER].getRegionHeight());
		}
		if (patches[MIDDLE_RIGHT] != null) {
			middleRight = add(patches[MIDDLE_RIGHT], color, false, true);
			rightWidth = Math.max(rightWidth, patches[MIDDLE_RIGHT].getRegionWidth());
			middleHeight = Math.max(middleHeight, patches[MIDDLE_RIGHT].getRegionHeight());
		}
		if (patches[TOP_LEFT] != null) {
			topLeft = add(patches[TOP_LEFT], color, false, false);
			leftWidth = Math.max(leftWidth, patches[TOP_LEFT].getRegionWidth());
			topHeight = Math.max(topHeight, patches[TOP_LEFT].getRegionHeight());
		}
		if (patches[TOP_CENTER] != null) {
			topCenter = add(patches[TOP_CENTER], color, true, false);
			middleWidth = Math.max(middleWidth, patches[TOP_CENTER].getRegionWidth());
			topHeight = Math.max(topHeight, patches[TOP_CENTER].getRegionHeight());
		}
		if (patches[TOP_RIGHT] != null) {
			topRight = add(patches[TOP_RIGHT], color, false, false);
			rightWidth = Math.max(rightWidth, patches[TOP_RIGHT].getRegionWidth());
			topHeight = Math.max(topHeight, patches[TOP_RIGHT].getRegionHeight());
		}
		if (idx < vertices.length) {
			float[] newVertices = new float[idx];
			System.arraycopy(vertices, 0, newVertices, 0, idx);
			vertices = newVertices;
		}
	}

	private int add (TextureRegion region, float color, boolean isStretchW, boolean isStretchH) {
		if (texture == null)
			texture = region.getTexture();
		else if (texture != region.getTexture()) //
			throw new IllegalArgumentException("All regions must be from the same texture.");

		float u = region.u;
		float v = region.v2;
		float u2 = region.u2;
		float v2 = region.v;

		// Add half pixel offsets on stretchable dimensions to avoid color bleeding when GL_LINEAR
		// filtering is used for the texture. This nudges the texture coordinate to the center
		// of the texel where the neighboring pixel has 0% contribution in linear blending mode.
		if (isStretchW) {
			float halfTexelWidth = 0.5f * 1.0f / texture.getWidth();
			u += halfTexelWidth;
			u2 -= halfTexelWidth;
		}
		if (isStretchH) {
			float halfTexelHeight = 0.5f * 1.0f / texture.getHeight();
			v -= halfTexelHeight;
			v2 += halfTexelHeight;
		}

		final float[] vertices = this.vertices;

		idx += 2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx] = v;
		idx += 3;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx] = v2;
		idx += 3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx] = v2;
		idx += 3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		return idx - 4 * 5;
	}

	/** Set the coordinates and color of a ninth of the patch. */
	private void set (int idx, float x, float y, float width, float height, float color) {
		final float fx2 = x + width;
		final float fy2 = y + height;
		final float[] vertices = this.vertices;
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx] = color;
		idx += 3;
		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx] = color;
		idx += 3;
		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx] = color;
		idx += 3;
		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx] = color;
	}

	public void draw (Batch batch, float x, float y, float width, float height) {
		final float centerColumnX = x + leftWidth;
		final float rightColumnX = x + width - rightWidth;
		final float middleRowY = y + bottomHeight;
		final float topRowY = y + height - topHeight;
		
		final float centerColumnX2 = x + (leftWidth * scaleX);
		final float rightColumnX2 = x + width - (rightWidth * scaleX);
		final float middleRowY2 = y + (bottomHeight * scaleY);
		final float topRowY2 = y + height - (topHeight * scaleY);
		
		final float c = tmpDrawColor.set(color).mul(batch.getColor()).toFloatBits();

		if (bottomLeft != -1)
			set(bottomLeft, x, y, (centerColumnX - x) * scaleX,
					(middleRowY - y) * scaleY, c);
		if (bottomCenter != -1)
			set(bottomCenter, centerColumnX2, y,
					rightColumnX2 - centerColumnX2, (middleRowY - y) * scaleY,
					c);
		if (bottomRight != -1)
			set(bottomRight, rightColumnX2, y, (x + width - rightColumnX)
					* scaleX, (middleRowY - y) * scaleY, c);
		if (middleLeft != -1)
			set(middleLeft, x, middleRowY2, (centerColumnX - x) * scaleX,
					topRowY2 - middleRowY2, c);
		if (middleCenter != -1)
			set(middleCenter, centerColumnX2, middleRowY2, rightColumnX2
					- centerColumnX2, topRowY2 - middleRowY2, c);
		if (middleRight != -1)
			set(middleRight, rightColumnX2, middleRowY2,
					(x + width - rightColumnX) * scaleX,
					topRowY2 - middleRowY2, c);
		if (topLeft != -1)
			set(topLeft, x, topRowY2, (centerColumnX - x) * scaleX,
					(y + height - topRowY) * scaleY, c);
		if (topCenter != -1)
			set(topCenter, centerColumnX2, topRowY2, rightColumnX2
					- centerColumnX2, (y + height - topRowY) * scaleY, c);
		if (topRight != -1)
			set(topRight, rightColumnX2, topRowY2, (x + width - rightColumnX)
					* scaleX, (y + height - topRowY) * scaleY, c);

		batch.draw(texture, vertices, 0, idx);
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

	/** Set the width of the middle column of the patch. At render time, this is implicitly the requested render-width of the entire
	 * nine patch, minus the left and right width. This value is only used for computing the {@link #getTotalWidth() default total
	 * width}. */
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
	public void setPadding (int left, int right, int top, int bottom) {
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

	/** See {@link #setPadding(int, int, int, int)} */
	public void setPadLeft (int left) {
		this.padLeft = left;
	}

	/** Returns the right padding if set, else returns {@link #getRightWidth()}. */
	public float getPadRight () {
		if (padRight == -1) return getRightWidth();
		return padRight;
	}

	/** See {@link #setPadding(int, int, int, int)} */
	public void setPadRight (int right) {
		this.padRight = right;
	}

	/** Returns the top padding if set, else returns {@link #getTopHeight()}. */
	public float getPadTop () {
		if (padTop == -1) return getTopHeight();
		return padTop;
	}

	/** See {@link #setPadding(int, int, int, int)} */
	public void setPadTop (int top) {
		this.padTop = top;
	}

	/** Returns the bottom padding if set, else returns {@link #getBottomHeight()}. */
	public float getPadBottom () {
		if (padBottom == -1) return getBottomHeight();
		return padBottom;
	}

	/** See {@link #setPadding(int, int, int, int)} */
	public void setPadBottom (int bottom) {
		this.padBottom = bottom;
	}

	public Texture getTexture () {
		return texture;
	}
	
	public void setScale(float xScale, float yScale) {
		scaleX = xScale;
		scaleY = yScale;
	}

	public void setScaleX(float xScale) {
		scaleX = xScale;
	}

	public void setScaleY(float yScale) {
		scaleY = yScale;
	}
}
