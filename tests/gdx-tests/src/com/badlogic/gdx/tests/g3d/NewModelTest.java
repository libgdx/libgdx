package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Light;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.JsonModelLoader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class NewModelTest extends BaseG3dHudTest {
	Lights lights = new Lights(0.2f, 0.2f, 0.2f).add(
		//new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -1f, 0f)
		new PointLight().set(1f, 0f, 0f, 5f, 5f, 5f, 100f),
		new PointLight().set(0f, 0f, 1f, -5f, 5f, 5f, 100f),
		new PointLight().set(0f, 1f, 0f, 0f, 5f, -5f, 100f)
		//new Light(0.5f, 0.5f, 0.5f, 1f),
		//new Light(0.5f, 0.5f, 0.5f, 1f, -1f, -2f, -3f)
	);
	
	@Override
	public void create () {
		super.create();

		onModelClicked("g3d/head.g3dj");
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		batch.render(instances, lights);
	}
	
	@Override
	protected void onModelClicked(final String name) {
		if (name == null)
			return;
		assets.load("data/"+name, Model.class);
		assets.finishLoading();

		instances.clear();
		instances.add(new ModelInstance(assets.get("data/"+name, Model.class)));
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
}
