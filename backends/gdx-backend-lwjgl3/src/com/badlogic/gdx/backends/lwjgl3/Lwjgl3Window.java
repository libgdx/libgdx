package com.badlogic.gdx.backends.lwjgl3;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

import org.lwjgl.glfw.GLFW;

import com.badlogic.gdx.ApplicationListener;
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

	public Lwjgl3Window(long windowHandle, ApplicationListener listener,
			Lwjgl3ApplicationConfiguration config) {
		this.windowHandle = windowHandle;
		this.listener = listener;
		this.config = config;
		this.graphics = new Lwjgl3Graphics(this);
		this.input = new Lwjgl3Input(this);		
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
			listener.create();
			listener.resize(graphics.getWidth(), graphics.getHeight());
			listenerInitialized = true;
		}
		synchronized(runnables) {
			for(Runnable runnable: runnables) {
				runnable.run();
			}
			runnables.clear();
		}
		graphics.update();
		input.update();
		listener.render();
		glfwSwapBuffers(windowHandle);
		glfwPollEvents();
	}
	
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(windowHandle) == GLFW.GLFW_TRUE;
	}

	@Override
	public void dispose() {
		listener.pause();
		listener.dispose();
	}

	public Lwjgl3ApplicationConfiguration getConfig() {
		return config;
	}

	public void postRunnable(Runnable runnable) {
		synchronized(runnables) {
			runnables.add(runnable);
		}
	}
}
