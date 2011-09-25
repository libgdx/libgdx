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
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/** A dropdown or combo box.
 * 
 * <h2>Functionality</h2> A ComboBox contains a list of Strings, with one of the strings being selected and displayed in the main
 * area of the ComboBox. Clicking the ComboBox brings up a popup list showing all the items. This popup list will grab the touch
 * focus while it is displayed. This is achieved by temporarily adding a new Actor to the root of the Stage the ComboBox is
 * contained in. As soon as an item is selected or a mouse click outside the area of the popup list is registered, the popup will
 * disappear again and the focus is given back. </p>
 * 
 * A {@link SelectionListener} can be registered with the ComboBox to receive notification of selection changes.</p>
 * 
 * <h2>Layout</h2> A ComboBox's (preferred) width and height are determined by the border patches in the background
 * {@link NinePatch} as well as the bounding box of the widest item in the list of strings. Use
 * {@link ComboBox#setPrefSize(int, int)} to change this size programmatically. In case the set size is to small to contain the
 * widest item, artifacts may appear.</p>
 * 
 * The additional popup list will be positioned at the bottom edge of the ComboBox, displaying all items. The width and size is
 * governed by the background {@link NinePatch} of the popup list as well as the bounding box around the list items.
 * 
 * <h2>Style</h2> A ComboBox is a {@link Widget} displaying a background {@link NinePatch} as well as the selected list item as a
 * label via a {@link BitmapFont} and a corresponding {@link Color}. Additionally a popup menu might be displayed, using a
 * {@link NinePatch} for the background, another {@link NinePatch} for highlighting the current selection and the same
 * {@link BitmapFont} and Color used to display the selected item in the actual ComboBox.</p>
 * 
 * The style is defined via an instance of the {@link ComboBoxStyle} class, which can be either done programmatically or via a
 * {@link Skin}.</p>
 * 
 * A ComboBox's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <combobox name="styleName"  			 
 *           background="backgroundNinePatch" 
 *           listBackground="popupBackgroundNinePatch" 
 *           listSelection="popupSelectionNinePatch"
 *           font="fontName" 
 *           fontColor="colorName" />
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newComboBox(String, String[], Stage, String)}.</li>
 * <li>The <code>background</code> attribute references a {@link NinePatch} by name, to be used as the ComboBox's background</li>
 * <li>The <code>listBackground</code> attribute references a {@link NinePatch} by name, to be used as the background for the
 * popup list</li>
 * <li>The <code>listSelection</code> attribute references a {@link NinePatch} by name, to be used for highlighting a selection in
 * the popup list</li>
 * <li>The <code>font</code> attribute references a {@link BitmapFont} by name, to be used to render the list items</li>
 * <li>The <code>fontColor</code> attribute references a {@link Color} by name, to be used to render the list items</li>
 * </ul>
 * 
 * @author mzechner */
public class ComboBox extends Widget {
	protected final Stage stage;
	protected ComboBoxStyle style;
	protected String[] items;
	protected int selection = 0;
	protected final TextBounds bounds = new TextBounds();
	protected final Vector2 screenCoords = new Vector2();
	protected ComboList list = null;
	protected SelectionListener listener;
	protected float prefWidth, prefHeight;

	public ComboBox (String[] items, Stage stage, Skin skin) {
		this(items, stage, skin.getStyle(ComboBoxStyle.class), null);
	}

	public ComboBox (String[] items, Stage stage, ComboBoxStyle style) {
		this(items, stage, style, null);
	}

	/** Creates a new combo box. The width and height are determined by the widets item and the style.
	 * @param name the name
	 * @param items the single-line items
	 * @param stage the stage, used for the popup
	 * @param style the {@link ComboBoxStyle} */
	public ComboBox (String[] items, Stage stage, ComboBoxStyle style, String name) {
		super(name);
		setStyle(style);
		setItems(items);
		this.stage = stage;
		layout();
	}

	/** Sets the style of this widget.
	 * @param style */
	public void setStyle (ComboBoxStyle style) {
		this.style = style;
		if (items != null) setItems(items);
	}

	public void setItems (String[] items) {
		if (items == null) throw new IllegalArgumentException("items cannot be null.");
		this.items = items;

		NinePatch background = style.background;
		BitmapFont font = style.font;

		prefHeight = Math.max(background.getTopHeight() + background.getBottomHeight() + font.getCapHeight() - font.getDescent()
			* 2, background.getTotalHeight());

		float max = 0;
		for (int i = 0; i < items.length; i++)
			max = Math.max(font.getBounds(items[i]).width, max);
		prefWidth = background.getLeftWidth() + background.getRightWidth() + max;

		width = prefWidth;
		height = prefHeight;
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

	Vector2 stageCoords = new Vector2();

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (list != null) stage.removeActor(list);
		stage.toStageCoordinates((int)screenCoords.x, (int)screenCoords.y, stageCoords);
		list = new ComboList(this.name + "-list", stageCoords.x, stageCoords.y);
		stage.addActor(list);
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		stage.getRoot().focus(list, pointer);
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
	}

	/** Defines the style of a combo box. See {@link ComboBox}
	 * @author mzechner */
	public static class ComboBoxStyle {
		public NinePatch background;
		public NinePatch listBackground;
		public NinePatch listSelection;
		public BitmapFont font;
		public Color fontColor = new Color(1, 1, 1, 1);

		public ComboBoxStyle () {
		}

		public ComboBoxStyle (BitmapFont font, Color fontColor, NinePatch background, NinePatch listBackground,
			NinePatch listSelection) {
			this.background = background;
			this.listBackground = listBackground;
			this.listSelection = listSelection;
			this.font = font;
			this.fontColor.set(fontColor);
		}
	}

	/** Interface for listening to selection events.
	 * @author mzechner */
	public interface SelectionListener {
		public void selected (ComboBox comboBox, int selectionIndex, String selection);
	}

	/** Sets the {@link SelectionListener}.
	 * @param listener the listener or null */
	public void setSelectionListener (SelectionListener listener) {
		this.listener = listener;
	}

	protected class ComboList extends Actor {
		Vector2 oldScreenCoords = new Vector2();
		float itemHeight = 0;
		float textOffsetX = 0;
		float textOffsetY = 0;
		int selected = ComboBox.this.selection;

		public ComboList (String name, float x, float y) {
			super(name);
			this.x = x;
			this.y = y;
			this.width = ComboBox.this.width;
			this.height = 100;
			this.oldScreenCoords.set(screenCoords);
			stage.getRoot().focus(this, 0);
			layout();
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
			itemHeight *= ComboBox.this.parent.scaleY;
			prefWidth += listSelection.getLeftWidth() + listSelection.getRightWidth();
			prefHeight = items.length * itemHeight;
			textOffsetX = listSelection.getLeftWidth();
			textOffsetY = listSelection.getTopHeight() + -font.getDescent();

			width = Math.max(prefWidth, ComboBox.this.width);
			width *= ComboBox.this.parent.scaleX;
			height = prefHeight;
			y -= height;
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
				font.setScale(ComboBox.this.parent.scaleX, ComboBox.this.parent.scaleY);
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
			if (items.length > 0 && listener != null) listener.selected(ComboBox.this, selected, items[selected]);
			return true;
		}

		@Override
		public void touchUp (float x, float y, int pointer) {
			stage.removeActor(this);
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
				stage.removeActor(this);
			}
		}
	}

	/** Sets the selected item via it's index
	 * @param selection the selection index */
	public void setSelection (int selection) {
		this.selection = selection;
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
}
