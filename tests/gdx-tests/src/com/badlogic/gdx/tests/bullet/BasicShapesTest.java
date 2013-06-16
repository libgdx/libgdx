package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btBoxShape;
import com.badlogic.gdx.physics.bullet.btConeShape;
import com.badlogic.gdx.physics.bullet.btCylinderShape;
import com.badlogic.gdx.physics.bullet.btSphereShape;

public class BasicShapesTest extends BaseBulletTest {
	@Override
	public void create () {
		super.create();

		final Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		disposables.add(texture);
		final Material material = new Material(TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1,1,1,1), FloatAttribute.createShininess(8f));
		final long attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
		
		final Model sphere = modelBuilder.createSphere(4f, 4f, 4f, 24, 24, material, attributes);
		disposables.add(sphere);
		world.addConstructor("sphere", new BulletConstructor(sphere, 10f, new btSphereShape(2f)));
		
		final Model cylinder = modelBuilder.createCylinder(4f, 6f, 4f, 16, material, attributes);
		disposables.add(cylinder);
		world.addConstructor("cylinder", new BulletConstructor(cylinder, 10f, new btCylinderShape(Vector3.tmp.set(2f, 3f, 2f))));
		
		final Model box = modelBuilder.createBox(4f, 4f, 2f, material, attributes);
		disposables.add(box);
		world.addConstructor("box2", new BulletConstructor(box, 10f, new btBoxShape(Vector3.tmp.set(2f, 2f, 1f))));
		
		final Model cone = modelBuilder.createCone(4f, 6f, 4f, 16, material, attributes);
		disposables.add(cone);
		world.addConstructor("cone", new BulletConstructor(cone, 10f, new btConeShape(2f,6f)));
			
		// Create the entities
		world.add("ground", 0f, 0f, 0f)
			.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		world.add("sphere", 0, 5, 5);
		world.add("cylinder", 5, 5, 0);
		world.add("box2", 0, 5, 0);
		world.add("cone", 5, 5, 5);
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}