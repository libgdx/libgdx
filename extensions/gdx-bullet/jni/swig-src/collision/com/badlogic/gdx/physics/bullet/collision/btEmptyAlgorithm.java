/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.11
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

public class btEmptyAlgorithm extends btCollisionAlgorithm {
	private long swigCPtr;
	
	protected btEmptyAlgorithm(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btEmptyAlgorithm_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btEmptyAlgorithm, normally you should not need this constructor it's intended for low-level usage. */
	public btEmptyAlgorithm(long cPtr, boolean cMemoryOwn) {
		this("btEmptyAlgorithm", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btEmptyAlgorithm_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btEmptyAlgorithm obj) {
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
				CollisionJNI.delete_btEmptyAlgorithm(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btEmptyAlgorithm(btCollisionAlgorithmConstructionInfo ci) {
    this(CollisionJNI.new_btEmptyAlgorithm(btCollisionAlgorithmConstructionInfo.getCPtr(ci), ci), true);
  }

  static public class CreateFunc extends BulletBase {
  	private long swigCPtr;
  	
  	protected CreateFunc(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, cPtr, cMemoryOwn);
  		swigCPtr = cPtr;
  	}
  	
  	/** Construct a new CreateFunc, normally you should not need this constructor it's intended for low-level usage. */ 
  	public CreateFunc(long cPtr, boolean cMemoryOwn) {
  		this("CreateFunc", cPtr, cMemoryOwn);
  		construct();
  	}
  	
  	@Override
  	protected void reset(long cPtr, boolean cMemoryOwn) {
  		if (!destroyed)
  			destroy();
  		super.reset(swigCPtr = cPtr, cMemoryOwn);
  	}
  	
  	public static long getCPtr(CreateFunc obj) {
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
  				CollisionJNI.delete_btEmptyAlgorithm_CreateFunc(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public btCollisionAlgorithm CreateCollisionAlgorithm(btCollisionAlgorithmConstructionInfo ci, btCollisionObjectWrapper body0Wrap, btCollisionObjectWrapper body1Wrap) {
      long cPtr = CollisionJNI.btEmptyAlgorithm_CreateFunc_CreateCollisionAlgorithm(swigCPtr, this, btCollisionAlgorithmConstructionInfo.getCPtr(ci), ci, btCollisionObjectWrapper.getCPtr(body0Wrap), body0Wrap, btCollisionObjectWrapper.getCPtr(body1Wrap), body1Wrap);
      return (cPtr == 0) ? null : new btCollisionAlgorithm(cPtr, false);
    }
  
    public CreateFunc() {
      this(CollisionJNI.new_btEmptyAlgorithm_CreateFunc(), true);
    }
  
  }

}
