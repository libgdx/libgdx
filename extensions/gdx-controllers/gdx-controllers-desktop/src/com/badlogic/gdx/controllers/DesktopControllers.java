
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.lwjgl.opengl.Display;

public class DesktopControllers {
	public static void main (String[] args) throws Exception {
		new LwjglFrame(new ApplicationAdapter() {
			public void create () {
				System.out.println(getWindowHandle());
			}

			long getWindowHandle () {
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
		}, "meow", 200, 200, true);
	}
}
