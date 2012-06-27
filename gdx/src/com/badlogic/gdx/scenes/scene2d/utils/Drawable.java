
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** A drawable knows how to draw itself at a given rectangular size. It provides border sizes and a minimum size so that other code
 * can determine how to size and position content.
 * @author Nathan Sweet */
public interface Drawable {
	public void draw (SpriteBatch batch, float x, float y, float width, float height);

	public float getTopHeight ();

	public float getLeftWidth ();

	public float getBottomHeight ();

	public float getRightWidth ();

	public float getMinWidth ();

	public float getMinHeight ();
}
