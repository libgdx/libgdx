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
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class List extends Widget {
	final ListStyle style;
	String[] entries;	
	boolean invalidated = false;
	float entryHeight = 0;
	float textOffsetX = 0;
	float textOffsetY = 0;
	int selected = 0;
	SelectionListener listener;	
	
	public List(String name, String[] entries, ListStyle style) {
		super(name, 0, 0);
		this.style = style;
		this.entries = entries;		
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}	

	@Override
	public void layout() {
		final BitmapFont font = style.font;		
		final NinePatch selectedPatch = style.selectedPatch;
		prefWidth = 0;
		prefHeight = 0;
		
		for(int i = 0; i < entries.length; i++) {
			String entry = entries[i];
			TextBounds bounds = font.getBounds(entry);
			prefWidth = Math.max(bounds.width, prefWidth);					
			
		}
				
		entryHeight = font.getLineHeight() - font.getDescent();
		entryHeight += selectedPatch.getTopHeight() + selectedPatch.getBottomHeight();
		prefWidth += selectedPatch.getLeftWidth() + selectedPatch.getRightWidth();		
		prefHeight = entries.length * entryHeight;
		textOffsetX = selectedPatch.getLeftWidth();
		textOffsetY = selectedPatch.getTopHeight() - font.getDescent();
		invalidated = false;
	}

	@Override
	protected void draw(SpriteBatch batch, float parentAlpha) {
		final BitmapFont font = style.font;		
		final NinePatch selectedPatch = style.selectedPatch;
		final Color fontColorSelected = style.fontColorSelected;
		final Color fontColorUnselected = style.fontColorUnselected;
		
		if(invalidated) layout();
		
		float posY = height;
		for(int i = 0; i < entries.length; i++) {
			if(selected == i) {
				selectedPatch.draw(batch, x, y + posY - entryHeight, Math.max(prefWidth, width), entryHeight);
				font.setColor(fontColorSelected);
			} else {
				font.setColor(fontColorUnselected);
			}
			font.draw(batch, entries[i], x + textOffsetX, y + posY - textOffsetY);
			posY -= entryHeight;
		}
	}

	@Override
	protected boolean touchDown(float x, float y, int pointer) {
		if(pointer != 0) return false;
		if(hit(x, y) != null) {
			selected = (int)((height-y) / entryHeight);
			selected = Math.max(0, selected);
			selected = Math.min(entries.length -1, selected);
			if(listener != null) listener.selected(this, selected, entries[selected]);
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
		return x >= 0 && x < Math.max(prefWidth, width) && y >= 0 && y < prefHeight?this:null;
	}

	public static class ListStyle {
		public final BitmapFont font;
		public final Color fontColorSelected = new Color(1, 1, 1, 1);
		public final Color fontColorUnselected = new Color(1, 1, 1, 1);
		public final NinePatch selectedPatch;
		
		public ListStyle(BitmapFont font, Color fontColorSelected, Color fontColorUnselected, NinePatch selectedPatch) {
			this.font = font;
			this.fontColorSelected.set(fontColorSelected);
			this.fontColorUnselected.set(this.fontColorUnselected);
			this.selectedPatch = selectedPatch;
		}
	}
	
	public interface SelectionListener {
		public void selected(List list, int selectedIndex, String selection);				
	}
	
	public int getSelectedIndex() {
		return selected;
	}
	
	public String getSelection() {
		return entries[selected];
	}
	
	public void setEntries(String[] entries) {
		this.entries = entries;
		selected = 0;
		invalidateHierarchy();
	}
	
	public void setSelectionListener(SelectionListener listener) {
		this.listener = listener;
	}
}
