/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2006 Erwin Coumans  http://continuousphysics.com/Bullet/

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

//#define COMPUTE_IMPULSE_DENOM 1
//#define BT_ADDITIONAL_DEBUG

//It is not necessary (redundant) to refresh contact manifolds, this refresh has been moved to the collision algorithms.

#include "btSequentialImpulseConstraintSolver.h"
#include "BulletCollision/NarrowPhaseCollision/btPersistentManifold.h"

#include "LinearMath/btIDebugDraw.h"
//#include "btJacobianEntry.h"
#include "LinearMath/btMinMax.h"
#include "BulletDynamics/ConstraintSolver/btTypedConstraint.h"
#include <new>
#include "LinearMath/btStackAlloc.h"
#include "LinearMath/btQuickprof.h"
//#include "btSolverBody.h"
//#include "btSolverConstraint.h"
#include "LinearMath/btAlignedObjectArray.h"
#include <string.h> //for memset

int		gNumSplitImpulseRecoveries = 0;

#include "BulletDynamics/Dynamics/btRigidBody.h"

btSequentialImpulseConstraintSolver::btSequentialImpulseConstraintSolver()
:m_btSeed2(0)
{

}

btSequentialImpulseConstraintSolver::~btSequentialImpulseConstraintSolver()
{
}

#ifdef USE_SIMD
#include <emmintrin.h>
#define btVecSplat(x, e) _mm_shuffle_ps(x, x, _MM_SHUFFLE(e,e,e,e))
static inline __m128 btSimdDot3( __m128 vec0, __m128 vec1 )
{
	__m128 result = _mm_mul_ps( vec0, vec1);
	return _mm_add_ps( btVecSplat( result, 0 ), _mm_add_ps( btVecSplat( result, 1 ), btVecSplat( result, 2 ) ) );
}
#endif//USE_SIMD

// Project Gauss Seidel or the equivalent Sequential Impulse
void btSequentialImpulseConstraintSolver::resolveSingleConstraintRowGenericSIMD(btSolverBody& body1,btSolverBody& body2,const btSolverConstraint& c)
{
#ifdef USE_SIMD
	__m128 cpAppliedImp = _mm_set1_ps(c.m_appliedImpulse);
	__m128	lowerLimit1 = _mm_set1_ps(c.m_lowerLimit);
	__m128	upperLimit1 = _mm_set1_ps(c.m_upperLimit);
	__m128 deltaImpulse = _mm_sub_ps(_mm_set1_ps(c.m_rhs), _mm_mul_ps(_mm_set1_ps(c.m_appliedImpulse),_mm_set1_ps(c.m_cfm)));
	__m128 deltaVel1Dotn	=	_mm_add_ps(btSimdDot3(c.m_contactNormal1.mVec128,body1.internalGetDeltaLinearVelocity().mVec128), btSimdDot3(c.m_relpos1CrossNormal.mVec128,body1.internalGetDeltaAngularVelocity().mVec128));
	__m128 deltaVel2Dotn	=	_mm_add_ps(btSimdDot3(c.m_contactNormal2.mVec128,body2.internalGetDeltaLinearVelocity().mVec128), btSimdDot3(c.m_relpos2CrossNormal.mVec128,body2.internalGetDeltaAngularVelocity().mVec128));
	deltaImpulse	=	_mm_sub_ps(deltaImpulse,_mm_mul_ps(deltaVel1Dotn,_mm_set1_ps(c.m_jacDiagABInv)));
	deltaImpulse	=	_mm_sub_ps(deltaImpulse,_mm_mul_ps(deltaVel2Dotn,_mm_set1_ps(c.m_jacDiagABInv)));
	btSimdScalar sum = _mm_add_ps(cpAppliedImp,deltaImpulse);
	btSimdScalar resultLowerLess,resultUpperLess;
	resultLowerLess = _mm_cmplt_ps(sum,lowerLimit1);
	resultUpperLess = _mm_cmplt_ps(sum,upperLimit1);
	__m128 lowMinApplied = _mm_sub_ps(lowerLimit1,cpAppliedImp);
	deltaImpulse = _mm_or_ps( _mm_and_ps(resultLowerLess, lowMinApplied), _mm_andnot_ps(resultLowerLess, deltaImpulse) );
	c.m_appliedImpulse = _mm_or_ps( _mm_and_ps(resultLowerLess, lowerLimit1), _mm_andnot_ps(resultLowerLess, sum) );
	__m128 upperMinApplied = _mm_sub_ps(upperLimit1,cpAppliedImp);
	deltaImpulse = _mm_or_ps( _mm_and_ps(resultUpperLess, deltaImpulse), _mm_andnot_ps(resultUpperLess, upperMinApplied) );
	c.m_appliedImpulse = _mm_or_ps( _mm_and_ps(resultUpperLess, c.m_appliedImpulse), _mm_andnot_ps(resultUpperLess, upperLimit1) );
	__m128	linearComponentA = _mm_mul_ps(c.m_contactNormal1.mVec128,body1.internalGetInvMass().mVec128);
	__m128	linearComponentB = _mm_mul_ps((c.m_contactNormal2).mVec128,body2.internalGetInvMass().mVec128);
	__m128 impulseMagnitude = deltaImpulse;
	body1.internalGetDeltaLinearVelocity().mVec128 = _mm_add_ps(body1.internalGetDeltaLinearVelocity().mVec128,_mm_mul_ps(linearComponentA,impulseMagnitude));
	body1.internalGetDeltaAngularVelocity().mVec128 = _mm_add_ps(body1.internalGetDeltaAngularVelocity().mVec128 ,_mm_mul_ps(c.m_angularComponentA.mVec128,impulseMagnitude));
	body2.internalGetDeltaLinearVelocity().mVec128 = _mm_add_ps(body2.internalGetDeltaLinearVelocity().mVec128,_mm_mul_ps(linearComponentB,impulseMagnitude));
	body2.internalGetDeltaAngularVelocity().mVec128 = _mm_add_ps(body2.internalGetDeltaAngularVelocity().mVec128 ,_mm_mul_ps(c.m_angularComponentB.mVec128,impulseMagnitude));
#else
	resolveSingleConstraintRowGeneric(body1,body2,c);
#endif
}

// Project Gauss Seidel or the equivalent Sequential Impulse
 void btSequentialImpulseConstraintSolver::resolveSingleConstraintRowGeneric(btSolverBody& body1,btSolverBody& body2,const btSolverConstraint& c)
{
	btScalar deltaImpulse = c.m_rhs-btScalar(c.m_appliedImpulse)*c.m_cfm;
	const btScalar deltaVel1Dotn	=	c.m_contactNormal1.dot(body1.internalGetDeltaLinearVelocity()) 	+ c.m_relpos1CrossNormal.dot(body1.internalGetDeltaAngularVelocity());
	const btScalar deltaVel2Dotn	=	c.m_contactNormal2.dot(body2.internalGetDeltaLinearVelocity())  + c.m_relpos2CrossNormal.dot(body2.internalGetDeltaAngularVelocity());

//	const btScalar delta_rel_vel	=	deltaVel1Dotn-deltaVel2Dotn;
	deltaImpulse	-=	deltaVel1Dotn*c.m_jacDiagABInv;
	deltaImpulse	-=	deltaVel2Dotn*c.m_jacDiagABInv;

	const btScalar sum = btScalar(c.m_appliedImpulse) + deltaImpulse;
	if (sum < c.m_lowerLimit)
	{
		deltaImpulse = c.m_lowerLimit-c.m_appliedImpulse;
		c.m_appliedImpulse = c.m_lowerLimit;
	}
	else if (sum > c.m_upperLimit) 
	{
		deltaImpulse = c.m_upperLimit-c.m_appliedImpulse;
		c.m_appliedImpulse = c.m_upperLimit;
	}
	else
	{
		c.m_appliedImpulse = sum;
	}

	body1.internalApplyImpulse(c.m_contactNormal1*body1.internalGetInvMass(),c.m_angularComponentA,deltaImpulse);
	body2.internalApplyImpulse(c.m_contactNormal2*body2.internalGetInvMass(),c.m_angularComponentB,deltaImpulse);
}

 void btSequentialImpulseConstraintSolver::resolveSingleConstraintRowLowerLimitSIMD(btSolverBody& body1,btSolverBody& body2,const btSolverConstraint& c)
{
#ifdef USE_SIMD
	__m128 cpAppliedImp = _mm_set1_ps(c.m_appliedImpulse);
	__m128	lowerLimit1 = _mm_set1_ps(c.m_lowerLimit);
	__m128	upperLimit1 = _mm_set1_ps(c.m_upperLimit);
	__m128 deltaImpulse = _mm_sub_ps(_mm_set1_ps(c.m_rhs), _mm_mul_ps(_mm_set1_ps(c.m_appliedImpulse),_mm_set1_ps(c.m_cfm)));
	__m128 deltaVel1Dotn	=	_mm_add_ps(btSimdDot3(c.m_contactNormal1.mVec128,body1.internalGetDeltaLinearVelocity().mVec128), btSimdDot3(c.m_relpos1CrossNormal.mVec128,body1.internalGetDeltaAngularVelocity().mVec128));
	__m128 deltaVel2Dotn	=	_mm_add_ps(btSimdDot3(c.m_contactNormal2.mVec128,body2.internalGetDeltaLinearVelocity().mVec128), btSimdDot3(c.m_relpos2CrossNormal.mVec128,body2.internalGetDeltaAngularVelocity().mVec128));
	deltaImpulse	=	_mm_sub_ps(deltaImpulse,_mm_mul_ps(deltaVel1Dotn,_mm_set1_ps(c.m_jacDiagABInv)));
	deltaImpulse	=	_mm_sub_ps(deltaImpulse,_mm_mul_ps(deltaVel2Dotn,_mm_set1_ps(c.m_jacDiagABInv)));
	btSimdScalar sum = _mm_add_ps(cpAppliedImp,deltaImpulse);
	btSimdScalar resultLowerLess,resultUpperLess;
	resultLowerLess = _mm_cmplt_ps(sum,lowerLimit1);
	resultUpperLess = _mm_cmplt_ps(sum,upperLimit1);
	__m128 lowMinApplied = _mm_sub_ps(lowerLimit1,cpAppliedImp);
	deltaImpulse = _mm_or_ps( _mm_and_ps(resultLowerLess, lowMinApplied), _mm_andnot_ps(resultLowerLess, deltaImpulse) );
	c.m_appliedImpulse = _mm_or_ps( _mm_and_ps(resultLowerLess, lowerLimit1), _mm_andnot_ps(resultLowerLess, sum) );
	__m128	linearComponentA = _mm_mul_ps(c.m_contactNormal1.mVec128,body1.internalGetInvMass().mVec128);
	__m128	linearComponentB = _mm_mul_ps(c.m_contactNormal2.mVec128,body2.internalGetInvMass().mVec128);
	__m128 impulseMagnitude = deltaImpulse;
	body1.internalGetDeltaLinearVelocity().mVec128 = _mm_add_ps(body1.internalGetDeltaLinearVelocity().mVec128,_mm_mul_ps(linearComponentA,impulseMagnitude));
	body1.internalGetDeltaAngularVelocity().mVec128 = _mm_add_ps(body1.internalGetDeltaAngularVelocity().mVec128 ,_mm_mul_ps(c.m_angularComponentA.mVec128,impulseMagnitude));
	body2.internalGetDeltaLinearVelocity().mVec128 = _mm_add_ps(body2.internalGetDeltaLinearVelocity().mVec128,_mm_mul_ps(linearComponentB,impulseMagnitude));
	body2.internalGetDeltaAngularVelocity().mVec128 = _mm_add_ps(body2.internalGetDeltaAngularVelocity().mVec128 ,_mm_mul_ps(c.m_angularComponentB.mVec128,impulseMagnitude));
#else
	resolveSingleConstraintRowLowerLimit(body1,body2,c);
#endif
}

// Projected Gauss Seidel or the equivalent Sequential Impulse
 void btSequentialImpulseConstraintSolver::resolveSingleConstraintRowLowerLimit(btSolverBody& body1,btSolverBody& body2,const btSolverConstraint& c)
{
	btScalar deltaImpulse = c.m_rhs-btScalar(c.m_appliedImpulse)*c.m_cfm;
	const btScalar deltaVel1Dotn	=	c.m_contactNormal1.dot(body1.internalGetDeltaLinearVelocity()) + c.m_relpos1CrossNormal.dot(body1.internalGetDeltaAngularVelocity());
	const btScalar deltaVel2Dotn	=	c.m_contactNormal2.dot(body2.internalGetDeltaLinearVelocity()) + c.m_relpos2CrossNormal.dot(body2.internalGetDeltaAngularVelocity());

	deltaImpulse	-=	deltaVel1Dotn*c.m_jacDiagABInv;
	deltaImpulse	-=	deltaVel2Dotn*c.m_jacDiagABInv;
	const btScalar sum = btScalar(c.m_appliedImpulse) + deltaImpulse;
	if (sum < c.m_lowerLimit)
	{
		deltaImpulse = c.m_lowerLimit-c.m_appliedImpulse;
		c.m_appliedImpulse = c.m_lowerLimit;
	}
	else
	{
		c.m_appliedImpulse = sum;
	}
	body1.internalApplyImpulse(c.m_contactNormal1*body1.internalGetInvMass(),c.m_angularComponentA,deltaImpulse);
	body2.internalApplyImpulse(c.m_contactNormal2*body2.internalGetInvMass(),c.m_angularComponentB,deltaImpulse);
}


void	btSequentialImpulseConstraintSolver::resolveSplitPenetrationImpulseCacheFriendly(
        btSolverBody& body1,
        btSolverBody& body2,
        const btSolverConstraint& c)
{
		if (c.m_rhsPenetration)
        {
			gNumSplitImpulseRecoveries++;
			btScalar deltaImpulse = c.m_rhsPenetration-btScalar(c.m_appliedPushImpulse)*c.m_cfm;
			const btScalar deltaVel1Dotn	=	c.m_contactNormal1.dot(body1.internalGetPushVelocity()) 	+ c.m_relpos1CrossNormal.dot(body1.internalGetTurnVelocity());
			const btScalar deltaVel2Dotn	=	c.m_contactNormal2.dot(body2.internalGetPushVelocity())		+ c.m_relpos2CrossNormal.dot(body2.internalGetTurnVelocity());

			deltaImpulse	-=	deltaVel1Dotn*c.m_jacDiagABInv;
			deltaImpulse	-=	deltaVel2Dotn*c.m_jacDiagABInv;
			const btScalar sum = btScalar(c.m_appliedPushImpulse) + deltaImpulse;
			if (sum < c.m_lowerLimit)
			{
				deltaImpulse = c.m_lowerLimit-c.m_appliedPushImpulse;
				c.m_appliedPushImpulse = c.m_lowerLimit;
			}
			else
			{
				c.m_appliedPushImpulse = sum;
			}
			body1.internalApplyPushImpulse(c.m_contactNormal1*body1.internalGetInvMass(),c.m_angularComponentA,deltaImpulse);
			body2.internalApplyPushImpulse(c.m_contactNormal2*body2.internalGetInvMass(),c.m_angularComponentB,deltaImpulse);
        }
}

 void btSequentialImpulseConstraintSolver::resolveSplitPenetrationSIMD(btSolverBody& body1,btSolverBody& body2,const btSolverConstraint& c)
{
#ifdef USE_SIMD
	if (!c.m_rhsPenetration)
		return;

	gNumSplitImpulseRecoveries++;

	__m128 cpAppliedImp = _mm_set1_ps(c.m_appliedPushImpulse);
	__m128	lowerLimit1 = _mm_set1_ps(c.m_lowerLimit);
	__m128	upperLimit1 = _mm_set1_ps(c.m_upperLimit);
	__m128 deltaImpulse = _mm_sub_ps(_mm_set1_ps(c.m_rhsPenetration), _mm_mul_ps(_mm_set1_ps(c.m_appliedPushImpulse),_mm_set1_ps(c.m_cfm)));
	__m128 deltaVel1Dotn	=	_mm_add_ps(btSimdDot3(c.m_contactNormal1.mVec128,body1.internalGetPushVelocity().mVec128), btSimdDot3(c.m_relpos1CrossNormal.mVec128,body1.internalGetTurnVelocity().mVec128));
	__m128 deltaVel2Dotn	=	_mm_add_ps(btSimdDot3(c.m_contactNormal2.mVec128,body2.internalGetPushVelocity().mVec128), btSimdDot3(c.m_relpos2CrossNormal.mVec128,body2.internalGetTurnVelocity().mVec128));
	deltaImpulse	=	_mm_sub_ps(deltaImpulse,_mm_mul_ps(deltaVel1Dotn,_mm_set1_ps(c.m_jacDiagABInv)));
	deltaImpulse	=	_mm_sub_ps(deltaImpulse,_mm_mul_ps(deltaVel2Dotn,_mm_set1_ps(c.m_jacDiagABInv)));
	btSimdScalar sum = _mm_add_ps(cpAppliedImp,deltaImpulse);
	btSimdScalar resultLowerLess,resultUpperLess;
	resultLowerLess = _mm_cmplt_ps(sum,lowerLimit1);
	resultUpperLess = _mm_cmplt_ps(sum,upperLimit1);
	__m128 lowMinApplied = _mm_sub_ps(lowerLimit1,cpAppliedImp);
	deltaImpulse = _mm_or_ps( _mm_and_ps(resultLowerLess, lowMinApplied), _mm_andnot_ps(resultLowerLess, deltaImpulse) );
	c.m_appliedPushImpulse = _mm_or_ps( _mm_and_ps(resultLowerLess, lowerLimit1), _mm_andnot_ps(resultLowerLess, sum) );
	__m128	linearComponentA = _mm_mul_ps(c.m_contactNormal1.mVec128,body1.internalGetInvMass().mVec128);
	__m128	linearComponentB = _mm_mul_ps(c.m_contactNormal2.mVec128,body2.internalGetInvMass().mVec128);
	__m128 impulseMagnitude = deltaImpulse;
	body1.internalGetPushVelocity().mVec128 = _mm_add_ps(body1.internalGetPushVelocity().mVec128,_mm_mul_ps(linearComponentA,impulseMagnitude));
	body1.internalGetTurnVelocity().mVec128 = _mm_add_ps(body1.internalGetTurnVelocity().mVec128 ,_mm_mul_ps(c.m_angularComponentA.mVec128,impulseMagnitude));
	body2.internalGetPushVelocity().mVec128 = _mm_add_ps(body2.internalGetPushVelocity().mVec128,_mm_mul_ps(linearComponentB,impulseMagnitude));
	body2.internalGetTurnVelocity().mVec128 = _mm_add_ps(body2.internalGetTurnVelocity().mVec128 ,_mm_mul_ps(c.m_angularComponentB.mVec128,impulseMagnitude));
#else
	resolveSplitPenetrationImpulseCacheFriendly(body1,body2,c);
#endif
}



unsigned long btSequentialImpulseConstraintSolver::btRand2()
{
	m_btSeed2 = (1664525L*m_btSeed2 + 1013904223L) & 0xffffffff;
	return m_btSeed2;
}



//See ODE: adam's all-int straightforward(?) dRandInt (0..n-1)
int btSequentialImpulseConstraintSolver::btRandInt2 (int n)
{
	// seems good; xor-fold and modulus
	const unsigned long un = static_cast<unsigned long>(n);
	unsigned long r = btRand2();

	// note: probably more aggressive than it needs to be -- might be
	//       able to get away without one or two of the innermost branches.
	if (un <= 0x00010000UL) {
		r ^= (r >> 16);
		if (un <= 0x00000100UL) {
			r ^= (r >> 8);
			if (un <= 0x00000010UL) {
				r ^= (r >> 4);
				if (un <= 0x00000004UL) {
					r ^= (r >> 2);
					if (un <= 0x00000002UL) {
						r ^= (r >> 1);
					}
				}
			}
		}
	}

	return (int) (r % un);
}



void	btSequentialImpulseConstraintSolver::initSolverBody(btSolverBody* solverBody, btCollisionObject* collisionObject, btScalar timeStep)
{

	btRigidBody* rb = collisionObject? btRigidBody::upcast(collisionObject) : 0;

	solverBody->internalGetDeltaLinearVelocity().setValue(0.f,0.f,0.f);
	solverBody->internalGetDeltaAngularVelocity().setValue(0.f,0.f,0.f);
	solverBody->internalGetPushVelocity().setValue(0.f,0.f,0.f);
	solverBody->internalGetTurnVelocity().setValue(0.f,0.f,0.f);

	if (rb)
	{
		solverBody->m_worldTransform = rb->getWorldTransform();
		solverBody->internalSetInvMass(btVector3(rb->getInvMass(),rb->getInvMass(),rb->getInvMass())*rb->getLinearFactor());
		solverBody->m_originalBody = rb;
		solverBody->m_angularFactor = rb->getAngularFactor();
		solverBody->m_linearFactor = rb->getLinearFactor();
		solverBody->m_linearVelocity = rb->getLinearVelocity();
		solverBody->m_angularVelocity = rb->getAngularVelocity();
		solverBody->m_externalForceImpulse = rb->getTotalForce()*rb->getInvMass()*timeStep;
		solverBody->m_externalTorqueImpulse = rb->getTotalTorque()*rb->getInvInertiaTensorWorld()*timeStep ;
		
	} else
	{
		solverBody->m_worldTransform.setIdentity();
		solverBody->internalSetInvMass(btVector3(0,0,0));
		solverBody->m_originalBody = 0;
		solverBody->m_angularFactor.setValue(1,1,1);
		solverBody->m_linearFactor.setValue(1,1,1);
		solverBody->m_linearVelocity.setValue(0,0,0);
		solverBody->m_angularVelocity.setValue(0,0,0);
		solverBody->m_externalForceImpulse.setValue(0,0,0);
		solverBody->m_externalTorqueImpulse.setValue(0,0,0);
	}


}






btScalar btSequentialImpulseConstraintSolver::restitutionCurve(btScalar rel_vel, btScalar restitution)
{
	btScalar rest = restitution * -rel_vel;
	return rest;
}



void	btSequentialImpulseConstraintSolver::applyAnisotropicFriction(btCollisionObject* colObj,btVector3& frictionDirection, int frictionMode)
{
	

	if (colObj && colObj->hasAnisotropicFriction(frictionMode))
	{
		// transform to local coordinates
		btVector3 loc_lateral = frictionDirection * colObj->getWorldTransform().getBasis();
		const btVector3& friction_scaling = colObj->getAnisotropicFriction();
		//apply anisotropic friction
		loc_lateral *= friction_scaling;
		// ... and transform it back to global coordinates
		frictionDirection = colObj->getWorldTransform().getBasis() * loc_lateral;
	}

}




void btSequentialImpulseConstraintSolver::setupFrictionConstraint(btSolverConstraint& solverConstraint, const btVector3& normalAxis,int  solverBodyIdA,int solverBodyIdB,btManifoldPoint& cp,const btVector3& rel_pos1,const btVector3& rel_pos2,btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, btScalar desiredVelocity, btScalar cfmSlip)
{

	
	btSolverBody& solverBodyA = m_tmpSolverBodyPool[solverBodyIdA];
	btSolverBody& solverBodyB = m_tmpSolverBodyPool[solverBodyIdB];

	btRigidBody* body0 = m_tmpSolverBodyPool[solverBodyIdA].m_originalBody;
	btRigidBody* body1 = m_tmpSolverBodyPool[solverBodyIdB].m_originalBody;

	solverConstraint.m_solverBodyIdA = solverBodyIdA;
	solverConstraint.m_solverBodyIdB = solverBodyIdB;

	solverConstraint.m_friction = cp.m_combinedFriction;
	solverConstraint.m_originalContactPoint = 0;

	solverConstraint.m_appliedImpulse = 0.f;
	solverConstraint.m_appliedPushImpulse = 0.f;

	if (body0)
	{
		solverConstraint.m_contactNormal1 = normalAxis;
		btVector3 ftorqueAxis1 = rel_pos1.cross(solverConstraint.m_contactNormal1);
		solverConstraint.m_relpos1CrossNormal = ftorqueAxis1;
		solverConstraint.m_angularComponentA = body0->getInvInertiaTensorWorld()*ftorqueAxis1*body0->getAngularFactor();
	}else
	{
		solverConstraint.m_contactNormal1.setZero();
		solverConstraint.m_relpos1CrossNormal.setZero();
		solverConstraint.m_angularComponentA .setZero();
	}

	if (body1)
	{
		solverConstraint.m_contactNormal2 = -normalAxis;
		btVector3 ftorqueAxis1 = rel_pos2.cross(solverConstraint.m_contactNormal2);
		solverConstraint.m_relpos2CrossNormal = ftorqueAxis1;
		solverConstraint.m_angularComponentB = body1->getInvInertiaTensorWorld()*ftorqueAxis1*body1->getAngularFactor();
	} else
	{
		solverConstraint.m_contactNormal2.setZero();
		solverConstraint.m_relpos2CrossNormal.setZero();
		solverConstraint.m_angularComponentB.setZero();
	}

	{
		btVector3 vec;
		btScalar denom0 = 0.f;
		btScalar denom1 = 0.f;
		if (body0)
		{
			vec = ( solverConstraint.m_angularComponentA).cross(rel_pos1);
			denom0 = body0->getInvMass() + normalAxis.dot(vec);
		}
		if (body1)
		{
			vec = ( -solverConstraint.m_angularComponentB).cross(rel_pos2);
			denom1 = body1->getInvMass() + normalAxis.dot(vec);
		}
		btScalar denom = relaxation/(denom0+denom1);
		solverConstraint.m_jacDiagABInv = denom;
	}

	{
		

		btScalar rel_vel;
		btScalar vel1Dotn = solverConstraint.m_contactNormal1.dot(body0?solverBodyA.m_linearVelocity+solverBodyA.m_externalForceImpulse:btVector3(0,0,0)) 
			+ solverConstraint.m_relpos1CrossNormal.dot(body0?solverBodyA.m_angularVelocity:btVector3(0,0,0));
		btScalar vel2Dotn = solverConstraint.m_contactNormal2.dot(body1?solverBodyB.m_linearVelocity+solverBodyB.m_externalForceImpulse:btVector3(0,0,0)) 
			+ solverConstraint.m_relpos2CrossNormal.dot(body1?solverBodyB.m_angularVelocity:btVector3(0,0,0));

		rel_vel = vel1Dotn+vel2Dotn;

//		btScalar positionalError = 0.f;

		btSimdScalar velocityError =  desiredVelocity - rel_vel;
		btSimdScalar	velocityImpulse = velocityError * btSimdScalar(solverConstraint.m_jacDiagABInv);
		solverConstraint.m_rhs = velocityImpulse;
		solverConstraint.m_cfm = cfmSlip;
		solverConstraint.m_lowerLimit = -solverConstraint.m_friction;
		solverConstraint.m_upperLimit = solverConstraint.m_friction;
		
	}
}

btSolverConstraint&	btSequentialImpulseConstraintSolver::addFrictionConstraint(const btVector3& normalAxis,int solverBodyIdA,int solverBodyIdB,int frictionIndex,btManifoldPoint& cp,const btVector3& rel_pos1,const btVector3& rel_pos2,btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, btScalar desiredVelocity, btScalar cfmSlip)
{
	btSolverConstraint& solverConstraint = m_tmpSolverContactFrictionConstraintPool.expandNonInitializing();
	solverConstraint.m_frictionIndex = frictionIndex;
	setupFrictionConstraint(solverConstraint, normalAxis, solverBodyIdA, solverBodyIdB, cp, rel_pos1, rel_pos2, 
							colObj0, colObj1, relaxation, desiredVelocity, cfmSlip);
	return solverConstraint;
}


void btSequentialImpulseConstraintSolver::setupRollingFrictionConstraint(	btSolverConstraint& solverConstraint, const btVector3& normalAxis1,int solverBodyIdA,int  solverBodyIdB,
									btManifoldPoint& cp,const btVector3& rel_pos1,const btVector3& rel_pos2,
									btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, 
									btScalar desiredVelocity, btScalar cfmSlip)

{
	btVector3 normalAxis(0,0,0);


	solverConstraint.m_contactNormal1 = normalAxis;
	solverConstraint.m_contactNormal2 = -normalAxis;
	btSolverBody& solverBodyA = m_tmpSolverBodyPool[solverBodyIdA];
	btSolverBody& solverBodyB = m_tmpSolverBodyPool[solverBodyIdB];

	btRigidBody* body0 = m_tmpSolverBodyPool[solverBodyIdA].m_originalBody;
	btRigidBody* body1 = m_tmpSolverBodyPool[solverBodyIdB].m_originalBody;

	solverConstraint.m_solverBodyIdA = solverBodyIdA;
	solverConstraint.m_solverBodyIdB = solverBodyIdB;

	solverConstraint.m_friction = cp.m_combinedRollingFriction;
	solverConstraint.m_originalContactPoint = 0;

	solverConstraint.m_appliedImpulse = 0.f;
	solverConstraint.m_appliedPushImpulse = 0.f;

	{
		btVector3 ftorqueAxis1 = -normalAxis1;
		solverConstraint.m_relpos1CrossNormal = ftorqueAxis1;
		solverConstraint.m_angularComponentA = body0 ? body0->getInvInertiaTensorWorld()*ftorqueAxis1*body0->getAngularFactor() : btVector3(0,0,0);
	}
	{
		btVector3 ftorqueAxis1 = normalAxis1;
		solverConstraint.m_relpos2CrossNormal = ftorqueAxis1;
		solverConstraint.m_angularComponentB = body1 ? body1->getInvInertiaTensorWorld()*ftorqueAxis1*body1->getAngularFactor() : btVector3(0,0,0);
	}


	{
		btVector3 iMJaA = body0?body0->getInvInertiaTensorWorld()*solverConstraint.m_relpos1CrossNormal:btVector3(0,0,0);
		btVector3 iMJaB = body1?body1->getInvInertiaTensorWorld()*solverConstraint.m_relpos2CrossNormal:btVector3(0,0,0);
		btScalar sum = 0;
		sum += iMJaA.dot(solverConstraint.m_relpos1CrossNormal);
		sum += iMJaB.dot(solverConstraint.m_relpos2CrossNormal);
		solverConstraint.m_jacDiagABInv = btScalar(1.)/sum;
	}

	{
		

		btScalar rel_vel;
		btScalar vel1Dotn = solverConstraint.m_contactNormal1.dot(body0?solverBodyA.m_linearVelocity+solverBodyA.m_externalForceImpulse:btVector3(0,0,0)) 
			+ solverConstraint.m_relpos1CrossNormal.dot(body0?solverBodyA.m_angularVelocity:btVector3(0,0,0));
		btScalar vel2Dotn = solverConstraint.m_contactNormal2.dot(body1?solverBodyB.m_linearVelocity+solverBodyB.m_externalForceImpulse:btVector3(0,0,0)) 
			+ solverConstraint.m_relpos2CrossNormal.dot(body1?solverBodyB.m_angularVelocity:btVector3(0,0,0));

		rel_vel = vel1Dotn+vel2Dotn;

//		btScalar positionalError = 0.f;

		btSimdScalar velocityError =  desiredVelocity - rel_vel;
		btSimdScalar	velocityImpulse = velocityError * btSimdScalar(solverConstraint.m_jacDiagABInv);
		solverConstraint.m_rhs = velocityImpulse;
		solverConstraint.m_cfm = cfmSlip;
		solverConstraint.m_lowerLimit = -solverConstraint.m_friction;
		solverConstraint.m_upperLimit = solverConstraint.m_friction;
		
	}
}








btSolverConstraint&	btSequentialImpulseConstraintSolver::addRollingFrictionConstraint(const btVector3& normalAxis,int solverBodyIdA,int solverBodyIdB,int frictionIndex,btManifoldPoint& cp,const btVector3& rel_pos1,const btVector3& rel_pos2,btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, btScalar desiredVelocity, btScalar cfmSlip)
{
	btSolverConstraint& solverConstraint = m_tmpSolverContactRollingFrictionConstraintPool.expandNonInitializing();
	solverConstraint.m_frictionIndex = frictionIndex;
	setupRollingFrictionConstraint(solverConstraint, normalAxis, solverBodyIdA, solverBodyIdB, cp, rel_pos1, rel_pos2, 
							colObj0, colObj1, relaxation, desiredVelocity, cfmSlip);
	return solverConstraint;
}


int	btSequentialImpulseConstraintSolver::getOrInitSolverBody(btCollisionObject& body,btScalar timeStep)
{

	int solverBodyIdA = -1;

	if (body.getCompanionId() >= 0)
	{
		//body has already been converted
		solverBodyIdA = body.getCompanionId();
        btAssert(solverBodyIdA < m_tmpSolverBodyPool.size());
	} else
	{
		btRigidBody* rb = btRigidBody::upcast(&body);
		//convert both active and kinematic objects (for their velocity)
		if (rb && (rb->getInvMass() || rb->isKinematicObject()))
		{
			solverBodyIdA = m_tmpSolverBodyPool.size();
			btSolverBody& solverBody = m_tmpSolverBodyPool.expand();
			initSolverBody(&solverBody,&body,timeStep);
			body.setCompanionId(solverBodyIdA);
		} else
		{
			
			if (m_fixedBodyId<0)
			{
				m_fixedBodyId = m_tmpSolverBodyPool.size();
				btSolverBody& fixedBody = m_tmpSolverBodyPool.expand();
				initSolverBody(&fixedBody,0,timeStep);
			}
			return m_fixedBodyId;
//			return 0;//assume first one is a fixed solver body
		}
	}

	return solverBodyIdA;

}
#include <stdio.h>


void btSequentialImpulseConstraintSolver::setupContactConstraint(btSolverConstraint& solverConstraint, 
																 int solverBodyIdA, int solverBodyIdB,
																 btManifoldPoint& cp, const btContactSolverInfo& infoGlobal,
																 btScalar& relaxation,
																 const btVector3& rel_pos1, const btVector3& rel_pos2)
{
			
			const btVector3& pos1 = cp.getPositionWorldOnA();
			const btVector3& pos2 = cp.getPositionWorldOnB();

			btSolverBody* bodyA = &m_tmpSolverBodyPool[solverBodyIdA];
			btSolverBody* bodyB = &m_tmpSolverBodyPool[solverBodyIdB];

			btRigidBody* rb0 = bodyA->m_originalBody;
			btRigidBody* rb1 = bodyB->m_originalBody;

//			btVector3 rel_pos1 = pos1 - colObj0->getWorldTransform().getOrigin(); 
//			btVector3 rel_pos2 = pos2 - colObj1->getWorldTransform().getOrigin();
			//rel_pos1 = pos1 - bodyA->getWorldTransform().getOrigin(); 
			//rel_pos2 = pos2 - bodyB->getWorldTransform().getOrigin();

			relaxation = 1.f;

			btVector3 torqueAxis0 = rel_pos1.cross(cp.m_normalWorldOnB);
			solverConstraint.m_angularComponentA = rb0 ? rb0->getInvInertiaTensorWorld()*torqueAxis0*rb0->getAngularFactor() : btVector3(0,0,0);
			btVector3 torqueAxis1 = rel_pos2.cross(cp.m_normalWorldOnB);		
			solverConstraint.m_angularComponentB = rb1 ? rb1->getInvInertiaTensorWorld()*-torqueAxis1*rb1->getAngularFactor() : btVector3(0,0,0);

				{
#ifdef COMPUTE_IMPULSE_DENOM
					btScalar denom0 = rb0->computeImpulseDenominator(pos1,cp.m_normalWorldOnB);
					btScalar denom1 = rb1->computeImpulseDenominator(pos2,cp.m_normalWorldOnB);
#else							
					btVector3 vec;
					btScalar denom0 = 0.f;
					btScalar denom1 = 0.f;
					if (rb0)
					{
						vec = ( solverConstraint.m_angularComponentA).cross(rel_pos1);
						denom0 = rb0->getInvMass() + cp.m_normalWorldOnB.dot(vec);
					}
					if (rb1)
					{
						vec = ( -solverConstraint.m_angularComponentB).cross(rel_pos2);
						denom1 = rb1->getInvMass() + cp.m_normalWorldOnB.dot(vec);
					}
#endif //COMPUTE_IMPULSE_DENOM		

					btScalar denom = relaxation/(denom0+denom1);
					solverConstraint.m_jacDiagABInv = denom;
				}

				if (rb0)
				{
					solverConstraint.m_contactNormal1 = cp.m_normalWorldOnB;
					solverConstraint.m_relpos1CrossNormal = torqueAxis0;
				} else
				{
					solverConstraint.m_contactNormal1.setZero();
					solverConstraint.m_relpos1CrossNormal.setZero();
				}
				if (rb1)
				{
					solverConstraint.m_contactNormal2 = -cp.m_normalWorldOnB;
					solverConstraint.m_relpos2CrossNormal = -torqueAxis1;
				}else
				{
					solverConstraint.m_contactNormal2.setZero();
					solverConstraint.m_relpos2CrossNormal.setZero();
				}

				btScalar restitution = 0.f;
				btScalar penetration = cp.getDistance()+infoGlobal.m_linearSlop;

				{
					btVector3 vel1,vel2;

					vel1 = rb0? rb0->getVelocityInLocalPoint(rel_pos1) : btVector3(0,0,0);
					vel2 = rb1? rb1->getVelocityInLocalPoint(rel_pos2) : btVector3(0,0,0);

	//			btVector3 vel2 = rb1 ? rb1->getVelocityInLocalPoint(rel_pos2) : btVector3(0,0,0);
					btVector3 vel  = vel1 - vel2;
					btScalar rel_vel = cp.m_normalWorldOnB.dot(vel);

					

					solverConstraint.m_friction = cp.m_combinedFriction;

				
					restitution =  restitutionCurve(rel_vel, cp.m_combinedRestitution);
					if (restitution <= btScalar(0.))
					{
						restitution = 0.f;
					};
				}


				///warm starting (or zero if disabled)
				if (infoGlobal.m_solverMode & SOLVER_USE_WARMSTARTING)
				{
					solverConstraint.m_appliedImpulse = cp.m_appliedImpulse * infoGlobal.m_warmstartingFactor;
					if (rb0)
						bodyA->internalApplyImpulse(solverConstraint.m_contactNormal1*bodyA->internalGetInvMass()*rb0->getLinearFactor(),solverConstraint.m_angularComponentA,solverConstraint.m_appliedImpulse);
					if (rb1)
						bodyB->internalApplyImpulse(-solverConstraint.m_contactNormal2*bodyB->internalGetInvMass()*rb1->getLinearFactor(),-solverConstraint.m_angularComponentB,-(btScalar)solverConstraint.m_appliedImpulse);
				} else
				{
					solverConstraint.m_appliedImpulse = 0.f;
				}

				solverConstraint.m_appliedPushImpulse = 0.f;

				{

					btVector3 externalForceImpulseA = bodyA->m_originalBody ? bodyA->m_externalForceImpulse: btVector3(0,0,0);
					btVector3 externalTorqueImpulseA = bodyA->m_originalBody ? bodyA->m_externalTorqueImpulse: btVector3(0,0,0);
					btVector3 externalForceImpulseB = bodyB->m_originalBody ? bodyB->m_externalForceImpulse: btVector3(0,0,0);
					btVector3 externalTorqueImpulseB = bodyB->m_originalBody ?bodyB->m_externalTorqueImpulse : btVector3(0,0,0);
						

					btScalar vel1Dotn = solverConstraint.m_contactNormal1.dot(bodyA->m_linearVelocity+externalForceImpulseA) 
						+ solverConstraint.m_relpos1CrossNormal.dot(bodyA->m_angularVelocity+externalTorqueImpulseA);
					btScalar vel2Dotn = solverConstraint.m_contactNormal2.dot(bodyB->m_linearVelocity+externalForceImpulseB) 
						+ solverConstraint.m_relpos2CrossNormal.dot(bodyB->m_angularVelocity+externalTorqueImpulseB);
					btScalar rel_vel = vel1Dotn+vel2Dotn;

					btScalar positionalError = 0.f;
					btScalar	velocityError = restitution - rel_vel;// * damping;
					

					btScalar erp = infoGlobal.m_erp2;
					if (!infoGlobal.m_splitImpulse || (penetration > infoGlobal.m_splitImpulsePenetrationThreshold))
					{
						erp = infoGlobal.m_erp;
					}

					if (penetration>0)
					{
						positionalError = 0;

						velocityError -= penetration / infoGlobal.m_timeStep;
					} else
					{
						positionalError = -penetration * erp/infoGlobal.m_timeStep;
					}

					btScalar  penetrationImpulse = positionalError*solverConstraint.m_jacDiagABInv;
					btScalar velocityImpulse = velocityError *solverConstraint.m_jacDiagABInv;

					if (!infoGlobal.m_splitImpulse || (penetration > infoGlobal.m_splitImpulsePenetrationThreshold))
					{
						//combine position and velocity into rhs
						solverConstraint.m_rhs = penetrationImpulse+velocityImpulse;//-solverConstraint.m_contactNormal1.dot(bodyA->m_externalForce*bodyA->m_invMass-bodyB->m_externalForce/bodyB->m_invMass)*solverConstraint.m_jacDiagABInv;
						solverConstraint.m_rhsPenetration = 0.f;

					} else
					{
						//split position and velocity into rhs and m_rhsPenetration
						solverConstraint.m_rhs = velocityImpulse;
						solverConstraint.m_rhsPenetration = penetrationImpulse;
					}
					solverConstraint.m_cfm = 0.f;
					solverConstraint.m_lowerLimit = 0;
					solverConstraint.m_upperLimit = 1e10f;
				}




}



void btSequentialImpulseConstraintSolver::setFrictionConstraintImpulse( btSolverConstraint& solverConstraint, 
																		int solverBodyIdA, int solverBodyIdB,
																 btManifoldPoint& cp, const btContactSolverInfo& infoGlobal)
{

	btSolverBody* bodyA = &m_tmpSolverBodyPool[solverBodyIdA];
	btSolverBody* bodyB = &m_tmpSolverBodyPool[solverBodyIdB];

	btRigidBody* rb0 = bodyA->m_originalBody;
	btRigidBody* rb1 = bodyB->m_originalBody;

	{
		btSolverConstraint& frictionConstraint1 = m_tmpSolverContactFrictionConstraintPool[solverConstraint.m_frictionIndex];
		if (infoGlobal.m_solverMode & SOLVER_USE_WARMSTARTING)
		{
			frictionConstraint1.m_appliedImpulse = cp.m_appliedImpulseLateral1 * infoGlobal.m_warmstartingFactor;
			if (rb0)
				bodyA->internalApplyImpulse(frictionConstraint1.m_contactNormal1*rb0->getInvMass()*rb0->getLinearFactor(),frictionConstraint1.m_angularComponentA,frictionConstraint1.m_appliedImpulse);
			if (rb1)
				bodyB->internalApplyImpulse(-frictionConstraint1.m_contactNormal2*rb1->getInvMass()*rb1->getLinearFactor(),-frictionConstraint1.m_angularComponentB,-(btScalar)frictionConstraint1.m_appliedImpulse);
		} else
		{
			frictionConstraint1.m_appliedImpulse = 0.f;
		}
	}

	if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
	{
		btSolverConstraint& frictionConstraint2 = m_tmpSolverContactFrictionConstraintPool[solverConstraint.m_frictionIndex+1];
		if (infoGlobal.m_solverMode & SOLVER_USE_WARMSTARTING)
		{
			frictionConstraint2.m_appliedImpulse = cp.m_appliedImpulseLateral2  * infoGlobal.m_warmstartingFactor;
			if (rb0)
				bodyA->internalApplyImpulse(frictionConstraint2.m_contactNormal1*rb0->getInvMass(),frictionConstraint2.m_angularComponentA,frictionConstraint2.m_appliedImpulse);
			if (rb1)
				bodyB->internalApplyImpulse(-frictionConstraint2.m_contactNormal2*rb1->getInvMass(),-frictionConstraint2.m_angularComponentB,-(btScalar)frictionConstraint2.m_appliedImpulse);
		} else
		{
			frictionConstraint2.m_appliedImpulse = 0.f;
		}
	}
}




void	btSequentialImpulseConstraintSolver::convertContact(btPersistentManifold* manifold,const btContactSolverInfo& infoGlobal)
{
	btCollisionObject* colObj0=0,*colObj1=0;

	colObj0 = (btCollisionObject*)manifold->getBody0();
	colObj1 = (btCollisionObject*)manifold->getBody1();

	int solverBodyIdA = getOrInitSolverBody(*colObj0,infoGlobal.m_timeStep);
	int solverBodyIdB = getOrInitSolverBody(*colObj1,infoGlobal.m_timeStep);

//	btRigidBody* bodyA = btRigidBody::upcast(colObj0);
//	btRigidBody* bodyB = btRigidBody::upcast(colObj1);

	btSolverBody* solverBodyA = &m_tmpSolverBodyPool[solverBodyIdA];
	btSolverBody* solverBodyB = &m_tmpSolverBodyPool[solverBodyIdB];



	///avoid collision response between two static objects
	if (!solverBodyA || (solverBodyA->m_invMass.isZero() && (!solverBodyB || solverBodyB->m_invMass.isZero())))
		return;

	int rollingFriction=1;
	for (int j=0;j<manifold->getNumContacts();j++)
	{

		btManifoldPoint& cp = manifold->getContactPoint(j);

		if (cp.getDistance() <= manifold->getContactProcessingThreshold())
		{
			btVector3 rel_pos1;
			btVector3 rel_pos2;
			btScalar relaxation;
			

			int frictionIndex = m_tmpSolverContactConstraintPool.size();
			btSolverConstraint& solverConstraint = m_tmpSolverContactConstraintPool.expandNonInitializing();
			btRigidBody* rb0 = btRigidBody::upcast(colObj0);
			btRigidBody* rb1 = btRigidBody::upcast(colObj1);
			solverConstraint.m_solverBodyIdA = solverBodyIdA;
			solverConstraint.m_solverBodyIdB = solverBodyIdB;

			solverConstraint.m_originalContactPoint = &cp;

			const btVector3& pos1 = cp.getPositionWorldOnA();
			const btVector3& pos2 = cp.getPositionWorldOnB();

			rel_pos1 = pos1 - colObj0->getWorldTransform().getOrigin(); 
			rel_pos2 = pos2 - colObj1->getWorldTransform().getOrigin();

			btVector3 vel1;// = rb0 ? rb0->getVelocityInLocalPoint(rel_pos1) : btVector3(0,0,0);
			btVector3 vel2;// = rb1 ? rb1->getVelocityInLocalPoint(rel_pos2) : btVector3(0,0,0);

			solverBodyA->getVelocityInLocalPointNoDelta(rel_pos1,vel1);
			solverBodyB->getVelocityInLocalPointNoDelta(rel_pos2,vel2 );
			
			btVector3 vel  = vel1 - vel2;
			btScalar rel_vel = cp.m_normalWorldOnB.dot(vel);

			setupContactConstraint(solverConstraint, solverBodyIdA, solverBodyIdB, cp, infoGlobal, relaxation, rel_pos1, rel_pos2);

			

//			const btVector3& pos1 = cp.getPositionWorldOnA();
//			const btVector3& pos2 = cp.getPositionWorldOnB();

			/////setup the friction constraints

			solverConstraint.m_frictionIndex = m_tmpSolverContactFrictionConstraintPool.size();

			btVector3 angVelA(0,0,0),angVelB(0,0,0);
			if (rb0)
				angVelA = rb0->getAngularVelocity();
			if (rb1)
				angVelB = rb1->getAngularVelocity();
			btVector3 relAngVel = angVelB-angVelA;

			if ((cp.m_combinedRollingFriction>0.f) && (rollingFriction>0))
			{
				//only a single rollingFriction per manifold
				rollingFriction--;
				if (relAngVel.length()>infoGlobal.m_singleAxisRollingFrictionThreshold)
				{
					relAngVel.normalize();
					applyAnisotropicFriction(colObj0,relAngVel,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj1,relAngVel,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					if (relAngVel.length()>0.001)
						addRollingFrictionConstraint(relAngVel,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);

				} else
				{
					addRollingFrictionConstraint(cp.m_normalWorldOnB,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
					btVector3 axis0,axis1;
					btPlaneSpace1(cp.m_normalWorldOnB,axis0,axis1);
					applyAnisotropicFriction(colObj0,axis0,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj1,axis0,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj0,axis1,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj1,axis1,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					if (axis0.length()>0.001)
						addRollingFrictionConstraint(axis0,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
					if (axis1.length()>0.001)
						addRollingFrictionConstraint(axis1,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
		
				}
			}

			///Bullet has several options to set the friction directions
			///By default, each contact has only a single friction direction that is recomputed automatically very frame 
			///based on the relative linear velocity.
			///If the relative velocity it zero, it will automatically compute a friction direction.
			
			///You can also enable two friction directions, using the SOLVER_USE_2_FRICTION_DIRECTIONS.
			///In that case, the second friction direction will be orthogonal to both contact normal and first friction direction.
			///
			///If you choose SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION, then the friction will be independent from the relative projected velocity.
			///
			///The user can manually override the friction directions for certain contacts using a contact callback, 
			///and set the cp.m_lateralFrictionInitialized to true
			///In that case, you can set the target relative motion in each friction direction (cp.m_contactMotion1 and cp.m_contactMotion2)
			///this will give a conveyor belt effect
			///
			if (!(infoGlobal.m_solverMode & SOLVER_ENABLE_FRICTION_DIRECTION_CACHING) || !cp.m_lateralFrictionInitialized)
			{
				cp.m_lateralFrictionDir1 = vel - cp.m_normalWorldOnB * rel_vel;
				btScalar lat_rel_vel = cp.m_lateralFrictionDir1.length2();
				if (!(infoGlobal.m_solverMode & SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION) && lat_rel_vel > SIMD_EPSILON)
				{
					cp.m_lateralFrictionDir1 *= 1.f/btSqrt(lat_rel_vel);
					applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					addFrictionConstraint(cp.m_lateralFrictionDir1,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);

					if((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
					{
						cp.m_lateralFrictionDir2 = cp.m_lateralFrictionDir1.cross(cp.m_normalWorldOnB);
						cp.m_lateralFrictionDir2.normalize();//??
						applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						addFrictionConstraint(cp.m_lateralFrictionDir2,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
					}

				} else
				{
					btPlaneSpace1(cp.m_normalWorldOnB,cp.m_lateralFrictionDir1,cp.m_lateralFrictionDir2);

					applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					addFrictionConstraint(cp.m_lateralFrictionDir1,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);

					if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
					{
						applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						addFrictionConstraint(cp.m_lateralFrictionDir2,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
					}


					if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS) && (infoGlobal.m_solverMode & SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION))
					{
						cp.m_lateralFrictionInitialized = true;
					}
				}

			} else
			{
				addFrictionConstraint(cp.m_lateralFrictionDir1,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation,cp.m_contactMotion1, cp.m_contactCFM1);

				if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
					addFrictionConstraint(cp.m_lateralFrictionDir2,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation, cp.m_contactMotion2, cp.m_contactCFM2);

			}
			setFrictionConstraintImpulse( solverConstraint, solverBodyIdA, solverBodyIdB, cp, infoGlobal);
		

			

		}
	}
}

void btSequentialImpulseConstraintSolver::convertContacts(btPersistentManifold** manifoldPtr,int numManifolds, const btContactSolverInfo& infoGlobal)
{
	int i;
	btPersistentManifold* manifold = 0;
//			btCollisionObject* colObj0=0,*colObj1=0;


	for (i=0;i<numManifolds;i++)
	{
		manifold = manifoldPtr[i];
		convertContact(manifold,infoGlobal);
	}
}

btScalar btSequentialImpulseConstraintSolver::solveGroupCacheFriendlySetup(btCollisionObject** bodies, int numBodies, btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer)
{
	m_fixedBodyId = -1;
	BT_PROFILE("solveGroupCacheFriendlySetup");
	(void)debugDrawer;

	m_maxOverrideNumSolverIterations = 0;

#ifdef BT_ADDITIONAL_DEBUG
	 //make sure that dynamic bodies exist for all (enabled) constraints
	for (int i=0;i<numConstraints;i++)
	{
		btTypedConstraint* constraint = constraints[i];
		if (constraint->isEnabled())
		{
			if (!constraint->getRigidBodyA().isStaticOrKinematicObject())
			{
				bool found=false;
				for (int b=0;b<numBodies;b++)
				{
                
					if (&constraint->getRigidBodyA()==bodies[b])
					{
						found = true;
						break;
					}
				}
				btAssert(found);
			}
			if (!constraint->getRigidBodyB().isStaticOrKinematicObject())
			{
				bool found=false;
				for (int b=0;b<numBodies;b++)
				{
					if (&constraint->getRigidBodyB()==bodies[b])
					{
						found = true;
						break;
					}
				}
				btAssert(found);
			}
		}
	}
    //make sure that dynamic bodies exist for all contact manifolds
    for (int i=0;i<numManifolds;i++)
    {
        if (!manifoldPtr[i]->getBody0()->isStaticOrKinematicObject())
        {
            bool found=false;
            for (int b=0;b<numBodies;b++)
            {
                
                if (manifoldPtr[i]->getBody0()==bodies[b])
                {
                    found = true;
                    break;
                }
            }
            btAssert(found);
        }
        if (!manifoldPtr[i]->getBody1()->isStaticOrKinematicObject())
        {
            bool found=false;
            for (int b=0;b<numBodies;b++)
            {
                if (manifoldPtr[i]->getBody1()==bodies[b])
                {
                    found = true;
                    break;
                }
            }
            btAssert(found);
        }
    }
#endif //BT_ADDITIONAL_DEBUG
	
	
	for (int i = 0; i < numBodies; i++)
	{
		bodies[i]->setCompanionId(-1);
	}


	m_tmpSolverBodyPool.reserve(numBodies+1);
	m_tmpSolverBodyPool.resize(0);

	//btSolverBody& fixedBody = m_tmpSolverBodyPool.expand();
    //initSolverBody(&fixedBody,0);

	//convert all bodies

	for (int i=0;i<numBodies;i++)
	{
		int bodyId = getOrInitSolverBody(*bodies[i],infoGlobal.m_timeStep);

		btRigidBody* body = btRigidBody::upcast(bodies[i]);
		if (body && body->getInvMass())
		{
			btSolverBody& solverBody = m_tmpSolverBodyPool[bodyId];
			btVector3 gyroForce (0,0,0);
			if (body->getFlags()&BT_ENABLE_GYROPSCOPIC_FORCE)
			{
				gyroForce = body->computeGyroscopicForce(infoGlobal.m_maxGyroscopicForce);
				solverBody.m_externalTorqueImpulse -= gyroForce*body->getInvInertiaTensorWorld()*infoGlobal.m_timeStep;
			}
		}
	}
	
	if (1)
	{
		int j;
		for (j=0;j<numConstraints;j++)
		{
			btTypedConstraint* constraint = constraints[j];
			constraint->buildJacobian();
			constraint->internalSetAppliedImpulse(0.0f);
		}
	}

	//btRigidBody* rb0=0,*rb1=0;

	//if (1)
	{
		{

			int totalNumRows = 0;
			int i;
			
			m_tmpConstraintSizesPool.resizeNoInitialize(numConstraints);
			//calculate the total number of contraint rows
			for (i=0;i<numConstraints;i++)
			{
				btTypedConstraint::btConstraintInfo1& info1 = m_tmpConstraintSizesPool[i];
				btJointFeedback* fb = constraints[i]->getJointFeedback();
				if (fb)
				{
					fb->m_appliedForceBodyA.setZero();
					fb->m_appliedTorqueBodyA.setZero();
					fb->m_appliedForceBodyB.setZero();
					fb->m_appliedTorqueBodyB.setZero();
				}

				if (constraints[i]->isEnabled())
				{
				}
				if (constraints[i]->isEnabled())
				{
					constraints[i]->getInfo1(&info1);
				} else
				{
					info1.m_numConstraintRows = 0;
					info1.nub = 0;
				}
				totalNumRows += info1.m_numConstraintRows;
			}
			m_tmpSolverNonContactConstraintPool.resizeNoInitialize(totalNumRows);

			
			///setup the btSolverConstraints
			int currentRow = 0;

			for (i=0;i<numConstraints;i++)
			{
				const btTypedConstraint::btConstraintInfo1& info1 = m_tmpConstraintSizesPool[i];
				
				if (info1.m_numConstraintRows)
				{
					btAssert(currentRow<totalNumRows);

					btSolverConstraint* currentConstraintRow = &m_tmpSolverNonContactConstraintPool[currentRow];
					btTypedConstraint* constraint = constraints[i];
					btRigidBody& rbA = constraint->getRigidBodyA();
					btRigidBody& rbB = constraint->getRigidBodyB();

					int solverBodyIdA = getOrInitSolverBody(rbA,infoGlobal.m_timeStep);
                    int solverBodyIdB = getOrInitSolverBody(rbB,infoGlobal.m_timeStep);

                    btSolverBody* bodyAPtr = &m_tmpSolverBodyPool[solverBodyIdA];
                    btSolverBody* bodyBPtr = &m_tmpSolverBodyPool[solverBodyIdB];




					int overrideNumSolverIterations = constraint->getOverrideNumSolverIterations() > 0 ? constraint->getOverrideNumSolverIterations() : infoGlobal.m_numIterations;
					if (overrideNumSolverIterations>m_maxOverrideNumSolverIterations)
						m_maxOverrideNumSolverIterations = overrideNumSolverIterations;


					int j;
					for ( j=0;j<info1.m_numConstraintRows;j++)
					{
						memset(&currentConstraintRow[j],0,sizeof(btSolverConstraint));
						currentConstraintRow[j].m_lowerLimit = -SIMD_INFINITY;
						currentConstraintRow[j].m_upperLimit = SIMD_INFINITY;
						currentConstraintRow[j].m_appliedImpulse = 0.f;
						currentConstraintRow[j].m_appliedPushImpulse = 0.f;
						currentConstraintRow[j].m_solverBodyIdA = solverBodyIdA;
						currentConstraintRow[j].m_solverBodyIdB = solverBodyIdB;
						currentConstraintRow[j].m_overrideNumSolverIterations = overrideNumSolverIterations;
					}

					bodyAPtr->internalGetDeltaLinearVelocity().setValue(0.f,0.f,0.f);
					bodyAPtr->internalGetDeltaAngularVelocity().setValue(0.f,0.f,0.f);
					bodyAPtr->internalGetPushVelocity().setValue(0.f,0.f,0.f);
					bodyAPtr->internalGetTurnVelocity().setValue(0.f,0.f,0.f);
					bodyBPtr->internalGetDeltaLinearVelocity().setValue(0.f,0.f,0.f);
					bodyBPtr->internalGetDeltaAngularVelocity().setValue(0.f,0.f,0.f);
					bodyBPtr->internalGetPushVelocity().setValue(0.f,0.f,0.f);
					bodyBPtr->internalGetTurnVelocity().setValue(0.f,0.f,0.f);


					btTypedConstraint::btConstraintInfo2 info2;
					info2.fps = 1.f/infoGlobal.m_timeStep;
					info2.erp = infoGlobal.m_erp;
					info2.m_J1linearAxis = currentConstraintRow->m_contactNormal1;
					info2.m_J1angularAxis = currentConstraintRow->m_relpos1CrossNormal;
					info2.m_J2linearAxis = currentConstraintRow->m_contactNormal2;
					info2.m_J2angularAxis = currentConstraintRow->m_relpos2CrossNormal;
					info2.rowskip = sizeof(btSolverConstraint)/sizeof(btScalar);//check this
					///the size of btSolverConstraint needs be a multiple of btScalar
		            btAssert(info2.rowskip*sizeof(btScalar)== sizeof(btSolverConstraint));
					info2.m_constraintError = &currentConstraintRow->m_rhs;
					currentConstraintRow->m_cfm = infoGlobal.m_globalCfm;
					info2.m_damping = infoGlobal.m_damping;
					info2.cfm = &currentConstraintRow->m_cfm;
					info2.m_lowerLimit = &currentConstraintRow->m_lowerLimit;
					info2.m_upperLimit = &currentConstraintRow->m_upperLimit;
					info2.m_numIterations = infoGlobal.m_numIterations;
					constraints[i]->getInfo2(&info2);

					///finalize the constraint setup
					for ( j=0;j<info1.m_numConstraintRows;j++)
					{
						btSolverConstraint& solverConstraint = currentConstraintRow[j];

						if (solverConstraint.m_upperLimit>=constraints[i]->getBreakingImpulseThreshold())
						{
							solverConstraint.m_upperLimit = constraints[i]->getBreakingImpulseThreshold();
						}

						if (solverConstraint.m_lowerLimit<=-constraints[i]->getBreakingImpulseThreshold())
						{
							solverConstraint.m_lowerLimit = -constraints[i]->getBreakingImpulseThreshold();
						}

						solverConstraint.m_originalContactPoint = constraint;

						{
							const btVector3& ftorqueAxis1 = solverConstraint.m_relpos1CrossNormal;
							solverConstraint.m_angularComponentA = constraint->getRigidBodyA().getInvInertiaTensorWorld()*ftorqueAxis1*constraint->getRigidBodyA().getAngularFactor();
						}
						{
							const btVector3& ftorqueAxis2 = solverConstraint.m_relpos2CrossNormal;
							solverConstraint.m_angularComponentB = constraint->getRigidBodyB().getInvInertiaTensorWorld()*ftorqueAxis2*constraint->getRigidBodyB().getAngularFactor();
						}

						{
							btVector3 iMJlA = solverConstraint.m_contactNormal1*rbA.getInvMass();
							btVector3 iMJaA = rbA.getInvInertiaTensorWorld()*solverConstraint.m_relpos1CrossNormal;
							btVector3 iMJlB = solverConstraint.m_contactNormal2*rbB.getInvMass();//sign of normal?
							btVector3 iMJaB = rbB.getInvInertiaTensorWorld()*solverConstraint.m_relpos2CrossNormal;

							btScalar sum = iMJlA.dot(solverConstraint.m_contactNormal1);
							sum += iMJaA.dot(solverConstraint.m_relpos1CrossNormal);
							sum += iMJlB.dot(solverConstraint.m_contactNormal2);
							sum += iMJaB.dot(solverConstraint.m_relpos2CrossNormal);
							btScalar fsum = btFabs(sum);
							btAssert(fsum > SIMD_EPSILON);
							solverConstraint.m_jacDiagABInv = fsum>SIMD_EPSILON?btScalar(1.)/sum : 0.f;
						}


						
						{
							btScalar rel_vel;
							btVector3 externalForceImpulseA = bodyAPtr->m_originalBody ? bodyAPtr->m_externalForceImpulse : btVector3(0,0,0);
							btVector3 externalTorqueImpulseA = bodyAPtr->m_originalBody ? bodyAPtr->m_externalTorqueImpulse : btVector3(0,0,0);

							btVector3 externalForceImpulseB = bodyBPtr->m_originalBody ? bodyBPtr->m_externalForceImpulse : btVector3(0,0,0);
							btVector3 externalTorqueImpulseB = bodyBPtr->m_originalBody ?bodyBPtr->m_externalTorqueImpulse : btVector3(0,0,0);
							
							btScalar vel1Dotn = solverConstraint.m_contactNormal1.dot(rbA.getLinearVelocity()+externalForceImpulseA) 
												+ solverConstraint.m_relpos1CrossNormal.dot(rbA.getAngularVelocity()+externalTorqueImpulseA);
							
							btScalar vel2Dotn = solverConstraint.m_contactNormal2.dot(rbB.getLinearVelocity()+externalForceImpulseB) 
																+ solverConstraint.m_relpos2CrossNormal.dot(rbB.getAngularVelocity()+externalTorqueImpulseB);

							rel_vel = vel1Dotn+vel2Dotn;
							btScalar restitution = 0.f;
							btScalar positionalError = solverConstraint.m_rhs;//already filled in by getConstraintInfo2
							btScalar	velocityError = restitution - rel_vel * info2.m_damping;
							btScalar	penetrationImpulse = positionalError*solverConstraint.m_jacDiagABInv;
							btScalar	velocityImpulse = velocityError *solverConstraint.m_jacDiagABInv;
							solverConstraint.m_rhs = penetrationImpulse+velocityImpulse;
							solverConstraint.m_appliedImpulse = 0.f;


						}
					}
				}
				currentRow+=m_tmpConstraintSizesPool[i].m_numConstraintRows;
			}
		}

		convertContacts(manifoldPtr,numManifolds,infoGlobal);

	}

//	btContactSolverInfo info = infoGlobal;


	int numNonContactPool = m_tmpSolverNonContactConstraintPool.size();
	int numConstraintPool = m_tmpSolverContactConstraintPool.size();
	int numFrictionPool = m_tmpSolverContactFrictionConstraintPool.size();

	///@todo: use stack allocator for such temporarily memory, same for solver bodies/constraints
	m_orderNonContactConstraintPool.resizeNoInitialize(numNonContactPool);
	if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
		m_orderTmpConstraintPool.resizeNoInitialize(numConstraintPool*2);
	else
		m_orderTmpConstraintPool.resizeNoInitialize(numConstraintPool);

	m_orderFrictionConstraintPool.resizeNoInitialize(numFrictionPool);
	{
		int i;
		for (i=0;i<numNonContactPool;i++)
		{
			m_orderNonContactConstraintPool[i] = i;
		}
		for (i=0;i<numConstraintPool;i++)
		{
			m_orderTmpConstraintPool[i] = i;
		}
		for (i=0;i<numFrictionPool;i++)
		{
			m_orderFrictionConstraintPool[i] = i;
		}
	}

	return 0.f;

}


btScalar btSequentialImpulseConstraintSolver::solveSingleIteration(int iteration, btCollisionObject** /*bodies */,int /*numBodies*/,btPersistentManifold** /*manifoldPtr*/, int /*numManifolds*/,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* /*debugDrawer*/)
{

	int numNonContactPool = m_tmpSolverNonContactConstraintPool.size();
	int numConstraintPool = m_tmpSolverContactConstraintPool.size();
	int numFrictionPool = m_tmpSolverContactFrictionConstraintPool.size();
	
	if (infoGlobal.m_solverMode & SOLVER_RANDMIZE_ORDER)
	{
		if (1)			// uncomment this for a bit less random ((iteration & 7) == 0)
		{

			for (int j=0; j<numNonContactPool; ++j) {
				int tmp = m_orderNonContactConstraintPool[j];
				int swapi = btRandInt2(j+1);
				m_orderNonContactConstraintPool[j] = m_orderNonContactConstraintPool[swapi];
				m_orderNonContactConstraintPool[swapi] = tmp;
			}

			//contact/friction constraints are not solved more than 
			if (iteration< infoGlobal.m_numIterations)
			{
				for (int j=0; j<numConstraintPool; ++j) {
					int tmp = m_orderTmpConstraintPool[j];
					int swapi = btRandInt2(j+1);
					m_orderTmpConstraintPool[j] = m_orderTmpConstraintPool[swapi];
					m_orderTmpConstraintPool[swapi] = tmp;
				}

				for (int j=0; j<numFrictionPool; ++j) {
					int tmp = m_orderFrictionConstraintPool[j];
					int swapi = btRandInt2(j+1);
					m_orderFrictionConstraintPool[j] = m_orderFrictionConstraintPool[swapi];
					m_orderFrictionConstraintPool[swapi] = tmp;
				}
			}
		}
	}

	if (infoGlobal.m_solverMode & SOLVER_SIMD)
	{
		///solve all joint constraints, using SIMD, if available
		for (int j=0;j<m_tmpSolverNonContactConstraintPool.size();j++)
		{
			btSolverConstraint& constraint = m_tmpSolverNonContactConstraintPool[m_orderNonContactConstraintPool[j]];
			if (iteration < constraint.m_overrideNumSolverIterations)
				resolveSingleConstraintRowGenericSIMD(m_tmpSolverBodyPool[constraint.m_solverBodyIdA],m_tmpSolverBodyPool[constraint.m_solverBodyIdB],constraint);
		}

		if (iteration< infoGlobal.m_numIterations)
		{
			for (int j=0;j<numConstraints;j++)
			{
				if (constraints[j]->isEnabled())
				{
					int bodyAid = getOrInitSolverBody(constraints[j]->getRigidBodyA(),infoGlobal.m_timeStep);
					int bodyBid = getOrInitSolverBody(constraints[j]->getRigidBodyB(),infoGlobal.m_timeStep);
					btSolverBody& bodyA = m_tmpSolverBodyPool[bodyAid];
					btSolverBody& bodyB = m_tmpSolverBodyPool[bodyBid];
					constraints[j]->solveConstraintObsolete(bodyA,bodyB,infoGlobal.m_timeStep);
				}
			}

			///solve all contact constraints using SIMD, if available
			if (infoGlobal.m_solverMode & SOLVER_INTERLEAVE_CONTACT_AND_FRICTION_CONSTRAINTS)
			{
				int numPoolConstraints = m_tmpSolverContactConstraintPool.size();
				int multiplier = (infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS)? 2 : 1;

				for (int c=0;c<numPoolConstraints;c++)
				{
					btScalar totalImpulse =0;

					{
						const btSolverConstraint& solveManifold = m_tmpSolverContactConstraintPool[m_orderTmpConstraintPool[c]];
						resolveSingleConstraintRowLowerLimitSIMD(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
						totalImpulse = solveManifold.m_appliedImpulse;
					}
					bool applyFriction = true;
					if (applyFriction)
					{
						{

							btSolverConstraint& solveManifold = m_tmpSolverContactFrictionConstraintPool[m_orderFrictionConstraintPool[c*multiplier]];

							if (totalImpulse>btScalar(0))
							{
								solveManifold.m_lowerLimit = -(solveManifold.m_friction*totalImpulse);
								solveManifold.m_upperLimit = solveManifold.m_friction*totalImpulse;

								resolveSingleConstraintRowGenericSIMD(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
							}
						}

						if (infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS)
						{

							btSolverConstraint& solveManifold = m_tmpSolverContactFrictionConstraintPool[m_orderFrictionConstraintPool[c*multiplier+1]];
				
							if (totalImpulse>btScalar(0))
							{
								solveManifold.m_lowerLimit = -(solveManifold.m_friction*totalImpulse);
								solveManifold.m_upperLimit = solveManifold.m_friction*totalImpulse;

								resolveSingleConstraintRowGenericSIMD(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
							}
						}
					}
				}

			}
			else//SOLVER_INTERLEAVE_CONTACT_AND_FRICTION_CONSTRAINTS
			{
				//solve the friction constraints after all contact constraints, don't interleave them
				int numPoolConstraints = m_tmpSolverContactConstraintPool.size();
				int j;

				for (j=0;j<numPoolConstraints;j++)
				{
					const btSolverConstraint& solveManifold = m_tmpSolverContactConstraintPool[m_orderTmpConstraintPool[j]];
					//resolveSingleConstraintRowLowerLimitSIMD(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
					resolveSingleConstraintRowLowerLimit(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);

				}
		
				

				///solve all friction constraints, using SIMD, if available

				int numFrictionPoolConstraints = m_tmpSolverContactFrictionConstraintPool.size();
				for (j=0;j<numFrictionPoolConstraints;j++)
				{
					btSolverConstraint& solveManifold = m_tmpSolverContactFrictionConstraintPool[m_orderFrictionConstraintPool[j]];
					btScalar totalImpulse = m_tmpSolverContactConstraintPool[solveManifold.m_frictionIndex].m_appliedImpulse;

					if (totalImpulse>btScalar(0))
					{
						solveManifold.m_lowerLimit = -(solveManifold.m_friction*totalImpulse);
						solveManifold.m_upperLimit = solveManifold.m_friction*totalImpulse;

						//resolveSingleConstraintRowGenericSIMD(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
						resolveSingleConstraintRowGeneric(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
					}
				}

				
				int numRollingFrictionPoolConstraints = m_tmpSolverContactRollingFrictionConstraintPool.size();
				for (j=0;j<numRollingFrictionPoolConstraints;j++)
				{

					btSolverConstraint& rollingFrictionConstraint = m_tmpSolverContactRollingFrictionConstraintPool[j];
					btScalar totalImpulse = m_tmpSolverContactConstraintPool[rollingFrictionConstraint.m_frictionIndex].m_appliedImpulse;
					if (totalImpulse>btScalar(0))
					{
						btScalar rollingFrictionMagnitude = rollingFrictionConstraint.m_friction*totalImpulse;
						if (rollingFrictionMagnitude>rollingFrictionConstraint.m_friction)
							rollingFrictionMagnitude = rollingFrictionConstraint.m_friction;

						rollingFrictionConstraint.m_lowerLimit = -rollingFrictionMagnitude;
						rollingFrictionConstraint.m_upperLimit = rollingFrictionMagnitude;

						resolveSingleConstraintRowGenericSIMD(m_tmpSolverBodyPool[rollingFrictionConstraint.m_solverBodyIdA],m_tmpSolverBodyPool[rollingFrictionConstraint.m_solverBodyIdB],rollingFrictionConstraint);
					}
				}
				

			}			
		}
	} else
	{
		//non-SIMD version
		///solve all joint constraints
		for (int j=0;j<m_tmpSolverNonContactConstraintPool.size();j++)
		{
			btSolverConstraint& constraint = m_tmpSolverNonContactConstraintPool[m_orderNonContactConstraintPool[j]];
			if (iteration < constraint.m_overrideNumSolverIterations)
				resolveSingleConstraintRowGeneric(m_tmpSolverBodyPool[constraint.m_solverBodyIdA],m_tmpSolverBodyPool[constraint.m_solverBodyIdB],constraint);
		}

		if (iteration< infoGlobal.m_numIterations)
		{
			for (int j=0;j<numConstraints;j++)
			{
				if (constraints[j]->isEnabled())
				{
					int bodyAid = getOrInitSolverBody(constraints[j]->getRigidBodyA(),infoGlobal.m_timeStep);
					int bodyBid = getOrInitSolverBody(constraints[j]->getRigidBodyB(),infoGlobal.m_timeStep);
					btSolverBody& bodyA = m_tmpSolverBodyPool[bodyAid];
					btSolverBody& bodyB = m_tmpSolverBodyPool[bodyBid];
					constraints[j]->solveConstraintObsolete(bodyA,bodyB,infoGlobal.m_timeStep);
				}
			}
			///solve all contact constraints
			int numPoolConstraints = m_tmpSolverContactConstraintPool.size();
			for (int j=0;j<numPoolConstraints;j++)
			{
				const btSolverConstraint& solveManifold = m_tmpSolverContactConstraintPool[m_orderTmpConstraintPool[j]];
				resolveSingleConstraintRowLowerLimit(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
			}
			///solve all friction constraints
			int numFrictionPoolConstraints = m_tmpSolverContactFrictionConstraintPool.size();
			for (int j=0;j<numFrictionPoolConstraints;j++)
			{
				btSolverConstraint& solveManifold = m_tmpSolverContactFrictionConstraintPool[m_orderFrictionConstraintPool[j]];
				btScalar totalImpulse = m_tmpSolverContactConstraintPool[solveManifold.m_frictionIndex].m_appliedImpulse;

				if (totalImpulse>btScalar(0))
				{
					solveManifold.m_lowerLimit = -(solveManifold.m_friction*totalImpulse);
					solveManifold.m_upperLimit = solveManifold.m_friction*totalImpulse;

					resolveSingleConstraintRowGeneric(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
				}
			}

			int numRollingFrictionPoolConstraints = m_tmpSolverContactRollingFrictionConstraintPool.size();
			for (int j=0;j<numRollingFrictionPoolConstraints;j++)
			{
				btSolverConstraint& rollingFrictionConstraint = m_tmpSolverContactRollingFrictionConstraintPool[j];
				btScalar totalImpulse = m_tmpSolverContactConstraintPool[rollingFrictionConstraint.m_frictionIndex].m_appliedImpulse;
				if (totalImpulse>btScalar(0))
				{
					btScalar rollingFrictionMagnitude = rollingFrictionConstraint.m_friction*totalImpulse;
					if (rollingFrictionMagnitude>rollingFrictionConstraint.m_friction)
						rollingFrictionMagnitude = rollingFrictionConstraint.m_friction;

					rollingFrictionConstraint.m_lowerLimit = -rollingFrictionMagnitude;
					rollingFrictionConstraint.m_upperLimit = rollingFrictionMagnitude;

					resolveSingleConstraintRowGeneric(m_tmpSolverBodyPool[rollingFrictionConstraint.m_solverBodyIdA],m_tmpSolverBodyPool[rollingFrictionConstraint.m_solverBodyIdB],rollingFrictionConstraint);
				}
			}
		}
	}
	return 0.f;
}


void btSequentialImpulseConstraintSolver::solveGroupCacheFriendlySplitImpulseIterations(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer)
{
	int iteration;
	if (infoGlobal.m_splitImpulse)
	{
		if (infoGlobal.m_solverMode & SOLVER_SIMD)
		{
			for ( iteration = 0;iteration<infoGlobal.m_numIterations;iteration++)
			{
				{
					int numPoolConstraints = m_tmpSolverContactConstraintPool.size();
					int j;
					for (j=0;j<numPoolConstraints;j++)
					{
						const btSolverConstraint& solveManifold = m_tmpSolverContactConstraintPool[m_orderTmpConstraintPool[j]];

						resolveSplitPenetrationSIMD(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
					}
				}
			}
		}
		else
		{
			for ( iteration = 0;iteration<infoGlobal.m_numIterations;iteration++)
			{
				{
					int numPoolConstraints = m_tmpSolverContactConstraintPool.size();
					int j;
					for (j=0;j<numPoolConstraints;j++)
					{
						const btSolverConstraint& solveManifold = m_tmpSolverContactConstraintPool[m_orderTmpConstraintPool[j]];

						resolveSplitPenetrationImpulseCacheFriendly(m_tmpSolverBodyPool[solveManifold.m_solverBodyIdA],m_tmpSolverBodyPool[solveManifold.m_solverBodyIdB],solveManifold);
					}
				}
			}
		}
	}
}

btScalar btSequentialImpulseConstraintSolver::solveGroupCacheFriendlyIterations(btCollisionObject** bodies ,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer)
{
	BT_PROFILE("solveGroupCacheFriendlyIterations");

	{
		///this is a special step to resolve penetrations (just for contacts)
		solveGroupCacheFriendlySplitImpulseIterations(bodies ,numBodies,manifoldPtr, numManifolds,constraints,numConstraints,infoGlobal,debugDrawer);

		int maxIterations = m_maxOverrideNumSolverIterations > infoGlobal.m_numIterations? m_maxOverrideNumSolverIterations : infoGlobal.m_numIterations;

		for ( int iteration = 0 ; iteration< maxIterations ; iteration++)
		//for ( int iteration = maxIterations-1  ; iteration >= 0;iteration--)
		{			
			solveSingleIteration(iteration, bodies ,numBodies,manifoldPtr, numManifolds,constraints,numConstraints,infoGlobal,debugDrawer);
		}
		
	}
	return 0.f;
}

btScalar btSequentialImpulseConstraintSolver::solveGroupCacheFriendlyFinish(btCollisionObject** bodies,int numBodies,const btContactSolverInfo& infoGlobal)
{
	int numPoolConstraints = m_tmpSolverContactConstraintPool.size();
	int i,j;

	if (infoGlobal.m_solverMode & SOLVER_USE_WARMSTARTING)
	{
		for (j=0;j<numPoolConstraints;j++)
		{
			const btSolverConstraint& solveManifold = m_tmpSolverContactConstraintPool[j];
			btManifoldPoint* pt = (btManifoldPoint*) solveManifold.m_originalContactPoint;
			btAssert(pt);
			pt->m_appliedImpulse = solveManifold.m_appliedImpulse;
		//	float f = m_tmpSolverContactFrictionConstraintPool[solveManifold.m_frictionIndex].m_appliedImpulse;
			//	printf("pt->m_appliedImpulseLateral1 = %f\n", f);
			pt->m_appliedImpulseLateral1 = m_tmpSolverContactFrictionConstraintPool[solveManifold.m_frictionIndex].m_appliedImpulse;
			//printf("pt->m_appliedImpulseLateral1 = %f\n", pt->m_appliedImpulseLateral1);
			if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
			{
				pt->m_appliedImpulseLateral2 = m_tmpSolverContactFrictionConstraintPool[solveManifold.m_frictionIndex+1].m_appliedImpulse;
			}
			//do a callback here?
		}
	}

	numPoolConstraints = m_tmpSolverNonContactConstraintPool.size();
	for (j=0;j<numPoolConstraints;j++)
	{
		const btSolverConstraint& solverConstr = m_tmpSolverNonContactConstraintPool[j];
		btTypedConstraint* constr = (btTypedConstraint*)solverConstr.m_originalContactPoint;
		btJointFeedback* fb = constr->getJointFeedback();
		if (fb)
		{
			fb->m_appliedForceBodyA += solverConstr.m_contactNormal1*solverConstr.m_appliedImpulse*constr->getRigidBodyA().getLinearFactor()/infoGlobal.m_timeStep;
			fb->m_appliedForceBodyB += solverConstr.m_contactNormal2*solverConstr.m_appliedImpulse*constr->getRigidBodyB().getLinearFactor()/infoGlobal.m_timeStep;
			fb->m_appliedTorqueBodyA += solverConstr.m_relpos1CrossNormal* constr->getRigidBodyA().getAngularFactor()*solverConstr.m_appliedImpulse/infoGlobal.m_timeStep;
			fb->m_appliedTorqueBodyB += solverConstr.m_relpos2CrossNormal* constr->getRigidBodyB().getAngularFactor()*solverConstr.m_appliedImpulse/infoGlobal.m_timeStep; /*RGM ???? */
			
		}

		constr->internalSetAppliedImpulse(solverConstr.m_appliedImpulse);
		if (btFabs(solverConstr.m_appliedImpulse)>=constr->getBreakingImpulseThreshold())
		{
			constr->setEnabled(false);
		}
	}



	for ( i=0;i<m_tmpSolverBodyPool.size();i++)
	{
		btRigidBody* body = m_tmpSolverBodyPool[i].m_originalBody;
		if (body)
		{
			if (infoGlobal.m_splitImpulse)
				m_tmpSolverBodyPool[i].writebackVelocityAndTransform(infoGlobal.m_timeStep, infoGlobal.m_splitImpulseTurnErp);
			else
				m_tmpSolverBodyPool[i].writebackVelocity();
			
			m_tmpSolverBodyPool[i].m_originalBody->setLinearVelocity(
				m_tmpSolverBodyPool[i].m_linearVelocity+
				m_tmpSolverBodyPool[i].m_externalForceImpulse);

			m_tmpSolverBodyPool[i].m_originalBody->setAngularVelocity(
				m_tmpSolverBodyPool[i].m_angularVelocity+
				m_tmpSolverBodyPool[i].m_externalTorqueImpulse);

			if (infoGlobal.m_splitImpulse)
				m_tmpSolverBodyPool[i].m_originalBody->setWorldTransform(m_tmpSolverBodyPool[i].m_worldTransform);

			m_tmpSolverBodyPool[i].m_originalBody->setCompanionId(-1);
		}
	}

	m_tmpSolverContactConstraintPool.resizeNoInitialize(0);
	m_tmpSolverNonContactConstraintPool.resizeNoInitialize(0);
	m_tmpSolverContactFrictionConstraintPool.resizeNoInitialize(0);
	m_tmpSolverContactRollingFrictionConstraintPool.resizeNoInitialize(0);

	m_tmpSolverBodyPool.resizeNoInitialize(0);
	return 0.f;
}



/// btSequentialImpulseConstraintSolver Sequentially applies impulses
btScalar btSequentialImpulseConstraintSolver::solveGroup(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer,btDispatcher* /*dispatcher*/)
{

	BT_PROFILE("solveGroup");
	//you need to provide at least some bodies
	
	solveGroupCacheFriendlySetup( bodies, numBodies, manifoldPtr,  numManifolds,constraints, numConstraints,infoGlobal,debugDrawer);

	solveGroupCacheFriendlyIterations(bodies, numBodies, manifoldPtr,  numManifolds,constraints, numConstraints,infoGlobal,debugDrawer);

	solveGroupCacheFriendlyFinish(bodies, numBodies, infoGlobal);
	
	return 0.f;
}

void	btSequentialImpulseConstraintSolver::reset()
{
	m_btSeed2 = 0;
}


