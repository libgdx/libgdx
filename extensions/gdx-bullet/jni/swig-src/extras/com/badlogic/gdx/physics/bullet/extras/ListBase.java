/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.extras;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.inversedynamics.MultiBodyTree;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btContactSolverInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

public class ListBase extends BulletBase {
	private long swigCPtr;
	
	protected ListBase(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new ListBase, normally you should not need this constructor it's intended for low-level usage. */ 
	public ListBase(long cPtr, boolean cMemoryOwn) {
		this("ListBase", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(ListBase obj) {
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
				ExtrasJNI.delete_ListBase(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setFirst(long value) {
    ExtrasJNI.ListBase_first_set(swigCPtr, this, value);
  }

  public long getFirst() {
    return ExtrasJNI.ListBase_first_get(swigCPtr, this);
  }

  public void setLast(long value) {
    ExtrasJNI.ListBase_last_set(swigCPtr, this, value);
  }

  public long getLast() {
    return ExtrasJNI.ListBase_last_get(swigCPtr, this);
  }

  public ListBase() {
    this(ExtrasJNI.new_ListBase(), true);
  }

}
