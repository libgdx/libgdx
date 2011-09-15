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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/** @author Nathan Sweet
 * @author mzechner */
public class FlickScrollPane extends Group implements Layout {
	private final Stage stage;
	private Actor widget;
	private float prefWidth, prefHeight;
	protected boolean needsLayout;

	private final Rectangle widgetAreaBounds = new Rectangle();
	private final Rectangle scissorBounds = new Rectangle();
	private GestureDetector gestureDetector;

	private boolean scrollX, scrollY;
	float amountX, amountY;
	private float maxX, maxY;
	float velocityX, velocityY;
	float flingTimer;

	public boolean bounces = true;
	public float flingTime = 1f;
	public float bounceDistance = 50, bounceSpeedMin = 30, bounceSpeedMax = 200;
	public boolean emptySpaceOnlyScroll;
	public boolean forceScrollX, forceScrollY;
	public boolean clamp;

	public FlickScrollPane (Actor widget, Stage stage) {
		this(widget, stage, 0, 0, null);
	}

	public FlickScrollPane (Actor widget, Stage stage, int prefWidth, int prefHeight, String name) {
		super(name);
		this.prefWidth = this.width = prefWidth;
		this.prefHeight = this.height = prefHeight;

		this.stage = stage;
		this.widget = widget;
		if (widget != null) this.addActor(widget);

		gestureDetector = new GestureDetector(new GestureListener() {
			public boolean pan (int x, int y, int deltaX, int deltaY) {
				amountX -= deltaX;
				amountY -= deltaY;
				clamp();
				return false;
			}

			public boolean fling (float x, float y) {
				if (Math.abs(x) > 150) {
					flingTimer = flingTime;
					velocityX = x;
				}
				if (Math.abs(y) > 150) {
					flingTimer = flingTime;
					velocityY = -y;
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
		if (!clamp) return;
		if (bounces) {
			amountX = Math.max(-bounceDistance, amountX);
			amountX = Math.min(maxX + bounceDistance, amountX);
			amountY = Math.max(-bounceDistance, amountY);
			amountY = Math.min(maxY + bounceDistance, amountY);
		} else {
			amountX = Math.max(0, amountX);
			amountX = Math.min(maxX, amountX);
			amountY = Math.max(0, amountY);
			amountY = Math.min(maxY, amountY);
		}
	}

	public void act (float delta) {
		if (flingTimer > 0) {
			float alpha = flingTimer / flingTime;
			alpha = alpha * alpha * alpha;
			amountX -= velocityX * alpha * delta;
			amountY += velocityY * alpha * delta;
			clamp();

			// Stop fling if hit bounce distance.
			if (amountX == -bounceDistance) velocityX = 0;
			if (amountX >= maxX + bounceDistance) velocityX = 0;
			if (amountY == -bounceDistance) velocityY = 0;
			if (amountY >= maxY + bounceDistance) velocityY = 0;

			flingTimer -= delta;
		}

		if (bounces && !gestureDetector.isPanning()) {
			if (amountX < 0) {
				amountX += (bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * -amountX / bounceDistance) * delta;
				if (amountX > 0) amountX = 0;
			} else if (amountX > maxX) {
				amountX -= (bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * -(maxX - amountX) / bounceDistance) * delta;
				if (amountX < maxX) amountX = maxX;
			}
			if (amountY < 0) {
				amountY += (bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * -amountY / bounceDistance) * delta;
				if (amountY > 0) amountY = 0;
			} else if (amountY > maxY) {
				amountY -= (bounceSpeedMin + (bounceSpeedMax - bounceSpeedMin) * -(maxY - amountY) / bounceDistance) * delta;
				if (amountY < maxY) amountY = maxY;
			}
		}
	}

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
		scrollX = widgetWidth > width || forceScrollX;
		scrollY = widgetHeight > height || forceScrollY;

		// If the widget is smaller than the available space, make it take up the available space.
		widgetWidth = Math.max(width, widgetWidth);
		widgetHeight = Math.max(height, widgetHeight);
		if (widget.width != widgetWidth || widget.height != widgetHeight) {
			widget.width = widgetWidth;
			widget.height = widgetHeight;
			needsLayout = true;
		}

		// Set the widget area bounds.
		widgetAreaBounds.set(0, 0, width, height);

		// Calculate the widgets offset depending on the scroll state and available widget area.
		maxX = widget.width - width;
		maxY = widget.height - height;
		widget.y = -(int)(scrollY ? amountY : maxY);
		widget.x = -(int)(scrollX ? amountX : 0);

		// Caculate the scissor bounds based on the batch transform, the available widget area and the camera transform. We need to
		// project those to screen coordinates for OpenGL ES to consume.
		ScissorStack.calculateScissors(stage.getCamera(), batchTransform, widgetAreaBounds, scissorBounds);
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		if (widget == null) return;

		// Setup transform for this group.
		applyTransform(batch);

		// Calculate the bounds for the scrollbars, the widget area and the scissor area.
		calculateBoundsAndPositions(batch.getTransformMatrix()); // BOZO - Call every frame?

		if (needsLayout) layout();

		// Enable scissors for widget area and draw the widget.
		ScissorStack.pushScissors(scissorBounds);
		drawChildren(batch, parentAlpha);
		ScissorStack.popScissors();

		resetTransform(batch);
	}

	@Override
	public void layout () {
		if (!needsLayout) return;
		needsLayout = false;
		if (widget instanceof Layout) {
			Layout layout = (Layout)widget;
			layout.invalidate();
			layout.layout();
		}
	}

	@Override
	public void invalidate () {
		needsLayout = true;
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

	public void setScrollX (float pixels) {
		this.amountX = pixels;
	}

	public float getScrollX () {
		return amountX;
	}

	public void setScrollY (float pixels) {
		amountY = pixels;
	}

	public float getScrollY () {
		return amountY;
	}

	/** Sets the {@link Actor} embedded in this scroll pane.
	 * @param widget the Actor */
	public void setWidget (Actor widget) {
		if (this.widget != null) removeActor(this.widget);
		this.widget = widget;
		if (widget != null) addActor(widget);
	}

	public Actor getWidget () {
		return widget;
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
}
