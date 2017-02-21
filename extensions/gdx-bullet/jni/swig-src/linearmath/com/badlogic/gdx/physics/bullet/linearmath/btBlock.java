/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
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

public class btBlock extends BulletBase {
	private long swigCPtr;
	
	protected btBlock(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btBlock, normally you should not need this constructor it's intended for low-level usage. */ 
	public btBlock(long cPtr, boolean cMemoryOwn) {
		this("btBlock", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btBlock obj) {
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
				LinearMathJNI.delete_btBlock(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setPrevious(btBlock value) {
    LinearMathJNI.btBlock_previous_set(swigCPtr, this, btBlock.getCPtr(value), value);
  }

  public btBlock getPrevious() {
    long cPtr = LinearMathJNI.btBlock_previous_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btBlock(cPtr, false);
  }

  public void setAddress(java.nio.ByteBuffer value) {
    assert value.isDirect() : "Buffer must be allocated direct.";
    {
      LinearMathJNI.btBlock_address_set(swigCPtr, this, value);
    }
  }

  public java.nio.ByteBuffer getAddress() {
    return LinearMathJNI.btBlock_address_get(swigCPtr, this);
}

  public btBlock() {
    this(LinearMathJNI.new_btBlock(), true);
  }

}
