package com.badlogic.gdx.tests.bullet;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btBulletWorldImporter;
import com.badlogic.gdx.physics.bullet.btCollisionObject;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.btMotionState;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btStringArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;

public class ImportTest extends BaseBulletTest {
	btBulletWorldImporter importer;
	Model model;
	
	public class MyImporter extends btBulletWorldImporter {
		public MyImporter (btDynamicsWorld world) {
			super(world);
		}
		@Override
		public btRigidBody createRigidBody (boolean isDynamic, float mass, Matrix4 startTransform, btCollisionShape shape, String bodyName) {
			Vector3 localInertia = new Vector3();
			if (mass > 0f)
				shape.calculateLocalInertia(mass, localInertia);

			btRigidBody result = new btRigidBody(mass, null, shape, localInertia);
			
			String nodeName = bodyName.split("_", 2)[0]+"_model";
			ModelInstance instance = new ModelInstance(model, nodeName, true, true);
			instance.transform.set(startTransform);
			
			BulletEntity entity = new BulletEntity(instance, result);
			ImportTest.this.world.add(entity);
			
			return result;
		}
	}
	
	@Override
	public void create () {
		super.create();
		
		ModelLoader g3djLoader = new G3dModelLoader(new JsonReader());
		model = g3djLoader.loadModel(Gdx.files.internal("data/g3d/btscene1.g3dj"));
		disposables.add(model);

		importer = new MyImporter((btDynamicsWorld)world.collisionWorld);
		importer.loadFile(Gdx.files.internal("data/g3d/btscene1.bullet"));
		
		camera.position.set(10f,15f,20f);
		camera.up.set(0,1,0);
		camera.lookAt(-10,8,0);
		camera.update();
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
	
	@Override
	public void dispose () {
		super.dispose();

		importer.deleteAllData();
		importer.dispose();
		importer = null;
	}
}