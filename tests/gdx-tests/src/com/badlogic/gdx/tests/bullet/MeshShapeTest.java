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

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.PHY_ScalarType;
import com.badlogic.gdx.physics.bullet.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btIndexedMesh;
import com.badlogic.gdx.physics.bullet.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.btSphereShape;
import com.badlogic.gdx.physics.bullet.btStridingMeshInterface;
import com.badlogic.gdx.physics.bullet.btTriangleIndexVertexArray;
import com.badlogic.gdx.physics.bullet.btTypedConstraint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/** @author xoppa */
public class MeshShapeTest extends BaseBulletTest {

	@Override
	public void create () {
		super.create();
		
		final StillModel sphereModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/sphere.obj"));
		sphereModel.subMeshes[0].getMesh().scale(0.25f, 0.25f, 0.25f);
		final BoundingBox sphereBounds = new BoundingBox();
		sphereModel.getBoundingBox(sphereBounds);
	
		final BulletConstructor sphereConstructor = new BulletConstructor(sphereModel, 0.25f, new btSphereShape(sphereBounds.getDimensions().x * 0.5f));
		sphereConstructor.bodyInfo.setM_restitution(1f);
		world.addConstructor("sphere", sphereConstructor);
		
		final StillModel sceneModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/scene.obj"));
		final BulletConstructor sceneConstructor = new BulletConstructor(sceneModel, 0f, new btBvhTriangleMeshShape(true, sceneModel));
		sceneConstructor.bodyInfo.setM_restitution(0.25f);
		world.addConstructor("scene", sceneConstructor);
		
		world.add("scene", (new Matrix4()).setToTranslation(0f, 2f, 0f).rotate(Vector3.Y, -90))
			.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);

		world.add("ground", 0f, 0f, 0f)
			.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		for (float x = -3; x < 7; x++) {
			for (float z = -5; z < 5; z++) {
				world.add("sphere", x, 10f + (float)Math.random() * 0.1f, z)
					.color.set(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
			}
		}
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}