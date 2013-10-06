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

 package de.swagner.paxbritannica.frigate;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Bullet;
import de.swagner.paxbritannica.Resources;

public class Missile extends Bullet {

	private MissileAI ai = new MissileAI(this);
	
	public Missile(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);
		turnSpeed = 300f;
		accel = 300.0f;	
		bulletSpeed = 50;
		this.velocity = new Vector2().set(facing).scl(bulletSpeed);
		damage = 50;
		
		this.set(Resources.getInstance().missile);
		this.setOrigin(this.getWidth()/2, this.getHeight()/2);
	}
	
	@Override
	public void draw(SpriteBatch spriteBatch) {
		ai.update();
		
		super.draw(spriteBatch);
	}
}
