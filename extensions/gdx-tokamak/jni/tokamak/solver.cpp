/*************************************************************************
 *                                                                       *
 * Tokamak Physics Engine, Copyright (C) 2002-2007 David Lam.            *
 * All rights reserved.  Email: david@tokamakphysics.com                 *
 *                       Web: www.tokamakphysics.com                     *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * LICENSE.TXT for more details.                                         *
 *                                                                       *
 *************************************************************************/

#include "math/ne_type.h"
#include "math/ne_debug.h"
#include "tokamak.h"
#include "containers.h"
#include "scenery.h"
#include "collision.h"
#include "constraint.h"
#include "rigidbody.h"
#include "stack.h"
#include "simulator.h"
#include "scenery.h"
#include "message.h"

f32 CONSTRAINT_THESHOLD_JOINT = 0.000f;

f32 CONSTRAINT_THESHOLD_CONTACT = 0.001f;

f32 CONSTRAINT_THESHOLD_LIMIT = 0.001f;

f32 CONSTRAINT_CONVERGE_FACTOR_JOINT = .5f;

f32 CONSTRAINT_CONVERGE_FACTOR_CONTACT = .5f;

f32 CONSTRAINT_CONVERGE_FACTOR_LIMIT = 0.5f;

NEINLINE void ApplyCollisionImpulseFast(neRigidBody_ * rb, const neV3 & impulse, const neV3 & contactPoint, s32 currentRecord, neBool immediate = true)
{
	neV3 dv, da;

	dv = impulse * rb->oneOnMass;

	da = contactPoint.Cross(impulse);

//	if (immediate)
		rb->Derive().linearVel += dv;
//	else
//	{
//		rb->totalDV += dv;
//		rb->impulseCount++;
//	}

//	rb->dvRecord[currentRecord] += dv;

//	neV3 dav = rb->Derive().Iinv * da;

//	rb->davRecord[currentRecord] += dav;

//	if (immediate)
	{
		rb->State().angularMom += da;

		rb->Derive().angularVel = rb->Derive().Iinv * rb->State().angularMom;
	}
//	else
//	{
//		rb->totalDA += da;
//		
//		rb->twistCount++;
//	}
}


void neFixedTimeStepSimulator::AddCollisionResult(neCollisionResult & cresult)
{
	neCollisionResult * newcr = cresultHeap2.Alloc(0);

	*newcr = cresult;
}

f32 neCollisionResult::SolveConstraint(neFixedTimeStepSimulator * sim)
{
	neV3 impulse;

	f32 ret = 0.0f;

	neV3 pII; 

	if (neIsConsiderZero(depth))
	{
		pII = kInv * initRelVel;

		impulse = -pII;
	}
	else
	{
		neV3 & desireVel = contactAWorld;//w2c * contactAWorld;

		neV3 tmp = desireVel * CONSTRAINT_CONVERGE_FACTOR_JOINT;
		
		f32 len = depth * CONSTRAINT_CONVERGE_FACTOR_JOINT;//tmp.Length();

		if (len > 0.05f)
		{
			tmp = desireVel * (0.05f / len);
		}
		tmp *= sim->oneOnCurrentTimeStep;// * CONSTRAINT_CONVERGE_FACTOR_JOINT;

		neV3 deltaU = tmp + initRelVel *  CONSTRAINT_CONVERGE_FACTOR_JOINT;

		impulse = kInv * deltaU * -1.0f;
	}
	neRigidBody_ * rb;

	if (bodyA && (rb = bodyA->AsRigidBody()))
	{
		ApplyCollisionImpulseFast(rb, impulse, contactA, sim->currentRecord);

		rb->needRecalc = true;
	}
	if (bodyB && (rb = bodyB->AsRigidBody()))
	{
		neV3 bimpulse = impulse * -1.0f;
		
		ApplyCollisionImpulseFast(rb, bimpulse, contactB, sim->currentRecord);

		rb->needRecalc = true;
	}
	return ret;
}

f32 neCollisionResult::SolveSlider(neFixedTimeStepSimulator * sim)
{
	neV3 impulse;

	if (neIsConsiderZero(finalRelativeSpeed))
	{
		impulse = kInv * initRelVel * -1.0f;

		impulse = impulse - impulse.Dot(contactBWorld) * contactBWorld;
	}
	else
	{
		neV3 & desireVel = contactAWorld;

		neV3 tmp = desireVel * CONSTRAINT_CONVERGE_FACTOR_JOINT;
		
		f32 len = finalRelativeSpeed * CONSTRAINT_CONVERGE_FACTOR_JOINT;//tmp.Length();

		if (len > 0.05f)
		{
			tmp = desireVel * (0.05f / len);
		}
		tmp *= sim->oneOnCurrentTimeStep;// * CONSTRAINT_CONVERGE_FACTOR_JOINT;

		neV3 deltaU = tmp + initRelVel *  CONSTRAINT_CONVERGE_FACTOR_JOINT;

		impulse = kInv * deltaU * -1.0f;

		f32 dot = impulse.Dot(contactBWorld);

		impulse = impulse - dot * contactBWorld;
	}
	neRigidBody_ * rb;

	if (bodyA && (rb = bodyA->AsRigidBody()))
	{
		ApplyCollisionImpulseFast(rb, impulse, contactA, sim->currentRecord);

		rb->needRecalc = true;
	}
	if (bodyB && (rb = bodyB->AsRigidBody()))
	{
		neV3 bimpulse = impulse * -1.0f;
		
		ApplyCollisionImpulseFast(rb, bimpulse, contactB, sim->currentRecord);

		rb->needRecalc = true;
	}
	return 0.0f;
}

f32 neCollisionResult::SolveSliderLimit(neFixedTimeStepSimulator * sim)
{
	ASSERT(depth >= 0.0f);

	neV3 impulse;

	f32 desireSpeed = depth * sim->oneOnCurrentTimeStep * CONSTRAINT_CONVERGE_FACTOR_LIMIT;

	if (desireSpeed > finalRelativeSpeed)
	{
		f32 dU = desireSpeed - finalRelativeSpeed * CONSTRAINT_CONVERGE_FACTOR_LIMIT;

		neV3 deltaU; deltaU = contactBWorld * dU;

		impulse = kInv * deltaU *-1.0f;
	}
	else
	{
		return 0.0f;
	}

	neRigidBody_ * rb;

	if (bodyA && (rb = bodyA->AsRigidBody()))
	{
		ApplyCollisionImpulseFast(rb, impulse, contactA, sim->currentRecord);

		rb->needRecalc = true;
	}
	if (bodyB && (rb = bodyB->AsRigidBody()) && !(sim->solverLastIteration))
	{
		neV3 bimpulse = impulse * -1.0f;
		
		ApplyCollisionImpulseFast(rb, bimpulse, contactB, sim->currentRecord);

		rb->needRecalc = true;
	}
	return 0.0f;
}

f32 neCollisionResult::SolveContact(neFixedTimeStepSimulator * sim)
{
	neV3 impulse1; impulse1.SetZero();
	neV3 impulse2; impulse2.SetZero();

	f32 ret = 0.0f;

	if (initRelVel[2] < 0.0f)
	{	
		if (sim->solverStage == 0)
			impulse1 = sim->CalcNormalImpulse(*this, FALSE);
		else
			impulse1 = sim->CalcNormalImpulse(*this, TRUE);

		initRelVel[2] = 0.0f;
	}
	if (depth > 0.05f)
		depth = 0.05f;

	f32 adjustedDepth = depth - CONSTRAINT_THESHOLD_CONTACT * sim->gravityMag;

	f32 desireNormalSpeed;

	if (depth <= 0.0f) // -ve mean not penetrating
	{

	}
	else if (adjustedDepth <= 0.0f)
	{
	}
	else if (sim->solverStage != 0)
	{
		desireNormalSpeed = adjustedDepth * sim->oneOnCurrentTimeStep * CONSTRAINT_CONVERGE_FACTOR_CONTACT;

		if (desireNormalSpeed > initRelVel[2])
		{
			neV3 deltaU;

			deltaU.Set(0.0f, 0.0f, desireNormalSpeed - initRelVel[2] * CONSTRAINT_CONVERGE_FACTOR_CONTACT);

			//deltaU.Set(- initRelVel[0], - initRelVel[1], desireNormalSpeed - initRelVel[2]);

			ret = neAbs(deltaU[2]);
	
			impulse2 = kInv * deltaU;
		}
	}
	neV3 impulse = impulse1 + impulse2;

	impulse = collisionFrame * impulse;

	neRigidBody_ * rb;

	if (bodyA && (rb = bodyA->AsRigidBody()))
	{
		ApplyCollisionImpulseFast(rb, impulse, contactA, sim->currentRecord, true);

		rb->needRecalc = true;
	}
	if (bodyB && (rb = bodyB->AsRigidBody()) && !(sim->solverLastIteration))
	{
		neV3 bimpulse = impulse * -1.0f;
		
		ApplyCollisionImpulseFast(rb, bimpulse, contactB, sim->currentRecord, true);

		rb->needRecalc = true;
	}
	return ret;
}

f32 neCollisionResult::SolveAngular(f32 pdepth, const neV3 & axis, f32 relAV, neFixedTimeStepSimulator * sim)
{
	neV3 deltaL;

	f32 threshold = 0.00f;

	f32 angularDisplacementNeeded;
	
	if (pdepth > threshold)
	{
		angularDisplacementNeeded = (pdepth - threshold);
	}
	else if (pdepth < -threshold)
	{
		angularDisplacementNeeded = (pdepth + threshold);
	}
	else
	{
		ASSERT(0);
		return 0;
	}
	f32 deltaAng;

	f32 scaledCorrection = angularDisplacementNeeded * CONSTRAINT_CONVERGE_FACTOR_LIMIT;

	neBool applyImpulse = false;
	
	f32 angularDisplacment;

	if (scaledCorrection > 0.0f)
	{
		if (relAV > 0.0f)
		{
			//spining into the limit
			
			deltaAng = -scaledCorrection * sim->oneOnCurrentTimeStep;

			deltaAng -= (relAV * CONSTRAINT_CONVERGE_FACTOR_LIMIT);

			applyImpulse = true;
		}
		else // relAV < 0
		{
			//spining out of the limit, but is it fast enough?
			return 0.0f;
			
			angularDisplacment = relAV * sim->_currentTimeStep;

			f32 d = angularDisplacementNeeded + angularDisplacment;

			if (d > 0.0f)
			{
				deltaAng = -d * CONSTRAINT_CONVERGE_FACTOR_LIMIT * sim->oneOnCurrentTimeStep;

				applyImpulse = true;
			}
		}
	}
	else
	{
		if (relAV < 0.0f)
		{
			//spining into the limit
			deltaAng = -scaledCorrection  * sim->oneOnCurrentTimeStep;

			deltaAng -= (relAV * CONSTRAINT_CONVERGE_FACTOR_LIMIT);

			applyImpulse = true;
		}
		else // relAV > 0
		{
			//spining out of the limit, but is it fast enough?
			return 0.0f;
			angularDisplacment = relAV * sim->_currentTimeStep;

			f32 d = angularDisplacementNeeded + angularDisplacment;

			if (d < 0.0f)
			{
				deltaAng = -d * CONSTRAINT_CONVERGE_FACTOR_LIMIT * sim->oneOnCurrentTimeStep;

				applyImpulse = true;
			}
		}
	}

	if (!applyImpulse)
		return 0;

	neV3 deltaAngVel; deltaAngVel = axis * deltaAng;

	deltaL = kInv * deltaAngVel;

	neRigidBody_ * rb;

	if (bodyA && (rb = bodyA->AsRigidBody()))
	{
		rb->SetAngMom(rb->State().angularMom + deltaL);

		rb->davRecord[rb->sim->stepSoFar % NE_RB_MAX_PAST_RECORDS] += rb->Derive().Iinv * deltaL;

//		rb->totalDA += deltaL;

//		rb->twistCount++;
	}
	if (bodyB && (rb = bodyB->AsRigidBody()))
	{
		rb->SetAngMom(rb->State().angularMom - deltaL);

		rb->davRecord[rb->sim->stepSoFar % NE_RB_MAX_PAST_RECORDS] -= rb->Derive().Iinv * deltaL;

//		rb->totalDA -= deltaL;

//		rb->twistCount++;
	}
	return 0;
}

f32 neCollisionResult::SolveAngular2(const neV3 & axisA, const neV3 & axisB, f32 relAV, f32 desireAV, f32 maxTorque, neFixedTimeStepSimulator * sim)
{
	f32 deltaAng = desireAV - relAV;

	//deltaAng *= 0.5f;

	neV3 deltaAngVel, deltaLA, deltaLB;

	deltaAngVel = axisA * deltaAng;

	deltaLA = kInv * deltaAngVel;

	neV3 torque = deltaLA * sim->oneOnCurrentTimeStep;

	f32 torqueMag = torque.Length();

	if (torqueMag > maxTorque && !neIsConsiderZero(neAbs(maxTorque)))
	{
		deltaLA = torque * (maxTorque * sim->_currentTimeStep / torqueMag);
	}

	neRigidBody_ * rb;

	if (bodyA && (rb = bodyA->AsRigidBody()))
	{
		rb->SetAngMom(rb->State().angularMom + deltaLA);

		rb->davRecord[rb->sim->stepSoFar % NE_RB_MAX_PAST_RECORDS] += rb->Derive().Iinv * deltaLA;

		//rb->totalDA += deltaLA;

		//rb->twistCount++;
	}
	deltaAngVel = axisB * deltaAng;

	deltaLB = kInv * deltaAngVel;

	torque = deltaLB * sim->oneOnCurrentTimeStep;

	torqueMag = torque.Length();

	if (torqueMag > maxTorque/* && !neIsConsiderZero(maxTorque)*/)
	{
		deltaLB = torque * (maxTorque * sim->_currentTimeStep / torqueMag);
	}
	if (bodyB && (rb = bodyB->AsRigidBody()))
	{
		rb->SetAngMom(rb->State().angularMom - deltaLB);

		rb->davRecord[rb->sim->stepSoFar % NE_RB_MAX_PAST_RECORDS] -= rb->Derive().Iinv * deltaLB;

		//rb->totalDA -= deltaLB;

		//rb->twistCount++;
	}
	return 0.0f;
}

f32 neCollisionResult::SolveAngular3(f32 pdepth, const neV3 & axis, f32 relAV, neFixedTimeStepSimulator * sim)
{
	neV3 deltaL;

	f32 threshold = 0.00f;

	//f32 angularDisplacementNeeded = -pdepth;
/*	
	if (pdepth > threshold)
	{
		angularDisplacementNeeded = (pdepth - threshold);
	}
	else if (pdepth < -threshold)
	{
		angularDisplacementNeeded = (pdepth + threshold);
	}
	else
	{
		ASSERT(0);
		return 0;
	}
*/	f32 deltaAng;

	f32 scaledDepth = pdepth * CONSTRAINT_CONVERGE_FACTOR_LIMIT;

	if (scaledDepth > 0.1f)
		scaledDepth = 0.1f;
	else if (scaledDepth < -0.1f)
		scaledDepth = -0.1f;

	neBool applyImpulse = false;
	
//	f32 angularDisplacment;

	if (scaledDepth > 0.0f)
	{
		if (relAV > 0.0f)
		{
			//spining into the limit
			
			deltaAng = -scaledDepth * sim->oneOnCurrentTimeStep;

			deltaAng -= (relAV * CONSTRAINT_CONVERGE_FACTOR_LIMIT);

			applyImpulse = true;
		}
		else //relAV < 0.0f
		{
			//spining out of the limit, but is it fast enough?

/*			deltaAng = -scaledDepth * sim->oneOnCurrentTimeStep;

			angularDisplacment = relAV * sim->_currentTimeStep;

			f32 d = pdepth + angularDisplacment;
			
			if (d > 0.0f)
			{
				deltaAng = -d * CONSTRAINT_CONVERGE_FACTOR_LIMIT * sim->oneOnCurrentTimeStep;

				applyImpulse = true;
			}
*/		}
	}
	else
	{
		if (relAV < 0.0f)
		{
			//spining into the limit
			deltaAng = -scaledDepth  * sim->oneOnCurrentTimeStep;

			deltaAng -= (relAV * CONSTRAINT_CONVERGE_FACTOR_LIMIT);

			applyImpulse = true;
		}
		else //relAV > 0.0f
		{
			//spining out of the limit, but is it fast enough?

/*			angularDisplacment = relAV * sim->_currentTimeStep;

			f32 d = pdepth + angularDisplacment;

			if (d < 0.0f)
			{
				deltaAng = -d * CONSTRAINT_CONVERGE_FACTOR_LIMIT * sim->oneOnCurrentTimeStep;

				applyImpulse = true;
			}
*/		}
	}

	if (!applyImpulse)
		return 0;

	neV3 deltaAngVel; deltaAngVel = axis * deltaAng;

	deltaL = kInv * deltaAngVel;

	neRigidBody_ * rb;

	if (bodyA && (rb = bodyA->AsRigidBody()))
	{
		neV3 deltaLA = k.TransposeMulV3(deltaL);

		rb->SetAngMom(rb->State().angularMom + deltaLA);

		rb->davRecord[rb->sim->stepSoFar % NE_RB_MAX_PAST_RECORDS] += rb->Derive().Iinv * deltaLA;

//		rb->totalDA += deltaL;

//		rb->twistCount++;
	}
	if (bodyB && (rb = bodyB->AsRigidBody()))
	{
		rb->SetAngMom(rb->State().angularMom - deltaL);

		rb->davRecord[rb->sim->stepSoFar % NE_RB_MAX_PAST_RECORDS] -= rb->Derive().Iinv * deltaL;

//		rb->totalDA -= deltaL;

//		rb->twistCount++;
	}
	return 0;
}

f32 neCollisionResult::SolveAngularPrimary(neFixedTimeStepSimulator * sim)
{
	neRigidBody_ * ba = NULL;

	neRigidBody_ * bb = NULL;

	f32 ava = 0.0f, avb = 0.0f;

	if (bodyA)
	{
		ba = bodyA->AsRigidBody();
	}
	if (bodyB)
	{
		bb = bodyB->AsRigidBody();
	}
	if (!ba && !bb)
		return 0.0f;

	SolveAngular(depth, contactBBody/*this is the axis*/, relativeSpeed, sim);

	return 0.0f;
}

f32 neCollisionResult::SolveAngularSecondary(neFixedTimeStepSimulator * sim)
{
	neRigidBody_ * ba = NULL;

	neRigidBody_ * bb = NULL;

	f32 ava = 0.0f, avb = 0.0f;

	if (bodyA && (ba = bodyA->AsRigidBody()))
	{
		ava = ba->Derive().angularVel.Dot(contactABody);
	}
	if (bodyB && (bb = bodyB->AsRigidBody()))
	{
		avb = bb->Derive().angularVel.Dot(contactBBody);
	}
	if (!ba && !bb)
		return 0.0f;

	f32 relAV = ava - avb;

	SolveAngular3(depth, contactBBody, relAV, sim);
/*
	SolveAngular(depth, contactBBody, relAV, sim);

	ava = 0.0f;
	
	avb = 0.0f;

	if (bodyA && (ba = bodyA->AsRigidBody()))
	{
		ava = ba->Derive().angularVel.Dot(contactABody);
	}
	if (bodyB && (bb = bodyB->AsRigidBody()))
	{
		avb = bb->Derive().angularVel.Dot(contactBBody);
	}
	relAV = ava - avb;

	SolveAngular(impulseScale, contactABody, relAV, sim);
*/
	return 0.0f;
}

f32 neCollisionResult::SolveAngularMotorPrimary(neFixedTimeStepSimulator * sim)
{
	neRigidBody_ * ba = NULL;

	neRigidBody_ * bb = NULL;

	f32 ava = 0.0f, avb = 0.0f;

	if (bodyA && (ba = bodyA->AsRigidBody()))
	{
		ava = ba->Derive().angularVel.Dot(contactABody);
	}
	if (bodyB && (bb = bodyB->AsRigidBody()))
	{
		avb = bb->Derive().angularVel.Dot(contactBBody);
	}
	if (!ba && !bb)
		return 0.0f;

	f32 relAV = ava - avb;

	SolveAngular2(contactABody, contactBBody, relAV, finalRelativeSpeed, depth, sim);

	return 0.0f;
}

f32 neCollisionResult::SolveRelativeLinear(neFixedTimeStepSimulator * sim)
{
	f32 velA = 0.0f, velB = 0.0f;

	neRigidBody_ * ba, * bb;

	if (bodyA && (ba = bodyA->AsRigidBody()))
	{
		velA = ba->Derive().linearVel.Dot(contactABody);
	}
	if (bodyB && (bb = bodyB->AsRigidBody()))
	{
		velB = bb->Derive().linearVel.Dot(contactABody);
	}
	if (!ba && !bb)
		return 0.0f;

	f32 speedRel = velA - velB;

	f32 impulseMag = kInv[0][0] * (finalRelativeSpeed - speedRel);

	neV3 impulse;

	f32 mag = impulseMag;

	if (mag > depth)
	{
		impulse = depth * sim->_currentTimeStep * contactABody;
	}
	else if (mag < -depth)
	{
		impulse = -depth * sim->_currentTimeStep * contactABody;
	}
	else
	{
		impulse = impulseMag * contactABody;
	}

	neV3 zeroVector; zeroVector.SetZero();

	if (ba)
		ApplyCollisionImpulseFast(ba, impulse, zeroVector, sim->currentRecord);

	impulse *= -1.f;

	if (bb)
		ApplyCollisionImpulseFast(bb, impulse, zeroVector, sim->currentRecord);

	return 0.0f;
}

void neFixedTimeStepSimulator::SolveContactConstrain()
{
// first solve all single object to terrain/animated body contacts

	cresultHeap2.Clear();

	neStackInfoItem * sitem = (neStackInfoItem * )stackHeaderX.head;

	solverStage = 1;

	while (sitem)
	{
		neStackInfo * sinfo = (neStackInfo *)sitem;

		sitem = sitem->next;

		if (sinfo->body->status == neRigidBody_::NE_RBSTATUS_IDLE)
		{
			continue;
		}
		if (!sinfo->body->needSolveContactDynamic)
		{
			continue;
		}
		sinfo->body->AddContactImpulseRecord(0);

		if (cresultHeap2.GetUsedCount() == 0)
		{
			continue;
		}
		
		neRigidBody_* rb = NULL;
		

		for (s32 tt = 0; tt < cresultHeap2.GetUsedCount(); tt++)
		{
			//neCollisionResult * cr = (neCollisionResult *)ci;
			neCollisionResult * cr = &cresultHeap2[tt];//(neCollisionResult *)ci;

			HandleCollision(cr->bodyA, cr->bodyB, *cr, IMPULSE_CONTACT, 1.0f/*cr->impulseScale*/);

			//ci = ci->next;
			rb = cr->bodyA->AsRigidBody();
		}
		cresultHeap2.Clear();

		ASSERT(rb);

		if (rb->CheckStationary())
		{
			if (rb->IsRestPointStillValid())
			{
				if (rb->CheckRestHull())
				{
					rb->BecomeIdle();
				}
			}
		}
	}

	// release any empty stack header
	
	neStackHeaderItem * hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (hitem)
	{		
		neStackHeader * sheader = (neStackHeader *) hitem;

		hitem = hitem->next;

		if (sheader->infoCount > 1)
			continue;

		neStackInfo * s = sheader->head;

		neRigidBody_ * rb = s->body;

		if (s->isTerminator)
		{
			ASSERT(rb->AllRestRecordInvalid());
			
			rb->stackInfo = NULL;

			sheader->Remove(s, 1);

			stackInfoHeap.Dealloc(s, 1);

			stackHeaderHeap.Dealloc(sheader);
		}
		else
		{
			sheader->Remove(s, 1);

			stackHeaderHeap.Dealloc(sheader);

			stackHeaderX.Add(s);
		}
	}
	hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (hitem)
	{
		neStackHeader * sheader = (neStackHeader *) hitem;

		hitem = hitem->next;

		if (sheader->isAllIdle || sheader->dynamicSolved)
			continue;

		pointerBuffer2.Clear(); // stack headers

		pointerBuffer1.Clear(); // constraint headers

		contactConstraintHeader.RemoveAll();

		cresultHeap2.Clear();

		sheader->AddToSolver(/*true*/);

#ifdef _WIN32
		perf->UpdateConstrain1();
#endif

		SolveOneConstrainChain(-1.0f, 2);

#ifdef _WIN32
		perf->UpdateConstrain2();
#endif

		if (contactConstraintHeader.StationaryCheck())
		{
			//all of object are stationary enough
			contactConstraintHeader.BecomeIdle(true);
		}
	}
	contactConstraintHeader.RemoveAll();

	cresultHeap2.Clear();
}

f32 neFixedTimeStepSimulator::SolveLocal(neCollisionResult * cr)
{
	f32 ret = 0.0f;

	neV3 velA;

	neV3 velB;

	neRigidBody_ * ba = NULL;

	neRigidBody_ * bb = NULL;

	if (cr->bodyA && cr->bodyA->AsRigidBody())
	{
		ba = cr->bodyA->AsRigidBody();
	}
	if (cr->bodyB && cr->bodyB->AsRigidBody())
	{
		bb = cr->bodyB->AsRigidBody();
	}
	
	switch (cr->impulseType)
	{
	case IMPULSE_CONTACT:
	case IMPULSE_CONSTRAINT:
	case IMPULSE_SLIDER:
		{
			if (ba)
			{
				velA = ba->VelocityAtPoint(cr->contactA);	
			}
			else
			{
				velA.SetZero();
			}
			if (bb)
			{
				velB = bb->VelocityAtPoint(cr->contactB);	
			}
			else
			{
				velB.SetZero();
			}

			neV3 relVel = velA - velB;

			cr->initRelVelWorld = relVel;

			if (cr->impulseType == IMPULSE_CONTACT)
				cr->initRelVel = cr->w2c * relVel;
			else if (cr->impulseType == IMPULSE_SLIDER)
				cr->initRelVel = relVel - relVel.Dot(cr->contactBWorld) * cr->contactBWorld;
			else
				cr->initRelVel = relVel;
		}	
		break;
	
	case IMPULSE_SLIDER_LIMIT_PRIMARY:
		{
			if (ba)
			{
				velA = ba->VelocityAtPoint(cr->contactA);	
			}
			else
			{
				velA.SetZero();
			}
			if (bb)
			{
				velB = bb->VelocityAtPoint(cr->contactB);	
			}
			else
			{
				velB.SetZero();
			}

			neV3 relVel = velA - velB;

			cr->finalRelativeSpeed = relVel.Dot(cr->contactBWorld) * -1.0f;

			//cr->initRelVel = cr->finalRelativeSpeed * cr->contactBWorld;
		}
		break;
		
	case IMPULSE_ANGULAR_LIMIT_PRIMARY:
		{
			f32 wA = 0.0f, wB = 0.0f;

			if (ba)
			{
				wA = ba->Derive().angularVel.Dot(cr->contactBBody);
			}
			if (bb)
			{
				wB = bb->Derive().angularVel.Dot(cr->contactBBody);	
			}
			cr->relativeSpeed = wA - wB;
		}
		break;

	case IMPULSE_ANGULAR_LIMIT_SECONDARY:
		{
			//do nothing
		}

		break;
	}
	switch (cr->impulseType)
	{
	case IMPULSE_CONTACT:

		ret = cr->SolveContact(this);

		break;
	
	case IMPULSE_CONSTRAINT:
		{
			//cr->relativeSpeed = cr->initRelVel.Length();

			//if (!neIsConsiderZero(cr->relativeSpeed))
				ret	= cr->SolveConstraint(this);
		}
		break;

	case IMPULSE_SLIDER:
		{
			ret = cr->SolveSlider(this);
		}
		break;
	case IMPULSE_SLIDER_LIMIT_PRIMARY:
		{
			ret = cr->SolveSliderLimit(this);
		}
		break;
	case IMPULSE_ANGULAR_LIMIT_PRIMARY:

		//((_neConstraint*)(cr->convexA))->limitStates[0].EnforcePrimary();

		ret = cr->SolveAngularPrimary(this);

		break;

	case IMPULSE_ANGULAR_LIMIT_SECONDARY:
		
		//((_neConstraint*)(cr->convexA))->limitStates[1].EnforceSecondary();

		ret = cr->SolveAngularSecondary(this);

		break;

	case IMPULSE_ANGULAR_MOTOR_PRIMARY:

		ret = cr->SolveAngularMotorPrimary(this);
		
		break;

	case IMPULSE_ANGULAR_MOTOR_SECONDARY:
		break;

	case IMPULSE_RELATIVE_LINEAR_VELOCITY:

		ret = cr->SolveRelativeLinear(this);

		break;
	}

	return ret;
}

void neFixedTimeStepSimulator::CheckIfStationary()
{
	neBool allStationary = true;
	s32 jj;
	
	for (jj = 0; jj < pointerBuffer1.GetUsedCount(); jj++) // in this loop we apply the total impulse from the 
															// solving stage to the rigid bodies
	{
		neConstraintHeader * ch = (neConstraintHeader*)pointerBuffer1[jj];

		if (!ch->StationaryCheck())
		{
			allStationary = FALSE;
		}
	}// next jj, next constraint

	if (!contactConstraintHeader.StationaryCheck())
	{
		allStationary = FALSE;
	}
	if (allStationary)
	{	
		//make everything idle
		for (jj = 0; jj < pointerBuffer1.GetUsedCount(); jj++) // in this loop we apply the total impulse from the 
																// solving stage to the rigid bodies
		{
			neConstraintHeader * ch = (neConstraintHeader*)pointerBuffer1[jj];

			ch->BecomeIdle();
		}// next jj, next constraint

		contactConstraintHeader.BecomeIdle();
	}
	else
	{
		//make everything idle
		for (jj = 0; jj < pointerBuffer1.GetUsedCount(); jj++) // in this loop we apply the total impulse from the 
																// solving stage to the rigid bodies
		{
			neConstraintHeader * ch = (neConstraintHeader*)pointerBuffer1[jj];

			ch->WakeUp();
		}// next jj, next constraint

		contactConstraintHeader.WakeUp();
	}
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::ResolvePenetration
*
****************************************************************************/ 
void neFixedTimeStepSimulator::ResolvePenetration()
{
	//CheckStackHeader();

	neStackInfoItem * sitem = (neStackInfoItem * )stackHeaderX.head;
/*
	while (sitem)
	{
		neStackInfo * sinfo = (neStackInfo *)sitem;

		sitem = sitem->next;

		if (sinfo->body->status != neRigidBody_::NE_RBSTATUS_IDLE || sinfo->body->isShifted)
			sinfo->Resolve();
	}
*/
	neSimpleArray<neStackHeader*> & activeHeaderBuffer = *((neSimpleArray<neStackHeader*>*)&pointerBuffer1);

	activeHeaderBuffer.Clear();

	// check if any of the stacks are all idle

	neStackHeaderItem * hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (hitem)
	{
		neStackHeader * sheader = (neStackHeader *) hitem;

		sheader->isAllIdle = true;

		neStackInfoItem * sitem = (neStackInfoItem *)sheader->head;

		while (sitem)
		{
			neStackInfo * sinfo = (neStackInfo*) sitem;

			sitem = sitem->next;

			if (sinfo->body->status != neRigidBody_::NE_RBSTATUS_IDLE || sinfo->body->isShifted)
			{
				sheader->isAllIdle = false;

				break;
			}
		}

		hitem = hitem->next;
	}

	//Resolve penetration

	hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (hitem)
	{		
		neStackHeader * sheader = (neStackHeader *) hitem;

		//sheader->CheckHeader();

		if (!sheader->isAllIdle)
		{
			//if (!sheader->dynamicSolved)
			//	sheader->Resolve();

			*activeHeaderBuffer.Alloc() = sheader;
		}
		hitem = hitem->next;
	}

	// check if any of the rest contact are still valid

	neRigidBody_ * rb;

	neList<neRigidBody_> * activeList;
	
	for (s32 j = 0; j < 2; j++)
	{
		if (j == 0)
			activeList = &activeRB;
		else
			activeList = &activeRP;

		rb = activeList->GetHead();

		while (rb)
		{
			//if (((s32)rb->id % 3) != cc)
			//{
			//	rb = activeRB.GetNext(rb);
			//	continue;
			//}
			if (!rb->stackInfo)
			{
				rb = activeList->GetNext(rb);
				continue;
			}
			if (rb->stackInfo->isTerminator)
			{
				rb = activeList->GetNext(rb);
				continue;
			}
			s32 v;
			
			v = rb->CheckContactValidity();

			rb->isShifted = false;
			if (v == 0)
			{
	/*			char ss[256];
				sprintf(ss, "%d disconnected\n", rb->id);
				OutputDebugString(ss);
	*/		}
			if (v == 0)
			{
				if (rb->stackInfo->stackHeader->isHeaderX)
				{
					stackHeaderX.Remove(rb->stackInfo);
	/*
					char ss[256];
					sprintf(ss, "disconnect %d from ground\n", rb->id);
					OutputDebugString(ss);
	*/				
					stackInfoHeap.Dealloc(rb->stackInfo, 1);

					ASSERT(rb->AllRestRecordInvalid());

					rb->stackInfo = NULL;					
				}
			}

			if (v <= 1) //no longer resting
			{
				if (rb->status == neRigidBody_::NE_RBSTATUS_IDLE && rb->_constraintHeader == NULL)
				{
					rb->WakeUp();
				}
			}
			rb = activeList->GetNext(rb);
		}
	}

/*
	hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());
	
	while (hitem)
	{		
		neStackHeader * sheader = (neStackHeader *) hitem;

		sheader->CheckHeader();

		hitem = hitem->next;
	}

	CheckStackHeader();
*/

	// check if the stack set is disconnected

	s32 thisFrame = stepSoFar % 5; //check stack disconnection once every 3 frames

	for (s32 i = 0; i < activeHeaderBuffer.GetUsedCount(); i++)
	{
		s32 g = i % 5;

		if (g == thisFrame)
		{
			if (!activeHeaderBuffer[i]->isAllIdle)
			{
				activeHeaderBuffer[i]->CheckStackDisconnected();
		
				stackHeaderHeap.Dealloc(activeHeaderBuffer[i]);
			}
		}
	}

/*	
	hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());
	
	while (hitem)
	{		
		neStackHeader * sheader = (neStackHeader *) hitem;

		sheader->CheckHeader();

		hitem = hitem->next;
	}
*/
	// apply collision impulse

	// check for empty stack sets
	hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (hitem)
	{
		neStackHeader * h = (neStackHeader *) hitem;

		if (h->infoCount == 0)
		{
			hitem = hitem->next;

			stackHeaderHeap.Dealloc(h);
		}
		else
		{
			hitem = hitem->next;
		}
	}

/*
	hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (hitem)
	{		
		neStackHeader * sheader = (neStackHeader *) hitem;

		sheader->CheckHeader();

		hitem = hitem->next;
	}
*/
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::HandleCollision
*
****************************************************************************/ 

neV3 neFixedTimeStepSimulator::CalcNormalImpulse(neCollisionResult & cresult, neBool isContact)
{
	neV3 pI, pII, impulse;

	pI.Set(0.0f, 0.0f, -cresult.initRelVel[2] / cresult.k[2][2]);

	pII = cresult.kInv * cresult.initRelVel;

	pII *= -1.0f;

	neV3 pDiff = pII - pI;

	f32 e = 0.0f;
	f32 et = 1.0f;
	f32 u = 0.0f;

	f32 eA, uA, eB, uB, den;

	GetMaterial(cresult.materialIdA, uA, eA, den);

	GetMaterial(cresult.materialIdB, uB, eB, den);

	if (uA < uB)
		u = uA;
	else
		u = uB;
	
	if (isContact)
	{
		e = 0.0f;

		//u *= 1.2f;

		//if (u > 1.0f)
		//	u = 1.0f;
	}
	else
	{
		//e = (eA + eB) * 0.5f;
		if (eA < eB)
			e = eA;
		else
			e = eB;
	}

	et = 0.0f;//u * 2.0f - 1.0f;

	neV3 candidate = pI * (1.0f + e);
	
	candidate += (1.0f + et) * pDiff; 

	f32 t1 = candidate[0] * candidate[0] + candidate[1] * candidate[1];

	f32 t2 = u * candidate[2];

	t2 = t2 * t2;

	if (t1 > t2)
	{
		f32	kap,
			temp;

		kap = u * (1.0f + e) * pI[2];

		neV3 tempv; tempv.Set( pII[0], pII[1], 0.0f );

		temp = sqrtf(tempv.Dot(tempv ));

		temp -= u * pDiff[2];

		if ( neAbs( temp ) > NE_ZERO )
		{
			kap /= temp;

			impulse = pI * (1.0f + e);

			tempv = pDiff * kap;

			impulse = impulse + tempv;
		}
		else
		{
			impulse.SetZero();
		}
	}
	else
	{
		impulse = candidate;
	}

	return impulse;
}

f32 neFixedTimeStepSimulator::HandleCollision(neRigidBodyBase * bodyA, 
											   neRigidBodyBase * bodyB, 
											   neCollisionResult & cresult, neImpulseType impulseType, 
											   f32 scale)
{
//	ASSERT(impulseType == IMPULSE_NORMAL);

	neM3 w2c; 

	w2c.SetTranspose(cresult.collisionFrame);

	ASSERT(w2c.IsFinite());

	neV3 velA;

	neV3 velB;

	neRigidBody_ * ba = NULL;

	neRigidBody_ * bb = NULL;

	if (bodyA && bodyA->AsRigidBody())
	{
		ba = bodyA->AsRigidBody();
	}
	if (bodyB && bodyB->AsRigidBody())
	{
		bb = bodyB->AsRigidBody();
	}

	if (!ba && !bb)
		return 0;

	if (ba)
	{
		velA = ba->VelocityAtPoint(cresult.contactA);	
	}
	else
	{
		velA.SetZero();
	}
	if (bb)
	{
		velB = bb->VelocityAtPoint(cresult.contactB);	
	}
	else
	{
		velB.SetZero();
	}

	neV3 relVel = velA - velB;

	cresult.initRelVelWorld = relVel;

	cresult.initRelVel = w2c * relVel;

	f32 initSpeed = cresult.relativeSpeed = cresult.initRelVel.Length();

	f32 theshold = -1.0f;

//	neM3 k, kInv;

	neV3 impulse;

	f32 ret = 0.0f;

	switch (impulseType)
	{
	case IMPULSE_NORMAL:
		{
			if (cresult.initRelVel[2] >= 0.0f)
			{
				return 0.0f;
			}

			impulse = CalcNormalImpulse(cresult, FALSE);
		}
		break;

	case IMPULSE_CONTACT:
		{
			cresult.SolveContact(this);
			return 0.0f;
		}
		break;
		
	default:
		ASSERT(0);
		break;
	}

	if (!impulse.IsFinite())
	{
		//ASSERT(0);

		return 0.0f;
	}

	impulse = cresult.collisionFrame * impulse * scale;

	neV3 bimpulse;

	if (impulseType == IMPULSE_NORMAL)
	{
		neBool doBreakCheck = (impulseType == IMPULSE_NORMAL && breakageCallback); 

		if (doBreakCheck)
		{
			if (!ba) // meaning either ca and bb
			{
				if (cresult.convexA->breakInfo.flag != neGeometry::NE_BREAK_DISABLE)
				{
					CheckBreakage(bodyA, cresult.convexA, cresult.contactAWorld, impulse);
				}
				bimpulse = impulse * -1.0f;

				if (cresult.convexB->breakInfo.flag != neGeometry::NE_BREAK_DISABLE)
				{
					CheckBreakage(bb, cresult.convexB, cresult.contactBWorld, bimpulse);
				}
				bb->ApplyCollisionImpulse(bimpulse, cresult.contactB, impulseType);

				bb->needRecalc = true;
			}
			else // meaning ba and bb, or ba and cb
			{
				if (!bb) //ba and cb
				{
					if (cresult.convexB->breakInfo.flag != neGeometry::NE_BREAK_DISABLE)
					{
						bimpulse = impulse * -1.0f;

						CheckBreakage(bodyB, cresult.convexB, cresult.contactBWorld, bimpulse);

						impulse = bimpulse * -1.0f;
					}
					if (cresult.convexA->breakInfo.flag != neGeometry::NE_BREAK_DISABLE)
					{
						CheckBreakage(ba, cresult.convexA, cresult.contactAWorld, impulse);
					}
					ba->ApplyCollisionImpulse(impulse, cresult.contactA, impulseType);

					ba->needRecalc = true;
				}
				else // meaning ba and bb
				{
					if (cresult.convexA->breakInfo.flag != neGeometry::NE_BREAK_DISABLE)
					{
						CheckBreakage(ba, cresult.convexA, cresult.contactAWorld, impulse);
					}
					bimpulse = impulse * -1.0f;
					
					if (cresult.convexB->breakInfo.flag != neGeometry::NE_BREAK_DISABLE)
					{
						CheckBreakage(bb, cresult.convexB, cresult.contactBWorld, bimpulse);
					}
					ba->ApplyCollisionImpulse(impulse, cresult.contactA, impulseType);

					ba->needRecalc = true;
		
					bb->ApplyCollisionImpulse(bimpulse, cresult.contactB, impulseType);

					bb->needRecalc = true;
				}
			}
		}
		else
		{
			if (ba)
			{
				ba->ApplyCollisionImpulse(impulse, cresult.contactA, impulseType);

				ba->needRecalc = true;
			}
			if (bb)
			{
				bimpulse = impulse * -1.0f;

				bb->ApplyCollisionImpulse(bimpulse, cresult.contactB, impulseType);

				bb->needRecalc = true;
			}
		}
	}
	else
	{
		if (ba)
		{
			ba->ApplyCollisionImpulse(impulse, cresult.contactA, impulseType);

			ba->needRecalc = true;
		}
		if (bb)
		{
			bimpulse = impulse * -1.0f;
			
			bb->ApplyCollisionImpulse(bimpulse, cresult.contactB, impulseType);

			bb->needRecalc = true;
		}
	}
	return ret;
}

#define AUTO_SLEEP_ON

void neFixedTimeStepSimulator::SolveAllConstrain()
{
	if (constraintHeaders.GetUsedCount() == 0)
		return;

	neDLinkList<neConstraintHeader>::iterator chiter;

	for (chiter = constraintHeaders.BeginUsed(); chiter.Valid(); chiter++)
	{
		(*chiter)->solved = false;
	}

	for (chiter = constraintHeaders.BeginUsed(); chiter.Valid(); chiter++)
	{
		if ((*chiter)->solved)
			continue;

		pointerBuffer2.Clear(); // stack headers

		pointerBuffer1.Clear(); // constraint headers

		contactConstraintHeader.RemoveAll();

		cresultHeap.Clear();

		cresultHeap2.Clear();

		neByte ** pt = pointerBuffer1.Alloc();

		*pt = (neByte*)(*chiter);
		
		neBodyHandle * bodyHandle = (*chiter)->bodies.GetHead();

		s32 allIdle = 0;
		s32 nbody = 0;
		neRigidBody_ * lastrb = NULL;

		while (bodyHandle)
		{
			neRigidBody_ * rb = bodyHandle->thing->AsRigidBody();

			if (rb)
			{
				rb->maxErrCResult = NULL;

				nbody++;
			}
		
			if (rb && rb->status != neRigidBody_::NE_RBSTATUS_IDLE)
			{
				allIdle++;
			}
			lastrb = rb;
			bodyHandle = bodyHandle->next;
		}
		if (allIdle == 0)
			continue;

		//lastrb->WakeUpAllJoint();
		
		f32 epsilon = -1.0f;//DEFAULT_CONSTRAINT_EPSILON;

		s32 iteration = -1;

		(*chiter)->AddToSolver(epsilon, iteration); // pointerBuffer2 will be filled after this call

		AddContactConstraint(epsilon, iteration);

		SolveOneConstrainChain(epsilon, iteration);

		CheckIfStationary();
	}
	contactConstraintHeader.RemoveAll();
}

void neFixedTimeStepSimulator::SolveOneConstrainChain(f32 epsilon, s32 iteration)
{
	solverStage = 0;

	if (cresultHeap.GetUsedCount() == 0 && cresultHeap2.GetUsedCount() == 0)
	{
		return;
	}

	if (epsilon == -1.0f)
		epsilon = DEFAULT_CONSTRAINT_EPSILON;

	if (iteration == -1)
	{
		iteration = (s32) (DEFAULT_CONSTRAINT_ITERATION);// * cresultHeap.GetUsedCount());

		if (iteration == 0)
			iteration = 1;
	}
	// pp == 0 is friction stage
	// pp == 1 is penetration stage


	s32 checkSleep = iteration >> 1;

	solverLastIteration = false;
	
	for (s32 pp = 0; pp < 2; pp++)
	{
		if (pp == 1)
		{
			solverStage = 1;
/*
			for (s32 tt = 0; tt < cresultHeap2.GetUsedCount(); tt++)
			{
				neCollisionResult * cr = &cresultHeap2[tt];
				
				ASSERT(cr->impulseType == IMPULSE_CONTACT);

				cr->StartStage2();
			}
*/		}
		s32 it;

		it = iteration;

		for (s32 i = 0; i < it; i++)
		{
			neBool doCheckSleep = false;

			if (pp == 1  && i == checkSleep)
			{
				doCheckSleep = true;
			}
			if (pp == 1 && i == (it -1))
			{
				solverLastIteration = true;
			}
			f32 maxError = 0.0f;

			s32 nConstraint = 0;

			neCollisionResult * cresult = &cresultHeap[0]; //*cresultHeap.BeginUsed();

			s32 tt;

			for (tt = 0; tt < cresultHeap.GetUsedCount(); tt++)
			{
				neCollisionResult * cr = &cresultHeap[tt];

				f32 err = 0.0f;

				err = SolveLocal(cr);

				if (err > maxError)
					maxError = err;
			}
			//for (tt = 0; tt < cresultHeap2.GetUsedCount(); tt++)
			for (tt = cresultHeap2.GetUsedCount()-1; tt >= 0 ; tt--)
			{
				neCollisionResult * cr = &cresultHeap2[tt];

				f32 err = 0.0f;

				err = SolveLocal(cr);

				if (err > maxError)
					maxError = err;
			}
			//maxError = 0.0f;

			s32 jj;
			
			for (jj = 0; jj < pointerBuffer1.GetUsedCount(); jj++) // in this loop we apply the total impulse from the 
																	// solving stage to the rigid bodies
			{
				neConstraintHeader * ch = (neConstraintHeader*)pointerBuffer1[jj];

				ch->TraverseApplyConstraint(doCheckSleep);
			}// next jj, next constraint

			contactConstraintHeader.TraverseApplyConstraint(doCheckSleep);

#if 1//AUTO_SLEEP_ON

			//if (doCheckSleep)
			if (pp == 1 && i == (it - 2)) // the second last iteration
			{
				for (tt = 0; tt < cresultHeap2.GetUsedCount(); tt++)
				{
					neCollisionResult * cr = &cresultHeap2[tt];

					if (cr->impulseType == IMPULSE_CONTACT)
					{
						cr->PrepareForSolver(false, true);
					}

				}
			}
#endif //AUTO_SLEEP_ON

		}
	}

	cresultHeap.Clear();

	cresultHeap2.Clear();

	ASSERT(cresultHeap.GetUsedCount() == 0);
}
void neFixedTimeStepSimulator::AddContactConstraint(f32 & epsilon, s32 & iteration)
{
	for (s32 i = 0; i < pointerBuffer2.GetUsedCount(); i++)
	{
		neStackHeader * sheader = (neStackHeader *) pointerBuffer2[i];

		sheader->AddToSolver(/*true*/);

		neStackInfoItem * sitem = (neStackInfoItem *)sheader->head;

		while (sitem)
		{
			neStackInfo * sinfo = (neStackInfo*) sitem;

			sitem = sitem->next;

			if (sinfo->body->_constraintHeader &&
				!sinfo->body->_constraintHeader->solved)
			{
				sinfo->body->_constraintHeader->AddToSolver(epsilon, iteration);

				*pointerBuffer1.Alloc() = (neByte *)(sinfo->body->_constraintHeader);
			}
		}
	}
}

/****************************************************************************
*
*	ChooseAxis
*
****************************************************************************/ 

void ChooseAxis(neV3 & x, neV3 & y, const neV3 & normal)
{
	neV3 mag;

	mag[0] = neAbs(normal[0]);
	mag[1] = neAbs(normal[1]);
	mag[2] = neAbs(normal[2]);

	if (mag[0] > mag[1])
	{
		if (mag[0] > mag[2])
		{
			x[0] = (normal[1] + normal[2]) / normal[0] * -1.0f;
			x[1] = 1.0f;
			x[2] = 1.0f;
		}
		else
		{
			x[2] = (normal[0] + normal[1]) / normal[2] * -1.0f;
			x[0] = 1.0f;
			x[1] = 1.0f;
		}
	}
	else if (mag[1] > mag[2])
	{
		x[1] = (normal[0] + normal[2]) / normal[1] * -1.0f;
		x[0] = 1.0f;
		x[2] = 1.0f;
	}
	else
	{
		x[2] = (normal[0] + normal[1]) / normal[2] * -1.0f;
		x[0] = 1.0f;
		x[1] = 1.0f;
	}
	x.Normalize();
	y = normal.Cross(x);
}
