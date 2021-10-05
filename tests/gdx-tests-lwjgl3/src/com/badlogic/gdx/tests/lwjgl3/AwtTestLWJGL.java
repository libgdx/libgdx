
package com.badlogic.gdx.tests.lwjgl3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.badlogic.gdx.backends.lwjgl3.awt.GlfwAWTLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
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
						JFrame frame = new JFrame("test");
						frame.setSize(640, 480);
						frame.setLocationRelativeTo(null);

						JButton button = new JButton("Try ImageIO");
						frame.getContentPane().add(button, BorderLayout.SOUTH);

						button.addActionListener( (event) -> {
							try {
								BufferedImage image = ImageIO.read(new URL("http://n4te.com/x/2586-tiNN.jpg").openStream());
								frame.getContentPane().add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
								frame.getContentPane().revalidate();
							} catch (IOException ex) {
								throw new RuntimeException(ex);
							}
						});

						frame.setVisible(true);
					}
				});
			}
		}
	};

	public static void main (String[] args) throws Exception {
		if (SharedLibraryLoader.isMac) {
			Configuration.GLFW_CHECK_THREAD0.set(false);
			Configuration.GLFW_LIBRARY_NAME.set(GlfwAWTLoader.load().getAbsolutePath());
		}

		if (!glfwInit()) {
			System.out.println("Couldn't initialize GLFW");
			System.exit(-1);
		}
		final long window = glfwCreateWindow(640, 480, "Test", 0, 0);
		if (window == 0) {
			throw new RuntimeException("Couldn't create window");
		}
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		glfwSwapInterval(0);
		glfwSetMouseButtonCallback(window, callback);

		while (!glfwWindowShouldClose(window)) {
			GL11.glViewport(0, 0, 640, 480);
			GL11.glClearColor(1, 0, 1, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glRotatef(0.1f, 0, 0, 1);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glVertex2f(-0.5f, -0.5f);
			GL11.glVertex2f(0.5f, -0.5f);
			GL11.glVertex2f(0, 0.5f);
			GL11.glEnd();
			glfwPollEvents();
			glfwSwapBuffers(window);
		}

		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
