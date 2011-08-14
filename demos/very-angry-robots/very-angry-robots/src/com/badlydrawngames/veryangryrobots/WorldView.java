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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlydrawngames.general.CameraHelper;
import com.badlydrawngames.general.CameraHelper.ViewportMode;
import com.badlydrawngames.general.Config;
import com.badlydrawngames.general.Particle;
import com.badlydrawngames.general.ParticleManager;
import com.badlydrawngames.veryangryrobots.mobiles.BaseShot;
import com.badlydrawngames.veryangryrobots.mobiles.Captain;
import com.badlydrawngames.veryangryrobots.mobiles.GameObject;
import com.badlydrawngames.veryangryrobots.mobiles.Player;
import com.badlydrawngames.veryangryrobots.mobiles.PlayerShot;
import com.badlydrawngames.veryangryrobots.mobiles.Robot;

import static com.badlydrawngames.general.MathUtils.*;
import static com.badlydrawngames.veryangryrobots.Assets.*;

/** The <code>WorldView</code> displays the {@link World} on screen. It also provides the means by which the player can control the
 * game.
 * 
 * @author Rod */
public class WorldView {

	/** The <code>Presenter</code> interface is how the <code>WorldView</code> communicates the state of its controls. */
	public static interface Presenter {
		void setController (float x, float y);

		void setFiringController (float dx, float dy);

		void pause ();

		void resume ();
	}

	private static final float PARTICLE_SIZE = Config.asFloat("particle.size", 0.1875f);

	private static final int SPRITE_CACHE_SIZE = 128; // TODO: add to Config?
	private static final float FIRING_DEAD_ZONE = 0.125f; // TODO: add to Config.
	private static final float JOYSTICK_DISTANCE_MULTIPLIER = 0.2f; // TODO: add to Config.

	private static final int MAX_PARTICLES = 256;

	private final World world;
	private final Presenter presenter;
	private OrthographicCamera worldCam;
	private SpriteCache spriteCache;
	private SpriteCache prevSpriteCache;
	private int cacheId;
	private int prevCacheId;
	private Matrix4 prevCacheTransform;
	private Matrix4 cacheTransform;
	private SpriteBatch spriteBatch;
	private Vector3 touchPoint;
	private Vector3 dragPoint;
	private Vector2 joystick;
	private final ParticleManager particleManager;
	private final ParticleAdapter particleAdapter;
	private final FlyupManager flyupManager;
	private final float worldMinX;
	private final float worldMinY;
	private final float worldWidth;
	private final float worldHeight;
	private final float worldMaxX;
	private final float worldMaxY;

	/** Constructs a new WorldView.
	 * 
	 * @param world the {@link World} that this is a view of.
	 * @param presenter the interface by which this <code>WorldView</code> communicates the state of its controls. */
	public WorldView (World world, StatusManager statusManager, Presenter presenter) {
		this.world = world;
		this.presenter = presenter;
		Rectangle bounds = world.getRoomBounds();
		worldMinX = bounds.x;
		worldMinY = bounds.y;
		worldWidth = bounds.width;
		worldHeight = bounds.height;
		worldMaxX = worldMinX + worldWidth;
		worldMaxY = worldMinY + worldHeight;
		// TODO: find some way of parameterising the viewport mode.
		worldCam = CameraHelper.createCamera2(ViewportMode.PIXEL_PERFECT, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, Assets.pixelDensity);
		worldCam.update();
		spriteBatch = new SpriteBatch();
		spriteCache = new SpriteCache(SPRITE_CACHE_SIZE, true);
		spriteBatch.setProjectionMatrix(worldCam.combined);
		spriteCache.setProjectionMatrix(worldCam.combined);
		prevSpriteCache = new SpriteCache(SPRITE_CACHE_SIZE, true);
		prevSpriteCache.setProjectionMatrix(worldCam.combined);
		cacheTransform = new Matrix4();
		prevCacheTransform = new Matrix4();
		touchPoint = new Vector3();
		dragPoint = new Vector3();
		joystick = new Vector2();
		particleManager = new ParticleManager(MAX_PARTICLES, PARTICLE_SIZE);
		particleAdapter = new ParticleAdapter(world, particleManager);
		world.addWorldListener(particleAdapter);
		flyupManager = new FlyupManager();
		statusManager.addScoringEventListener(flyupManager);
		resetCaches();
	}

	public void update (float delta) {
		particleAdapter.update(delta);
		flyupManager.update(delta);
	}

	/** Called when the view should be rendered.
	 * 
	 * @param delta the time in seconds since the last render. */
	public void render (float delta) {
		switch (world.getState()) {

		case World.RESETTING:
			resetCaches();
			break;

		case World.ENTERED_ROOM:
			if (world.getStateTime() == 0.0f) {
				createMazeContent();
				particleAdapter.setRobotColor(world.getRobotColor());
			}
			updatePanning();
			drawWallsAndDoors();
			drawMobiles();
			break;

		case World.PLAYING:
			// TODO: this is really a bit of a hack ... does it really need to be called every tick?
			if (world.getStateTime() == 0.0f) {
				cacheTransform.idt();
				spriteCache.setTransformMatrix(cacheTransform);
				spriteBatch.setTransformMatrix(cacheTransform);
			}
			// break;
		case World.PLAYER_DEAD:
			drawWallsAndDoors();
			drawMobiles();
			break;
		}
	}

	private void resetCaches () {
		cacheId = -1;
		cacheTransform.idt();
		prevCacheTransform.idt();
	}

	private void createMazeContent () {
		cycleCaches();
		cacheId = createWallsAndDoors(spriteCache);
	}

	private void cycleCaches () {
		SpriteCache tempCache = prevSpriteCache;
		prevSpriteCache = spriteCache;
		spriteCache = tempCache;
		prevCacheId = cacheId;
	}

	private int createWallsAndDoors (SpriteCache sc) {
		// Walls and doors never move, so we put them into a sprite cache.
		sc.clear();
		sc.beginCache();
		sc.setColor(Color.BLUE);
		Array<Rectangle> rects = world.getWallRects();
		for (int i = 0; i < rects.size; i++) {
			Rectangle rect = rects.get(i);
			sc.add(Assets.pureWhiteTextureRegion, rect.x, rect.y, rect.width, rect.height);
		}
		sc.setColor(1, 1, 0, 1);
		rects = world.getDoorRects();
		for (int i = 0; i < rects.size; i++) {
			Rectangle rect = rects.get(i);
			sc.add(Assets.pureWhiteTextureRegion, rect.x, rect.y, rect.width, rect.height);
		}
		return sc.endCache();
	}

	private void updatePanning () {
		// If we're moving from one room to another then we want to draw the old room scrolling off as the new room
		// scrolls on.
		final float w = worldWidth;
		final float h = worldHeight;
		float time = min(1.0f, world.getStateTime() / World.ROOM_TRANSITION_TIME);

		// The direction of scrolling is determined by the door position.
		switch (world.getDoorPosition()) {
		case DoorPositions.MIN_Y:
			prevCacheTransform.idt().trn(0.0f, -h * time, 0.0f);
			cacheTransform.idt().trn(0.0f, h - h * time, 0.0f);
			break;

		case DoorPositions.MAX_Y:
			prevCacheTransform.idt().trn(0.0f, h * time, 0.0f);
			cacheTransform.idt().trn(0.0f, -h + h * time, 0.0f);
			break;

		case DoorPositions.MIN_X:
			prevCacheTransform.idt().trn(-w * time, 0.0f, 0.0f);
			cacheTransform.idt().trn(w - w * time, 0.0f, 0.0f);
			break;

		case DoorPositions.MAX_X:
			prevCacheTransform.idt().trn(w * time, 0.0f, 0.0f);
			cacheTransform.idt().trn(-w + w * time, 0.0f, 0.0f);
		}

		prevSpriteCache.setTransformMatrix(prevCacheTransform);
		spriteCache.setTransformMatrix(cacheTransform);
		spriteBatch.setTransformMatrix(cacheTransform);
	}

	private void drawWallsAndDoors () {
		// Draw the old room if it is scrolling off.
		if (world.getState() == World.ENTERED_ROOM && prevCacheId != -1) {
			prevSpriteCache.begin();
			prevSpriteCache.draw(prevCacheId);
			prevSpriteCache.end();
		}

		// Draw the current room.
		spriteCache.begin();
		spriteCache.draw(cacheId);
		spriteCache.end();
	}

	private void drawMobiles () {
		spriteBatch.setProjectionMatrix(worldCam.combined);
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		drawPlayersShots();
		drawRobotsShots();
		drawRobots();
		drawCaptain();
		drawPlayer();
		drawParticles();
		drawFlyups();
		spriteBatch.end();
	}

	private void drawRobots () {
		spriteBatch.setColor(world.getRobotColor());
		for (Robot robot : world.getRobots()) {
			drawRobot(robot);
		}
		spriteBatch.setColor(Color.WHITE);
	}

	private void drawCaptain () {
		Captain captain = world.getCaptain();
		if (captain.state == Captain.CHASING) {
			drawClipped(captain, Assets.nemesisAnimation.getKeyFrame(captain.stateTime, true));
		}
	}

	private void drawPlayersShots () {
		for (PlayerShot shot : world.getPlayerShots()) {
			draw(shot, Assets.playerShot);
		}
	}

	private void drawRobotsShots () {
		for (BaseShot shot : world.getRobotShots()) {
			draw(shot, Assets.robotShot);
		}
	}

	private void drawPlayer () {
		Player player = world.getPlayer();
		switch (player.state) {
		case Player.WALKING_LEFT:
			draw(player, Assets.playerWalkingLeftAnimation.getKeyFrame(player.stateTime, true));
			break;
		case Player.WALKING_RIGHT:
			draw(player, Assets.playerWalkingRightAnimation.getKeyFrame(player.stateTime, true));
			break;
		case Player.FACING_LEFT:
			draw(player, Assets.playerWalkingLeft2);
			break;
		case Player.FACING_RIGHT:
			draw(player, Assets.playerWalkingRight2);
			break;
		}
	}

	private void drawRobot (Robot robot) {
		Animation robotAnimation = null;
		switch (robot.state) {
		case Robot.SCANNING:
			robotAnimation = Assets.robotScanningAnimation;
			break;
		case Robot.WALKING_DOWN:
			robotAnimation = Assets.robotWalkingLeftAnimation;
			break;
		case Robot.WALKING_LEFT:
			robotAnimation = Assets.robotWalkingLeftAnimation;
			break;
		case Robot.WALKING_LEFT_DIAGONAL:
			robotAnimation = Assets.robotWalkingLeftAnimation;
			break;
		case Robot.WALKING_RIGHT:
			robotAnimation = Assets.robotWalkingRightAnimation;
			break;
		case Robot.WALKING_RIGHT_DIAGONAL:
			robotAnimation = Assets.robotWalkingRightAnimation;
			break;
		case Robot.WALKING_UP:
			robotAnimation = Assets.robotWalkingRightAnimation;
			break;
		default:
			robotAnimation = Assets.robotScanningAnimation;
		}
		draw(robot, robotAnimation.getKeyFrame(robot.stateTime, true));
	}

	private void draw (GameObject go, TextureRegion region) {
		spriteBatch.draw(region, go.x, go.y, go.width, go.height);
	}

	private void drawClipped (GameObject go, TextureRegion region) {
		float boundsMinX = worldMinX + World.OUTER_WALL_ADJUST + World.WALL_HEIGHT;
		float boundsMaxX = worldMaxX - World.OUTER_WALL_ADJUST - World.WALL_HEIGHT;
		float boundsMinY = worldMinY + World.OUTER_WALL_ADJUST + World.WALL_HEIGHT;
		float boundsMaxY = worldMaxY - World.OUTER_WALL_ADJUST - World.WALL_HEIGHT;

		// Don't draw if it's completely out of bounds.
		float maxX = go.x + go.width;
		if (maxX < boundsMinX) return;
		float minX = go.x;
		if (minX > boundsMaxX) return;
		float maxY = go.y + go.height;
		if (maxY < boundsMinY) return;
		float minY = go.y;
		if (minY > boundsMaxY) return;

		// Clip to the visible bounds.
		float x = go.x;
		float y = go.y;
		int srcX = region.getRegionX();
		int srcY = region.getRegionY();
		int srcWidth = region.getRegionWidth();
		int srcHeight = region.getRegionHeight();
		if (minX < boundsMinX) {
			float n = (boundsMinX - minX);
			x += n;
			n *= (srcWidth / go.width);
			srcX += n;
			srcWidth -= n;
		} else if (maxX > boundsMaxX) {
			float n = (maxX - boundsMaxX);
			srcWidth -= n * (srcWidth / go.width);
		}
		if (minY < boundsMinY) {
			float n = (boundsMinY - minY);
			y += n;
			srcHeight -= n * (srcHeight / go.height);
		} else if (maxY > boundsMaxY) {
			float n = (maxY - boundsMaxY) * (srcHeight / go.height);
			srcHeight -= n;
			srcY += n;
		}
		float width = go.width * srcWidth / region.getRegionWidth();
		float height = go.height * srcHeight / region.getRegionHeight();

		spriteBatch.draw(region.getTexture(), x, y, width, height, srcX, srcY, srcWidth, srcHeight, false, false);
	}

	private void drawParticles () {
		spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		for (Particle particle : particleManager.getParticles()) {
			if (particle.active) {
				spriteBatch.setColor(particle.color);
				spriteBatch.draw(Assets.pureWhiteTextureRegion, particle.x, particle.y, particle.size, particle.size);
			}
		}
		spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void drawFlyups () {
		BitmapFont font = Assets.flyupFont;
		float scale = font.getScaleX();
		font.setScale(1.0f / Assets.pixelDensity);
		spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		for (Flyup flyup : flyupManager.flyups) {
			if (flyup.active) {
				font.draw(spriteBatch, flyup.scoreString, flyup.x, flyup.y);
			}
		}
		spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		font.setScale(scale);
	}

	/** Updates the state of the on-screen controls.
	 * 
	 * @param delta time in seconds since the last render. */
	public void updateControls (float delta) {
		presenter.setController(0.0f, 0.0f);
		if (Gdx.input.justTouched()) {
			worldCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			if (world.isPaused()) {
				presenter.resume();
			} else if (touchPoint.y >= worldMaxY) {
				presenter.pause();
			}
		} else if (Gdx.input.isTouched()) {
			worldCam.unproject(dragPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			float dx = dragPoint.x - touchPoint.x;
			float dy = dragPoint.y - touchPoint.y;
			joystick.set(dx, dy).mul(JOYSTICK_DISTANCE_MULTIPLIER);
			float len = joystick.len();
			if (len > 1) {
				joystick.nor();
			}
			if (presenter != null) {
				presenter.setController(joystick.x, joystick.y);
				if (len >= FIRING_DEAD_ZONE) {
					joystick.nor();
					presenter.setFiringController(joystick.x, joystick.y);
				}
			}
		}
	}
}
