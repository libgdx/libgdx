
package com.badlogic.gdx.tests.desktop;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.utils.BufferUtils;

public class MicroBenchmarks {
	static long start;

	public static void main (String[] argv) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024 * Float.SIZE / 8);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = byteBuffer.asFloatBuffer();

		float[] array = new float[1024 * 1024];

		// single put
		tic();
		for (int tries = 0; tries < 1000; tries++) {
			for (int i = 0; i < array.length; i++)
				buffer.put(array[i]);
			buffer.clear();
		}
		toc("single put");

		// single indexed put
		tic();
		for (int tries = 0; tries < 1000; tries++) {
			for (int i = 0; i < array.length; i++)
				buffer.put(i, array[i]);
			buffer.clear();
		}
		toc("single indexed put");

		// bulk put
		tic();
		for (int tries = 0; tries < 1000; tries++) {
			buffer.put(array);
			buffer.clear();
		}
		toc("vector put");

		// jni bulk put
		tic();
		for (int tries = 0; tries < 1000; tries++)
			BufferUtils.copy(array, buffer, array.length, 0);
		toc("jni put");

		// jni test
		byteBuffer = ByteBuffer.allocateDirect(4 * Float.SIZE / 8);
		byteBuffer.order(ByteOrder.nativeOrder());
		buffer = byteBuffer.asFloatBuffer();

		array = new float[] {0, 1, 2, 3};
		BufferUtils.copy(array, buffer, 4, 0);
		System.out.println(buffer.get());
		System.out.println(buffer.get());
		System.out.println(buffer.get());
		System.out.println(buffer.get());
	}

	private static void tic () {
		start = System.nanoTime();
	}

	private static void toc (String info) {
		System.out.println("MicroBenchmarks: " + info + ", " + (System.nanoTime() - start) / 1000000000.0f);
	}
}
