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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
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
	SelectBoxStyle style;
	String[] items;
	int selectedIndex = 0;
	private final TextBounds bounds = new TextBounds();
	final Vector2 screenCoords = new Vector2();
	SelectList list;
	private float prefWidth, prefHeight;
	private ClickListener clickListener;

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
				if (list != null && list.getParent() != null) {
					hideList();
					return true;
				}
				Stage stage = getStage();
				Vector2 stageCoords = Vector2.tmp;
				stage.screenToStageCoordinates(stageCoords.set(screenCoords.x, screenCoords.y));
				list = new SelectList(stageCoords.x, stageCoords.y);
				stage.addActor(list);
				return true;
			}
		});
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
		prefWidth = Math.max(prefWidth, max + style.listBackground.getLeftWidth() + style.listBackground.getRightWidth() + 2
			* style.itemSpacing);

		invalidateHierarchy();
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

		// calculate screen coords where list should be displayed
		getStage().toScreenCoordinates(screenCoords.set(x, y), batch.getTransformMatrix());
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
		if (list.getParent() == null) return;
		list.addAction(sequence(fadeOut(0.15f, Interpolation.fade), removeActor()));
	}

	class SelectList extends Actor {
		Vector2 oldScreenCoords = new Vector2();
		float itemHeight;
		float textOffsetX, textOffsetY;
		int listSelectedIndex = SelectBox.this.selectedIndex;

		InputListener stageListener = new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0 && button != 0) return false;
				stageToLocalCoordinates(Vector2.tmp);
				x = Vector2.tmp.x;
				y = Vector2.tmp.y;
				if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
					listSelectedIndex = (int)((getHeight() - y) / itemHeight);
					listSelectedIndex = Math.max(0, listSelectedIndex);
					listSelectedIndex = Math.min(items.length - 1, listSelectedIndex);
					selectedIndex = listSelectedIndex;
					if (items.length > 0) {
						ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
						SelectBox.this.fire(changeEvent);
						Pools.free(changeEvent);
					}
				}
				return true;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				hideList();
				event.getStage().removeCaptureListener(stageListener);
			}

			public boolean mouseMoved (InputEvent event, float x, float y) {
				stageToLocalCoordinates(Vector2.tmp);
				x = Vector2.tmp.x;
				y = Vector2.tmp.y;
				if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
					listSelectedIndex = (int)((getHeight() - style.listBackground.getTopHeight() - y) / itemHeight);
					listSelectedIndex = Math.max(0, listSelectedIndex);
					listSelectedIndex = Math.min(items.length - 1, listSelectedIndex);
				}
				return true;
			}
		};

		public SelectList (float x, float y) {
			setBounds(x, 0, SelectBox.this.getWidth(), 100);
			this.oldScreenCoords.set(screenCoords);
			layout();
			Stage stage = SelectBox.this.getStage();
			float height = getHeight();
			if (y - height < 0 && y + SelectBox.this.getHeight() + height < stage.getCamera().viewportHeight)
				setY(y + SelectBox.this.getHeight());
			else
				setY(y - height);
			stage.addCaptureListener(stageListener);
			getColor().a = 0;
			addAction(fadeIn(0.3f, Interpolation.fade));
		}

		private void layout () {
			final BitmapFont font = style.font;
			final Drawable listSelection = style.listSelection;
			final Drawable listBackground = style.listBackground;

			itemHeight = font.getCapHeight() + -font.getDescent() * 2 + style.itemSpacing;
			itemHeight += listSelection.getTopHeight() + listSelection.getBottomHeight();

			textOffsetX = listSelection.getLeftWidth() + style.itemSpacing;
			textOffsetY = listSelection.getTopHeight() + -font.getDescent() + style.itemSpacing / 2;

			setWidth(SelectBox.this.getWidth());
			setHeight(items.length * itemHeight + listBackground.getTopHeight() + listBackground.getBottomHeight());
		}

		@Override
		public void draw (SpriteBatch batch, float parentAlpha) {
			final Drawable listBackground = style.listBackground;
			final Drawable listSelection = style.listSelection;
			final BitmapFont font = style.font;
			final Color fontColor = style.fontColor;

			float x = getX();
			float y = getY();
			float width = getWidth();
			float height = getHeight();

			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			listBackground.draw(batch, x, y, width, height);

			width -= listBackground.getLeftWidth() + listBackground.getRightWidth();
			x += listBackground.getLeftWidth();
			float posY = height - listBackground.getTopHeight();
			for (int i = 0; i < items.length; i++) {
				if (listSelectedIndex == i) {
					listSelection.draw(batch, x, y + posY - itemHeight, width, itemHeight);
				}
				font.setColor(fontColor.r, fontColor.g, fontColor.b, color.a * fontColor.a * parentAlpha);
				font.draw(batch, items[i], x + textOffsetX, y + posY - textOffsetY);
				posY -= itemHeight;
			}
		}

		@Override
		public Actor hit (float x, float y, boolean touchable) {
			return this;
		}

		public void act (float delta) {
			super.act(delta);
			if (screenCoords.x != oldScreenCoords.x || screenCoords.y != oldScreenCoords.y) hideList();
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
		public float itemSpacing = 10;

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
