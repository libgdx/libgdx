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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;

public class SimplePNTriangleTesslation extends GdxTest {
	ShaderProgram shader;
	Mesh mesh;
	Matrix4 projection = new Matrix4();
	Matrix4 view = new Matrix4();
	Matrix4 model = new Matrix4();
	Matrix4 combined = new Matrix4();
	Vector3 axis = new Vector3(1, 0, 1).nor();
	float angle = 45;
	private Array<FileHandle> models;
	private int modelIndex = 0;

	@Override
	public void create () {
		
		ShaderStage.geometry.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 150\n" : "#version 320 es\n";
		ShaderStage.tesslationControl.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 400\n" : "#version 320 es\n";
		ShaderStage.tesslationEvaluation.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 400\n" : "#version 320 es\n";

		shader = new ShaderProgram(
			new ShaderPart(ShaderStage.vertex, Gdx.files.internal("data/g3d/shaders/pnt.vert").readString()),
			new ShaderPart(ShaderStage.tesslationControl, Gdx.files.internal("data/g3d/shaders/pnt.tesc").readString()),
			new ShaderPart(ShaderStage.tesslationEvaluation, Gdx.files.internal("data/g3d/shaders/pnt.tese").readString()),
			new ShaderPart(ShaderStage.geometry, Gdx.files.internal("data/g3d/shaders/pnt.geom").readString()),
			new ShaderPart(ShaderStage.fragment, Gdx.files.internal("data/g3d/shaders/pnt.frag").readString()));
		
		if(!shader.isCompiled()){
			throw new GdxRuntimeException(shader.getLog());
		}
		
		models = new Array<FileHandle>();
		models.add(Gdx.files.internal("data/g3d/shapes/teapot.g3dj"));
		models.add(Gdx.files.internal("data/g3d/shapes/torus.g3dj"));
		models.add(Gdx.files.internal("data/g3d/shapes/sphere.g3dj"));
		models.add(Gdx.files.internal("data/g3d/knight.g3dj"));
		
		Gdx.input.setInputProcessor(this);
		
		nextMesh();
	}
	
	@Override
	public boolean keyDown (int keycode) {
		if(keycode == Input.Keys.SPACE){
			nextMesh();
		}
		return super.keyDown(keycode);
	}
	
	private void nextMesh(){
		mesh = new G3dModelLoader(new JsonReader()).loadModel(models.get(modelIndex)).meshes.first();
		modelIndex = (modelIndex+1) % models.size;
	}

	@Override
	public void render () {
		angle += Gdx.graphics.getDeltaTime() * 40.0f;
		float aspect = Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		projection.setToProjection(1.0f, 20.0f, 60.0f, aspect);
		view.idt().trn(0, 0, -12.0f);
		model.setToRotation(axis, angle);
		combined.set(projection).mul(view).mul(model);
		
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.setUniformMatrix("u_mvpMatrix", combined);
		mesh.render(shader, GL30.GL_PATCHES);
	}
}
