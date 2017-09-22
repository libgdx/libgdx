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

import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;
import com.badlogic.gdx.backends.android.AndroidWallpaperListener;
import com.badlogic.gdx.tests.MeshShaderTest;

public class LiveWallpaper extends AndroidLiveWallpaperService {
	
	@Override
	public void onCreateApplication () {
		super.onCreateApplication();
		
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		
		ApplicationListener listener = new MyLiveWallpaperListener();
		initialize(listener, config);
	}
	
	// implement AndroidWallpaperListener additionally to ApplicationListener 
	// if you want to receive callbacks specific to live wallpapers
	public static class MyLiveWallpaperListener extends MeshShaderTest implements AndroidWallpaperListener {
		
		@Override
		public void offsetChange (float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
			int yPixelOffset) {
			Log.i("LiveWallpaper test", "offsetChange(xOffset:"+xOffset+" yOffset:"+yOffset+" xOffsetSteep:"+xOffsetStep+" yOffsetStep:"+yOffsetStep+" xPixelOffset:"+xPixelOffset+" yPixelOffset:"+yPixelOffset+")");
		}

		@Override
		public void previewStateChange (boolean isPreview) {
			Log.i("LiveWallpaper test", "previewStateChange(isPreview:"+isPreview+")");
		}

		@Override
		public void iconDropped (int x, int y) {
			Log.i("LiveWallpaper test", "iconDropped (" + x + ", " + y + ")");
		}
		
		
	}
}