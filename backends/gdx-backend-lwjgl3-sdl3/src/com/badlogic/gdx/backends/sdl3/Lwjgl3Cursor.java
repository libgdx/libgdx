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

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.sdl.*;

public class Lwjgl3Cursor implements Cursor {
	static final Array<Lwjgl3Cursor> cursors = new Array<Lwjgl3Cursor>();
	static final Map<SystemCursor, Long> systemCursors = new HashMap<SystemCursor, Long>();

	private static int inputModeBeforeNoneCursor = -1;

	final Lwjgl3Window window;
	Pixmap pixmapCopy;
	SDL_Surface sdlSurface;
	final long sdlCursor;

	Lwjgl3Cursor (Lwjgl3Window window, Pixmap pixmap, int xHotspot, int yHotspot) {
		this.window = window;
		if (pixmap.getFormat() != Pixmap.Format.RGBA8888) {
			throw new GdxRuntimeException("Cursor image pixmap is not in RGBA8888 format.");
		}

		if ((pixmap.getWidth() & (pixmap.getWidth() - 1)) != 0) {
			throw new GdxRuntimeException(
				"Cursor image pixmap width of " + pixmap.getWidth() + " is not a power-of-two greater than zero.");
		}

		if ((pixmap.getHeight() & (pixmap.getHeight() - 1)) != 0) {
			throw new GdxRuntimeException(
				"Cursor image pixmap height of " + pixmap.getHeight() + " is not a power-of-two greater than zero.");
		}

		if (xHotspot < 0 || xHotspot >= pixmap.getWidth()) {
			throw new GdxRuntimeException(
				"xHotspot coordinate of " + xHotspot + " is not within image width bounds: [0, " + pixmap.getWidth() + ").");
		}

		if (yHotspot < 0 || yHotspot >= pixmap.getHeight()) {
			throw new GdxRuntimeException(
				"yHotspot coordinate of " + yHotspot + " is not within image height bounds: [0, " + pixmap.getHeight() + ").");
		}

		this.pixmapCopy = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
		this.pixmapCopy.setBlending(Blending.None);
		this.pixmapCopy.drawPixmap(pixmap, 0, 0);

		sdlSurface = SDLSurface.SDL_CreateSurfaceFrom(pixmapCopy.getWidth(), pixmapCopy.getHeight(),
			SDLPixels.SDL_PIXELFORMAT_RGBA8888, pixmapCopy.getPixels(), pixmapCopy.getWidth());
		if (sdlSurface != null) {
			sdlCursor = SDLMouse.SDL_CreateColorCursor(sdlSurface, xHotspot, yHotspot);
			cursors.add(this);
		} else {
			sdlCursor = 0;
			Lwjgl3ApplicationConfiguration.errorStream.println(SDLError.SDL_GetError());
		}
	}

	@Override
	public void dispose () {
		if (pixmapCopy == null) {
			throw new GdxRuntimeException("Cursor already disposed");
		}
		cursors.removeValue(this, true);
		pixmapCopy.dispose();
		pixmapCopy = null;
		SDLSurface.SDL_DestroySurface(sdlSurface);
		SDLMouse.SDL_DestroyCursor(sdlCursor);
	}

	static void dispose (Lwjgl3Window window) {
		for (int i = cursors.size - 1; i >= 0; i--) {
			Lwjgl3Cursor cursor = cursors.get(i);
			if (cursor.window.equals(window)) {
				cursors.removeIndex(i).dispose();
			}
		}
	}

	static void disposeSystemCursors () {
		for (long systemCursor : systemCursors.values()) {
			SDLMouse.SDL_DestroyCursor(systemCursor);
		}
		systemCursors.clear();
	}

	static void setSystemCursor (Lwjgl3Window window, SystemCursor systemCursor) {
		if (systemCursor == SystemCursor.None) {
			SDLMouse.SDL_HideCursor();
			return;
		} else {
			SDLMouse.SDL_ShowCursor();
		}
		Long sdlCursor = systemCursors.get(systemCursor);
		if (sdlCursor == null) {
			long handle = 0;
			if (systemCursor == SystemCursor.Arrow) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT);
			} else if (systemCursor == SystemCursor.Crosshair) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR);
			} else if (systemCursor == SystemCursor.Hand) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_POINTER);
			} else if (systemCursor == SystemCursor.HorizontalResize) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE);
			} else if (systemCursor == SystemCursor.VerticalResize) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE);
			} else if (systemCursor == SystemCursor.Ibeam) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_TEXT);
			} else if (systemCursor == SystemCursor.NWSEResize) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_NWSE_RESIZE);
			} else if (systemCursor == SystemCursor.NESWResize) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_NESW_RESIZE);
			} else if (systemCursor == SystemCursor.AllResize) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_MOVE);
			} else if (systemCursor == SystemCursor.NotAllowed) {
				handle = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_NOT_ALLOWED);
			} else {
				throw new GdxRuntimeException("Unknown system cursor " + systemCursor);
			}

			if (handle == 0) {
				return;
			}
			sdlCursor = handle;
			systemCursors.put(systemCursor, sdlCursor);
		}
		window.currentCursor = sdlCursor;
	}
}
