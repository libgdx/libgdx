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

package com.badlogicgames.superjumper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class HelpScreen3 implements Screen {
	Game game;

	OrthographicCamera guiCam;
	SpriteBatch batcher;
	Rectangle nextBounds;
	Vector3 touchPoint;
	Texture helpImage;
	TextureRegion helpRegion;

	public HelpScreen3 (Game game) {
		this.game = game;

		guiCam = new OrthographicCamera(320, 480);
		guiCam.position.set(320 / 2, 480 / 2, 0);
		nextBounds = new Rectangle(320 - 64, 0, 64, 64);
		touchPoint = new Vector3();
		batcher = new SpriteBatch();
		helpImage = Assets.loadTexture("data/help3.png");
		helpRegion = new TextureRegion(helpImage, 0, 0, 320, 480);
	}

	public void update (float deltaTime) {
		if (Gdx.input.justTouched()) {
			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

			if (OverlapTester.pointInRectangle(nextBounds, touchPoint.x, touchPoint.y)) {
				Assets.playSound(Assets.clickSound);
				game.setScreen(new HelpScreen4(game));
				return;
			}
		}
	}

	public void draw (float deltaTime) {
		GLCommon gl = Gdx.gl;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		guiCam.update();

		batcher.setProjectionMatrix(guiCam.combined);
		batcher.disableBlending();
		batcher.begin();
		batcher.draw(helpRegion, 0, 0, 320, 480);
		batcher.end();

		batcher.enableBlending();
		batcher.begin();
		batcher.draw(Assets.arrow, 320, 0, -64, 64);
		batcher.end();

		gl.glDisable(GL10.GL_BLEND);
	}

	@Override
	public void render (float delta) {
		update(delta);
		draw(delta);
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void show () {
	}

	@Override
	public void hide () {
	}

	@Override
	public void pause () {
		helpImage.dispose();
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
