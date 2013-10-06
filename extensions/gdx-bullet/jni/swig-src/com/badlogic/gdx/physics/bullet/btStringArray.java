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

public class btStringArray extends BulletBase {
	private long swigCPtr;
	
	protected btStringArray(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btStringArray(long cPtr, boolean cMemoryOwn) {
		this("btStringArray", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btStringArray obj) {
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
				gdxBulletJNI.delete_btStringArray(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btStringArray() {
    this(gdxBulletJNI.new_btStringArray__SWIG_0(), true);
  }

  public btStringArray(btStringArray otherArray) {
    this(gdxBulletJNI.new_btStringArray__SWIG_1(btStringArray.getCPtr(otherArray), otherArray), true);
  }

  public int size() {
    return gdxBulletJNI.btStringArray_size(swigCPtr, this);
  }

  public String at(int n) {
    return gdxBulletJNI.btStringArray_at__SWIG_0(swigCPtr, this, n);
  }

  public void clear() {
    gdxBulletJNI.btStringArray_clear(swigCPtr, this);
  }

  public void pop_back() {
    gdxBulletJNI.btStringArray_pop_back(swigCPtr, this);
  }

  public void resizeNoInitialize(int newsize) {
    gdxBulletJNI.btStringArray_resizeNoInitialize(swigCPtr, this, newsize);
  }

  public void resize(int newsize, String fillData) {
    gdxBulletJNI.btStringArray_resize__SWIG_0(swigCPtr, this, newsize, fillData);
  }

  public void resize(int newsize) {
    gdxBulletJNI.btStringArray_resize__SWIG_1(swigCPtr, this, newsize);
  }

  public String expandNonInitializing() {
    return gdxBulletJNI.btStringArray_expandNonInitializing(swigCPtr, this);
  }

  public String expand(String fillValue) {
    return gdxBulletJNI.btStringArray_expand__SWIG_0(swigCPtr, this, fillValue);
  }

  public String expand() {
    return gdxBulletJNI.btStringArray_expand__SWIG_1(swigCPtr, this);
  }

  public void push_back(String _Val) {
    gdxBulletJNI.btStringArray_push_back(swigCPtr, this, _Val);
  }

  public int capacity() {
    return gdxBulletJNI.btStringArray_capacity(swigCPtr, this);
  }

  public void reserve(int _Count) {
    gdxBulletJNI.btStringArray_reserve(swigCPtr, this, _Count);
  }

  public void swap(int index0, int index1) {
    gdxBulletJNI.btStringArray_swap(swigCPtr, this, index0, index1);
  }

  public int findBinarySearch(String key) {
    return gdxBulletJNI.btStringArray_findBinarySearch(swigCPtr, this, key);
  }

  public int findLinearSearch(String key) {
    return gdxBulletJNI.btStringArray_findLinearSearch(swigCPtr, this, key);
  }

  public void remove(String key) {
    gdxBulletJNI.btStringArray_remove(swigCPtr, this, key);
  }

  public void initializeFromBuffer(SWIGTYPE_p_void buffer, int size, int capacity) {
    gdxBulletJNI.btStringArray_initializeFromBuffer(swigCPtr, this, SWIGTYPE_p_void.getCPtr(buffer), size, capacity);
  }

  public void copyFromArray(btStringArray otherArray) {
    gdxBulletJNI.btStringArray_copyFromArray(swigCPtr, this, btStringArray.getCPtr(otherArray), otherArray);
  }

}
