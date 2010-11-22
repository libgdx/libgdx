
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.SpriteSheet;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SpriteSheetTest extends GdxTest {
	SpriteBatch batch;
	Sprite badlogic, badlogicSmall, star;
	SpriteSheet spriteSheet;

	public void create () {
		batch = new SpriteBatch();

		spriteSheet = new SpriteSheet(Gdx.files.internal("data"));
		badlogic = spriteSheet.get("badlogicslice");
		badlogicSmall = spriteSheet.get("badlogicsmall");
		star = spriteSheet.get("particle-star");

		badlogic.setPosition(50, 50);
		badlogicSmall.setPosition(10, 10);
		star.setPosition(10, 70);

		Gdx.gl.glClearColor(0, 1, 0, 1);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		badlogicSmall.draw(batch);
		badlogic.draw(batch);
		star.draw(batch);
		batch.end();
	}

	public void resize (int width, int height) {
	}

	public void pause () {
	}

	public void resume () {
	}

	public void dispose () {
		spriteSheet.dispose();
	}

	public boolean needsGL20 () {
		return false;
	}
}
