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

import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBody;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyRigidBodyCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyWorldInfo;
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld;
import com.badlogic.gdx.utils.BufferUtils;

/** @author xoppa */
public class SoftMeshTest extends BaseBulletTest {
	btSoftBodyWorldInfo worldInfo;
	btSoftBody softBody;
	Model model;
	BulletEntity entity;
	ShortBuffer indexMap;
	Vector3 tmpV = new Vector3();
	int positionOffset;
	int normalOffset;

	@Override
	public BulletWorld createWorld () {
		btDefaultCollisionConfiguration collisionConfiguration = new btSoftBodyRigidBodyCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 broadphase = new btAxisSweep3(tmpV1.set(-1000, -1000, -1000), tmpV2.set(1000, 1000, 1000), 1024);
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btSoftRigidDynamicsWorld dynamicsWorld = new btSoftRigidDynamicsWorld(dispatcher, broadphase, solver,
			collisionConfiguration);

		worldInfo = new btSoftBodyWorldInfo();
		worldInfo.setBroadphase(broadphase);
		worldInfo.setDispatcher(dispatcher);
		worldInfo.getSparsesdf().Initialize();

		return new BulletWorld(collisionConfiguration, dispatcher, broadphase, solver, dynamicsWorld);
	}

	@Override
	public void create () {
		super.create();

		world.maxSubSteps = 20;

		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		// Note: not every model is suitable for a one on one translation with a soft body, a better model might be added later.
		model = objLoader.loadModel(Gdx.files.internal("data/wheel.obj"));
		MeshPart meshPart = model.nodes.get(0).parts.get(0).meshPart;

		meshPart.mesh.scale(6, 6, 6);

		indexMap = BufferUtils.newShortBuffer(meshPart.size);

		positionOffset = meshPart.mesh.getVertexAttribute(Usage.Position).offset;
		normalOffset = meshPart.mesh.getVertexAttribute(Usage.Normal).offset;

		softBody = new btSoftBody(worldInfo, meshPart.mesh.getVerticesBuffer(), meshPart.mesh.getVertexSize(), positionOffset,
			normalOffset, meshPart.mesh.getIndicesBuffer(), meshPart.offset, meshPart.size, indexMap, 0);
		// Set mass of the first vertex to zero so its unmovable, comment out this line to make it a fully dynamic body.
		softBody.setMass(0, 0);
		com.badlogic.gdx.physics.bullet.softbody.btSoftBody.Material pm = softBody.appendMaterial();
		pm.setKLST(0.2f);
		pm.setFlags(0);
		softBody.generateBendingConstraints(2, pm);
		// Be careful increasing iterations, it decreases performance (but increases accuracy).
		softBody.setConfig_piterations(7);
		softBody.setConfig_kDF(0.2f);
		softBody.randomizeConstraints();
		softBody.setTotalMass(1);
		softBody.translate(tmpV.set(1, 5, 1));
		((btSoftRigidDynamicsWorld)(world.collisionWorld)).addSoftBody(softBody);

		world.add(entity = new BulletEntity(model, (btCollisionObject)null, 1, 5, 1));
	}

	@Override
	public void dispose () {
		((btSoftRigidDynamicsWorld)(world.collisionWorld)).removeSoftBody(softBody);
		softBody.dispose();
		softBody = null;
		indexMap = null;

		super.dispose();

		worldInfo.dispose();
		worldInfo = null;
		model.dispose();
		model = null;
	}

	@Override
	public void render () {
		if (world.renderMeshes) {
			MeshPart meshPart = model.nodes.get(0).parts.get(0).meshPart;
			softBody.getVertices(meshPart.mesh.getVerticesBuffer(), meshPart.mesh.getVertexSize(), positionOffset, normalOffset,
				meshPart.mesh.getIndicesBuffer(), meshPart.offset, meshPart.size, indexMap, 0);
			softBody.getWorldTransform(entity.transform);
		}
		super.render();
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y, 20f);
		return true;
	}
}
