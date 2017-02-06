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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;
import com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback;
import com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback.EFlags;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;

/** @author jsjolund */
public class TriangleRaycastTest extends BaseBulletTest {

	private class MyTriangleRaycastCallback extends btTriangleRaycastCallback {

		public Vector3 hitNormalLocal = new Vector3();
		public float hitFraction = 1;
		public int partId = -1;
		public int triangleIndex = -1;

		private btVector3 tmpSetFrom = new btVector3();
		private btVector3 tmpSetTo = new btVector3();

		public MyTriangleRaycastCallback (Vector3 from, Vector3 to) {
			super(from, to);
		}

		public void clearReport () {
			hitNormalLocal.setZero();
			hitFraction = 1;
			partId = -1;
			triangleIndex = -1;
		}

		@Override
		public void setHitFraction (float hitFraction) {
			super.setHitFraction(hitFraction);
			this.hitFraction = hitFraction;
		}

		@Override
		public float reportHit (Vector3 hitNormalLocal, float hitFraction, int partId, int triangleIndex) {
			// The hit with lowest hitFraction is closest to the ray origin.
			// We need to find the lowest hitFraction since the super class does not handle it for us.
			if (hitFraction < this.hitFraction) {
				this.hitNormalLocal.set(hitNormalLocal);
				this.hitFraction = hitFraction;
				this.partId = partId;
				this.triangleIndex = triangleIndex;
			}
			return hitFraction;
		}

		public void setFrom (Vector3 value) {
			tmpSetFrom.setValue(value.x, value.y, value.z);
			super.setFrom(tmpSetFrom);
		}

		public void setTo (Vector3 value) {
			tmpSetTo.setValue(value.x, value.y, value.z);
			super.setTo(tmpSetTo);
		}

		@Override
		public void dispose () {
			tmpSetFrom.dispose();
			tmpSetTo.dispose();
			super.dispose();
		}
	}

	private Model model;
	private btBvhTriangleMeshShape triangleShape;
	private MyTriangleRaycastCallback triangleRaycastCallback;
	private Vector3[] selectedTriangleVertices = {new Vector3(), new Vector3(), new Vector3()};
	private ShapeRenderer shapeRenderer;
	private Vector3 rayFrom = new Vector3();
	private Vector3 rayTo = new Vector3();

	@Override
	public void create () {
		super.create();
		instructions = "Tap a triangle to ray cast\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom";

		shapeRenderer = new ShapeRenderer();

		model = objLoader.loadModel(Gdx.files.internal("data/scene.obj"));
		model.materials.get(0).clear();
		model.materials.get(0).set(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE));

		// Only indexed BvhTriangleMeshShape can be used for triangle picking.
		btTriangleIndexVertexArray vertexArray = new btTriangleIndexVertexArray(model.meshParts);
		triangleShape = new btBvhTriangleMeshShape(vertexArray, true);

		triangleRaycastCallback = new MyTriangleRaycastCallback(Vector3.Zero, Vector3.Zero);
		// Ignore intersection with mesh backfaces.
		triangleRaycastCallback.setFlags(EFlags.kF_FilterBackfaces);

		world.addConstructor("scene", new BulletConstructor(model, 0, triangleShape));
		world.add("scene", 0, 0, 0);

		disposables.add(model);
		disposables.add(triangleRaycastCallback);
		disposables.add(triangleShape);
		disposables.add(vertexArray);
		disposables.add(shapeRenderer);

	}

	@Override
	public void render () {
		super.render();
		Gdx.gl.glLineWidth(5);
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(1, 0, 0, 1f);
		shapeRenderer.line(selectedTriangleVertices[0], selectedTriangleVertices[1]);
		shapeRenderer.line(selectedTriangleVertices[1], selectedTriangleVertices[2]);
		shapeRenderer.line(selectedTriangleVertices[2], selectedTriangleVertices[0]);
		shapeRenderer.end();
		Gdx.gl.glLineWidth(1);
	}

	@Override
	public boolean tap (float screenX, float screenY, int count, int button) {
		Ray ray = camera.getPickRay(screenX, screenY);
		rayFrom.set(ray.origin);
		rayTo.set(ray.direction).scl(100).add(rayFrom);

		// Clean the callback object for reuse.
		triangleRaycastCallback.setHitFraction(1);
		triangleRaycastCallback.clearReport();
		triangleRaycastCallback.setFrom(rayFrom);
		triangleRaycastCallback.setTo(rayTo);

		// Ray casting is performed directly on the collision shape.
		// The callback specifies the intersected MeshPart as well as triangle.
		triangleShape.performRaycast(triangleRaycastCallback, rayFrom, rayTo);
		int currentTriangleIndex = triangleRaycastCallback.triangleIndex;
		int currentPartId = triangleRaycastCallback.partId;

		if (currentTriangleIndex == -1 || currentPartId == -1) {
			// No intersection was found.
			return false;
		}

		// Get the position coordinates of the vertices belonging to intersected triangle.
		Mesh mesh = model.meshParts.get(currentPartId).mesh;
		FloatBuffer verticesBuffer = mesh.getVerticesBuffer();
		ShortBuffer indicesBuffer = mesh.getIndicesBuffer();

		int posOffset = mesh.getVertexAttributes().findByUsage(VertexAttributes.Usage.Position).offset / 4;
		int vertexSize = mesh.getVertexSize() / 4;
		int currentTriangleFirstVertexIndex = currentTriangleIndex * 3;

		// Store the three vertices belonging to the selected triangle.
		for (int i = 0; i < 3; i++) {
			int currentVertexIndex = indicesBuffer.get(currentTriangleFirstVertexIndex + i);
			int j = currentVertexIndex * vertexSize + posOffset;
			float x = verticesBuffer.get(j++);
			float y = verticesBuffer.get(j++);
			float z = verticesBuffer.get(j);
			selectedTriangleVertices[i].set(x, y, z);
		}
		return true;
	}

}
