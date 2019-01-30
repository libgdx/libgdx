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

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Color;

public class WindowedTest extends AndroidApplication implements ApplicationListener {
	Color color = new Color(1, 1, 1, 1);

	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);

		Button b1 = new Button(this);
		b1.setText(getResources().getString(R.string.change_color));
		b1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		Button b2 = new Button(this);
		b2.setText(getResources().getString(R.string.new_window));
		b2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		View view = initializeForView(this);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(b1);
		layout.addView(b2);
		layout.addView(view, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		setContentView(layout);

		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick (View arg0) {
				color.set((float)Math.random(), (float)Math.random(), (float)Math.random(), 1);
			}

		});

		b2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick (View v) {
				Intent intent = new Intent(WindowedTest.this, WindowedTest.class);
				WindowedTest.this.startActivity(intent);
			}
		});
	}

	public void onPause () {
		super.onPause();
	}

	@Override
	public void onDestroy () {
		super.onDestroy();
		Log.w("WindowedTest", "destroying");
	}

	@Override
	public void create () {
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(color.r, color.g, color.g, color.a);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

	}

	@Override
	public void dispose () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void resize (int width, int height) {
	}
}
