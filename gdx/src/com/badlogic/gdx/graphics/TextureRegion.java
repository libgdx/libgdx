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
 * A TextureRegion defines a rectangular area in a texture given in pixels. The
 * coordinate system used has its origin in the upper left corner with the x-axis
 * pointing to the left and the y axis pointing downwards.
 * 
 * @author mzechner
 * 
 */
public class TextureRegion {
	public int x, y;
	public int width, height;
	public Texture texture;

	public TextureRegion (Texture texture, int x, int y, int width, int height) {
		set(texture, x, y, width, height);
	}

	public void set (Texture texture, int x, int y, int width, int height) {
		this.texture = texture;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
