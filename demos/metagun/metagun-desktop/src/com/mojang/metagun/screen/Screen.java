
package com.mojang.metagun.screen;

import java.util.Random;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.mojang.metagun.Art;
import com.mojang.metagun.Input;
import com.mojang.metagun.Metagun;

public abstract class Screen {
	protected static Random random = new Random();
	private Metagun metagun;
	public SpriteBatch spriteBatch;

	public void removed () {
		spriteBatch.dispose();
	}

	public final void init (Metagun metagun) {
		this.metagun = metagun;
		Matrix4 projection = new Matrix4();
		projection.setToOrtho(0, 320, 240, 0, -1, 1);

		spriteBatch = new SpriteBatch(100);
		spriteBatch.setProjectionMatrix(projection);
	}

	protected void setScreen (Screen screen) {
		metagun.setScreen(screen);
	}

	String[] chars = {"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", ".,!?:;\"'+-=/\\< "};

	public void draw (TextureRegion region, int x, int y) {
		int width = region.getRegionWidth();
		if(width <0) width = -width;
		spriteBatch.draw(region, x, y, width, -region.getRegionHeight());
	}

	public void drawString (String string, int x, int y) {
		string = string.toUpperCase();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			for (int ys = 0; ys < chars.length; ys++) {
				int xs = chars[ys].indexOf(ch);
				if (xs >= 0) {
					draw(Art.guys[xs][ys + 9], x + i * 6, y);
				}
			}
		}
	}

	public abstract void render ();

	public void tick (Input input) {
	}
}
