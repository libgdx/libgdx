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
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.badlogic.gdx.utils.Array;

/** @author xoppa */
public class ConstraintsTest extends BaseBulletTest {

	final Array<btTypedConstraint> constraints = new Array<btTypedConstraint>();

	@Override
	public void create () {
		super.create();

		final Model barModel = modelBuilder.createBox(10f, 1f, 1f, new Material(new ColorAttribute(ColorAttribute.Diffuse,
			Color.WHITE)), Usage.Position | Usage.Normal);
		disposables.add(barModel);
		world.addConstructor("bar", new BulletConstructor(barModel, 0f)); // mass = 0: static body

		// Create the entities
		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		BulletEntity bar = world.add("bar", 0f, 7f, 0f);
		bar.setColor(0.75f + 0.25f * (float)Math.random(), 0.75f + 0.25f * (float)Math.random(),
			0.75f + 0.25f * (float)Math.random(), 1f);

		BulletEntity box1 = world.add("box", -4.5f, 6f, 0f);
		box1.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
			1f);
		btPoint2PointConstraint constraint = new btPoint2PointConstraint((btRigidBody)bar.body, (btRigidBody)box1.body,
			tmpV1.set(-5, -0.5f, -0.5f), tmpV2.set(-0.5f, 0.5f, -0.5f));
		((btDynamicsWorld)world.collisionWorld).addConstraint(constraint, false);
		constraints.add(constraint);
		BulletEntity box2 = null;
		for (int i = 0; i < 10; i++) {
			if (i % 2 == 0) {
				box2 = world.add("box", -3.5f + (float)i, 6f, 0f);
				box2.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
					0.5f + 0.5f * (float)Math.random(), 1f);
				constraint = new btPoint2PointConstraint((btRigidBody)box1.body, (btRigidBody)box2.body, tmpV1.set(0.5f, -0.5f,
					0.5f), tmpV2.set(-0.5f, -0.5f, 0.5f));
			} else {
				box1 = world.add("box", -3.5f + (float)i, 6f, 0f);
				box1.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
					0.5f + 0.5f * (float)Math.random(), 1f);
				constraint = new btPoint2PointConstraint((btRigidBody)box2.body, (btRigidBody)box1.body, tmpV1.set(0.5f, 0.5f,
					-0.5f), tmpV2.set(-0.5f, 0.5f, -0.5f));
			}
			((btDynamicsWorld)world.collisionWorld).addConstraint(constraint, false);
			constraints.add(constraint);
		}
		constraint = new btPoint2PointConstraint((btRigidBody)bar.body, (btRigidBody)box1.body, tmpV1.set(5f, -0.5f, -0.5f),
			tmpV2.set(0.5f, 0.5f, -0.5f));
		((btDynamicsWorld)world.collisionWorld).addConstraint(constraint, false);
		constraints.add(constraint);
	}

	@Override
	public void dispose () {
		for (int i = 0; i < constraints.size; i++) {
			((btDynamicsWorld)world.collisionWorld).removeConstraint(constraints.get(i));
			constraints.get(i).dispose();
		}
		constraints.clear();
		super.dispose();
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}
