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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.dynamics.*;

/** @author jsjolund, ax-rwnd and mjolnir92 */
public class VehicleFilterTest extends VehicleTest {

	static final short FILTER_GROUP = (short)(1 << 11);
	static final short FILTER_MASK = FILTER_GROUP;

	@Override
	protected btVehicleRaycaster getRaycaster () {
		FilterableVehicleRaycaster raycaster = new FilterableVehicleRaycaster((btDynamicsWorld)world.collisionWorld);
		raycaster.setCollisionFilterGroup(FILTER_GROUP);
		raycaster.setCollisionFilterMask(FILTER_MASK);
		return raycaster;
	}

	@Override
	public void create () {
		super.create();
		chassis.setColor(Color.BLUE);
	}

	@Override
	public BulletWorld createWorld () {
		// Force all objects to same collision group and filter
		return new BulletWorld() {
			@Override
			public void add (final BulletEntity entity) {
				world.entities.add(entity);
				if (entity.body != null) {
					if (entity.body instanceof btRigidBody)
						((btDiscreteDynamicsWorld)collisionWorld).addRigidBody((btRigidBody)entity.body, FILTER_GROUP, FILTER_MASK);
					else
						collisionWorld.addCollisionObject(entity.body, FILTER_GROUP, FILTER_MASK);
					// Store the index of the entity in the collision object.
					entity.body.setUserValue(entities.size - 1);
				}
			}
		};
	}
}
