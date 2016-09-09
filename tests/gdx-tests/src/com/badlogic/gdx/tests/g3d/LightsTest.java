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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class LightsTest extends ModelTest {
	DirectionalLight dirLight;
	PointLight pointLight;
	Model lightModel;
	Renderable pLight;
	Vector3 center = new Vector3(), transformedCenter = new Vector3(), tmpV = new Vector3();
	float radius = 1f;

	@Override
	public void create () {
		super.create();
		environment.clear();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.0f));
		environment.add(dirLight = new DirectionalLight().set(0.8f, 0.2f, 0.2f, -1f, -2f, -0.5f));
		environment.add(pointLight = new PointLight().set(0.2f, 0.8f, 0.2f, 0f, 0f, 0f, 100f));

		ModelBuilder mb = new ModelBuilder();
		lightModel = mb.createSphere(1, 1, 1, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position);
		lightModel.nodes.get(0).parts.get(0).setRenderable(pLight = new Renderable());
	}

	@Override
	protected void onLoaded () {
		super.onLoaded();
		BoundingBox bounds = instances.get(0).calculateBoundingBox(new BoundingBox());
		bounds.getCenter(center);
		radius = bounds.getDimensions(tmpV).len() * .5f;
		pointLight.position.set(0, radius, 0).add(transformedCenter.set(center).mul(transform));
		pointLight.intensity = radius * radius;
		((ColorAttribute)pLight.material.get(ColorAttribute.Diffuse)).color.set(pointLight.color);
		final float s = 0.2f * radius;
		pLight.worldTransform.setToScaling(s, s, s);
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		final float delta = Gdx.graphics.getDeltaTime();
		dirLight.direction.rotate(Vector3.X, delta * 45f);
		dirLight.direction.rotate(Vector3.Y, delta * 25f);
		dirLight.direction.rotate(Vector3.Z, delta * 33f);

		pointLight.position.sub(transformedCenter);
		pointLight.position.rotate(Vector3.X, delta * 50f);
		pointLight.position.rotate(Vector3.Y, delta * 13f);
		pointLight.position.rotate(Vector3.Z, delta * 3f);
		pointLight.position.add(transformedCenter.set(center).mul(transform));

		pLight.worldTransform.setTranslation(pointLight.position);
		batch.render(pLight);

		super.render(batch, instances);
	}

	@Override
	public void dispose () {
		lightModel.dispose();
		super.dispose();
	}
}
