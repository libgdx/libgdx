/**
 * @file polygonMap.h
 * @brief Represents a polygonMap of a layer
 **/
#pragma once 

#include <stdio.h>
#include "IpolygonMap.h"
#include "types.h"
#include "surface.h"
#include GL2_H

// Forwards
class ArrayList;
class Layer;
class PolygonBuffer;
class VertexBuffer;


class PolygonMap : public IPolygonMap
{
 public:

  /**
   * からのポリゴンマップを作成
   **/
  PolygonMap();

  /**
   * すべてのリソースを開放
   **/
  ~PolygonMap();

  /**
   * バイトデータからロード
   **/
  bool load( const byte *data, int infoOffset, int uvsOffset, int vertexNum, 
	     VertexBuffer* points, VertexBuffer* vcolors, VertexBuffer* normals, 
	     VertexBuffer* matrixIndices, VertexBuffer* matrixWeights, 
	     ArrayList* matrixIndicesNames);

  /**
   * Copy from existing polygon map
   **/
  void loadFrom( PolygonMap* pm, int triangleOffset, int triangleLength, int vertexOffset, int vertexLength,
		 VertexBuffer* points, VertexBuffer* vcolors, VertexBuffer* normals, 
		 VertexBuffer* matrixIndices, VertexBuffer* matrixWeights, ArrayList* matrixIndicesNames);

  /**
   * Create for default merge group
   **/
  void loadForSpriteMergeGroup();

  /**
   * Create for merge group
   **/
  void loadMergeGroup( PolygonMap* pm, int maxVertices,
		       VertexBuffer* points, VertexBuffer* vcolors, VertexBuffer* normals, 
		       VertexBuffer* matrixIndices, VertexBuffer* matrixWeights);

  /**
   * Append polygon info only to merge group
   **/
  void appendPolygonMap( PolygonMap* pm, int voffset );

  /**
   * Append uvs only to merge group
   **/
  void appendUvs( PolygonMap* pm );

  /**
   * Append sprite to merge group
   **/
  void appendTriangles( int voffset, int vertices, int triangles, 
			const unsigned short* indices, const float* uvs);

  /**
   * Start a new merge group
   **/
  void startGroup();

  /**
   * Commit merge group to VBO
   **/
  void endGroup();

  /**
   * 相当するサーフェースをリンクする
   */
  void linkSurface(ArrayList* surfaces);

  /**
   * レンダリング準備
   **/
  bool initRenderEnv();

  /**
   * レンダリング準備を解消
   **/
  void uninitRenderEnv();

  /**
   * レンダリング
   **/
  void execRender();

  /**
   * @return ボーン名(ボーンの順番)
   **/
  ArrayList* getMatrixIndicesNames();

  /**
   * サーフェースバインド
   **/
  void bindSurface();

  /**
   * サーフェースのテクスチャーをバインド
   **/
  void bindSurfaceTexture();

  VertexBuffer* getPoints(){ return points; }
  VertexBuffer* getVcolors(){ return vcolors; }
  VertexBuffer* getNormals(){ return normals; }
  VertexBuffer* getMatrixIndices(){ return matrixIndices; }
  VertexBuffer* getMatrixWeights(){ return matrixWeights; }

  ISurface* getSurface(){ return &surface; }
  char* getSurfaceName() { return surfaceName; }

  /**
   * @brief	オリジナルデータを解放
   */
  void releaseOriginalData();

  float* getUvsBuffer();
  int getUvsBufferLength();
  unsigned short* getPolygonsBuffer();
  int getPolygonsBufferLength();

  void commitUvsChanges();
  void commitPolygonsChanges();


 private:

  // 使用のサーフェース
  char* surfaceName;
  // UV名
  char* uvName;

  // ポリゴンバッファ (GLDrawElementのIndexとなる)
  PolygonBuffer *polygons;

  // UVバッファー
  VertexBuffer* uvs;

  // 参照バッファー
  VertexBuffer* points;
  VertexBuffer* vcolors;
  VertexBuffer* normals;
  VertexBuffer* matrixIndices;
  VertexBuffer* matrixWeights;

  // ボーン名リスト
  ArrayList* matrixIndicesNames;

  // 関連のサーフェース
  Surface surface;

  // Private methods
  bool loadInfo( const byte *data, int offset );
  bool loadUVs( const byte *data, int offset, int vertexNum );
};
