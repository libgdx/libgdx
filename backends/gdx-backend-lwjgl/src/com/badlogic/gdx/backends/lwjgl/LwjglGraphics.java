/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.backends.lwjgl;

import java.awt.Canvas;
import java.awt.Toolkit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.MathUtils;

/**
 * An implementation of the {@link Graphics} interface based on Lwjgl.
 * @author mzechner
 */
public final class LwjglGraphics implements Graphics {
	static int major, minor;

	GLCommon gl;
	GL10 gl10;
	GL11 gl11;
	GL20 gl20;
	GLU glu;
	final boolean useGL2;
	float deltaTime = 0;
	long frameStart = 0;
	int frames = 0;
	int fps;
	long lastTime = System.nanoTime();
	int width;
	int height;
	String title;
	Canvas canvas;
	boolean enforcePotImages = true;

	LwjglGraphics (String title, int width, int height, boolean useGL2IfAvailable) {
		useGL2 = useGL2IfAvailable;
		this.title = title;
		this.width = width;
		this.height = height;
	}

	LwjglGraphics (Canvas canvas, boolean useGL2IfAvailable) {
		useGL2 = useGL2IfAvailable;
		this.title = "";
		this.width = canvas.getWidth();
		this.height = canvas.getHeight();
		this.canvas = canvas;
	}

	public GL10 getGL10 () {
		return gl10;
	}

	public GL11 getGL11 () {
		return gl11;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	public GLU getGLU () {
		return glu;
	}

	public int getHeight () {
		if (canvas != null)
			return canvas.getHeight();
		else
			return height;
	}

	public int getWidth () {
		if (canvas != null)
			return canvas.getWidth();
		else
			return width;
	}

	public boolean isGL11Available () {
		return gl11 != null;
	}

	public boolean isGL20Available () {
		return gl20 != null;
	}

	public float getDeltaTime () {
		return deltaTime;
	}

	public GraphicsType getType () {
		return GraphicsType.LWJGL;
	}

	public int getFramesPerSecond () {
		return fps;
	}

	@Override public GLCommon getGLCommon () {
		return gl;
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

	void setupDisplay () throws LWJGLException {
		if (canvas != null) {
			Display.setParent(canvas);
		} else {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setFullscreen(false);
			Display.setTitle(title);
		}
		int samples = 0;
		try {
			Display.create(new PixelFormat(8, 8, 0, samples));
		} catch (Exception ex) {
			Display.destroy();
			try {
				Display.create(new PixelFormat(8, 8, 0));
			} catch (Exception ex2) {
				Display.destroy();
				try {
					Display.create(new PixelFormat());
				} catch (Exception ex3) {
					if (ex3.getMessage().contains("Pixel format not accelerated"))
						throw new GdxRuntimeException("OpenGL is not supported by the video driver.", ex3);
				}
			}
		}

		initiateGLInstances();
	}

	private void initiateGLInstances () {
		String version = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VERSION);
		major = Integer.parseInt("" + version.charAt(0));
		minor = Integer.parseInt("" + version.charAt(2));

		if (useGL2 && major >= 2) {
			// FIXME add check whether gl 2.0 is supported
			gl20 = new LwjglGL20();
			gl = gl20;
		} else {
			if (major == 1 && minor < 5) {
				gl10 = new LwjglGL10();
			} else {
				gl11 = new LwjglGL11();
				gl10 = gl11;
			}
			gl = gl10;
		}

		glu = new LwjglGLU();

		Gdx.glu = glu;
		Gdx.gl = gl;
		Gdx.gl10 = gl10;
		Gdx.gl11 = gl11;
		Gdx.gl20 = gl20;
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

	public void setEnforcePotImages (boolean enforcePotImages) {
		this.enforcePotImages = enforcePotImages;
	}
}
