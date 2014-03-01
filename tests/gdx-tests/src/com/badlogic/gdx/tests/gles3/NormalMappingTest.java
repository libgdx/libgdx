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

package com.badlogic.gdx.tests.gles3;

import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.gles3.TextureFormatES3.TextureParameters;

public class NormalMappingTest extends AbstractES3test {
	ShaderProgramES3 shader;
	VBOGeometry geom;
	GenericTexture normalTexture;
	UniformBufferObject cameraBuffer;
	Matrix4 proj = new Matrix4();
	Matrix4 view = new Matrix4();
	float progress = 0;
	final float viewOffset = 2.0f;

	private final String vertexShader = "#version 300 es                                    \n"
		+ "uniform CameraMatrices {                                                          \n"
		+ "   mat4 view;                                                                     \n"
		+ "   mat4 projection;                                                               \n"
		+ "};                                                                                \n"
		+ "layout(location = 0)in vec3 inPos;                                                \n"
		+ "layout(location = 1)in vec3 inNorm;                                               \n"
		+ "layout(location = 2)in vec3 inTan;                                                \n"
		+ "layout(location = 3)in vec3 inBiTan;                                              \n"
		+ "layout(location = 4)in vec2 inTex;                                                \n"
		+ "out mat3 tbnMatrix;                                                               \n"
		+ "out vec2 texcoord;                                                                \n"
		+ "void main()                                                                       \n"
		+ "{                                                                                 \n"
		+ "   gl_Position = projection * view * vec4(inPos, 1);                              \n"
		+ "                                                                                  \n"
		+ "   texcoord = inTex;                                                              \n"
		+ "                                                                                  \n"
		+ "   mat3 normalMat = mat3(view);                                                   \n"
		+ "   tbnMatrix = mat3(normalMat * inTan,                                            \n"
		+ "                    normalMat * inBiTan,                                          \n"
		+ "                    normalMat * inNorm);                                          \n"
		+ "}                                                                                 \n";

	private final String fragmentNormalShader = "#version 300 es                            \n"
		+ "precision lowp float;                                                             \n"
		+ "uniform sampler2D normalTexture;                                                  \n"
		+ "in mat3 tbnMatrix;                                                                \n"
		+ "in vec2 texcoord;                                                                 \n"
		+ "out vec4 fragColor;                                                               \n"
		+ "void main()                                                                       \n"
		+ "{                                                                                 \n"
		+ "   vec3 mapNormal = texture2D(normalTexture, texcoord).xyz * 2.0 - 1.0;           \n"
		+ "                                                                                  \n"
		+ "   vec3 viewNormal = tbnMatrix * mapNormal;                                       \n"
		+ "   viewNormal = normalize(viewNormal) * 0.5 + 0.5;                                \n"
		+ "                                                                                  \n"
		+ "   fragColor = vec4(viewNormal,1);                                                \n"
		+ "}                                                                                 \n";

	private final String fragmentLightingShader = "#version 300 es                          \n"
		+ "precision lowp float;                                                             \n"
		+ "uniform sampler2D normalTexture;                                                  \n"
		+ "in mat3 tbnMatrix;                                                                \n"
		+ "in vec2 texcoord;                                                                 \n"
		+ "out vec4 fragColor;                                                               \n"
		+ "void main()                                                                       \n"
		+ "{                                                                                 \n"
		+ "   vec3 mapNormal = texture2D(normalTexture, texcoord).xyz * 2.0 - 1.0;           \n"
		+ "                                                                                  \n"
		+ "   vec3 viewNormal = tbnMatrix * mapNormal;                                       \n"
		+ "                                                                                  \n"
		+ "   vec3 lightDirection = normalize(vec3(1,1,1));                                  \n"
		+ "   float lightContribution = clamp(dot(viewNormal, lightDirection), 0.0, 1.0);    \n"
		+ "                                                                                  \n"
		+ "   fragColor = vec4(vec3(lightContribution), 1);                                  \n"
		+ "}                                                                                 \n";

	@Override
	public boolean createLocal () {
		// load the shader
		shader = new ShaderProgramES3(vertexShader, fragmentLightingShader);
		if (!shader.isCompiled()) {
			System.out.println(shader.getErrorLog());
			return false;
		}
		shader.registerTextureSampler("normalTexture").setBinding(0);
		shader.registerUniformBlock("CameraMatrices").setBinding(3);
		cameraBuffer = new UniformBufferObject(4 * 16 * 2, 3);

		//
		TextureParameters tParams = new TextureParameters();
		tParams.magFilter = tParams.minFilter = GL20.GL_LINEAR;
		FileHandle texFile = Gdx.files.internal("data/brick_normal.jpg");
		normalTexture = new GenericTexture(texFile);
		normalTexture.setTexParameters(tParams);

		//
		long atts = Usage.Position | Usage.Normal | Usage.Tangent | Usage.BiNormal | Usage.TextureCoordinates;
		geom = VBOGeometry.box(atts);

		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);

		return true;
	}

	private void updateView () {

		// update camera
		progress += 0.005f;
		float aspectRatio = Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		proj.setToProjection(0.01f, 100.0f, 45, aspectRatio);
		view.setToLookAt(
			new Vector3(MathUtils.cos(progress), 0.2f * MathUtils.sin(progress * 4.0f), MathUtils.sin(progress)).scl(viewOffset),
			Vector3.Zero, Vector3.Y);

		// store camera matrices in buffer
		FloatBuffer fb = cameraBuffer.getDataBuffer().asFloatBuffer();
		fb.position(0);
		fb.put(view.val).put(proj.val);
	}

	@Override
	public void renderLocal () {
		updateView();

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shader.use();
		normalTexture.bind();
		cameraBuffer.bind();
		geom.bind();
		geom.draw();
	}

	@Override
	protected void disposeLocal () {
		shader.dispose();
		geom.dispose();
		normalTexture.dispose();
		cameraBuffer.dispose();
	}
}
