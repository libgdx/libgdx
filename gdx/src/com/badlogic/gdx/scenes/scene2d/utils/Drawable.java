
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Drawable {
	public void draw (SpriteBatch batch, float x, float y, float width, float height);

	public float getTopHeight ();

	public float getLeftWidth ();

	public float getBottomHeight ();

	public float getRightWidth ();

	public float getMinWidth ();

	public float getMinHeight ();
}
