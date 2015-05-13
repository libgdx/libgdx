/**
 * @file renderList.h
 * @brief Render priority list and management
 **/
#pragma once

#include "arrayList.h"
#include "matrix.h"
#include "renderEnums.h"

// Forward declarations
class PolygonMap;
class Object;
class ShaderProgram;
class DrawCall;
class RenderQueue;

/**
 * Render priority list and management
 **/
class RenderList
{
 public:

  /**
   * 初期化
   **/
  RenderList();

  /**
   * リソース開放
   **/
  ~RenderList();

  /**
   * Draw call registration
   **/
  void registerDrawCall(DrawCall* drawCall);
  
  /**
   * すべての登録されたレンダリングを行う
   * @param	clearScreen	glClear()を呼ぶか
   **/
  void execRender( RenderQueue* queue );

private:

  /**
   * 優先リスト
   **/
  ArrayList* renderList;
};
