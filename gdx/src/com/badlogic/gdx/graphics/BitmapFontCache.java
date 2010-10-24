/*
 * Copyright (c) 2008-2010, Matthias Mann
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of Matthias Mann nor
 * the names of its contributors may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.graphics.BitmapFont.Glyph;

/**
 * A BitmapFontCache caches glyph geometry produced by a call to one of the
 * {@link BitmapFont#cacheText(BitmapFontCache, CharSequence, int, int, Color)} methods. It caches the glyph geometry, providing a
 * fast way to render static text. <br>
 * <br>
 * The code is heavily based on Matthias Mann's TWL BitmapFont class. Thanks for sharing, Matthias! :)
 * @author Nathan Sweet <misc@n4te.com>
 * @author Matthias Mann
 */
public class BitmapFontCache {
	private final Texture texture;
	private float[] vertices;
	private int idx;
	int width, height;
	private float x, y;
	private float color;

	BitmapFontCache (Texture texture) {
		this.texture = texture;
	}

	/**
	 * Sets the position of the text, relative to the position when the cached text was created.
	 * @param x The x coordinate
	 * @param y The y coodinate
	 */
	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

	/**
	 * Sets the position of the text, relative to its current position.
	 * @param xAmount The amount in x to move the text
	 * @param yAmount The amount in y to move the text
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
	 * @param tint The {@link Color}
	 */
	public void setColor (Color tint) {
		final float color = tint.toFloatBits();
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	/**
	 * Sets the tint color of the text.
	 */
	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = Float.intBitsToFloat(intBits);
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	void addGlyph (Glyph glyph, float x, float y, float color) {
		x += glyph.xoffset;
		y += glyph.yoffset;
		final float x2 = x + glyph.width;
		final float y2 = y + glyph.height;
		final float u = glyph.u;
		final float u2 = glyph.u2;
		final float v = glyph.v;
		final float v2 = glyph.v2;

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
	 * Draws the contents of the cache via a {@link SpriteBatch}. Must be called between a {@link SpriteBatch#begin()}/
	 * {@link SpriteBatch#end()} pair.
	 * @param spriteBatch The SpriteBatch
	 */
	public void draw (SpriteBatch spriteBatch) {
		spriteBatch.draw(texture, vertices, 0, idx);
	}

	void reset (int glyphCount) {
		x = 0;
		y = 0;
		idx = 0;

		int vertexCount = glyphCount * 20;
		if (vertices == null || vertices.length < vertexCount) vertices = new float[vertexCount];
	}

	/**
	 * @return The width of the contained text
	 */
	public int getWidth () {
		return width;
	}

	/**
	 * @return The height of the contained text
	 */
	public int getHeight () {
		return height;
	}

	/**
	 * @return The x coordinate of the contained text, relative to the position when the cached text was created
	 */
	public float getX () {
		return x;
	}

	/**
	 * @return The y coordinate of the contained text, relative to the position when the cached text was created
	 */
	public float getY () {
		return y;
	}
}
