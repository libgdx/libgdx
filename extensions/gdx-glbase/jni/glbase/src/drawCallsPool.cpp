/**
 * @file drawCallsPool.cpp
 * @brief Draw calls pool implementation
 **/

#include "drawCallsPool.h"
#include "glbase.h"
#include "object.h"
#include "layer.h"
#include "polygonMap.h"
#include "macros.h"
#include "arrayList.h"
#include "drawCall.h"

DrawCallsPool::DrawCallsPool()
{
  drawCallPool = new DrawCall[MAX_DRAW_CALLS];
  nextAvailable = 0;
}

DrawCallsPool::~DrawCallsPool()
{
  delete[] drawCallPool;
}

DrawCall* DrawCallsPool::acquireDrawCall()
{
  DrawCall* nDrawCall = &drawCallPool[nextAvailable++];
  nDrawCall->setFullScreen();
  return nDrawCall;
}

DrawCall* DrawCallsPool::acquireDrawCall(IPolygonMap* polygonMap)
{
  DrawCall* nDrawCall = &drawCallPool[nextAvailable++];
  nDrawCall->set( polygonMap );
  return nDrawCall;
}

DrawCall* DrawCallsPool::acquireDrawCall(float* bbox)
{
  DrawCall* nDrawCall = &drawCallPool[nextAvailable++];
  nDrawCall->set( bbox );
  return nDrawCall;
}

DrawCall* DrawCallsPool::acquireDrawCall(int numParticles)
{
  DrawCall* nDrawCall = &drawCallPool[nextAvailable++];
  nDrawCall->set( numParticles );
  return nDrawCall;
}

DrawCall* DrawCallsPool::acquireDrawCall(RenderEnums::ClearMode mode, float* color)
{
  DrawCall* nDrawCall = &drawCallPool[nextAvailable++];
  nDrawCall->set( mode, color );
  return nDrawCall;
}

void DrawCallsPool::reset()
{
  nextAvailable = 0;
}
