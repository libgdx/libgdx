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

public class btStackAlloc extends BulletBase {
	private long swigCPtr;
	
	protected btStackAlloc(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btStackAlloc(long cPtr, boolean cMemoryOwn) {
		this("btStackAlloc", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btStackAlloc obj) {
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
				gdxBulletJNI.delete_btStackAlloc(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btStackAlloc(long size) {
    this(gdxBulletJNI.new_btStackAlloc(size), true);
  }

  public void create(long size) {
    gdxBulletJNI.btStackAlloc_create(swigCPtr, this, size);
  }

  public void destroy() {
    gdxBulletJNI.btStackAlloc_destroy(swigCPtr, this);
  }

  public int getAvailableMemory() {
    return gdxBulletJNI.btStackAlloc_getAvailableMemory(swigCPtr, this);
  }

  public java.nio.ByteBuffer allocate(long size) {
    return gdxBulletJNI.btStackAlloc_allocate(swigCPtr, this, size);
}

  public btBlock beginBlock() {
    long cPtr = gdxBulletJNI.btStackAlloc_beginBlock(swigCPtr, this);
    return (cPtr == 0) ? null : new btBlock(cPtr, false);
  }

  public void endBlock(btBlock block) {
    gdxBulletJNI.btStackAlloc_endBlock(swigCPtr, this, btBlock.getCPtr(block), block);
  }

}
