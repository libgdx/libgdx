package com.badlogic.gdx.backends.ios;

import cli.MonoTouch.Foundation.ExportAttribute;
import cli.MonoTouch.CoreAnimation.CAEAGLLayer;
import cli.MonoTouch.OpenGLES.EAGLColorFormat;
import cli.MonoTouch.OpenGLES.EAGLRenderingAPI;
import cli.MonoTouch.UIKit.UIScreen;
import cli.OpenTK.FrameEventArgs;
import cli.OpenTK.Platform.iPhoneOS.iPhoneOSGameView;
import cli.System.EventArgs;
import cli.System.Drawing.RectangleF;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Pixmap;

public class IOSGraphics extends iPhoneOSGameView implements Graphics {
	IOSApplication app;

	public IOSGraphics(RectangleF bounds, IOSApplication app) {
		super(bounds);
		app.log("IOSGraphics", bounds.get_Width() + "x" + bounds.get_Height() + ", " + UIScreen.get_MainScreen().get_Scale());
		this.app = app;
		set_LayerRetainsBacking(false);
		set_ContentScaleFactor(1);
		set_MultipleTouchEnabled(true);
		set_AutoResize(false);
		set_LayerColorFormat(EAGLColorFormat.RGB565);
		set_ContextRenderingApi(EAGLRenderingAPI
				.wrap(EAGLRenderingAPI.OpenGLES2));
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
		app.listener.resize(0, 0); // FIXME
	}

	@Override
	protected void OnRenderFrame(FrameEventArgs arg0) {
		super.OnRenderFrame(arg0);
		MakeCurrent();
		app.listener.render();
		SwapBuffers();
	}

	@Override
	protected void OnResize(EventArgs arg0) {
		super.OnResize(arg0);
		MakeCurrent();
		app.listener.resize(0, 0); // FIXME
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
		return false;
	}

	@Override
	public GLCommon getGLCommon() {
		return null;
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
		return null;
	}

	@Override
	public GLU getGLU() {
		return null;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public float getDeltaTime() {
		return 0;
	}

	@Override
	public float getRawDeltaTime() {
		return 0;
	}

	@Override
	public int getFramesPerSecond() {
		return 0;
	}

	@Override
	public GraphicsType getType() {
		return null;
	}

	@Override
	public float getPpiX() {
		return 0;
	}

	@Override
	public float getPpiY() {
		return 0;
	}

	@Override
	public float getPpcX() {
		return 0;
	}

	@Override
	public float getPpcY() {
		return 0;
	}

	@Override
	public float getDensity() {
		return 0;
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
	public void setIcon(Pixmap[] pixmaps) {
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
		return false;
	}
}
