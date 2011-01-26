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

package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.android.surfaceview.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.MathUtils;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import java.io.InputStream;

/**
 * An implementation of {@link Graphics} for Android.
 *
 * @author mzechner
 */
public final class AndroidGraphics implements Graphics, Renderer {
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

    volatile boolean created = false;
    volatile boolean running = false;
    volatile boolean pause = false;
    volatile boolean resume = false;
    volatile boolean destroy = false;

    private float ppiX = 0;
    private float ppiY = 0;
    private float ppcX = 0;
    private float ppcY = 0;

    public AndroidGraphics(AndroidApplication activity, boolean useGL2IfAvailable, ResolutionStrategy resolutionStrategy) {
        view = createGLSurfaceView(activity, useGL2IfAvailable, resolutionStrategy);
        this.app = activity;
    }

    private View createGLSurfaceView(Activity activity, boolean useGL2, ResolutionStrategy resolutionStrategy) {
        EGLConfigChooser configChooser = getEglConfigChooser();

        if (useGL2 && checkGL20()) {
            GLSurfaceView20 view = new GLSurfaceView20(activity, resolutionStrategy);
            if (configChooser != null) view.setEGLConfigChooser(configChooser);
            view.setRenderer(this);
            return view;
        } else {
            if (Integer.parseInt(android.os.Build.VERSION.SDK) <= 4) {
                GLSurfaceViewCupcake view = new GLSurfaceViewCupcake(activity, resolutionStrategy);
                if (configChooser != null) view.setEGLConfigChooser(configChooser);
                view.setRenderer(this);
                return view;
            } else {
                android.opengl.GLSurfaceView view = new DefaultGLSurfaceView(activity, resolutionStrategy);
                if (configChooser != null) view.setEGLConfigChooser(configChooser);
                view.setRenderer(this);
                return view;
            }
        }
    }

    private EGLConfigChooser getEglConfigChooser() {
        if (!Build.DEVICE.equalsIgnoreCase("GT-I7500"))
            return null;
        else
            return new android.opengl.GLSurfaceView.EGLConfigChooser() {

                public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

                    // Ensure that we get a 16bit depth-buffer. Otherwise, we'll fall
                    // back to Pixelflinger on some device (read: Samsung I7500)
                    int[] attributes = new int[]{EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE};
                    EGLConfig[] configs = new EGLConfig[1];
                    int[] result = new int[1];
                    egl.eglChooseConfig(display, attributes, configs, 1, result);
                    return configs[0];
                }
            };
    }

    private void updatePpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ppiX = metrics.xdpi;
        ppiY = metrics.ydpi;
        ppcX = metrics.xdpi / 2.54f;
        ppcY = metrics.ydpi / 2.54f;
    }

    private boolean checkGL20() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
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

    private static boolean isPowerOfTwo(int value) {
        return ((value != 0) && (value & (value - 1)) == 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pixmap newPixmap(int width, int height, Format format) {
        return new AndroidPixmap(width, height, format);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pixmap newPixmap(InputStream in) {
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        if (bitmap == null) throw new GdxRuntimeException("Couldn't load Pixmap from InputStream");
        return new AndroidPixmap(bitmap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pixmap newPixmap(FileHandle file) {
        return newPixmap(file.read());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pixmap newPixmap(Object nativePixmap) {
        return new AndroidPixmap((Bitmap) nativePixmap);
    }

    /**
     * This instantiates the GL10, GL11 and GL20 instances. Includes the check for certain devices that pretend to support GL11 but
     * fuck up vertex buffer objects. This includes the pixelflinger which segfaults when buffers are deleted as well as the
     * Motorola CLIQ and the Samsung Behold II.
     *
     * @param gl
     */
    private void setupGL(javax.microedition.khronos.opengles.GL10 gl) {
        if (gl10 != null || gl20 != null) return;

        if (view instanceof GLSurfaceView20) {
            gl20 = new AndroidGL20();
            this.gl = gl20;
        } else {
            gl10 = new AndroidGL10(gl);
            this.gl = gl10;
            if (gl instanceof javax.microedition.khronos.opengles.GL11) {
                String renderer = gl.glGetString(GL10.GL_RENDERER);
                if (!renderer.toLowerCase().contains("pixelflinger")
                        && !(android.os.Build.MODEL.equals("MB200") || android.os.Build.MODEL.equals("MB220") || android.os.Build.MODEL
                        .contains("Behold"))) {
                    gl11 = new AndroidGL11((javax.microedition.khronos.opengles.GL11) gl);
                    gl10 = gl11;
                }
            }
        }

        Gdx.gl = this.gl;
        Gdx.gl10 = gl10;
        Gdx.gl11 = gl11;
        Gdx.gl20 = gl20;

        Gdx.app.log("AndroidGraphics", "OGL renderer: " + gl.glGetString(GL10.GL_RENDERER));
        Gdx.app.log("AndroidGraphics", "OGL vendor: " + gl.glGetString(GL10.GL_VENDOR));
        Gdx.app.log("AndroidGraphics", "OGL version: " + gl.glGetString(GL10.GL_VERSION));
        Gdx.app.log("AndroidGraphics", "OGL extensions: " + gl.glGetString(GL10.GL_EXTENSIONS));
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        updatePpi();
        gl.glViewport(0, 0, this.width, this.height);
        app.listener.resize(width, height);
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, EGLConfig config) {
        setupGL(gl);
        logConfig(config);
        updatePpi();

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

        if (created == false) {
            app.listener.create();
            created = true;
            synchronized (this) {
                running = true;
            }
        }
    }

    private void logConfig(EGLConfig config) {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int r = getAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
        int g = getAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
        int b = getAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
        int a = getAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
        int d = getAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
        int s = getAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);

        Gdx.app.log("AndroidGraphics", "framebuffer: (" + r + ", " + g + ", " + b + ", " + a + ")");
        Gdx.app.log("AndroidGraphics", "depthbuffer: (" + d + ")");
        Gdx.app.log("AndroidGraphics", "stencilbuffer: (" + s + ")");
    }

    int[] value = new int[1];

    private int getAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attrib, int defValue) {
        if (egl.eglGetConfigAttrib(display, config, attrib, value)) {
            return value[0];
        }
        return defValue;
    }

    Object synch = new Object();

    void resume() {
        synchronized (synch) {
            running = true;
            resume = true;
        }
    }

    void pause() {
        synchronized (synch) {
            running = false;
            pause = true;
            while (pause) {
                try {
                    synch.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    void destroy() {
        synchronized (synch) {
            running = false;
            destroy = true;

            while (destroy) {
                try {
                    synch.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
        long time = System.nanoTime();
        deltaTime = (time - lastFrameTime) / 1000000000.0f;
        lastFrameTime = time;
        mean.addValue(deltaTime);

        boolean lrunning = false;
        boolean lpause = false;
        boolean ldestroy = false;
        boolean lresume = false;

        synchronized (synch) {
            lrunning = running;
            lpause = pause;
            ldestroy = destroy;
            lresume = resume;

            if (resume) {
                resume = false;
            }

            if (pause) {
                pause = false;
                synch.notifyAll();
            }

            if (destroy) {
                destroy = false;
                synch.notifyAll();
            }
        }

        if (lresume) {
            app.listener.resume();
            Gdx.app.log("AndroidGraphics", "resumed");
        }

        if (lrunning) {
            app.input.processEvents();
            app.listener.render();
        }

        if (lpause) {
            app.listener.pause();
            Gdx.app.log("AndroidGraphics", "paused");
        }

        if (ldestroy) {
            app.listener.dispose();
            Gdx.app.log("AndroidGraphics", "destroyed");
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
    @Override
    public float getDeltaTime() {
        return mean.getMean() == 0 ? deltaTime : mean.getMean();
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
    public Texture newUnmanagedTexture(int width, int height, Format format, TextureFilter minFilter,
                                       TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
        if (gl != gl20 && (!MathUtils.isPowerOfTwo(width) || !MathUtils.isPowerOfTwo(height)))
            throw new GdxRuntimeException("Dimensions have to be a power of two");

        Bitmap.Config config = AndroidPixmap.getInternalFormat(format);
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Texture texture = null;
        texture = new AndroidTexture(this, bitmap, minFilter, magFilter, uWrap, vWrap, false, null);
        bitmap.recycle();
        return texture;
    }

    @Override
    public Texture newUnmanagedTexture(Pixmap pixmap, TextureFilter minFilter, TextureFilter magFilter,
                                       TextureWrap uWrap, TextureWrap vWrap) {

        if (gl != gl20 && (!MathUtils.isPowerOfTwo(pixmap.getWidth()) || !MathUtils.isPowerOfTwo(pixmap.getHeight())))
            throw new GdxRuntimeException("Dimensions have to be a power of two");

        return new AndroidTexture(this, (Bitmap) pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, false, null);
    }

    @Override
    public Texture newTexture(FileHandle file, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap,
                              TextureWrap vWrap) {
        return new AndroidTexture(this, (Bitmap) null, minFilter, magFilter, uWrap, vWrap, true, file);
    }

    @Override
    public Texture newTexture(TextureData textureData, TextureFilter minFilter, TextureFilter magFilter,
                              TextureWrap uWrap, TextureWrap vWrap) {
        return new AndroidTexture(this, textureData, minFilter, magFilter, uWrap, vWrap);
    }

    public void clearManagedCaches() {
        Mesh.clearAllMeshes();
        AndroidTexture.clearAllTextures();
        ShaderProgram.clearAllShaderPrograms();
        FrameBuffer.clearAllFrameBuffers();
    }

    public View getView() {
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GLCommon getGLCommon() {
        return gl;
    }

    @Override
    public float getPpiX() {
        return ppiX;
    }

    @Override
    public float getPpiY() {
        return ppiY;
    }

    @Override
    public float getPpcX() {
        return ppcX;
    }

    @Override
    public float getPpcY() {
        return ppcY;
    }
}
