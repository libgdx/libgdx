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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ToggleButton extends Widget {
	final ToggleButtonStyle style;
	String text;	
	final TextBounds bounds = new TextBounds();
	boolean isPressed = false;
	ClickListener listener = null;

	public ToggleButton(String name, String label, ToggleButtonStyle style) {
		super(name, 0, 0);
		this.style = style;
		this.text = label;
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}

	@Override
	public void layout() {
		final BitmapFont font = style.font;
		final NinePatch downPatch = style.down;		
		
		bounds.set(font.getMultiLineBounds(text));
		bounds.height -= font.getDescent();

		prefHeight = downPatch.getBottomHeight() + downPatch.getTopHeight()
				+ bounds.height;
		prefWidth = downPatch.getLeftWidth() + downPatch.getRightWidth()
				+ bounds.width;
		invalidated = false;
	}

	@Override
	protected void draw(SpriteBatch batch, float parentAlpha) {
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;
		final NinePatch downPatch = style.down;
		final NinePatch upPatch = style.up;
		
		if(invalidated) layout();
		if (isPressed)
			downPatch.draw(batch, x, y, width, height, parentAlpha);
		else
			upPatch.draw(batch, x, y, width, height, parentAlpha);

		float textY = (int) (height / 2) + (int) (bounds.height / 2);
		font.setColor(fontColor);
		font.drawMultiLine(batch, text, x + (int) (width / 2), y + textY, 0,
				HAlignment.CENTER);
	}

	@Override
	protected boolean touchDown(float x, float y, int pointer) {
		if (pointer != 0)
			return false;
		if (hit(x, y) != null) {
			isPressed = !isPressed;			
			if(listener != null) listener.click(this, isPressed);
			return true;
		}
		return false;
	}

	@Override
	protected boolean touchUp(float x, float y, int pointer) {		
		return false;
	}

	@Override
	protected boolean touchDragged(float x, float y, int pointer) {
		return false;
	}

	@Override
	public Actor hit(float x, float y) {
		return x > 0 && x < width && y > 0 && y < height ? this : null;
	}

	public static class ToggleButtonStyle {
		public final NinePatch down;
		public final NinePatch up;
		public final BitmapFont font;
		public final Color fontColor;

		public ToggleButtonStyle(BitmapFont font, Color fontColor, NinePatch down,
				NinePatch up) {
			this.font = font;
			this.fontColor = fontColor;
			this.down = down;
			this.up = up;
		}
	}

	public interface ClickListener {
		public void click(ToggleButton button, boolean isPressed);
	}

	public void setText(String text) {
		this.text = text;
		invalidateHierarchy();
	}
	
	public String getText () { 
		return text;
	}

	public ToggleButton setClickListener(ClickListener listener) {
		this.listener = listener;
		return this;
	}
	
	public boolean isPressed() {
		return isPressed;
	}
	
	public void setPressed(boolean isPressed) {
		this.isPressed = isPressed;
	}	
}
