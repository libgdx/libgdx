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

public class GIM_TRIANGLE_CONTACT extends BulletBase {
	private long swigCPtr;
	
	protected GIM_TRIANGLE_CONTACT(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new GIM_TRIANGLE_CONTACT, normally you should not need this constructor it's intended for low-level usage. */ 
	public GIM_TRIANGLE_CONTACT(long cPtr, boolean cMemoryOwn) {
		this("GIM_TRIANGLE_CONTACT", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(GIM_TRIANGLE_CONTACT obj) {
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
				CollisionJNI.delete_GIM_TRIANGLE_CONTACT(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setPenetration_depth(float value) {
    CollisionJNI.GIM_TRIANGLE_CONTACT_penetration_depth_set(swigCPtr, this, value);
  }

  public float getPenetration_depth() {
    return CollisionJNI.GIM_TRIANGLE_CONTACT_penetration_depth_get(swigCPtr, this);
  }

  public void setPoint_count(int value) {
    CollisionJNI.GIM_TRIANGLE_CONTACT_point_count_set(swigCPtr, this, value);
  }

  public int getPoint_count() {
    return CollisionJNI.GIM_TRIANGLE_CONTACT_point_count_get(swigCPtr, this);
  }

  public void setSeparating_normal(btVector4 value) {
    CollisionJNI.GIM_TRIANGLE_CONTACT_separating_normal_set(swigCPtr, this, btVector4.getCPtr(value), value);
  }

  public btVector4 getSeparating_normal() {
    long cPtr = CollisionJNI.GIM_TRIANGLE_CONTACT_separating_normal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector4(cPtr, false);
  }

  public void setPoints(btVector3 value) {
    CollisionJNI.GIM_TRIANGLE_CONTACT_points_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getPoints() {
    long cPtr = CollisionJNI.GIM_TRIANGLE_CONTACT_points_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void copy_from(GIM_TRIANGLE_CONTACT other) {
    CollisionJNI.GIM_TRIANGLE_CONTACT_copy_from(swigCPtr, this, GIM_TRIANGLE_CONTACT.getCPtr(other), other);
  }

  public GIM_TRIANGLE_CONTACT() {
    this(CollisionJNI.new_GIM_TRIANGLE_CONTACT__SWIG_0(), true);
  }

  public GIM_TRIANGLE_CONTACT(GIM_TRIANGLE_CONTACT other) {
    this(CollisionJNI.new_GIM_TRIANGLE_CONTACT__SWIG_1(GIM_TRIANGLE_CONTACT.getCPtr(other), other), true);
  }

  public void merge_points(btVector4 plane, float margin, btVector3 points, int point_count) {
    CollisionJNI.GIM_TRIANGLE_CONTACT_merge_points(swigCPtr, this, btVector4.getCPtr(plane), plane, margin, btVector3.getCPtr(points), points, point_count);
  }

}
