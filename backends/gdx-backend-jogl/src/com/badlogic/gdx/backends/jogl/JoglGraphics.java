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

import java.util.List;

import javax.media.nativewindow.NativeWindow;
import javax.media.nativewindow.util.Dimension;
import javax.media.nativewindow.util.DimensionImmutable;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLEventListener;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.joal.OpenALAudio;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.jogamp.newt.Screen;
import com.jogamp.newt.ScreenMode;
import com.jogamp.newt.util.MonitorMode;
import com.jogamp.newt.util.ScreenModeUtil;

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
		canvas.setFullscreen(config.fullscreen);
		canvas.setUndecorated(config.fullscreen);
		canvas.getScreen().addReference();
		ScreenMode mode = canvas.getScreen().getCurrentScreenMode();
		//FIXME use JoglApplicationConfiguration.getDesktopDisplayMode ()
		desktopMode = (JoglDisplayMode) new JoglDisplayMode(mode.getRotatedWidth(), mode.getRotatedHeight(), mode.getMonitorMode().getRefreshRate(), mode.getMonitorMode().getSurfaceSize().getBitsPerPixel(), mode);
	}

	public void create () {
		super.create();
	}

	public void pause () {
		super.pause();
		if (!canvas.getContext().isCurrent()) {
		    canvas.getContext().makeCurrent();
		}
		listener.pause();
	}

	public void resume () {
		if (!canvas.getContext().isCurrent()) {
		    canvas.getContext().makeCurrent();
		}
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
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				}
				((JoglInput)(Gdx.input)).processEvents();
				listener.render();
				((OpenALAudio)Gdx.audio).update();
			}
		}
	}

	public void destroy () {
		if (!canvas.getContext().isCurrent()) {
		    canvas.getContext().makeCurrent();
		}
		listener.dispose();
		canvas.setFullscreen(false);
	}

	private float getScreenResolution() {
		Screen screen = canvas.getScreen();
		screen.addReference();
		ScreenMode sm = screen.getCurrentScreenMode();
		final MonitorMode mmode = sm.getMonitorMode();
		final DimensionImmutable sdim = mmode.getScreenSizeMM();
		final DimensionImmutable spix = mmode.getSurfaceSize().getResolution();
        float screenResolution = (float)spix.getWidth() / (float)sdim.getWidth();
        canvas.getScreen().removeReference();
        return(screenResolution);
	}
	
	@Override
	public float getPpiX () {
		return getScreenResolution();
	}

	@Override
	public float getPpiY () {
		return getScreenResolution();
	}

	@Override
	public float getPpcX () {
		return (getScreenResolution() / 2.54f);
	}

	@Override
	public float getPpcY () {
		return (getScreenResolution() / 2.54f);
	}

	@Override
	public float getDensity () {
		return (getScreenResolution() / 160f);
	}

	@Override
	public boolean supportsDisplayModeChange () {
		return true;
	}

	protected static class JoglDisplayMode extends DisplayMode {
		final ScreenMode mode;

		protected JoglDisplayMode (int width, int height, int refreshRate, int bitsPerPixel, ScreenMode mode) {
			super(width, height, refreshRate, bitsPerPixel);
			this.mode = mode;
		}
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		//FIXME use JoglApplicationConfiguration.getDisplayModes()
		List<ScreenMode> screenModes = canvas.getScreen().getScreenModes();
		DisplayMode[] displayModes = new DisplayMode[screenModes.size()];
		for (int modeIndex = 0 ; modeIndex < displayModes.length ; modeIndex++) {
			ScreenMode mode = screenModes.get(modeIndex);
			displayModes[modeIndex] = new JoglDisplayMode(mode.getRotatedWidth(), mode.getRotatedHeight(), mode.getMonitorMode().getRefreshRate(), mode.getMonitorMode().getSurfaceSize().getBitsPerPixel(), mode);
		}
		return displayModes;
	}

	@Override
	public void setTitle (String title) {
		canvas.setTitle(title);
	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		return desktopMode;
	}

	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		if (width == canvas.getWidth() && height == canvas.getHeight() && canvas.isFullscreen() == fullscreen) {
			return true;
		}
		ScreenMode targetDisplayMode = null;
		List<ScreenMode> screenModes = canvas.getScreen().getScreenModes();
		Dimension dimension = new Dimension(width,height);
		screenModes = ScreenModeUtil.filterByResolution(screenModes, dimension);
		screenModes = ScreenModeUtil.filterByRate(screenModes, canvas.getScreen().getCurrentScreenMode().getMonitorMode().getRefreshRate());
		screenModes = ScreenModeUtil.getHighestAvailableRate(screenModes);
		if (screenModes == null || screenModes.isEmpty()) {
			return false;
		}
		targetDisplayMode = screenModes.get(0);
		canvas.setUndecorated(fullscreen);
		canvas.setFullscreen(fullscreen);
		canvas.getScreen().setCurrentScreenMode(targetDisplayMode);
		if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, targetDisplayMode.getRotatedWidth(), targetDisplayMode.getRotatedHeight());
		config.width = targetDisplayMode.getRotatedWidth();
		config.height = targetDisplayMode.getRotatedHeight();
		return true;
	}
	
	@Override
	public boolean setDisplayMode (DisplayMode displayMode) {
		ScreenMode screenMode = ((JoglDisplayMode)displayMode).mode;
		
		canvas.setUndecorated(true);
		canvas.setFullscreen(true);
		canvas.getScreen().setCurrentScreenMode(screenMode);
		if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, displayMode.width, displayMode.height);
		config.width = displayMode.width;
		config.height = displayMode.height;
		
		return true;
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
		GLCapabilitiesImmutable caps = canvas.getChosenGLCapabilities();
		return new BufferFormat(caps.getRedBits(), caps.getGreenBits(), caps.getBlueBits(), caps.getAlphaBits(),
			caps.getDepthBits(), caps.getStencilBits(), caps.getNumSamples(), false);
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	@Override
	public boolean isFullscreen () {
		return canvas.isFullscreen();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		setContinuousRendering(true);
        pause();
        destroy();
	}

	@Override
	public void setContinuousRendering(boolean isContinuous) {
	}

	@Override
	public boolean isContinuousRendering() {
		return true;
	}
}
