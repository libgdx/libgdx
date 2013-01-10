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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.TimeUtils;

/** A list of tables, highlighting the currently selected item. An {@link ListAdapter} 
 * can be attached to the list, in order to customize the row layout.
 * <p>
 * {@link ChangeEvent} is fired when the list selection changes.
 * <p>
 *  * @author aspic */
public class TableList<T> extends Table {
	private TableListStyle style;
	private T[] items;
	private ListAdapter adapter;

	Table selected;
	int selectedIndex;

	public TableList (ListAdapter adapter, T[] items, Skin skin) {
		this(adapter, items, skin.get(TableListStyle.class));
	}

	public TableList (ListAdapter adapter, T[] items, Skin skin, String styleName) {
		this(adapter, items, skin.get(styleName, TableListStyle.class));
	}

	public TableList (ListAdapter adapter, T[] items, TableListStyle style) {
		setStyle(style);
		setAdapter(adapter);
		defaults().expandX().fillX();
		setItems(items);
	}

	public void setStyle (TableListStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		if (items != null)
			setItems(items);
		else
			invalidateHierarchy();
	}

	/** Returns the list's style. Modifying the returned style may not have an effect until {@link #setStyle(TableListStyle)} is called. */
	public TableListStyle getStyle () {
		return style;
	}
	
	/** @return The index of the currently selected item. The top item has an index of 0. */
	public int getSelectedIndex () {
		return selectedIndex;
	}
	
	public Table getSelectedRow() {
		return (Table)getChildren().get(selectedIndex);
	}

	/** @return The text of the currently selected item or null if the list is empty. */
	public T getSelection () {
		return items[selectedIndex];
	}

	/** Clears and sets new items for this list. */
	public void setItems (T[] items) {
		if (items == null ) throw new IllegalArgumentException("items cannot be null.");
		else if(adapter == null) throw new IllegalArgumentException("no adapter attached, use setAdapter");
		this.items = items;
		clear();
		for (int i = 0; i < items.length; i++) {
			
			// Le hack?
			final int currentIndex = i;
			
			// Create a row
			final Table row = adapter.setupRow(items[i], currentIndex);
			row.setBackground(style.unselected);
			row.setTouchable(Touchable.enabled);
			
			row.addListener(new ClickListener() {
				// Assign 
				int index = currentIndex;
				
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(selectedIndex != index || selected == null) {
						selectedIndex = index;
						
						ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
						fire(changeEvent);
						Pools.free(changeEvent);
						
						selected = row;
					} else if(selectedIndex == index) {
						selected = null;
						selectedIndex = -1;
					}
				}

				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					row.setBackground(style.selection);
					return super.touchDown(event, currentIndex, y, pointer, button);
				}
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					row.setBackground(style.unselected);
					super.touchUp(event, currentIndex, y, pointer, button);
				}

				
			});
			// Expand last
			if(i == items.length - 1) add(row).expand();
			else {
				add(row);
				row();
			}
		}
	}
	/** Returns the items for this list instance. */
	public T[] getItems () {
		return items;
	}
	
	/** Add an adapter which sets up items properly. */
	public void setAdapter(ListAdapter adapter) {
		this.adapter = adapter;
	}
	
	/** Interface for setting the row layout. An object will be provided,
	 * and it expects a table to be returned. */
	public interface ListAdapter<T> {
		/** Provides the object to be added, as well as the current index. */
		public Table setupRow(T item, int index);
	}

	/** The style for a list, see {@link TableList}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class TableListStyle {
		public Drawable selection;
		public Drawable unselected;

		public TableListStyle () {
		}

		public TableListStyle (Drawable selection, Drawable unselected) {
			this.selection = selection;
			this.unselected = selection;
		}

		public TableListStyle (TableListStyle style) {
			this.selection = style.selection;
			this.unselected = style.unselected;
		}
	}
}
