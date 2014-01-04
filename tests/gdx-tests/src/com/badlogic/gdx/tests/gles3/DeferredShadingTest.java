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
import java.util.Random;

import com.badlogic.gdx.Gdx;
import static com.badlogic.gdx.Gdx.gl30;
import static com.badlogic.gdx.graphics.GL30.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.gles3.TextureFormatES3.TextureParameters;

public class DeferredShadingTest extends AbstractES3test {
	ShaderProgramES3 gBufferProgram;
	ShaderProgramES3 pointLightProgram;
	ShaderProgramES3 compositionProgram;
	ShaderProgramES3 intermediaryColorProgram;
	ShaderProgramES3 intermediaryDepthProgram;

	FrameBufferObject fboGbufferModels;
	FrameBufferObject fboGbufferLights;

	GenericTexture modelAlbedo;
	GenericTexture modelNormal;
	VBOGeometry fsQuad;
	VBOGeometry sphere;
	VBOGeometry model1;
	VBOGeometry model2;

	// gbuffer contents
	TextureFormatES3 albedoFormat;
	TextureFormatES3 normalFormat;
	TextureFormatES3 depthFormat;
	TextureFormatES3 lightFormat;
	GenericTexture gbufferAlbedo;
	GenericTexture gbufferNormals;
	GenericTexture gbufferDepth;
	GenericTexture gbufferLight;

	// scene info
	UniformBufferObject pointLightView;
	UniformBufferObject pointLightParams;
	UniformBufferObject cameraBuffer;
	Pointlight[] lights;
	Matrix4 proj = new Matrix4();
	Matrix4 projInv = new Matrix4();
	Matrix4 view = new Matrix4();
	Matrix4 viewProj = new Matrix4();
	float progress = 0;
	final float viewOffset = 3.0f;
	final int numShownGbufferTextures = 4;
	float w, h;

	static final class Pointlight {
		Vector3 worldPos = new Vector3();
		float distLimit = 1; // = dropoff limit
		Vector3 colorMagnitude = new Vector3(1, 1, 1);
	}

	@Override
	public boolean createLocal () {
		if (!loadTextures()) return false;
		if (!loadShaders()) return false;

		prepareFramebuffers();
		prepareUniformbuffers();

		long usage = Usage.Position | Usage.Normal | Usage.Tangent | Usage.BiNormal | Usage.TextureCoordinates;
		model1 = VBOGeometry.box(usage);
		model2 = VBOGeometry.sphere(usage);
		sphere = VBOGeometry.sphere(Usage.Position);
		fsQuad = VBOGeometry.fsQuad(Usage.Position);

		return true;
	}

	/** Any resize will force the screen-sized gbuffer to re-initialize its contents. New textures will be created and bound to the
	 * required FBO's */
	@Override
	protected void resizeLocal (int width, int height) {
		if (gbufferAlbedo != null) gbufferAlbedo.dispose();
		if (gbufferNormals != null) gbufferNormals.dispose();
		if (gbufferDepth != null) gbufferDepth.dispose();
		if (gbufferLight != null) gbufferLight.dispose();

		albedoFormat.width = normalFormat.width = depthFormat.width = lightFormat.width = width;
		albedoFormat.height = normalFormat.height = depthFormat.height = lightFormat.height = height;

		gbufferAlbedo = new GenericTexture(albedoFormat);
		gbufferNormals = new GenericTexture(normalFormat);
		gbufferDepth = new GenericTexture(depthFormat);
		gbufferLight = new GenericTexture(lightFormat);

		fboGbufferModels.bind();
		gbufferAlbedo.setFBOBinding(GL_COLOR_ATTACHMENT0);
		gbufferNormals.setFBOBinding(GL_COLOR_ATTACHMENT1);
		gbufferDepth.setFBOBinding(GL_DEPTH_ATTACHMENT);
		fboGbufferModels.unbind();

		fboGbufferLights.bind();
		gbufferLight.setFBOBinding(GL_COLOR_ATTACHMENT0);
		fboGbufferLights.unbind();

		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();
		super.isCreated = true;
	}

	/** Loads textures from files, and prepares the gbuffer to use the correct texture formats (The gbuffer is filled once resize is
	 * called). */
	private boolean loadTextures () {
		TextureParameters imageParams = new TextureParameters();
		imageParams.magFilter = imageParams.minFilter = GL_LINEAR;

		modelAlbedo = new GenericTexture(Gdx.files.internal("data/brick_albedo.jpg"));
		modelAlbedo.setTexParameters(imageParams);
		modelNormal = new GenericTexture(Gdx.files.internal("data/brick_normal.jpg"));
		modelNormal.setTexParameters(imageParams);

		TextureParameters bufferParams = new TextureParameters();

		albedoFormat = new TextureFormatES3();
		albedoFormat.glInternalFormat = GL_RGBA8;
		albedoFormat.glFormat = GL_RGBA;
		albedoFormat.glType = GL_UNSIGNED_BYTE;
		albedoFormat.params.copyFrom(bufferParams);

		normalFormat = new TextureFormatES3();
		normalFormat.glInternalFormat = GL_RGBA8;
		normalFormat.glFormat = GL_RGBA;
		normalFormat.glType = GL_UNSIGNED_BYTE;
		normalFormat.params.copyFrom(bufferParams);

		depthFormat = new TextureFormatES3();
		depthFormat.glInternalFormat = GL_DEPTH_COMPONENT24;
		depthFormat.glFormat = GL_DEPTH_COMPONENT;
		depthFormat.glType = GL_UNSIGNED_INT;
		depthFormat.params.copyFrom(bufferParams);

		lightFormat = new TextureFormatES3();
		lightFormat.glInternalFormat = GL_RGB16F;
		lightFormat.glFormat = GL_RGBA;
		lightFormat.glType = GL_HALF_FLOAT;
		lightFormat.params.copyFrom(bufferParams);

		return true;
	}

	private boolean loadShaders () {
		String vert_src, frag_src;

		//
		vert_src = Gdx.files.internal("data/shaders/def_gbuffer.vert").readString();
		frag_src = Gdx.files.internal("data/shaders/def_gbuffer.frag").readString();
		gBufferProgram = new ShaderProgramES3(vert_src, frag_src);
		if (!gBufferProgram.isCompiled()) {
			System.out.println("gBufferProgram:\n" + gBufferProgram.getErrorLog());
			return false;
		}
		gBufferProgram.registerTextureSampler("albedoTexture").setBinding(0);
		gBufferProgram.registerTextureSampler("normalTexture").setBinding(1);
		gBufferProgram.registerUniformBlock("CameraMatrices").setBinding(0);

		//
		vert_src = Gdx.files.internal("data/shaders/def_lighting.vert").readString();
		frag_src = Gdx.files.internal("data/shaders/def_lighting.frag").readString();
		pointLightProgram = new ShaderProgramES3(vert_src, frag_src);
		if (!pointLightProgram.isCompiled()) {
			System.out.println("pointLightProgram:\n" + pointLightProgram.getErrorLog());
			return false;
		}
		pointLightProgram.registerTextureSampler("normalBuffer").setBinding(0);
		pointLightProgram.registerTextureSampler("depthBuffer").setBinding(1);
		pointLightProgram.registerUniformBlock("CameraMatrices").setBinding(0);
		pointLightProgram.registerUniformBlock("LightView").setBinding(1);
		pointLightProgram.registerUniformBlock("LightParams").setBinding(2);

		//
		vert_src = Gdx.files.internal("data/shaders/def_quad.vert").readString();
		frag_src = Gdx.files.internal("data/shaders/def_composition.frag").readString();
		compositionProgram = new ShaderProgramES3(vert_src, frag_src);
		if (!compositionProgram.isCompiled()) {
			System.out.println("compositionProgram:\n" + compositionProgram.getErrorLog());
			return false;
		}
		compositionProgram.registerTextureSampler("albedoBuffer").setBinding(0);
		compositionProgram.registerTextureSampler("lightBuffer").setBinding(1);

		//
		vert_src = Gdx.files.internal("data/shaders/def_quad.vert").readString();
		frag_src = Gdx.files.internal("data/shaders/def_intermediary_color.frag").readString();
		intermediaryColorProgram = new ShaderProgramES3(vert_src, frag_src);
		if (!intermediaryColorProgram.isCompiled()) {
			System.out.println("intermediaryColorProgram:\n" + intermediaryColorProgram.getErrorLog());
			return false;
		}
		intermediaryColorProgram.registerTextureSampler("colorBuffer").setBinding(0);

		//
		vert_src = Gdx.files.internal("data/shaders/def_quad.vert").readString();
		frag_src = Gdx.files.internal("data/shaders/def_intermediary_depth.frag").readString();
		intermediaryDepthProgram = new ShaderProgramES3(vert_src, frag_src);
		if (!intermediaryDepthProgram.isCompiled()) {
			System.out.println("intermediaryDepthProgram:\n" + intermediaryDepthProgram.getErrorLog());
			return false;
		}
		intermediaryDepthProgram.registerTextureSampler("depthBuffer").setBinding(0);

		return true;
	}

	private void prepareFramebuffers () {
		fboGbufferModels = new FrameBufferObject(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1);

		fboGbufferLights = new FrameBufferObject(GL_COLOR_ATTACHMENT0);
	}

	private void prepareUniformbuffers () {
		final int floatSize = 4;

		// 4 float matrices
		cameraBuffer = new UniformBufferObject(floatSize * (16 * 4), 0);

		// light 3d position, radius and magnitude (color+intensity)
		final int numLights = 6;
		pointLightView = new UniformBufferObject(floatSize * 4 * numLights, 1);
		pointLightParams = new UniformBufferObject(floatSize * 8 * numLights, 2);

		MathUtils.random.setSeed(0);
		lights = new Pointlight[numLights];
		for (int i = 0, j = 0; i < numLights; ++i) {
			float rad = MathUtils.PI2 * (float)i / (float)numLights;
			lights[i] = new Pointlight();
			lights[i].worldPos.set(MathUtils.cos(rad), 0, MathUtils.sin(rad));
			lights[i].distLimit = MathUtils.random(0.1f, 2.0f);
			lights[i].colorMagnitude.set(MathUtils.random(0.5f, 3.2f), MathUtils.random(0.5f, 3.2f), MathUtils.random(0.5f, 3.2f));
		}
		lights[0].colorMagnitude.scl(4);

		float[] rawParams = new float[numLights * 8];
		for (int i = 0, j = 0; i < numLights; ++i) {
			rawParams[j++] = lights[i].worldPos.x;
			rawParams[j++] = lights[i].worldPos.y;
			rawParams[j++] = lights[i].worldPos.z;
			rawParams[j++] = lights[i].distLimit;
			rawParams[j++] = lights[i].colorMagnitude.x;
			rawParams[j++] = lights[i].colorMagnitude.y;
			rawParams[j++] = lights[i].colorMagnitude.z;
			rawParams[j++] = 0;
		}
		FloatBuffer fb = pointLightParams.getDataBuffer().asFloatBuffer();
		fb.position(0);
		fb.put(rawParams);
	}

	@Override
	public void renderLocal () {
		updateView(w / h);
		updateLights();

		gl30.glClearColor(0, 0, 0, 0);
		gl30.glEnable(GL_CULL_FACE);

		gl30.glDepthMask(true);
		gl30.glDepthFunc(GL_LESS);
		gl30.glEnable(GL_DEPTH_TEST);
		gl30.glCullFace(GL_BACK);
		renderModels();

		gl30.glBlendFunc(GL_ONE, GL_ONE);
		gl30.glEnable(GL_BLEND);
		gl30.glDepthMask(false);
		gl30.glDepthFunc(GL_GREATER);
		gl30.glCullFace(GL_FRONT);
		renderLights();

		gl30.glDisable(GL_CULL_FACE);
		gl30.glDisable(GL_BLEND);
		gl30.glDisable(GL_DEPTH_TEST);
		renderComposition();

		renderGbufferContents();
	}

	private boolean once = true;

	private void updateView (float aspectRatio) {

		// update camera
		progress += 0.01f;
		float near = 0.33f;
		float far = 16.8f;
		proj.setToProjection(near, far, 45, aspectRatio);
		projInv.set(proj).inv();
		float px = MathUtils.cos(progress);
		float py = 0.2f * MathUtils.sin(progress * 4.0f);
		float pz = MathUtils.sin(progress);
		view.setToLookAt(new Vector3(px, py, pz).scl(viewOffset), Vector3.Zero, Vector3.Y);
		viewProj.set(proj).mul(view);

		// store camera matrices in buffer
		FloatBuffer fb = cameraBuffer.getDataBuffer().asFloatBuffer();
		fb.position(0);
		fb.put(view.val).put(proj.val).put(viewProj.val).put(projInv.val);
	}

	/** Transform all lights from world space into view space in one go. */
	private void updateLights () {
		float[] subData = new float[4 * lights.length];
		for (int i = 0, j = 0; i < lights.length; ++i) {
			subData[j++] = lights[i].worldPos.x;
			subData[j++] = lights[i].worldPos.y;
			subData[j++] = lights[i].worldPos.z;
			subData[j++] = 0;
		}
		Matrix4.mulVec(view.val, subData, 0, lights.length, 4);

		FloatBuffer fb = pointLightView.getDataBuffer().asFloatBuffer();
		fb.position(0);
		fb.put(subData);
	}

	/** Render all models in the scene into the gbuffer. */
	private void renderModels () {
		fboGbufferModels.bind();

		// clear buffers in the fbo
		gl30.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// draw all models here

		gBufferProgram.use();
		cameraBuffer.bind();

		modelAlbedo.bind(0);
		modelNormal.bind(1);
		model1.bind();
		model1.draw();

		// assume all models are drawn now

		fboGbufferModels.unbind();
	}

	private void renderLights () {
		fboGbufferLights.bind();

		gl30.glClear(GL_COLOR_BUFFER_BIT);

		pointLightProgram.use();
		cameraBuffer.bind();
		pointLightView.bind();
		pointLightParams.bind();
		gbufferNormals.bind(0);
		gbufferDepth.bind(1);

		sphere.bind();
		sphere.drawInstances(lights.length);

		fboGbufferLights.unbind();
	}

	/** Show contents of the gbuffer for debugging. */
	private void renderGbufferContents () {
		if (numShownGbufferTextures <= 0) return;

		gl30.glDisable(GL_DEPTH_TEST);

		int smallVPwidth = (int)(w / numShownGbufferTextures);
		int smallVPheight = (int)(h / numShownGbufferTextures);
		int y = 0;

		gl30.glViewport(0, y, smallVPwidth, smallVPheight);
		y += smallVPheight;

		intermediaryColorProgram.use();
		gbufferAlbedo.bind(0);
		fsQuad.bind();
		fsQuad.draw();

		gl30.glViewport(0, y, smallVPwidth, smallVPheight);
		y += smallVPheight;

		intermediaryColorProgram.use();
		gbufferNormals.bind(0);
		fsQuad.bind();
		fsQuad.draw();

		gl30.glViewport(0, y, smallVPwidth, smallVPheight);
		y += smallVPheight;

		intermediaryDepthProgram.use();
		gbufferDepth.bind(0);
		fsQuad.bind();
		fsQuad.draw();

		gl30.glViewport(0, y, smallVPwidth, smallVPheight);
		y += smallVPheight;

		intermediaryColorProgram.use();
		gbufferLight.bind(0);
		fsQuad.bind();
		fsQuad.draw();

		gl30.glViewport(0, 0, (int)w, (int)h);
	}

	/** Combine the gathered lighting information with the albedo of the models to get the final image. */
	private void renderComposition () {

		compositionProgram.use();
		gbufferAlbedo.bind(0);
		gbufferLight.bind(1);

		fsQuad.bind();
		fsQuad.draw();
	}

}
