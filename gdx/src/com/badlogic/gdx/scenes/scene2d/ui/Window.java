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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
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
 * The preferred size of a window is the preferred size of the title text and the children as laid out by the table. After adding
 * children to the window, it can be convenient to call {@link #pack()} to size the window to the size of the children.
 * @author Nathan Sweet */
public class Window extends Table {
	static private final Vector2 tmpPosition = new Vector2();
	static private final Vector2 tmpSize = new Vector2();
	static private final int MOVE = 1 << 5;

	private WindowStyle style;
	private String title;
	private BitmapFontCache titleCache;
	boolean isMovable = true, isModal, isResizable;
	int resizeBorder = 8;
	boolean dragging;
	private int titleAlignment = Align.center;
	boolean keepWithinStage = true;
	Table buttonTable;

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

		buttonTable = new Table();
		addActor(buttonTable);

		addCaptureListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				toFront();
				return false;
			}
		});
		addListener(new InputListener() {
			int edge;
			float startX, startY, lastX, lastY;

			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (button == 0) {
					int border = resizeBorder;
					float width = getWidth(), height = getHeight();
					edge = 0;
					if (isResizable) {
						if (x < border) edge |= Align.left;
						if (x > width - border) edge |= Align.right;
						if (y < border) edge |= Align.bottom;
						if (y > height - border) edge |= Align.top;
						if (edge != 0) border += 25;
						if (x < border) edge |= Align.left;
						if (x > width - border) edge |= Align.right;
						if (y < border) edge |= Align.bottom;
						if (y > height - border) edge |= Align.top;
					}
					if (isMovable && edge == 0 && y <= height && y >= height - getPadTop() && x >= 0 && x <= width) edge = MOVE;
					dragging = edge != 0;
					startX = x;
					startY = y;
					lastX = x;
					lastY = y;
				}
				return edge != 0 || isModal;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				dragging = false;
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (!dragging) return;
				float width = getWidth(), height = getHeight();
				float windowX = getX(), windowY = getY();

				float minWidth = getMinWidth(), maxWidth = getMaxWidth();
				float minHeight = getMinHeight(), maxHeight = getMaxHeight();
				Stage stage = getStage();
				boolean clampPosition = keepWithinStage && getParent() == stage.getRoot();

				if ((edge & MOVE) != 0) {
					float amountX = x - startX, amountY = y - startY;
					windowX += amountX;
					windowY += amountY;
				}
				if ((edge & Align.left) != 0) {
					float amountX = x - startX;
					if (width - amountX < minWidth) amountX = -(minWidth - width);
					if (clampPosition && windowX + amountX < 0) amountX = -windowX;
					width -= amountX;
					windowX += amountX;
				}
				if ((edge & Align.bottom) != 0) {
					float amountY = y - startY;
					if (height - amountY < minHeight) amountY = -(minHeight - height);
					if (clampPosition && windowY + amountY < 0) amountY = -windowY;
					height -= amountY;
					windowY += amountY;
				}
				if ((edge & Align.right) != 0) {
					float amountX = x - lastX;
					if (width + amountX < minWidth) amountX = minWidth - width;
					if (clampPosition && windowX + width + amountX > stage.getWidth()) amountX = stage.getWidth() - windowX - width;
					width += amountX;
				}
				if ((edge & Align.top) != 0) {
					float amountY = y - lastY;
					if (height + amountY < minHeight) amountY = minHeight - height;
					if (clampPosition && windowY + height + amountY > stage.getHeight())
						amountY = stage.getHeight() - windowY - height;
					height += amountY;
				}
				lastX = x;
				lastY = y;
				setBounds(Math.round(windowX), Math.round(windowY), Math.round(width), Math.round(height));
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

	void keepWithinStage () {
		if (!keepWithinStage) return;
		Stage stage = getStage();
		if (getParent() == stage.getRoot()) {
			float parentWidth = stage.getWidth();
			float parentHeight = stage.getHeight();
			if (getX() < 0) setX(0);
			if (getRight() > parentWidth) setX(parentWidth - getWidth());
			if (getY() < 0) setY(0);
			if (getTop() > parentHeight) setY(parentHeight - getHeight());
		}
	}

	public void draw (Batch batch, float parentAlpha) {
		Stage stage = getStage();
		if (stage.getKeyboardFocus() == null) stage.setKeyboardFocus(this);

		keepWithinStage();

		if (style.stageBackground != null) {
			stageToLocalCoordinates(tmpPosition.set(0, 0));
			stageToLocalCoordinates(tmpSize.set(stage.getWidth(), stage.getHeight()));
			drawStageBackground(batch, parentAlpha, getX() + tmpPosition.x, getY() + tmpPosition.y, getX() + tmpSize.x, getY()
				+ tmpSize.y);
		}

		super.draw(batch, parentAlpha);
	}

	protected void drawStageBackground (Batch batch, float parentAlpha, float x, float y, float width, float height) {
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		style.stageBackground.draw(batch, x, y, width, height);
	}

	protected void drawBackground (Batch batch, float parentAlpha, float x, float y) {
		float width = getWidth(), height = getHeight();
		float padTop = getPadTop();

		super.drawBackground(batch, parentAlpha, x, y);

		// Draw button table.
		buttonTable.getColor().a = getColor().a;
		buttonTable.pack();
		buttonTable.setPosition(width - buttonTable.getWidth(), Math.min(height - padTop, height - buttonTable.getHeight()));
		buttonTable.draw(batch, parentAlpha);

		// Draw the title without the batch transformed or clipping applied.
		y += height;
		TextBounds bounds = titleCache.getBounds();
		if ((titleAlignment & Align.left) != 0)
			x += getPadLeft();
		else if ((titleAlignment & Align.right) != 0)
			x += width - bounds.width - getPadRight();
		else
			x += (width - bounds.width) / 2;
		if ((titleAlignment & Align.top) == 0) {
			if ((titleAlignment & Align.bottom) != 0)
				y -= padTop - bounds.height;
			else
				y -= (padTop - bounds.height) / 2;
		}
		titleCache.tint(Color.tmp.set(getColor()).mul(style.titleFontColor));
		titleCache.setPosition((int)x, (int)y);
		titleCache.draw(batch, parentAlpha);
	}

	public Actor hit (float x, float y, boolean touchable) {
		Actor hit = super.hit(x, y, touchable);
		if (hit == null && isModal && (!touchable || getTouchable() == Touchable.enabled)) return this;
		float height = getHeight();
		if (hit == null || hit == this) return hit;
		if (y <= height && y >= height - getPadTop() && x >= 0 && x <= getWidth()) {
			// Hit the title bar, don't use the hit child if it is in the Window's table.
			Actor current = hit;
			while (current.getParent() != this)
				current = current.getParent();
			if (getCell(current) != null) return this;
		}
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

	public boolean isMovable () {
		return isMovable;
	}

	public void setMovable (boolean isMovable) {
		this.isMovable = isMovable;
	}

	public boolean isModal () {
		return isModal;
	}

	public void setModal (boolean isModal) {
		this.isModal = isModal;
	}

	public void setKeepWithinStage (boolean keepWithinStage) {
		this.keepWithinStage = keepWithinStage;
	}

	public boolean isResizable () {
		return isResizable;
	}

	public void setResizable (boolean isResizable) {
		this.isResizable = isResizable;
	}

	public void setResizeBorder (int resizeBorder) {
		this.resizeBorder = resizeBorder;
	}

	public boolean isDragging () {
		return dragging;
	}

	public float getTitleWidth () {
		return titleCache.getBounds().width;
	}

	public float getPrefWidth () {
		return Math.max(super.getPrefWidth(), getTitleWidth() + getPadLeft() + getPadRight());
	}

	public Table getButtonTable () {
		return buttonTable;
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
