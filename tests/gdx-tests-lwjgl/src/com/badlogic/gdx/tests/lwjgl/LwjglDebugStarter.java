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

package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.extensions.ControllersTest;
import com.badlogic.gdx.tests.extensions.FreeTypeMetricsTest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;

public class LwjglDebugStarter {
	public static void main (String[] argv) {
		// this is only here for me to debug native code faster
//		new SharedLibraryLoader("../../extensions/gdx-audio/libs/gdx-audio-natives.jar").load("gdx-audio");
//		new SharedLibraryLoader("../../extensions/gdx-image/libs/gdx-image-natives.jar").load("gdx-image");
//		new SharedLibraryLoader("../../extensions/gdx-freetype/libs/gdx-freetype-natives.jar").load("gdx-freetype");
//		new SharedLibraryLoader("../../extensions/gdx-controllers/gdx-controllers-desktop/libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");
//		new SharedLibraryLoader("../../gdx/libs/gdx-natives.jar").load("gdx");

		GdxTest test = new DragAndDropTest();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.r = config.g = config.b = config.a = 8;
//		config.width = 320;
//		config.height = 241;
		config.width = 960;
		config.height = 600;
//		config.width = 1920;
//		config.height = 1080;
//		config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		new LwjglApplication(test, config);
	}
}
