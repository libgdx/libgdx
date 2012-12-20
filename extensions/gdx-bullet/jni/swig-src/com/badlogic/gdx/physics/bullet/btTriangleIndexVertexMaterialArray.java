/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btTriangleIndexVertexMaterialArray extends btTriangleIndexVertexArray {
  private long swigCPtr;

  protected btTriangleIndexVertexMaterialArray(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btTriangleIndexVertexMaterialArray_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btTriangleIndexVertexMaterialArray obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btTriangleIndexVertexMaterialArray(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public btTriangleIndexVertexMaterialArray() {
    this(gdxBulletJNI.new_btTriangleIndexVertexMaterialArray__SWIG_0(), true);
  }

  public btTriangleIndexVertexMaterialArray(int numTriangles, SWIGTYPE_p_int triangleIndexBase, int triangleIndexStride, int numVertices, float[] vertexBase, int vertexStride, int numMaterials, SWIGTYPE_p_unsigned_char materialBase, int materialStride, SWIGTYPE_p_int triangleMaterialsBase, int materialIndexStride) {
    this(gdxBulletJNI.new_btTriangleIndexVertexMaterialArray__SWIG_1(numTriangles, SWIGTYPE_p_int.getCPtr(triangleIndexBase), triangleIndexStride, numVertices, vertexBase, vertexStride, numMaterials, SWIGTYPE_p_unsigned_char.getCPtr(materialBase), materialStride, SWIGTYPE_p_int.getCPtr(triangleMaterialsBase), materialIndexStride), true);
  }

  public void addMaterialProperties(btMaterialProperties mat, int triangleType) {
    gdxBulletJNI.btTriangleIndexVertexMaterialArray_addMaterialProperties__SWIG_0(swigCPtr, this, btMaterialProperties.getCPtr(mat), mat, triangleType);
  }

  public void addMaterialProperties(btMaterialProperties mat) {
    gdxBulletJNI.btTriangleIndexVertexMaterialArray_addMaterialProperties__SWIG_1(swigCPtr, this, btMaterialProperties.getCPtr(mat), mat);
  }

  public void getLockedMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType, int subpart) {
    gdxBulletJNI.btTriangleIndexVertexMaterialArray_getLockedMaterialBase__SWIG_0(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType), subpart);
  }

  public void getLockedMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType) {
    gdxBulletJNI.btTriangleIndexVertexMaterialArray_getLockedMaterialBase__SWIG_1(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType));
  }

  public void getLockedReadOnlyMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType, int subpart) {
    gdxBulletJNI.btTriangleIndexVertexMaterialArray_getLockedReadOnlyMaterialBase__SWIG_0(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType), subpart);
  }

  public void getLockedReadOnlyMaterialBase(SWIGTYPE_p_p_unsigned_char materialBase, SWIGTYPE_p_int numMaterials, SWIGTYPE_p_PHY_ScalarType materialType, SWIGTYPE_p_int materialStride, SWIGTYPE_p_p_unsigned_char triangleMaterialBase, SWIGTYPE_p_int numTriangles, SWIGTYPE_p_int triangleMaterialStride, SWIGTYPE_p_PHY_ScalarType triangleType) {
    gdxBulletJNI.btTriangleIndexVertexMaterialArray_getLockedReadOnlyMaterialBase__SWIG_1(swigCPtr, this, SWIGTYPE_p_p_unsigned_char.getCPtr(materialBase), SWIGTYPE_p_int.getCPtr(numMaterials), SWIGTYPE_p_PHY_ScalarType.getCPtr(materialType), SWIGTYPE_p_int.getCPtr(materialStride), SWIGTYPE_p_p_unsigned_char.getCPtr(triangleMaterialBase), SWIGTYPE_p_int.getCPtr(numTriangles), SWIGTYPE_p_int.getCPtr(triangleMaterialStride), SWIGTYPE_p_PHY_ScalarType.getCPtr(triangleType));
  }

}
