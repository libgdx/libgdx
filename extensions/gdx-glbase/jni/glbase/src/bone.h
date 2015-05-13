/**
 * @file bone.h
 * @brief Represent a bone's transformation
 **/
#pragma once

#include "matrix.h"
#include "arrayList.h"

/**
 * ボーン変換
 **/
class Bone
{
 public:
  
  /**
   * 初期化
   **/
  Bone();
  ~Bone();

  // アクセサー
  Matrix* getRestMatrix(){return &RestMatrix;}
  Matrix* getMoveMatrix(){return &MoveMatrix;}
  Matrix* getFinalMatrix(){ return FinalMatrix;}
  void setFinalMatrix(Matrix *m){FinalMatrix = m;}

  unsigned int ItemNo;
  unsigned int ParentNo;

  char*  Name;
  char*  WeightMapName;

  Bone* Parent;
  Bone* Root;

 private:
  // 行列
  Matrix RestMatrix;
  Matrix MoveMatrix;
  Matrix baseFinalMatrix; //MoveにRestをかけたもの.
  Matrix *FinalMatrix; //パレットになげるマトリックス.
};
