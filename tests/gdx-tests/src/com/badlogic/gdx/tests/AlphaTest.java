
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.utils.GdxTest;

public class AlphaTest implements GdxTest {
	SpriteBatch batch;
	Texture texture;

	@Override public void dispose () {

	}

	@Override public void render () {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(texture, 0, 0, 256, 256, 0, 0, 256, 256, Color.WHITE, false, false);
		batch.end();
	}

	@Override public void surfaceChanged (int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override public void surfaceCreated () {
		Pixmap pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(0, 1, 0, 0.7f);
		pixmap.fill();

		texture = Gdx.graphics.newUnmanagedTexture(pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);
		batch = new SpriteBatch();
	}

	@Override public boolean needsGL20 () {
		return false;
	}
}
