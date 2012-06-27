
package com.badlogic.gdx.scenes.scene2d.utils;

/** Base class for a drawable that stores the size information.
 * @author Nathan Sweet */
abstract public class BaseDrawable implements Drawable {
	private float leftWidth, rightWidth, topHeight, bottomHeight, minWidth, minHeight;

	public float getLeftWidth () {
		return leftWidth;
	}

	public void setLeftWidth (float leftWidth) {
		this.leftWidth = leftWidth;
	}

	public float getRightWidth () {
		return rightWidth;
	}

	public void setRightWidth (float rightWidth) {
		this.rightWidth = rightWidth;
	}

	public float getTopHeight () {
		return topHeight;
	}

	public void setTopHeight (float topHeight) {
		this.topHeight = topHeight;
	}

	public float getBottomHeight () {
		return bottomHeight;
	}

	public void setBottomHeight (float bottomHeight) {
		this.bottomHeight = bottomHeight;
	}

	public float getMinWidth () {
		return minWidth;
	}

	public void setMinWidth (float minWidth) {
		this.minWidth = minWidth;
	}

	public float getMinHeight () {
		return minHeight;
	}

	public void setMinHeight (float minHeight) {
		this.minHeight = minHeight;
	}
}
