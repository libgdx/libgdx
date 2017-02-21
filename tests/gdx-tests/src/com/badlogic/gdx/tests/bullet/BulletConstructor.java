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

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;

/** @author xoppa Holds the information necessary to create a bullet btRigidBody. This class should outlive the btRigidBody (entity)
 *         itself. */
public class BulletConstructor extends BaseWorld.Constructor<BulletEntity> {
	public btRigidBodyConstructionInfo bodyInfo = null;
	public btCollisionShape shape = null;
	private final static Vector3 tmpV = new Vector3();

	/** Specify null for the shape to use only the renderable part of this entity and not the physics part. */
	public BulletConstructor (final Model model, final float mass, final btCollisionShape shape) {
		create(model, mass, shape);
	}

	/** Specify null for the shape to use only the renderable part of this entity and not the physics part. */
	public BulletConstructor (final Model model, final btCollisionShape shape) {
		this(model, -1f, shape);
	}

	/** Creates a btBoxShape with the specified dimensions. */
	public BulletConstructor (final Model model, final float mass, final float width, final float height, final float depth) {
		create(model, mass, width, height, depth);
	}

	/** Creates a btBoxShape with the specified dimensions and NO rigidbody. */
	public BulletConstructor (final Model model, final float width, final float height, final float depth) {
		this(model, -1f, width, height, depth);
	}

	/** Creates a btBoxShape with the same dimensions as the shape. */
	public BulletConstructor (final Model model, final float mass) {
		final BoundingBox boundingBox = new BoundingBox();
		model.calculateBoundingBox(boundingBox);
		create(model, mass, boundingBox.getWidth(), boundingBox.getHeight(), boundingBox.getDepth());
	}

	/** Creates a btBoxShape with the same dimensions as the shape and NO rigidbody. */
	public BulletConstructor (final Model model) {
		this(model, -1f);
	}

	private void create (final Model model, final float mass, final float width, final float height, final float depth) {
		// Create a simple boxshape
		create(model, mass, new btBoxShape(tmpV.set(width * 0.5f, height * 0.5f, depth * 0.5f)));
	}

	private void create (final Model model, final float mass, final btCollisionShape shape) {
		this.model = model;
		this.shape = shape;

		if (shape != null && mass >= 0) {
			// Calculate the local inertia, bodies with no mass are static
			Vector3 localInertia;
			if (mass == 0)
				localInertia = Vector3.Zero;
			else {
				shape.calculateLocalInertia(mass, tmpV);
				localInertia = tmpV;
			}

			// For now just pass null as the motionstate, we'll add that to the body in the entity itself
			bodyInfo = new btRigidBodyConstructionInfo(mass, null, shape, localInertia);
		}
	}

	@Override
	public void dispose () {
		// Don't rely on the GC
		if (bodyInfo != null) bodyInfo.dispose();
		if (shape != null) shape.dispose();
		// Remove references so the GC can do it's work
		bodyInfo = null;
		shape = null;
	}

	@Override
	public BulletEntity construct (float x, float y, float z) {
		if (bodyInfo == null && shape != null) {
			btCollisionObject obj = new btCollisionObject();
			obj.setCollisionShape(shape);
			return new BulletEntity(model, obj, x, y, z);
		} else
			return new BulletEntity(model, bodyInfo, x, y, z);
	}

	@Override
	public BulletEntity construct (final Matrix4 transform) {
		if (bodyInfo == null && shape != null) {
			btCollisionObject obj = new btCollisionObject();
			obj.setCollisionShape(shape);
			return new BulletEntity(model, obj, transform);
		} else
			return new BulletEntity(model, bodyInfo, transform);
	}
}
