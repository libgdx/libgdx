
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

/** A table that can be dragged and act as a modal window.
 * <p>
 * The preferred size of a window is the preferred size of the children as layed out by the table. After adding children to the
 * window, it can be convenient to call {@link #pack()} to size the window to the size of the children.
 * @author Nathan Sweet */
public class Window extends Table {
	private WindowStyle style;
	private String title;
	private BitmapFontCache titleCache;
	private boolean isMovable = true, isModal;
	private final Vector2 dragOffset = new Vector2();
	private boolean dragging;

	public Window (Stage stage, Skin skin) {
		this("", stage, skin.getStyle(WindowStyle.class), null);
	}

	public Window (String title, Stage stage, Skin skin) {
		this(title, stage, skin.getStyle(WindowStyle.class), null);
	}

	public Window (String title, Stage stage, WindowStyle style) {
		this(title, stage, style, null);
	}

	public Window (String title, Stage stage, WindowStyle style, String name) {
		super(null, null, name);
		if (title == null) throw new IllegalArgumentException("title cannot be null.");
		if (stage == null) throw new IllegalArgumentException("stage cannot be null.");
		this.stage = stage;
		setClip(true);
		this.title = title;
		setStyle(style);
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

	private int getTitleBarHeight () {
		return getTableLayout().getToolkit().height(getTableLayout(), getPadTop());
	}

	public void layout () {
		super.layout();
		TextBounds bounds = style.titleFont.getMultiLineBounds(title);
		titleCache.setMultiLineText(title, width / 2 - bounds.width / 2, height - getTitleBarHeight() / 2 + bounds.height / 2);
	}

	protected void drawBackground (SpriteBatch batch, float parentAlpha) {
		super.drawBackground(batch, parentAlpha);
		// Draw the title without the batch transformed or clipping applied.
		titleCache.setPosition(x, y);
		titleCache.draw(batch, parentAlpha);
	}

	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;

		// Make this window on top.
		if (parent.getActors().size() > 1) parent.swapActor(this, parent.getActors().get(parent.getActors().size() - 1));

		if (super.touchDown(x, y, pointer)) return true;

		dragging = isMovable && height - y <= getTitleBarHeight();
		dragOffset.set(x, y);
		return true;
	}

	public void touchDragged (float x, float y, int pointer) {
		if (!dragging) return;
		this.x += x - dragOffset.x;
		this.y += y - dragOffset.y;
	}

	public Actor hit (float x, float y) {
		return isModal || (x > 0 && x < width && y > 0 && y < height) ? this : null;
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

	static public class WindowStyle {
		public NinePatch background;
		public BitmapFont titleFont;
		public Color titleFontColor = new Color(1, 1, 1, 1);

		public WindowStyle () {
		}

		public WindowStyle (BitmapFont titleFont, Color titleFontColor, NinePatch backgroundPatch) {
			this.background = backgroundPatch;
			this.titleFont = titleFont;
			this.titleFontColor.set(titleFontColor);
		}
	}
}
