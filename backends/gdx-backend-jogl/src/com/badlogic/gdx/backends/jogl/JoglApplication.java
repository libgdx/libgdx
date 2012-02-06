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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GLCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.jogl.JoglGraphics.JoglDisplayMode;
import com.badlogic.gdx.backends.openal.OpenALAudio;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An implemenation of the {@link Application} interface based on Jogl for Windows, Linux and Mac. Instantiate this class with
 * apropriate parameters and then register {@link ApplicationListener} or {@link InputProcessor} instances.
 * 
 * @author mzechner */
public final class JoglApplication implements Application {
	JoglGraphics graphics;
	JoglInput input;
	JoglFiles files;
	OpenALAudio audio;
	JFrame frame;
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

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run () {
						initialize(listener, config);
					}
				});
			} catch (Exception e) {
				throw new GdxRuntimeException("Creating window failed", e);
			}
		} else {
			config.useGL20 = useGL20IfAvailable;
			initialize(listener, config);
		}
	}

	public JoglApplication (final ApplicationListener listener, final JoglApplicationConfiguration config) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run () {
						initialize(listener, config);
					}
				});
			} catch (Exception e) {
				throw new GdxRuntimeException("Creating window failed", e);
			}
		} else {
			initialize(listener, config);
		}
	}

	void initialize (ApplicationListener listener, JoglApplicationConfiguration config) {
		JoglNativesLoader.load();
		graphics = new JoglGraphics(listener, config);
		input = new JoglInput(graphics.getCanvas());
		audio = new OpenALAudio();
		files = new JoglFiles();

		Gdx.app = JoglApplication.this;
		Gdx.graphics = JoglApplication.this.getGraphics();
		Gdx.input = JoglApplication.this.getInput();
		Gdx.audio = JoglApplication.this.getAudio();
		Gdx.files = JoglApplication.this.getFiles();

		if (!config.fullscreen) {
			frame = new JFrame(config.title);
			graphics.getCanvas().setPreferredSize(new Dimension(config.width, config.height));
			frame.setSize(config.width + frame.getInsets().left + frame.getInsets().right, frame.getInsets().top
				+ frame.getInsets().bottom + config.height);
			frame.add(graphics.getCanvas(), BorderLayout.CENTER);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.addWindowListener(windowListener);

			frame.pack();
			frame.setVisible(true);
			graphics.create();
		} else {
			GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice device = genv.getDefaultScreenDevice();
			frame = new JFrame(config.title);
			graphics.getCanvas().setPreferredSize(new Dimension(config.width, config.height));
			frame.setSize(config.width + frame.getInsets().left + frame.getInsets().right, frame.getInsets().top
				+ frame.getInsets().bottom + config.height);
			frame.add(graphics.getCanvas(), BorderLayout.CENTER);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.addWindowListener(windowListener);
			frame.setUndecorated(true);
			frame.setResizable(false);
			frame.pack();
			frame.setVisible(true);
			java.awt.DisplayMode desktopMode = device.getDisplayMode();
			try {
				device.setFullScreenWindow(frame);
				JoglDisplayMode mode = graphics.findBestMatch(config.width, config.height);
				if (mode == null)
					throw new GdxRuntimeException("Couldn't set fullscreen mode " + config.width + "x" + config.height);
				device.setDisplayMode(mode.mode);
			} catch (Throwable e) {
				e.printStackTrace();
				device.setDisplayMode(desktopMode);
				device.setFullScreenWindow(null);
				frame.dispose();
				audio.dispose();
				System.exit(-1);
			}
			graphics.create();
		}
	}

	final WindowAdapter windowListener = new WindowAdapter() {
		@Override
		public void windowOpened (WindowEvent arg0) {
			graphics.getCanvas().requestFocus();
			graphics.getCanvas().requestFocusInWindow();
		}

		@Override
		public void windowIconified (WindowEvent arg0) {
		}

		@Override
		public void windowDeiconified (WindowEvent arg0) {
		}

		@Override
		public void windowClosing (WindowEvent arg0) {
			graphics.pause();
			graphics.destroy();
			audio.dispose();
			frame.remove(graphics.getCanvas());
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

	/** @return the JFrame of the application. */
	public JFrame getJFrame () {
		return frame;
	}

	/** @return the GLCanvas of the application. */
	public GLCanvas getGLCanvas () {
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
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
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
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
	}
}
