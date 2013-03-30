package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.JsonModelLoader;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.graphics.g3d.test.TestShader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class NewModelTest extends GdxTest {
	PerspectiveCamera cam;
	ModelBatch modelBatch;
	Model model;
	ModelInstance instance;
	ShapeRenderer shapeRenderer;
	
	Light[] lights = new Light[] {
		new Light(Color.WHITE, Vector3.tmp.set(-10f, 10f, -10f), 15f),
		new Light(Color.BLUE, Vector3.tmp.set(10f, 5f, 0f), 10f),
		new Light(Color.GREEN, Vector3.tmp.set(0f, 10f, 5f), 5f)
	};
	
	float touchStartX = 0;
	float touchStartY = 0;
	
	@Override
	public void create () {
		JsonModelLoader loader = new JsonModelLoader();
		model = new Model(loader.parseModel(Gdx.files.internal("data/g3d/cubes.g3dj")));
		instance = new ModelInstance(model);
		modelBatch = new ModelBatch();
		TestShader.ignoreUnimplemented = true;
		shapeRenderer = new ShapeRenderer();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.direction.set(-1, -1, -1);
		cam.near = 0.1f;
		cam.far = 300f;
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		cam.update();
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.line(0, 0, 0, 100, 0, 0);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.line(0, 0, 0, 0, 100, 0);
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.line(0, 0, 0, 0, 0, 100);
		shapeRenderer.end();

		instance.transform.idt();
		instance.transform.translate(0, 0, 3);
		
		modelBatch.begin(cam);
		modelBatch.render(instance, lights);
		modelBatch.end();
	}
	
	@Override
	public void dispose () {
		model.dispose();
		modelBatch.dispose();
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int newParam) {
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		cam.rotateAround(new Vector3(), Vector3.X, y - touchStartY);
		cam.rotateAround(new Vector3(), Vector3.Y, x - touchStartX);
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		cam.fieldOfView -= -amount * Gdx.graphics.getDeltaTime() * 100;
		return false;
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
}
