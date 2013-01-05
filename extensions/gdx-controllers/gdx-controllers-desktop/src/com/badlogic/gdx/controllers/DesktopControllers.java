
package com.badlogic.gdx.controllers;

import java.awt.Frame;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class DesktopControllers {


	public static void main (String[] args) throws Exception {
		DesktopControllersBuild.main(null);
		new SharedLibraryLoader("libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");

		ApplicationAdapter app = new ApplicationAdapter() {
			Ois ois;
			
			@Override
			public void create () {
				this.ois = new Ois();
			}
			
			public void render() {
				ois.update();
			}
		};

		new LwjglApplication(app);
//		new LwjglFrame(app, "Controllers", 200, 200, false);
		
//		final JFrame frame = new JFrame("FrameDemo");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.pack();
//		frame.setVisible(true);
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run () {
//				OisWrapper.initialize(getWindowId(frame));
//			}
//		});
	}
}
