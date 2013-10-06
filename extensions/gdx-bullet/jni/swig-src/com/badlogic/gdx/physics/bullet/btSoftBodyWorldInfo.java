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

public class btSoftBodyWorldInfo extends BulletBase {
	private long swigCPtr;
	
	protected btSoftBodyWorldInfo(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btSoftBodyWorldInfo(long cPtr, boolean cMemoryOwn) {
		this("btSoftBodyWorldInfo", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btSoftBodyWorldInfo obj) {
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
				gdxBulletJNI.delete_btSoftBodyWorldInfo(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setAir_density(float value) {
    gdxBulletJNI.btSoftBodyWorldInfo_air_density_set(swigCPtr, this, value);
  }

  public float getAir_density() {
    return gdxBulletJNI.btSoftBodyWorldInfo_air_density_get(swigCPtr, this);
  }

  public void setWater_density(float value) {
    gdxBulletJNI.btSoftBodyWorldInfo_water_density_set(swigCPtr, this, value);
  }

  public float getWater_density() {
    return gdxBulletJNI.btSoftBodyWorldInfo_water_density_get(swigCPtr, this);
  }

  public void setWater_offset(float value) {
    gdxBulletJNI.btSoftBodyWorldInfo_water_offset_set(swigCPtr, this, value);
  }

  public float getWater_offset() {
    return gdxBulletJNI.btSoftBodyWorldInfo_water_offset_get(swigCPtr, this);
  }

  public void setWater_normal(btVector3 value) {
    gdxBulletJNI.btSoftBodyWorldInfo_water_normal_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getWater_normal() {
    long cPtr = gdxBulletJNI.btSoftBodyWorldInfo_water_normal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setBroadphase(btBroadphaseInterface value) {
    gdxBulletJNI.btSoftBodyWorldInfo_broadphase_set(swigCPtr, this, btBroadphaseInterface.getCPtr(value), value);
  }

  public btBroadphaseInterface getBroadphase() {
    long cPtr = gdxBulletJNI.btSoftBodyWorldInfo_broadphase_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btBroadphaseInterface(cPtr, false);
  }

  public void setDispatcher(btDispatcher value) {
    gdxBulletJNI.btSoftBodyWorldInfo_dispatcher_set(swigCPtr, this, btDispatcher.getCPtr(value), value);
  }

  public btDispatcher getDispatcher() {
    long cPtr = gdxBulletJNI.btSoftBodyWorldInfo_dispatcher_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btDispatcher(cPtr, false);
  }

  public void setGravity(btVector3 value) {
    gdxBulletJNI.btSoftBodyWorldInfo_gravity_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getGravity() {
    long cPtr = gdxBulletJNI.btSoftBodyWorldInfo_gravity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setSparsesdf(btSparseSdf3 value) {
    gdxBulletJNI.btSoftBodyWorldInfo_sparsesdf_set(swigCPtr, this, btSparseSdf3.getCPtr(value), value);
  }

  public btSparseSdf3 getSparsesdf() {
    long cPtr = gdxBulletJNI.btSoftBodyWorldInfo_sparsesdf_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btSparseSdf3(cPtr, false);
  }

  public btSoftBodyWorldInfo() {
    this(gdxBulletJNI.new_btSoftBodyWorldInfo(), true);
  }

}
