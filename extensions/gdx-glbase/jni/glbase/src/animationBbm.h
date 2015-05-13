/**
 * @file animationBbm.h
 * @brief Represents an animation resource composed of a bbm file
 **/
#pragma once
#include "types.h"
#include "arrayList.h"
#include "animation.h"
#include "matrix.h"

/**
 * アニメーション
 **/
class AnimationBbm : public Animation
{
 public:

  /**
   * 初期化
   **/
  AnimationBbm(byte* bbmFile, int bbmFileLength);

  /**
   * デストラクタ
   **/
  ~AnimationBbm();

  /**
   * Bbmファイルを読み込んで、アニメーションを準備します
   **/
  bool load();

 private:

  // 設定
  ValueType valueType;
  byte leftBitShift;
  int bonesNum;

  // 一時的データハンドル
  byte* bbmFile;
  int bbmFileLength;


  // private methods
  bool checkBbmHeader(byte* bbmFile, int* boneInfoOffset, int* boneMatricesOffset);
  void loadMotionDec(byte* bbmFile);
  void loadBonesInfo(byte* bbmFile, int offset);
  void loadBoneInfo(byte* bbmFile, int offset, Bone* bone);
  void loadBoneMatrices(byte* bbmFile, int offset);

  void getRestMatrix(int bone, Matrix* outMatrix);
  void getMoveMatrix(int bone, int frame, Matrix* outMatrix);
  
  void getMatrix(byte* matrixDataStart, Matrix* outMatrix);
};
