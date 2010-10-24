
package com.badlogic.gdx.tests.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.GdxRuntimeException;

public class GdxTestEnumerator {

	public static String[] getTestNames () {
		String packageDirectory = "/com/badlogic/gdx/tests/";
		String packageName = "com.badlogic.gdx.tests.";
		
		URL url = GdxTestEnumerator.class.getResource(packageDirectory);
		File file = new File(url.getFile());
		List<String> tests = new ArrayList<String>();

		if (file.exists()) {
			String[] files = file.list();
			for (String name : files) {
				if (name.endsWith(".class") && !name.contains("$")) {
					name = name.replace(".class", "");

					try {
						Class clazz = Class.forName(packageName + name);
						Class[] interfaces = clazz.getInterfaces();
						for (Class interf : interfaces) {
							if (interf.equals(GdxTest.class)) tests.add(name);
						}

					} catch (Throwable t) {
						// empty catch, yay
					}
				}
			}

			return tests.toArray(new String[tests.size()]);
		}

		return null;
	}

	public static GdxTest newTest (String name) {
		try {
			Class clazz = Class.forName("com.badlogic.gdx.tests." + name);
			return (GdxTest)clazz.newInstance();
		} catch (Throwable t) {
			throw new GdxRuntimeException( "couldn't load test");
		}
	}

	public static void main (String[] argv) {
		String[] tests = GdxTestEnumerator.getTestNames();

		System.out.println(tests.length + " tests");
		System.out.println(Arrays.toString(tests));
	}
}
