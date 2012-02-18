/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btTypedConstraint

/*
 * Disable btScalar * <-> float[] for this struct, because SWIG can't guess
 * how big these arrays are and I wasn't bothered to measure them and
 * tweak the typemaps.  Re-enabled at bottom of file.
 */
%include "../gdxDisableArrays.i"

// Nested struct or class copied from Bullet header
struct btConstraintInfo2 {
	// integrator parameters: frames per second (1/stepsize), default error
	// reduction parameter (0..1).
	btScalar fps,erp;

	// for the first and second body, pointers to two (linear and angular)
	// n*3 jacobian sub matrices, stored by rows. these matrices will have
	// been initialized to 0 on entry. if the second body is zero then the
	// J2xx pointers may be 0.
	btScalar *m_J1linearAxis,*m_J1angularAxis,*m_J2linearAxis,*m_J2angularAxis;

	// elements to jump from one row to the next in J's
	int rowskip;

	// right hand sides of the equation J*v = c + cfm * lambda. cfm is the
	// "constraint force mixing" vector. c is set to zero on entry, cfm is
	// set to a constant value (typically very small or zero) value on entry.
	btScalar *m_constraintError,*cfm;

	// lo and hi limits for variables (set to -/+ infinity on entry).
	btScalar *m_lowerLimit,*m_upperLimit;

	// findex vector for variables. see the LCP solver interface for a
	// description of what this does. this is set to -1 on entry.
	// note that the returned indexes are relative to the first index of
	// the constraint.
	int *findex;
	// number of solver iterations
	int m_numIterations;

	//damping of the velocity
	btScalar	m_damping;
};


%nestedworkaround btTypedConstraint::btConstraintInfo2;

%{
#include <BulletDynamics/ConstraintSolver/btTypedConstraint.h>
%}

%include "BulletDynamics/ConstraintSolver/btTypedConstraint.h"

%{
typedef btTypedConstraint::btConstraintInfo2 btConstraintInfo2;
%}

/* Re-enable */
%include "../gdxEnableArrays.i"
