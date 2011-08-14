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

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlydrawngames.veryangryrobots.StatusManager.Achievements;

public class VeryAngryRobotsGame extends Game implements AchievementsListener {

	Screen mainMenuScreen;
	Screen playingScreen;
	ScoresScreen scoresScreen;
	IShowScores scoreDisplayer;
	ISubmitScores scoreSubmitter;
	AchievementsListener achievementsListener;

	/** Creates all the screens that the game will need, then switches to the main menu. */
	@Override
	public void create () {
		Assets.load();
		mainMenuScreen = new MainMenuScreen(this);
		playingScreen = new WorldPresenter(this);
		scoresScreen = new ScoresScreen(this);
		setScreen(mainMenuScreen);
	}

	public void submitScore (int score) {
		if (scoreSubmitter != null) {
			scoreSubmitter.submitScore(score);
		}
	}

	public void showScores () {
		if (scoreDisplayer != null) {
			scoreDisplayer.showScores();
		}
	}

	@Override
	public void onAttained (Achievements achievement) {
		if (achievementsListener != null) {
			achievementsListener.onAttained(achievement);
		}
	}

	public void setScoreDisplayer (IShowScores scoreDisplayer) {
		this.scoreDisplayer = scoreDisplayer;
	}

	public void setScoreSubmitter (ISubmitScores scoreSubmitter) {
		this.scoreSubmitter = scoreSubmitter;
	}

	public void setAchievementsListener (AchievementsListener listener) {
		this.achievementsListener = listener;
	}

	public boolean canShowScores () {
		return scoreDisplayer != null;
	}
}
