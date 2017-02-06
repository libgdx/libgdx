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

public class btSubSimplexClosestResult extends BulletBase {
	private long swigCPtr;
	
	protected btSubSimplexClosestResult(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSubSimplexClosestResult, normally you should not need this constructor it's intended for low-level usage. */ 
	public btSubSimplexClosestResult(long cPtr, boolean cMemoryOwn) {
		this("btSubSimplexClosestResult", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btSubSimplexClosestResult obj) {
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
				CollisionJNI.delete_btSubSimplexClosestResult(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setClosestPointOnSimplex(btVector3 value) {
    CollisionJNI.btSubSimplexClosestResult_closestPointOnSimplex_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getClosestPointOnSimplex() {
    long cPtr = CollisionJNI.btSubSimplexClosestResult_closestPointOnSimplex_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setUsedVertices(btUsageBitfield value) {
    CollisionJNI.btSubSimplexClosestResult_usedVertices_set(swigCPtr, this, btUsageBitfield.getCPtr(value), value);
  }

  public btUsageBitfield getUsedVertices() {
    long cPtr = CollisionJNI.btSubSimplexClosestResult_usedVertices_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btUsageBitfield(cPtr, false);
  }

  public void setBarycentricCoords(float[] value) {
    CollisionJNI.btSubSimplexClosestResult_barycentricCoords_set(swigCPtr, this, value);
  }

  public float[] getBarycentricCoords() {
    return CollisionJNI.btSubSimplexClosestResult_barycentricCoords_get(swigCPtr, this);
  }

  public void setDegenerate(boolean value) {
    CollisionJNI.btSubSimplexClosestResult_degenerate_set(swigCPtr, this, value);
  }

  public boolean getDegenerate() {
    return CollisionJNI.btSubSimplexClosestResult_degenerate_get(swigCPtr, this);
  }

  public void reset() {
    CollisionJNI.btSubSimplexClosestResult_reset(swigCPtr, this);
  }

  public boolean isValid() {
    return CollisionJNI.btSubSimplexClosestResult_isValid(swigCPtr, this);
  }

  public void setBarycentricCoordinates(float a, float b, float c, float d) {
    CollisionJNI.btSubSimplexClosestResult_setBarycentricCoordinates__SWIG_0(swigCPtr, this, a, b, c, d);
  }

  public void setBarycentricCoordinates(float a, float b, float c) {
    CollisionJNI.btSubSimplexClosestResult_setBarycentricCoordinates__SWIG_1(swigCPtr, this, a, b, c);
  }

  public void setBarycentricCoordinates(float a, float b) {
    CollisionJNI.btSubSimplexClosestResult_setBarycentricCoordinates__SWIG_2(swigCPtr, this, a, b);
  }

  public void setBarycentricCoordinates(float a) {
    CollisionJNI.btSubSimplexClosestResult_setBarycentricCoordinates__SWIG_3(swigCPtr, this, a);
  }

  public void setBarycentricCoordinates() {
    CollisionJNI.btSubSimplexClosestResult_setBarycentricCoordinates__SWIG_4(swigCPtr, this);
  }

  public btSubSimplexClosestResult() {
    this(CollisionJNI.new_btSubSimplexClosestResult(), true);
  }

}
