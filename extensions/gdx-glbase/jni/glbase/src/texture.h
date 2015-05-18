/**
 * @file texture.h
 * @brief Texture module
 **/
#pragma once

#include "types.h"
#include GL2_H

class Framebuffer;

/**
 * GLES2.0テクスチャーを象徴する
 **/
class Texture
{
  friend class Framebuffer;
  friend class RenderQueue;
  
 public:
  
  /**
   * 初期化
   **/
  Texture(int width, int height, int format, int pixelFormat, bool filterlinear,
	  bool repeat, byte* data, bool mipmap, int bpp );

  /**
   * リソース開放
   **/
  virtual ~Texture();

  /**
   * レンダリング準備を行う
   **/
  virtual void initRenderEnv();

  /**
   * Create VBO and set filters
   **/
  virtual void createVBO();

  /**
   * Do glTexImage2D for image and all mipmaps
   **/
  virtual void loadDataAndMipMaps( GLenum textureMode );
  
  /**
   * テクスチャーをバインドする
   **/
  virtual bool bind();
   
  /**
   * @return テクスチャーの幅
   **/
  int getWidth();
    
  /**
   * @return テクスチャーの高さ
   **/
  int getHeight();

  /**
   * @return フォーマット(GL_RGBA等)
   **/
  int getFormat();

  /**
   * @return ピクセルフォーマット(GL_UNSIGNED_SHORT_4_4_4_4等)
   **/
  int getPixelFormat();

  /**
   * @return Whether filter is linear
   **/
  bool getFilterLinear();

  /**
   * @retun Whether is repeat
   **/
  bool getRepeat();

  /**
   * @return Whether mipmap is on
   **/
  bool getMipMap();

  /**
   * @return The bpp
   **/
  int getBpp();

  /**
   * @return Texture mode such as GL_TEXTURE_2D
   **/
  GLenum getTextureMode();

  /**
   * @return テクスチャ情報
   */
  TextureInfo getTextureInfo();
  
  /**
   * バイトデータを解放
   */
  void unload();

  /**
   * バイトデータの管理責任を外す
   */
  void disownData();

  /**
   * 指定テクスチャの指定位置にサブ画像をコピー
   * @return true
   */
  virtual bool copySubImage(int offsetX, int offsetY, int width, int height, 
			    int format, int pixelFormat, byte *subData);

 protected:

  /**
   * For children implementors
   **/
  Texture();

  // GL用id
  GLuint glid;  

  // テクスチャーサイズ
  GLsizei width;
  GLsizei height;
  GLenum format;
  GLenum pixelFormat;
  byte *imageData;
  bool filterLinear;
  bool repeat;
  bool mipmap;
  int bpp;
  GLenum textureMode;
  
private:

  /**
   * レンダリング準備を解除する
   **/
  void unloadRenderEnv();

};
