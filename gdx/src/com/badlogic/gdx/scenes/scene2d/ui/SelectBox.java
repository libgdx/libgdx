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

/** A select box (aka a drop-down list) allows a user to choose one of a number of values from a list. When inactive, the selected
 * value is displayed. When activated, it shows the list of values that may be selected.
 * <p>
 * The preferred size of the select box is determined by the maximum text bounds of the items and the size of the
 * {@link SelectBoxStyle#background}.
 * @author mzechner */
public class SelectBox extends Widget {
	SelectBoxStyle style;
	String[] items;
	int selection = 0;
	private final TextBounds bounds = new TextBounds();
	final Vector2 screenCoords = new Vector2();
	private SelectList list = null;
	SelectionListener listener;
	private float prefWidth, prefHeight;
	final Vector2 stageCoords = new Vector2();

	public SelectBox (Skin skin) {
		this(new String[0], skin);
	}

	public SelectBox (Object[] items, Skin skin) {
		this(items, skin.getStyle(SelectBoxStyle.class), null);
	}

	public SelectBox (Object[] items, SelectBoxStyle style) {
		this(items, style, null);
	}

	public SelectBox (Object[] items, SelectBoxStyle style, String name) {
		super(name);
		setStyle(style);
		setItems(items);
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public void setStyle (SelectBoxStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		if (items != null)
			setItems(items);
		else
			invalidateHierarchy();
	}

	/** Returns the select box's style. Modifying the returned style may not have an effect until {@link #setStyle(SelectBoxStyle)}
	 * is called. */
	public SelectBoxStyle getStyle () {
		return style;
	}

	public void setItems (Object[] objects) {
		if (objects == null) throw new IllegalArgumentException("items cannot be null.");

		if (!(objects instanceof String[])) {
			String[] strings = new String[objects.length];
			for (int i = 0, n = objects.length; i < n; i++)
				strings[i] = String.valueOf(objects[i]);
			objects = strings;
		}

		this.items = (String[])objects;

		NinePatch bg = style.background;
		BitmapFont font = style.font;

		prefHeight = Math.max(bg.getTopHeight() + bg.getBottomHeight() + font.getCapHeight() - font.getDescent() * 2,
			bg.getTotalHeight());

		float max = 0;
		for (int i = 0; i < items.length; i++)
			max = Math.max(font.getBounds(items[i]).width, max);
		prefWidth = bg.getLeftWidth() + bg.getRightWidth() + max;

		invalidateHierarchy();
	}

	@Override
	public void layout () {
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		final NinePatch background = style.background;
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		background.draw(batch, x, y, width, height);
		if (items.length > 0) {
			float availableWidth = width - background.getLeftWidth() - background.getRightWidth();
			int numGlyphs = font.computeVisibleGlyphs(items[selection], 0, items[selection].length(), availableWidth);
			bounds.set(font.getBounds(items[selection]));
			float textY = (int)(height / 2) + (int)(bounds.height / 2);
			font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
			font.draw(batch, items[selection], x + background.getLeftWidth(), y + textY, 0, numGlyphs);
		}

		// calculate screen coords where list should be displayed
		ScissorStack.toWindowCoordinates(stage.getCamera(), batch.getTransformMatrix(), screenCoords.set(x, y));
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (list != null && list.parent != null) {
			stage.removeActor(list);
			return true;
		}
		stage.toStageCoordinates((int)screenCoords.x, (int)screenCoords.y, stageCoords);
		list = new SelectList(this.name + "-list", stageCoords.x, stageCoords.y);
		stage.addActor(list);
		stage.setTouchFocus(list, 0);
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
	}

	/** Sets the {@link SelectionListener}.
	 * @param listener the listener or null */
	public void setSelectionListener (SelectionListener listener) {
		this.listener = listener;
	}

	/** Sets the selected item via it's index
	 * @param selection the selection index */
	public void setSelection (int selection) {
		this.selection = selection;
	}

	public void setSelection (String item) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(item)) {
				selection = i;
			}
		}
	}

	/** @return the index of the current selection. The top item has an index of 0 */
	public int getSelectionIndex () {
		return selection;
	}

	/** @return the string of the currently selected item */
	public String getSelection () {
		return items[selection];
	}

	public float getPrefWidth () {
		return prefWidth;
	}

	public float getPrefHeight () {
		return prefHeight;
	}

	class SelectList extends Actor {
		Vector2 oldScreenCoords = new Vector2();
		float itemHeight;
		float textOffsetX, textOffsetY;
		int selected = SelectBox.this.selection;

		public SelectList (String name, float x, float y) {
			super(name);
			this.x = x;
			width = SelectBox.this.width;
			height = 100;
			this.oldScreenCoords.set(screenCoords);
			layout();
			Stage stage = SelectBox.this.getStage();
			if (y - height < 0 && y + SelectBox.this.height + height < SelectBox.this.getStage().getCamera().viewportHeight)
				this.y = y + SelectBox.this.height;
			else
				this.y = y - height;
		}

		private void layout () {
			final BitmapFont font = style.font;
			final NinePatch listSelection = style.listSelection;

			float prefWidth = 0;
			float prefHeight = 0;

			for (int i = 0; i < items.length; i++) {
				String item = items[i];
				TextBounds bounds = font.getBounds(item);
				prefWidth = Math.max(bounds.width, prefWidth);
			}

			itemHeight = font.getCapHeight() + -font.getDescent() * 2;
			itemHeight += listSelection.getTopHeight() + listSelection.getBottomHeight();
			itemHeight *= SelectBox.this.parent.scaleY;
			prefWidth += listSelection.getLeftWidth() + listSelection.getRightWidth();
			prefHeight = items.length * itemHeight;
			textOffsetX = listSelection.getLeftWidth();
			textOffsetY = listSelection.getTopHeight() + -font.getDescent();

			width = Math.max(prefWidth, SelectBox.this.width);
			width *= SelectBox.this.parent.scaleX;
			height = prefHeight;
		}

		@Override
		public void draw (SpriteBatch batch, float parentAlpha) {
			final NinePatch listBackground = style.listBackground;
			final NinePatch listSelection = style.listSelection;
			final BitmapFont font = style.font;
			final Color fontColor = style.fontColor;

			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			listBackground.draw(batch, x, y, width, height);
			float posY = height;
			for (int i = 0; i < items.length; i++) {
				if (selected == i) {
					listSelection.draw(batch, x, y + posY - itemHeight, width, itemHeight);
				}
				font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
				font.setScale(SelectBox.this.parent.scaleX, SelectBox.this.parent.scaleY);
				font.draw(batch, items[i], x + textOffsetX, y + posY - textOffsetY);
				font.setScale(1, 1);
				posY -= itemHeight;
			}
		}

		@Override
		public boolean touchDown (float x, float y, int pointer) {
			if (pointer != 0 || hit(x, y) == null) return false;
			selected = (int)((height - y) / itemHeight);
			selected = Math.max(0, selected);
			selected = Math.min(items.length - 1, selected);
			selection = selected;
			if (items.length > 0 && listener != null) listener.selected(SelectBox.this, selected, items[selected]);
			return true;
		}

		@Override
		public void touchUp (float x, float y, int pointer) {
			if (stage != null) stage.removeActor(this);
		}

		@Override
		public void touchDragged (float x, float y, int pointer) {
		}

		@Override
		public boolean touchMoved (float x, float y) {
			if (hit(x, y) != null) {
				selected = (int)((height - y) / itemHeight);
				selected = Math.max(0, selected);
				selected = Math.min(items.length - 1, selected);
			}
			return true;
		}

		@Override
		public Actor hit (float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		public void act (float delta) {
			if (screenCoords.x != oldScreenCoords.x || screenCoords.y != oldScreenCoords.y) {
				if (stage != null) stage.removeActor(this);
			}
		}
	}

	/** The style for a select box, see {@link SelectBox}.
	 * @author mzechner */
	static public class SelectBoxStyle {
		public NinePatch background;
		public NinePatch listBackground;
		public NinePatch listSelection;
		public BitmapFont font;
		public Color fontColor = new Color(1, 1, 1, 1);

		public SelectBoxStyle () {
		}

		public SelectBoxStyle (BitmapFont font, Color fontColor, NinePatch background, NinePatch listBackground,
			NinePatch listSelection) {
			this.background = background;
			this.listBackground = listBackground;
			this.listSelection = listSelection;
			this.font = font;
			this.fontColor.set(fontColor);
		}
	}
}
