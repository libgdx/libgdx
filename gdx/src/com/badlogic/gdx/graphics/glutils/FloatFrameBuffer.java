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

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** This is a {@link FrameBuffer} variant backed by a float texture. */
public class FloatFrameBuffer extends FrameBuffer {

	/**
	 * Creates a GLFrameBuffer from the specifications provided by {@param bufferBuilder}
	 *
	 * @param bufferBuilder
	 **/
	protected FloatFrameBuffer (FrameBufferBuilder bufferBuilder) {
		super(bufferBuilder);
	}

	/** Creates a new FrameBuffer with a float backing texture, having the given dimensions and potentially a depth buffer attached.
	 * 
	 * @param width the width of the framebuffer in pixels
	 * @param height the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @throws GdxRuntimeException in case the FrameBuffer could not be created */
	public static FloatFrameBuffer createFloatFrameBuffer (int width, int height, boolean hasDepth) {
		//super(null, width, height, hasDepth);
		return null;
	}

	@Override
	protected Texture createTexture (GLFrameBufferAttachmentSpec attachmentSpec) {
		FloatTextureData data = new FloatTextureData(bufferBuilder.width, bufferBuilder.height);
		Texture result = new Texture(data);
		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.Applet)
			result.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			// no filtering for float textures in OpenGL ES
			result.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		result.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		return result;
	}

	@Override
	protected void attachFrameBufferColorTexture (Texture texture) {
		super.attachFrameBufferColorTexture(texture);
	}
	
	@Override
	protected void disposeColorTexture (Texture colorTexture) {
		colorTexture.dispose();
	}
}
