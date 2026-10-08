
package com.badlogic.gdx.tests.sdl3;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.sdl.*;

public class SDLTest {
	private static long windowHandle;

	public static void main (String[] argv) {
		if (!SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO)) {
			System.err.println(SDLError.SDL_GetError());
			System.out.println("Couldn't initialize SDL");
			System.exit(-1);
		}

		int props = SDLProperties.SDL_CreateProperties();

		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true);
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true);
		SDLProperties.SDL_SetStringProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_TITLE_STRING, "Test");

		// fullscreen, not current resolution, fails
		PointerBuffer modes = SDLVideo.SDL_GetFullscreenDisplayModes(SDLVideo.SDL_GetPrimaryDisplay());
		for (int i = 0; i < modes.limit(); i++) {
			SDL_DisplayMode mode = SDL_DisplayMode.create(modes.get(i));
			System.out.println(mode.w() + "x" + mode.h());
		}
		SDL_DisplayMode m = SDL_DisplayMode.create(modes.get(7)); // Why 7?
		System.out.println("Mode: " + m.w() + "x" + m.h());
		windowHandle = SDLVideo.SDL_CreateWindowWithProperties(props);
		SDLProperties.SDL_DestroyProperties(props);
		if (windowHandle == 0) {
			System.err.println(SDLError.SDL_GetError());
			throw new RuntimeException("Couldn't create window");
		}
		SDLVideo.SDL_SetWindowFullscreen(windowHandle, true);
		SDLVideo.SDL_SetWindowFullscreenMode(windowHandle, m);
		SDLVideo.SDL_SyncWindow(windowHandle);
		long context = SDLVideo.SDL_GL_CreateContext(windowHandle);
		if (context == 0) {
			System.err.println(SDLError.SDL_GetError());
			throw new RuntimeException("Couldn't create GL context");
		}
		if (!SDLVideo.SDL_GL_MakeCurrent(windowHandle, context)) {
			System.err.println(SDLError.SDL_GetError());
		}
		modes.flip();
		SDLStdinc.SDL_free(modes);
		GL.createCapabilities();
		SDLVideo.SDL_GL_SetSwapInterval(1);
		SDLVideo.SDL_ShowWindow(windowHandle);

		IntBuffer tmp = BufferUtils.createIntBuffer(1);
		IntBuffer tmp2 = BufferUtils.createIntBuffer(1);

		int fbWidth = 0;
		int fbHeight = 0;

		SDL_Event event = SDL_Event.calloc();
		boolean shouldClose = false;
		while (!shouldClose) {
			SDLVideo.SDL_GetWindowSizeInPixels(windowHandle, tmp, tmp2);
			if (fbWidth != tmp.get(0) || fbHeight != tmp2.get(0)) {
				fbWidth = tmp.get(0);
				fbHeight = tmp2.get(0);
				System.out.println("Framebuffer: " + fbWidth + "x" + fbHeight);
				GL11.glViewport(0, 0, fbWidth, fbHeight);
			}
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glVertex2f(-1f, -1f);
			GL11.glVertex2f(1f, -1f);
			GL11.glVertex2f(0, 1f);
			GL11.glEnd();
			SDLVideo.SDL_GL_SwapWindow(windowHandle);
			while (SDLEvents.SDL_PollEvent(event)) {
				switch (event.type()) {
				case SDLEvents.SDL_EVENT_QUIT:
				case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED:
					shouldClose = true;
				}
			}
		}
		event.free();

		SDLVideo.SDL_GL_DestroyContext(context);
		SDLVideo.SDL_DestroyWindow(windowHandle);
		SDLInit.SDL_Quit();
	}
}
