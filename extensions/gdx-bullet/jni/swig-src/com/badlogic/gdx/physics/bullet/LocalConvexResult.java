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

public class LocalConvexResult extends BulletBase {
	private long swigCPtr;
	
	protected LocalConvexResult(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected LocalConvexResult(long cPtr, boolean cMemoryOwn) {
		this("LocalConvexResult", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(LocalConvexResult obj) {
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
				gdxBulletJNI.delete_LocalConvexResult(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public LocalConvexResult(btCollisionObject hitCollisionObject, LocalShapeInfo localShapeInfo, Vector3 hitNormalLocal, Vector3 hitPointLocal, float hitFraction) {
    this(gdxBulletJNI.new_LocalConvexResult(btCollisionObject.getCPtr(hitCollisionObject), hitCollisionObject, LocalShapeInfo.getCPtr(localShapeInfo), localShapeInfo, hitNormalLocal, hitPointLocal, hitFraction), true);
  }

  public void setHitCollisionObject(btCollisionObject value) {
    gdxBulletJNI.LocalConvexResult_hitCollisionObject_set(swigCPtr, this, btCollisionObject.getCPtr(value), value);
  }

  public btCollisionObject getHitCollisionObject() {
	return btCollisionObject.getInstance(gdxBulletJNI.LocalConvexResult_hitCollisionObject_get(swigCPtr, this), false);
}

  public void setLocalShapeInfo(LocalShapeInfo value) {
    gdxBulletJNI.LocalConvexResult_localShapeInfo_set(swigCPtr, this, LocalShapeInfo.getCPtr(value), value);
  }

  public LocalShapeInfo getLocalShapeInfo() {
    long cPtr = gdxBulletJNI.LocalConvexResult_localShapeInfo_get(swigCPtr, this);
    return (cPtr == 0) ? null : new LocalShapeInfo(cPtr, false);
  }

  public void setHitNormalLocal(btVector3 value) {
    gdxBulletJNI.LocalConvexResult_hitNormalLocal_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getHitNormalLocal() {
    long cPtr = gdxBulletJNI.LocalConvexResult_hitNormalLocal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setHitPointLocal(btVector3 value) {
    gdxBulletJNI.LocalConvexResult_hitPointLocal_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getHitPointLocal() {
    long cPtr = gdxBulletJNI.LocalConvexResult_hitPointLocal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setHitFraction(float value) {
    gdxBulletJNI.LocalConvexResult_hitFraction_set(swigCPtr, this, value);
  }

  public float getHitFraction() {
    return gdxBulletJNI.LocalConvexResult_hitFraction_get(swigCPtr, this);
  }

}
