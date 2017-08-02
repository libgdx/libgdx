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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.tests.g3d.BaseG3dTest;
import com.badlogic.gdx.utils.Array;

public class SensorTest extends BaseG3dTest {
	ModelInstance skydome;
	Model floorModel;
	ModelInstance tree;
	DirectionalLight light;

	Environment lights;
	final Quaternion tmpRotation = new Quaternion();
	final Vector3 tmpUp = new Vector3();
	final Vector3 tmpDirection = new Vector3();

	@Override
	public void create () {
		super.create();
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		light = new DirectionalLight().set(0.8f, 0.8f, 0.8f, -.4f, -.4f, -.4f);
		lights.add(light = new DirectionalLight().set(0.8f, 0.8f, 0.8f, -.4f, -.4f, -.4f));
		cam.position.set(10, 10, 10);
		cam.lookAt(0, 0, 0);
		cam.update();
		assets.load("data/g3d/skydome.g3db", Model.class);
		assets.load("data/g3d/concrete.png", Texture.class);
		assets.load("data/tree.png", Texture.class);
		loading = true;

		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		builder.node().id = "floor";
		MeshPartBuilder part = builder.part("floor", GL20.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates | Usage.Normal,
			new Material("concrete"));
		part.ensureVertices(4 * 1600);
		part.ensureRectangleIndices(1600);
		for (float x = -200f; x < 200f; x += 10f) {
			for (float z = -200f; z < 200f; z += 10f) {
				part.rect(x, 0, z + 10f, x + 10f, 0, z + 10f, x + 10f, 0, z, x, 0, z, 0, 1, 0);
			}
		}
		builder.node().id = "tree";
		part = builder.part("tree", GL20.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates | Usage.Normal,
			new Material("tree"));
		part.rect(0f, 0f, -10f, 10f, 0f, -10f, 10f, 10f, -10f, 0f, 10f, -10f, 0, 0, 1f);
		part.setUVRange(1, 0, 0, 1);
		part.rect(10f, 0f, -10f, 0f, 0f, -10f, 0f, 10f, -10f, 10f, 10f, -10f, 0, 0, -1f);
		floorModel = builder.end();
	}

	@Override
	public void render (final Array<ModelInstance> instances) {
		tmpUp.set(cam.up);
		tmpDirection.set(cam.direction);

		Input input = Gdx.input;
		tmpRotation.setEulerAngles(-input.getAzimuth(), -input.getPitch(), -input.getRoll());

		cam.position.set(5, 10, 5);
		cam.direction.set(0, -1, 0);
		cam.up.set(0, 0, -1);
		cam.rotate(tmpRotation);
		cam.update();

		super.render(instances);
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		batch.render(instances, lights);
		if (skydome != null) batch.render(skydome);
	}

	@Override
	protected void onLoaded () {
		if (skydome == null) {
			skydome = new ModelInstance(assets.get("data/g3d/skydome.g3db", Model.class));
			floorModel.getMaterial("concrete")
				.set(TextureAttribute.createDiffuse(assets.get("data/g3d/concrete.png", Texture.class)));
			floorModel.getMaterial("tree").set(TextureAttribute.createDiffuse(assets.get("data/tree.png", Texture.class)),
				new BlendingAttribute());
			instances.add(new ModelInstance(floorModel, "floor"));
			instances.add(tree = new ModelInstance(floorModel, "tree"));
			loading = true;
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		floorModel.dispose();
	}
}
