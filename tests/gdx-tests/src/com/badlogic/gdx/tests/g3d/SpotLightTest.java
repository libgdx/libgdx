package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class SpotLightTest extends ModelTest{
	SpotLight spotLight;
	Model lightModel;
	Renderable pLight;
	Vector3 center = new Vector3(), transformedCenter = new Vector3(), tmpV = new Vector3();
	float radius = 1f;

	@Override
	public void create () {
		super.create();
		environment.clear();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1.0f));
		environment.add(spotLight = new SpotLight().set(0.2f, 0.8f, 0.2f, new Vector3(), 1, new Vector3(), 1,0.5f,0.2f));

		ModelBuilder mb = new ModelBuilder();
		lightModel = mb.createSphere(1, 1, 1, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position);
		lightModel.nodes.get(0).parts.get(0).setRenderable(pLight = new Renderable());
	}

	@Override
	protected void onLoaded () {
		super.onLoaded();
		BoundingBox bounds = instances.get(0).calculateBoundingBox(new BoundingBox());
		center.set(bounds.getCenter());
		radius = bounds.getDimensions().len() * .5f;
		spotLight.position.set(0, radius, 0).add(transformedCenter.set(center).mul(transform));
		//spotLight.intensity = radius * radius;
		((ColorAttribute)pLight.material.get(ColorAttribute.Diffuse)).color.set(spotLight.color);
		final float s = 0.2f * radius;
		pLight.worldTransform.setToScaling(s, s, s);
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		final float delta = Gdx.graphics.getDeltaTime();
		spotLight.position.sub(transformedCenter);
		spotLight.position.rotate(Vector3.X, delta * 50f);
		spotLight.position.rotate(Vector3.Y, delta * 13f);
		spotLight.position.rotate(Vector3.Z, delta * 3f);
		spotLight.position.add(transformedCenter.set(center).mul(transform));
		spotLight.direction.set(spotLight.position.cpy().scl(-1).nor());

		pLight.worldTransform.setTranslation(spotLight.position);
		batch.render(pLight);

		super.render(batch, instances);
	}

	@Override
	public void dispose () {
		lightModel.dispose();
		super.dispose();
	}

}
