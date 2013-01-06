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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Application.ApplicationType;
import com.jogamp.newt.awt.NewtCanvasAWT;

public class JoglApplet  extends Applet {
	final Canvas canvas;
	JoglApplication app;

	class JoglAppletApplication extends JoglApplication {

		public JoglAppletApplication (ApplicationListener listener, JoglApplicationConfiguration config) {
			super(listener, config);
		}
		
		@Override
		public ApplicationType getType () {
			return ApplicationType.Applet;
		}
	}
	
	public JoglApplet(final ApplicationListener listener, final JoglApplicationConfiguration config) {
		app = new JoglAppletApplication(listener, config);
		canvas = new NewtCanvasAWT(app.getGLCanvas());
		//FIXME maybe something is needed to stop the application in removeNotify()
		setLayout(new BorderLayout());
		canvas.setIgnoreRepaint(true);
		add(canvas);
		canvas.setFocusable(true);
		canvas.requestFocus();
	}

	public void destroy () {
		remove(canvas);
		super.destroy();
	}
}
