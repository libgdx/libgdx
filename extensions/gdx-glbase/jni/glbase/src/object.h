/**
 * @file object.h
 * @brief Private interface for object type
 **/
#pragma once 

#include "Iobject.h"
#include "arrayList.h"
#include "types.h"


/**
 * @brief レンダーメッシュを象徴するクラス
 **/
class Object : public IObject
{
public:

  /**
   * からのオブジェクトを生成します
   **/
  Object();

  /**
   * すべての所属リソースを解放する
   **/
  ~Object();
  
  /**
   * Bo3リソースを読み込み、Objectを構成します
   * VRAMロードや必要なGLセットアップが行われます
   * 
   * @param data bo3リソースのデータが格納されているバッファー
   * @param length dataバッファーの長さ
   * @return 成功：true エラー：false
   **/
  bool loadFromBo3(const byte* data, int length, bool gpuOnly=false);

  /**
   * @brief 他のオブジェクトをコピーして作成します。
   * ただし、頂点情報が一切コピーされません。
   * レイヤーが一つ、ポリゴンマップ一つ、全サーフェースの情報だけが
   * コピーされ、その他の情報が無視されます。
   *
   * @param このオブジェクトから情報がコピーされます
   **/
  bool loadFromObject(Object* object, int polygonMaps);
  
  /**
   * bo3モデルから一部の頂点・インデックスデータを利用し、
   * サブオブジェクトを抽出します。
   **/
  bool loadFromObjectPart(Object* object, int vertexOffset, int triangleOffset,
			  int vertexLength, int triangleLength, int layer, int polygonMap);

  /**
   * Load for sprite merge group
   **/
  bool loadForSpriteMergeGroup();

  /**
   * @return オブジェクトID
   **/
  char* getId();

  /**
   * @return オブジェクトの名前
   **/
  char* getName();

  /**
   * @return オブジェクトのバージョン
   **/
  char* getVersion();

  /**
   * @return オブジェクトのMetaInfo
   **/
  char* getMetainfo();
  
  /**
   * @return オブジェクトのファイル名
   **/
  char* getFilename();

  /**
   * @return ?
   **/
  int getLeftBitShift();

  /**
   * @return ?
   **/
  char* getShadeModel();

  /**
   * @return ?
   **/
  char* getShadeValue();

  /**
   * @return The layers in this object
   **/
  ArrayList* getLayers();

  /**
   * レンダリング準備
   **/
  bool initRenderEnv();

  /**
   * レンダリング準備を解消
   **/
  void uninitRenderEnv();

  /**
   * @return レンダリングの準備は済んでいるか否か
   **/
  bool isRenderEnvInitialized();

  /**
   * @return バウンディングボックス
   **/
  float* getBoundingBox();

  /**
   * @brief	オリジナルデータを解放
   */
  void releaseOriginalData();

  /**
   * @brief Sets the texture of the specified pm
   **/
  void setTexture( int layerIdx, int polygonMapIdx, int texture );
  void setTexture( int polygonMapIdx, int texture );

  /**
   * Draw call production
   **/
  void addDrawCalls( IRenderQueue* queue, RenderParameters* params );
  int prepareDrawCalls( DrawCall* &result, RenderParameters* params );


  /**
   * @brief Sets the index names for specified layer
   **/
  void setMatrixIndicesNames( ArrayList* names, int layerIdx=0 );

protected:
  // Copy utility
  void copyBasicInfo(Object* object);
  void setDefaultInfo();
  void copyParamsToDrawCall(RenderParameters* params, DrawCall* drawCall);

  // Bo3 fields
  char* id;
  char* name;
  char* version;
  char* metainfo;
  char* filename;
  int leftBitShift;
  char* shadeModel;
  char* shadeValue;

  // Substructures
  ArrayList layers;

  // ロード済みフラグ
  bool loaded;
  // レンダリング準備済
  bool renderEnvInitialized;

  // BBOX
  float boundingBox[6];

  //VRAMに転送後、オリジナルデータを解放するか
  bool gpuOnly;
  
  // Private methods
  bool loadObjectInfo( const byte *data, int offset );
  bool loadLayers( const byte *data, int offset );
  bool loadSurfaces( const byte *data, int offset, ArrayList* surfaces );
};
