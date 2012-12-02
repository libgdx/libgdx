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
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.btTypedConstraint;
import com.badlogic.gdx.tests.bullet.BaseBulletTest.Entity;
import com.badlogic.gdx.utils.Array;

/** @author xoppa */
public class ConstraintsTest extends BaseBulletTest {

	final Array<btTypedConstraint> constraints = new Array<btTypedConstraint>(); 
	
	@Override
	public void create () {
		super.create();

		final Mesh barMesh = new Mesh(true, 8, 36, new VertexAttribute(Usage.Position, 3, "a_position"));
		barMesh.setVertices(new float[] {5f, 0.5f, 0.5f, 5f, 0.5f, -0.5f, -5f, 0.5f, 0.5f, -5f, 0.5f, -0.5f,
			5f, -0.5f, 0.5f, 5f, -0.5f, -0.5f, -5f, -0.5f, 0.5f, -5f, -0.5f, -0.5f});
		barMesh.setIndices(new short[] {0, 1, 2, 1, 2, 3, // top
			4, 5, 6, 5, 6, 7, // bottom
			0, 2, 4, 4, 6, 2, // front
			1, 3, 5, 5, 7, 3, // back
			2, 3, 6, 6, 7, 3, // left
			0, 1, 4, 4, 5, 1 // right
			});
		world.constructors.put("bar", new Entity.ConstructInfo(barMesh, 0f)); // mass = 0: static body
		
		// Create the entities
		world.add("ground", 0f, 0f, 0f)
			.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		Entity bar = world.add("bar", 0f, 7f, 0f);
		bar.color.set(0.75f + 0.25f * (float)Math.random(), 0.75f + 0.25f * (float)Math.random(), 0.75f + 0.25f * (float)Math.random(), 1f);
		
		Entity box1 = world.add("box", -4.5f, 6f, 0f);
		box1.color.set(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
		btPoint2PointConstraint constraint = new btPoint2PointConstraint(bar.body, box1.body, Vector3.tmp.set(-5, -0.5f, -0.5f), Vector3.tmp2.set(-0.5f, 0.5f, -0.5f));
		world.dynamicsWorld.addConstraint(constraint, false);
		constraints.add(constraint);
		Entity box2 = null;
		for (int i = 0; i < 10; i++) {
			if (i % 2 == 0) {
				box2 = world.add("box", -3.5f + (float)i, 6f, 0f);
				box2.color.set(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
				constraint = new btPoint2PointConstraint(box1.body, box2.body, Vector3.tmp.set(0.5f, -0.5f, 0.5f), Vector3.tmp2.set(-0.5f, -0.5f, 0.5f));
			} else {
				box1 = world.add("box", -3.5f + (float)i, 6f, 0f);
				box1.color.set(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
				constraint = new btPoint2PointConstraint(box2.body, box1.body, Vector3.tmp.set(0.5f, 0.5f, -0.5f), Vector3.tmp2.set(-0.5f, 0.5f, -0.5f));
			}
			world.dynamicsWorld.addConstraint(constraint, false);
			constraints.add(constraint);
		}
		constraint = new btPoint2PointConstraint(bar.body, box1.body, Vector3.tmp.set(5f, -0.5f, -0.5f), Vector3.tmp2.set(0.5f, 0.5f, -0.5f));
		world.dynamicsWorld.addConstraint(constraint, false);
		constraints.add(constraint);
	}
	
	@Override
	public void dispose () {
		for (int i = 0; i < constraints.size; i++) {
			world.dynamicsWorld.removeConstraint(constraints.get(i));
			constraints.get(i).delete();
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
