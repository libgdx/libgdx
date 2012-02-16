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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.openal.OpenALAudio;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Implements the {@link Graphics} interface with Jogl.
 * 
 * @author mzechner */
public class JoglGraphics extends JoglGraphicsBase implements GLEventListener {
	ApplicationListener listener = null;
	boolean useGL2;
	boolean created = false;
	boolean exclusiveMode = false;
	final JoglDisplayMode desktopMode;
	final JoglApplicationConfiguration config;
	String extensions;

	public JoglGraphics (ApplicationListener listener, JoglApplicationConfiguration config) {
		initialize(config);
		if (listener == null) throw new GdxRuntimeException("RenderListener must not be null");
		this.listener = listener;
		this.config = config;

		desktopMode = (JoglDisplayMode)JoglApplicationConfiguration.getDesktopDisplayMode();
	}

	public void create () {
		super.create();
	}

	public void pause () {
		super.pause();
		canvas.getContext().makeCurrent();
		listener.pause();
	}

	public void resume () {
		canvas.getContext().makeCurrent();
		listener.resume();
		super.resume();
	}

	@Override
	public void init (GLAutoDrawable drawable) {
		initializeGLInstances(drawable);
		setVSync(config.vSyncEnabled);

		if (!created) {
			listener.create();
			synchronized (this) {
				paused = false;
			}
			created = true;
		}
	}

	@Override
	public void reshape (GLAutoDrawable drawable, int x, int y, int width, int height) {
		listener.resize(width, height);
		requestRendering();
	}

	@Override
	public void display (GLAutoDrawable arg0) {
		synchronized (this) {
			if (!paused) {
				updateTimes();
				synchronized (((JoglApplication)Gdx.app).runnables) {
					JoglApplication app = ((JoglApplication)Gdx.app);
					app.executedRunnables.clear();
					app.executedRunnables.addAll(app.runnables);
					app.runnables.clear();
					
					for (int i = 0; i < app.executedRunnables.size(); i++) {
						try {
							app.executedRunnables.get(i).run();
						}
						catch(Throwable t) {
							t.printStackTrace();
						}
					}
				}
				((JoglInput)((JoglApplication)Gdx.app).getInput()).processEvents();
				listener.render();
				((OpenALAudio)Gdx.audio).update();
			}
		}
	}

	@Override
	public void displayChanged (GLAutoDrawable arg0, boolean arg1, boolean arg2) {

	}

	public void destroy () {
		canvas.getContext().makeCurrent();
		listener.dispose();
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = genv.getDefaultScreenDevice();
		device.setFullScreenWindow(null);
	}

	@Override
	public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpcX () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override
	public float getPpcY () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override
	public float getDensity () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 160f);
	}

	@Override
	public boolean supportsDisplayModeChange () {
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = genv.getDefaultScreenDevice();
		return device.isFullScreenSupported() && (Gdx.app instanceof JoglApplication);
	}

	protected static class JoglDisplayMode extends DisplayMode {
		final java.awt.DisplayMode mode;

		protected JoglDisplayMode (int width, int height, int refreshRate, int bitsPerPixel, java.awt.DisplayMode mode) {
			super(width, height, refreshRate, bitsPerPixel);
			this.mode = mode;
		}
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		return JoglApplicationConfiguration.getDisplayModes();
	}

	@Override
	public void setTitle (String title) {
		Container parent = canvas.getParent();
		while (parent != null) {
			if (parent instanceof JFrame) {
				((JFrame)parent).setTitle(title);
				return;
			}
			parent = parent.getParent();
		}
	}

	@Override
	public void setIcon (Pixmap[] pixmap) {

	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		return desktopMode;
	}

	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		if (!supportsDisplayModeChange()) return false;

		if (!fullscreen) {
			setWindowedMode(width, height);
		} else {
			DisplayMode mode = findBestMatch(width, height);
			if (mode == null) return false;
			setDisplayMode(mode);
		}
		return false;
	}

	protected JoglDisplayMode findBestMatch (int width, int height) {
		DisplayMode[] modes = getDisplayModes();
		int maxBitDepth = 0;
		DisplayMode best = null;
		for (DisplayMode mode : modes) {
			if (mode.width == width && mode.height == height && mode.bitsPerPixel == desktopMode.bitsPerPixel) {
				maxBitDepth = mode.bitsPerPixel;
				best = mode;
			}
		}
		return (JoglDisplayMode)best;
	}

	@Override
	public boolean setDisplayMode (DisplayMode displayMode) {
		if (!supportsDisplayModeChange()) return false;

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = genv.getDefaultScreenDevice();
		final JFrame frame = findJFrame(canvas);
		if (frame == null) return false;

		// create new canvas, sharing the rendering context with the old canvas
		// and pause the animator
		super.pause();
		GLCanvas newCanvas = new GLCanvas(canvas.getChosenGLCapabilities(), null, canvas.getContext(), device);
		newCanvas.addGLEventListener(this);

		JFrame newframe = new JFrame(frame.getTitle());
		newframe.setUndecorated(true);
		newframe.setResizable(false);
		newframe.add(newCanvas, BorderLayout.CENTER);
		newframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newframe.setLocationRelativeTo(null);
		newframe.pack();
		newframe.setVisible(true);

		device.setFullScreenWindow(newframe);
		device.setDisplayMode(((JoglDisplayMode)displayMode).mode);

		initializeGLInstances(canvas);
		this.canvas = newCanvas;
		((JoglInput)Gdx.input).setListeners(canvas);
		canvas.requestFocus();
		newframe.addWindowListener(((JoglApplication)Gdx.app).windowListener);
		((JoglApplication)Gdx.app).frame = newframe;
		resume();

		Gdx.app.postRunnable(new Runnable() {
			public void run () {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run () {
						frame.dispose();
					}
				});
			}
		});

		return true;
	}

	private boolean setWindowedMode (int width, int height) {

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = genv.getDefaultScreenDevice();
		if (device.isDisplayChangeSupported()) {
			device.setDisplayMode(desktopMode.mode);
			device.setFullScreenWindow(null);

			final JFrame frame = findJFrame(canvas);
			if (frame == null) return false;

			// create new canvas, sharing the rendering context with the old canvas
			// and pause the animator
			super.pause();
			GLCanvas newCanvas = new GLCanvas(canvas.getChosenGLCapabilities(), null, canvas.getContext(), device);
			newCanvas.setBackground(Color.BLACK);
			newCanvas.setPreferredSize(new Dimension(width, height));
			newCanvas.addGLEventListener(this);

			JFrame newframe = new JFrame(frame.getTitle());
			newframe.setUndecorated(false);
			newframe.setResizable(true);
			newframe.setSize(width + newframe.getInsets().left + newframe.getInsets().right,
				newframe.getInsets().top + newframe.getInsets().bottom + height);
			newframe.add(newCanvas, BorderLayout.CENTER);
			newframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			newframe.setLocationRelativeTo(null);
			newframe.pack();
			newframe.setVisible(true);

			initializeGLInstances(canvas);
			this.canvas = newCanvas;
			((JoglInput)Gdx.input).setListeners(canvas);
			canvas.requestFocus();
			newframe.addWindowListener(((JoglApplication)Gdx.app).windowListener);
			((JoglApplication)Gdx.app).frame = newframe;
			resume();

			Gdx.app.postRunnable(new Runnable() {
				public void run () {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run () {
							frame.dispose();
						}
					});
				}
			});
		} else {
			final JFrame frame = findJFrame(canvas);
			if (frame == null) return false;
			frame.setSize(width + frame.getInsets().left + frame.getInsets().right, frame.getInsets().top + frame.getInsets().bottom
				+ height);
		}

		return true;
	}

	protected static JFrame findJFrame (Component component) {
		Container parent = component.getParent();
		while (parent != null) {
			if (parent instanceof JFrame) {
				return (JFrame)parent;
			}
			parent = parent.getParent();
		}

		return null;
	}

	@Override
	public void setVSync (boolean vsync) {
		if (vsync)
			canvas.getGL().setSwapInterval(1);
		else
			canvas.getGL().setSwapInterval(0);
	}

	@Override
	public BufferFormat getBufferFormat () {
		GLCapabilities caps = canvas.getChosenGLCapabilities();
		return new BufferFormat(caps.getRedBits(), caps.getGreenBits(), caps.getBlueBits(), caps.getAlphaBits(),
			caps.getDepthBits(), caps.getStencilBits(), caps.getNumSamples(), false);
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}
}
