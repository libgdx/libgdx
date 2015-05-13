/**
 * @file polygonBuffer.h
 * @brief Represents a vbo element array buffer
 **/
#pragma once 

#include "types.h"
#include GL2_H

/**
 * ポリゴンバッファー（＝三角バッファー）
 **/
class PolygonBuffer
{
public:
  
  /**
   * ショートバッファーを作成
   **/
  PolygonBuffer( unsigned short *data, int dataLength, int elementsCnt );
  
  /**
   * バイトバッファーを作成
   **/
  PolygonBuffer( byte *data, int dataLength, int elementsCnt );

  /**
   * リソース開放
   **/
  ~PolygonBuffer();

  /**
   * バッファーをVboにロードする
   **/
  bool loadVbo();

  /**
   * バッファーを削除
   **/
  void unloadVbo();

  /**
   * バッファーをバインドする
   **/
  void bind();

  /**
   * 描画
   * @param drawCnt 描画エレメント数（負の値ならバッファ内全てを描画）
   **/
  void draw( int drawCnt = -1 );

  /**
   * Get the short buffer
   **/
  unsigned short *getShorts();

  /**
   * Get the byte buffer
   **/
  byte *getBytes();

  /**
   * Get the number of elements
   **/
  int getElementsCnt();

  /**
   * Clear merge group
   **/
  void clear();

  /**
   * 必要であれば、配列を伸長
   * @param addDataLength 追加するデータの長さ
   */
  void adjustData( int addDataLength );

  /**
   * Append merge group
   **/
  void append( PolygonBuffer* polygonBuffer, int vertexOffset );

  /**
   * Append arbitrary data
   **/
  void append( const unsigned short *appendData, int appendElementsCnt, int vertexOffset );
  void append( const byte *appendData, int appendElementsCnt, int vertexOffset );

  /**
   * Commit merge group
   **/
  void commitGroup();

  /**
   * @brief	オリジナルデータを解放
   */
  void releaseOriginalData();
  
private:

  //データ配列を自動伸長する際の係数
  static const float kGrowth;
  
  // バッファーデータ
  union {
    unsigned short *shorts;
    byte *bytes;
  } data;

  // エレメント数（＝ポリゴン数×3）
  int elementsCnt;

  //データ配列の長さ
  int dataLength;

  // VBOインデックス
  GLuint vboId;

  // バッファータイプ
  GLenum type;

  //タイプのサイズ
  int sizeOfType;
};
