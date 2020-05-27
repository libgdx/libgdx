package com.badlogic.gdx.backends.android.textureview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;
import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class GLTextureView extends TextureView
        implements TextureView.SurfaceTextureListener, View.OnLayoutChangeListener {

    private final static String TAG = GLTextureView.class.getSimpleName();

    private final static boolean LOG_ATTACH_DETACH = false;
    private final static boolean LOG_THREADS = false;
    private final static boolean LOG_PAUSE_RESUME = false;
    private final static boolean LOG_SURFACE = false;
    private final static boolean LOG_RENDERER = false;
    private final static boolean LOG_RENDERER_DRAW_FRAME = false;
    private final static boolean LOG_EGL = false;

    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     */
    public GLTextureView(Context context, ResolutionStrategy resolutionStrategy, int targetGLESVersion) {
        super(context);
        this.resolutionStrategy = resolutionStrategy;
        eglContextClientVersion = targetGLESVersion;
        init(false, 16, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ResolutionStrategy.MeasuredDimension measures = resolutionStrategy.calcMeasures(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measures.width, measures.height);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {

        // add this line, the IME can show the selectable words when use chinese input method editor.
        if (outAttrs != null) {
            outAttrs.imeOptions = outAttrs.imeOptions | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        }

        return new BaseInputConnection(GLTextureView.this, false) {
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                int sdkVersion = android.os.Build.VERSION.SDK_INT;
                if (sdkVersion >= 16) {
                    /*
                     * In Jelly Bean, they don't send key events for delete. Instead, they send beforeLength = 1, afterLength = 0. So,
                     * we'll just simulate what it used to do.
                     */
                    if (beforeLength == 1 && afterLength == 0) {
                        sendDownUpKeyEventForBackwardCompatibility(KeyEvent.KEYCODE_DEL);
                        return true;
                    }
                }
                return super.deleteSurroundingText(beforeLength, afterLength);
            }

            @TargetApi(16)
            private void sendDownUpKeyEventForBackwardCompatibility(final int code) {
                final long eventTime = SystemClock.uptimeMillis();
                super.sendKeyEvent(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, code, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
                super.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime, KeyEvent.ACTION_UP, code, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (glThread != null) {
                // GLThread may still be running if this view was never
                // attached to a window.
                glThread.requestExitAndWait();
            }
        } finally {
            super.finalize();
        }
    }

    private void init(boolean translucent, int depth, int stencil) {
        setSurfaceTextureListener(this);
        this.setEGLContextFactory(new GLTextureView.DefaultContextFactory());
        this.setEGLConfigChooser(translucent ?
                new GdxEglConfigChooser(8, 8, 8, 8, depth, stencil, 0) :
                new GdxEglConfigChooser(5, 6, 5, 0, depth, stencil, 0));
        this.setSurfaceTextureListener(this);

        if (translucent) {
            setOpaque(false);
        }
    }

    /**
     * Control whether the EGL context is preserved when the GLTextureView is paused and
     * resumed.
     * <p>
     * If set to true, then the EGL context may be preserved when the GLTextureView is paused.
     * Whether the EGL context is actually preserved or not depends upon whether the
     * Android device that the program is running on can support an arbitrary number of EGL
     * contexts or not. Devices that can only support a limited number of EGL contexts must
     * release the  EGL context in order to allow multiple applications to share the GPU.
     * <p>
     * If set to false, the EGL context will be released when the GLTextureView is paused,
     * and recreated when the GLTextureView is resumed.
     * <p>
     * <p>
     * The default is false.
     *
     * @param preserveOnPause preserve the EGL context when paused
     */
    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        preserveEGLContextOnPause = preserveOnPause;
    }

    /**
     * @return true if the EGL context will be preserved when paused
     */
    public boolean getPreserveEGLContextOnPause() {
        return preserveEGLContextOnPause;
    }

    /**
     * Set the renderer associated with this view. Also starts the thread that
     * will call the renderer, which in turn causes the rendering to start.
     * <p>This method should be called once and only once in the life-cycle of
     * a GLTextureView.
     * <p>The following GLTextureView methods can only be called <em>before</em>
     * setRenderer is called:
     * <ul>
     * <li>{@link #setEGLConfigChooser(boolean)}
     * <li>{@link #setEGLConfigChooser(GLSurfaceView.EGLConfigChooser)}
     * <li>{@link #setEGLConfigChooser(int, int, int, int, int, int)}
     * </ul>
     * <p>
     * The following GLTextureView methods can only be called <em>after</em>
     * setRenderer is called:
     * <ul>
     * <li>{@link #getRenderMode()}
     * <li>{@link #onPause()}
     * <li>{@link #onResume()}
     * <li>{@link #queueEvent(Runnable)}
     * <li>{@link #requestRender()}
     * <li>{@link #setRenderMode(int)}
     * </ul>
     *
     * @param renderer the renderer to use to perform OpenGL drawing.
     */
    public void setRenderer(GLSurfaceView.Renderer renderer) {
        checkRenderThreadState();
        if (eglConfigChooser == null) {
            eglConfigChooser = new SimpleEGLConfigChooser(true);
        }
        if (eglContextFactory == null) {
            eglContextFactory = new DefaultContextFactory();
        }
        if (eglWindowSurfaceFactory == null) {
            eglWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        this.renderer = renderer;
        glThread = new GLThread(mThisWeakRef);
        glThread.start();
    }

    /**
     * Install a custom EGLContextFactory.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(GLSurfaceView.Renderer)}
     * is called.
     * <p>
     * If this method is not called, then by default
     * a context will be created with no shared context and
     * with a null attribute list.
     */
    public void setEGLContextFactory(GLSurfaceView.EGLContextFactory factory) {
        checkRenderThreadState();
        eglContextFactory = factory;
    }

    /**
     * Install a custom EGLWindowSurfaceFactory.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(GLSurfaceView.Renderer)}
     * is called.
     * <p>
     * If this method is not called, then by default
     * a window surface will be created with a null attribute list.
     */
    public void setEGLWindowSurfaceFactory(GLSurfaceView.EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        eglWindowSurfaceFactory = factory;
    }

    /**
     * Install a custom EGLConfigChooser.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(GLSurfaceView.Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an EGLConfig that is compatible with the current
     * android.view.Surface, with a depth buffer depth of
     * at least 16 bits.
     */
    public void setEGLConfigChooser(GLSurfaceView.EGLConfigChooser configChooser) {
        checkRenderThreadState();
        eglConfigChooser = configChooser;
    }

    /**
     * Install a config chooser which will choose a config
     * as close to 16-bit RGB as possible, with or without an optional depth
     * buffer as close to 16-bits as possible.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(GLSurfaceView.Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_888 surface with a depth buffer depth of
     * at least 16 bits.
     */
    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
    }

    /**
     * Install a config chooser which will choose a config
     * with at least the specified depthSize and stencilSize,
     * and exactly the specified redSize, greenSize, blueSize and alphaSize.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(GLSurfaceView.Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_888 surface with a depth buffer depth of
     * at least 16 bits.
     */
    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize,
                                    int depthSize, int stencilSize) {
        setEGLConfigChooser(
                new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
    }

    /**
     * Inform the default EGLContextFactory and default EGLConfigChooser
     * which EGLContext client version to pick.
     * <p>Use this method to create an OpenGL ES 2.0-compatible context.
     * Example:
     * <pre class="prettyprint">
     * public MyView(Context context) {
     * super(context);
     * setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
     * setRenderer(new MyRenderer());
     * }
     * </pre>
     * <p>Note: Activities which require OpenGL ES 2.0 should indicate this by
     * setting @lt;uses-feature android:glEsVersion="0x00020000" /> in the activity's
     * AndroidManifest.xml file.
     * <p>If this method is called, it must be called before {@link #setRenderer(GLSurfaceView.Renderer)}
     * is called.
     * <p>This method only affects the behavior of the default EGLContexFactory and the
     * default EGLConfigChooser. If
     * {@link #setEGLContextFactory(GLSurfaceView.EGLContextFactory)} has been called, then the supplied
     * EGLContextFactory is responsible for creating an OpenGL ES 2.0-compatible context.
     * If
     * {@link #setEGLConfigChooser(GLSurfaceView.EGLConfigChooser)} has been called, then the supplied
     * EGLConfigChooser is responsible for choosing an OpenGL ES 2.0-compatible config.
     *
     * @param version The EGLContext client version to choose. Use 2 for OpenGL ES 2.0
     */
    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        eglContextClientVersion = version;
    }

    /**
     * Set the rendering mode. When renderMode is
     * RENDERMODE_CONTINUOUSLY, the renderer is called
     * repeatedly to re-render the scene. When renderMode
     * is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface
     * is created, or when {@link #requestRender} is called. Defaults to RENDERMODE_CONTINUOUSLY.
     * <p>
     * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance
     * by allowing the GPU and CPU to idle when the view does not need to be updated.
     * <p>
     * This method can only be called after {@link #setRenderer(GLSurfaceView.Renderer)}
     *
     * @param renderMode one of the RENDERMODE_X constants
     * @see GLSurfaceView#RENDERMODE_CONTINUOUSLY
     * @see GLSurfaceView#RENDERMODE_WHEN_DIRTY
     */
    public void setRenderMode(int renderMode) {
        glThread.setRenderMode(renderMode);
    }

    /**
     * Get the current rendering mode. May be called
     * from any thread. Must not be called before a renderer has been set.
     *
     * @return the current rendering mode.
     * @see GLSurfaceView#RENDERMODE_CONTINUOUSLY
     * @see GLSurfaceView#RENDERMODE_WHEN_DIRTY
     */
    public int getRenderMode() {
        return glThread.getRenderMode();
    }

    /**
     * Request that the renderer render a frame.
     * This method is typically used when the render mode has been set to
     * {@link GLSurfaceView#RENDERMODE_WHEN_DIRTY}, so that frames are only rendered on demand.
     * May be called
     * from any thread. Must not be called before a renderer has been set.
     */
    public void requestRender() {
        glThread.requestRender();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLTextureView.
     */
    public void surfaceCreated(SurfaceTexture texture) {
        glThread.surfaceCreated();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLTextureView.
     */
    public void surfaceDestroyed(SurfaceTexture texture) {
        // Surface will be destroyed when we return
        glThread.surfaceDestroyed();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLTextureView.
     */
    public void surfaceChanged(SurfaceTexture texture, int format, int w, int h) {
        glThread.onWindowResize(w, h);
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    public void onPause() {
        glThread.onPause();
    }

    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    public void onResume() {
        glThread.onResume();
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     *
     * @param r the runnable to be run on the GL rendering thread.
     */
    public void queueEvent(Runnable r) {
        glThread.queueEvent(r);
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of GLTextureView.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =" + detached);
        }
        if (detached && (renderer != null)) {
            int renderMode = RENDERMODE_CONTINUOUSLY;
            if (glThread != null) {
                renderMode = glThread.getRenderMode();
            }
            glThread = new GLThread(mThisWeakRef);
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                glThread.setRenderMode(renderMode);
            }
            glThread.start();
        }
        detached = false;
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of GLTextureView.
     * Must not be called before a renderer has been set.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow");
        }
        if (glThread != null) {
            glThread.requestExitAndWait();
        }
        detached = true;
        super.onDetachedFromWindow();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                               int oldTop, int oldRight, int oldBottom) {
        surfaceChanged(getSurfaceTexture(), 0, right - left, bottom - top);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceCreated(surface);
        surfaceChanged(surface, 0, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        surfaceChanged(surface, 0, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        surfaceDestroyed(surface);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (getRenderMode() == RENDERMODE_CONTINUOUSLY) {
            requestRender();
        }
    }

    // ----------------------------------------------------------------------

    protected class DefaultContextFactory implements GLSurfaceView.EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = {
                    EGL_CONTEXT_CLIENT_VERSION, eglContextClientVersion, EGL10.EGL_NONE
            };

            EGLContext context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
                    eglContextClientVersion != 0 ? attrib_list : null);
            boolean success = checkEglError("After eglCreateContext " + eglContextClientVersion, egl);

            if ((!success || context == null) && eglContextClientVersion > 2) {
                Log.w(TAG, "Falling back to GLES 2");
                eglContextClientVersion = 2;
                return createContext(egl, display, config);
            }
            Log.w(TAG, "Returning a GLES " + eglContextClientVersion + " context");
            return context;
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                if (LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().getId());
                }
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
            }
        }
    }

    private static class DefaultWindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {

        @Override
        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config,
                                              Object nativeWindow) {
            EGLSurface result = null;
            try {
                result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
                Log.e(TAG, "eglCreateWindowSurface", e);
            }
            return result;
        }

        @Override
        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    private abstract class BaseConfigChooser implements GLSurfaceView.EGLConfigChooser {
        public BaseConfigChooser(int[] configSpec) {
            mConfigSpec = filterConfigSpec(configSpec);
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs);

        protected int[] mConfigSpec;

        private int[] filterConfigSpec(int[] configSpec) {
            if (eglContextClientVersion != 2) {
                return configSpec;
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
            newConfigSpec[len] = 0x0004; /* EGL_OPENGL_ES2_BIT */
            newConfigSpec[len + 1] = EGL10.EGL_NONE;
            return newConfigSpec;
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
    private class ComponentSizeChooser extends BaseConfigChooser {
        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize,
                                    int depthSize, int stencilSize) {
            super(new int[]{
                    EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE,
                    blueSize, EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize, EGL10.EGL_NONE
            });
            value = new int[1];
            this.redSize = redSize;
            this.greenSize = greenSize;
            this.blueSize = blueSize;
            this.alphaSize = alphaSize;
            this.depthSize = depthSize;
            this.stencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
                if ((d >= depthSize) && (s >= stencilSize)) {
                    int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
                    if ((r == redSize) && (g == greenSize) && (b == blueSize) && (a == alphaSize)) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute,
                                     int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                return value[0];
            }
            return defaultValue;
        }

        private int[] value;
        // Subclasses can adjust these values:
        protected int redSize;
        protected int greenSize;
        protected int blueSize;
        protected int alphaSize;
        protected int depthSize;
        protected int stencilSize;
    }

    /**
     * This class will choose a RGB_888 surface with
     * or without a depth buffer.
     */
    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0);
        }
    }

    /**
     * An EGL helper class.
     */

    private static class EglHelper {
        public EglHelper(WeakReference<GLTextureView> glTextureViewWeakReference) {
            this.glTextureViewWeakRef = glTextureViewWeakReference;
        }

        /**
         * Initialize EGL for a given configuration spec.
         */
        public void start() {
            if (LOG_EGL) {
                Log.w("EglHelper", "start() tid=" + Thread.currentThread().getId());
            }
            /*
             * Get an EGL instance
             */
            egl = (EGL10) EGLContext.getEGL();

            /*
             * Get to the default display.
             */
            eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }

            /*
             * We can now initialize EGL for that display
             */
            int[] version = new int[2];
            if (!egl.eglInitialize(eglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed");
            }
            GLTextureView view = glTextureViewWeakRef.get();
            if (view == null) {
                eglConfig = null;
                eglContext = null;
            } else {
                eglConfig = view.eglConfigChooser.chooseConfig(egl, eglDisplay);

                /*
                 * Create an EGL context. We want to do this as rarely as we can, because an
                 * EGL context is a somewhat heavy object.
                 */
                eglContext = view.eglContextFactory.createContext(egl, eglDisplay, eglConfig);
            }
            if (eglContext == null || eglContext == EGL10.EGL_NO_CONTEXT) {
                eglContext = null;
                throwEglException("createContext");
            }
            if (LOG_EGL) {
                Log.w("EglHelper",
                        "createContext " + eglContext + " tid=" + Thread.currentThread().getId());
            }

            eglSurface = null;
        }

        /**
         * Create an egl surface for the current SurfaceHolder surface. If a surface
         * already exists, destroy it before creating the new surface.
         *
         * @return true if the surface was created successfully.
         */
        public boolean createSurface() {
            if (LOG_EGL) {
                Log.w("EglHelper", "createSurface()  tid=" + Thread.currentThread().getId());
            }
            /*
             * Check preconditions.
             */
            if (egl == null) {
                throw new RuntimeException("egl not initialized");
            }
            if (eglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            }
            if (eglConfig == null) {
                throw new RuntimeException("eglConfig not initialized");
            }

            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */
            destroySurfaceImp();

            /*
             * Create an EGL surface we can render into.
             */
            GLTextureView view = glTextureViewWeakRef.get();
            if (view != null) {
                eglSurface = view.eglWindowSurfaceFactory.createWindowSurface(egl, eglDisplay, eglConfig,
                        view.getSurfaceTexture());
            } else {
                eglSurface = null;
            }

            if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
                int error = egl.eglGetError();
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                }
                return false;
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */
            if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                /*
                 * Could not make the context current, probably because the underlying
                 * TextureView surface has been destroyed.
                 */
                logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", egl.eglGetError());
                return false;
            }

            return true;
        }

        /**
         * Create a GL object for the current EGL context.
         */
        private GL createGL() {
            GL gl = eglContext.getGL();
            return gl;
        }

        /**
         * Display the current render surface.
         *
         * @return the EGL error code from eglSwapBuffers.
         */
        public int swap() {
            if (!egl.eglSwapBuffers(eglDisplay, eglSurface)) {
                return egl.eglGetError();
            }
            return EGL10.EGL_SUCCESS;
        }

        public void destroySurface() {
            if (LOG_EGL) {
                Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().getId());
            }
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            if (eglSurface != null && eglSurface != EGL10.EGL_NO_SURFACE) {
                egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                GLTextureView view = glTextureViewWeakRef.get();
                if (view != null) {
                    view.eglWindowSurfaceFactory.destroySurface(egl, eglDisplay, eglSurface);
                }
                eglSurface = null;
            }
        }

        public void finish() {
            if (LOG_EGL) {
                Log.w("EglHelper", "finish() tid=" + Thread.currentThread().getId());
            }
            if (eglContext != null) {
                GLTextureView view = glTextureViewWeakRef.get();
                if (view != null) {
                    view.eglContextFactory.destroyContext(egl, eglDisplay, eglContext);
                }
                eglContext = null;
            }
            if (eglDisplay != null) {
                egl.eglTerminate(eglDisplay);
                eglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throwEglException(function, egl.eglGetError());
        }

        public static void throwEglException(String function, int error) {
            String message = formatEglError(function, error);
            if (LOG_THREADS) {
                Log.e("EglHelper",
                        "throwEglException tid=" + Thread.currentThread().getId() + " " + message);
            }
            throw new RuntimeException(message);
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
            Log.w(tag, formatEglError(function, error));
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + error;
        }

        private WeakReference<GLTextureView> glTextureViewWeakRef;
        EGL10 egl;
        EGLDisplay eglDisplay;
        EGLSurface eglSurface;
        EGLConfig eglConfig;
        EGLContext eglContext;
    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
     * to a Renderer instance to do the actual drawing. Can be configured to
     * render continuously or on request.
     * <p>
     * All potentially blocking synchronization is done through the
     * glThreadManager object. This avoids multiple-lock ordering issues.
     */
    static class GLThread extends Thread {
        GLThread(WeakReference<GLTextureView> glTextureViewWeakRef) {
            super();
            width = 0;
            height = 0;
            requestRender = true;
            renderMode = RENDERMODE_CONTINUOUSLY;
            this.glTextureViewWeakRef = glTextureViewWeakRef;
        }

        @Override
        public void run() {
            setName("GLThread " + getId());
            if (LOG_THREADS) {
                Log.i("GLThread", "starting tid=" + getId());
            }

            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                glThreadManager.threadExiting(this);
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(glThreadManager) block.
         */
        private void stopEglSurfaceLocked() {
            if (haveEglSurface) {
                haveEglSurface = false;
                eglHelper.destroySurface();
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(glThreadManager) block.
         */
        private void stopEglContextLocked() {
            if (haveEglContext) {
                eglHelper.finish();
                haveEglContext = false;
                glThreadManager.releaseEglContextLocked(this);
            }
        }

        private void guardedRun() throws InterruptedException {
            eglHelper = new EglHelper(glTextureViewWeakRef);
            haveEglContext = false;
            haveEglSurface = false;
            try {
                GL10 gl = null;
                boolean createEglContext = false;
                boolean createEglSurface = false;
                boolean createGlInterface = false;
                boolean lostEglContext = false;
                boolean sizeChanged = false;
                boolean wantRenderNotification = false;
                boolean doRenderNotification = false;
                boolean askedToReleaseEglContext = false;
                int w = 0;
                int h = 0;
                Runnable event = null;

                while (true) {
                    synchronized (glThreadManager) {
                        while (true) {
                            if (shouldExit) {
                                return;
                            }

                            if (!eventQueue.isEmpty()) {
                                event = eventQueue.remove(0);
                                break;
                            }

                            // Update the pause state.
                            boolean pausing = false;
                            if (paused != requestPaused) {
                                pausing = requestPaused;
                                paused = requestPaused;
                                glThreadManager.notifyAll();
                                if (LOG_PAUSE_RESUME) {
                                    Log.i("GLThread", "paused is now " + paused + " tid=" + getId());
                                }
                            }

                            // Do we need to give up the EGL context?
                            if (shouldReleaseEglContext) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "releasing EGL context because asked to tid=" + getId());
                                }
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                shouldReleaseEglContext = false;
                                askedToReleaseEglContext = true;
                            }

                            // Have we lost the EGL context?
                            if (lostEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                lostEglContext = false;
                            }

                            // When pausing, release the EGL surface:
                            if (pausing && haveEglSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "releasing EGL surface because paused tid=" + getId());
                                }
                                stopEglSurfaceLocked();
                            }

                            // When pausing, optionally release the EGL Context:
                            if (pausing && haveEglContext) {
                                GLTextureView view = glTextureViewWeakRef.get();
                                boolean preserveEglContextOnPause =
                                        view == null ? false : view.preserveEGLContextOnPause;
                                if (!preserveEglContextOnPause
                                        || glThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                    stopEglContextLocked();
                                    if (LOG_SURFACE) {
                                        Log.i("GLThread", "releasing EGL context because paused tid=" + getId());
                                    }
                                }
                            }

                            // When pausing, optionally terminate EGL:
                            if (pausing) {
                                if (glThreadManager.shouldTerminateEGLWhenPausing()) {
                                    eglHelper.finish();
                                    if (LOG_SURFACE) {
                                        Log.i("GLThread", "terminating EGL because paused tid=" + getId());
                                    }
                                }
                            }

                            // Have we lost the TextureView surface?
                            if ((!hasSurface) && (!waitingForSurface)) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "noticed textureView surface lost tid=" + getId());
                                }
                                if (haveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                waitingForSurface = true;
                                surfaceIsBad = false;
                                glThreadManager.notifyAll();
                            }

                            // Have we acquired the surface view surface?
                            if (hasSurface && waitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "noticed textureView surface acquired tid=" + getId());
                                }
                                waitingForSurface = false;
                                glThreadManager.notifyAll();
                            }

                            if (doRenderNotification) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "sending render notification tid=" + getId());
                                }
                                wantRenderNotification = false;
                                doRenderNotification = false;
                                renderComplete = true;
                                glThreadManager.notifyAll();
                            }

                            // Ready to draw?
                            if (readyToDraw()) {

                                // If we don't have an EGL context, try to acquire one.
                                if (!haveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false;
                                    } else if (glThreadManager.tryAcquireEglContextLocked(this)) {
                                        try {
                                            eglHelper.start();
                                        } catch (RuntimeException t) {
                                            glThreadManager.releaseEglContextLocked(this);
                                            throw t;
                                        }
                                        haveEglContext = true;
                                        createEglContext = true;

                                        glThreadManager.notifyAll();
                                    }
                                }

                                if (haveEglContext && !haveEglSurface) {
                                    haveEglSurface = true;
                                    createEglSurface = true;
                                    createGlInterface = true;
                                    sizeChanged = true;
                                }

                                if (haveEglSurface) {
                                    if (this.sizeChanged) {
                                        sizeChanged = true;
                                        w = width;
                                        h = height;
                                        wantRenderNotification = true;
                                        if (LOG_SURFACE) {
                                            Log.i("GLThread", "noticing that we want render notification tid=" + getId());
                                        }

                                        // Destroy and recreate the EGL surface.
                                        createEglSurface = true;

                                        this.sizeChanged = false;
                                    }
                                    requestRender = false;
                                    glThreadManager.notifyAll();
                                    break;
                                }
                            }

                            // By design, this is the only place in a GLThread thread where we wait().
                            if (LOG_THREADS) {
                                Log.i("GLThread", "waiting tid=" + getId() + " haveEglContext: " + haveEglContext
                                        + " haveEglSurface: " + haveEglSurface + " paused: " + paused + " hasSurface: "
                                        + hasSurface + " surfaceIsBad: " + surfaceIsBad + " waitingForSurface: "
                                        + waitingForSurface + " width: " + width + " height: " + height
                                        + " requestRender: " + requestRender + " renderMode: " + renderMode);
                            }
                            glThreadManager.wait();
                        }
                    } // end of synchronized(glThreadManager)

                    if (event != null) {
                        event.run();
                        event = null;
                        continue;
                    }

                    if (createEglSurface) {
                        if (LOG_SURFACE) {
                            Log.w("GLThread", "egl createSurface");
                        }
                        if (!eglHelper.createSurface()) {
                            synchronized (glThreadManager) {
                                surfaceIsBad = true;
                                glThreadManager.notifyAll();
                            }
                            continue;
                        }
                        createEglSurface = false;
                    }

                    if (createGlInterface) {
                        gl = (GL10) eglHelper.createGL();

                        glThreadManager.checkGLDriver(gl);
                        createGlInterface = false;
                    }

                    if (createEglContext) {
                        if (LOG_RENDERER) {
                            Log.w("GLThread", "onSurfaceCreated");
                        }
                        GLTextureView view = glTextureViewWeakRef.get();
                        if (view != null) {
                            view.renderer.onSurfaceCreated(gl, eglHelper.eglConfig);
                        }
                        createEglContext = false;
                    }

                    if (sizeChanged) {
                        if (LOG_RENDERER) {
                            Log.w("GLThread", "onSurfaceChanged(" + w + ", " + h + ")");
                        }
                        GLTextureView view = glTextureViewWeakRef.get();
                        if (view != null) {
                            view.renderer.onSurfaceChanged(gl, w, h);
                        }
                        sizeChanged = false;
                    }

                    if (LOG_RENDERER_DRAW_FRAME) {
                        Log.w("GLThread", "onDrawFrame tid=" + getId());
                    }
                    {
                        GLTextureView view = glTextureViewWeakRef.get();
                        if (view != null) {
                            view.renderer.onDrawFrame(gl);
                        }
                    }
                    int swapError = eglHelper.swap();
                    switch (swapError) {
                        case EGL10.EGL_SUCCESS:
                            break;
                        case EGL11.EGL_CONTEXT_LOST:
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "egl context lost tid=" + getId());
                            }
                            lostEglContext = true;
                            break;
                        default:
                            // Other errors typically mean that the current surface is bad,
                            // probably because the TextureView surface has been destroyed,
                            // but we haven't been notified yet.
                            // Log the error to help developers understand why rendering stopped.
                            EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", swapError);

                            synchronized (glThreadManager) {
                                surfaceIsBad = true;
                                glThreadManager.notifyAll();
                            }
                            break;
                    }

                    if (wantRenderNotification) {
                        doRenderNotification = true;
                    }
                }
            } finally {
                /*
                 * clean-up everything...
                 */
                synchronized (glThreadManager) {
                    stopEglSurfaceLocked();
                    stopEglContextLocked();
                }
            }
        }

        public boolean ableToDraw() {
            return haveEglContext && haveEglSurface && readyToDraw();
        }

        private boolean readyToDraw() {
            return (!paused) && hasSurface && (!surfaceIsBad) && (width > 0) && (height > 0) && (
                    requestRender || (renderMode == RENDERMODE_CONTINUOUSLY));
        }

        public void setRenderMode(int renderMode) {
            if (!((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY))) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized (glThreadManager) {
                this.renderMode = renderMode;
                glThreadManager.notifyAll();
            }
        }

        public int getRenderMode() {
            synchronized (glThreadManager) {
                return renderMode;
            }
        }

        public void requestRender() {
            synchronized (glThreadManager) {
                requestRender = true;
                glThreadManager.notifyAll();
            }
        }

        public void surfaceCreated() {
            synchronized (glThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated tid=" + getId());
                }
                hasSurface = true;
                glThreadManager.notifyAll();
                while ((waitingForSurface) && (!exited)) {
                    try {
                        glThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized (glThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed tid=" + getId());
                }
                hasSurface = false;
                glThreadManager.notifyAll();
                while ((!waitingForSurface) && (!exited)) {
                    try {
                        glThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (glThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onPause tid=" + getId());
                }
                requestPaused = true;
                glThreadManager.notifyAll();
                while ((!exited) && (!paused)) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onPause waiting for paused.");
                    }
                    try {
                        glThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onResume() {
            synchronized (glThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onResume tid=" + getId());
                }
                requestPaused = false;
                requestRender = true;
                renderComplete = false;
                glThreadManager.notifyAll();
                while ((!exited) && paused && (!renderComplete)) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onResume waiting for !paused.");
                    }
                    try {
                        glThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (glThreadManager) {
                width = w;
                height = h;
                sizeChanged = true;
                requestRender = getRenderMode() == RENDERMODE_CONTINUOUSLY;
                renderComplete = false;
                glThreadManager.notifyAll();

                // Wait for thread to react to resize and render a frame
                while (!exited && !paused && !renderComplete && ableToDraw()) {
                    if (LOG_SURFACE) {
                        Log.i("Main thread", "onWindowResize waiting for render complete from tid=" + getId());
                    }
                    try {
                        glThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            synchronized (glThreadManager) {
                shouldExit = true;
                glThreadManager.notifyAll();
                while (!exited) {
                    try {
                        glThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            shouldReleaseEglContext = true;
            glThreadManager.notifyAll();
        }

        /**
         * Queue an "event" to be run on the GL rendering thread.
         *
         * @param r the runnable to be run on the GL rendering thread.
         */
        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized (glThreadManager) {
                eventQueue.add(r);
                glThreadManager.notifyAll();
            }
        }

        // Once the thread is started, all accesses to the following member
        // variables are protected by the glThreadManager monitor
        private boolean shouldExit;
        private boolean exited;
        private boolean requestPaused;
        private boolean paused;
        private boolean hasSurface;
        private boolean surfaceIsBad;
        private boolean waitingForSurface;
        private boolean haveEglContext;
        private boolean haveEglSurface;
        private boolean shouldReleaseEglContext;
        private int width;
        private int height;
        private int renderMode;
        private boolean requestRender;
        private boolean renderComplete;
        private ArrayList<Runnable> eventQueue = new ArrayList<>();
        private boolean sizeChanged = true;

        // End of member variables protected by the glThreadManager monitor.

        private EglHelper eglHelper;

        /**
         * Set once at thread construction time, nulled out when the parent view is garbage
         * called. This weak reference allows the GLTextureView to be garbage collected while
         * the GLThread is still alive.
         */
        private WeakReference<GLTextureView> glTextureViewWeakRef;
    }

    static boolean checkEglError(String prompt, EGL10 egl) {
        int error;
        boolean result = true;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            result = false;
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
        return result;
    }

    private void checkRenderThreadState() {
        if (glThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }

    private static class GLThreadManager {
        private static String TAG = "GLThreadManager";

        public synchronized void threadExiting(GLThread thread) {
            if (LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" + thread.getId());
            }
            thread.exited = true;
            if (eglOwner == thread) {
                eglOwner = null;
            }
            notifyAll();
        }

        /*
         * Tries once to acquire the right to use an EGL
         * context. Does not block. Requires that we are already
         * in the glThreadManager monitor when this is called.
         *
         * @return true if the right to use an EGL context was acquired.
         */
        public boolean tryAcquireEglContextLocked(GLThread thread) {
            if (eglOwner == thread || eglOwner == null) {
                eglOwner = thread;
                notifyAll();
                return true;
            }
            checkGLESVersion();
            if (multipleGLESContextsAllowed) {
                return true;
            }
            // Notify the owning thread that it should release the context.
            // TODO: implement a fairness policy. Currently
            // if the owning thread is drawing continuously it will just
            // reacquire the EGL context.
            if (eglOwner != null) {
                eglOwner.requestReleaseEglContextLocked();
            }
            return false;
        }

        /*
         * Releases the EGL context. Requires that we are already in the
         * glThreadManager monitor when this is called.
         */
        public void releaseEglContextLocked(GLThread thread) {
            if (eglOwner == thread) {
                eglOwner = null;
            }
            notifyAll();
        }

        public synchronized boolean shouldReleaseEGLContextWhenPausing() {
            // Release the EGL context when pausing even if
            // the hardware supports multiple EGL contexts.
            // Otherwise the device could run out of EGL contexts.
            return limitedGLESContexts;
        }

        public synchronized boolean shouldTerminateEGLWhenPausing() {
            checkGLESVersion();
            return !multipleGLESContextsAllowed;
        }

        public synchronized void checkGLDriver(GL10 gl) {
            if (!glesDriverCheckComplete) {
                checkGLESVersion();
                String renderer = gl.glGetString(GL10.GL_RENDERER);
                if (glesVersion < kGLES_20) {
                    multipleGLESContextsAllowed = !renderer.startsWith(kMSM7K_RENDERER_PREFIX);
                    notifyAll();
                }
                limitedGLESContexts = !multipleGLESContextsAllowed;
                if (LOG_SURFACE) {
                    Log.w(TAG, "checkGLDriver renderer = \"" + renderer + "\" multipleContextsAllowed = "
                            + multipleGLESContextsAllowed + " limitedGLESContexts = " + limitedGLESContexts);
                }
                glesDriverCheckComplete = true;
            }
        }

        private void checkGLESVersion() {
            if (!glesVersionCheckComplete) {
                glesVersionCheckComplete = true;
            }
        }

        /**
         * This check was required for some pre-Android-3.0 hardware. Android 3.0 provides
         * support for hardware-accelerated views, therefore multiple EGL contexts are
         * supported on all Android 3.0+ EGL drivers.
         */
        private boolean glesVersionCheckComplete;
        private int glesVersion;
        private boolean glesDriverCheckComplete;
        private boolean multipleGLESContextsAllowed;
        private boolean limitedGLESContexts;
        private static final int kGLES_20 = 0x20000;
        private static final String kMSM7K_RENDERER_PREFIX = "Q3Dimension MSM7500 ";
        private GLThread eglOwner;
    }

    private static final GLThreadManager glThreadManager = new GLThreadManager();

    private final WeakReference<GLTextureView> mThisWeakRef = new WeakReference<>(this);
    private GLThread glThread;
    private GLSurfaceView.Renderer renderer;
    private boolean detached;
    private GLSurfaceView.EGLConfigChooser eglConfigChooser;
    private GLSurfaceView.EGLContextFactory eglContextFactory;
    private GLSurfaceView.EGLWindowSurfaceFactory eglWindowSurfaceFactory;
    private int eglContextClientVersion;
    private boolean preserveEGLContextOnPause;
    private final ResolutionStrategy resolutionStrategy;
}
