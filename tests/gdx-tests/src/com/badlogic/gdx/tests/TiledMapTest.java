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
import com.badlogic.gdx.maps.loaders.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TiledMapTest extends GdxTest {
	
	private TiledMap map;
	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	
	AssetManager assetManager;
	
	Texture tiles;
	
	Texture texture;
	
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
		
		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		assetManager.load("data/maps/tiles.tmx", TiledMap.class);
		assetManager.finishLoading();
		map = assetManager.get("data/maps/tiles.tmx");
		renderer = new TiledMapRenderer(map, 1f / 32f);

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();
		if (cameraController.dirty) {
			renderer.setProjectionMatrix(camera.combined);
			cameraController.dirty = false;
		}
		renderer.begin();
		renderer.render(camera);
		renderer.end();
		System.out.println("renders: " + renderer.getSpriteBatch().renderCalls);
		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20); 
		batch.end();
	}
	
	public class OrthoCamController extends InputAdapter {
		final OrthographicCamera camera;
		final Vector3 curr = new Vector3();
		final Vector3 last = new Vector3(-1, -1, -1);
		final Vector3 delta = new Vector3();

		boolean dirty = true;
		
		public OrthoCamController (OrthographicCamera camera) {
			this.camera = camera;
		}

		@Override
		public boolean touchDragged (int x, int y, int pointer) {
			camera.unproject(curr.set(x, y, 0));
			if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
				camera.unproject(delta.set(last.x, last.y, 0));
				delta.sub(curr);
				camera.position.add(delta.x, delta.y, 0);
				dirty = true;
			}
			last.set(x, y, 0);
			return false;
		}

		@Override
		public boolean touchUp (int x, int y, int pointer, int button) {
			last.set(-1, -1, -1);
			return false;
		}
	}
	
}
