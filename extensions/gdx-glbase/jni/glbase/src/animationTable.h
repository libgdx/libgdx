/**
 * @file animationTable.h
 * @brief Animation resources table
 **/
#pragma once

#include "types.h"



// Forward declaration
class Animation;
class ArrayList;

/**
 * アニメーションリソースの管理
 **/
class AnimationTable
{
public:
  
  /**
   * 初期化
   **/ 
  AnimationTable();

  /**
   * @brief アニメーションリソースをロードする
   * @return 成功の場合は、新しいアニメーションを象徴するID、失敗の場合は-1
   **/
  int loadAnimation(char* bsFile, int bsFileLenght, byte* binFile, int binFileLength);

  /**
   * @brief アニメーションリソースをロードする
   * @return 成功の場合は、新しいアニメーションを象徴するID、失敗の場合は-1
   **/
  int loadAnimation(byte* bbmFile, int bbmFileLenght);

  /**
   * @brief 指定のアニメーションの関連リソースを開放する
   * @param loadAnimationで取得されたアニメーション番号
   *
   * @return 成功の場合はtrue、指定のIDが存在しない場合はfalse
   **/
  bool deleteAnimation(int animationID);

  /**
   * @brief 指定のアニメーションを取得する
   * @return 成功の場合は、取得したアニメーション、存在しない場合はNULL
   **/
  Animation* getAnimation(int animationID);

private:
  
  // アニメーションの最大数
  static const int MAX_ANIMATIONS = 5000;
  
  Animation* animations[MAX_ANIMATIONS];
  int nextAvailable;


  // private methods
  int addAnimation(Animation* animation);
};
