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

package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;



/** Creates a shadow cubemap for a PointLight by rendering the depth information for the objects in a scene in each of
 *  6 directions from the light.
 *  
 *  An example usage is
 *  	Environment environment = new Environment();
 * 	PointShadowLight shadowLight = new PointShadowLight(1024, 1024, 30f, 30f, 1f, 100f);
 *		shadowLight.set(...);
 *		environment.add(shadowLight);
 *		environment.shadowBox = shadowLight;
 *		ModelBatch modelBatch = new ModelBatch();
 *    ModelBatch shadowModelBatch(new DepthShaderProvider());
 *    ModelInstance instance = ...
 *		shadowLight.begin();
 *		while (shadowLight.nextSide()) {
 *        shadowModelBatch.begin(shadowLight.camera);
 *        shadowModelBatch.render(instance);
 *        shadowModelBatch.end();
 *		}
 *		shadowLight.end();
 *
 *		modelBatch.begin(cam);
 *		modelBatch.render(instance, environment);
 *		modelBatch.end();
 * 
 * @deprecated Experimental, likely to change, do not use!
 * @author ryanastout */
public class PointShadowLight extends PointLight implements Disposable {
	protected FrameBufferCubemap frameBufferCube;
	public Camera camera;
	protected TextureDescriptor textureDesc;
	private boolean beginCalled;

	public PointShadowLight (int shadowMapWidth, int shadowMapHeight, float shadowViewportWidth, float shadowViewportHeight,
		float shadowNear, float shadowFar) {
		camera = new PerspectiveCamera(90, shadowViewportWidth, shadowViewportHeight);
		camera.near = shadowNear;
		camera.far = shadowFar;

		textureDesc = new TextureDescriptor();
		textureDesc.minFilter = textureDesc.magFilter = Texture.TextureFilter.Nearest;
		textureDesc.uWrap = textureDesc.vWrap = Texture.TextureWrap.ClampToEdge;
		beginCalled = false;
		frameBufferCube = new FrameBufferCubemap(Pixmap.Format.RGB888, shadowMapWidth, shadowMapHeight, true);
	}

	public void begin () {
		final int w = frameBufferCube.getWidth();
		final int h = frameBufferCube.getHeight();
		frameBufferCube.begin();
		Gdx.gl.glViewport(0, 0, w, h);
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		camera.position.set(position);
		beginCalled = true;
	}

	public void end () {
		beginCalled = false;
		frameBufferCube.end();
	}

	public FrameBufferCubemap getFrameBufferCube () {
		return frameBufferCube;
	}

	@Override
	public void dispose () {
		if (frameBufferCube != null) frameBufferCube.dispose();
		frameBufferCube = null;
	}

	public boolean nextSide () {
		if (!beginCalled) {
			throw new GdxRuntimeException("call begin() before calling nextSide()");
		}

		if (!frameBufferCube.nextSide()) {
			return false;
		}
		frameBufferCube.getSide().getUp(camera.up);
		frameBufferCube.getSide().getDirection(camera.direction);
		camera.update();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		return true;
	}
}
