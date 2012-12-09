package com.badlogic.gdx.tests.bullet;
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btCollisionObject;

/** @author xoppa */
public class KinematicTest extends BaseBulletTest {
	Entity kinematicBox;
	final static Vector3 position = new Vector3(5f, 0.5f, 0f); 
	
	@Override
	public void create () {
		super.create();

		// Create the entities
		world.add("ground", 0f, 0f, 0f)
			.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		kinematicBox = world.add("staticbox", position.x, position.y, position.z);
		kinematicBox.body.setCollisionFlags(kinematicBox.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		kinematicBox.body.setActivationState(4); // 4 is the flag value of DISABLE_DEACTIVATION 
	}
	
	private float angle = 0f;
	@Override
	public void render () {
		angle = (angle + Gdx.graphics.getDeltaTime() * 360f / 5f) % 360;
		kinematicBox.worldTransform.transform.idt().rotate(Vector3.Y, angle).translate(position);
		super.render();
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}