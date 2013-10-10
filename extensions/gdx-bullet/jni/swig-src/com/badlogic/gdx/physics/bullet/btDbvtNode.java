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

public class btDbvtNode extends BulletBase {
	private long swigCPtr;
	
	protected btDbvtNode(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btDbvtNode(long cPtr, boolean cMemoryOwn) {
		this("btDbvtNode", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btDbvtNode obj) {
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
				gdxBulletJNI.delete_btDbvtNode(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setVolume(btDbvtAabbMm value) {
    gdxBulletJNI.btDbvtNode_volume_set(swigCPtr, this, btDbvtAabbMm.getCPtr(value), value);
  }

  public btDbvtAabbMm getVolume() {
    long cPtr = gdxBulletJNI.btDbvtNode_volume_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btDbvtAabbMm(cPtr, false);
  }

  public void setParent(btDbvtNode value) {
    gdxBulletJNI.btDbvtNode_parent_set(swigCPtr, this, btDbvtNode.getCPtr(value), value);
  }

  public btDbvtNode getParent() {
    long cPtr = gdxBulletJNI.btDbvtNode_parent_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btDbvtNode(cPtr, false);
  }

  public boolean isleaf() {
    return gdxBulletJNI.btDbvtNode_isleaf(swigCPtr, this);
  }

  public boolean isinternal() {
    return gdxBulletJNI.btDbvtNode_isinternal(swigCPtr, this);
  }

  public btDbvtNode() {
    this(gdxBulletJNI.new_btDbvtNode(), true);
  }

}
