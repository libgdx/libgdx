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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class CheckBox extends Widget {
	final CheckBoxStyle style;
	String label;	
	boolean isChecked = false;
	CheckedListener listener = null;
	
	final Vector2 boxPos = new Vector2();
	final Vector2 textPos = new Vector2();
	final TextBounds textBounds = new TextBounds();
	final Rectangle bounds = new Rectangle();
	float checkWidth = 0;
	float checkHeight = 0;	
	
	public CheckBox(String name, String label, CheckBoxStyle style) {
		super(name, 0, 0);
		this.style = style;
		this.label = label;		
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}	

	@Override
	public void layout() {
		final BitmapFont font = style.font;
		final TextureRegion checkedRegion = style.checked;
		final TextureRegion uncheckedRegion = style.unchecked;
		
		textBounds.set(font.getBounds(label));
		textBounds.height -= font.getDescent();
		checkWidth = Math.max(checkedRegion.getRegionWidth(), uncheckedRegion.getRegionWidth());
		checkHeight = Math.max(checkedRegion.getRegionHeight(), uncheckedRegion.getRegionHeight());		
		if(textBounds.height > checkHeight) {
			prefHeight = textBounds.height;
			boxPos.y = (int)((textBounds.height - checkedRegion.getRegionHeight()) / 2);
			textPos.y = textBounds.height;
		} else {
			prefHeight = checkHeight;			
			boxPos.y = 0;
			textPos.y = (int)((checkHeight - textBounds.height) / 2) + textBounds.height;
		}
		
		boxPos.x = 0;
		textPos.x = checkWidth + 5;
		prefWidth = checkWidth + 5 + textBounds.width;
		invalidated = false;
	}

	@Override
	protected void draw(SpriteBatch batch, float parentAlpha) {
		final BitmapFont font = style.font;
		final TextureRegion checkedRegion = style.checked;
		final TextureRegion uncheckedRegion = style.unchecked;
		final Color fontColor = style.fontColor;
		
		if(invalidated) layout();
		if(isChecked) batch.draw(checkedRegion, x + boxPos.x, y + boxPos.y);
		else batch.draw(uncheckedRegion, x + boxPos.x, y + boxPos.y);
		
		font.setColor(fontColor);
		font.draw(batch, label, x + textPos.x, y + textPos.y);
	}

	@Override
	protected boolean touchDown(float x, float y, int pointer) {
		return false;
	}

	@Override
	protected boolean touchUp(float x, float y, int pointer) {
		if(x >= 0 && x < width &&
		   y >= 0 && y < height) {
			isChecked = !isChecked;
			if(listener != null) listener.checked(this, isChecked);
		}
		return false;
	}

	@Override
	protected boolean touchDragged(float x, float y, int pointer) {
		return false;
	}

	@Override
	public Actor hit(float x, float y) {
		return x > 0 && x < width && y > 0 && y < height?this: null;
	}	
	
	public interface CheckedListener {
		public void checked(CheckBox checkBox, boolean isChecked);
	}
	
	public void setCheckedListener(CheckedListener listener) {
		this.listener = listener;
	}
	
	public static class CheckBoxStyle {
		public final BitmapFont font;
		public final Color fontColor;
		public final TextureRegion checked;
		public final TextureRegion unchecked;
		
		public CheckBoxStyle(BitmapFont font, Color fontColor, TextureRegion checked, TextureRegion unchecked) {
			this.font = font;
			this.fontColor = fontColor;
			this.checked = checked;
			this.unchecked = unchecked;
		}
	}

	public void setChecked (boolean isChecked) { 
		this.isChecked = isChecked;
		invalidate();
	}
	
	public boolean isChecked() {
		return isChecked;
	}
}