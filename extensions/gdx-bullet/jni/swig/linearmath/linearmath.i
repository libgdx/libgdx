%module(directors="1") LinearMath
		
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
%include "LinearMath/btScalar.h"

%include "btTransform.i"

%{
#include <LinearMath/btVector3.h>
%}
%include <LinearMath/btVector3.h>

%{
#include <LinearMath/btQuaternion.h>
%}
%include <LinearMath/btQuaternion.h>

%{
#include <LinearMath/btQuadWord.h>
%}
#include <LinearMath/btQuadWord.h>

%{
#include <LinearMath/btMatrix3x3.h>
%}
#include <LinearMath/btMatrix3x3.h>

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

%{
#include <LinearMath/btConvexHull.h>
%}
%include "LinearMath/btConvexHull.h"

%{
#include <LinearMath/btGrahamScan2dConvexHull.h>
%}
%include "LinearMath/btGrahamScan2dConvexHull.h"

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

%include "./btMotionState.i"

%include "./btDefaultMotionState.i"

#ifndef BT_NO_PROFILE
void CProfileIterator::Enter_Largest_Child()
{
}
#endif

%template(btVector3Array) btAlignedObjectArray<btVector3>;
%template(btScalarArray) btAlignedObjectArray<btScalar>;
