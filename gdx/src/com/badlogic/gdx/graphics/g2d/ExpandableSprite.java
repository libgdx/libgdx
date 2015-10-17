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
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.NumberUtils;

/** Behaves similarly to {@linkplain Sprite}, but it can be instantiated with extra vertex attributes beyond the standard ones
 * (position, color, and texture coordinates). The sprite can be drawn with a SpriteBatch that was instantiated with the same
 * array of VertexAttributes that were defined in the input {@linkplain Template}. See
 * {@link SpriteBatch#SpriteBatch(int, ShaderProgram, VertexAttribute...)}.
 * <p>
 * Attempting to draw the Sprite with a SpriteBatch whose extra attributes don't match will cause unexpected rendering and
 * possible Exceptions. */
public class ExpandableSprite extends TextureRegion {

	final protected VertexAttribute[] extraAttributes;
	final private int[] extraAttributeIndexMapping; // the first index of each attribute of the first vertex
	protected final AttributesMapping attributes;
	protected final int vertexSize;
	protected final int spriteSize;
	protected final float[] vertices;
	private final Color color = new Color(1, 1, 1, 1);
	private float x, y;
	float width, height;
	private float originX, originY;
	private float rotation;
	private float scaleX = 1, scaleY = 1;
	private boolean positionDirty = true;
	private Rectangle bounds;

	public static final class Template {
		final public VertexAttribute[] extraAttributes;
		final int[] extraAttributeIndexMapping;
		protected final AttributesMapping attributes;
		protected final int vertexSize;
		protected final int spriteSize;

		public Template (VertexAttribute[] extraAttributes) {
			this.extraAttributes = extraAttributes;
			int extrasSize = VertexAttribute.calculateSize(extraAttributes);
			attributes = AttributesMapping.get(extrasSize);
			vertexSize = Sprite.VERTEX_SIZE + extrasSize;
			spriteSize = vertexSize * 4;
			extraAttributeIndexMapping = new int[extraAttributes.length];
			int start = 0;
			for (int i = 0; i < extraAttributeIndexMapping.length; i++) {
				extraAttributeIndexMapping[i] = Sprite.VERTEX_SIZE + start;
				start += extraAttributes[i].usage == Usage.ColorPacked ? 1 : extraAttributes[i].numComponents;
			}
		}
	}

	/** Creates an uninitialized sprite. The sprite will need a texture region and bounds set before it can be drawn.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used to instantiate every sprite that uses the same extra VertexAttributes.
	 *           The VertexAttribute array should never be modified. */
	public ExpandableSprite (Template template) {
		extraAttributes = template.extraAttributes;
		attributes = template.attributes;
		vertexSize = template.vertexSize;
		spriteSize = template.spriteSize;
		vertices = new float[spriteSize];
		extraAttributeIndexMapping = template.extraAttributeIndexMapping;
		setColor(1, 1, 1, 1);
	}

	/** Creates a sprite with width, height, and texture region equal to the size of the texture.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used to instantiate every sprite that uses the same extra VertexAttributes.
	 *           The VertexAttribute array should never be modified. */
	public ExpandableSprite (Template template, Texture texture) {
		this(template, texture, 0, 0, texture.getWidth(), texture.getHeight());
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size. The texture region's upper left corner
	 * will be 0,0.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used to instantiate every sprite that uses the same extra VertexAttributes.
	 *           The VertexAttribute array should never be modified.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ExpandableSprite (Template template, Texture texture, int srcWidth, int srcHeight) {
		this(template, texture, 0, 0, srcWidth, srcHeight);
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used to instantiate every sprite that uses the same extra VertexAttributes.
	 *           The VertexAttribute array should never be modified.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ExpandableSprite (Template template, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
		this(template);
		if (texture == null) throw new IllegalArgumentException("texture cannot be null.");
		this.texture = texture;
		setRegion(srcX, srcY, srcWidth, srcHeight);
		setColor(1, 1, 1, 1);
		setSize(Math.abs(srcWidth), Math.abs(srcHeight));
		setOrigin(width / 2, height / 2);
	}

	// Note the region is copied.
	/** Sets the sprite to a specific TextureRegion, the new sprite's region is a copy of the parameter region - altering one does
	 * not affect the other.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used to instantiate every sprite that uses the same extra VertexAttributes.
	 *           The VertexAttribute array should never be modified. */
	public ExpandableSprite (Template template, TextureRegion region) {
		this(template);
		setRegion(region);
		setColor(1, 1, 1, 1);
		setSize(region.getRegionWidth(), region.getRegionHeight());
		setOrigin(width / 2, height / 2);
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size, relative to specified sprite's texture
	 * region.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used to instantiate every sprite that uses the same extra VertexAttributes.
	 *           The VertexAttribute array should never be modified.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ExpandableSprite (Template template, TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
		this(template);
		setRegion(region, srcX, srcY, srcWidth, srcHeight);
		setColor(1, 1, 1, 1);
		setSize(Math.abs(srcWidth), Math.abs(srcHeight));
		setOrigin(width / 2, height / 2);
	}

	public int getSpriteSize () {
		return spriteSize;
	}

	protected int getVertexSize () {
		return vertexSize;
	}

	/** Sets the position and size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale
	 * are changed, it is slightly more efficient to set the bounds after those operations. */
	public void setBounds (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		if (positionDirty) return;

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[attributes.x1] = x;
		vertices[attributes.y1] = y;

		vertices[attributes.x2] = x;
		vertices[attributes.y2] = y2;

		vertices[attributes.x3] = x2;
		vertices[attributes.y3] = y2;

		vertices[attributes.x4] = x2;
		vertices[attributes.y4] = y;

		if (rotation != 0 || scaleX != 1 || scaleY != 1) positionDirty = true;
	}

	/** Sets the size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale are changed,
	 * it is slightly more efficient to set the size after those operations. If both position and size are to be changed, it is
	 * better to use {@link #setBounds(float, float, float, float)}. */
	public void setSize (float width, float height) {
		this.width = width;
		this.height = height;

		if (positionDirty) return;

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[attributes.x1] = x;
		vertices[attributes.y1] = y;

		vertices[attributes.x2] = x;
		vertices[attributes.y2] = y2;

		vertices[attributes.x3] = x2;
		vertices[attributes.y3] = y2;

		vertices[attributes.x4] = x2;
		vertices[attributes.y4] = y;

		if (rotation != 0 || scaleX != 1 || scaleY != 1) positionDirty = true;
	}

	/** Sets the position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
	 * to set the position after those operations. If both position and size are to be changed, it is better to use
	 * {@link #setBounds(float, float, float, float)}. */
	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

	/** Sets the x position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
	 * to set the position after those operations. If both position and size are to be changed, it is better to use
	 * {@link #setBounds(float, float, float, float)}. */
	public void setX (float x) {
		translateX(x - this.x);
	}

	/** Sets the y position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
	 * to set the position after those operations. If both position and size are to be changed, it is better to use
	 * {@link #setBounds(float, float, float, float)}. */
	public void setY (float y) {
		translateY(y - this.y);
	}

	/** Sets the x position so that it is centered on the given x parameter */
	public void setCenterX (float x) {
		setX(x - width / 2);
	}

	/** Sets the y position so that it is centered on the given y parameter */
	public void setCenterY (float y) {
		setY(y - height / 2);
	}

	/** Sets the position so that the sprite is centered on (x, y) */
	public void setCenter (float x, float y) {
		setCenterX(x);
		setCenterY(y);
	}

	/** Sets the x position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translateX (float xAmount) {
		this.x += xAmount;

		if (positionDirty) return;

		float[] vertices = this.vertices;
		vertices[attributes.x1] += xAmount;
		vertices[attributes.x2] += xAmount;
		vertices[attributes.x3] += xAmount;
		vertices[attributes.x4] += xAmount;
	}

	/** Sets the y position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translateY (float yAmount) {
		y += yAmount;

		if (positionDirty) return;

		float[] vertices = this.vertices;
		vertices[attributes.y1] += yAmount;
		vertices[attributes.y2] += yAmount;
		vertices[attributes.y3] += yAmount;
		vertices[attributes.y4] += yAmount;
	}

	/** Sets the position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translate (float xAmount, float yAmount) {
		x += xAmount;
		y += yAmount;

		if (positionDirty) return;

		float[] vertices = this.vertices;
		vertices[attributes.x1] += xAmount;
		vertices[attributes.y1] += yAmount;

		vertices[attributes.x2] += xAmount;
		vertices[attributes.y2] += yAmount;

		vertices[attributes.x3] += xAmount;
		vertices[attributes.y3] += yAmount;

		vertices[attributes.x4] += xAmount;
		vertices[attributes.y4] += yAmount;
	}

	/** Sets the color used to tint this sprite. Default is {@link Color#WHITE}. */
	public void setColor (Color tint) {
		float color = tint.toFloatBits();
		float[] vertices = this.vertices;
		vertices[attributes.c1] = color;
		vertices[attributes.c2] = color;
		vertices[attributes.c3] = color;
		vertices[attributes.c4] = color;
	}

	/** Sets the alpha portion of the color used to tint this sprite. */
	public void setAlpha (float a) {
		int intBits = NumberUtils.floatToIntColor(vertices[attributes.c1]);
		int alphaBits = (int)(255 * a) << 24;

		// clear alpha on original color
		intBits = intBits & 0x00FFFFFF;
		// write new alpha
		intBits = intBits | alphaBits;
		float color = NumberUtils.intToFloatColor(intBits);
		vertices[attributes.c1] = color;
		vertices[attributes.c2] = color;
		vertices[attributes.c3] = color;
		vertices[attributes.c4] = color;
	}

	/** @see #setColor(Color) */
	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = NumberUtils.intToFloatColor(intBits);
		float[] vertices = this.vertices;
		vertices[attributes.c1] = color;
		vertices[attributes.c2] = color;
		vertices[attributes.c3] = color;
		vertices[attributes.c4] = color;
	}

	/** @see #setColor(Color)
	 * @see Color#toFloatBits() */
	public void setColor (float color) {
		float[] vertices = this.vertices;
		vertices[attributes.c1] = color;
		vertices[attributes.c2] = color;
		vertices[attributes.c3] = color;
		vertices[attributes.c4] = color;
	}

	/** Sets the origin in relation to the sprite's position for scaling and rotation. */
	public void setOrigin (float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
		positionDirty = true;
	}

	/** Place origin in the center of the sprite */
	public void setOriginCenter () {
		this.originX = width / 2;
		this.originY = height / 2;
		positionDirty = true;
	}

	/** Sets the rotation of the sprite in degrees. Rotation is centered on the origin set in {@link #setOrigin(float, float)} */
	public void setRotation (float degrees) {
		this.rotation = degrees;
		positionDirty = true;
	}

	/** @return the rotation of the sprite in degrees */
	public float getRotation () {
		return rotation;
	}

	/** Sets the sprite's rotation in degrees relative to the current rotation. Rotation is centered on the origin set in
	 * {@link #setOrigin(float, float)} */
	public void rotate (float degrees) {
		if (degrees == 0) return;
		rotation += degrees;
		positionDirty = true;
	}

	/** Rotates this sprite 90 degrees in-place by rotating the texture coordinates. This rotation is unaffected by
	 * {@link #setRotation(float)} and {@link #rotate(float)}. */
	public void rotate90 (boolean clockwise) {
		float[] vertices = this.vertices;

		if (clockwise) {
			float temp = vertices[attributes.v1];
			vertices[attributes.v1] = vertices[attributes.v4];
			vertices[attributes.v4] = vertices[attributes.v3];
			vertices[attributes.v3] = vertices[attributes.v2];
			vertices[attributes.v2] = temp;

			temp = vertices[attributes.u1];
			vertices[attributes.u1] = vertices[attributes.u4];
			vertices[attributes.u4] = vertices[attributes.u3];
			vertices[attributes.u3] = vertices[attributes.u2];
			vertices[attributes.u2] = temp;
		} else {
			float temp = vertices[attributes.v1];
			vertices[attributes.v1] = vertices[attributes.v2];
			vertices[attributes.v2] = vertices[attributes.v3];
			vertices[attributes.v3] = vertices[attributes.v4];
			vertices[attributes.v4] = temp;

			temp = vertices[attributes.u1];
			vertices[attributes.u1] = vertices[attributes.u2];
			vertices[attributes.u2] = vertices[attributes.u3];
			vertices[attributes.u3] = vertices[attributes.u4];
			vertices[attributes.u4] = temp;
		}
	}

	/** Sets the sprite's scale for both X and Y uniformly. The sprite scales out from the origin. This will not affect the values
	 * returned by {@link #getWidth()} and {@link #getHeight()} */
	public void setScale (float scaleXY) {
		this.scaleX = scaleXY;
		this.scaleY = scaleXY;
		positionDirty = true;
	}

	/** Sets the sprite's scale for both X and Y. The sprite scales out from the origin. This will not affect the values returned by
	 * {@link #getWidth()} and {@link #getHeight()} */
	public void setScale (float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		positionDirty = true;
	}

	/** Sets the sprite's scale relative to the current scale. for example: original scale 2 -> sprite.scale(4) -> final scale 6.
	 * The sprite scales out from the origin. This will not affect the values returned by {@link #getWidth()} and
	 * {@link #getHeight()} */
	public void scale (float amount) {
		this.scaleX += amount;
		this.scaleY += amount;
		positionDirty = true;
	}

	/** Returns the packed vertices, colors, and texture coordinates for this sprite. */
	public float[] getVertices () {
		if (positionDirty) {
			positionDirty = false;

			final AttributesMapping attributes = this.attributes;

			float[] vertices = this.vertices;
			float localX = -originX;
			float localY = -originY;
			float localX2 = localX + width;
			float localY2 = localY + height;
			float worldOriginX = this.x - localX;
			float worldOriginY = this.y - localY;
			if (scaleX != 1 || scaleY != 1) {
				localX *= scaleX;
				localY *= scaleY;
				localX2 *= scaleX;
				localY2 *= scaleY;
			}
			if (rotation != 0) {
				final float cos = MathUtils.cosDeg(rotation);
				final float sin = MathUtils.sinDeg(rotation);
				final float localXCos = localX * cos;
				final float localXSin = localX * sin;
				final float localYCos = localY * cos;
				final float localYSin = localY * sin;
				final float localX2Cos = localX2 * cos;
				final float localX2Sin = localX2 * sin;
				final float localY2Cos = localY2 * cos;
				final float localY2Sin = localY2 * sin;

				final float x1 = localXCos - localYSin + worldOriginX;
				final float y1 = localYCos + localXSin + worldOriginY;
				vertices[attributes.x1] = x1;
				vertices[attributes.y1] = y1;

				final float x2 = localXCos - localY2Sin + worldOriginX;
				final float y2 = localY2Cos + localXSin + worldOriginY;
				vertices[attributes.x2] = x2;
				vertices[attributes.y2] = y2;

				final float x3 = localX2Cos - localY2Sin + worldOriginX;
				final float y3 = localY2Cos + localX2Sin + worldOriginY;
				vertices[attributes.x3] = x3;
				vertices[attributes.y3] = y3;

				vertices[attributes.x4] = x1 + (x3 - x2);
				vertices[attributes.y4] = y3 - (y2 - y1);
			} else {
				final float x1 = localX + worldOriginX;
				final float y1 = localY + worldOriginY;
				final float x2 = localX2 + worldOriginX;
				final float y2 = localY2 + worldOriginY;

				vertices[attributes.x1] = x1;
				vertices[attributes.y1] = y1;

				vertices[attributes.x2] = x1;
				vertices[attributes.y2] = y2;

				vertices[attributes.x3] = x2;
				vertices[attributes.y3] = y2;

				vertices[attributes.x4] = x2;
				vertices[attributes.y4] = y1;
			}
		}
		return vertices;
	}

	/** Returns the bounding axis aligned {@link Rectangle} that bounds this sprite. The rectangles x and y coordinates describe its
	 * bottom left corner. If you change the position or size of the sprite, you have to fetch the triangle again for it to be
	 * recomputed.
	 * 
	 * @return the bounding Rectangle */
	public Rectangle getBoundingRectangle () {
		final float[] vertices = getVertices();

		final AttributesMapping attributes = this.attributes;

		float minx = vertices[attributes.x1];
		float miny = vertices[attributes.y1];
		float maxx = vertices[attributes.x1];
		float maxy = vertices[attributes.y1];

		minx = minx > vertices[attributes.x2] ? vertices[attributes.x2] : minx;
		minx = minx > vertices[attributes.x3] ? vertices[attributes.x3] : minx;
		minx = minx > vertices[attributes.x4] ? vertices[attributes.x4] : minx;

		maxx = maxx < vertices[attributes.x2] ? vertices[attributes.x2] : maxx;
		maxx = maxx < vertices[attributes.x3] ? vertices[attributes.x3] : maxx;
		maxx = maxx < vertices[attributes.x4] ? vertices[attributes.x4] : maxx;

		miny = miny > vertices[attributes.y2] ? vertices[attributes.y2] : miny;
		miny = miny > vertices[attributes.y3] ? vertices[attributes.y3] : miny;
		miny = miny > vertices[attributes.y4] ? vertices[attributes.y4] : miny;

		maxy = maxy < vertices[attributes.y2] ? vertices[attributes.y2] : maxy;
		maxy = maxy < vertices[attributes.y3] ? vertices[attributes.y3] : maxy;
		maxy = maxy < vertices[attributes.y4] ? vertices[attributes.y4] : maxy;

		if (bounds == null) bounds = new Rectangle();
		bounds.x = minx;
		bounds.y = miny;
		bounds.width = maxx - minx;
		bounds.height = maxy - miny;
		return bounds;
	}

	/** Draw the sprite with a SpriteBatch that was instantiated with the same array of VertexAttributes as used in this subclass of
	 * {@linkplain ExpandableSprite}. Attempting to draw the Sprite with a SpriteBatch whose extra attributes don't match will
	 * cause unexpected rendering and possible Exceptions. */
	public void draw (SpriteBatch batch) {
		batch.draw(texture, getVertices(), 0, spriteSize);
	}

	/** Draw the sprite with a SpriteBatch that was instantiated with the same array of VertexAttributes as used in this subclass of
	 * {@linkplain ExpandableSprite}. Attempting to draw the Sprite with a SpriteBatch whose extra attributes don't match will
	 * cause unexpected rendering and possible Exceptions. */
	public void draw (SpriteBatch batch, float alphaModulation) {
		float oldAlpha = getColor().a;
		setAlpha(oldAlpha * alphaModulation);
		draw(batch);
		setAlpha(oldAlpha);
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	/** @return the width of the sprite, not accounting for scale. */
	public float getWidth () {
		return width;
	}

	/** @return the height of the sprite, not accounting for scale. */
	public float getHeight () {
		return height;
	}

	/** The origin influences {@link #setPosition(float, float)}, {@link #setRotation(float)} and the expansion direction of scaling
	 * {@link #setScale(float, float)} */
	public float getOriginX () {
		return originX;
	}

	/** The origin influences {@link #setPosition(float, float)}, {@link #setRotation(float)} and the expansion direction of scaling
	 * {@link #setScale(float, float)} */
	public float getOriginY () {
		return originY;
	}

	/** X scale of the sprite, independent of size set by {@link #setSize(float, float)} */
	public float getScaleX () {
		return scaleX;
	}

	/** Y scale of the sprite, independent of size set by {@link #setSize(float, float)} */
	public float getScaleY () {
		return scaleY;
	}

	/** Returns the color of this sprite. Changing the returned color will have no affect, {@link #setColor(Color)} or
	 * {@link #setColor(float, float, float, float)} must be used. */
	public Color getColor () {
		int intBits = NumberUtils.floatToIntColor(vertices[attributes.c1]);
		Color color = this.color;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	public void setRegion (float u, float v, float u2, float v2) {
		super.setRegion(u, v, u2, v2);

		float[] vertices = ExpandableSprite.this.vertices;
		final AttributesMapping attributes = this.attributes;
		vertices[attributes.u1] = u;
		vertices[attributes.v1] = v2;

		vertices[attributes.u2] = u;
		vertices[attributes.v2] = v;

		vertices[attributes.u3] = u2;
		vertices[attributes.v3] = v;

		vertices[attributes.u4] = u2;
		vertices[attributes.v4] = v2;
	}

	public void setU (float u) {
		super.setU(u);
		vertices[attributes.u1] = u;
		vertices[attributes.u2] = u;
	}

	public void setV (float v) {
		super.setV(v);
		vertices[attributes.v2] = v;
		vertices[attributes.v3] = v;
	}

	public void setU2 (float u2) {
		super.setU2(u2);
		vertices[attributes.u3] = u2;
		vertices[attributes.u4] = u2;
	}

	public void setV2 (float v2) {
		super.setV2(v2);
		vertices[attributes.v1] = v2;
		vertices[attributes.v4] = v2;
	}

	/** Set the sprite's flip state regardless of current condition
	 * @param x the desired horizontal flip state
	 * @param y the desired vertical flip state */
	public void setFlip (boolean x, boolean y) {
		boolean performX = false;
		boolean performY = false;
		if (isFlipX() != x) {
			performX = true;
		}
		if (isFlipY() != y) {
			performY = true;
		}
		flip(performX, performY);
	}

	/** boolean parameters x,y are not setting a state, but performing a flip
	 * @param x perform horizontal flip
	 * @param y perform vertical flip */
	public void flip (boolean x, boolean y) {
		super.flip(x, y);
		float[] vertices = ExpandableSprite.this.vertices;
		final AttributesMapping attributes = this.attributes;
		if (x) {
			float temp = vertices[attributes.u1];
			vertices[attributes.u1] = vertices[attributes.u3];
			vertices[attributes.u3] = temp;
			temp = vertices[attributes.u2];
			vertices[attributes.u2] = vertices[attributes.u4];
			vertices[attributes.u4] = temp;
		}
		if (y) {
			float temp = vertices[attributes.v1];
			vertices[attributes.v1] = vertices[attributes.v3];
			vertices[attributes.v3] = temp;
			temp = vertices[attributes.v2];
			vertices[attributes.v2] = vertices[attributes.v4];
			vertices[attributes.v4] = temp;
		}
	}

	public void scroll (float xAmount, float yAmount) {
		float[] vertices = ExpandableSprite.this.vertices;
		final AttributesMapping attributes = this.attributes;
		if (xAmount != 0) {
			float u = (vertices[attributes.u1] + xAmount) % 1;
			float u2 = u + width / texture.getWidth();
			this.u = u;
			this.u2 = u2;
			vertices[attributes.u1] = u;
			vertices[attributes.u2] = u;
			vertices[attributes.u3] = u2;
			vertices[attributes.u4] = u2;
		}
		if (yAmount != 0) {
			float v = (vertices[attributes.v2] + yAmount) % 1;
			float v2 = v + height / texture.getHeight();
			this.v = v;
			this.v2 = v2;
			vertices[attributes.v1] = v2;
			vertices[attributes.v2] = v;
			vertices[attributes.v3] = v;
			vertices[attributes.v4] = v2;
		}
	}

	/** Set the value of a vertex attribute on all four vertices.
	 * @param value The value to apply.
	 * @param extraAttributeNumber The index of the extra VertexAttribute, as was defined in the {@linkplain Template} used to
	 *           instantiate this sprite.
	 * @param element The index of the element of the vertex attribute to be set. It is in the range of [0,
	 *           {@linkplain VertexAttribute#numComponents}), unless it is a
	 *           {@linkplain com.badlogic.gdx.graphics.VertexAttributes.Usage#ColorPacked ColorPacked}, in which case the element
	 *           should always be 0. */
	public void setExtraAttributeValue (float value, int extraAttributeNumber, int element) {
		int firstIndex = extraAttributeIndexMapping[extraAttributeNumber];
		vertices[firstIndex + element] = value;
		firstIndex += vertexSize;
		vertices[firstIndex + element] = value;
		firstIndex += vertexSize;
		vertices[firstIndex + element] = value;
		firstIndex += vertexSize;
		vertices[firstIndex + element] = value;
	}

	/** Set the value of a vertex attribute on a specific vertex.
	 * @param value The value to apply.
	 * @param extraAttributeNumber The index of the extra VertexAttribute, as was defined in the {@linkplain Template} used to
	 *           instantiate this sprite.
	 * @param element The index of the element of the vertex attribute to be set. It is in the range of [0,
	 *           {@linkplain VertexAttribute#numComponents}), unless it is a
	 *           {@linkplain com.badlogic.gdx.graphics.VertexAttributes.Usage#ColorPacked ColorPacked}, in which case the element
	 *           should always be 0.
	 * @param vertex The vertex number. One of 0 to 3 inclusive. */
	public void setExtraAttributeValue (float value, int extraAttributeNumber, int element, int vertex) {
		vertices[extraAttributeIndexMapping[extraAttributeNumber] + vertex * vertexSize + element] = value;
	}

	protected static class AttributesMapping {
		public final int x1, y1, c1, u1, v1, x2, y2, c2, u2, v2, x3, y3, c3, u3, v3, x4, y4, c4, u4, v4;
		/** The index of the first of the extra attributes for each of the four vertices of the sprite. */
		public final int ex1, ex2, ex3, ex4;

		private static final IntMap<AttributesMapping> STORE = new IntMap<AttributesMapping>();

		public static AttributesMapping get (int extraAttributesCount) {
			AttributesMapping mapping = STORE.get(extraAttributesCount);
			if (mapping != null) return mapping;
			mapping = new AttributesMapping(extraAttributesCount);
			STORE.put(extraAttributesCount, mapping);
			return mapping;
		}

		private AttributesMapping (int extraAttributesCount) {
			int idx = 0;
			x1 = idx++;
			y1 = idx++;
			c1 = idx++;
			u1 = idx++;
			v1 = idx++;
			ex1 = idx;
			idx += extraAttributesCount;
			x2 = idx++;
			y2 = idx++;
			c2 = idx++;
			u2 = idx++;
			v2 = idx++;
			ex2 = idx;
			idx += extraAttributesCount;
			x3 = idx++;
			y3 = idx++;
			c3 = idx++;
			u3 = idx++;
			v3 = idx++;
			ex3 = idx;
			idx += extraAttributesCount;
			x4 = idx++;
			y4 = idx++;
			c4 = idx++;
			u4 = idx++;
			v4 = idx++;
			ex4 = idx;
		}
	}
}
