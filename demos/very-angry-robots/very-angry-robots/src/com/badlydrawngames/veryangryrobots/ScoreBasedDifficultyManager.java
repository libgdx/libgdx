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

import com.badlogic.gdx.graphics.Color;
import com.badlydrawngames.general.Config;

public class ScoreBasedDifficultyManager implements DifficultyManager, ScoreListener {

	// Robot numbers.
	private static final int MAX_ROBOTS = Config.asInt("Global.maxRobots", 12);
	private static final int INITIAL_ROBOTS = Config.asInt("Global.initialRobots", 6);

	// Shot speeds.
	private static final float SLOW_SHOT_SPEED = Config.asFloat("RobotShot.slowSpeed", 75.0f);
	private static final float FAST_SHOT_SPEED = Config.asFloat("RobotShot.fastSpeed", 125.0f);

	// Level progression.
	private static final int ROBOT_INCREMENT = Config.asInt("Level.robotIncrement", 3);
	private static final int LEVEL_0_ROBOT_SHOTS = Config.asInt("Level.0.robotShots", 0);
	private static final int LEVEL_1_ROBOT_SHOTS = Config.asInt("Level.1.robotShots", 1);
	private static final int LEVEL_2_ROBOT_SHOTS = Config.asInt("Level.2.robotShots", 2);
	private static final int LEVEL_3_ROBOT_SHOTS = Config.asInt("Level.3.robotShots", 3);
	private static final int LEVEL_4_ROBOT_SHOTS = Config.asInt("Level.4.robotShots", 4);
	private static final int LEVEL_5_ROBOT_SHOTS = Config.asInt("Level.5.robotShots", 5);
	private static final int LEVEL_6_ROBOT_SHOTS = Config.asInt("Level.6.robotShots", 1);
	private static final int LEVEL_7_ROBOT_SHOTS = Config.asInt("Level.7.robotShots", 2);
	private static final int LEVEL_8_ROBOT_SHOTS = Config.asInt("Level.8.robotShots", 3);
	private static final int LEVEL_9_ROBOT_SHOTS = Config.asInt("Level.9.robotShots", 4);
	private static final int LEVEL_10_ROBOT_SHOTS = Config.asInt("Level.10.robotShots", 5);
	private static final int LEVEL_11_ROBOT_SHOTS = Config.asInt("Level.11.robotShots", 6);
	private static final int LEVEL_1_SCORE = Config.asInt("Level.1.score", 200);
	private static final int LEVEL_2_SCORE = Config.asInt("Level.2.score", 500);
	private static final int LEVEL_3_SCORE = Config.asInt("Level.3.score", 1000);
	private static final int LEVEL_4_SCORE = Config.asInt("Level.4.score", 2500);
	private static final int LEVEL_5_SCORE = Config.asInt("Level.5.score", 5000);
	private static final int LEVEL_6_SCORE = Config.asInt("Level.6.score", 7500);
	private static final int LEVEL_7_SCORE = Config.asInt("Level.7.score", 10000);
	private static final int LEVEL_8_SCORE = Config.asInt("Level.8.score", 12500);
	private static final int LEVEL_9_SCORE = Config.asInt("Level.9.score", 15000);
	private static final int LEVEL_10_SCORE = Config.asInt("Level.10.score", 17500);
	private static final int LEVEL_11_SCORE = Config.asInt("Level.11.score", 20000);

	// Robot colours.
	private static final Color DARK_YELLOW = new Color(0.75f, 0.75f, 0.0f, 1.0f);
	private static final Color RED = Color.RED;
	private static final Color DARK_CYAN = new Color(0.0f, 0.75f, 0.75f, 1.0f);
	private static final Color GREEN = Color.GREEN;
	private static final Color DARK_PURPLE = new Color(0.75f, 0.0f, 0.75f, 1.0f);
	private static final Color LIGHT_YELLOW = new Color(1.0f, 1.0f, 0.0f, 1.0f);
	private static final Color WHITE = Color.WHITE;
	private static final Color LIGHT_PURPLE = new Color(1.0f, 0.0f, 1.0f, 1.0f);
	private static final Color GREY = new Color(0.75f, 0.75f, 0.75f, 1.0f);

	private boolean dirty;
	private int score;
	private Color robotColor;
	private int numRobots;
	private int numRobotShots;
	private float robotShotSpeed;

	@Override
	public Color getRobotColor () {
		updateIfDirty();
		return robotColor;
	}

	@Override
	public int getNumberOfRobots () {
		updateIfDirty();
		return numRobots;
	}

	@Override
	public int getNumberOfRobotShots () {
		updateIfDirty();
		return numRobotShots;
	}

	@Override
	public float getRobotShotSpeed () {
		updateIfDirty();
		return robotShotSpeed;
	}

	@Override
	public void onScoreChanged (int score) {
		dirty = true;
		this.score = score;
	}

	@Override
	public void onLivesChanged (int lives) {
	}

	private void updateIfDirty () {
		if (!dirty) return;
		dirty = false;

		Color lastRobotColor = robotColor;
		if (score < LEVEL_1_SCORE) {
			robotColor = DARK_YELLOW;
			numRobotShots = LEVEL_0_ROBOT_SHOTS;
			robotShotSpeed = SLOW_SHOT_SPEED;
		} else if (score < LEVEL_2_SCORE) {
			robotColor = RED;
			numRobotShots = LEVEL_1_ROBOT_SHOTS;
			robotShotSpeed = SLOW_SHOT_SPEED;
		} else if (score < LEVEL_3_SCORE) {
			robotColor = DARK_CYAN;
			numRobotShots = LEVEL_2_ROBOT_SHOTS;
			robotShotSpeed = SLOW_SHOT_SPEED;
		} else if (score < LEVEL_4_SCORE) {
			robotColor = GREEN;
			numRobotShots = LEVEL_3_ROBOT_SHOTS;
			robotShotSpeed = SLOW_SHOT_SPEED;
		} else if (score < LEVEL_5_SCORE) {
			robotColor = DARK_PURPLE;
			numRobotShots = LEVEL_4_ROBOT_SHOTS;
			robotShotSpeed = SLOW_SHOT_SPEED;
		} else if (score < LEVEL_6_SCORE) {
			robotColor = LIGHT_YELLOW;
			numRobotShots = LEVEL_5_ROBOT_SHOTS;
			robotShotSpeed = SLOW_SHOT_SPEED;
		} else if (score < LEVEL_7_SCORE) {
			robotColor = WHITE;
			numRobotShots = LEVEL_6_ROBOT_SHOTS;
			robotShotSpeed = FAST_SHOT_SPEED;
		} else if (score < LEVEL_8_SCORE) {
			robotColor = DARK_CYAN;
			numRobotShots = LEVEL_7_ROBOT_SHOTS;
			robotShotSpeed = FAST_SHOT_SPEED;
		} else if (score < LEVEL_9_SCORE) {
			robotColor = LIGHT_PURPLE;
			numRobotShots = LEVEL_8_ROBOT_SHOTS;
			robotShotSpeed = FAST_SHOT_SPEED;
		} else if (score < LEVEL_10_SCORE) {
			robotColor = GREY;
			numRobotShots = LEVEL_9_ROBOT_SHOTS;
			robotShotSpeed = FAST_SHOT_SPEED;
		} else if (score < LEVEL_11_SCORE) {
			robotColor = DARK_YELLOW;
			numRobotShots = LEVEL_10_ROBOT_SHOTS;
			robotShotSpeed = FAST_SHOT_SPEED;
		} else {
			robotColor = RED;
			numRobotShots = LEVEL_11_ROBOT_SHOTS;
			robotShotSpeed = FAST_SHOT_SPEED;
		}

		if (lastRobotColor == robotColor && score >= LEVEL_1_SCORE) {
			if (numRobots < MAX_ROBOTS) {
				numRobots += ROBOT_INCREMENT;
			}
		} else {
			numRobots = INITIAL_ROBOTS;
		}
	}
}
