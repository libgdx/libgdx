
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class AccelerometerTest extends GdxTest {
	@Override
	public boolean needsGL20 () {
		return false;
	}

	BitmapFont font;
	SpriteBatch batch;

	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLine(batch, "accel: [" + Gdx.input.getAccelerometerX() + "," + Gdx.input.getAccelerometerY() + ","
			+ Gdx.input.getAccelerometerZ() + "]\n" + "orientation: " + Gdx.input.getNativeOrientation() + "\n" + "rotation: "
			+ Gdx.input.getRotation() + "\n" + "wh: " + Gdx.graphics.getDesktopDisplayMode() + "\n", 0, 100);
		batch.end();
	}
}
