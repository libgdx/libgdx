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
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

/** @author xoppa */
public class MeshShapeTest extends BaseBulletTest {

	@Override
	public void create () {
		super.create();

		final Model sphereModel = modelBuilder.createSphere(0.5f, 0.5f, 0.5f, 8, 8,
			new Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE)), Usage.Position
				| Usage.Normal);
		disposables.add(sphereModel);
		final BulletConstructor sphereConstructor = new BulletConstructor(sphereModel, 0.25f, new btSphereShape(0.25f));
		sphereConstructor.bodyInfo.setRestitution(1f);
		world.addConstructor("sphere", sphereConstructor);

		final Model sceneModel = objLoader.loadModel(Gdx.files.internal("data/scene.obj"));
		disposables.add(sceneModel);
		final BulletConstructor sceneConstructor = new BulletConstructor(sceneModel, 0f, new btBvhTriangleMeshShape(
			sceneModel.meshParts));
		sceneConstructor.bodyInfo.setRestitution(0.25f);
		world.addConstructor("scene", sceneConstructor);

		world.add("scene", (new Matrix4()).setToTranslation(0f, 2f, 0f).rotate(Vector3.Y, -90)).setColor(
			0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);

		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		for (float x = -3; x < 7; x++) {
			for (float z = -5; z < 5; z++) {
				world.add("sphere", x, 10f + (float)Math.random() * 0.1f, z).setColor(0.5f + 0.5f * (float)Math.random(),
					0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
			}
		}
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}
