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

public class btContactArray extends btGimContactArray {
	private long swigCPtr;
	
	protected btContactArray(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btContactArray_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btContactArray, normally you should not need this constructor it's intended for low-level usage. */
	public btContactArray(long cPtr, boolean cMemoryOwn) {
		this("btContactArray", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btContactArray_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btContactArray obj) {
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
				CollisionJNI.delete_btContactArray(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btContactArray() {
    this(CollisionJNI.new_btContactArray(), true);
  }

  public void push_contact(Vector3 point, Vector3 normal, float depth, int feature1, int feature2) {
    CollisionJNI.btContactArray_push_contact(swigCPtr, this, point, normal, depth, feature1, feature2);
  }

  public void push_triangle_contacts(GIM_TRIANGLE_CONTACT tricontact, int feature1, int feature2) {
    CollisionJNI.btContactArray_push_triangle_contacts(swigCPtr, this, GIM_TRIANGLE_CONTACT.getCPtr(tricontact), tricontact, feature1, feature2);
  }

  public void merge_contacts(btContactArray contacts, boolean normal_contact_average) {
    CollisionJNI.btContactArray_merge_contacts__SWIG_0(swigCPtr, this, btContactArray.getCPtr(contacts), contacts, normal_contact_average);
  }

  public void merge_contacts(btContactArray contacts) {
    CollisionJNI.btContactArray_merge_contacts__SWIG_1(swigCPtr, this, btContactArray.getCPtr(contacts), contacts);
  }

  public void merge_contacts_unique(btContactArray contacts) {
    CollisionJNI.btContactArray_merge_contacts_unique(swigCPtr, this, btContactArray.getCPtr(contacts), contacts);
  }

}
