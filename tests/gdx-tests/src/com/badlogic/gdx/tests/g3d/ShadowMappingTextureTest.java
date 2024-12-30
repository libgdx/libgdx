
package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ShadowMappingTextureTest extends GdxTest {
	PerspectiveCamera cam;
	CameraInputController camController;
	ModelBatch modelBatch;
	Model model;
	ModelInstance instance;
	ModelInstance instance2;
	Environment environment;
	DirectionalShadowLight shadowLight;
	ModelBatch shadowBatch;
	private Model model2;

	@Override
	public void create () {
		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
		environment
			.add((shadowLight = new DirectionalShadowLight(1024, 1024, 30f, 30f, 1f, 100f)).set(0.8f, 0.8f, 0.8f, 1f, -.1f, -.2f));
		environment.shadowMap = shadowLight;

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 7f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 1f;
		cam.far = 50f;
		cam.update();

		ModelBuilder modelBuilder = new ModelBuilder();

		modelBuilder.begin();

		MeshPartBuilder mpb = modelBuilder.part("parts", GL20.GL_TRIANGLES,
			Usage.Position | Usage.Normal | Usage.ColorUnpacked | Usage.TextureCoordinates, new Material(
				TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("data/animation.png"))), new BlendingAttribute(1)));

		mpb.setColor(1f, 1f, 1f, 1f);
		mpb.sphere(2f, 2f, 2f, 10, 10);
		model = modelBuilder.end();
		instance = new ModelInstance(model);

		modelBuilder.begin();
		mpb = modelBuilder.part("parts2", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.ColorUnpacked, new Material());
		mpb.setColor(1f, 1f, 1f, 1f);
		mpb.sphere(1f, 10f, 10f, 10, 10);
		model2 = modelBuilder.end();
		instance2 = new ModelInstance(model2);

		shadowBatch = new ModelBatch(new DepthShaderProvider());

		Gdx.input.setInputProcessor(camController = new CameraInputController(cam));
	}

	@Override
	public void render () {
		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		instance2.transform.setToTranslation(4, 0, 0);

		shadowLight.begin(Vector3.Zero, cam.direction);
		shadowBatch.begin(shadowLight.getCamera());
		shadowBatch.render(instance);
		shadowBatch.render(instance2);
		shadowBatch.end();
		shadowLight.end();

		modelBatch.begin(cam);
		modelBatch.render(instance, environment);
		modelBatch.render(instance2, environment);
		modelBatch.end();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
		model2.dispose();
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
