/**
 * @file IpolygonMap.h
 * @brief Public interface of a polygonMap of an layer
 **/
#pragma once

class ISurface;

class IPolygonMap
{
 public:

  virtual ~IPolygonMap() {
  }

  /**
   * UVバッファーデータを取得します。
   * データの内容を編集することが出来るのは、
   * initRenderEnv()が呼ばれる時までになります。
   * initRenderEnv()が実行済みのポリゴンマップは、
   * 一度uninitRenderEnv()を呼び、バッファーを取得し
   * 編集して、再度initRenderEnv()を呼び出す必要があります。
   **/
  virtual float* getUvsBuffer() = 0;

  /**
   * @return UVバッファーの長さ(floatの数)
   **/
  virtual int getUvsBufferLength() = 0;

  /**
   * Polygonsバッファーデータを取得します。
   * データの内容を編集することが出来るのは、
   * initRenderEnv()が呼ばれる時までになります。
   * initRenderEnv()が実行済みのポリゴンマップは、
   * 一度uninitRenderEnv()を呼び、バッファーを取得し
   * 編集して、再度initRenderEnv()を呼び出す必要があります。
   **/
  virtual unsigned short* getPolygonsBuffer() = 0;

  /**
   * @return Polygonsバッファーの長さ(unsigned shortの数)
   **/
  virtual int getPolygonsBufferLength() = 0;

  /**
   * getUvsBuffer()で頂点情報を取得し、編集を行ったあと
   * 一度このメソッドを呼び出すことで、編集された頂点が
   * VBOにコミットされます。
   **/  
  virtual void commitUvsChanges() = 0;

  /**
   * getPolygonsBuffer()で頂点情報を取得し、編集を行ったあと
   * 一度このメソッドを呼び出すことで、編集された頂点が
   * VBOにコミットされます。
   **/
  virtual void commitPolygonsChanges() = 0;


  /**
   * @return サーフェイス
   */
  virtual ISurface* getSurface() = 0;

  /**
   * @return サーフェイス名
   */
  virtual char* getSurfaceName() = 0;
};
