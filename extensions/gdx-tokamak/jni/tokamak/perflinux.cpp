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

#include "stack.h"
#include "simulator.h"
#include "perflinux.h"

#include <sys/time.h>

nePerformanceData * nePerformanceData::Create()
{
	return new nePerformanceData;
}

s64 perfFreq;

timeval counter;

/****************************************************************************
*
*	nePerformanceData::Start
*
****************************************************************************/

void DunselFunction() { return; }

void nePerformanceData::Init()
{
	Reset();

	void (*pFunc)() = DunselFunction;

	gettimeofday(&counter, NULL);
}

void nePerformanceData::Start()
{
	Reset();

	gettimeofday(&counter, NULL);
}

f32 nePerformanceData::GetCount()
{
	timeval tStart, tStop;
	f32 start, end;

	tStart = counter;

	gettimeofday(&tStop, NULL);

	start = (tStart.tv_sec * 1000000.0) + tStart.tv_usec;
    end = (tStop.tv_sec * 1000000.0) + tStop.tv_usec;

	return (end - start) * 0.000001;
}

void nePerformanceData::UpdateDynamic()
{
	dynamic += GetCount();
}
void nePerformanceData::UpdatePosition()
{
	position += GetCount();
}
void nePerformanceData::UpdateConstrain1()
{
	constrain_1 += GetCount();
}
void nePerformanceData::UpdateConstrain2()
{
	constrain_2 += GetCount();
}
void nePerformanceData::UpdateCD()
{
	cd += GetCount();
}
void nePerformanceData::UpdateCDCulling()
{
	cdCulling += GetCount();
}
void nePerformanceData::UpdateTerrain()
{
	terrain += GetCount();
}
void nePerformanceData::UpdateControllerCallback()
{
	controllerCallback += GetCount();
}
void nePerformanceData::UpdateTerrainCulling()
{
	terrainCulling += GetCount();
}
