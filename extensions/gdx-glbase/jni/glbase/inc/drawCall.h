/**
 * @file drawCall.h
 * @brief A wrapper class for rendering options
 **/
#pragma once

#include <map>

#include "renderEnums.h"
#include "matrix.h"
#include "types.h"
#include "renderEnums.h"
#include GL2_H

// Forward decs
class IPolygonMap;
class IObject;
class IAnimationPlayer;
class ArrayList;
class RenderPass;
class RenderQueue;
class Object;
class PolygonMap;
class MyUniformValue;

#define TYPE_PMP 0
#define TYPE_FUL 1
#define TYPE_BOX 2
#define TYPE_PRT 3
#define TYPE_CLR 4


/**
 * 描画登録データ
 **/
class DrawCall
{
  friend class RenderList;

public:
  Matrix modelTransform;      // モデル変換
  
  union{
    IPolygonMap* polygonMap;  // ポリゴンマップレンダー単位候補
    float box[6];             // Boxレンダー候補
    int numParticles;         // Particles
    struct{
      float color[4];         // Clear color
      RenderEnums::ClearMode mode; // Clear mode
    } clear;
  } renderTarget;

  int shader;                 // 使用シェダープログラム
  int texture;                // 使用テクスチャー
  int framebuffer;            // 描画先FBO (-1=画面)
  IAnimationPlayer* animationPlayer; // 設定アニメーション
  std::map<int, MyUniformValue*> myUniforms; // 使用シェーダ用カスタムUniform

  // ブレンドモード
  bool blendSrcAlpha;
  RenderEnums::BlendMode blendMode;

  RenderEnums::CullingMode cullingMode;  // カーリングモード

  // デプステスト
  bool useDepthTest;
  RenderEnums::DepthFunc depthFunc;

  bool depthMask;                        // デプスマスク
  bool colorMask[4];                     // カラーマスク

  byte renderType;            // TYPE_PMP:ポリゴンマップ単位レンダー TYPE_OBJ:オブジェクト単位レンダー
  /**
   * Setters
   **/
  void set( IPolygonMap* polygonMap );
  void set( RenderEnums::ClearMode mode, float* clearColor );
  void set( float* box );
  void set( int numParticles );
  void setFullScreen();
  void set( DrawCall *drawCall );

private:
  void setDefault();  

  // Package scope
  void execRender(RenderQueue* queue);
  void execRenderPolygonMap(RenderQueue* queue, PolygonMap* pm);
  void execRenderFullScreen(RenderQueue* queue);
  void execRenderBBox(RenderQueue* queue, float* box);
  void execRenderParticle(RenderQueue* queue, int numParticles);
  void bindTexture();
  void bindModelTransform();
  void clear();
};
