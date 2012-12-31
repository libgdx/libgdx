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
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.PHY_ScalarType;
import com.badlogic.gdx.physics.bullet.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btIndexedMesh;
import com.badlogic.gdx.physics.bullet.btSphereShape;
import com.badlogic.gdx.physics.bullet.btStridingMeshInterface;
import com.badlogic.gdx.physics.bullet.btTriangleIndexVertexArray;
import com.badlogic.gdx.utils.Array;

/** @author xoppa */
public class MeshShapeTest extends BaseBulletTest {

	@Override
	public void create () {
		super.create();
		

		final StillModel model = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/sphere.obj"));
		
		final Mesh sphereMesh = model.subMeshes[0].getMesh();
		sphereMesh.scale(0.25f, 0.25f, 0.25f);

		final StillModel sceneModel = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/scene.obj")); // we need indices for this test 
		final Mesh sceneMesh = sceneModel.subMeshes[0].getMesh();
		
		final BulletConstructor sphereConstructor = new BulletConstructor(sphereMesh, 0.25f, new btSphereShape(sphereMesh.calculateBoundingBox().getDimensions().x * 0.5f));
		sphereConstructor.bodyInfo.setM_restitution(1f);
		world.addConstructor("sphere", sphereConstructor);
		final BulletConstructor sceneConstructor = new BulletConstructor(sceneMesh, 0f, createMeshShape(sceneMesh));
		sceneConstructor.bodyInfo.setM_restitution(0.25f);
		world.addConstructor("scene", sceneConstructor);
		
		BulletEntity scene = world.add("scene", 0f, 2f, 0f);
		scene.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		scene.transform.rotate(Vector3.Y, -90);
		// Since the transform is changed, it's needed to apply it again.
		scene.body.setWorldTransform(scene.transform);

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
	
	// Create a TriangleMeshShape based on a Mesh
	public static btCollisionShape createMeshShape(Mesh mesh) {
		btIndexedMesh indexedMesh = new btIndexedMesh();
		indexedMesh.setM_indexType(PHY_ScalarType.PHY_SHORT);
		indexedMesh.setM_numTriangles(mesh.getNumIndices()/3);
		indexedMesh.setM_numVertices(mesh.getNumVertices());
		indexedMesh.setM_triangleIndexStride(6);
		indexedMesh.setM_vertexStride(mesh.getVertexSize());
		indexedMesh.setM_vertexType(PHY_ScalarType.PHY_FLOAT);
		indexedMesh.setTriangleIndexBase(mesh.getIndicesBuffer());
		indexedMesh.setVertexBase(mesh.getVerticesBuffer());
		btTriangleIndexVertexArray meshInterface = new TestTriangleIndexVertexArray();
		meshInterface.addIndexedMesh(indexedMesh, PHY_ScalarType.PHY_SHORT);
		return new TestBvhTriangleMeshShape(meshInterface,true);
	}
	
	/** 
	 * Convenience class that keeps a reference of the sub meshes.
	 * Don't use this method if the btIndexedMesh instances are shared amongst other btTriangleIndexVertexArray instances.
	 */
	public static class TestTriangleIndexVertexArray extends btTriangleIndexVertexArray {
		Array<btIndexedMesh> meshes = new Array<btIndexedMesh>();
		
		@Override
		public void addIndexedMesh(btIndexedMesh mesh, int indexType) {
			super.addIndexedMesh(mesh, indexType);
			meshes.add(mesh);
		}
		
		@Override
		public synchronized void delete() {
			super.delete();
			for (int i = 0; i < meshes.size; i++)
				meshes.get(i).delete();
			meshes.clear();
		}
	}
	
	/** 
	 * Convenience class that keeps a reference of the mesh interface 
	 * Don't use this method if the btStridingMeshInterface is shared amongst other btBvhTriangleMeshShape instances. 
	 */
	public static class TestBvhTriangleMeshShape extends btBvhTriangleMeshShape {
		btStridingMeshInterface meshInterface;
		public TestBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression) {
			super(meshInterface, useQuantizedAabbCompression);
			this.meshInterface = meshInterface;
		}
		
		@Override
		public synchronized void delete() {
			super.delete();
			if (meshInterface != null)
				meshInterface.delete();
			meshInterface = null;
		}
	}
}