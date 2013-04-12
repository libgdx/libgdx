package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Light;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.JsonModelLoader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class NewModelTest extends GdxTest {
	AssetManager assets;
	PerspectiveCamera cam;
	ModelBatch modelBatch;
	Model model;
	ModelInstance instance;
	ShapeRenderer shapeRenderer;
	
	Light[] lights = new Light[] {
		new Light(0.8f, 0.8f, 0.8f, 1f),
		new Light(0.2f, 0.2f, 0.2f, 1f, -1f, -2f, -3f)
	};
	
	float touchStartX = 0;
	float touchStartY = 0;
	
	@Override
	public void create () {
		if (assets == null)
			assets = new AssetManager();
		assets.load("data/g3d/head.g3dj", Model.class);
		assets.finishLoading();
		model = assets.get("data/g3d/head.g3dj", Model.class);

		instance = new ModelInstance(model);
		instance.transform.scale(5f, 5f, 5f);
		modelBatch = new ModelBatch();
		shapeRenderer = new ShapeRenderer();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0,0,0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();

		Gdx.input.setInputProcessor(new CameraInputController(cam));
	}

	final float GRID_MIN = -10f;
	final float GRID_MAX = 10f;
	final float GRID_STEP = 1f;
	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.LIGHT_GRAY);
		for (float t = GRID_MIN; t <= GRID_MAX; t+=GRID_STEP) {
			shapeRenderer.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			shapeRenderer.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.line(0, 0, 0, 100, 0, 0);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.line(0, 0, 0, 0, 100, 0);
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.line(0, 0, 0, 0, 0, 100);
		shapeRenderer.end();
		
		modelBatch.begin(cam);
		modelBatch.render(instance, lights);
		modelBatch.end();
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		assets.dispose();
		assets = null;
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
}
