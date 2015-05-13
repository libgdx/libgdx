/**
 * @file object.h
 * @brief Public interface for object type
 **/
#pragma once

#include "Imrf.h"
#include "renderEnums.h"

// Forward declarations
class ArrayList;
class DrawCall;
class ILayer;
class IRenderQueue;
class ISurface;

/**
 * IObject描画コール作成をしていただくには必要な
 * パラメターの構造体
 **/
class RenderParameters : public BasicRenderParameters
{
 public:
  RenderParameters();

  int shader;                 // 使用シェダープログラム

    // ブレンドモード
  bool blendSrcAlpha;
  RenderEnums::BlendMode blendMode;

  RenderEnums::CullingMode cullingMode;  // カーリングモード

  // デプステスト
  bool useDepthTest;
  RenderEnums::DepthFunc depthFunc;

  bool depthMask;                        // デプスマスク
  bool colorMask[4];                     // カラーマスク
};


/**
 * Objectのpublicインターフェース
 **/
class IObject
{

public:
  
  virtual ~IObject() {
  }

  /**
   * @return オブジェクトID
   **/
  virtual char* getId() = 0;

  /**
   * @return オブジェクトの名前
   **/
  virtual char* getName() = 0;

  /**
   * @return オブジェクトのバージョン
   **/
  virtual char* getVersion() = 0;

  /**
   * @return オブジェクトのMetaInfo
   **/
  virtual char* getMetainfo() = 0;
  
  /**
   * @return オブジェクトのファイル名
   **/
  virtual char* getFilename() = 0;

  /**
   * @return バウンディングボックス
   **/
  virtual float* getBoundingBox() = 0;

  /**
   * @return ?
   **/
  virtual int getLeftBitShift() = 0;

  /**
   * @return ?
   **/
  virtual char* getShadeModel() = 0;

  /**
   * @return ?
   **/
  virtual char* getShadeValue() = 0;

  /**
   * @return The layers in this object
   **/
  virtual ArrayList* getLayers() = 0;
  
  /**
   * @brief テクスチャーをサーフェースに貼り付けます
   * オブジェクトがオブジェクトレンダー単位としてレンダーされる時、
   * 指定のサーフェースがしようされるタイミングで、テクスチャーがバインドされます。
   *
   * @param layerIdx オブジェクト内のLayer番号(0~
   * @param polygonMapIdx オブジェクト内のPolygonMap番号(0~
   * @param texture テクスチャーリソースの仮ID
   *
   **/
  virtual void setTexture( int layerIdx, int polygonMapIdx, int texture ) = 0;

  /**
   * @brief setTexture(0, polygonMapIdx, texture) 
   * 
   * @param polygonMapIdx オブジェクト内のPolygonMap番号(0~
   * @param texture テクスチャーリソースの仮ID
   *
   **/
  virtual void setTexture( int polygonMapIdx, int texture ) = 0;

  /**
   * @brief produceDrawCallsをし、その結果で生成された
   * DrawCallらを指定のリストに登録します
   *
   * @param queue このリストに登録されます
   * @param params 入力専用：レンダー設定指定
   **/
  virtual void addDrawCalls( IRenderQueue* queue, 
			     RenderParameters* params ) = 0;

  /**
   * @brief このオブジェクト内のポリゴンマップの数分だけ、DrawCallを準備します。
   *
   * @param result この配列に結果のDrawCallが必要されます
   * @param params 入力専用：レンダー設定指定
   *
   * @return 準備されたDrawCall数
   **/
  virtual int prepareDrawCalls( DrawCall* &result, 
				RenderParameters* params ) = 0;

  /**
   * @brief 行列インデックスの名前らを設定します。
   * このメソッドでは、以前に保管されているインデックス名は
   * 解法され、上書き設定となります。
   *
   * @names const char*で指定された名前の羅列
   * @layerIdx 適応レイヤー
   **/
  virtual void setMatrixIndicesNames( ArrayList* names, int layerIdx=0 ) = 0;

  /**
   * @brief オブジェクトをレンダリングできる状態にする
   * レンダリングを登録する前、一度このメソッドでオブジェクトを準備
   * しないとレンダリングができません。
   *
   * 必要なVRAMバッファーリソースなどロードされます。
   *
   * @return 成功の場合はtrue、エラーの場合はfalse
   **/
  virtual bool initRenderEnv() = 0;

  /**
   * @brief	オリジナルデータを解放
   */
  virtual void releaseOriginalData() = 0;
};
