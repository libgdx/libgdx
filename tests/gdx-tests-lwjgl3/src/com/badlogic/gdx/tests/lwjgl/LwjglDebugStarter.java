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

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.tests.utils.GdxTest;

public class LwjglDebugStarter {
	public static void main (String[] argv) {		
		GdxTest test = new GdxTest() {};		
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.r = config.g = config.b = config.a = 8;
		config.width = 960;
		config.height = 600;
		new Lwjgl3Application(test, config);
	}
}
