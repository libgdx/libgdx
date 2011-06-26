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
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Label extends Widget {		
	final LabelStyle style;
	String label;
	final TextBounds bounds = new TextBounds();
	final Vector2 textPos = new Vector2();
	
	public Label(String name, String label, LabelStyle style) {
		super(name, 0, 0);
		this.style = style;
		this.label = label;		
		this.touchable = false;
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}	

	@Override
	public void layout() {
		final BitmapFont font = style.font;
		
		bounds.set(font.getMultiLineBounds(label));
		bounds.height -= font.getDescent();
		prefWidth = bounds.width;
		prefHeight = bounds.height;
		textPos.x = 0;
		textPos.y = prefHeight;
		invalidated = false;
	}

	@Override
	protected void draw(SpriteBatch batch, float parentAlpha) {
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;
		
		if(invalidated) layout();
		font.setColor(fontColor);		
		font.drawMultiLine(batch, label, x + textPos.x, y + textPos.y);		
	}

	@Override
	protected boolean touchDown(float x, float y, int pointer) {
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
		return null;
	}
	
	public static class LabelStyle {
		public BitmapFont font;
		public Color fontColor;
		
		public LabelStyle(BitmapFont font, Color fontColor) {
			this.font = font;
			this.fontColor = fontColor;
		}
	}	
	
	public void setText(String text) {
		this.label = text;
		invalidateHierarchy();
	}
}
