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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tests.Bresenham2Test;
import com.badlogic.gdx.tests.DelaunayTriangulatorTest;
import com.badlogic.gdx.tests.EarClippingTriangulatorTest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.MipMapTest;
import com.badlogic.gdx.tests.TextureAtlasTest;
import com.badlogic.gdx.tests.TimerTest;
import com.badlogic.gdx.tests.g3d.Basic3DSceneTest;
import com.badlogic.gdx.tests.g3d.Basic3DTest;
import com.badlogic.gdx.tests.g3d.FogTest;
import com.badlogic.gdx.tests.g3d.MaterialTest;
import com.badlogic.gdx.tests.g3d.ModelLoaderTest;
import com.badlogic.gdx.tests.g3d.ModelTest;
import com.badlogic.gdx.tests.g3d.voxel.VoxelTest;
import com.badlogic.gdx.tests.utils.GdxTest;

public class LwjglDebugStarter {
	public static void main (String[] argv) {
		// this is only here for me to debug native code faster
//		new SharedLibraryLoader("../../extensions/gdx-audio/libs/gdx-audio-natives.jar").load("gdx-audio");
//		new SharedLibraryLoader("../../extensions/gdx-image/libs/gdx-image-natives.jar").load("gdx-image");
//		new SharedLibraryLoader("../../extensions/gdx-freetype/libs/gdx-freetype-natives.jar").load("gdx-freetype");
//		new SharedLibraryLoader("../../extensions/gdx-controllers/gdx-controllers-desktop/libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");
//		new SharedLibraryLoader("../../gdx/libs/gdx-natives.jar").load("gdx");

		GdxTest test = new FogTest();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.useGL20 = test.needsGL20();
		config.width = 1024;
		config.height = 768;
		new LwjglApplication(test, config);
	}
}
