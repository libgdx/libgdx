
package com.badlogic.gdx.scenes.scene2d.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Label extends Actor {
//	public Font font;
	public String text;	

	public Label (String name, Object font, String text) {
		super(name);
//		this.font = font;
//		this.text = text;
//		this.width = font.getStringWidth(text);
//		this.height = font.getLineHeight();
	}

	@Override protected void render (SpriteBatch batch) {
//		batch.drawText(font, text, x, y, color);
	}

	@Override protected boolean touchDown (float x, float y, int pointer) {
//		return x > 0 && y > 0 && x < font.getStringWidth(text) && x < font.getLineHeight();
		return false;
	}

	@Override protected boolean touchUp (float x, float y, int pointer) {
//		return x > 0 && y > 0 && x < font.getStringWidth(text) && x < font.getLineHeight();
		return false;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
//		return x > 0 && y > 0 && x < font.getStringWidth(text) && x < font.getLineHeight();
		return false;
	}

	@Override public Actor hit (float x, float y) {
//		return x > 0 && y > 0 && x < font.getStringWidth(text) && x < font.getLineHeight() ? this : null;
		return null;
	}

}
