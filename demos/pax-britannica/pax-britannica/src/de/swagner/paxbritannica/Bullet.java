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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Bullet extends Ship {

	private float buffer = 500;
	public float damage=0;
	public float bulletSpeed = 0f;

	public Bullet(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);
	}

	@Override
	public void draw(SpriteBatch spriteBatch) {
		if(alive == false) return;
		if( !Targeting.onScreen(collisionCenter,buffer)) {
			alive = false;
		} else if(velocity.len()<=5) {
			alive = false;
			GameInstance.getInstance().explosionParticles.addTinyExplosion(collisionCenter);
		} else {		
			super.draw(spriteBatch);
		}
		
	}
}
