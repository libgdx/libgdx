
package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.utils.ScreenUtils;

/** Render a basic scene in a FrameBufferCubemap and displays it in a rotating cube. */
public class FrameBufferCubemapTest extends Basic3DSceneTest {
	protected PerspectiveCamera camFb;
	protected PerspectiveCamera camCube;
	protected FrameBufferCubemap fb;
	protected Cubemap cubemap;
	protected Model cubeMesh;
	protected ModelInstance cubeInstance;
	protected ModelBatch cubeBatch;

	@Override
	public void create () {
		super.create();

		camFb = new PerspectiveCamera(90, 800, 800);
		camFb.position.set(10f, 10f, 10f);
		camFb.lookAt(0, 0, 0);
		camFb.near = 0.1f;
		camFb.far = 1000f;
		camFb.update();

		fb = new FrameBufferCubemap(Format.RGBA8888, 800, 800, true);
		cubemap = fb.getColorBufferTexture();

		ObjLoader objLoader = new ObjLoader();
		cubeMesh = objLoader.loadModel(files.internal("data/cube.obj"));
		cubeInstance = new ModelInstance(cubeMesh);

		cubeBatch = new ModelBatch(files.internal("data/shaders/cubemap-vert.glsl"),
			files.internal("data/shaders/cubemap-frag.glsl"));

		cubeInstance.materials.get(0).set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));

		camCube = new PerspectiveCamera(67, graphics.getWidth() * 0.5f, graphics.getHeight() * 0.5f);
		camCube.position.set(0f, 2f, 2f);
		camCube.lookAt(0, 0, 0);
		camCube.near = 1f;
		camCube.far = 300f;
		camCube.update();
	}

	@Override
	public void render () {
		renderScene();
		renderCube();
	}

	public void renderScene () {
		gl.glViewport(0, 0, graphics.getBackBufferWidth(), graphics.getBackBufferHeight());
		gl.glDisable(GL20.GL_SCISSOR_TEST);

		// Render scene to screen
		super.render();

		// Render scene to cubemap
		camFb.position.set(cam.position);
		camFb.near = cam.near;
		camFb.far = cam.far;
		fb.begin();
		while (fb.nextSide()) {
			fb.getSide().getUp(camFb.up);
			fb.getSide().getDirection(camFb.direction);
			camFb.update();

			ScreenUtils.clear(1, 1, 1, 1, true);

			modelBatch.begin(camFb);
			for (ModelInstance instance : instances)
				modelBatch.render(instance, lights);
			if (space != null) modelBatch.render(space);
			modelBatch.end();
		}
		fb.end();
	}

	float yaw, pitch, roll;

	public void renderCube () {
		int w = graphics.getBackBufferWidth();
		int h = graphics.getBackBufferHeight();
		int x = (int)(w - w * 0.5f);
		int y = (int)(h - h * 0.5f);
		w *= 0.5f;
		h *= 0.5f;

		gl.glViewport(x, y, w, h);
		gl.glEnable(GL20.GL_SCISSOR_TEST);
		gl.glScissor(x, y, w, h);
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		pitch += 25 * graphics.getDeltaTime();
		yaw += 45 * graphics.getDeltaTime();
		cubeInstance.transform.setFromEulerAngles(yaw, pitch, roll);
		cubeBatch.begin(camCube);
		cubeBatch.render(cubeInstance);
		cubeBatch.end();
	}
}
