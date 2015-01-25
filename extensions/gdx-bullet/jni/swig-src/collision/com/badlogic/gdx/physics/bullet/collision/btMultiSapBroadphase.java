/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
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

public class btMultiSapBroadphase extends btBroadphaseInterface {
	private long swigCPtr;
	
	protected btMultiSapBroadphase(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btMultiSapBroadphase_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btMultiSapBroadphase, normally you should not need this constructor it's intended for low-level usage. */
	public btMultiSapBroadphase(long cPtr, boolean cMemoryOwn) {
		this("btMultiSapBroadphase", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btMultiSapBroadphase_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btMultiSapBroadphase obj) {
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
				CollisionJNI.delete_btMultiSapBroadphase(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  static public class btMultiSapProxy extends btBroadphaseProxy {
  	private long swigCPtr;
  	
  	protected btMultiSapProxy(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_SWIGUpcast(cPtr), cMemoryOwn);
  		swigCPtr = cPtr;
  	}
  	
  	/** Construct a new btMultiSapProxy, normally you should not need this constructor it's intended for low-level usage. */
  	public btMultiSapProxy(long cPtr, boolean cMemoryOwn) {
  		this("btMultiSapProxy", cPtr, cMemoryOwn);
  		construct();
  	}
  	
  	@Override
  	protected void reset(long cPtr, boolean cMemoryOwn) {
  		if (!destroyed)
  			destroy();
  		super.reset(CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
  	}
  	
  	public static long getCPtr(btMultiSapProxy obj) {
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
  				CollisionJNI.delete_btMultiSapBroadphase_btMultiSapProxy(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public void setAabbMin(btVector3 value) {
      CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_aabbMin_set(swigCPtr, this, btVector3.getCPtr(value), value);
    }
  
    public btVector3 getAabbMin() {
      long cPtr = CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_aabbMin_get(swigCPtr, this);
      return (cPtr == 0) ? null : new btVector3(cPtr, false);
    }
  
    public void setAabbMax(btVector3 value) {
      CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_aabbMax_set(swigCPtr, this, btVector3.getCPtr(value), value);
    }
  
    public btVector3 getAabbMax() {
      long cPtr = CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_aabbMax_get(swigCPtr, this);
      return (cPtr == 0) ? null : new btVector3(cPtr, false);
    }
  
    public void setShapeType(int value) {
      CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_shapeType_set(swigCPtr, this, value);
    }
  
    public int getShapeType() {
      return CollisionJNI.btMultiSapBroadphase_btMultiSapProxy_shapeType_get(swigCPtr, this);
    }
  
    public btMultiSapProxy(Vector3 aabbMin, Vector3 aabbMax, int shapeType, long userPtr, short collisionFilterGroup, short collisionFilterMask) {
      this(CollisionJNI.new_btMultiSapBroadphase_btMultiSapProxy(aabbMin, aabbMax, shapeType, userPtr, collisionFilterGroup, collisionFilterMask), true);
    }
  
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_btBroadphaseInterface_p_t getBroadphaseArray() {
    return new SWIGTYPE_p_btAlignedObjectArrayT_btBroadphaseInterface_p_t(CollisionJNI.btMultiSapBroadphase_getBroadphaseArray__SWIG_0(swigCPtr, this), false);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback, Vector3 aabbMin, Vector3 aabbMax) {
    CollisionJNI.btMultiSapBroadphase_rayTest__SWIG_0(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback, aabbMin, aabbMax);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback, Vector3 aabbMin) {
    CollisionJNI.btMultiSapBroadphase_rayTest__SWIG_1(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback, aabbMin);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback) {
    CollisionJNI.btMultiSapBroadphase_rayTest__SWIG_2(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback);
  }

  public void addToChildBroadphase(btMultiSapBroadphase.btMultiSapProxy parentMultiSapProxy, btBroadphaseProxy childProxy, btBroadphaseInterface childBroadphase) {
    CollisionJNI.btMultiSapBroadphase_addToChildBroadphase(swigCPtr, this, btMultiSapBroadphase.btMultiSapProxy.getCPtr(parentMultiSapProxy), parentMultiSapProxy, btBroadphaseProxy.getCPtr(childProxy), childProxy, btBroadphaseInterface.getCPtr(childBroadphase), childBroadphase);
  }

  public boolean testAabbOverlap(btBroadphaseProxy proxy0, btBroadphaseProxy proxy1) {
    return CollisionJNI.btMultiSapBroadphase_testAabbOverlap(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy0), proxy0, btBroadphaseProxy.getCPtr(proxy1), proxy1);
  }

  public btOverlappingPairCache getOverlappingPairCache() {
    long cPtr = CollisionJNI.btMultiSapBroadphase_getOverlappingPairCache__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btOverlappingPairCache(cPtr, false);
  }

  public void buildTree(Vector3 bvhAabbMin, Vector3 bvhAabbMax) {
    CollisionJNI.btMultiSapBroadphase_buildTree(swigCPtr, this, bvhAabbMin, bvhAabbMax);
  }

  public void quicksort(btBroadphasePairArray a, int lo, int hi) {
    CollisionJNI.btMultiSapBroadphase_quicksort(swigCPtr, this, btBroadphasePairArray.getCPtr(a), a, lo, hi);
  }

}
