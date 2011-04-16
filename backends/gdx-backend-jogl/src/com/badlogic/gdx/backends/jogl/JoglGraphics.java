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

import java.awt.Toolkit;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.openal.OpenALAudio;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Implements the {@link Graphics} interface with Jogl.
 * 
 * @author mzechner
 * 
 */
public class JoglGraphics extends JoglGraphicsBase implements GLEventListener {
	ApplicationListener listener = null;
	boolean useGL2;
	boolean created = false;	

	public JoglGraphics (ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		initialize(title, width, height, useGL2);
		if (listener == null) throw new GdxRuntimeException("RenderListener must not be null");
		this.listener = listener;
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

	@Override public void init (GLAutoDrawable drawable) {
		initializeGLInstances(drawable);

		if (!created) {
			listener.create();
			synchronized (this) {
				paused = false;
			}
			created = true;
		}
	}

	@Override public void reshape (GLAutoDrawable drawable, int x, int y, int width, int height) {
		listener.resize(width, height);
	}

	@Override public void display (GLAutoDrawable arg0) {
		synchronized (this) {
			if (!paused) {
				updateTimes();
				synchronized (((JoglApplication)Gdx.app).runnables) {
					List<Runnable> runnables = ((JoglApplication)Gdx.app).runnables;
					for(int i = 0; i < runnables.size(); i++) {
						runnables.get(i).run();
					}
					runnables.clear();
				}
				((JoglInput)((JoglApplication)Gdx.app).getInput()).processEvents();
				listener.render();
				((OpenALAudio)Gdx.audio).update();
			}
		}
	}

	@Override public void displayChanged (GLAutoDrawable arg0, boolean arg1, boolean arg2) {

	}

	public void destroy () {
		canvas.getContext().makeCurrent();
		listener.dispose();
	}

	@Override public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override public float getPpcX () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override public float getPpcY () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}
	
	@Override public boolean supportsDisplayModeChange () {
		return false;
	}

	@Override public boolean setDisplayMode (DisplayMode displayMode) {
		return false;
	}
	
	@Override public DisplayMode[] getDisplayModes () {
		return new DisplayMode[0];
	}

	@Override public void setTitle (String title) {
		
	}

	@Override public void setIcon (Pixmap pixmap) {
		
	}
}
