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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.webgl.client.WebGLContextAttributes;
import com.google.gwt.webgl.client.WebGLRenderingContext;

public class GwtGraphics implements Graphics {
	CanvasElement canvas;
	WebGLRenderingContext context;
	GL20 gl;
	String extensions;
	float fps = 0;
	long lastTimeStamp = System.currentTimeMillis();
	float deltaTime = 0;
	float time = 0;
	int frames;
	GwtApplicationConfiguration config;

	public GwtGraphics (Panel root, GwtApplicationConfiguration config) {
		Canvas canvasWidget = Canvas.createIfSupported();
		if(canvasWidget == null) throw new GdxRuntimeException("Canvas not supported");
		canvas = canvasWidget.getCanvasElement();
		root.add(canvasWidget);
		canvas.setWidth(config.width);
		canvas.setHeight(config.height);
		this.config = config;
		
		WebGLContextAttributes attributes = WebGLContextAttributes.create();
		attributes.setAntialias(config.antialiasing);
		attributes.setStencil(config.stencil);
		attributes.setAlpha(false);
		attributes.setPremultipliedAlpha(false);
		
		context = WebGLRenderingContext.getContext(canvas, attributes);
		context.viewport(0, 0, config.width, config.height);
		this.gl = config.useDebugGL?new GwtGL20Debug(context): new GwtGL20(context);
	}

	public WebGLRenderingContext getContext () {
		return context;
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
		return canvas.getWidth();
	}

	@Override
	public int getHeight () {
		return canvas.getHeight();
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
		return false; // FIXME
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		return null; // FIXME
	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		return null; // FIXME
	}

	@Override
	public boolean setDisplayMode (DisplayMode displayMode) {
		return false; // FIXME
	}

	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		return false; // FIXME
	}

	@Override
	public void setTitle (String title) {

	}

	@Override
	public void setVSync (boolean vsync) {

	}

	@Override
	public BufferFormat getBufferFormat () {
		return new BufferFormat(8, 8, 8, 8, 16, config.stencil?8:0, 0, false); // FIXME
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	public void update () {
		long currTimeStamp = System.currentTimeMillis();
		deltaTime = (currTimeStamp - lastTimeStamp) / 1000.0f;
		lastTimeStamp = currTimeStamp;
		time += deltaTime;
		frames++;
		if(time > 1) {
			this.fps = frames;
			time = 0;
			frames = 0;
		}
	}

	@Override
	public float getDensity () {
		throw new GdxRuntimeException("No supported");
	}

	@Override
	public void setIcon (Pixmap[] pixmaps) {
		throw new GdxRuntimeException("No supported");
	}

	@Override
	public void setContinuousRendering (boolean isContinuous) {
		throw new GdxRuntimeException("No supported");
	}

	@Override
	public boolean isContinuousRendering () {
		throw new GdxRuntimeException("No supported");
	}

	@Override
	public void requestRendering () {
		throw new GdxRuntimeException("No supported");
	}

	@Override
	public float getRawDeltaTime () {
		return getDeltaTime();
	}

	@Override
	public boolean isFullscreen () {
		return false;
	}
}