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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MaterialTest extends GdxTest {
	
	float angleY = 0;
	
	Model model;
	ModelInstance modelInstance;
	ModelBatch modelBatch;
	
	TextureAttribute textureAttribute;
	ColorAttribute colorAttribute;
	BlendingAttribute blendingAttribute;

	Material material;
	
	Texture texture;
	
	Camera camera;

	@Override
	public void create () {
		ObjLoader objLoader = new ObjLoader();
		model =  objLoader.loadObj(Gdx.files.internal("data/cube.obj"));
		
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);
		
		// Create material attributes. Each material can contain x-number of attributes.
		textureAttribute = new TextureAttribute(TextureAttribute.Diffuse, texture);
		colorAttribute = new ColorAttribute(ColorAttribute.Diffuse, Color.ORANGE);
		blendingAttribute = new BlendingAttribute(GL10.GL_ONE, GL10.GL_ONE);

		modelInstance = new ModelInstance(model);
		
		material = modelInstance.materials.get(0);
		material.clear();
		
		modelBatch = new ModelBatch();
		
		camera = new PerspectiveCamera(45, 4, 4);
		camera.position.set(3, 3, 3);
		camera.direction.set(-1, -1, -1);
		
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		GL10 gl = Gdx.graphics.getGL10();

		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		camera.update();

		modelInstance.transform.rotate(Vector3.Y, 30 * Gdx.graphics.getDeltaTime());
		modelBatch.begin(camera);
		modelBatch.render(modelInstance);
		modelBatch.end();
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		
		if(!material.has(TextureAttribute.Diffuse))
			material.set(textureAttribute);
		else if(!material.has(ColorAttribute.Diffuse))
			material.set(colorAttribute);
		else if(!material.has(BlendingAttribute.Type))
			material.set(blendingAttribute);
		else
			material.clear();
		
		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public void dispose () {
		texture.dispose();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}