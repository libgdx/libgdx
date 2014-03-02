/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.jglfw.tests;

import com.badlogic.gdx.backends.jglfw.JglfwApplication;
import com.badlogic.gdx.backends.jglfw.JglfwApplicationConfiguration;
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

		GdxTest test = new SuperKoalio();
		JglfwApplicationConfiguration config = new JglfwApplicationConfiguration();
		config.vSync = true;
		new JglfwApplication(test, config);
	}
}