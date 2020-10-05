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
%feature("director") CustomActionInterface;
%ignore CustomActionInterface::updateAction(btCollisionWorld*, btScalar);
%ignore CustomActionInterface::debugDraw(btIDebugDraw*);

%include "./btRigidBody.i"

%include "./btTypedConstraint.i"

%rename(getConstraintConst) btDynamicsWorld::getConstraint(int) const;
%rename(getSolverInfoConst) btDynamicsWorld::getSolverInfo() const;
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
#include <gdx/dynamics/CustomActionInterface.h>
%}
%include "gdx/dynamics/CustomActionInterface.h"

%rename(getSimulationIslandManagerConst) btDiscreteDynamicsWorld::getSimulationIslandManager() const;
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

%rename(getFrameOffsetAConst) btSliderConstraint::getFrameOffsetA() const;
%rename(getFrameOffsetBConst) btSliderConstraint::getFrameOffsetB() const;
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

%rename(getFrameOffsetAConst) btGeneric6DofConstraint::getFrameOffsetA() const;
%rename(getFrameOffsetBConst) btGeneric6DofConstraint::getFrameOffsetB() const;
%{
#include <BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btUniversalConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btUniversalConstraint.h"

%rename(getContactManifoldConst) btContactConstraint::getContactManifold() const;
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

%rename(getFrameOffsetAConst) btGeneric6DofSpring2Constraint::getFrameOffsetA() const;
%rename(getFrameOffsetBConst) btGeneric6DofSpring2Constraint::getFrameOffsetB() const;
%{
#include <BulletDynamics/ConstraintSolver/btGeneric6DofSpring2Constraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGeneric6DofSpring2Constraint.h"

%rename(getAFrameConst) btHingeConstraint::getAFrame() const;
%rename(getBFrameConst) btHingeConstraint::getBFrame() const;
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

%{
#include <gdx/dynamics/FilterableVehicleRaycaster.h>
%}
%include "gdx/dynamics/FilterableVehicleRaycaster.h"

%{
#include <BulletDynamics/ConstraintSolver/btGearConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGearConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btNNCGConstraintSolver.h>
%}
%include "BulletDynamics/ConstraintSolver/btNNCGConstraintSolver.h"

%{
#include <BulletDynamics/Dynamics/btDiscreteDynamicsWorldMt.h>
%}
%include "BulletDynamics/Dynamics/btDiscreteDynamicsWorldMt.h"

%{
#include <BulletDynamics/Dynamics/btSimulationIslandManagerMt.h>
%}
%include "BulletDynamics/Dynamics/btSimulationIslandManagerMt.h"

%rename(getLinkConst) btMultiBody::getLink(int) const;
%rename(getBaseColliderConst) btMultiBody::getBaseCollider() const;
%rename(getJointVelMultiDofConst) btMultiBody::getJointVelMultiDof(int) const;
%rename(getJointPosMultiDofConst) btMultiBody::getJointPosMultiDof(int) const;
%{
#include <BulletDynamics/Featherstone/btMultiBody.h>
%}
%include "BulletDynamics/Featherstone/btMultiBody.h"

%rename(jacobianAConst) btMultiBodyConstraint::jacobianA(int) const;
%rename(jacobianBConst) btMultiBodyConstraint::jacobianB(int) const;
%{
#include <BulletDynamics/Featherstone/btMultiBodyConstraint.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyConstraint.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodyGearConstraint.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyGearConstraint.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodyConstraintSolver.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyConstraintSolver.h"

%rename(getMultiBodyConstraintConst) btMultiBodyDynamicsWorld::getMultiBodyConstraint(int) const;
%rename(getMultiBodyConst) btMultiBodyDynamicsWorld::getMultiBody(int) const;
%{
#include <BulletDynamics/Featherstone/btMultiBodyDynamicsWorld.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyDynamicsWorld.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodyFixedConstraint.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyFixedConstraint.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodyJointFeedback.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyJointFeedback.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodyJointLimitConstraint.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyJointLimitConstraint.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodyJointMotor.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyJointMotor.h"

%immutable btMultibodyLink::m_linkName;
%immutable btMultibodyLink::m_jointName;
%{
#include <BulletDynamics/Featherstone/btMultiBodyLink.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyLink.h"

%rename(upcastConstBtCollisionObject) btMultiBodyLinkCollider::upcast(btCollisionObject const *);
%{
#include <BulletDynamics/Featherstone/btMultiBodyLinkCollider.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyLinkCollider.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodyPoint2Point.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodyPoint2Point.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodySliderConstraint.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodySliderConstraint.h"

%{
#include <BulletDynamics/Featherstone/btMultiBodySolverConstraint.h>
%}
%include "BulletDynamics/Featherstone/btMultiBodySolverConstraint.h"

%{
#include <BulletDynamics/MLCPSolvers/btDantzigLCP.h>
%}
%include "BulletDynamics/MLCPSolvers/btDantzigLCP.h"

%{
#include <BulletDynamics/MLCPSolvers/btMLCPSolverInterface.h>
%}
%include "BulletDynamics/MLCPSolvers/btMLCPSolverInterface.h"

%{
#include <BulletDynamics/MLCPSolvers/btDantzigSolver.h>
%}
%include "BulletDynamics/MLCPSolvers/btDantzigSolver.h"

%{
#include <BulletDynamics/MLCPSolvers/btLemkeAlgorithm.h>
%}
%include "BulletDynamics/MLCPSolvers/btLemkeAlgorithm.h"

%{
#include <BulletDynamics/MLCPSolvers/btLemkeSolver.h>
%}
%include "BulletDynamics/MLCPSolvers/btLemkeSolver.h"

%{
#include <BulletDynamics/MLCPSolvers/btMLCPSolver.h>
%}
%include "BulletDynamics/MLCPSolvers/btMLCPSolver.h"

%{
#include <BulletDynamics/MLCPSolvers/btPATHSolver.h>
%}
%include "BulletDynamics/MLCPSolvers/btPATHSolver.h"

%{
#include <BulletDynamics/MLCPSolvers/btSolveProjectedGaussSeidel.h>
%}
%include "BulletDynamics/MLCPSolvers/btSolveProjectedGaussSeidel.h"


%{
#include <BulletDynamics/Vehicle/btRaycastVehicle.h>
%}
%include "BulletDynamics/Vehicle/btRaycastVehicle.h"

%{
#include <BulletDynamics/Vehicle/btVehicleRaycaster.h>
%}
%include "BulletDynamics/Vehicle/btVehicleRaycaster.h"

%{
#include <BulletDynamics/Vehicle/btWheelInfo.h>
%}
%include "BulletDynamics/Vehicle/btWheelInfo.h"
