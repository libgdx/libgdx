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

#ifdef _WIN32
#include <windows.h>
#endif

#include "stack.h"
#include "simulator.h"
#include "perfwin32.h"
/*
typedef unsigned __int64 u64;
typedef __int64 s64;
u64 tickPerSec;
u64 m_ticks_at_start;

float divide64(u64 a, u64 b)
{
	return 0;//float( double(s64(a)) / double(s64(b)) );
}

u64 getTickCounter()
{
	u64 ticks;
	// note: using cpuid as a serializing makes timings more accurate, 
	// at the expense of more overhead. (1.5% without versus 5% with cpuid)
	__asm {
		push ebx
		//cpuid 
		pop ebx
		rdtsc
		mov dword ptr[ticks  ], eax
		mov dword ptr[ticks+4], edx
	}

	return ticks;
}

u64 getTicksPerSecond()
{
	static u64 freq = 0;
	if(freq==0)
	{
		u64 ticks;
		u64 qticks;
		u64 ticks2;
		u64 qticks2;
		double minFactor = 1e6f;

		// Iterate several times
		// We take the minimum value beacuse Sleep() sleeps for at least the specified time
		for (int iter = 0; iter <10; iter++)
		{
			ticks = getTickCounter();
			QueryPerformanceCounter( (LARGE_INTEGER*) &qticks);

			///
			///	Sleep for a little while
			///
			volatile x=1;
			for (int j=0; j< 5000; j++)
			{
				x += x*x;
			}


			ticks2 = getTickCounter();
			QueryPerformanceCounter( (LARGE_INTEGER*) &qticks2);

			// We assume that this is fixed & regular 
			QueryPerformanceFrequency( (LARGE_INTEGER*) &freq);

			// Work our calibration factor
			u64 diff = ticks2 - ticks;
			u64 qdiff = qticks2 - qticks;

			double factor = double(diff)/ double(qdiff);
			
			// Is this smaller?
			if (factor < minFactor)
			{
				minFactor = factor;
			}
		}
		freq = u64(minFactor * freq);
	}
	return freq;
}
*/
nePerformanceData * nePerformanceData::Create()
{
	return new nePerformanceData;
}

LARGE_INTEGER perfFreq;

LARGE_INTEGER counter;

/****************************************************************************
*
*	nePerformanceData::Start
*
****************************************************************************/ 

void DunselFunction() { return; }

void nePerformanceData::Init()
{
   Reset();

//   tickPerSec = getTicksPerSecond();

//   return;
   
   void (*pFunc)() = DunselFunction;

   // Assume the worst
   if ( QueryPerformanceFrequency(&perfFreq) )
      {
      // We can use hires timer, determine overhead

      overheadTicks = 200;
      for ( int i=0; i < 20; i++ )
         {
         LARGE_INTEGER b,e;
         int Ticks;
         QueryPerformanceCounter(&b);
         (*pFunc)();
         QueryPerformanceCounter(&e);
         Ticks = e.LowPart - b.LowPart;
         if ( Ticks >= 0 && Ticks < overheadTicks )
            overheadTicks = Ticks;
         }
      // See if Freq fits in 32 bits; if not lose some precision
      perfFreqAdjust = 0;

      int High32 = perfFreq.HighPart;
      
	  while ( High32 )
         {
         High32 >>= 1;
         perfFreqAdjust++;
         }
      }
   
   //QueryPerformanceCounter(&counter);
}

void nePerformanceData::Start()
{
	Reset();
	
	QueryPerformanceCounter(&counter);
	//m_ticks_at_start = getTickCounter();
}

f32 nePerformanceData::GetCount()
{
	//u64 ticks_now = getTickCounter();

	//u64 m_ticks_total = ticks_now - m_ticks_at_start;

	//return divide64(m_ticks_total, tickPerSec);

	LARGE_INTEGER tStart, tStop;
	LARGE_INTEGER Freq = perfFreq;
	int Oht = overheadTicks;
	int ReduceMag = 0;
/*
	SetThreadPriority(GetCurrentThread(), 
	 THREAD_PRIORITY_TIME_CRITICAL);

	QueryPerformanceCounter(&tStart);
	(*funcp)();   //call the actual function being timed
*/	
	tStart = counter;
	
	QueryPerformanceCounter(&tStop);

	counter = tStop;
//	SetThreadPriority(GetCurrentThread(), THREAD_PRIORITY_NORMAL);
	// Results are 64 bits but we only do 32
	unsigned int High32 = tStop.HighPart - counter.HighPart;
	while ( High32 )
	{
		High32 >>= 1;
		ReduceMag++;
	}
	if ( perfFreqAdjust || ReduceMag )
	{
		if ( perfFreqAdjust > ReduceMag )
			ReduceMag = perfFreqAdjust;

		 tStart.QuadPart = Int64ShrlMod32(tStart.QuadPart, ReduceMag);
		 tStop.QuadPart = Int64ShrlMod32(tStop.QuadPart, ReduceMag);
		 Freq.QuadPart = Int64ShrlMod32(Freq.QuadPart, ReduceMag);
		 Oht >>= ReduceMag;
	}
	double time;
	
	// Reduced numbers to 32 bits, now can do the math
	if ( Freq.LowPart == 0 )
		time = 0.0;
	else
		time = ((double)(tStop.LowPart - tStart.LowPart - Oht))/Freq.LowPart;

	return (f32)time;
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