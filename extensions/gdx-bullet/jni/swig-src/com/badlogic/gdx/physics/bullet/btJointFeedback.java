/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

public class btJointFeedback extends BulletBase {
	private long swigCPtr;
	
	protected btJointFeedback(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btJointFeedback(long cPtr, boolean cMemoryOwn) {
		this("btJointFeedback", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btJointFeedback obj) {
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
				gdxBulletJNI.delete_btJointFeedback(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setAppliedForceBodyA(btVector3 value) {
    gdxBulletJNI.btJointFeedback_appliedForceBodyA_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getAppliedForceBodyA() {
    long cPtr = gdxBulletJNI.btJointFeedback_appliedForceBodyA_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setAppliedTorqueBodyA(btVector3 value) {
    gdxBulletJNI.btJointFeedback_appliedTorqueBodyA_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getAppliedTorqueBodyA() {
    long cPtr = gdxBulletJNI.btJointFeedback_appliedTorqueBodyA_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setAppliedForceBodyB(btVector3 value) {
    gdxBulletJNI.btJointFeedback_appliedForceBodyB_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getAppliedForceBodyB() {
    long cPtr = gdxBulletJNI.btJointFeedback_appliedForceBodyB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setAppliedTorqueBodyB(btVector3 value) {
    gdxBulletJNI.btJointFeedback_appliedTorqueBodyB_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getAppliedTorqueBodyB() {
    long cPtr = gdxBulletJNI.btJointFeedback_appliedTorqueBodyB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public btJointFeedback() {
    this(gdxBulletJNI.new_btJointFeedback(), true);
  }

}
