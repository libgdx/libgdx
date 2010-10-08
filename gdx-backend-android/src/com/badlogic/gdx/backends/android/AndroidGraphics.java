/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx.backends.android;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.FrameBuffer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.ShaderProgram;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.WindowedMean;

/**
 * An implementation of {@link Graphics} for Android.
 *
 * @author mzechner
 */
final class AndroidGraphics implements Graphics, Renderer {
	/**
	 * the gl surfaceview *
	 */
	protected final GLSurfaceView view;

	/**
	 * the android input we have to call *
	 */
	private AndroidInput input;

	/**
	 * the render listener *
	 */
	protected RenderListener listener;

	/**
	 * width & height of the surface *
	 */
	protected int width;
	protected int height;

	/**
	 * the app *
	 */
	protected AndroidApplication app;

	/**
	 * the GL10 instance *
	 */
	protected GL10 gl10;

	/**
	 * the GL11 instance *
	 */
	protected GL11 gl11;

	/**
	 * the GL20 instance *
	 */
	protected GL20 gl20;

	/**
	 * the last frame time *
	 */
	private long lastFrameTime = System.nanoTime();

	/**
	 * the deltaTime *
	 */
	private float deltaTime = 0;

	/**
	 * frame start time *
	 */
	private long frameStart = System.nanoTime();

	/**
	 * frame counter *
	 */
	private int frames = 0;

	/**
	 * last fps *
	 */
	private int fps;

	/**
	 * the deltaTime mean *
	 */
	private WindowedMean mean = new WindowedMean(5);

	/**
	 * whether to dispose the render listeners *
	 */
	private boolean dispose = false;

	public AndroidGraphics(AndroidApplication activity, boolean useGL2IfAvailable, LayoutParams layoutParams) {
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (useGL2IfAvailable) {
			if( checkGL20( activity ) )
				view = new GLSurfaceView20( activity );
			else
				view = new android.opengl.GLSurfaceView(activity);
		} else {
			view = new android.opengl.GLSurfaceView(activity);
		}

		view.setRenderer(this);

		if (layoutParams != null)
			activity.setContentView(view, layoutParams);
		else
			activity.setContentView(view);

		this.app = activity;
	}

	/**
	 * This is a hack...
	 *
	 * @param input
	 */
	protected void setInput(AndroidInput input) {
		this.input = input;
	}

	private boolean checkGL20(Activity context) {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		int[] version = new int[2];
		egl.eglInitialize(display, version);

		int EGL_OPENGL_ES2_BIT = 4;
		int[] configAttribs =
		{
				EGL10.EGL_RED_SIZE, 4,
				EGL10.EGL_GREEN_SIZE, 4,
				EGL10.EGL_BLUE_SIZE, 4,
				EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
				EGL10.EGL_NONE
		};

		EGLConfig[] configs = new EGLConfig[10];
		int[] num_config = new int[1];
		egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
		egl.eglTerminate(display);
		return num_config[0] > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GL10 getGL10() {
		return gl10;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GL11 getGL11() {
		return gl11;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GL20 getGL20() {
		return gl20;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGL11Available() {
		return gl11 != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGL20Available() {
		return gl20 != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Font newFont(String fontName, int size, FontStyle style ) {
		return new AndroidFont(this, fontName, size, style );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Font newFont(FileHandle file, int size, FontStyle style) {
		AndroidFileHandle aHandle = (AndroidFileHandle) file;
		AndroidFont font = new AndroidFont(this, aHandle.getAssetManager(), aHandle.getFileName(), size, style);
		return font;
	}

	private static boolean isPowerOfTwo( int value )
	{
		return ((value!=0) && (value&(value-1))==0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pixmap newPixmap(int width, int height, Format format) {
		if( !isPowerOfTwo( width ) || !isPowerOfTwo( height ) )
			throw new GdxRuntimeException( "Dimensions have to be a power of two" );

		return new AndroidPixmap(width, height, format);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pixmap newPixmap(InputStream in) {
		Bitmap bitmap = BitmapFactory.decodeStream(in, null, null);

		if( bitmap == null )
			throw new GdxRuntimeException( "Couldn't load Pixmap from InputStream" );

		if( !isPowerOfTwo( bitmap.getWidth() ) || !isPowerOfTwo( bitmap.getHeight() ) )
			throw new GdxRuntimeException( "Dimensions have to be a power of two" );

		return new AndroidPixmap(bitmap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pixmap newPixmap(FileHandle file) {
		AndroidFileHandle aHandle = (AndroidFileHandle) file;
		Bitmap bitmap = null;

		if (aHandle.isAsset()) {
			InputStream in;
			try {
				in = aHandle.getAssetManager().open(aHandle.getFileName());
				bitmap = BitmapFactory.decodeStream(in, null, null);
				in.close();
			} catch (IOException e) {
				throw new GdxRuntimeException( "Couldn't open Pixmap from file '" + file + "'", e );
			}
		} else {
			bitmap = BitmapFactory.decodeFile(aHandle.getFileName());
		}

		if( bitmap == null )
			throw new GdxRuntimeException( "Couldn't open Pixmap from file '" + file + "'" );

		if( !isPowerOfTwo( bitmap.getWidth() ) || !isPowerOfTwo( bitmap.getHeight() ) )
			throw new GdxRuntimeException( "Dimensions have to be a power of two" );

		return new AndroidPixmap(bitmap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pixmap newPixmap(Object nativePixmap) {
		return new AndroidPixmap((Bitmap) nativePixmap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRenderListener(RenderListener listener) {
		synchronized (this) {
			if (this.listener != null)
				this.listener.dispose();
			this.listener = listener;
		}
	}

	@Override
	public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
		// calculate delta time
		deltaTime = (System.nanoTime() - lastFrameTime) / 1000000000.0f;
		lastFrameTime = System.nanoTime();
		mean.addValue(deltaTime);

		// this is a hack so the events get processed synchronously.
		if (input != null)
			input.update();

		synchronized (this) {
			if (listener != null)
				listener.render();
		}

		if (dispose) {
			if (listener != null)
				listener.dispose();
			listener = null;
			dispose = false;
		}


		if (System.nanoTime() - frameStart > 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = System.nanoTime();
		}
		frames++;
	}

	/**
	 * This instantiates the GL10, GL11 and GL20 instances.
	 * Includes the check for certain devices that pretend
	 * to support GL11 but fuck up vertex buffer objects. This
	 * includes the pixelflinger which segfaults when buffers
	 * are deleted as well as the Motorola CLIQ and the Samsung
	 * Behold II.
	 *
	 * @param gl
	 */
	private void setupGL(javax.microedition.khronos.opengles.GL10 gl) {
		if (gl10 != null || gl20 != null)
			return;

		if( view instanceof GLSurfaceView20 )
			gl20 = new AndroidGL20();
		else
		{
			gl10 = new AndroidGL10(gl);
			if (gl instanceof javax.microedition.khronos.opengles.GL11) {
				String renderer = gl.glGetString(GL10.GL_RENDERER);
				if (renderer.toLowerCase().contains("pixelflinger"))
					return;

				if (android.os.Build.MODEL.equals("MB200") || android.os.Build.MODEL.equals("MB220") || android.os.Build.MODEL.contains("Behold"))
					return;
				gl11 = new AndroidGL11((javax.microedition.khronos.opengles.GL11) gl);
				gl10 = gl11;
			}
		}
	}

	@Override
	public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;

		if (listener != null)
			listener.surfaceChanged(width, height);
	}

	@Override
	public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, EGLConfig config) {
		setupGL(gl);

		Mesh.invalidateAllMeshes();
		AndroidTexture.invalidateAllTextures();
		ShaderProgram.invalidateAllShaderPrograms();
		FrameBuffer.invalidateAllFrameBuffers();
		Font.invalidateAllFonts();

		Display display = app.getWindowManager().getDefaultDisplay();
		this.width = display.getWidth();
		this.height = display.getHeight();

		if (listener != null)
			listener.surfaceCreated();
		mean = new WindowedMean(5);
		this.lastFrameTime = System.nanoTime();

		gl.glViewport( 0, 0, this.width, this.height );
	}

	/**
	 * {@inheritDoc}
	 */
	 @Override
	 public float getDeltaTime() {
		 return mean.getMean() == 0 ? deltaTime : mean.getMean();
	 }

	 public void disposeRenderListener() {
		 dispose = true;
		 while (dispose) {
			 try {
				 Thread.sleep(20);
			 } catch (InterruptedException e) {
			 }
		 }
	 }

	 /**
	  * {@inheritDoc}
	  */
	 @Override
	 public GraphicsType getType() {
		 return GraphicsType.AndroidGL;
	 }

	 /**
	  * {@inheritDoc}
	  */
	 @Override
	 public int getFramesPerSecond() {
		 return fps;
	 }

	 @Override
	 public Texture newUnmanagedTexture(int width, int height, Format format,
			 TextureFilter minFilter, TextureFilter magFilter,
			 TextureWrap uWrap, TextureWrap vWrap) 
	 {
		 if( !isPowerOfTwo( width ) || !isPowerOfTwo( height ) )
			 throw new GdxRuntimeException( "Dimensions have to be a power of two" );

		 Bitmap.Config config = AndroidPixmap.getInternalFormat(format);
		 Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		 Texture texture = null;
		 if (gl10 != null)
			 texture = new AndroidTexture(this, gl10, bitmap, minFilter, magFilter, uWrap, vWrap, false, null );
		 else
			 texture = new AndroidTexture(this, gl20, bitmap, minFilter, magFilter, uWrap, vWrap, false, null );

		 bitmap.recycle();
		 return texture;
	 }

	 @Override
	 public Texture newUnmanagedTexture(Pixmap pixmap, TextureFilter minFilter,
			 TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {

		 if( !isPowerOfTwo( pixmap.getWidth() ) || !isPowerOfTwo( pixmap.getHeight() ) )
			 throw new GdxRuntimeException( "Dimensions have to be a power of two" );

		 if (gl10 != null)
			 return new AndroidTexture(this, gl10, (Bitmap) pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, false, null);
		 else
			 return new AndroidTexture(this, gl20, (Bitmap) pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, false, null);
	 }

	 @Override
	 public Texture newTexture(FileHandle file, TextureFilter minFilter,
			 TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		 if (gl10 != null)
			 return new AndroidTexture(this, gl10, (Bitmap) null, minFilter, magFilter, uWrap, vWrap, true, (AndroidFileHandle)file);
		 else
			 return new AndroidTexture(this, gl20, (Bitmap) null, minFilter, magFilter, uWrap, vWrap, true, (AndroidFileHandle)file);	}

	 public void clearManagedCaches() 
	 {
		 Mesh.clearAllMeshes();
		 AndroidTexture.clearAllTextures();
		 ShaderProgram.clearAllShaderPrograms();
		 FrameBuffer.clearAllFrameBuffers();
		 Font.clearAllFonts();
	 }

}
