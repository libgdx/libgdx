
package com.badlogic.gdx.tests;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;

public class IsometricTileTest extends GdxTest {
	static final int LAYERS = 1;
	static final int WIDTH = 4;
	static final int HEIGHT = 5;
	static final int TILES_PER_LAYER = WIDTH * HEIGHT;
	static final int TILE_WIDTH = 54;
	static final int TILE_HEIGHT = 54;
	static final int TILE_HEIGHT_DIAMOND = 28;
	static final int BOUND_X = HEIGHT * TILE_WIDTH / 2 + WIDTH * TILE_WIDTH / 2;
	static final int BOUND_Y = HEIGHT * TILE_HEIGHT_DIAMOND / 2 + WIDTH * TILE_HEIGHT_DIAMOND / 2;

	Texture texture;
	SpriteCache[] caches = new SpriteCache[LAYERS];
	int[] layers = new int[LAYERS];
	OrthographicCamera cam;
	OrthoCamController camController;
	ImmediateModeRenderer renderer;
	long startTime = System.nanoTime();

	@Override public void create () {
		cam = new OrthographicCamera();
		cam.setViewport(480, 320);
		camController = new OrthoCamController(cam);
		Gdx.input.setInputProcessor(camController);

		renderer = new ImmediateModeRenderer();
		texture = new Texture(Gdx.files.internal("data/isotile.png"));

		Random rand = new Random();
		for (int i = 0; i < LAYERS; i++) {
			caches[i] = new SpriteCache();
			SpriteCache cache = caches[i];
			cache.beginCache();

			int colX = HEIGHT * TILE_WIDTH / 2 - TILE_WIDTH / 2;
			int colY = BOUND_Y - TILE_HEIGHT_DIAMOND;
			for (int x = 0; x < WIDTH; x++) {
				for (int y = 0; y < HEIGHT; y++) {
					int tileX = colX - y * TILE_WIDTH / 2;
					int tileY = colY - y * TILE_HEIGHT_DIAMOND / 2;
					cache.add(texture, tileX, tileY, rand.nextInt(2) * 54, 0, TILE_WIDTH, TILE_HEIGHT);
				}
				colX += TILE_WIDTH / 2;
				colY -= TILE_HEIGHT_DIAMOND / 2;
			}

			layers[i] = cache.endCache();
		}
	}

	@Override public void render () {
		GL10 gl = Gdx.gl10;
		gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.update();

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		for (int i = 0; i < LAYERS; i++) {
			SpriteCache cache = caches[i];
			cache.setProjectionMatrix(cam.getCombinedMatrix());
			cache.begin();
			cache.draw(layers[i]);
			cache.end();
		}

		renderer.begin(GL10.GL_LINES);
		renderer.color(1, 0, 0, 1);
		renderer.vertex(0, 0, 0);
		renderer.color(1, 0, 0, 1);
		renderer.vertex(500, 0, 0);
		renderer.color(0, 1, 0, 1);
		renderer.vertex(0, 0, 0);
		renderer.color(0, 1, 0, 1);
		renderer.vertex(0, 500, 0);

		renderer.color(0, 0, 1, 1);
		renderer.vertex(0, BOUND_Y, 0);
		renderer.color(0, 0, 1, 1);
		renderer.vertex(BOUND_X, BOUND_Y, 0);

		renderer.color(0, 0, 1, 1);
		renderer.vertex(BOUND_X, 0, 0);
		renderer.color(0, 0, 1, 1);
		renderer.vertex(BOUND_X, BOUND_Y, 0);

		renderer.end();
	}

	@Override public boolean needsGL20 () {
		return false;
	}
}
