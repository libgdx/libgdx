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

package com.badlogic.gdx.backends.gwt;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

public abstract class GwtApplication implements EntryPoint, Application {
	private ApplicationListener listener;
	private GwtApplicationConfiguration config;
	private GwtGraphics graphics;
	private Panel root = null;
	private TextArea log = null;
	private int logLevel = LOG_ERROR;
	private List<Runnable> runnables = new ArrayList<Runnable>();
	private int lastWidth, lastHeight;

	@Override
	public void onModuleLoad () {
		this.listener = getApplicationListener();
		this.config = getConfig();
		this.root = config.rootPanel != null ? config.rootPanel : RootPanel.get();

		graphics = new GwtGraphics(root, config);
		lastWidth = graphics.getWidth();
		lastHeight = graphics.getHeight();
		
		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.gl20 = graphics.getGL20();
		Gdx.gl = graphics.getGLCommon();

		setupLoop();
	}

	private void setupLoop () {
		// tell listener about app creation
		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());

		// add resize handler
//		graphics.surface.addHandler(new ResizeHandler() {
//			@Override
//			public void onResize (ResizeEvent event) {
//				GwtApplication.this.listener.resize(event.getWidth(), event.getHeight());
//			}
//		}, ResizeEvent.getType());

		// setup rendering timer
		new Timer() {
			@Override
			public void run () {
				graphics.setFps(0); // FIXME
				if(Gdx.graphics.getWidth() != lastWidth || Gdx.graphics.getHeight() != lastHeight) {
					GwtApplication.this.listener.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
					lastWidth = graphics.getWidth();
					lastHeight = graphics.getHeight();
				}
				for (int i = 0; i < runnables.size(); i++) {
					runnables.get(i).run();
				}
				runnables.clear();
				listener.render();
			}
		}.scheduleRepeating(config.fps);
	}

	public abstract GwtApplicationConfiguration getConfig ();

	public abstract ApplicationListener getApplicationListener ();

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Audio getAudio () {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public Input getInput () {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public Files getFiles () {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	private void checkLogLabel () {
		if (log == null) {
			log = new TextArea();
			log.setSize(graphics.getWidth() + "px", "150px");
			log.setReadOnly(true);
			root.add(log);
		}
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message);
			log.setCursorPos(log.getText().length() - 1);
		}
	}

	@Override
	public void log (String tag, String message, Exception exception) {
		if (logLevel >= LOG_INFO) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + exception.getMessage());
			log.setCursorPos(log.getText().length() - 1);
		}
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message);
			log.setCursorPos(log.getText().length() - 1);
		}
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			checkLogLabel();
			log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + exception.getMessage());
			log.setCursorPos(log.getText().length() - 1);
		}
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.WebGL;
	}

	@Override
	public int getVersion () {
		return 0;
	}

	@Override
	public long getJavaHeap () {
		return 0;
	}

	@Override
	public long getNativeHeap () {
		return 0;
	}

	@Override
	public Preferences getPreferences (String name) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void postRunnable (Runnable runnable) {
		runnables.add(runnable);
	}

	@Override
	public void exit () {
	}

	@Override
	public void debug (String tag, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		// TODO Auto-generated method stub

	}
}
