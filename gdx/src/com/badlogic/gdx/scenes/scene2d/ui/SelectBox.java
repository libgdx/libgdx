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

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pools;

/** A select box (aka a drop-down list) allows a user to choose one of a number of values from a list. When inactive, the selected
 * value is displayed. When activated, it shows the list of values that may be selected.
 * <p>
 * {@link ChangeEvent} is fired when the selectbox selection changes.
 * <p>
 * The preferred size of the select box is determined by the maximum text bounds of the items and the size of the
 * {@link SelectBoxStyle#background}.
 * @author mzechner
 * @author Nathan Sweet */
public class SelectBox<T> extends Widget implements Disableable {
	static final Vector2 tmpCoords = new Vector2();

	SelectBoxStyle style;
	final Array<T> items = new Array();
	T selected;
	private final TextBounds bounds = new TextBounds();
	ListScroll scroll;
	Selection<T> selection;
	Actor previousScrollFocus;
	private float prefWidth, prefHeight;
	private ClickListener clickListener;
	int maxListCount;
	boolean disabled;

	public SelectBox (Skin skin) {
		this(skin.get(SelectBoxStyle.class));
	}

	public SelectBox (Skin skin, String styleName) {
		this(skin.get(styleName, SelectBoxStyle.class));
	}

	public SelectBox (SelectBoxStyle style) {
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());

		scroll = new ListScroll();
		selection = scroll.list.getSelection();

		addListener(clickListener = new ClickListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0 && button != 0) return false;
				if (disabled) return false;
				showList();
				return true;
			}
		});
	}

	/** Set the max number of items to display when the select box is opened. Set to 0 (the default) to display as many as fit in
	 * the stage height. */
	public void setMaxListCount (int maxListCount) {
		this.maxListCount = maxListCount;
	}

	/** @return Max number of items to display when the box is opened, or <= 0 to display them all. */
	public int getMaxListCount () {
		return maxListCount;
	}

	public void setStyle (SelectBoxStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/** Returns the select box's style. Modifying the returned style may not have an effect until {@link #setStyle(SelectBoxStyle)}
	 * is called. */
	public SelectBoxStyle getStyle () {
		return style;
	}

	public void setItems (T... newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");

		items.clear();
		items.addAll(newItems);

		scroll.list.setItems(items);

		invalidateHierarchy();
	}

	public void setItems (Array<T> newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");

		items.clear();
		items.addAll(newItems);

		scroll.list.setItems(items);

		invalidateHierarchy();
	}

	public Array<T> getItems () {
		return items;
	}

	public void layout () {
		Drawable bg = style.background;
		BitmapFont font = style.font;

		prefHeight = Math.max(bg.getTopHeight() + bg.getBottomHeight() + font.getCapHeight() - font.getDescent() * 2,
			bg.getMinHeight());

		float maxItemWidth = 0;
		for (int i = 0; i < items.size; i++)
			maxItemWidth = Math.max(font.getBounds(items.get(i).toString()).width, maxItemWidth);

		prefWidth = bg.getLeftWidth() + bg.getRightWidth() + maxItemWidth;

		ListStyle listStyle = style.listStyle;
		ScrollPaneStyle scrollStyle = style.scrollStyle;
		prefWidth = Math.max(
			prefWidth,
			maxItemWidth
				+ (scrollStyle.background == null ? 0 : scrollStyle.background.getLeftWidth()
					+ scrollStyle.background.getRightWidth())
				+ listStyle.selection.getLeftWidth()
				+ listStyle.selection.getRightWidth()
				+ Math.max(style.scrollStyle.vScroll != null ? style.scrollStyle.vScroll.getMinWidth() : 0,
					style.scrollStyle.vScrollKnob != null ? style.scrollStyle.vScrollKnob.getMinWidth() : 0));
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();

		Drawable background;
		if (disabled && style.backgroundDisabled != null)
			background = style.backgroundDisabled;
		else if (scroll.hasParent() && style.backgroundOpen != null)
			background = style.backgroundOpen;
		else if (clickListener.isOver() && style.backgroundOver != null)
			background = style.backgroundOver;
		else
			background = style.background;
		final BitmapFont font = style.font;
		final Color fontColor = (disabled && style.disabledFontColor != null) ? style.disabledFontColor : style.fontColor;

		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		background.draw(batch, x, y, width, height);
		T selected = this.selected != null ? this.selected : selection.first();
		if (selected != null) {
			float availableWidth = width - background.getLeftWidth() - background.getRightWidth();
			String string = selected.toString();
			int numGlyphs = font.computeVisibleGlyphs(string, 0, string.length(), availableWidth);
			bounds.set(font.getBounds(string));
			height -= background.getBottomHeight() + background.getTopHeight();
			float textY = (int)(height / 2 + background.getBottomHeight() + bounds.height / 2);
			font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
			font.draw(batch, string, x + background.getLeftWidth(), y + textY, 0, numGlyphs);
		}
	}

	public Selection<T> getSelection () {
		return selection;
	}

	/** Returns the first selected item, or null. */
	public T getSelected () {
		return selection.first();
	}

	/** Sets the selection to only the item if found, else selects the first item. */
	public void setSelected (T item) {
		if (items.contains(item, false))
			selection.set(item);
		else if (items.size > 0)
			selection.set(items.first());
		else
			selection.clear();
	}

	/** @return The index of the first selected item. The top item has an index of 0. Nothing selected has an index of -1. */
	public int getSelectedIndex () {
		ObjectSet<T> selected = selection.items();
		return selected.size == 0 ? -1 : items.indexOf(selected.first(), false);
	}

	/** Sets the selection to only the selected index. */
	public void setSelectedIndex (int index) {
		selection.set(items.get(index));
	}

	public void setDisabled (boolean disabled) {
		if (disabled && !this.disabled) hideList();
		this.disabled = disabled;
	}

	public float getPrefWidth () {
		validate();
		return prefWidth;
	}

	public float getPrefHeight () {
		validate();
		return prefHeight;
	}

	public void showList () {
		selected = selection.first();
		scroll.list.setTouchable(Touchable.enabled);
		scroll.show(getStage());
	}

	public void hideList () {
		if (!scroll.hasParent()) return;
		selected = null;
		scroll.list.setTouchable(Touchable.disabled);
		Stage stage = scroll.getStage();
		if (stage != null) {
			if (previousScrollFocus != null && previousScrollFocus.getStage() == null) previousScrollFocus = null;
			Actor actor = stage.getScrollFocus();
			if (actor == null || actor.isDescendantOf(scroll)) stage.setScrollFocus(previousScrollFocus);
		}
		scroll.addAction(sequence(fadeOut(0.15f, Interpolation.fade), removeActor()));
	}

	/** Returns the list shown when the select box is open. */
	public List getList () {
		return scroll.list;
	}

	/** Returns the scroll pane containing the list that is shown when the select box is open. */
	public ScrollPane getScrollPane () {
		return scroll;
	}

	class ListScroll extends ScrollPane {
		final List<T> list;
		final Vector2 screenCoords = new Vector2();

		public ListScroll () {
			super(null, style.scrollStyle);

			setOverscroll(false, false);
			setFadeScrollBars(false);

			list = new List(style.listStyle);
			setWidget(list);
			list.addListener(new InputListener() {
				public boolean mouseMoved (InputEvent event, float x, float y) {
					list.setSelectedIndex(Math.min(items.size - 1, (int)((list.getHeight() - y) / list.getItemHeight())));
					return true;
				}
			});

			addListener(new InputListener() {
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					if (event.getTarget() == list) return true;
					setSelected(selected); // Revert.
					hideList();
					return false;
				}

				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					if (hit(x, y, true) == list) {
						ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
						SelectBox.this.fire(changeEvent);
						Pools.free(changeEvent);
						hideList();
					}
				}
			});
		}

		public void show (Stage stage) {
			stage.addActor(this);

			SelectBox.this.localToStageCoordinates(tmpCoords.set(0, 0));
			screenCoords.set(tmpCoords);

			// Show the list above or below the select box, limited to a number of items and the available height in the stage.
			float itemHeight = list.getItemHeight();
			float height = itemHeight * (maxListCount <= 0 ? items.size : Math.min(maxListCount, items.size));
			Drawable background = getStyle().background;
			if (background != null) height += background.getTopHeight() + background.getBottomHeight();

			float heightBelow = tmpCoords.y;
			float heightAbove = stage.getCamera().viewportHeight - tmpCoords.y - SelectBox.this.getHeight();
			boolean below = true;
			if (height > heightBelow) {
				if (heightAbove > heightBelow) {
					below = false;
					height = Math.min(height, heightAbove);
				} else
					height = heightBelow;
			}

			if (below)
				setY(tmpCoords.y - height);
			else
				setY(tmpCoords.y + SelectBox.this.getHeight());
			setX(tmpCoords.x);
			setWidth(SelectBox.this.getWidth());
			setHeight(height);

			validate();
			scrollToCenter(0, list.getHeight() - getSelectedIndex() * itemHeight - itemHeight / 2, 0, 0);
			updateVisualScroll();

			clearActions();
			getColor().a = 0;
			addAction(fadeIn(0.3f, Interpolation.fade));

			previousScrollFocus = null;
			Actor actor = stage.getScrollFocus();
			if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor;

			stage.setScrollFocus(this);
		}

		@Override
		public Actor hit (float x, float y, boolean touchable) {
			Actor actor = super.hit(x, y, touchable);
			return actor != null ? actor : this;
		}

		public void act (float delta) {
			super.act(delta);
			SelectBox.this.localToStageCoordinates(tmpCoords.set(0, 0));
			if (tmpCoords.x != screenCoords.x || tmpCoords.y != screenCoords.y) hideList();
		}
	}

	/** The style for a select box, see {@link SelectBox}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class SelectBoxStyle {
		public BitmapFont font;
		public Color fontColor = new Color(1, 1, 1, 1);
		/** Optional. */
		public Color disabledFontColor;
		public Drawable background;
		public ScrollPaneStyle scrollStyle;
		public ListStyle listStyle;
		/** Optional. */
		public Drawable backgroundOver, backgroundOpen, backgroundDisabled;

		public SelectBoxStyle () {
		}

		public SelectBoxStyle (BitmapFont font, Color fontColor, Drawable background, ScrollPaneStyle scrollStyle,
			ListStyle listStyle) {
			this.font = font;
			this.fontColor.set(fontColor);
			this.background = background;
			this.scrollStyle = scrollStyle;
			this.listStyle = listStyle;
		}

		public SelectBoxStyle (SelectBoxStyle style) {
			this.font = style.font;
			this.fontColor.set(style.fontColor);
			if (style.disabledFontColor != null) this.disabledFontColor = new Color(style.disabledFontColor);
			this.background = style.background;
			this.backgroundOver = style.backgroundOver;
			this.backgroundOpen = style.backgroundOpen;
			this.backgroundDisabled = style.backgroundDisabled;
			this.scrollStyle = new ScrollPaneStyle(style.scrollStyle);
			this.listStyle = new ListStyle(style.listStyle);
		}
	}
}
