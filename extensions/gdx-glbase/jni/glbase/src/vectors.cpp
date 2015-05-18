#include "vectors.h"
#include <math.h>

void Vectors::normalize(float* v)
{
  float l = length(v);
  v[0] /= l;
  v[1] /= l;
  v[2] /= l;
}

void Vectors::cross(float* v1, float* v2, float* res)
{
  res[0] = v1[1]*v2[2] - v1[2]*v2[1];
  res[1] = v1[2]*v2[0] - v1[0]*v2[2];
  res[2] = v1[0]*v2[1] - v1[1]*v2[0];
}

float Vectors::length(float* v)
{
  return sqrtf(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
}
