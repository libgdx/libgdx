/*
 *******************************************************************************
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
import com.badlogic.gdx.backends.android.WearOsWatchFaceListener;
import com.badlogic.gdx.backends.android.WearOsWatchFaceService;
import com.badlogic.gdx.tests.MeshShaderTest;

public class WearOsWatchFace extends WearOsWatchFaceService {
	@Override
	public void onCreateGdxApplication () {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		ApplicationListener listener = new MyApplication();
		initialize(listener, config);
	}

	/**
	 * Due to the limitation of CPU and other hardware, do NOT run complex test (ex. MeshShapeTest)
	 * on a real watch device. Instead we can run them on a emulator to just test the function.
	 */
	private static class MyApplication extends MeshShaderTest implements WearOsWatchFaceListener {

		@Override
		public void create () {
			log("create");
			super.create();
		}

		@Override
		public void resume () {
			log("resume");
			super.resume();
		}

		@Override
		public void resize (int width, int height) {
			log("resize: w=" + width + ", h=" + height);
			super.resize(width, height);
		}

		@Override
		public void pause () {
			log("pause");
			super.pause();
		}

		@Override
		public void dispose () {
			log("dispose");
			super.dispose();
		}

		@Override
		public void onAmbientModeChanged (boolean inAmbientMode) {
			log("onAmbientModeChanged, inAmbientMode=" + inAmbientMode);
		}
	}

	private static void log (String s) {
		Log.i("WatchFaceTest", s + ", " + Thread.currentThread().getName());
	}
}
