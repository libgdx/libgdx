/*******************************************************************************
 * Copyright 2010 mzechner
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.android;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class LagTest extends Activity implements Renderer {
	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		GLSurfaceView view = new GLSurfaceView(this);
		view.setRenderer(this);
		setContentView(view);
	}

	long startTime = 0;
	int frames;

	@Override public void onDrawFrame (GL10 gl) {
		frames++;
		if (System.nanoTime() - startTime > 1000000000) {
			Log.d("Lag Test", "fps: " + frames);
			startTime = System.nanoTime();
			frames = 0;
		}
	}

	@Override public void onSurfaceChanged (GL10 gl, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override public void onSurfaceCreated (GL10 gl, EGLConfig config) {
		startTime = System.nanoTime();
		frames = 0;
	}
}
