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

public class btGeneric6DofSpringConstraintData extends BulletBase {
	private long swigCPtr;
	
	protected btGeneric6DofSpringConstraintData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btGeneric6DofSpringConstraintData(long cPtr, boolean cMemoryOwn) {
		this("btGeneric6DofSpringConstraintData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btGeneric6DofSpringConstraintData obj) {
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
				gdxBulletJNI.delete_btGeneric6DofSpringConstraintData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void set6dofData(btGeneric6DofConstraintData value) {
    gdxBulletJNI.btGeneric6DofSpringConstraintData_6dofData_set(swigCPtr, this, btGeneric6DofConstraintData.getCPtr(value), value);
  }

  public btGeneric6DofConstraintData get6dofData() {
    long cPtr = gdxBulletJNI.btGeneric6DofSpringConstraintData_6dofData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btGeneric6DofConstraintData(cPtr, false);
  }

  public void setSpringEnabled(int[] value) {
    gdxBulletJNI.btGeneric6DofSpringConstraintData_springEnabled_set(swigCPtr, this, value);
  }

  public int[] getSpringEnabled() {
    return gdxBulletJNI.btGeneric6DofSpringConstraintData_springEnabled_get(swigCPtr, this);
}

  public void setEquilibriumPoint(float[] value) {
    gdxBulletJNI.btGeneric6DofSpringConstraintData_equilibriumPoint_set(swigCPtr, this, value);
  }

  public float[] getEquilibriumPoint() {
    return gdxBulletJNI.btGeneric6DofSpringConstraintData_equilibriumPoint_get(swigCPtr, this);
}

  public void setSpringStiffness(float[] value) {
    gdxBulletJNI.btGeneric6DofSpringConstraintData_springStiffness_set(swigCPtr, this, value);
  }

  public float[] getSpringStiffness() {
    return gdxBulletJNI.btGeneric6DofSpringConstraintData_springStiffness_get(swigCPtr, this);
}

  public void setSpringDamping(float[] value) {
    gdxBulletJNI.btGeneric6DofSpringConstraintData_springDamping_set(swigCPtr, this, value);
  }

  public float[] getSpringDamping() {
    return gdxBulletJNI.btGeneric6DofSpringConstraintData_springDamping_get(swigCPtr, this);
}

  public btGeneric6DofSpringConstraintData() {
    this(gdxBulletJNI.new_btGeneric6DofSpringConstraintData(), true);
  }

}
