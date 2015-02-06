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

package com.badlogic.gdx.backends.lwjgl3;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;

/** A simple controller that updates and control all GLFW window/Gdx Application. <br>
 * 
 * @author Natan Guilherme */
public class Lwjgl3WindowController {

	static long currentWindow;

	Array<Lwjgl3Application> windows;

	Runnable runnable;

	boolean shareContext;

	/** Sharecontext is for object sharing with multiple windows (1 Texture for all windows for example). <br>
	 * <br>
	 * 
	 * If the first window is destroyed than other windows will have a black texture. <br>
	 * You can change the parent context with {@link Lwjgl3WindowController#changeParentWindow(long)} */
	public Lwjgl3WindowController (boolean shareContext) {
		Lwjgl3NativesLoader.load();

		if (glfwInit() != GL11.GL_TRUE) throw new IllegalStateException("Unable to initialize GLFW");

		this.shareContext = shareContext;
		windows = new Array<Lwjgl3Application>();
	}

	/** Main Loop for GLFW. This call will block <br> */
	public void start () {
		while (windows.size > 0) {
			glfwPollEvents();

			for (int i = 0; i < windows.size; i++) {
				Lwjgl3Application app = windows.get(i);

				if (app.running) {

					if (app.init == false) {
						app.init = true;

						app.graphics.initWindow();
						glfwMakeContextCurrent(app.graphics.window);

						if (shareContext == false)
							app.context = GLContext.createFromCurrent();
						else {
							if (Lwjgl3Graphics.contextShare == 0) {
								app.context = GLContext.createFromCurrent();
								Lwjgl3Graphics.contextShare = app.graphics.window;
							}
						}

						app.graphics.initGL();
						app.graphics.show();

						app.initStaticVariables();

						app.input.addCallBacks();
						app.addCallBacks();
					}

					app.loop();
				} else {
					app.dispose();
					windows.removeIndex(i);
					i--;
				}
			}
		}
		glfwTerminate();
	}

	public void addWindow (String id, ApplicationListener listener, final Lwjgl3ApplicationConfiguration config) {
		addWindow(id, new Lwjgl3Application(listener, config, false));
	}

	public void addWindow (String id, Lwjgl3Application app) {
		if (app.autoloop) // cannot have a running loop
			return;

		if (getWindow(id) == null) {
			app.id = id;
			windows.add(app);
		}
	}

	public boolean removeWindow (String id) {
		for (int i = 0; i < windows.size; i++) {
			Lwjgl3Application app = windows.get(i);
			if (app.id.equals(id)) {
				app.exit();
				return true;
			}
		}
		return false;
	}

	public Lwjgl3Application getWindow (String id) {
		for (int i = 0; i < windows.size; i++) {
			Lwjgl3Application app = windows.get(i);
			if (app.id.equals(id)) {
				return app;
			}
		}
		return null;
	}

	/** Set a new window to share its context. Default is 0. Only for new windows. */
	public void changeParentWindow (long firstWindow) {
		Lwjgl3Graphics.contextShare = firstWindow;
	}

	/** The last focus window so you can use Glfw calls to manipulate it.*/
	public static long getCurrentWindow () {
		return currentWindow;
	}

}
