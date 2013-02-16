package com.badlogic.gdx.tests.bench;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer.OrthogonalTiledMapRenderer2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TiledMapBench extends GdxTest {
	
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
		camera.setToOrtho(false, (w / h) * 320, 320);
		camera.update();
		
		cameraController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(cameraController);
	
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		{
			tiles = new Texture(Gdx.files.internal("data/maps/tiles.png"));
			TextureRegion[][] splitTiles = TextureRegion.split(tiles, 32, 32);
			map = new TiledMap();
			MapLayers layers = map.getLayers();
			for (int l = 0; l < 20; l++) {
				TiledMapTileLayer layer = new TiledMapTileLayer(150, 100, 32, 32);
				for (int x = 0; x < 150; x++) {
					for (int y = 0; y < 100; y++) {
						int ty = (int)(Math.random() * splitTiles.length);
						int tx = (int)(Math.random() * splitTiles[ty].length);
						layer.setCell(x, y, new StaticTiledMapTile(splitTiles[ty][tx]));
					}
				}
				layers.addLayer(layer);
			}
		}
		
		renderer = new OrthogonalTiledMapRenderer2(map);

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		if (cameraController.dirty) {
			camera.update();
			renderer.setProjectionMatrix(camera.combined);
			cameraController.dirty = false;
			((OrthogonalTiledMapRenderer2) renderer).recache = true;
		}
		renderer.setViewBounds(camera.position.x - camera.viewportWidth * 0.5f, camera.position.y - camera.viewportHeight * 0.5f, camera.viewportWidth, camera.viewportHeight);
		renderer.begin();
		renderer.render();
		renderer.end();
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

	@Override
	public boolean needsGL20 () {
		// TODO Auto-generated method stub
		return false;
	}
	
}
