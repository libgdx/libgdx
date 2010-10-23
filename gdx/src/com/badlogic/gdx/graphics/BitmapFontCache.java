
package com.badlogic.gdx.graphics;

/**
 * <p>A BitmapFontCache caches glyph geometry produced by a call to one of the
 * {@link BitmapFontCache#cacheText()} methods. It provides a fast way to
 * render static text.</p> 
 * 
 * <p>The code is heavily based on Matthias Mann's TWL BitmapFont class. Thanks for sharing
 * Matthias :)</p>
 * 
 * @author nathan.sweet
 *
 */
public class BitmapFontCache {
	private final Texture texture;
	private float[] vertices;
	private final float invTexWidth, invTexHeight;
	private int idx;
	int width, height;
	private float x, y;
	private float color;

	BitmapFontCache (Texture texture) {
		this.texture = texture;
		invTexWidth = 1.0f / texture.getWidth();
		invTexHeight = 1.0f / texture.getHeight();
	}

	/**
	 * Sets the position of the text
	 * @param x the x coordinate
	 * @param y the y coodinate
	 */
	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

	/**
	 * Translates the text
	 * @param xAmount the amount in x to move the text 
	 * @param yAmount the amount in y to move the text
	 */
	public void translate (float xAmount, float yAmount) {
		if (xAmount == 0 && yAmount == 0) return;
		x += xAmount;
		y += yAmount;
		float[] vertices = this.vertices;
		for (int i = 0, n = idx; i < n; i += 5) {
			vertices[i] += xAmount;
			vertices[i + 1] += yAmount;
		}
	}

	/**
	 * Sets the tint color of the text.
	 * @param tint the {@link Color}
	 */
	public void setColor (Color tint) {
		final float color = tint.toFloatBits();
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	void addGlyph (float x, float y, int srcX, int srcY, int srcWidth, int srcHeight, Color tint) {
		final float x2 = x + srcWidth;
		final float y2 = y + srcHeight;
		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;
		final float color = tint.toFloatBits();

		float[] vertices = this.vertices;
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/**
	 * Draws the contents of the given cache via a {@link SpriteBatch}. Must
	 * be called between a {@link SpriteBatch#begin()}/{@link SpriteBatch#end()} pair.
	 * @param spriteBatch the SpriteBatch.
	 */
	public void draw (SpriteBatch spriteBatch) {
		spriteBatch.draw(texture, vertices, 0, idx);
	}

	void reset (int glyphCount) {
		x = 0;
		y = 0;
		idx = 0;

		int vertexCount = glyphCount * 20;
		if ( vertices == null || vertices.length < vertexCount) vertices = new float[vertexCount];
	}

	/**
	 * @return the width of the contained text
	 */
	public int getWidth () {
		return width;
	}

	/**
	 * @return the height of the contained text
	 */
	public int getHeight () {
		return height;
	}

	/**
	 * @return the x coordinate of the contained text
	 */
	public float getX () {
		return x;
	}

	/**
	 * @return the y coordinate of the contained text
	 */
	public float getY () {
		return y;
	}
}
