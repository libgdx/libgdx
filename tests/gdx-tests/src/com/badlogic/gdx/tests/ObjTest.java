/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.tests.utils.GdxTest;

@Deprecated
public class ObjTest extends GdxTest implements InputProcessor {
	PerspectiveCamera cam;
	Model model;
	Texture texture;
	float angleY = 0;
	float angleX = 0;
	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};
	float touchStartX = 0;
	float touchStartY = 0;

	@Override
	public void create () {
		ObjLoader objLoader = new ObjLoader();
		model =  objLoader.loadModel(Gdx.files.internal("data/cube.obj"));
		
		BoundingBox bbox = new BoundingBox();
		model.calculateBoundingBox(bbox);
		
		Gdx.app.log("ObjTest", "obj bounds: " + bbox);
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);
		texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);

		cam = new PerspectiveCamera(45, 4, 4);
		cam.position.set(3, 3, 3);
		cam.direction.set(-1, -1, -1);
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
		texture.bind();
		model.meshes.get(0).render(GL10.GL_TRIANGLES);
	}
	
	@Override
	public void dispose () {
		model.dispose();
		texture.dispose();
	}

	@Override
	public boolean keyDown (int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		return false;
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
	public boolean touchUp (int x, int y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

	@Override
	public boolean mouseMoved (int x, int y) {
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		return false;
	}
}
