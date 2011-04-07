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

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Label extends Actor {
	public BitmapFontCache cache;

	public Label (String name, BitmapFont font, String text) {
		super(name);
		cache = new BitmapFontCache(font);
		setText(text);
	}

	public void setText (String text) {
		cache.setText(text, 0, 0);
		TextBounds bounds = cache.getBounds();
		width = bounds.width;
		height = bounds.height;
	}

	@Override protected void draw (SpriteBatch batch, float parentAlpha) {
		cache.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		cache.setPosition(x, y);
		cache.draw(batch);
	}

	@Override protected boolean touchDown (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override protected boolean touchUp (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override public Actor hit (float x, float y) {
		return x > 0 && y > 0 && x < width && y < height ? this : null;
	}
}
