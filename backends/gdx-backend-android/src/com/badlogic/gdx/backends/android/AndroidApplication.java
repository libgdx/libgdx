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
package com.badlogic.gdx.backends.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.GdxNativesLoader;

/**
 * An implementation of the {@link Application} interface for Android. Create an {@link Activity} that derives from this class. In
 * the {@link Activity#onCreate(Bundle)} method call the {@link #initialize(ApplicationListener, boolean)} method specifying the
 * configuration for the GLSurfaceView.
 *
 * @author mzechner
 */
public class AndroidApplication extends Activity implements Application {
    static {
   	 GdxNativesLoader.load();
    }
    
    protected AndroidGraphics graphics;
    protected AndroidInput input;
    protected AndroidAudio audio;
    protected AndroidFiles files;
    protected ApplicationListener listener;
    protected Handler handler;
    protected boolean firstResume = true;
    protected final List<Runnable> runnables = new ArrayList<Runnable>();
    protected WakeLock wakeLock = null;

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. If useGL20IfAvailable is set the AndroidApplication will try to create an OpenGL ES 2.0
     * context which can then be used via {@link Graphics#getGL20()}. The {@link GL10} and {@link GL11} interfaces should not be
     * used when OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was successful use the
     * {@link Graphics#isGL20Available()} method. Uses a default {@link AndroidApplicationConfiguration}.
     *
     * @param listener          the {@link ApplicationListener} implementing the program logic
     * @param useGL2IfAvailable whether to use OpenGL ES 2.0 if its available.
     */
    public void initialize(ApplicationListener listener, boolean useGL2IfAvailable) {
   	 AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
   	 config.useGL20 = useGL2IfAvailable;
       initialize(listener, config);
    }


    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. If config.useGL20 is set the AndroidApplication will try to create an OpenGL ES 2.0
     * context which can then be used via {@link Graphics#getGL20()}. The {@link GL10} and {@link GL11} interfaces should not be
     * used when OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was successful use the
     * {@link Graphics#isGL20Available()} method. You can configure other aspects of the application with the rest of the fields
     * in the {@link AndroidApplicationConfiguration} instance.
     *
     * @param listener          the {@link ApplicationListener} implementing the program logic
     * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer, etc.).
     */
    public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
   	 graphics = new AndroidGraphics(this, config.useGL20, config.resolutionStrategy==null?new FillResolutionStrategy():config.resolutionStrategy);
       input = new AndroidInput(this, graphics.view, config);
       audio = new AndroidAudio(this);
       files = new AndroidFiles(this.getAssets());
       this.listener = listener;
       this.handler = new Handler();

       Gdx.app = this;
       Gdx.input = this.getInput();
       Gdx.audio = this.getAudio();
       Gdx.files = this.getFiles();
       Gdx.graphics = this.getGraphics();

       requestWindowFeature(Window.FEATURE_NO_TITLE);
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
       setContentView(graphics.getView(), createLayoutParams());
       createWakeLock(config);
    }
    
    protected FrameLayout.LayoutParams createLayoutParams() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        return layoutParams;
    }

    protected void createWakeLock(AndroidApplicationConfiguration config) {
   	 if(config.useWakelock) {
	   	 PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
	       wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "libgdx wakelock");  
   	 }
    }
    
    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. If useGL20IfAvailable is set the AndroidApplication will try to create an OpenGL ES 2.0
     * context which can then be used via {@link Graphics#getGL20()}. The {@link GL10} and {@link GL11} interfaces should not be
     * used when OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was successful use the
     * {@link Graphics#isGL20Available()} method. Uses a default {@link AndroidApplicationConfiguration}.
     * <p/>
     * Note: you have to add the returned view to your layout!
     *
     * @param listener          the {@link ApplicationListener} implementing the program logic
     * @param useGL2IfAvailable whether to use OpenGL ES 2.0 if its available.
     * @return the GLSurfaceView of the application
     */
    public View initializeForView(ApplicationListener listener, boolean useGL2IfAvailable) {
   	 AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
   	 config.useGL20 = useGL2IfAvailable;
       return initializeForView(listener, config);
    }
    
    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. If config.useGL20 is set the AndroidApplication will try to create an OpenGL ES 2.0
     * context which can then be used via {@link Graphics#getGL20()}. The {@link GL10} and {@link GL11} interfaces should not be
     * used when OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was successful use the
     * {@link Graphics#isGL20Available()} method. You can configure other aspects of the application with the rest of the fields
     * in the {@link AndroidApplicationConfiguration} instance.
     * <p/>
     * Note: you have to add the returned view to your layout!
     *
     * @param listener          the {@link ApplicationListener} implementing the program logic
     * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer, etc.).
     * @return the GLSurfaceView of the application
     */
    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config) {
   	 graphics = new AndroidGraphics(this, config.useGL20, config.resolutionStrategy==null?new FillResolutionStrategy():config.resolutionStrategy);
       input = new AndroidInput(this, graphics.view, config);
       audio = new AndroidAudio(this);
       files = new AndroidFiles(this.getAssets());
       this.listener = listener;
       this.handler = new Handler();

       Gdx.app = this;
       Gdx.input = this.getInput();
       Gdx.audio = this.getAudio();
       Gdx.files = this.getFiles();
       Gdx.graphics = this.getGraphics();

       createWakeLock(config);
       return graphics.getView();
    }

    @Override
    protected void onPause() {
   	 if(wakeLock != null) wakeLock.release();
        graphics.pause();
        
        if (audio != null) audio.pause();
        
        input.unregisterSensorListeners();
        
        if (isFinishing()) {      	
           graphics.clearManagedCaches();
           graphics.destroy();           
        }
        
        if (graphics != null && graphics.view != null) {
            if (graphics.view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake) graphics.view).onPause();
            if (graphics.view instanceof android.opengl.GLSurfaceView)
                ((android.opengl.GLSurfaceView) graphics.view).onPause();
        }        
        
        super.onPause();
    }

    @Override
    protected void onResume() {
   	 if(wakeLock != null) wakeLock.acquire();   	 
        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();

        ((AndroidInput)getInput()).registerSensorListeners();
        if (audio != null) audio.resume();
        
        if (graphics != null && graphics.view != null) {
            if (graphics.view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake) graphics.view).onResume();
            if (graphics.view instanceof android.opengl.GLSurfaceView)
                ((android.opengl.GLSurfaceView) graphics.view).onResume();
        }
        
        if (!firstResume)
            graphics.resume();
        else
            firstResume = false;
        super.onResume();
    }

    @Override
    protected void onDestroy() {        
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Audio getAudio() {
        return audio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Files getFiles() {
        return files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Input getInput() {
        return input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(String tag, String message) {
        Log.d(tag, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationType getType() {
        return ApplicationType.Android;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion() {
   	 return Integer.parseInt(android.os.Build.VERSION.SDK);        
    }

    @Override
    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize();
    }

	@Override public Preferences getPreferences (String name) {
		return new AndroidPreferences(getSharedPreferences(name, Context.MODE_PRIVATE));
	}

	@Override public void postRunnable (Runnable runnable) {
		synchronized(runnables) {
			runnables.add(runnable);
		}
	}
	
	@Override public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
		boolean keyboardAvailable = false;
		if(config.keyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) keyboardAvailable = true;
		input.keyboardAvailable = keyboardAvailable;		
	}

	@Override public void log (String tag, String message, Exception exception) {
		Log.d(tag, message, exception);
	}
}
