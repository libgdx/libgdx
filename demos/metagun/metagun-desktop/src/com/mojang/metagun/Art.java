
package com.mojang.metagun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Art {
	public static TextureRegion[][] guys;
	public static TextureRegion[][] player1;
	public static TextureRegion[][] player2;
	public static TextureRegion[][] walls;
	public static TextureRegion[][] gremlins;
	public static TextureRegion bg;
	public static Pixmap level;
	public static TextureRegion titleScreen;
	public static TextureRegion shot;
	public static TextureRegion[][] buttons;

	public static TextureRegion winScreen1;
	public static TextureRegion winScreen2;

	public static void load () {
		bg = load("res/background.png", 320, 240);
		level = new Pixmap(Gdx.files.internal("res/levels.png"));
		titleScreen = load("res/titlescreen.png", 320, 740);
		guys = split("res/guys.png", 6, 6);
		player1 = split("res/player.png", 16, 32);
		player2 = split("res/player.png", 16, 32, true);
		walls = split("res/walls.png", 10, 10);
		gremlins = split("res/gremlins.png", 30, 30);
		buttons = split("res/buttons.png", 32, 32);
		shot = new TextureRegion(guys[0][0].getTexture(), 3, 27, 2, 2);
		winScreen1 = load("res/winscreen1.png", 320, 240);
		winScreen2 = load("res/winscreen2.png", 320, 240);
	}

	private static TextureRegion[][] split (String name, int width, int height) {
		return split(name, width, height, false);
	}

	private static TextureRegion[][] split (String name, int width, int height, boolean flipX) {
		Texture texture = new Texture(Gdx.files.internal(name));
		int xSlices = texture.getWidth() / width;
		int ySlices = texture.getHeight() / height;
		TextureRegion[][] res = new TextureRegion[xSlices][ySlices];
		for (int x = 0; x < xSlices; x++) {
			for (int y = 0; y < ySlices; y++) {
				res[x][y] = new TextureRegion(texture, x * width, y * height, width, height);
				res[x][y].flip(flipX, true);
			}
		}
		return res;
	}

	public static TextureRegion load (String name, int width, int height) {
		Texture texture = new Texture(Gdx.files.internal(name));
		TextureRegion region = new TextureRegion(texture, 0, 0, width, height);
		region.flip(false, true);
		return region;
	}

// private static BufferedImage scale (BufferedImage src, int scale) {
// int w = src.getWidth() * scale;
// int h = src.getHeight() * scale;
// BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
// Graphics g = res.getGraphics();
// g.drawImage(src.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING), 0, 0, null);
// g.dispose();
// return res;
// }
//
// private static BufferedImage[][] mirrorsplit (BufferedImage src, int xs, int ys) {
// int xSlices = src.getWidth() / xs;
// int ySlices = src.getHeight() / ys;
// BufferedImage[][] res = new BufferedImage[xSlices][ySlices];
// for (int x = 0; x < xSlices; x++) {
// for (int y = 0; y < ySlices; y++) {
// res[x][y] = new BufferedImage(xs, ys, BufferedImage.TYPE_INT_ARGB);
// Graphics g = res[x][y].getGraphics();
// g.drawImage(src, xs, 0, 0, ys, x * xs, y * ys, (x + 1) * xs, (y + 1) * ys, null);
// g.dispose();
// }
// }
// return res;
// }

// private static BufferedImage[][] split(BufferedImage src, int xs, int ys) {
// int xSlices = src.getWidth() / xs;
// int ySlices = src.getHeight() / ys;
// BufferedImage[][] res = new BufferedImage[xSlices][ySlices];
// for (int x = 0; x < xSlices; x++) {
// for (int y = 0; y < ySlices; y++) {
// res[x][y] = new BufferedImage(xs, ys, BufferedImage.TYPE_INT_ARGB);
// Graphics g = res[x][y].getGraphics();
// g.drawImage(src, -x * xs, -y * ys, null);
// g.dispose();
// }
// }
// return res;
// }
}
