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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.UBJsonReader;
import com.badlogic.gdx.utils.UBJsonWriter;

public class UBJsonTest extends GdxTest {
	static final String fn = "test.ubjson";
	static final String longString;
	static {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 300; i++)
			sb.append((char)((i % 26) + 'a'));
		longString = sb.toString();
	}

	@Override
	public void create () {
		try {

			UBJsonWriter uw = new UBJsonWriter(Gdx.files.external(fn).write(false));
			uw.object();
			uw.set(longString, longString);
			uw.set("0floats", new float[] {});
			uw.set("3floats", new float[] {1, 2, 3.456789f});
			uw.set("xfloats", new float[] {Float.MIN_VALUE, Float.MAX_VALUE, Float.NaN, Float.NEGATIVE_INFINITY});
			uw.set("double", 0.000000000000000000001);
			uw.set("long", Long.MAX_VALUE);
			uw.set("3bytes", new byte[] {(byte)1, (byte)2, (byte)3});
			uw.set("3shorts", new short[] {(short)1, (short)2, (short)3});
			uw.set("3ints", new int[] {1, 2, 3});
			uw.set("3long", new long[] {1l, 2l, 3l});
			uw.set("3double", new double[] {1, 2, 3.456789});
			uw.set("3char", new char[] {'a', 'b', 'c'});
			uw.set("3strings", new String[] {"", "a", "abc"});
			uw.array("arr");
			uw.object().pop();
			uw.value(true).value(false).value(true);
			uw.value((byte)254);
			uw.value((byte)(-2));
			uw.value((short)-32000);
			uw.value((int)-123456);
			uw.value((long)(-((1 << 63) - 1)));
			uw.pop();
			uw.pop();
			uw.close();
			UBJsonReader ur = new UBJsonReader();
			ur.oldFormat = false;
			JsonValue v = ur.parse(Gdx.files.external(fn));
			Gdx.app.log("UBJsonTest", "result = \n" + v.toString());
			performanceTest();
			Gdx.app.log("UBJsonTest", "Test succeeded");
		} catch (Throwable t) {
			Gdx.app.error("UBJsonTest", "Test failed", t);
		}
	}

	private void performanceTest () throws Exception {
		Gdx.app.log("UBJsonTest", "--- performanceTest ---");
		long start = System.currentTimeMillis();
		UBJsonWriter uw = new UBJsonWriter(Gdx.files.external(fn).write(false, 8192));
		uw.object();
		uw.set("0floats", new float[] {});
		uw.set("3floats", new float[] {1, 2, 3.456789f});
		uw.set("xfloats", new float[] {Float.MIN_VALUE, Float.MAX_VALUE, Float.NaN, Float.NEGATIVE_INFINITY});
		uw.set("double", 0.000000000000000000001);
		uw.set("long", Long.MAX_VALUE);
		uw.array("arr");
		uw.object().pop();
		for (int i = 0; i < 50000; i++) {
			uw.value(true).value(false).value(true);
			uw.value((byte)254);
			uw.value((byte)(-2));
			uw.value((short)-32000);
			uw.value((int)-123456);
			uw.value((long)(-((1 << 63) - 1)));
			uw.value(longString);
		}
		uw.pop();
		uw.pop();
		uw.close();

		Gdx.app.log("UBJsonTest", "Writing the test file took " + (System.currentTimeMillis() - start) + "ms");
		Gdx.app.log("UBJsonTest", "File size is " + Gdx.files.external(fn).length());
		UBJsonReader ur = new UBJsonReader();
		ur.oldFormat = false;
		start = System.currentTimeMillis();
		ur.parse(Gdx.files.external(fn));
		Gdx.app.log("UBJsonTest", "Parsing the test file took " + (System.currentTimeMillis() - start) + "ms");
	}
}
