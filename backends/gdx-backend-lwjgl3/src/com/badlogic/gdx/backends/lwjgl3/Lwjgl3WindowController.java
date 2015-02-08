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

import sun.tools.jar.Main;

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

	Object SYNC = new Object();
	
	private Runnable windowRunnablesWait;
	private Runnable executedWindowRunnablesWait;
	
	private final Array<Runnable> mainRunnables = new Array();
	private final Array<Runnable> executedMainRunnables = new Array();
	private final Array<Runnable> windowRunnables = new Array();
	private final Array<Runnable> executedWindowRunnables = new Array();
	
	
	private GLFWErrorCallback errorCallback;
	static long currentWindow;

	Array<Lwjgl3Application> windows;
	Array<Lwjgl3Application> queueWindows;

	Runnable runnable;

	int targetFPS = 60;

	boolean running = true;
	boolean shareContext;
	Thread windowThread;
	Thread mainThread;

	boolean jumpLoop;
	
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
		queueWindows = new Array<Lwjgl3Application>();

		this.shareContext = shareContext;

		runnable = new Runnable() {

			@Override
			public void run () {

				while (running) {
					
					executeWindowRunnablesAndWait();
					executeWindowRunnables();
					if(jumpLoop)
						continue;
					for (int i = 0; i < windows.size; i++) {
						final Lwjgl3Application app = windows.get(i);

						if (app.running) {
							
							if(app.init)
								app.loop();
						} else {
							app.setGlobals();
							app.disposeListener();
							windows.removeIndex(i);
							i--;
							
							Runnable run = new Runnable() {
								
								@Override
								public void run () {
									app.dispose();
								}
							};
							postMainRunnable(run);
							
							glfwPostEmptyEvent();
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
		};

		windowThread = new Thread(runnable,"Lwjgl3WindowController");
	}

	/** Main Loop for GLFW. This call will block <br> */
	public void start () {
		mainThread = Thread.currentThread();
		windowThread.start();
		running = true;
		while (running) {
			executeMainRunnables();
			
			for(int i = 0; i < queueWindows.size;i++)
			{
				final Lwjgl3Application app = queueWindows.removeIndex(i);
				i--;
				
				Runnable run = new Runnable() {
					@Override
					public void run () 
					{
						glfwMakeContextCurrent(0);
						jumpLoop = true;
					}
				};
				postWindowRunnableAndWait(run);
				
				initWindow(app);
				glfwShowWindow(app.graphics.window);
				
				
				Runnable run2 = new Runnable() {
					@Override
					public void run () 
					{
						initContext(app);
						windows.add(app);
					}
				};
				postWindowRunnable(run2);
				jumpLoop = false;
				break;
			}
			
			if (windows.size == 0 && queueWindows.size == 0) running = false;
			glfwWaitEvents();
		}
		try {
			windowThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		glfwTerminate();
		if(errorCallback != null)
			errorCallback.release();
	}
	void initWindow(Lwjgl3Application app)
	{
		app.graphics.initWindow();
		if (shareContext == true && Lwjgl3Graphics.contextShare == 0) {
			Lwjgl3Graphics.contextShare = app.graphics.window;
		}
		app.initStaticVariables();
		app.input.addCallBacks();
		app.addCallBacks();
	}
	void initContext(Lwjgl3Application app)
	{
		if (app.init == false) {
			
			glfwMakeContextCurrent(app.graphics.window);
			app.context = GLContext.createFromCurrent();
			app.graphics.initGL();
			app.graphics.show();
			app.init = true;
		}
	}

	public void addWindow (String id, ApplicationListener listener, final Lwjgl3ApplicationConfiguration config) {
		addWindow(id, new Lwjgl3Application(listener, config, false));
	}

	public void addWindow (final String id, final Lwjgl3Application app) {
		if (app.autoloop) // cannot have a running loop
			return;
		
		Thread thisThread = Thread.currentThread();
		
		
		if(thisThread == mainThread)
		{
			if (getWindow(id) == null)
			{
				app.id = id;
				queueWindows.add(app);
			}
		}
		else
		{
			Runnable run = new Runnable() {
				
				@Override
				public void run () 
				{
					if (getWindow(id) == null)
					{
						app.id = id;
						queueWindows.add(app);
					}
				}
			};
			postMainRunnable(run);
		}
			
//			Runnable run = new Runnable() {
//				
//				@Override
//				public void run () {
//					
//					
//					if (getWindow(id) == null)
//					{
//						app.id = id;
//						Runnable run = new Runnable() {
//							
//							@Override
//							public void run () 
//							{
//								
//								glfwMakeContextCurrent(0);
//								System.out.println("22222");
//							}
//						};
//						
//						jumpLoop = true; // dont let Window thread loop to let window creation successfull.
//						System.out.println("1111");
//						if(windowThread == Thread.currentThread())
//						{
//							postWindowRunnableAndWait(run);
//						}
//						else
//							run.run();
//						
//						
//						System.out.println("3333: " + jumpLoop);
//						System.out.println("Thread: " + Thread.currentThread());
//						app.id = id;
//						initWindow(app);
//						
//						windows.add(app);
//						
//						
//						
//						Runnable run2 = new Runnable() {
//							
//							@Override
//							public void run () 
//							{
//								initContext(app);
//							}
//						};
//						postWindowRunnable(run2);
//					
//						System.out.println("444: " + jumpLoop);
//						jumpLoop = false;
//					}
//				}
//			};
//			jumpLoop = true;
//			
//			if(mainThread == Thread.currentThread())
//				run.run();
//			else 
//				postMainRunnable(run);
//			
				
//				initWindow(app);
//				windows.add(app);
				
				
//				glfwPostEmptyEvent();
//				toRefresh3 = true;
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
	
	
	public void postMainRunnable (Runnable runnable) {
		synchronized (mainRunnables) {
			mainRunnables.add(runnable);
		}
	}
	
	
	public void postWindowRunnable (Runnable runnable) {
		synchronized (windowRunnables) {
			windowRunnables.add(runnable);
		}
	}
	public void postWindowRunnableAndWait (Runnable runnable) {
		synchronized (SYNC) {
			windowRunnablesWait = runnable;
		}
		
		while(windowRunnablesWait != null)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private void executeMainRunnables () {
		synchronized (mainRunnables) {
			for (int i = mainRunnables.size - 1; i >= 0; i--)
				executedMainRunnables.addAll(mainRunnables.get(i));
			mainRunnables.clear();
		}
		
		if (executedMainRunnables.size == 0) return;
		for (int i = executedMainRunnables.size - 1; i >= 0; i--)
			executedMainRunnables.removeIndex(i).run();
	}
	
	private void executeWindowRunnables () {
		synchronized (windowRunnables) {
			for (int i = windowRunnables.size - 1; i >= 0; i--)
				executedWindowRunnables.addAll(windowRunnables.get(i));
			windowRunnables.clear();
		}
		
		if (executedWindowRunnables.size == 0) return;
		for (int i = executedWindowRunnables.size - 1; i >= 0; i--)
			executedWindowRunnables.removeIndex(i).run();
	}
	
	
	private void executeWindowRunnablesAndWait () {
		
		if(windowRunnablesWait == null)
			return;
		
		synchronized (SYNC) {
			executedWindowRunnablesWait = windowRunnablesWait ;
		}
		executedWindowRunnablesWait.run();
		executedWindowRunnablesWait = null;
		windowRunnablesWait = null;
	}
	
	
//	public boolean executeRunnables () {
//		synchronized (runnables) {
//			for (int i = runnables.size - 1; i >= 0; i--)
//				executedRunnables.addAll(runnables.get(i));
//			runnables.clear();
//		}
//		if (executedRunnables.size == 0) return false;
//		for (int i = executedRunnables.size - 1; i >= 0; i--)
//			executedRunnables.removeIndex(i).run();
//		return true;
//	}

}
