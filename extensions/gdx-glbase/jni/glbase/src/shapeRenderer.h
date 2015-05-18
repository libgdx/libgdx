/**
 * @file shapeRenderer.h
 * @brief Methods for rendering simple geometry
 **/
#pragma once

#include "types.h"
#include "matrix.h"

class PolygonBuffer;
class VertexBuffer;

#define PARTICLE_MAX 128

/**
 * 簡単な立体/図形をレンダーする機能集
 **/
class ShapeRenderer 
{
public:

  /**
   * 初期化を行い、レンダーに必要なデータをロードする
   **/
  void initialize();

  /**
   * BBOXのレンダー
   **/
  void renderBBox( float* bbox );

  /**
   * パーティクルレンダー
   **/
  void renderParticles( int numParticles );

  /**
   * フールスクリーンレンダー
   **/
  void renderFullScreen();

  /**
   * 長方形スプライトを描画
   **/
  void renderRectangle( float left, float top, float right, float bottom );
    
  /**
   * 長方形スプライトを描画
   **/
  void renderSprite( float left, float top, float right, float bottom,
                    float srcLeft, float srcTop, float srcRight, float srcBottom,
                    Matrix* spriteTransform);

 private:
  
  VertexBuffer* particlePoints;
  VertexBuffer* particleUvs;
  PolygonBuffer *particlePolygons;

  // private methods
  void loadParticleSystem();
};
