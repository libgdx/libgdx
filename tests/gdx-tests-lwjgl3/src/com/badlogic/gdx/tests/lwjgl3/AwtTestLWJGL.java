
package com.badlogic.gdx.tests.lwjgl3;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;

import static org.lwjgl.glfw.GLFW.*;

public class AwtTestLWJGL {
	static GLFWMouseButtonCallback callback = new GLFWMouseButtonCallback() {
		@Override
		public void invoke (long window, int button, int action, int mods) {
			if (action == GLFW_PRESS) {
				System.out.println("Bam");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run () {
						final JFileChooser fc = new JFileChooser();
						fc.showOpenDialog(null);
					}
				});
			}			
		}
	};
	

	public static void main (String[] args) throws Exception { 
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				Toolkit.getDefaultToolkit();
				new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

				if (!glfwInit()) {
					System.out.println("Couldn't initialize GLFW");
					System.exit(-1);
				}
				final long window = glfwCreateWindow(640, 480, "Test", 0, 0);
				if (window == 0) {
					throw new RuntimeException("Couldn't create window");
				}
				glfwMakeContextCurrent(window);
				glfwSwapInterval(0);
				glfwSetMouseButtonCallback(window, callback);

				new Runnable() {
					public void run () {
						if (glfwWindowShouldClose(window)) {
							glfwDestroyWindow(window);
							glfwTerminate();
							return;
						}

						GL.createCapabilities();
						GL11.glViewport(0, 0, 640, 480);
						GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
						GL11.glRotatef(0.1f, 0, 0, 1);
						GL11.glBegin(GL11.GL_TRIANGLES);
						GL11.glVertex2f(-0.5f, -0.5f);
						GL11.glVertex2f(0.5f, -0.5f);
						GL11.glVertex2f(0, 0.5f);
						GL11.glEnd();
						glfwPollEvents();
						glfwSwapBuffers(window);
						EventQueue.invokeLater(this);
					}
				}.run();
			}
		});
	}
}
