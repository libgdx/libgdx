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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
public class SelectBox extends Widget {
	static final Vector2 tmpCoords = new Vector2();

	SelectBoxStyle style;
	String[] items;
	int selectedIndex = 0;
	private final TextBounds bounds = new TextBounds();
	SelectList list;
	private float prefWidth, prefHeight;
	private ClickListener clickListener;
	int maxListCount;

	public SelectBox (Object[] items, Skin skin) {
		this(items, skin.get(SelectBoxStyle.class));
	}

	public SelectBox (Object[] items, Skin skin, String styleName) {
		this(items, skin.get(styleName, SelectBoxStyle.class));
	}

	public SelectBox (Object[] items, SelectBoxStyle style) {
		setStyle(style);
		setItems(items);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());

		addListener(clickListener = new ClickListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0 && button != 0) return false;
				Stage stage = getStage();
				if (list == null) list = new SelectList();
				list.show(stage);
				return true;
			}
		});
	}

	/** Set the max number of items to display when the select box is opened. Set to 0 (the default) to display as many as fit in
	 * the stage height. */
	public void setMaxListCount (int maxListCount) {
		this.maxListCount = maxListCount;
	}

	/** @return Max number of dropdown List items to display when the box is opened, or <= 0 to display them all. */
	public int getMaxListCount () {
		return maxListCount;
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
		selectedIndex = 0;

		Drawable bg = style.background;
		BitmapFont font = style.font;

		prefHeight = Math.max(bg.getTopHeight() + bg.getBottomHeight() + font.getCapHeight() - font.getDescent() * 2,
			bg.getMinHeight());

		float max = 0;
		for (int i = 0; i < items.length; i++)
			max = Math.max(font.getBounds(items[i]).width, max);
		prefWidth = bg.getLeftWidth() + bg.getRightWidth() + max;
		prefWidth = Math.max(prefWidth, max + style.listBackground.getLeftWidth() + style.listBackground.getRightWidth()
			+ style.listSelection.getLeftWidth() + style.listSelection.getRightWidth());

		if (items.length > 0) {
			ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
			SelectBox.this.fire(changeEvent);
			Pools.free(changeEvent);
		}

		invalidateHierarchy();
	}

	public String[] getItems () {
		return items;
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		Drawable background;
		if (list != null && list.getParent() != null && style.backgroundOpen != null)
			background = style.backgroundOpen;
		else if (clickListener.isOver() && style.backgroundOver != null)
			background = style.backgroundOver;
		else
			background = style.background;
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
			int numGlyphs = font.computeVisibleGlyphs(items[selectedIndex], 0, items[selectedIndex].length(), availableWidth);
			bounds.set(font.getBounds(items[selectedIndex]));
			height -= background.getBottomHeight() + background.getTopHeight();
			float textY = (int)(height / 2 + background.getBottomHeight() + bounds.height / 2);
			font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
			font.draw(batch, items[selectedIndex], x + background.getLeftWidth(), y + textY, 0, numGlyphs);
		}
	}

	/** Sets the selected item via it's index
	 * @param selection the selection index */
	public void setSelection (int selection) {
		this.selectedIndex = selection;
	}

	public void setSelection (String item) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(item)) {
				selectedIndex = i;
			}
		}
	}

	/** @return the index of the current selection. The top item has an index of 0 */
	public int getSelectionIndex () {
		return selectedIndex;
	}

	/** @return the string of the currently selected item */
	public String getSelection () {
		return items[selectedIndex];
	}

	public float getPrefWidth () {
		return prefWidth;
	}

	public float getPrefHeight () {
		return prefHeight;
	}

	public void hideList () {
		if (list == null || list.getParent() == null) return;
		list.addAction(sequence(fadeOut(0.15f, Interpolation.fade), removeActor()));
	}

	class SelectList extends ScrollPane {
		final List list;
		final Vector2 screenCoords = new Vector2();

		public SelectList () {
			super(null);

			getStyle().background = style.listBackground;
			setOverscroll(false, false);

			ListStyle listStyle = new ListStyle();
			listStyle.font = style.font;
			listStyle.fontColorSelected = style.fontColor;
			listStyle.fontColorUnselected = style.fontColor;
			listStyle.selection = style.listSelection;
			list = new List(new Object[0], listStyle);
			setWidget(list);
			list.addListener(new InputListener() {
				public boolean mouseMoved (InputEvent event, float x, float y) {
					list.setSelectedIndex(Math.min(items.length - 1, (int)((list.getHeight() - y) / list.getItemHeight())));
					return true;
				}
			});

			addListener(new InputListener() {
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					if (event.getTarget() == list) return true;
					hideList();
					return false;
				}

				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					if (hit(x, y, true) == list) {
						setSelection(list.getSelectedIndex());
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

			list.setItems(items);
			list.setSelectedIndex(selectedIndex);

			// Show the list above or below the select box, limited to a number of items and the available height in the stage.
			float itemHeight = list.getItemHeight();
			float height = itemHeight * (maxListCount <= 0 ? items.length : Math.min(maxListCount, items.length));
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

			scrollToCenter(0, list.getHeight() - selectedIndex * itemHeight - itemHeight / 2, 0, 0);
			updateVisualScroll();

			clearActions();
			getColor().a = 0;
			addAction(fadeIn(0.3f, Interpolation.fade));
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
		public Drawable background;
		/** Optional. */
		public Drawable backgroundOver, backgroundOpen;
		public Drawable listBackground;
		public Drawable listSelection;
		public BitmapFont font;
		public Color fontColor = new Color(1, 1, 1, 1);

		public SelectBoxStyle () {
		}

		public SelectBoxStyle (BitmapFont font, Color fontColor, Drawable background, Drawable listBackground,
			Drawable listSelection) {
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
