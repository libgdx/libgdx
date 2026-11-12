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

package com.badlogic.gdx.backends.sdl3;

import java.nio.IntBuffer;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Os;
import org.lwjgl.BufferUtils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.lwjgl.sdl.*;

public class Lwjgl3Window implements Disposable {
	private long windowHandle;
	long glContext;
	final ApplicationListener listener;
	private final Array<LifecycleListener> lifecycleListeners;
	final Lwjgl3ApplicationBase application;
	private boolean listenerInitialized = false;
	Lwjgl3WindowListener windowListener;
	private Lwjgl3Graphics graphics;
	private Lwjgl3Input input;
	private final Lwjgl3ApplicationConfiguration config;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<Runnable> executedRunnables = new Array<Runnable>();
	private final IntBuffer tmpBuffer;
	private final IntBuffer tmpBuffer2;
	private boolean shouldClose = false;
	boolean iconified = false;
	boolean autoIconify = false;
	boolean focused = false;
	private boolean requestRendering = false;
	boolean isMouseInside = false;
	private long previousCursor;
	long currentCursor;
	long sdlID;

	void focusCallback (final boolean focused) {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				if (focused) {
					if (config.pauseWhenLostFocus) {
						synchronized (lifecycleListeners) {
							for (LifecycleListener lifecycleListener : lifecycleListeners) {
								lifecycleListener.resume();
							}
						}
						listener.resume();
					}
					if (windowListener != null) {
						windowListener.focusGained();
					}
				} else {
					if (windowListener != null) {
						windowListener.focusLost();
					}
					if (config.pauseWhenLostFocus) {
						synchronized (lifecycleListeners) {
							for (LifecycleListener lifecycleListener : lifecycleListeners) {
								lifecycleListener.pause();
							}
						}
						listener.pause();
					}
				}
				Lwjgl3Window.this.focused = focused;
			}
		});
	}

	void iconifyCallback (final boolean iconified) {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				if (windowListener != null) {
					windowListener.iconified(iconified);
				}
				Lwjgl3Window.this.iconified = iconified;
				if (iconified) {
					if (config.pauseWhenMinimized) {
						synchronized (lifecycleListeners) {
							for (LifecycleListener lifecycleListener : lifecycleListeners) {
								lifecycleListener.pause();
							}
						}
						listener.pause();
					}
				} else {
					if (config.pauseWhenMinimized) {
						synchronized (lifecycleListeners) {
							for (LifecycleListener lifecycleListener : lifecycleListeners) {
								lifecycleListener.resume();
							}
						}
						listener.resume();
					}
				}
			}
		});
	}

	void maximizeCallback (final boolean maximized) {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				if (windowListener != null) {
					windowListener.maximized(maximized);
				}
			}
		});
	}

	Array<String> dropFiles = new Array<>(1);

	void dropClear () {
		dropFiles.clear();
	}

	void dropFile (String name) {
		dropFiles.add(name);
	}

	void dropCallback () {
		String[] files = new String[this.dropFiles.size];
		for (int i = 0; i < files.length; i++) {
			files[i] = this.dropFiles.get(i);
		}
		postRunnable(new Runnable() {
			@Override
			public void run () {
				if (windowListener != null) {
					windowListener.filesDropped(files);
				}
			}
		});
	}

	void refreshCallback () {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				if (windowListener != null) {
					windowListener.refreshRequested();
				}
			}
		});
	}

	Lwjgl3Window (ApplicationListener listener, Array<LifecycleListener> lifecycleListeners, Lwjgl3ApplicationConfiguration config,
		Lwjgl3ApplicationBase application) {
		this.listener = listener;
		this.lifecycleListeners = lifecycleListeners;
		this.windowListener = config.windowListener;
		this.config = config;
		this.application = application;
		this.tmpBuffer = BufferUtils.createIntBuffer(1);
		this.tmpBuffer2 = BufferUtils.createIntBuffer(1);
	}

	void create (long windowHandle, long glContext) {
		this.sdlID = SDLVideo.SDL_GetWindowID(windowHandle);
		this.windowHandle = windowHandle;
		this.glContext = glContext;
		this.input = application.createInput(this);
		this.graphics = new Lwjgl3Graphics(this);

		if (windowListener != null) {
			windowListener.created(this);
		}
	}

	/** @return the {@link ApplicationListener} associated with this window **/
	public ApplicationListener getListener () {
		return listener;
	}

	/** @return the {@link Lwjgl3WindowListener} set on this window **/
	public Lwjgl3WindowListener getWindowListener () {
		return windowListener;
	}

	public void setWindowListener (Lwjgl3WindowListener listener) {
		this.windowListener = listener;
	}

	/** Post a {@link Runnable} to this window's event queue. Use this if you access statics like {@link Gdx#graphics} in your
	 * runnable instead of {@link Application#postRunnable(Runnable)}. */
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

	/** Sets the position of the window in logical coordinates. All monitors span a virtual surface together. The coordinates are
	 * relative to the first monitor in the virtual surface. **/
	public void setPosition (int x, int y) {
		SDLVideo.SDL_SetWindowPosition(windowHandle, x, y);
	}

	/** @return the window position in logical coordinates. All monitors span a virtual surface together. The coordinates are
	 *         relative to the first monitor in the virtual surface. **/
	public int getPositionX () {
		SDLVideo.SDL_GetWindowPosition(windowHandle, tmpBuffer, tmpBuffer2);
		return tmpBuffer.get(0) == -1 ? 0 : tmpBuffer.get(0); // Return 0 for wayland instead of -1
	}

	/** @return the window position in logical coordinates. All monitors span a virtual surface together. The coordinates are
	 *         relative to the first monitor in the virtual surface. **/
	public int getPositionY () {
		SDLVideo.SDL_GetWindowPosition(windowHandle, tmpBuffer, tmpBuffer2);
		return tmpBuffer2.get(0) == -1 ? 0 : tmpBuffer2.get(0); // Return 0 for wayland instead of -1
	}

	/** Sets the visibility of the window. Invisible windows will still call their {@link ApplicationListener} */
	public void setVisible (boolean visible) {
		if (visible) {
			Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_ShowWindow(windowHandle));
		} else {
			Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_HideWindow(windowHandle));
		}
	}

	/** Closes this window and pauses and disposes the associated {@link ApplicationListener}. */
	public void closeWindow () {
		shouldClose = true;
	}

	/** Minimizes (iconifies) the window. Iconified windows do not call their {@link ApplicationListener} until the window is
	 * restored. */
	public void iconifyWindow () {
		Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_MinimizeWindow(windowHandle));
	}

	/** Whether the window is iconfieid */
	public boolean isIconified () {
		return iconified;
	}

	/** De-minimizes (de-iconifies) and de-maximizes the window. */
	public void restoreWindow () {
		Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_RestoreWindow(windowHandle));
	}

	/** Maximizes the window. */
	public void maximizeWindow () {
		Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_MaximizeWindow(windowHandle));
	}

	/** Brings the window to front and sets input focus. The window should already be visible and not iconified. */
	public void focusWindow () {
		Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_RaiseWindow(windowHandle));
	}

	public boolean isFocused () {
		return focused;
	}

	/** Sets the icon that will be used in the window's title bar. Has no effect in macOS, which doesn't use window icons.
	 * @param image One or more images. The one closest to the system's desired size will be scaled. Good sizes include 16x16,
	 *           32x32 and 48x48. Pixmap format {@link com.badlogic.gdx.graphics.Pixmap.Format#RGBA8888 RGBA8888} is preferred so
	 *           the images will not have to be copied and converted. The chosen image is copied, and the provided Pixmaps are not
	 *           disposed. */
	public void setIcon (Pixmap... image) {
		setIcon(windowHandle, image);
	}

	static void setIcon (long windowHandle, String[] imagePaths, Files.FileType imageFileType) {
		if (SharedLibraryLoader.os == Os.MacOsX) return;

		Pixmap[] pixmaps = new Pixmap[imagePaths.length];
		for (int i = 0; i < imagePaths.length; i++) {
			pixmaps[i] = new Pixmap(Gdx.files.getFileHandle(imagePaths[i], imageFileType));
		}

		setIcon(windowHandle, pixmaps);

		for (Pixmap pixmap : pixmaps) {
			pixmap.dispose();
		}
	}

	static void setIcon (long windowHandle, Pixmap[] images) {
		if (SharedLibraryLoader.os == Os.MacOsX) return;

		SDL_Surface icon = null;
		Pixmap[] tmpPixmaps = new Pixmap[images.length];

		for (int i = 0; i < images.length; i++) {
			Pixmap pixmap = images[i];

			if (pixmap.getFormat() != Pixmap.Format.RGBA8888) {
				Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
				rgba.setBlending(Pixmap.Blending.None);
				rgba.drawPixmap(pixmap, 0, 0);
				tmpPixmaps[i] = rgba;
				pixmap = rgba;
			}

			try (SDL_Surface surface = SDLSurface.SDL_CreateSurfaceFrom(pixmap.getWidth(), pixmap.getHeight(),
				SDLPixels.SDL_PIXELFORMAT_RGBA8888, pixmap.getPixels(), pixmap.getWidth())) {
				if (surface == null) {
					Lwjgl3ApplicationConfiguration.errorStream.println(SDLError.SDL_GetError());
				} else {
					if (icon == null) {
						icon = surface;
					} else {
						Lwjgl3ApplicationConfiguration.checkError(SDLSurface.SDL_AddSurfaceAlternateImage(icon, surface));
					}
				}
			}
		}

		if (icon != null) {
			Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_SetWindowIcon(windowHandle, icon));
		}

		for (Pixmap pixmap : tmpPixmaps) {
			if (pixmap != null) {
				pixmap.dispose();
			}
		}

	}

	public void setTitle (CharSequence title) {
		Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_SetWindowTitle(windowHandle, title));
	}

	/** Sets minimum and maximum size limits for the window. If the window is full screen or not resizable, these limits are
	 * ignored. Use -1 to indicate an unrestricted dimension. */
	public void setSizeLimits (int minWidth, int minHeight, int maxWidth, int maxHeight) {
		setSizeLimits(windowHandle, minWidth, minHeight, maxWidth, maxHeight);
	}

	static void setSizeLimits (long windowHandle, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		Lwjgl3ApplicationConfiguration
			.checkError(SDLVideo.SDL_SetWindowMinimumSize(windowHandle, Math.max(minWidth, 0), Math.max(minHeight, 0)));
		Lwjgl3ApplicationConfiguration
			.checkError(SDLVideo.SDL_SetWindowMaximumSize(windowHandle, Math.max(maxWidth, 0), Math.max(maxHeight, 0)));
	}

	Lwjgl3Graphics getGraphics () {
		return graphics;
	}

	Lwjgl3Input getInput () {
		return input;
	}

	public long getWindowHandle () {
		return windowHandle;
	}

	void updateCursor () {
		if (currentCursor != previousCursor) {
			SDLMouse.SDL_SetCursor(currentCursor);
			previousCursor = currentCursor;
		}
	}

	void windowHandleChanged (long windowHandle) {
		this.windowHandle = windowHandle;
		input.windowHandleChanged(windowHandle);
	}

	boolean update () {
		if (!listenerInitialized) {
			initializeListener();
		}
		synchronized (runnables) {
			executedRunnables.addAll(runnables);
			runnables.clear();
		}
		for (Runnable runnable : executedRunnables) {
			runnable.run();
		}
		boolean shouldRender = executedRunnables.size > 0 || graphics.isContinuousRendering();
		executedRunnables.clear();

		if (!iconified) input.update();

		synchronized (this) {
			shouldRender |= requestRendering && !iconified;
			requestRendering = false;
		}

		if (isMouseInside) updateCursor();

		if (shouldRender) {
			graphics.update();
			listener.render();
			Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_GL_SwapWindow(windowHandle));
		}

		if (!iconified) input.prepareNext();

		return shouldRender;
	}

	void requestRendering () {
		synchronized (this) {
			this.requestRendering = true;
		}
	}

	boolean shouldClose () {
		return shouldClose;
	}

	Lwjgl3ApplicationConfiguration getConfig () {
		return config;
	}

	boolean isListenerInitialized () {
		return listenerInitialized;
	}

	void initializeListener () {
		if (!listenerInitialized) {
			listener.create();
			listener.resize(graphics.getWidth(), graphics.getHeight());
			listenerInitialized = true;
		}
	}

	void makeCurrent () {
		Gdx.graphics = graphics;
		Gdx.gl32 = graphics.getGL32();
		Gdx.gl31 = Gdx.gl32 != null ? Gdx.gl32 : graphics.getGL31();
		Gdx.gl30 = Gdx.gl31 != null ? Gdx.gl31 : graphics.getGL30();
		Gdx.gl20 = Gdx.gl30 != null ? Gdx.gl30 : graphics.getGL20();
		Gdx.gl = Gdx.gl20;
		Gdx.input = input;

		Lwjgl3ApplicationConfiguration.checkError((SDLVideo.SDL_GL_MakeCurrent(windowHandle, glContext)));
	}

	@Override
	public void dispose () {
		listener.pause();
		listener.dispose();
		Lwjgl3Cursor.dispose(this);
		graphics.dispose();
		input.dispose();
		SDLVideo.SDL_DestroyWindow(windowHandle);
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)(windowHandle ^ (windowHandle >>> 32));
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Lwjgl3Window other = (Lwjgl3Window)obj;
		if (windowHandle != other.windowHandle) return false;
		return true;
	}

	public void flash () {
		Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_FlashWindow(windowHandle, SDLVideo.SDL_FLASH_UNTIL_FOCUSED));
	}

	public enum ProgressState {
		None(SDLVideo.SDL_PROGRESS_STATE_NONE), Invalid(SDLVideo.SDL_PROGRESS_STATE_INVALID), Indeterminate(
			SDLVideo.SDL_PROGRESS_STATE_INDETERMINATE), Normal(SDLVideo.SDL_PROGRESS_STATE_NORMAL), Paused(
				SDLVideo.SDL_PROGRESS_STATE_PAUSED), Error(SDLVideo.SDL_PROGRESS_STATE_ERROR);

		final int sdlState;

		ProgressState (int sdlState) {
			this.sdlState = sdlState;
		}
	}

	public void setProgressState (ProgressState state) {
		Lwjgl3ApplicationConfiguration.checkError(SDLVideo.SDL_SetWindowProgressState(windowHandle, state.sdlState));
	}

	public void setProgress (float value) {
		Lwjgl3ApplicationConfiguration.checkError((SDLVideo.SDL_SetWindowProgressValue(windowHandle, value)));
	}

	public ProgressState getProgressState () {
		switch (SDLVideo.SDL_GetWindowProgressState(windowHandle)) {
		case SDLVideo.SDL_PROGRESS_STATE_NONE:
			return ProgressState.None;
		case SDLVideo.SDL_PROGRESS_STATE_INDETERMINATE:
			return ProgressState.Indeterminate;
		case SDLVideo.SDL_PROGRESS_STATE_NORMAL:
			return ProgressState.Normal;
		case SDLVideo.SDL_PROGRESS_STATE_PAUSED:
			return ProgressState.Paused;
		case SDLVideo.SDL_PROGRESS_STATE_ERROR:
			return ProgressState.Error;
		default:
			return ProgressState.Invalid;
		}
	}

	public float getProgress () {
		return SDLVideo.SDL_GetWindowProgressValue(windowHandle);
	}
}
