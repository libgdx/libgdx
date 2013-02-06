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
/*
#ifdef _WIN32
#include <windows.h>
#endif
*/
#include "stack.h"
#include "simulator.h"
#include "scenery.h"
/*
#include <algorithm>
#include <assert.h>
*/
s32 neStackHeader::golbalTime = 0;

void neRestRecord::Update()
{
	worldThisBody = body->State().b2w * bodyPoint;

	neRigidBody_ * rb = otherBody->AsRigidBody();

	neCollisionBody_ * cb = otherBody->AsCollisionBody();

	if (cb)
	{
		normalWorld = cb->b2w.rot * normalBody;

		worldOtherBody = cb->b2w * otherBodyPoint;
	}
	else
	{
		normalWorld = rb->State().b2w.rot * normalBody;

		worldOtherBody = rb->State().b2w * otherBodyPoint;
	}
	worldDiff = worldThisBody - worldOtherBody;
	
	normalDiff = worldDiff.Dot(normalWorld); // < 0.0f means penetration

	neV3 v = worldDiff;

	v.RemoveComponent(normalWorld); 

	tangentialDiffSq = v.Dot(v);
}

void neStackInfo::Resolve()
{
	isResolved = true;

	if (isTerminator)
		return;

	ASSERT(body);

	for (s32 i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (body->GetRestRecord(i).IsValid())
		{
			neRigidBody_ * rb = body->GetRestRecord(i).GetOtherRigidBody();

			if (rb && rb->stackInfo)
			{
				if (!rb->stackInfo->isResolved)
					rb->stackInfo->Resolve();
			}
		}
	}
	//body->ResolveRestingPenetration();
}

void neStackInfo::CheckHeader(neStackHeader * sh)
{
	ASSERT(stackHeader == sh);

	neRigidBody_ * b = body->AsRigidBody();

	if (!b)
		return;

	for (s32 i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (b->GetRestRecord(i).GetOtherRigidBody())
		{
			neRigidBody_ * otherBody = b->GetRestRecord(i).GetOtherRigidBody();
			
			if (otherBody)
			{
				ASSERT(otherBody->stackInfo);

				ASSERT(otherBody->stackInfo->stackHeader == sh);

				if (!otherBody->stackInfo->isTerminator)
				{
					otherBody->stackInfo->CheckHeader(sh);
				}
			}
		}
	}
}

#if 0
void neStackInfo::Break()
{
/*	char ss [256];
	sprintf(ss, "break %d\n", body->id);
	OutputDebugString(ss);
*/	
	if (stackHeader->isHeaderX)
	{
		stackHeader->Remove(this);

		body->FreeStackInfo();

		return;
	}
	stackHeader->CheckHeader();

	neStackHeader * newHeader = body->sim->NewStackHeader(NULL);

	for (s32 i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (body->GetRestRecord(i).otherBody)
		{
			neRigidBody_* b = (neRigidBody_*)body->GetRestRecord(i).otherBody;

			if (b)
			{
				if (!b->stackInfo) //remove from previous iteration
					continue;
				
				if (b->stackInfo->stackHeader == newHeader) //migrate from previous iteration
					continue;
				
				if (b->stackInfo->isTerminator)
				{
					stackHeader->Remove(b->stackInfo);
					
					b->FreeStackInfo();

					b->ResetRestOnRecords();
				}
				else
				{
					ASSERT(b->stackInfo->stackHeader == stackHeader);

					b->MigrateNewHeader(newHeader, stackHeader);

					body->GetRestRecord(i).Init();
				}
			}
		}
	}
	if (newHeader->infoCount == 0)
	{
		body->sim->stackHeaderHeap.Dealloc(newHeader);
	}
	else
	{
		newHeader->CheckHeader();
	}
	if (stackHeader->infoCount == 1)
	{
		ASSERT(stackHeader->head == this);

		stackHeader->infoCount = 0;

		neStackHeader * h = stackHeader;
		
		h->Remove(this);

		body->sim->stackHeaderHeap.Dealloc(h);

		body->FreeStackInfo();
	}
	else
	{
		body->stackInfo->isTerminator = true;

		stackHeader->CheckHeader();
	}
}
#endif

void neStackHeader::Resolve()
{
	// resolve all penetration under this header

//	ASSERT(head);

//	if (head == NULL)
//		return;

	s32 c = 0;

	neStackInfoItem * item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;

		ASSERT(sinfo->stackHeader == this);

		sinfo->isResolved = false;

		item = item->next;

		c++;
	}

	item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;

		item = item->next;

		if (!sinfo->isResolved)
			sinfo->Resolve();
	}
}
/*
void neStackHeader::Purge()
{
	if (!head)
		return;

	neStackInfoItem * item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;
		
		item = item->next;

		sim->stackInfoHeap.Dealloc(sinfo, 1);
	}
	Null();
}
*/
void neStackHeader::ChangeHeader(neStackHeader * newHeader)
{
	if (!head)
	{
		ASSERT(0);
	}
	neStackInfoItem * item = (neStackInfoItem *) head;

	s32 c = 0;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;
		
		item = item->next;

		sinfo->stackHeader = newHeader;

		c++;
	}

	ASSERT(c == infoCount);
	
	ASSERT(newHeader->tail);
	
	neStackInfoItem * newTailItem = (neStackInfoItem *) newHeader->tail;
	
	newTailItem->Concat((neStackInfoItem *)head);

	newHeader->tail = tail;

	newHeader->infoCount += c;
}
/*
s32 pop = 0;

neStackHeader * hell[256];
*/
neBool neStackHeader::CheckStackDisconnected()
{
//	OutputDebugString("start\n");
	//neSimpleArray<neStackInfo*, 1000> stackInfoBuffer;

	neSimpleArray<neByte *> & stackInfoBuffer = sim->pointerBuffer2;

	stackInfoBuffer.Clear();

	neStackInfoItem * item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;

		sinfo->startTime = 0;
		sinfo->endTime = 0;

		item = item->next;

		ASSERT(sinfo->stackHeader == this);

		Remove(sinfo, 1);

		*stackInfoBuffer.Alloc() = (neByte *)sinfo;
	}

	s32 i;

	for (i = 0; i < stackInfoBuffer.GetUsedCount(); i++)
	{
		neStackInfo * sinfo = (neStackInfo *)stackInfoBuffer[i];

		if (sinfo->isBroken)
			continue;

		neRigidBody_ * rb = sinfo->body;

		for (s32 j = 0; j < NE_RB_MAX_RESTON_RECORDS; j++)
		{
			if (rb->GetRestRecord(j).IsValid())
			{
				neRigidBody_ * otherbody = rb->GetRestRecord(j).GetOtherRigidBody();

				if (otherbody)
				{
					if (otherbody->stackInfo->stackHeader)
					{
						if (sinfo->stackHeader)
						{
							if (sinfo->stackHeader != otherbody->stackInfo->stackHeader)
							{
								// merge
								neStackHeader * otherHeader = otherbody->stackInfo->stackHeader;

								otherHeader->ChangeHeader(sinfo->stackHeader);

								sim->stackHeaderHeap.Dealloc(otherHeader);
							}
						}
						else
						{
							otherbody->stackInfo->stackHeader->Add(sinfo);
						}
					}
					else
					{
						if (sinfo->stackHeader)
						{
							sinfo->stackHeader->Add(otherbody->stackInfo);
						}
						else
						{
							neStackHeader * newStackHeader = sim->NewStackHeader(NULL);

							newStackHeader->dynamicSolved = dynamicSolved;

							newStackHeader->Add(sinfo);

							newStackHeader->Add(otherbody->stackInfo);
						}
					}
				}
			}
		}
	}
	for (i = 0; i < stackInfoBuffer.GetUsedCount(); i++)
	{
		neStackInfo * sinfo = (neStackInfo *)stackInfoBuffer[i];

		if (!sinfo->stackHeader)
		{
			neRigidBody_ * rb = sinfo->body;

			sim->stackInfoHeap.Dealloc(sinfo, 1);

			for (s32 i = 0; i < NE_MAX_REST_ON; i++)
			{
				rb->GetRestRecord(i).SetInvalid();
			}

			rb->stackInfo = NULL;
		}
	}

/*
	item = (neStackInfoItem *) head;

	//pop = 0;

	for (s32 i = 0; i < stackInfoBuffer.GetUsedCount(); i++)
	{
//		char ss[256];

		neStackInfo * sinfo = (neStackInfo *)stackInfoBuffer[i];

//		sprintf(ss, "starting at %d\n", sinfo->body->id);
//		OutputDebugString(ss);

		neStackHeader * newStackHeader = sim->NewStackHeader(NULL);

//		hell[pop] = newStackHeader;
//		pop++;

		neStackHeader * anotherHeader = sinfo->CheckAcceptNewHeader(newStackHeader);

		if (anotherHeader && (anotherHeader != newStackHeader))
		{
			for (s32 j = 0; j < i ; j++)
			{
				((neStackInfo *)stackInfoBuffer[j])->ForceAcceptNewHeader(anotherHeader);
			}
		}

//		sprintf(ss, "newStackheader %d count = %d\n",pop, newStackHeader->infoCount);
//		OutputDebugString(ss);

		if (newStackHeader->infoCount == 0)
		{
			sim->stackHeaderHeap.Dealloc(newStackHeader);

//			sprintf(ss, "dealloc %d\n",pop);
//			OutputDebugString(ss);
		}
		
	}
	ASSERT(infoCount == 0);

//	sim->stackHeaderHeap.Dealloc(this);
//	sim->CheckStackHeader();
*/	
	return true; // always dealloc this header
}

#if 0

void neStackHeader::AddToSolver()
{
	neStackInfoItem * sitem = (neStackInfoItem *)head;

	while (sitem)
	{
		neStackInfo * sinfo = (neStackInfo*) sitem;

		sitem = sitem->next;

		if (!sinfo->isTerminator)
			sinfo->body->AddContactImpulseRecord(true);

		sinfo->body->needRecalc = true;

		if (!sinfo->body->GetConstraintHeader())
		{
			sinfo->body->SetConstraintHeader(&sinfo->body->sim->contactConstraintHeader);

			sinfo->body->sim->contactConstraintHeader.bodies.Add(&sinfo->body->constraintHeaderItem);
		}
	}
}

#else

void neStackHeader::AddToSolver()
{
	neStackInfoItem * item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;

		ASSERT(sinfo->stackHeader == this);

		sinfo->isResolved = false;

		item = item->next;
	}
	item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;

		item = item->next;

		if (!sinfo->isResolved)
		{
			if (!sinfo->body->GetConstraintHeader())
			{
				sinfo->body->SetConstraintHeader(&sinfo->body->sim->contactConstraintHeader);

				sinfo->body->sim->contactConstraintHeader.bodies.Add(&sinfo->body->constraintHeaderItem);
			}
			if (!sinfo->isTerminator)
				sinfo->AddToSolver(true);
		}
	}
}

void neStackHeader::AddToSolverNoConstraintHeader()
{
	neStackInfoItem * item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;

		ASSERT(sinfo->stackHeader == this);

		sinfo->isResolved = false;

		item = item->next;
	}
	item = (neStackInfoItem *) head;

	while (item)
	{
		neStackInfo * sinfo = (neStackInfo *) item;

		item = item->next;

		if (!sinfo->isResolved)
		{
			if (!sinfo->isTerminator)
				sinfo->AddToSolver(false);
		}
	}
/*	
	neStackInfoItem * sitem = (neStackInfoItem *)head;

	while (sitem)
	{
		neStackInfo * sinfo = (neStackInfo*) sitem;

		sitem = sitem->next;

		if (!sinfo->isTerminator)
			sinfo->body->AddContactImpulseRecord(false);

		sinfo->body->needRecalc = true;
	}
*/	
}

#endif

void neStackInfo::AddToSolver(neBool addCHeader)
{
	isResolved = true;

	ASSERT (!isTerminator);

	ASSERT(body);

//	body->AddContactImpulseRecord(addCHeader);

	for (s32 i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		if (!body->GetRestRecord(i).IsValid())
		{
			continue;
		}
		neRigidBody_ * rb = body->GetRestRecord(i).GetOtherRigidBody();

		if (!rb || !rb->stackInfo)
		{
			continue;
		}
		if (rb->stackInfo->isResolved)
		{
			continue;
		}
		if (!rb->GetConstraintHeader() && addCHeader)
		{
			rb->SetConstraintHeader(&rb->sim->contactConstraintHeader);

			rb->sim->contactConstraintHeader.bodies.Add(&rb->constraintHeaderItem);
		}
		if (!rb->stackInfo->isTerminator)
			rb->stackInfo->AddToSolver(addCHeader);
	}
	body->AddContactImpulseRecord(addCHeader);
}

void neStackHeader::ResetRigidBodyFlag()
{
	neStackInfoItem * sitem = (neStackInfoItem *)head;

	while (sitem)
	{
		neStackInfo * sinfo = (neStackInfo*) sitem;

		sitem = sitem->next;

		sinfo->body->needRecalc = false;
	}		
}

neStackHeader * neStackInfo::CheckAcceptNewHeader(neStackHeader * newHeader)
{
	// this function is for diagnostic only

	if (startTime > 0) // already visited
	{
		return NULL;
	}

	startTime = ++neStackHeader::golbalTime;

	if (stackHeader) //already visited
	{
		if (stackHeader != newHeader)
		{
			return stackHeader;
		}
		else
		{
			return NULL;
		}
	}
	if (isTerminator)
	{
		newHeader->Add(this);

		return NULL;
	}
	if (isBroken)
	{
		newHeader->Add(this);

		isTerminator = true;

		return NULL;
	}
	neBool anotherHeaderFound = false;

	neStackHeader * anotherHeader = NULL;

	neRigidBody_ * foundBody;

	s32 i;

	for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		neRigidBody_* otherBody = (neRigidBody_*)body->GetRestRecord(i).GetOtherRigidBody();

		if (!otherBody)
			continue;
/*
		if (otherBody->AsCollisionBody())
		{
			continue;
		}
*/		ASSERT(otherBody->stackInfo);

		anotherHeader = otherBody->stackInfo->CheckAcceptNewHeader(newHeader);

		ASSERT(anotherHeader != newHeader);

		if (anotherHeader != NULL)
		{
			anotherHeaderFound = true;
			foundBody = otherBody;
			break;
		}
	}
	if (anotherHeaderFound)
	{
		anotherHeader->Add(this);

		for (i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
		{
			neRigidBody_* otherBody = (neRigidBody_*)body->GetRestRecord(i).GetOtherRigidBody();

			if (!otherBody)
				continue;
/*
			if (otherBody->AsCollisionBody())
				continue;
*/
			if (otherBody != foundBody)
			{
				if (otherBody->stackInfo->stackHeader != anotherHeader)
					otherBody->stackInfo->ForceAcceptNewHeader(anotherHeader);
			}
		}
		return stackHeader;
	}
	else
	{
		newHeader->Add(this);

		return NULL;
	}
}

void neStackInfo::ForceAcceptNewHeader(neStackHeader * newHeader)
{
	if (isTerminator)
	{
		if (stackHeader)
			stackHeader->Remove(this);

		newHeader->Add(this);

		return;
	}
	if (isBroken)
	{
		if (stackHeader)
			stackHeader->Remove(this);

		newHeader->Add(this);

		return;
	}
	if (stackHeader)
	{
		if (stackHeader == newHeader)
		{
			return;
		}
		stackHeader->Remove(this);
	}
	newHeader->Add(this);

	for (s32 i = 0; i < NE_RB_MAX_RESTON_RECORDS; i++)
	{
		neRigidBody_* otherBody = (neRigidBody_*)body->GetRestRecord(i).GetOtherRigidBody();

		if (!otherBody)
			continue;
/*
		if (otherBody->AsCollisionBody())
		{
			continue;
		}
*/		ASSERT(otherBody->stackInfo);
		
		otherBody->stackInfo->ForceAcceptNewHeader(newHeader);
	}
}

