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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/** A table that can be dragged and act as a modal window. The top padding is used as the window's title height.
 * <p>
 * The preferred size of a window is the preferred size of the title text and the children as layed out by the table. After adding
 * children to the window, it can be convenient to call {@link #pack()} to size the window to the size of the children.
 * @author Nathan Sweet */
public class Window extends Table {
	static private final Vector2 tmpPosition = new Vector2();
	static private final Vector2 tmpSize = new Vector2();

	private WindowStyle style;
	private String title;
	private BitmapFontCache titleCache;
	boolean isMovable = true, isModal;
	final Vector2 dragOffset = new Vector2();
	boolean dragging;
	private int titleAlignment = Align.center;
	boolean keepWithinStage = true;

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
		setTitle(title);

		addCaptureListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				toFront();
				return false;
			}
		});
		addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (button == 0) {
					dragging = isMovable && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth();
					dragOffset.set(x, y);
				}
				return dragging || isModal;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (dragging) dragging = false;
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (!dragging) return;
				translate(x - dragOffset.x, y - dragOffset.y);
			}

			public boolean mouseMoved (InputEvent event, float x, float y) {
				return isModal;
			}

			public boolean scrolled (InputEvent event, float x, float y, int amount) {
				return isModal;
			}

			public boolean keyDown (InputEvent event, int keycode) {
				return isModal;
			}

			public boolean keyUp (InputEvent event, int keycode) {
				return isModal;
			}

			public boolean keyTyped (InputEvent event, char character) {
				return isModal;
			}
		});
	}

	public void setStyle (WindowStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		setBackground(style.background);
		titleCache = new BitmapFontCache(style.titleFont);
		titleCache.setColor(style.titleFontColor);
		if (title != null) setTitle(title);
		invalidateHierarchy();
	}

	/** Returns the window's style. Modifying the returned style may not have an effect until {@link #setStyle(WindowStyle)} is
	 * called. */
	public WindowStyle getStyle () {
		return style;
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		Stage stage = getStage();
		if (keepWithinStage && getParent() == stage.getRoot()) {
			float parentWidth = stage.getWidth();
			float parentHeight = stage.getHeight();
			if (getX() < 0) setX(0);
			if (getRight() > parentWidth) setX(parentWidth - getWidth());
			if (getY() < 0) setY(0);
			if (getTop() > parentHeight) setY(parentHeight - getHeight());
		}
		super.draw(batch, parentAlpha);
	}

	protected void drawBackground (SpriteBatch batch, float parentAlpha) {
		if (style.stageBackground != null) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			Stage stage = getStage();
			stageToLocalCoordinates(/* in/out */tmpPosition.set(0, 0));
			stageToLocalCoordinates(/* in/out */tmpSize.set(stage.getWidth(), stage.getHeight()));
			style.stageBackground
				.draw(batch, getX() + tmpPosition.x, getY() + tmpPosition.y, getX() + tmpSize.x, getY() + tmpSize.y);
		}

		super.drawBackground(batch, parentAlpha);
		// Draw the title without the batch transformed or clipping applied.
		float x = getX(), y = getY() + getHeight();
		TextBounds bounds = titleCache.getBounds();
		if ((titleAlignment & Align.left) != 0)
			x += getPadLeft();
		else if ((titleAlignment & Align.right) != 0)
			x += getWidth() - bounds.width - getPadRight();
		else
			x += (getWidth() - bounds.width) / 2;
		if ((titleAlignment & Align.top) == 0) {
			if ((titleAlignment & Align.bottom) != 0)
				y -= getPadTop() - bounds.height;
			else
				y -= (getPadTop() - bounds.height) / 2;
		}
		titleCache.setColor(Color.tmp.set(getColor()).mul(style.titleFontColor));
		titleCache.setPosition((int)x, (int)y);
		titleCache.draw(batch, parentAlpha);
	}

	public Actor hit (float x, float y, boolean touchable) {
		Actor hit = super.hit(x, y, touchable);
		if (hit == null && isModal && (!touchable || getTouchable() == Touchable.enabled)) return this;
		return hit;
	}

	public void setTitle (String title) {
		this.title = title;
		titleCache.setMultiLineText(title, 0, 0);
	}

	public String getTitle () {
		return title;
	}

	/** @param titleAlignment {@link Align} */
	public void setTitleAlignment (int titleAlignment) {
		this.titleAlignment = titleAlignment;
	}

	public void setMovable (boolean isMovable) {
		this.isMovable = isMovable;
	}

	public void setModal (boolean isModal) {
		this.isModal = isModal;
	}

	public void setKeepWithinStage (boolean keepWithinStage) {
		this.keepWithinStage = keepWithinStage;
	}

	public boolean isDragging () {
		return dragging;
	}

	public float getPrefWidth () {
		return Math.max(super.getPrefWidth(), titleCache.getBounds().width + getPadLeft() + getPadRight());
	}

	/** The style for a window, see {@link Window}.
	 * @author Nathan Sweet */
	static public class WindowStyle {
		/** Optional. */
		public Drawable background;
		public BitmapFont titleFont;
		/** Optional. */
		public Color titleFontColor = new Color(1, 1, 1, 1);
		/** Optional. */
		public Drawable stageBackground;

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
