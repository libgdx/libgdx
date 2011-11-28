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
package com.badlogic.gdx.backends.gwt;

import gwt.g3d.client.Surface3D;
import gwt.g3d.client.gl2.GL2;
import gwt.g3d.client.gl2.WebGLContextAttributes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.user.client.ui.Panel;

public class GwtGraphics implements Graphics {
	final Surface3D surface;
	final GL20 gl;
	String extensions;
	float fps = 0;
	long lastTimeStamp = System.currentTimeMillis();
	float deltaTime = 0;

	public GwtGraphics (Panel root, GwtApplicationConfiguration config) {
		// create surface per configuration
		WebGLContextAttributes contextAttribs = new WebGLContextAttributes();
		contextAttribs.setStencilEnable(config.stencil);
		contextAttribs.setAntialiasEnable(config.antialiasing);
		surface = new Surface3D(config.width, config.height, contextAttribs);
		root.add(surface);

		// check whether WebGL is supported
		GL2 gl = surface.getGL();
		if (gl == null) {
			throw new GdxRuntimeException("WebGL not supported");
		}

		this.gl = new GwtGL20(surface);

		// set initial viewport to cover entire surface and
		gl.viewport(0, 0, surface.getWidth(), surface.getHeight());
	}

	public Surface3D getSurface () {
		return surface;
	}

	@Override
	public boolean isGL11Available () {
		return false;
	}

	@Override
	public boolean isGL20Available () {
		return true;
	}

	@Override
	public GLCommon getGLCommon () {
		return gl;
	}

	@Override
	public GL10 getGL10 () {
		return null;
	}

	@Override
	public GL11 getGL11 () {
		return null;
	}

	@Override
	public GL20 getGL20 () {
		return gl;
	}

	@Override
	public GLU getGLU () {
		return null;
	}

	@Override
	public int getWidth () {
		return surface.getWidth();
	}

	@Override
	public int getHeight () {
		return surface.getHeight();
	}

	@Override
	public float getDeltaTime () {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond () {
		return (int)fps;
	}

	@Override
	public GraphicsType getType () {
		return GraphicsType.WebGL;
	}

	@Override
	public float getPpiX () {
		return 0;
	}

	@Override
	public float getPpiY () {
		return 0;
	}

	@Override
	public float getPpcX () {
		return 0;
	}

	@Override
	public float getPpcY () {
		return 0;
	}

	@Override
	public boolean supportsDisplayModeChange () {
		return false;
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		return null;
	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		return null;
	}

	@Override
	public boolean setDisplayMode (DisplayMode displayMode) {
		return false;
	}

	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		return false;
	}

	@Override
	public void setTitle (String title) {

	}

	@Override
	public void setVSync (boolean vsync) {

	}

	@Override
	public BufferFormat getBufferFormat () {
		return null;
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	public void setFps (float fps) {
		long currTimeStamp = System.currentTimeMillis();
		deltaTime = (currTimeStamp - lastTimeStamp) / 1000.0f;
		lastTimeStamp = currTimeStamp;
		this.fps = fps;
	}

	@Override
	public float getDensity () {
		throw new GdxRuntimeException("No supported");
	}

	@Override
	public void setIcon (Pixmap[] pixmaps) {
	}
}