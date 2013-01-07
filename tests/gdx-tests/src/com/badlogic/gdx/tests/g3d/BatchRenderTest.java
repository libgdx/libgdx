package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.xoppa.BaseRenderBatch;
import com.badlogic.gdx.graphics.g3d.xoppa.BatchRendererGLES10;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class BatchRenderTest extends GdxTest {
	public static class ModelInstance {
		public Model model;
		public Matrix4 transform;
		public ModelInstance(Model model, Matrix4 transform) {
			this.model = model;
			this.transform = transform;
		}
	}
	PerspectiveCamera cam;
	Array<ModelInstance> models = new Array<ModelInstance>();
	RenderBatch renderBatch = new BaseRenderBatch(new BatchRendererGLES10());
	
	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};
	float touchStartX = 0;
	float touchStartY = 0;
	
	@Override
	public void create () {
		final StillModel sphereModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/sphere.obj"));
		final StillModel sceneModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/scene.obj"));
		
		models.add(new ModelInstance(sceneModel, new Matrix4()));
		for (int i = 0; i < 10; i++)
			models.add(new ModelInstance(sphereModel, (new Matrix4()).setToTranslation(-5f + (float)Math.random() * 10f, 1f + (float)Math.random() * 5f, -5f + (float)Math.random() * 10f).scl(0.05f + (float)Math.random())));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 0, 0);
		cam.update();
		Gdx.input.setInputProcessor(this);
	}
	
	@Override
	public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		renderBatch.begin(cam);
		for (int i = 0; i < models.size; i++)
			renderBatch.addModel(models.get(i).model, models.get(i).transform);
		renderBatch.end();
	}
	
	@Override
	public boolean touchDown (int x, int y, int pointer, int newParam) {
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		cam.rotateAround(Vector3.Zero, Vector3.X, (x - touchStartX));
		cam.rotateAround(Vector3.Zero, Vector3.Y, (y - touchStartY));
		touchStartX = x;
		touchStartY = y;
		cam.update();
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		cam.fieldOfView -= -amount * Gdx.graphics.getDeltaTime() * 100;
		cam.update();
		return false;
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}