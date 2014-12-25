%module btTypedConstraint

/*
 * Disable btScalar * <-> FloatBuffer for this struct, because SWIG can't guess
 * how big these arrays are and I wasn't bothered to measure them and
 * tweak the typemaps.  Re-enabled at bottom of file.
 */
%include "../common/gdxDisableBuffers.i"

%{
#include <BulletDynamics/ConstraintSolver/btTypedConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btTypedConstraint.h"

/* Re-enable */
%include "../common/gdxEnableBuffers.i"
