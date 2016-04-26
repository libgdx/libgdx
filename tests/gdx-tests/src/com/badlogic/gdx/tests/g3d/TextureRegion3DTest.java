package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class TextureRegion3DTest extends GdxTest {
	PerspectiveCamera cam;
	CameraInputController inputController;
	ModelBatch modelBatch;
	Model model;
	ModelInstance instance;
	Environment environment;
	TextureAtlas atlas;
	Array<AtlasRegion> regions;
	TextureAttribute attribute;
	float time = 1;
	int index = -1;

	@Override
	public void create () {
		Gdx.gl.glClearColor(0.2f, 0.3f, 1.0f, 0.f);
		
		atlas = new TextureAtlas(Gdx.files.internal("data/testpack"));
		regions = atlas.getRegions();

		modelBatch = new ModelBatch(new DefaultShaderProvider());

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();

		ModelBuilder modelBuilder = new ModelBuilder();
		final Material material = new Material(ColorAttribute.createDiffuse(1f, 1f, 1f, 1f), new TextureAttribute(TextureAttribute.Diffuse));
		model = modelBuilder.createBox(5f, 5f, 5f, material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		instance = new ModelInstance(model);
		attribute = instance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Diffuse);

		Gdx.input.setInputProcessor(new InputMultiplexer(this, inputController = new CameraInputController(cam)));
	}
	
	@Override
	public void render () {
		inputController.update();
		if ((time += Gdx.graphics.getDeltaTime()) >= 1f) {
			time -= 1f;
			index = (index + 1) % regions.size;
			attribute.set(regions.get(index));
			Gdx.app.log("TextureRegion3DTest", "Current region = "+regions.get(index).name);
		}

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(instance, environment);
		modelBatch.end();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
		atlas.dispose();
	}

	public boolean needsGL20 () {
		return true;
	}

	public void resume () {
	}

	public void resize (int width, int height) {
	}

	public void pause () {
	}
}