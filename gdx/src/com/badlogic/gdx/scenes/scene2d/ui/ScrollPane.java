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

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Cullable;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/** A group that scrolls a child widget using scroll bars.
 * <p>
 * The widget is sized to its preferred size. If the widget's preferred width or height is less than the size of this scroll pane,
 * it is set to the size of this scroll pane. Scrollbars appear when the widget is larger than the scroll pane.
 * <p>
 * The scroll pane's preferred size is that of the child widget. At this size, the child widget will not need to scroll, so the
 * scroll pane is typically sized by ignoring the preferred size in one or both directions.
 * @author mzechner
 * @author Nathan Sweet */
public class ScrollPane extends WidgetGroup {
	private ScrollPaneStyle style;
	private Actor widget;

	private final Rectangle hScrollBounds = new Rectangle();
	private final Rectangle vScrollBounds = new Rectangle();
	private final Rectangle hKnobBounds = new Rectangle();
	private final Rectangle vKnobBounds = new Rectangle();
	private final Rectangle widgetAreaBounds = new Rectangle();
	private final Rectangle widgetCullingArea = new Rectangle();
	private final Rectangle scissorBounds = new Rectangle();

	private boolean scrollX, scrollY;
	private float amountX, amountY;
	private boolean touchScrollH, touchScrollV;
	private final Vector2 lastPoint = new Vector2();
	private float handlePosition;
	private boolean disableX, disableY;

	public ScrollPane (Skin skin) {
		this(null, skin);
	}

	/** @param widget May be null. */
	public ScrollPane (Actor widget, Skin skin) {
		this(widget, skin.getStyle(ScrollPaneStyle.class), null);
	}

	/** @param widget May be null. */
	public ScrollPane (Actor widget, ScrollPaneStyle style) {
		this(widget, style, null);
	}

	/** @param widget May be null. */
	public ScrollPane (Actor widget, ScrollPaneStyle style, String name) {
		super(name);
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.widget = widget;
		this.style = style;
		setWidget(widget);
		width = 150;
		height = 150;
	}

	public void setStyle (ScrollPaneStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/** Returns the scroll pane's style. Modifying the returned style may not have an effect until
	 * {@link #setStyle(ScrollPaneStyle)} is called. */
	public ScrollPaneStyle getStyle () {
		return style;
	}

	private void calculateBoundsAndPositions (Matrix4 batchTransform) {
		final NinePatch bg = style.background;
		final NinePatch hScrollKnob = style.hScrollKnob;
		final NinePatch vScrollKnob = style.vScrollKnob;

		// Get available space size by subtracting background's padded area.
		float areaWidth = width - bg.getLeftWidth() - bg.getRightWidth();
		float areaHeight = height - bg.getTopHeight() - bg.getBottomHeight();

		// Get widget's desired width.
		float widgetWidth, widgetHeight;
		if (widget instanceof Layout) {
			Layout layout = (Layout)widget;
			widgetWidth = layout.getPrefWidth();
			widgetHeight = layout.getPrefHeight();
		} else {
			widgetWidth = widget.width;
			widgetHeight = widget.height;
		}

		// Figure out if we need horizontal/vertical scrollbars.
		scrollX = false;
		scrollY = false;
		if (!disableX && widgetWidth > areaWidth) scrollX = true;
		if (!disableY && widgetHeight > areaHeight) scrollY = true;

		// Check again, now taking into account the area that's taken up by any enabled scrollbars.
		if (!disableX && scrollY && widgetWidth > areaWidth - vScrollKnob.getTotalWidth()) {
			scrollX = true;
			areaWidth -= vScrollKnob.getTotalWidth();
		}
		if (!disableY && scrollX && widgetHeight > areaHeight - hScrollKnob.getTotalHeight()) {
			scrollY = true;
			areaHeight -= hScrollKnob.getTotalHeight();
		}

		// Set the widget area bounds.
		widgetAreaBounds.set(bg.getLeftWidth(), bg.getBottomHeight() + (scrollX ? hScrollKnob.getTotalHeight() : 0), areaWidth,
			areaHeight);
		amountX = MathUtils.clamp(amountX, 0, widgetAreaBounds.x);
		amountY = MathUtils.clamp(amountY, 0, widgetAreaBounds.y);

		// If the widget is smaller than the available space, make it take up the available space.
		widgetWidth = disableX ? width : Math.max(areaWidth, widgetWidth);
		widgetHeight = disableY ? height : Math.max(areaHeight, widgetHeight);
		if (widget.width != widgetWidth || widget.height != widgetHeight) {
			widget.width = widgetWidth;
			widget.height = widgetHeight;
			if (widget instanceof Layout) ((Layout)widget).invalidate();
		}

		// Set the bounds and scroll knob sizes if scrollbars are needed.
		if (scrollX) {
			hScrollBounds.set(bg.getLeftWidth(), bg.getBottomHeight(), areaWidth, hScrollKnob.getTotalHeight());
			hKnobBounds.width = Math.max(hScrollKnob.getTotalWidth(), (int)(hScrollBounds.width * areaWidth / widget.width));
			hKnobBounds.height = hScrollKnob.getTotalHeight();
			hKnobBounds.x = hScrollBounds.x + (int)((hScrollBounds.width - hKnobBounds.width) * getScrollPercentX());
			hKnobBounds.y = hScrollBounds.y;
		}
		if (scrollY) {
			vScrollBounds.set(width - bg.getRightWidth() - vScrollKnob.getTotalWidth(), height - bg.getTopHeight() - areaHeight,
				vScrollKnob.getTotalWidth(), areaHeight);
			vKnobBounds.width = vScrollKnob.getTotalWidth();
			vKnobBounds.height = Math.max(vScrollKnob.getTotalHeight(), (int)(vScrollBounds.height * areaHeight / widget.height));
			vKnobBounds.x = vScrollBounds.x;
			vKnobBounds.y = vScrollBounds.y + (int)((vScrollBounds.height - vKnobBounds.height) * (1 - getScrollPercentY()));
		}

		// Calculate the widgets offset depending on the scroll state and available widget area.
		widget.y = widgetAreaBounds.y - (!scrollY ? (int)(widget.height - areaHeight) : 0)
			- (scrollY ? (int)((widget.height - areaHeight) * (1 - getScrollPercentY())) : 0);
		widget.x = widgetAreaBounds.x - (scrollX ? (int)((widget.width - areaWidth) * getScrollPercentX()) : 0);

		// Caculate the scissor bounds based on the batch transform, the available widget area and the camera transform. We need to
		// project those to screen coordinates for OpenGL ES to consume.
		ScissorStack.calculateScissors(stage.getCamera(), batchTransform, widgetAreaBounds, scissorBounds);

		if (widget instanceof Cullable) {
			widgetCullingArea.x = -widget.x + widgetAreaBounds.x;
			widgetCullingArea.y = -widget.y + widgetAreaBounds.y;
			widgetCullingArea.width = areaWidth;
			widgetCullingArea.height = areaHeight;
			((Cullable)widget).setCullingArea(widgetCullingArea);
		}
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		if (widget == null) return;

		validate();

		// Setup transform for this group.
		applyTransform(batch);

		// Calculate the bounds for the scrollbars, the widget area and the scissor area.
		calculateBoundsAndPositions(batch.getTransformMatrix());

		// Draw the background ninepatch.
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		style.background.draw(batch, 0, 0, width, height);
		batch.flush();

		// Enable scissors for widget area and draw the widget.
		if (ScissorStack.pushScissors(scissorBounds)) {
			drawChildren(batch, parentAlpha);
			ScissorStack.popScissors();
		}

		// Render scrollbars and knobs on top.
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (scrollX) {
			style.hScroll.draw(batch, hScrollBounds.x, hScrollBounds.y, hScrollBounds.width, hScrollBounds.height);
			style.hScrollKnob.draw(batch, hKnobBounds.x, hKnobBounds.y, hKnobBounds.width, hKnobBounds.height);
		}
		if (scrollY) {
			style.vScroll.draw(batch, vScrollBounds.x, vScrollBounds.y, vScrollBounds.width, vScrollBounds.height);
			style.vScrollKnob.draw(batch, vKnobBounds.x, vKnobBounds.y, vKnobBounds.width, vKnobBounds.height);
		}

		resetTransform(batch);
	}

	public float getPrefWidth () {
		if (widget instanceof Layout) return ((Layout)widget).getPrefWidth();
		return 150;
	}

	public float getPrefHeight () {
		if (widget instanceof Layout) return ((Layout)widget).getPrefHeight();
		return 150;
	}

	public float getMinWidth () {
		return 0;
	}

	public float getMinHeight () {
		return 0;
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;

		if (scrollX && hScrollBounds.contains(x, y)) {
			if (hKnobBounds.contains(x, y)) {
				lastPoint.set(x, y);
				handlePosition = hKnobBounds.x;
				touchScrollH = true;
				return true;
			}
			if (x < hKnobBounds.x)
				setScrollPercentX(Math.max(0, getScrollPercentX() - 0.1f));
			else
				setScrollPercentX(Math.min(1, getScrollPercentX() + 0.1f));
			return false;
		} else if (scrollY && vScrollBounds.contains(x, y)) {
			if (vKnobBounds.contains(x, y)) {
				lastPoint.set(x, y);
				handlePosition = vKnobBounds.y;
				touchScrollV = true;
				return true;
			}
			if (y < vKnobBounds.y)
				setScrollPercentY(Math.max(0, getScrollPercentY() + 0.1f));
			else
				setScrollPercentY(Math.min(1, getScrollPercentY() - 0.1f));
			return false;
		} else if (widgetAreaBounds.contains(x, y)) {
			return super.touchDown(x, y, pointer);
		} else
			return false;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		touchScrollH = false;
		touchScrollV = false;
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		if (touchScrollH) {
			float delta = x - lastPoint.x;
			float scrollH = handlePosition + delta;
			handlePosition = scrollH;
			scrollH = Math.max(hScrollBounds.x, scrollH);
			scrollH = Math.min(hScrollBounds.x + hScrollBounds.width - hKnobBounds.width, scrollH);
			setScrollPercentX((scrollH - hScrollBounds.x) / (hScrollBounds.width - hKnobBounds.width));
			lastPoint.set(x, y);
		} else if (touchScrollV) {
			float delta = y - lastPoint.y;
			float scrollV = handlePosition + delta;
			handlePosition = scrollV;
			scrollV = Math.max(vScrollBounds.y, scrollV);
			scrollV = Math.min(vScrollBounds.y + vScrollBounds.height - vKnobBounds.height, scrollV);
			setScrollPercentY(1 - ((scrollV - vScrollBounds.y) / (vScrollBounds.height - vKnobBounds.height)));
			lastPoint.set(x, y);
		} else
			super.touchDragged(x, y, pointer);
	}

	/** Sets the {@link Actor} embedded in this scroll pane.
	 * @param widget the Actor */
	public void setWidget (Actor widget) {
		if (widget == null) throw new IllegalArgumentException("widget cannot be null.");
		if (this.widget != null) super.removeActor(this.widget);
		this.widget = widget;
		if (widget != null) super.addActor(widget);
	}

	public void addActor (Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	public void addActorAt (int index, Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	public void addActorBefore (Actor actorBefore, Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	public void removeActor (Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget(null).");
	}

	public void removeActorRecursive (Actor actor) {
		if (actor == widget)
			setWidget(null);
		else if (widget instanceof Group) //
			((Group)widget).removeActorRecursive(actor);
	}

	public Actor hit (float x, float y) {
		if (x > 0 && x < width && y > 0 && y < height) return super.hit(x, y);
		return null;
	}

	public void setScrollX (float pixels) {
		this.amountX = pixels;
	}

	/** Returns the x scroll position in pixels. */
	public float getScrollX () {
		return amountX;
	}

	public void setScrollY (float pixels) {
		amountY = pixels;
	}

	/** Returns the y scroll position in pixels. */
	public float getScrollY () {
		return amountY;
	}

	public float getScrollPercentX () {
		return amountX / widgetAreaBounds.x;
	}

	public void setScrollPercentX (float percentX) {
		amountX = widgetAreaBounds.x * percentX;
	}

	public float getScrollPercentY () {
		return amountY / widgetAreaBounds.y;
	}

	public void setScrollPercentY (float percentY) {
		amountY = widgetAreaBounds.y * percentY;
	}

	/** Returns the maximum scroll value in the x direction. */
	public float getMaxX () {
		return widgetAreaBounds.x;
	}

	/** Returns the maximum scroll value in the y direction. */
	public float getMaxY () {
		return widgetAreaBounds.y;
	}

	/** Disables scrolling in a direction. The widget will be sized to the FlickScrollPane in the disabled direction. */
	public void setScrollingDisabled (boolean x, boolean y) {
		disableX = x;
		disableY = y;
	}

	/** The style for a scroll pane, see {@link ScrollPane}.
	 * @author mzechner */
	static public class ScrollPaneStyle {
		public NinePatch background;
		public NinePatch hScroll;
		public NinePatch hScrollKnob;
		public NinePatch vScroll;
		public NinePatch vScrollKnob;

		public ScrollPaneStyle () {
		}

		public ScrollPaneStyle (NinePatch backgroundPatch, NinePatch hScroll, NinePatch hScrollKnob, NinePatch vScroll,
			NinePatch vScrollKnob) {
			this.background = backgroundPatch;
			this.hScroll = hScroll;
			this.hScrollKnob = hScrollKnob;
			this.vScroll = vScroll;
			this.vScrollKnob = vScrollKnob;
		}
	}
}
