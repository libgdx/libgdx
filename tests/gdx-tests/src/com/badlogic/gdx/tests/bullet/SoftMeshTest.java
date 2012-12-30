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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Material;
import com.badlogic.gdx.physics.bullet.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.btSoftBody;
import com.badlogic.gdx.physics.bullet.btSoftBodyRigidBodyCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btSoftBodyWorldInfo;
import com.badlogic.gdx.physics.bullet.btSoftRigidDynamicsWorld;

/** @author xoppa */
public class SoftMeshTest extends BaseBulletTest {
	btSoftBodyWorldInfo worldInfo;
	btSoftBody  softBody;
	Mesh mesh;
	Matrix4 tmpM = new Matrix4();
	Color color = new Color(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
	
	@Override
	public BulletWorld createWorld () {
		btDefaultCollisionConfiguration collisionConfiguration = new btSoftBodyRigidBodyCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 broadphase = new btAxisSweep3(Vector3.tmp.set(-1000, -1000, -1000), Vector3.tmp2.set(1000, 1000, 1000), 1024);
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btSoftRigidDynamicsWorld dynamicsWorld = new btSoftRigidDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		
		worldInfo = new btSoftBodyWorldInfo();
		worldInfo.setM_broadphase(broadphase);
		worldInfo.setM_dispatcher(dispatcher);
		worldInfo.getM_sparsesdf().Initialize();
		
		return new BulletWorld(collisionConfiguration, dispatcher, broadphase, solver, dynamicsWorld);
	}
	
	@Override
	public void create () {
		super.create();
		
		world.maxSubSteps = 20;
		
		world.add("ground", 0f, 0f, 0f)
		.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		// Note: not every model is suitable for a one on one translation with a soft body, a better model might be added later.
		
		final StillModel model = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/wheel.obj"));
		mesh = model.subMeshes[0].getMesh();
		mesh.scale(6f, 6f, 6f);
		
		softBody = new btSoftBody(worldInfo, mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize(), mesh.getVertexAttribute(Usage.Position).offset, mesh.getIndicesBuffer(), mesh.getNumIndices()/3);
		// Set mass of 1 one to zero so its unmovable, comment out this line to make it a full dynamic body.
		softBody.setMass(0, 0);
		Material pm = softBody.appendMaterial();
		pm.setM_kLST(0.2f);
		pm.setM_flags(0);
		softBody.generateBendingConstraints(2, pm);
		// Be careful increasing iterations, it decreases performance (but increases accuracy). 
		softBody.setConfig_piterations(7);
		softBody.setConfig_kDF(0.2f);
		softBody.randomizeConstraints();
		softBody.setTotalMass(1);
		softBody.translate(Vector3.tmp.set(1, 5, 1));
		((btSoftRigidDynamicsWorld)(world.dynamicsWorld)).addSoftBody(softBody);
	}
	
	@Override
	public void dispose () {
		((btSoftRigidDynamicsWorld)(world.dynamicsWorld)).removeSoftBody(softBody);
		softBody.delete();
		softBody = null;
		
		super.dispose();
		
		worldInfo.delete();
		worldInfo = null;
		mesh.dispose();
		mesh = null;
	}
	
	@Override
	public void render () {
		super.render();
		if (world.renderMeshes) {
			softBody.getVertices(mesh.getVerticesBuffer(), softBody.getNodeCount(), mesh.getVertexSize(), 0);
			softBody.getWorldTransform(tmpM);
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glMultMatrixf(tmpM.val, 0);
			Gdx.gl10.glColor4f(color.r, color.g, color.b, color.a);
			mesh.render(GL10.GL_TRIANGLES);
			Gdx.gl10.glPopMatrix();
		}	
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y, 20f);
		return true;
	}
}
