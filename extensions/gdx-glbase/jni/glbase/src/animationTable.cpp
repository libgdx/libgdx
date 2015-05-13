/**
 * @file animationTable.cpp
 * @brief Animation resources table implementation
 **/
#include <stdio.h>
#include <string.h>

#include "animationTable.h"
#include "animationBsBin.h"
#include "animationBbm.h"

AnimationTable::AnimationTable()
{
  nextAvailable = 0;

  memset(animations, 0, sizeof(animations));
}

int AnimationTable::loadAnimation(char* bsFile, int bsFileLength, byte* binFile, int binFileLength)
{
  AnimationBsBin* newAnimation = new AnimationBsBin(bsFile, bsFileLength, binFile, binFileLength);

  if( !newAnimation->load()  ) {
    delete newAnimation;
    return -1;
  }

  return addAnimation( newAnimation );
}

int AnimationTable::loadAnimation(byte* bbmFile, int bbmFileLength)
{
  AnimationBbm* newAnimation = new AnimationBbm(bbmFile, bbmFileLength);

  if( !newAnimation->load()  ) {
    delete newAnimation;
    return -1;
  }

  return addAnimation( newAnimation );  
}

bool AnimationTable::deleteAnimation(int animationID)
{
  delete animations[animationID];
  animations[animationID] = NULL;
  return true;
}

Animation* AnimationTable::getAnimation(int animationID)
{
  if( animationID < 0 || animationID > MAX_ANIMATIONS-1 ) return NULL;
  return animations[animationID];
}

int AnimationTable::addAnimation(Animation* newAnimation)
{
  int res = nextAvailable;
  animations[res] = newAnimation;
  
  while( animations[++nextAvailable] != NULL ){
    nextAvailable = (nextAvailable + 1) % MAX_ANIMATIONS;
  }

  return res;
}
