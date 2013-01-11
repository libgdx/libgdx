package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.loaders.json.JsonModelLoader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderBatch;
import com.badlogic.gdx.graphics.g3d.xoppa.test.OldBatchRendererGLES11;
import com.badlogic.gdx.graphics.g3d.xoppa.test.OldBatchRendererGLES20;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.ExclusiveTextures;
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
	Array<ModelInstance> instances = new Array<ModelInstance>();
	Array<Model> models = new Array<Model>();
	Array<Texture> textures = new Array<Texture>();
	RenderBatch renderBatch;
	OldBatchRendererGLES20 renderer;
	ExclusiveTextures exclusiveTextures;
	
	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};
	float touchStartX = 0;
	float touchStartY = 0;
	
	@Override
	public void create () {
		final JsonModelLoader loader = new JsonModelLoader();
		
		float MIN_X = -100f, MIN_Y = -100f, MIN_Z = -100f;
		float SIZE_X = 200f, SIZE_Y = 200f, SIZE_Z = 200f;
		// need more higher resolution textures for this test...
		String[] TEXTURES = {"data/badlogic.jpg", "data/egg.png", "data/particle-fire.png", "data/planet_earth.png", "data/planet_heavyclouds.jpg",
			"data/resource1.jpg", "data/stones.jpg", "data/sys.png", "data/wheel.png"};
		int TEXTURE_COUNT = 30;
		int BOX_COUNT = 500;
		int UNIT_OFFSET = 2;
		int MAX_TEXTURES = Math.min(8 /*GL10.GL_MAX_TEXTURE_UNITS*/ - UNIT_OFFSET, ExclusiveTextures.MAX_GLES_UNITS - UNIT_OFFSET);
		int BIND_METHOD = ExclusiveTextures.WEIGHTED;
		
		for (int i = 0; i < TEXTURE_COUNT; i++)
			textures.add(new Texture(Gdx.files.internal(TEXTURES[i%TEXTURES.length])));
		
		final StillModel sphereModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/sphere.obj"));
		final StillModel sceneModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/scene.obj"));
		final StillModel cubeModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/cube.obj"));
		final Model testModel = loader.load(Gdx.files.internal("data/g3d/test.g3dj"), null);
		
		StillSubMesh mesh = (StillSubMesh)(cubeModel.subMeshes[0]);
		for (int i = 0; i < textures.size; i++)
			models.add(new StillModel(new StillSubMesh(mesh.name, mesh.mesh, mesh.primitiveType, new Material("mat", new TextureAttribute(textures.get(i), 0, "")))));
		
		/*instances.add(new ModelInstance(sceneModel, new Matrix4()));
		instances.add(new ModelInstance(testModel, (new Matrix4()).setToTranslation(0, 2, 2)));
		for (int i = 0; i < 10; i++)
			instances.add(new ModelInstance(sphereModel, (new Matrix4()).setToTranslation(MIN_X + (float)Math.random() * SIZE_X, MIN_Y + (float)Math.random() * SIZE_Y, MIN_Z + (float)Math.random() * SIZE_Z).scl(0.05f + (float)Math.random())));
		*/
		
		for (int i = 0; i < BOX_COUNT; i++)
			instances.add(new ModelInstance(models.get((int)(Math.random()*models.size)), (new Matrix4()).setToTranslation(MIN_X + (float)Math.random() * SIZE_X, MIN_Y + (float)Math.random() * SIZE_Y, MIN_Z + (float)Math.random() * SIZE_Z).scl(0.05f + (float)Math.random())));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 0, 0);
		cam.update();
		
		renderBatch = new RenderBatch(exclusiveTextures = new ExclusiveTextures(BIND_METHOD, UNIT_OFFSET, MAX_TEXTURES));
		
		Gdx.input.setInputProcessor(this);
	}
	
	float dbgTimer = 0f;
	@Override
	public void render () {
		if ((dbgTimer += Gdx.graphics.getDeltaTime()) >= 1f) {
			dbgTimer -= 1f;
			Gdx.app.log("Test", "FPS: "+Gdx.graphics.getFramesPerSecond()+", binds: "+exclusiveTextures.getBindCount()+", reused: "+exclusiveTextures.getReuseCount());
			exclusiveTextures.resetCounter();
		}
		GL20 gl = Gdx.gl20;
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		renderBatch.begin(cam);
		for (int i = 0; i < instances.size; i++)
			renderBatch.addModel(instances.get(i).model, instances.get(i).transform);
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
		return true;
	}
}