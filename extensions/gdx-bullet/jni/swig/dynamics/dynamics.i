%module(directors="1") Dynamics

%include "arrays_java.i"

%import "../collision/collision.i"

%include "../common/gdxCommon.i"

%include "../../swig-src/linearmath/classes.i"
%include "../../swig-src/collision/classes.i"

%ignore btSolverBody::getWorldTransform;
%ignore btSolverBody::setWorldTransform;
%ignore btSolverBody::getDeltaLinearVelocity;
%ignore btSolverBody::getDeltaAngularVelocity;
%ignore btSolverBody::getPushVelocity;
%ignore btSolverBody::getTurnVelocity;

%ignore btSequentialImpulseConstraintSolver::getSSE2ConstraintRowSolverGeneric();
%ignore btSequentialImpulseConstraintSolver::getSSE2ConstraintRowSolverLowerLimit();
%ignore btSequentialImpulseConstraintSolver::getSSE4_1ConstraintRowSolverGeneric();
%ignore btSequentialImpulseConstraintSolver::getSSE4_1ConstraintRowSolverLowerLimit();


%typemap(javaimports) SWIGTYPE	%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
%}
%pragma(java) jniclassimports=%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;
%}
%pragma(java) moduleimports=%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
%}

%feature("director") InternalTickCallback;

%include "./btRigidBody.i"

%include "./btTypedConstraint.i"

%{
#include <BulletDynamics/Dynamics/btDynamicsWorld.h>
%}
%include "BulletDynamics/Dynamics/btDynamicsWorld.h"

%{
#include <gdx/dynamics/InternalTickCallback.h>
%}
%include "gdx/dynamics/InternalTickCallback.h"

%{
#include <BulletDynamics/Dynamics/btSimpleDynamicsWorld.h>
%}
%include "BulletDynamics/Dynamics/btSimpleDynamicsWorld.h"

%{
#include <BulletDynamics/Dynamics/btActionInterface.h>
%}
%include "BulletDynamics/Dynamics/btActionInterface.h"

%{
#include <BulletDynamics/Dynamics/btDiscreteDynamicsWorld.h>
%}
%include "BulletDynamics/Dynamics/btDiscreteDynamicsWorld.h"

%{
#include <BulletDynamics/Character/btCharacterControllerInterface.h>
%}
%include "BulletDynamics/Character/btCharacterControllerInterface.h"

%{
#include <BulletDynamics/Character/btKinematicCharacterController.h>
%}
%include "BulletDynamics/Character/btKinematicCharacterController.h"

%{
#include <BulletDynamics/ConstraintSolver/btContactSolverInfo.h>
%}
%include "BulletDynamics/ConstraintSolver/btContactSolverInfo.h"

%{
#include <BulletDynamics/ConstraintSolver/btConstraintSolver.h>
%}
%include "BulletDynamics/ConstraintSolver/btConstraintSolver.h"

%{
#include <BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h>
%}
%include "BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h"


%{
#include <BulletDynamics/ConstraintSolver/btSolverBody.h>
%}
%include "BulletDynamics/ConstraintSolver/btSolverBody.h"

%{
#include <BulletDynamics/ConstraintSolver/btSliderConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btSliderConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btJacobianEntry.h>
%}
%include "BulletDynamics/ConstraintSolver/btJacobianEntry.h"

%{
#include <BulletDynamics/ConstraintSolver/btSolve2LinearConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btSolve2LinearConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btUniversalConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btUniversalConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btContactConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btContactConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btConeTwistConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btConeTwistConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btGeneric6DofSpringConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGeneric6DofSpringConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btGeneric6DofSpring2Constraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGeneric6DofSpring2Constraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btHingeConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btHingeConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btSolverConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btSolverConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btHinge2Constraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btHinge2Constraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btFixedConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btFixedConstraint.h"

%{
#include <BulletDynamics/Vehicle/btVehicleRaycaster.h>
%}
%include "BulletDynamics/Vehicle/btVehicleRaycaster.h"

// NOTE: btWheelInfo doesnt have a ctor but is required, this must be manually added.
%{
#include <BulletDynamics/Vehicle/btWheelInfo.h>
%}
%include "BulletDynamics/Vehicle/btWheelInfo.h"

%include "./btRaycastVehicle.i"
