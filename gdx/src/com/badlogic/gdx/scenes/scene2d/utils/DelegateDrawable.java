
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.Batch;

/** Drawable that wraps another drawable.
 * @author Nathan Sweet */
public class DelegateDrawable implements Drawable {
	Drawable drawable;

	public DelegateDrawable () {
	}

	public DelegateDrawable (Drawable drawable) {
		this.drawable = drawable;
	}

	public Drawable getDrawable () {
		return drawable;
	}

	public void setDrawable (Drawable drawable) {
		this.drawable = drawable;
	}

	public void draw (Batch batch, float x, float y, float width, float height) {
		drawable.draw(batch, x, y, width, height);
	}

	public float getLeftWidth () {
		return drawable.getLeftWidth();
	}

	public void setLeftWidth (float leftWidth) {
		drawable.setLeftWidth(leftWidth);
	}

	public float getRightWidth () {
		return drawable.getRightWidth();
	}

	public void setRightWidth (float rightWidth) {
		drawable.setRightWidth(rightWidth);
	}

	public float getTopHeight () {
		return drawable.getTopHeight();
	}

	public void setTopHeight (float topHeight) {
		drawable.setTopHeight(topHeight);
	}

	public float getBottomHeight () {
		return drawable.getBottomHeight();
	}

	public void setBottomHeight (float bottomHeight) {
		drawable.setBottomHeight(bottomHeight);
	}

	public float getMinWidth () {
		return drawable.getMinWidth();
	}

	public void setMinWidth (float minWidth) {
		drawable.setMinWidth(minWidth);
	}

	public float getMinHeight () {
		return drawable.getMinHeight();
	}

	public void setMinHeight (float minHeight) {
		drawable.setMinHeight(minHeight);
	}
}
