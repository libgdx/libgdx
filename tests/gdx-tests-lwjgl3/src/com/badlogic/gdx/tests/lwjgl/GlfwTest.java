
package com.badlogic.gdx.tests.lwjgl;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class GlfwTest {
	private static long windowHandle;
	private static boolean isFullscreen = true;
	private static int width = 640;
	private static int height = 480;
	private static char lastChar = 0;
	private static Runnable runnable = null;
	private static GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);

	static GLFWCharCallback charCallback = new GLFWCharCallback() {
		@Override
		public void invoke (long window, int codepoint) {
			lastChar = (char)codepoint;
		}
	};
	
	public static void main (String[] argv) {
		GLFW.glfwSetErrorCallback(errorCallback);
		if (glfwInit() != GLFW_TRUE) {
			System.out.println("Couldn't initialize GLFW");
			System.exit(-1);
		}

		toggleFullscreen();				

		while (glfwWindowShouldClose(windowHandle) != GLFW_TRUE) {						

			GL11.glViewport(0, 0, width, height);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glRotatef(1f, 0, 0, 1);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glVertex2f(-1f, -1f);
			GL11.glVertex2f(1f, -1f);
			GL11.glVertex2f(0, 1f);
			GL11.glEnd();
			glfwSwapBuffers(windowHandle);
			glfwPollEvents();
			
			if(lastChar == 'f') {
				toggleFullscreen();
				lastChar = 0;
			}
		}

		glfwDestroyWindow(windowHandle);		
		glfwTerminate();
		charCallback.release();
	}
	
	private static void toggleFullscreen() {
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);		
		
		long oldWindowHandle = windowHandle;
		if(isFullscreen) {
			windowHandle = glfwCreateWindow(640, 480, "Test", 0, oldWindowHandle);
			width = 640;
			height = 480;
		} else {
			// fullscreen, current resolution, works
//			GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//			glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
//			width = mode.width();
//			height = mode.height();			
//			windowHandle = glfwCreateWindow(width, height, "Test", glfwGetPrimaryMonitor(), oldWindowHandle);
			
			// fake fullscreen, kinda works, but shows
			// menu bar on Mac OS X
//			glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
//			GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());			
//			width = mode.width();
//			height = mode.height();				
//			windowHandle = glfwCreateWindow(width, height, "Test", 0, oldWindowHandle);			
			
			// fullscreen, not current resolution, fails
			Buffer modes = glfwGetVideoModes(glfwGetPrimaryMonitor());
			GLFWVidMode mode = null;
			for(int i = 0; i < modes.limit(); i++) {
				mode = modes.get(i);
				if(mode.width() == 1024) {
					break;
				}
			}
			width = mode.width();
			height = mode.height();			
			glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
			windowHandle = glfwCreateWindow(width, height, "Test", glfwGetPrimaryMonitor(), oldWindowHandle);
		}
		if (windowHandle == 0) {
			throw new RuntimeException("Couldn't create window");
		}
		if(oldWindowHandle != 0) {
			glfwDestroyWindow(oldWindowHandle);
		}
		glfwSetCharCallback(windowHandle, charCallback);
		glfwMakeContextCurrent(windowHandle);
		GL.createCapabilities();
		glfwSwapInterval(1);
		glfwShowWindow(windowHandle);
		isFullscreen = !isFullscreen;
	}
}
