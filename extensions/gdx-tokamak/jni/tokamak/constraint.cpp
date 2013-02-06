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

#include "stdio.h"

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

//extern void DrawLine(const neV3 & colour, neV3 * startpoint, s32 count);

#pragma inline_recursion( on )
#pragma inline_depth( 50 )

f32 AngleBetweenVector(const neV3 & va, const neV3 & vb, const neV3 & normal)
{
	// rotation from va to vb

	neV3 ra = va;
	
	ra.RemoveComponent(normal);

	ra.Normalize();

	neV3 rb = vb;
	
	rb.RemoveComponent(normal);

	rb.Normalize();

	f32 dot = ra.Dot(rb);

	if (neIsConsiderZero(dot - 1.0f))
	{
		return 0.0f;
	}
	if (neIsConsiderZero(dot + 1.0f))
	{
		return NE_PI;
	}
	neV3 cross = ra.Cross(rb);

	f32 dot2 = cross.Dot(normal);

	if (dot2 > 0.0f)
	{
		return acosf(dot);
	}
	else
	{
		return 2.0f * NE_PI - acosf(dot);
	}
}

neQ GetQuatRotateTo(const neV3 & fromV, const neV3 & toV, const neV3 & axis)
{
	neQ ret;

	f32 dot = fromV.Dot(toV);

	if (neIsConsiderZero(dot - 1.0f))
	{
		ret.Identity();
	}
	else if (neIsConsiderZero(dot + 1.0f))
	{
		ret.Set(NE_PI, axis);
	}
	else
	{
		neV3 cross = fromV.Cross(toV);

		cross.Normalize();

		f32 angle;

		angle = acosf(dot);

		ret.Set(angle, cross);
	}

	return ret;
}

f32 DistanceFromPlane(const neV3 & point, const neV3 & normal, const neV3 & pointOnPlane, neV3 & projected)
{
	neV3 diff = point - pointOnPlane;

	f32 dot = diff.Dot(normal);

	projected = point - normal * dot;

	return dot;
}

void _neConstraint::GeneratePointsFromFrame()
{
	f32 hingeHalfLength = jointLength * 0.5f;

	switch (type)
	{
	case neJoint::NE_JOINT_BALLSOCKET:
		
		cpointsA[0].PtBody() = frameA.pos;

		cpointsB[0].PtBody() = frameB.pos;

		break;

	case neJoint::NE_JOINT_HINGE:
		
		cpointsA[0].PtBody() = frameA.pos + frameA.rot[1] * hingeHalfLength;

		cpointsA[1].PtBody() = frameA.pos - frameA.rot[1] * hingeHalfLength;

		cpointsB[0].PtBody() = frameB.pos + frameB.rot[1] * hingeHalfLength;

		cpointsB[1].PtBody() = frameB.pos - frameB.rot[1] * hingeHalfLength;

		break;

	case neJoint::NE_JOINT_SLIDE:
		
		cpointsA[0].PtBody() = frameA.pos + frameA.rot[1] * hingeHalfLength;

		cpointsA[1].PtBody() = frameA.pos - frameA.rot[1] * hingeHalfLength;

		break;

	default:
		ASSERT(0);
		break;
	}
}

void _neConstraint::Enable(neBool yes)
{
	if (!bodyA)
		return;

	if (enable && yes)
		return;

	if (!enable && !yes)
		return;

	if (alreadySetup)
	{
		enable = yes;

		if (bodyA->status == neRigidBody_::NE_RBSTATUS_IDLE)
		{
			bodyA->WakeUp();
		}
		if (bodyB && bodyB->AsRigidBody())
		{
			if (bodyB->AsRigidBody()->status == neRigidBody_::NE_RBSTATUS_IDLE)
			{
				bodyB->AsRigidBody()->WakeUp();
			}
		}
	}
	else
	{
		if (yes)
		{
			alreadySetup = true;
			
			GeneratePointsFromFrame();

			AddToRigidBody();

			enable = true;

			if (bodyA->status == neRigidBody_::NE_RBSTATUS_IDLE)
			{
				bodyA->WakeUp();
			}
			if (bodyB && bodyB->AsRigidBody())
			{
				if (bodyB->AsRigidBody()->status == neRigidBody_::NE_RBSTATUS_IDLE)
				{
					bodyB->AsRigidBody()->WakeUp();
				}
			}
		}
		else
		{
			enable = false;
		}
	}
}

void _neConstraint::AddToRigidBody()
{
	neConstraintHeader * header = NULL;

	neRigidBody_ * rb = bodyB ? bodyB->AsRigidBody() : NULL;

	neCollisionBody_ * cb =bodyB ? bodyB->AsCollisionBody() : NULL;

	if (bodyA->GetConstraintHeader())
	{
		header = bodyA->GetConstraintHeader();
	}

	if (!bodyB)
	{
		if (header == NULL)
		{
			header = sim->NewConstraintHeader();

			if (!header)
			{
				if (bodyA->sim->logLevel >= neSimulator::LOG_OUTPUT_LEVEL_ONE)
				{

					sprintf(bodyA->sim->logBuffer, MSG_CONSTRAINT_HEADER_FULL);
					bodyA->sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
				}
				return;
			}

			header->Add(this);

			bodyA->SetConstraintHeader(header);

			header->bodies.Add(&bodyA->constraintHeaderItem);
		}
		else
		{
			header->Add(this);
		}
	}
	else
	{	
		neConstraintHeader * bHeader = NULL;

		if (cb && cb->GetConstraintHeader())
		{
			bHeader = cb->GetConstraintHeader();
		}
		else if (rb && rb->GetConstraintHeader())
		{
			bHeader = rb->GetConstraintHeader();
		}
		if (bHeader)
		{
			if (header == NULL)
			{
				bodyA->SetConstraintHeader(bHeader);

				//header = rb->GetConstraintHeader();

				bHeader->Add(this);

				bHeader->bodies.Add(&bodyA->constraintHeaderItem);
			}
			else
			{
				//if (rb->GetConstraintHeader() == 
				//	bodyA->GetConstraintHeader())
				if (bHeader == header)
				{
					header->Add(this);
				}
				else
				{
					//merge
					//ASSERT(0);
					header->Add(this);

					while (bHeader->head != NULL)
					{
						_neConstraint * c = bHeader->head;

						bHeader->Remove(bHeader->head);

						header->Add(c);
					}
					neBodyHandle * bodyHandle = bHeader->bodies.GetHead();

					while (bodyHandle)
					{
						neBodyHandle * bBodyHandle = bodyHandle;
						
						bodyHandle = bodyHandle->next;

						bBodyHandle->Remove();

						header->bodies.Add(bBodyHandle);

						bBodyHandle->thing->SetConstraintHeader(header);
					}
					sim->constraintHeaders.Dealloc(bHeader);
				}
			}
		}
		else
		{
			if (header == NULL)
			{
				//create new header
				header = sim->NewConstraintHeader();

				if (!header)
				{
					if (bodyA->sim->logLevel >= neSimulator::LOG_OUTPUT_LEVEL_ONE)
					{
						sprintf(bodyA->sim->logBuffer, MSG_CONSTRAINT_HEADER_FULL);
						bodyA->sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
					}
					return;
				}
				
				bodyA->SetConstraintHeader(header);

				header->bodies.Add(&bodyA->constraintHeaderItem);

				header->Add(this);

				bodyB->SetConstraintHeader(header);

				header->bodies.Add(&bodyB->constraintHeaderItem);
			}
			else
			{
				header->Add(this);

				bodyB->SetConstraintHeader(header);

				header->bodies.Add(&bodyB->constraintHeaderItem);
			}
		}
	}
	bodyA->GetConstraintHeader()->flag |= neConstraintHeader::FLAG_NEED_SETUP;
}

void _neConstraint::InfiniteMassB(neBool yes)
{
	infiniteMassB = yes;
}

void _neConstraint::Reset()
{
	bodyA = NULL;
	bodyB = NULL;
	enable = false;
	infiniteMassB = false;
	type = neJoint::NE_JOINT_BALLSOCKET;
	accuracy = -1.0f;//0.5f;
	sim = NULL;
	controllers = NULL;
	controllerCursor = NULL;
	frameA.rot.SetIdentity();
	frameA.pos.SetZero();
	frameB.rot.SetIdentity();
	frameB.pos.SetZero();
	jointLength = 1.0f;
	iteration = -1;
	jointDampingFactor = 0.0f;
	
	limitStates[0].Reset(this);
	limitStates[1].Reset(this, 1);

	motors[0].Reset();
	motors[1].Reset();

	alreadySetup = false;

/*	limitConstraintCount = 0;
	for (s32 i = 0; i < 2; i++)
		for (s32 j = 0; j < 2; j++)
			limitRigidConstraint[i][j] = NULL;

	enableLimitMiniConstraint = false;

	applyLimitImpulse = false;
*/
	bodyAHandle.thing = this;

	bodyBHandle.thing = this;

//	cres[0] = cres[1] = NULL;
}

void _neConstraint::SetType(neJoint::ConstraintType t)
{
	switch (t)	
	{
	case neJoint::NE_JOINT_BALLSOCKET:
		type = t;
		pointCount = 1;
		break;
	case neJoint::NE_JOINT_HINGE:
		type = t;
		pointCount = 2;
		break;
	case neJoint::NE_JOINT_SLIDE:
		type = t;
		pointCount = 2;
		break;
	}
}

neController * _neConstraint::AddController(neJointControllerCallback * jc, s32 period)
{
	if (!jc)
		return NULL;

	neController * c = sim->controllerHeap.Alloc(1);

	if (!c)
	{
		sprintf(sim->logBuffer, MSG_CONTROLLER_FULL);
		
		sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

		return NULL;
	}
	if (!controllers)
	{
		controllers = c;
	}
	else
	{
		((neControllerItem *)controllers)->Append((neControllerItem *)c);		
	}
	c->rb = NULL;

	c->constraint = this;

	c->rbc = NULL;

	c->jc = jc;

	c->period = period;

	c->count = period;

	c->forceA.SetZero();

	c->torqueA.SetZero();

	c->forceB.SetZero();

	c->torqueB.SetZero();

	return c;
}

void _neConstraint::BeginIterateController()
{
	controllerCursor = (neControllerItem *)controllers;
}

neController * _neConstraint::GetNextController()
{
	if (!controllerCursor)
		return NULL;

	neController * ret = (neController *)controllerCursor;

	controllerCursor = controllerCursor->next;

	return ret;
}

void _neConstraint::UpdateController()
{
	neControllerItem * ci = (neControllerItem *)controllers;

	while (ci)
	{
		neController * con = (neController *) ci;

		ci = ci->next;

		if (con->count == 0)
		{
			ASSERT(con->jc);
			
			con->jc->ConstraintControllerCallback((neJointController*)con, sim->_currentTimeStep);
			
			con->count = con->period;
		}
		else
		{
			con->count--;
		}
		con->constraint->bodyA->cforce += con->forceA;

		con->constraint->bodyA->ctorque += con->torqueA;

		if (con->constraint->bodyB)
		{
			neRigidBody_* bb = (neRigidBody_*)con->constraint->bodyB->AsRigidBody();

			if (bb)
			{
				bb->cforce += con->forceB;

				bb->ctorque += con->torqueB;
			}
		}
	}
}

void _neConstraint::UpdateConstraintPoint()
{
	ASSERT(type == neJoint::NE_JOINT_BALLSOCKET ||
			type == neJoint::NE_JOINT_HINGE ||
			type == neJoint::NE_JOINT_SLIDE);

	ASSERT(bodyA);

	neRigidBody_* rbodyB = NULL;

	neCollisionBody_* cbodyB = NULL;

	if (bodyB)
	{
		rbodyB = bodyB->AsRigidBody();

		cbodyB = bodyB->AsCollisionBody();
	}

	for (s32 i = 0; i < pointCount; i++)
	{
		if (type == neJoint::NE_JOINT_BALLSOCKET || 
			type == neJoint::NE_JOINT_HINGE)
		{
			cpointsA[i].PtWorld() = bodyA->State().b2w * cpointsA[i].PtBody();

			if (rbodyB)
			{
				cpointsB[i].PtWorld() = rbodyB->State().b2w * cpointsB[i].PtBody();
			}
			else if (cbodyB)
			{
				cpointsB[i].PtWorld() = cbodyB->b2w * cpointsB[i].PtBody();
			}
			else
			{
				cpointsB[i].PtWorld() = cpointsB[i].PtBody();
			}
		}
		else if (type == neJoint::NE_JOINT_SLIDE)
		{
			cpointsA[i].PtWorld() = bodyA->State().b2w * cpointsA[i].PtBody();
		}

		ASSERT(cpointsA[i].PtWorld().IsFinite());

		ASSERT(cpointsB[i].PtWorld().IsFinite());
	}
}

/****************************************************************************
*
*	neConstrain::FindGreatest
*
****************************************************************************/ 

void _neConstraint::FindGreatest()
{
	neRigidBody_ * rbodyB = NULL;
	
	neCollisionBody_ * cbodyB = NULL;

	if (bodyB)
	{
		rbodyB = bodyB->AsRigidBody();

		cbodyB = bodyB->AsCollisionBody();
	}
	switch (type)
	{
	case neJoint::NE_JOINT_BALLSOCKET:
	case neJoint::NE_JOINT_HINGE:
		{

			for (s32 i = 0; i < pointCount; i++)
			{
				neCollisionResult * cresult = bodyA->sim->cresultHeap.Alloc(0);

				cresult->contactA = cpointsA[i].PtWorld() - bodyA->GetPos();

				if (rbodyB)
				{
					cresult->contactB = cpointsB[i].PtWorld() - rbodyB->GetPos();
				}
				else
				{
					cresult->contactB = cpointsB[i].PtWorld();

					cresult->contactBWorld = cpointsB[i].PtWorld();
				}

				cresult->contactAWorld = cpointsA[i].PtWorld() - cpointsB[i].PtWorld();
				cresult->depth = cresult->contactAWorld.Length();
				cresult->bodyA = bodyA;
				cresult->bodyB = bodyB;
				//cresult->relativeSpeed = relSpeed;
				cresult->impulseType = IMPULSE_CONSTRAINT;
				cresult->PrepareForSolver();
			}
		}	
		if (limitStates[0].enableLimit || 
			limitStates[1].enableLimit)
		{
			CheckLimit();
		}
		break;

	case neJoint::NE_JOINT_SLIDE:
		{
			for (s32 i = 0; i < pointCount; i++)
			{
				neCollisionResult * cresult = bodyA->sim->cresultHeap.Alloc(0);

				cresult->contactA = cpointsA[i].PtWorld() - bodyA->GetPos();

				neV3 diff = cpointsA[i].PtWorld() - frameBWorld.pos;

				f32 dot = diff.Dot(frameBWorld.rot[1]);

				cpointsB[i].PtWorld() = dot * frameBWorld.rot[1] + frameBWorld.pos;

				if (rbodyB)
				{
					cresult->contactB = cpointsB[i].PtWorld() - rbodyB->GetPos();
				}
				else
				{
					cresult->contactB = cpointsB[i].PtWorld();

					cresult->contactBWorld = cpointsB[i].PtWorld();
				}
				cresult->contactAWorld = cpointsA[i].PtWorld() - cpointsB[i].PtWorld();
				cresult->finalRelativeSpeed = cresult->contactAWorld.Length();
				cresult->contactBWorld = frameBWorld.rot[1];
				cresult->bodyA = bodyA;
				cresult->bodyB = bodyB;
				cresult->impulseType = IMPULSE_SLIDER;
				cresult->PrepareForSolver();
			}
			if (limitStates[0].enableLimit || 
				limitStates[1].enableLimit)
			{
				CheckLimit();
			}
		}
		break;
	
	default:
		break;
	}
	if (bodyA->stackInfo)
	{
		bodyA->AddContactConstraint();
	}
	if (rbodyB && rbodyB->stackInfo)
	{
		rbodyB->AddContactConstraint();
	}
	if (motors[0].enable)
		motors[0].PrepareForSolver(this);

	if (motors[1].enable)
		motors[1].PrepareForSolver(this);
}
/*
void _neConstraint::SetupLimitCollision()
{
	neRigidBody_ * rbodyB = NULL;
	
	neCollisionBody_ * cbodyB = NULL;

	if (bodyB)
	{
		rbodyB = bodyB->AsRigidBody();

		cbodyB = bodyB->AsCollisionBody();
	}

	frameAWorld = bodyA->State().b2w * frameA;

	frameBWorld = GetFrameBWorld();
}
*/
void _neConstraint::DrawCPointLine()
{
#if 0
	neV3 points[2];
	neV3 color;

//	if (limitConstraintCount == 1)
//		return;

	s32 i;
	for (i = 0; i < pointCount; i++)
	{
		points[0] = limitPt[i][0].PtWorld();

		points[1] = limitPt[i][1].PtWorld();

		DrawLine(color, points, 2);	
	}

	for (i = 0; i < pointCount; i++)
	{
		for (s32 j = 0; j < limitConstraintCount; j++)
		{
			neMiniConstraintItem * item = (neMiniConstraintItem *)limitRigidConstraint[i][j];

			while (item)
			{
				neMiniConstraint * mc = (neMiniConstraint *) item;

				item = item->next;

				points[0] = *mc->pointA;

				points[1] = *mc->pointB;

				DrawLine(color, points, 2);	
			}
		}
	}
#endif
}

neT3 _neConstraint::GetFrameBWorld()
{
	neT3 ret;

	neRigidBody_ * rb = NULL;

	if (bodyB)
	{
		rb = bodyB->AsRigidBody();

		if (rb)
		{
			ret = rb->State().b2w * frameB;
		}
		else
		{
			ret = bodyB->AsCollisionBody()->b2w * frameB;
		}
	}
	else
	{
		ret = frameB;
	}
	return ret;
}

neT3 _neConstraint::GetBodyB2W()
{
	neT3 ret;

	neRigidBody_ * rb = NULL;

	if (bodyB)
	{
		rb = bodyB->AsRigidBody();

		if (rb)
		{
			ret = rb->State().b2w;
		}
		else
		{
			ret = bodyB->AsCollisionBody()->b2w;
		}
	}
	else
	{
		ret.SetIdentity();
	}
	return ret;
}

void _neConstraint::CheckLimit()
{
	if (type == neJoint::NE_JOINT_BALLSOCKET || type == neJoint::NE_JOINT_HINGE)
	{
		if (limitStates[0].enableLimit)
			limitStates[0].CheckLimitPrimary();

		if (type == neJoint::NE_JOINT_BALLSOCKET && 
			limitStates[1].enableLimit)
		{
			limitStates[1].CheckLimitSecondary();
		}
	}
	else if (type == neJoint::NE_JOINT_SLIDE)
	{
		if (limitStates[0].enableLimit)
			limitStates[0].CheckLimitPrimarySlider();
//		if (limitStates[1].enableLimit)
//			limitStates[1].CheckLimitSecondarySlider();
	}
}

void _neConstraint::UpdateCurrentPosition()
{
	neRigidBody_ * rb = NULL;

	if (bodyB && bodyB->AsRigidBody())
	{
		rb = bodyB->AsRigidBody();
	}
	frameAWorld = bodyA->State().b2w * frameA;

	frameBWorld = GetFrameBWorld();

	f32 dot;

	if (type == neJoint::NE_JOINT_HINGE)
	{
		limitStates[0].limitAxis = frameAWorld.rot[1] + frameBWorld.rot[1];

		limitStates[0].limitAxis.Normalize();

		f32 dot = frameAWorld.rot[0].Dot(frameBWorld.rot[0]);

		if (neIsConsiderZero(dot - 1.0f)) // dot = 1
		{
			pos = 0.0f;

			pos2 = 0.0f;
		}
		else if (neIsConsiderZero(dot + 1.0f)) // dot = -1
		{
			pos = NE_PI;

			pos2 = -NE_PI;
		}
		else
		{
			neV3 cross = frameBWorld.rot[0].Cross(frameAWorld.rot[0]);

			f32 len = cross.Length();

			cross *= (1.0f / len);

			f32 dot2 = limitStates[0].limitAxis.Dot(cross);

			if (dot2 > 0.0f)
			{
				pos = acosf(dot);

				pos2 = pos;
			}
			else
			{
				f32 t = acosf(dot);

				pos = 2.0f * NE_PI - t;

				pos2 = -t; 
			}
		}
	}
	else if (type == neJoint::NE_JOINT_BALLSOCKET)
	{
		dot = frameAWorld.rot[0].Dot(frameBWorld.rot[0]);

		if (neIsConsiderZero(dot - 1.0f)) // dot = 1
		{
			pos = 0.0f;
			
			limitStates[0].limitAxis = frameAWorld.rot[1];
		}
		else if (neIsConsiderZero(dot + 1.0f)) // = -1
		{
			pos = NE_PI;

			limitStates[0].limitAxis = frameAWorld.rot[1];
		}
		else
		{
			limitStates[0].limitAxis = frameBWorld.rot[0].Cross(frameAWorld.rot[0]);

			f32 len = limitStates[0].limitAxis.Length();

			ASSERT(!neIsConsiderZero(len));
	
			limitStates[0].limitAxis *= (1.0f / len);

			pos = acosf(dot);
		}
	}
	else if (type == neJoint::NE_JOINT_SLIDE)
	{
		limitStates[0].limitAxis = frameAWorld.rot[1] + frameBWorld.rot[1];

		limitStates[0].limitAxis.Normalize();

		neV3 diff = frameAWorld.pos - frameBWorld.pos;

		pos = diff.Dot(frameBWorld.rot[1]);
	}
	else
	{
		return;
	}
}

#if 1

void neLimitState::CheckLimitSecondary()
{
	ASSERT(limitType == 1);

	applyLimitImpulse = false;

	f32 dot;

	f32 ang = -constr->pos;

	neQ quat;

	quat.Set(ang, constr->limitStates[0].limitAxis);

	neV3 zaAdjust;

	zaAdjust = quat * constr->frameAWorld.rot[2];

	f32 angle;

	dot = constr->frameBWorld.rot[2].Dot(zaAdjust);

	if (neIsConsiderZero(dot - 1.0f)) // dot == 1
	{
		angle = 0.0f;
	}
	else if (neIsConsiderZero(dot + 1.0f))
	{
		angle = NE_PI;
	}
	else
	{
		neV3 cross = constr->frameBWorld.rot[2].Cross(zaAdjust);

		f32 len = cross.Length();

		cross *= (1.0f / len);

		f32 dot2 = constr->frameBWorld.rot[0].Dot(cross);

		if (dot2 >= 0.0f)
		{
			angle = acosf(dot);
		}
		else
		{
			f32 t = acosf(dot);

			angle = - t;
		}
	}

	if (angle > lowerLimit)
	{
		limitAngularPenetration = (angle - lowerLimit);

		limitAngularPenetration2 = limitAngularPenetration;

		applyLimitImpulse = true;
	}
	else if (angle < -lowerLimit)
	{
		limitAngularPenetration = (angle + lowerLimit);

		limitAngularPenetration2 = limitAngularPenetration;

		applyLimitImpulse = true;
	}
	else
	{
		return;
	}
	neCollisionResult * cresult = constr->bodyA->sim->cresultHeap.Alloc(0);

	cresult->bodyA = constr->bodyA;

	cresult->bodyB = constr->bodyB;

	cresult->impulseType = IMPULSE_ANGULAR_LIMIT_SECONDARY;

	cresult->contactBBody = constr->frameBWorld.rot[0];

	cresult->contactABody = constr->frameAWorld.rot[0];

	cresult->depth = limitAngularPenetration;

	cresult->impulseScale = limitAngularPenetration2;

	cresult->k = quat.BuildMatrix3();

	//cresult->k = constr->bodyB->GetB2W().rot * cresult->k;

	cresult->PrepareForSolver();
}

#else

void neLimitState::CheckLimitSecondary()
{
	ASSERT(limitType == 1);

	applyLimitImpulse = false;

	f32 dot = constr->frameAWorld.rot[1].Dot(constr->frameBWorld.rot[1]);

	neV3 target;

	f32 ang;

	if (dot >= 0.0f)
	{
		target = constr->frameAWorld.rot[0];

		ang = -constr->pos;
	}
	else
	{
		applyLimitImpulse = false;

		return;
	}

	neQ quat;

	quat.Set(ang, constr->limitStates[0].limitAxis);

	neV3 zaAdjust;

	zaAdjust = quat * constr->frameAWorld.rot[2];

	f32 angle;

	dot = constr->frameBWorld.rot[2].Dot(zaAdjust);

	if (neIsConsiderZero(dot - 1.0f)) // dot == 1
	{
		angle = 0.0f;
	}
	else if (neIsConsiderZero(dot + 1.0f))
	{
		angle = NE_PI;
	}
	else
	{
		neV3 cross = constr->frameBWorld.rot[2].Cross(zaAdjust);

		f32 len = cross.Length();

		cross *= (1.0f / len);

		f32 dot2 = constr->frameAWorld.rot[0].Dot(cross);

		if (dot2 >= 0.0f)
		{
			angle = acosf(dot);
		}
		else
		{
			f32 t = acosf(dot);

			angle = - t;
		}
	}

	if (angle > lowerLimit)
	{
		limitAngularPenetration = (angle - lowerLimit);

		limitAngularPenetration2 = limitAngularPenetration;

		applyLimitImpulse = true;
	}
	else if (angle < -lowerLimit)
	{
		limitAngularPenetration = (angle + lowerLimit);

		limitAngularPenetration2 = limitAngularPenetration;

		applyLimitImpulse = true;
	}
	else
	{
		return;
	}
	neCollisionResult * cresult = constr->bodyA->sim->cresultHeap.Alloc(0);

	cresult->bodyA = constr->bodyA;

	cresult->bodyB = constr->bodyB;

	cresult->impulseType = IMPULSE_ANGULAR_LIMIT_SECONDARY;

	cresult->contactBBody = constr->frameBWorld.rot[0];

	cresult->contactABody = constr->frameAWorld.rot[0];

	cresult->depth = limitAngularPenetration;

	cresult->impulseScale = limitAngularPenetration2;

	cresult->PrepareForSolver();
}
#endif

void neLimitState::CheckLimitPrimary()
{
	applyLimitImpulse = false;

//	neRigidBody_ * rb = NULL;

//	if (constr->bodyB && constr->bodyB->AsRigidBody())
//	{
//		rb = constr->bodyB->AsRigidBody();
//	}

	ASSERT(neIsFinite(constr->pos));

	ASSERT(neIsFinite(constr->pos2));

	f32 rotation;

	if (lowerLimit > 0.0f || constr->type == neJoint::NE_JOINT_BALLSOCKET)
	{
		//if (upperLimit >= 0.0f) //upperlimit must also be positive
		{
			if (constr->pos < lowerLimit)
			{
				lowerLimitOn = true;

				rotation = -(lowerLimit - constr->pos);
			}
			else if (constr->pos > upperLimit)
			{
				lowerLimitOn = false;

				rotation = constr->pos - upperLimit;
			}
			else
			{
				return;
			}
		}
	}
	else
	{
		if (upperLimit >= 0.0f)
		{
			if (constr->pos2 < lowerLimit)
			{
				lowerLimitOn = true;

				rotation = -(lowerLimit - constr->pos2);
			}
			else if (constr->pos2 > upperLimit)
			{
				lowerLimitOn = false;

				rotation = constr->pos2 - upperLimit;
			}
			else
			{
				return;
			}
		}
		else
		{
			if (constr->pos2 < lowerLimit)
			{
				lowerLimitOn = true;

				rotation = -(lowerLimit - constr->pos2);
			}
			else if (constr->pos2 > upperLimit)
			{
				lowerLimitOn = false;

				rotation = constr->pos2 - upperLimit;
			}
			else
			{
				return;
			}
		}
	}
	limitAngularPenetration = rotation;

	applyLimitImpulse = true;

	neCollisionResult * cresult = constr->bodyA->sim->cresultHeap.Alloc(0);

	cresult->bodyA = constr->bodyA;

	cresult->bodyB = constr->bodyB;

	cresult->impulseType = IMPULSE_ANGULAR_LIMIT_PRIMARY;

	cresult->contactBBody = limitAxis;

	cresult->depth = limitAngularPenetration;

	cresult->convexA = (TConvex *)constr;

	cresult->PrepareForSolver();
}

void neLimitState::CheckLimitPrimarySlider()
{
	neV3 diff = constr->frameAWorld.pos - constr->frameBWorld.pos;

	f32 dot = diff.Dot(constr->frameBWorld.rot[1]);

	f32 depth, sign;

	if (dot > upperLimit)
	{
		depth = dot - upperLimit;

		sign = 1.0f;

		lowerLimitOn = false;

		applyLimitImpulse = true;
	}
	else if (dot < lowerLimit)
	{
		depth = lowerLimit - dot;

		sign = -1.0f;

		lowerLimitOn = true;

		applyLimitImpulse = true;
	}
	else
	{
		applyLimitImpulse = false;

		return;
	}

	ASSERT(depth >= 0.0f);

	neCollisionResult * cresult = constr->bodyA->sim->cresultHeap.Alloc(0);

	if (depth > 0.05f)
		depth = 0.05f;

	cresult->depth = depth;

	cresult->bodyA = constr->bodyA;

	cresult->bodyB = constr->bodyB;

	cresult->contactA = constr->frameAWorld.pos - constr->bodyA->State().b2w.pos;

	if (constr->bodyB)
	{
		cresult->contactB = constr->frameBWorld.pos - constr->bodyB->GetB2W().pos;
	}
	else
	{
		cresult->contactB = constr->frameBWorld.pos;
	}
	cresult->impulseType = IMPULSE_SLIDER_LIMIT_PRIMARY;

	cresult->contactBWorld = constr->frameBWorld.rot[1] * sign;

	cresult->convexA = (TConvex *)constr;

	cresult->PrepareForSolver();
}

void neConstraintHeader::AddToSolver(f32 & epsilon, s32 & iteration)
{
	_neConstraint * constraint = head;

	s32 i = 0;

	while (constraint)
	{
		if (constraint->enable)
		{
			constraint->UpdateCurrentPosition();

			constraint->UpdateConstraintPoint();

			constraint->FindGreatest();

			//constraint->DrawCPointLine();

			if (epsilon == -1.0f || (constraint->accuracy > 0.0f && constraint->accuracy < epsilon))
				epsilon = constraint->accuracy;

			if (constraint->iteration > iteration)
				iteration = constraint->iteration;
		}
		neFreeListItem<_neConstraint> * item = (neFreeListItem<_neConstraint> *)constraint;

		constraint = (_neConstraint*)(item->next);

		i++;
	}

	solved = true;
}

void _neConstraint::ApplyDamping()
{
	// rel vel between a and b, as if b was hold still

	neRigidBody_ * rb = NULL;

	if (bodyB && bodyB->AsRigidBody())
	{
		rb = bodyB->AsRigidBody();
	}

	neV3 relVel;
	bool isLinear = false;

	switch (type)
	{
	case neJoint::NE_JOINT_BALLSOCKET:
		//damp rotation

		relVel = bodyA->Derive().angularVel;

		if (rb)
		{
			relVel -= rb->Derive().angularVel;
		}
		
		break;

	case neJoint::NE_JOINT_HINGE:
		//damp rotation only along the axis of the hinge joint
		{
			frameAWorld = bodyA->State().b2w * frameA;

			frameBWorld = GetFrameBWorld();

			neV3 jointAxis = frameAWorld.rot[1] + frameBWorld.rot[1];

			jointAxis.Normalize();

			relVel = bodyA->Derive().angularVel.Dot(jointAxis) * jointAxis;

			if (rb)
			{
				relVel -= (rb->Derive().angularVel.Dot(jointAxis) * jointAxis);
			}
		}
		break;

	case neJoint::NE_JOINT_SLIDE:
		break;
	default:
		ASSERT(0);
		break;
	}
	if (isLinear)
	{
		bodyA->totalForce -= (relVel * jointDampingFactor);
		
		if (rb)
			rb->totalForce += (relVel * jointDampingFactor);
	}
	else
	{
		bodyA->totalTorque -= (relVel * jointDampingFactor);

		if (rb)
			rb->totalTorque += (relVel * jointDampingFactor);
	}
}

void neConstraintHeader::TraverseApplyConstraint(neBool doCheckSleep)
{
	neBodyHandle * bodyHandle = bodies.GetHead();

	while (bodyHandle)
	{
		if (!bodyHandle->thing->AsRigidBody())
		{
			bodyHandle = bodyHandle->next;

			continue;
		}

		bodyHandle->thing->AsRigidBody()->needRecalc = false;

		neRigidBody_* rb = bodyHandle->thing->AsRigidBody();
/*
		if (!doCheckSleep && rb->status == neRigidBody_::NE_RBSTATUS_IDLE)
		{
			bodyHandle = bodyHandle->next;

			continue;
		}
*/		if (rb->impulseCount > 0)
		{
			rb->ApplyLinearConstraint();

		}
		if (rb->twistCount > 0)
		{
			rb->ApplyAngularConstraint();
		}
/*		if (doCheckSleep && rb->status == neRigidBody_::NE_RBSTATUS_IDLE)
		{
			rb->ConstraintDoSleepCheck();
		}
*/		bodyHandle = bodyHandle->next;
	}// while (bodyHanle)   next body in the constraint
}

neBool neConstraintHeader::StationaryCheck()
{
	neBool allStationary = true;

	neBodyHandle * bodyHandle = bodies.GetHead();

	while (bodyHandle)
	{
		neRigidBody_* rb = bodyHandle->thing->AsRigidBody();

		if (!rb)
		{
			bodyHandle = bodyHandle->next;

			continue;
		}

		if (!rb->CheckStationary())
		{
			allStationary = false;
		}
		bodyHandle = bodyHandle->next;
	}
	return allStationary;
}

void neConstraintHeader::BecomeIdle(neBool checkResting)
{
	neBodyHandle * bodyHandle = bodies.GetHead();

	while (bodyHandle)
	{
		neRigidBody_* rb = bodyHandle->thing->AsRigidBody();

		if (!rb)
		{
			bodyHandle = bodyHandle->next;

			continue;
		}
		if (checkResting)
		{
			if (rb->IsRestPointStillValid())
			{
				//if (rb->CheckRestHull())
				{
					rb->BecomeIdle();
				}
			}
		}
		else
		{
			rb->BecomeIdle();
		}
		bodyHandle = bodyHandle->next;
	}
}

void neConstraintHeader::WakeUp()
{
	neBodyHandle * bodyHandle = bodies.GetHead();

	while (bodyHandle)
	{
		neRigidBody_* rb = bodyHandle->thing->AsRigidBody();

		if (!rb)
		{
			bodyHandle = bodyHandle->next;

			continue;
		}

		rb->status = neRigidBody_::NE_RBSTATUS_NORMAL;

		bodyHandle = bodyHandle->next;
	}
}

void neConstraintHeader::RemoveAll()
{
	neBodyHandle * bodyHandle = bodies.GetHead();

	while (bodyHandle)
	{
		neRigidBody_ * rb = bodyHandle->thing->AsRigidBody();

		ASSERT(rb);

		bodyHandle = bodyHandle->next;

		rb->SetConstraintHeader(NULL);

		bodies.Remove(&rb->constraintHeaderItem);
	}
	Reset();
}

void neMotor::PrepareForSolver(_neConstraint * constr)
{
	if (this == &constr->motors[0])
	{
		// primary motor

		// check if at limit already
		if (constr->limitStates[0].enableLimit && constr->limitStates[0].applyLimitImpulse)
		{
			if (constr->limitStates[0].lowerLimitOn)
			{
				if (desireVelocity < 0.0f)
					return;
			}
			else
			{
				if (desireVelocity > 0.0f)
					return;
			}
		}

		switch (constr->type)
		{
		case neJoint::NE_JOINT_HINGE:
			{
				neCollisionResult * cresult = constr->bodyA->sim->cresultHeap.Alloc(0);

				cresult->bodyA = constr->bodyA;

				cresult->bodyB = constr->bodyB;

				cresult->impulseType = IMPULSE_ANGULAR_MOTOR_PRIMARY;

				cresult->finalRelativeSpeed = desireVelocity;

				cresult->depth = maxForce;

				cresult->contactBBody = constr->frameBWorld.rot[1];

				cresult->contactABody = constr->frameAWorld.rot[1];//limitStates[0].limitAxis;

				cresult->PrepareForSolver();
			}
			break;
		
		case neJoint::NE_JOINT_SLIDE:
			{
				// up and down the shaft of the slider
				neCollisionResult * cresult = constr->bodyA->sim->cresultHeap.Alloc(0);

				cresult->bodyA = constr->bodyA;

				cresult->bodyB = constr->bodyB;

				cresult->impulseType = IMPULSE_RELATIVE_LINEAR_VELOCITY;

				cresult->finalRelativeSpeed = desireVelocity;

				cresult->depth = maxForce;

				cresult->contactBBody = constr->frameBWorld.rot[1];

				cresult->contactABody = constr->frameAWorld.rot[1];

				cresult->PrepareForSolver();
			}
			break;

		default:
			break;
		}
	}
	else
	{
		// secondary motor
		switch (constr->type)
		{
		case neJoint::NE_JOINT_SLIDE:
			{
				// rotation around the slider
			}
			break;

		default:
			break;
		}
	}
}