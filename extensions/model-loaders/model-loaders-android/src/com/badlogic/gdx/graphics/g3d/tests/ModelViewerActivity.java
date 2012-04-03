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

package com.badlogic.gdx.graphics.g3d.tests;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.g3d.experimental.HybridLightTest;
import com.badlogic.gdx.graphics.g3d.test.KeyframedModelViewer;
import com.badlogic.gdx.graphics.g3d.test.StillModelViewerGL20;
import com.badlogic.gdx.graphics.g3d.test.Viewer;

public class ModelViewerActivity extends AndroidApplication {
	/** Called when the activity is first created. */
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		config.useWakelock = true;
		config.useGL20 = true;

// initialize(new QbobViewer(), config);
// initialize(new KeyframedModelViewer("data/models/knight.md2", "data/models/knight.jpg"), config);
// initialize(new SkeletonModelViewer("data/ninja.mesh.xml", "data/ninja.jpg"), config);
// initialize(new StillModelViewerGL20("data/models/basicscene.obj", "data/multipleuvs_1.png", "data/multipleuvs_2.png"),
// config);
		initialize(new HybridLightTest(), config);
		// initialize(new Viewer(), config);
	}
}
