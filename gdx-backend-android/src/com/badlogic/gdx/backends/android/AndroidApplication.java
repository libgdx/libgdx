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


import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout.LayoutParams;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;

/**
 * An implementation of the {@link Application} interface for Android. Create an {@link Activity}
 * that derives from this class. In the {@link Activity.onCreate()} method call the {@link initialize()}
 * method specifying the configuration for the GLSurfaceView.
 *
 * @author mzechner
 */
public class AndroidApplication extends Activity implements Application {
    static {
        System.loadLibrary("gdx-" + Version.VERSION);
    }

    /**
     * the android graphics instance *
     */
    private AndroidGraphics graphics;

    /**
     * the input instance *
     */
    private AndroidInput input;

    /**
     * the audio instance *
     */
    private AndroidAudio audio;

    /**
     * the resources instance *
     */
    private AndroidFiles resources;

    /**
     * the DestroyListener *
     */
    private ApplicationListener listener;

    /**
     * This method has to be called in the {@link Activity.onCreate()}
     * method. It sets up all the things necessary to get input, render
     * via OpenGL and so on. If useGL20IfAvailable is set the
     * AndroidApplication will try to create an OpenGL ES 2.0 context
     * which can then be used via {@link AndroidApplication.getGraphics().getGL20()}. The
     * {@link GL10} and {@link GL11} interfaces should not be used when
     * OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was
     * successful use the {@link AndroidApplication.getGraphics().isGL20Available()}
     * method. Sleep time in touch event handler is 0, so no sleeping is performed.
     *
     * @param useGL2IfAvailable whether to use OpenGL ES 2.0 if its available.
     */
    public void initialize(boolean useGL2IfAvailable) {
        initialize(useGL2IfAvailable, 0, null);
    }

    /**
     * This method has to be called in the {@link Activity.onCreate()}
     * method. It sets up all the things necessary to get input, render
     * via OpenGL and so on. If useGL20IfAvailable is set the
     * AndroidApplication will try to create an OpenGL ES 2.0 context
     * which can then be used via {@link AndroidApplication.getGraphics().getGL20()}. The
     * {@link GL10} and {@link GL11} interfaces should not be used when
     * OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was
     * successful use the {@link AndroidApplication.getGraphics().isGL20Available()}
     * method. sleepTime specifies the number of milliseconds to sleep in the touch
     * event handler. This may be used on <= 1.6 Android devices. Note that it will not
     * solve the CPU usage problem of the event handler of the Android system. Things will
     * still slow down.
     *
     * @param useGL2IfAvailable whether to use OpenGL ES 2.0 if its available.
     * @param sleepTime         specifies the number of milliseconds to sleep in the touch event handler
     */
    public void initialize(boolean useGL2IfAvailable, int sleepTime) {
        this.initialize(useGL2IfAvailable, sleepTime, null);
    }

    /**
     * This method has to be called in the {@link Activity.onCreate()}
     * method. It sets up all the things necessary to get input, render
     * via OpenGL and so on. If useGL20IfAvailable is set the
     * AndroidApplication will try to create an OpenGL ES 2.0 context
     * which can then be used via {@link AndroidApplication.getGraphics().getGL20()}. The
     * {@link GL10} and {@link GL11} interfaces should not be used when
     * OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was
     * successful use the {@link AndroidApplication.getGraphics().isGL20Available()}
     * method. sleepTime specifies the number of milliseconds to sleep in the touch
     * event handler. This may be used on <= 1.6 Android devices. Note that it will not
     * solve the CPU usage problem of the event handler of the Android system. Things will
     * still slow down.
     *
     * @param useGL2IfAvailable whether to use OpenGL ES 2.0 if its available.
     * @param layoutParams      specifies the {@link LayoutParams} that will be applied to the ContentView in {@link AndroidGraphics}
     */
    public void initialize(boolean useGL2IfAvailable, LayoutParams layoutParams) {
        this.initialize(useGL2IfAvailable, 0, layoutParams);
    }

    /**
     * This method has to be called in the {@link Activity.onCreate()}
     * method. It sets up all the things necessary to get input, render
     * via OpenGL and so on. If useGL20IfAvailable is set the
     * AndroidApplication will try to create an OpenGL ES 2.0 context
     * which can then be used via {@link AndroidApplication.getGraphics().getGL20()}. The
     * {@link GL10} and {@link GL11} interfaces should not be used when
     * OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was
     * successful use the {@link AndroidApplication.getGraphics().isGL20Available()}
     * method. sleepTime specifies the number of milliseconds to sleep in the touch
     * event handler. This may be used on <= 1.6 Android devices. Note that it will not
     * solve the CPU usage problem of the event handler of the Android system. Things will
     * still slow down.
     *
     * @param useGL2IfAvailable whether to use OpenGL ES 2.0 if its available.
     * @param sleepTime         specifies the number of milliseconds to sleep in the touch event handler
     * @param layoutParams      specifies the {@link LayoutParams} that will be applied to the ContentView in {@link AndroidGraphics}
     */
    public void initialize(boolean useGL2IfAvailable, int sleepTime, LayoutParams layoutParams) {
        graphics = new AndroidGraphics(this, useGL2IfAvailable, layoutParams);
        input = new AndroidInput(this, graphics.view, sleepTime);
        graphics.setInput(input);
        audio = new AndroidAudio(this);
        resources = new AndroidFiles(this.getAssets());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing())
            graphics.disposeRenderListener();

        if (graphics.view != null)
            graphics.view.onPause();

        audio.pause();

        if (listener != null)
            listener.pause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listener != null)
            listener.resume(this);

        if (graphics.view != null)
            graphics.view.onResume();

        audio.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listener != null)
            listener.destroy(this);
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
        return resources;
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
    public void setApplicationListener(ApplicationListener listener) {
        this.listener = listener;
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
}
