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
import com.badlogic.gdx.tests.DecalTest;
import com.badlogic.gdx.tests.ETC1Test;
import com.badlogic.gdx.tests.FilterPerformanceTest;
import com.badlogic.gdx.tests.InverseKinematicsTest;
import com.badlogic.gdx.tests.MipMapTest;
import com.badlogic.gdx.tests.ObjTest;
import com.badlogic.gdx.tests.ProjectiveTextureTest;
import com.badlogic.gdx.tests.ShadowMappingTest;
import com.badlogic.gdx.tests.SimpleDecalTest;
import com.badlogic.gdx.tests.TransformationTest;
import com.badlogic.gdx.tests.UITest;

public class LwjglDebugStarter {
	public static void main (String[] argv) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		config.useGL20 = true;
		new LwjglApplication(new SimpleDecalTest(), config);
	}
}
