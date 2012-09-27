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
package com.badlogic.gdx.backends.ios;

import cli.MonoTouch.Foundation.ExportAttribute;
import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.CoreAnimation.CAEAGLLayer;
import cli.MonoTouch.ObjCRuntime.Selector;
import cli.MonoTouch.OpenGLES.EAGLColorFormat;
import cli.MonoTouch.OpenGLES.EAGLRenderingAPI;
import cli.MonoTouch.UIKit.UIDevice;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIUserInterfaceIdiom;
import cli.OpenTK.FrameEventArgs;
import cli.OpenTK.Platform.iPhoneOS.iPhoneOSGameView;
import cli.System.EventArgs;
import cli.System.Drawing.RectangleF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Pixmap;

public class IOSGraphics extends iPhoneOSGameView implements Graphics {
	IOSApplication app;
	IOSInput input;
	IOSGLES20 gl20;
	int width;
	int height;
	long lastFrameTime;
	float deltaTime;
	long framesStart;
	int frames;
	int fps;

	private float ppiX = 0;
	private float ppiY = 0;
	private float ppcX = 0;
	private float ppcY = 0;
	private float density = 1;

	public IOSGraphics(RectangleF bounds, IOSApplication app, IOSInput input) {
		super(bounds);
		
		// setup view and OpenGL
		width = (int)bounds.get_Width();
		height = (int)bounds.get_Height();
		app.log("IOSGraphics", bounds.get_Width() + "x" + bounds.get_Height() + ", " + UIScreen.get_MainScreen().get_Scale());
		this.app = app;
		this.input = input;
		set_LayerRetainsBacking(false);
		set_ContentScaleFactor(1);
		set_MultipleTouchEnabled(true);
		set_AutoResize(false);
		set_LayerColorFormat(EAGLColorFormat.RGB565);
		set_ContextRenderingApi(EAGLRenderingAPI.wrap(EAGLRenderingAPI.OpenGLES2));
		gl20 = new IOSGLES20();
		Gdx.gl = gl20;
		Gdx.gl20 = gl20;
		
		// determine display density and PPI (PPI values via Wikipedia!)
		if ((UIScreen.get_MainScreen().RespondsToSelector(new Selector("scale:"))) &&
		    (UIScreen.get_MainScreen().get_Scale() == 2.0f)) {
			// Retina display!
			density = 2.0f;
		}
		else {
			// regular display
			density = 1.0f;
		}
		int ppi;  
		if (UIDevice.get_CurrentDevice().get_UserInterfaceIdiom().Value == UIUserInterfaceIdiom.Pad) {
			// iPad
			ppi = Math.round(density * 132);
		}
		else {
			// iPhone or iPodTouch
			ppi = Math.round(density * 163);
		}
		ppiX = ppi;
		ppiY = ppi;
		ppcX = ppiX / 2.54f;
		ppcY = ppcY / 2.54f;
		app.log("IOSGraphics", "Display: ppi=" + ppi + ", density=" + density);
		
		// time + FPS
		lastFrameTime = System.nanoTime();
		framesStart = lastFrameTime;
	}

	@Override
	protected void ConfigureLayer(CAEAGLLayer layer) {
		layer.set_Opaque(true);
		super.ConfigureLayer(layer);
	}

	@Override
	protected void OnLoad(EventArgs arg0) {
		super.OnLoad(arg0);
		MakeCurrent();
		app.listener.create();
	}

	@Override
	protected void OnRenderFrame(FrameEventArgs arg0) {
		super.OnRenderFrame(arg0);
		
		long time = System.nanoTime();
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;
		
		frames++;
		if (time - framesStart >= 1000000000l) {
			framesStart = time;
			fps = frames;
			frames = 0;
		}
		
		MakeCurrent();
		((IOSInput)Gdx.input).processEvents();
		app.listener.render();
		SwapBuffers();
	}

	@Override
	protected void OnResize(EventArgs event) {
		super.OnResize(event);

		// noblemaster: I don't think this method will get called on iOS!? (at least not as of 2012-09-27)
		Gdx.app.error("IOSGraphics", "OnResize(...) is not implement.");
	}

	@ExportAttribute.Annotation("layerClass")
	static cli.MonoTouch.ObjCRuntime.Class LayerClass() {
		return iPhoneOSGameView.GetLayerClass();
	}

	@Override
	public boolean isGL11Available() {
		return false;
	}

	@Override
	public boolean isGL20Available() {
		return true;
	}

	@Override
	public GLCommon getGLCommon() {
		return gl20;
	}

	@Override
	public GL10 getGL10() {
		return null;
	}

	@Override
	public GL11 getGL11() {
		return null;
	}

	@Override
	public GL20 getGL20() {
		return gl20;
	}

	@Override
	public GLU getGLU() {
		return null;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public float getDeltaTime() {
		return deltaTime;
	}

	@Override
	public float getRawDeltaTime() {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

	@Override
	public GraphicsType getType() {
		return GraphicsType.iOSGL;
	}

	@Override
	public float getPpiX () {
		return ppiX;
	}

	@Override
	public float getPpiY () {
		return ppiY;
	}

	@Override
	public float getPpcX () {
		return ppcX;
	}

	@Override
	public float getPpcY () {
		return ppcY;
	}

	/**
	 * Returns the display density.
	 * 
	 * @return 1.0f for non-retina devices, 2.0f for retina devices.
	 */
	@Override
	public float getDensity () {
		return density;
	}

	@Override
	public boolean supportsDisplayModeChange() {
		return false;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return null;
	}

	@Override
	public DisplayMode getDesktopDisplayMode() {
		return null;
	}

	@Override
	public boolean setDisplayMode(DisplayMode displayMode) {
		return false;
	}

	@Override
	public boolean setDisplayMode(int width, int height, boolean fullscreen) {
		return false;
	}

	@Override
	public void setTitle(String title) {
	}

	@Override
	public void setVSync(boolean vsync) {
	}

	@Override
	public BufferFormat getBufferFormat() {
		return null;
	}

	@Override
	public boolean supportsExtension(String extension) {
		return false;
	}

	@Override
	public void setContinuousRendering(boolean isContinuous) {
	}

	@Override
	public boolean isContinuousRendering() {
		return false;
	}

	@Override
	public void requestRendering() {
	}

	@Override
	public boolean isFullscreen() {
		return true;
	}

	@Override
	public void TouchesBegan(NSSet touches, UIEvent event) {
		super.TouchesBegan(touches, event);
		input.touchDown(touches, event);
	}

	@Override
	public void TouchesCancelled(NSSet touches, UIEvent event) {
		super.TouchesCancelled(touches, event);
		input.touchUp(touches, event);
	}

	@Override
	public void TouchesEnded(NSSet touches, UIEvent event) {
		super.TouchesEnded(touches, event);
		input.touchUp(touches, event);
	}

	@Override
	public void TouchesMoved(NSSet touches, UIEvent event) {
		super.TouchesMoved(touches, event);
		input.touchMoved(touches, event);
	}
}