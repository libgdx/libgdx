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
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;

public class AnimationTest extends GdxTest {

	class Caveman {
		static final float VELOCITY = 20;
		public final Vector2 pos;
		public final boolean headsLeft;
		public float stateTime;

		public Caveman (float x, float y, boolean headsLeft) {
			pos = new Vector2().set(x, y);
			this.headsLeft = headsLeft;
			this.stateTime = (float)Math.random();
		}

		public void update (float deltaTime) {
			stateTime += deltaTime;
			pos.x = pos.x + (headsLeft ? -VELOCITY * deltaTime : VELOCITY * deltaTime);
			if (pos.x < -64) pos.x = Gdx.graphics.getWidth();
			if (pos.x > Gdx.graphics.getWidth() + 64) pos.x = -64;
		}
	}

	Animation leftWalk;
	Animation rightWalk;
	Caveman[] cavemen;
	Texture texture;
	SpriteBatch batch;
	FPSLogger fpsLog;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/walkanim.png"));
		TextureRegion[] leftWalkFrames = TextureRegion.split(texture, 64, 64)[0];
		TextureRegion[] rightWalkFrames = new TextureRegion[leftWalkFrames.length];
		for (int i = 0; i < rightWalkFrames.length; i++) {
			TextureRegion frame = new TextureRegion(leftWalkFrames[i]);
			frame.flip(true, false);
			rightWalkFrames[i] = frame;
		}
		leftWalk = new Animation(0.25f, leftWalkFrames);
		rightWalk = new Animation(0.25f, rightWalkFrames);

		cavemen = new Caveman[100];
		for (int i = 0; i < 100; i++) {
			cavemen[i] = new Caveman((float)Math.random() * Gdx.graphics.getWidth(),
				(float)Math.random() * Gdx.graphics.getHeight(), Math.random() > 0.5 ? true : false);
		}
		batch = new SpriteBatch();
		fpsLog = new FPSLogger();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		for (int i = 0; i < cavemen.length; i++) {
			Caveman caveman = cavemen[i];
			TextureRegion frame = caveman.headsLeft ? leftWalk.getKeyFrame(caveman.stateTime, true) : rightWalk.getKeyFrame(
				caveman.stateTime, true);
			batch.draw(frame, caveman.pos.x, caveman.pos.y);
		}
		batch.end();

		for (int i = 0; i < cavemen.length; i++) {
			cavemen[i].update(Gdx.graphics.getDeltaTime());
		}

		fpsLog.log();
	}

	@Override
	public void dispose () {
		batch.dispose();
		texture.dispose();
	}
}
