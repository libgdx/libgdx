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
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;

public class BufferUtilsTest extends GdxTest {
	static final int NUM_MB = 5;

	@Override
	public void create () {
		//Not emulated in gwt
		//ByteBuffer bytebuffer = BufferUtils.newUnsafeByteBuffer(1000 * 1000);
		//BufferUtils.disposeUnsafeByteBuffer(bytebuffer);
		
		ByteBuffer bb = BufferUtils.newByteBuffer(8);
		CharBuffer cb = BufferUtils.newCharBuffer(8);
		ShortBuffer sb = BufferUtils.newShortBuffer(8);
		IntBuffer ib = BufferUtils.newIntBuffer(8);
		LongBuffer lb = BufferUtils.newLongBuffer(8);
		FloatBuffer fb = BufferUtils.newFloatBuffer(8);
		DoubleBuffer db = BufferUtils.newDoubleBuffer(8);

		bb.position(4);
		BufferUtils.copy(new byte[] {1, 2, 3, 4}, 0, bb, 4);
		checkInt(bb.get(), 1);
		checkInt(bb.get(), 2);
		checkInt(bb.get(), 3);
		checkInt(bb.get(), 4);
		
		cb.position(4);
		BufferUtils.copy(new char[] {1, 2, 3, 4}, 0, cb, 4);
		checkInt(cb.get(), 1);
		checkInt(cb.get(), 2);
		checkInt(cb.get(), 3);
		checkInt(cb.get(), 4);
		cb.position(0);
		BufferUtils.copy(new char[] {5, 6, 7, 8}, 1, cb, 3);
		checkInt(cb.get(), 6);
		checkInt(cb.get(), 7);
		checkInt(cb.get(), 8);
		
		sb.position(4);
		BufferUtils.copy(new short[] {1, 2, 3, 4}, 0, sb, 4);
		checkInt(sb.get(), 1);
		checkInt(sb.get(), 2);
		checkInt(sb.get(), 3);
		checkInt(sb.get(), 4);
		sb.position(0);
		BufferUtils.copy(new short[] {5, 6, 7, 8}, 1, sb, 3);
		checkInt(sb.get(), 6);
		checkInt(sb.get(), 7);
		checkInt(sb.get(), 8);

		ib.position(4);
		BufferUtils.copy(new int[] {1, 2, 3, 4}, 0, ib, 4);
		checkInt(ib.get(), 1);
		checkInt(ib.get(), 2);
		checkInt(ib.get(), 3);
		checkInt(ib.get(), 4);
		ib.position(0);
		BufferUtils.copy(new int[] {5, 6, 7, 8}, 1, ib, 3);
		checkInt(ib.get(), 6);
		checkInt(ib.get(), 7);
		checkInt(ib.get(), 8);
		
		lb.position(4);
		BufferUtils.copy(new long[] {1, 2, 3, 4}, 0, lb, 4);
		checkInt(lb.get(), 1);
		checkInt(lb.get(), 2);
		checkInt(lb.get(), 3);
		checkInt(lb.get(), 4);
		lb.position(0);
		BufferUtils.copy(new long[] {5, 6, 7, 8}, 1, lb, 3);
		checkInt(lb.get(), 6);
		checkInt(lb.get(), 7);
		checkInt(lb.get(), 8);

		fb.position(4);
		BufferUtils.copy(new float[] {1, 2, 3, 4}, 0, fb, 4);
		checkFloat(fb.get(), 1);
		checkFloat(fb.get(), 2);
		checkFloat(fb.get(), 3);
		checkFloat(fb.get(), 4);
		fb.position(0);
		BufferUtils.copy(new float[] {5, 6, 7, 8}, 1, fb, 3);
		checkFloat(fb.get(), 6);
		checkFloat(fb.get(), 7);
		checkFloat(fb.get(), 8);

		if (Gdx.app.getType() != ApplicationType.WebGL) { // gwt throws: NYI: Numbers.doubleToRawLongBits
			db.position(4);
			BufferUtils.copy(new double[] {1, 2, 3, 4}, 0, db, 4);
			checkFloat(db.get(), 1);
			checkFloat(db.get(), 2);
			checkFloat(db.get(), 3);
			checkFloat(db.get(), 4);
			db.position(0);
			BufferUtils.copy(new double[] {5, 6, 7, 8}, 1, db, 3);
			checkFloat(db.get(), 6);
			checkFloat(db.get(), 7);
			checkFloat(db.get(), 8);
		}

		ByteBuffer bb2 = BufferUtils.newByteBuffer(4);
		bb.position(4);
		BufferUtils.copy(bb, bb2, 4);
		checkInt(bb2.get(), 1);
		checkInt(bb2.get(), 2);
		checkInt(bb2.get(), 3);
		checkInt(bb2.get(), 4);

		bench();
	}

	private void bench () {
		benchByte();
		benchShort();
		benchInt();
		benchLong();
		benchFloat();
		benchDouble();
	}

	private void benchByte () {
		ByteBuffer bb = BufferUtils.newByteBuffer(1024 * 1024);
		byte[] bytes = new byte[1024 * 1024];
		int len = bytes.length;
		final int NUM_MB = 5;

		// relative put
		long start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			bb.clear();
			for (int i = 0; i < len; i++)
				bb.put(bytes[i]);
		}
		Gdx.app.log("BufferUtilsTest", "ByteBuffer relative put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// absolute put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			bb.clear();
			for (int i = 0; i < len; i++)
				bb.put(i, bytes[i]);
		}
		Gdx.app.log("BufferUtilsTest", "ByteBuffer absolute put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// bulk put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			bb.clear();
			bb.put(bytes);
		}
		Gdx.app.log("BufferUtilsTest", "ByteBuffer bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// JNI put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			bb.clear();
			BufferUtils.copy(bytes, 0, bb, len);
		}
		Gdx.app.log("BufferUtilsTest", "ByteBuffer native bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);
	}

	private void benchShort () {
		ShortBuffer sb = BufferUtils.newShortBuffer(1024 * 1024 / 2);
		short[] shorts = new short[1024 * 1024 / 2];
		int len = shorts.length;

		// relative put
		long start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			sb.clear();
			for (int i = 0; i < len; i++)
				sb.put(shorts[i]);
		}
		Gdx.app.log("BufferUtilsTest", "ShortBuffer relative put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// absolute put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			sb.clear();
			for (int i = 0; i < len; i++)
				sb.put(i, shorts[i]);
		}
		Gdx.app.log("BufferUtilsTest", "ShortBuffer absolute put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// bulk put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			sb.clear();
			sb.put(shorts);
		}
		Gdx.app.log("BufferUtilsTest", "ShortBuffer bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// JNI put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			sb.clear();
			BufferUtils.copy(shorts, 0, sb, len);
		}
		Gdx.app.log("BufferUtilsTest", "ShortBuffer native bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);
	}

	private void benchInt () {
		IntBuffer ib = BufferUtils.newIntBuffer(1024 * 1024 / 4);
		int[] ints = new int[1024 * 1024 / 4];
		int len = ints.length;

		// relative put
		long start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			ib.clear();
			for (int i = 0; i < len; i++)
				ib.put(ints[i]);
		}
		Gdx.app.log("BufferUtilsTest", "IntBuffer relative put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// absolute put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			ib.clear();
			for (int i = 0; i < len; i++)
				ib.put(i, ints[i]);
		}
		Gdx.app.log("BufferUtilsTest", "IntBuffer absolute put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// bulk put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			ib.clear();
			ib.put(ints);
		}
		Gdx.app.log("BufferUtilsTest", "IntBuffer bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// JNI put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			ib.clear();
			BufferUtils.copy(ints, 0, ib, len);
		}
		Gdx.app.log("BufferUtilsTest", "IntBuffer native bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);
	}

	private void benchLong () {
		LongBuffer lb = BufferUtils.newLongBuffer(1024 * 1024 / 8);
		long[] longs = new long[1024 * 1024 / 8];
		int len = longs.length;

		// relative put
		long start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			lb.clear();
			for (int i = 0; i < len; i++)
				lb.put(longs[i]);
		}
		Gdx.app.log("BufferUtilsTest", "LongBuffer relative put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// absolute put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			lb.clear();
			for (int i = 0; i < len; i++)
				lb.put(i, longs[i]);
		}
		Gdx.app.log("BufferUtilsTest", "LongBuffer absolute put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// bulk put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			lb.clear();
			lb.put(longs);
		}
		Gdx.app.log("BufferUtilsTest", "LongBuffer bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// JNI put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			lb.clear();
			BufferUtils.copy(longs, 0, lb, len);
		}
		Gdx.app.log("BufferUtilsTest", "LongBuffer native bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);
	}

	private void benchFloat () {
		FloatBuffer fb = BufferUtils.newFloatBuffer(1024 * 1024 / 4);
		float[] floats = new float[1024 * 1024 / 4];
		int len = floats.length;

		// relative put
		long start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			fb.clear();
			for (int i = 0; i < len; i++)
				fb.put(floats[i]);
		}
		Gdx.app.log("BufferUtilsTest", "FloatBuffer relative put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// absolute put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			fb.clear();
			for (int i = 0; i < len; i++)
				fb.put(i, floats[i]);
		}
		Gdx.app.log("BufferUtilsTest", "FloatBuffer absolute put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// bulk put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			fb.clear();
			fb.put(floats);
		}
		Gdx.app.log("BufferUtilsTest", "FloatBuffer bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// JNI put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			fb.clear();
			BufferUtils.copy(floats, 0, fb, len);
		}
		Gdx.app.log("BufferUtilsTest", "FloatBuffer native bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);
	}

	private void benchDouble () {
		DoubleBuffer db = BufferUtils.newDoubleBuffer(1024 * 1024 / 8);
		double[] doubles = new double[1024 * 1024 / 8];
		int len = doubles.length;

		// relative put
		long start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			db.clear();
			for (int i = 0; i < len; i++)
				db.put(doubles[i]);
		}
		Gdx.app.log("BufferUtilsTest", "DoubleBuffer relative put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// absolute put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			db.clear();
			for (int i = 0; i < len; i++)
				db.put(i, doubles[i]);
		}
		Gdx.app.log("BufferUtilsTest", "DoubleBuffer absolute put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// bulk put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			db.clear();
			db.put(doubles);
		}
		Gdx.app.log("BufferUtilsTest", "DoubleBuffer bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);

		// JNI put
		start = TimeUtils.nanoTime();
		for (int j = 0; j < NUM_MB; j++) {
			db.clear();
			BufferUtils.copy(doubles, 0, db, len);
		}
		Gdx.app.log("BufferUtilsTest", "DoubleBuffer bulk put: " + (TimeUtils.nanoTime() - start) / 1000000000.0f);
	}

	private void checkInt (long val1, long val2) {
		if (val1 != val2) { 
			Gdx.app.error("BufferUtilsTest", "checkInt failed: "+val1+" != "+val2);
			throw new GdxRuntimeException("Error, val1 != val2");
		}
	}

	private void checkFloat (double val1, double val2) {
		if (val1 != val2) {
			Gdx.app.error("BufferUtilsTest", "checkFloat failed: "+val1+" != "+val2);
			throw new GdxRuntimeException("Error, val1 != val2");
		}
	}
}
