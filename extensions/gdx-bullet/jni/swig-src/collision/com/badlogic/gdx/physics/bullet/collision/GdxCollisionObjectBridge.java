/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class GdxCollisionObjectBridge extends BulletBase {
	private long swigCPtr;
	
	protected GdxCollisionObjectBridge(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new GdxCollisionObjectBridge, normally you should not need this constructor it's intended for low-level usage. */ 
	public GdxCollisionObjectBridge(long cPtr, boolean cMemoryOwn) {
		this("GdxCollisionObjectBridge", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(GdxCollisionObjectBridge obj) {
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
				CollisionJNI.delete_GdxCollisionObjectBridge(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setUserValue(int value) {
    CollisionJNI.GdxCollisionObjectBridge_userValue_set(swigCPtr, this, value);
  }

  public int getUserValue() {
    return CollisionJNI.GdxCollisionObjectBridge_userValue_get(swigCPtr, this);
  }

  public void setContactCallbackFlag(int value) {
    CollisionJNI.GdxCollisionObjectBridge_contactCallbackFlag_set(swigCPtr, this, value);
  }

  public int getContactCallbackFlag() {
    return CollisionJNI.GdxCollisionObjectBridge_contactCallbackFlag_get(swigCPtr, this);
  }

  public void setContactCallbackFilter(int value) {
    CollisionJNI.GdxCollisionObjectBridge_contactCallbackFilter_set(swigCPtr, this, value);
  }

  public int getContactCallbackFilter() {
    return CollisionJNI.GdxCollisionObjectBridge_contactCallbackFilter_get(swigCPtr, this);
  }

  public GdxCollisionObjectBridge() {
    this(CollisionJNI.new_GdxCollisionObjectBridge(), true);
  }

}
