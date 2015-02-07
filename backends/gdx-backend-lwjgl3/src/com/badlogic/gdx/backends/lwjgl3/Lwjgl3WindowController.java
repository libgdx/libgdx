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

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/** A simple controller that updates and control all GLFW window/Gdx Application. <br>
 * 
 * @author Natan Guilherme */
public class Lwjgl3WindowController {

	private GLFWErrorCallback errorCallback;
	static long currentWindow;

	Array<Lwjgl3Application> queuewindows;
	Array<Lwjgl3Application> windows;

	Runnable runnable;

	int targetFPS = 60;

	boolean running = true;
	boolean shareContext;
	Thread thread;

	static Object SYNC = new Object();
	static Object SYNC2 = new Object();
	static Object SYNC3 = new Object();
	static boolean toRefresh = true;

	boolean toRefresh2 = true;
	boolean toRefresh3 = true;
	
	/** Sharecontext is for object sharing with multiple windows (1 Texture for all windows for example). <br>
	 * <br>
	 * 
	 * If the first window is destroyed than other windows will have a black texture. <br>
	 * You can change the parent context with {@link Lwjgl3WindowController#changeParentWindow(long)} */
	public Lwjgl3WindowController (boolean shareContext) {
		Lwjgl3NativesLoader.load();

		
		if (glfwInit() != GL11.GL_TRUE) throw new IllegalStateException("Unable to initialize GLFW");

		glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));
		windows = new Array<Lwjgl3Application>();
		queuewindows = new Array<Lwjgl3Application>();

		this.shareContext = shareContext;

		runnable = new Runnable() {

			@Override
			public void run () {

				while (running) {
					
					if(toRefresh2 == false)
						continue;
					synchronized (SYNC3) {
						for (int i = 0; i < windows.size; i++) {
							Lwjgl3Application app = windows.get(i);

							if (app.running) {

								if (app.init == false) continue;

								if (toRefresh == false) // simple sync logic to refresh window when there is a refresh call
									continue;
								synchronized (SYNC) {
									app.loop();
								}
							} else {
								app.dispose();
								windows.removeIndex(i);
								i--;
							}
						}

						if (targetFPS != 0) {
							if (targetFPS == -1)
								Lwjgl3Application.sleep(100);
							else
								Sync.sync(targetFPS);
						}

					}
				}
			}
		};

		thread = new Thread(runnable,"Lwjgl3WindowController");
	}

	/** Main Loop for GLFW. This call will block <br> */
	public void start () {
		thread.start();
		running = true;
		while (running) {
			
			glfwWaitEvents();
			if(toRefresh3 == false)
				continue;
			
			synchronized (SYNC2) {

				for (int i = 0; i < queuewindows.size; i++) {
					Lwjgl3Application app = queuewindows.removeIndex(i);
					i--;
					
					initWindow(app);
					
					toRefresh2 = false;
					synchronized (SYNC3) {
						windows.add(app);
						
						toRefresh2 = true;
					}

				}
			}

			if (windows.size == 0) running = false;
		}
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		glfwTerminate();
		if(errorCallback != null)
			errorCallback.release();
	}
	
	void initWindow(Lwjgl3Application app)
	{
		if (app.init == false) {
			
			glfwMakeContextCurrent(0); // null the context so it dont pop up a error
			app.graphics.initWindow();
			glfwMakeContextCurrent(app.graphics.window);
			app.context = GLContext.createFromCurrent();
			if (shareContext == true && Lwjgl3Graphics.contextShare == 0) {
				Lwjgl3Graphics.contextShare = app.graphics.window;
			}
			
			app.graphics.initGL();
			app.graphics.show();

			app.initStaticVariables();

			app.input.addCallBacks();
			app.addCallBacks();

			app.init = true;
		}
	}

	public void addWindow (String id, ApplicationListener listener, final Lwjgl3ApplicationConfiguration config) {
		addWindow(id, new Lwjgl3Application(listener, config, false));
	}

	public void addWindow (String id, Lwjgl3Application app) {
		if (app.autoloop) // cannot have a running loop
			return;
		
		toRefresh3 = false;
		synchronized (SYNC2) {
			if (getWindow(id) == null) {
				app.id = id;
				queuewindows.add(app);
				toRefresh3 = true;
			}
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

	/** The last focus window so you can use Glfw calls to manipulate it. */
	public static long getCurrentWindow () {
		return currentWindow;
	}

}
