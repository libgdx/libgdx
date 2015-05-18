/**
 * @file animationBbm.cpp
 * @brief Represents an animation resource composed of a bbm file
 **/
#include <string.h>
#include "animationBbm.h"
#include "bone.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"

// BBMヘヘッダーサイズ
#define BBM_HEADER_SIZE 16

AnimationBbm::AnimationBbm(byte* bbmFile, int bbmFileLength)
{
  this->bbmFile = bbmFile;
  this->bbmFileLength = bbmFileLength;  
}

AnimationBbm::~AnimationBbm()
{ 
}

bool AnimationBbm::load()
{
  int boneInfoOffset;
  int boneMatricesOffset;

  // ヘッダーチェック
  if( !checkBbmHeader(bbmFile, &boneInfoOffset, &boneMatricesOffset) ) return false;
 
  // モーション定義を読み込む
  loadMotionDec(bbmFile);
  trace("Motion dec loaded.");

  // ボーン行列をロード
  loadBoneMatrices(bbmFile, boneMatricesOffset);
  trace("Bone matrices loaded.");

  // ボーンをロード
  loadBonesInfo(bbmFile, boneInfoOffset);
  trace("Bone info loaded.");
  
  return true;
}

bool AnimationBbm::checkBbmHeader(byte* bbmFile, int* boneInfoOffset, int* boneMatricesOffset)
{
  int offset = 0;

  // 署名
  if( FileUtils::readByte(bbmFile, offset) != 'B' ) return false;
  if( FileUtils::readByte(bbmFile, offset) != 'B' ) return false;
  if( FileUtils::readByte(bbmFile, offset) != 'M' ) return false;

  if( FileUtils::readByte(bbmFile, offset) != 1 ) return false; // Major version
  if( FileUtils::readByte(bbmFile, offset) != 0 ) return false; // Minor version

  offset += 3; //unused

  *boneInfoOffset = FileUtils::readInt(bbmFile, offset);
  *boneMatricesOffset = FileUtils::readInt(bbmFile, offset);

  return true;
}

void AnimationBbm::loadMotionDec(byte* bbmFile)
{
  int offset = BBM_HEADER_SIZE;

  int idLength = FileUtils::readByte(bbmFile, offset);
  int metaInfoLength = FileUtils::readByte(bbmFile, offset);

  valueType = (ValueType)FileUtils::readByte(bbmFile, offset);
  leftBitShift = FileUtils::readByte(bbmFile, offset);
  
  bonesNum = FileUtils::readByte(bbmFile, offset);
  dataMode = (DataMode)FileUtils::readByte(bbmFile, offset);
  fps = FileUtils::readByte(bbmFile, offset);
  length = FileUtils::readInt(bbmFile, offset);  
}

void AnimationBbm::loadBonesInfo(byte* bbmFile, int offset)
{
  int startPos = offset;
  
  int* offsets = FileUtils::readInts(bbmFile, offset, bonesNum);
  for( int i=0; i<bonesNum; i++ ){
    Bone* bone = new Bone();
    loadBoneInfo(bbmFile, startPos + offsets[i], bone);

    // Set REST matrix once and for all
    if( dataMode == RESTMOVE_ONLY || dataMode == RESTMOVEFINAL ){
      bone->getRestMatrix()->copyFrom(&restMatrices[i]);
    }

    bones.add(bone);
  }

  // 親設定
  for( int i=0; i<bonesNum; i++ ){
    Bone* b = (Bone*)bones.get(i);
    if( b->ParentNo == 0xFF ) b->Parent = NULL;
    else b->Parent = (Bone*)bones.get(b->ParentNo);
  }
  
  // ルートボーン設定
  setRootBones();

  delete[] offsets;
}

void AnimationBbm::loadBoneInfo(byte* bbmFile, int offset, Bone* bone)
{
  bone->ParentNo = FileUtils::readByte(bbmFile, offset);
  int nameLength = FileUtils::readByte(bbmFile, offset);
  int weightMapNameLength = FileUtils::readByte(bbmFile, offset);

  bone->Name = FileUtils::readString(bbmFile, offset, nameLength);
  bone->WeightMapName = FileUtils::readString(bbmFile, offset, weightMapNameLength);

  // WeightMap name to lower case
  for( int i=0; i<weightMapNameLength; i++ ){
    if( bone->WeightMapName[i] >= 'A' && bone->WeightMapName[i] <= 'Z' ){
      bone->WeightMapName[i] += 'a'-'A';
    }
  }

  trace("bone %s", bone->WeightMapName);
}

void AnimationBbm::loadBoneMatrices(byte* bbmFile, int offset)
{
  if( dataMode == RESTMOVE_ONLY || dataMode == RESTMOVEFINAL ){
    // Copy rest matrices
    restMatrices = new Matrix[bonesNum];
    copyMatrices(restMatrices, bbmFile + offset, bonesNum, valueType);
    offset += bonesNum * MATRIX_LENGTH;
  }

  if( dataMode == RESTMOVE_ONLY || dataMode == RESTMOVEFINAL ){
    moveMatrices = new Matrix[bonesNum*length];
    copyMatrices(moveMatrices, bbmFile + offset, bonesNum*length, valueType);
    offset += length * bonesNum * MATRIX_LENGTH;
  }

  if( dataMode == FINAL_ONLY || dataMode == RESTMOVEFINAL ){
    finalMatrices = new Matrix[bonesNum*length];
    copyMatrices(finalMatrices, bbmFile + offset, bonesNum*length, valueType);
  }
}
