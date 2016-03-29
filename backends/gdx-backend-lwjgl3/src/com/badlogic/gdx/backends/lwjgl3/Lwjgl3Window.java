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

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Lwjgl3Window implements Disposable {
	private long windowHandle;
	private final ApplicationListener listener;
	private boolean listenerInitialized = false;
	private Lwjgl3WindowListener windowListener;
	private final Lwjgl3Graphics graphics;
	private final Lwjgl3Input input;
	private final Lwjgl3ApplicationConfiguration config;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<Runnable> executedRunnables = new Array<Runnable>();
	private final IntBuffer tmpBuffer;
	private final IntBuffer tmpBuffer2;
	private boolean iconified = false;
	
	private final GLFWWindowFocusCallback focusCallback = new GLFWWindowFocusCallback() {
		@Override
		public void invoke(long windowHandle, final int focused) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					if(windowListener != null) {
						if(focused == GLFW.GLFW_TRUE) {
							windowListener.focusGained();
						} else {
							windowListener.focusLost();
						}
					}
				}
			});			
		}
	};
	
	private final GLFWWindowIconifyCallback iconifyCallback = new GLFWWindowIconifyCallback() {
		@Override
		public void invoke(long windowHandle, final int iconified) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					if(windowListener != null) {
						if(iconified == GLFW.GLFW_TRUE) {
							windowListener.iconified();
						} else {
							windowListener.deiconified();
						}
					}
					Lwjgl3Window.this.iconified = iconified == GLFW.GLFW_TRUE? true: false;
					if(iconified == GLFW.GLFW_TRUE) {
						listener.pause();
					} else {
						listener.resume();
					}
				}
			});	
		}
	};
	
	private final GLFWWindowCloseCallback closeCallback = new GLFWWindowCloseCallback() {
		@Override
		public void invoke(final long windowHandle) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					if(windowListener != null) {
						if(!windowListener.closeRequested()) {
							GLFW.glfwSetWindowShouldClose(windowHandle, GLFW.GLFW_FALSE);
						}
					}
				}
			});	
		}
	};
	
	private final GLFWDropCallback dropCallback = new GLFWDropCallback() {
		@Override
		public void invoke(final long windowHandle, final int count, final long names) {
			final String[] files = getNames(count, names);
			postRunnable(new Runnable() {
				@Override
				public void run() {
					if(windowListener != null) {
						windowListener.filesDropped(files);
					}
				}
			});	
		}
	};

	Lwjgl3Window(long windowHandle, ApplicationListener listener,
			Lwjgl3ApplicationConfiguration config) {
		this.windowHandle = windowHandle;
		this.listener = listener;
		this.windowListener = config.windowListener;
		this.config = config;
		this.input = new Lwjgl3Input(this);
		this.graphics = new Lwjgl3Graphics(this);
		this.tmpBuffer = BufferUtils.createIntBuffer(1);
		this.tmpBuffer2 = BufferUtils.createIntBuffer(1);
		
		GLFW.glfwSetWindowFocusCallback(windowHandle, focusCallback);
		GLFW.glfwSetWindowIconifyCallback(windowHandle, iconifyCallback);
		GLFW.glfwSetWindowCloseCallback(windowHandle, closeCallback);
		GLFW.glfwSetDropCallback(windowHandle, dropCallback);
	}

	/** @return the {@link ApplicationListener} associated with this window **/	 
	public ApplicationListener getListener() {
		return listener;
	}
	
	/** @return the {@link Lwjgl3WindowListener} set on this window **/
	public Lwjgl3WindowListener getWindowListener() {
		return windowListener;
	}
	
	public void setWindowListener(Lwjgl3WindowListener listener) {
		this.windowListener = listener;
	}
	
	/**
	 * Post a {@link Runnable} to this window's event queue. Use this
	 * if you access statics like {@link Gdx#graphics} in your runnable
	 * instead of {@link Application#postRunnable(Runnable)}.
	 */
	public void postRunnable(Runnable runnable) {
		synchronized(runnables) {
			runnables.add(runnable);
		}
	}
	
	/** Sets the position of the window in logical coordinates. All monitors
	 * span a virtual surface together. The coordinates are relative to
	 * the first monitor in the virtual surface. **/
	public void setPosition(int x, int y) {
		GLFW.glfwSetWindowPos(windowHandle, x, y);
	}
		
	/** @return the window position in logical coordinates. All monitors
	 * span a virtual surface together. The coordinates are relative to
	 * the first monitor in the virtual surface. **/
	public int getPositionX() {
		GLFW.glfwGetWindowPos(windowHandle, tmpBuffer, tmpBuffer2);
		return tmpBuffer.get(0);
	}
	
	/** @return the window position in logical coordinates. All monitors
	 * span a virtual surface together. The coordinates are relative to
	 * the first monitor in the virtual surface. **/
	public int getPositionY() {
		GLFW.glfwGetWindowPos(windowHandle, tmpBuffer, tmpBuffer2);
		return tmpBuffer2.get(0);
	}
	
	/**
	 * Sets the visibility of the window. Invisible windows will still
	 * call their {@link ApplicationListener}
	 */
	public void setVisible(boolean visible) {
		if(visible) {
			GLFW.glfwShowWindow(windowHandle);
		} else {
			GLFW.glfwHideWindow(windowHandle);
		}
	}
	
	/**
	 * Closes this window and pauses and disposes the associated
	 * {@link ApplicationListener}.
	 */
	public void closeWindow() {
		GLFW.glfwSetWindowShouldClose(windowHandle, GLFW.GLFW_TRUE);
	}
	
	/**
	 * Minimizes (iconfies) the window. Iconified windows do not call
	 * their {@link ApplicationListener} until the window is deiconified.
	 */
	public void iconifyWindow() {
		GLFW.glfwIconifyWindow(windowHandle);
	}
	
	/**
	 * De-minimizes the window.
	 */
	public void deiconifyWindow() {
		GLFW.glfwRestoreWindow(windowHandle);
	}

	Lwjgl3Graphics getGraphics() {
		return graphics;
	}

	Lwjgl3Input getInput() {
		return input;
	}

	long getWindowHandle() {
		return windowHandle;
	}
	
	void windowHandleChanged(long windowHandle) {
		this.windowHandle = windowHandle;
		input.windowHandleChanged(windowHandle);
	}
	
	void update(Array<LifecycleListener> lifecycleListeners) {
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
		
		if(!iconified) {
			graphics.update();		
			listener.render();
			GLFW.glfwSwapBuffers(windowHandle);
			input.update();		
		}
	}
	
	boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(windowHandle) == GLFW.GLFW_TRUE;
	}

	Lwjgl3ApplicationConfiguration getConfig() {
		return config;
	}

	boolean isListenerInitialized() {
		return listenerInitialized;		
	}

	void initializeListener() {
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
		Lwjgl3Cursor.dispose(this);
		graphics.dispose();
		input.dispose();
		GLFW.glfwSetWindowFocusCallback(windowHandle, null);
		GLFW.glfwSetWindowIconifyCallback(windowHandle, null);
		GLFW.glfwSetWindowCloseCallback(windowHandle, null);
		GLFW.glfwSetDropCallback(windowHandle, null);
		GLFW.glfwDestroyWindow(windowHandle);
		
		focusCallback.release();
		iconifyCallback.release();
		closeCallback.release();
		dropCallback.release();
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
