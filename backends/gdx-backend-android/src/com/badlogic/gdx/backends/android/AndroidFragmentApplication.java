
package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.fragment.app.Fragment;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.utils.*;

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

	protected AndroidGraphics graphics;
	protected AndroidInput input;
	protected AndroidAudio audio;
	protected AndroidFiles files;
	protected AndroidNet net;
	protected AndroidClipboard clipboard;
	protected ApplicationListener listener;
	public Handler handler;
	protected boolean firstResume = true;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(LifecycleListener.class);
	private final Array<AndroidEventListener> androidEventListeners = new Array<AndroidEventListener>();
	protected int logLevel = LOG_INFO;
	protected ApplicationLogger applicationLogger;

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
		if (!use || getVersion() < Build.VERSION_CODES.KITKAT) return;

		View view = this.graphics.getView();

		int code = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		view.setSystemUiVisibility(code);
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
		setApplicationLogger(new AndroidApplicationLogger());
		graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
			: config.resolutionStrategy);
		input = createInput(this, getActivity(), graphics.view, config);
		audio = createAudio(getActivity(), config);
		files = new AndroidFiles(getResources().getAssets(), getActivity().getFilesDir().getAbsolutePath());
		net = new AndroidNet(this, config);
		this.listener = listener;
		this.handler = new Handler();
		this.clipboard = new AndroidClipboard(getActivity());

		// Add a specialized audio lifecycle listener
		addLifecycleListener(new LifecycleListener() {

			@Override
			public void resume () {
				audio.resume();
			}

			@Override
			public void pause () {
				audio.pause();
			}

			@Override
			public void dispose () {
				audio.dispose();
			}
		});

		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
		Gdx.net = this.getNet();
		createWakeLock(config.useWakelock);
		useImmersiveMode(config.useImmersiveMode);
		if (config.useImmersiveMode && getVersion() >= Build.VERSION_CODES.KITKAT) {
			AndroidVisibilityListener vlistener = new AndroidVisibilityListener();
			vlistener.createListener(this);
		}

		// detect an already connected bluetooth keyboardAvailable
		if (getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
			input.setKeyboardAvailable(true);
		return graphics.getView();
	}

	@Override
	public void onPause () {
		boolean isContinuous = graphics.isContinuousRendering();
		boolean isContinuousEnforced = AndroidGraphics.enforceContinuousRendering;

		// from here we don't want non continuous rendering
		AndroidGraphics.enforceContinuousRendering = true;
		graphics.setContinuousRendering(true);
		// calls to setContinuousRendering(false) from other thread (ex: GLThread)
		// will be ignored at this point...
		graphics.pause();

		input.onPause();

		// davebaol & mobidevelop:
		// This fragment (or one of the parent)  is currently being removed from its activity or the activity is in the process of finishing
		if (isRemoving() || isAnyParentFragmentRemoving() || getActivity().isFinishing()) {
			graphics.clearManagedCaches();
			graphics.destroy();
		}

		AndroidGraphics.enforceContinuousRendering = isContinuousEnforced;
		graphics.setContinuousRendering(isContinuous);

		graphics.onPauseGLSurfaceView();

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

		input.onResume();

		if (graphics != null) {
			graphics.onResumeGLSurfaceView();
		}

		if (!firstResume) {
			graphics.resume();
		} else
			firstResume = false;
		super.onResume();
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
	public AndroidInput getInput () {
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

	@Override
	public Clipboard getClipboard () {
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
		input.setKeyboardAvailable(keyboardAvailable);
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
	public void setApplicationLogger (ApplicationLogger applicationLogger) {
		this.applicationLogger = applicationLogger;
	}

	@Override
	public ApplicationLogger getApplicationLogger () {
		return applicationLogger;
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
	public SnapshotArray<LifecycleListener> getLifecycleListeners () {
		return lifecycleListeners;
	}

	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// forward events to our listeners if there are any installed
		synchronized (androidEventListeners) {
			for (int i = 0; i < androidEventListeners.size; i++) {
				androidEventListeners.get(i).onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	/** Adds an event listener for Android specific event such as onActivityResult(...). */
	public void addAndroidEventListener (AndroidEventListener listener) {
		synchronized (androidEventListeners) {
			androidEventListeners.add(listener);
		}
	}

	/** Removes an event listener for Android specific event such as onActivityResult(...). */
	public void removeAndroidEventListener (AndroidEventListener listener) {
		synchronized (androidEventListeners) {
			androidEventListeners.removeValue(listener, true);
		}
	}

	@Override
	public Window getApplicationWindow () {
		return this.getActivity().getWindow();
	}

	@Override
	public Handler getHandler () {
		return this.handler;
	}

	@Override
	public AndroidAudio createAudio (Context context, AndroidApplicationConfiguration config) {
		return new AndroidAudioImpl(context, config);
	}

	@Override
	public AndroidInput createInput (Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
		return new AndroidInputImpl(this, getActivity(), graphics.view, config);
	}

	@Override
	public WindowManager getWindowManager () {
		return (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
	}

	/**
	* Iterates over nested fragments hierarchy and returns true if one of the fragment is in the removal process
	*
	* @return true - one of the parent fragments is being removed
	*/
	private boolean isAnyParentFragmentRemoving() {
		Fragment fragment = getParentFragment();

		 while (fragment != null) {
			if (fragment.isRemoving())
				return true;

			fragment = fragment.getParentFragment();
		}

		return false;
	}
}
