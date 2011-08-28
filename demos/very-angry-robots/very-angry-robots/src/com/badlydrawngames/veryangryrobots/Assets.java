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

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlydrawngames.general.CameraHelper;
import com.badlydrawngames.general.CollisionGeometry;
import com.badlydrawngames.general.Config;

import static com.badlydrawngames.general.Rectangles.*;

public class Assets {

	private static final String FLYUP_FONT = Config.asString("Global.flyupFont", "ocr_a_small.fnt");
	private static final String SCORE_FONT = Config.asString("Global.scoreFont", "wellbutrin.fnt");
	private static final String TEXT_FONT = Config.asString("Global.textFont", "ocr_a.fnt");

	private static final float PLAYER_BORDER_WIDTH = Config.asFloat("Player.borderWidthPercent", 25.0f);
	private static final float PLAYER_BORDER_HEIGHT = Config.asFloat("Player.borderHeightPercent", 6.7f);
	private static final float PLAYER_FRAME_DURATION = Config.asFloat("Player.frameDuration", 0.2f);

	private static final float ROBOT_FRAME_DURATION = Config.asFloat("Robot.frameDuration", 0.1f);

	private static final float CAPTAIN_BORDER_WIDTH = Config.asFloat("Captain.borderWidthPercent", 16.7f);
	private static final float CAPTAIN_BORDER_HEIGHT = Config.asFloat("Captain.borderHeightPercent", 16.7f);
	private static final float CAPTAIN_FRAME_DURATION = Config.asFloat("Captain.frameDuration", 0.1f);

	private static final float PARTICLE_SIZE = Config.asFloat("particle.size", 0.1875f);

	private static TextureAtlas atlas;

	public static final float VIRTUAL_WIDTH = 30.0f;
	public static final float VIRTUAL_HEIGHT = 20.0f;

	public static TextureRegion pureWhiteTextureRegion;
	public static TextureRegion playerWalkingRight1;
	public static TextureRegion playerWalkingRight2;
	public static TextureRegion playerWalkingLeft1;
	public static TextureRegion playerWalkingLeft2;
	public static TextureRegion robotLeft1;
	public static TextureRegion robotLeft2;
	public static TextureRegion robotLeft3;
	public static TextureRegion robotLeft4;
	public static TextureRegion robotRight1;
	public static TextureRegion robotRight2;
	public static TextureRegion robotRight3;
	public static TextureRegion robotRight4;
	public static TextureRegion robotScan1;
	public static TextureRegion robotScan2;
	public static TextureRegion robotScan3;
	public static TextureRegion robotScan4;
	public static TextureRegion playerShot;
	public static TextureRegion robotShot;
	public static TextureRegion nemesis1;
	public static TextureRegion nemesis2;
	public static TextureRegion pauseButton;

	public static Animation playerWalkingRightAnimation;
	public static Animation playerWalkingLeftAnimation;

	public static Animation robotWalkingLeftAnimation;
	public static Animation robotWalkingRightAnimation;
	public static Animation robotScanningAnimation;

	public static Animation nemesisAnimation;

	public static BitmapFont scoreFont;
	public static BitmapFont textFont;
	public static BitmapFont flyupFont;

	public static Sound[] chickenTaunts;
	public static Sound[] standardTaunts;
	public static Sound extraLifeSound;
	public static Sound theHumanoidMustNotEscapeSound;
	public static Sound exitRoomAsChickenSpeech;
	public static Sound electrocuteRobotSound;
	public static Sound electrocutePlayerSound;
	public static Sound robotShotSound;
	public static Sound killLastRobotSound;
	public static Sound killRobotSound;
	public static Sound spawnPlayerSound;
	public static Sound killPlayerSound;
	public static Sound playerShotSound;
	public static Sound captainEnterRoomSpeech;
	public static Sound achievementSound;
	public static Sound buttonSound;

	public static float playerWidth;
	public static float playerHeight;
	public static float robotWidth;
	public static float robotHeight;
	public static float captainWidth;
	public static float captainHeight;
	public static float playerShotWidth;
	public static float playerShotHeight;
	public static float robotShotWidth;
	public static float robotShotHeight;
	public static float particleWidth;
	public static float particleHeight;

	public static CollisionGeometry playerGeometry;
	public static CollisionGeometry captainGeometry;

	public static float pixelDensity;

	public static void load () {
		pixelDensity = calculatePixelDensity();
		String textureDir = "data/textures/" + (int)pixelDensity;
		String textureFile = textureDir + "/pack";
		atlas = new TextureAtlas(Gdx.files.internal(textureFile), Gdx.files.internal(textureDir));
		loadTextures();
		createAnimations();
		loadFonts();
		loadSounds();
		initialiseGeometries();
	}

	private static void loadTextures () {
		pureWhiteTextureRegion = atlas.findRegion("8x8");
		playerWalkingRight1 = atlas.findRegion("HeroRight1");
		playerWalkingRight2 = atlas.findRegion("HeroRight2");
		playerWalkingLeft1 = atlas.findRegion("HeroLeft1");
		playerWalkingLeft2 = atlas.findRegion("HeroLeft2");
		robotLeft1 = atlas.findRegion("RobotLeft0");
		robotLeft2 = atlas.findRegion("RobotLeft1");
		robotLeft3 = atlas.findRegion("RobotLeft2");
		robotLeft4 = atlas.findRegion("RobotLeft3");
		robotRight1 = atlas.findRegion("RobotRight0");
		robotRight2 = atlas.findRegion("RobotRight1");
		robotRight3 = atlas.findRegion("RobotRight2");
		robotRight4 = atlas.findRegion("RobotRight3");
		robotScan1 = atlas.findRegion("RobotScan0");
		robotScan2 = atlas.findRegion("RobotScan1");
		robotScan3 = atlas.findRegion("RobotScan2");
		robotScan4 = atlas.findRegion("RobotScan3");
		playerShot = atlas.findRegion("PlayerShot01");
		robotShot = atlas.findRegion("RobotShot01");
		nemesis1 = atlas.findRegion("BigBadGuy1");
		nemesis2 = atlas.findRegion("BigBadGuy2");
		pauseButton = atlas.findRegion("pause");
	}

	private static float calculatePixelDensity () {
		FileHandle textureDir = Gdx.files.internal("data/textures");
		FileHandle[] availableDensities = textureDir.list();
		FloatArray densities = new FloatArray();
		for (int i = 0; i < availableDensities.length; i++) {
			try {
				float density = Float.parseFloat(availableDensities[i].name());
				densities.add(density);
			}
			catch (NumberFormatException ex) {
				// Ignore anything non-numeric, such as ".svn" folders.
			}
		}
		densities.shrink();	// Remove empty slots to get rid of zeroes.
		densities.sort();	// Now the lowest density comes first.
		return CameraHelper.bestDensity(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, densities.items);
	}

	private static void createAnimations () {
		playerWalkingRightAnimation = new Animation(PLAYER_FRAME_DURATION, Assets.playerWalkingRight1, Assets.playerWalkingRight2);
		playerWalkingLeftAnimation = new Animation(PLAYER_FRAME_DURATION, Assets.playerWalkingLeft1, Assets.playerWalkingLeft2);

		robotWalkingLeftAnimation = new Animation(ROBOT_FRAME_DURATION, robotLeft1, robotLeft2, robotLeft3, robotLeft4, robotLeft3,
			robotLeft2);
		robotWalkingRightAnimation = new Animation(ROBOT_FRAME_DURATION, robotRight1, robotRight2, robotRight3, robotRight4,
			robotRight3, robotRight2);
		robotScanningAnimation = new Animation(ROBOT_FRAME_DURATION, robotScan1, robotScan2, robotScan3, robotScan4, robotScan3,
			robotScan2);

		nemesisAnimation = new Animation(CAPTAIN_FRAME_DURATION, nemesis1, nemesis2);
	}

	private static void loadFonts () {
		String fontDir = "data/fonts/" + (int)pixelDensity + "/";

		scoreFont = new BitmapFont(Gdx.files.internal(fontDir + SCORE_FONT), false);
		textFont = new BitmapFont(Gdx.files.internal(fontDir + TEXT_FONT), false);
		flyupFont = new BitmapFont(Gdx.files.internal(fontDir + FLYUP_FONT), false);

		scoreFont.setScale(1.0f / pixelDensity);
		textFont.setScale(1.0f / pixelDensity);
		flyupFont.setScale(1.0f / pixelDensity);
	}

	private static void loadSounds () {
		standardTaunts = loadSounds("standard_taunts");
		chickenTaunts = loadSounds("chicken_taunts");
		achievementSound = loadSound("achievement.ogg");
		buttonSound = loadSound("bleep.ogg");
		electrocuteRobotSound = loadSound("buzz01.ogg");
		electrocutePlayerSound = loadSound("buzz05.ogg");
		captainEnterRoomSpeech = loadSound("captain_enter_room_speech.ogg");
		spawnPlayerSound = loadSound("electrifying01.ogg");
		exitRoomAsChickenSpeech = loadSound("exit_room_as_chicken_speech.ogg");
		killRobotSound = loadSound("hit01.ogg");
		killPlayerSound = loadSound("hit02.ogg");
		playerShotSound = loadSound("hit02.ogg");
		robotShotSound = loadSound("hit02.ogg");
		killLastRobotSound = loadSound("hit03.ogg");
		extraLifeSound = loadSound("pickup.ogg");
	}

	private static Sound[] loadSounds (String dir) {
		FileHandle dh = Gdx.files.internal("data/sounds/" + dir);
		FileHandle[] fhs = dh.list();
		List<Sound> sounds = new ArrayList<Sound>();
		for (int i = 0; i < fhs.length; i++) {
			String name = fhs[i].name();
			if (name.endsWith(".ogg")) {
				sounds.add(loadSound(dir + "/" + name));
			}
		}
		Sound[] result = new Sound[0];
		return sounds.toArray(result);
	}

	private static Sound loadSound (String filename) {
		return Gdx.audio.newSound(Gdx.files.internal("data/sounds/" + filename));
	}

	private static void initialiseGeometries () {

		playerWidth = toWidth(playerWalkingRight1);
		playerHeight = toHeight(playerWalkingRight1);
		robotWidth = toWidth(robotRight1);
		robotHeight = toHeight(robotRight1);
		captainWidth = toWidth(nemesis1);
		captainHeight = toHeight(nemesis1);
		playerShotWidth = toWidth(playerShot);
		playerShotHeight = toHeight(playerShot);
		robotShotWidth = toWidth(robotShot);
		robotShotHeight = toHeight(robotShot);
		particleWidth = PARTICLE_SIZE;
		particleHeight = PARTICLE_SIZE;

		// TODO: The below is a complete hack just to provide the player and captain with some collision geometry
		// so that he doesn't die when he's clearly not in contact with a wall, bullet or enemy. Ideally it would
		// be generated from the bitmap, or loaded.

		// Configure player collision geometry.
		Array<Rectangle> playerRectangles = new Array<Rectangle>();
		Rectangle r = new Rectangle();
		float x = (playerWidth * PLAYER_BORDER_WIDTH / 100.0f) / 2.0f;
		float y = (playerHeight * PLAYER_BORDER_HEIGHT / 100.0f) / 2.0f;
		float w = playerWidth - 2 * x;
		float h = playerHeight - 2 * y;
		setRectangle(r, x, y, w, h);
		playerRectangles.add(r);
		playerGeometry = new CollisionGeometry(playerRectangles);

		// Configure "captain" collision geometry.
		Array<Rectangle> captainRectangles = new Array<Rectangle>();
		r = new Rectangle();
		x = (captainWidth * CAPTAIN_BORDER_WIDTH / 100.0f) / 2.0f;
		y = (captainHeight * CAPTAIN_BORDER_HEIGHT / 100.0f) / 2.0f;
		w = captainWidth - 2 * x;
		h = captainHeight - 2 * y;
		setRectangle(r, x, y, w, h);
		captainRectangles.add(r);
		captainGeometry = new CollisionGeometry(captainRectangles);
	}

	private static float toWidth (TextureRegion region) {
		return region.getRegionWidth() / pixelDensity;
	}

	private static float toHeight (TextureRegion region) {
		return region.getRegionHeight() / pixelDensity;
	}

	public static void playSound (Sound sound) {
		sound.play(1);
	}
}
