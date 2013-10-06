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

public class btBvhSubtreeInfo extends BulletBase {
	private long swigCPtr;
	
	protected btBvhSubtreeInfo(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btBvhSubtreeInfo(long cPtr, boolean cMemoryOwn) {
		this("btBvhSubtreeInfo", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btBvhSubtreeInfo obj) {
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
				gdxBulletJNI.delete_btBvhSubtreeInfo(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setQuantizedAabbMin(int[] value) {
    gdxBulletJNI.btBvhSubtreeInfo_quantizedAabbMin_set(swigCPtr, this, value);
  }

  public int[] getQuantizedAabbMin() {
    return gdxBulletJNI.btBvhSubtreeInfo_quantizedAabbMin_get(swigCPtr, this);
  }

  public void setQuantizedAabbMax(int[] value) {
    gdxBulletJNI.btBvhSubtreeInfo_quantizedAabbMax_set(swigCPtr, this, value);
  }

  public int[] getQuantizedAabbMax() {
    return gdxBulletJNI.btBvhSubtreeInfo_quantizedAabbMax_get(swigCPtr, this);
  }

  public void setRootNodeIndex(int value) {
    gdxBulletJNI.btBvhSubtreeInfo_rootNodeIndex_set(swigCPtr, this, value);
  }

  public int getRootNodeIndex() {
    return gdxBulletJNI.btBvhSubtreeInfo_rootNodeIndex_get(swigCPtr, this);
  }

  public void setSubtreeSize(int value) {
    gdxBulletJNI.btBvhSubtreeInfo_subtreeSize_set(swigCPtr, this, value);
  }

  public int getSubtreeSize() {
    return gdxBulletJNI.btBvhSubtreeInfo_subtreeSize_get(swigCPtr, this);
  }

  public void setPadding(int[] value) {
    gdxBulletJNI.btBvhSubtreeInfo_padding_set(swigCPtr, this, value);
  }

  public int[] getPadding() {
    return gdxBulletJNI.btBvhSubtreeInfo_padding_get(swigCPtr, this);
  }

  public btBvhSubtreeInfo() {
    this(gdxBulletJNI.new_btBvhSubtreeInfo(), true);
  }

  public void setAabbFromQuantizeNode(btQuantizedBvhNode quantizedNode) {
    gdxBulletJNI.btBvhSubtreeInfo_setAabbFromQuantizeNode(swigCPtr, this, btQuantizedBvhNode.getCPtr(quantizedNode), quantizedNode);
  }

}
