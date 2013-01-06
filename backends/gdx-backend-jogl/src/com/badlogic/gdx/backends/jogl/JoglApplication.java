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

package com.badlogic.gdx.backends.jogl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.nativewindow.util.Dimension;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.jogl.JoglGraphics.JoglDisplayMode;
import com.badlogic.gdx.backends.joal.OpenALAudio;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

/** An implemenation of the {@link Application} interface based on Jogl for Windows, Linux and Mac. Instantiate this class with
 * apropriate parameters and then register {@link ApplicationListener} or {@link InputProcessor} instances.
 * 
 * @author mzechner */
public class JoglApplication implements Application {
	JoglGraphics graphics;
	JoglInput input;
	protected JoglNet net;
	JoglFiles files;
	OpenALAudio audio;
	List<Runnable> runnables = new ArrayList<Runnable>();
	List<Runnable> executedRunnables = new ArrayList<Runnable>();
	int logLevel = LOG_INFO;

	/** Creates a new {@link JoglApplication} with the given title and dimensions. If useGL20IfAvailable is set the JoglApplication
	 * will try to create an OpenGL 2.0 context which can then be used via JoglApplication.getGraphics().getGL20(). To query
	 * whether enabling OpenGL 2.0 was successful use the JoglApplication.getGraphics().isGL20Available() method.
	 * 
	 * @param listener the ApplicationListener implementing the program logic
	 * @param title the title of the application
	 * @param width the width of the surface in pixels
	 * @param height the height of the surface in pixels
	 * @param useGL20IfAvailable wheter to use OpenGL 2.0 if it is available or not */
	public JoglApplication (final ApplicationListener listener, final String title, final int width, final int height,
		final boolean useGL20IfAvailable) {
		final JoglApplicationConfiguration config = new JoglApplicationConfiguration();
		config.title = title;
		config.width = width;
		config.height = height;
		config.useGL20 = useGL20IfAvailable;
        initialize(listener, config);
	}

	public JoglApplication (final ApplicationListener listener, final JoglApplicationConfiguration config) {
		initialize(listener, config);
	}

	void initialize (ApplicationListener listener, JoglApplicationConfiguration config) {
		JoglNativesLoader.load();
		graphics = new JoglGraphics(listener, config);
		input = new JoglInput(graphics.getCanvas());
		audio = new OpenALAudio(16, config.audioDeviceBufferCount, config.audioDeviceBufferSize);
		files = new JoglFiles();
        net = new JoglNet();
		Gdx.app = this;
		Gdx.graphics = getGraphics();
		Gdx.input = getInput();
		Gdx.audio = getAudio();
		Gdx.files = getFiles();
		Gdx.net = getNet();
		graphics.create();
		graphics.getCanvas().addWindowListener(windowListener);
		graphics.getCanvas().setTitle(config.title);
		graphics.getCanvas().setSize(config.width, config.height);
		graphics.getCanvas().setUndecorated(config.fullscreen);
		graphics.getCanvas().setFullscreen(config.fullscreen);
		graphics.getCanvas().setVisible(true);
	}

	WindowAdapter windowListener = new WindowAdapter() {
		public void windowDestroyed(WindowEvent e) {
            audio.dispose();
		}
	};
	
	/** {@inheritDoc} */
	@Override
	public Audio getAudio () {
		return audio;
	}

	/** {@inheritDoc} */
	@Override
	public Files getFiles () {
		return files;
	}

	/** {@inheritDoc} */
	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	/** {@inheritDoc} */
	@Override
	public Input getInput () {
		return input;
	}

	/** {@inheritDoc} */
	@Override
	public ApplicationType getType () {
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion () {
		return 0;
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return getJavaHeap();
	}

	/** @return the GLCanvas of the application. */
	public GLWindow getGLCanvas () {
		return graphics.canvas;
	}

	Map<String, Preferences> preferences = new HashMap<String, Preferences>();

	@Override
	public Preferences getPreferences (String name) {
		if (preferences.containsKey(name)) {
			return preferences.get(name);
		} else {
			Preferences prefs = new JoglPreferences(name);
			preferences.put(name, prefs);
			return prefs;
		}
	}

	@Override
	public Clipboard getClipboard () {
		return new JoglClipboard();
	}
	
	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
			Gdx.graphics.requestRendering();
		}
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void log (String tag, String message, Exception exception) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
		}
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
			exception.printStackTrace(System.err);
		}
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public void exit () {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				//FIXME maybe it is a bit brutal (and useless)
				graphics.canvas.destroy();
			}
		});
	}

	@Override
	public Net getNet() {
		return net;
	}
}
