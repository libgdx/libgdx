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

public class btTriangleIndexVertexMaterialArray extends btTriangleIndexVertexArray {
	private long swigCPtr;
	
	protected btTriangleIndexVertexMaterialArray(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btTriangleIndexVertexMaterialArray_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btTriangleIndexVertexMaterialArray, normally you should not need this constructor it's intended for low-level usage. */
	public btTriangleIndexVertexMaterialArray(long cPtr, boolean cMemoryOwn) {
		this("btTriangleIndexVertexMaterialArray", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btTriangleIndexVertexMaterialArray_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btTriangleIndexVertexMaterialArray obj) {
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
				CollisionJNI.delete_btTriangleIndexVertexMaterialArray(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btTriangleIndexVertexMaterialArray() {
    this(CollisionJNI.new_btTriangleIndexVertexMaterialArray__SWIG_0(), true);
  }

  static private long SwigConstructbtTriangleIndexVertexMaterialArray(int numTriangles, java.nio.IntBuffer triangleIndexBase, int triangleIndexStride, int numVertices, java.nio.FloatBuffer vertexBase, int vertexStride, int numMaterials, java.nio.ByteBuffer materialBase, int materialStride, java.nio.IntBuffer triangleMaterialsBase, int materialIndexStride) {
    assert triangleIndexBase.isDirect() : "Buffer must be allocated direct.";
    assert vertexBase.isDirect() : "Buffer must be allocated direct.";
    assert materialBase.isDirect() : "Buffer must be allocated direct.";
    assert triangleMaterialsBase.isDirect() : "Buffer must be allocated direct.";
    return CollisionJNI.new_btTriangleIndexVertexMaterialArray__SWIG_1(numTriangles, triangleIndexBase, triangleIndexStride, numVertices, vertexBase, vertexStride, numMaterials, materialBase, materialStride, triangleMaterialsBase, materialIndexStride);
  }

  public btTriangleIndexVertexMaterialArray(int numTriangles, java.nio.IntBuffer triangleIndexBase, int triangleIndexStride, int numVertices, java.nio.FloatBuffer vertexBase, int vertexStride, int numMaterials, java.nio.ByteBuffer materialBase, int materialStride, java.nio.IntBuffer triangleMaterialsBase, int materialIndexStride) {
    this(btTriangleIndexVertexMaterialArray.SwigConstructbtTriangleIndexVertexMaterialArray(numTriangles, triangleIndexBase, triangleIndexStride, numVertices, vertexBase, vertexStride, numMaterials, materialBase, materialStride, triangleMaterialsBase, materialIndexStride), true);
  }

  public void addMaterialProperties(btMaterialProperties mat, int triangleType) {
    CollisionJNI.btTriangleIndexVertexMaterialArray_addMaterialProperties__SWIG_0(swigCPtr, this, btMaterialProperties.getCPtr(mat), mat, triangleType);
  }

  public void addMaterialProperties(btMaterialProperties mat) {
    CollisionJNI.btTriangleIndexVertexMaterialArray_addMaterialProperties__SWIG_1(swigCPtr, this, btMaterialProperties.getCPtr(mat), mat);
  }

  public void getLockedMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType, int subpart) {
    CollisionJNI.btTriangleIndexVertexMaterialArray_getLockedMaterialBase__SWIG_0(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType), subpart);
  }

  public void getLockedMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType) {
    CollisionJNI.btTriangleIndexVertexMaterialArray_getLockedMaterialBase__SWIG_1(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType));
  }

  public void getLockedReadOnlyMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType, int subpart) {
    CollisionJNI.btTriangleIndexVertexMaterialArray_getLockedReadOnlyMaterialBase__SWIG_0(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType), subpart);
  }

  public void getLockedReadOnlyMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType) {
    CollisionJNI.btTriangleIndexVertexMaterialArray_getLockedReadOnlyMaterialBase__SWIG_1(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType));
  }

}
