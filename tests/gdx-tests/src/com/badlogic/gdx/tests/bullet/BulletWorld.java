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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.physics.bullet.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btSequentialImpulseConstraintSolver;

/** @author xoppa
 * Bullet physics world that holds all bullet entities and constructors.  
 */
public class BulletWorld extends BaseWorld<BulletEntity> {
	// For debugging purposes:
	private DebugDrawer debugDrawer = null;
	public boolean renderMeshes = true;
	
	public final btCollisionConfiguration collisionConfiguration;
	public final btCollisionDispatcher dispatcher;
	public final btBroadphaseInterface broadphase;
	public final btConstraintSolver solver;
	public final btDynamicsWorld dynamicsWorld;
	public final Vector3 gravity;	
	
	private WindowedMean mean = new WindowedMean(5);
	public float bulletTime;
	public float bulletLoad;
	public int maxSubSteps = 5;
	
	public BulletWorld(final btCollisionConfiguration collisionConfiguration, final btCollisionDispatcher dispatcher,
		final btBroadphaseInterface broadphase, final btConstraintSolver solver, final btDynamicsWorld dynamicsWorld,  
		final Vector3 gravity) {
		this.collisionConfiguration = collisionConfiguration;
		this.dispatcher = dispatcher;
		this.broadphase = broadphase;
		this.solver = solver;
		this.dynamicsWorld = dynamicsWorld;
		this.dynamicsWorld.setGravity(gravity);
		this.gravity = gravity;
	}
	
	public BulletWorld(final btCollisionConfiguration collisionConfiguration, final btCollisionDispatcher dispatcher,
		final btBroadphaseInterface broadphase, final btConstraintSolver solver, final btDynamicsWorld dynamicsWorld) {
		this(collisionConfiguration, dispatcher, broadphase, solver, dynamicsWorld, new Vector3(0, -10, 0));
	}
	
	public BulletWorld(final Vector3 gravity) {
		collisionConfiguration = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfiguration);
		broadphase = new btDbvtBroadphase();
		solver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		dynamicsWorld.setGravity(gravity);
		this.gravity = gravity;
	}
	
	public BulletWorld() {
		this(new Vector3(0, -10, 0));
	}
	
	@Override
	public void add(final BulletEntity entity) {
		super.add(entity);
		if (entity.body != null)
			dynamicsWorld.addRigidBody(entity.body);
	}
	
	@Override
	public void update () {
		final float dt = Gdx.graphics.getDeltaTime();
		bulletTime = mean.getMean();
		final float load = dt == 0 ? 0 : bulletTime / dt;
		bulletLoad = (dt > 1f) ? load : dt * load + (1f - dt) * bulletLoad;
		
		final long start = System.nanoTime();
		dynamicsWorld.stepSimulation(dt, maxSubSteps);
		mean.addValue((System.nanoTime() - start) / 1000000000.0f);

		if (debugDrawer != null && debugDrawer.getDebugMode() > 0) {
			debugDrawer.begin();
			dynamicsWorld.debugDrawWorld();
			debugDrawer.end();
		}
		if (renderMeshes)
			super.update();
	}
	
	@Override
	public void dispose () {
		for (int i = 0; i < entities.size; i++) {
			btRigidBody body = entities.get(i).body;
			if (body != null)
				dynamicsWorld.removeRigidBody(body);
		}
		
		super.dispose();
		
		dynamicsWorld.delete();
		solver.delete();
		broadphase.delete();
		dispatcher.delete();
		collisionConfiguration.delete();
	}
	
	public void setDebugMode(final int mode, final Matrix4 projMatrix) {
		if (mode == btIDebugDraw.DebugDrawModes.DBG_NoDebug && debugDrawer == null)
			return;
		if (debugDrawer == null)
			dynamicsWorld.setDebugDrawer(debugDrawer = new DebugDrawer());
		debugDrawer.lineRenderer.setProjectionMatrix(projMatrix);
		debugDrawer.setDebugMode(mode);
	}
	
	public int getDebugMode() {
		return (debugDrawer == null) ? 0 : debugDrawer.getDebugMode();
	}
}