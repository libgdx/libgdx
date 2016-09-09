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

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.extras.btBulletWorldImporter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;

public class ImportTest extends BaseBulletTest {
	btBulletWorldImporter importer;
	Model model;

	public class MyImporter extends btBulletWorldImporter {
		public MyImporter (btDynamicsWorld world) {
			super(world);
		}

		@Override
		public btRigidBody createRigidBody (boolean isDynamic, float mass, Matrix4 startTransform, btCollisionShape shape,
			String bodyName) {
			Vector3 localInertia = new Vector3();
			if (mass > 0f) shape.calculateLocalInertia(mass, localInertia);

			btRigidBody result = new btRigidBody(mass, null, shape, localInertia);

			String nodeName = bodyName.split("_", 2)[0] + "_model";
			ModelInstance instance = new ModelInstance(model, nodeName, true, true);
			instance.transform.set(startTransform);

			BulletEntity entity = new BulletEntity(instance, result);
			ImportTest.this.world.add(entity);

			return result;
		}
	}

	@Override
	public void create () {
		super.create();

		ModelLoader g3djLoader = new G3dModelLoader(new JsonReader());
		model = g3djLoader.loadModel(Gdx.files.internal("data/g3d/btscene1.g3dj"));
		disposables.add(model);

		importer = new MyImporter((btDynamicsWorld)world.collisionWorld);
		importer.loadFile(Gdx.files.internal("data/g3d/btscene1.bullet"));

		camera.position.set(10f, 15f, 20f);
		camera.up.set(0, 1, 0);
		camera.lookAt(-10, 8, 0);
		camera.update();
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}

	@Override
	public void dispose () {
		super.dispose();

		importer.deleteAllData();
		importer.dispose();
		importer = null;
	}
}
