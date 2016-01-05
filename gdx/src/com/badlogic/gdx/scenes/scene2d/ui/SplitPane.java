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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A container that contains two widgets and is divided either horizontally or vertically. The user may resize the widgets. The
 * child widgets are always sized to fill their half of the splitpane.
 * <p>
 * The preferred size of a splitpane is that of the child widgets and the size of the {@link SplitPaneStyle#handle}. The widgets
 * are sized depending on the splitpane's size and the {@link #setSplitAmount(float) split position}.
 * @author mzechner
 * @author Nathan Sweet */
public class SplitPane extends WidgetGroup {
	SplitPaneStyle style;
	private Actor firstWidget, secondWidget;
	boolean vertical;
	float splitAmount = 0.5f, minAmount, maxAmount = 1;
	private float oldSplitAmount;

	private Rectangle firstWidgetBounds = new Rectangle();
	private Rectangle secondWidgetBounds = new Rectangle();
	Rectangle handleBounds = new Rectangle();
	private Rectangle firstScissors = new Rectangle();
	private Rectangle secondScissors = new Rectangle();

	Vector2 lastPoint = new Vector2();
	Vector2 handlePosition = new Vector2();

	/** @param firstWidget May be null.
	 * @param secondWidget May be null. */
	public SplitPane (Actor firstWidget, Actor secondWidget, boolean vertical, Skin skin) {
		this(firstWidget, secondWidget, vertical, skin, "default-" + (vertical ? "vertical" : "horizontal"));
	}

	/** @param firstWidget May be null.
	 * @param secondWidget May be null. */
	public SplitPane (Actor firstWidget, Actor secondWidget, boolean vertical, Skin skin, String styleName) {
		this(firstWidget, secondWidget, vertical, skin.get(styleName, SplitPaneStyle.class));
	}

	/** @param firstWidget May be null.
	 * @param secondWidget May be null. */
	public SplitPane (Actor firstWidget, Actor secondWidget, boolean vertical, SplitPaneStyle style) {
		this.firstWidget = firstWidget;
		this.secondWidget = secondWidget;
		this.vertical = vertical;
		setStyle(style);
		setFirstWidget(firstWidget);
		setSecondWidget(secondWidget);
		setSize(getPrefWidth(), getPrefHeight());
		initialize();
	}

	private void initialize () {
		addListener(new InputListener() {
			int draggingPointer = -1;

			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (draggingPointer != -1) return false;
				if (pointer == 0 && button != 0) return false;
				if (handleBounds.contains(x, y)) {
					draggingPointer = pointer;
					lastPoint.set(x, y);
					handlePosition.set(handleBounds.x, handleBounds.y);
					return true;
				}
				return false;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == draggingPointer) draggingPointer = -1;
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (pointer != draggingPointer) return;

				Drawable handle = style.handle;
				if (!vertical) {
					float delta = x - lastPoint.x;
					float availWidth = getWidth() - handle.getMinWidth();
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
					float availHeight = getHeight() - handle.getMinHeight();
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
		});
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

		Actor firstWidget = this.firstWidget;
		if (firstWidget != null) {
			Rectangle firstWidgetBounds = this.firstWidgetBounds;
			firstWidget.setBounds(firstWidgetBounds.x, firstWidgetBounds.y, firstWidgetBounds.width, firstWidgetBounds.height);
			if (firstWidget instanceof Layout) ((Layout)firstWidget).validate();
		}
		Actor secondWidget = this.secondWidget;
		if (secondWidget != null) {
			Rectangle secondWidgetBounds = this.secondWidgetBounds;
			secondWidget.setBounds(secondWidgetBounds.x, secondWidgetBounds.y, secondWidgetBounds.width, secondWidgetBounds.height);
			if (secondWidget instanceof Layout) ((Layout)secondWidget).validate();
		}
	}

	@Override
	public float getPrefWidth () {
		float first = firstWidget == null ? 0 : (firstWidget instanceof Layout ? ((Layout)firstWidget).getPrefWidth() : firstWidget
			.getWidth());
		float second = secondWidget == null ? 0 : (secondWidget instanceof Layout ? ((Layout)secondWidget).getPrefWidth()
			: secondWidget.getWidth());
		if (vertical) return Math.max(first, second);
		return first + style.handle.getMinWidth() + second;
	}

	@Override
	public float getPrefHeight () {
		float first = firstWidget == null ? 0 : (firstWidget instanceof Layout ? ((Layout)firstWidget).getPrefHeight()
			: firstWidget.getHeight());
		float second = secondWidget == null ? 0 : (secondWidget instanceof Layout ? ((Layout)secondWidget).getPrefHeight()
			: secondWidget.getHeight());
		if (!vertical) return Math.max(first, second);
		return first + style.handle.getMinHeight() + second;
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
		Drawable handle = style.handle;

		float height = getHeight();

		float availWidth = getWidth() - handle.getMinWidth();
		float leftAreaWidth = (int)(availWidth * splitAmount);
		float rightAreaWidth = availWidth - leftAreaWidth;
		float handleWidth = handle.getMinWidth();

		firstWidgetBounds.set(0, 0, leftAreaWidth, height);
		secondWidgetBounds.set(leftAreaWidth + handleWidth, 0, rightAreaWidth, height);
		handleBounds.set(leftAreaWidth, 0, handleWidth, height);
	}

	private void calculateVertBoundsAndPositions () {
		Drawable handle = style.handle;

		float width = getWidth();
		float height = getHeight();

		float availHeight = height - handle.getMinHeight();
		float topAreaHeight = (int)(availHeight * splitAmount);
		float bottomAreaHeight = availHeight - topAreaHeight;
		float handleHeight = handle.getMinHeight();

		firstWidgetBounds.set(0, height - topAreaHeight, width, topAreaHeight);
		secondWidgetBounds.set(0, 0, width, bottomAreaHeight);
		handleBounds.set(0, bottomAreaHeight, width, handleHeight);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();

		Color color = getColor();

		Drawable handle = style.handle;
		applyTransform(batch, computeTransform());
		Matrix4 transform = batch.getTransformMatrix();
		if (firstWidget != null) {
			batch.flush();
			getStage().calculateScissors(firstWidgetBounds, firstScissors);
			if (ScissorStack.pushScissors(firstScissors)) {
				if (firstWidget.isVisible()) firstWidget.draw(batch, parentAlpha * color.a);
				batch.flush();
				ScissorStack.popScissors();
			}
		}
		if (secondWidget != null) {
			batch.flush();
			getStage().calculateScissors(secondWidgetBounds, secondScissors);
			if (ScissorStack.pushScissors(secondScissors)) {
				if (secondWidget.isVisible()) secondWidget.draw(batch, parentAlpha * color.a);
				batch.flush();
				ScissorStack.popScissors();
			}
		}
		batch.setColor(color.r, color.g, color.b, parentAlpha * color.a);
		handle.draw(batch, handleBounds.x, handleBounds.y, handleBounds.width, handleBounds.height);
		resetTransform(batch);
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
		if (maxAmount > 1) throw new GdxRuntimeException("maxAmount has to be <= 1");
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

	public boolean removeActor (Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget(null).");
	}

	/** The style for a splitpane, see {@link SplitPane}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class SplitPaneStyle {
		public Drawable handle;

		public SplitPaneStyle () {
		}

		public SplitPaneStyle (Drawable handle) {
			this.handle = handle;
		}

		public SplitPaneStyle (SplitPaneStyle style) {
			this.handle = style.handle;
		}
	}
}
