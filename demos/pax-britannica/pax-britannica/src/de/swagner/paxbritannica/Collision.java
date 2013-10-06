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

 package de.swagner.paxbritannica;

import com.badlogic.gdx.math.Intersector;

public class Collision {

	private static Bullet bullet;
	private static Ship ship;
	
	public static void collisionCheck() {
		
		for (int i=0; i< GameInstance.getInstance().bullets.size;i++) {
			bullet = GameInstance.getInstance().bullets.get(i);
			if (bullet.alive) {
				for (int n=0; n< GameInstance.getInstance().fighters.size;n++) {
					ship =  GameInstance.getInstance().fighters.get(n);
					collisionCheck(bullet, ship);
				}
				for (int n=0; n< GameInstance.getInstance().bombers.size;n++) {
					ship =  GameInstance.getInstance().bombers.get(n);
					collisionCheck(bullet, ship);
				}
				for (int n=0; n< GameInstance.getInstance().frigates.size;n++) {
					ship =  GameInstance.getInstance().frigates.get(n);
					collisionCheck(bullet, ship);
				}
				for (int n=0; n< GameInstance.getInstance().factorys.size;n++) {
					ship =  GameInstance.getInstance().factorys.get(n);
					collisionCheck(bullet, ship);
				}
			}

		}
	}

	private static void collisionCheck(Bullet bullet, Ship ship) {
		if (bullet.id!=ship.id && ship.alive) {
			
			for(int i = 0; i<ship.collisionPoints.size;++i) {
				if(Intersector.isPointInPolygon(bullet.collisionPoints, ship.collisionPoints.get(i))) {
					ship.damage(bullet.damage);
					GameInstance.getInstance().bulletHit(ship, bullet);
					bullet.alive = false;
					return;
				}
			}
			
			for(int i = 0; i<bullet.collisionPoints.size;++i) {
				if(Intersector.isPointInPolygon(ship.collisionPoints, bullet.collisionPoints.get(i))) {
					ship.damage(bullet.damage);
					GameInstance.getInstance().bulletHit(ship, bullet);
					bullet.alive = false;
					return;
				}
			}
		}
	}

}
