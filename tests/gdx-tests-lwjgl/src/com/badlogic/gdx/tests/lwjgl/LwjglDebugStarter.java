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
import com.badlogic.gdx.tests.AssetManagerTest;
import com.badlogic.gdx.tests.Box2DInitialOverlapTest;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.Box2DTestCollection;
import com.badlogic.gdx.tests.EdgeDetectionTest;
import com.badlogic.gdx.tests.GroupCullingTest;
import com.badlogic.gdx.tests.PixmapTest;
import com.badlogic.gdx.tests.SoundTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.MD5Test;
import com.badlogic.gdx.tests.StbTrueTypeTest;
import com.badlogic.gdx.tests.TiledMapTest;
import com.badlogic.gdx.tests.VorbisTest;
import com.badlogic.gdx.tests.utils.GdxTest;

public class LwjglDebugStarter {
	public static void main (String[] argv) {
		GdxTest test = new PixmapTest();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.useGL20 = test.needsGL20();
		config.vSyncEnabled = true;
		new LwjglApplication(test, config);
	}
}
