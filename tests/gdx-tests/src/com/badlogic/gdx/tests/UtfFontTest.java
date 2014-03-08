
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

/** See <a href="https://github.com/libgdx/libgdx/issues/1315">#1315</a>
 * @author badlogic */
public class UtfFontTest extends GdxTest {
	BitmapFont font;
	SpriteBatch batch;

	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/utf-font.fnt"));
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, "test ⌘", 29, 20);
		batch.end();
	}
}
