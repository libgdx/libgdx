/**
 * @file layer.h
 * @brief Represents a layer of an object
 **/
#pragma once 

#include <stdio.h>
#include "Ilayer.h"
#include "arrayList.h"
#include "types.h"
#include GL2_H

// Forwards
class Object;
class VertexBuffer;


/**
 * @brief Vertex's maximum number of components
 **/
#define MAX_VERTEX_UNITS 4

/**
 * @brief Maximum number of palette matrices
 **/
#define MAX_PALETTE_MATRICES 32


/**
 * @brief レイヤークラス
 **/
class Layer : public ILayer
{
 public:

  /**
   * からのレイヤーを生成します
   **/
  Layer();

  /**
   * すべての所属リソースを解放する
   **/
  ~Layer();

  
  /**
   * バイトデータからロード
   **/
  bool load(const byte *data, int infoOffset, int weightNamesOffset, int polygonMapsOffset, Object* parent);

  /**
   * Load from another layer
   **/
  void loadFrom(Layer* layer, int vertexOffset, int triangleOffset,
		int vertexLength, int triangleLength, int polygonMap, int polygonMaps);

  /**
   * Load for a sprite merge group
   **/
  void loadForSpriteMergeGroup();

  /**
   * Create empty buffers for merge group
   **/
  void createMergeBuffers(Layer* layer, int maxVertices, int polygonMaps);

  /**
   * Start merge group
   **/
  void startGroup();

  /**
   * Commits merges to VBO
   **/
  void endGroup();

  /**
   * Add polygon map to merge group
   **/
  void addPolygonMap(PolygonMap* pm, int targetPmId, Matrix* transform);

  /**
   * Add polygon map to merge group
   **/  
  void addPolygonMap(PolygonMap* pm, int targetPmId, float x, float y, float z);

  /**
   * Add sprite to merge group
   **/
  void addTriangles(int vertices, int triangles, const float* points, 
		    const unsigned short* indices, const float* uvs);

  /**
   * GLリソースをロード、レンダリングの準備
   **/
  bool initRenderEnv();

  /**
   * レンダリング準備を解消
   **/
  void uninitRenderEnv();

  /**
   * @return The polygon maps in this layer
   **/
  ArrayList* getPolygonMaps();
  
  /**
   * @return レイヤー名
   **/
  char* getName();

  /**
   * @return BBOX
   **/
  float* getBoundingBox();

  /**
   * @brief	オリジナルデータを解放
   */
  void releaseOriginalData();

  /**
   * @brief Set matrix indices names (replaces)
   **/
  void setMatrixIndicesNames( ArrayList* names );

  // Implement
  int getPointsNum();
  float* getPointsBuffer();
  float* getVcolorsBuffer();
  float* getNormalsBuffer();
  byte* getMatrixIndicesBuffer();
  float* getMatrixWeightsBuffer();

  void commitPointsChanges();
  void commitVcolorsChanges();
  void commitNormalsChanges();
  void commitMatrixIndicesChanges();
  void commitMatrixWeightsChanges();

  
 private:
  // 頂点バッファー
  VertexBuffer* points;
  // 頂点色バッファー
  VertexBuffer* vcolors;
  // 法線バッファー
  VertexBuffer* normals;
  // 行列インデックスバッファー
  VertexBuffer* matrixIndices;
  // 頂点ウエイトバッファー
  VertexBuffer* matrixWeights;

  // ??
  unsigned int ItemNo;
  unsigned int ParentNo;

  // 情報
  char* id;
  char* name;
  float boundingBox[ 6 ];
  float* pivot;

  // Substructures
  ArrayList polygonMaps;

  // ボーン名リスト
  ArrayList matrixIndicesName;


  // Private methods
  bool loadInfo( const byte *data, int offset, int &outPointsCnt );
  bool loadWeightNames( const byte *data, int offset );
  bool loadPolygonMaps( const byte *data, int offset, Object *parent, int pointsCnt );

  void appendAllButPoints(PolygonMap* pm, int targetPmId);
};
