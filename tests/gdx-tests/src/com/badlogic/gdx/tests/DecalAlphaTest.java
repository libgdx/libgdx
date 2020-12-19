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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.AlphaTestGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.LinkedList;

public class DecalAlphaTest extends GdxTest {
	public static final int TARGET_FPS = 40;
	public static final int INITIAL_RENDERED = 100;
	Texture egg;
	Texture wheel;
	LinkedList<Decal> toRender = new LinkedList<Decal>();
	DecalBatch batch;
	float timePassed = 0;
	int frames = 0;
	Camera cam;
	WindowedMean fps = new WindowedMean(5);
	int idx = 0;

	private Viewport viewport;

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.app.log("DecalAlphaTest","create");

		egg = new Texture(Gdx.files.internal("data/egg.png"));
		wheel = new Texture(Gdx.files.internal("data/wheel.png"));

		for (int i = 0; i < INITIAL_RENDERED; i++) {
			toRender.add(makeDecal());
		}
		cam = new PerspectiveCamera(67f,16f, 9f);
		viewport = new StretchViewport(1920,1080,cam);
		viewport.apply();
		cam.position.set(0, 0, 10f);
		cam.direction.set(0, 0, -10f);
		cam.update();
		batch = new DecalBatch(new AlphaTestGroupStrategy(cam));

		Gdx.gl.glClearColor(0, 0, 1, 1);
	}

	@Override
	public void dispose () {
		egg.dispose();
		wheel.dispose();
		batch.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		float elapsed = Gdx.graphics.getDeltaTime();
		float scale = timePassed > 0.5 ? 1 - timePassed / 2 : 0.5f + timePassed / 2;

		for (Decal decal : toRender) {
			decal.rotateZ(elapsed * 45f);
			decal.rotateY(elapsed * 90f);
			decal.setScale(scale);
			batch.add(decal);
		}
		batch.flush();

		timePassed += elapsed;
		frames++;
		if (timePassed > 1.0f) {
			System.out.println("DecalPerformanceTest2 fps: " + frames + " at spritecount: " + toRender.size());
			fps.addValue(frames);
			if (fps.hasEnoughData()) {
				float factor = fps.getMean() / (float)TARGET_FPS;
				int target = (int)(toRender.size() * factor);
				if (fps.getMean() > TARGET_FPS) {
					int start = toRender.size();
					for (int i = start; toRender.size() < target; i++) {
						toRender.add(makeDecal());
					}
					fps.clear();
				} else {
					while (toRender.size() > target) {
						toRender.removeLast();
					}
					fps.clear();
				}
			}
			timePassed = 0;
			frames = 0;
		}
	}

	@Override
	public void resize (int width, int height) {
		viewport.update(width, height);
	}

	private Decal makeDecal () {
		Decal sprite = null;
		switch (idx % 2) {
		case 0:
			sprite = Decal.newDecal(10,10,new TextureRegion(egg), true);
			break;
		case 1:
			sprite = Decal.newDecal(10,10,new TextureRegion(wheel),false);
			break;
		}
		final float bounds =12f;
		sprite.setPosition(-bounds + (float)Math.random() * (bounds*2f),  -bounds + (float)Math.random() * (bounds*2f),
						   (float)Math.random() * -10);
		idx++;
		sprite.setColor(1f,1f,1f, (idx % 100)/100f);

		return sprite;
	}
}
