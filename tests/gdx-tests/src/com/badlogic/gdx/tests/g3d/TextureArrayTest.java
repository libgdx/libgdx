/*
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;

/** @author Tomski, cypherdare **/
public class TextureArrayTest extends GdxTest {
	
	private static final int TEX_W = 64, TEX_H = 32, TEX_D = 6;
	float[] counters;
	Pixmap canvas;

	TextureArray textureArray, pixmapTextureArray;
	TextureRegion[] regions;
	Mesh terrain;

	ShaderProgram terrainShader, batchShader;
	SpriteBatch batch;

	PerspectiveCamera camera;
	FirstPersonCameraController cameraController;

	Matrix4 modelView = new Matrix4();

	GLProfiler glProfiler;

	@Override
	public void create () {
		glProfiler = new GLProfiler(Gdx.graphics);
		glProfiler.enable();

		ShaderProgram.prependVertexCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n #extension GL_EXT_texture_array : enable\n" : "#version 300 es\n";
		ShaderProgram.prependFragmentCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n #extension GL_EXT_texture_array : enable\n" : "#version 300 es\n";

		String[] texPaths = new String[] {  "data/g3d/materials/Searing Gorge.jpg",  "data/g3d/materials/Lava Cracks.jpg", "data/g3d/materials/Deep Fire.jpg" };

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(8, 10f, 20f);
		camera.lookAt(10, 0, 10);
		camera.up.set(0, 1, 0);
		camera.update();
		cameraController = new FirstPersonCameraController(camera);
		Gdx.input.setInputProcessor(cameraController);

		textureArray = new TextureArray(texPaths);
		textureArray.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		terrainShader = loadTextureArrayShader("data/shaders/texturearray.vert", "data/shaders/texturearray.frag");

		int vertexStride = 6;
		int vertexCount = 100 * 100;
		terrain = new Mesh(false, vertexCount * 6, 0, new VertexAttributes(VertexAttribute.Position(), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 3, ShaderProgram.TEXCOORD_ATTRIBUTE + 0)));

		Pixmap data = new Pixmap(Gdx.files.internal("data/g3d/heightmap.png"));
		float[] vertices = new float[vertexCount * vertexStride * 6];
		int idx = 0;
		for (int i = 0; i < 100 - 1; i++) {
			for (int j = 0; j < 100 - 1; j++) {
				idx = addVertex(i, j, vertices, data, idx);
				idx = addVertex(i, j + 1, vertices, data, idx);
				idx = addVertex(i + 1, j, vertices, data, idx);

				idx = addVertex(i, j + 1, vertices, data, idx);
				idx = addVertex(i + 1, j + 1, vertices, data, idx);
				idx = addVertex(i + 1, j, vertices, data, idx);
			}
		}
		terrain.setVertices(vertices);

		data.dispose();
		
		batchShader = loadTextureArrayShader("data/shaders/batchTextureArray.vert", "data/shaders/batchTextureArray.frag");
		batch = new SpriteBatch(100, batchShader);
		batch.disableBlending();

		canvas = new Pixmap(TEX_W, TEX_H, Pixmap.Format.RGBA8888);
		pixmapTextureArray = new TextureArray(Pixmap.Format.RGBA8888, false, TEX_W, TEX_H, TEX_D);
		counters = new float[TEX_D];
		regions = new TextureRegion[TEX_D];
		for (int i=0; i<TEX_D; i++){
			counters[i] = (float)(TEX_D - i);
			drawRandomPattern(i);
			regions[i] = new TextureRegion(pixmapTextureArray);
			TextureRegion.putLayerInCoordinates(regions[i], i);
		}
	}

	Color tmpColor = new Color();
	private int addVertex (int i, int j, float[] vertsOut, Pixmap heightmap, int idx) {
		int pixel = heightmap.getPixel((int) (i/100f * heightmap.getWidth()), (int)(j/100f * heightmap.getHeight()));
		tmpColor.set(pixel);
		vertsOut[idx++] = i/5f;
		vertsOut[idx++] = tmpColor.r * 25f/5f;
		vertsOut[idx++] = j/ 5f;
		vertsOut[idx++] = i/20f;
		vertsOut[idx++] = j/20f;
		vertsOut[idx++] = (tmpColor.r * 3f) - 0.5f;
		return idx;
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glCullFace(GL20.GL_BACK);

		modelView.translate(10f, 0, 10f).rotate(0, 1f, 0, 2f * Gdx.graphics.getDeltaTime()).translate(-10f, 0, -10f);

		cameraController.update();

		textureArray.bind();
		terrainShader.begin();
		terrainShader.setUniformi("u_textureArray", 0);
		terrainShader.setUniformMatrix("u_projViewTrans", camera.combined);
		terrainShader.setUniformMatrix("u_modelView", modelView);
		terrain.render(terrainShader, GL20.GL_TRIANGLES);
		terrainShader.end();
		
		float delta = Gdx.graphics.getDeltaTime();
		for (int i=0; i<counters.length; i++){
			counters[i] += delta;
			if (counters[i] > TEX_D){
				drawRandomPattern(i);
				counters[i] = counters[i] % (float)TEX_D;
			}
		}
		int spacing = ((int)(2 / batch.getProjectionMatrix().getValues()[Matrix4.M00]) - (TEX_W * TEX_D)) / (TEX_D + 1);
		batch.begin();
		for (int i=0; i<TEX_D; i++){
			batch.draw(regions[i], spacing + i * (spacing + TEX_W), 10);
		}
		batch.end();
	}
	
	void drawRandomPattern (int layer){
		canvas.setColor(MathUtils.random(1f), MathUtils.random(1f), MathUtils.random(1f), 1f);
		canvas.fill();
		int interval = MathUtils.random(8, 40);
		canvas.setColor(MathUtils.random(1f), MathUtils.random(1f), MathUtils.random(1f), 1f);
		for (int i=0; i<TEX_W; i++){
			for (int j=0; j<TEX_H; j++){
				int idx = i * TEX_H + j;
				if (idx % interval == 0) canvas.drawPixel(i, j);
			}
		}
		pixmapTextureArray.setDrawLayer(layer);
		pixmapTextureArray.draw(canvas, 0, 0);
	}

	@Override
	public void dispose () {
		terrain.dispose();
		terrainShader.dispose();
		batchShader.dispose();
		batch.dispose();
		textureArray.dispose();
		pixmapTextureArray.dispose();
		canvas.dispose();
	}
	
	static ShaderProgram loadTextureArrayShader (String vertexPath, String fragmentPath) {
		ShaderProgram.prependFragmentCode = ShaderProgram.prependVertexCode = 
			Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n #extension GL_EXT_texture_array : enable\n" : "#version 300 es\n";
		
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal(vertexPath), Gdx.files.internal(fragmentPath));
		System.out.println(shader.getLog());
		
		ShaderProgram.prependFragmentCode = ShaderProgram.prependVertexCode = "";
		return shader;
	}
}
