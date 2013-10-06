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

public class btSimpleBroadphaseProxy extends btBroadphaseProxy {
	private long swigCPtr;
	
	protected btSimpleBroadphaseProxy(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, gdxBulletJNI.btSimpleBroadphaseProxy_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btSimpleBroadphaseProxy(long cPtr, boolean cMemoryOwn) {
		this("btSimpleBroadphaseProxy", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btSimpleBroadphaseProxy obj) {
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
				gdxBulletJNI.delete_btSimpleBroadphaseProxy(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setNextFree(int value) {
    gdxBulletJNI.btSimpleBroadphaseProxy_nextFree_set(swigCPtr, this, value);
  }

  public int getNextFree() {
    return gdxBulletJNI.btSimpleBroadphaseProxy_nextFree_get(swigCPtr, this);
  }

  public btSimpleBroadphaseProxy() {
    this(gdxBulletJNI.new_btSimpleBroadphaseProxy__SWIG_0(), true);
  }

  public btSimpleBroadphaseProxy(Vector3 minpt, Vector3 maxpt, int shapeType, SWIGTYPE_p_void userPtr, short collisionFilterGroup, short collisionFilterMask, SWIGTYPE_p_void multiSapProxy) {
    this(gdxBulletJNI.new_btSimpleBroadphaseProxy__SWIG_1(minpt, maxpt, shapeType, SWIGTYPE_p_void.getCPtr(userPtr), collisionFilterGroup, collisionFilterMask, SWIGTYPE_p_void.getCPtr(multiSapProxy)), true);
  }

  public void SetNextFree(int next) {
    gdxBulletJNI.btSimpleBroadphaseProxy_SetNextFree(swigCPtr, this, next);
  }

  public int GetNextFree() {
    return gdxBulletJNI.btSimpleBroadphaseProxy_GetNextFree(swigCPtr, this);
  }

}
