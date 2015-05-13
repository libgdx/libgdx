/**
 * @file vertexBuffer.h
 * @brief Represents a vbo vertex buffer of some shader attribute
 **/
#pragma once 

#include "types.h"
#include GL2_H
#include "shaderProgram.h"

class Matrix;

/**
 * 頂点バッファー
 **/
class VertexBuffer
{
public:
  
  /**
   * フロートバッファーを作成
   **/
  VertexBuffer( ShaderProgram::INDEX_ATTRIBUTES attribute, float* data, int dataLength, int length, int componentsNum );
  
  /**
   * バイトバッファーを作成
   **/
  VertexBuffer( ShaderProgram::INDEX_ATTRIBUTES attribute, byte* data, int dataLength, int length, int componentsNum );

  /**
   * リソース開放
   **/
  ~VertexBuffer();

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
  bool bind();
  
  /**
   * バインドを解消する
   **/
  void unbind();

  /**
   * Get the float buffer
   **/
  float* getFloats();

  /**
   * Get the byte buffer
   **/
  byte* getBytes();

  /**
   * Gets buffer length
   **/
  int getLength();

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
  void append(VertexBuffer* vbuffer);

  /**
   * Append merge group with shifting
   **/
  void append(VertexBuffer* vbuffer, float x, float y, float z);
  void append(VertexBuffer* vbuffer, Matrix* transform);

  /**
   * Append arbitrary data
   **/
  void append(const float* data, int numComponents);
  void append(const byte* data, int numComponents);

  /**
   * Commit merge group
   **/
  void commitGroup();

  /**
   * @return 関連シェーダーattribute
   **/
  ShaderProgram::INDEX_ATTRIBUTES getShaderAttribute();

  /**
   * @brief	オリジナルデータを解放
   */
  void releaseOriginalData();
  
private:

  //データ配列を自動伸長する際の係数
  static const float kGrowth;

  // バッファーデータ
  union {
    float* floats;
    byte* bytes;
  } data;

  // バッファー長さ
  int length;

  //データ配列の長さ
  int dataLength;

  // VBOインデックス
  GLuint vboId;

  // バッファータイプ
  GLenum type;

  // コンポーネント数(一頂点に対する情報量)
  int componentsNum;

  // 関連のシェーダーattribute
  ShaderProgram::INDEX_ATTRIBUTES attribute;
};
