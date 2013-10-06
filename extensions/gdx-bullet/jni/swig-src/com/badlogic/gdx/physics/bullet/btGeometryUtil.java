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

public class btGeometryUtil extends BulletBase {
	private long swigCPtr;
	
	protected btGeometryUtil(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btGeometryUtil(long cPtr, boolean cMemoryOwn) {
		this("btGeometryUtil", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btGeometryUtil obj) {
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
				gdxBulletJNI.delete_btGeometryUtil(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public static void getPlaneEquationsFromVertices(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t vertices, SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t planeEquationsOut) {
    gdxBulletJNI.btGeometryUtil_getPlaneEquationsFromVertices(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(vertices), SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(planeEquationsOut));
  }

  public static void getVerticesFromPlaneEquations(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t planeEquations, SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t verticesOut) {
    gdxBulletJNI.btGeometryUtil_getVerticesFromPlaneEquations(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(planeEquations), SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(verticesOut));
  }

  public static boolean isInside(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t vertices, Vector3 planeNormal, float margin) {
    return gdxBulletJNI.btGeometryUtil_isInside(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(vertices), planeNormal, margin);
  }

  public static boolean isPointInsidePlanes(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t planeEquations, Vector3 point, float margin) {
    return gdxBulletJNI.btGeometryUtil_isPointInsidePlanes(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(planeEquations), point, margin);
  }

  public static boolean areVerticesBehindPlane(Vector3 planeNormal, SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t vertices, float margin) {
    return gdxBulletJNI.btGeometryUtil_areVerticesBehindPlane(planeNormal, SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(vertices), margin);
  }

  public btGeometryUtil() {
    this(gdxBulletJNI.new_btGeometryUtil(), true);
  }

}
