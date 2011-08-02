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
package com.badlogic.gdx.backends.angle;

import java.awt.Toolkit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Pixmap;

public class AngleGraphics implements Graphics {
	GL20 gl;
	GLU glu;
	int width;
	int height;
	long lastTime = System.nanoTime();
	long frameStart = System.nanoTime();
	int fps;
	int frames;
	float deltaTime = 0;
	String extensions;

	AngleGraphics (int width, int height) {
		gl = new AngleGLES20();
		glu = new AngleGLU();
	}

	@Override public boolean isGL11Available () {
		return false;
	}

	@Override public boolean isGL20Available () {
		return true;
	}

	@Override public GLCommon getGLCommon () {
		return gl;
	}

	@Override public GL10 getGL10 () {
		return null;
	}

	@Override public GL11 getGL11 () {
		return null;
	}

	@Override public GL20 getGL20 () {
		return gl;
	}
	
	@Override public GLU getGLU() {
		return glu;
	}

	@Override public int getWidth () {
		return width;
	}

	@Override public int getHeight () {
		return height;
	}

	@Override public float getDeltaTime () {
		return deltaTime;
	}

	@Override public int getFramesPerSecond () {
		return fps;
	}

	@Override public GraphicsType getType () {
		return GraphicsType.Angle;
	}

	@Override public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override public float getPpcX () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override public float getPpcY () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	void updateTime () {
		long time = System.nanoTime();
		deltaTime = (time - lastTime) / 1000000000.0f;
		lastTime = time;

		if (time - frameStart >= 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}

	@Override public boolean supportsDisplayModeChange () {
		return false;
	}

	@Override public boolean setDisplayMode (DisplayMode displayMode) {
		return false;
	}
	
	@Override public DisplayMode[] getDisplayModes () {
		return new DisplayMode[0];
	}

	@Override public void setTitle (String title) {
		
	}

	@Override public void setIcon (Pixmap pixmap) {
		
	}

	@Override public DisplayMode getDesktopDisplayMode () {
		return null;
	}

	@Override public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		return false;
	}

	@Override public void setVSync (boolean vsync) {
		// TODO Auto-generated method stub
		
	}

	@Override public BufferFormat getBufferFormat () {
		return null;
	}
	
	@Override public boolean supportsExtension (String extension) {
		if(extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	@Override
	public float getDensity() {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 160f);
	}
}
