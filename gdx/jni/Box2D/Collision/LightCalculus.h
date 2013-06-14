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
#include <stdint.h>

union int_float_bits {
  int32_t int_bits;
  float float_bits;
};

class PointLight: public b2RayCastCallback
{
protected:
  static b2Filter *rayFilter;
  bool collideSensor;
  float *sin;
  float *cos;

  b2Vec2 start;
  float distance;

  float *endX;
  float *endY;

  int nbRays;
  int m_index;

  float openingSize;
  float directionDegree;

  float* pointsCast;

  b2World *world;

  void convertColor( float colorF, float& r, float& g, float b, float& a)
  {
    int_float_bits color;
    color.float_bits = colorF;
    int32_t colorI = color.int_bits;
    r = (float)(colorI>>24);
    g = (float)(0xFF&(colorI>>16));
    b = (float)(0xFF&(colorI>>8));
    a = (float)(0xFF&(colorI));
  }

  PointLight( b2World *world )
  {
    this->world = world;
  }

public:
  /**
  * create a new Point Light
  * 
  * @param world current Box2D world
  * @param rays nb of raycast to perform
  */
  PointLight( b2World *world, int rays )
  {
    this->world = world;
    nbRays = rays;

    collideSensor = false;

    sin = new float[rays];
    cos = new float[rays];
    endX = new float[rays];
    endY = new float[rays];

    pointsCast = new float[rays*3];

    directionDegree = 0;
    openingSize = 360;

    float angleNum = (openingSize / ((float)rays-1.f))*(3.1415927f/180.f);
    for (int i = 0; i < rays; i++) {
      float angle = directionDegree + angleNum * i;
      sin[i] = sinf(angle);
      cos[i] = cosf(angle);
    }

    distance = -1.f;
  }

  virtual ~PointLight(){
    delete[] sin;
    delete[] cos;
    delete[] endX;
    delete[] endY;
    delete[] pointsCast;
  }

  virtual void computePoints( float x, float y, float d, float dir, float coneSize )
  {

    bool update = false;
    if( directionDegree!=dir )
    {
      update = true;
      directionDegree = dir;
    }
    if( openingSize!=coneSize )
    {
      update = true;
      openingSize = coneSize;
    }
    if( update )
    {
      float angleNum = (openingSize / ((float)nbRays-1.f))*(3.1415927f/180.f);
      for (int i = 0; i < nbRays; i++) {
        float angle = directionDegree + angleNum * i;
        sin[i] = sinf(angle);
        cos[i] = cosf(angle);
      }
    }
    computePoints(x, y, d);
  }

  virtual void computePoints( float x, float y, float d )
  {
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

  void setSensorFilter( bool shouldCollide )
  {
    collideSensor = shouldCollide;
  }

  /**
  * create new contact filter for ALL LIGHTS with give parameters
  * 
  * @param categoryBits
  * @param groupIndex
  * @param maskBits
  */
  static void setContactFilter(short categoryBits, short groupIndex,
    short maskBits)
  {
    if( rayFilter!=NULL )
      delete rayFilter;
    rayFilter = new b2Filter();
    rayFilter->categoryBits = categoryBits;
    rayFilter->groupIndex = groupIndex;
    rayFilter->maskBits = maskBits;
  }

  virtual float32 ReportFixture( b2Fixture* fixture, const b2Vec2& point, const b2Vec2& normal, float32 fraction)
  {
    if( !collideSensor && fixture->IsSensor() )
      return -1;

    const b2Filter& filterB = fixture->GetFilterData();

    if( rayFilter!=NULL )
    {
      if ( rayFilter->groupIndex == filterB.groupIndex && rayFilter->groupIndex < 0 )
        return -1;

      if(  (rayFilter->maskBits & filterB.categoryBits) == 0
        || (filterB.maskBits & rayFilter->categoryBits) == 0)
        return -1;
    }

    pointsCast[m_index*3] = point.x;
    pointsCast[m_index*3+1] = point.y;
    pointsCast[m_index*3+2] = fraction;

    return fraction;
  }

  virtual void setLightMesh( float* segments, float colorF, bool isGL20 ) {
    // ray starting point
    int size = 0, point = 0;

    segments[size++] = start.x;
    segments[size++] = start.y;
    segments[size++] = colorF;
    segments[size++] = 1;
    if( isGL20 )
    {
      // rays ending points.
      for (int i = 0; i < nbRays; i++) {
        segments[size++] = pointsCast[point++];
        segments[size++] = pointsCast[point++];
        segments[size++] = colorF;
        segments[size++] = 1.f - pointsCast[point++];
      }
    }
    else
    {
      float r,g,b,a;
      r=g=b=a=0.f;
      convertColor(colorF, r,g,b,a);
      int_float_bits colorCvt;

      // rays ending points.
      for (int i = 0; i < nbRays; i++) {
        segments[size++] = pointsCast[point++];
        segments[size++] = pointsCast[point++];
        float s = 1.f - pointsCast[point++];
        // ugly inlining
        colorCvt.int_bits = ((int) (a * s) << 24)
          | ((int) (b * s) << 16) | ((int) (g * s) << 8)
          | ((int) (r * s));
        segments[size++] = colorCvt.float_bits;
      }
    }
  }

  virtual void setShadowMesh( float* segments, float colorF, float softShadowLenght, bool isGL20 ) {
    int size = 0, point = 0;
    // rays ending points.

    if( isGL20 )
    {
      for (int i = 0; i < nbRays; i++) {
        point = i*3;
        segments[size++] = pointsCast[point];
        segments[size++] = pointsCast[point+1];
        segments[size++] = colorF;
        float s = (1.f - pointsCast[point+2]);
        segments[size++] = s;

        s = s * softShadowLenght;
        segments[size++] = pointsCast[point] + s * cos[i];
        segments[size++] = pointsCast[point+1] + s * sin[i];
        segments[size++] = 0.f;
        segments[size++] = 0.f;
      }
    }else{
      float r,g,b,a;
      r=g=b=a=0.f;
      convertColor(colorF, r,g,b,a);
      int_float_bits colorCvt;
      for (int i = 0; i < nbRays; i++) {
        point = i*3;
        segments[size++] = pointsCast[point];
        segments[size++] = pointsCast[point+1];
        // color value is cached.
        float s = 1.f - pointsCast[point+2];
        // ugly inlining
        colorCvt.int_bits = ((int) (a * s) << 24)
          | ((int) (b * s) << 16) | ((int) (g * s) << 8)
          | ((int) (r * s));
        segments[size++] = colorCvt.float_bits;

        s = s * softShadowLenght;
        segments[size++] = pointsCast[point] + s * cos[i];
        segments[size++] = pointsCast[point+1] + s * sin[i];
        segments[size++] = 0.f;
      }
    }
  }
};

class DirectionalLight : public PointLight
{
protected:
  float step;

  float rayDx;
  float rayDy;
public:
  DirectionalLight( b2World *world, int rays ):
    PointLight(world)
  {
    nbRays = rays;

    collideSensor = false;

    sin = new float[1];
    cos = new float[1];
    endX = new float[rays];
    endY = new float[rays];

    pointsCast = new float[rays*3];

    directionDegree = 0;
    openingSize = 360;

    sin[0] = sinf(directionDegree);
    cos[0] = cosf(directionDegree);

    distance = -1.f;//infinite distance...
  }

  ~DirectionalLight()
  {
  }
  
  virtual void computePoints( float x, float y, float dist, float dir, float coneSize )
  {
    directionDegree = dir;
    openingSize = coneSize;
    computePoints(x, y, dist);
  }

  virtual void computePoints( float x, float y, float d )
  {
    start.x = x;
    start.y = y;

    sin[0] = sinf(directionDegree);
    cos[0] = cosf(directionDegree);

    step = openingSize/nbRays;

    rayDx = d * cos[0];
    rayDy = d * sin[0];

    b2Vec2 tmpStart, tmpEnd;

    tmpStart.x = endX[0] = x - (openingSize/2.f)/nbRays;
    tmpStart.y = endY[0] = y;

    tmpEnd.x = pointsCast[0] = endX[0] + rayDx;
    tmpEnd.y = pointsCast[1] = endY[0] + rayDy;
    pointsCast[2] = 1;

    m_index = 0;
    world->RayCast(this, tmpStart, tmpEnd);

    for (int i = 1; i < nbRays; i++) {
      m_index = i;
      tmpStart.x = endX[i] = endX[i-1] + step;
      tmpStart.y = endY[i] = y;

      tmpEnd.x = pointsCast[m_index*3] = endX[i] + rayDx;
      tmpEnd.y = pointsCast[m_index*3+1] = endY[i] + rayDy;
      pointsCast[m_index*3+2] = 1;

      world->RayCast(this, tmpStart, tmpEnd);
    }
  }

  virtual void setLightMesh( float* segments, float colorF, bool isGL20 ) {
    // ray starting point
    int size = 0, point = 0;
    if( isGL20 )
    {
      // rays ending points.
      for (int i = 0; i < nbRays; i++) {
        segments[size++] = endX[i];
        segments[size++] = endY[i];
        segments[size++] = colorF;
        segments[size++] = 1.f;
        segments[size++] = pointsCast[point++];
        segments[size++] = pointsCast[point++];
        segments[size++] = colorF;
        segments[size++] = 1.f - pointsCast[point++];
      }
    }
    else
    {
      float r,g,b,a;
      r=g=b=a=0.f;
      convertColor(colorF, r,g,b,a);
      int_float_bits colorCvt;

      // rays ending points.
      for (int i = 0; i < nbRays; i++) {
        segments[size++] = endX[i];
        segments[size++] = endY[i];
        segments[size++] = colorF;

        segments[size++] = pointsCast[point++];
        segments[size++] = pointsCast[point++];
        float s = 1.f - pointsCast[point++];
        // ugly inlining
        colorCvt.int_bits = ((int) (a * s) << 24)
          | ((int) (b * s) << 16) | ((int) (g * s) << 8)
          | ((int) (r * s));
        segments[size++] = colorCvt.float_bits;
      }
    }
  }

  virtual void setShadowMesh( float* segments, float colorF, float softShadowLenght, bool isGL20 ) {
    int size = 0, point = 0;
    // rays ending points.

    if( isGL20 )
    {
      for (int i = 0; i < nbRays; i++) {
        point = i*3;
        segments[size++] = pointsCast[point];
        segments[size++] = pointsCast[point+1];
        segments[size++] = colorF;
        float s = (1.f - pointsCast[point+2]);
        segments[size++] = s;

        s = s * softShadowLenght;
        segments[size++] = pointsCast[point] + s * cos[0];
        segments[size++] = pointsCast[point+1] + s * sin[0];
        segments[size++] = 0.f;
        segments[size++] = 0.f;
      }
    }else{
      float r,g,b,a;
      r=g=b=a=0.f;
      convertColor(colorF, r,g,b,a);
      int_float_bits colorCvt;
      for (int i = 0; i < nbRays; i++) {
        point = i*3;
        segments[size++] = pointsCast[point];
        segments[size++] = pointsCast[point+1];
        // color value is cached.
        float s = 1.f - pointsCast[point+2];
        // ugly inlining
        colorCvt.int_bits = ((int) (a * s) << 24)
          | ((int) (b * s) << 16) | ((int) (g * s) << 8)
          | ((int) (r * s));
        segments[size++] = colorCvt.float_bits;

        s = s * softShadowLenght;
        segments[size++] = pointsCast[point] + s * cos[0];
        segments[size++] = pointsCast[point+1] + s * sin[0];
        segments[size++] = 0.f;
      }
    }
  }
};

#endif