package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3djModelLoader;
import com.badlogic.gdx.tests.utils.GdxTest;

public class JsonModelLoaderTest extends GdxTest {
	PerspectiveCamera cam;
	Model model;
	
	float angleY = 0;
	float angleX = 0;
	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};
	float touchStartX = 0;
	float touchStartY = 0;
	
	@Override
	public void create () {
		G3djModelLoader loader = new G3djModelLoader();
		model = loader.load(Gdx.files.internal("data/g3d/head2.g3dj"));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(1f, 1.5f, 1f);
		cam.direction.set(-1, -1, -1);
		cam.near = 0.001f;
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		GL10 gl = Gdx.graphics.getGL10();

		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		cam.update();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
// Gdx.graphics.getGLU().gluPerspective(Gdx.gl10, 45, 1, 1, 100);
		gl.glLoadMatrixf(cam.projection.val, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadMatrixf(cam.view.val, 0);

		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);

		gl.glRotatef(angleY, 0, 1, 0);
		gl.glRotatef(angleX, 1, 0, 0);
		
		// FIXME model.render()
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int newParam) {
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		angleY += (x - touchStartX);
		angleX += (y - touchStartY);
		touchStartX = x;
		touchStartY = y;
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
