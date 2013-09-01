/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btConvexInternalAabbCachingShape extends btConvexInternalShape {
	private long swigCPtr;
	
	protected btConvexInternalAabbCachingShape(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, gdxBulletJNI.btConvexInternalAabbCachingShape_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btConvexInternalAabbCachingShape(long cPtr, boolean cMemoryOwn) {
		this("btConvexInternalAabbCachingShape", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btConvexInternalAabbCachingShape obj) {
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
				gdxBulletJNI.delete_btConvexInternalAabbCachingShape(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void recalcLocalAabb() {
    gdxBulletJNI.btConvexInternalAabbCachingShape_recalcLocalAabb(swigCPtr, this);
  }

}
