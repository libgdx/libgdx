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

/**
 * A TextureRegion defines a rectangular area in a texture given in pixels. The coordinate system used has its origin in the upper
 * left corner with the x-axis pointing to the left and the y axis pointing downwards.
 * @author mzechner
 */
public class TextureRegion {
	Texture texture;
	float u, v;
	float u2, v2;

	public TextureRegion () {
	}

	public TextureRegion (Texture texture) {
		this.texture = texture;
		set(0, 0, texture.getWidth(), texture.getHeight());
	}

	public TextureRegion (Texture texture, int x, int y, int width, int height) {
		this.texture = texture;
		set(x, y, width, height);
	}

	public TextureRegion (Texture texture, float u, float v, float u2, float v2) {
		this.texture = texture;
		set(u, v, u2, v2);
	}

	public TextureRegion (TextureRegion region) {
		set(region);
	}

	public TextureRegion (TextureRegion region, int x, int y, int width, int height) {
		set(region, x, y, width, height);
	}

	public void set (Texture texture) {
		this.texture = texture;
		set(0, 0, texture.getWidth(), texture.getHeight());
	}

	/**
	 * Sets the texture coordinates in pixels to apply to the sprite. This resets calling {@link #flip(boolean, boolean)}.
	 * 
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
	 */
	public void set (int x, int y, int width, int height) {
		float invTexWidth = 1f / texture.getWidth();
		float invTexHeight = 1f / texture.getHeight();
		set(x * invTexWidth, y * invTexHeight, (x + width) * invTexWidth, (y + height) * invTexHeight);
	}

	public void set (float u, float v, float u2, float v2) {
		this.u = u;
		this.v = v;
		this.u2 = u2;
		this.v2 = v2;
	}

	public void set (TextureRegion region) {
		texture = region.texture;
		set(region.u, region.v, region.u2, region.v2);
	}

	public void set (TextureRegion region, int x, int y, int width, int height) {
		texture = region.texture;
		set(region.getX() + x, region.getY() + y, width, height);
	}

	public Texture getTexture () {
		return texture;
	}

	public void setTexture (Texture texture) {
		this.texture = texture;
	}

	public float getU () {
		return u;
	}

	public void setU (float u) {
		this.u = u;
	}

	public float getV () {
		return v;
	}

	public void setV (float v) {
		this.v = v;
	}

	public float getU2 () {
		return u2;
	}

	public void setU2 (float u2) {
		this.u2 = u2;
	}

	public float getV2 () {
		return v2;
	}

	public void setV2 (float v2) {
		this.v2 = v2;
	}

	public int getX () {
		return (int)(getU() * texture.getWidth());
	}

	public void setX (int x) {
		setU(x / (float)texture.getWidth());
	}

	public int getY () {
		return (int)(getV() * texture.getHeight());
	}

	public void setY (int y) {
		setV(y / texture.getHeight());
	}

	/**
	 * Returns the region's width in pixels. May be negative if the texture region is flipped horizontally.
	 */
	public int getWidth () {
		return (int)((getU2() - getU()) * texture.getWidth());
	}

	public void setWidth (int width) {
		setU2(getU() + width / (float)texture.getWidth());
	}

	/**
	 * Returns the region's height in pixels. May be negative if the texture region is flipped horizontally.
	 */
	public int getHeight () {
		return (int)((getV2() - getV()) * texture.getHeight());
	}

	public void setHeight (int height) {
		setV2(getV() + height / (float)texture.getHeight());
	}

	public void flip (boolean x, boolean y) {
		if (x) {
			float temp = u;
			u = u2;
			u2 = temp;
		}
		if (y) {
			float temp = v;
			v = v2;
			v2 = temp;
		}
	}

	/**
	 * Offsets the texture region relative to the current texture region.
	 * 
	 * @param xAmount The percentage to offset horizontally.
	 * @param yAmount The percentage to offset vertically.
	 */
	public void scroll (float xAmount, float yAmount) {
		if (xAmount != 0) {
			float width = (u2 - u) * texture.getWidth();
			u = (u + xAmount) % 1;
			u2 = u + width / texture.getWidth();
		}
		if (yAmount != 0) {
			float height = (v2 - v) * texture.getHeight();
			v = (v + yAmount) % 1;
			v2 = v + height / texture.getHeight();
		}
	}
}
