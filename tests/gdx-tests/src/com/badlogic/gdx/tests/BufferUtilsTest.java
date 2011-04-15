package com.badlogic.gdx.tests;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class BufferUtilsTest extends GdxTest {
	@Override public boolean needsGL20 () {
		return false;
	}
	
	@Override public void create() {
		ByteBuffer bb = BufferUtils.newByteBuffer(8);
		CharBuffer cb = BufferUtils.newCharBuffer(8);
		ShortBuffer sb = BufferUtils.newShortBuffer(8);
		IntBuffer ib = BufferUtils.newIntBuffer(8);
		LongBuffer lb = BufferUtils.newLongBuffer(8);
		FloatBuffer fb = BufferUtils.newFloatBuffer(8);
		DoubleBuffer db = BufferUtils.newDoubleBuffer(8);
		
		bb.position(4);
		BufferUtils.copy(new byte[] { 1, 2, 3, 4 }, 0, bb, 4);
		checkInt(bb.get(), 1); 
		checkInt(bb.get(), 2);
		checkInt(bb.get(), 3);
		checkInt(bb.get(), 4);
		
		cb.position(4);
		BufferUtils.copy(new char[] { 1, 2, 3, 4 }, 0, cb, 4);
		checkInt(cb.get(), 1); 
		checkInt(cb.get(), 2);
		checkInt(cb.get(), 3);
		checkInt(cb.get(), 4);
		
		sb.position(4);
		BufferUtils.copy(new short[] { 1, 2, 3, 4 }, 0, sb, 4);
		checkInt(sb.get(), 1); 
		checkInt(sb.get(), 2);
		checkInt(sb.get(), 3);
		checkInt(sb.get(), 4);
		
		ib.position(4);
		BufferUtils.copy(new int[] { 1, 2, 3, 4 }, 0, ib, 4);
		checkInt(ib.get(), 1); 
		checkInt(ib.get(), 2);
		checkInt(ib.get(), 3);
		checkInt(ib.get(), 4);
		
		lb.position(4);
		BufferUtils.copy(new long[] { 1, 2, 3, 4 }, 0, lb, 4);
		checkInt(lb.get(), 1); 
		checkInt(lb.get(), 2);
		checkInt(lb.get(), 3);
		checkInt(lb.get(), 4);
		
		fb.position(4);
		BufferUtils.copy(new float[] { 1, 2, 3, 4 }, 0, fb, 4);
		checkFloat(fb.get(), 1); 
		checkFloat(fb.get(), 2);
		checkFloat(fb.get(), 3);
		checkFloat(fb.get(), 4);
		
		db.position(4);
		BufferUtils.copy(new double[] { 1, 2, 3, 4 }, 0, db, 4);
		checkFloat(db.get(), 1); 
		checkFloat(db.get(), 2);
		checkFloat(db.get(), 3);
		checkFloat(db.get(), 4);
		
		ByteBuffer bb2 = BufferUtils.newByteBuffer(4);
		bb.position(4);
		BufferUtils.copy(bb, bb2, 4);
		checkInt(bb2.get(), 1); 
		checkInt(bb2.get(), 2);
		checkInt(bb2.get(), 3);
		checkInt(bb2.get(), 4);
		
		bench();
	}
	
	private void bench() {
		ByteBuffer bb = BufferUtils.newByteBuffer(1024*1024);
		byte[] bytes = new byte[1024*1024];
		int len = bytes.length;
		
		// relative put
		long start = System.nanoTime();
		for(int j = 0; j < 50; j++) {
			bb.clear();		
			for(int i = 0; i < len; i++) bb.put(bytes[i]);
		}
		Gdx.app.log("BufferUtilsTest", "relative put: " + (System.nanoTime() - start) / 1000000000.0f);
		
		// absolute put
		start = System.nanoTime();
		for(int j = 0; j < 50; j++) {
			bb.clear();		
			for(int i = 0; i < len; i++) bb.put(i, bytes[i]);
		}
		Gdx.app.log("BufferUtilsTest", "absolute put: " + (System.nanoTime() - start) / 1000000000.0f);
		
		// bulk put
		start = System.nanoTime();
		for(int j = 0; j < 10; j++) {
			bb.clear();		
			bb.put(bytes);
		}
		Gdx.app.log("BufferUtilsTest", "bulk put: " + (System.nanoTime() - start) / 1000000000.0f);
		
		// JNI put
		start = System.nanoTime();
		for(int j = 0; j < 10; j++) {
			bb.clear();		
			BufferUtils.copy(bytes, 0, bb, len);
		}
		Gdx.app.log("BufferUtilsTest", "bulk put: " + (System.nanoTime() - start) / 1000000000.0f);
	}
	
	private void checkInt(long val1, long val2) {
		if(val1 != val2) throw new GdxRuntimeException("Error, val1 != val2");
	}
	
	private void checkFloat(double val1, double val2) {
		if(val1 != val2) throw new GdxRuntimeException("Error, val1 != val2");
	}
}
