
package com.badlogic.gdx.tests.lwjgl3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;

import static org.lwjgl.glfw.GLFW.*;
/**
 * A test class that integrates AWT and LWJGL for rendering OpenGL content in a window and displaying a GUI on mouse press.
 * It demonstrates the usage of GLFW for window creation and handling mouse events in combination with AWT for GUI components.
 */
public class AwtTestLWJGL {
	 
    /**
     * A callback that listens for mouse button events. When a mouse button is pressed, it opens a JFrame with an image loading functionality.
     */
	static GLFWMouseButtonCallback callback = new GLFWMouseButtonCallback() {
		@Override
		public void invoke (long window, int button, int action, int mods) {
			// If the mouse button is pressed, trigger the GUI action
			if (action == GLFW_PRESS) {
				System.out.println("Bam");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run () {
						// Create and configure a JFrame
						JFrame frame = new JFrame("test");
						frame.setSize(640, 480);
						frame.setLocationRelativeTo(null);
						// Create and add a button that loads an image on click
						JButton button = new JButton("Try ImageIO");
						frame.getContentPane().add(button, BorderLayout.SOUTH);
						// Add an ActionListener to load and display an image when the button is clicked
						button.addActionListener( (event) -> {
							try {
								// Load an image from a URL and display it in the JFrame
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
/**
     * The main method that initializes the GLFW window, sets up the OpenGL context, and starts the rendering loop.
     * It also listens for mouse button events and handles the rendering of a rotating triangle.
     * 
     * @param args Command line arguments.
     * @throws Exception If there is an error during initialization or rendering.
     */
	public static void main (String[] args) throws Exception {
		// Initialize the AWT toolkit for the current thread
		java.awt.EventQueue.invokeAndWait(new Runnable() {
			public void run () {
				java.awt.Toolkit.getDefaultToolkit();
			}
		});
		 // Set the GLFW library name for MacOS X, if applicable
		if (SharedLibraryLoader.os == Os.MacOsX) {
			Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
		}
		// Initialize GLFW
		if (!glfwInit()) {
			System.out.println("Couldn't initialize GLFW");
			System.exit(-1);
		}
		// Create a window using GLFW
		final long window = glfwCreateWindow(640, 480, "Test", 0, 0);
		if (window == 0) {
			throw new RuntimeException("Couldn't create window");
		}
		// Make the OpenGL context current and initialize OpenGL capabilities
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		glfwSwapInterval(0);
		glfwSetMouseButtonCallback(window, callback);
		// Enter the rendering loop
		while (!glfwWindowShouldClose(window)) {
			// Set the OpenGL viewport and clear the screen with a magenta color
			GL11.glViewport(0, 0, 640, 480);
			GL11.glClearColor(1, 0, 1, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glRotatef(0.1f, 0, 0, 1);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glVertex2f(-0.5f, -0.5f);
			GL11.glVertex2f(0.5f, -0.5f);
			GL11.glVertex2f(0, 0.5f);
			GL11.glEnd();
			// Poll for window events (including mouse events) and swap the buffers
			glfwPollEvents();
			glfwSwapBuffers(window);
		}
		 // Clean up and terminate GLFW
		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
