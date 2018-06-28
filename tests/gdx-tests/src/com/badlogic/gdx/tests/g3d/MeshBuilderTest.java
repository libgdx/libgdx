
package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;

/** @author Xoppa */
public class MeshBuilderTest extends BaseG3dHudTest {
	Model model;
	Environment environment;

	@Override
	public void create () {
		super.create();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, -0.8f));

		modelsWindow.setVisible(false);

		Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));

		Material material = new Material(TextureAttribute.createDiffuse(texture));
		Material solidMaterial = new Material();

		MeshBuilder meshBuilder = new MeshBuilder();
		meshBuilder.begin(Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates, GL20.GL_TRIANGLES);
		meshBuilder.box(1f, 1f, 1f);
		Mesh mesh = new Mesh(true, meshBuilder.getNumVertices(), meshBuilder.getNumIndices(), meshBuilder.getAttributes());
		mesh = meshBuilder.end(mesh);
		
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		modelBuilder.manage(texture);
	
		modelBuilder.node().id = "box";
		MeshPartBuilder mpb = modelBuilder.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates
			| Usage.ColorPacked, material);
		mpb.setColor(Color.RED);
		mpb.box(1f, 1f, 1f);

		modelBuilder.node().id = "sphere";
		mpb = modelBuilder.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates
			| Usage.ColorPacked, material);
		mpb.sphere(2f, 2f, 2f, 10, 5);

		modelBuilder.node().id = "cone";
		mpb = modelBuilder.part("cone", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates
			| Usage.ColorPacked, material);
		mpb.setVertexTransform(new Matrix4().rotate(Vector3.X, -45f));
		mpb.cone(2f, 3f, 1f, 8);

		modelBuilder.node().id = "cylinder";
		mpb = modelBuilder.part("cylinder", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates
			| Usage.ColorPacked, material);
		mpb.setUVRange(1f, 1f, 0f, 0f);
		mpb.cylinder(2f, 4f, 3f, 15);
		
		modelBuilder.node().id = "capsule";
		mpb = modelBuilder.part("capsule", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates
			| Usage.ColorPacked, material);
		mpb.setUVRange(1f, 1f, 0f, 0f);
		mpb.capsule(1.5f, 5f, 15);
		
		modelBuilder.node().id = "capsuleNoTexture";
		mpb = modelBuilder.part("capsuleNoTexture", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal
			| Usage.ColorPacked, solidMaterial);
		mpb.setUVRange(1f, 1f, 0f, 0f);
		mpb.capsule(1.5f, 5f, 15);
		
		modelBuilder.node().id = "transformedSphere";
		mpb = modelBuilder.part("transformedSphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal
			| Usage.ColorPacked, solidMaterial);
		mpb.setUVRange(1f, 1f, 0f, 0f);
		mpb.sphere(new Matrix4().translate(5, 0, 10).rotate(Vector3.Z, 45).scale(1, 2, 1), 1f, 1f, 1f, 12, 16);
		
		modelBuilder.node().id = "mesh";
		mpb = modelBuilder.part("mesh", GL20.GL_TRIANGLES, mesh.getVertexAttributes(), material);
		Matrix4 transform = new Matrix4();
		mpb.setVertexTransform(transform.setToTranslation(0, 2, 0));
		mpb.addMesh(mesh);
		mpb.setColor(Color.BLUE);
		mpb.setVertexTransform(transform.setToTranslation(1, 1, 0));
		mpb.addMesh(mesh);
		mpb.setColor(null);
		mpb.setVertexTransform(transform.setToTranslation(-1, 1, 0).rotate(Vector3.X, 45));
		mpb.addMesh(mesh);
		mpb.setVertexTransform(transform.setToTranslation(0, 1, 1));
		mpb.setUVRange(0.75f, 0.75f, 0.25f, 0.25f);
		mpb.addMesh(mesh);

		model = modelBuilder.end();

		instances.add(new ModelInstance(model, new Matrix4().trn(0f, 0f, 0f), "mesh", true));
		instances.add(new ModelInstance(model, new Matrix4().trn(-5f, 0f, -5f), "box", true));
		instances.add(new ModelInstance(model, new Matrix4().trn(5f, 0f, -5f), "sphere", true));
		instances.add(new ModelInstance(model, new Matrix4().trn(-5f, 0f, 5f), "cone", true));
		instances.add(new ModelInstance(model, new Matrix4().trn(5f, 0f, 5f), "cylinder", true));
		instances.add(new ModelInstance(model, new Matrix4().trn(0f, 0f, 5f), "capsule", true));
		instances.add(new ModelInstance(model, new Matrix4().trn(0f, 0f, 10f), "capsuleNoTexture", true));
		instances.add(new ModelInstance(model, new Matrix4().trn(0f, 0f, 0f), "transformedSphere", true));
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		batch.render(instances, environment);
	}

	@Override
	protected void onModelClicked (String name) {
	}

	@Override
	public void dispose () {
		super.dispose();
		model.dispose();
	}
}
