/**
 * @file Ilayer.h
 * @brief Public interface of a layer of an object
 **/
#pragma once

class ArrayList;

class ILayer
{
 public:

  virtual ~ILayer() {
  }

  /**
   * @return The polygon maps in this layer
   **/
  virtual ArrayList* getPolygonMaps() = 0;

  /**
   * @return 頂点数
   **/
  virtual int getPointsNum() = 0;

  /**
   * 頂点バッファーデータを取得します。
   * データの内容を編集することが出来るのは、
   * initRenderEnv()が呼ばれる時までになります。
   * initRenderEnv()が実行済みのポリゴンマップは、
   * 一度uninitRenderEnv()を呼び、バッファーを取得し
   * 編集して、再度initRenderEnv()を呼び出す必要があります。
   **/
  virtual float* getPointsBuffer() = 0;
  
  /**
   * 頂点色バッファーデータを取得します。
   * データの内容を編集することが出来るのは、
   * initRenderEnv()が呼ばれる時までになります。
   * initRenderEnv()が実行済みのポリゴンマップは、
   * 一度uninitRenderEnv()を呼び、バッファーを取得し
   * 編集して、再度initRenderEnv()を呼び出す必要があります。
   **/
  virtual float* getVcolorsBuffer() = 0;

  /**
   * 法線バッファーデータを取得します。
   * データの内容を編集することが出来るのは、
   * initRenderEnv()が呼ばれる時までになります。
   * initRenderEnv()が実行済みのポリゴンマップは、
   * 一度uninitRenderEnv()を呼び、バッファーを取得し
   * 編集して、再度initRenderEnv()を呼び出す必要があります。
   **/
  virtual float* getNormalsBuffer() = 0;

  /**
   * 行列インデックスバッファーデータを取得します。
   * データの内容を編集することが出来るのは、
   * initRenderEnv()が呼ばれる時までになります。
   * initRenderEnv()が実行済みのポリゴンマップは、
   * 一度uninitRenderEnv()を呼び、バッファーを取得し
   * 編集して、再度initRenderEnv()を呼び出す必要があります。
   **/
  virtual byte* getMatrixIndicesBuffer() = 0;

  /**
   * ウエイトバッファーデータを取得します。
   * データの内容を編集することが出来るのは、
   * initRenderEnv()が呼ばれる時までになります。
   * initRenderEnv()が実行済みのポリゴンマップは、
   * 一度uninitRenderEnv()を呼び、バッファーを取得し
   * 編集して、再度initRenderEnv()を呼び出す必要があります。
   **/
  virtual float* getMatrixWeightsBuffer() = 0;

  /**
   * @return レイヤー名
   **/
  virtual char* getName() = 0;

  /**
   * @return BBOX
   **/
  virtual float* getBoundingBox() = 0;

  /**
   * @param names ウエイト名リスト
   */
  virtual void setMatrixIndicesNames( ArrayList* names ) = 0;

  /**
   * getPointsBuffer()で頂点情報を取得し、編集を行ったあと
   * 一度このメソッドを呼び出すことで、編集された頂点が
   * VBOにコミットされます。
   **/
  virtual void commitPointsChanges() = 0;

  /**
   * getVcolorsBuffer()で頂点情報を取得し、編集を行ったあと
   * 一度このメソッドを呼び出すことで、編集された頂点が
   * VBOにコミットされます。
   **/
  virtual void commitVcolorsChanges() = 0;

  /**
   * getNormalsBuffer()で頂点情報を取得し、編集を行ったあと
   * 一度このメソッドを呼び出すことで、編集された頂点が
   * VBOにコミットされます。
   **/
  virtual void commitNormalsChanges() = 0;

  /**
   * getMatrixIndicesBuffer()で頂点情報を取得し、編集を行ったあと
   * 一度このメソッドを呼び出すことで、編集された頂点が
   * VBOにコミットされます。
   **/
  virtual void commitMatrixIndicesChanges() = 0;

  /**
   * getMatrixWeightsBuffer()で頂点情報を取得し、編集を行ったあと
   * 一度このメソッドを呼び出すことで、編集された頂点が
   * VBOにコミットされます。
   **/
  virtual void commitMatrixWeightsChanges() = 0;
};
