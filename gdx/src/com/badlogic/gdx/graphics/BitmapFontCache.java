
package com.badlogic.gdx.graphics;

public class BitmapFontCache {
	private final Texture texture;
	private float[] vertices;
	private final float invTexWidth, invTexHeight;
	private int idx;
	int width, height;
	private float x, y;
	private float color;

	public BitmapFontCache (Texture texture, int glyphCount) {
		this.texture = texture;
		vertices = new float[glyphCount * 20];
		invTexWidth = 1.0f / texture.getWidth();
		invTexHeight = 1.0f / texture.getHeight();
	}

	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

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

	public void setColor (Color tint) {
		final float color = tint.toFloatBits();
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	public void addGlyph (float x, float y, int srcX, int srcY, int srcWidth, int srcHeight, Color tint) {
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

	public void draw (SpriteBatch spriteBatch) {
		spriteBatch.draw(texture, vertices, 0, idx);
	}

	public void reset (int glyphCount) {
		x = 0;
		y = 0;
		idx = 0;

		int vertexCount = glyphCount * 20;
		if (vertices.length < vertexCount) vertices = new float[vertexCount];
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}
}
