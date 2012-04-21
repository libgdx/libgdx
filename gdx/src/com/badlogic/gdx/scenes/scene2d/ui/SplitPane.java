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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A container that divides two widgets either horizontally or vertically and allows the user to resize them. The child widgets
 * are always sized to fill the half of the splitpane.
 * <p>
 * The preferred size of a splitpane is that of the child widgets and the size of the {@link SplitPaneStyle#handle}. The widgets
 * are sized depending on the splitpane's size and the {@link #setSplitAmount(float) split position}.
 * @author mzechner
 * @author Nathan Sweet */
public class SplitPane extends WidgetGroup {
	private SplitPaneStyle style;
	private Actor firstWidget, secondWidget;
	private boolean vertical;
	private float splitAmount = 0.5f, minAmount, maxAmount = 1;
	private float oldSplitAmount;
	private boolean touchDrag;

	private Rectangle firstWidgetBounds = new Rectangle();
	private Rectangle secondWidgetBounds = new Rectangle();
	private Rectangle handleBounds = new Rectangle();
	private Rectangle firstScissors = new Rectangle();
	private Rectangle secondScissors = new Rectangle();

	private Vector2 lastPoint = new Vector2();
	private Vector2 handlePosition = new Vector2();

	/** Creates a horizontal splitpane with no children. */
	public SplitPane (Skin skin) {
		this(null, null, false, skin);
	}

	/** @param firstWidget May be null.
	 * @param secondWidget May be null. */
	public SplitPane (Actor firstWidget, Actor secondWidget, boolean vertical, Skin skin) {
		this(firstWidget, secondWidget, vertical, skin.getStyle("default-horizontal", SplitPaneStyle.class), null);
	}

	/** @param firstWidget May be null.
	 * @param secondWidget May be null. */
	public SplitPane (Actor firstWidget, Actor secondWidget, boolean vertical, SplitPaneStyle style) {
		this(firstWidget, secondWidget, vertical, style, null);
	}

	/** @param firstWidget May be null.
	 * @param secondWidget May be null. */
	public SplitPane (Actor firstWidget, Actor secondWidget, boolean vertical, SplitPaneStyle style, String name) {
		super(name);
		this.firstWidget = firstWidget;
		this.secondWidget = secondWidget;
		this.vertical = vertical;
		setStyle(style);
		setFirstWidget(firstWidget);
		setSecondWidget(secondWidget);
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public void setStyle (SplitPaneStyle style) {
		this.style = style;
		invalidateHierarchy();
	}

	/** Returns the split pane's style. Modifying the returned style may not have an effect until {@link #setStyle(SplitPaneStyle)}
	 * is called. */
	public SplitPaneStyle getStyle () {
		return style;
	}

	@Override
	public void layout () {
		if (!vertical)
			calculateHorizBoundsAndPositions();
		else
			calculateVertBoundsAndPositions();

		if (firstWidget != null && firstWidget.width != firstWidgetBounds.width || firstWidget.height != firstWidgetBounds.height) {
			firstWidget.x = firstWidgetBounds.x;
			firstWidget.y = firstWidgetBounds.y;
			firstWidget.width = firstWidgetBounds.width;
			firstWidget.height = firstWidgetBounds.height;
			if (firstWidget instanceof Layout) {
				Layout layout = (Layout)firstWidget;
				layout.invalidate();
				layout.validate();
			}
		}
		if (secondWidget != null && secondWidget.width != secondWidgetBounds.width
			|| secondWidget.height != secondWidgetBounds.height) {
			secondWidget.x = secondWidgetBounds.x;
			secondWidget.y = secondWidgetBounds.y;
			secondWidget.width = secondWidgetBounds.width;
			secondWidget.height = secondWidgetBounds.height;
			if (secondWidget instanceof Layout) {
				Layout layout = (Layout)secondWidget;
				layout.invalidate();
				layout.validate();
			}
		}
	}

	@Override
	public float getPrefWidth () {
		float width = firstWidget instanceof Layout ? ((Layout)firstWidget).getPrefWidth() : firstWidget.width;
		width += secondWidget instanceof Layout ? ((Layout)secondWidget).getPrefWidth() : secondWidget.width;
		if (!vertical) width += style.handle.getTotalWidth();
		return width;
	}

	@Override
	public float getPrefHeight () {
		float height = firstWidget instanceof Layout ? ((Layout)firstWidget).getPrefHeight() : firstWidget.height;
		height += secondWidget instanceof Layout ? ((Layout)secondWidget).getPrefHeight() : secondWidget.height;
		if (vertical) height += style.handle.getTotalHeight();
		return height;
	}

	public float getMinWidth () {
		return 0;
	}

	public float getMinHeight () {
		return 0;
	}

	public void setVertical (boolean vertical) {
		this.vertical = vertical;
	}

	private void calculateHorizBoundsAndPositions () {
		NinePatch handle = style.handle;

		float availWidth = width - handle.getTotalWidth();
		float leftAreaWidth = (int)(availWidth * splitAmount);
		float rightAreaWidth = availWidth - leftAreaWidth;
		float handleWidth = handle.getTotalWidth();

		firstWidgetBounds.set(0, 0, leftAreaWidth, height);
		secondWidgetBounds.set(leftAreaWidth + handleWidth, 0, rightAreaWidth, height);
		handleBounds.set(leftAreaWidth, 0, handleWidth, height);
	}

	private void calculateVertBoundsAndPositions () {
		NinePatch handle = style.handle;

		float availHeight = height - handle.getTotalHeight();
		float topAreaHeight = (int)(availHeight * splitAmount);
		float bottomAreaHeight = availHeight - topAreaHeight;
		float handleHeight = handle.getTotalHeight();

		firstWidgetBounds.set(0, height - topAreaHeight, width, topAreaHeight);
		secondWidgetBounds.set(0, 0, width, bottomAreaHeight);
		handleBounds.set(0, bottomAreaHeight, width, handleHeight);
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();

		NinePatch handle = style.handle;
		applyTransform(batch);
		Matrix4 transform = batch.getTransformMatrix();
		if (firstWidget != null) {
			ScissorStack.calculateScissors(stage.getCamera(), transform, firstWidgetBounds, firstScissors);
			if (ScissorStack.pushScissors(firstScissors)) {
				drawChild(firstWidget, batch, parentAlpha);
				ScissorStack.popScissors();
			}
		}
		if (secondWidget != null) {
			ScissorStack.calculateScissors(stage.getCamera(), transform, secondWidgetBounds, secondScissors);
			if (ScissorStack.pushScissors(secondScissors)) {
				drawChild(secondWidget, batch, parentAlpha);
				ScissorStack.popScissors();
			}
		}
		batch.setColor(color.r, color.g, color.b, color.a);
		handle.draw(batch, handleBounds.x, handleBounds.y, handleBounds.width, handleBounds.height);
		resetTransform(batch);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (handleBounds.contains(x, y)) {
			touchDrag = true;
			lastPoint.set(x, y);
			handlePosition.set(handleBounds.x, handleBounds.y);
			return true;
		}
		return super.touchDown(x, y, pointer);
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		if (touchDrag)
			touchDrag = false;
		else
			super.touchUp(x, y, pointer);
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		if (!touchDrag) {
			super.touchDragged(x, y, pointer);
			return;
		}

		NinePatch handle = style.handle;
		if (!vertical) {
			float delta = x - lastPoint.x;
			float availWidth = width - handle.getTotalWidth();
			float dragX = handlePosition.x + delta;
			handlePosition.x = dragX;
			dragX = Math.max(0, dragX);
			dragX = Math.min(availWidth, dragX);
			splitAmount = dragX / availWidth;
			if (splitAmount < minAmount) splitAmount = minAmount;
			if (splitAmount > maxAmount) splitAmount = maxAmount;
			lastPoint.set(x, y);
		} else {
			float delta = y - lastPoint.y;
			float availHeight = height - handle.getTotalHeight();
			float dragY = handlePosition.y + delta;
			handlePosition.y = dragY;
			dragY = Math.max(0, dragY);
			dragY = Math.min(availHeight, dragY);
			splitAmount = 1 - (dragY / availHeight);
			if (splitAmount < minAmount) splitAmount = minAmount;
			if (splitAmount > maxAmount) splitAmount = maxAmount;
			lastPoint.set(x, y);
		}
		invalidate();
	}

	/** @param split The split amount between the min and max amount. */
	public void setSplitAmount (float split) {
		this.splitAmount = Math.max(Math.min(maxAmount, split), minAmount);
		invalidate();
	}

	public float getSplit () {
		return splitAmount;
	}

	public void setMinSplitAmount (float minAmount) {
		if (minAmount < 0) throw new GdxRuntimeException("minAmount has to be >= 0");
		if (minAmount >= maxAmount) throw new GdxRuntimeException("minAmount has to be < maxAmount");
		this.minAmount = minAmount;
	}

	public void setMaxSplitAmount (float maxAmount) {
		if (maxAmount > 1) throw new GdxRuntimeException("maxAmount has to be >= 0");
		if (maxAmount <= minAmount) throw new GdxRuntimeException("maxAmount has to be > minAmount");
		this.maxAmount = maxAmount;
	}

	/** @param widget May be null. */
	public void setFirstWidget (Actor widget) {
		if (firstWidget != null) super.removeActor(firstWidget);
		firstWidget = widget;
		if (widget != null) super.addActor(widget);
		invalidate();
	}

	/** @param widget May be null. */
	public void setSecondWidget (Actor widget) {
		if (secondWidget != null) super.removeActor(secondWidget);
		secondWidget = widget;
		if (widget != null) super.addActor(widget);
		invalidate();
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
		if (actor == firstWidget)
			setFirstWidget(null);
		else if (actor == firstWidget)
			setSecondWidget(null);
		else {
			if (firstWidget instanceof Group) ((Group)firstWidget).removeActorRecursive(actor);
			if (secondWidget instanceof Group) ((Group)secondWidget).removeActorRecursive(actor);
		}
	}

	/** The style for a splitpane, see {@link SplitPane}.
	 * @author mzechner */
	static public class SplitPaneStyle {
		public NinePatch handle;

		public SplitPaneStyle () {
		}

		public SplitPaneStyle (NinePatch handle) {
			this.handle = handle;
		}
		
		public SplitPaneStyle(SplitPaneStyle style) {
			this.handle = style.handle;
		}
	}
}
