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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/**
 * A special container that allows scrolling over its children, supporting scrolling by click/dragging anywhere and flick
 * gestures.
 * 
 * <h2>Functionality</h2> A ScrollPane can embed any {@link Actor} (and {@link Widget} or {@link Container} for that matter) and
 * provide scrolling functionality in case the embedded Actor is bigger than the scroll pane itself. The scroll pane will
 * automatically decide whether it needs a vertical and/or horizontal scroll handle based on the contained Actor's size with
 * respect to the scroll pane's own size.</p>
 * 
 * <b>Note: do not use any of the {@link #addActor(Actor)} or {@link #removeActor(Actor)} methods with this class! The embedded
 * widget is specified at construction time or via {@link #setWidget(Actor)}.</b>
 * 
 * <h2>Layout</h2> The (preferred) width and height of a scroll pane is determined by the size passed to its constructor. The
 * contained Actor will be positioned in such a way that it's top left corner will coincide with the scroll pane's corner when the
 * vertical and horizontal scroll handles are at their minimum position.</p>
 * 
 * <h2>Style</h2> A ScrollPane is a {@link Group} (note the comment in the functionality section!) that displays the embedded
 * Actor, clipped to the available area inside of the scroll pane.</p>
 * 
 * @author mzechner
 * @author Nathan Sweet
 */
public class FlickScrollPane extends Group implements Layout {
	Actor widget;
	Stage stage;
	float prefWidth;
	float prefHeight;

	boolean invalidated = true;

	Rectangle widgetAreaBounds = new Rectangle();
	Rectangle scissorBounds = new Rectangle();

	float hScrollAmount = 0;
	float vScrollAmount = 0;
	boolean hasHScroll = false;
	boolean hasVScroll = false;
	boolean touchScroll = false;
	Vector2 lastPoint = new Vector2();

	public FlickScrollPane (String name, Stage stage, Actor widget, int prefWidth, int prefHeight) {
		super(name);
		this.prefWidth = this.width = prefWidth;
		this.prefHeight = this.height = prefHeight;

		this.stage = stage;
		this.widget = widget;
		this.addActor(widget);
		layout();
	}

	Vector3 tmp = new Vector3();

	private void calculateBoundsAndPositions (Matrix4 batchTransform) {
		// get available space size by subtracting background's
		// padded area
		hasHScroll = false;
		hasVScroll = false;

		// Figure out if we need horizontal/vertical scrollbars,
		if (widget.width > width) hasHScroll = true;
		if (widget.height > height) hasVScroll = true;

		// Set the widget area bounds
		widgetAreaBounds.set(0, 0, width, height);

		// Calculate the widgets offset depending on the scroll state and
		// available widget area.
		widget.y = -(!hasVScroll ? (int)(widget.height - height) : 0)
			- (hasVScroll ? (int)((widget.height - height) * (1 - vScrollAmount)) : 0);
		widget.x = -(hasHScroll ? (int)((widget.width - width) * hScrollAmount) : 0);

		// Caculate the scissor bounds based on the batch transform,
		// the available widget area and the camera transform. We
		// need to project those to screen coordinates for OpenGL ES
		// to consume. This is pretty freaking nasty...
		ScissorStack.calculateScissors(stage.getCamera(), batchTransform, widgetAreaBounds, scissorBounds);
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		// setup transform for this group
		setupTransform(batch);

		// if invalidated layout!
		if (invalidated) layout();

		// calculate the bounds for the scrollbars, the widget
		// area and the scissor area. Nasty...
		calculateBoundsAndPositions(batch.getTransformMatrix());

		// enable scissors for widget area and draw that damn
		// widget. Nasty #2
		ScissorStack.pushScissors(scissorBounds);
		drawChildren(batch, parentAlpha);
		ScissorStack.popScissors();

		resetTransform(batch);
	}

	@Override
	public void layout () {
		if (widget instanceof Layout) {
			Layout layout = (Layout)widget;
			widget.width = Math.max(width, layout.getPrefWidth());
			widget.height = Math.max(height, layout.getPrefHeight());
			layout.invalidate();
			layout.layout();
		}
		invalidated = false;
	}

	@Override
	public void invalidate () {
		if (widget instanceof Layout) ((Layout)widget).invalidate();
		invalidated = true;
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
	protected boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		lastPoint.set(x, y);
		touchScroll = true;
		focus(this, 0);
		return true;
	}

	@Override
	protected boolean touchUp (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (touchScroll) {
			focus(null, 0);
			touchScroll = false;
			return true;
		} else
			return super.touchUp(x, y, pointer);
	}

	@Override
	protected boolean touchDragged (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (touchScroll) {
			if (hasHScroll) {
				hScrollAmount -= (x - lastPoint.x) / (widget.width - width);
				hScrollAmount = Math.max(0, hScrollAmount);
				hScrollAmount = Math.min(1, hScrollAmount);
			}

			if (hasVScroll) {
				vScrollAmount += (y - lastPoint.y) / (widget.height - height);
				vScrollAmount = Math.max(0, vScrollAmount);
				vScrollAmount = Math.min(1, vScrollAmount);
			}

			lastPoint.set(x, y);
			return true;
		} else
			return super.touchDragged(x, y, pointer);
	}

	@Override
	public Actor hit (float x, float y) {
		return x > 0 && x < width && y > 0 && y < height ? this : null;
	}

	/**
	 * Sets the {@link Actor} embedded in this scroll pane.
	 * @param widget the Actor
	 */
	public void setWidget (Actor widget) {
		if (widget == null) throw new IllegalArgumentException("widget must not be null");
		this.removeActor(this.widget);
		this.widget = widget;
		this.addActor(widget);
		invalidate();
	}
}
