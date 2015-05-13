/**
 * @file surface.h
 * @brief Represents a surface of a bo3 object
 **/
#pragma once 

#include <stdio.h>
#include "Isurface.h"
#include "arrayList.h"
#include "types.h"
#include GL2_H


class Surface : public ISurface
{
public:

  /**
   * 空っぽのサーフェースを作成
   **/
  Surface();

  /**
   * リリースを開放
   **/
  ~Surface();

  /**
   * バイトデータからロードする
   **/
  bool load(const byte *data, int infoOffset, int texturesOffset);

  /**
   * For sprite merge group
   **/
  bool loadForSpriteMergeGroup();

  /**
   * Copy load
   **/
  void loadFrom(Surface* surface);

  /**
   * サーフェース名取得
   **/
  char* getName();

  /**
   * テクスチャーを設定
   **/
  void setTexture( int texture );

  /**
   * @return Texture id
   **/
  int getTexture();

  /**
   * バインドする
   **/ 
  void bind();

  /**
   * サーフェースのテクスチャーをバインドする
   **/
  void bindTexture();

private:

  char* id;
  char* name;
  char* face;

  float ambient[4];
  float diffuse[4];
  float specular[4];
  float shininess[4];
  float emission[4];

  int texture;
};
