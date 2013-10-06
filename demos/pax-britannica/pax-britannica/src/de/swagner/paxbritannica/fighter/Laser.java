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

 package de.swagner.paxbritannica.fighter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Bullet;
import de.swagner.paxbritannica.Resources;

public class Laser extends Bullet {

	float delta;
	
	public Laser(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);
		
		bulletSpeed = 1000;
		damage = 10;
		
		this.velocity = new Vector2().set(facing).scl(bulletSpeed);
		
		this.set(Resources.getInstance().laser);
		this.setOrigin(0,0);
	}
	
	@Override
	public void draw(SpriteBatch spriteBatch) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		velocity.scl( (float) Math.pow(1.03f, delta * 30.f));
		super.draw(spriteBatch);
	}
	

}
