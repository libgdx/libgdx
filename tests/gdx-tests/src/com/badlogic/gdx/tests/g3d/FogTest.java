package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FogTest extends GdxTest implements ApplicationListener {
	public PerspectiveCamera cam;
	public CameraInputController inputController;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	public Lights lights;
	
	@Override
	public void create () {
		modelBatch = new ModelBatch();
		lights = new Lights();
		lights.ambientLight.set(0.4f, 0.4f, 0.4f, 1f);
        lights.fog = new Color(0.13f, 0.13f, 0.13f, 1f);
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(30f, 10f, 30f);
		cam.lookAt(0,0,0);
		cam.near = 0.1f;
		cam.far = 45f;
		cam.update();

		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal);
		instance = new ModelInstance(model);

		Gdx.input.setInputProcessor(new InputMultiplexer(this, inputController = new CameraInputController(cam)));
	}

	@Override
	public void render () {

        animate();

		inputController.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glClearColor(0.13f, 0.13f, 0.13f, 1);

		modelBatch.begin(cam);
		modelBatch.render(instance, lights);
		modelBatch.end();
	}

    float delta = 0f, dir = 1;
    private void animate() {

        delta = Gdx.graphics.getDeltaTime();

        instance.transform.val[14] += delta*4*dir;

        if(Math.abs(instance.transform.val[14]) > 5) {
            dir *= -1;
        }
    }
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
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
