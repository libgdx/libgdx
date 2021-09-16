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

package com.badlogic.gdx.backends.lwjgl;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

import com.badlogic.gdx.ApplicationListener;

/** An OpenGL surface in an applet.
 * @author Nathan Sweet */
public class LwjglApplet extends Applet {
	final Canvas canvas;
	LwjglApplication app;

	class LwjglAppletApplication extends LwjglApplication {

		public LwjglAppletApplication (ApplicationListener listener, Canvas canvas) {
			super(listener, canvas);
		}

		public LwjglAppletApplication (ApplicationListener listener, Canvas canvas, LwjglApplicationConfiguration config) {
			super(listener, config, canvas);
		}

		@Override
		public ApplicationType getType () {
			return ApplicationType.Applet;
		}
	}

	public LwjglApplet (final ApplicationListener listener, final LwjglApplicationConfiguration config) {
		LwjglNativesLoader.load = false;
		canvas = new Canvas() {
			public final void addNotify () {
				super.addNotify();
				app = new LwjglAppletApplication(listener, canvas, config);
			}

			public final void removeNotify () {
				app.stop();
				super.removeNotify();
			}
		};
		setLayout(new BorderLayout());
		canvas.setIgnoreRepaint(true);
		add(canvas);
		canvas.setFocusable(true);
		canvas.requestFocus();
	}

	public LwjglApplet (final ApplicationListener listener) {
		LwjglNativesLoader.load = false;
		canvas = new Canvas() {
			public final void addNotify () {
				super.addNotify();
				app = new LwjglAppletApplication(listener, canvas);
			}

			public final void removeNotify () {
				app.stop();
				super.removeNotify();
			}
		};
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
