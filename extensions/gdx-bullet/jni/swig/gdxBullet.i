/*
 *	Main SWIG 2.0 input file for Bullet.
 */

//%module(directors="1") gdxBullet

#define gdxToString(X)	"X"

/* remove m_ prefix for the getters/setters of properties, 
 * %rename has some conflict with %template so we much be careful when selecting what to rename */
%rename("%(strip:[m_])s", %$ismember, %$ispublic, %$not %$isclass, %$not %$istemplate, %$not %$isfunction, regexmatch$name="m_.*$") "";
/* some classes have both public properties and getter/setter methods, ignore the latter. */

%include "common/gdxDefault.i"

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

//%include "common/gdxManagedObject.i"

/* Prefer libgdx's linear math types (Vector3, Matrix3, etc.). */
%include "common/gdxMathTypes.i"

%include "gdxLinearMath.i"

%include "collision/gdxBulletCollision.i"

%include "dynamics/gdxBulletDynamics.i"

%include "softbody/gdxBulletSoftbody.i"

%include "gdxMissingBulletMethods.i"

%include "extras/serialize/gdxBulletSerialize.i"

//%include "dynamics/PhysicsAPI.i"