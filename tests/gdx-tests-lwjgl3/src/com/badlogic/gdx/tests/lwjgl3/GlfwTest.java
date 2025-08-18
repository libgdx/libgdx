
package com.badlogic.gdx.tests.lwjgl3;

import static org.lwjgl.glfw.GLFW.*;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

/** A simple GLFW test that demonstrates window creation, OpenGL context setup, and rendering a triangle. The window is created in
 * fullscreen mode using the primary monitor's video mode.
 * 
 * The program also prints the available video modes for the primary monitor, selects one, and uses it to set the window size. It
 * also prints the framebuffer size if it changes during the window's lifecycle.
 * 
 * It uses the GLFW library for window and input management and OpenGL for rendering. */
public class GlfwTest {
	// Handle to the window created by GLFW
	private static long windowHandle;
	// Error callback to log GLFW errors to the standard error stream
	private static GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);

	/** The main entry point of the application. Initializes GLFW, creates a fullscreen window using the primary monitor's video
	 * mode, sets up the OpenGL context, and enters the rendering loop.
	 * 
	 * @param argv Command-line arguments (not used in this test). */

	public static void main (String[] argv) {
		GLFW.glfwSetErrorCallback(errorCallback);
		if (!glfwInit()) {
			System.out.println("Couldn't initialize GLFW");
			System.exit(-1);
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

		// fullscreen, not current resolution, fails
		Buffer modes = glfwGetVideoModes(glfwGetPrimaryMonitor());
		for (int i = 0; i < modes.limit(); i++) {
			System.out.println(modes.get(i).width() + "x" + modes.get(i).height());
		}
		GLFWVidMode mode = modes.get(7);
		System.out.println("Mode: " + mode.width() + "x" + mode.height());
		windowHandle = glfwCreateWindow(mode.width(), mode.height(), "Test", glfwGetPrimaryMonitor(), 0);
		if (windowHandle == 0) {
			throw new RuntimeException("Couldn't create window");
		}
		glfwMakeContextCurrent(windowHandle);
		GL.createCapabilities();
		glfwSwapInterval(1);
		glfwShowWindow(windowHandle);

		IntBuffer tmp = BufferUtils.createIntBuffer(1);
		IntBuffer tmp2 = BufferUtils.createIntBuffer(1);

		int fbWidth = 0;
		int fbHeight = 0;

		while (!glfwWindowShouldClose(windowHandle)) {
			glfwGetFramebufferSize(windowHandle, tmp, tmp2);
			if (fbWidth != tmp.get(0) || fbHeight != tmp2.get(0)) {
				fbWidth = tmp.get(0);
				fbHeight = tmp2.get(0);
				System.out.println("Framebuffer: " + tmp.get(0) + "x" + tmp2.get(0));
// GL11.glViewport(0, 0, tmp.get(0) * 2, tmp2.get(0) * 2);
			}
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glVertex2f(-1f, -1f);
			GL11.glVertex2f(1f, -1f);
			GL11.glVertex2f(0, 1f);
			GL11.glEnd();
			glfwSwapBuffers(windowHandle);
			glfwPollEvents();
		}

		glfwDestroyWindow(windowHandle);
		glfwTerminate();
	}
}
