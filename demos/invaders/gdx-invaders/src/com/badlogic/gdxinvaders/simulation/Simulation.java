/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
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

package com.badlogic.gdxinvaders.simulation;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector3;

public class Simulation {
	public final static float PLAYFIELD_MIN_X = -14;
	public final static float PLAYFIELD_MAX_X = 14;
	public final static float PLAYFIELD_MIN_Z = -15;
	public final static float PLAYFIELD_MAX_Z = 2;

	public ArrayList<Invader> invaders = new ArrayList<Invader>();
	public ArrayList<Block> blocks = new ArrayList<Block>();
	public ArrayList<Shot> shots = new ArrayList<Shot>();
	public ArrayList<Explosion> explosions = new ArrayList<Explosion>();
	public Ship ship;
	public Shot shipShot = null;
	public transient SimulationListener listener;
	public float multiplier = 1;
	public int score;
	public int wave = 1;

	private ArrayList<Shot> removedShots = new ArrayList<Shot>();
	private ArrayList<Explosion> removedExplosions = new ArrayList<Explosion>();

	public Simulation () {
		populate();
	}

	private void populate () {
		ship = new Ship();

		for (int row = 0; row < 4; row++) {
			for (int column = 0; column < 8; column++) {
				Invader invader = new Invader(new Vector3(-PLAYFIELD_MAX_X / 2 + column * 2f, 0, PLAYFIELD_MIN_Z + row * 2f));
				invaders.add(invader);
			}
		}

		for (int shield = 0; shield < 3; shield++) {
			blocks.add(new Block(new Vector3(-10 + shield * 10 - 1, 0, -2)));
			blocks.add(new Block(new Vector3(-10 + shield * 10 - 1, 0, -3)));
			blocks.add(new Block(new Vector3(-10 + shield * 10 + 0, 0, -3)));
			blocks.add(new Block(new Vector3(-10 + shield * 10 + 1, 0, -3)));
			blocks.add(new Block(new Vector3(-10 + shield * 10 + 1, 0, -2)));
		}
	}

	public void update (float delta) {
		ship.update(delta);
		updateInvaders(delta);
		updateShots(delta);
		updateExplosions(delta);
		checkShipCollision();
		checkInvaderCollision();
		checkBlockCollision();
		checkNextLevel();
	}

	private void updateInvaders (float delta) {
		for (int i = 0; i < invaders.size(); i++) {
			Invader invader = invaders.get(i);
			invader.update(delta, multiplier);
		}
	}

	private void updateShots (float delta) {
		removedShots.clear();
		for (int i = 0; i < shots.size(); i++) {
			Shot shot = shots.get(i);
			shot.update(delta);
			if (shot.hasLeftField) removedShots.add(shot);
		}

		for (int i = 0; i < removedShots.size(); i++)
			shots.remove(removedShots.get(i));

		if (shipShot != null && shipShot.hasLeftField) shipShot = null;

		if (Math.random() < 0.01 * multiplier && invaders.size() > 0) {
			int index = (int)(Math.random() * (invaders.size() - 1));
			Shot shot = new Shot(invaders.get(index).position, true);
			shots.add(shot);
			if (listener != null) listener.shot();
		}
	}

	public void updateExplosions (float delta) {
		removedExplosions.clear();
		for (int i = 0; i < explosions.size(); i++) {
			Explosion explosion = explosions.get(i);
			explosion.update(delta);
			if (explosion.aliveTime > Explosion.EXPLOSION_LIVE_TIME) removedExplosions.add(explosion);
		}

		for (int i = 0; i < removedExplosions.size(); i++)
			explosions.remove(removedExplosions.get(i));
	}

	private void checkInvaderCollision () {
		if (shipShot == null) return;

		for (int j = 0; j < invaders.size(); j++) {
			Invader invader = invaders.get(j);
			if (invader.position.dst(shipShot.position) < Invader.INVADER_RADIUS) {
				shots.remove(shipShot);
				shipShot = null;
				invaders.remove(invader);
				explosions.add(new Explosion(invader.position));
				if (listener != null) listener.explosion();
				score += Invader.INVADER_POINTS;
				break;
			}
		}
	}

	private void checkShipCollision () {
		removedShots.clear();

		if (!ship.isExploding) {
			for (int i = 0; i < shots.size(); i++) {
				Shot shot = shots.get(i);
				if (!shot.isInvaderShot) continue;

				if (ship.position.dst(shot.position) < Ship.SHIP_RADIUS) {
					removedShots.add(shot);
					shot.hasLeftField = true;
					ship.lives--;
					ship.isExploding = true;
					explosions.add(new Explosion(ship.position));
					if (listener != null) listener.explosion();
					break;
				}
			}

			for (int i = 0; i < removedShots.size(); i++)
				shots.remove(removedShots.get(i));
		}

		for (int i = 0; i < invaders.size(); i++) {
			Invader invader = invaders.get(i);
			if (invader.position.dst(ship.position) < Ship.SHIP_RADIUS) {
				ship.lives--;
				invaders.remove(invader);
				ship.isExploding = true;
				explosions.add(new Explosion(invader.position));
				explosions.add(new Explosion(ship.position));
				if (listener != null) listener.explosion();
				break;
			}
		}
	}

	private void checkBlockCollision () {
		removedShots.clear();

		for (int i = 0; i < shots.size(); i++) {
			Shot shot = shots.get(i);

			for (int j = 0; j < blocks.size(); j++) {
				Block block = blocks.get(j);
				if (block.position.dst(shot.position) < Block.BLOCK_RADIUS) {
					removedShots.add(shot);
					shot.hasLeftField = true;
					blocks.remove(block);
					break;
				}
			}
		}

		for (int i = 0; i < removedShots.size(); i++)
			shots.remove(removedShots.get(i));
	}

	private void checkNextLevel () {
		if (invaders.size() == 0 && ship.lives > 0) {
			blocks.clear();
			shots.clear();
			shipShot = null;
			Vector3 shipPosition = ship.position;
			int lives = ship.lives;
			populate();
			ship.position.set(shipPosition);
			ship.lives = lives;
			multiplier += 0.1f;
			wave++;
		}
	}

	public void moveShipLeft (float delta, float scale) {
		if (ship.isExploding) return;

		ship.position.x -= delta * Ship.SHIP_VELOCITY * scale;
		if (ship.position.x < PLAYFIELD_MIN_X) ship.position.x = PLAYFIELD_MIN_X;
	}

	public void moveShipRight (float delta, float scale) {
		if (ship.isExploding) return;

		ship.position.x += delta * Ship.SHIP_VELOCITY * scale;
		if (ship.position.x > PLAYFIELD_MAX_X) ship.position.x = PLAYFIELD_MAX_X;
	}

	public void shot () {
		if (shipShot == null && !ship.isExploding) {
			shipShot = new Shot(ship.position, false);
			shots.add(shipShot);
			if (listener != null) listener.shot();
		}
	}
}
