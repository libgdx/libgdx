
package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Implementation of the {@link AndroidApplicationBase} that is based on the {@link Fragment} class. This class is similar in use
 * to the {@link AndroidApplication} class, which is based on an {@link Activity}.
 * 
 * @author Bartol Karuza (me@bartolkaruza.com) */
public class AndroidFragmentApplication extends Fragment implements AndroidApplicationBase {

	/** Callbacks interface for letting the fragment interact with the Activitiy, parent fragment or target fragment.
	 * 
	 * @author Bartol Karuza (me@bartolkaruza.com) */
	public interface Callbacks {
		void exit ();
	}

	static {
		GdxNativesLoader.load();
	}

	public static final int MINIMUM_SDK = 8;
	protected AndroidGraphics graphics;
	protected AndroidInput input;
	protected AndroidAudio audio;
	protected AndroidFiles files;
	protected AndroidNet net;
	protected ApplicationListener listener;
	public Handler handler;
	protected boolean firstResume = true;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	protected int logLevel = LOG_INFO;

	protected Callbacks callbacks;

	@Override
	public void onAttach (Activity activity) {
		if (activity instanceof Callbacks) {
			this.callbacks = (Callbacks)activity;
		} else if (getParentFragment() instanceof Callbacks) {
			this.callbacks = (Callbacks)getParentFragment();
		} else if (getTargetFragment() instanceof Callbacks) {
			this.callbacks = (Callbacks)getTargetFragment();
		} else {
			throw new RuntimeException(
				"Missing AndroidFragmentApplication.Callbacks. Please implement AndroidFragmentApplication.Callbacks on the parent activity, fragment or target fragment.");
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach () {
		super.onDetach();
		this.callbacks = null;
	}

	protected FrameLayout.LayoutParams createLayoutParams () {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
			android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER;
		return layoutParams;
	}

	protected void createWakeLock (boolean use) {
		if (use) {
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@TargetApi(19)
	@Override
	public void useImmersiveMode (boolean use) {
		if (!use || getVersion() < 19) return;

		View view = getApplicationWindow().getDecorView();

		try {
			Method m = View.class.getMethod("setSystemUiVisibility", int.class);
			int code = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			code ^= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			code ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			code ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
			code ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			code ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			m.invoke(view, code);
		} catch (Exception e) {
			log("AndroidApplication", "Can't set immersive mode", e);
		}
	}

	/** This method has to be called in the
	 * {@link Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} method. It sets up all
	 * the things necessary to get input, render via OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
	 * <p/>
	 * Note: you have to return the returned view from the
	 * {@link Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}!
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @return the GLSurfaceView of the application */
	public View initializeForView (ApplicationListener listener) {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		return initializeForView(listener, config);
	}

	/** This method has to be called in the
	 * {@link Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} method. It sets up all
	 * the things necessary to get input, render via OpenGL and so on. You can configure other aspects of the application with the
	 * rest of the fields in the {@link AndroidApplicationConfiguration} instance.
	 * <p/>
	 * Note: you have to return the returned view from
	 * {@link Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
	 *           etc.).
	 * @return the GLSurfaceView of the application */
	public View initializeForView (ApplicationListener listener, AndroidApplicationConfiguration config) {
		if (this.getVersion() < MINIMUM_SDK) {
			throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
		}
		graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
			: config.resolutionStrategy);
		input = AndroidInputFactory.newAndroidInput(this, getActivity(), graphics.view, config);
		audio = new AndroidAudio(getActivity(), config);
		files = new AndroidFiles(getResources().getAssets(), getActivity().getFilesDir().getAbsolutePath());
		net = new AndroidNet(this);
		this.listener = listener;
		this.handler = new Handler();

		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
		Gdx.net = this.getNet();
		createWakeLock(config.useWakelock);
		useImmersiveMode(config.useImmersiveMode);
		if (config.useImmersiveMode && getVersion() >= 19) {
			try {
				Class<?> vlistener = Class.forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
				Object o = vlistener.newInstance();
				Method method = vlistener.getDeclaredMethod("createListener", AndroidApplicationBase.class);
				method.invoke(o, this);
			} catch (Exception e) {
				log("AndroidApplication", "Failed to create AndroidVisibilityListener", e);
			}
		}
		return graphics.getView();
	}

	@Override
	public void onPause () {
		boolean isContinuous = graphics.isContinuousRendering();
		graphics.setContinuousRendering(true);
		graphics.pause();

		input.unregisterSensorListeners();

		int[] realId = input.realId;
		// erase pointer ids. this sucks donkeyballs...
		Arrays.fill(realId, -1);
		boolean[] touched = input.touched;
		// erase touched state. this also sucks donkeyballs...
		Arrays.fill(touched, false);

		graphics.setContinuousRendering(isContinuous);

		if (graphics != null && graphics.view != null) {
			if (graphics.view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)graphics.view).onPause();
			if (graphics.view instanceof android.opengl.GLSurfaceView) ((android.opengl.GLSurfaceView)graphics.view).onPause();
		}

		super.onPause();
	}

	@Override
	public void onResume () {
		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
		Gdx.net = this.getNet();

		((AndroidInput)getInput()).registerSensorListeners();

		if (graphics != null && graphics.view != null) {
			if (graphics.view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)graphics.view).onResume();
			if (graphics.view instanceof android.opengl.GLSurfaceView) ((android.opengl.GLSurfaceView)graphics.view).onResume();
		}

		if (!firstResume) {
			graphics.resume();
		} else
			firstResume = false;
		super.onResume();
	}

	@Override
	public void onDestroyView () {
		super.onDestroyView();
		graphics.clearManagedCaches();
		graphics.destroy();
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}

	@Override
	public Audio getAudio () {
		return audio;
	}

	@Override
	public Files getFiles () {
		return files;
	}

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Input getInput () {
		return input;
	}

	@Override
	public Net getNet () {
		return net;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.Android;
	}

	@Override
	public int getVersion () {
		return android.os.Build.VERSION.SDK_INT;
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return Debug.getNativeHeapAllocatedSize();
	}

	@Override
	public Preferences getPreferences (String name) {
		return new AndroidPreferences(getActivity().getSharedPreferences(name, Context.MODE_PRIVATE));
	}

	AndroidClipboard clipboard;

	@Override
	public Clipboard getClipboard () {
		if (clipboard == null) {
			clipboard = new AndroidClipboard(getActivity());
		}
		return clipboard;
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
			Gdx.graphics.requestRendering();
		}
	}

	@Override
	public void onConfigurationChanged (Configuration config) {
		super.onConfigurationChanged(config);
		boolean keyboardAvailable = false;
		if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) keyboardAvailable = true;
		input.keyboardAvailable = keyboardAvailable;
	}

	@Override
	public void exit () {
		handler.post(new Runnable() {
			@Override
			public void run () {
				callbacks.exit();
			}
		});
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			Log.d(tag, message);
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			Log.d(tag, message, exception);
		}
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) Log.i(tag, message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) Log.i(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) Log.e(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) Log.e(tag, message, exception);
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel () {
		return logLevel;
	}

	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}

	@Override
	public Context getContext () {
		return getActivity();
	}

	@Override
	public Array<Runnable> getRunnables () {
		return runnables;
	}

	@Override
	public Array<Runnable> getExecutedRunnables () {
		return executedRunnables;
	}

	@Override
	public void runOnUiThread (Runnable runnable) {
		getActivity().runOnUiThread(runnable);
	}

	@Override
	public Array<LifecycleListener> getLifecycleListeners () {
		return lifecycleListeners;
	}

	@Override
	public boolean isFragment () {
		return true;
	}

	@Override
	public Window getApplicationWindow () {
		return this.getActivity().getWindow();
	}

	@Override
	public Handler getHandler () {
		return this.handler;
	}
}
