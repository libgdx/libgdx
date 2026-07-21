
package com.badlogic.gdx.tests;

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
		font = new BitmapFont(files.internal("data/utf-font.fnt"));
	}

	@Override
	public void render () {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();

		// https://github.com/libgdx/libgdx/pull/6501#issuecomment-821749417
		font.draw(batch, "\u0089\u0065\u0089\u0074", 20, 400);// Missing chars should print 'et'

		font.draw(batch, "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØŒŠþÙÚÛÜÝŸ", 20, 300);
		font.draw(batch, "àáâãäåæçèéêëìíîïðñòóôõöøœšÞùúûüýÿ", 20, 200);
		font.draw(batch, "⌘¢ß¥£™©®ª×÷±²³¼½¾µ¿¶·¸º°¯§…¤¦≠¬ˆ¨‰", 20, 100);
		batch.end();
	}
}
