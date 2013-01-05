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
package com.badlogic.gdx.tests.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.WaterRipples;

public class LiveWallpaper extends AndroidLiveWallpaperService {
	@Override
	public ApplicationListener createListener (boolean isPreview) {
		return new MeshShaderTest();
	}

	@Override
	public AndroidApplicationConfiguration createConfig () {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = true;
		return config;
	}

	@Override
	public void offsetChange (ApplicationListener listener, float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
		int xPixelOffset, int yPixelOffset) {
		Gdx.app.log("LiveWallpaper", "offset changed: " + xOffset + ", " + yOffset);
	}
}