/**
 * @file texture.cpp
 * @brief Texture module implementation
 **/

#include "glbase.h"
#include "texture.h"
#include "fileUtils.h"
#include "macros.h"

#include <stdlib.h>

Texture::Texture()
{
  glid = -1;
  width = -1;
  height = -1;
  format = -1;
  pixelFormat = -1;
  imageData = NULL;
  filterLinear = true;
  repeat = true;
  mipmap = false;
  bpp = -1;

  textureMode = GL_TEXTURE_2D;
}

Texture::Texture(int width, int height, int format, int pixelFormat, 
		 bool filterLinear, bool repeat, byte* data, bool mipmap, int bpp )
{
  glid = -1;
  textureMode = GL_TEXTURE_2D;

  this->width = width;
  this->height = height;
  this->format = format;
  this->pixelFormat = pixelFormat;
  this->imageData = data;
  this->filterLinear = filterLinear;
  this->repeat = repeat;
  this->mipmap = mipmap;
  this->bpp = bpp;
}

Texture::~Texture()
{
  unloadRenderEnv();
  unload();
}

void Texture::unload() {
  if( imageData ) {
    delete[] imageData;
    imageData = NULL;
  }
}

void Texture::disownData() {
  imageData = NULL;
}

void Texture::unloadRenderEnv()
{
  if( glid >= 0 ) {
    trace( "deleted texture with glid %d, width %d, height %d, format %X, pixelFormat %X, repeat %d", glid, width, height, format, pixelFormat, repeat );
    GLOP( glDeleteTextures( 1, &glid ) );
    glid = -1;
  }
}

bool Texture::bind()
{
  int nUniSampler = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_TEXTURE0);

  if( nUniSampler < 0 ){
    return false;
  }

  // テクスチャー0を有効
  GLOP( glActiveTexture( GL_TEXTURE0 ) );

  // テクスチャーをバインド
  GLOP( glBindTexture(GL_TEXTURE_2D, glid ) );

  // サンプラーをバインド
  GLOP( glUniform1i( nUniSampler, 0 ) );
  return true;
}

void Texture::initRenderEnv() {    
  // Create
  createVBO();

  // Load data
  loadDataAndMipMaps( textureMode );

  // Unbind
  GLOP( glBindTexture( textureMode, 0 ) );
}

void Texture::createVBO()
{
  GLBase::get()->getBinder()->resetTexture();
  GLOP( glGenTextures(1, &glid) );
  GLOP( glBindTexture(textureMode, glid) );

  if( filterLinear ) {
    GLOP( glTexParameterf( textureMode, GL_TEXTURE_MIN_FILTER, GL_LINEAR ) );
    GLOP( glTexParameterf( textureMode, GL_TEXTURE_MAG_FILTER, GL_LINEAR ) );
  } else {
    GLOP( glTexParameterf( textureMode, GL_TEXTURE_MIN_FILTER, GL_NEAREST ) );
    GLOP( glTexParameterf( textureMode, GL_TEXTURE_MAG_FILTER, GL_NEAREST ) );
  }

  if( repeat ) {
    GLOP( glTexParameterf( textureMode, GL_TEXTURE_WRAP_S, GL_REPEAT ) );
	GLOP( glTexParameterf( textureMode, GL_TEXTURE_WRAP_T, GL_REPEAT ) );
  } else {
    GLOP( glTexParameterf( textureMode, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE ) );
    GLOP( glTexParameterf( textureMode, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE ) );
  }
}

void Texture::loadDataAndMipMaps( GLenum textureMode )
{
  GLOP( glTexImage2D( textureMode, 0, format, width, height, 
		      0, format, pixelFormat, imageData ) );
  trace( "loaded texture with glid %d, width %d, height %d, format %X, pixelFormat %X, repeat %d", glid, width, height, format, pixelFormat, repeat );
  
  // ミップマップあり
  if( mipmap ) {
    // PKMの場合は、データバッファーに全てのミップマップが格納されている仮定
    int size = width;
    int level = 0;
    byte* readPos = imageData + ((width*width*bpp) >> 3);
    int levelSize = (size*size*bpp) >> 3;
    
    
    while( size > 1 ){
      level++;
      size /= 2;
      readPos += levelSize;
      levelSize = (size*size*bpp) >> 3;
      
      GLOP( glTexImage2D( textureMode, level, format, width, height, 
			  0, format, pixelFormat, readPos ) );
    }
  }
}

/**
 * @return テクスチャーの幅
 **/
int Texture::getWidth()
{
    return width;
}

/**
 * @return テクスチャーの高さ
 **/
int Texture::getHeight()
{
    return height;
}

/**
 * @return フォーマット(GL_RGBA等)
 **/
int Texture::getFormat() {
  return format;
}
/**
 * @return ピクセルフォーマット(GL_UNSIGNED_SHORT_4_4_4_4等)
 **/
int Texture::getPixelFormat() {
  return pixelFormat;
}

bool Texture::getFilterLinear()
{
  return filterLinear;
}

bool Texture::getRepeat()
{
  return repeat;
}

bool Texture::getMipMap()
{
  return mipmap;
}

int Texture::getBpp()
{
  return bpp;
}

GLenum Texture::getTextureMode()
{
  return textureMode;
}

/**
 * @return テクスチャ情報
 */
TextureInfo Texture::getTextureInfo() {
  TextureInfo textureInfo;

  textureInfo.width = getWidth();
  textureInfo.height = getHeight();
  textureInfo.format = getFormat();
  textureInfo.pixelFormat = getPixelFormat();
  textureInfo.imageData = imageData;
  return textureInfo;
}

/**
 * 指定テクスチャの指定位置にサブ画像をコピー
 * @return true
 */
bool Texture::copySubImage(int offsetX, int offsetY, int width, int height, int format, int pixelFormat, byte *subData) {
  GLBase::get()->getBinder()->resetTexture();
  GLOP( glBindTexture( GL_TEXTURE_2D, glid ) );
  GLOP( glTexSubImage2D( GL_TEXTURE_2D, 0, offsetX, offsetY, width, height, format, pixelFormat, subData ) );
  GLOP( glBindTexture( GL_TEXTURE_2D, 0 ) );
  return true;
}
