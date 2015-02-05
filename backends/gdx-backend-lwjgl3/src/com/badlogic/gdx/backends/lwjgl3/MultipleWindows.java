/*
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package com.badlogic.gdx.backends.lwjgl3;

import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLContext;

import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/** GLFW demo that showcases rendering to multiple windows from a single thread. */
public final class MultipleWindows {

	private MultipleWindows() {
	}

	public static void main(String[] args) {
		Lwjgl3NativesLoader.load();
		GLFWErrorCallback errorfun = errorCallbackPrint(System.err);
		glfwSetErrorCallback(errorfun);
		if ( glfwInit() == 0 )
			throw new IllegalStateException("Failed to initialize GLFW.");

		try {
			demo();
		} finally {
			glfwTerminate();
			errorfun.release();
		}
	}

	private static void demo() {
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

		Window[] windows = new Window[4];

		final AtomicInteger latch = new AtomicInteger(windows.length);

		for ( int i = 0; i < windows.length; i++ ) {
			final int windowIndex = i + 1;

			long handle = glfwCreateWindow(300, 200, "GLFW Demo - " + windowIndex, NULL, NULL);
			if ( handle == 0L )
				throw new IllegalStateException("Failed to create GLFW window");

			Window window = new Window(handle);

			glfwSetCursorEnterCallback(handle, window.cursorenterfun = new GLFWCursorEnterCallback() {
				@Override
				public void invoke(long window, int entered) {
					if ( entered == GL_TRUE )
						System.out.println("Mouse entered window: " + windowIndex);
				}
			});

			glfwSetKeyCallback(handle, window.keyfun = new GLFWKeyCallback() {
				@Override
				public void invoke(long window, int key, int scancode, int action, int mods) {
					if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
						latch.set(0);
				}
			});

			glfwMakeContextCurrent(handle);
			window.context = GLContext.createFromCurrent();

			glClearColor((i & 1), (i >> 1), (i == 1) ? 0.f : 1.f, 0.f);

			glfwShowWindow(handle);
			glfwSetWindowPos(handle, 100 + (i & 1) * 400, 100 + (i >> 1) * 400);

			windows[i] = window;
		}

		while ( latch.get() != 0 ) {
			glfwPollEvents();

			for ( int i = 0; i < 4; i++ ) {
				Window window = windows[i];
				if ( window == null )
					continue;

				glfwMakeContextCurrent(window.handle);
				GL.setCurrent(window.context);

				glClear(GL_COLOR_BUFFER_BIT);
				glfwSwapBuffers(window.handle);

				if ( glfwWindowShouldClose(window.handle) != 0 ) {
					glfwDestroyWindow(window.handle);
					windows[i] = null;

					latch.decrementAndGet();
				}
			}
		}

		for ( int i = 0; i < 4; i++ ) {
			Window window = windows[i];
			if ( window != null )
				glfwDestroyWindow(window.handle);
		}
	}

	private static class Window {
		final long handle;

		GLContext context;

		GLFWCursorEnterCallback cursorenterfun;
		GLFWKeyCallback         keyfun;

		Window(long handle) {
			this.handle = handle;
		}
	}

}