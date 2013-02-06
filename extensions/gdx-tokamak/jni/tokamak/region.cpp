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
#include "scenery.h"
/*
#ifdef _WIN32
#include <windows.h>
#endif
*/
#include "stack.h"
#include "simulator.h"

#include <stdio.h>
#include <assert.h>

/****************************************************************************
*
*	neRegion::~neRegion
*
****************************************************************************/ 

neRegion::~neRegion()
{
}

/****************************************************************************
*
*	neRegion::Initialise
*
****************************************************************************/ 

void neRegion::Initialise(neFixedTimeStepSimulator * s, neByte sortD)
{
	sim = s;

	maxRigidBodies = s->maxRigidBodies;

	maxAnimBodies = s->maxAnimBodies;

	totalBodies = maxRigidBodies + maxAnimBodies;

	maxParticle = s->maxParticles;

	b2b.Reserve(totalBodies * (totalBodies - 1) / 2, sim->allocator);

	b2p.Reserve(totalBodies * maxParticle, sim->allocator);

	newBodies.Reserve(totalBodies + maxParticle, sim->allocator);

	bodies.Reserve(totalBodies + maxParticle, sim->allocator);

	overlappedPairs.Reserve(sim->sizeInfo.overlappedPairsCount, sim->allocator);

	sortDimension = sortD;

	for (s32 i = 0; i < 3; i++)
	{
		if (sortD & (1 << i ) )
		{
			bool b = coordLists[i].Reserve((totalBodies + maxParticle) * 2, sim->allocator);
			
			coordLists[i].dim = i;

			coordLists[i].dimPower2 = 1 << i;

			coordLists[i].region = this;
			
			ASSERT(b);
		}
	}

//	needRebuild = true;

	terrainTree.sim = sim;
	
#ifdef _DEBUG_REGION
	debugOn = false;
#endif
}

/****************************************************************************
*
*	neRegion::AddBody
*
****************************************************************************/ 

bool neRegion::AddBody(neRigidBodyBase * bb, neRigidBodyBase * hint)
{
	if (bb->IsInRegion())
		return true;
	
	neAddBodyInfo * bi = newBodies.Alloc();
	
	bi->body = bb;

	bi->hint = hint;

	neRigidBodyBase ** bp = bodies.Alloc();
		
	*bp = bi->body;

	bi->body->regionHandle = (neFreeListItem<neRigidBodyBase *>*)bp;

	bb->pendingAddToRegion = 1;

	return true;
}

/****************************************************************************
*
*	neRegion::RemoveBody
*
****************************************************************************/ 

void neRegion::RemoveBody(neRigidBodyBase * bb)
{
	if (!bb->IsInRegion())
		return;

	if (bb->pendingAddToRegion == 1)
	{
		bb->pendingAddToRegion = 2; // to signify adding to region is aborted
	}

	for (s32 i = 0; i < 3; i++)
	{
		if (sortDimension & (1 << i) )
		{
			if (bb->maxCoord[i])
			{
				coordLists[i].coordList.Dealloc(bb->maxCoord[i]);

				bb->maxCoord[i] = NULL;
			}
			if (bb->minCoord[i])
			{
				coordLists[i].coordList.Dealloc(bb->minCoord[i]);

				bb->minCoord[i] = NULL;
			}
		}
	}
	if (bb->regionHandle)
		bodies.Dealloc((neRigidBodyBase**)bb->regionHandle);

	bb->regionHandle = NULL;
	
	neFreeListItem<neOverlappedPair> * oitem = (neFreeListItem<neOverlappedPair> *)(*overlappedPairs.BeginUsed());

	while (oitem)
	{
		neOverlappedPair * op = (neOverlappedPair *) oitem;

		oitem = oitem->next;

		if (op->bodyA == bb || op->bodyB == bb)
		{
			overlappedPairs.Dealloc(op);
		}
	}
}

/****************************************************************************
*
*	neRegion::UpdateOverlapPairs
*
****************************************************************************/ 

void neRegion::Rebuild()
{
	// sort coordinate list
	for (s32 i = 0; i < 3; i++)
	{
		if (sortDimension & (1 << i) )
		{
			coordLists[i].Sort(true);
		}
	}

	overlappedPairs.Clear();
	
	neRigidBody_ * rb = sim->activeRB.GetHead();

	while (rb)
	{
		neRigidBody_ * rbNext = sim->activeRB.GetNext(rb);

		neRigidBody_ * rbNext_ = rbNext;

		while (rbNext)
		{
			ResetOverlapStatus(rb, rbNext);

			rbNext = sim->activeRB.GetNext(rbNext);
		}
		rb = rbNext_;
	}

	rb = sim->activeRB.GetHead();

	while (rb)
	{
		neCollisionBody_ * cb = sim->activeCB.GetHead();

		while (cb)
		{
			ResetOverlapStatus(rb, cb);

			cb = sim->activeCB.GetNext(cb);
		}
		rb = sim->activeRB.GetNext(rb);
	}
}

/****************************************************************************
*
*	neRegion::Update
*
****************************************************************************/ 

void neRegion::Update()
{
	for (s32 i = 0; i < 3; i++)
	{
		if (sortDimension & (1 << i) )
		{
			coordLists[i].Sort(false);
		}
	}

	for (s32 k = 0; k < newBodies.GetUsedCount(); k++)
	{
		neAddBodyInfo * bi = &newBodies[k];

		if (bi->body->pendingAddToRegion == 2 || !bi->body->IsValid())
		{
			continue;
		}

		ASSERT(bi->body->pendingAddToRegion == 1);

		bi->body->pendingAddToRegion = 0;

		InsertCoordList(bi->body, bi->hint);

		neDLinkList<neRigidBodyBase *>::iterator iter;

		for (iter = bodies.BeginUsed(); iter.Valid(); iter++)
		{
			neRigidBodyBase * b = *(*iter);

			if (b->minCoord[0] == NULL && b->minCoord[1] == NULL && b->minCoord[2] == NULL)
				continue;

			if (b == bi->body)
				continue;
			
			neRigidBody_ * b1 = bi->body->AsRigidBody();

			neRigidBody_ * b2 = b->AsRigidBody();

			if ((b1 && b2))
			{
				if (! (b1->IsParticle() && b2->IsParticle()) )
					ResetOverlapStatus(bi->body, b);
			}
			else
			{
				ResetOverlapStatus(bi->body, b);
			}
		}
/*
		neRigidBodyBase ** bp = bodies.Alloc();
		
		*bp = bi->body;

		bi->body->regionHandle = (neFreeListItem<neRigidBodyBase *>*)bp;
*/	}

	newBodies.Clear();

#ifdef _DEBUG_REGION
	
	if (debugOn)
	{
		for (s32 j = 0; j < 3; j++)
		{
			if (sortDimension & 1 << j)
				coordLists[j].OuputDebug();
		}
	}

#endif
}

/****************************************************************************
*
*	neRegion::GetOverlappedPair
*
****************************************************************************/ 

neOverlapped * neRegion::GetOverlappedStatus(neRigidBodyBase * a, neRigidBodyBase * b)
{
	s32 smallIndex, largeIndex;

	if (a->id > b->id)
	{
		smallIndex = b->id;
		largeIndex = a->id;
	}
	else
	{
		smallIndex = a->id;
		largeIndex = b->id;
	}
	
	neOverlapped * o;

	if (largeIndex >= totalBodies)
	{
		//b overlapping p
		//if (smallIndex >= maxRigidBodies) // ab 2 ab, return
		//	return NULL;

		ASSERT(smallIndex < totalBodies); //ab 2 ab

		ASSERT(((largeIndex - totalBodies) + smallIndex * maxParticle) < b2p.GetTotalSize());
		
		o = &b2p[(largeIndex - totalBodies) + smallIndex * maxParticle];
	}
	else
	{
		//b overlapping b
		ASSERT(((largeIndex * (largeIndex - 1)) / 2) + smallIndex < b2b.GetTotalSize());
		
		o = &b2b[((largeIndex * (largeIndex - 1)) / 2) + smallIndex];
	}
	return o;
}
/****************************************************************************
*
*	neRegion::ToggleOverlapStatus
*
****************************************************************************/ 

void neRegion::ToggleOverlapStatus(neRigidBodyBase * a, neRigidBodyBase * b, neByte dimp2)
{
	neOverlapped * o = GetOverlappedStatus(a,b);

	ASSERT(o);
	
	if (o->status == sortDimension)
	{
		o->status ^= dimp2;

		//if (o->status != sortDimension && (sim->colTable.Get(a->cid, b->cid) != neCollisionTable::NE_COLLISION_IGNORE))
		if (o->status != sortDimension && o->pairItem)
		{
			//remove
			overlappedPairs.Dealloc(o->pairItem);

			o->pairItem = NULL;
		}
	}
	else
	{
		o->status ^= dimp2;

		if (o->status == sortDimension && (sim->colTable.Get(a->cid, b->cid) != neCollisionTable::RESPONSE_IGNORE))
		{
			if (overlappedPairs.usedCount >= sim->sizeInfo.overlappedPairsCount)
			{
				sprintf(sim->logBuffer, "Overlap Pair buffer full. Increase buffer size.\n");
				sim->LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
				return;
			}
			//insert
			o->pairItem = overlappedPairs.Alloc();

			o->pairItem->bodyA = a;

			o->pairItem->bodyB = b;
		}
	}
	//zDebugMessage("toggle in %d, between objects %d and %d\n", dimp2 >> 1, a->id, b->id);
	//ASSERT(overlappedPairs.unusedCount <= 1);
}

/****************************************************************************
*
*	neRegion::ResetOverlapStatus
*
****************************************************************************/ 

void neRegion::ResetOverlapStatus(neRigidBodyBase * a, neRigidBodyBase * b)
{
	neOverlapped * o = GetOverlappedStatus(a,b);

	o->status = a->IsAABOverlapped(b);

	if (o->status == sortDimension)
	{
		o->pairItem = overlappedPairs.Alloc();

		neRigidBody_ * ra = a->AsRigidBody();

		neRigidBody_ * rb = b->AsRigidBody();
		
		if (ra)
		{
			if (ra->IsParticle())
			{
				if (rb)
				{
					ASSERT(!rb->IsParticle());

					o->pairItem->bodyA = b;

					o->pairItem->bodyB = a;
				}
				else
				{
					o->pairItem->bodyA = a;

					o->pairItem->bodyB = b;
				}
			}
			else
			{
				o->pairItem->bodyA = a;

				o->pairItem->bodyB = b;
			}
		}
		else
		{
			o->pairItem->bodyA = b;

			o->pairItem->bodyB = a;
		}
	}
	else
	{
		o->pairItem = NULL;
	}
}

void neRegion::MakeTerrain(neTriangleMesh * tris)
{
	terrainTree.BuildTree(tris->vertices, tris->vertexCount, tris->triangles, tris->triangleCount, sim->allocator);
}

void neRegion::FreeTerrain()
{
	terrainTree.FreeTree();
}

void neRegion::InsertCoordList(neRigidBodyBase * bb, neRigidBodyBase * hint)
{
	for (s32 i = 0; i < 3; i++)
	{
		if (sortDimension & (1 << i) )
		{
			coordLists[i].Add(bb, hint, i);
		}
		else
		{
			bb->maxCoord[i] = NULL;
			bb->minCoord[i] = NULL;
		}
	}
}

/****************************************************************************
*
*	neCoordList::Add
*
****************************************************************************/ 

void neCoordList::Add(neRigidBodyBase * bb, neRigidBodyBase * hint, s32 hintCoord)
{
	CCoordListEntryItem * startSearch = coordList.usedTail;

	CCoordListEntry * lentry = coordList.Alloc();
	
	lentry->bb = bb;
	
	lentry->flag = CCoordListEntry::LowEnd;
	
	bb->minCoord[dim] = lentry;

	CCoordListEntry * hentry = coordList.Alloc();

	hentry->bb = bb;

	hentry->flag = CCoordListEntry::HighEnd;

	bb->maxCoord[dim] = hentry;

	if (bb->AsCollisionBody())
	{
		bb->AsCollisionBody()->UpdateAABB();
	}
	else
	{
		bb->AsRigidBody()->UpdateAABB();
	}

	if (!startSearch)
	{
		return;
	}

	CCoordListEntryItem * lentryItem = (CCoordListEntryItem*)lentry;

	CCoordListEntryItem * hentryItem = (CCoordListEntryItem*)hentry;

	if (lentryItem->thing.value >= lentryItem->prev->thing.value)
	{
		//already in place
		return;
	}

	lentryItem->Remove();

	hentryItem->Remove();

	coordList.usedTail = startSearch;
	
	if (hint)
	{
		if (hint->minBound[hintCoord] == 0)
		{
			hint = NULL;
		}
		else
		{
			ASSERT(hint->minBound[hintCoord] && hint->maxBound[hintCoord]);
		}
	}

	if (!hint)
	{
		//search from the end of the list

		CCoordListEntryItem* cur = startSearch;
		
		neBool done = false;

		do 
		{
			if (lentryItem->thing.value < cur->thing.value)
			{
				if (cur->prev)
				{
					cur = cur->prev;
				}
				else
				{
					cur->Insert(lentryItem);

					coordList.used = lentryItem;

					done = true;
				}
			}
			else
			{
				if (cur == coordList.usedTail)
					coordList.usedTail = lentryItem;
				
				cur->Append(lentryItem);

				done = true;
			}
		} while (!done);

		cur = startSearch;

		done = false;

		do 
		{
			if (hentryItem->thing.value <= cur->thing.value)
			{
				ASSERT(hentryItem->thing.bb != cur->thing.bb); //should go pass the same body's low end

				if (cur->prev)
				{
					cur = cur->prev;
				}
				else
				{
					cur->Insert(hentryItem);

					coordList.used = hentryItem;

					done = true;
				}
			}
			else
			{
				if (cur == coordList.usedTail)
					coordList.usedTail = hentryItem;

				cur->Append(hentryItem);

				done = true;
			}
		} while (!done);
	}
	else
	{
		//search from where the hint is

		for (s32 i = 0; i < 2; i++)
		{
			CCoordListEntryItem * entryItem;

			CCoordListEntryItem* cur;

			neBool searchUp;
			
			if (i == 0)
			{
				entryItem = lentryItem;
				
				cur = (CCoordListEntryItem*)hint->minCoord[hintCoord];

				if (entryItem->thing.value < cur->thing.value)
					searchUp = true;
				else
					searchUp = false;
			}
			else
			{
				cur = lentryItem;

				entryItem = hentryItem;

				searchUp = false;
			}
			
			if (searchUp)
			{
				neBool done = false;

				do 
				{
					if (entryItem->thing.value < cur->thing.value)
					{
						if (cur->prev)
						{
							cur = cur->prev;
						}
						else
						{
							cur->Insert(entryItem);

							coordList.used = entryItem;

							done = true;
						}
					}
					else
					{
						if (cur == coordList.usedTail)
							coordList.usedTail = entryItem;
						
						cur->Append(lentryItem);

						done = true;
					}
				} while (!done);
			}
			else
			{
				neBool done = false;

				do 
				{
					if (entryItem->thing.value >= cur->thing.value)
					{
						if (cur->next)
						{
							cur = cur->next;
						}
						else
						{
							cur->Append(entryItem);

							coordList.usedTail = entryItem;

							done = true;
						}
					}
					else
					{
						if (cur == coordList.used)
							coordList.used = entryItem;

						cur->Insert(entryItem);

						done = true;
					}
				} while (!done);
			}
		}
	}
}

/****************************************************************************
*
*	neCoordList::Sort
*
****************************************************************************/ 

void neCoordList::Sort(bool sortOnly)
{
	neDLinkList<CCoordListEntry>::listItem * sortStart = coordList.used;

	if (! sortStart)
		return;

	coordList.used = sortStart->next;
	
	sortStart->Remove();

	neDLinkList<CCoordListEntry>::listItem * usedStart = sortStart;

	neDLinkList<CCoordListEntry>::listItem * usedTail = NULL;

	while (coordList.used)
	{
		neDLinkList<CCoordListEntry>::listItem * nextUsed;

		nextUsed = coordList.used->next;
		
		coordList.used->Remove();

		neDLinkList<CCoordListEntry>::listItem * insert = coordList.used;

		// insert sort start here
		bool done = false;

		if (insert->thing.flag == CCoordListEntry::LowEnd)
		{
			neDLinkList<CCoordListEntry>::listItem * cur = sortStart;

			while (!done && cur)
			{
				//compare
				if (cur->thing.bb == insert->thing.bb)
				{
					if (cur->prev)
					{
						cur = cur->prev;
					}
					else
					{
						cur->Insert(insert);

						usedStart = insert;
						
						done = true;
					}
				}
				else
				{
					if (insert->thing.value < cur->thing.value)
					{
						// update overlap status
						if (cur->thing.flag == CCoordListEntry::HighEnd && !sortOnly)
						{
							neRigidBody_ * b1 = cur->thing.bb->AsRigidBody();

							neRigidBody_ * b2 = insert->thing.bb->AsRigidBody();

							if (b1 && b2)
							{
								if (! (b1->IsParticle() && b2->IsParticle()))
									region->ToggleOverlapStatus(cur->thing.bb, insert->thing.bb, dimPower2);
							}
							else
							{
								region->ToggleOverlapStatus(cur->thing.bb, insert->thing.bb, dimPower2);
							}
						}
						if (cur->prev)
						{
							cur = cur->prev;
						}
						else
						{
							cur->Insert(insert);

							usedStart = insert;

							done = true;
						}
					}
					else
					{
						cur->Append(insert);
						
						done = true;

						if (cur == sortStart)
						{
							sortStart = insert;
						}
					}
				}
			}
		}
		else //HighEnd
		{
			neDLinkList<CCoordListEntry>::listItem * cur = sortStart;

			while (!done && cur)
			{
				//compare
				if (cur->thing.bb == insert->thing.bb)
				{
					cur->Append(insert);

					done = true;

					if (cur == sortStart)
						sortStart = insert;
				}
				else
				{
					if (insert->thing.value < cur->thing.value)
					{
						// update overlap status
						if (cur->thing.flag == CCoordListEntry::LowEnd && !sortOnly)
						{
							neRigidBody_ * b1 = cur->thing.bb->AsRigidBody();

							neRigidBody_ * b2 = insert->thing.bb->AsRigidBody();

							if (b1 && b2)
							{
								if (! (b1->IsParticle() && b2->IsParticle()))
									region->ToggleOverlapStatus(cur->thing.bb, insert->thing.bb, dimPower2);
							}
							else
							{
								region->ToggleOverlapStatus(cur->thing.bb, insert->thing.bb, dimPower2);
							}
						}

						if (cur->prev)
						{
							cur = cur->prev;
						}
						else
						{
							cur->Insert(insert);

							usedStart = insert;

							done = true;
						}
					}
					else
					{
						cur->Append(insert);
						
						done = true;

						if (cur == sortStart)
							sortStart = insert;
					}
				}
			}
		}

		// insert sort end

		coordList.used = nextUsed;
	}

	coordList.used = usedStart;

	coordList.usedTail = sortStart;
}

#ifdef _DEBUG_REGION
void neCoordList::OuputDebug()
{
	neDLinkList<CCoordListEntry>::iterator iter;

	zDebugMessage("Coord %d\n", this->dim);

	for (iter = coordList.BeginUsed(); iter.Valid(); iter++)
	{
		CCoordListEntry * c = (*iter);

		char * high = "High";
		char * low = "Low";

		char * fs;

		if (c->flag == CCoordListEntry::LowEnd)
			fs = low;
		else
			fs = high;
		
		zDebugMessage("object id = %d   %s    %f \n", c->bb->id, fs, c->value);
	}
}
#endif //_DEBUG
