/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
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

package com.badlydrawngames.veryangryrobots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlydrawngames.general.CameraHelper;
import com.badlydrawngames.general.CameraHelper.ViewportMode;
import com.badlydrawngames.general.GameScreen;
import com.badlydrawngames.general.ScoreString;

import static com.badlydrawngames.veryangryrobots.Assets.*;

public class ScoresScreen extends GameScreen<VeryAngryRobotsGame> {

	private static final String LAST_SCORE = "Score:";
	private static final String TOP_SCORE = "Best: ";
	private static final String VERY_ANGRY_ROBOTS = "VeryAngryRobots";
	private static final String TOP_SCORE_PREF = "top score";
	private static final String TAP_TO_CONTINUE = "Tap to continue";

	private ScoreString scoreString;
	private ScoreString topScoreString;
	private OrthographicCamera scoreCam;
	private SpriteBatch spriteBatch;
	private boolean touched;
	private int topScore;
	private final Preferences preferences;
	private float stateTime;

	public ScoresScreen (VeryAngryRobotsGame game) {
		super(game);
		scoreCam = CameraHelper.createCamera2(ViewportMode.PIXEL_PERFECT, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, Assets.pixelDensity);
		spriteBatch = new SpriteBatch();
		spriteBatch.setProjectionMatrix(scoreCam.combined);
		scoreString = new ScoreString();
		topScoreString = new ScoreString();
		preferences = Gdx.app.getPreferences(VERY_ANGRY_ROBOTS);
		topScore = preferences.getInteger(TOP_SCORE_PREF, 1000);
		topScoreString.setScore(topScore);
		stateTime = 0.0f;
	}

	public void setScore (int score) {
		scoreString.setScore(score);
		if (score > topScore) {
			topScore = score;
			preferences.putInteger(TOP_SCORE_PREF, topScore);
			preferences.flush();
			topScoreString.setScore(score);
		}
	}

	@Override
	public void render (float delta) {
		stateTime += delta;
		if (Gdx.input.justTouched()) {
			touched = true;
		} else if (touched && !Gdx.input.isTouched()) {
			touched = false;
			game.setScreen(game.mainMenuScreen);
		} else {
			Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			spriteBatch.begin();
			Assets.scoreFont.setColor(Color.WHITE);
			TextBounds b = Assets.scoreFont.getBounds(LAST_SCORE);
			float w = b.width + Assets.scoreFont.getSpaceWidth();
			b = Assets.scoreFont.getBounds(scoreString);
			float w2 = b.width;
			float x = (VIRTUAL_WIDTH - (w + w2)) / 2.0f;
			float y = 3 * VIRTUAL_HEIGHT / 4.0f;
			Assets.scoreFont.setColor(Color.WHITE);
			Assets.scoreFont.draw(spriteBatch, TOP_SCORE, x, y);
			Assets.scoreFont.draw(spriteBatch, topScoreString, x + w, y);

			b = Assets.scoreFont.getBounds(TOP_SCORE);
			w = b.width + Assets.scoreFont.getSpaceWidth();
			b = Assets.scoreFont.getBounds(topScoreString);
			w2 = b.width;
			x = (VIRTUAL_WIDTH - (w + w2)) / 2.0f;
			y += 2 * b.height;
			Assets.scoreFont.draw(spriteBatch, LAST_SCORE, x, y);
			Assets.scoreFont.draw(spriteBatch, scoreString, x + w, y);

			if (stateTime % 1.0f < 0.5f) {
				Assets.textFont.setColor(Color.WHITE);
				Assets.textFont.drawWrapped(spriteBatch, TAP_TO_CONTINUE, 0, VIRTUAL_HEIGHT / 4, VIRTUAL_WIDTH, HAlignment.CENTER);
			}
			spriteBatch.end();
		}
	}

	@Override
	public void show () {
		Gdx.input.setCatchBackKey(true);
		stateTime = 0.0f;
	}

	@Override
	public void hide () {
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void resume () {
		Gdx.input.setCatchBackKey(true);
	}
}
