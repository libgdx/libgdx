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

#pragma inline_recursion( on )
#pragma inline_depth( 250 )

#include "stdio.h"

/*
#ifdef _WIN32
#include <windows.h>
#endif
*/

#include "tokamak.h"
#include "containers.h"
#include "scenery.h"
#include "collision.h"
#include "constraint.h"
#include "rigidbody.h"
#include "scenery.h"
#include "stack.h"
#include "simulator.h"
#include "message.h"

#define ne_Default_Mass 1.0f

#define CONTACT_VALIDITY_NORMAL_DISTANCE (0.01f)
#define CONTACT_VALIDITY_TANGENT_DISTANCE_SQ (0.001f)//(0.01f * 0.01f)

/****************************************************************************
*
*	neRigidBody_::CheckForIdle
*
****************************************************************************/ 
neBool TestZeroInTriangle(const neV3 & _p1, const neV3 & _p2, const neV3 & _p3)
{
	ASSERT(_p1.IsFinite());
	ASSERT(_p2.IsFinite());
	ASSERT(_p3.IsFinite());

	neV3 p1, p2, p3, average;

	average = _p1 + _p2 + _p3;

	average *= (1.0f / 3.0f);

	//increase the triangle by 10%
	p1 = average + (_p1 - average) * 1.1f;
	p2 = average + (_p2 - average) * 1.1f;
	p3 = average + (_p3 - average) * 1.1f;

	//p1 cross p2
	const s32 x = 0;
	const s32 z = 2;

	f32 cross12 = p1[z] * p2[x] - p1[x] * p2[z];
	f32 cross23 = p2[z] * p3[x] - p2[x] * p3[z];
	f32 cross31 = p3[z] * p1[x] - p3[x] * p1[z];

	if (cross12 > 0.0f && cross23 > 0.0f && cross31 > 0.0f)
	{
		return true;
	}
	else if (cross12 < 0.0f && cross23 < 0.0f && cross31 < 0.0f)
	{
		return true;
	}
	return false;
}

void neRigidBody_::CheckForIdle()
{
	CheckForIdleNonJoint();
/*
	if (CheckStationary())
	{
		if ((stackInfo && IsRestPointStillValid()))
		{
			s32 hull = CheckRestHull();

			if (hull >= 2)
			{
				//if (hull == 2 && lowEnergyCounter > 100)
				//	BecomeIdle();
				//else if (hull > 2)
					BecomeIdle();
			}
		}
	}
*/
}
#pragma optimize( "", off )		
void neRigidBody_::CheckForIdleNonJoint()
{
	//return;
	
	f32 e = Derive().linearVel.Dot(Derive().linearVel);

	f32 f = (Derive().angularVel.Dot(Derive().angularVel)) * 1.0f;

	if (e < sim->highEnergy && f < sim->highEnergy)
	{
		lowEnergyCounter++;
	}
	else
	{
		s32 i;

		neV3 total; total.SetZero();

		for (i = 0; i < NE_RB_MAX_PAST_RECORDS; i++)
		{
			total += GetVelRecord(i);
		}

		neV3 t;
		t = total *(1.0f / NE_RB_MAX_PAST_RECORDS);
		total = t;

		ASSERT(total.IsFinite());

		f32 g; g = total.Dot(sim->gravityVector);

		if (g > 0.0f)
		{
			total.RemoveComponent(sim->gravityVector);
		}

		f32 v = total.Dot(total);

		if (v > 2.0f)//5
		{
			lowEnergyCounter = 0;
			return;
		}
		total.SetZero();

		for (i = 0; i < NE_RB_MAX_PAST_RECORDS; i++)
		{
			total += GetAngVelRecord(i);
		}
		total *= (1.0f / NE_RB_MAX_PAST_RECORDS);

		v = total.Dot(total);

		if (v > 1.0f)//4
		{
			lowEnergyCounter = 0;
			return;
		}
		lowEnergyCounter+=1;
	}
	if (lowEnergyCounter > 10)
	{
		if ((stackInfo && IsRestPointStillValid()))
		{
			s32 hull = CheckRestHull();

			if (hull)
			{
				if (hull == 2 && lowEnergyCounter > 100)
					BecomeIdle();
				else if (hull > 2)
					BecomeIdle();
			}
		}
	}
}
#pragma optimize( "", on )
void neRigidBody_::CheckForIdleJoint()
{
	f32 e = Derive().linearVel.Length();

	f32 f = col.boundingRadius * Derive().angularVel.Length();

	e += f;

	if (e < sleepingParam)
	{
		lowEnergyCounter++;
	}
	else
	{
		lowEnergyCounter = 0;
	}
	if (lowEnergyCounter > 50)
	{
		// calculate net force
		//NE_RB_MAX_PAST_RECORDS

		neV3 sum;

		sum.SetZero();

		for (s32 i = 0; i < NE_RB_MAX_PAST_RECORDS; i++)
		{
			sum += dvRecord[i];
		}

		ASSERT(sum.IsFinite());

		neV3 average = sum;// / (f32)NE_RB_MAX_PAST_RECORDS;

		f32 len1 = average.Length();

		if (len1 < sleepingParam/*0.3f*/)
		{
			BecomeIdle();
		}
	}
}

s32 neRigidBody_::CheckRestHull()
{
	if (totalForce.IsConsiderZero())
		return 3;
	
	neM3 forceFrame;

	forceFrame[1] = totalForce;

	forceFrame[1].Normalize();

	void ChooseAxis(neV3 & x, neV3 & y, const neV3 & normal);

	ChooseAxis(forceFrame[0], forceFrame[2], forceFrame[1]);

	forceFrame.SetTranspose(forceFrame);

	neV3 p[3];
	
	if (GetRestHull().htype == neRestHull::TRIANGLE)
	{
		neBool allIdle = true;

		for (s32 i = 0; i < 3; i++)
		{
			if (!GetRestRecord(i).IsValid())
			{
				ASSERT(0);
			}
			if (!GetRestRecord(i).CanConsiderOtherBodyIdle())
			{
				allIdle = false;

				break;
			}
			p[i] = GetRestRecord(i).worldThisBody - GetPos();

			p[i] = forceFrame.TransposeMulV3(p[i]);
		}
		if (!allIdle)
			return 0;
		
		neBool ret = TestZeroInTriangle(p[0], p[1], p[2]);

		if (ret)
			return 3;
	}
	if (GetRestHull().htype == neRestHull::LINE)
	{
		neBool allIdle = true;

		for (s32 i = 0; i < 2; i++)
		{
			s32 j = GetRestHull().indices[i];

			if (!GetRestRecord(j).IsValid())
			{
				allIdle = false;

				break;
			}
			if (!GetRestRecord(j).CanConsiderOtherBodyIdle())
			{
				allIdle = false;

				break;
			}
			p[i] = GetRestRecord(j).worldThisBody - GetPos();

			p[i] = forceFrame.TransposeMulV3(p[i]);

			p[i][1] = 0.0f;
		}
		if (!allIdle)
			return 0;
		
		p[2].SetZero();
		
		f32 d = p[2].GetDistanceFromLine(p[0], p[1]);

		if (d < 0.005f)
			return 2;

		return false;
	}
	else if (GetRestHull().htype == neRestHull::POINT)
	{
		neBool allIdle = true;

		s32 i = GetRestHull().indices[0];

		if (!GetRestRecord(i).IsValid())
		{
			return 0;
		}
		if (!GetRestRecord(i).CanConsiderOtherBodyIdle())
		{
			return 0;
		}
		p[0] = GetRestRecord(GetRestHull().indices[0]).worldThisBody - GetPos();

		p[0] = forceFrame.TransposeMulV3(p[0]);

		p[0][1] = 0.0f;
		
		f32 d = sqrtf(p[0][0] * p[0][0] + p[0][2] * p[0][2]);

		if (d < 0.005f)
			return 1;

		return false;
	}
	else
	{
		return false;
	}
}

neBool neRigidBody_::CheckStillIdle()
{
	if (!CheckHighEnergy())
	{
		if (subType == NE_RIGID_PARTICLE || CheckRestHull())
		{
			ZeroMotion();

			UpdateController();
			
			sim->idleBodyCount++;
			return true;
		}
		else
		{
			return false;
		}
		
	}
	else
	{
		WakeUp();

		return false;
	}
}

s32 neRigidBody_::CheckContactValidity()
{
	if (!stackInfo)
		return 0;
	
	s32 validCount = 0;

	s32 i;

	neBool allIdle = false;

	if (status == neRigidBody_::NE_RBSTATUS_IDLE && !isShifted)
	{
		allIdle = true;

		for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
		{
			if (!GetRestRecord(i).IsValid())
			{
				continue;
			}
				
			if (!GetRestRecord(i).CanConsiderOtherBodyIdle())
			{
				allIdle = false;

				break;
			}
			validCount++;
		}
	}

	if (allIdle)
	{
		return validCount;
	}

	validCount = 0;

	for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (!GetRestRecord(i).IsValid())
			continue;

		GetRestRecord(i).Update();

		f32 d = GetRestRecord(i).normalWorld.Dot(sim->gravityVector);

		if (d > 0.0f/*-TILT_TOLERANCE*/)
		{
			GetRestRecord(i).SetInvalid();
			GetRestHull().htype = neRestHull::NONE;
			continue;
		}
		if (GetRestRecord(i).normalDiff > CONTACT_VALIDITY_NORMAL_DISTANCE)
		{
			GetRestRecord(i).SetInvalid();
			GetRestHull().htype = neRestHull::NONE;
			continue;
		}
		if (GetRestRecord(i).tangentialDiffSq > CONTACT_VALIDITY_TANGENT_DISTANCE_SQ)
		{
			GetRestRecord(i).SetInvalid();
			GetRestHull().htype = neRestHull::NONE;
			continue;
		}
		validCount++;
	}
	if (validCount == 0)
		stackInfo->isBroken = true;
	else
		stackInfo->isBroken = false;
	
	return validCount;
}

s32 neRigidBody_::AddContactImpulseRecord(neBool withConstraint)
{
	s32 i = 0;
	static neV3 world1[NE_RB_MAX_RESTON_RECORDS];
	static neV3 world2[NE_RB_MAX_RESTON_RECORDS];
	static neV3 diff[NE_RB_MAX_RESTON_RECORDS];
	static f32 height[NE_RB_MAX_RESTON_RECORDS];
	s32 validCount = 0;
	static s32 validIndices[NE_RB_MAX_RESTON_RECORDS];
	s32 deepestIndex = -1;
	f32 deepest = -1.0e6f;

	for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (!GetRestRecord(i).IsValid())
			continue;

		if (!GetRestRecord(i).CheckOtherBody(sim))
		{
			GetRestRecord(i).SetInvalid();

			continue;
		}
		GetRestRecord(i).Update();

		world1[i] = State().b2w * GetRestRecord(i).bodyPoint;

		world2[i] = GetRestRecord(i).GetOtherBodyPoint();

		diff[i] = world1[i] - world2[i];

		neV3 d; d = diff[i];

		d.RemoveComponent(GetRestRecord(i).normalWorld);

		if (d.Dot(d) > 0.025f)
		{
			GetRestRecord(i).SetInvalid();

			GetRestHull().htype = neRestHull::NONE;

			continue;
		}

		height[i] = diff[i].Dot(GetRestRecord(i).normalWorld);

		validIndices[validCount] = i;

		validCount++;

		if (height[i] > deepest)
		{
			deepest = height[i];

			deepestIndex = i;
		}
	}
	if (validCount == 0)
	{		
		GetRestHull().htype = neRestHull::NONE;
		return 0;
	}

	if (0)//subType == NE_RIGID_PARTICLE)
	{
		ASSERT(deepestIndex != -1);

		i = deepestIndex;

		neCollisionResult tmpcr;
		neCollisionResult * cresult = &tmpcr;

		cresult->bodyA = (neRigidBodyBase*)this;
		cresult->bodyB = GetRestRecord(i).GetOtherBody();
		cresult->collisionFrame[2] = GetRestRecord(i).normalWorld;
		cresult->contactA = world1[i] - GetPos();
		cresult->contactB = world2[i] - GetRestRecord(i).GetOtherBody()->GetB2W().pos;
		cresult->materialIdA = GetRestRecord(i).material;
		cresult->materialIdB = GetRestRecord(i).otherMaterial;
		cresult->depth = -GetRestRecord(i).normalDiff;//GetRestRecord(i).depth;	
		cresult->impulseType = IMPULSE_CONTACT;
		cresult->impulseScale = 1.0f;
		cresult->PrepareForSolver();
		
		if (withConstraint || !cresult->CheckIdle())
		{
			sim->AddCollisionResult(tmpcr);			
		}
	}

	s32 j;
	
	f32 heightScale = 1.0f;

	if (validCount == 1 && height[validIndices[0]] < 0.0f)
	{
		//if (subType == NE_RIGID_NORMAL)
		{
			i = validIndices[0];

			neCollisionResult tmpcr;
			neCollisionResult * cresult = &tmpcr;

			cresult->bodyA = (neRigidBodyBase*)this;
			cresult->bodyB = GetRestRecord(i).GetOtherBody();
			cresult->collisionFrame[2] = GetRestRecord(i).normalWorld;
			ASSERT(cresult->collisionFrame[2].IsFinite());
			cresult->contactA = world1[i] - GetPos();
			cresult->contactB = world2[i] - GetRestRecord(i).GetOtherBody()->GetB2W().pos;
			cresult->materialIdA = GetRestRecord(i).material;
			cresult->materialIdB = GetRestRecord(i).otherMaterial;
			cresult->depth = -GetRestRecord(i).normalDiff;//GetRestRecord(i).depth;	
			cresult->impulseType = IMPULSE_CONTACT;
			//cresult->UpdateConstraintRelativeSpeed();
			cresult->PrepareForSolver();
			cresult->impulseScale = 1.0f;

			if (withConstraint || !cresult->CheckIdle())
			{
				//*sim->cresultHeap.Alloc(0) = tmpcr;
				sim->AddCollisionResult(tmpcr);
			}
		}			
		GetRestHull().htype = neRestHull::NONE;
		return 1;
	}
	else if (validCount == 2)
	{
		s32 v1  = validIndices[0];
		s32 v2  = validIndices[1];

		neV3 d1 = world1[v1] - world1[v2];
		neV3 d2 = world2[v1] - world2[v2];

		f32 len1 = d1.Length();

		if (neIsConsiderZero(len1))
		{
			heightScale = 1.0f;
		}
		else
		{
			f32 len2 = d2.Length();

			if (neIsConsiderZero(len2))
			{
				heightScale = 1.0f;
			}
			else
			{
				d1 *= (1.0f / len1);

				d2 *= (1.0f / len2);

				heightScale = neAbs(d1.Dot(d2));
			}
		}

		ASSERT(neIsFinite(heightScale));
		//if (!neIsFinite(heightScale))
		//	heightScale = 1.0f;
	}
	else if (validCount == 3)
	{
		neV3 tri1[3];
		neV3 tri2[3];

		tri1[0] = world1[1] - world1[0];
		tri1[1] = world1[2] - world1[1];
		tri1[2] = world1[0] - world1[2];

		tri2[0] = world2[1] - world2[0];
		tri2[1] = world2[2] - world2[1];
		tri2[2] = world2[0] - world2[2];
		
		neV3 normal1 = tri1[1].Cross(tri1[0]);
		neV3 normal2 = tri2[1].Cross(tri2[0]);

		f32 len1 = normal1.Length();
		
		if (neIsConsiderZero(len1))
		{
			heightScale = 1.0f;
		}
		else
		{
			f32 len2 = normal2.Length();

			if (neIsConsiderZero(len2))
			{
				heightScale = 1.0f;
			}
			else
			{
				normal1 *= (1.0f / len1);

				normal2 *= (1.0f / len2);

				heightScale = neAbs(normal1.Dot(normal2));
			}
		}
		ASSERT(neIsFinite(heightScale));
		//if (!neIsFinite(heightScale))
		//	heightScale = 1.0f;
	}
	
	f32 e = 0.0005f;
	f32 f = 1.0f - e;

	heightScale = (heightScale - f) / e;

	if (heightScale < 0.0f)
	{
		heightScale = e;
	}
	s32 actualValidCount = 0;
	//f32 limit = 0.05f;
	f32 limit = 0.01f;

	for (i = 0; i < validCount; i++)
	{
		f32 scale = 1.0f;

		j = validIndices[i];

		f32 scaleLimit = 0.01f;//limit * heightScale;

		if (height[j] > 0)
		{
			if (height[j] > scaleLimit)
			{
				//GetRestRecord(j).rtype = neRestRecord::REST_ON_NOT_VALID;
				//GetRestHull().htype = neRestHull::NONE;
				continue;
			}
			scale = (scaleLimit - height[j]) / scaleLimit;

			scale = scale * scale * scale;
		}
		//if (subType == NE_RIGID_NORMAL)
		{
			neCollisionResult tmpcr;
			neCollisionResult * cresult = &tmpcr;//sim->cresultHeap.Alloc(0);

			cresult->bodyA = (neRigidBodyBase*)this;
			cresult->bodyB = GetRestRecord(j).GetOtherBody();
			cresult->collisionFrame[2] = GetRestRecord(j).normalWorld;
			cresult->contactA = world1[j] - GetPos();
			cresult->contactB = world2[j] - GetRestRecord(j).GetOtherBody()->GetB2W().pos;
			cresult->materialIdA = GetRestRecord(j).material;
			cresult->materialIdB = GetRestRecord(j).otherMaterial;
			cresult->depth = -GetRestRecord(j).normalDiff;//GetRestRecord(j).depth;
			cresult->impulseType = IMPULSE_CONTACT;
			//cresult->UpdateConstraintRelativeSpeed();
			cresult->PrepareForSolver();
			cresult->impulseScale = scale;			
			if (withConstraint || !cresult->CheckIdle())
			{
				//*sim->cresultHeap.Alloc(0) = tmpcr;
				sim->AddCollisionResult(tmpcr);
			}
		}
		//sim->HandleCollision(this, GetRestRecord(j).otherBody, cresult, IMPULSE_NORMAL, scale);

		GetRestHull().indices[actualValidCount++] = j;
	}
	if (actualValidCount >= 2)
	{
		if (actualValidCount == 2)
		{
			GetRestHull().htype = neRestHull::LINE;
			
			ASSERT(GetRestRecord(GetRestHull().indices[0]).IsValid());
			ASSERT(GetRestRecord(GetRestHull().indices[1]).IsValid());

			GetRestHull().normal = GetRestRecord(GetRestHull().indices[0]).normalWorld +
									GetRestRecord(GetRestHull().indices[1]).normalWorld;
/*
			neV3 diff = world2[GetRestHull().indices[0]] - world2[GetRestHull().indices[1]];

			neV3 cross = diff.Cross(sim->gravityVector);

			GetRestHull().normal= cross.Cross(diff);
*/
			GetRestHull().normal.Normalize();

			if (GetRestHull().normal.Dot(sim->gravityVector) < 0.0f)
				GetRestHull().normal *= -1.0f;
		}
		else
		{
			GetRestHull().htype = neRestHull::TRIANGLE;

			ASSERT(GetRestRecord(GetRestHull().indices[0]).IsValid());
			ASSERT(GetRestRecord(GetRestHull().indices[1]).IsValid());
			ASSERT(GetRestRecord(GetRestHull().indices[2]).IsValid());

			GetRestHull().normal = GetRestRecord(GetRestHull().indices[0]).normalWorld +
									GetRestRecord(GetRestHull().indices[1]).normalWorld +
									GetRestRecord(GetRestHull().indices[2]).normalWorld;

/*
			neV3 diff1 = world2[GetRestHull().indices[0]] - world2[GetRestHull().indices[1]];

			neV3 diff2 = world2[GetRestHull().indices[2]] - world2[GetRestHull().indices[0]];

			GetRestHull().normal = diff1.Cross(diff2);
*/
			GetRestHull().normal.Normalize();

			if (GetRestHull().normal.Dot(sim->gravityVector) < 0.0f)
				GetRestHull().normal *= -1.0f;
		}
	}
	else
	{
		if (actualValidCount == 1)
		{
			GetRestHull().htype = neRestHull::POINT;

			ASSERT(GetRestRecord(GetRestHull().indices[0]).IsValid());

			GetRestHull().normal = GetRestRecord(GetRestHull().indices[0]).normalWorld;

			if (GetRestHull().normal.Dot(sim->gravityVector) < 0.0f)
				GetRestHull().normal *= -1.0f;
		}
		else
		{
			GetRestHull().htype = neRestHull::NONE;
		}
	}
	return actualValidCount;
}

void neRigidBody_::AddContactConstraint()
{
	if (stackInfo->stackHeader == &sim->stackHeaderX)
	{
		if (needSolveContactDynamic)
		{
			needSolveContactDynamic = false;
		
			AddContactImpulseRecord(true);
		}
	}
	else
	{
		if (stackInfo->stackHeader->dynamicSolved)
			return;

		neByte ** p = sim->pointerBuffer2.Alloc();

		ASSERT(p);

		*p = (neByte*)(stackInfo->stackHeader);

		stackInfo->stackHeader->dynamicSolved = true;
	}
}

neBool neRigidBody_::CheckHighEnergy()
{
	f32 e;
	
	f32 m;

	if (0)//_constraintHeader)
	{
		e = Derive().linearVel.Length();

		e += col.boundingRadius * Derive().angularVel.Length();

		m = 0.5f;
	}
	else
	{
		e = Derive().linearVel.Dot(Derive().linearVel);

		e += Derive().angularVel.Dot(Derive().angularVel);

		m = sim->highEnergy;
	}

	if (e < m)
		return false;
	
	return true;
}

neBool neRigidBody_::CheckStationary()
{
//	return false;

	const s32 oldCounterMax = 60;

	const f32 StationarySpeed = sleepingParam;//0.2f;
	const f32 StationaryAcc = 5.0f;
	const f32 StationaryW = 10.f;
	const f32 StationaryAngAcc = 10.5f;

	if (oldCounter < oldCounterMax)
		return FALSE;

	neV3 deltaPos = State().b2w.pos - oldPosition;

	neV3 vel = deltaPos / (sim->_currentTimeStep * oldCounterMax);

	f32 speed = vel.Length();

	if (speed > StationarySpeed)
	{
		SyncOldState();

		return FALSE;
	}

	neV3 deltaVel = Derive().linearVel - oldVelocity;

	neV3 acc = deltaVel / (sim->_currentTimeStep * oldCounterMax);

	f32 accMag = acc.Length();

	if (accMag > StationaryAcc)
	{
		SyncOldState();

		return FALSE;
	}

	neQ oldQInvert = oldRotation;

	oldQInvert.Invert();

	neQ deltaQ = State().q * oldQInvert;

	neV3 axis; f32 angle;

	deltaQ.GetAxisAngle(axis, angle);

	f32 angularVel = angle / (sim->_currentTimeStep * oldCounterMax);

	if (angularVel > StationaryW)
	{
		SyncOldState();

		return FALSE;
	}

	neV3 deltaW = Derive().angularVel - oldAngularVelocity;

	f32 angularAcc = deltaW.Length();
	
	angularAcc /= (sim->_currentTimeStep * oldCounterMax);

	if (angularAcc > StationaryAngAcc)
	{
		SyncOldState();

		return FALSE;
	}
/*	Derive().linearVel *= 0.9f;

	neV3 am = State().angularMom * 0.9f;

	SetAngMom(am);
*/
	return true;
}

/****************************************************************************
*
*	neRigidBody_::AddStackInfo
*
****************************************************************************/ 

neBool neRigidBody_::AddStackInfo(neRestRecord & rc)
{
	if (!stackInfo)
		return NewStackInfo(rc);

	neRigidBody_ * rb = (neRigidBody_ *) rc.GetOtherBody();

	ASSERT(stackInfo->stackHeader);

	if (stackInfo->isTerminator)
	{
		stackInfo->isTerminator = false;

		//ResetRestOnRecords();
	}

	AddRestContact(rc);
	
	if (rc.GetOtherCollisionBody())
	{
		return true;
	}

	if (!rb->stackInfo)
	{
		if (stackInfo->stackHeader->isHeaderX)
		{
			sim->stackHeaderX.Remove(stackInfo);

			sim->NewStackHeader(stackInfo);

			return rb->NewStackInfoTerminator(stackInfo->stackHeader);
		}
		else
		{
			return rb->NewStackInfoTerminator(stackInfo->stackHeader);
		}
	}
	
	neStackHeader * otherStackHeader = rb->stackInfo->stackHeader;

	ASSERT(otherStackHeader);

	if (otherStackHeader->isHeaderX)
	{
		if (stackInfo->stackHeader->isHeaderX)
		{
			sim->stackHeaderX.Remove(stackInfo);
			
			sim->stackHeaderX.Remove(rb->stackInfo);

			sim->NewStackHeader(stackInfo);

			stackInfo->stackHeader->Add(rb->stackInfo);

			//stackInfo->stackHeader->CheckHeader();
		}
		else
		{
			otherStackHeader->Remove(rb->stackInfo);

			stackInfo->stackHeader->Add(rb->stackInfo);

			//stackInfo->stackHeader->CheckHeader();
		}
	}
	else
	{
		if (stackInfo->stackHeader->isHeaderX)
		{
			stackInfo->stackHeader->Remove(stackInfo);

			otherStackHeader->Add(stackInfo);

			//otherStackHeader->CheckHeader();
		}
		else 
		{
			if (stackInfo->stackHeader != otherStackHeader)
			{
				// merge
				otherStackHeader->ChangeHeader(stackInfo->stackHeader);

				//stackInfo->stackHeader->CheckHeader();

 				sim->stackHeaderHeap.Dealloc(otherStackHeader);
			}
		}
	}
	return true;
}

void neRigidBody_::ResetRestOnRecords()
{
/*	for (s32 i = 0; i < NE_MAX_REST_ON; i++)
	{
		GetRestRecord(i).Init();
	}
*/
}

void  neRigidBody_::FreeStackInfo()
{
	ASSERT(stackInfo);

	sim->stackInfoHeap.Dealloc(stackInfo, 1);

	stackInfo = NULL;
}

neBool neRigidBody_::NewStackInfo(neRestRecord & rc)
{
	ASSERT(stackInfo == NULL);

	stackInfo = sim->stackInfoHeap.Alloc(1);

	if (!stackInfo)
	{
		if (sim->logLevel >= neSimulator::LOG_OUTPUT_LEVEL_ONE)
		{
			sprintf(sim->logBuffer,	MSG_STACK_BUFFER_FULL);
			sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
		}
		return false;
	}

	stackInfo->Init();

	{
		ASSERT(AllRestRecordInvalid());
	}
	ResetRestOnRecords();
	
	stackInfo->body = this;

	stackInfo->isTerminator = false;

	AddRestContact(rc);

	if (rc.GetOtherCollisionBody())
	{
		sim->stackHeaderX.Add(stackInfo);

		return true;
	}

	neRigidBody_ * rb = (neRigidBody_ *) rc.GetOtherBody();
	
	if (!rb->stackInfo)
	{
		sim->NewStackHeader(stackInfo);
		
		ASSERT(stackInfo->stackHeader);
		
		return rb->NewStackInfoTerminator(stackInfo->stackHeader);
	}

	neStackHeader * otherStackHeader = rb->stackInfo->stackHeader;

	ASSERT(otherStackHeader);

	if (otherStackHeader->isHeaderX)
	{
		sim->stackHeaderX.Remove(rb->stackInfo);

		sim->NewStackHeader(stackInfo);

		stackInfo->stackHeader->Add(rb->stackInfo);

		//stackInfo->stackHeader->CheckHeader();
	}
	else
	{
		otherStackHeader->Add(stackInfo);

		//otherStackHeader->CheckHeader();
	}
	if (!rb->_constraintHeader)
		rb->WakeUp();
	
	rb = rc.GetOtherRigidBody();
	
	if (rb && !rb->_constraintHeader)
	{
		rb->WakeUp();
	}
	return true;
}

neBool neRigidBody_::NewStackInfoTerminator(neStackHeader * header)
{
	ASSERT(stackInfo == NULL);

	stackInfo = sim->stackInfoHeap.Alloc(1);

	ASSERT(stackInfo);

	if (!stackInfo)
	{
		if (sim->logLevel >= neSimulator::LOG_OUTPUT_LEVEL_ONE)
		{
			sprintf(sim->logBuffer,	MSG_STACK_BUFFER_FULL);
			sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
		}
		return false;
	}
	stackInfo->Init();

	{
		ASSERT(AllRestRecordInvalid());
	}

	ResetRestOnRecords();

	stackInfo->body = this;

	stackInfo->isTerminator = true;

	header->Add(stackInfo);

	//header->CheckHeader();

	return true;
}

void neRigidBody_::MigrateNewHeader(neStackHeader * newHeader, neStackHeader * curHeader)
{
	ASSERT(stackInfo);
	ASSERT(stackInfo->stackHeader != newHeader);

	ASSERT(curHeader == stackInfo->stackHeader);

	neStackHeader * oldHeader = stackInfo->stackHeader;

	oldHeader->Remove(stackInfo);

	newHeader->Add(stackInfo);

//	oldHeader->CheckHeader();

	for (s32 i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		

		if (GetRestRecord(i).GetOtherBody())
		{
			neRigidBody_* otherBody = GetRestRecord(i).GetOtherRigidBody();

			if (otherBody)
			{
				if (!otherBody->stackInfo)
					continue;

				if (otherBody->stackInfo->stackHeader == newHeader)
					continue;

				if (!otherBody->stackInfo->isTerminator)
					otherBody->MigrateNewHeader(newHeader, curHeader);			
			}
		}
	}
}

neBool neRigidBody_::IsRestPointStillValid()
{
	if (!stackInfo || stackInfo->isTerminator)
		return false;
	
	s32 count = 0;

	switch (GetRestHull().htype)
	{
	case neRestHull::LINE:
		count = 2;
		//return false;
		break;

	case neRestHull::TRIANGLE:
		count = 3;
		break;
	default:
		return false;
	}

	for (s32 i = 0; i < count; i++)
	{
		neV3 world1, world2;

		s32 j = GetRestHull().indices[i];

		ASSERT(j < 3);
		ASSERT(j >= 0);

		ASSERT(GetRestRecord(j).IsValid());
		//if (GetRestRecord(j).rtype != neRestRecord::REST_ON_NOT_VALID)
		//	continue;

		world1 = State().b2w * GetRestRecord(j).bodyPoint;

		world2 = GetRestRecord(j).GetOtherBodyPoint();
		
		neV3 diff; diff = world1 - world2;

		//diff.RemoveComponent(sim->gravityVector); //remove the vertical component

		f32 d = diff.Dot(diff);

		//if (d > 0.02f) // 0.05 M or 5 cm
		if (d > 0.002f) // 0.05 M or 5 cm
		{
			GetRestRecord(j).SetInvalid();
			
			GetRestHull().htype = neRestHull::NONE;

			return false;
		}
	}
	return true;
}
/*
void neRigidBody_::ResolveRestingPenetration()
{
	s32 i;

	neBool s = false;

	for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (!GetRestRecord(i).IsValid())
			continue;

		if ((GetRestRecord(i).otherBody != sim->GetTerrainBody()) && 
			(!GetRestRecord(i).otherBody->IsValid() || !GetRestRecord(i).otherBody->isActive))
		{
			GetRestRecord(i).rtype = neRestRecord::REST_ON_NOT_VALID;

			GetRestRecord(i).otherBody = NULL;

			continue;
		}
		GetRestRecord(i).Update();

		if (GetRestRecord(i).normalDiff >= 0.0f)
			continue;

		if (neAbs(GetRestRecord(i).normalDiff) < -0.005f)
			continue;

//		if (neAbs(GetRestRecord(i).normalDiff) > 0.9f)
//			continue;

		s = true;

		CorrectPenetrationDrift2(i, true, 1);
	}
}
*/
#if 1

void neRigidBody_::CorrectRotation(f32 massOther, neV3 & pointThis, neV3 & pointDest, neV3 & pointDest2, s32 flag, s32 changeLast)
{
	neV3 dir1 = pointThis - GetPos();
	
	neV3 dir2 = pointDest - GetPos();

	f32 len1 = dir1.Length();

	if (neIsConsiderZero(len1) || !neIsFinite(len1))
		return;

	f32 len2 = dir2.Length();

	if (neIsConsiderZero(len2) || !neIsFinite(len2))
		return;

	dir1 *= (1.0f / len1);

	dir2 *= (1.0f / len2);

	f32 dot = dir1.Dot(dir2);

	if (neIsConsiderZero(neAbs(dot) - 1.0f))
		return;
	
	neV3 axis = dir1.Cross(dir2);

	axis.Normalize();

	f32 angle = acosf(dot);

	neQ quat; quat.Set(angle, axis);

	quat.Normalize();

	if (flag == 1)
	//if (1)
	{
		State().q = quat * State().q;

		UpdateDerive();
	}
	else
	{
		totalRot = quat * totalRot;

		rotCount++;

		if (changeLast)
		{
			totalLastRot = quat * totalLastRot;

			lastRotCount++;
		}
	}
}

#else

void neRigidBody_::CorrectRotation(f32 massOther, neV3 & pointThis, neV3 & pointDest, neV3 & pointDest2, s32 flag, s32 changeLast)
{
	neV3 p1 = pointThis - GetPos();
	
	neV3 p2 = pointDest2 - pointThis;

	f32 dot = p1.Dot(p2);

	neV3 cross = p1.Cross(p2);

	f32 len = cross.Length();

	if (!neIsFinite(len) || neIsConsiderZero(len))
	{
		return;
	}

	neV3 magic; magic = Derive().Iinv * cross;

	neV3 deltaR = (mass * massOther) / (mass + massOther) * magic;

	f32 angle = deltaR.Length();

	deltaR *= (1.0f / angle);

	//if (angle > 1.0f)
	//	angle = 1.0f;

	neQ quat; quat.Set(angle, deltaR);

	if (flag == 1)
	{
		State().q = quat * State().q;

		UpdateDerive();
	}
	else
	{
		totalRot = quat * totalRot;

		rotCount++;

		if (changeLast)
		{
			totalLastRot = quat * totalLastRot;

			lastRotCount++;
		}
	}
}

#endif
	

void neRigidBody_::CorrectPosition(neV3 & pointThis, neV3 & pointDest, s32 flag, s32 changeLast)
{
	neV3 shift = pointDest - pointThis;

	if (flag == 1)
	//if (1)
	{
		State().b2w.pos = GetPos() + shift;
		//SetPos(GetPos() + shift);
	}
	else
	{
		totalTrans += shift;

		transCount++;

		if (changeLast)
		{
			totalLastTrans += shift;

			lastTransCount++;
		}
	}
}
/*
void neRigidBody_::CorrectPenetrationRotation()
{
	if (!stackInfo)
		return;
	
	s32 i;

	s32 deepestIndex = -1;

	f32 deepest = 0.0f;

	for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (GetRestRecord(i).rtype == neRestRecord::REST_ON_NOT_VALID)
			continue;

		if ((GetRestRecord(i).otherBody != sim->GetTerrainBody()) && 
			(!GetRestRecord(i).otherBody->IsValid() || !GetRestRecord(i).otherBody->isActive))
		{
			GetRestRecord(i).rtype = neRestRecord::REST_ON_NOT_VALID;

			GetRestRecord(i).otherBody = NULL;

			continue;
		}
		GetRestRecord(i).Update();

		if (GetRestRecord(i).normalDiff >= 0.0f)
			continue;
		
		if (GetRestRecord(i).normalDiff >= -0.005f) // never move things out completely
			continue;

		if (neAbs(GetRestRecord(i).normalDiff) > 1.0f)
			continue;

		CorrectPenetrationRotation2(i, false);
	}
}

void neRigidBody_::CorrectPenetrationTranslation()
{
	if (!stackInfo)
		return;
	
	s32 i;

	s32 deepestIndex = -1;

	f32 deepest = 0.0f;

	for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (GetRestRecord(i).rtype == neRestRecord::REST_ON_NOT_VALID)
			continue;

		if ((GetRestRecord(i).otherBody != sim->GetTerrainBody()) && 
			(!GetRestRecord(i).otherBody->IsValid() || !GetRestRecord(i).otherBody->isActive))
		{
			GetRestRecord(i).rtype = neRestRecord::REST_ON_NOT_VALID;

			GetRestRecord(i).otherBody = NULL;

			continue;
		}
		GetRestRecord(i).Update();

		if (GetRestRecord(i).normalDiff >= 0.0f)
			continue;
		
		if (GetRestRecord(i).normalDiff >= -0.005f) // never move things out completely
			continue;

		if (neAbs(GetRestRecord(i).normalDiff) > 1.0f)
			continue;

		CorrectPenetrationTranslation2(i, false);
	}
}
*/
void neRigidBody_::CorrectPenetrationRotation2(s32 index, neBool slide)
{
/*	neRigidBodyBase * rb = GetRestRecord(index).otherBody;

	f32 effectiveMassA, effectiveMassB, mass2;

	neV3 dir = GetRestRecord(index).normalWorld;

	neV3 pointA = GetRestRecord(index).worldThisBody;

	neV3 pointB = GetRestRecord(index).worldOtherBody;

	f32 alinear, arotate;

	f32 blinear, brotate;

	effectiveMassA = TestImpulse(dir, pointA, alinear, arotate);

	if (rb->AsRigidBody())
	{
		effectiveMassB = rb->AsRigidBody()->TestImpulse(dir * -1.0f, pointB, blinear, brotate);

		mass2 = rb->AsRigidBody()->mass;
	}
	else
	{
		effectiveMassB = 0.0f;

		mass2 = 1.0e6f;
	}

	neV3 diff = pointA - pointB;

	f32 dot;

	slide = 0;

	if (slide)
	{
		dot = diff.Dot(GetRestRecord(index).normalWorld);

		diff = dot * GetRestRecord(index).normalWorld;
	}

	f32 scale = 0.5f;

	neV3 midA = pointA - (effectiveMassA) / (effectiveMassA + effectiveMassB) * diff * scale * arotate;

	neV3 midB = pointB + (effectiveMassB) / (effectiveMassA + effectiveMassB) * diff * scale * brotate;

	CorrectRotation(mass2, pointA, midA, pointB, 0, true);

	if (rb->AsRigidBody())
	{
		rb->AsRigidBody()->CorrectRotation(mass, pointB, midB, pointA, 0, true);
	}
*/
}

void neRigidBody_::CorrectPenetrationTranslation2(s32 index, neBool slide)
{
/*
	neRigidBodyBase * rb = GetRestRecord(index).otherBody;

	f32 effectiveMassA, effectiveMassB, mass2;

	neV3 pointA = State().b2w * GetRestRecord(index).bodyPoint;

	neV3 pointB = rb->GetB2W() * GetRestRecord(index).otherBodyPoint;

	neV3 dir = GetRestRecord(index).normalWorld;

	f32 alinear, arotate;

	f32 blinear, brotate;

	effectiveMassA = TestImpulse(dir, pointA, alinear, arotate);

	if (rb->AsRigidBody())
	{
		effectiveMassB = rb->AsRigidBody()->TestImpulse(dir * -1.0f, pointB, blinear, brotate);

		mass2 = rb->AsRigidBody()->mass;
	}
	else
	{
		effectiveMassB = 0.0f;

		mass2 = 1.0e6f;
	}

	neV3 diff = pointA - pointB;

	slide = 0;
	
	if (slide)
	{
		f32 dot = diff.Dot(GetRestRecord(index).normalWorld);

		diff = dot * GetRestRecord(index).normalWorld;
	}

	neV3 midA, midB;

	f32 scale = 0.5f;

	if (!slide)
		midA = pointA - (effectiveMassA) / (effectiveMassA + effectiveMassB) * diff * scale * alinear;
	else
		midA = pointA - (effectiveMassA) / (effectiveMassA + effectiveMassB) * diff * scale * alinear;

	midB = pointB + (effectiveMassB) / (effectiveMassA + effectiveMassB) * diff * scale * blinear;
	
	CorrectPosition(pointA, midA, 0, true);

	if (rb->AsRigidBody())
		rb->AsRigidBody()->CorrectPosition(pointB, midB, 0, true);
*/
}
#if 0
void neRigidBody_::CorrectPenetrationDrift()
{
	if (!stackInfo)
		return;
	
	s32 i;

	s32 deepestIndex = -1;

	f32 deepest = 0.0f;

	for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (GetRestRecord(i).rtype == neRestRecord::REST_ON_NOT_VALID)
			continue;

		if ((GetRestRecord(i).otherBody != sim->GetTerrainBody()) && 
			(!GetRestRecord(i).otherBody->IsValid() || !GetRestRecord(i).otherBody->isActive))
		{
			GetRestRecord(i).rtype = neRestRecord::REST_ON_NOT_VALID;

			GetRestRecord(i).otherBody = NULL;

			continue;
		}
		GetRestRecord(i).Update();

		if (GetRestRecord(i).normalDiff >= 0.0f)
			continue;
		
		if (GetRestRecord(i).normalDiff >= -0.005f) // never move things out completely
			continue;

		if (neAbs(GetRestRecord(i).normalDiff) > 2.0f)
			continue;

		CorrectPenetrationDrift2(i, false, 1);
/*
		if (GetRestRecord(i).normalDiff < deepest)
		{
			deepestIndex = i;

			deepest = GetRestRecord(i).normalDiff;
		}
*/	}
//	if (deepestIndex != -1)
//		CorrectPenetrationDrift2(deepestIndex, false);
}

void neRigidBody_::CorrectPenetrationDrift2(s32 index, neBool slide, s32 flag)
{
	// remember current position

//	neV3 posA = GetPos();

	neQ currentQ = State().q;

	currentQ.Invert();

	//neQ deltaQA = correctionInfo.lastQuat * currentQ;

//	neV3 posB;

	//neQ deltaQB;

/*	if (GetRestRecord(index).otherBody->AsRigidBody())
	{
		posB = GetRestRecord(index).otherBody->AsRigidBody()->GetPos();

		currentQ = GetRestRecord(index).otherBody->AsRigidBody()->State().q;

		currentQ.Invert();

		deltaQB =  GetRestRecord(index).otherBody->AsRigidBody()->correctionInfo.lastQuat * 
								currentQ;
	}
*/
	f32 err = 0.0f;
	
	neRigidBodyBase * rb = GetRestRecord(index).otherBody;

	f32 effectiveMassA, effectiveMassB, mass2;

	neV3 dir = GetRestRecord(index).normalWorld;

	neV3 pointA = GetRestRecord(index).worldThisBody;

	neV3 pointB = GetRestRecord(index).worldOtherBody;

	f32 alinear, arotate;

	f32 blinear, brotate;

	effectiveMassA = TestImpulse(dir, pointA, alinear, arotate);

	if (rb->AsRigidBody())
	{
		neV3 tmp = dir * -1.0f;

		effectiveMassB = rb->AsRigidBody()->TestImpulse(tmp, pointB, blinear, brotate);

		mass2 = rb->AsRigidBody()->mass;
	}
	else
	{
		effectiveMassB = 0.0f;

		mass2 = 1.0e6f;

		brotate = 0.0f;

		blinear = 0.0f;
	}
	//effectiveMassB = 0.0f;

	neV3 diff = pointA - pointB;

	f32 dot;

	if (slide)
	{
		dot = diff.Dot(GetRestRecord(index).normalWorld);

		diff = dot * GetRestRecord(index).normalWorld;
	}

	f32 scaleA = 0.8f;

	f32 scaleB = 0.1f;

	neV3 midA = pointA - (effectiveMassA) / (effectiveMassA + effectiveMassB) * diff * arotate * scaleA;

	neV3 midB = pointB + (effectiveMassB) / (effectiveMassA + effectiveMassB) * diff * brotate * scaleB;

	CorrectRotation(mass2, pointA, midA, pointB, flag, false);

	if (rb->AsRigidBody())
	{
		rb->AsRigidBody()->CorrectRotation(mass, pointB, midB, pointA, flag, false);
	}
	pointA = State().b2w * GetRestRecord(index).bodyPoint;

	pointB = rb->GetB2W() * GetRestRecord(index).otherBodyPoint;

	diff = pointA - pointB;

	if (slide)
	{
		dot = diff.Dot(GetRestRecord(index).normalWorld);

		diff = dot * GetRestRecord(index).normalWorld;
	}

	if (!slide)
		midA = pointA - (effectiveMassA) / (effectiveMassA + effectiveMassB) * diff * alinear * scaleA;
	else
		midA = pointA - (effectiveMassA) / (effectiveMassA + effectiveMassB) * diff * alinear * scaleA;

	midB = pointB + (effectiveMassB) / (effectiveMassA + effectiveMassB) * diff * blinear * scaleB;
	
	CorrectPosition(pointA, midA, flag, false);

	if (rb->AsRigidBody())
		rb->AsRigidBody()->CorrectPosition(pointB, midB, flag, false);
/*
	neV3 shifted = GetPos() - posA;

	correctionInfo.lastPos += shifted;

	correctionInfo.lastQuat = deltaQA * State().q;

	if (GetRestRecord(index).otherBody->AsRigidBody())
	{
		shifted = GetRestRecord(index).otherBody->AsRigidBody()->GetPos() - posB;

		GetRestRecord(index).otherBody->AsRigidBody()->correctionInfo.lastPos += shifted;

		GetRestRecord(index).otherBody->AsRigidBody()->correctionInfo.lastQuat = deltaQB * 
			GetRestRecord(index).otherBody->AsRigidBody()->State().q;
	}
*/
}
#endif

f32 neRigidBody_::TestImpulse(neV3 & dir, neV3 & pt, f32 & linear, f32 & angular)
{
	neV3 point = pt - GetPos();

	neV3 dv = dir * oneOnMass;

	neV3 da = point.Cross(dir);

	neV3 angVel = Derive().Iinv * da;

	neV3 vel;

	neV3 dav = angVel.Cross(point);

	vel = dv + dav;

	//neV3 dist = vel;// / sim->currentTimeStep;

	f32 linearSpeed = dv.Length();

	f32 angularSpeed = dav.Length();

	f32 totalSpeed = linearSpeed + angularSpeed;

	linear = linearSpeed / totalSpeed;

	angular = angularSpeed / totalSpeed;

	f32 ret = linearSpeed + angularSpeed;//dist.Length();

	if (neIsFinite(ret))
		return ret;
	
	return 0.0f;
}

void neRigidBody_::ShiftPosition(const neV3 & delta)
{
	neConstraintHeader * header = GetConstraintHeader();

	SetPos(GetPos() + delta);
}

neBool neRigidBody_::AllRestRecordInvalid()	
{
	for (s32 i = 0; i < 3; i++)
	{
		if (GetRestRecord(i).IsValid())
			return false;
	}
	return true;
}
