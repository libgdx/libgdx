
package com.badlogic.gdx.tests.lwjgl;

import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import java.awt.EventQueue;

import javax.sound.midi.Synthesizer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GlfwTest {
	private static long windowHandle;
	private static boolean isFullscreen = false;
	private static Runnable runnable = null;

	static GLFWMouseButtonCallback callback = new GLFWMouseButtonCallback() {
		@Override
		public void invoke (long window, int button, int action, int mods) {				
			if(action == GLFW.GLFW_PRESS) {
				runnable = new Runnable() {
					@Override
					public void run () {
						Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
						config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
						long newHandle = Lwjgl3Application.createGlfwWindow(config, windowHandle);						
						if(newHandle == 0) {
							throw new GdxRuntimeException("fuck");
						}					
						glfwDestroyWindow(windowHandle);
						glfwSetMouseButtonCallback(windowHandle, callback);
						windowHandle = newHandle;
						GLFW.glfwShowWindow(windowHandle);
					}
				};					
			}
		}
	};			

	
	public static void main (String[] argv) {
		if (glfwInit() != GLFW_TRUE) {
			System.out.println("Couldn't initialize GLFW");
			System.exit(-1);
		}
		
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(640, 480);
//		windowHandle = glfwCreateWindow(640, 480, "Test", 0, 0);
		windowHandle = Lwjgl3Application.createGlfwWindow(config, 0);
		GLFW.glfwShowWindow(windowHandle);
		if (windowHandle == 0) {
			throw new RuntimeException("Couldn't create window");
		}
		glfwSwapInterval(0);
		glfwSetMouseButtonCallback(windowHandle, callback);
			
		while (true) {
			if(runnable != null) {
				runnable.run();
				runnable = null;
			}
			
			if (glfwWindowShouldClose(windowHandle) == GLFW_TRUE) {
				glfwDestroyWindow(windowHandle);
				glfwTerminate();
				return;
			}			
			
			glfwMakeContextCurrent(windowHandle);
			GL.createCapabilities();
			
			GL11.glViewport(0, 0, 640, 480);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glRotatef(0.1f, 0, 0, 1);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glVertex2f(-0.5f, -0.5f);
			GL11.glVertex2f(0.5f, -0.5f);
			GL11.glVertex2f(0, 0.5f);
			GL11.glEnd();			
			glfwSwapBuffers(windowHandle);
			glfwPollEvents();
		}
	}
}
