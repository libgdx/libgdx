package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TiledMapGroupLayerTest extends GdxTest {

	private TiledMap map;
	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	private BitmapFont font;
	private SpriteBatch batch;

	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 20, 20);
		camera.position.set(10.0f, 2.5f, 0.0f);
		camera.update();

		cameraController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(cameraController);

		font = new BitmapFont();
		batch = new SpriteBatch();

		map = new TmxMapLoader().load("data/maps/tiled/grouplayertest.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);

		testMapProperty(map.getLayers().get("group1Visible"), true, 1f, 32, 0, true);

		MapGroupLayer group1Visible = (MapGroupLayer)map.getLayers().get("group1Visible");

		testMapProperty(group1Visible.getLayers().get("group1ChildGroup"), true, 1f, 32, 0, true);
		testMapProperty(group1Visible.getLayers().get("group1ChildVisible"), true, 1f, 0, 0, false);
		testMapProperty(group1Visible.getLayers().get("group1ChildNotVisible"), false, 1f, 32, 0, false);


		testMapProperty(map.getLayers().get("group2NotVisible"), false, 1f, 0, 0, true);
		testMapProperty(map.getLayers().get("Layer1"), true, 1f, 0, 0, false);
	}

	private void testMapProperty (MapLayer mapLayer, boolean visibleState, float opacity, int offsetX, int offsetY, boolean isGroup) {
		boolean isValid = mapLayer.isVisible() == visibleState && opacity == mapLayer.getOpacity() && offsetX == mapLayer.getOffsetX() && mapLayer.getOffsetY() == mapLayer.getOffsetY();
		boolean groupInstance = mapLayer instanceof MapGroupLayer;
		if (isGroup && !groupInstance) {
			isValid = false;
		}
		if (!isGroup && groupInstance) {
			isValid = false;
		}
		if (!isValid) throw new GdxRuntimeException("Map layer " + mapLayer.getName() + " is not valid");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.55f, 0.55f, 0.55f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		map.getLayers().get("group1Visible").setOffsetX(map.getLayers().get("group1Visible").getOffsetX() + 1);

		camera.update();
		renderer.setView(camera);
		renderer.render();
	}

	@Override
	public void dispose () {
		map.dispose();
		batch.dispose();
	}

}
