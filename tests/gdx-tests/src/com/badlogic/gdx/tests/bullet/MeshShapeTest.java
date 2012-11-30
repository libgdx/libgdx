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
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.PHY_ScalarType;
import com.badlogic.gdx.physics.bullet.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btIndexedMesh;
import com.badlogic.gdx.physics.bullet.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.btSphereShape;
import com.badlogic.gdx.physics.bullet.btStridingMeshInterface;
import com.badlogic.gdx.physics.bullet.btTriangleIndexVertexArray;
import com.badlogic.gdx.physics.bullet.btTypedConstraint;
import com.badlogic.gdx.tests.bullet.BaseBulletTest.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/** @author xoppa */
public class MeshShapeTest extends BaseBulletTest {

	@Override
	public void create () {
		super.create();
		
		final Mesh sphereMesh = ObjLoader.loadObj(Gdx.files.internal("data/sphere.obj").read());
		sphereMesh.scale(0.25f, 0.25f, 0.25f);

		final Mesh sceneMesh = ObjLoader.loadObj(Gdx.files.internal("data/scene.obj").read(), true, true); // we need indices for this test
		
		world.constructors.put("sphere", new Entity.ConstructInfo(sphereMesh, 0.25f, new btSphereShape(sphereMesh.calculateBoundingBox().getDimensions().x * 0.5f)));
		world.constructors.put("scene", new Entity.ConstructInfo(sceneMesh, 0f, createMeshShape(sceneMesh)));
		
		Entity scene = world.add("scene", 0f, 2f, 0f);
		scene.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		scene.worldTransform.transform.rotate(Vector3.Y, -90);
		// Since the transform is changed, it's needed to apply it again.
		scene.body.setMotionState(scene.worldTransform);

		world.add("ground", 0f, 0f, 0f)
			.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		for (float x = -3; x < 7; x++) {
			for (float z = -5; z < 5; z++) {
				world.add("sphere", x, 10f, z)
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
	
	// NOTE: The following is subject to change as it involves some nasty memory management and overriding JNI classes.
	
	// Create a TriangleMeshShape based on a Mesh
	public btCollisionShape createMeshShape(Mesh mesh) {
		short[] indices = new short[mesh.getNumIndices()];
		float[] vertices = new float[mesh.getNumVertices()*mesh.getVertexSize()/4];
		mesh.getIndices(indices);
		mesh.getVertices(vertices);
		btIndexedMesh indexedMesh = new TestIndexedMesh();
		indexedMesh.setM_indexType(PHY_ScalarType.PHY_SHORT);
		indexedMesh.setM_numTriangles(mesh.getNumIndices()/3);
		indexedMesh.setM_numVertices(mesh.getNumVertices());
		indexedMesh.setM_triangleIndexStride(6);
		indexedMesh.setM_vertexStride(mesh.getVertexSize());
		indexedMesh.setM_vertexType(PHY_ScalarType.PHY_FLOAT);
		indexedMesh.setTriangleIndexBase(indices, indices.length);
		indexedMesh.setVertexBase(vertices, vertices.length);
		btTriangleIndexVertexArray meshInterface = new TestTriangleIndexVertexArray();
		meshInterface.addIndexedMesh(indexedMesh, PHY_ScalarType.PHY_SHORT);
		return new TestBvhTriangleMeshShape(meshInterface,true);
	}
	
	// Need to free memory (created for holding a copy of the indices and vertices)
	class TestIndexedMesh extends btIndexedMesh {
		boolean disposed = false;
		@Override
		public synchronized void delete() {
			if (!disposed) {
				dispose();
				disposed = true;
			}
			super.delete();
		}
	}
	
	// Need to keep reference to the meshes to avoid memory being freed too early.
	class TestTriangleIndexVertexArray extends btTriangleIndexVertexArray {
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
	
	// Need to keep reference to meshInterface to avoid memory being freed too early.
	class TestBvhTriangleMeshShape extends btBvhTriangleMeshShape {
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