
package com.badlogic.gdx.tests.sdl3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.sdl.*;

public class AwtTestLWJGL {
	private static void callback (long window, int button, boolean down) {
		if (down) {
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

	public static void main (String[] args) throws Exception {
		java.awt.EventQueue.invokeAndWait(new Runnable() {
			public void run () {
				java.awt.Toolkit.getDefaultToolkit();
			}
		});

		if (!SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO)) {
			System.out.println("Couldn't initialize SDL");
			System.exit(-1);
		}
		final long window = SDLVideo.SDL_CreateWindow("Test", 640, 480, SDLVideo.SDL_WINDOW_OPENGL);
		if (window == 0) {
			System.err.println(SDLError.SDL_GetError());
			throw new RuntimeException("Couldn't create window");
		}
		long context = SDLVideo.SDL_GL_CreateContext(window);
		if (context == 0) {
			System.err.println(SDLError.SDL_GetError());
			throw new RuntimeException("Couldn't create GL context");
		}
		SDLVideo.SDL_GL_MakeCurrent(window, context);
		GL.createCapabilities();
		SDLVideo.SDL_GL_SetSwapInterval(0);

		SDL_Event event = SDL_Event.calloc();
		boolean shouldClose = false;
		while (!shouldClose) {
			GL11.glViewport(0, 0, 640, 480);
			GL11.glClearColor(1, 0, 1, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glRotatef(0.1f, 0, 0, 1);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glVertex2f(-0.5f, -0.5f);
			GL11.glVertex2f(0.5f, -0.5f);
			GL11.glVertex2f(0, 0.5f);
			GL11.glEnd();
			while (SDLEvents.SDL_PollEvent(event)) {
				switch (event.type()) {
				case SDLEvents.SDL_EVENT_QUIT:
				case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED:
					shouldClose = true;
					break;
				case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN:
					callback(SDLVideo.SDL_GetWindowFromID(event.button().windowID()), event.button().button(), true);
				case SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP:
					callback(SDLVideo.SDL_GetWindowFromID(event.button().windowID()), event.button().button(), false);
				}
			}
			SDLVideo.SDL_GL_SwapWindow(window);
		}
		event.free();

		SDLVideo.SDL_DestroyWindow(window);
		SDLInit.SDL_Quit();
	}
}
