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

public class btTriangleRaycastCallback extends btTriangleCallback {
	private long swigCPtr;
	
	protected btTriangleRaycastCallback(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, gdxBulletJNI.btTriangleRaycastCallback_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btTriangleRaycastCallback(long cPtr, boolean cMemoryOwn) {
		this("btTriangleRaycastCallback", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btTriangleRaycastCallback obj) {
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
				gdxBulletJNI.delete_btTriangleRaycastCallback(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setFrom(btVector3 value) {
    gdxBulletJNI.btTriangleRaycastCallback_from_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getFrom() {
    long cPtr = gdxBulletJNI.btTriangleRaycastCallback_from_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setTo(btVector3 value) {
    gdxBulletJNI.btTriangleRaycastCallback_to_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getTo() {
    long cPtr = gdxBulletJNI.btTriangleRaycastCallback_to_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setFlags(long value) {
    gdxBulletJNI.btTriangleRaycastCallback_flags_set(swigCPtr, this, value);
  }

  public long getFlags() {
    return gdxBulletJNI.btTriangleRaycastCallback_flags_get(swigCPtr, this);
  }

  public void setHitFraction(float value) {
    gdxBulletJNI.btTriangleRaycastCallback_hitFraction_set(swigCPtr, this, value);
  }

  public float getHitFraction() {
    return gdxBulletJNI.btTriangleRaycastCallback_hitFraction_get(swigCPtr, this);
  }

  public float reportHit(Vector3 hitNormalLocal, float hitFraction, int partId, int triangleIndex) {
    return gdxBulletJNI.btTriangleRaycastCallback_reportHit(swigCPtr, this, hitNormalLocal, hitFraction, partId, triangleIndex);
  }

  public final static class EFlags {
    public final static int kF_None = 0;
    public final static int kF_FilterBackfaces = 1 << 0;
    public final static int kF_KeepUnflippedNormal = 1 << 1;
    public final static int kF_Terminator = 0xFFFFFFFF;
  }

}
