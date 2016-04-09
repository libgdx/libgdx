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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MaterialTest extends GdxTest {

	float angleY = 0;

	Model model, backModel;
	ModelInstance modelInstance;
	ModelInstance background;
	ModelBatch modelBatch;

	TextureAttribute textureAttribute;
	ColorAttribute colorAttribute;
	BlendingAttribute blendingAttribute;

	Material material;

	Texture texture;

	Camera camera;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);

		// Create material attributes. Each material can contain x-number of attributes.
		textureAttribute = new TextureAttribute(TextureAttribute.Diffuse, texture);
		colorAttribute = new ColorAttribute(ColorAttribute.Diffuse, Color.ORANGE);
		blendingAttribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		ModelBuilder builder = new ModelBuilder();
		model = builder.createBox(1, 1, 1, new Material(), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		model.manageDisposable(texture);
		modelInstance = new ModelInstance(model);
		modelInstance.transform.rotate(Vector3.X, 45);

		material = modelInstance.materials.get(0);

		builder.begin();
		MeshPartBuilder mpb = builder.part("back", GL20.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates, new Material(
			textureAttribute));
		mpb.rect(-2, -2, -2, 2, -2, -2, 2, 2, -2, -2, 2, -2, 0, 0, 1);
		backModel = builder.end();
		background = new ModelInstance(backModel);

		modelBatch = new ModelBatch();

		camera = new PerspectiveCamera(45, 4, 4);
		camera.position.set(0, 0, 3);
		camera.direction.set(0, 0, -1);
		camera.update();

		Gdx.input.setInputProcessor(this);
	}

	private float counter = 0.f;

	@Override
	public void render () {
		counter = (counter + Gdx.graphics.getDeltaTime()) % 1.f;
		blendingAttribute.opacity = 0.25f + Math.abs(0.5f - counter);

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelInstance.transform.rotate(Vector3.Y, 30 * Gdx.graphics.getDeltaTime());
		modelBatch.begin(camera);
		modelBatch.render(background);
		modelBatch.render(modelInstance);
		modelBatch.end();
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {

		if (!material.has(TextureAttribute.Diffuse))
			material.set(textureAttribute);
		else if (!material.has(ColorAttribute.Diffuse))
			material.set(colorAttribute);
		else if (!material.has(BlendingAttribute.Type))
			material.set(blendingAttribute);
		else
			material.clear();

		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public void dispose () {
		model.dispose();
		backModel.dispose();
		modelBatch.dispose();
	}
}
