/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btBvhTree extends BulletBase {
	private long swigCPtr;
	
	protected btBvhTree(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btBvhTree, normally you should not need this constructor it's intended for low-level usage. */ 
	public btBvhTree(long cPtr, boolean cMemoryOwn) {
		this("btBvhTree", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btBvhTree obj) {
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
				CollisionJNI.delete_btBvhTree(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btBvhTree() {
    this(CollisionJNI.new_btBvhTree(), true);
  }

  public void build_tree(GIM_BVH_DATA_ARRAY primitive_boxes) {
    CollisionJNI.btBvhTree_build_tree(swigCPtr, this, GIM_BVH_DATA_ARRAY.getCPtr(primitive_boxes), primitive_boxes);
  }

  public void clearNodes() {
    CollisionJNI.btBvhTree_clearNodes(swigCPtr, this);
  }

  public int getNodeCount() {
    return CollisionJNI.btBvhTree_getNodeCount(swigCPtr, this);
  }

  public boolean isLeafNode(int nodeindex) {
    return CollisionJNI.btBvhTree_isLeafNode(swigCPtr, this, nodeindex);
  }

  public int getNodeData(int nodeindex) {
    return CollisionJNI.btBvhTree_getNodeData(swigCPtr, this, nodeindex);
  }

  public void getNodeBound(int nodeindex, btAABB bound) {
    CollisionJNI.btBvhTree_getNodeBound(swigCPtr, this, nodeindex, btAABB.getCPtr(bound), bound);
  }

  public void setNodeBound(int nodeindex, btAABB bound) {
    CollisionJNI.btBvhTree_setNodeBound(swigCPtr, this, nodeindex, btAABB.getCPtr(bound), bound);
  }

  public int getLeftNode(int nodeindex) {
    return CollisionJNI.btBvhTree_getLeftNode(swigCPtr, this, nodeindex);
  }

  public int getRightNode(int nodeindex) {
    return CollisionJNI.btBvhTree_getRightNode(swigCPtr, this, nodeindex);
  }

  public int getEscapeNodeIndex(int nodeindex) {
    return CollisionJNI.btBvhTree_getEscapeNodeIndex(swigCPtr, this, nodeindex);
  }

  public GIM_BVH_TREE_NODE get_node_pointer(int index) {
    long cPtr = CollisionJNI.btBvhTree_get_node_pointer__SWIG_0(swigCPtr, this, index);
    return (cPtr == 0) ? null : new GIM_BVH_TREE_NODE(cPtr, false);
  }

  public GIM_BVH_TREE_NODE get_node_pointer() {
    long cPtr = CollisionJNI.btBvhTree_get_node_pointer__SWIG_1(swigCPtr, this);
    return (cPtr == 0) ? null : new GIM_BVH_TREE_NODE(cPtr, false);
  }

}
