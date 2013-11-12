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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;

/** A list (aka list box) displays textual items and highlights the currently selected item.
 * <p>
 * {@link ChangeEvent} is fired when the list selection changes.
 * <p>
 * The preferred size of the list is determined by the text bounds of the items and the size of the {@link ListStyle#selection}.
 * @author mzechner */
public class List extends Widget implements Cullable {
	private ListStyle style;
	private String[] items;
	private int selectedIndex;
	private Rectangle cullingArea;
	private float prefWidth, prefHeight;
	private float itemHeight;
	private float textOffsetX, textOffsetY;
	private boolean selectable = true;

	public List (Object[] items, Skin skin) {
		this(items, skin.get(ListStyle.class));
	}

	public List (Object[] items, Skin skin, String styleName) {
		this(items, skin.get(styleName, ListStyle.class));
	}

	public List (Object[] items, ListStyle style) {
		setStyle(style);
		setItems(items);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());

		addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0 && button != 0) return false;
				if (!isSelectable()) return false; // don't eat touch event when NOT selectable
				List.this.touchDown(y);
				return true;
			}
		});
	}

	/** Sets whether this List's items are selectable. If not selectable, touch events will not be consumed. */
	public void setSelectable (boolean selectable) {
		this.selectable = selectable;
	}

	/** @return True if items are selectable. */
	public boolean isSelectable () {
		return selectable;
	}

	void touchDown (float y) {
		int oldIndex = selectedIndex;
		selectedIndex = (int)((getHeight() - y) / itemHeight);
		selectedIndex = Math.max(0, selectedIndex);
		selectedIndex = Math.min(items.length - 1, selectedIndex);
		if (oldIndex != selectedIndex) {
			ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
			if (fire(changeEvent)) selectedIndex = oldIndex;
			Pools.free(changeEvent);
		}
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
		Drawable selectedDrawable = style.selection;
		Color fontColorSelected = style.fontColorSelected;
		Color fontColorUnselected = style.fontColorUnselected;

		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		float x = getX();
		float y = getY();

		font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
		float itemY = getHeight();
		for (int i = 0; i < items.length; i++) {
			if (cullingArea == null || (itemY - itemHeight <= cullingArea.y + cullingArea.height && itemY >= cullingArea.y)) {
				if (selectedIndex == i) {
					selectedDrawable.draw(batch, x, y + itemY - itemHeight, getWidth(), itemHeight);
					font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha);
				}
				font.draw(batch, items[i], x + textOffsetX, y + itemY - textOffsetY);
				if (selectedIndex == i) {
					font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a
						* parentAlpha);
				}
			} else if (itemY < cullingArea.y) {
				break;
			}
			itemY -= itemHeight;
		}
	}

	/** @return The index of the currently selected item. The top item has an index of 0. Nothing selected has an index of -1. */
	public int getSelectedIndex () {
		return selectedIndex;
	}

	/** @param index Set to -1 for nothing selected. */
	public void setSelectedIndex (int index) {
		if (index < -1 || index >= items.length)
			throw new GdxRuntimeException("index must be >= -1 and < " + items.length + ": " + index);
		selectedIndex = index;
	}

	/** @return The text of the currently selected item, or null if the list is empty or nothing is selected. */
	public String getSelection () {
		if (items.length == 0 || selectedIndex == -1) return null;
		return items[selectedIndex];
	}

	/** Sets the selection to the item if found, else sets the selection to nothing.
	 * @return The new selected index. */
	public int setSelection (String item) {
		selectedIndex = -1;
		for (int i = 0, n = items.length; i < n; i++) {
			if (items[i].equals(item)) {
				selectedIndex = i;
				break;
			}
		}
		return selectedIndex;
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

		selectedIndex = 0;

		final BitmapFont font = style.font;
		final Drawable selectedDrawable = style.selection;

		itemHeight = font.getCapHeight() - font.getDescent() * 2;
		itemHeight += selectedDrawable.getTopHeight() + selectedDrawable.getBottomHeight();
		textOffsetX = selectedDrawable.getLeftWidth();
		textOffsetY = selectedDrawable.getTopHeight() - font.getDescent();

		prefWidth = 0;
		for (int i = 0; i < items.length; i++) {
			TextBounds bounds = font.getBounds(items[i]);
			prefWidth = Math.max(bounds.width, prefWidth);
		}
		prefWidth += selectedDrawable.getLeftWidth() + selectedDrawable.getRightWidth();
		prefHeight = items.length * itemHeight;

		invalidateHierarchy();
	}

	public String[] getItems () {
		return items;
	}

	public float getItemHeight () {
		return itemHeight;
	}

	public float getPrefWidth () {
		return prefWidth;
	}

	public float getPrefHeight () {
		return prefHeight;
	}

	public void setCullingArea (Rectangle cullingArea) {
		this.cullingArea = cullingArea;
	}

	/** The style for a list, see {@link List}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class ListStyle {
		public BitmapFont font;
		public Color fontColorSelected = new Color(1, 1, 1, 1);
		public Color fontColorUnselected = new Color(1, 1, 1, 1);
		public Drawable selection;

		public ListStyle () {
		}

		public ListStyle (BitmapFont font, Color fontColorSelected, Color fontColorUnselected, Drawable selection) {
			this.font = font;
			this.fontColorSelected.set(fontColorSelected);
			this.fontColorUnselected.set(fontColorUnselected);
			this.selection = selection;
		}

		public ListStyle (ListStyle style) {
			this.font = style.font;
			this.fontColorSelected.set(style.fontColorSelected);
			this.fontColorUnselected.set(style.fontColorUnselected);
			this.selection = style.selection;
		}
	}
}
