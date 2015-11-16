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

public class btGImpactBvh extends BulletBase {
	private long swigCPtr;
	
	protected btGImpactBvh(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btGImpactBvh, normally you should not need this constructor it's intended for low-level usage. */ 
	public btGImpactBvh(long cPtr, boolean cMemoryOwn) {
		this("btGImpactBvh", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btGImpactBvh obj) {
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
				CollisionJNI.delete_btGImpactBvh(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btGImpactBvh() {
    this(CollisionJNI.new_btGImpactBvh__SWIG_0(), true);
  }

  public btGImpactBvh(btPrimitiveManagerBase primitive_manager) {
    this(CollisionJNI.new_btGImpactBvh__SWIG_1(btPrimitiveManagerBase.getCPtr(primitive_manager), primitive_manager), true);
  }

  public btAABB getGlobalBox() {
    return new btAABB(CollisionJNI.btGImpactBvh_getGlobalBox(swigCPtr, this), true);
  }

  public void setPrimitiveManager(btPrimitiveManagerBase primitive_manager) {
    CollisionJNI.btGImpactBvh_setPrimitiveManager(swigCPtr, this, btPrimitiveManagerBase.getCPtr(primitive_manager), primitive_manager);
  }

  public btPrimitiveManagerBase getPrimitiveManager() {
    long cPtr = CollisionJNI.btGImpactBvh_getPrimitiveManager(swigCPtr, this);
    return (cPtr == 0) ? null : new btPrimitiveManagerBase(cPtr, false);
  }

  public void update() {
    CollisionJNI.btGImpactBvh_update(swigCPtr, this);
  }

  public void buildSet() {
    CollisionJNI.btGImpactBvh_buildSet(swigCPtr, this);
  }

  public boolean boxQuery(btAABB box, SWIGTYPE_p_btAlignedObjectArrayT_int_t collided_results) {
    return CollisionJNI.btGImpactBvh_boxQuery(swigCPtr, this, btAABB.getCPtr(box), box, SWIGTYPE_p_btAlignedObjectArrayT_int_t.getCPtr(collided_results));
  }

  public boolean boxQueryTrans(btAABB box, Matrix4 transform, SWIGTYPE_p_btAlignedObjectArrayT_int_t collided_results) {
    return CollisionJNI.btGImpactBvh_boxQueryTrans(swigCPtr, this, btAABB.getCPtr(box), box, transform, SWIGTYPE_p_btAlignedObjectArrayT_int_t.getCPtr(collided_results));
  }

  public boolean rayQuery(Vector3 ray_dir, Vector3 ray_origin, SWIGTYPE_p_btAlignedObjectArrayT_int_t collided_results) {
    return CollisionJNI.btGImpactBvh_rayQuery(swigCPtr, this, ray_dir, ray_origin, SWIGTYPE_p_btAlignedObjectArrayT_int_t.getCPtr(collided_results));
  }

  public boolean hasHierarchy() {
    return CollisionJNI.btGImpactBvh_hasHierarchy(swigCPtr, this);
  }

  public boolean isTrimesh() {
    return CollisionJNI.btGImpactBvh_isTrimesh(swigCPtr, this);
  }

  public int getNodeCount() {
    return CollisionJNI.btGImpactBvh_getNodeCount(swigCPtr, this);
  }

  public boolean isLeafNode(int nodeindex) {
    return CollisionJNI.btGImpactBvh_isLeafNode(swigCPtr, this, nodeindex);
  }

  public int getNodeData(int nodeindex) {
    return CollisionJNI.btGImpactBvh_getNodeData(swigCPtr, this, nodeindex);
  }

  public void getNodeBound(int nodeindex, btAABB bound) {
    CollisionJNI.btGImpactBvh_getNodeBound(swigCPtr, this, nodeindex, btAABB.getCPtr(bound), bound);
  }

  public void setNodeBound(int nodeindex, btAABB bound) {
    CollisionJNI.btGImpactBvh_setNodeBound(swigCPtr, this, nodeindex, btAABB.getCPtr(bound), bound);
  }

  public int getLeftNode(int nodeindex) {
    return CollisionJNI.btGImpactBvh_getLeftNode(swigCPtr, this, nodeindex);
  }

  public int getRightNode(int nodeindex) {
    return CollisionJNI.btGImpactBvh_getRightNode(swigCPtr, this, nodeindex);
  }

  public int getEscapeNodeIndex(int nodeindex) {
    return CollisionJNI.btGImpactBvh_getEscapeNodeIndex(swigCPtr, this, nodeindex);
  }

  public void getNodeTriangle(int nodeindex, btPrimitiveTriangle triangle) {
    CollisionJNI.btGImpactBvh_getNodeTriangle(swigCPtr, this, nodeindex, btPrimitiveTriangle.getCPtr(triangle), triangle);
  }

  public GIM_BVH_TREE_NODE get_node_pointer(int index) {
    long cPtr = CollisionJNI.btGImpactBvh_get_node_pointer__SWIG_0(swigCPtr, this, index);
    return (cPtr == 0) ? null : new GIM_BVH_TREE_NODE(cPtr, false);
  }

  public GIM_BVH_TREE_NODE get_node_pointer() {
    long cPtr = CollisionJNI.btGImpactBvh_get_node_pointer__SWIG_1(swigCPtr, this);
    return (cPtr == 0) ? null : new GIM_BVH_TREE_NODE(cPtr, false);
  }

  public static void find_collision(btGImpactBvh boxset1, Matrix4 trans1, btGImpactBvh boxset2, Matrix4 trans2, btPairSet collision_pairs) {
    CollisionJNI.btGImpactBvh_find_collision(btGImpactBvh.getCPtr(boxset1), boxset1, trans1, btGImpactBvh.getCPtr(boxset2), boxset2, trans2, btPairSet.getCPtr(collision_pairs), collision_pairs);
  }

}
