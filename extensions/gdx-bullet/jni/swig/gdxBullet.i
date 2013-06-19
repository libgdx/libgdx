/*
 *	Main SWIG 2.0 input file for Bullet.
 */

%module(directors="1") gdxBullet

#define gdxToString(X)	"X"

/* 
 * Allow public access to the CPtr methods on proxy classes and wrappers.
 * 
 * public was the default in SWIG <= 2.0.4, but changed to protected in
 * 2.0.5. Getting pointers to native Bullet objects can be useful (for 
 * instance, to map them back to associated Java scene objects), so make
 * the getCPtr method public.
 */
SWIG_JAVABODY_PROXY(protected, public, SWIGTYPE)
SWIG_JAVABODY_TYPEWRAPPER(protected, protected, public, SWIGTYPE)

/* Configures types that need down cast support */
%include "common/gdxDownCast.i"

/*
 * Use java.nio.Buffer where Bullet wants btScalar * and alike.  This gets disabled
 * for some types (and re-enabled right after).
 */
%include "common/gdxBuffers.i"
%include "common/gdxEnableBuffers.i"

%include "common/gdxCriticalArrays.i"

%include "common/gdxPool.i"
%include "common/gdxPooledTypemap.i"

%include "common/gdxPooledObject.i"

%include "common/gdxManagedObject.i"

/* Prefer libgdx's linear math types (Vector3, Matrix3, etc.). */
%include "common/gdxMathTypes.i"

/* Map "void *" to "jlong". */
// %include "gdxVoidPointer.i";

/* Use "unsafe" enums (plain integer constants) instead of typesafe enum classes. */
%include "enumtypeunsafe.swg"
%javaconst(1);

/* Include Java imports for all the types we'll need in all extensions/custom types. */
%include "common/gdxJavaImports.i"


%{
#include <stdint.h>
%}

/*
 * btScalar.h defines macros the other types need, so process it first.  
 * It also defines some static functions that end up in gdxBulletJNI.java.
 */
%include "LinearMath/btScalar.h"

/*
 * Extend some classes with custom Java.
 */
%include "common/btTransform.i"

/* Configure directors for types with virtual methods that need Java implementations */
%feature("director") btIDebugDraw;

/*
 * The rest of the types (some are disabled, commented out at the bottom).
 * 
 * The order below is important.  If an "%include"ed type depends on another
 * type, that other type's "#include" line needs to come before it.  Avoid
 * splitting the #include/%include pairs for a type, so move the whole type
 * block.
 */

%{
#include <LinearMath/btVector3.h>
%}
%include <LinearMath/btVector3.h>

%{
#include <LinearMath/btQuadWord.h>
%}
#include <LinearMath/btQuadWord.h>

%{
#include <LinearMath/btQuaternion.h>
%}
%include <LinearMath/btQuaternion.h>

%{
#include <LinearMath/btMatrix3x3.h>
%}
#include <LinearMath/btMatrix3x3.h>

%{
#include <LinearMath/btAabbUtil2.h>
%}
%include "LinearMath/btAabbUtil2.h"

DISABLE_POOLED_TYPEMAP(btTransform);
DISABLE_POOLED_TYPEMAP(btVector3);
%{
#include <LinearMath/btIDebugDraw.h>
%}
%include "LinearMath/btIDebugDraw.h"
ENABLE_POOLED_TYPEMAP(btVector3, Vector3, "Lcom/badlogic/gdx/math/Vector3;");
ENABLE_POOLED_TYPEMAP(btTransform, Matrix4, "Lcom/badlogic/gdx/math/Matrix4;");

%{
#include <LinearMath/btGeometryUtil.h>
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

%include "collision/gdxBulletCollision.i"

%include "dynamics/gdxBulletDynamics.i"

%include "softbody/gdxBulletSoftbody.i"

%include "gdxMissingBulletMethods.i"

%include "extras/serialize/gdxBulletSerialize.i"
