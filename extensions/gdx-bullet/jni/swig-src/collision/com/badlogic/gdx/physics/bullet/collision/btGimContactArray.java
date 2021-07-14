/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btGimContactArray extends BulletBase {
	private long swigCPtr;
	
	protected btGimContactArray(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btGimContactArray, normally you should not need this constructor it's intended for low-level usage. */ 
	public btGimContactArray(long cPtr, boolean cMemoryOwn) {
		this("btGimContactArray", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btGimContactArray obj) {
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
				CollisionJNI.delete_btGimContactArray(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btGimContactArray operatorAssignment(btGimContactArray other) {
    return new btGimContactArray(CollisionJNI.btGimContactArray_operatorAssignment(swigCPtr, this, btGimContactArray.getCPtr(other), other), false);
  }

  public btGimContactArray() {
    this(CollisionJNI.new_btGimContactArray__SWIG_0(), true);
  }

  public btGimContactArray(btGimContactArray otherArray) {
    this(CollisionJNI.new_btGimContactArray__SWIG_1(btGimContactArray.getCPtr(otherArray), otherArray), true);
  }

  public int size() {
    return CollisionJNI.btGimContactArray_size(swigCPtr, this);
  }

  public GIM_CONTACT atConst(int n) {
    return new GIM_CONTACT(CollisionJNI.btGimContactArray_atConst(swigCPtr, this, n), false);
  }

  public GIM_CONTACT at(int n) {
    return new GIM_CONTACT(CollisionJNI.btGimContactArray_at(swigCPtr, this, n), false);
  }

  public GIM_CONTACT operatorSubscriptConst(int n) {
    return new GIM_CONTACT(CollisionJNI.btGimContactArray_operatorSubscriptConst(swigCPtr, this, n), false);
  }

  public GIM_CONTACT operatorSubscript(int n) {
    return new GIM_CONTACT(CollisionJNI.btGimContactArray_operatorSubscript(swigCPtr, this, n), false);
  }

  public void clear() {
    CollisionJNI.btGimContactArray_clear(swigCPtr, this);
  }

  public void pop_back() {
    CollisionJNI.btGimContactArray_pop_back(swigCPtr, this);
  }

  public void resizeNoInitialize(int newsize) {
    CollisionJNI.btGimContactArray_resizeNoInitialize(swigCPtr, this, newsize);
  }

  public void resize(int newsize, GIM_CONTACT fillData) {
    CollisionJNI.btGimContactArray_resize__SWIG_0(swigCPtr, this, newsize, GIM_CONTACT.getCPtr(fillData), fillData);
  }

  public void resize(int newsize) {
    CollisionJNI.btGimContactArray_resize__SWIG_1(swigCPtr, this, newsize);
  }

  public GIM_CONTACT expandNonInitializing() {
    return new GIM_CONTACT(CollisionJNI.btGimContactArray_expandNonInitializing(swigCPtr, this), false);
  }

  public GIM_CONTACT expand(GIM_CONTACT fillValue) {
    return new GIM_CONTACT(CollisionJNI.btGimContactArray_expand__SWIG_0(swigCPtr, this, GIM_CONTACT.getCPtr(fillValue), fillValue), false);
  }

  public GIM_CONTACT expand() {
    return new GIM_CONTACT(CollisionJNI.btGimContactArray_expand__SWIG_1(swigCPtr, this), false);
  }

  public void push_back(GIM_CONTACT _Val) {
    CollisionJNI.btGimContactArray_push_back(swigCPtr, this, GIM_CONTACT.getCPtr(_Val), _Val);
  }

  public int capacity() {
    return CollisionJNI.btGimContactArray_capacity(swigCPtr, this);
  }

  public void reserve(int _Count) {
    CollisionJNI.btGimContactArray_reserve(swigCPtr, this, _Count);
  }

  static public class less extends BulletBase {
  	private long swigCPtr;
  	
  	protected less(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, cPtr, cMemoryOwn);
  		swigCPtr = cPtr;
  	}
  	
  	/** Construct a new less, normally you should not need this constructor it's intended for low-level usage. */ 
  	public less(long cPtr, boolean cMemoryOwn) {
  		this("less", cPtr, cMemoryOwn);
  		construct();
  	}
  	
  	@Override
  	protected void reset(long cPtr, boolean cMemoryOwn) {
  		if (!destroyed)
  			destroy();
  		super.reset(swigCPtr = cPtr, cMemoryOwn);
  	}
  	
  	public static long getCPtr(less obj) {
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
  				CollisionJNI.delete_btGimContactArray_less(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public less() {
      this(CollisionJNI.new_btGimContactArray_less(), true);
    }
  
  }

  public void swap(int index0, int index1) {
    CollisionJNI.btGimContactArray_swap(swigCPtr, this, index0, index1);
  }

  public void removeAtIndex(int index) {
    CollisionJNI.btGimContactArray_removeAtIndex(swigCPtr, this, index);
  }

  public void initializeFromBuffer(long buffer, int size, int capacity) {
    CollisionJNI.btGimContactArray_initializeFromBuffer(swigCPtr, this, buffer, size, capacity);
  }

  public void copyFromArray(btGimContactArray otherArray) {
    CollisionJNI.btGimContactArray_copyFromArray(swigCPtr, this, btGimContactArray.getCPtr(otherArray), otherArray);
  }

}
