/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.scenes.scene2d.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Layout;

/**
 * A simple Button {@link Actor}, useful for simple UIs
 * 
 * @author mzechner
 * 
 */
public class Button extends Actor implements Layout {
	public interface ClickListener {
		public void clicked (Button button);
	}

	public TextureRegion pressedRegion;
	public TextureRegion unpressedRegion;
	public ClickListener clickListener;
	public boolean pressed = false;
	protected int pointer = -1;

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
	 * Creates a new Button instance with the given name, using the complete supplied texture for displaying the pressed and
	 * unpressed state of the button.
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
		width = Math.abs(unpressedRegion.getRegionWidth());
		height = Math.abs(unpressedRegion.getRegionHeight());
		originX = width / 2.0f;
		originY = height / 2.0f;
		this.unpressedRegion = new TextureRegion(unpressedRegion);
		this.pressedRegion = new TextureRegion(pressedRegion);
	}

	@Override protected void draw (SpriteBatch batch, float parentAlpha) {
		TextureRegion region = pressed ? pressedRegion : unpressedRegion;
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (region.getTexture() != null) {
			if (scaleX == 1 && scaleY == 1 && rotation == 0)
				batch.draw(region, x, y, width, height);
			else
				batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
		}
	}

	@Override public boolean touchDown (float x, float y, int pointer) {
		if(pressed) return false;

		boolean result = x > 0 && y > 0 && x < width && y < height;
		pressed = result;
		
		if (pressed) {
			parent.focus(this, pointer);
			this.pointer = pointer;
		}
		return result;
	}

	@Override public boolean touchUp (float x, float y, int pointer) {
		if (!pressed) return false;

		if(pointer == this.pointer) {
			parent.focus(null, pointer);
		}
		pressed = false;
		if (clickListener != null) clickListener.clicked(this);
		return true;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
		return pressed;
	}

	public Actor hit (float x, float y) {
		return x > 0 && y > 0 && x < width && y < height ? this : null;
	}

	public void layout () {
	}

	public void invalidate () {
	}

	public float getPrefWidth () {
		return unpressedRegion.getRegionWidth() * scaleX;
	}

	public float getPrefHeight () {
		return unpressedRegion.getRegionHeight() * scaleY;
	}
}
