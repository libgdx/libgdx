package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.loader.JsonModelLoader;
import com.badlogic.gdx.graphics.g3d.old.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.graphics.g3d.test.TestShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class BatchRenderTest extends GdxTest {
	int TEXTURE_COUNT = 30;
	int BOX_COUNT = 500;
	int UNIT_OFFSET = 2;
	int MAX_TEXTURES = Math.min(16 /*GL10.GL_MAX_TEXTURE_UNITS*/ - UNIT_OFFSET, DefaultTextureBinder.MAX_GLES_UNITS - UNIT_OFFSET);
	int BIND_METHOD = DefaultTextureBinder.WEIGHTED;
	float MIN_X = -10f, MIN_Y = -10f, MIN_Z = -10f;
	float SIZE_X = 20f, SIZE_Y = 20f, SIZE_Z = 20f;
	
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
	Model sphereModel;
	Model sceneModel;
	Model cubeModel;
	Model carModel;
	Model testModel;
	Array<Model> cubes = new Array<Model>();
	Array<Texture> textures = new Array<Texture>();
	ModelBatch renderBatch;
	DefaultTextureBinder exclusiveTextures;
	Light[] lights;
	
	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};
	float touchStartX = 0;
	float touchStartY = 0;
	
	@Override
	public void create () {
		TestShader.ignoreUnimplemented = true;
		
		final JsonModelLoader loader = new JsonModelLoader();

		// need more higher resolution textures for this test...
		String[] TEXTURES = {"data/badlogic.jpg", "data/egg.png", "data/particle-fire.png", "data/planet_earth.png", "data/planet_heavyclouds.jpg",
			"data/resource1.jpg", "data/stones.jpg", "data/sys.png", "data/wheel.png"};
		
		for (int i = 0; i < TEXTURE_COUNT; i++)
			textures.add(new Texture(Gdx.files.internal(TEXTURES[i%TEXTURES.length])));
		
		ObjLoader objLoader = new ObjLoader();
		sphereModel = objLoader.loadObj(Gdx.files.internal("data/sphere.obj"));
		sceneModel = objLoader.loadObj(Gdx.files.internal("data/scene.obj"));
		cubeModel = objLoader.loadObj(Gdx.files.internal("data/cube.obj"));
		carModel = objLoader.loadObj(Gdx.files.internal("data/car.obj"));
		testModel = new Model(loader.parseModel(Gdx.files.internal("data/g3d/head.g3dj"),  null));
		
		//StillSubMesh mesh = (StillSubMesh)(cubeModel.subMeshes[0]);
		for (int i = 0; i < textures.size; i++)
			cubes.add(cubeModel);
			// cubes.add(new InterimModel(new StillModel(new StillSubMesh(mesh.name, mesh.mesh, mesh.primitiveType, new Material("mat", new TextureAttribute(textures.get(i), 0, TextureAttribute.diffuseTexture))))));
		
		createScene2();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 1f;
		cam.far = 100f;
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 0, 0);
		cam.update();
		
		renderBatch = new ModelBatch();
		
		lights = new Light[] {
			new Light(Color.WHITE, Vector3.tmp.set(-10f, 10f, -10f), 15f),
			new Light(Color.BLUE, Vector3.tmp.set(10f, 5f, 0f), 10f),
			new Light(Color.GREEN, Vector3.tmp.set(0f, 10f, 5f), 5f)
		};
		
		Gdx.input.setInputProcessor(this);
	}
	
	public void createScene1() {
		for (int i = 0; i < BOX_COUNT; i++)
			instances.add(new ModelInstance(cubes.get((int)(Math.random()*cubes.size)), (new Matrix4()).setToTranslation(MIN_X + (float)Math.random() * SIZE_X, MIN_Y + (float)Math.random() * SIZE_Y, MIN_Z + (float)Math.random() * SIZE_Z).scl(0.05f + (float)Math.random())));
	}

	public void createScene2() {
		instances.add(new ModelInstance(sceneModel, new Matrix4()));
		instances.add(new ModelInstance(testModel, (new Matrix4()).setToTranslation(0, 5, 4)));
		instances.add(new ModelInstance(carModel, (new Matrix4()).setToTranslation(6, 0, -4)));
		
		for (int i = 0; i < 10; i++)
			instances.add(new ModelInstance(sphereModel, (new Matrix4()).setToTranslation(MIN_X + (float)Math.random() * SIZE_X, MIN_Y + (float)Math.random() * SIZE_Y, MIN_Z + (float)Math.random() * SIZE_Z).scl(0.25f + (float)Math.random())));		
	}
	
	float dbgTimer = 0f;
	boolean test = false;
	@Override
	public void render () {
		if ((dbgTimer += Gdx.graphics.getDeltaTime()) >= 1f) {
			dbgTimer -= 1f;
			// Gdx.app.log("Test", "FPS: "+Gdx.graphics.getFramesPerSecond()+", binds: "+exclusiveTextures.getBindCount()+", reused: "+exclusiveTextures.getReuseCount());
			// exclusiveTextures.resetCounts();
		}
		GL20 gl = Gdx.gl20;
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 0);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		renderBatch.begin(cam);
		for (int i = 0; i < instances.size; i++) {
			if (instances.get(i).model == null)
				Gdx.app.log("Test", "Model "+i+" is null");
			else
				renderBatch.addModel(instances.get(i).model, instances.get(i).transform, lights);
		}
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