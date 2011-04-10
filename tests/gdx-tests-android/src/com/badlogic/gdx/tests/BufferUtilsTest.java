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
package com.badlogic.gdx.tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.Activity;
import android.os.Bundle;

import com.badlogic.gdx.utils.BufferUtils;

public class BufferUtilsTest extends Activity {
	static {
		System.loadLibrary("gdx");
	}

	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);

		ByteBuffer buffer = ByteBuffer.allocateDirect(6 * 5000 * (2 + 4 + 2));
		buffer.order(ByteOrder.nativeOrder());
		float[] test = new float[4000];

		BufferUtils.copy(test, buffer, 4000, 0);

	}
}
