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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Cullable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A list (aka list box) displays textual items and highlights the currently selected item.
 * <p>
 * The preferred size of the list is determined by the text bounds of the items and the size of the
 * {@link ListStyle#selectedPatch}.
 * @author mzechner */
public class List extends Widget implements Cullable {
	private ListStyle style;
	private String[] items;
	private int selected;
	private SelectionListener listener;
	private Rectangle cullingArea;
	private float prefWidth, prefHeight;
	private float itemHeight;
	private float textOffsetX, textOffsetY;

	public List (Skin skin) {
		this(new String[0], skin);
	}

	public List (Object[] items, Skin skin) {
		this(items, skin.getStyle(ListStyle.class), null);
	}

	public List (Object[] items, ListStyle style) {
		this(items, style, null);
	}

	public List (Object[] items, ListStyle style, String name) {
		super(name);
		setStyle(style);
		setItems(items);
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public void setStyle (ListStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		if (items != null)
			setItems(items);
		else
			invalidateHierarchy();
	}

	/** Returns the list's style. Modifying the returned style may not have an effect until {@link #setStyle(ListStyle)} is called. */
	public ListStyle getStyle () {
		return style;
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		BitmapFont font = style.font;
		NinePatch selectedPatch = style.selectedPatch;
		Color fontColorSelected = style.fontColorSelected;
		Color fontColorUnselected = style.fontColorUnselected;

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
		float itemY = height;
		for (int i = 0; i < items.length; i++) {
			if (cullingArea == null || 
				(itemY - itemHeight <= cullingArea.y + cullingArea.height && itemY >= cullingArea.y)) {
				if (selected == i) {
					selectedPatch.draw(batch, x, y + itemY - itemHeight, Math.max(prefWidth, width), itemHeight);
					font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha);
				}
				font.draw(batch, items[i], x + textOffsetX, y + itemY - textOffsetY);
				if (selected == i) {
					font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a
						* parentAlpha);
				}
			} else if (itemY < cullingArea.y) {
				break;
			}
			itemY -= itemHeight;
		}
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		selected = (int)((height - y) / itemHeight);
		selected = Math.max(0, selected);
		selected = Math.min(items.length - 1, selected);
		if (listener != null && items.length > 0) listener.selected(this, selected, items[selected]);
		return true;
	}

	/** @return The index of the currently selected item. The top item has an index of 0. */
	public int getSelectedIndex () {
		return selected;
	}

	public void setSelectedIndex (int index) {
		if (index < 0 || index >= items.length)
			throw new GdxRuntimeException("index must be >= 0 and < " + items.length + ": " + index);
		selected = index;
	}

	/** @return The text of the currently selected item or null if the list is empty. */
	public String getSelection () {
		if (items.length == 0) return null;
		return items[selected];
	}

	/** @return The index of the item that was selected, or -1. */
	public int setSelection (String item) {
		selected = -1;
		for (int i = 0, n = items.length; i < n; i++) {
			if (items[i].equals(item)) {
				selected = i;
				break;
			}
		}
		return selected;
	}

	public void setItems (Object[] objects) {
		if (objects == null) throw new IllegalArgumentException("items cannot be null.");

		if (!(objects instanceof String[])) {
			String[] strings = new String[objects.length];
			for (int i = 0, n = objects.length; i < n; i++)
				strings[i] = String.valueOf(objects[i]);
			items = strings;
		} else
			items = (String[])objects;

		selected = 0;

		final BitmapFont font = style.font;
		final NinePatch selectedPatch = style.selectedPatch;

		itemHeight = font.getCapHeight() - font.getDescent() * 2;
		itemHeight += selectedPatch.getTopHeight() + selectedPatch.getBottomHeight();
		prefWidth += selectedPatch.getLeftWidth() + selectedPatch.getRightWidth();
		textOffsetX = selectedPatch.getLeftWidth();
		textOffsetY = selectedPatch.getTopHeight() - font.getDescent();

		prefWidth = 0;
		for (int i = 0; i < items.length; i++) {
			TextBounds bounds = font.getBounds(items[i]);
			prefWidth = Math.max(bounds.width, prefWidth);
		}
		prefHeight = items.length * itemHeight;

		invalidateHierarchy();
	}

	public String[] getItems () {
		return items;
	}

	public float getPrefWidth () {
		return prefWidth;
	}

	public float getPrefHeight () {
		return prefHeight;
	}

	/** @param listener May be null. */
	public void setSelectionListener (SelectionListener listener) {
		this.listener = listener;
	}

	public void setCullingArea (Rectangle cullingArea) {
		this.cullingArea = cullingArea;
	}

	/** The style for a list, see {@link List}.
	 * @author mzechner */
	static public class ListStyle {
		public BitmapFont font;
		public Color fontColorSelected = new Color(1, 1, 1, 1);
		public Color fontColorUnselected = new Color(1, 1, 1, 1);
		public NinePatch selectedPatch;

		public ListStyle () {
		}

		public ListStyle (BitmapFont font, Color fontColorSelected, Color fontColorUnselected, NinePatch selectedPatch) {
			this.font = font;
			this.fontColorSelected.set(fontColorSelected);
			this.fontColorUnselected.set(fontColorUnselected);
			this.selectedPatch = selectedPatch;
		}
		
		public ListStyle(ListStyle style) {
			this.font = style.font;
			this.fontColorSelected.set(style.fontColorSelected);
			this.fontColorUnselected.set(style.fontColorUnselected);
			this.selectedPatch = style.selectedPatch;
		}
	}
}
