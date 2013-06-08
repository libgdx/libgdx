%module(directors="1") gdxBulletDynamics

%feature("director") InternalTickCallback;

%include "./btRigidBody.i"

%include "./btTypedConstraint.i"

%{
#include <BulletDynamics/Dynamics/btDynamicsWorld.h>
%}
%include "BulletDynamics/Dynamics/btDynamicsWorld.h"

%{
#include <gdx/InternalTickCallback.h>
%}
%include "gdx/InternalTickCallback.h"

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
#include <BulletDynamics/Vehicle/btVehicleRaycaster.h>
%}
%include "BulletDynamics/Vehicle/btVehicleRaycaster.h"

// NOTE: btWheelInfo doesnt have a ctor but is required, this must be manually added.
%{
#include <BulletDynamics/Vehicle/btWheelInfo.h>
%}
%include "BulletDynamics/Vehicle/btWheelInfo.h"

%include "./btRaycastVehicle.i"
