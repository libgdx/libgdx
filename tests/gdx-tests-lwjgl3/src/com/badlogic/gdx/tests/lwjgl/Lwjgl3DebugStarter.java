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

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.tests.BulletTestCollection;
import com.badlogic.gdx.tests.DeltaTimeTest;
import com.badlogic.gdx.tests.LifeCycleTest;
import com.badlogic.gdx.tests.StageTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.bullet.BulletTest;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Lwjgl3DebugStarter {
	public static void main (String[] argv) {	
		GdxTest test = new UITest();
		DisplayMode mode = Lwjgl3ApplicationConfiguration.getDesktopDisplayMode();		
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setFromDisplayMode(mode);
		new Lwjgl3Application(test, config);
	}
}
