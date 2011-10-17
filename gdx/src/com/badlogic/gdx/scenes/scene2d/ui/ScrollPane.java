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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/** A special container that allows scrolling over its children.
 * 
 * <h2>Functionality</h2> A ScrollPane can embed any {@link Actor} (and {@link Widget} or {@link Table} for that matter) and
 * provide scrolling functionality in case the embedded Actor is bigger than the scroll pane itself. The scroll pane will
 * automatically decide whether it needs a vertical and/or horizontal scroll handle based on the contained Actor's size with
 * respect to the scroll pane's own size.</p>
 * 
 * <b>Note: do not use any of the {@link #addActor(Actor)} or {@link #removeActor(Actor)} methods with this class! The embedded
 * widget is specified at construction time or via {@link #setWidget(Actor)}.</b> * <h2>Layout</h2> The (preferred) width and
 * height of a scroll pane is determined by the size passed to its constructor. The contained Actor will be positioned in such a
 * way that it's top left corner will coincide with the scroll pane's corner when the vertical and horizontal scroll handles are
 * at their minimum position.</p>
 * 
 * 
 * <h2>Style</h2> A ScrollPane is a {@link Group} (note the comment in the functionality section!) that conditionally displays
 * horizontal and vertical scroll bars and handles as well as the embedded Actor, clipped to the available area inside of the
 * scroll pane. The scroll bars are {@link NinePatch} instances, the scroll handles are {@link NinePatch} instances as well. In
 * addition a background {@link NinePatch} is displayed behind the embedded Actor. The style is defined via a
 * {@link ScrollPaneStyle} instance, which can be either done programmatically or via a {@link Skin}.</p>
 * 
 * The height of the horizontal scroll bar and handle is constant and takes on the maximum {@link NinePatch#getTotalHeight()}
 * value of the involed NinePatch instances. The width is determined automatically based on the size of the scroll pane.</p>
 * 
 * The width of the vertical scroll bar and handle is constant and takes on the maximum {@link NinePatch#getTotalWidth()} value of
 * the involed NinePatch instances. The height is determined automatically based on the size of the scroll pane.</p>
 * 
 * 
 * A ScrollPane's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <scrollpane name="default" 
 *             background="backgroundPatch" 
 *             hScroll="horizontalScrollBarPatch" 
 *             hScrollKnob="horizontalScrollHandlePatch" 
 *             vScroll="verticalScrollBarPatch" 
 *             vScrollKnob="verticalScrollBarHandle"/>
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newScrollPane(String, Stage, Actor, int, int, String)}.</li>
 * <li>The <code>background</code> attribute references a {@link NinePatch} by name, to be used as the scroll pane's background</li>
 * <li>The <code>hScroll</code> attribute references a {@link NinePatch} by name, to be used as the scroll pane's horizontal
 * scroll bar.</li>
 * <li>The <code>hScrollKnow</code> attribute references a {@link NinePatch} by name, to be used as the scroll pane's horizontal
 * scroll handle.</li>
 * <li>The <code>vScroll</code> attribute references a {@link NinePatch} by name, to be used as the scroll pane's vertical scroll
 * bar.</li>
 * <li>The <code>vScrollKnow</code> attribute references a {@link NinePatch} by name, to be used as the scroll pane's vertical
 * scroll handle..</li>
 * </ul>
 * 
 * @author mzechner */
public class ScrollPane extends WidgetGroup {
	protected final ScrollPaneStyle style;
	protected Actor widget;
	protected Stage stage;

	protected final Rectangle hScrollBounds = new Rectangle();
	protected final Rectangle vScrollBounds = new Rectangle();
	protected final Rectangle hScrollKnobBounds = new Rectangle();
	protected final Rectangle vScrollKnobBounds = new Rectangle();
	protected final Rectangle widgetAreaBounds = new Rectangle();
	protected final Rectangle widgetCullingArea = new Rectangle();
	protected final Rectangle scissorBounds = new Rectangle();

	protected float hScrollAmount = 0;
	protected float vScrollAmount = 0;
	protected boolean hasHScroll = false;
	protected boolean hasVScroll = false;
	protected boolean touchScrollH = false;
	protected boolean touchScrollV = false;
	protected Vector2 lastPoint = new Vector2();
	float handlePos = 0;

	public ScrollPane (Actor widget, Stage stage, Skin skin) {
		this(widget, stage, skin.getStyle(ScrollPaneStyle.class), null);
	}

	public ScrollPane (Actor widget, Stage stage, ScrollPaneStyle style) {
		this(widget, stage, style, null);
	}

	public ScrollPane (Actor widget, Stage stage, ScrollPaneStyle style, String name) {
		super(name);
		this.widget = widget;
		this.stage = stage;
		this.style = style;
		addActor(widget);
		width = 150;
		height = 150;
	}

	private void calculateBoundsAndPositions (Matrix4 batchTransform) {
		final NinePatch background = style.background;
		final NinePatch hScrollKnob = style.hScrollKnob;
		final NinePatch vScrollKnob = style.vScrollKnob;

		// Get available space size by subtracting background's padded area.
		float areaWidth = width - background.getLeftWidth() - background.getRightWidth();
		float areaHeight = height - background.getTopHeight() - background.getBottomHeight();

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

		// Check again, now taking into account the area that's taken up by any enabled scrollbars.
		if (hasVScroll && (widgetWidth > areaWidth - vScrollKnob.getTotalWidth())) {
			hasHScroll = true;
			areaWidth -= vScrollKnob.getTotalWidth();
		}
		if (hasHScroll && (widgetHeight > areaHeight - hScrollKnob.getTotalHeight())) {
			hasVScroll = true;
			areaHeight -= hScrollKnob.getTotalHeight();
		}

		// If the widget is smaller than the available space, make it take up the available space.
		widgetWidth = Math.max(areaWidth, widgetWidth);
		widgetHeight = Math.max(areaHeight, widgetHeight);
		if (widget.width != widgetWidth || widget.height != widgetHeight) {
			widget.width = widgetWidth;
			widget.height = widgetHeight;
			if (widget instanceof Layout) ((Layout)widget).invalidate();
		}

		// Set the bounds and scroll knob sizes if scrollbars are needed.
		if (hasHScroll) {
			hScrollBounds.set(background.getLeftWidth(), background.getBottomHeight(), areaWidth, hScrollKnob.getTotalHeight());
			hScrollKnobBounds.width = Math.max(hScrollKnob.getTotalWidth(), (int)(hScrollBounds.width * areaWidth / widget.width));
			hScrollKnobBounds.height = hScrollKnob.getTotalHeight();

			hScrollKnobBounds.x = hScrollBounds.x + (int)((hScrollBounds.width - hScrollKnobBounds.width) * hScrollAmount);
			hScrollKnobBounds.y = hScrollBounds.y;
		}

		if (hasVScroll) {
			vScrollBounds.set(width - background.getRightWidth() - vScrollKnob.getTotalWidth(), height - background.getTopHeight()
				- areaHeight, vScrollKnob.getTotalWidth(), areaHeight);
			vScrollKnobBounds.width = vScrollKnob.getTotalWidth();
			vScrollKnobBounds.height = Math.max(vScrollKnob.getTotalHeight(),
				(int)(vScrollBounds.height * areaHeight / widget.height));
			vScrollKnobBounds.x = vScrollBounds.x;
			vScrollKnobBounds.y = vScrollBounds.y + (int)((vScrollBounds.height - vScrollKnobBounds.height) * (1 - vScrollAmount));
		}

		// Set the widget area bounds.
		widgetAreaBounds.set(background.getLeftWidth(), background.getBottomHeight()
			+ (hasHScroll ? hScrollKnob.getTotalHeight() : 0), areaWidth, areaHeight);

		// Calculate the widgets offset depending on the scroll state and available widget area.
		widget.y = widgetAreaBounds.y - (!hasVScroll ? (int)(widget.height - areaHeight) : 0)
			- (hasVScroll ? (int)((widget.height - areaHeight) * (1 - vScrollAmount)) : 0);
		widget.x = widgetAreaBounds.x - (hasHScroll ? (int)((widget.width - areaWidth) * hScrollAmount) : 0);

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

		final NinePatch background = style.background;
		final NinePatch hScrollKnob = style.hScrollKnob;
		final NinePatch hScroll = style.hScroll;
		final NinePatch vScrollKnob = style.vScrollKnob;
		final NinePatch vScroll = style.vScroll;

		// Setup transform for this group.
		applyTransform(batch);

		// Calculate the bounds for the scrollbars, the widget area and the scissor area.
		calculateBoundsAndPositions(batch.getTransformMatrix());

		// Draw the background ninepatch.
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		background.draw(batch, 0, 0, width, height);
		batch.flush();

		// Enable scissors for widget area and draw the widget.
		if (ScissorStack.pushScissors(scissorBounds)) {
			drawChildren(batch, parentAlpha);
			ScissorStack.popScissors();
		}

		// Render scrollbars and knobs on top.
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (hasHScroll) {
			hScroll.draw(batch, hScrollBounds.x, hScrollBounds.y, hScrollBounds.width, hScrollBounds.height);
			hScrollKnob.draw(batch, hScrollKnobBounds.x, hScrollKnobBounds.y, hScrollKnobBounds.width, hScrollKnobBounds.height);
		}
		if (hasVScroll) {
			vScroll.draw(batch, vScrollBounds.x, vScrollBounds.y, vScrollBounds.width, vScrollBounds.height);
			vScrollKnob.draw(batch, vScrollKnobBounds.x, vScrollKnobBounds.y, vScrollKnobBounds.width, vScrollKnobBounds.height);
		}

		resetTransform(batch);
	}

	@Override
	public float getPrefWidth () {
		return 150;
	}

	@Override
	public float getPrefHeight () {
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

		if (hasHScroll && hScrollBounds.contains(x, y)) {
			if (hScrollKnobBounds.contains(x, y)) {
				lastPoint.set(x, y);
				handlePos = hScrollKnobBounds.x;
				touchScrollH = true;
			} else {
				if (x < hScrollKnobBounds.x) {
					hScrollAmount = Math.max(0, hScrollAmount - 0.1f);
				} else {
					hScrollAmount = Math.min(1, hScrollAmount + 0.1f);
				}
			}
			return true;
		} else if (hasVScroll && vScrollBounds.contains(x, y)) {
			if (vScrollKnobBounds.contains(x, y)) {
				lastPoint.set(x, y);
				handlePos = vScrollKnobBounds.y;
				touchScrollV = true;
			} else {
				if (y < vScrollKnobBounds.y) {
					vScrollAmount = Math.min(1, vScrollAmount + 0.1f);
				} else {
					vScrollAmount = Math.max(0, vScrollAmount - 0.1f);
				}
			}
			return true;
		} else if (widgetAreaBounds.contains(x, y)) {
			return super.touchDown(x, y, pointer);
		} else
			return false;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		if (touchScrollH || touchScrollV) {
			touchScrollH = false;
			touchScrollV = false;
			return;
		}
		if (focusedActor[pointer] != null) super.touchUp(x, y, pointer);
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		if (touchScrollH) {
			float delta = x - lastPoint.x;
			float scrollH = handlePos + delta;
			handlePos = scrollH;
			scrollH = Math.max(hScrollBounds.x, scrollH);
			scrollH = Math.min(hScrollBounds.x + hScrollBounds.width - hScrollKnobBounds.width, scrollH);
			hScrollAmount = (scrollH - hScrollBounds.x) / (hScrollBounds.width - hScrollKnobBounds.width);
			lastPoint.set(x, y);
		} else if (touchScrollV) {
			float delta = y - lastPoint.y;
			float scrollV = handlePos + delta;
			handlePos = scrollV;
			scrollV = Math.max(vScrollBounds.y, scrollV);
			scrollV = Math.min(vScrollBounds.y + vScrollBounds.height - vScrollKnobBounds.height, scrollV);
			vScrollAmount = 1 - ((scrollV - vScrollBounds.y) / (vScrollBounds.height - vScrollKnobBounds.height));
			lastPoint.set(x, y);
		} else
			super.touchDragged(x, y, pointer);
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
		if (widget == null) throw new IllegalArgumentException("widget cannot be null.");
		if (this.widget != null) removeActor(this.widget);
		this.widget = widget;
		if (widget != null) addActor(widget);
	}

	public Actor hit (float x, float y) {
		if (x > 0 && x < width && y > 0 && y < height) return super.hit(x, y);
		return null;
	}

	/** Defines a scroll pane's style, see {@link ScrollPane}.
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
