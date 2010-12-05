/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.utils.MathUtils;

/**
 * Holds the geometry, color, and texture information for drawing 2D sprites using {@link SpriteBatch}. A Sprite has a position
 * and a size given as width and height. The position is relative to the origin of the coordinate system specified via
 * {@link SpriteBatch#begin()} and the respective matrices. A Sprite is always rectangular and its position (x, y) are located in
 * the bottom left corner of that rectangle. A Sprite also has an origin around which rotations and scaling are performed. The
 * origin is given relative to the bottom left corner of the Sprite, its position. Texture information is given as pixels and is
 * always relative to texture space.
 * @author mzechner
 * @author Nathan Sweet <misc@n4te.com>
 */
public class Sprite {
	static final int VERTEX_SIZE = 2 + 1 + 2;
	static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

	private final SpriteTextureRegion region = new SpriteTextureRegion();
	final float[] vertices = new float[20];
	private final Color color = new Color(1, 1, 1, 1);
	private float x, y;
	float width, height;
	private float originX, originY;
	private float rotation;
	private float scaleX = 1, scaleY = 1;
	private boolean dirty = true;

	/**
	 * Creates an uninitialized sprite. The sprite will need a texture, texture region, bounds, and color set before it can be
	 * drawn.
	 */
	public Sprite () {
	}

	/**
	 * Creates a sprite with width, height, and texture region equal to the size of the texture.
	 */
	public Sprite (Texture texture) {
		this(texture, 0, 0, texture.getWidth(), texture.getHeight());
	}

	/**
	 * Creates a sprite with width, height, and texture region equal to the specified size. The texture region's upper left corner
	 * will be 0,0. * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
	 */
	public Sprite (Texture texture, int srcWidth, int srcHeight) {
		this(texture, 0, 0, srcWidth, srcHeight);
	}

	/**
	 * Creates a sprite with width, height, and texture region equal to the specified size. * @param srcWidth The width of the
	 * texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
	 */
	public Sprite (Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (texture == null) throw new IllegalArgumentException("texture cannot be null.");
		region.texture = texture;
		region.set(srcX, srcY, srcWidth, srcHeight);
		setColor(1, 1, 1, 1);
		setSize(Math.abs(srcWidth), Math.abs(srcHeight));
		setOrigin(width / 2, height / 2);
	}

	// Note the region is copied.
	public Sprite (TextureRegion region) {
		this.region.set(region);
		setColor(1, 1, 1, 1);
		setSize(Math.abs(region.getWidth()), Math.abs(region.getHeight()));
		setOrigin(width / 2, height / 2);
	}

	/**
	 * Creates a sprite with width, height, and texture region equal to the specified size, relative to specified sprite's texture
	 * region. * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
	 */
	public Sprite (TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
		this.region.texture = region.texture;
		this.region.set(region, srcX, srcY, srcWidth, srcHeight);
		setColor(1, 1, 1, 1);
		setSize(Math.abs(region.getWidth()), Math.abs(region.getHeight()));
		setOrigin(width / 2, height / 2);
	}

	/**
	 * Creates a sprite that is a copy in every way of the specified sprite.
	 */
	public Sprite (Sprite sprite) {
		set(sprite);
	}

	public void set (Sprite sprite) {
		if (sprite == null) throw new IllegalArgumentException("sprite cannot be null.");
		System.arraycopy(sprite.vertices, 0, vertices, 0, SPRITE_SIZE);
		region.texture = sprite.region.texture;
		x = sprite.x;
		y = sprite.y;
		width = sprite.width;
		height = sprite.height;
		originX = sprite.originX;
		originY = sprite.originY;
		rotation = sprite.rotation;
		scaleX = sprite.scaleX;
		scaleY = sprite.scaleY;
		color.set(sprite.color);
		dirty = sprite.dirty;
	}

	/**
	 * Sets the position and size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale
	 * are changed, it is slightly more efficient to set the bounds after those operations.
	 */
	public void setBounds (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		if (dirty) return;

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;

		if (rotation != 0 || scaleX != 1 || scaleY != 1) dirty = true;
	}

	/**
	 * Sets the size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale are changed,
	 * it is slightly more efficient to set the size after those operations. If both position and size are to be changed, it is
	 * better to use {@link #setBounds(float, float, float, float)}.
	 */
	public void setSize (float width, float height) {
		this.width = width;
		this.height = height;

		if (dirty) return;

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;

		if (rotation != 0 || scaleX != 1 || scaleY != 1) dirty = true;
	}

	/**
	 * Sets the position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
	 * to set the position after those operations. If both position and size are to be changed, it is better to use
	 * {@link #setBounds(float, float, float, float)}.
	 */
	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

	/**
	 * Sets the position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations.
	 */
	public void translate (float xAmount, float yAmount) {
		x += xAmount;
		y += yAmount;

		if (dirty) return;

		float[] vertices = this.vertices;
		vertices[X1] += xAmount;
		vertices[Y1] += yAmount;

		vertices[X2] += xAmount;
		vertices[Y2] += yAmount;

		vertices[X3] += xAmount;
		vertices[Y3] += yAmount;

		vertices[X4] += xAmount;
		vertices[Y4] += yAmount;
	}

	public void setColor (Color tint) {
		float color = tint.toFloatBits();
		float[] vertices = this.vertices;
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = Float.intBitsToFloat(intBits);
		float[] vertices = this.vertices;
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	/**
	 * Sets the origin in relation to the sprite's position for scaling and rotation.
	 */
	public void setOrigin (float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
		dirty = true;
	}

	public void setRotation (float degrees) {
		this.rotation = degrees;
		dirty = true;
	}

	/**
	 * Sets the sprite's rotation relative to the current rotation.
	 */
	public void rotate (float degrees) {
		rotation += degrees;
		dirty = true;
	}

	/**
	 * Rotates this sprite 90 degrees in-place by rotating the texture coordinates. This rotation is unaffected by
	 * {@link #setRotation(float)} and {@link #rotate(float)}.
	 */
	public void rotate90 (boolean clockwise) {
		float[] vertices = this.vertices;

		if (clockwise) {
			float temp = vertices[V1];
			vertices[V1] = vertices[V4];
			vertices[V4] = vertices[V3];
			vertices[V3] = vertices[V2];
			vertices[V2] = temp;

			temp = vertices[U1];
			vertices[U1] = vertices[U4];
			vertices[U4] = vertices[U3];
			vertices[U3] = vertices[U2];
			vertices[U2] = temp;
		} else {
			float temp = vertices[V1];
			vertices[V1] = vertices[V2];
			vertices[V2] = vertices[V3];
			vertices[V3] = vertices[V4];
			vertices[V4] = temp;

			temp = vertices[U1];
			vertices[U1] = vertices[U2];
			vertices[U2] = vertices[U3];
			vertices[U3] = vertices[U4];
			vertices[U4] = temp;
		}
	}

	protected void flip (boolean x, boolean y) {
		float[] vertices = Sprite.this.vertices;
		if (x) {
			float u = vertices[U1];
			float u2 = vertices[U3];
			vertices[U1] = u2;
			vertices[U2] = u2;
			vertices[U3] = u;
			vertices[U4] = u;
		}
		if (y) {
			float v = vertices[V2];
			float v2 = vertices[V1];
			vertices[V1] = v;
			vertices[V2] = v2;
			vertices[V3] = v2;
			vertices[V4] = v;
		}
	}

	public void setScale (float scaleXY) {
		this.scaleX = scaleXY;
		this.scaleY = scaleXY;
		dirty = true;
	}

	public void setScale (float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		dirty = true;
	}

	/**
	 * Sets the sprite's scale relative to the current scale.
	 */
	public void scale (float amount) {
		this.scaleX += amount;
		this.scaleY += amount;
		dirty = true;
	}

	/**
	 * Returns the packed vertices, colors, and texture coordinates for this sprite.
	 */
	public float[] getVertices () {
		if (dirty) {
			dirty = false;

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
				final float localY2Cos = localX2 * cos;
				final float localY2Sin = localY2 * sin;

				final float x1 = localXCos - localYSin + worldOriginX;
				final float y1 = localYCos + localXSin + worldOriginY;
				vertices[X1] = x1;
				vertices[Y1] = y1;

				final float x2 = localXCos - localY2Sin + worldOriginX;
				final float y2 = localY2Cos + localXSin + worldOriginY;
				vertices[X2] = x2;
				vertices[Y2] = y2;

				final float x3 = localX2Cos - localY2Sin + worldOriginX;
				final float y3 = localY2Cos + localX2Sin + worldOriginY;
				vertices[X3] = x3;
				vertices[Y3] = y3;

				vertices[X4] = x1 + (x3 - x2);
				vertices[Y4] = y3 - (y2 - y1);
			} else {
				final float x1 = localX + worldOriginX;
				final float y1 = localY + worldOriginY;
				final float x2 = localX2 + worldOriginX;
				final float y2 = localY2 + worldOriginY;

				vertices[X1] = x1;
				vertices[Y1] = y1;

				vertices[X2] = x1;
				vertices[Y2] = y2;

				vertices[X3] = x2;
				vertices[Y3] = y2;

				vertices[X4] = x2;
				vertices[Y4] = y1;
			}
		}
		return vertices;
	}

	public void draw (SpriteBatch spriteBatch) {
		spriteBatch.draw(region.texture, getVertices(), 0, SPRITE_SIZE);
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	public float getWidth () {
		return width;
	}

	public float getHeight () {
		return height;
	}

	public float getOriginX () {
		return originX;
	}

	public float getOriginY () {
		return originY;
	}

	public float getRotation () {
		return rotation;
	}

	public float getScaleX () {
		return scaleX;
	}

	public float getScaleY () {
		return scaleY;
	}

	public TextureRegion getRegion () {
		return region;
	}

	public Texture getTexture () {
		return region.texture;
	}

	/**
	 * Returns the color of this sprite. Changing the returned color will have no affect, {@link #setColor(Color)} or
	 * {@link #setColor(float, float, float, float)} must be used.
	 */
	public Color getColor () {
		float floatBits = vertices[C1];
		int intBits = Float.floatToRawIntBits(vertices[C1]);
		Color color = this.color;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	class SpriteTextureRegion extends TextureRegion {
		public void set (float u, float v, float u2, float v2) {
			float[] vertices = Sprite.this.vertices;
			vertices[U1] = u;
			vertices[V1] = v2;

			vertices[U2] = u;
			vertices[V2] = v;

			vertices[U3] = u2;
			vertices[V3] = v;

			vertices[U4] = u2;
			vertices[V4] = v2;
		}

		public void setU (float u) {
			vertices[U1] = u;
			vertices[U2] = u;
		}

		public void setV (float v) {
			vertices[V2] = v;
			vertices[V3] = v;
		}

		public void setU2 (float u2) {
			vertices[U3] = u2;
			vertices[U4] = u2;
		}

		public void setV2 (float v2) {
			vertices[V1] = v2;
			vertices[V4] = v2;
		}

		public float getU () {
			return vertices[U1];
		}

		public float getV () {
			return vertices[V2];
		}

		public float getU2 () {
			return vertices[U3];
		}

		public float getV2 () {
			return vertices[V1];
		}

		public void flip (boolean x, boolean y) {
			Sprite.this.flip(x, y);
		}

		public void scroll (float xAmount, float yAmount) {
			float[] vertices = Sprite.this.vertices;
			if (xAmount != 0) {
				float u = (vertices[U1] + xAmount) % 1;
				float u2 = u + width / texture.getWidth();
				vertices[U1] = u;
				vertices[U2] = u;
				vertices[U3] = u2;
				vertices[U4] = u2;
			}
			if (yAmount != 0) {
				float v = (vertices[V2] + yAmount) % 1;
				float v2 = v + height / texture.getHeight();
				vertices[V1] = v2;
				vertices[V2] = v;
				vertices[V3] = v;
				vertices[V4] = v2;
			}
		}
	}

	static private final int X1 = 0;
	static private final int Y1 = 1;
	static private final int C1 = 2;
	static private final int U1 = 3;
	static private final int V1 = 4;
	static private final int X2 = 5;
	static private final int Y2 = 6;
	static private final int C2 = 7;
	static private final int U2 = 8;
	static private final int V2 = 9;
	static private final int X3 = 10;
	static private final int Y3 = 11;
	static private final int C3 = 12;
	static private final int U3 = 13;
	static private final int V3 = 14;
	static private final int X4 = 15;
	static private final int Y4 = 16;
	static private final int C4 = 17;
	static private final int U4 = 18;
	static private final int V4 = 19;
}
