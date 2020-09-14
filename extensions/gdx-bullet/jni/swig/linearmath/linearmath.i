%module(directors="1") LinearMath

%include "arrays_java.i"

%{
#ifndef BT_INFINITY
static  int btInfinityMask = 0x7F800000;
#define BT_INFINITY (*(float*)&btInfinityMask)
#endif
%}

%feature("director") btIDebugDraw;

%include "../common/gdxCommon.i"

%ignore btHashString::getHash;
%ignore btTypedObject::getObjectType;

/*
 * btScalar.h defines macros the other types need, so process it first.
 * It also defines some static functions that end up in gdxBulletJNI.java.
 */
%ignore btInfMaskConverter;
%ignore btInfinityMask;
%ignore btGetInfinityMask();
%rename(btSwapEndianInt) btSwapEndian(int);
%rename(operatorbtScalarPtr) operator btScalar*;
%rename(operatorbtConstScalarPtr) operator const btScalar*;
%include "LinearMath/btScalar.h"

%include "btTransform.i"

%{
#include <LinearMath/btVector3.h>
%}
%include <LinearMath/btVector3.h>

%{
#include <LinearMath/btQuadWord.h>
%}
%include <LinearMath/btQuadWord.h>

%{
#include <LinearMath/btQuaternion.h>
%}
%include <LinearMath/btQuaternion.h>

%rename(operatorSubscriptConst) btMatrix3x3::operator [](int) const;
%{
#include <LinearMath/btMatrix3x3.h>
%}
%include <LinearMath/btMatrix3x3.h>

%{
#include <LinearMath/btAabbUtil2.h>
%}
%include "LinearMath/btAabbUtil2.h"

%{
#include <LinearMath/btIDebugDraw.h>
%}
%include "LinearMath/btIDebugDraw.h"

%{
#include <LinearMath/btGeometryUtil.h>

bool btGeometryUtil::isInside(btAlignedObjectArray<btVector3> const&, btVector3 const&, float)
{
	return false;
}
%}
%include "LinearMath/btGeometryUtil.h"

%{
#include <LinearMath/btRandom.h>
%}
%include "LinearMath/btRandom.h"

%{
#include <LinearMath/btTransformUtil.h>
%}
%include "LinearMath/btTransformUtil.h"

%rename(operatorSubscriptConst) int4::operator [](int) const;
%{
#include <LinearMath/btConvexHull.h>
%}
%include "LinearMath/btConvexHull.h"

%{
#include <LinearMath/btGrahamScan2dConvexHull.h>
%}
%include "LinearMath/btGrahamScan2dConvexHull.h"

%rename(getPoolAddressConst) btPoolAllocator::getPoolAddress() const;
%{
#include <LinearMath/btPoolAllocator.h>
%}
%include "LinearMath/btPoolAllocator.h"

%{
#include <LinearMath/btQuickprof.h>
%}
%include "LinearMath/btQuickprof.h"

%{
#include <LinearMath/btConvexHullComputer.h>
%}
%include "LinearMath/btConvexHullComputer.h"

%rename(atConst) btAlignedObjectArray< btVector3 >::at(int) const;
%rename(atConst) btAlignedObjectArray< btScalar >::at(int) const const;
%rename(operatorSubscriptConst) btAlignedObjectArray< btVector3 >::operator[](int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< btScalar >::operator[](int) const;
%{
#include <LinearMath/btAlignedObjectArray.h>
%}
%include "LinearMath/btAlignedObjectArray.h"

%{
#include <LinearMath/btList.h>
%}
%include "LinearMath/btList.h"

%{
#include <LinearMath/btAlignedAllocator.h>
%}
%include "LinearMath/btAlignedAllocator.h"

%immutable btHashString::m_string;
%{
#include <LinearMath/btHashMap.h>
%}
%include "LinearMath/btHashMap.h"

%{
#include <LinearMath/btStackAlloc.h>
%}
%include "LinearMath/btStackAlloc.h"

%{
#include <LinearMath/btMinMax.h>
%}
%include "LinearMath/btMinMax.h"

//%{
//#include <LinearMath/btCpuFeatureUtility.h>
//%}
//%include "LinearMath/btCpuFeatureUtility.h"

%rename(m_colsVar) btMatrixX::m_cols;
%rename(m_rowsVar) btMatrixX::m_rows;
%{
#include <LinearMath/btMatrixX.h>
%}
%include "LinearMath/btMatrixX.h"

%{
#include <LinearMath/btPolarDecomposition.h>
%}
%include "LinearMath/btPolarDecomposition.h"

%immutable btDefaultSerializer::m_skipPointers;
%{
#include <LinearMath/btSerializer.h>
%}
%include "LinearMath/btSerializer.h"

%{
#include <LinearMath/btSpatialAlgebra.h>
%}
%include "LinearMath/btSpatialAlgebra.h"

%{
#include <LinearMath/btStackAlloc.h>
%}
%include "LinearMath/btStackAlloc.h"

%{
#include <LinearMath/btThreads.h>
%}
%include "LinearMath/btThreads.h"

%include "./btMotionState.i"

%include "./btDefaultMotionState.i"

#ifndef BT_NO_PROFILE
void CProfileIterator::Enter_Largest_Child()
{
}
#endif

%template(btVector3Array) btAlignedObjectArray<btVector3>;
%template(btScalarArray) btAlignedObjectArray<btScalar>;
