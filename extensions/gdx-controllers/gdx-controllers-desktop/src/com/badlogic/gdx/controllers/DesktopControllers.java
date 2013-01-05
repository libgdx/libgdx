
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
	private static long getWindowHandle () {
		try {
			Method getImplementation = Display.class.getDeclaredMethod("getImplementation", new Class[0]);
			getImplementation.setAccessible(true);
			Object display = getImplementation.invoke(null, (Object[])null);
			String fieldName = System.getProperty("os.name").toLowerCase().contains("windows") ? "hwnd" : "parent_window";
			Field field = display.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Long)field.get(display);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Unable to get window handle.", ex);
		}
	}
	
   protected static long getWindowId(Frame frame) {

      try {
          // The reflection code below does the same as this
          // long handle = frame.getPeer() != null ? ((WComponentPeer) frame.getPeer()).getHWnd() : 0;
          Object wComponentPeer = invokeMethod(frame, "getPeer");
          Long hwnd = (Long) invokeMethod(wComponentPeer, "getHWnd");
          return hwnd;

      } catch (Exception ex) {
      	ex.printStackTrace();
      }

      return 0;
  }

  protected static Object invokeMethod(Object o, String methodName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

      Class c = o.getClass();
      for (Method m : c.getMethods()) {
          if (m.getName().equals(methodName)) {
              Object ret = m.invoke(o);
              return ret;
          }
      }
      throw new RuntimeException("Could not find method named '"+methodName+"' on class " + c);

  }

	public static void main (String[] args) throws Exception {
		DesktopControllersBuild.main(null);
		new SharedLibraryLoader("libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");

		ApplicationAdapter app = new ApplicationAdapter() {

			@Override
			public void create () {
				OisWrapper.initialize(OisWrapper.invisibleWindowHack());
			}
		};

//		new LwjglApplication(app);
		new LwjglFrame(app, "Controllers", 200, 200, false);
		
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
