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
package com.badlogic.gdx.scenes.scene2d.actors;

import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Image extends Actor {
	public final TextureRegion region;

	public Image (String name) {
		super(name);
		this.region = new TextureRegion();
	}

	public Image (String name, Texture texture) {
		super(name);
		this.originX = texture.getWidth() / 2.0f;
		this.originY = texture.getHeight() / 2.0f;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		this.region = new TextureRegion(texture);
	}

	public Image (String name, TextureRegion region) {
		super(name);
		width = region.getWidth();
		height = region.getHeight();
		originX = width / 2.0f;
		originY = height / 2.0f;
		this.region = new TextureRegion(region);
	}

	@Override protected void render (SpriteBatch batch) {
		if (region.getTexture() != null) {
			if (scaleX == 0 && scaleY == 0 && rotation == 0)
				batch.draw(region, x, y, width, height, color);
			else
				batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, color);
		}
	}

	@Override protected boolean touchDown (float x, float y, int pointer) {
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override protected boolean touchUp (float x, float y, int pointer) {
		return false;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
		return false;
	}

	public Actor hit (float x, float y) {
		if (x > 0 && x < width) if (y > 0 && y < height) return this;

		return null;
	}
}
