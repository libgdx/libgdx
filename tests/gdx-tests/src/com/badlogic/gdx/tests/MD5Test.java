/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.loaders.md5.MD5Animation;
import com.badlogic.gdx.graphics.loaders.md5.MD5AnimationInfo;
import com.badlogic.gdx.graphics.loaders.md5.MD5Joints;
import com.badlogic.gdx.graphics.loaders.md5.MD5Loader;
import com.badlogic.gdx.graphics.loaders.md5.MD5Model;
import com.badlogic.gdx.graphics.loaders.md5.MD5Renderer;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MD5Test extends GdxTest implements InputProcessor {
	PerspectiveCamera camera;
	MD5Model model;
	MD5Animation anim;
	MD5AnimationInfo animInfo;
	MD5Joints skeleton;
	MD5Renderer renderer;
	SpriteBatch batch;
	BitmapFont font;

	@Override public void create () {
		Gdx.app.log("MD5 Test", "created");
		model = MD5Loader.loadModel(Gdx.files.internal("data/zfat.md5mesh").read(), true);
		anim = MD5Loader.loadAnimation(Gdx.files.internal("data/walk1.md5anim").read());
		skeleton = new MD5Joints();
		skeleton.joints = new float[anim.frames[0].joints.length];
		animInfo = new MD5AnimationInfo(anim.frames.length, anim.secondsPerFrame);
		renderer = new MD5Renderer(model, false, true);
		renderer.setSkeleton(model.baseSkeleton);

		// long start = System.nanoTime();
		// for( int i = 0; i < 100000; i++ )
		// renderer.setSkeleton( model.baseSkeleton );
		// app.log( "MD5 Test", "took: " + (System.nanoTime() - start ) /
		// 1000000000.0 );

		camera = new PerspectiveCamera();
		camera.getPosition().set(0, 25, 100);
		camera.setFov(60);
		camera.setNear(1);
		camera.setFar(1000);
		camera.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		batch = new SpriteBatch();
		font = new BitmapFont();
		Gdx.graphics.getGL10().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Gdx.input.setInputProcessor(this);
	}

	float angle = 0;

	@Override public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		camera.setMatrices();
		angle += Gdx.graphics.getDeltaTime() * 20;
		animInfo.update(Gdx.graphics.getDeltaTime());

		gl.glEnable(GL10.GL_DEPTH_TEST);

		long start = 0;
		float renderTime = 0;
		float skinTime = 0;

		for (int z = 0; z < 50; z += 50) {
			gl.glLoadIdentity();
			gl.glTranslatef(0, 0, -z);
			gl.glRotatef(angle, 0, 1, 0);
			gl.glRotatef(-90, 1, 0, 0);

			start = System.nanoTime();
			MD5Animation.interpolate(anim.frames[animInfo.getCurrentFrame()], anim.frames[animInfo.getNextFrame()], skeleton,
				animInfo.getInterpolation());
			renderer.setSkeleton(skeleton);
			skinTime = (System.nanoTime() - start) / 1000000000.0f;

			start = System.nanoTime();
			renderer.render();
			renderTime = (System.nanoTime() - start) / 1000000000.0f;
		}

		gl.glDisable(GL10.GL_DEPTH_TEST);

		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond() + (renderer.isJniUsed() ? ", jni" : ", java")
			+ ", render time: " + renderTime + ", skin time: " + skinTime, 10, 20);
		batch.end();
	}

	@Override public void dispose () {
		batch.dispose();
		renderer.dispose();
		font.dispose();

		batch = null;
		renderer = null;
		font = null;

		System.gc();
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		renderer.setUseJni(!renderer.isJniUsed());
		return false;
	}

	@Override public boolean needsGL20 () {
		return false;
	}

}
