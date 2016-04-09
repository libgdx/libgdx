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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

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
	boolean isMovable = true, isModal, isResizable;
	int resizeBorder = 8;
	boolean dragging;
	boolean keepWithinStage = true;
	Label titleLabel;
	Table titleTable;
	boolean drawTitleTable;

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
		setTouchable(Touchable.enabled);
		setClip(true);

		titleLabel = new Label(title, new LabelStyle(style.titleFont, style.titleFontColor));
		titleLabel.setEllipsis(true);

		titleTable = new Table() {
			public void draw (Batch batch, float parentAlpha) {
				if (drawTitleTable) super.draw(batch, parentAlpha);
			}
		};
		titleTable.add(titleLabel).expandX().fillX().minWidth(0);
		addActor(titleTable);

		setStyle(style);
		setWidth(150);
		setHeight(150);

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
					if (isResizable && x >= 0 && x < width && y >= 0 && y < height) {
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
					lastX = x - width;
					lastY = y - height;
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
					float amountX = x - lastX - width;
					if (width + amountX < minWidth) amountX = minWidth - width;
					if (clampPosition && windowX + width + amountX > stage.getWidth()) amountX = stage.getWidth() - windowX - width;
					width += amountX;
				}
				if ((edge & Align.top) != 0) {
					float amountY = y - lastY - height;
					if (height + amountY < minHeight) amountY = minHeight - height;
					if (clampPosition && windowY + height + amountY > stage.getHeight())
						amountY = stage.getHeight() - windowY - height;
					height += amountY;
				}
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
		titleLabel.setStyle(new LabelStyle(style.titleFont, style.titleFontColor));
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
		Camera camera = stage.getCamera();
		if (camera instanceof OrthographicCamera) {
			OrthographicCamera orthographicCamera = (OrthographicCamera)camera;
			float parentWidth = stage.getWidth();
			float parentHeight = stage.getHeight();
			if (getX(Align.right) - camera.position.x > parentWidth / 2 / orthographicCamera.zoom)
				setPosition(camera.position.x + parentWidth / 2 / orthographicCamera.zoom, getY(Align.right), Align.right);
			if (getX(Align.left) - camera.position.x < -parentWidth / 2 / orthographicCamera.zoom)
				setPosition(camera.position.x - parentWidth / 2 / orthographicCamera.zoom, getY(Align.left), Align.left);
			if (getY(Align.top) - camera.position.y > parentHeight / 2 / orthographicCamera.zoom)
				setPosition(getX(Align.top), camera.position.y + parentHeight / 2 / orthographicCamera.zoom, Align.top);
			if (getY(Align.bottom) - camera.position.y < -parentHeight / 2 / orthographicCamera.zoom)
				setPosition(getX(Align.bottom), camera.position.y - parentHeight / 2 / orthographicCamera.zoom, Align.bottom);
		} else if (getParent() == stage.getRoot()) {
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
		super.drawBackground(batch, parentAlpha, x, y);

		// Manually draw the title table before clipping is done.
		titleTable.getColor().a = getColor().a;
		float padTop = getPadTop(), padLeft = getPadLeft();
		titleTable.setSize(getWidth() - padLeft - getPadRight(), padTop);
		titleTable.setPosition(padLeft, getHeight() - padTop);
		drawTitleTable = true;
		titleTable.draw(batch, parentAlpha);
		drawTitleTable = false; // Avoid drawing the title table again in drawChildren.
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

	public float getPrefWidth () {
		return Math.max(super.getPrefWidth(), titleLabel.getPrefWidth() + getPadLeft() + getPadRight());
	}

	public Table getTitleTable () {
		return titleTable;
	}

	public Label getTitleLabel () {
		return titleLabel;
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
