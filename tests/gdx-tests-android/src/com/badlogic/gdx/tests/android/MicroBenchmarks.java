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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.badlogic.gdx.utils.BufferUtils;

public class MicroBenchmarks extends Activity {
	final int TRIES = 5;
	long start = 0;
	ScrollView sv;
	TextView tv;
	Thread testThread = new Thread(new Runnable() {

		@Override
		public void run () {
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024 * Float.SIZE / 8);
			buffer.order(ByteOrder.nativeOrder());
			FloatBuffer floatBuffer = buffer.asFloatBuffer();
			IntBuffer intBuffer = buffer.asIntBuffer();

			float[] floatArray = new float[1024 * 1024];
			int[] intArray = new int[1024 * 1024];

			// single put
			tic();
			for (int tries = 0; tries < TRIES; tries++) {
				for (int i = 0; i < floatArray.length; i++)
					floatBuffer.put(floatArray[i]);
				floatBuffer.clear();
			}
			toc("single put");

			// single indexed put
			tic();
			for (int tries = 0; tries < TRIES; tries++) {
				for (int i = 0; i < floatArray.length; i++)
					floatBuffer.put(i, floatArray[i]);
				floatBuffer.clear();
			}
			toc("single indexed put");

			// bulk put
			tic();
			for (int tries = 0; tries < TRIES; tries++) {
				floatBuffer.put(floatArray);
				floatBuffer.clear();
			}
			toc("vector put");

			// convert bulk put
			tic();
			for (int tries = 0; tries < TRIES; tries++) {
				for (int i = 0; i < floatArray.length; i++)
					intArray[i] = Float.floatToIntBits(floatArray[i]);
				intBuffer.put(intArray);
				intBuffer.clear();
			}
			toc("convert bulk put");

			// jni bulk put
			tic();
			for (int tries = 0; tries < TRIES; tries++) {
				BufferUtils.copy(floatArray, floatBuffer, floatArray.length, 0);
				floatBuffer.clear();
			}
			toc("jni bulk put");
		}

	});

	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);

		tv = new TextView(this);
		sv = new ScrollView(this);
		sv.addView(tv);
		setContentView(sv);

		testThread.start();
	}

	void tic () {
		start = System.nanoTime();
	}

	void toc (final String info) {
		final float time = (System.nanoTime() - start) / 1000000000.0f;

		tv.post(new Runnable() {

			@Override
			public void run () {
				StringBuilder buff = new StringBuilder(tv.getText());
				buff.append(info).append(", ").append(time).append(" secs\n");
				tv.setText(buff.toString());
			}
		});

		Log.d("MicroBenchmarks", info + ", " + time);
	}
}
