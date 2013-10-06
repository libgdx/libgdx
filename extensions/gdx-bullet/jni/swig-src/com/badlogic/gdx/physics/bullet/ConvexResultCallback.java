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

public class ConvexResultCallback extends BulletBase {
	private long swigCPtr;
	
	protected ConvexResultCallback(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected ConvexResultCallback(long cPtr, boolean cMemoryOwn) {
		this("ConvexResultCallback", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(ConvexResultCallback obj) {
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
				gdxBulletJNI.delete_ConvexResultCallback(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    gdxBulletJNI.ConvexResultCallback_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    gdxBulletJNI.ConvexResultCallback_change_ownership(this, swigCPtr, true);
  }

  public void setClosestHitFraction(float value) {
    gdxBulletJNI.ConvexResultCallback_closestHitFraction_set(swigCPtr, this, value);
  }

  public float getClosestHitFraction() {
    return gdxBulletJNI.ConvexResultCallback_closestHitFraction_get(swigCPtr, this);
  }

  public void setCollisionFilterGroup(short value) {
    gdxBulletJNI.ConvexResultCallback_collisionFilterGroup_set(swigCPtr, this, value);
  }

  public short getCollisionFilterGroup() {
    return gdxBulletJNI.ConvexResultCallback_collisionFilterGroup_get(swigCPtr, this);
  }

  public void setCollisionFilterMask(short value) {
    gdxBulletJNI.ConvexResultCallback_collisionFilterMask_set(swigCPtr, this, value);
  }

  public short getCollisionFilterMask() {
    return gdxBulletJNI.ConvexResultCallback_collisionFilterMask_get(swigCPtr, this);
  }

  public ConvexResultCallback() {
    this(gdxBulletJNI.new_ConvexResultCallback(), true);
    gdxBulletJNI.ConvexResultCallback_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public boolean hasHit() {
    return gdxBulletJNI.ConvexResultCallback_hasHit(swigCPtr, this);
  }

  public boolean needsCollision(btBroadphaseProxy proxy0) {
    return (getClass() == ConvexResultCallback.class) ? gdxBulletJNI.ConvexResultCallback_needsCollision(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy0), proxy0) : gdxBulletJNI.ConvexResultCallback_needsCollisionSwigExplicitConvexResultCallback(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy0), proxy0);
  }

  public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
    return gdxBulletJNI.ConvexResultCallback_addSingleResult(swigCPtr, this, LocalConvexResult.getCPtr(convexResult), convexResult, normalInWorldSpace);
  }

}
