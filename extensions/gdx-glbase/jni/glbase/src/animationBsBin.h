/**
 * @file animationBsBin.h
 * @brief Represents an animation resource composed of a bs/bin file pair
 **/
#pragma once
#include "animation.h"
#include "arrayList.h"
#include "matrix.h"
#include "types.h"


// Forward declarations
class BinBones;
class Command;
class Scene;


/**
 * アニメーション
 **/
class AnimationBsBin : public Animation
{
 public:

  /**
   * 初期化
   **/
  AnimationBsBin(char* bsFile, int bsFileLength, byte* binFile, int binFileLength);

  /**
   * デストラクタ
   **/
  ~AnimationBsBin();

  /**
   * Bs/binファイルを読み込んで、アニメーションを準備します
   * BsファイルはJSON式文字列、binファイルはバイナリー形式です
   **/
  bool load();

protected:

  /**
   * レイヤー行列（最初のフレームだけ）
   */
  Matrix *getLayerMatrix();

 private:
  // 一時的データハンドル
  char* bsFile;
  int bsFileLength;
  byte* binFile;
  int binFileLength;
  
  // Bin
  BinBones* binBones;

  //レイヤー行列（最初のフレームだけ）
  Matrix layerMatrix;


  // private methods
  void createBones(Scene* scene);
  // Load the matrices
  void loadMatrices(byte* binFile);
  //レイヤー行列の最初のフレームだけを抽出
  bool getFirtLayerMatrix( Command *cmd );

};
