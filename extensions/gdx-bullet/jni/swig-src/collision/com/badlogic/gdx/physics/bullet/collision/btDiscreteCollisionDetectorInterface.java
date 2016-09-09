/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
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

public class btDiscreteCollisionDetectorInterface extends BulletBase {
	private long swigCPtr;
	
	protected btDiscreteCollisionDetectorInterface(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btDiscreteCollisionDetectorInterface, normally you should not need this constructor it's intended for low-level usage. */ 
	public btDiscreteCollisionDetectorInterface(long cPtr, boolean cMemoryOwn) {
		this("btDiscreteCollisionDetectorInterface", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btDiscreteCollisionDetectorInterface obj) {
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
				CollisionJNI.delete_btDiscreteCollisionDetectorInterface(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  static public class Result extends BulletBase {
  	private long swigCPtr;
  	
  	protected Result(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, cPtr, cMemoryOwn);
  		swigCPtr = cPtr;
  	}
  	
  	/** Construct a new Result, normally you should not need this constructor it's intended for low-level usage. */ 
  	public Result(long cPtr, boolean cMemoryOwn) {
  		this("Result", cPtr, cMemoryOwn);
  		construct();
  	}
  	
  	@Override
  	protected void reset(long cPtr, boolean cMemoryOwn) {
  		if (!destroyed)
  			destroy();
  		super.reset(swigCPtr = cPtr, cMemoryOwn);
  	}
  	
  	public static long getCPtr(Result obj) {
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
  				CollisionJNI.delete_btDiscreteCollisionDetectorInterface_Result(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public void setShapeIdentifiersA(int partId0, int index0) {
      CollisionJNI.btDiscreteCollisionDetectorInterface_Result_setShapeIdentifiersA(swigCPtr, this, partId0, index0);
    }
  
    public void setShapeIdentifiersB(int partId1, int index1) {
      CollisionJNI.btDiscreteCollisionDetectorInterface_Result_setShapeIdentifiersB(swigCPtr, this, partId1, index1);
    }
  
    public void addContactPoint(Vector3 normalOnBInWorld, Vector3 pointInWorld, float depth) {
      CollisionJNI.btDiscreteCollisionDetectorInterface_Result_addContactPoint(swigCPtr, this, normalOnBInWorld, pointInWorld, depth);
    }
  
  }

  static public class ClosestPointInput extends BulletBase {
  	private long swigCPtr;
  	
  	protected ClosestPointInput(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, cPtr, cMemoryOwn);
  		swigCPtr = cPtr;
  	}
  	
  	/** Construct a new ClosestPointInput, normally you should not need this constructor it's intended for low-level usage. */ 
  	public ClosestPointInput(long cPtr, boolean cMemoryOwn) {
  		this("ClosestPointInput", cPtr, cMemoryOwn);
  		construct();
  	}
  	
  	@Override
  	protected void reset(long cPtr, boolean cMemoryOwn) {
  		if (!destroyed)
  			destroy();
  		super.reset(swigCPtr = cPtr, cMemoryOwn);
  	}
  	
  	public static long getCPtr(ClosestPointInput obj) {
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
  				CollisionJNI.delete_btDiscreteCollisionDetectorInterface_ClosestPointInput(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public ClosestPointInput() {
      this(CollisionJNI.new_btDiscreteCollisionDetectorInterface_ClosestPointInput(), true);
    }
  
    public void setTransformA(btTransform value) {
      CollisionJNI.btDiscreteCollisionDetectorInterface_ClosestPointInput_transformA_set(swigCPtr, this, btTransform.getCPtr(value), value);
    }
  
    public btTransform getTransformA() {
      long cPtr = CollisionJNI.btDiscreteCollisionDetectorInterface_ClosestPointInput_transformA_get(swigCPtr, this);
      return (cPtr == 0) ? null : new btTransform(cPtr, false);
    }
  
    public void setTransformB(btTransform value) {
      CollisionJNI.btDiscreteCollisionDetectorInterface_ClosestPointInput_transformB_set(swigCPtr, this, btTransform.getCPtr(value), value);
    }
  
    public btTransform getTransformB() {
      long cPtr = CollisionJNI.btDiscreteCollisionDetectorInterface_ClosestPointInput_transformB_get(swigCPtr, this);
      return (cPtr == 0) ? null : new btTransform(cPtr, false);
    }
  
    public void setMaximumDistanceSquared(float value) {
      CollisionJNI.btDiscreteCollisionDetectorInterface_ClosestPointInput_maximumDistanceSquared_set(swigCPtr, this, value);
    }
  
    public float getMaximumDistanceSquared() {
      return CollisionJNI.btDiscreteCollisionDetectorInterface_ClosestPointInput_maximumDistanceSquared_get(swigCPtr, this);
    }
  
  }

  public void getClosestPoints(btDiscreteCollisionDetectorInterface.ClosestPointInput input, btDiscreteCollisionDetectorInterface.Result output, btIDebugDraw debugDraw, boolean swapResults) {
    CollisionJNI.btDiscreteCollisionDetectorInterface_getClosestPoints__SWIG_0(swigCPtr, this, btDiscreteCollisionDetectorInterface.ClosestPointInput.getCPtr(input), input, btDiscreteCollisionDetectorInterface.Result.getCPtr(output), output, btIDebugDraw.getCPtr(debugDraw), debugDraw, swapResults);
  }

  public void getClosestPoints(btDiscreteCollisionDetectorInterface.ClosestPointInput input, btDiscreteCollisionDetectorInterface.Result output, btIDebugDraw debugDraw) {
    CollisionJNI.btDiscreteCollisionDetectorInterface_getClosestPoints__SWIG_1(swigCPtr, this, btDiscreteCollisionDetectorInterface.ClosestPointInput.getCPtr(input), input, btDiscreteCollisionDetectorInterface.Result.getCPtr(output), output, btIDebugDraw.getCPtr(debugDraw), debugDraw);
  }

}
