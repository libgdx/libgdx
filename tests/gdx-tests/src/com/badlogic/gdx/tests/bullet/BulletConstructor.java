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
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.btBoxShape;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btRigidBodyConstructionInfo;

/** @author xoppa
 *  Holds the information necessary to create a bullet btRigidBody. This class should outlive the btRigidBody (entity) itself.
 */
public class BulletConstructor extends BaseWorld.Constructor<BulletEntity> {
	public btRigidBodyConstructionInfo bodyInfo = null;
	public btCollisionShape shape = null;
		
	/**
	 * Specify null for the shape to use only the renderable part of this entity and not the physics part. 
	 */
	public BulletConstructor (final Model model, final float mass, final btCollisionShape shape) {
		create(model, mass, shape);
	}

	/**
	 * Creates a btBoxShape with the specified dimensions.
	 */
	public BulletConstructor (final Model model, final float mass, final float width, final float height, final float depth) {
		create(model, mass, width, height, depth);
	}
	
	/**
	 * Creates a btBoxShape with the same dimensions as the shape.
	 */
	public BulletConstructor (final Model model, final float mass) {
		final BoundingBox boundingBox = new BoundingBox(); 
		model.getBoundingBox(boundingBox);
		final Vector3 dimensions = boundingBox.getDimensions();
		create(model, mass, dimensions.x, dimensions.y, dimensions.z);
	}
	
	private void create (final Model model, final float mass, final float width, final float height, final float depth) {			
		// Create a simple boxshape
		create(model, mass, new btBoxShape(Vector3.tmp.set(width * 0.5f, height * 0.5f, depth * 0.5f)));
	}
	
	private void create(final Model model, final float mass, final btCollisionShape shape) {
		this.model = model;
		this.shape = shape;
		
		if (shape != null) {
			// Calculate the local inertia, bodies with no mass are static
			Vector3 localInertia;
			if (mass == 0)
				localInertia = Vector3.Zero;
			else {
				shape.calculateLocalInertia(mass, Vector3.tmp);
				localInertia = Vector3.tmp;
			}
			
			// For now just pass null as the motionstate, we'll add that to the body in the entity itself
			bodyInfo = new btRigidBodyConstructionInfo(mass, null, shape, localInertia);
		}
	}

	@Override
	public void dispose () {
		// Don't rely on the GC
		if (bodyInfo != null) bodyInfo.delete();
		if (shape != null) shape.delete();
		// Remove references so the GC can do it's work
		bodyInfo = null;
		shape = null;
	}

	@Override
	public BulletEntity construct (float x, float y, float z) {
		return new BulletEntity(this, x, y, z);
	}
	
	@Override
	public BulletEntity construct (final Matrix4 transform) {
		return new BulletEntity(this, transform);
	}
}