
package com.badlogic.gdx.jglfw.tests;

import com.badlogic.gdx.backends.jglfw.JglfwApplication;
import com.badlogic.gdx.backends.jglfw.JglfwApplicationConfiguration;
import com.badlogic.gdx.tests.FullscreenTest;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.VertexArrayTest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class JglfwDebugStarter {
	public static void main (String[] argv) {
		// this is only here for me to debug native code faster
		new SharedLibraryLoader("../../extensions/gdx-audio/libs/gdx-audio-natives.jar").load("gdx-audio");
		new SharedLibraryLoader("../../extensions/gdx-image/libs/gdx-image-natives.jar").load("gdx-image");
		new SharedLibraryLoader("../../extensions/gdx-freetype/libs/gdx-freetype-natives.jar").load("gdx-freetype");
		new SharedLibraryLoader("../../extensions/gdx-controllers/gdx-controllers-desktop/libs/gdx-controllers-desktop-natives.jar")
			.load("gdx-controllers-desktop");
		new SharedLibraryLoader("../../gdx/libs/gdx-natives.jar").load("gdx");

		GdxTest test = new FullscreenTest();
		JglfwApplicationConfiguration config = new JglfwApplicationConfiguration();
		config.useGL20 = test.needsGL20();
		new JglfwApplication(test, config);
	}
}
