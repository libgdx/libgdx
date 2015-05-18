package com.btdstudio.glbase;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

class Pools {
	// Draw call pool
	static class DrawCallPool extends Pool<DrawCall> {
		@Override
		public DrawCall newObject(){
			return new DrawCall(-1);
		}
	}
	
	// MyUniform pool
	static class MyUniformPool extends Pool<MyUniformValue> {
		@Override
		public MyUniformValue newObject(){
			return new MyUniformValue(-1);
		}
	}

	// Draw call pool
	private static DrawCallPool drawCallPool = new DrawCallPool();
	private static Array<DrawCall> drawCallsToFree = new Array<DrawCall>();
	
	// My Uniform pool
	private static MyUniformPool myUniformPool = new MyUniformPool();
	private static Array<MyUniformValue> uniformsToFree = new Array<MyUniformValue>();
	
	/**
	 * @return A cached draw call
	 */
	public static DrawCall acquireDrawCall(){
		DrawCall dc = drawCallPool.obtain();
		drawCallsToFree.add(dc);
		return dc;
	}
	
	/**
	 * @return A cached uniform value
	 */
	public static MyUniformValue acquiteMyUniformValue(){
		MyUniformValue mu = myUniformPool.obtain();
		uniformsToFree.add(mu);
		return mu;
	}
	
	/**
	 * Free everything that was acquired
	 */
	public static void freeAll(){
		drawCallPool.freeAll(drawCallsToFree);
		myUniformPool.freeAll(uniformsToFree);
		
		drawCallsToFree.clear();
		uniformsToFree.clear();
	}
}
