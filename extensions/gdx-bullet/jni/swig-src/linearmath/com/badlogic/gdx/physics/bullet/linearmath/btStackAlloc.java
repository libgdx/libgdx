/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.linearmath;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btStackAlloc extends BulletBase {
	private long swigCPtr;
	
	protected btStackAlloc(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btStackAlloc, normally you should not need this constructor it's intended for low-level usage. */ 
	public btStackAlloc(long cPtr, boolean cMemoryOwn) {
		this("btStackAlloc", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btStackAlloc obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}

  @Override protected synchronized void delete() {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				LinearMathJNI.delete_btStackAlloc(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btStackAlloc(long size) {
    this(LinearMathJNI.new_btStackAlloc(size), true);
  }

  public void create(long size) {
    LinearMathJNI.btStackAlloc_create(swigCPtr, this, size);
  }

  public void destroy() {
    LinearMathJNI.btStackAlloc_destroy(swigCPtr, this);
  }

  public int getAvailableMemory() {
    return LinearMathJNI.btStackAlloc_getAvailableMemory(swigCPtr, this);
  }

  public java.nio.ByteBuffer allocate(long size) {
    return LinearMathJNI.btStackAlloc_allocate(swigCPtr, this, size);
}

  public btBlock beginBlock() {
    long cPtr = LinearMathJNI.btStackAlloc_beginBlock(swigCPtr, this);
    return (cPtr == 0) ? null : new btBlock(cPtr, false);
  }

  public void endBlock(btBlock block) {
    LinearMathJNI.btStackAlloc_endBlock(swigCPtr, this, btBlock.getCPtr(block), block);
  }

}
