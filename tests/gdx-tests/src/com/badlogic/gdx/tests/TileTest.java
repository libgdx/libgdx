package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.SpriteCache;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TileTest extends GdxTest {
	static final int LAYERS = 10;
	static final int BLOCK_TILES = 25;
	static final int WIDTH = 15;
	static final int HEIGHT = 10;
	static final int TILES_PER_LAYER = WIDTH * HEIGHT;
	SpriteCache[] caches = new SpriteCache[LAYERS];
	Texture texture;
	int[] layers = new int[LAYERS];
	OrthographicCamera cam;
	long startTime = System.nanoTime();
	
	@Override
	public void create() {
		cam = new OrthographicCamera();
		cam.setViewport(480, 320);
		cam.getPosition().set(WIDTH*32/2, 10*HEIGHT/2,0);		
		texture = Gdx.graphics.newTexture(Gdx.files.internal("data/tiles.png"), TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		
		for(int i = 0; i < LAYERS; i++) {
			caches[i] = new SpriteCache();
			SpriteCache cache = caches[i];
			cache.beginCache();
			for(int y = 0; y < HEIGHT; y++) {
				for(int x = 0; x < WIDTH; x++) {
					cache.add(texture, x << 5, y << 5, 1, 1, 32, 32, Color.WHITE);
				}
			}	
			layers[i] = cache.endCache();
		}
		
	}
	
	@Override
	public void render() {
		GL10 gl = Gdx.gl10;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.update();
		
		gl.glEnable(GL10.GL_BLEND);
		for(int i=0; i < LAYERS; i++) {
			SpriteCache cache = caches[i];			
			cache.setProjectionMatrix(cam.getCombinedMatrix());
			cache.begin();		
			for(int j = 0; j < TILES_PER_LAYER; j+=BLOCK_TILES) {
				cache.draw(layers[i], j, BLOCK_TILES);
			}
			cache.end();
		}
		
		if(System.nanoTime()-startTime>=1000000000) {
			Gdx.app.log("TileTest", "fps: " + Gdx.graphics.getFramesPerSecond());
			startTime = System.nanoTime();
		}
	}
	
	@Override
	public boolean needsGL20() {
		return false;
	}
}
