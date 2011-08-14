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

package com.badlydrawngames.veryangryrobots.mobiles;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlydrawngames.general.Config;
import com.badlydrawngames.veryangryrobots.Assets;
import com.badlydrawngames.veryangryrobots.World.FireCommand;

import static com.badlogic.gdx.math.MathUtils.*;
import static com.badlydrawngames.general.MathUtils.*;

public class Robot extends GameObject {

	public static final int SCANNING = INACTIVE + 1;
	public static final int WALKING_RIGHT = SCANNING + 1;
	public static final int WALKING_RIGHT_DIAGONAL = WALKING_RIGHT + 1;
	public static final int WALKING_LEFT = WALKING_RIGHT_DIAGONAL + 1;
	public static final int WALKING_LEFT_DIAGONAL = WALKING_LEFT + 1;
	public static final int WALKING_DOWN = WALKING_LEFT_DIAGONAL + 1;
	public static final int WALKING_UP = WALKING_DOWN + 1;

	private static final float WALKING_SPEED = Config.asFloat("Robot.speed", 1.25f);

	private GameObject player;
	private Array<Rectangle> walls;
	private final float distance;
	private final float fudge;
	private FireCommand fireCommand;
	private Vector2 firingDirection;
	private Vector2 robotPos;
	private Vector2 playerPos;
	private Vector2 lineStart;
	private Vector2 lineEnd;
	private float respawnX;
	private float respawnY;

	public Robot () {
		width = Assets.robotWidth;
		height = Assets.robotHeight;
		distance = max(width, height);
		fudge = distance * 0.25f;
		setState(INACTIVE);
		firingDirection = new Vector2();
		robotPos = new Vector2();
		playerPos = new Vector2();
		lineStart = new Vector2();
		lineEnd = new Vector2();
	}

	public void setPlayer (GameObject player) {
		this.player = player;
	}

	public void setWalls (Array<Rectangle> walls) {
		this.walls = walls;
	}

	public void setFireCommand (FireCommand fireCommand) {
		this.fireCommand = fireCommand;
	}

	@Override
	public void update (float delta) {
		stateTime += delta;
		moveRobot(delta);
		if (fireCommand != null && canFire(delta) && canSeePlayer()) {
			firingDirection.set(player.x - x, player.y - y);
			firingDirection.nor();
			fireCommand.fire(this, firingDirection.x, firingDirection.y);
		}
	}

	private boolean canFire (float delta) {
		// TODO: remove magic numbers, or possibly switch to expovariate randomness.
		return random(100) < 50 * delta;
	}

	private void moveRobot (float delta) {
		float dx = (player.x + player.width / 2) - (x + width / 2);
		float dy = (player.y + player.height / 2) - (y + height / 2);
		dx = abs(dx) >= 2 ? sgn(dx) : 0.0f;
		dy = abs(dy) >= 2 ? sgn(dy) : 0.0f;
		float ax = 0.0f;
		float ay = 0.0f;
		if (!wouldHitWall(dx, dy)) {
			ax = dx;
			ay = dy;
		} else if (dx != 0 && !wouldHitWall(dx, 0)) {
			ax = dx;
		} else if (dy != 0 && !wouldHitWall(0, dy)) {
			ay = dy;
		}
		dx = ax * WALKING_SPEED;
		dy = ay * WALKING_SPEED;
		x += dx * delta;
		y += dy * delta;

		int newState = getMovementState(dx, dy);
		if (newState != state) {
			setState(newState);
		}
	}

	private int getMovementState (float dx, float dy) {
		if (dx == 0.0f && dy == 0.0f) {
			return SCANNING;
		} else if (dx > 0) {
			return (dy == 0) ? WALKING_RIGHT : WALKING_RIGHT_DIAGONAL;
		} else if (dx < 0) {
			return (dy == 0) ? WALKING_LEFT : WALKING_LEFT_DIAGONAL;
		} else if (dy < 0) {
			return WALKING_DOWN;
		} else {
			return WALKING_UP;
		}
	}

	private boolean wouldHitWall (float dx, float dy) {
		float x1 = x + width / 2;
		float y1 = y + height / 2;
		float x2 = x1 + dx * distance;
		float y2 = y1 + dy * distance;

		for (int i = 0; i < walls.size; i++) {
			Rectangle wall = walls.get(i);
			if (doesLineHitWall(wall, x1, y1, x2, y2)) {
				return true;
			}
		}
		return false;
	}

	private boolean doesLineHitWall (Rectangle rect, float x1, float y1, float x2, float y2) {
		// Does not intersect if minimum y coordinate is below the rectangle.
		float minY = min(y1, y2);
		if (minY >= rect.y + rect.height + fudge) return false;

		// Does not intersect if maximum y coordinate is above the rectangle.
		float maxY = max(y1, y2);
		if (maxY < rect.y - fudge) return false;

		// Does not intersect if minimum x coordinate is to the right of the rectangle.
		float minX = min(x1, x2);
		if (minX >= rect.x + rect.width + fudge) return false;

		// Does not intersect if maximum x coordinate is to the left of the rectangle.
		float maxX = max(x1, x2);
		if (maxX < rect.x - fudge) return false;

		// And that's good enough, because the robots need to be a bit stupid
		// when they're near the ends of walls.
		return true;
	}

	private boolean canSeePlayer () {
		return hasLineOfSight(this, player);
	}

	private boolean hasLineOfSight (GameObject a, GameObject b) {
		return hasLineOfSight(a.x + a.width / 2, a.y + a.height / 2, b.x + b.width / 2, b.y + b.height / 2);
	}

	private boolean hasLineOfSight (float x1, float y1, float x2, float y2) {
		robotPos.set(x1, y1);
		playerPos.set(x2, y2);
		for (int i = 0; i < walls.size; i++) {
			Rectangle wall = walls.get(i);
			if (wall.width > wall.height) {
				lineStart.set(wall.x, wall.y + wall.height / 2);
				lineEnd.set(wall.x + wall.width, lineStart.y);
			} else {
				lineStart.set(wall.x + wall.width / 2, wall.y);
				lineEnd.set(lineStart.x, wall.y + wall.height);
			}
			if (Intersector.intersectSegments(robotPos, playerPos, lineStart, lineEnd, null)) {
				return false;
			}
		}
		return true;
	}

	public void setRespawnPoint (float x, float y) {
		respawnX = x;
		respawnY = y;
	}

	public void respawn () {
		x = respawnX;
		y = respawnY;
	}
}
