/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
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

package com.badlydrawngames.general;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlydrawngames.veryangryrobots.Assets;

public class SimpleButton {
	private HAlignment alignment;
	private String text;
	private boolean wasPressed;
	private float x;
	private float y;
	private float w;
	private float h;
	private boolean activated;
	private boolean down;
	private BitmapFont font;
	private float textHeight;

	public SimpleButton (String text, BitmapFont font) {
		this.text = text;
		this.wasPressed = false;
		this.activated = false;
		this.down = false;
		this.font = font;
		TextBounds bounds = Assets.textFont.getBounds(text);
		textHeight = bounds.height;
		w = bounds.width * 2;
		h = bounds.height * 2;
		alignment = HAlignment.CENTER;
	}

	public void setWidth (float width) {
		w = width;
	}

	public void setHeight (float height) {
		h = height;
	}

	public void setAlignment (HAlignment alignment) {
		this.alignment = alignment;
	}

	public void update (float delta, boolean justTouched, boolean isTouched, boolean justReleased, float x, float y) {
		wasPressed = false;
		if (justTouched && inBounds(x, y)) {
			activated = true;
			down = true;
		} else if (isTouched) {
			down = activated && inBounds(x, y);
		} else if (justReleased) {
			wasPressed = activated && inBounds(x, y);
			activated = false;
			down = false;
		} else {
			activated = false;
		}
	}

	private boolean inBounds (float x, float y) {
		return x >= this.x && x < this.x + this.w && y >= this.y && y < this.y + this.h;
	}

	public void draw (SpriteBatch spriteBatch) {
		Color oldColor = font.getColor();
		if (down) {
			spriteBatch.setColor(Color.RED);
		} else {
			spriteBatch.setColor(Color.BLUE);
		}
		spriteBatch.draw(Assets.pureWhiteTextureRegion, x, y, w, h);
		spriteBatch.setColor(Color.WHITE);
		if (down) {
			font.setColor(oldColor.r / 2, oldColor.g / 2, oldColor.b / 2, oldColor.a);
		}
		float textX = x;
		float textY = y + h;
		textY -= (h - textHeight) / 2;
		font.drawWrapped(spriteBatch, text, textX, textY, w, alignment);
		font.setColor(oldColor);
	}

	public boolean wasPressed () {
		return this.wasPressed;
	}

	public void rightOn (float right) {
		x = right - w;
	}

	public void leftOn (float left) {
		x = left;
	}

	public void centerHorizontallyOn (float centerX) {
		x = centerX - w / 2;
	}

	public void bottomOn (float bottom) {
		y = bottom;
	}

	public void topOn (float top) {
		y = top - h;
	}

	public void centerVerticallyOn (float centerY) {
		y = centerY - h / 2;
	}
}
