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
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

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
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
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
		selection = 0;

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

		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();

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
		ScissorStack.toWindowCoordinates(getStage().getCamera(), batch.getTransformMatrix(), screenCoords.set(x, y));
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (list != null && list.getParent() != null) {
			list.remove();
			return true;
		}
		Stage stage = getStage();
		Vector2 stageCoords = stage.toStageCoordinates((int)screenCoords.x, (int)screenCoords.y);
		list = new SelectList(getName() + "-list", stageCoords.x, stageCoords.y);
		stage.addActor(list);
		stage.setTouchFocus(list, 0);
		return true;
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
		boolean ownsTouch = false;

		public SelectList (String name, float x, float y) {
			super(name);
			setX(x);
			setWidth(SelectBox.this.getWidth());
			setHeight(100);
			this.oldScreenCoords.set(screenCoords);
			layout();
			Stage stage = SelectBox.this.getStage();
			float height = getHeight();
			if (y - height < 0 && y + SelectBox.this.getHeight() + height < SelectBox.this.getStage().getCamera().viewportHeight)
				setY(y + SelectBox.this.getHeight());
			else
				setY(y - height);
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

			itemHeight = font.getCapHeight() + -font.getDescent() * 2 + style.itemSpacing;
			itemHeight += listSelection.getTopHeight() + listSelection.getBottomHeight();
			itemHeight *= SelectBox.this.getParent().getScaleY();
			prefWidth += listSelection.getLeftWidth() + listSelection.getRightWidth() + 2 * style.itemSpacing;
			prefHeight = items.length * itemHeight;
			textOffsetX = listSelection.getLeftWidth() + style.itemSpacing;
			textOffsetY = listSelection.getTopHeight() + -font.getDescent() + style.itemSpacing / 2;

			float width = Math.max(prefWidth, SelectBox.this.getWidth());
			setWidth(width * SelectBox.this.getParent().getScaleX());
			setHeight(prefHeight);
		}

		@Override
		public void draw (SpriteBatch batch, float parentAlpha) {
			final NinePatch listBackground = style.listBackground;
			final NinePatch listSelection = style.listSelection;
			final BitmapFont font = style.font;
			final Color fontColor = style.fontColor;

			float x = getX();
			float y = getY();
			float width = getWidth();
			float height = getHeight();
			float scaleX = SelectBox.this.getParent().getScaleX();
			float scaleY = SelectBox.this.getParent().getScaleY();

			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			listBackground.draw(batch, x, y, width, height);
			float posY = height;
			for (int i = 0; i < items.length; i++) {
				if (selected == i) {
					listSelection.draw(batch, x, y + posY - itemHeight, width, itemHeight);
				}
				font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
				font.setScale(scaleX, scaleY);
				font.draw(batch, items[i], x + textOffsetX, y + posY - textOffsetY);
				font.setScale(1, 1);
				posY -= itemHeight;
			}
		}

		@Override
		public boolean touchDown (float x, float y, int pointer) {
			if (pointer != 0) return false;
			ownsTouch = true;
			if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
				selected = (int)((getHeight() - y) / itemHeight);
				selected = Math.max(0, selected);
				selected = Math.min(items.length - 1, selected);
				selection = selected;
				if (items.length > 0 && listener != null) listener.selected(SelectBox.this, selected, items[selected]);
			}
			return super.touchDown(x, y, pointer);
		}

		@Override
		public void touchUp (float x, float y, int pointer) {
			if (ownsTouch) remove();
			ownsTouch = false;
			super.touchUp(x, y, pointer);
		}

		@Override
		public boolean touchMoved (float x, float y) {
			if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
				selected = (int)((getHeight() - y) / itemHeight);
				selected = Math.max(0, selected);
				selected = Math.min(items.length - 1, selected);
			}
			super.mouseMoved(x, y);
			return true;
		}

		@Override
		public Actor hit (float x, float y) {
			return this;
		}

		public void act (float delta) {
			if (screenCoords.x != oldScreenCoords.x || screenCoords.y != oldScreenCoords.y) remove();
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
		public float itemSpacing = 10;

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

		public SelectBoxStyle (SelectBoxStyle style) {
			this.background = style.background;
			this.listBackground = style.listBackground;
			this.listSelection = style.listSelection;
			this.font = style.font;
			this.fontColor.set(style.fontColor);
		}
	}
}
