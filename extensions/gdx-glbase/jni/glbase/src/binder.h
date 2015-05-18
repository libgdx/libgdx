/**
 * @file binder.h
 * @brief State machine that mantains current bind informations (vbufs, textures, shaders...)
 **/
#pragma once

#include "renderEnums.h"
#include "shaderProgram.h"
#include GL2_H

// Forward decs
class Framebuffer;
class PolygonBuffer;
class RenderQueue;
class Surface;
class VertexBuffer;

/**
 * ステートマシーンクラス、現在のglエンジンの
 * バインド状態を表し、再バインドの必要性などを判定する。
 *
 **/
class Binder
{
 public:

  /**
   * 初期化
   **/
  Binder();

  /**
   * 頂点バッファをバインド
   **/
  void bindBuffer(VertexBuffer* buffer);

  /**
   * 頂点バッファーをアンバインド
   **/
  void unbindBuffer(ShaderProgram::INDEX_ATTRIBUTES);

  /**
   * ポリゴンバッファをバインド
   **/
  void bindPolygons( PolygonBuffer *buffer );

  /**
   * ポリゴンバッファのバインドを解除
   **/
  void unbindPolygons();

  /**
   * 全てのバッファーをアンバインド
   **/
  void unbindAllBuffers();
  
  /**
   * テクスチャーをバインド
   **/ 
  void bindTexture(int textureID);

  /**
   * glBindTexture()を呼ぶ場合は、これでバインド情報をリセット
   */
  void resetTexture();

  /**
   * テクスチャーをアンバインド
   **/
  void unbindTexture();

  /**
   * FBOをバインド
   **/
  void bindFBO(int framebufferID);

  /**
   * FBOをバインド
   */
  void bindFBO( Framebuffer *fbo );

  /**
   * FBOをアンバインド
   **/
  void unbindFBO(RenderQueue* queue);

  /**
   * シェーダープログラムをバインド
   **/
  bool bindProgram(int shaderId);

  /**
   * シェーダープログラムをリセット
   */
  void resetProgram();

  /**
   * 初期の設定をバインドし、準備します
   **/
  void initialize();

  /**
   * ブレンドモードを設定
   **/
  void setBlendMode(bool blendSrcAlpha, RenderEnums::BlendMode blendMode);

  /**
   * Cullingモードを設定
   **/
  void setCullingMode(RenderEnums::CullingMode cullingMode);

  /**
   * デプステストを設定
   **/
  void setDepthTest(bool useDepthTest, RenderEnums::DepthFunc depthFunct);

  /**
   * デプスマスクを設定
   **/
  void setDepthMask(bool useDepthMask);

  /**
   * カラーマスクを設定
   **/
  void setColorMask(bool* colorMask);


  /**
   * バインド中のシェーダープログラムを取得
   * 何もバインドされていなければ-1
   **/
  int getCurrentProgram();

  /**
   * ブレンド、カリング等の各強制設定フラグを立てます
   */
  void setForceSetFlags();

 private:
  
  // 頂点バッファー(シェーダの各attributeに対する)
  VertexBuffer* curVertexBuffers[ShaderProgram::IATTRIBUTE_MAX];

  //ポリゴンバッファー（glDrawElements用）
  PolygonBuffer *curPolygonBuffer;

  // テクスチャ
  int curTexture;

  // FBO
  Framebuffer *curFBO;
  GLint screenFBO;

  // 現在バインド中のプログラム
  int bindedProgram;

  // ブレンドモード
  bool useBlend;
  bool blendSrcAlpha;
  RenderEnums::BlendFunc blendFunc;
  RenderEnums::BlendEq blendEq;

  // Cullingモード
  RenderEnums::CullingMode cullingMode;

  // デプステスト
  bool useDepthTest;
  RenderEnums::DepthFunc depthFunc;
  
  // デプスマスク
  bool useDepthMask;
  
  // カラーマスク
  bool colorMask[4];

  //ビューポート
  int viewport[ 4 ];

  bool forceSetUseBlend;
  bool forceSetBlendFunc;
  bool forceSetBlendEq;
  bool forceSetCullingMode;
  bool forceSetUseDepthTest;
  bool forceSetDepthFunc;
  bool forceSetDepthMask;
  bool forceSetColorMask;
  bool forceSetViewport;
  
  /*!
   * @brief	ブレンド設定.
   */
  void setUseBlend( bool useBlend );

  /*!
   * @brief	ブレンド関数設定.
   */
  void setBlendFunc( bool blendSrcAlpha, RenderEnums::BlendFunc blendFunc );
  
  /*!
   * @brief	ブレンド式設定.
   */
  void setBlendEq( RenderEnums::BlendEq blendEq );

  /**
   * ビューポート設定
   */
  void setViewport( int viewport[ 4 ] );
};
