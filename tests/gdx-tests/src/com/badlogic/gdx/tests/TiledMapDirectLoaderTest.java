package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer.IsometricTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer.OrthogonalTiledMapRenderer2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;

public class TiledMapDirectLoaderTest extends GdxTest {
	
	private TiledMap map;
	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	
	BitmapFont font;
	SpriteBatch batch;
	
	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 10, 10);
		camera.update();
		
		cameraController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(cameraController);
	
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		map = new TmxMapLoader().load("data/maps/tiles.tmx");
		renderer = new OrthogonalTiledMapRenderer2(map, 1f / 32f);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.55f, 0.55f, 0.55f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();
		renderer.setProjectionMatrix(camera.combined);
		renderer.setViewBounds(camera.position.x - camera.viewportWidth * 0.5f, camera.position.y - camera.viewportHeight * 0.5f, camera.viewportWidth, camera.viewportHeight);
		renderer.begin();
		renderer.render();
		renderer.end();
		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20); 
		batch.end();
	}
	
	@Override
	public boolean needsGL20 () {
		return true;
	}

	@Override
	public void dispose () {
		map.dispose();
	}
}
