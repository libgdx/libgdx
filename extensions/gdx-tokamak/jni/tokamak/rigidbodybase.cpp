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

/****************************************************************************
*
*	neRigidBodyBase::GetConvex
*
****************************************************************************/ 
/*
TConvex * neRigidBodyBase::GetConvex(s32 index)
{
	ASSERT(index < col.convexCount);

	if (col.convexCount == 0)
		return NULL;

	if (col.convexCount == 1 && index == 0)
		return &col.obb;

	if (index == -1)
	{
		return &col.obb;
	}
	
	return &col.convex[index];
}
*/
void neRigidBodyBase::CollideConnected(neBool yes)
{
	isCollideConnected = yes;
}

neBool neRigidBodyBase::CollideConnected()
{
	return isCollideConnected;
}

void neRigidBodyBase::RecalcBB()
{
	col.CalcBB();

	neV3 maxExt, minExt;

	col.obb.GetExtend(minExt, maxExt);

	neSensorItem * si = (neSensorItem *)sensors;

	while (si)
	{
		neSensor_ * sensor = (neSensor_ *) si;

		si = si->next;

		neV3 sstart;
		neV3 send;

		sstart = sensor->pos;
		send = sensor->pos + sensor->dir;

		for (s32 j = 0; j < 3; j++)
		{
			maxExt[j] = neMax(maxExt[j], sstart[j]);
			maxExt[j] = neMax(maxExt[j], send[j]);
			minExt[j] = neMin(minExt[j], sstart[j]);
			minExt[j] = neMin(minExt[j], send[j]);
		}
	}

	for (s32 i = 0; i < 3; i++)
	{
		col.obb.as.box.boxSize[i] = ( maxExt[i] - minExt[i] ) * 0.5f;
		col.obb.c2p.pos[i] = minExt[i] + col.obb.as.box.boxSize[i];
	}

	obb.rot[0] = col.obb.as.box.boxSize[0] * col.obb.c2p.rot[0];
	obb.rot[1] = col.obb.as.box.boxSize[1] * col.obb.c2p.rot[1];
	obb.rot[2] = col.obb.as.box.boxSize[2] * col.obb.c2p.rot[2];
	obb.pos = col.obb.c2p.pos;

};

/*
neV3 neRigidBodyBase::VelocityAtPoint(const neV3 & pt)
{
	neV3 ret;

	if (AsCollisionBody())
	{
		ret.SetZero();

		return ret;
	}
	else
	{
		ret = ((neRigidBody_*)this)->Derive().linearVel;

		ret += ((neRigidBody_*)this)->Derive().angularVel.Cross(pt);

		return ret;
	}
}
*/
neSensor_ * neRigidBodyBase::AddSensor()
{
	neSensor_ * newSensor = sim->sensorHeap.Alloc(1);
	
	if (!newSensor)
	{
		sprintf(sim->logBuffer,	MSG_RUN_OUT_SENSOR);

		sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

		return NULL;
	}
	if (sensors)
	{
		//((neSensorItem *)sensors)->Append((neSensorItem *)newSensor);

		neSensorItem * sitem = (neSensorItem *)sensors;

		while (sitem->next)
		{
			sitem = sitem->next;
		}
		sitem->Append((neSensorItem *)newSensor);
	}
	else
	{
		sensors = newSensor;
	}
	return newSensor;
}

void neRigidBodyBase::BeginIterateSensor()
{
	sensorCursor = (neSensorItem *)sensors;
}

neSensor_ * neRigidBodyBase::GetNextSensor()
{
	if (!sensorCursor)
		return NULL;

	neSensor_ * ret = (neSensor_ *)sensorCursor;

	sensorCursor = sensorCursor->next;

	return ret;
}

void neRigidBodyBase::ClearSensor()
{
	neSensorItem * si = (neSensorItem *)sensors;

	while (si)
	{
		neSensor_ * s = (neSensor_ *) si;

		si = si->next;

		s->depth = 0.0f;

		s->body = NULL;
	}
}

TConvex * neRigidBodyBase::AddGeometry()
{
	TConvex * newConvex = sim->geometryHeap.Alloc(1);

	if (!newConvex)
	{
		sprintf(sim->logBuffer,	MSG_RUN_OUT_GEOMETRY);

		sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

		return NULL;
	}
	newConvex->Initialise();
	
	if (col.convex)
	{
		TConvexItem * citem = (TConvexItem *)col.convex;

		while (citem)
		{
			if (!citem->next)
			{
				citem->Append((TConvexItem *)newConvex);		
				
				break;
			}
			else
			{
				citem = citem->next;
			}
		}
	}
	else
	{
		col.convex = newConvex;
	}
	col.convexCount++;

	if (isActive && !IsInRegion())
		sim->region.AddBody(this, NULL);

	return newConvex;
}

void neRigidBodyBase::BeginIterateGeometry()
{
	geometryCursor = (TConvexItem *)col.convex;
}

TConvex * neRigidBodyBase::GetNextGeometry()
{
	if (!geometryCursor)
		return NULL;

	TConvex * ret = (TConvex *)geometryCursor;

	geometryCursor = geometryCursor->next;

	return ret;
}

void neRigidBodyBase::RemoveConstraintHeader()
{
	neConstraintHeader * h = GetConstraintHeader();

	if (h)
	{
		h->bodies.Remove(&constraintHeaderItem);

		h->flag = neConstraintHeader::FLAG_NEED_REORG;

		SetConstraintHeader(NULL);

		if (h->bodies.count == 0)
		{
			sim->constraintHeaders.Dealloc(h);
		}
	}
}

void neRigidBodyBase::Free()
{
	//free sensor
	
	neFreeListItem<neSensor_> * si = (neFreeListItem<neSensor_> *) sensors;

	while (si)
	{
		neFreeListItem<neSensor_> * next = si->next;

		//si->Remove();

		sim->sensorHeap.Dealloc((neSensor_*)si, 1);

		si = next;
	}
	sensors = NULL;

	//remove from region
	if (IsInRegion())
		sim->region.RemoveBody(this);

	//free geometry

	neFreeListItem<TConvex> * gi = (neFreeListItem<TConvex> *) col.convex;

	while (gi)
	{
		neFreeListItem<TConvex> * next = gi->next;

		//gi->Remove();

		sim->geometryHeap.Dealloc((TConvex*)gi, 1);

		gi = next;
	}
	col.convex = NULL;

	col.convexCount = 0;

	//free constraint
	neConstraintHandle * chandle = constraintCollection.GetHead();

	while (chandle)
	{
		_neConstraint * c = chandle->thing;

		chandle = constraintCollection.GetNext(chandle);

		c->bodyA->constraintCollection.Remove(&c->bodyAHandle);

		if (c->bodyB)
			c->bodyB->constraintCollection.Remove(&c->bodyBHandle);

		if (GetConstraintHeader())
			GetConstraintHeader()->Remove(c);

		neFreeListItem<neController> * ci = (neFreeListItem<neController> *) c->controllers;

		while (ci)
		{
			neFreeListItem<neController> * next = ci->next;

			ci->Remove();

			sim->controllerHeap.Dealloc((neController *)ci, 1);

			ci = next;
		}
		c->controllers = NULL;

		sim->constraintHeap.Dealloc(c, 1);
	}
	neRestRecordHandle * rhandle = rbRestingOnMe.GetHead();

	while (rhandle)
	{
		neRestRecord * r = rhandle->thing;

		rhandle = rbRestingOnMe.GetNext(rhandle);

		r->SetInvalid();
	};
}

neBool neRigidBodyBase::IsValid()
{
	if (btype == NE_OBJECT_COLISION)
	{
		return ((neList<neCollisionBody_>::itemType *)this)->state;// sim->abHeap.IsInUse((neCollisionBody_*)this);
	}
	else
	{
		return ((neList<neRigidBody_>::itemType *)this)->state;//sim->rbHeap.IsInUse((neRigidBody_*)this);
	}
}

neT3 & neRigidBodyBase::GetB2W()
{
	if (btype == NE_OBJECT_COLISION)
	{
		return AsCollisionBody()->b2w;
	}
	else
	{
		return AsRigidBody()->State().b2w;
	}
}

void neRigidBody_::DrawCPointLine()
{
	return;

#if 0
	neConstraintPointArray & pointArray = GetRBCData().GetCPointArray();

	for (s32 i = 0; i < pointArray.GetUsedCount(); i++)
	{
		for (s32 j = i + 1; j < pointArray.GetUsedCount(); j++)
		{
//			if (pointArray[i].constraint == pointArray[j].constraint)
//				continue;
			
			neV3 points[2];
			neV3 color;

			points[0] = *pointArray[i].GetPtResult(this);
			points[1] = *pointArray[j].GetPtResult(this);
			f32 test = (points[0] - points[1]).Length();
			DrawLine(color, points, 2);
		}
	}
	if (calignMethod == ALIGN_POINT_ORIGIN ||
		calignMethod == ALIGN_LINE_ORIGIN ||
		calignMethod == ALIGN_TRI_AUX)
	{
		for (i = 0; i < pointArray.GetUsedCount(); i++)
		{
			neV3 points[2];
			neV3 color;

			points[0] = *pointArray[i].GetPtResult(this);
			points[1] = auxCPoints[1];

			f32 test = (points[0] - points[1]).Length();
			DrawLine(color, points, 2);
		}

	}
#endif
}

void neRigidBodyBase::Active(neBool yes, neRigidBodyBase * hint)
{
	if (isActive && yes)
		return;

	if (isActive) //make inactive
	{
		if (AsCollisionBody())
		{
			sim->activeCB.Remove((neCollisionBody_*)this);

			sim->inactiveCB.Add((neCollisionBody_*)this);
		}
		else
		{
			if (AsRigidBody()->IsParticle())
			{
				sim->activeRP.Remove((neRigidBody_*)this);

				sim->inactiveRP.Add((neRigidBody_*)this);
			}
			else
			{
				sim->activeRB.Remove((neRigidBody_*)this);

				sim->inactiveRB.Add((neRigidBody_*)this);
			}
		}
		//remove from region

		if (IsInRegion())
			sim->region.RemoveBody(this);

		isActive = false;
	}
	else //make active
	{
		if (AsCollisionBody())
		{
			sim->inactiveCB.Remove((neCollisionBody_*)this);

			sim->activeCB.Add((neCollisionBody_*)this);
		}
		else
		{
			if (AsRigidBody()->IsParticle())
			{
				sim->inactiveRP.Remove((neRigidBody_*)this);

				sim->activeRP.Add((neRigidBody_*)this);
			}
			else
			{
				sim->inactiveRB.Remove((neRigidBody_*)this);

				sim->activeRB.Add((neRigidBody_*)this);
			}
		}
		//insert into the region

		if (col.convexCount > 0 || isCustomCD)
			sim->region.AddBody(this, hint);

		isActive = true;
	}
}
