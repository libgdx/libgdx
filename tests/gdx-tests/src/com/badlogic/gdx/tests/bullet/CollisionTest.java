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

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.ContactResultCallback;
import com.badlogic.gdx.physics.bullet.btCollisionObject;
import com.badlogic.gdx.physics.bullet.btCollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.btManifoldPoint;
import com.badlogic.gdx.physics.bullet.btPersistentManifold;
import com.badlogic.gdx.tests.bullet.CollisionWorldTest.TestContactResultCallback;
import com.badlogic.gdx.utils.Array;

/** @author Xoppa */
public class CollisionTest extends ShootTest {
	BulletEntity projectile;
	Array<BulletEntity> hits = new Array<BulletEntity>();
	Array<BulletEntity> contacts = new Array<BulletEntity>();
	Array<Color> colors = new Array<Color>();
	
	public class TestContactResultCallback extends ContactResultCallback
	{
		@Override
		public float addSingleResult (btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
			btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
			btCollisionObject other = colObj0Wrap.getM_collisionObject() == projectile.body ?
					colObj1Wrap.getM_collisionObject() : colObj0Wrap.getM_collisionObject();
			if (other != null && other.userData != null && other.userData instanceof BulletEntity) {
				BulletEntity ent = (BulletEntity)other.userData;
				if (ent != ground && !hits.contains(ent, true))
					hits.add((BulletEntity)other.userData);
			}
			return 0f;
		}
	}
	TestContactResultCallback contactCB;
	
	public void updateContactInfo() {
		int n = world.dispatcher.getNumManifolds();
		for (int i = 0; i < n; i++) {
			btPersistentManifold manifold = world.dispatcher.getManifoldByIndexInternal(i);
			btCollisionObject objA = manifold.getBody0();
			btCollisionObject objB = manifold.getBody1();
			if (objA != ground.body && objB != ground.body) {
				if (objA.userData != null && objA.userData instanceof BulletEntity) {
					BulletEntity ent = (BulletEntity)objA.userData; 
					if (ent != projectile && !contacts.contains(ent, true) && !hits.contains(ent, true))
						contacts.add(ent);
				}
				if (objB.userData != null && objB.userData instanceof BulletEntity) {
					BulletEntity ent = (BulletEntity)objB.userData;
					if (ent != projectile && !contacts.contains(ent, true) && !hits.contains(ent, true))
						contacts.add(ent);
				}
			}
		}
	}

	@Override
	public void create () {
		super.create();
		
		contactCB = new TestContactResultCallback();
	}
	
	@Override
	public void render () {
		process();
	}
	
	public void process() {
		Color color = null;
		update();
		hits.clear();
		contacts.clear();
		colors.clear();
		
		// Note that this might miss collisions, use InternalTickCallback to check for collision on every tick.
		// See InternalTickTest on how to implement it.
		
		// Check what the projectile hits
		if (projectile != null)
			world.collisionWorld.contactTest(projectile.body, contactCB);
		// Check for other collisions
		updateContactInfo();
		
		color = projectile.color;
		projectile.color = Color.RED;
		if (hits.size > 0) {
			for (int i = 0; i < hits.size; i++) {
				colors.add(hits.get(i).color);
				hits.get(i).color = Color.RED;
			}
		}
		if (contacts.size > 0) {
			for (int i = 0; i < contacts.size; i++) {
				colors.add(contacts.get(i).color);
				contacts.get(i).color = Color.BLUE;
			}
		}
		render(false);
		if (hits.size > 0) {
			projectile.color = color;
			for (int i = 0; i < hits.size; i++)
				hits.get(i).color = colors.get(i);
		}
		if (contacts.size > 0) {
			for (int i = 0; i < contacts.size; i++)
				contacts.get(i).color = colors.get(hits.size+i);
		}
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		projectile = shoot(x, y);
		return true;
	}
}
