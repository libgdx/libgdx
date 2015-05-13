/**
 * @file drawCallsPool.h
 * @brief Pool of draw calls
 **/
#pragma once

#include "renderEnums.h"

class DrawCall;
class IPolygonMap;

/**
 * Pool of draw calls
 **/
class DrawCallsPool 
{
 public:
  /**
   * 1フレームないの最大の可能な描画登録(同じリストで)
   **/
  static const int MAX_DRAW_CALLS = 4096;

  DrawCallsPool();
  ~DrawCallsPool();
 
  /**
   * 新しいDrawCallオブジェクトをプールから作成
   **/
  DrawCall* acquireDrawCall();
  DrawCall* acquireDrawCall(IPolygonMap* polygonMap);
  DrawCall* acquireDrawCall(float* bbox);
  DrawCall* acquireDrawCall(int numParticles);
  DrawCall* acquireDrawCall(RenderEnums::ClearMode mode, float* color);
  
  /**
   * Reset all instances in pool to available
   **/
  void reset();

 private:

  /**
   * DrawCallメモリープール
   **/
  DrawCall* drawCallPool;
  int nextAvailable;
};
