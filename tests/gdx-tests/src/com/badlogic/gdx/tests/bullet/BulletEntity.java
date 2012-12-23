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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.btMotionState;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btRigidBodyConstructionInfo;
import com.badlogic.gdx.utils.Disposable;

/** @author xoppa
 * Renderable BaseEntity with a bullet physics body. 
 */
public class BulletEntity extends BaseEntity {
	private final static Matrix4 tmpM = new Matrix4();
	public BulletEntity.MotionState motionState;
	public btRigidBody body;

	public BulletEntity (final Mesh mesh, final btRigidBodyConstructionInfo bodyInfo, final float x, final float y, final float z) {
		this(mesh, bodyInfo, tmpM.setToTranslation(x, y, z));
	}
	
	public BulletEntity (final Mesh mesh, final btRigidBodyConstructionInfo bodyInfo, final Matrix4 transform) {
		this.mesh = mesh;
		this.transform.set(transform);
		
		if (bodyInfo != null) {
			this.motionState = new MotionState(this.transform);
			this.body = new btRigidBody(bodyInfo);
			this.body.setMotionState(motionState);
		}
	}

	public BulletEntity (final BulletConstructor constructInfo, final float x, final float y, final float z) {
		this(constructInfo.mesh, constructInfo.bodyInfo, x, y, z);
	}
	
	public BulletEntity (final BulletConstructor constructInfo, final Matrix4 transform) {
		this(constructInfo.mesh, constructInfo.bodyInfo, transform);
	}

	@Override
	public void dispose () {
		// Don't rely on the GC
		if (motionState != null) motionState.dispose();
		if (body != null) body.delete();
		// And remove the reference
		motionState = null;
		body = null;
	}
	
	static class MotionState extends btMotionState implements Disposable {
		private final Matrix4 transform;
		
		public MotionState(final Matrix4 transform) {
			this.transform = transform;
		}
		
		/**
		 * For dynamic and static bodies this method is called by bullet once to get the initial state of the body.
		 * For kinematic bodies this method is called on every update, unless the body is deactivated.
		 */
		@Override
		public void getWorldTransform (final Matrix4 worldTrans) {
			worldTrans.set(transform);
		}

		/**
		 * For dynamic bodies this method is called by bullet every update to inform about the new position and rotation.
		 */
		@Override
		public void setWorldTransform (final Matrix4 worldTrans) {
			transform.set(worldTrans);
		}
		
		@Override
		public void dispose () {
			delete();
		}
	}
}