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

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;

import org.lwjgl.glfw.GLFW;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

class Lwjgl3Window implements Disposable {
	private long windowHandle;
	private final ApplicationListener listener;
	private boolean listenerInitialized = false;
	private final Lwjgl3Graphics graphics;
	private final Lwjgl3Input input;
	private final Lwjgl3ApplicationConfiguration config;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<Runnable> executedRunnables = new Array<Runnable>();

	public Lwjgl3Window(long windowHandle, ApplicationListener listener,
			Lwjgl3ApplicationConfiguration config) {
		this.windowHandle = windowHandle;
		this.listener = listener;
		this.config = config;
		this.input = new Lwjgl3Input(this);
		this.graphics = new Lwjgl3Graphics(this);		
	}

	public ApplicationListener getListener() {
		return listener;
	}

	public Lwjgl3Graphics getGraphics() {
		return graphics;
	}

	public Lwjgl3Input getInput() {
		return input;
	}

	public long getWindowHandle() {
		return windowHandle;
	}
	
	void windowHandleChanged(long windowHandle) {
		this.windowHandle = windowHandle;
		input.windowHandleChanged(windowHandle);
	}
	
	public void update(Array<LifecycleListener> lifecycleListeners) {
		if(listenerInitialized == false) {
			initializeListener();
		}
		synchronized(runnables) {		
			executedRunnables.addAll(runnables);
			runnables.clear();
		}
		for(Runnable runnable: executedRunnables) {
			runnable.run();
		}		
		executedRunnables.clear();
		
		graphics.update();		
		listener.render();
		glfwSwapBuffers(windowHandle);
		input.update();
		glfwPollEvents();
	}
	
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(windowHandle) == GLFW.GLFW_TRUE;
	}

	public Lwjgl3ApplicationConfiguration getConfig() {
		return config;
	}

	public void postRunnable(Runnable runnable) {
		synchronized(runnables) {
			runnables.add(runnable);
		}
	}

	public boolean isListenerInitialized() {
		return listenerInitialized;		
	}

	public void initializeListener() {
		if(!listenerInitialized) {
			listener.create();			
			listener.resize(graphics.getWidth(), graphics.getHeight());
			listenerInitialized = true;		
		}
	}
	
	@Override
	public void dispose() {
		listener.pause();
		listener.dispose();		
		graphics.dispose();
		input.dispose();		
		glfwDestroyWindow(windowHandle);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (windowHandle ^ (windowHandle >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Lwjgl3Window other = (Lwjgl3Window) obj;
		if (windowHandle != other.windowHandle)
			return false;
		return true;
	}
}
