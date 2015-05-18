/**
 * @file textureTable.h
 * @brief Module for management of texture resources
 **/
#pragma once

#include "types.h"


// Forward declarations
class Texture;


/**
 * テクスチャー管理
 **/
class TextureTable
{
 public:

  /**
   * 初期化
   **/
  TextureTable();

  /**
   * @brief テクスチャーをバイトデータからロード
   *
   * @return 成功の場合は、新しいテクスチャーを象徴するID、失敗の場合は-1
   **/
  int loadTexture(byte* data, int length, bool repeat, bool mipmap, bool gpuOnly);

  /**
   * @brief Load a texture cube from byte data of 6 images
   *
   **/
  int loadTextureCube(byte* data[6], int length[6], bool mipmap);

  /**
   * @brief 新規テクスチャー作成
   **/
  int createTexture( int width, int height, int format, int pixelFormat, 
		     bool filterLinear, bool repeat, byte* data );

  /**
   * @brief Create a new cubemap texture
   **/
  int createTextureCube( int width, int height, int format, int pixelFormat, 
			 bool filterLinear, bool repeat, byte* data[6] );
  
  /**
   * 以前ロードされたテクスチャーを削除する
   **/
  bool deleteTexture(int textureID);

  /**
   * @return テクスチャ情報
   */
  TextureInfo getTextureInfo(int texture);

  /**
   * 指定のテクスチャーをバインドする
   *
   * @return true-成功 false-テクスチャが存在しない、又はシェーダにs2DTexture0のUniformが存在しない
   **/
  bool bind(int textureID);

  /**
   * 指定のテクスチャーを取得する
   * @return 指定のテクスチャーが存在する場合は、そのテクスチャ、存在しない場合はNULL
   **/
  Texture* getTexture(int textureID);

  /**
   * 指定テクスチャの指定位置にサブ画像をコピー
   * @return true-成功 false-テクスチャが存在しないか、コピーに失敗
   */
  bool copySubImage(int texture, int offsetX, int offsetY, int width, int height, int format, int pixelFormat, byte *subData);


 private:
  
  // テクスチャーの最大数
  static const int MAX_TEXTURES = 5000;

  // テクスチャーマップ
  Texture* textures[MAX_TEXTURES];
  int nextAvailable;
};
