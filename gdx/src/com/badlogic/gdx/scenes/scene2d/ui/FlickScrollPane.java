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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/** @author Nathan Sweet
 * @author mzechner */
public class FlickScrollPane extends WidgetGroup {
	protected final Stage stage;
	protected Actor widget;

	protected final Rectangle widgetAreaBounds = new Rectangle();
	protected final Rectangle widgetCullingArea = new Rectangle();
	protected final Rectangle scissorBounds = new Rectangle();
	protected GestureDetector gestureDetector;

	protected boolean scrollX, scrollY;
	protected float amountX, amountY;
	public float maxX, maxY;
	protected float velocityX, velocityY;
	protected float flingTimer;

	public boolean bounces = true;
	public float flingTime = 1f;
	public float bounceDistance = 50, bounceSpeedMin = 30, bounceSpeedMax = 200;
	/** When true, touch down on widgets works as normal and the FlickScrollPane can only be screen by touching down where there is
	 * no widget. */
	public boolean emptySpaceOnlyScroll;
	/** Forces the enabling of scrolling in a direction, even if the contents do not exceed the bounds in that direction. */
	public boolean forceBounceX, forceBounceY;
	/** Disables scrolling in a direction. The widget will be sized to the FlickScrollPane in the disabled direction. */
	public boolean disableX, disableY;
	/** Prevents scrolling out of the widget's bounds. */
	public boolean clamp = true;

	public FlickScrollPane (Stage stage) {
		this(null, stage, null);
	}

	public FlickScrollPane (Actor widget, Stage stage) {
		this(widget, stage, null);
	}

	public FlickScrollPane (Actor widget, Stage stage, String name) {
		super(name);

		this.stage = stage;
		this.widget = widget;
		if (widget != null) addActor(widget);

		gestureDetector = new GestureDetector(new GestureListener() {
			public boolean pan (int x, int y, int deltaX, int deltaY) {
				amountX -= deltaX;
				amountY += deltaY;
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

		width = 150;
		height = 150;
	}

	boolean tap (int x, int y) {
		focus(null, 0);
		if (!super.touchDown(x, y, 0)) return false;
		Actor actor = focusedActor[0];
		toLocalCoordinates(actor, point);
		actor.touchUp(point.x, point.y, 0);
		focus(null, 0);
		return true;
	}

	public void toLocalCoordinates (Actor actor, Vector2 point) {
		if (actor.parent == this) return;
		toLocalCoordinates(actor.parent, point);
		Group.toChildCoordinates(actor, point.x, point.y, point);
	}

	void clamp () {
		if (!clamp) return;
		if (bounces) {
			amountX = MathUtils.clamp(amountX, -bounceDistance, maxX + bounceDistance);
			amountY = MathUtils.clamp(amountY, -bounceDistance, maxY + bounceDistance);
		} else {
			amountX = MathUtils.clamp(amountX, 0, maxX);
			amountY = MathUtils.clamp(amountY, 0, maxY);
		}
	}

	public void act (float delta) {
		if (flingTimer > 0) {
			float alpha = flingTimer / flingTime;
			amountX -= velocityX * alpha * delta;
			amountY -= velocityY * alpha * delta;
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
		scrollX = !disableX && (widgetWidth > width || forceBounceX);
		scrollY = !disableY && (widgetHeight > height || forceBounceY);

		// If the widget is smaller than the available space, make it take up the available space.
		widgetWidth = Math.max(width, widgetWidth);
		boolean invalidate = false;
		if (disableX || widget.width != widgetWidth) {
			widget.width = widgetWidth;
			invalidate = true;
		}
		widgetHeight = Math.max(height, widgetHeight);
		if (disableY || widget.width != widgetWidth || widget.height != widgetHeight) {
			widget.height = widgetHeight;
			invalidate = true;
		}
		if (invalidate && widget instanceof Layout) ((Layout)widget).invalidate();

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

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		if (widget == null) return;

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

	public float getScrollPercentX () {
		return amountX / maxX;
	}

	public float getScrollPercentY () {
		return amountY / maxY;
	}

	/** Sets the {@link Actor} embedded in this scroll pane.
	 * @param widget the Actor */
	public void setWidget (Actor widget) {
		if (widget == null) throw new IllegalArgumentException("widget cannot be null.");
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
}
