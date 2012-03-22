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
import com.badlogic.gdx.tests.ActionSequenceTest;
import com.badlogic.gdx.tests.ActionTest;
import com.badlogic.gdx.tests.AssetManagerTest;
import com.badlogic.gdx.tests.AtlasIssueTest;
import com.badlogic.gdx.tests.BitmapFontAlignmentTest;
import com.badlogic.gdx.tests.Box2DCharacterControllerTest;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.Box2DTestCollection;
import com.badlogic.gdx.tests.ComplexActionTest;
import com.badlogic.gdx.tests.CustomShaderSpriteBatchTest;
import com.badlogic.gdx.tests.DecalTest;
import com.badlogic.gdx.tests.ImageScaleTest;
import com.badlogic.gdx.tests.ImageTest;
import com.badlogic.gdx.tests.LabelTest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.ParticleEmitterTest;
import com.badlogic.gdx.tests.PixelsPerInchTest;
import com.badlogic.gdx.tests.PixmapBlendingTest;
import com.badlogic.gdx.tests.ShaderMultitextureTest;
import com.badlogic.gdx.tests.SoundTest;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.SpriteCacheOffsetTest;
import com.badlogic.gdx.tests.VertexBufferObjectShaderTest;
import com.badlogic.gdx.tests.gwt.GwtBinaryTest;
import com.badlogic.gdx.tests.gwt.GwtTestWrapper;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class LwjglDebugStarter {
	public static void main (String[] argv) {
		// this is only here for me to debug native code faster
		new SharedLibraryLoader("../../gdx/libs/gdx-natives.jar").load("gdx");
		new SharedLibraryLoader("../../extensions/gdx-audio/libs/gdx-audio-natives.jar").load("gdx-audio");
		new SharedLibraryLoader("../../extensions/gdx-stb-truetype/libs/gdx-stb-truetype-natives.jar").load("gdx-stb-truetype");
		new SharedLibraryLoader("../../extensions/gdx-image/libs/gdx-image-natives.jar").load("gdx-image");
		new SharedLibraryLoader("../../extensions/gdx-freetype/libs/gdx-freetype-natives.jar").load("gdx-freetype");
		
		GdxTest test = new SoundTest();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 480;
		config.useGL20 = test.needsGL20();
		config.vSyncEnabled = true;
		config.resizable = true;
		new LwjglApplication(test, config);
	}
}
