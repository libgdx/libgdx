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
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/** A table that can be dragged and act as a modal window.
 * <p>
 * The preferred size of a window is the preferred size of the children as layed out by the table. After adding children to the
 * window, it can be convenient to call {@link #pack()} to size the window to the size of the children.
 * @author Nathan Sweet */
public class Window extends Table {
	private WindowStyle style;
	private String title;
	private BitmapFontCache titleCache;
	boolean isMovable = true, isModal;
	final Vector2 dragOffset = new Vector2();
	boolean dragging;

	public Window (String title, Skin skin) {
		this(title, skin.get(WindowStyle.class));
		setSkin(skin);
	}

	public Window (String title, Skin skin, String styleName) {
		this(title, skin.get(styleName, WindowStyle.class));
		setSkin(skin);
	}

	public Window (String title, WindowStyle style) {
		if (title == null) throw new IllegalArgumentException("title cannot be null.");
		this.title = title;
		setTouchable(Touchable.enabled);
		setClip(true);
		setStyle(style);
		setWidth(150);
		setHeight(150);

		addCaptureListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0) toFront();
				return false;
			}
		});
		addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0) return false;
				dragging = isMovable && getHeight() - y <= getTitleBarHeight() && y < getHeight() && x > 0 && x < getWidth();
				dragOffset.set(x, y);
				return dragging;
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (!dragging) return;
				translate(x - dragOffset.x, y - dragOffset.y);
			}
		});
	}

	public void setStyle (WindowStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		setBackground(style.background);
		titleCache = new BitmapFontCache(style.titleFont);
		titleCache.setColor(style.titleFontColor);
		invalidateHierarchy();
	}

	/** Returns the window's style. Modifying the returned style may not have an effect until {@link #setStyle(WindowStyle)} is
	 * called. */
	public WindowStyle getStyle () {
		return style;
	}

	float getTitleBarHeight () {
		return getPadTop().height(this);
	}

	public void layout () {
		super.layout();
		TextBounds bounds = style.titleFont.getMultiLineBounds(title);
		titleCache.setMultiLineText(title, getWidth() / 2 - bounds.width / 2, getHeight() - getTitleBarHeight() / 2 + bounds.height
			/ 2);
	}

	protected void drawBackground (SpriteBatch batch, float parentAlpha) {
		super.drawBackground(batch, parentAlpha);
		// Draw the title without the batch transformed or clipping applied.
		titleCache.setPosition(getX(), getY());
		titleCache.draw(batch, parentAlpha);
	}

	public Actor hit (float x, float y) {
		Actor hit = super.hit(x, y);
		if (hit == null && isModal) return this;
		return hit;
	}

	public void setTitle (String title) {
		this.title = title;
	}

	public String getTitle () {
		return title;
	}

	public void setMovable (boolean isMovable) {
		this.isMovable = isMovable;
	}

	public void setModal (boolean isModal) {
		this.isModal = isModal;
	}

	public boolean isDragging () {
		return dragging;
	}

	/** The style for a window, see {@link Window}.
	 * @author Nathan Sweet */
	static public class WindowStyle {
		public Drawable background;
		public BitmapFont titleFont;
		public Color titleFontColor = new Color(1, 1, 1, 1);

		public WindowStyle () {
		}

		public WindowStyle (BitmapFont titleFont, Color titleFontColor, Drawable background) {
			this.background = background;
			this.titleFont = titleFont;
			this.titleFontColor.set(titleFontColor);
		}

		public WindowStyle (WindowStyle style) {
			this.background = style.background;
			this.titleFont = style.titleFont;
			this.titleFontColor = new Color(style.titleFontColor);
		}
	}
}
