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

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;
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
	static final Vector2 temp = new Vector2();

	SelectBoxStyle style;
	final Array<T> items = new Array();
	SelectBoxList<T> selectBoxList;
	private float prefWidth, prefHeight;
	private ClickListener clickListener;
	boolean disabled;
	private int alignment = Align.left;
	boolean selectedPrefWidth;

	final ArraySelection<T> selection = new ArraySelection(items) {
		public boolean fireChangeEvent () {
			if (selectedPrefWidth) invalidateHierarchy();
			return super.fireChangeEvent();
		}
	};

	public SelectBox (Skin skin) {
		this(skin.get(SelectBoxStyle.class));
	}

	public SelectBox (Skin skin, String styleName) {
		this(skin.get(styleName, SelectBoxStyle.class));
	}

	public SelectBox (SelectBoxStyle style) {
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());

		selection.setActor(this);
		selection.setRequired(true);

		selectBoxList = new SelectBoxList(this);

		addListener(clickListener = new ClickListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0 && button != 0) return false;
				if (disabled) return false;
				if (selectBoxList.hasParent())
					hideList();
				else
					showList();
				return true;
			}
		});
	}

	/** Set the max number of items to display when the select box is opened. Set to 0 (the default) to display as many as fit in
	 * the stage height. */
	public void setMaxListCount (int maxListCount) {
		selectBoxList.maxListCount = maxListCount;
	}

	/** @return Max number of items to display when the box is opened, or <= 0 to display them all. */
	public int getMaxListCount () {
		return selectBoxList.maxListCount;
	}

	protected void setStage (Stage stage) {
		if (stage == null) selectBoxList.hide();
		super.setStage(stage);
	}

	public void setStyle (SelectBoxStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		if (selectBoxList != null) {
			selectBoxList.setStyle(style.scrollStyle);
			selectBoxList.list.setStyle(style.listStyle);
		}
		invalidateHierarchy();
	}

	/** Returns the select box's style. Modifying the returned style may not have an effect until {@link #setStyle(SelectBoxStyle)}
	 * is called. */
	public SelectBoxStyle getStyle () {
		return style;
	}

	/** Set the backing Array that makes up the choices available in the SelectBox */
	public void setItems (T... newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
		float oldPrefWidth = getPrefWidth();

		items.clear();
		items.addAll(newItems);
		selection.validate();
		selectBoxList.list.setItems(items);

		invalidate();
		if (oldPrefWidth != getPrefWidth()) invalidateHierarchy();
	}

	/** Sets the items visible in the select box. */
	public void setItems (Array<T> newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
		float oldPrefWidth = getPrefWidth();

		if (newItems != items) {
			items.clear();
			items.addAll(newItems);
		}
		selection.validate();
		selectBoxList.list.setItems(items);

		invalidate();
		if (oldPrefWidth != getPrefWidth()) invalidateHierarchy();
	}

	public void clearItems () {
		if (items.size == 0) return;
		items.clear();
		selection.clear();
		invalidateHierarchy();
	}

	/** Returns the internal items array. If modified, {@link #setItems(Array)} must be called to reflect the changes. */
	public Array<T> getItems () {
		return items;
	}

	public void layout () {
		Drawable bg = style.background;
		BitmapFont font = style.font;

		if (bg != null) {
			prefHeight = Math.max(bg.getTopHeight() + bg.getBottomHeight() + font.getCapHeight() - font.getDescent() * 2,
				bg.getMinHeight());
		} else
			prefHeight = font.getCapHeight() - font.getDescent() * 2;

		Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
		GlyphLayout layout = layoutPool.obtain();
		if (selectedPrefWidth) {
			prefWidth = 0;
			if (bg != null) prefWidth = bg.getLeftWidth() + bg.getRightWidth();
			T selected = getSelected();
			if (selected != null) {
				layout.setText(font, toString(selected));
				prefWidth += layout.width;
			}
		} else {
			float maxItemWidth = 0;
			for (int i = 0; i < items.size; i++) {
				layout.setText(font, toString(items.get(i)));
				maxItemWidth = Math.max(layout.width, maxItemWidth);
			}

			prefWidth = maxItemWidth;
			if (bg != null) prefWidth = Math.max(prefWidth + bg.getLeftWidth() + bg.getRightWidth(), bg.getMinWidth());

			ListStyle listStyle = style.listStyle;
			ScrollPaneStyle scrollStyle = style.scrollStyle;
			float listWidth = maxItemWidth + listStyle.selection.getLeftWidth() + listStyle.selection.getRightWidth();
			bg = scrollStyle.background;
			if (bg != null) listWidth = Math.max(listWidth + bg.getLeftWidth() + bg.getRightWidth(), bg.getMinWidth());
			if (selectBoxList == null || !selectBoxList.disableY) {
				listWidth += Math.max(style.scrollStyle.vScroll != null ? style.scrollStyle.vScroll.getMinWidth() : 0,
					style.scrollStyle.vScrollKnob != null ? style.scrollStyle.vScrollKnob.getMinWidth() : 0);
			}
			prefWidth = Math.max(prefWidth, listWidth);
		}
		layoutPool.free(layout);
	}

	public void draw (Batch batch, float parentAlpha) {
		validate();

		Drawable background;
		if (disabled && style.backgroundDisabled != null)
			background = style.backgroundDisabled;
		else if (selectBoxList.hasParent() && style.backgroundOpen != null)
			background = style.backgroundOpen;
		else if (clickListener.isOver() && style.backgroundOver != null)
			background = style.backgroundOver;
		else if (style.background != null)
			background = style.background;
		else
			background = null;
		BitmapFont font = style.font;
		Color fontColor;
		if (disabled && style.disabledFontColor != null)
			fontColor = style.disabledFontColor;
		else if (style.overFontColor != null && (clickListener.isOver() || selectBoxList.hasParent()))
			fontColor = style.overFontColor;
		else
			fontColor = style.fontColor;

		Color color = getColor();
		float x = getX(), y = getY();
		float width = getWidth(), height = getHeight();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (background != null) background.draw(batch, x, y, width, height);

		T selected = selection.first();
		if (selected != null) {
			if (background != null) {
				width -= background.getLeftWidth() + background.getRightWidth();
				height -= background.getBottomHeight() + background.getTopHeight();
				x += background.getLeftWidth();
				y += (int)(height / 2 + background.getBottomHeight() + font.getData().capHeight / 2);
			} else {
				y += (int)(height / 2 + font.getData().capHeight / 2);
			}
			font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
			drawItem(batch, font, selected, x, y, width);
		}
	}

	protected GlyphLayout drawItem (Batch batch, BitmapFont font, T item, float x, float y, float width) {
		String string = toString(item);
		return font.draw(batch, string, x, y, 0, string.length(), width, alignment, false, "...");
	}

	/** Sets the alignment of the selected item in the select box. See {@link #getList()} and {@link List#setAlignment(int)} to set
	 * the alignment in the list shown when the select box is open.
	 * @param alignment See {@link Align}. */
	public void setAlignment (int alignment) {
		this.alignment = alignment;
	}

	/** Get the set of selected items, useful when multiple items are selected
	 * @return a Selection object containing the selected elements */
	public ArraySelection<T> getSelection () {
		return selection;
	}

	/** Returns the first selected item, or null. For multiple selections use {@link SelectBox#getSelection()}. */
	@Null
	public T getSelected () {
		return selection.first();
	}

	/** Sets the selection to only the passed item, if it is a possible choice, else selects the first item. */
	public void setSelected (@Null T item) {
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

	/** When true the pref width is based on the selected item. */
	public void setSelectedPrefWidth (boolean selectedPrefWidth) {
		this.selectedPrefWidth = selectedPrefWidth;
	}

	/** Returns the pref width of the select box if the widest item was selected, for use when
	 * {@link #setSelectedPrefWidth(boolean)} is true. */
	public float getMaxSelectedPrefWidth () {
		Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
		GlyphLayout layout = layoutPool.obtain();
		float width = 0;
		for (int i = 0; i < items.size; i++) {
			layout.setText(style.font, toString(items.get(i)));
			width = Math.max(layout.width, width);
		}
		Drawable bg = style.background;
		if (bg != null) width = Math.max(width + bg.getLeftWidth() + bg.getRightWidth(), bg.getMinWidth());
		return width;
	}

	public void setDisabled (boolean disabled) {
		if (disabled && !this.disabled) hideList();
		this.disabled = disabled;
	}

	public boolean isDisabled () {
		return disabled;
	}

	public float getPrefWidth () {
		validate();
		return prefWidth;
	}

	public float getPrefHeight () {
		validate();
		return prefHeight;
	}

	protected String toString (T item) {
		return item.toString();
	}

	public void showList () {
		if (items.size == 0) return;
		if (getStage() != null) selectBoxList.show(getStage());
	}

	public void hideList () {
		selectBoxList.hide();
	}

	/** Returns the list shown when the select box is open. */
	public List<T> getList () {
		return selectBoxList.list;
	}

	/** Disables scrolling of the list shown when the select box is open. */
	public void setScrollingDisabled (boolean y) {
		selectBoxList.setScrollingDisabled(true, y);
		invalidateHierarchy();
	}

	/** Returns the scroll pane containing the list that is shown when the select box is open. */
	public ScrollPane getScrollPane () {
		return selectBoxList;
	}

	protected void onShow (Actor selectBoxList, boolean below) {
		selectBoxList.getColor().a = 0;
		selectBoxList.addAction(fadeIn(0.3f, Interpolation.fade));
	}

	protected void onHide (Actor selectBoxList) {
		selectBoxList.getColor().a = 1;
		selectBoxList.addAction(sequence(fadeOut(0.15f, Interpolation.fade), removeActor()));
	}

	/** @author Nathan Sweet */
	static class SelectBoxList<T> extends ScrollPane {
		private final SelectBox<T> selectBox;
		int maxListCount;
		private final Vector2 screenPosition = new Vector2();
		final List<T> list;
		private InputListener hideListener;
		private Actor previousScrollFocus;

		public SelectBoxList (final SelectBox<T> selectBox) {
			super(null, selectBox.style.scrollStyle);
			this.selectBox = selectBox;

			setOverscroll(false, false);
			setFadeScrollBars(false);
			setScrollingDisabled(true, false);

			list = new List<T>(selectBox.style.listStyle) {
				public String toString (T obj) {
					return selectBox.toString(obj);
				}
			};
			list.setTouchable(Touchable.disabled);
			list.setTypeToSelect(true);
			setActor(list);

			list.addListener(new ClickListener() {
				public void clicked (InputEvent event, float x, float y) {
					T selected = list.getSelected();
					// Force clicking the already selected item to trigger a change event.
					if (selected != null) selectBox.selection.items().clear();
					selectBox.selection.choose(selected);
					hide();
				}

				public boolean mouseMoved (InputEvent event, float x, float y) {
					int index = list.getItemIndexAt(y);
					if (index != -1) list.setSelectedIndex(index);
					return true;
				}
			});

			addListener(new InputListener() {
				public void exit (InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
					if (toActor == null || !isAscendantOf(toActor)) {
						list.selection.set(list.getSelected());
					}
				}
			});

			hideListener = new InputListener() {
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					Actor target = event.getTarget();
					if (isAscendantOf(target)) return false;
					list.selection.set(list.getSelected());
					hide();
					return false;
				}

				public boolean keyDown (InputEvent event, int keycode) {
					switch (keycode) {
					case Keys.ENTER:
						selectBox.selection.choose(list.getSelected());
						// Fall thru.
					case Keys.ESCAPE:
						hide();
						event.stop();
						return true;
					}
					return false;
				}
			};
		}

		public void show (Stage stage) {
			if (list.isTouchable()) return;

			stage.addActor(this);
			stage.addCaptureListener(hideListener);
			stage.addListener(list.getKeyListener());

			selectBox.localToStageCoordinates(screenPosition.set(0, 0));

			// Show the list above or below the select box, limited to a number of items and the available height in the stage.
			float itemHeight = list.getItemHeight();
			float height = itemHeight * (maxListCount <= 0 ? selectBox.items.size : Math.min(maxListCount, selectBox.items.size));
			Drawable scrollPaneBackground = getStyle().background;
			if (scrollPaneBackground != null) height += scrollPaneBackground.getTopHeight() + scrollPaneBackground.getBottomHeight();
			Drawable listBackground = list.getStyle().background;
			if (listBackground != null) height += listBackground.getTopHeight() + listBackground.getBottomHeight();

			float heightBelow = screenPosition.y;
			float heightAbove = stage.getCamera().viewportHeight - screenPosition.y - selectBox.getHeight();
			boolean below = true;
			if (height > heightBelow) {
				if (heightAbove > heightBelow) {
					below = false;
					height = Math.min(height, heightAbove);
				} else
					height = heightBelow;
			}

			if (below)
				setY(screenPosition.y - height);
			else
				setY(screenPosition.y + selectBox.getHeight());
			setX(screenPosition.x);
			setHeight(height);
			validate();
			float width = Math.max(getPrefWidth(), selectBox.getWidth());
			if (getPrefHeight() > height && !disableY) width += getScrollBarWidth();
			setWidth(width);

			validate();
			scrollTo(0, list.getHeight() - selectBox.getSelectedIndex() * itemHeight - itemHeight / 2, 0, 0, true, true);
			updateVisualScroll();

			previousScrollFocus = null;
			Actor actor = stage.getScrollFocus();
			if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor;
			stage.setScrollFocus(this);

			list.selection.set(selectBox.getSelected());
			list.setTouchable(Touchable.enabled);
			clearActions();
			selectBox.onShow(this, below);
		}

		public void hide () {
			if (!list.isTouchable() || !hasParent()) return;
			list.setTouchable(Touchable.disabled);

			Stage stage = getStage();
			if (stage != null) {
				stage.removeCaptureListener(hideListener);
				stage.removeListener(list.getKeyListener());
				if (previousScrollFocus != null && previousScrollFocus.getStage() == null) previousScrollFocus = null;
				Actor actor = stage.getScrollFocus();
				if (actor == null || isAscendantOf(actor)) stage.setScrollFocus(previousScrollFocus);
			}

			clearActions();
			selectBox.onHide(this);
		}

		public void draw (Batch batch, float parentAlpha) {
			selectBox.localToStageCoordinates(temp.set(0, 0));
			if (!temp.equals(screenPosition)) hide();
			super.draw(batch, parentAlpha);
		}

		public void act (float delta) {
			super.act(delta);
			toFront();
		}

		protected void setStage (Stage stage) {
			Stage oldStage = getStage();
			if (oldStage != null) {
				oldStage.removeCaptureListener(hideListener);
				oldStage.removeListener(list.getKeyListener());
			}
			super.setStage(stage);
		}
	}

	/** The style for a select box, see {@link SelectBox}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class SelectBoxStyle {
		public BitmapFont font;
		public Color fontColor = new Color(1, 1, 1, 1);
		/** Optional. */
		@Null public Color overFontColor, disabledFontColor;
		/** Optional. */
		@Null public Drawable background;
		public ScrollPaneStyle scrollStyle;
		public ListStyle listStyle;
		/** Optional. */
		@Null public Drawable backgroundOver, backgroundOpen, backgroundDisabled;

		public SelectBoxStyle () {
		}

		public SelectBoxStyle (BitmapFont font, Color fontColor, @Null Drawable background, ScrollPaneStyle scrollStyle,
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
			if (style.overFontColor != null) this.overFontColor = new Color(style.overFontColor);
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
