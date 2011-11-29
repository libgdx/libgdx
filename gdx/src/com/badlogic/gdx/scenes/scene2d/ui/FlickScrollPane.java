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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Interpolation;
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

/** A group that scrolls a child widget by pressing and dragging.
 * <p>
 * The widget is sized to its preferred size. If the widget's preferred width or height is less than the size of this scroll pane,
 * it is set to the size of this scroll pane.
 * <p>
 * The scroll pane's preferred size is that of the child widget. At this size, the child widget will not need to scroll, so the
 * scroll pane is typically sized by ignoring the preferred size in one or both directions.
 * @author Nathan Sweet
 * @author mzechner */
public class FlickScrollPane extends WidgetGroup {
	private Actor widget;

	private final Rectangle widgetAreaBounds = new Rectangle();
	private final Rectangle widgetCullingArea = new Rectangle();
	private final Rectangle scissorBounds = new Rectangle();
	private final GestureDetector gestureDetector;

	private boolean scrollX, scrollY;
	float amountX, amountY;
	private float maxX, maxY;
	float velocityX, velocityY;
	float flingTimer;
	private Actor touchFocusedChild;

	private boolean overscroll = true;
	float flingTime = 1f;
	private float overscrollDistance = 50, overscrollSpeedMin = 30, overscrollSpeedMax = 200;
	private Interpolation overscrollInterpolation = Interpolation.elasticOut;
	private boolean emptySpaceOnlyScroll;
	private boolean forceOverscrollX, forceOverscrollY;
	private boolean disableX, disableY;
	private boolean clamp = true;

	public FlickScrollPane () {
		this(null, null);
	}

	/** @param widget May be null. */
	public FlickScrollPane (Actor widget) {
		this(widget, null);
	}

	/** @param widget May be null. */
	public FlickScrollPane (Actor widget, String name) {
		super(name);
		this.widget = widget;
		if (widget != null) setWidget(widget);

		gestureDetector = new GestureDetector(new GestureListener() {
			public boolean pan (int x, int y, int deltaX, int deltaY) {
				amountX -= deltaX;
				amountY += deltaY;
				clamp();
				cancelTouchFocusedChild();
				return true;
			}

			public boolean fling (float x, float y) {
				if (Math.abs(x) > 150) {
					flingTimer = flingTime;
					velocityX = x;
					cancelTouchFocusedChild();
				}
				if (Math.abs(y) > 150) {
					flingTimer = flingTime;
					velocityY = -y;
					cancelTouchFocusedChild();
				}
				return flingTimer > 0;
			}

			public boolean touchDown (int x, int y, int pointer) {
				flingTimer = 0;
				return true;
			}

			public boolean tap (int x, int y, int count) {
				return false;
			}

			public boolean zoom (float originalDistance, float currentDistance) {
				return false;
			}

			public boolean longPress (int x, int y) {
				return false;
			}
		});

		width = 150;
		height = 150;
	}

	void clamp () {
		if (!clamp) return;
		if (overscroll) {
			amountX = MathUtils.clamp(amountX, -overscrollDistance, maxX + overscrollDistance);
			amountY = MathUtils.clamp(amountY, -overscrollDistance, maxY + overscrollDistance);
		} else {
			amountX = MathUtils.clamp(amountX, 0, maxX);
			amountY = MathUtils.clamp(amountY, 0, maxY);
		}
	}

	public void act (float delta) {
		super.act(delta);

		if (flingTimer > 0) {
			float alpha = flingTimer / flingTime;
			amountX -= velocityX * alpha * delta;
			amountY -= velocityY * alpha * delta;
			clamp();

			// Stop fling if hit overscroll distance.
			if (amountX == -overscrollDistance) velocityX = 0;
			if (amountX >= maxX + overscrollDistance) velocityX = 0;
			if (amountY == -overscrollDistance) velocityY = 0;
			if (amountY >= maxY + overscrollDistance) velocityY = 0;

			flingTimer -= delta;
		}

		if (overscroll && !gestureDetector.isPanning()) {
			if (amountX < 0) {
				amountX += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountX / overscrollDistance) * delta;
				if (amountX > 0) amountX = 0;
			} else if (amountX > maxX) {
				amountX -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -(maxX - amountX) / overscrollDistance)
					* delta;
				if (amountX < maxX) amountX = maxX;
			}
			if (amountY < 0) {
				amountY += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountY / overscrollDistance) * delta;
				if (amountY > 0) amountY = 0;
			} else if (amountY > maxY) {
				amountY -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -(maxY - amountY) / overscrollDistance)
					* delta;
				if (amountY < maxY) amountY = maxY;
			}
		}
	}

	// BOZO - Use layout.
	private void calculateBoundsAndPositions (Matrix4 batchTransform) {
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

		// Figure out if we need horizontal/vertical scrollbars,
		scrollX = !disableX && (widgetWidth > width || forceOverscrollX);
		scrollY = !disableY && (widgetHeight > height || forceOverscrollY);

		// If the widget is smaller than the available space, make it take up the available space.
		widgetWidth = disableX ? width : Math.max(width, widgetWidth);
		widgetHeight = disableY ? height : Math.max(height, widgetHeight);
		if (widget.width != widgetWidth || widget.height != widgetHeight) {
			widget.width = widgetWidth;
			widget.height = widgetHeight;
			if (widget instanceof Layout) ((Layout)widget).invalidate();
		}

		// Calculate the widgets offset depending on the scroll state and available widget area.
		maxX = widget.width - width;
		maxY = widget.height - height;
		widget.y = (int)(scrollY ? amountY : maxY) - widget.height + height;
		widget.x = -(int)(scrollX ? amountX : 0);

		// Caculate the scissor bounds based on the batch transform, the available widget area and the camera transform. We need to
		// project those to screen coordinates for OpenGL ES to consume.
		widgetAreaBounds.set(0, 0, width, height);
		ScissorStack.calculateScissors(stage.getCamera(), batchTransform, widgetAreaBounds, scissorBounds);

		if (widget instanceof Cullable) {
			widgetCullingArea.x = -widget.x;
			widgetCullingArea.y = -widget.y;
			widgetCullingArea.width = width;
			widgetCullingArea.height = height;
			((Cullable)widget).setCullingArea(widgetCullingArea);
		}
	}

	public void layout () {
		if (widget instanceof Layout) {
			Layout layout = (Layout)widget;
			layout.invalidate();
			layout.validate();
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

		// Enable scissors for widget area and draw the widget.
		if (ScissorStack.pushScissors(scissorBounds)) {
			drawChildren(batch, parentAlpha);
			ScissorStack.popScissors();
		}

		resetTransform(batch);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		super.touchDown(x, y, pointer);
		touchFocusedChild = stage.getTouchFocus(0) != this ? stage.getTouchFocus(0) : null;
		gestureDetector.touchDown((int)x, (int)y, pointer, 0);
		if (stage != null) stage.setTouchFocus(this, 0); // Always take the touch focus.
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		clamp();
		if (gestureDetector.touchUp((int)x, (int)y, pointer, 0)) {
			x = Integer.MIN_VALUE;
			y = Integer.MIN_VALUE;
		} else if (touchFocusedChild != null) {
			point.x = x;
			point.y = y;
			toLocalCoordinates(touchFocusedChild, point);
			touchFocusedChild.touchUp(point.x, point.y, 0);
		}
	}

	void cancelTouchFocusedChild () {
		if (touchFocusedChild == null) return;
		touchFocusedChild.touchUp(Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
		touchFocusedChild = null;
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		gestureDetector.touchDragged((int)x, (int)y, pointer);
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
		return amountX / maxX;
	}

	public void setScrollPercentX (float percentX) {
		amountX = maxX * percentX;
	}

	public float getScrollPercentY () {
		return amountY / maxY;
	}

	public void setScrollPercentY (float percentY) {
		amountY = maxY * percentY;
	}

	/** Returns the maximum scroll value in the x direction. */
	public float getMaxX () {
		return maxX;
	}

	/** Returns the maximum scroll value in the y direction. */
	public float getMaxY () {
		return maxY;
	}

	/** Sets the {@link Actor} embedded in this scroll pane.
	 * @param widget May be null. */
	public void setWidget (Actor widget) {
		if (widget == null) throw new IllegalArgumentException("widget cannot be null.");
		if (this.widget != null) super.removeActor(this.widget);
		this.widget = widget;
		if (widget != null) super.addActor(widget);
	}

	public Actor getWidget () {
		return widget;
	}

	public void addActor (Actor actor) {
		throw new UnsupportedOperationException("Use FlickScrollPane#setWidget.");
	}

	public void addActorAt (int index, Actor actor) {
		throw new UnsupportedOperationException("Use FlickScrollPane#setWidget.");
	}

	public void addActorBefore (Actor actorBefore, Actor actor) {
		throw new UnsupportedOperationException("Use FlickScrollPane#setWidget.");
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

	public boolean isPanning () {
		return gestureDetector.isPanning();
	}

	public float getVelocityX () {
		if (flingTimer <= 0) return 0;
		float alpha = flingTimer / flingTime;
		alpha = alpha * alpha * alpha;
		return velocityX * alpha * alpha * alpha;
	}

	public float getVelocityY () {
		return velocityY;
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

	public Actor hit (float x, float y) {
		if (x > 0 && x < width && y > 0 && y < height) return super.hit(x, y);
		return null;
	}

	/** If true, the widget can be scrolled slightly past its bounds and will animate back to its bounds when scrolling is stopped.
	 * Default is true. */
	public void setOverscroll (boolean overscroll) {
		this.overscroll = overscroll;
	}

	/** Sets the overscroll distance in pixels and the speed it returns to the widgets bounds in seconds. Default is 50, 30, 200. */
	public void setupOverscroll (float distance, float speedMin, float speedMax) {
		overscrollDistance = distance;
		overscrollSpeedMin = speedMin;
		overscrollSpeedMax = speedMax;
	}

	/** Forces the enabling of overscrolling in a direction, even if the contents do not exceed the bounds in that direction. */
	public void setForceOverscroll (boolean x, boolean y) {
		forceOverscrollX = x;
		forceOverscrollY = y;
	}

	/** Sets the amount of time in seconds that a fling will continue to scroll. Default is 1. */
	public void setFlingTime (float flingTime) {
		this.flingTime = flingTime;
	}

	/** If true, only pressing and dragging on empty space in the FlickScrollPane will cause a scroll and widgets receive touch down
	 * events as normal. If false, pressing and dragging anywhere will trigger a scroll and widgets will only receive touch down
	 * events if pressed and released without dragging. Default is false. */
	public void setEmptySpaceOnlyScroll (boolean emptySpaceOnlyScroll) {
		this.emptySpaceOnlyScroll = emptySpaceOnlyScroll;
	}

	/** Disables scrolling in a direction. The widget will be sized to the FlickScrollPane in the disabled direction. */
	public void setScrollingDisabled (boolean x, boolean y) {
		disableX = x;
		disableY = y;
	}

	/** Prevents scrolling out of the widget's bounds. Default is true. */
	public void setClamp (boolean clamp) {
		this.clamp = clamp;
	}
}
