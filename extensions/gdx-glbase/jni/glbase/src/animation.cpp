/**
 * @file animation.cpp
 * @brief Represents an animation resource
 **/
#include <string.h>
#include "glbase.h"
#include "animation.h"
#include "scene.h"
#include "matrix.h"
#include "bone.h"

Animation::Animation() : bones(256)
{
  length = 0;
  fps = 0;
  frameOffset = 0;

  this->restMatrices = NULL;
  this->moveMatrices = NULL;
  this->finalMatrices = NULL;
}

Animation::~Animation()
{
  for( int i=0; i<bones.getSize(); i++ ){
    delete (Bone*)bones.get(i);
  }

  delete restMatrices;
  delete moveMatrices;
  delete finalMatrices;
}

Bone* Animation::getRootBone( Bone* bone, Bone* stopBone )
{
  if( (bone->Parent != NULL) && ((stopBone == NULL) || (stopBone != bone->Parent)) ) {
    return getRootBone( bone->Parent, stopBone );
  }

  return bone;
}

ArrayList* Animation::getBones()
{
  return &bones;
}

int Animation::getLength()
{
  return length;
}

int Animation::getFps()
{
  return fps;
}
  
int Animation::getFrameOffset()
{
  return frameOffset;
}

void Animation::setBonesToTime(int frame)
{
 for(int j=0; j<bones.getSize(); j++){
   Bone* bone = (Bone*)bones.get(j);

   switch(dataMode){

   case RESTMOVE_ONLY:
     bone->getMoveMatrix()->copyFrom(&moveMatrices[j*length+frame]);
     
     // final = move * rest
     bone->getFinalMatrix()->copyFrom(bone->getMoveMatrix());
     bone->getFinalMatrix()->multiply(bone->getRestMatrix());
     break;

   case FINAL_ONLY:
   case RESTMOVEFINAL:
     //計算が不要なfinalMatricesはポインターを保持
     bone->setFinalMatrix(&finalMatrices[j*length+frame]);
     break;

   case LAYERRESTMOVE_ONLY:
     bone->getMoveMatrix()->copyFrom(&moveMatrices[j*length+frame]);
     
     // final = layer * move * rest
	 bone->getFinalMatrix()->copyFrom(getLayerMatrix());
     bone->getFinalMatrix()->multiply(bone->getMoveMatrix());
     bone->getFinalMatrix()->multiply(bone->getRestMatrix());
     break;
   }
 }
}

void Animation::copyMatrices(Matrix* to, byte* from, int num, ValueType valueType)
{
  for( int i=0; i<num; i++ ){
    switch( valueType ){

    case FIXED:{
      int src[12];
      memcpy(src, from+i*MATRIX_LENGTH, MATRIX_LENGTH);
      float* dst = to[i].getMatrixPointer();
      dst[0] = XTOF( src[0] ); dst[4] = XTOF( src[3] ); dst[ 8] = XTOF( src[6] ); dst[12] = XTOF( src[ 9] );
      dst[1] = XTOF( src[1] ); dst[5] = XTOF( src[4] ); dst[ 9] = XTOF( src[7] ); dst[13] = XTOF( src[10] );
      dst[2] = XTOF( src[2] ); dst[6] = XTOF( src[5] ); dst[10] = XTOF( src[8] ); dst[14] = XTOF( src[11] );
      dst[3] = 0.0f;           dst[7] = 0.0f;           dst[11] = 0.0f;           dst[15] = 1.0f;
      break;
    }

    case FLOAT: {
      float src[12];
      memcpy(src, from+i*MATRIX_LENGTH, MATRIX_LENGTH);
      float* dst = to[i].getMatrixPointer();
      dst[0] = src[0]; dst[4] = src[3]; dst[ 8] = src[6]; dst[12] = src[ 9];
      dst[1] = src[1]; dst[5] = src[4]; dst[ 9] = src[7]; dst[13] = src[10];
      dst[2] = src[2]; dst[6] = src[5]; dst[10] = src[8]; dst[14] = src[11];
      dst[3] = 0.0f;   dst[7] = 0.0f;   dst[11] = 0.0f;   dst[15] = 1.0f;
      break;
    }
    }
  }
}

void Animation::prependParent(Animation* parent)
{
  // Make the child animation FINAL_ONLY
  makeFinal();

  // Animation final length
  int maxLength = MAX2(length, parent->length);

  // If child is shorter, make it longer and loop
  if( length < maxLength ){
    Matrix* newFinal = new Matrix[bones.getSize()*maxLength];
    for( int i = 0; i < maxLength; i++ ) {
      for( int k = 0; k < bones.getSize(); k++ ) {
        newFinal[ k * maxLength + i ].copyFrom( &finalMatrices[ k * length + (i % length) ] );
	  }
    }
    
    prependMatrices(newFinal, parent, maxLength);

    delete[] finalMatrices;
    finalMatrices = newFinal;
	length = maxLength;
  } else {
    prependMatrices(finalMatrices, parent, length);
  }
}

void Animation::prependMatrices(Matrix* child, Animation* parent, int length) {
  for( int j=0; j<bones.getSize(); j++ ){
    Bone* b = ((Bone*)bones.get(j));
    
    // Find root in parent
    Bone* parentBone = NULL;
    int p=0;
    for( p=0; p<parent->bones.getSize(); p++ ){
      Bone* pb = ((Bone*)parent->bones.get(p));
      if( b->Root != NULL && strcmp( pb->WeightMapName, b->Root->WeightMapName) == 0 )  {
	parentBone = ((Bone*)parent->bones.get(p));
	break;
      }
    }

    if( parentBone == NULL && b->Root != NULL ) {
      trace("Cannot find root %s of %s in parent animation", 
	    b->Root->WeightMapName, b->WeightMapName);
      continue;
    }

    // For all frames of this bone in child, pre-multiply the parent final
    for( int i=0; i<length; i++ ){
      Matrix parentFinal;
      parent->getFinalMatrix(p, i, &parentFinal);      
      child[j*length+i].premultiply(&parentFinal);

      float* m = parentFinal.getMatrixPointer();
    }
  }
}

void Animation::getFinalMatrix(int bone, int frame, Matrix* out)
{
  switch( dataMode ){
  case RESTMOVE_ONLY:
  case LAYERRESTMOVE_ONLY:
    out->copyFrom(&moveMatrices[bone*length+frame]);
    out->multiply(&restMatrices[bone]);
    break;
    
  case FINAL_ONLY:
  case RESTMOVEFINAL:
    out->copyFrom(&finalMatrices[bone*length+frame]);
    break;
  }
}

Matrix *Animation::getLayerMatrix() {
  return NULL;
}

void Animation::makeFinal()
{
  switch( dataMode ){
  case RESTMOVE_ONLY:
    dataMode = RESTMOVEFINAL;
    finalMatrices = new Matrix[bones.getSize()*length];
    for(int j=0; j<bones.getSize(); j++){
      for( int i=0; i<length; i++ ){
	finalMatrices[j*length+i].copyFrom(&moveMatrices[j*length+i]);
	finalMatrices[j*length+i].multiply(&restMatrices[j]);
      }
    }
    break;
    
  case FINAL_ONLY:
  case RESTMOVEFINAL:
    break;
  case LAYERRESTMOVE_ONLY:
    dataMode = RESTMOVEFINAL;
    finalMatrices = new Matrix[bones.getSize()*length];
    for(int j=0; j<bones.getSize(); j++){
      for( int i=0; i<length; i++ ){
    finalMatrices[j*length+i].copyFrom(getLayerMatrix());
	finalMatrices[j*length+i].multiply(&moveMatrices[j*length+i]);
	finalMatrices[j*length+i].multiply(&restMatrices[j]);
      }
    }
    break;
  }
}

void Animation::setRootBones() {
  for(int i=0; i<bones.getSize(); i++){
    Bone* bone = (Bone*)bones.get(i);
    
    // トップ
    Bone* top = getRootBone( bone );
    bone->Root = getRootBone( bone, top );
    trace("bone %s root: %s", bone->WeightMapName, bone->Root->WeightMapName);
  }
}
