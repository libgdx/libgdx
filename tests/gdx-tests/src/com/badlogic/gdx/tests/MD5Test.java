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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Animation;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5AnimationInfo;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Joints;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Loader;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Model;
import com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Renderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.TimeUtils;

public class MD5Test extends GdxTest implements InputProcessor {
	PerspectiveCamera camera;
	MD5Model model;
	MD5Animation anim;
	MD5AnimationInfo animInfo;
	MD5Joints skeleton;
	MD5Renderer renderer;
	SpriteBatch batch;
	BitmapFont font;

	@Override
	public void create () {
		Gdx.app.log("MD5 Test", "created");
		boolean useNormals = false;
		model = MD5Loader.loadModel(Gdx.files.internal("data/zfat.md5mesh").read(), useNormals);
		anim = MD5Loader.loadAnimation(Gdx.files.internal("data/walk1.md5anim").read());
		skeleton = new MD5Joints();
		skeleton.joints = new float[anim.frames[0].joints.length];
		animInfo = new MD5AnimationInfo(anim.frames.length, anim.secondsPerFrame);
		renderer = new MD5Renderer(model, useNormals, false);
		renderer.setSkeleton(model.baseSkeleton);

		// long start = TimeUtils.nanoTime();
		// for( int i = 0; i < 100000; i++ )
		// renderer.setSkeleton( model.baseSkeleton );
		// app.log( "MD5 Test", "took: " + (TimeUtils.nanoTime() - start ) /
		// 1000000000.0 );

		camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0, 25, 100);
		camera.near = 1;
		camera.far = 1000;

		batch = new SpriteBatch();
		font = new BitmapFont();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Gdx.input.setInputProcessor(this);
	}

	float angle = 0;

	@Override
	public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		camera.update();
		camera.apply(gl);
		angle += Gdx.graphics.getDeltaTime() * 20;
		animInfo.update(Gdx.graphics.getDeltaTime());

		gl.glEnable(GL10.GL_DEPTH_TEST);

		long start = 0;
		float renderTime = 0;
		float skinTime = 0;

		for (int z = 0; z < 100; z += 50) {
			gl.glPushMatrix();
			gl.glTranslatef(0, 0, -z);
			gl.glRotatef(angle, 0, 1, 0);
			gl.glRotatef(-90, 1, 0, 0);

			start = TimeUtils.nanoTime();
			MD5Animation.interpolate(anim.frames[animInfo.getCurrentFrame()], anim.frames[animInfo.getNextFrame()], skeleton,
				animInfo.getInterpolation());
			renderer.setSkeleton(skeleton);
			skinTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

			start = TimeUtils.nanoTime();
			renderer.render();
			renderTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;
			gl.glPopMatrix();
		}

		gl.glDisable(GL10.GL_DEPTH_TEST);

		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond() + (renderer.isJniUsed() ? ", jni" : ", java")
			+ ", render time: " + renderTime + ", skin time: " + skinTime, 10, 20);
		font.draw(batch, "#triangles: " + model.getNumTriangles() + ", #vertices: " + model.getNumVertices(), 10, 40);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		renderer.dispose();
		font.dispose();

		batch = null;
		renderer = null;
		font = null;

		System.gc();
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
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		renderer.setUseJni(!renderer.isJniUsed());
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
