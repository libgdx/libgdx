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

public class btStaticPlaneShapeData extends BulletBase {
	private long swigCPtr;
	
	protected btStaticPlaneShapeData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btStaticPlaneShapeData(long cPtr, boolean cMemoryOwn) {
		this("btStaticPlaneShapeData", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btStaticPlaneShapeData obj) {
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
				gdxBulletJNI.delete_btStaticPlaneShapeData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setCollisionShapeData(btCollisionShapeData value) {
    gdxBulletJNI.btStaticPlaneShapeData_collisionShapeData_set(swigCPtr, this, btCollisionShapeData.getCPtr(value), value);
  }

  public btCollisionShapeData getCollisionShapeData() {
    long cPtr = gdxBulletJNI.btStaticPlaneShapeData_collisionShapeData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionShapeData(cPtr, false);
  }

  public void setLocalScaling(btVector3FloatData value) {
    gdxBulletJNI.btStaticPlaneShapeData_localScaling_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getLocalScaling() {
    long cPtr = gdxBulletJNI.btStaticPlaneShapeData_localScaling_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setPlaneNormal(btVector3FloatData value) {
    gdxBulletJNI.btStaticPlaneShapeData_planeNormal_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getPlaneNormal() {
    long cPtr = gdxBulletJNI.btStaticPlaneShapeData_planeNormal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setPlaneConstant(float value) {
    gdxBulletJNI.btStaticPlaneShapeData_planeConstant_set(swigCPtr, this, value);
  }

  public float getPlaneConstant() {
    return gdxBulletJNI.btStaticPlaneShapeData_planeConstant_get(swigCPtr, this);
  }

  public void setPad(String value) {
    gdxBulletJNI.btStaticPlaneShapeData_pad_set(swigCPtr, this, value);
  }

  public String getPad() {
    return gdxBulletJNI.btStaticPlaneShapeData_pad_get(swigCPtr, this);
  }

  public btStaticPlaneShapeData() {
    this(gdxBulletJNI.new_btStaticPlaneShapeData(), true);
  }

}
