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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

public class BasicShapesTest extends BaseBulletTest {
	@Override
	public void create () {
		super.create();

		final Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		disposables.add(texture);
		final Material material = new Material(TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1, 1, 1, 1),
			FloatAttribute.createShininess(8f));
		final long attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

		final Model sphere = modelBuilder.createSphere(4f, 4f, 4f, 24, 24, material, attributes);
		disposables.add(sphere);
		world.addConstructor("sphere", new BulletConstructor(sphere, 10f, new btSphereShape(2f)));

		final Model cylinder = modelBuilder.createCylinder(4f, 6f, 4f, 16, material, attributes);
		disposables.add(cylinder);
		world.addConstructor("cylinder", new BulletConstructor(cylinder, 10f, new btCylinderShape(tmpV1.set(2f, 3f, 2f))));

		final Model capsule = modelBuilder.createCapsule(2f, 6f, 16, material, attributes);
		disposables.add(capsule);
		world.addConstructor("capsule", new BulletConstructor(capsule, 10f, new btCapsuleShape(2f, 2f)));

		final Model box = modelBuilder.createBox(4f, 4f, 2f, material, attributes);
		disposables.add(box);
		world.addConstructor("box2", new BulletConstructor(box, 10f, new btBoxShape(tmpV1.set(2f, 2f, 1f))));

		final Model cone = modelBuilder.createCone(4f, 6f, 4f, 16, material, attributes);
		disposables.add(cone);
		world.addConstructor("cone", new BulletConstructor(cone, 10f, new btConeShape(2f, 6f)));

		// Create the entities
		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);
		world.add("sphere", 0, 5, 5);
		world.add("cylinder", 5, 5, 0);
		world.add("box2", 0, 5, 0);
		world.add("capsule", 5, 5, 5);
		world.add("cone", 10, 5, 0);
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}
