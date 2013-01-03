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

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.btConeTwistConstraint;
import com.badlogic.gdx.physics.bullet.btConstraintSetting;
import com.badlogic.gdx.physics.bullet.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btTypedConstraint;
import com.badlogic.gdx.physics.bullet.btVector3;
import com.badlogic.gdx.physics.bullet.gdxBullet;
import com.badlogic.gdx.utils.Array;

/** @author xoppa */
public class RayPickRagdollTest extends BaseBulletTest {
	
	final Array<btTypedConstraint> constraints = new Array<btTypedConstraint>();
	btPoint2PointConstraint pickConstraint = null;
	btRigidBody pickedBody = null;
	float pickDistance;
	
	@Override
	public void create () {
		super.create();
		instructions = "Tap to shoot\nDrag ragdoll to pick\nLong press to toggle debug mode\nSwipe for next test";
		
		camera.position.set(4f, 2f, 4f);
		camera.lookAt(0f, 1f, 0f);
		camera.update();
		
		world.addConstructor("pelvis", new BulletConstructor(createCapsuleModel(0.15f, 0.2f), 1f, new btCapsuleShape(0.15f, 0.2f)));
		world.addConstructor("spine", new BulletConstructor(createCapsuleModel(0.15f, 0.28f), 1f, new btCapsuleShape(0.15f, 0.28f)));
		world.addConstructor("head", new BulletConstructor(createCapsuleModel(0.1f, 0.05f), 1f, new btCapsuleShape(0.1f, 0.05f)));
		world.addConstructor("upperleg", new BulletConstructor(createCapsuleModel(0.07f, 0.45f), 1f, new btCapsuleShape(0.07f, 0.45f)));
		world.addConstructor("lowerleg", new BulletConstructor(createCapsuleModel(0.05f, 0.37f), 1f, new btCapsuleShape(0.05f, 0.37f)));
		world.addConstructor("upperarm", new BulletConstructor(createCapsuleModel(0.05f, 0.33f), 1f, new btCapsuleShape(0.05f, 0.33f)));
		world.addConstructor("lowerarm", new BulletConstructor(createCapsuleModel(0.04f, 0.25f), 1f, new btCapsuleShape(0.04f, 0.25f)));
		
		world.add("ground", 0f, 0f, 0f)
			.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		addRagdoll(0, 3f, 0);
		addRagdoll(1f, 6f, 0);
		addRagdoll(-1f, 12f, 0);
	}
	
	@Override
	public void dispose () {
		for (int i = 0; i < constraints.size; i++) {
			world.dynamicsWorld.removeConstraint(constraints.get(i));
			constraints.get(i).delete();
		}
		constraints.clear();
		super.dispose();
	}	
	
	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		boolean result = false;
		if (button == Buttons.LEFT) {
			Ray ray = camera.getPickRay(screenX, screenY);
			Vector3.tmp.set(ray.direction).mul(10f).add(ray.origin);
			ClosestRayResultCallback cb = new ClosestRayResultCallback(ray.origin, Vector3.tmp);
			world.dynamicsWorld.rayTest(ray.origin, Vector3.tmp, cb);
			if (cb.hasHit()) {
				btRigidBody body = btRigidBody.upcast(cb.getM_collisionObject());
				if (body != null && !body.isStaticObject() && !body.isKinematicObject()) {
					pickedBody = body;
					body.setActivationState(gdxBullet.DISABLE_DEACTIVATION);
					
					btVector3 hitpoint = cb.getM_hitPointWorld();
					Vector3.tmp.set(hitpoint.getX(), hitpoint.getY(), hitpoint.getZ());
					Vector3.tmp.mul(body.getCenterOfMassTransform().inv());
					
					pickConstraint = new btPoint2PointConstraint(body,Vector3.tmp);
					btConstraintSetting setting = pickConstraint.getM_setting();
					setting.setM_impulseClamp(30f);
					setting.setM_tau(0.001f);
					pickConstraint.setM_setting(setting);
					
					world.dynamicsWorld.addConstraint(pickConstraint);
		
					pickDistance = Vector3.tmp.sub(camera.position).len();
					result = true;
				}
			}
			cb.delete();
		}
		return result ? result : super.touchDown(screenX, screenY, pointer, button);
	}
	
	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		boolean result = false;
		if (button == Buttons.LEFT) {
			if (pickConstraint != null) {
				world.dynamicsWorld.removeConstraint(pickConstraint);
				pickConstraint.delete();
				pickConstraint = null;
				result = true;
			}
			if (pickedBody != null) {
				pickedBody.forceActivationState(gdxBullet.ACTIVE_TAG);
				pickedBody.setDeactivationTime(0f);
				pickedBody = null;
			}
		}
		return result ? result : super.touchUp(screenX, screenY, pointer, button);
	}
	
	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		boolean result = false;
		if (pickConstraint != null) {
			Ray ray = camera.getPickRay(screenX, screenY);
			Vector3.tmp.set(ray.direction).mul(pickDistance).add(camera.position);
			pickConstraint.setPivotB(Vector3.tmp);
			result = true;
		}
		return result ? result : super.touchDragged(screenX, screenY, pointer);
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
	
	final static float PI = MathUtils.PI;
	final static float PI2 = 0.5f * PI;
	final static float PI4 = 0.25f * PI;
	public void addRagdoll(final float x, final float y, final float z) {
		final Matrix4 tmpM = new Matrix4();
		btRigidBody pelvis = world.add("pelvis", x, y+1, z).body;
		btRigidBody spine = world.add("spine", x, y+1.2f, z).body;
		btRigidBody head = world.add("head", x, y+1.6f, z).body;
		btRigidBody leftupperleg = world.add("upperleg", x-0.18f, y+0.65f, z).body;
		btRigidBody leftlowerleg = world.add("lowerleg", x-0.18f, y+0.2f, z).body;
		btRigidBody rightupperleg = world.add("upperleg", x+0.18f, y+0.65f, z).body;
		btRigidBody rightlowerleg = world.add("lowerleg", x+0.18f, y+0.2f, z).body;		
		btRigidBody leftupperarm = world.add("upperarm", tmpM.setFromEulerAngles(PI2, 0, 0).trn(x-0.35f, y+1.45f, z)).body;
		btRigidBody leftlowerarm = world.add("lowerarm", tmpM.setFromEulerAngles(PI2, 0, 0).trn(x-0.7f, y+1.45f, z)).body;
		btRigidBody rightupperarm = world.add("upperarm", tmpM.setFromEulerAngles(-PI2, 0, 0).trn(x+0.35f, y+1.45f, z)).body;
		btRigidBody rightlowerarm = world.add("lowerarm", tmpM.setFromEulerAngles(-PI2, 0, 0).trn(x+0.7f, y+1.45f, z)).body;
		
		final Matrix4 localA = new Matrix4();
		final Matrix4 localB = new Matrix4();
		btHingeConstraint hingeC = null;
		btConeTwistConstraint coneC = null;
		
		// PelvisSpine
		localA.setFromEulerAngles(0, PI2, 0).trn(0, 0.15f, 0);
		localB.setFromEulerAngles(0, PI2, 0).trn(0, -0.15f, 0);
		constraints.add(hingeC = new btHingeConstraint(pelvis, spine, localA, localB));
		hingeC.setLimit(-PI4, PI2);
		world.dynamicsWorld.addConstraint(hingeC, true);
		
		// SpineHead
		localA.setFromEulerAngles(PI2, 0, 0).trn(0, 0.3f, 0);
		localB.setFromEulerAngles(PI2, 0, 0).trn(0, -0.14f, 0);
		constraints.add(coneC = new btConeTwistConstraint(spine, head, localA, localB));
		coneC.setLimit(PI4, PI4, PI2);
		world.dynamicsWorld.addConstraint(coneC, true);
		
		// LeftHip
		localA.setFromEulerAngles(-PI4*5f, 0, 0).trn(-0.18f, -0.1f, 0);
		localB.setFromEulerAngles(-PI4*5f, 0, 0).trn(0, 0.225f, 0);
		constraints.add(coneC = new btConeTwistConstraint(pelvis, leftupperleg, localA, localB));
		coneC.setLimit(PI4, PI4, 0);
		world.dynamicsWorld.addConstraint(coneC, true);
		
		// LeftKnee
		localA.setFromEulerAngles(0, PI2, 0).trn(0, -0.225f, 0);
		localB.setFromEulerAngles(0, PI2, 0).trn(0, 0.185f, 0);
		constraints.add(hingeC = new btHingeConstraint(leftupperleg, leftlowerleg, localA, localB));
		hingeC.setLimit(0, PI2);
		world.dynamicsWorld.addConstraint(hingeC, true);
		
		// RightHip
		localA.setFromEulerAngles(-PI4*5f, 0, 0).trn(0.18f, -0.1f, 0);
		localB.setFromEulerAngles(-PI4*5f, 0, 0).trn(0, 0.225f, 0);
		constraints.add(coneC = new btConeTwistConstraint(pelvis, rightupperleg, localA, localB));
		coneC.setLimit(PI4, PI4, 0);
		world.dynamicsWorld.addConstraint(coneC, true);
		
		// RightKnee
		localA.setFromEulerAngles(0, PI2, 0).trn(0, -0.225f, 0);
		localB.setFromEulerAngles(0, PI2, 0).trn(0, 0.185f, 0);
		constraints.add(hingeC = new btHingeConstraint(rightupperleg, rightlowerleg, localA, localB));
		hingeC.setLimit(0, PI2);
		world.dynamicsWorld.addConstraint(hingeC, true);
		
		// LeftShoulder
		localA.setFromEulerAngles(PI, 0, 0).trn(-0.2f, 0.15f, 0);
		localB.setFromEulerAngles(PI2, 0, 0).trn(0, -0.18f, 0);
		constraints.add(coneC = new btConeTwistConstraint(pelvis, leftupperarm, localA, localB));
		coneC.setLimit(PI2, PI2, 0);
		world.dynamicsWorld.addConstraint(coneC, true);
		
		// LeftElbow
		localA.setFromEulerAngles(0, PI2, 0).trn(0, 0.18f, 0);
		localB.setFromEulerAngles(0, PI2, 0).trn(0, -0.14f, 0);
		constraints.add(hingeC = new btHingeConstraint(leftupperarm, leftlowerarm, localA, localB));
		hingeC.setLimit(0, PI2);
		world.dynamicsWorld.addConstraint(hingeC, true);
		
		// RightShoulder
		localA.setFromEulerAngles(PI, 0, 0).trn(0.2f, 0.15f, 0);
		localB.setFromEulerAngles(PI2, 0, 0).trn(0, -0.18f, 0);
		constraints.add(coneC = new btConeTwistConstraint(pelvis, rightupperarm, localA, localB));
		coneC.setLimit(PI2, PI2, 0);
		world.dynamicsWorld.addConstraint(coneC, true);
		
		// RightElbow
		localA.setFromEulerAngles(0, PI2, 0).trn(0, 0.18f, 0);
		localB.setFromEulerAngles(0, PI2, 0).trn(0, -0.14f, 0);
		constraints.add(hingeC = new btHingeConstraint(rightupperarm, rightlowerarm, localA, localB));
		hingeC.setLimit(0, PI2);
		world.dynamicsWorld.addConstraint(hingeC, true);
	}
	
	protected Model createCapsuleModel(float radius, float height) {
		final float hh = radius + 0.5f * height;
		final Mesh mesh = new Mesh(true, 8, 36, new VertexAttribute(Usage.Position, 3, "a_position"));
		mesh.setVertices(new float[] {radius, hh, radius, radius, hh, -radius, -radius, hh, radius, -radius, hh, -radius,
			radius, -hh, radius, radius, -hh, -radius, -radius, -hh, radius, -radius, -hh, -radius});
		mesh.setIndices(new short[] {0, 1, 2, 1, 2, 3, // top
			4, 5, 6, 5, 6, 7, // bottom
			0, 2, 4, 4, 6, 2, // front
			1, 3, 5, 5, 7, 3, // back
			2, 3, 6, 6, 7, 3, // left
			0, 1, 4, 4, 5, 1 // right
			});
		return new StillModel(new StillSubMesh("capsule", mesh, GL10.GL_TRIANGLES, new Material()));
	}
}
