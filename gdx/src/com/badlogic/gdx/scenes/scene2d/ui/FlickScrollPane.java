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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

public class FlickScrollPane extends Group implements Layout {
	Actor widget;
	Stage stage;
	float prefWidth;
	float prefHeight;

	Rectangle widgetAreaBounds = new Rectangle();
	Rectangle scissorBounds = new Rectangle();
	GestureDetector gestureDetector;

	float hScrollAmount = 0;
	float vScrollAmount = 0;
	boolean hasHScroll = false;
	boolean hasVScroll = false;
	Vector2 lastPoint = new Vector2();

	private boolean scrolling = false;
	private long scrollingStarted;
	private Vector2 scrollStartPoint = new Vector2();
	float velocityX, velocityY;
	float flingTime = 1f, flingTimer;
	private boolean bounces = true;
	float bounceDistance = 50, bounceSpeedMin = 30, bounceSpeedMax = 200;
	public boolean emptySpaceOnlyScroll;

	public FlickScrollPane (String name, Stage stage, Actor widget, int prefWidth, int prefHeight) {
		super(name);
		this.prefWidth = this.width = prefWidth;
		this.prefHeight = this.height = prefHeight;

		this.stage = stage;
		this.widget = widget;
		this.addActor(widget);
		layout();

		gestureDetector = new GestureDetector(new GestureListener() {
			public boolean pan (int x, int y, int deltaX, int deltaY) {
				hScrollAmount -= deltaX / (FlickScrollPane.this.widget.width - width);
				vScrollAmount += deltaY / (FlickScrollPane.this.widget.height - height);
				clamp();
				return false;
			}

			public boolean fling (float x, float y) {
				if (Math.abs(x) > 150) {
					flingTimer = flingTime;
					velocityX = x / FlickScrollPane.this.widget.width;
				}
				if (Math.abs(y) > 150) {
					flingTimer = flingTime;
					velocityY = y / FlickScrollPane.this.widget.height;
				}
				return flingTimer > 0;
			}

			public boolean touchDown (int x, int y, int pointer) {
				flingTimer = 0;
				return true;
			}

			public boolean zoom (float originalDistance, float currentDistance) {
				return false;
			}

			public boolean tap (int x, int y, int count) {
				return FlickScrollPane.this.tap(x, y);
			}

			public boolean longPress (int x, int y) {
				return false;
			}
		});
	}

	boolean tap (int x, int y) {
		focus(null, 0);
		if (!super.touchDown(x, y, 0)) return false;
		super.touchUp(x, y, 0);
		return true;
	}

	void clamp () {
		if (bounces) {
			float bounceX = bounceDistance / (widget.width - width);
			float bounceY = bounceDistance / (widget.height - height);
			hScrollAmount = Math.max(-bounceX, hScrollAmount);
			hScrollAmount = Math.min(1 + bounceX, hScrollAmount);
			vScrollAmount = Math.max(-bounceY, vScrollAmount);
			vScrollAmount = Math.min(1 + bounceY, vScrollAmount);
		} else {
			hScrollAmount = Math.max(0, hScrollAmount);
			hScrollAmount = Math.min(1, hScrollAmount);
			vScrollAmount = Math.max(0, vScrollAmount);
			vScrollAmount = Math.min(1, vScrollAmount);
		}
	}

	public void act (float delta) {
		float bounceX = bounceDistance / (widget.width - width);
		float bounceY = bounceDistance / (widget.height - height);

		if (flingTimer > 0) {
			float alpha = flingTimer / flingTime;
			alpha = alpha * alpha * alpha;
			hScrollAmount -= velocityX * alpha * delta;
			vScrollAmount += velocityY * alpha * delta;
			clamp();

			// Stop fling if hit bounce distance.
			if (hScrollAmount == -bounceX) velocityX = 0;
			if (hScrollAmount >= 1 + bounceX) velocityX = 0;
			if (vScrollAmount == -bounceY) velocityY = 0;
			if (vScrollAmount >= 1 + bounceY) velocityY = 0;

			flingTimer -= delta;
		}

		if (bounces && !gestureDetector.isPanning()) {
			if (hScrollAmount < 0) {
				float overscrollPercent = -hScrollAmount / bounceX;
				float bouncePixels = bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * overscrollPercent;
				float bouncePercent = bouncePixels / (widget.width - width);
				hScrollAmount += bouncePercent * delta;
				if (hScrollAmount > 0) hScrollAmount = 0;
			} else if (hScrollAmount > 1) {
				float overscrollPercent = -(1 - hScrollAmount) / bounceX;
				float bouncePixels = bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * overscrollPercent;
				float bouncePercent = bouncePixels / (widget.width - width);
				hScrollAmount -= bouncePercent * delta;
				if (hScrollAmount < 1) hScrollAmount = 1;
			}
			if (vScrollAmount < 0) {
				float overscrollPercent = -vScrollAmount / bounceY;
				float bouncePixels = bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * overscrollPercent;
				float bouncePercent = bouncePixels / (widget.height - height);
				vScrollAmount += bouncePercent * delta;
				if (vScrollAmount > 0) vScrollAmount = 0;
			} else if (vScrollAmount > 1) {
				float overscrollPercent = -(1 - vScrollAmount) / bounceY;
				float bouncePixels = bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * overscrollPercent;
				float bouncePercent = bouncePixels / (widget.height - height);
				vScrollAmount -= bouncePercent * delta;
				if (vScrollAmount < 1) vScrollAmount = 1;
			}
		}
	}

	private void calculateBoundsAndPositions (Matrix4 batchTransform) {
		// Get available space size by subtracting background's padded area.
		float areaWidth = width;
		float areaHeight = height;

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
		hasHScroll = false;
		hasVScroll = false;
		if (widgetWidth > areaWidth) hasHScroll = true;
		if (widgetHeight > areaHeight) hasVScroll = true;

		// If the widget is smaller than the available space, make it take up the available space.
		widgetWidth = Math.max(areaWidth, widgetWidth);
		widgetHeight = Math.max(areaHeight, widgetHeight);
		if (widget.width != widgetWidth || widget.height != widgetHeight) {
			widget.width = widgetWidth;
			widget.height = widgetHeight;
			if (widget instanceof Layout) {
				Layout layout = (Layout)widget;
				layout.invalidate();
				layout.layout();
			}
		}

		// Set the widget area bounds.
		widgetAreaBounds.set(0, 0, areaWidth, areaHeight);

		// Calculate the widgets offset depending on the scroll state and available widget area.
		widget.y = widgetAreaBounds.y - (!hasVScroll ? (int)(widget.height - areaHeight) : 0)
			- (hasVScroll ? (int)((widget.height - areaHeight) * (1 - vScrollAmount)) : 0);
		widget.x = widgetAreaBounds.x - (hasHScroll ? (int)((widget.width - areaWidth) * hScrollAmount) : 0);

		// Caculate the scissor bounds based on the batch transform, the available widget area and the camera transform. We need to
		// project those to screen coordinates for OpenGL ES to consume.
		ScissorStack.calculateScissors(stage.getCamera(), batchTransform, widgetAreaBounds, scissorBounds);
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		// Setup transform for this group.
		applyTransform(batch);

		// Calculate the bounds for the scrollbars, the widget area and the scissor area.
		calculateBoundsAndPositions(batch.getTransformMatrix());

		// Enable scissors for widget area and draw the widget.
		ScissorStack.pushScissors(scissorBounds);
		drawChildren(batch, parentAlpha);
		ScissorStack.popScissors();

		resetTransform(batch);
	}

	@Override
	public void layout () {
	}

	@Override
	public void invalidate () {
	}

	@Override
	public float getPrefWidth () {
		return prefWidth;
	}

	@Override
	public float getPrefHeight () {
		return prefHeight;
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (emptySpaceOnlyScroll && super.touchDown(x, y, pointer)) return true;
		return gestureDetector.touchDown((int)x, (int)y, pointer, 0);
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		clamp();
		gestureDetector.touchUp((int)x, (int)y, pointer, 0);
		if (focusedActor[pointer] != null) super.touchUp(x, y, pointer);
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		gestureDetector.touchDragged((int)x, (int)y, pointer);
		super.touchDragged(x, y, pointer);
	}

	@Override
	public Actor hit (float x, float y) {
		return x > 0 && x < width && y > 0 && y < height ? this : null;
	}

	public void setVScrollAmount (float vScrollAmount) {
		this.vScrollAmount = vScrollAmount;
	}

	public void setHScrollAmount (float hScrollAmount) {
		this.hScrollAmount = hScrollAmount;
	}

	/** Sets the {@link Actor} embedded in this scroll pane.
	 * @param widget the Actor */
	public void setWidget (Actor widget) {
		if (widget == null) throw new IllegalArgumentException("widget must not be null.");
		this.removeActor(this.widget);
		this.widget = widget;
		this.addActor(widget);
		invalidate();
	}
}
