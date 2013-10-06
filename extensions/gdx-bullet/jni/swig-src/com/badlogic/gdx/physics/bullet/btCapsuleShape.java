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

public class btCapsuleShape extends btConvexInternalShape {
	private long swigCPtr;
	
	protected btCapsuleShape(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, gdxBulletJNI.btCapsuleShape_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btCapsuleShape(long cPtr, boolean cMemoryOwn) {
		this("btCapsuleShape", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btCapsuleShape obj) {
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
				gdxBulletJNI.delete_btCapsuleShape(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btCapsuleShape(float radius, float height) {
    this(gdxBulletJNI.new_btCapsuleShape__SWIG_1(radius, height), true);
  }

  public int getUpAxis() {
    return gdxBulletJNI.btCapsuleShape_getUpAxis(swigCPtr, this);
  }

  public float getRadius() {
    return gdxBulletJNI.btCapsuleShape_getRadius(swigCPtr, this);
  }

  public float getHalfHeight() {
    return gdxBulletJNI.btCapsuleShape_getHalfHeight(swigCPtr, this);
  }

}
