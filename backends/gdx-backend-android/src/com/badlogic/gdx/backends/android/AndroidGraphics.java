/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * An implementation of {@link Graphics} for Android.
 * 
 * @author mzechner
 */
final class AndroidGraphics implements Graphics, Renderer {
	final View view;
	int width;
	int height;
	AndroidApplication app;
	GLCommon gl;
	GL10 gl10;
	GL11 gl11;
	GL20 gl20;
	
	private long lastFrameTime = System.nanoTime();	
	private float deltaTime = 0;	
	private long frameStart = System.nanoTime();
	private int frames = 0;	
	private int fps;	
	private WindowedMean mean = new WindowedMean(5);		

	boolean created = false;
	boolean running = false;
	boolean pause = false;
	boolean resume = false;
	boolean destroy = false;	
	
	public AndroidGraphics (AndroidApplication activity, boolean useGL2IfAvailable) {
		view = createGLSurfaceView(activity, useGL2IfAvailable);
		this.app = activity;
	}

	private View createGLSurfaceView (Activity activity, boolean useGL2) {
		if (useGL2 && checkGL20()) {
			GLSurfaceView20 view = new GLSurfaceView20(activity);
			view.setRenderer(this);
			return view;
		} else {
			if (Integer.parseInt(android.os.Build.VERSION.SDK) <= 4) {
				GLSurfaceViewCupcake view = new GLSurfaceViewCupcake(activity);
				view.setRenderer(this);
				return view;
			} else {
				android.opengl.GLSurfaceView view = new android.opengl.GLSurfaceView(activity);
				view.setRenderer(this);
				return view;
			}
		}

	}

	private boolean checkGL20 () {
		EGL10 egl = (EGL10)EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		int[] version = new int[2];
		egl.eglInitialize(display, version);

		int EGL_OPENGL_ES2_BIT = 4;
		int[] configAttribs = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE,
			EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

		EGLConfig[] configs = new EGLConfig[10];
		int[] num_config = new int[1];
		egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
		egl.eglTerminate(display);
		return num_config[0] > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public GL10 getGL10 () {
		return gl10;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public GL11 getGL11 () {
		return gl11;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public GL20 getGL20 () {
		return gl20;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getHeight () {
		return height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getWidth () {
		return width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean isGL11Available () {
		return gl11 != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean isGL20Available () {
		return gl20 != null;
	}

	private static boolean isPowerOfTwo (int value) {
		return ((value != 0) && (value & (value - 1)) == 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Pixmap newPixmap (int width, int height, Format format) {
		return new AndroidPixmap(width, height, format);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Pixmap newPixmap (InputStream in) {
		Bitmap bitmap = BitmapFactory.decodeStream(in);
		if (bitmap == null) throw new GdxRuntimeException("Couldn't load Pixmap from InputStream");	
		return new AndroidPixmap(bitmap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Pixmap newPixmap (FileHandle file) {
		return newPixmap(file.readFile());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Pixmap newPixmap (Object nativePixmap) {
		return new AndroidPixmap((Bitmap)nativePixmap);
	}

	/**
	 * This instantiates the GL10, GL11 and GL20 instances. Includes the check for certain devices that pretend to support GL11 but
	 * fuck up vertex buffer objects. This includes the pixelflinger which segfaults when buffers are deleted as well as the
	 * Motorola CLIQ and the Samsung Behold II.
	 * 
	 * @param gl
	 */
	private void setupGL (javax.microedition.khronos.opengles.GL10 gl) {
		if (gl10 != null || gl20 != null) return;

		if (view instanceof GLSurfaceView20) {
			gl20 = new AndroidGL20();
			this.gl = gl20;
		} else {
			gl10 = new AndroidGL10(gl);
			this.gl = gl10;
			if (gl instanceof javax.microedition.khronos.opengles.GL11) {
				String renderer = gl.glGetString(GL10.GL_RENDERER);
				if (!renderer.toLowerCase().contains("pixelflinger") &&
				    !(android.os.Build.MODEL.equals("MB200") || android.os.Build.MODEL.equals("MB220")
					|| android.os.Build.MODEL.contains("Behold"))) {
				gl11 = new AndroidGL11((javax.microedition.khronos.opengles.GL11)gl);
				gl10 = gl11;
				}
			}
		}
		
		Gdx.gl = this.gl;
		Gdx.gl10 = gl10;
		Gdx.gl11 = gl11;
		Gdx.gl20 = gl20;
	}

	@Override public void onSurfaceChanged (javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;		
		app.listener.resize(width, height);
	}

	@Override public void onSurfaceCreated (javax.microedition.khronos.opengles.GL10 gl, EGLConfig config) {
		setupGL(gl);

		Mesh.invalidateAllMeshes();
		AndroidTexture.invalidateAllTextures();
		ShaderProgram.invalidateAllShaderPrograms();
		FrameBuffer.invalidateAllFrameBuffers();		

		Display display = app.getWindowManager().getDefaultDisplay();
		this.width = display.getWidth();
		this.height = display.getHeight();		
		mean = new WindowedMean(5);
		this.lastFrameTime = System.nanoTime();

		gl.glViewport(0, 0, this.width, this.height);
		
		if( created == false ) {
			app.listener.create();
			created = true;
			synchronized(this) {
				running = true;
			}
		}
	}
	
	Object synch = new Object();	
	void resume () {
		synchronized(synch) {
			running = false;
			resume = true;
		}
	}
	
	void pause () {
		synchronized(synch) {
			running = false;	
			pause = true;
		}
		boolean cond = false;
		while(!cond) {
			synchronized(synch) {
				cond = !pause;
			}
		}
	}
	
	void destroy () {
		synchronized(synch) {
			running = false;
			destroy = true;
		}
		boolean cond = false;
		while(!cond) {
			synchronized(synch) {
				cond = !destroy;
			}
		}
	}
	
	@Override public void onDrawFrame (javax.microedition.khronos.opengles.GL10 gl) {
		long time = System.nanoTime();
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;
		mean.addValue(deltaTime);			

		synchronized (Gdx.input) {
			synchronized (synch) {
				if (running) {
					app.listener.render();
				}

				if (pause) {
					app.listener.pause();
					pause = false;
				}

				if (resume) {
					app.listener.resume();
					resume = false;
					running = true;
				}

				if (destroy) {
					app.listener.dispose();
					destroy = false;
				}
			}

			Gdx.input.processEvents(null);
		}
		
		if (time - frameStart > 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}
	
	

	/**
	 * {@inheritDoc}
	 */
	@Override public float getDeltaTime () {
		return mean.getMean() == 0 ? deltaTime : mean.getMean();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public GraphicsType getType () {
		return GraphicsType.AndroidGL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getFramesPerSecond () {
		return fps;
	}

	@Override public Texture newUnmanagedTexture (int width, int height, Format format, TextureFilter minFilter,
		TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		if (!isPowerOfTwo(width) || !isPowerOfTwo(height)) throw new GdxRuntimeException("Dimensions have to be a power of two");

		Bitmap.Config config = AndroidPixmap.getInternalFormat(format);
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Texture texture = null;
		texture = new AndroidTexture(this, bitmap, minFilter, magFilter, uWrap, vWrap, false, null);
		bitmap.recycle();
		return texture;
	}

	@Override public Texture newUnmanagedTexture (Pixmap pixmap, TextureFilter minFilter, TextureFilter magFilter,
		TextureWrap uWrap, TextureWrap vWrap) {

		if (!isPowerOfTwo(pixmap.getWidth()) || !isPowerOfTwo(pixmap.getHeight()))
			throw new GdxRuntimeException("Dimensions have to be a power of two");

		return new AndroidTexture(this, (Bitmap)pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, false, null);
	}

	@Override public Texture newTexture (FileHandle file, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap,
		TextureWrap vWrap) {
		return new AndroidTexture(this, (Bitmap)null, minFilter, magFilter, uWrap, vWrap, true, file);
	}

	public void clearManagedCaches () {
		Mesh.clearAllMeshes();
		AndroidTexture.clearAllTextures();
		ShaderProgram.clearAllShaderPrograms();
		FrameBuffer.clearAllFrameBuffers();		
	}
	
	View getView () {
		return view;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public GLCommon getGLCommon () {
		return gl;
	}

	@Override
	public Texture newTexture(ByteBuffer buffer, Format format, int width,
			int height, TextureFilter minFilter, TextureFilter magFilter,
			TextureWrap uWrap, TextureWrap vWrap) {
		throw new GdxRuntimeException("not implemented");
	}
}
