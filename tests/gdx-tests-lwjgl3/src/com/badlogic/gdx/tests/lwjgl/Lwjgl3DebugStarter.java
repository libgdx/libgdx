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
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.tests.BulletTestCollection;
import com.badlogic.gdx.tests.DeltaTimeTest;
import com.badlogic.gdx.tests.LifeCycleTest;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.StageTest;
import com.badlogic.gdx.tests.TextInputDialogTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.bullet.BulletTest;
import com.badlogic.gdx.tests.g3d.Animation3DTest;
import com.badlogic.gdx.tests.g3d.BaseG3dHudTest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class Lwjgl3DebugStarter {
	public static void main (String[] argv) {	
		GdxTest test = new Animation3DTest();
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.width = 900;
		config.height = 600;
		config.vSyncEnabled = false;
		new Lwjgl3Application(test, config);
	}
}
