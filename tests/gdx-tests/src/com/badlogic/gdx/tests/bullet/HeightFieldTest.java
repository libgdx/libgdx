
package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btHeightfieldTerrainShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.tests.g3d.HeightField;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/** Demonstration of Bullets {@link btHeightfieldTerrainShape}.
 * @author JamesTKhan */
public class HeightFieldTest extends BaseBulletTest {
	private HeightField field;
	private Texture texture;
	private BulletEntity terrainEntity;
	private FloatBuffer floatBuffer;
	private btHeightfieldTerrainShape terrainShape;

	@Override
	public void create () {
		super.create();

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		disposables.add(texture);

		createSpheres();
		createTerrain(25, 5);
	}

	private void createTerrain (float size, float heightScale) {
		// Create the height field model
		Pixmap data = new Pixmap(Gdx.files.internal("data/g3d/heightmap.png"));
		field = new HeightField(true, data, true, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
			| VertexAttributes.Usage.ColorUnpacked | VertexAttributes.Usage.TextureCoordinates);
		data.dispose();
		field.corner00.set(-size / 2, 0, -size / 2);
		field.corner10.set(size / 2, 0, -size / 2);
		field.corner01.set(-size / 2, 0, size / 2);
		field.corner11.set(size / 2, 0, size / 2);
		field.magnitude.set(0f, heightScale, 0f);
		field.update();

		// Find the min/max height of our height field
		float minHeight = Float.MAX_VALUE;
		float maxHeight = Float.MIN_VALUE;
		for (float f : field.data) {
			if (f < minHeight) minHeight = f;
			if (f > maxHeight) maxHeight = f;
		}

		// Create the terrain model
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		modelBuilder.part("terrain", field.mesh, GL20.GL_TRIANGLES, new Material(TextureAttribute.createDiffuse(texture)));
		Model terrain = modelBuilder.end();
		disposables.add(terrain);

		// Convert our height data into a ByteBuffer/FloatBuffer for passing to Bullet
		// We are responsible for maintaining the float buffer
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(field.data.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(field.data);
		floatBuffer.position(0);

		// Create the terrain physics shape
		terrainShape = new btHeightfieldTerrainShape(field.width, field.height, floatBuffer, 1f, minHeight, maxHeight, 1, true);
		terrainShape.setLocalScaling(new Vector3((size) / ((field.width - 1)), heightScale, (size) / ((field.height - 1))));

		world.addConstructor("terrain", new BulletConstructor(terrain, terrainShape));
		terrainEntity = world.add("terrain", 0, 0f, 0);

		// Align the physics body with the models height
		float adjustedHeight = (maxHeight + minHeight) / 2f * heightScale;
		Matrix4 adjustedTransform = terrainEntity.body.getWorldTransform().trn(0, adjustedHeight, 0);
		terrainEntity.body.setWorldTransform(adjustedTransform);
	}

	private void createSpheres () {
		final Material material = new Material(ColorAttribute.createDiffuse(Color.GREEN), ColorAttribute.createSpecular(1, 1, 1, 1),
			FloatAttribute.createShininess(8f));
		final long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
			| VertexAttributes.Usage.TextureCoordinates;
		final Model sphere = modelBuilder.createSphere(1f, 1f, 1f, 24, 24, material, attributes);
		disposables.add(sphere);

		world.addConstructor("sphere", new BulletConstructor(sphere, 5f, new btSphereShape(.5f)));

		for (int i = -2; i < 2; i++) {
			for (int j = -2; j < 2; j++) {
				world.add("sphere", i * 2, 10, j * 2);
			}
		}
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}
