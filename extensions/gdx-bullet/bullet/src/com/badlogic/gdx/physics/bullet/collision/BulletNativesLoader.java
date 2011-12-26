package com.badlogic.gdx.physics.bullet.collision;

import static com.badlogic.gdx.utils.GdxNativesLoader.extractLibrary;
import static com.badlogic.gdx.utils.GdxNativesLoader.isLinux;
import static com.badlogic.gdx.utils.GdxNativesLoader.isMac;
import static com.badlogic.gdx.utils.GdxNativesLoader.isWindows;

import java.lang.reflect.Method;

import com.badlogic.gdx.utils.GdxNativesLoader;

public class BulletNativesLoader {
		static public boolean load = true;
		static {
			try {
				Method method = Class.forName("javax.jnlp.ServiceManager").getDeclaredMethod("lookup", new Class[] {String.class});
				method.invoke(null, "javax.jnlp.PersistenceService");
				load = false;
			} catch (Throwable ex) {
				load = true;
			}
		}

		static void load () {
			if (GdxNativesLoader.disableNativesLoading) return;
			if (!load) return;
			String path = null;
			if (isWindows) {
				path = extractLibrary("bullet.dll", "bullet-64.dll");
			} else if (isMac) {
				path = extractLibrary("bullet.dylib", "bullet.dylib");
			} else if (isLinux) {
				path = extractLibrary("libbullet.so", "libbullet-64.so");
			}
			if (path != null) System.load(path);
			load = false;
		}
}
