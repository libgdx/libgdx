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

/**
 * A simple Button {@link Actor}, useful for simple UIs
 * 
 * @author mzechner
 *
 */
public class Button extends Actor {
	public interface ClickListener {
		public void clicked (Button button);
	}

	public final TextureRegion pressedRegion;
	public final TextureRegion unpressedRegion;
	public ClickListener clickListener;
	protected boolean pressed = false;

	/**
	 * Creates a new Button instance with the given name.
	 * @param name the name 
	 */
	public Button (String name) {
		super(name);
		this.pressedRegion = new TextureRegion();
		this.unpressedRegion = new TextureRegion();
	}

	/**
	 * Creates a new Button instance with the given name, using
	 * the complete supplied texture for displaying the pressed
	 * and unpressed state of the button.
	 * @param name the name
	 * @param texture the {@link Texture}
	 */
	public Button (String name, Texture texture) {
		super(name);
		originX = texture.getWidth() / 2.0f;
		originY = texture.getHeight() / 2.0f;
		width = texture.getWidth();
		height = texture.getHeight();
		pressedRegion = new TextureRegion(texture);
		unpressedRegion = new TextureRegion(texture);
	}

	public Button (String name, TextureRegion region) {
		this(name, region, region);
	}

	public Button (String name, TextureRegion unpressedRegion, TextureRegion pressedRegion) {
		super(name);
		width = unpressedRegion.getWidth();
		height = unpressedRegion.getHeight();
		originX = width / 2.0f;
		originY = height / 2.0f;
		this.unpressedRegion = new TextureRegion(unpressedRegion);
		this.pressedRegion = new TextureRegion(pressedRegion);
	}

	@Override protected void render (SpriteBatch batch) {
		TextureRegion region = pressed ? pressedRegion : unpressedRegion;
		batch.setColor(color);
		if (region.getTexture() != null) {
			if (scaleX == 0 && scaleY == 0 && rotation == 0)
				batch.draw(region, x, y, width, height);
			else
				batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
		}
	}

	@Override protected boolean touchDown (float x, float y, int pointer) {
		boolean result = x > 0 && y > 0 && x < width && y < height;
		pressed = result;
		if (pressed) parent.focus(this);
		return result;
	}

	@Override protected boolean touchUp (float x, float y, int pointer) {
		if (!pressed) return false;

		parent.focus(null);
		pressed = false;
		if (clickListener != null) clickListener.clicked(this);
		return true;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
		return true;
	}

	public Actor hit (float x, float y) {
		return x > 0 && y > 0 && x < width && y < height ? this : null;
	}

}
