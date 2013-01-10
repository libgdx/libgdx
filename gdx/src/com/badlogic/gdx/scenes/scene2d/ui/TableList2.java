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
import com.badlogic.gdx.scenes.scene2d.ui.TableList.TableListStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.tablelayout.Cell;

/** A list of tables, highlighting the currently selected item. An {@link ListAdapter} 
 * can be attached to the list, in order to customize the row layout.
 * <p>
 * {@link ChangeEvent} is fired when the list selection changes.
 * <p>
 *  * @author aspic */
public class TableList2<T> extends Table {
	private TableListStyle style;
	private T[] items;
	private ListAdapter adapter;

	private int selectedIndex;

	public TableList2 (ListAdapter adapter, T[] items, Skin skin, String styleName) {
		this(adapter, items, skin.get(styleName, TableListStyle.class));
	}

	public TableList2 (ListAdapter adapter, T[] items, TableListStyle style) {
		setStyle(style);
		setAdapter(adapter);
		defaults().expandX().fillX();
		setItems(items);
		debug();
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
			Cell row = this.add().expandX();
			row = adapter.setupRow(row, items[i], currentIndex);
			row();
		}
	}
	
	private Cell selectedCell;
	
	public Actor hit (float x, float y, boolean touchable) {
		int selected = getRow(getY() + y);
		System.out.println(selected);
		if(selected >= 0 && selected < getCells().size()) selectedCell = getCells().get(selected);

		return super.hit(x, y, touchable);
	}
	
	public void draw (SpriteBatch batch, float parentAlpha) {
		if(selectedCell != null) drawCell(batch, selectedCell, true);
		for (int i = 0; i < getCells().size(); i++) {
			if(selectedIndex == i) continue;
			Cell cell = getCells().get(i);
			drawCell(batch, cell, false);
		}
		super.draw(batch, parentAlpha);
	}
	
	private void drawCell(SpriteBatch batch, Cell cell, boolean selected) {
		if(selected) {
			style.selection.draw(batch, getX() + cell.getWidgetX(), getY() + cell.getWidgetY(), getWidth(), cell.getMinHeight());
		} else {
			style.unselected.draw(batch, getX() + cell.getWidgetX(), getY() + cell.getWidgetY(), getWidth(), cell.getMinHeight());
		}
	}
	
	private void drawSelected(SpriteBatch batch) {
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
		public Cell setupRow(Cell row, T item, int index);
	}

}
