/*
* Copyright (c) 2013 Thibault Lelore (based on http://box2dlights.googlecode.com)
*
* This part of software is provided 'as-is', without any express or implied
* warranty.  In no event will the authors be held liable for any damages
* arising from the use of this software.
* Permission is granted to anyone to use this software for any purpose,
* including commercial applications, and to alter it and redistribute it
* freely, subject to the following restrictions:
* 1. The origin of this software must not be misrepresented; you must not
* claim that you wrote the original software. If you use this software
* in a product, an acknowledgment in the product documentation would be
* appreciated but is not required.
* 2. Altered source versions must be plainly marked as such, and must not be
* misrepresented as being the original software.
* 3. This notice may not be removed or altered from any source distribution.
*/
#ifndef B2_LIGHT_COMPUTE_H
#define B2_LIGHT_COMPUTE_H

#include <Box2D/Dynamics/b2WorldCallbacks.h>
#include <Box2D/Dynamics/b2Fixture.h>
#include <Box2D/Dynamics/b2World.h>
#include <Box2D/Common/b2Settings.h>

#include <stdio.h>

class PointLight: public b2RayCastCallback
{
  float *sin;
  float *cos;

  b2Vec2 start;
  float distance;

  float *endX;
  float *endY;

  int nbRays;
  int m_index;

  float coneDegree;
  float directionDegree;

  float* pointsCast;

  b2World *world;

public:
  PointLight(  b2World *world, int rays )
  {
    this->world = world;
    nbRays = rays;

    sin = new float[rays];
    cos = new float[rays];
    endX = new float[rays];
    endY = new float[rays];

    directionDegree = 0;
    coneDegree = 360;

    float angleNum = (coneDegree / ((float)rays-1.f))*(3.1415927f/180.f);
    for (int i = 0; i < rays; i++) {
      float angle = directionDegree + angleNum * i;
      sin[i] = sinf(angle);
      cos[i] = cosf(angle);
    }

    distance = -1.f;
  }
  ~PointLight(){
    delete[] sin;
    delete[] cos;
    delete[] endX;
    delete[] endY;;
  }

  void computePoints( float* pointsCast, int nbValues, float x, float y, float d, float dir, float coneSize )
  {

    bool update = false;
    if( directionDegree!=dir )
    {
      update = true;
      directionDegree = dir;
    }
    if( coneDegree!=coneSize )
    {
      update = true;
      coneDegree = coneSize;
    }
    if( update )
    {
      float angleNum = (coneDegree / ((float)nbRays-1.f))*(3.1415927f/180.f);
      for (int i = 0; i < nbRays; i++) {
        float angle = directionDegree + angleNum * i;
        sin[i] = sinf(angle);
        cos[i] = cosf(angle);
      }
    }
    computePoints(pointsCast, nbValues, x, y, d);
  }

  void computePoints( float* pC, int nbValues, float x, float y, float d )
  {
    b2Assert((nbValues-3)==nbRays*3);
    this->pointsCast = pC;

    pointsCast[nbValues-3] = start.x;
    pointsCast[nbValues-2] = start.y;
    pointsCast[nbValues-1] = 1;

    if(distance != d)
    {
      distance = d;
      for (int i = 0; i < nbRays; i++) {
        endX[i] = distance * cos[i];
        endY[i] = distance * sin[i];
      }
    }

    start.x = x;
    start.y = y;

    b2Vec2 tmpEnd;
    for (int i = 0; i < nbRays; i++) {
      m_index = i;
      tmpEnd.x = endX[i] + start.x;
      tmpEnd.y = endY[i] + start.y;

      pointsCast[m_index*3] = tmpEnd.x;//in case the ray don't find shape!
      pointsCast[m_index*3+1] = tmpEnd.y;
      pointsCast[m_index*3+2] = 1;
      
      world->RayCast(this, start, tmpEnd);
    }

  }

  virtual float32 ReportFixture( b2Fixture* fixture, const b2Vec2& point, const b2Vec2& normal, float32 fraction)
  {
    if( fixture->IsSensor() )
      return -1;

    pointsCast[m_index*3] = point.x;
    pointsCast[m_index*3+1] = point.y;
    pointsCast[m_index*3+2] = fraction;

    return fraction;
  }
};

#endif