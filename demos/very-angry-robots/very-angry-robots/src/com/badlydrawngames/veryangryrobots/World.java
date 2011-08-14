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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlydrawngames.general.Colliders;
import com.badlydrawngames.general.Colliders.ColliderHandler;
import com.badlydrawngames.general.Colliders.RemovalHandler;
import com.badlydrawngames.general.Colliders.SceneryHandler;
import com.badlydrawngames.general.Config;
import com.badlydrawngames.general.Grid;
import com.badlydrawngames.general.Pools;
import com.badlydrawngames.veryangryrobots.mobiles.BaseShot;
import com.badlydrawngames.veryangryrobots.mobiles.Captain;
import com.badlydrawngames.veryangryrobots.mobiles.GameObject;
import com.badlydrawngames.veryangryrobots.mobiles.Player;
import com.badlydrawngames.veryangryrobots.mobiles.PlayerShot;
import com.badlydrawngames.veryangryrobots.mobiles.Robot;
import com.badlydrawngames.veryangryrobots.mobiles.RobotShot;

import static com.badlogic.gdx.math.MathUtils.*;
import static com.badlydrawngames.general.MathUtils.*;

/** The <code>World</code> is the representation of the game world of <b>Very Angry Robots</b>. It knows nothing about how it will
 * be displayed, neither does it know about how the player is controlled, particle effects, sounds, nor anything else. It purely
 * knows about the {@link Player}, {@link Robot}s and the walls of the room that the player is in.
 * 
 * @author Rod */
public class World {

	/** The <code>FireCommand</code> interface is how the {@link World} is told that a {@link GameObject} wants to fire. */
	public static interface FireCommand {
		/** Tells the {@link World} that a {@link GameObject} wants to fire. Note that <code>dx</code> and <code>dy</code> must be
		 * normalised. The World does not have to fire just because it is asked to.
		 * 
		 * @param firer the {@link GameObject} that wants to fire.
		 * @param dx the horizontal component of the bullet's direction.
		 * @param dy the vertical component of the bullet's direction. */
		void fire (GameObject firer, float dx, float dy);
	}

	// Maze proportions.
	private static final int VCELLS = Config.asInt("Maze.vCells", 3);
	private static final int HCELLS = Config.asInt("Maze.hCells", 5);

	// Wall sizes.
	public static final float WALL_HEIGHT = 0.25f;
	public static final float WALL_WIDTH = 6.0f;
	public static final float OUTER_WALL_ADJUST = WALL_HEIGHT;

	private static final int MAX_PLAYER_SHOTS = Config.asInt("Player.maxShots", 4);
	private static final int MAX_ROBOT_SHOTS = Config.asInt("Robot.maxShots", 6);
	private static final int MAX_ROBOTS = Config.asInt("Global.maxRobots", 12);
	private static final float PLAYER_DEAD_INTERVAL = Config.asFloat("Global.deadTime", 2.0f);
	private static final float FIRING_AMNESTY_INTERVAL = Config.asFloat("Global.amnestyTime", 2.0f);
	private static final float CAPTAIN_LURK_MULTIPLIER = Config.asFloat("Captain.lurkMultiplier", 2.0f);
	private static final float CAPTAIN_MIN_DELAY = Config.asFloat("Captain.minLurkTime", 2.0f);
	private static final float FIRING_INTERVAL = Config.asFloat("Player.firingInterval", 0.25f);
	public static final float ROOM_TRANSITION_TIME = Config.asFloat("Global.roomTransitionTime", 0.5f);

	// Game states.
	public static final int RESETTING = 1;
	public static final int ENTERED_ROOM = 2;
	public static final int PLAYING = 3;
	public static final int PLAYER_DEAD = 4;

	private final Pool<PlayerShot> shotPool;
	private final Pool<Robot> robotPool;
	private final Pool<RobotShot> robotShotPool;
	private final Grid roomGrid;
	private final RoomBuilder roomBuilder;
	private final Rectangle roomBounds;
	private final float minX;
	private final float maxX;
	private final float minY;
	private final float maxY;
	private final WorldNotifier notifier;
	private final DifficultyManager difficultyManager;
	private long roomSeed;
	private int roomX;
	private int roomY;
	private float playingTime;
	private float nextFireTime;
	private int numRobotShots;
	private float robotShotSpeed;
	private int numRobots;
	private Vector2 playerPos;
	private float now;
	private Array<Rectangle> doorRects;
	private Array<Rectangle> wallRects;
	private int doorPosition;
	private Player player;
	private Array<PlayerShot> playerShots;
	private Array<Robot> robots;
	private Array<RobotShot> robotShots;
	private Captain captain;
	private int state;
	private float stateTime;
	private Color robotColor;
	private boolean isPaused;
	private float pausedTime;

	public void pause () {
		isPaused = true;
		pausedTime = 0.0f;
	}

	public void resume () {
		isPaused = false;
	}

	public boolean isPaused () {
		return isPaused;
	}

	public float getPausedTime () {
		return pausedTime;
	}

	public int getState () {
		return state;
	}

	private void setState (int newState) {
		state = newState;
		stateTime = 0.0f;
	}

	public float getStateTime () {
		return stateTime;
	}

	public Color getRobotColor () {
		return robotColor;
	}

	public int getDoorPosition () {
		return doorPosition;
	}

	public Array<Rectangle> getDoorRects () {
		return doorRects;
	}

	public Array<Rectangle> getWallRects () {
		return wallRects;
	}

	public Rectangle getRoomBounds () {
		return roomBounds;
	}

	public Player getPlayer () {
		return player;
	}

	public Array<PlayerShot> getPlayerShots () {
		return playerShots;
	}

	public Array<Robot> getRobots () {
		return robots;
	}

	public Array<RobotShot> getRobotShots () {
		return robotShots;
	}

	public Captain getCaptain () {
		return captain;
	}

	/** Adds another listener to the {@link World}.
	 * 
	 * @param listener the listener. */
	public void addWorldListener (WorldListener listener) {
		notifier.addListener(listener);
	}

	public final FireCommand firePlayerShot = new FireCommand() {
		@Override
		public void fire (GameObject firer, float dx, float dy) {
			if (now >= nextFireTime) {
				addPlayerShot(dx, dy);
				nextFireTime = now + FIRING_INTERVAL;
			}
		}
	};

	private void addPlayerShot (float dx, float dy) {
		if (state == PLAYING && playerShots.size < MAX_PLAYER_SHOTS) {
			PlayerShot shot = shotPool.obtain();
			shot.inCollision = false;
			float x = player.x + player.width / 2 - shot.width / 2;
			float y = player.y + player.height / 2 - shot.height / 2;
			shot.fire(x, y, dx, dy);
			playerShots.add(shot);
			notifier.onPlayerFired();
		}
	}

	public final FireCommand fireRobotShot = new FireCommand() {
		@Override
		public void fire (GameObject firer, float dx, float dy) {
			addRobotShot(firer, dx, dy);
		}
	};

	private void addRobotShot (GameObject firer, float dx, float dy) {
		if (state == PLAYING && robotShots.size < numRobotShots && stateTime >= FIRING_AMNESTY_INTERVAL) {
			RobotShot shot = robotShotPool.obtain();
			shot.inCollision = false;
			shot.setOwner(firer);
			shot.setShotSpeed(robotShotSpeed);
			float x = firer.x + firer.width / 2 - shot.width / 2;
			float y = firer.y + firer.height / 2 - shot.height / 2;
			shot.fire(x, y, dx, dy);
			robotShots.add(shot);
			notifier.onRobotFired((Robot)firer);
		}
	}

	private final RemovalHandler<Robot> robotRemovalHandler = new RemovalHandler<Robot>() {
		@Override
		public void onRemove (Robot robot) {
			notifier.onRobotDestroyed(robot);
		}
	};

	private final RemovalHandler<BaseShot> shotRemovalHandler = new RemovalHandler<BaseShot>() {
		@Override
		public void onRemove (BaseShot shot) {
			notifier.onShotDestroyed(shot);
		}
	};

	private final ColliderHandler<GameObject, GameObject> gameObjectCollisionHandler = new ColliderHandler<GameObject, GameObject>() {
		@Override
		public void onCollision (GameObject t, GameObject u) {
			t.inCollision = true;
			u.inCollision = true;
		}
	};

	private final ColliderHandler<Captain, GameObject> captainGameObjectCollisionHandler = new ColliderHandler<Captain, GameObject>() {
		@Override
		public void onCollision (Captain captain, GameObject go) {
			go.inCollision = true;
		}
	};

	private final ColliderHandler<PlayerShot, Robot> shotRobotCollisionHandler = new ColliderHandler<PlayerShot, Robot>() {
		@Override
		public void onCollision (PlayerShot shot, Robot robot) {
			if (!robot.inCollision) {
				notifier.onRobotHit(robot);
			}
			shot.inCollision = true;
			robot.inCollision = true;
		}
	};

	private final ColliderHandler<Robot, Robot> robotRobotCollisionHandler = new ColliderHandler<Robot, Robot>() {
		@Override
		public void onCollision (Robot t, Robot u) {
			t.inCollision = true;
			u.inCollision = true;
		}
	};

	private final SceneryHandler<Player> playerSceneryHandler = new SceneryHandler<Player>() {
		@Override
		public void onCollision (Player player, Rectangle r) {
			player.inCollision = true;
		}
	};

	private final SceneryHandler<GameObject> gameObjectSceneryHandler = new SceneryHandler<GameObject>() {
		@Override
		public void onCollision (GameObject t, Rectangle r) {
			t.inCollision = true;
		}
	};

	/** Constructs a new {@link World}. */
	public World (DifficultyManager difficultyManager) {
		this.difficultyManager = difficultyManager;
		notifier = new WorldNotifier();
		minX = 0;
		maxX = WALL_WIDTH * HCELLS;
		minY = 0;
		maxY = WALL_WIDTH * VCELLS;
		roomBounds = new Rectangle(minX, minY, maxX - minX, maxY - minY);
		player = new Player();
		captain = new Captain();
		playerPos = new Vector2();
		roomBuilder = new RoomBuilder(HCELLS, VCELLS);
		roomGrid = new Grid(HCELLS * 2, VCELLS * 2, maxX, maxY);

		shotPool = new Pool<PlayerShot>(MAX_PLAYER_SHOTS, MAX_PLAYER_SHOTS) {
			@Override
			protected PlayerShot newObject () {
				return new PlayerShot();
			}
		};

		robotPool = new Pool<Robot>(MAX_ROBOTS, MAX_ROBOTS) {
			@Override
			protected Robot newObject () {
				return new Robot();
			}
		};

		robotShotPool = new Pool<RobotShot>(MAX_ROBOT_SHOTS, MAX_ROBOT_SHOTS) {
			@Override
			protected RobotShot newObject () {
				return new RobotShot();
			}
		};
	}

	/** Resets the {@link World} to its starting state. */
	public void reset () {
		setState(RESETTING);
	}

	/** Called when the {@link World} is to be updated.
	 * 
	 * @param delta the time in seconds since the last render. */
	public void update (float delta) {
		if (!isPaused) {
			now += delta;
			stateTime += delta;
			switch (state) {
			case RESETTING:
				updateResetting();
				break;
			case ENTERED_ROOM:
				updateEnteredRoom();
				break;
			case PLAYING:
				updatePlaying(delta);
				break;
			case PLAYER_DEAD:
				updatePlayerDead(delta);
				break;
			}
		} else {
			pausedTime += delta;
		}
	}

	private void updateResetting () {
		notifier.onWorldReset();
		roomSeed = System.currentTimeMillis();
		roomX = 0;
		roomY = 0;
		populateRoom(DoorPositions.MIN_Y);
	}

	private void updateEnteredRoom () {
		if (stateTime >= ROOM_TRANSITION_TIME) {
			setState(PLAYING);
		}
	}

	private void updatePlaying (float delta) {
		player.update(delta);
		updateMobiles(delta);
		checkForCollisions();
		checkForLeavingRoom();
	}

	private void updatePlayerDead (float delta) {
		updateMobiles(delta);
		checkForCollisions();
		if (now >= playingTime) {
			resetRoom();
			setState(PLAYING);
		}
	}

	private void populateRoom (int doorPos) {
		doorPosition = doorPos;
		setRandomSeedFromRoom();
		createMaze();
		placePlayer();
		createRobots();
		placeCaptain();
		createPlayerShots();
		createRobotShots();
		setState(ENTERED_ROOM);
		notifier.onEnteredRoom(now, numRobots);
	}

	private void createMaze () {
		roomBuilder.build(doorPosition);
		wallRects = roomBuilder.getWalls();
		doorRects = roomBuilder.getDoors();
		roomGrid.clear();
		for (Rectangle r : wallRects) {
			roomGrid.add(r);
		}
		for (Rectangle r : doorRects) {
			roomGrid.add(r);
		}
	}

	private void setRandomSeedFromRoom () {
		long seed = roomSeed + ((roomX & 0xff) | ((roomY & 0xff) << 8));
		random.setSeed(seed);
	}

	private void placePlayer () {
		player.inCollision = false;

		switch (doorPosition) {

		case DoorPositions.MIN_X:
			player.x = minX + player.width / 2;
			player.y = (maxY + minY) / 2 - player.height / 2;
			break;

		case DoorPositions.MAX_X:
			player.x = maxX - player.width - player.width / 2;
			player.y = (maxY + minY) / 2 - player.height / 2;
			break;

		case DoorPositions.MAX_Y:
			player.x = (maxX + minX) / 2 - player.width / 2;
			player.y = maxY - player.height - player.height / 4;
			break;

		case DoorPositions.MIN_Y:
		default:
			player.x = (maxX + minX) / 2 - player.width / 2;
			player.y = minY + player.height / 4;
			break;
		}

		player.setState(Player.FACING_RIGHT);
		notifier.onPlayerSpawned();
	}

	private void placeCaptain () {
		captain.inCollision = false;
		captain.setState(Captain.LURKING);
		captain.activateAfter(max(CAPTAIN_MIN_DELAY, CAPTAIN_LURK_MULTIPLIER * robots.size));
		captain.setPlayer(player);

		switch (doorPosition) {
		case DoorPositions.MIN_X:
			captain.x = minX - 2 * captain.width;
			captain.y = (maxY + minY) / 2 - captain.height / 2;
			break;

		case DoorPositions.MAX_X:
			captain.x = maxX + captain.width;
			captain.y = (maxY + minY) / 2 - captain.height / 2;
			break;

		case DoorPositions.MAX_Y:
			captain.x = (maxX + minX) / 2 - captain.width / 2;
			captain.y = maxY + captain.height;
			break;

		case DoorPositions.MIN_Y:
		default:
			captain.x = (maxX + minX) / 2 - captain.width / 2;
			captain.y = minY - 2 * captain.height;
			break;
		}
	}

	private void createRobots () {
		robotColor = difficultyManager.getRobotColor();
		numRobots = difficultyManager.getNumberOfRobots();
		numRobotShots = difficultyManager.getNumberOfRobotShots();
		robotShotSpeed = difficultyManager.getRobotShotSpeed();

		final float minXSpawn = minX + WALL_HEIGHT;
		final float minYSpawn = minY + WALL_HEIGHT;
		final float maxXSpawn = maxX - WALL_HEIGHT;
		final float maxYSpawn = maxY - WALL_HEIGHT;
		robots = Pools.makeArrayFromPool(robots, robotPool, MAX_ROBOTS);
		playerPos.set(player.x, player.y);
		for (int i = 0; i < numRobots; i++) {
			Robot robot = robotPool.obtain();
			robot.inCollision = false;
			do {
				robot.x = random(minXSpawn, maxXSpawn - robot.width);
				robot.y = random(minYSpawn, maxYSpawn - robot.height);
			} while (!canSpawnHere(robot));
			robot.setRespawnPoint(robot.x, robot.y);
			robot.setPlayer(player);
			robot.setWalls(wallRects);
			robot.setFireCommand(fireRobotShot);
			robots.add(robot);
		}
	}

	private boolean canSpawnHere (Robot robot) {
		return !(intersectsWalls(robot) || intersectsDoors(robot) || intersectsRobots(robot) || playerPos.dst(robot.x, robot.y) < WALL_WIDTH);
	}

	private boolean intersectsWalls (Robot robot) {
		return Colliders.intersects(robot.bounds(), wallRects);
	}

	private boolean intersectsDoors (Robot robot) {
		return Colliders.intersects(robot.bounds(), doorRects);
	}

	private boolean intersectsRobots (Robot robot) {
		for (int i = 0; i < robots.size; i++) {
			if (robot.boundsIntersect(robots.get(i))) {
				return true;
			}
		}
		return false;
	}

	private void createPlayerShots () {
		playerShots = Pools.makeArrayFromPool(playerShots, shotPool, MAX_PLAYER_SHOTS);
	}

	private void createRobotShots () {
		robotShots = Pools.makeArrayFromPool(robotShots, robotShotPool, MAX_ROBOT_SHOTS);
	}

	private void updateMobiles (float delta) {
		updateCaptain(delta);
		update(robots, delta);
		update(playerShots, delta);
		update(robotShots, delta);
	}

	private void resetRoom () {
		placePlayer();
		placeCaptain();
		for (Robot robot : robots) {
			robot.respawn();
		}
		robotShots.clear();
		playerShots.clear();
	}

	private void updateCaptain (float delta) {
		captain.update(delta);
		if (captain.stateTime == 0.0f && captain.state == Captain.CHASING) {
			doCaptainActivated();
		}
	}

	private void update (Array<? extends GameObject> gos, float delta) {
		for (GameObject go : gos) {
			go.update(delta);
		}
	}

	private void checkForCollisions () {
		checkMobileMobileCollisions();
		checkMobileSceneryCollisions();
		removeMarkedMobiles();
		if (state == PLAYING && player.inCollision) {
			doPlayerHit();
		}
	}

	private void checkMobileMobileCollisions () {
		Colliders.collide(player, robots, gameObjectCollisionHandler);
		Colliders.collide(player, robotShots, gameObjectCollisionHandler);
		Colliders.collide(captain, player, captainGameObjectCollisionHandler);
		Colliders.collide(playerShots, robots, shotRobotCollisionHandler);
		Colliders.collide(playerShots, robotShots, gameObjectCollisionHandler);
		Colliders.collide(robots, robotRobotCollisionHandler);
		Colliders.collide(robotShots, robots, gameObjectCollisionHandler);
		Colliders.collide(robotShots, gameObjectCollisionHandler);
		Colliders.collide(captain, robots, captainGameObjectCollisionHandler);
		Colliders.collide(captain, playerShots, captainGameObjectCollisionHandler);
		Colliders.collide(captain, robotShots, captainGameObjectCollisionHandler);
	}

	private void checkMobileSceneryCollisions () {
		Colliders.collide(player, roomGrid.get(player.bounds()), playerSceneryHandler);
		markSceneryCollisions(robots, gameObjectSceneryHandler);
		markSceneryCollisions(playerShots, gameObjectSceneryHandler);
		markSceneryCollisions(robotShots, gameObjectSceneryHandler);
	}

	private <U extends GameObject, T extends U> void markSceneryCollisions (Array<T> gos, SceneryHandler<U> handler) {
		for (int i = 0; i < gos.size; i++) {
			T go = gos.get(i);
			Colliders.collide(go, roomGrid.get(go.bounds()), handler);
		}
	}

	private void removeMarkedMobiles () {
		Colliders.removeOutOfBounds(shotPool, playerShots, roomBounds);
		Colliders.removeOutOfBounds(robotShotPool, robotShots, roomBounds);
		Colliders.removeMarkedCollisions(shotPool, playerShots, shotRemovalHandler);
		Colliders.removeMarkedCollisions(robotPool, robots, robotRemovalHandler);
		Colliders.removeMarkedCollisions(robotShotPool, robotShots, shotRemovalHandler);
	}

	private void checkForLeavingRoom () {
		int newDoor = -1;
		if (player.x + player.width / 2 < minX) {
			roomX--;
			newDoor = DoorPositions.MAX_X;
		} else if (player.x + player.width / 2 > maxX) {
			roomX++;
			newDoor = DoorPositions.MIN_X;
		} else if (player.y + player.height / 2 < minY) {
			roomY--;
			newDoor = DoorPositions.MAX_Y;
		} else if (player.y + player.height / 2 > maxY) {
			roomY++;
			newDoor = DoorPositions.MIN_Y;
		}
		if (newDoor != -1) {
			doLeftRoom(newDoor);
		}
	}

	private void doPlayerHit () {
		notifier.onPlayerHit();
		setState(PLAYER_DEAD);
		playingTime = now + PLAYER_DEAD_INTERVAL;
	}

	private void doLeftRoom (int newDoor) {
		notifier.onExitedRoom(now, robots.size);
		populateRoom(newDoor);
	}

	private void doCaptainActivated () {
		notifier.onCaptainActivated(now);
	}
}
