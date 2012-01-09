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

//#include <assert.h>
#include <stdio.h>
#ifdef _WIN32
#include <windows.h>
#endif

char neFixedTimeStepSimulator::logBuffer[256];

//extern void DrawLine(const neV3 & colour, neV3 * startpoint, s32 count);

extern s32 currentMicroStep;

#define NE_HIGH_ENERGY 1.0f

void ChooseAxis(neV3 & x, neV3 & y, const neV3 & normal);

/****************************************************************************
*
*	neFixedTimeStepSimulator::neFixedTimeStepSimulator(
*
****************************************************************************/ 

neFixedTimeStepSimulator::neFixedTimeStepSimulator(const neSimulatorSizeInfo & _sizeInfo, neAllocatorAbstract * alloc, const neV3 * grav)
{
	sizeInfo = _sizeInfo;
	
	if (alloc)
	{
		allocator = alloc;
	}
	else
	{
		allocator = &allocDef;
	}
	neV3 g;

	if (grav)
		g = *grav;
	else
		g.SetZero();

	neFixedTimeStepSimulator::Initialise(g);

	for (int i=0; i < MAX_MATERIAL; i++)
	{
		materials[i].density = 1.0f;
		materials[i].friction = .5f;
		materials[i].resititution = 0.4f;
	}
	perfReport = NULL;

	treeNodes.Reserve(100, allocator, 100);

	triangleIndex.Reserve(200, allocator, 200);

	constraintHeap.Reserve(sizeInfo.constraintsCount , allocator);

	constraintHeaders.Reserve(sizeInfo.constraintSetsCount , allocator);

	//miniConstraintHeap.Reserve(sizeInfo.constraintBufferSize, allocator);

	stackInfoHeap.Reserve(sizeInfo.rigidBodiesCount + sizeInfo.rigidParticleCount , allocator);

	stackHeaderHeap.Reserve(sizeInfo.rigidBodiesCount  + sizeInfo.rigidParticleCount + 100, allocator);

	controllerHeap.Reserve(sizeInfo.controllersCount, allocator);

	sensorHeap.Reserve(sizeInfo.sensorsCount, allocator);

	geometryHeap.Reserve(sizeInfo.geometriesCount, allocator);

	pointerBuffer1.Reserve(1000, allocator, 100);

	pointerBuffer2.Reserve(1000, allocator, 100);

	cresultHeap.Reserve(100, allocator, 100);

	cresultHeap2.Reserve(100, allocator, 100);

	//fastImpulseHeap.Reserve(500, allocator);

	logCallback = NULL;

	collisionCallback = NULL;

	breakageCallback = NULL;

	terrainQueryCallback = NULL;

	customCDRB2RBCallback = NULL;

	customCDRB2ABCallback = NULL;

	logLevel = neSimulator::LOG_OUTPUT_LEVEL_NONE;

//	solver.sim = this;

	fakeCollisionBody.moved = false;

	fakeCollisionBody.sim = this;

	fakeCollisionBody.id = -1;

	fakeCollisionBody.cookies = 0;

	fakeCollisionBody.isActive = true;

	highEnergy = NE_HIGH_ENERGY;

#ifdef _WIN32
	perf = nePerformanceData::Create();

	perf->Init();
#else
	perf = NULL;
#endif

	timeFromLastFrame = 0.0f;

	lastTimeStep = 0;
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::Initialise
*
****************************************************************************/ 
void neFixedTimeStepSimulator::SetGravity(const neV3 & g)
{
	gravity = g;

	gravityVector = gravity;

	gravityVector.Normalize();

	restingSpeed = sqrtf(gravity.Dot(gravity));

	//restingSpeed = sqrtf(restingSpeed * 2.0e-2f) * 4.0f;

	restingSpeed = restingSpeed * 0.3f;

	gravityMag = g.Length();

	if (!neIsFinite(gravityMag))
	{
		gravityMag = 0.0f;
	}
}

void neFixedTimeStepSimulator::Initialise(const neV3& _gravity)
{
	buildCoordList = true;

	SetGravity(_gravity);

	maxRigidBodies = sizeInfo.rigidBodiesCount;

	maxAnimBodies = sizeInfo.animatedBodiesCount;

	maxParticles = sizeInfo.rigidParticleCount;

	if (!rigidBodyHeap.Reserve(maxRigidBodies, allocator))
	{
		sprintf(logBuffer, MSG_MEMORY_ALLOC_FAILED);

		LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

		return;
	}

	if (!collisionBodyHeap.Reserve(maxAnimBodies, allocator))
	{
		sprintf(logBuffer, MSG_MEMORY_ALLOC_FAILED);

		LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

		return;
	}

	if (!rigidParticleHeap.Reserve(maxParticles, allocator))
	{
		sprintf(logBuffer, MSG_MEMORY_ALLOC_FAILED);

		LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

		return;
	}

	region.Initialise(this, neRegion::SORT_DIMENSION_X | neRegion::SORT_DIMENSION_Y | neRegion::SORT_DIMENSION_Z);

	stepSoFar = 0;

	stackHeaderX.Null();

	stackHeaderX.isHeaderX = true;

	stackHeaderX.sim = this;

	fakeCollisionBody.b2w.SetIdentity();
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::SetCollisionCallback
*
****************************************************************************/ 

neCollisionCallback * neFixedTimeStepSimulator::SetCollisionCallback(neCollisionCallback * fn)
{
	neCollisionCallback * ret = collisionCallback;

	collisionCallback = fn;

	return ret;
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::SetLogOutputCallback
*
****************************************************************************/ 

neLogOutputCallback * neFixedTimeStepSimulator::SetLogOutputCallback(neLogOutputCallback * fn)
{
	neLogOutputCallback * ret = logCallback;

	logCallback = fn;

	return ret;
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::SetLogOutputLevel
*
****************************************************************************/ 

void neFixedTimeStepSimulator::SetLogOutputLevel(neSimulator::LOG_OUTPUT_LEVEL lvl)
{
	logLevel = lvl;
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::LogOutput
*
****************************************************************************/ 

void neFixedTimeStepSimulator::LogOutput(neSimulator::LOG_OUTPUT_LEVEL lvl)
{
	if (!logCallback)
		return;

	if (lvl <= logLevel)
		logCallback(logBuffer);
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::SetMaterial
*
****************************************************************************/ 

bool neFixedTimeStepSimulator::SetMaterial(s32 index, f32 friction, f32 restitution, f32 density)
{
	if (index < 0)
		return false;

	if (index >= MAX_MATERIAL)
		return false;

	materials[index].density = density;
	materials[index].friction = friction;
	materials[index].resititution = restitution;

	return true;
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::GetMaterial
*
****************************************************************************/ 

bool neFixedTimeStepSimulator::GetMaterial(s32 index, f32& friction, f32& restitution, f32& density)
{
	if (index < 0)
		return false;

	if (index >= MAX_MATERIAL)
		return false;

	density = materials[index].density;
	friction = materials[index].friction;
	restitution = materials[index].resititution;

	return true;	
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::CreateRigidBody
*
****************************************************************************/ 

neRigidBody_* neFixedTimeStepSimulator::CreateRigidBody(neBool isParticle)
{
	neRigidBody_ * ret;

	if (!isParticle)
	{
		ret = rigidBodyHeap.Alloc(1);

		if (!ret)
		{
			sprintf(logBuffer, MSG_RUN_OUT_RIDIGBODY);

			LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

			return NULL;
		}

		//ASSERT(ret);

		new (ret) neRigidBody_;

		activeRB.Add(ret);

		ret->id = rigidBodyHeap.GetID(ret);

		ret->subType = NE_RIGID_NORMAL;
	}
	else
	{
		ret = rigidParticleHeap.Alloc(1);

		if (!ret)
		{
			sprintf(logBuffer, MSG_RUN_OUT_RIDIGPARTICLE);

			LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);

			return NULL;
		}
		//ASSERT(ret);

		new (ret) neRigidBody_;

		activeRP.Add(ret);

		ret->id = rigidParticleHeap.GetID(ret) + rigidBodyHeap.Size() + collisionBodyHeap.Size();

		ret->subType = NE_RIGID_PARTICLE;
	}

	ret->col.convexCount = 0;

	ret->col.obb.Initialise();

	ret->sim = this;
	
	return ret;
}

neRigidBody_ * neFixedTimeStepSimulator::CreateRigidBodyFromConvex(TConvex * convex, neRigidBodyBase * originalBody)
{
	//make sure convex belong to this body and
	//this convex is not the only convex on this body

	originalBody->BeginIterateGeometry();

	TConvex * con;

	s32 ccount = 0;

	neBool found = false;

	while (con = originalBody->GetNextGeometry())
	{
		if (con == convex)
		{
			found = true;
		}
		ccount++;
	}

	if (ccount == 1 || !found)
	{
		return NULL;
	}
	neBool isParticle = false;

	if (convex->breakInfo.flag == neGeometry::NE_BREAK_ALL_PARTICLE ||
		convex->breakInfo.flag == neGeometry::NE_BREAK_NORMAL_PARTICLE ||
		convex->breakInfo.flag == neGeometry::NE_BREAK_NEIGHBOUR_PARTICLE)
	{
		isParticle = true;
	}

	neRigidBody_ * newBody = CreateRigidBody(false);

	if (!newBody)
	{
		return NULL;
	}

	newBody->mass = convex->breakInfo.mass;

	newBody->oneOnMass = 1.0f / convex->breakInfo.mass;

	newBody->Ibody.SetIdentity();

	for (s32 i = 0; i < 3; i++)
		newBody->Ibody[i][i] = convex->breakInfo.inertiaTensor[i];

	newBody->IbodyInv.SetInvert(newBody->Ibody);

	convex->breakInfo.flag = neGeometry::NE_BREAK_DISABLE;

	newBody->State().b2w = originalBody->GetB2W() * convex->c2p;

	newBody->State().q.SetupFromMatrix3(newBody->State().rot()); 

	originalBody->col.convexCount--;

	if (originalBody->col.convex == convex)
		originalBody->col.convex = (TConvex*)((TConvexItem *)convex)->next;

	if (originalBody->col.convexCount == 0 && originalBody->isActive)
	{
		region.RemoveBody(originalBody);
	}
	else if (originalBody->col.convexCount == 1)
	{
		originalBody->RecalcBB();
	}

	((TConvexItem *)convex)->Remove();

	convex->c2p.SetIdentity();

	newBody->col.convex = convex;
	
	newBody->col.convexCount++;

	newBody->RecalcBB();

	region.AddBody(newBody, originalBody);

	return newBody;
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::CreateAnimateBody
*
****************************************************************************/ 

neCollisionBody_* neFixedTimeStepSimulator::CreateCollisionBody()
{
	neCollisionBody_ * ret =  collisionBodyHeap.Alloc(1);

	//ASSERT(ret);

	new (ret) neCollisionBody_;

	activeCB.Add(ret);

	ret->id = collisionBodyHeap.GetID(ret) + rigidBodyHeap.Size();

	ret->col.convexCount = 0;

	ret->col.obb.Initialise();

	ret->sim = this;

	ret->b2w.SetIdentity();
	
	//region.AddBody(ret);

	return ret;
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::Free
*
****************************************************************************/ 

void neFixedTimeStepSimulator::Free(neRigidBodyBase * bb)
{
	if (bb->AsCollisionBody())
	{
		neCollisionBody_* cb = reinterpret_cast<neCollisionBody_*>(bb);

		if (collisionBodyHeap.CheckBelongAndInUse(cb))
		{
			((neCollisionBody_*)bb)->Free();

			if (bb->isActive)
				activeCB.Remove(cb);
			else
				inactiveCB.Remove(cb);

			collisionBodyHeap.Dealloc(cb, 1);
		}
		else
		{
			sprintf(logBuffer, MSG_TRYING_TO_FREE_INVALID_CB);

			LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
		}
	}
	else
	{
		neRigidBody_* rb = reinterpret_cast<neRigidBody_*>(bb);

		rb->Free();

		if (rb->IsParticle())
		{
			if (rigidParticleHeap.CheckBelongAndInUse(rb))
			{
				if (rb->isActive)
					activeRP.Remove(rb);
				else
					inactiveRP.Remove(rb);

				rigidParticleHeap.Dealloc(rb, 1);
			}
			else
			{
				sprintf(logBuffer, MSG_TRYING_TO_FREE_INVALID_RP);

				LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
			}
		}
		else
		{
			if (rigidBodyHeap.CheckBelongAndInUse(rb))
			{
				if (rb->isActive)
					activeRB.Remove(rb);
				else
					inactiveRB.Remove(rb);

				rigidBodyHeap.Dealloc(rb, 1);
			}
			else
			{
				sprintf(logBuffer, MSG_TRYING_TO_FREE_INVALID_RB);

				LogOutput(neSimulator::LOG_OUTPUT_LEVEL_ONE);
			}
		}
	}
}

/****************************************************************************
*
*	~neFixedTimeStepSimulator::neFixedTimeStepSimulator
*
****************************************************************************/ 

neFixedTimeStepSimulator::~neFixedTimeStepSimulator()
{
	FreeAllBodies();

	if (perf)
		delete perf;
}

///////////////////////////////////////////////////////////////////

//#define DETAIL_PERF_REPORTING

#ifdef _WIN32
#define UPDATE_PERF_REPORT(n) {if (perfReport) perf->n();}
#else
#define UPDATE_PERF_REPORT(n)
#endif

void neFixedTimeStepSimulator::Advance(nePerformanceReport * _perfReport)
{
	ClearCollisionBodySensors();
		
	UpdateAABB();

	region.Update();

UPDATE_PERF_REPORT(UpdateCDCulling)

	CheckCollision();

UPDATE_PERF_REPORT(UpdateCD);

	CheckTerrainCollision();

UPDATE_PERF_REPORT(UpdateTerrain);

	ResetTotalForce();
		
	ApplyJointDamping();
	
	//Advance Rigid Body Dynamic
	AdvanceDynamicRigidBodies();
		
	//Advance Rigid Particle Dynamic
	AdvanceDynamicParticles();

UPDATE_PERF_REPORT(UpdateDynamic);

	ResetStackHeaderFlag();

	SolveAllConstrain();	

//UPDATE_PERF_REPORT(UpdateConstrain1);

	ResolvePenetration();

	SolveContactConstrain();

UPDATE_PERF_REPORT(UpdateConstrain2);

	//Advance Position

	AdvancePositionRigidBodies();

	AdvancePositionParticles();

UPDATE_PERF_REPORT(UpdatePosition);

	UpdateConstraintControllers();

UPDATE_PERF_REPORT(UpdateControllerCallback);
}

void neFixedTimeStepSimulator::Advance(f32 time, u32 nStep, nePerformanceReport * _perfReport)
{
	_currentTimeStep = time / (f32)nStep;

	oneOnCurrentTimeStep = 1.0f / _currentTimeStep;

	perfReport = _perfReport;

	currentRecord = stepSoFar % NE_RB_MAX_PAST_RECORDS;

#ifdef _WIN32
	if (perfReport)
	{
		for (s32 j = 0; j < nePerformanceReport::NE_PERF_LAST; j++)
		{
			perfReport->time[j] = 0.0f;
		}
		perf->Start();
	}
#endif

	int i;

	for (i = 0; i < (s32)nStep; i++)
	{
		magicNumber = 0;

		Advance(perfReport);
	}

	neCollisionBody_ * cb = activeCB.GetHead();

	while (cb)
	{
		cb->moved = false;

		cb = activeCB.GetNext(cb);
	}
	if (perfReport)
	{
		if (perfReport->reportType == nePerformanceReport::NE_PERF_SAMPLE)
		{
			f32 totalTime = perfReport->time[nePerformanceReport::NE_PERF_TOTAL_TIME] = perf->GetTotalTime();

#ifdef DETAIL_PERF_REPORTING

			perfReport->time[nePerformanceReport::NE_PERF_DYNAMIC] = perf->dynamic / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_POSITION] = perf->position / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_COLLISION_DETECTION] = perf->cd / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_COLLISION_CULLING] = perf->cdCulling / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_TERRAIN] = perf->terrain / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_TERRAIN_CULLING] = perf->terrainCulling / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] = perf->constrain_1 / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] = perf->constrain_2 / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] = perf->controllerCallback / totalTime * 100.0f;;
#endif
		}
		else
		{
			f32 totalTime = perf->GetTotalTime();

			if (totalTime < 100.0f)
			{
				perfReport->numSample ++;

				perfReport->accTime[nePerformanceReport::NE_PERF_TOTAL_TIME] += totalTime;

#ifdef DETAIL_PERF_REPORTING				
				perfReport->accTime[nePerformanceReport::NE_PERF_DYNAMIC] += (perf->dynamic / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_POSITION] += (perf->position / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_DETECTION] += (perf->cd / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_CULLING] += (perf->cdCulling / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN] += (perf->terrain / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN_CULLING] += (perf->terrainCulling / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] += (perf->controllerCallback / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] += (perf->constrain_1 / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] += (perf->constrain_2 / totalTime * 100.0f);
#endif
				perfReport->time[nePerformanceReport::NE_PERF_TOTAL_TIME] = perfReport->accTime[nePerformanceReport::NE_PERF_TOTAL_TIME] / perfReport->numSample;
#ifdef DETAIL_PERF_REPORTING
				perfReport->time[nePerformanceReport::NE_PERF_DYNAMIC] = perfReport->accTime[nePerformanceReport::NE_PERF_DYNAMIC] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_POSITION] = perfReport->accTime[nePerformanceReport::NE_PERF_POSITION] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_COLLISION_DETECTION] = perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_DETECTION] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_COLLISION_CULLING] = perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_CULLING] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_TERRAIN] = perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_TERRAIN_CULLING] = perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN_CULLING] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] = perfReport->accTime[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] = perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] = perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] / perfReport->numSample;
#endif
			}
		}
	}

	stepSoFar++;
}

void neFixedTimeStepSimulator::Advance(f32 sec, f32 minTimeStep, f32 maxTimeStep, nePerformanceReport * _perfReport)
{
	perfReport = _perfReport;

#ifdef _WIN32
	if (perfReport)
	{
		for (s32 j = 0; j < nePerformanceReport::NE_PERF_LAST; j++)
		{
			perfReport->time[j] = 0.0f;
		}
		perf->Start();
	}
#endif

	const f32 frameDiffTolerance = 0.2f;
	
	f32 timeLeft = sec + timeFromLastFrame;
	
	f32 currentTimeStep = maxTimeStep;

	while (minTimeStep <= timeLeft)
	{
		while (currentTimeStep <= timeLeft)
		{
			if ((lastTimeStep > 0.0f) && neIsFinite(lastTimeStep))
			{
				f32 diffPercent = neAbs((currentTimeStep - lastTimeStep) / lastTimeStep);

				if (diffPercent > frameDiffTolerance) // more than 20% different
				{
					if (currentTimeStep > lastTimeStep)
					{
						currentTimeStep = lastTimeStep * (1.0f + frameDiffTolerance);
					}
					else
					{
						currentTimeStep = lastTimeStep * (1.0f - frameDiffTolerance);
					}
				}
			}

			_currentTimeStep = currentTimeStep;

			oneOnCurrentTimeStep = 1.0f / _currentTimeStep;

			currentRecord = stepSoFar % NE_RB_MAX_PAST_RECORDS;

			Advance(perfReport);

			stepSoFar++;
			
			timeLeft -= currentTimeStep;

			lastTimeStep = currentTimeStep;
		}
		currentTimeStep = neMin(timeLeft, maxTimeStep);
	}
	timeFromLastFrame = timeLeft;

	neCollisionBody_ * cb = activeCB.GetHead();

	while (cb)
	{
		cb->moved = false;

		cb = activeCB.GetNext(cb);
	}
	if (perfReport)
	{
		if (perfReport->reportType == nePerformanceReport::NE_PERF_SAMPLE)
		{
			f32 totalTime = perfReport->time[nePerformanceReport::NE_PERF_TOTAL_TIME] = perf->GetTotalTime();

#ifdef DETAIL_PERF_REPORTING

			perfReport->time[nePerformanceReport::NE_PERF_DYNAMIC] = perf->dynamic / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_POSITION] = perf->position / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_COLLISION_DETECTION] = perf->cd / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_COLLISION_CULLING] = perf->cdCulling / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_TERRAIN] = perf->terrain / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_TERRAIN_CULLING] = perf->terrainCulling / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] = perf->constrain_1 / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] = perf->constrain_2 / totalTime * 100.0f;
			perfReport->time[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] = perf->controllerCallback / totalTime * 100.0f;;
#endif
		}
		else
		{
			f32 totalTime = perf->GetTotalTime();

			if (totalTime < 100.0f)
			{
				perfReport->numSample ++;

				perfReport->accTime[nePerformanceReport::NE_PERF_TOTAL_TIME] += totalTime;

#ifdef DETAIL_PERF_REPORTING				
				perfReport->accTime[nePerformanceReport::NE_PERF_DYNAMIC] += (perf->dynamic / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_POSITION] += (perf->position / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_DETECTION] += (perf->cd / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_CULLING] += (perf->cdCulling / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN] += (perf->terrain / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN_CULLING] += (perf->terrainCulling / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] += (perf->controllerCallback / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] += (perf->constrain_1 / totalTime * 100.0f);
				perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] += (perf->constrain_2 / totalTime * 100.0f);
#endif
				perfReport->time[nePerformanceReport::NE_PERF_TOTAL_TIME] = perfReport->accTime[nePerformanceReport::NE_PERF_TOTAL_TIME] / perfReport->numSample;
#ifdef DETAIL_PERF_REPORTING
				perfReport->time[nePerformanceReport::NE_PERF_DYNAMIC] = perfReport->accTime[nePerformanceReport::NE_PERF_DYNAMIC] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_POSITION] = perfReport->accTime[nePerformanceReport::NE_PERF_POSITION] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_COLLISION_DETECTION] = perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_DETECTION] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_COLLISION_CULLING] = perfReport->accTime[nePerformanceReport::NE_PERF_COLLISION_CULLING] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_TERRAIN] = perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_TERRAIN_CULLING] = perfReport->accTime[nePerformanceReport::NE_PERF_TERRAIN_CULLING] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] = perfReport->accTime[nePerformanceReport::NE_PERF_CONTROLLER_CALLBACK] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] = perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_1] / perfReport->numSample;
				perfReport->time[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] = perfReport->accTime[nePerformanceReport::NE_PERF_CONTRAIN_SOLVING_2] / perfReport->numSample;
#endif
			}
		}
	}
}

void neFixedTimeStepSimulator::ResetTotalForce()
{
	neRigidBody_ * rb = activeRB.GetHead();

	while (rb)
	{
		rb->totalForce.SetZero();

		rb->totalTorque.SetZero();

		rb = activeRB.GetNext(rb);
	}
	rb = activeRP.GetHead();

	while (rb)
	{
		rb->totalForce.SetZero();

		rb->totalTorque.SetZero();

		rb = activeRP.GetNext(rb);
	}
}

void neFixedTimeStepSimulator::AdvanceDynamicRigidBodies()
{
	neRigidBody_ * rb = activeRB.GetHead();

	idleBodyCount = 0;

	while (rb)
	{
		rb->AdvanceDynamic(_currentTimeStep);

		rb = activeRB.GetNext(rb);
	}
}		

void neFixedTimeStepSimulator::AdvanceDynamicParticles()
{
	neRigidBody_ * rp = activeRP.GetHead();

	while (rp)
	{
		rp->AdvanceDynamic(_currentTimeStep);

		rp = activeRP.GetNext(rp);
	}
}

void neFixedTimeStepSimulator::AdvancePositionRigidBodies()
{
	neRigidBody_ * rb = activeRB.GetHead();

	while (rb)
	{
		rb->needSolveContactDynamic = true;

		if (rb->status != neRigidBody_::NE_RBSTATUS_IDLE)
		{
			if (!rb->_constraintHeader)
			{
				rb->CheckForIdle();
			}

			if (rb->status != neRigidBody_::NE_RBSTATUS_IDLE)
				rb->AdvancePosition(_currentTimeStep);
		}

		rb = activeRB.GetNext(rb);
	}
}
	
void neFixedTimeStepSimulator::AdvancePositionParticles()
{
	neRigidBody_ * rp = activeRP.GetHead();

	while (rp)
	{
		rp->needSolveContactDynamic = true;

		if (rp->status != neRigidBody_::NE_RBSTATUS_IDLE)
		{
			if (!rp->_constraintHeader)
			{
				rp->CheckForIdle();
			}

			if (rp->status != neRigidBody_::NE_RBSTATUS_IDLE)
				rp->AdvancePosition(_currentTimeStep);
		}

		rp = activeRP.GetNext(rp);
	}
}

void neFixedTimeStepSimulator::ApplyJointDamping()
{
	neFreeListItem<neConstraintHeader> * hitem = (neFreeListItem<neConstraintHeader> *)(*constraintHeaders.BeginUsed());

	while (hitem)
	{
		neConstraintHeader * h = (neConstraintHeader *)hitem;

		hitem = hitem->next;

		neFreeListItem<_neConstraint> * citem = (neFreeListItem<_neConstraint> *) h->head;
		
		while (citem)
		{
			_neConstraint * c = (_neConstraint *)citem;	

			citem = citem->next;

			if (c->enable)
				c->ApplyDamping();
		}
	}
}

void neFixedTimeStepSimulator::ClearCollisionBodySensors()
{
	neCollisionBody_ * cb = activeCB.GetHead();

	while (cb)
	{
		if (cb->sensors)
			cb->ClearSensor();

		cb = activeCB.GetNext(cb);
	}

	neRigidBody_ * rp = activeRP.GetHead();

	while (rp)
	{
		if (rp->sensors)
			rp->ClearSensor();

		rp = activeRP.GetNext(rp);
	}

	rp = activeRB.GetHead();

	while (rp)
	{
		if (rp->sensors)
			rp->ClearSensor();

		rp = activeRP.GetNext(rp);
	}
}

void neFixedTimeStepSimulator::UpdateAABB()
{
	neRigidBody_ * rb = activeRB.GetHead();

	while (rb)
	{
		rb->UpdateAABB();

		rb = activeRB.GetNext(rb);
	}
	neRigidBody_ * rp = activeRP.GetHead();

	while (rp)
	{
		rp->UpdateAABB();

		rp = activeRP.GetNext(rp);
	}
	neCollisionBody_ * cb = activeCB.GetHead();

	while (cb)
	{
		if (cb->moved)
			cb->UpdateAABB();

		cb = activeCB.GetNext(cb);
	}
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::CheckCollision
*
****************************************************************************/ 

void neFixedTimeStepSimulator::CheckCollision()
{
	//OutputDebugString("region.Update\n");/////////////////////////////////

	neDLinkList<neOverlappedPair>::iterator oiter;

	neCollisionResult result;

	neV3 backupVector;
	//OutputDebugString("obj 2 obj test\n");/////////////////////////////////

	for (oiter = region.overlappedPairs.BeginUsed(); oiter.Valid(); oiter++)
	{
		//ASSERT((*oiter)->bodyA);
		//ASSERT((*oiter)->bodyB);
		
		neRigidBodyBase * bodyA;
		neRigidBodyBase * bodyB;

		bodyA = (*oiter)->bodyA;
		bodyB = (*oiter)->bodyB;

		neRigidBody_* ra = bodyA->AsRigidBody();
		neRigidBody_* rb = bodyB->AsRigidBody();

		neCollisionBody_* ca = bodyA->AsCollisionBody();
		neCollisionBody_* cb = bodyB->AsCollisionBody();
		
		if (ca && cb)
			continue;

		neCollisionTable::neReponseBitFlag collisionflag = colTable.Get(bodyA->cid, bodyB->cid);

		if (collisionflag == neCollisionTable::RESPONSE_IGNORE)
			continue;
		
		neBool isCustomeCD = false;

		result.penetrate = false;

		if (ca)
		{
			if (rb->status != neRigidBody_::NE_RBSTATUS_IDLE ||
				rb->isShifted ||
				ca->moved)
			{
				if ((rb->isCustomCD || ca->isCustomCD))
				{
					if (customCDRB2ABCallback)
					{
						isCustomeCD = true;

						neCustomCDInfo cdInfo;

						memset(&cdInfo, 0, sizeof(cdInfo));

						if (customCDRB2ABCallback((neRigidBody*)rb, (neAnimatedBody*)ca, cdInfo))
						{
							result.penetrate = true;
							result.bodyA = ca;
							result.bodyB = rb;
							result.collisionFrame[2] = -cdInfo.collisionNormal;
							result.materialIdA = cdInfo.materialIdB;
							result.materialIdB = cdInfo.materialIdA;
							result.contactA = cdInfo.worldContactPointB - ca->GetB2W().pos;
							result.contactB = cdInfo.worldContactPointA - rb->GetB2W().pos;
							result.contactAWorld = cdInfo.worldContactPointB;
							result.contactBWorld = cdInfo.worldContactPointA;
							result.contactABody = ca->GetB2W().rot.TransposeMulV3(result.contactA);
							result.contactBBody = rb->GetB2W().rot.TransposeMulV3(result.contactB);
							result.depth = cdInfo.penetrationDepth;
							ChooseAxis(result.collisionFrame[0], result.collisionFrame[1], result.collisionFrame[2]);
						}
					}
				}
				else
				{
					backupVector = rb->backupVector - ca->backupVector;

					CollisionTest(result, ca->col, ca->b2w, 
											rb->col, rb->State().b2w, backupVector);

					if (rb->sensors)
					{
						CollisionTestSensor(&rb->col.obb,
											rb->sensors,
											rb->State().b2w,
											ca->col,
											ca->b2w,
											ca);
					}
				}
			}
		}
		else
		{
			if (cb)
			{
				if (ra->status != neRigidBody_::NE_RBSTATUS_IDLE ||
					ra->isShifted ||
					cb->moved)
				{
					if ((ra->isCustomCD || cb->isCustomCD))
					{
						if (customCDRB2ABCallback)
						{
							isCustomeCD = true;

							neCustomCDInfo cdInfo;

							memset(&cdInfo, 0, sizeof(cdInfo));

							if (customCDRB2ABCallback((neRigidBody*)ra, (neAnimatedBody*)cb, cdInfo))
							{
								result.penetrate = true;
								result.bodyA = ra;
								result.bodyB = cb;
								result.collisionFrame[2] = cdInfo.collisionNormal;
								result.materialIdA = cdInfo.materialIdA;
								result.materialIdB = cdInfo.materialIdB;
								result.contactA = cdInfo.worldContactPointA - ra->GetB2W().pos;
								result.contactB = cdInfo.worldContactPointB - cb->GetB2W().pos;
								result.contactAWorld = cdInfo.worldContactPointA;
								result.contactBWorld = cdInfo.worldContactPointB;
								result.contactABody = ra->GetB2W().rot.TransposeMulV3(result.contactA);
								result.contactBBody = cb->GetB2W().rot.TransposeMulV3(result.contactB);
								result.depth = cdInfo.penetrationDepth;
								ChooseAxis(result.collisionFrame[0], result.collisionFrame[1], result.collisionFrame[2]);
							}
						}
					}
					else
					{
						backupVector = cb->backupVector - ra->backupVector;

						CollisionTest(result, ra->col, ra->State().b2w,
										cb->col, cb->b2w, backupVector);
						
						if (ra->sensors)
						{
							CollisionTestSensor(&ra->col.obb,
												ra->sensors,
												ra->State().b2w,
												cb->col,
												cb->b2w,
												cb);
						}
					}
				}
			}
			else
			{
				neBool doCollision = false;
				
				if (ra->GetConstraintHeader() && 
					(ra->GetConstraintHeader() == rb->GetConstraintHeader()))
				{
					if (ra->isCollideConnected && rb->isCollideConnected)
					{
						if (ra->status != neRigidBody_::NE_RBSTATUS_IDLE ||
							rb->status != neRigidBody_::NE_RBSTATUS_IDLE)
	
							doCollision = true;
					}
				}
				else
				{
					if (ra->status != neRigidBody_::NE_RBSTATUS_IDLE ||
						rb->status != neRigidBody_::NE_RBSTATUS_IDLE || 
						ra->isShifted ||
						rb->isShifted)
					{
						doCollision = true;
					}
				}
				if (doCollision)
				{
					if ((ra->isCustomCD || rb->isCustomCD))
					{
						if (customCDRB2RBCallback)
						{
							isCustomeCD = true;

							neCustomCDInfo cdInfo;

							memset(&cdInfo, 0, sizeof(cdInfo));

							if (customCDRB2RBCallback((neRigidBody*)ra, (neRigidBody*)rb, cdInfo))
							{
								result.penetrate = true;
								result.bodyA = ra;
								result.bodyB = rb;
								result.collisionFrame[2] = cdInfo.collisionNormal;
								result.materialIdA = cdInfo.materialIdA;
								result.materialIdB = cdInfo.materialIdB;
								result.contactA = cdInfo.worldContactPointA - ra->GetB2W().pos;
								result.contactB = cdInfo.worldContactPointB - rb->GetB2W().pos;
								result.contactAWorld = cdInfo.worldContactPointA;
								result.contactBWorld = cdInfo.worldContactPointB;
								result.contactABody = ra->GetB2W().rot.TransposeMulV3(result.contactA);
								result.contactBBody = rb->GetB2W().rot.TransposeMulV3(result.contactB);
								result.depth = cdInfo.penetrationDepth;
								ChooseAxis(result.collisionFrame[0], result.collisionFrame[1], result.collisionFrame[2]);
							}
						}
					}
					else
					{
						backupVector = rb->backupVector - ra->backupVector;

						CollisionTest(result, ra->col, ra->State().b2w,
											rb->col, rb->State().b2w, backupVector);
						if (ra->sensors)
						{
							CollisionTestSensor(&ra->col.obb,
												ra->sensors,
												ra->State().b2w,
												rb->col,
												rb->State().b2w,
												rb);
						}
						if (rb->sensors)
						{
							CollisionTestSensor(&rb->col.obb,
												rb->sensors,
												rb->State().b2w,
												ra->col,
												ra->State().b2w,
												ra);
						}
					}
				}
			}
		}
//		if (perfReport)
//			perf.UpdateCD();

		if (result.penetrate)
		{
			neBool bothAnimated = false;

			if (ra && ra->status == neRigidBody_::NE_RBSTATUS_ANIMATED &&
				rb && rb->status == neRigidBody_::NE_RBSTATUS_ANIMATED)
			{
				bothAnimated = true;
			}
			neBool response = true;

			if (!result.collisionFrame[2].IsFinite() || result.collisionFrame[2].IsConsiderZero())
			{
				response = false;
			}

			result.impulseType = IMPULSE_NORMAL;

			if ((collisionflag & neCollisionTable::RESPONSE_IMPULSE) && 
				response &&
				(!bothAnimated))
			{
				result.bodyA = bodyA;
				result.bodyB = bodyB;
				RegisterPenetration(bodyA, bodyB, result);
			}
			if ((collisionflag & neCollisionTable::RESPONSE_CALLBACK) && collisionCallback && !isCustomeCD)
			{
				static neCollisionInfo cinfo;

				cinfo.bodyA = (neByte *)bodyA;
				cinfo.bodyB = (neByte *)bodyB;
				cinfo.typeA = bodyA->btype == NE_OBJECT_COLISION? NE_ANIMATED_BODY : NE_RIGID_BODY;
				cinfo.typeB = bodyB->btype == NE_OBJECT_COLISION? NE_ANIMATED_BODY : NE_RIGID_BODY;
				cinfo.materialIdA = result.materialIdA;
				cinfo.materialIdB = result.materialIdB;
				cinfo.geometryA = (neGeometry*)result.convexA;
				cinfo.geometryB = (neGeometry*)result.convexB;
				cinfo.bodyContactPointA = result.contactABody;
				cinfo.bodyContactPointB = result.contactBBody;
				cinfo.worldContactPointA = result.contactAWorld;
				cinfo.worldContactPointB = result.contactBWorld;
				cinfo.relativeVelocity = result.initRelVelWorld;
				cinfo.collisionNormal = result.collisionFrame[2];

				collisionCallback(cinfo);
			}
		}
	}
}
	//OutputDebugString("terrain test\n");/////////////////////////////////

void neFixedTimeStepSimulator::CheckTerrainCollision()
{
	neCollisionResult result;

	neTreeNode & rootNode = region.GetTriangleTree().GetRoot();

	neT3 identity;

	identity.SetIdentity();

	neCollision & triCol = fakeCollisionBody.col;

	triCol.convexCount = 1;

	triCol.obb.SetTransform(identity);

	neV3 backupVector;

	for (s32 mop = 0; mop < 2; mop++)
	{
		neList<neRigidBody_> * activeList = &activeRB; 
		
		if (mop == 1)
		{
			activeList = &activeRP;

		}
		neRigidBody_ * rb = activeList->GetHead();

		//for (riter = rbHeap.BeginUsed(); riter.Valid(); riter++)
		while (rb)
		{
			neRigidBody_ * bodyA = (rb);

			if (bodyA->status == neRigidBody_::NE_RBSTATUS_IDLE &&
				!bodyA->isShifted)
			{
				rb = activeList->GetNext(rb);

				continue;
			}
			backupVector = -rb->backupVector;
			
			treeNodes.Clear();

			triangleIndex.Clear();
			
			if (!terrainQueryCallback)
			{
				rootNode.GetCandidateNodes(treeNodes, bodyA->minBound, bodyA->maxBound, 0);

				if (treeNodes.GetUsedCount() == 0)
				{
					rb = activeList->GetNext(rb);
					
					continue;
				}

				//printf("node count %d\n", treeNodes.GetUsedCount());
				
				for (s32 i = 0; i < treeNodes.GetUsedCount(); i++)
				{
					neTreeNode * t = treeNodes[i];

					for (s32 j = 0; j < t->triangleIndices.GetUsedCount(); j++)
					{
						s32 k;

						for (k = 0; k < triangleIndex.GetUsedCount(); k++)
						{
							if (t->triangleIndices[j] == triangleIndex[k])
								break;
						}
						if (k == triangleIndex.GetUsedCount())
						{
							s32 * triIndex = triangleIndex.Alloc();

							//ASSERT(triIndex);
			
							*triIndex = t->triangleIndices[j];
						}
					}
				}

#ifdef _WIN32
if (perfReport)
	perf->UpdateTerrainCulling();
#endif
				triCol.obb.SetTerrain(triangleIndex, region.terrainTree.triangles, region.terrainTree.vertices);

				triCol.convex = &triCol.obb;

				CollisionTest(result, bodyA->col, ((neRigidBody_*)bodyA)->State().b2w,
								triCol, identity, backupVector);

				if (bodyA->sensors)
				{
					CollisionTestSensor(&bodyA->col.obb,
										bodyA->sensors,
										bodyA->State().b2w,
										triCol,
										identity,
										NULL);
				}

			}
			else
			{
				neTriangle * tris;
				s32 triCount;
				s32 * candidates;
				s32 candidateCount;
				neV3 *verts;

				static neSimpleArray<s32> _candArray;
				static neArray<neTriangle_> _triArray;
				
				terrainQueryCallback(bodyA->minBound, bodyA->maxBound, &candidates, &tris, &verts, &candidateCount, &triCount, (neRigidBody*)bodyA);

#ifdef _WIN32
if (perfReport)
	perf->UpdateTerrainCulling();
#endif

				_candArray.MakeFromPointer(candidates, candidateCount);

				_triArray.MakeFromPointer((neTriangle_*)tris, triCount);

				triCol.obb.SetTerrain(_candArray, _triArray, verts);

				triCol.convex = &triCol.obb;

				CollisionTest(result, bodyA->col, ((neRigidBody_*)bodyA)->State().b2w,
								triCol, identity, backupVector);

				if (bodyA->sensors)
				{
					CollisionTestSensor(&bodyA->col.obb,
										bodyA->sensors,
										bodyA->State().b2w,
										triCol,
										identity,
										NULL);
				}
				_candArray.MakeFromPointer(NULL, 0);
				_triArray.MakeFromPointer(NULL, 0);
			}
			if (result.penetrate)
			{
				result.impulseType = IMPULSE_NORMAL;

				neCollisionTable::neReponseBitFlag collisionflag = colTable.Get(bodyA->cid, -1); //-1 is terrain

				if ((collisionflag & neCollisionTable::RESPONSE_IMPULSE) &&
					bodyA->status != neRigidBody_::NE_RBSTATUS_ANIMATED)
				{
					result.bodyA = bodyA;
					result.bodyB = &fakeCollisionBody;

					RegisterPenetration(bodyA, &fakeCollisionBody, result);
				}
				if ((collisionflag & neCollisionTable::RESPONSE_CALLBACK) && collisionCallback)
				{
					static neCollisionInfo cinfo;

					cinfo.bodyA = (neByte *)bodyA;
					cinfo.bodyB = (neByte *)result.convexB;
					cinfo.typeA = NE_RIGID_BODY;
					cinfo.typeB = NE_TERRAIN;
					cinfo.materialIdA = result.materialIdA;
					cinfo.materialIdB = result.materialIdB;
					cinfo.geometryA = (neGeometry*)result.convexA;
					cinfo.geometryB = NULL;
					cinfo.bodyContactPointA = result.contactABody;
					cinfo.bodyContactPointB = result.contactBBody;
					cinfo.worldContactPointA = result.contactAWorld;
					cinfo.worldContactPointB = result.contactBWorld;
					cinfo.relativeVelocity = result.initRelVelWorld;
					cinfo.collisionNormal = result.collisionFrame[2];

					collisionCallback(cinfo);
				}
			}

			rb = activeList->GetNext(rb);
		}
	}//mop

	for (s32 mop2 = 0; mop2 < 2; mop2++)
	{
		neList<neRigidBody_> * activeList = &activeRB; 
		
		if (mop2 == 1)
		{
			activeList = &activeRP;

		}
		neRigidBody_ * rb = activeList->GetHead();

		while (rb)
		{
			if (rb->isShifted2)
			{
				rb->isShifted = true;
				rb->isShifted2 = false;
			}
			else
			{
				rb->isShifted = false;
				rb->isShifted2 = false;
			}
			rb = activeList->GetNext(rb);
		}
	}
}
/****************************************************************************
*
*	neFixedTimeStepSimulator::SolveConstrain
*
****************************************************************************/ 
/*
void RecalcRelative(neCollisionResult * cresult)
{
	cresult->initRelVelWorld = cresult->bodyA->VelocityAtPoint(cresult->contactA) * -1.0f;

	if (cresult->bodyB)
		cresult->initRelVelWorld += cresult->bodyB->VelocityAtPoint(cresult->contactB);
	
	cresult->relativeSpeed = cresult->initRelVelWorld.Length();

	if (!cresult->penetrate)
	{
		cresult->collisionFrame[2] = cresult->initRelVelWorld * (1.0f / cresult->relativeSpeed);
	}
	else
	{
		cresult->relativeSpeed = cresult->initRelVelWorld.Dot(cresult->collisionFrame[2]);
		
		if (cresult->relativeSpeed < 0.0f)
			cresult->relativeSpeed = 0.0f;
	}
}
*/
/*
void neFixedTimeStepSimulator::SolveConstrain()
{
	neDLinkList<neConstraintHeader>::iterator chiter;
	
	for (chiter = constraintHeaders.BeginUsed(); chiter.Valid(); chiter++)
	{
		//check if the whole chain is idle
		neConstraintHeader * header = *chiter; 

		neBodyHandle * bodyHandle = header->bodies.GetHead();

		neBool allIdle = true;

		while (bodyHandle)
		{
			neRigidBody_ * rb = bodyHandle->thing->AsRigidBody();

			if (rb && rb->status != neRigidBody_::NE_RBSTATUS_IDLE)
			{
				allIdle = false;

				break;
			}
			bodyHandle = bodyHandle->next;
		}
	
		if (allIdle)
			continue;

		_neConstraint * constraint = header->head;

		constraint = (*chiter)->head;

		while (constraint)
		{
			if (constraint->limitStates[0].enableLimit || 
				constraint->limitStates[1].enableLimit)
			{
				constraint->CheckLimit();
			}
			neFreeListItem<_neConstraint> * item = (neFreeListItem<_neConstraint> *)constraint;

			constraint = (_neConstraint*)(item->next);
		}
	}
}
*/

/****************************************************************************
*
*	neFixedTimeStepSimulator::RegisterPenetration
*
****************************************************************************/ 

void neFixedTimeStepSimulator::RegisterPenetration(neRigidBodyBase * bodyA, neRigidBodyBase * bodyB, neCollisionResult & cresult)
{
	neRigidBody_ * ba = bodyA->AsRigidBody();

	neRigidBody_ * bb = bodyB->AsRigidBody();

	neRestRecord rc;

	neBool isConnected = false;

	if (ba)
	{
		isConnected = ba->IsConstraintNeighbour(bodyB);
	}
	else
	{
		if (bb)
		{
			isConnected = bb->IsConstraintNeighbour(bodyA);
		}
	}
	if (isConnected && !(bodyA->isCollideDirectlyConnected && bodyB->isCollideDirectlyConnected))
	{
		//HandleCollision(bodyA, bodyB, cresult, IMPULSE_NORMAL, 1.0f);
		
		return;
	}
	if (ba && bb)
	{
		if (bb->IsParticle() )
		{
			//ASSERT(!ba->IsParticle());

			CollisionRigidParticle(ba, bb, cresult);

			return;
		}
	}

	rc.depth = cresult.depth;

	f32 alignWithGravity = cresult.collisionFrame[2].Dot(gravityVector);

	//f32 angle = 0.3f;
	f32 angle = 0.3f;

	if (1)//neAbs(alignWithGravity) > angle)
	{
		neV3 velA = bodyA->VelocityAtPoint(cresult.contactA);

		neV3 velB = bodyB->VelocityAtPoint(cresult.contactB);

		cresult.initRelVelWorld = velA - velB;

		if (alignWithGravity < 0.0f) //ba on top
		{
			if (ba)
			{
				cresult.PrepareForSolver();

				HandleCollision(bodyA, bodyB, cresult, IMPULSE_NORMAL, 1.0f);

				neV3 normalBody;

				if (bodyB)
					normalBody = bodyB->GetB2W().rot.TransposeMulV3(cresult.collisionFrame[2]);
				else
					normalBody = cresult.collisionFrame[2];

				rc.SetTmp(bodyB, cresult.contactABody, cresult.contactBBody, normalBody, cresult.materialIdA, cresult.materialIdB);

				ba->AddStackInfo(rc);
			}
			else if (bb)
			{
				SimpleShift(cresult);

				cresult.PrepareForSolver();

				HandleCollision(bodyA, bodyB, cresult, IMPULSE_NORMAL, 1.0f);
			}
		}
		else
		{
			if (bb)
			{
				cresult.PrepareForSolver();

				HandleCollision(bodyA, bodyB, cresult, IMPULSE_NORMAL, 1.0f);

				//if (cresult.relativeSpeed < restingSpeed)
				{
					neV3 normalBody;

					if (bodyA)
						normalBody = bodyA->GetB2W().rot.TransposeMulV3(cresult.collisionFrame[2] * -1.0f);
					else
						normalBody = cresult.collisionFrame[2] * -1.0f;

					rc.SetTmp(bodyA, cresult.contactBBody, cresult.contactABody, normalBody, cresult.materialIdB, cresult.materialIdA);

					bb->AddStackInfo(rc);
				}
			}
			else if (ba)
			{
				SimpleShift(cresult);

				cresult.PrepareForSolver();

				HandleCollision(bodyA, bodyB, cresult, IMPULSE_NORMAL, 1.0f);
			}
		}
	}
	else // not resting collision, resolve now
	{
		if (ba && bb)
		{
			if (!ba->isShifted && ba->status == neRigidBody_::NE_RBSTATUS_IDLE)
			{
				f32 e = ba->Derive().linearVel.Dot(ba->Derive().linearVel);

				e += ba->Derive().angularVel.Dot(ba->Derive().angularVel);

				if (e > highEnergy)
				{
					SimpleShift(cresult);
				}
				else
				{
					bb->SetPos( bb->GetPos() - (cresult.collisionFrame[2] * cresult.depth));

					bb->isShifted2 = true;
				}
			}
			else if (!bb->isShifted && bb->status == neRigidBody_::NE_RBSTATUS_IDLE)
			{
				f32 e = bb->Derive().linearVel.Dot(bb->Derive().linearVel);

				e += bb->Derive().angularVel.Dot(bb->Derive().angularVel);

				if (e > highEnergy)
				{
					SimpleShift(cresult);
				}
				else
				{
					ba->SetPos( ba->GetPos() + (cresult.collisionFrame[2] * cresult.depth));

					ba->isShifted2 = true;
				}
			}
			else
			{
				SimpleShift(cresult);
			}
		}
		else
		{
			SimpleShift(cresult);
		}
		cresult.PrepareForSolver();

		HandleCollision(bodyA, bodyB, cresult, IMPULSE_NORMAL, 1.0f);
	}
}

void neFixedTimeStepSimulator::CollisionRigidParticle(neRigidBody_ * ba, neRigidBody_ * bb, neCollisionResult & cresult)
{
	cresult.PrepareForSolver();

	HandleCollision(ba, bb, cresult, IMPULSE_NORMAL, 1.0f);

	neV3 shift; 
	
	shift = cresult.collisionFrame[2] * cresult.depth;

	bb->SetPos( bb->GetPos() - shift);
}

void neFixedTimeStepSimulator::SimpleShift(const neCollisionResult & cresult)
{
	neV3 shift; shift = cresult.collisionFrame[2] * cresult.depth;

	f32 aratio, bratio;

	neRigidBodyBase * bodyA = cresult.bodyA;

	neRigidBodyBase * bodyB = cresult.bodyB;

	neRigidBody_ * ba = bodyA->AsRigidBody();

	neRigidBody_ * bb = bodyB->AsRigidBody();

	if (!ba)
	{
		aratio = 0.0f;
		bratio = 1.0f;

		if (bb)
			bb->isShifted2 = true;
	}
	else if (!bb)
	{
		aratio = 1.0f;
		bratio = 0.0f;

		if (ba)
			ba->isShifted2 = true;
	}
	else
	{
		ba->isShifted2 = true;
		bb->isShifted2 = true;

		f32 totalMass = ba->mass + bb->mass;

		aratio = bb->mass / totalMass;

		bratio = ba->mass / totalMass;
	}

	if (ba)
	{
		ba->SetPos( ba->GetPos() + (shift * aratio) * 1.0f);
	}
	if (bb)
	{
		bb->SetPos( bb->GetPos() - (shift * bratio) * 1.0f);
	}
}


neBool neFixedTimeStepSimulator::CheckBreakage(neRigidBodyBase * originalBody, TConvex * convex, const neV3 & contactPoint, neV3 & impulse)
{
	f32 impulseMag;

	neV3 breakPlane;

	neM3 rot = originalBody->GetB2W().rot * convex->c2p.rot;

	breakPlane = rot * convex->breakInfo.breakPlane;

	neV3 breakImpulse = impulse;

	breakImpulse.RemoveComponent(breakPlane);

	impulseMag = breakImpulse.Length();

	if (impulseMag < convex->breakInfo.breakMagnitude)
	{
		return false;
	}

	f32 dot = impulse.Dot(breakPlane);

	impulse = breakPlane * dot;

	breakImpulse *= convex->breakInfo.breakMagnitude / impulseMag;

	neRigidBody_* newBody = NULL;

	neV3 newImpulse, newContactPoint;

	newImpulse = breakImpulse * convex->breakInfo.breakAbsorb;

	breakImpulse *= (1.0f - convex->breakInfo.breakAbsorb);

	impulse += breakImpulse;

	neBodyType originalBodyType;

	if (originalBody->AsRigidBody())
	{
		originalBodyType = NE_RIGID_BODY;
	}
	else
	{
		originalBodyType = NE_ANIMATED_BODY;
	}

	switch (convex->breakInfo.flag)
	{
	case neGeometry::NE_BREAK_NORMAL:
	case neGeometry::NE_BREAK_NORMAL_PARTICLE:

		newBody = CreateRigidBodyFromConvex(convex, originalBody);

		newContactPoint = contactPoint - newBody->GetPos();

		newBody->ApplyCollisionImpulse(newImpulse, newContactPoint, IMPULSE_NORMAL);

		break;
	case neGeometry::NE_BREAK_ALL:
	case neGeometry::NE_BREAK_ALL_PARTICLE:

		break;
	case neGeometry::NE_BREAK_NEIGHBOUR:
	case neGeometry::NE_BREAK_NEIGHBOUR_PARTICLE:

		break;
	}
	if (originalBodyType == NE_ANIMATED_BODY)
	{
		impulse = newImpulse;
	}
	if (newBody)
	{
		breakageCallback((neByte *)originalBody, originalBodyType, (neGeometry *)convex, (neRigidBody*)newBody);
	
		return true;
	}
	else
	{
		return false;
	}
}


void neFixedTimeStepSimulator::ResetStackHeaderFlag()
{
	neStackHeaderItem * hitem = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (hitem)
	{
		neStackHeader * sheader = (neStackHeader *) hitem;

		sheader->dynamicSolved = false;

		hitem = hitem->next;
	}
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::SetTerrainMesh
*
****************************************************************************/ 

void neFixedTimeStepSimulator::SetTerrainMesh(neTriangleMesh * tris)
{
	region.MakeTerrain(tris);
}

void neFixedTimeStepSimulator::FreeTerrainMesh()
{
	region.FreeTerrain();
}

neStackHeader * neFixedTimeStepSimulator::NewStackHeader(neStackInfo * sinfo)
{
	neStackHeader * n = stackHeaderHeap.Alloc();

	//ASSERT(n);

	n->Null();

	n->sim = this;

	if (sinfo)
		n->Add(sinfo);

	return n;
}

neConstraintHeader * neFixedTimeStepSimulator::NewConstraintHeader()
{
	neConstraintHeader * ret = constraintHeaders.Alloc();

	ret->Reset();

	return ret;
}

void neFixedTimeStepSimulator::CheckStackHeader()
{
	neStackHeaderItem * item = (neStackHeaderItem *)(*stackHeaderHeap.BeginUsed());

	while (item)
	{
		neStackHeader * h = (neStackHeader *)item;

		//ASSERT(h->infoCount > 0);
		//assert(h->infoCount > 0);
		item = item->next;
	}
}

void neFixedTimeStepSimulator::UpdateConstraintControllers()
{
	neFreeListItem<neConstraintHeader> * hitem = (neFreeListItem<neConstraintHeader> *)(*constraintHeaders.BeginUsed());

	while (hitem)
	{
		neConstraintHeader * h = (neConstraintHeader *)hitem;

		hitem = hitem->next;

		neFreeListItem<_neConstraint> * citem = (neFreeListItem<_neConstraint> *) h->head;
		
		while (citem)
		{
			_neConstraint * c = (_neConstraint *)citem;	

			citem = citem->next;

			if (c->enable)
				c->UpdateController();
		}
	}
}

void neFixedTimeStepSimulator::FreeAllBodies()
{
	neRigidBody_ * rb = activeRB.GetHead();

	while (rb)
	{
		rb->Free();

		neRigidBody_ * rbNext = activeRB.GetNext(rb);

		activeRB.Remove(rb);

		rigidBodyHeap.Dealloc(rb, 1);
		
		rb = rbNext;
	}
	rb = inactiveRB.GetHead();

	while (rb)
	{
		rb->Free();

		neRigidBody_ * rbNext = inactiveRB.GetNext(rb);

		inactiveRB.Remove(rb);

		rigidBodyHeap.Dealloc(rb, 1);
		
		rb = rbNext;
	}

	//ASSERT(activeRB.count == 0);

	//ASSERT(inactiveRB.count == 0);

	activeRB.Reset();

	inactiveRB.Reset();

	///////////////////////////////////////////////////////////

	neRigidBody_ * rp = activeRP.GetHead();

	while (rp)
	{
		rp->Free();

		neRigidBody_ * rpNext = activeRP.GetNext(rp);

		activeRP.Remove(rp);

		rigidParticleHeap.Dealloc(rp, 1);
		
		rp = rpNext;
	}
	rp = inactiveRP.GetHead();

	while (rp)
	{
		rp->Free();

		neRigidBody_ * rpNext = inactiveRP.GetNext(rp);

		inactiveRP.Remove(rp);

		rigidParticleHeap.Dealloc(rp, 1);
		
		rp = rpNext;
	}

	//ASSERT(activeRP.count == 0);

	//ASSERT(inactiveRP.count == 0);

	activeRP.Reset();

	inactiveRP.Reset();

	///////////////////////////////////////////////////////////

	neCollisionBody_ * cb = activeCB.GetHead();

	while (cb)
	{
		cb->Free();

		neCollisionBody_ * cbNext = activeCB.GetNext(cb);

		activeCB.Remove(cb);

		collisionBodyHeap.Dealloc(cb, 1);
		
		cb = cbNext;
	}

	cb = inactiveCB.GetHead();

	while (cb)
	{
		cb->Free();

		neCollisionBody_ * cbNext = inactiveCB.GetNext(cb);

		inactiveCB.Remove(cb);

		collisionBodyHeap.Dealloc(cb, 1);
		
		cb = cbNext;
	}

	//ASSERT(activeCB.count == 0);

	//ASSERT(inactiveCB.count == 0);

	activeCB.Reset();

	inactiveCB.Reset();

	//ASSERT(rigidBodyHeap.GetUsedCount() == 0);

	//ASSERT(collisionBodyHeap.GetUsedCount() == 0);

	//ASSERT(constraintHeap.GetUsedCount() == 0);

	//ASSERT(geometryHeap.GetUsedCount() == 0);

	//ASSERT(controllerHeap.GetUsedCount() == 0);

//	ASSERT(miniConstraintHeap.GetUsedCount() == 0);

	//ASSERT(stackInfoHeap.GetUsedCount() == 0);
}

/****************************************************************************
*
*	neFixedTimeStepSimulator::GetMemoryUsage
*
****************************************************************************/ 

void neFixedTimeStepSimulator::GetMemoryAllocated(s32 & memoryAllocated)
{
	memoryAllocated = 0;

	memoryAllocated += rigidBodyHeap.Size() * sizeof(neFreeListItem<neRigidBody_>);

	memoryAllocated += rigidParticleHeap.Size() * sizeof(neFreeListItem<neRigidBody_>);
	
	memoryAllocated += collisionBodyHeap.Size() * sizeof(neFreeListItem<neCollisionBody_>);

	memoryAllocated += treeNodes.GetTotalSize() * sizeof(neTreeNode *);

	memoryAllocated += triangleIndex.GetTotalSize() * sizeof(s32);

	memoryAllocated += constraintHeaders.Size() * sizeof(neFreeListItem<neConstraintHeader>);

	memoryAllocated += constraintHeap.Size() * sizeof(neFreeListItem<_neConstraint>);

//	memoryAllocated += miniConstraintHeap.Size() * sizeof(neFreeListItem<neMiniConstraint>);

	memoryAllocated += controllerHeap.Size() * sizeof(neFreeListItem<neController>);

	memoryAllocated += stackInfoHeap.Size() * sizeof(neFreeListItem<neStackInfo>);

	memoryAllocated += stackHeaderHeap.Size() * sizeof(neFreeListItem<neStackHeader>);

	memoryAllocated += sensorHeap.Size() * sizeof(neFreeListItem<neSensor_>);

	memoryAllocated += geometryHeap.Size() * sizeof(neFreeListItem<TConvex>);

	//memoryAllocated += cresultHeap.Size() * sizeof(neFreeListItem<neCollisionResult>);
	memoryAllocated += cresultHeap.GetTotalSize() * sizeof(neFreeListItem<neCollisionResult>);

	memoryAllocated += pointerBuffer1.GetTotalSize() * sizeof(neByte *);

	memoryAllocated += pointerBuffer2.GetTotalSize() * sizeof(neByte *);

	//region stuff
	memoryAllocated += region.b2b.GetTotalSize() * sizeof(neOverlapped);

	memoryAllocated += region.b2p.GetTotalSize() * sizeof(neOverlapped);

	memoryAllocated += region.newBodies.GetTotalSize() * sizeof(neAddBodyInfo);

	memoryAllocated += region.bodies.Size() * sizeof(neFreeListItem<neRigidBodyBase *>);

	memoryAllocated += region.overlappedPairs.Size() * sizeof(neFreeListItem<neOverlappedPair>);

	memoryAllocated += region.coordLists[0].coordList.Size() * sizeof(neFreeListItem<CCoordListEntry>);

	memoryAllocated += region.coordLists[1].coordList.Size() * sizeof(neFreeListItem<CCoordListEntry>);

	memoryAllocated += region.coordLists[2].coordList.Size() * sizeof(neFreeListItem<CCoordListEntry>);

	memoryAllocated += region.terrainTree.nodes.GetTotalSize() * sizeof(neTreeNode);

	memoryAllocated += region.terrainTree.triangles.GetTotalSize() * sizeof(neTriangle_);
}

/****************************************************************************
*
*	neCollisionTable_::neCollisionTable_
*
****************************************************************************/ 

neCollisionTable_::neCollisionTable_()
{
	for (s32 i = 0 ; i < NE_COLLISION_TABLE_MAX; i++)
	{
		for (s32 j = 0 ; j < NE_COLLISION_TABLE_MAX; j++)
			table[i][j] = table[j][i] = neCollisionTable::RESPONSE_IMPULSE;

		terrainTable[i] = neCollisionTable::RESPONSE_IMPULSE;
	}
}

/****************************************************************************
*
*	neCollisionTable_::~neCollisionTable_
*
****************************************************************************/ 

neCollisionTable_::~neCollisionTable_()
{
}

/****************************************************************************
*
*	neCollisionTable_::Set
*
****************************************************************************/ 

void neCollisionTable_::Set(s32 collisionID1, s32 collisionID2, neCollisionTable::neReponseBitFlag value)
{
	//ASSERT(collisionID1 >= -1 && collisionID1 < neCollisionTable::NE_COLLISION_TABLE_MAX);
	//ASSERT(collisionID2 >= -1 && collisionID2 < neCollisionTable::NE_COLLISION_TABLE_MAX);

	if (collisionID1 == -1 && collisionID2 == -1)
	{
		return;
	}

	if (collisionID1 == -1)
	{
		terrainTable[collisionID2] = value;
	}
	else if (collisionID2 == -1)
	{
		terrainTable[collisionID1] = value;
	}
	else
	{
		table[collisionID1][collisionID2] = value;
		table[collisionID2][collisionID1] = value;
	}
}

/****************************************************************************
*
*	neCollisionTable_::Get
*
****************************************************************************/ 

neCollisionTable::neReponseBitFlag neCollisionTable_::Get(s32 collisionID1, s32 collisionID2)
{
	//ASSERT(collisionID1 >= -1 && collisionID1 < neCollisionTable::NE_COLLISION_TABLE_MAX);
	//ASSERT(collisionID2 >= -1 && collisionID2 < neCollisionTable::NE_COLLISION_TABLE_MAX);

	if (collisionID1 == -1 && collisionID2 == -1)
	{
		return neCollisionTable::RESPONSE_IGNORE;
	}
	
	if (collisionID1 == -1)
	{
		return terrainTable[collisionID2];
	}
	else if (collisionID2 == -1)
	{
		return terrainTable[collisionID1];
	}
	
	return table[collisionID1][collisionID2];
}
