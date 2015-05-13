/**
 * @file Imrf.h
 * @brief Public interface for mrf type
 **/
#pragma once

#include "arrayList.h"
#include "matrix.h"

// Forward declarations
class IRenderQueue;
class DrawCall;
class IAnimationPlayer;
class MyUniformValue;
class IObject;
class IRenderPass;

/**
 * Mrf描画コール作成をしていただくには必要な
 * パラメターの構造体
 **/
class BasicRenderParameters
{
 public:
  BasicRenderParameters();

  Matrix modelTransform;      // モデル変換
  int texture;                // 使用テクスチャー
  int framebuffer;            // 描画先FBO (-1=画面)
  IAnimationPlayer* animationPlayer; // 設定アニメーション
};

/**
 * MRFのpublicインターフェース
 **/
class IMrf
{

public:
  
  virtual ~IMrf() {
  }

  /**
   * @brief mrf共通の15個FBOをロードし、準備します。
   * mrfレンダリングを行うにはこのメソッドを初期時一度呼び出す必要があります。
   **/
  static void initialize( int screenWidth, int screenHeight );

  /**
   * @param renderTarget レンダーターゲット(RenderEnums::TextureType)
   * @return 指定レンダーターゲットのフレームバッファ
   */
  static int getFramebuffer( int renderTarget );

  /**
   * @brief produceDrawCallsをし、その結果で生成された
   * DrawCallらを指定のリストに登録します
   *
   * @param queue このリストに登録されます
   * @param params 入力専用：レンダー設定指定
   **/
  virtual void registerDrawCalls(IRenderQueue* queue, 
				 BasicRenderParameters* params,
				 IObject* object) = 0;
  
  /**
   * @brief mrf定義のレンダーパスを必要な分だけ、DrawCallを準備します。
   * 指定のdrawCallで設定されているテクスチャー、シェーダー、モデルなど
   * がRenderModeがDEFAULTのパスの場合で使われ、mrf内での他の指定の方が
   * 優先的に使われます。
   *
   * @param result この配列に結果のDrawCallが必要されます
   * @param params 入力専用：レンダー設定指定
   *
   * @return 準備されたDrawCall数
   **/
  virtual int prepareDrawCalls(DrawCall* &result,
			       BasicRenderParameters* params,
			       IObject* object) = 0;

  /**
   * @return パスの数
   */
  virtual int getPassesNum() = 0;

  /**
   * @param passIndex パスのインデックス
   * @return パス情報
   */
  virtual IRenderPass *getPass( int passIndex ) = 0;

  /**
   * @return ID
   */
  virtual char *getId() = 0;

  /**
   * ロード時にskipSetUniformsをtrueにしていた場合は、使用前にこれを一度呼ぶ
   */
  virtual void setUniforms() = 0;
};
