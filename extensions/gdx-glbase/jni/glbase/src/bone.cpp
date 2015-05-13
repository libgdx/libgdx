/**
 * @file bone.cpp
 * @brief Represent a bone's transformation
 **/
#include <stdio.h>
#include "bone.h"


Bone::Bone()
{
  Name = NULL;
  WeightMapName = NULL;
  Parent = NULL;
  Root = NULL;
  
  RestMatrix.setIdentity();
  MoveMatrix.setIdentity();
  baseFinalMatrix.setIdentity();
  FinalMatrix = &baseFinalMatrix;
}

Bone::~Bone()
{
  delete[] Name;
  delete[] WeightMapName;
}

