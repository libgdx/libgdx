/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.softbody;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btSoftSoftCollisionAlgorithm extends btCollisionAlgorithm {
	private long swigCPtr;
	
	protected btSoftSoftCollisionAlgorithm(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, SoftbodyJNI.btSoftSoftCollisionAlgorithm_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSoftSoftCollisionAlgorithm, normally you should not need this constructor it's intended for low-level usage. */
	public btSoftSoftCollisionAlgorithm(long cPtr, boolean cMemoryOwn) {
		this("btSoftSoftCollisionAlgorithm", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(SoftbodyJNI.btSoftSoftCollisionAlgorithm_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btSoftSoftCollisionAlgorithm obj) {
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
				SoftbodyJNI.delete_btSoftSoftCollisionAlgorithm(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btSoftSoftCollisionAlgorithm(btCollisionAlgorithmConstructionInfo ci) {
    this(SoftbodyJNI.new_btSoftSoftCollisionAlgorithm__SWIG_0(btCollisionAlgorithmConstructionInfo.getCPtr(ci), ci), true);
  }

  public btSoftSoftCollisionAlgorithm(btPersistentManifold mf, btCollisionAlgorithmConstructionInfo ci, btCollisionObjectWrapper body0Wrap, btCollisionObjectWrapper body1Wrap) {
    this(SoftbodyJNI.new_btSoftSoftCollisionAlgorithm__SWIG_1(btPersistentManifold.getCPtr(mf), mf, btCollisionAlgorithmConstructionInfo.getCPtr(ci), ci, btCollisionObjectWrapper.getCPtr(body0Wrap), body0Wrap, btCollisionObjectWrapper.getCPtr(body1Wrap), body1Wrap), true);
  }

  static public class CreateFunc extends btCollisionAlgorithmCreateFunc {
  	private long swigCPtr;
  	
  	protected CreateFunc(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, SoftbodyJNI.btSoftSoftCollisionAlgorithm_CreateFunc_SWIGUpcast(cPtr), cMemoryOwn);
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
  		super.reset(SoftbodyJNI.btSoftSoftCollisionAlgorithm_CreateFunc_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
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
  				SoftbodyJNI.delete_btSoftSoftCollisionAlgorithm_CreateFunc(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public CreateFunc() {
      this(SoftbodyJNI.new_btSoftSoftCollisionAlgorithm_CreateFunc(), true);
    }
  
  }

}
