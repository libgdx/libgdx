
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

/** A group with a single child that sizes and positions the child using constraints. This provides layout similar to a
 * {@link Table} with a single cell but is more lightweight.
 * @author Nathan Sweet */
public class Container extends WidgetGroup {
	private Actor widget;
	private Float minWidth, minHeight;
	private Float prefWidth, prefHeight;
	private Float maxWidth, maxHeight;
	private float padTop, padLeft, padBottom, padRight;
	private float fillX, fillY;
	private int align;
	private Drawable background;
	private boolean clip;
	private boolean round = true;

	public Container () {
		setTouchable(Touchable.childrenOnly);
		setTransform(false);
	}

	public Container (Actor widget) {
		this();
		setWidget(widget);
	}

	public void draw (Batch batch, float parentAlpha) {
		validate();
		if (widget == null) return;
		if (isTransform()) {
			applyTransform(batch, computeTransform());
			drawBackground(batch, parentAlpha, 0, 0);
			if (clip) {
				batch.flush();
				boolean draw = background == null ? clipBegin(0, 0, getWidth(), getHeight()) : clipBegin(padLeft, padBottom,
					getWidth() - padLeft - padRight, getHeight() - padBottom - padTop);
				if (draw) {
					drawChildren(batch, parentAlpha);
					clipEnd();
				}
			} else
				drawChildren(batch, parentAlpha);
			resetTransform(batch);
		} else {
			drawBackground(batch, parentAlpha, getX(), getY());
			super.draw(batch, parentAlpha);
		}
	}

	/** Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
	 * drawable. */
	protected void drawBackground (Batch batch, float parentAlpha, float x, float y) {
		if (background == null) return;
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		background.draw(batch, x, y, getWidth(), getHeight());
	}

	/** Sets the background drawable and sets the container's padding to {@link Drawable#getBottomHeight()} ,
	 * {@link Drawable#getTopHeight()}, {@link Drawable#getLeftWidth()}, and {@link Drawable#getRightWidth()}.
	 * @param background If null, the background will be cleared and all padding is removed. */
	public void setBackground (Drawable background) {
		if (this.background == background) return;
		this.background = background;
		if (background == null)
			pad(0);
		else {
			pad(background.getTopHeight(), background.getLeftWidth(), background.getBottomHeight(), background.getRightWidth());
			invalidate();
		}
	}

	/** @see #setBackground(Drawable) */
	public Container background (Drawable background) {
		setBackground(background);
		return this;
	}

	public Drawable getBackground () {
		return background;
	}

	public void layout () {
		if (widget == null) return;

		float containerWidth = getWidth() - padLeft - padRight, containerHeight = getHeight() - padTop - padBottom;
		float minWidth, minHeight, prefWidth, prefHeight, maxWidth, maxHeight;
		Layout layout = widget instanceof Layout ? (Layout)widget : null;
		if (layout != null) {
			minWidth = this.minWidth == null ? layout.getMinWidth() : this.minWidth;
			minHeight = this.minHeight == null ? layout.getMinHeight() : this.minHeight;
			prefWidth = this.prefWidth == null ? layout.getPrefWidth() : this.prefWidth;
			prefHeight = this.prefHeight == null ? layout.getPrefHeight() : this.prefHeight;
			maxWidth = this.maxWidth == null ? layout.getMaxWidth() : this.maxWidth;
			maxHeight = this.maxHeight == null ? layout.getMaxHeight() : this.maxHeight;
		} else {
			minWidth = this.minWidth == null ? widget.getWidth() : this.minWidth;
			minHeight = this.minHeight == null ? widget.getHeight() : this.minHeight;
			prefWidth = this.prefWidth == null ? widget.getWidth() : this.prefWidth;
			prefHeight = this.prefHeight == null ? widget.getHeight() : this.prefHeight;
			maxWidth = this.maxWidth == null ? widget.getWidth() : this.maxWidth;
			maxHeight = this.maxHeight == null ? widget.getHeight() : this.maxHeight;
		}

		float width;
		if (fillX > 0)
			width = containerWidth * fillX;
		else
			width = Math.min(prefWidth, containerWidth);
		if (width < minWidth) width = minWidth;
		if (maxWidth > 0 && width > maxWidth) width = maxWidth;

		float height;
		if (fillY > 0)
			height = containerHeight * fillY;
		else
			height = Math.min(prefHeight, containerHeight);
		if (height < minHeight) height = minHeight;
		if (maxHeight > 0 && height > maxHeight) height = maxHeight;

		float x = padLeft;
		if ((align & Align.right) != 0)
			x += containerWidth - width;
		else if ((align & Align.left) == 0) // center
			x += (containerWidth - width) / 2;

		float y = padBottom;
		if ((align & Align.top) != 0)
			y += containerHeight - height;
		else if ((align & Align.bottom) == 0) // center
			y += (containerHeight - height) / 2;

		if (round) {
			x = Math.round(x);
			y = Math.round(y);
			width = Math.round(width);
			height = Math.round(height);
		}

		widget.setBounds(x, y, width, height);
		if (widget instanceof Layout) ((Layout)widget).validate();
	}

	/** @param widget May be null. */
	public void setWidget (Actor widget) {
		if (widget == this) throw new IllegalArgumentException("widget cannot be the Container.");
		if (this.widget != null) super.removeActor(this.widget);
		this.widget = widget;
		if (widget != null) super.addActor(widget);
	}

	/** @return May be null. */
	public Actor getWidget () {
		return widget;
	}

	/** @deprecated Container may have only a single child.
	 * @see #setWidget(Actor) */
	public void addActor (Actor actor) {
		throw new UnsupportedOperationException("Use Container#setWidget.");
	}

	/** @deprecated Container may have only a single child.
	 * @see #setWidget(Actor) */
	public void addActorAt (int index, Actor actor) {
		throw new UnsupportedOperationException("Use Container#setWidget.");
	}

	/** @deprecated Container may have only a single child.
	 * @see #setWidget(Actor) */
	public void addActorBefore (Actor actorBefore, Actor actor) {
		throw new UnsupportedOperationException("Use Container#setWidget.");
	}

	/** @deprecated Container may have only a single child.
	 * @see #setWidget(Actor) */
	public void addActorAfter (Actor actorAfter, Actor actor) {
		throw new UnsupportedOperationException("Use Container#setWidget.");
	}

	public boolean removeActor (Actor actor) {
		if (actor != widget) return false;
		setWidget(null);
		return true;
	}

	/** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value. */
	public Container size (float size) {
		minWidth = size;
		minHeight = size;
		prefWidth = size;
		prefHeight = size;
		maxWidth = size;
		maxHeight = size;
		return this;
	}

	/** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values. */
	public Container size (float width, float height) {
		minWidth = width;
		minHeight = height;
		prefWidth = width;
		prefHeight = height;
		maxWidth = width;
		maxHeight = height;
		return this;
	}

	/** Sets the minWidth, prefWidth, and maxWidth to the specified value. */
	public Container width (float width) {
		minWidth = width;
		prefWidth = width;
		maxWidth = width;
		return this;
	}

	/** Sets the minHeight, prefHeight, and maxHeight to the specified value. */
	public Container height (float height) {
		minHeight = height;
		prefHeight = height;
		maxHeight = height;
		return this;
	}

	/** Sets the minWidth and minHeight to the specified value. */
	public Container minSize (float size) {
		minWidth = size;
		minHeight = size;
		return this;
	}

	/** Sets the minWidth and minHeight to the specified values. */
	public Container minSize (float width, float height) {
		minWidth = width;
		minHeight = height;
		return this;
	}

	public Container minWidth (float minWidth) {
		this.minWidth = minWidth;
		return this;
	}

	public Container minHeight (float minHeight) {
		this.minHeight = minHeight;
		return this;
	}

	/** Sets the prefWidth and prefHeight to the specified value. */
	public Container prefSize (float size) {
		prefWidth = size;
		prefHeight = size;
		return this;
	}

	/** Sets the prefWidth and prefHeight to the specified values. */
	public Container prefSize (float width, float height) {
		prefWidth = width;
		prefHeight = height;
		return this;
	}

	public Container prefWidth (float prefWidth) {
		this.prefWidth = prefWidth;
		return this;
	}

	public Container prefHeight (float prefHeight) {
		this.prefHeight = prefHeight;
		return this;
	}

	/** Sets the maxWidth and maxHeight to the specified value. */
	public Container maxSize (float size) {
		maxWidth = size;
		maxHeight = size;
		return this;
	}

	/** Sets the maxWidth and maxHeight to the specified values. */
	public Container maxSize (float width, float height) {
		maxWidth = width;
		maxHeight = height;
		return this;
	}

	public Container maxWidth (float maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}

	public Container maxHeight (float maxHeight) {
		this.maxHeight = maxHeight;
		return this;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
	public Container pad (float pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		return this;
	}

	public Container pad (float top, float left, float bottom, float right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		return this;
	}

	public Container padTop (float padTop) {
		this.padTop = padTop;
		return this;
	}

	public Container padLeft (float padLeft) {
		this.padLeft = padLeft;
		return this;
	}

	public Container padBottom (float padBottom) {
		this.padBottom = padBottom;
		return this;
	}

	public Container padRight (float padRight) {
		this.padRight = padRight;
		return this;
	}

	/** Sets fillX and fillY to 1. */
	public Container fill () {
		fillX = 1f;
		fillY = 1f;
		return this;
	}

	/** Sets fillX to 1. */
	public Container fillX () {
		fillX = 1f;
		return this;
	}

	/** Sets fillY to 1. */
	public Container fillY () {
		fillY = 1f;
		return this;
	}

	public Container fill (float x, float y) {
		fillX = x;
		fillY = y;
		return this;
	}

	/** Sets fillX and fillY to 1 if true, 0 if false. */
	public Container fill (boolean x, boolean y) {
		fillX = x ? 1f : 0;
		fillY = y ? 1f : 0;
		return this;
	}

	/** Sets fillX and fillY to 1 if true, 0 if false. */
	public Container fill (boolean fill) {
		fillX = fill ? 1f : 0;
		fillY = fill ? 1f : 0;
		return this;
	}

	/** Sets the alignment of the widget within the container. Set to {@link Align#center}, {@link Align#top}, {@link Align#bottom},
	 * {@link Align#left}, {@link Align#right}, or any combination of those. */
	public Container align (int align) {
		this.align = align;
		return this;
	}

	/** Sets the alignment of the widget within the container to {@link Align#center}. This clears any other alignment. */
	public Container center () {
		align = Align.center;
		return this;
	}

	/** Sets {@link Align#top} and clears {@link Align#bottom} for the alignment of the widget within the container. */
	public Container top () {
		align |= Align.top;
		align &= ~Align.bottom;
		return this;
	}

	/** Sets {@link Align#left} and clears {@link Align#right} for the alignment of the widget within the container. */
	public Container left () {
		align |= Align.left;
		align &= ~Align.right;
		return this;
	}

	/** Sets {@link Align#bottom} and clears {@link Align#top} for the alignment of the widget within the container. */
	public Container bottom () {
		align |= Align.bottom;
		align &= ~Align.top;
		return this;
	}

	/** Sets {@link Align#right} and clears {@link Align#left} for the alignment of the widget within the container. */
	public Container right () {
		align |= Align.right;
		align &= ~Align.left;
		return this;
	}

	public float getMinWidth () {
		return (minWidth == null ? (widget instanceof Layout ? ((Layout)widget).getMinWidth() : (widget != null ? widget.getWidth() : 0)) : minWidth)
			+ padLeft + padRight;
	}

	/** @return May be null if min height has not been set. */
	public Float getMinHeightValue () {
		return minHeight;
	}

	public float getMinHeight () {
		return (minHeight == null ? (widget instanceof Layout ? ((Layout)widget).getMinHeight() : (widget != null ? widget.getHeight() : 0)) : minHeight)
			+ padTop + padBottom;
	}

	/** @return May be null if pref width has not been set. */
	public Float getPrefWidthValue () {
		return prefWidth;
	}

	public float getPrefWidth () {
		float v = prefWidth == null ? (widget instanceof Layout ? ((Layout)widget).getPrefWidth() : (widget != null ? widget.getWidth() : 0)) : prefWidth;
		if (background != null) v = Math.max(v, background.getMinWidth());
		return v + padLeft + padRight;
	}

	/** @return May be null if pref height has not been set. */
	public Float getPrefHeightValue () {
		return prefHeight;
	}

	public float getPrefHeight () {
		float v = prefHeight == null ? (widget instanceof Layout ? ((Layout)widget).getPrefHeight() : (widget != null ? widget.getHeight() : 0))
			: prefHeight;
		if (background != null) v = Math.max(v, background.getMinHeight());
		return v + padTop + padBottom;
	}

	/** @return May be null if max width has not been set. */
	public Float getMaxWidthValue () {
		return maxWidth;
	}

	public float getMaxWidth () {
		float v = maxWidth == null ? (widget instanceof Layout ? ((Layout)widget).getMaxWidth() : (widget != null ? widget.getWidth() : 0)) : maxWidth;
		if (v > 0) v += padLeft + padRight;
		return v;
	}

	/** @return May be null if max height has not been set. */
	public Float getMaxHeightValue () {
		return maxHeight;
	}

	public float getMaxHeight () {
		float v = maxHeight == null ? (widget instanceof Layout ? ((Layout)widget).getMaxHeight() : (widget != null ? widget.getHeight() : 0)) : maxHeight;
		if (v > 0) v += padTop + padBottom;
		return v;
	}

	public float getPadTop () {
		return padTop;
	}

	public float getPadLeft () {
		return padLeft;
	}

	public float getPadBottom () {
		return padBottom;
	}

	public float getPadRight () {
		return padRight;
	}

	public float getFillX () {
		return fillX;
	}

	public float getFillY () {
		return fillY;
	}

	public int getAlign () {
		return align;
	}

	/** If true (the default), positions and sizes are rounded to integers. */
	public void setRound (boolean round) {
		this.round = round;
	}

	/** Causes the contents to be clipped if they exceed the container bounds. Enabling clipping will set
	 * {@link #setTransform(boolean)} to true. */
	public void setClip (boolean enabled) {
		clip = enabled;
		setTransform(enabled);
		invalidate();
	}

	public boolean getClip () {
		return clip;
	}

	public Actor hit (float x, float y, boolean touchable) {
		if (clip) {
			if (touchable && getTouchable() == Touchable.disabled) return null;
			if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return null;
		}
		return super.hit(x, y, touchable);
	}
}
