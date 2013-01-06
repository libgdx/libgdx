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

import java.awt.Canvas;
import java.awt.Cursor;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.joal.OpenALAudio;
import com.jogamp.newt.awt.NewtCanvasAWT;

/** An OpenGL surface on a NEWT canvas linked to an AWT peer, allowing OpenGL to be embedded in a Swing application
 * @author Julien Gouesse */
public class JoglAWTCanvas extends JoglApplication {
	
	private NewtCanvasAWT canvas;

	public JoglAWTCanvas(final ApplicationListener listener, final String title, final int width, final int height,
			final boolean useGL20IfAvailable) {
		this(listener, title, width, height, useGL20IfAvailable, null);
	}
	
	public JoglAWTCanvas(final ApplicationListener listener, final String title, final int width, final int height,
			final boolean useGL20IfAvailable, JoglAWTCanvas shared) {
		super(listener, title, width, height, useGL20IfAvailable);
		if (shared != null) {
		    getGLCanvas().setSharedContext(shared.getGLCanvas().getContext());
		}
	}
	
	void initialize (ApplicationListener listener, JoglApplicationConfiguration config) {
		JoglNativesLoader.load();
		graphics = new JoglGraphics(listener, config) {
			public void setTitle (String title) {
				super.setTitle(title);
				JoglAWTCanvas.this.setTitle(title);
			}

			public boolean setDisplayMode (int width, int height, boolean fullscreen) {
				if (!super.setDisplayMode(width, height, fullscreen)) return false;
				if (!fullscreen) JoglAWTCanvas.this.setDisplayMode(width, height);
				return true;
			}

			public boolean setDisplayMode (DisplayMode displayMode) {
				if (!super.setDisplayMode(displayMode)) return false;
				JoglAWTCanvas.this.setDisplayMode(displayMode.width, displayMode.height);
				return true;
			}
		};
		canvas = new NewtCanvasAWT(getGLCanvas());
		input = new JoglInput(graphics.getCanvas());
		//audio = new OpenALAudio(16, config.audioDeviceBufferCount, config.audioDeviceBufferSize);
		//files = new JoglFiles();
		//net = new JoglNet();
		Gdx.app = this;
		Gdx.graphics = getGraphics();
		Gdx.input = getInput();
		//Gdx.audio = getAudio();
		//Gdx.files = getFiles();
		//Gdx.net = getNet();
		if (!JoglApplicationConfiguration.disableAudio && Gdx.audio == null) {
			audio = new OpenALAudio();
			Gdx.audio = audio;
		} else {
			audio = null;
		}
		if (Gdx.files == null) {
			files = new JoglFiles();
			Gdx.files = files;
		} else {
			files = null;
		}
		if (Gdx.net == null) {
			net = new JoglNet();
			Gdx.net = net;
		} else {
			net = null;
		}
		graphics.create();
		graphics.getCanvas().addWindowListener(windowListener);
		graphics.getCanvas().setTitle(config.title);
		graphics.getCanvas().setSize(config.width, config.height);
		graphics.getCanvas().setUndecorated(false);
		graphics.getCanvas().setFullscreen(config.fullscreen);
		graphics.getCanvas().setVisible(true);
	}
	
	protected void setDisplayMode (int width, int height) {
	}

	protected void setTitle (String title) {
	}
	
	public void setCursor (Cursor cursor) {
		canvas.setCursor(cursor);
	}

	public Canvas getCanvas () {
		return(canvas);
	}
}
