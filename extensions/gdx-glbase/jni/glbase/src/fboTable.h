/**
 * @file fboTable.h
 * @brief Module for management of framebuffer resources
 **/
#pragma once

#include "types.h"
#include "framebuffer.h"


/**
 * FBO管理
 **/
class FBOTable
{
 public:

  /**
   * 初期化
   **/
  FBOTable();

  /**
   * @brief 新規テクスチャー作成
   **/
  int createFBO();
 
  /**
   * 以前ロードされたFBOを削除する
   **/
  bool deleteFBO(int fboID);

  /**
   * 指定のFBOを取得する
   * @return 指定のFBOが存在する場合はそのFBO、存在しない場合はNULL
   **/
  Framebuffer* getFramebuffer(int fboID);


 private:
  
  // FBOの最大数
  static const int MAX_FBO = 512;

  // FBOマップ
  Framebuffer* fbos[MAX_FBO];
  int nextAvailable;
};
