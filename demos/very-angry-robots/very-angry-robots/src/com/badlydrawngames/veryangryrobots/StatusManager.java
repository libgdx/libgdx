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

import java.util.EnumSet;

import com.badlogic.gdx.Gdx;
import com.badlydrawngames.general.Config;
import com.badlydrawngames.veryangryrobots.mobiles.BaseShot;
import com.badlydrawngames.veryangryrobots.mobiles.Robot;

public class StatusManager implements WorldListener {

	public enum Achievements {
		PERFECTIONIST("Perfectionist", "Clear " + CLEAN_ROOMS + " rooms in a row."), PERFECT_SHOT("Perfect Shot",
			"Clear a room by shooting everything."), PACIFICIST("Pacifist", "Clear a room without shooting anything."), DAREDEVIL(
			"Daredevil", "Survive for " + DAREDEVIL_SECONDS + " seconds after the captain enters the room."), COWARD("Coward",
			"Run from " + CHICKEN_ROOMS + " rooms in a row."), ADDICT("Addict", "Play " + ADDICT_GAMES + " games."), LUCKY_JIM(
			"Lucky Jim", "Survive for " + LUCKY_JIM_ROOMS + " rooms without being hit."), ROBOCIDE("Robocide", "Destroyed "
			+ ROBOCIDE_ROBOTS + " robots");

		private final String summary;
		private final String text;

		private Achievements (String summary, String text) {
			this.summary = summary;
			this.text = text;
		}

		public String summary () {
			return this.summary;
		}

		public String text () {
			return this.text;
		}
	}

	// Achievements.
	private static final int CLEAN_ROOMS = Config.asInt("achievements.cleanRoomsForPerfectionist", 5);
	private static final int ROBOCIDE_ROBOTS = Config.asInt("achievements.robotsForRobocide", 50);
	private static final int LUCKY_JIM_ROOMS = Config.asInt("achievements.roomsForLuckyJim", 10);
	private static final int CHICKEN_ROOMS = Config.asInt("achievements.roomsForChicken", 5);
	private static final int ADDICT_GAMES = Config.asInt("achievements.gamesForAddict", 5);
	private static final float DAREDEVIL_SECONDS = Config.asInt("achievements.daredevilSeconds", 10);

	// Lives and scoring.
	private static final int INITIAL_LIVES = Config.asInt("Player.lives", 3);
	private static final int ROBOT_SCORE = Config.asInt("Robot.score", 50);
	private static final int ROBOT_BONUS_SCORE = Config.asInt("Robot.bonusScore", 100);
	private static final int EXTRA_LIFE_SCORE_1 = Config.asInt("Player.firstExtraLife", 10000);
	private static final int EXTRA_LIFE_SCORE_2 = Config.asInt("Player.secondExtraLife", 50000);

	private final ScoreNotifier scoreNotifier;
	private final ScoringEventNotifier scoringEventNotifier;
	private final AchievementsNotifier achievementsNotifier;
	private final EnumSet<Achievements> achieved;

	/** The number of lives that the player has. */
	private int lives;

	/** The player's score. */
	private int score;

	/** The number of games that the player has played. */
	private int numGames;

	/** The number of robots that have been shot in this room. */
	private int robotsShot;

	/** The number of robots that have been destroyed in this room (including those that have been shot). */
	private int robotsDestroyed;

	/** The number of times the player has been hit in this room. */
	private int playerHits;


	/** The number of robots that have been destroyed in this game (including those that have been shot). */
	private int gameRobotsDestroyed;

	/** The number of times the player has cleared a room in a row. */
	private int cleanRooms;

	/** The number of rooms that the player has got through without dying. */
	private int gameRoomsWithoutDying;

	/** The number of robots in the room. */
	private int numRobots;

	/** The number of times the player has fled from a room in a row. */
	private int chickenRooms;

	/** The time at which the daredevil achievement is fired. */
	private float daredevilTime;

	/** True if the player was hit. */
	private boolean isPlayerHit;

	private float now;

	public StatusManager () {
		scoreNotifier = new ScoreNotifier();
		achievementsNotifier = new AchievementsNotifier();
		scoringEventNotifier = new ScoringEventNotifier();
		achieved = EnumSet.noneOf(Achievements.class);
	}

	public void update (float delta) {
		now += delta;
		if (daredevilTime != 0.0f && now >= daredevilTime) {
			achievement(Achievements.DAREDEVIL);
			daredevilTime = 0.0f;
		}
	}

	public void addScoreListener (ScoreListener listener) {
		scoreNotifier.addListener(listener);
	}

	public void addAchievementsListener (AchievementsListener listener) {
		achievementsNotifier.addListener(listener);
	}

	public void addScoringEventListener (ScoringEventListener listener) {
		scoringEventNotifier.addListener(listener);
	}

	private void setLives (int lives) {
		this.lives = lives;
		scoreNotifier.onLivesChanged(lives);
	}

	private void addLives (int inc) {
		setLives(lives + inc);
	}

	private void setScore (int newScore) {
		this.score = newScore;
		scoreNotifier.onScoreChanged(newScore);
	}

	private void addScore (int inc) {
		int oldScore = score;
		setScore(score + inc);
		if (oldScore < EXTRA_LIFE_SCORE_1 && score >= EXTRA_LIFE_SCORE_1) {
			addLives(1);
		} else if (oldScore < EXTRA_LIFE_SCORE_2 && score >= EXTRA_LIFE_SCORE_2) {
			addLives(1);
		}
	}

	@Override
	public void onWorldReset () {
		numGames++;
		achieved.clear();
		if (numGames == ADDICT_GAMES) {
			achievement(Achievements.ADDICT);
		}
		gameRobotsDestroyed = 0;
		gameRoomsWithoutDying = 0;
		cleanRooms = 0;
		chickenRooms = 0;
		isPlayerHit = false;
		now = 0.0f;
		setLives(INITIAL_LIVES);
		setScore(0);
	}

	@Override
	public void onEnteredRoom (float time, int robots) {
		robotsShot = 0;
		robotsDestroyed = 0;
		playerHits = 0;
		daredevilTime = 0.0f;
		numRobots = robots;
	}

	@Override
	public void onExitedRoom (float time, int robots) {
		if (robots == 0) {
			chickenRooms = 0;
			cleanRooms++;
			if (cleanRooms == CLEAN_ROOMS) {
				achievement(Achievements.PERFECTIONIST);
			} else if (robotsShot == 0) {
				achievement(Achievements.PACIFICIST);
			}
		} else {
			cleanRooms = 0;
			chickenRooms++;
			if (chickenRooms == CHICKEN_ROOMS) {
				achievement(Achievements.COWARD);
			}
		}
		if (playerHits == 0) {
			gameRoomsWithoutDying++;
			if (gameRoomsWithoutDying == LUCKY_JIM_ROOMS) {
				achievement(Achievements.LUCKY_JIM);
			}
		} else {
			gameRoomsWithoutDying = 0;
		}
		Gdx.app.log("Metrics", "FPS = " + Gdx.graphics.getFramesPerSecond());
	}

	@Override
	public void onRobotHit (Robot robot) {
		robotsShot++;
		if (robotsShot == numRobots) {
			achievement(Achievements.PERFECT_SHOT);
		}
	}

	@Override
	public void onRobotDestroyed (Robot robot) {
		robotsDestroyed++;
		gameRobotsDestroyed++;
		int robotScore = ROBOT_SCORE;
		if (robotsDestroyed == numRobots) {
			robotScore += ROBOT_BONUS_SCORE;
		}
		addScore(robotScore);
		scoringEventNotifier.onScoringEvent(robot.x + robot.width / 2, robot.y + robot.height / 2, robotScore);
		if (gameRobotsDestroyed == ROBOCIDE_ROBOTS) {
			achievement(Achievements.ROBOCIDE);
		}
	}

	@Override
	public void onPlayerHit () {
		daredevilTime = 0.0f;
		playerHits++;
		isPlayerHit = true;
	}

	@Override
	public void onCaptainActivated (float time) {
		daredevilTime = now + DAREDEVIL_SECONDS;
	}

	@Override
	public void onPlayerFired () {
	}

	@Override
	public void onPlayerSpawned () {
		if (isPlayerHit) {
			addLives(-1);
			isPlayerHit = false;
		}
	}

	@Override
	public void onRobotFired (Robot robot) {
	}

	@Override
	public void onShotDestroyed (BaseShot shot) {
	}

	private void achievement (Achievements achievement) {
		if (!achieved.contains(achievement)) {
			achieved.add(achievement);
			achievementsNotifier.onAttained(achievement);
		}
	}
}
