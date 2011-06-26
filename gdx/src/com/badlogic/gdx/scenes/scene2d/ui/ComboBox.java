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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

public class ComboBox extends Widget {	
	final Stage stage;	
	final ComboBoxStyle style;
	String[] entries;
	int selection = 0;
	final TextBounds bounds = new TextBounds();
	final Vector2 screenCoords = new Vector2();
	ComboList list = null;
	SelectionListener listener;
	
	public ComboBox(String name, String[] entries, Stage stage, ComboBoxStyle style) {
		super(name, 0, 0);
		this.entries = entries;
		this.style = style;
		this.stage = stage;		
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}	

	@Override public void layout () {
		final NinePatch background = style.background;
		final BitmapFont font = style.font;
		
		prefHeight = background.getTotalHeight();
		float max = 0;
		for(int i = 0; i < entries.length; i++) {
			max = Math.max(font.getBounds(entries[i]).width, max);
		}
		prefWidth = background.getLeftWidth() + background.getRightWidth() + max;
		invalidated = false;		
	}

	@Override protected void draw (SpriteBatch batch, float parentAlpha) {
		final NinePatch background = style.background;
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;
		
		if(invalidated) layout();
		background.draw(batch, x, y, width, height, parentAlpha);
		if(entries.length > 0) {
			float availableWidth = width - background.getLeftWidth() - background.getRightWidth();
			int numGlyphs = font.computeVisibleGlyphs(entries[selection], 0, entries[selection].length(), availableWidth);
			bounds.set(font.getBounds(entries[selection]));
			bounds.height -= font.getDescent();
			float textY = (int)(height / 2) + (int)(bounds.height / 2);
			font.setColor(fontColor);
			font.draw(batch, entries[selection], x + background.getLeftWidth(), y + textY, 0, numGlyphs); 
		}
		
		// calculate screen coords where list should be displayed
		ScissorStack.toWindowCoordinates(stage.getCamera(), batch.getTransformMatrix(), screenCoords.set(x, y));
	}

	Vector2 stageCoords = new Vector2();
	@Override protected boolean touchDown (float x, float y, int pointer) {
		if(pointer != 0) return false;
		if(hit(x, y) != null) {
			if(list != null) stage.removeActor(list);
			stage.toStageCoordinates((int)screenCoords.x, (int)screenCoords.y, stageCoords);
			list = new ComboList(this.name + "-list", stageCoords.x, stageCoords.y);
			stage.addActor(list);
			return true;
		}
		return false;
	}

	@Override protected boolean touchUp (float x, float y, int pointer) {
		return false;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
		return false;
	}
	
	public static class ComboBoxStyle {
		public final NinePatch background;
		public final NinePatch listBackground;		
		public final NinePatch listSelection;
		public final BitmapFont font;
		public final Color fontColor = new Color(1, 1, 1, 1);
		
		public ComboBoxStyle(BitmapFont font, Color fontColor, NinePatch background, NinePatch listBackground, NinePatch listSelection) {
			this.background = background;
			this.listBackground = listBackground;
			this.listSelection = listSelection;
			this.font = font;
			this.fontColor.set(fontColor);			
		}
	}
	
	public interface SelectionListener {
		public void selected(ComboBox comboBox, int selectionIndex, String selection);
	}
	
	public void setSelectionListener(SelectionListener listener) {
		this.listener = listener;
	}
	
	protected class ComboList extends Actor {
		Vector2 oldScreenCoords = new Vector2();
		float entryHeight = 0;
		float textOffsetX = 0;
		float textOffsetY = 0;
		int selected = ComboBox.this.selection;
		
		public ComboList (String name, float x, float y) {
			super(name);
			this.x = x;
			this.y = y;
			this.width = ComboBox.this.width;
			this.height = 100;
			this.oldScreenCoords.set(screenCoords);
			stage.getRoot().focus(this, 0);
			layout();
		}

		private void layout() {
			final BitmapFont font = style.font;
			final NinePatch listSelection = style.listSelection;
			
			float prefWidth = 0;
			float prefHeight = 0;
			
			for(int i = 0; i < entries.length; i++) {
				String entry = entries[i];
				TextBounds bounds = font.getBounds(entry);
				prefWidth = Math.max(bounds.width, prefWidth);					
				
			}
					
			entryHeight = font.getLineHeight() - font.getDescent();
			entryHeight += listSelection.getTopHeight() + listSelection.getBottomHeight();
			prefWidth += listSelection.getLeftWidth() + listSelection.getRightWidth();		
			prefHeight = entries.length * entryHeight;
			textOffsetX = listSelection.getLeftWidth();
			textOffsetY = listSelection.getTopHeight() - font.getDescent();
			
			width = Math.max(prefWidth, ComboBox.this.width);
			height = prefHeight;
			y -= height;
		}
		
		@Override protected void draw (SpriteBatch batch, float parentAlpha) {
			final NinePatch listBackground = style.listBackground;
			final NinePatch listSelection = style.listSelection;
			final BitmapFont font = style.font;
			final Color fontColor = style.fontColor;
			
			listBackground.draw(batch, x, y, width, height);
			float posY = height;
			for(int i = 0; i < entries.length; i++) {
				if(selected == i) {
					listSelection.draw(batch, x, y + posY - entryHeight, width, entryHeight);					
				}
				font.setColor(fontColor);
				font.draw(batch, entries[i], x + textOffsetX, y + posY - textOffsetY);
				posY -= entryHeight;
			}
			
			if(screenCoords.x != oldScreenCoords.x || screenCoords.y != oldScreenCoords.y) {
				stage.removeActor(this);
			}
		}

		@Override protected boolean touchDown (float x, float y, int pointer) {
			if(pointer != 0) return false;
			if(hit(x, y) != null) {			
				selected = (int)((height-y) / entryHeight);
				selected = Math.max(0, selected);
				selected = Math.min(entries.length -1, selected);
				selection = selected;
				if(entries.length > 0 && listener != null) listener.selected(ComboBox.this, selected, entries[selected]);
			}			
			return true;
		}

		boolean firstUp = false;
		@Override protected boolean touchUp (float x, float y, int pointer) {
			if(firstUp) {
				stage.removeActor(this);
			} else firstUp = true;
			return true;
		}

		@Override protected boolean touchDragged (float x, float y, int pointer) {
			return true;
		}
		
		@Override protected boolean touchMoved(float x, float y) {
			if(hit(x, y) != null) {
				selected = (int)((height-y) / entryHeight);
				selected = Math.max(0, selected);
				selected = Math.min(entries.length -1, selected);								
			}
			return true;
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height?this: null;
		}		
	}

	public void setSelection (int selection) {
		this.selection = selection;
	}
}
