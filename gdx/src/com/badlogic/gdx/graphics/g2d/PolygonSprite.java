
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.NumberUtils;

public class PolygonSprite {

	PolygonRegion region;
	private float x, y;
	private float width, height;
	private float scaleX = 1f, scaleY = 1f;
	private float rotation;
	private float originX, originY;
	private float[] vertices;
	private boolean dirty;
	private Rectangle bounds = new Rectangle();

	private final Color color = new Color(1f, 1f, 1f, 1f);

	// Note the region is copied.
	public PolygonSprite (PolygonRegion region) {
		setRegion(region);
		setColor(1, 1, 1, 1);
		setSize(region.getRegion().getRegionWidth(), region.getRegion().getRegionHeight());
		setOrigin(width / 2, height / 2);
	}

	/** Creates a sprite that is a copy in every way of the specified sprite. */
	public PolygonSprite (PolygonSprite sprite) {
		set(sprite);
	}

	public void set (PolygonSprite sprite) {
		if (sprite == null) throw new IllegalArgumentException("sprite cannot be null.");

		setRegion(sprite.region);

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

	/** Sets the position and size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale
	 * are changed, it is slightly more efficient to set the bounds after those operations. */
	public void setBounds (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		dirty = true;
	}

	/** Sets the size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale are changed,
	 * it is slightly more efficient to set the size after those operations. If both position and size are to be changed, it is
	 * better to use {@link #setBounds(float, float, float, float)}. */
	public void setSize (float width, float height) {
		this.width = width;
		this.height = height;

		dirty = true;
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

	/** Sets the x position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translateX (float xAmount) {
		this.x += xAmount;

		if (dirty) return;

		final float[] vertices = this.vertices;
		for (int i = 0; i < vertices.length; i += Sprite.VERTEX_SIZE) {
			vertices[i] += xAmount;
		}
	}

	/** Sets the y position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translateY (float yAmount) {
		y += yAmount;

		if (dirty) return;

		final float[] vertices = this.vertices;
		for (int i = 0; i < vertices.length; i += Sprite.VERTEX_SIZE) {
			vertices[i + 1] += yAmount;
		}
	}

	/** Sets the position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translate (float xAmount, float yAmount) {
		x += xAmount;
		y += yAmount;

		if (dirty) return;

		final float[] vertices = this.vertices;
		for (int i = 0; i < vertices.length; i += Sprite.VERTEX_SIZE) {
			vertices[i] += xAmount;
			vertices[i + 1] += yAmount;
		}
	}

	public void setColor (Color tint) {
		float color = tint.toFloatBits();

		final float[] vertices = this.vertices;
		for (int i = 0; i < vertices.length; i += Sprite.VERTEX_SIZE) {
			vertices[i + 2] = color;
		}
	}

	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = NumberUtils.intToFloatColor(intBits);
		final float[] vertices = this.vertices;
		for (int i = 0; i < vertices.length; i += Sprite.VERTEX_SIZE) {
			vertices[i + 2] = color;
		}
	}

	/** Sets the origin in relation to the sprite's position for scaling and rotation. */
	public void setOrigin (float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
		dirty = true;
	}

	public void setRotation (float degrees) {
		this.rotation = degrees;
		dirty = true;
	}

	/** Sets the sprite's rotation relative to the current rotation. */
	public void rotate (float degrees) {
		rotation += degrees;
		dirty = true;
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

	/** Sets the sprite's scale relative to the current scale. */
	public void scale (float amount) {
		this.scaleX += amount;
		this.scaleY += amount;
		dirty = true;
	}

	/** Returns the packed vertices, colors, and texture coordinates for this sprite. */
	public float[] getVertices () {
		if (dirty) {
			dirty = false;

			final float worldOriginX = x + originX;
			final float worldOriginY = y + originY;
			float sX = width / region.getRegion().getRegionWidth();
			float sY = height / region.getRegion().getRegionHeight();
			float fx, rx;
			float fy, ry;

			float[] localVertices = region.getLocalVertices();

			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			for (int i = 0; i < localVertices.length; i += 2) {
				fx = localVertices[i] * sX;
				fy = localVertices[i + 1] * sY;

				fx -= originX;
				fy -= originY;

				if (scaleX != 1.0f || scaleY != 1.0) {
					fx *= scaleX;
					fy *= scaleY;
				}

				rx = cos * fx - sin * fy;
				ry = sin * fx + cos * fy;

				rx += worldOriginX;
				ry += worldOriginY;

				vertices[(i / 2) * 5] = rx;
				vertices[((i / 2) * 5) + 1] = ry;
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

		float minx = vertices[0];
		float miny = vertices[1];
		float maxx = vertices[0];
		float maxy = vertices[1];

		for (int i = 0; i < vertices.length; i += 5) {
			minx = minx > vertices[i] ? vertices[i] : minx;
			maxx = maxx < vertices[i] ? vertices[i] : maxx;
			miny = miny > vertices[i + 1] ? vertices[i + 1] : miny;
			maxy = maxy < vertices[i + 1] ? vertices[i + 1] : maxy;
		}

		bounds.x = minx;
		bounds.y = miny;
		bounds.width = maxx - minx;
		bounds.height = maxy - miny;

		return bounds;
	}

	public void draw (PolygonSpriteBatch spriteBatch) {
		spriteBatch.draw(region, getVertices(), 0, vertices.length);
	}

	public void draw (PolygonSpriteBatch spriteBatch, float alphaModulation) {
		Color color = getColor();
		float oldAlpha = color.a;
		color.a *= alphaModulation;
		setColor(color);
		draw(spriteBatch);
		color.a = oldAlpha;
		setColor(color);
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

	/** Returns the color of this sprite. Changing the returned color will have no affect, {@link #setColor(Color)} or
	 * {@link #setColor(float, float, float, float)} must be used. */
	public Color getColor () {
		int intBits = NumberUtils.floatToIntColor(vertices[2]);
		Color color = this.color;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	public void setRegion (PolygonRegion region) {
		this.region = region;

		float[] localVertices = region.getLocalVertices();
		float[] localTextureCoords = region.getTextureCoords();

		if (vertices == null || localVertices.length != vertices.length) vertices = new float[(localVertices.length / 2) * 5];

		// Pack the region info into this sprite's vertices
		for (int i = 0; i < localVertices.length / 2; i++) {
			vertices[(i * 5)] = localVertices[(i * 2)];
			vertices[(i * 5) + 1] = localVertices[(i * 2) + 1];
			vertices[(i * 5) + 2] = color.toFloatBits();
			vertices[(i * 5) + 3] = localTextureCoords[(i * 2)];
			vertices[(i * 5) + 4] = localTextureCoords[(i * 2) + 1];
		}
	}
}
