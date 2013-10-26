#define gdxToString(X)	"X"

// Used by the classes.i files
%define SPECIFY_CLASS(TYPE, PACKAGE)
%typemap("javapackage") TYPE, TYPE *, TYPE & gdxToString(PACKAGE);
%enddef

%{
#include <gdx/common/jniHelpers.h>
%}

%rename("%(strip:[m_])s", %$ismember, %$ispublic, %$not %$isclass, %$not %$istemplate, %$not %$isfunction, regexmatch$name="m_.*$") "";
%ignore btHashString::getHash;
%ignore btTypedObject::getObjectType;

%include "arrays_java.i"

%include "../common/gdxDefault.i"

/* Configures types that need down cast support */
%include "../common/gdxDownCast.i"

/*
 * Use java.nio.Buffer where Bullet wants btScalar * and alike.  This gets disabled
 * for some types (and re-enabled right after).
 */
%include "../common/gdxBuffers.i"
%include "../common/gdxEnableBuffers.i"

%include "../common/gdxCriticalArrays.i"

%include "../common/gdxPool.i"
%include "../common/gdxPooledTypemap.i"

%include "../common/gdxPooledObject.i"


/* Map "void *" to "jlong". */
%include "gdxVoidPointer.i";

/* Use "unsafe" enums (plain integer constants) instead of typesafe enum classes. */
%include "enumtypeunsafe.swg"
%javaconst(1);

/* Include Java imports for all the types we'll need in all extensions/custom types. */
%include "../common/gdxJavaImports.i"

%{
#include <stdint.h>
%}

//%include "common/gdxManagedObject.i"

/* Prefer libgdx's linear math types (Vector3, Matrix3, etc.). */
%include "../common/gdxMathTypes.i"
