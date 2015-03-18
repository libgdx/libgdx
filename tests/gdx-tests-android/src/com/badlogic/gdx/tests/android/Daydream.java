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

import android.annotation.TargetApi;
import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidDaydream;
import com.badlogic.gdx.tests.MeshShaderTest;

@TargetApi(17)
public class Daydream extends AndroidDaydream {
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		Log.i("Andrew", "hi");

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();

		setInteractive(false);

		ApplicationListener app = new MeshShaderTest();
		initialize(app, cfg);
	}
}