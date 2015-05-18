/**
 * @file textureTable.cpp
 * @brief Module for management of texture resources implementation
 **/

#include <stdio.h>
#include <string.h>

#include "types.h"
#include GL2_H
#include GL2EXT_H
#include "glbase.h"
#include "macros.h"
#include "textureTable.h"
#include "texture.h"
#include "textureCompressed.h"
#include "textureCube.h"
#include "textureLoader.h"

TextureTable::TextureTable()
{
  nextAvailable = 0;

  memset(textures, 0, sizeof(textures));
}

int TextureTable::loadTexture( byte* data, int length, bool repeat, bool mipmap, bool gpuOnly ) {
  
  // Generate texture loader for specified data
  TextureLoader* loader = TextureLoader::createLoaderFor(data);
  if( loader == NULL ) {
    etrace("Unkown texture format");
    return -1;
  }

  // Read the texture
  Texture* newTexture = loader->load(data, length, repeat, mipmap);
  delete loader;
  
  if( newTexture == NULL ){
    etrace("Unkown texture format");
    return -1;
  }

  // Load on vbo
  newTexture->initRenderEnv();

  // Release image data
  if( gpuOnly ) {
    newTexture->unload();
  }

  // Register
  int res = nextAvailable;
  textures[res] = newTexture;

  while( textures[++nextAvailable] != NULL ){
    nextAvailable = (nextAvailable + 1) % MAX_TEXTURES;
  }

  return res;
}

int TextureTable::loadTextureCube(byte* data[6], int length[6], bool mipmap)
{
  Texture* texs[6];

  // Load 6 textures
  for( int i=0; i<6; i++ ){
    TextureLoader* loader = TextureLoader::createLoaderFor(data[i]);
    if( loader == NULL ) {
      etrace("Unkown texture format");
      return -1;
    }
    
    // Read the texture
    texs[i] = loader->load(data[i], length[i], false, mipmap);
    delete loader;
    
    if( texs[i] == NULL ){
      etrace("Unkown texture format");
      return -1;
    }
  }

  // Initialize cube texture
  TextureCube* newTexture = new TextureCube(texs);
  newTexture->initRenderEnv();

  // Delete textures
  for( int i=0; i<6; i++ ){
    delete texs[i];
  }

  // Register
  int res = nextAvailable;
  textures[res] = newTexture;
  
  while( textures[++nextAvailable] != NULL ){
    nextAvailable = (nextAvailable + 1) % MAX_TEXTURES;
  }
  
  return res;
}


bool TextureTable::deleteTexture(int textureID)
{
  delete textures[textureID];
  textures[textureID] = NULL;
  return true;
}

/**
 * @return テクスチャ情報
 */
TextureInfo TextureTable::getTextureInfo(int textureID) {
  Texture* texture = textures[ textureID ];

  if( texture == NULL ) {
    TextureInfo textureInfo;

	textureInfo.width = -1;
	textureInfo.height = -1;
	textureInfo.format = -1;
	textureInfo.pixelFormat = -1;
	return textureInfo;
  }

  return texture->getTextureInfo();
}

int getCompressedBPP( int format ) {
  switch( format ){
  case GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG:
  case GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG:
	  return 2;
  case GL_ETC1_RGB8_OES:
  case GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG:
  case GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG:
  case GL_ATC_RGB_AMD:
	  return 4;
  case GL_ATC_RGBA_EXPLICIT_ALPHA_AMD:
  case GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD:
	  return 8;
  default:
	  break;
  }

  return -1;
}

int TextureTable::createTexture( int width, int height, int format, int pixelFormat, bool filterLinear, bool repeat, byte* data ) {
  
  Texture* newTexture;
  
  // Switch format to choose normal or compressed texture
  switch( format ){
  case GL_ETC1_RGB8_OES:
  case GL_ATC_RGB_AMD:
  case GL_ATC_RGBA_EXPLICIT_ALPHA_AMD:
  case GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD:
  case GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG:
  case GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG:
  case GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG:
  case GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG:
    newTexture = new TextureCompressed( width, height, format, filterLinear, 
					repeat, data, false, getCompressedBPP( format ) );
    break;

  default:
    newTexture = new Texture( width, height, format, pixelFormat, 
			      filterLinear, repeat, data, false, -1 );
    break;
  }
  
  // Check if ok
  if( newTexture == NULL ) {
    delete newTexture;
    return -1;
  }

  // Load on vbo
  newTexture->initRenderEnv();
  newTexture->disownData();

  int res = nextAvailable;
  textures[res] = newTexture;
  nextAvailable++;

  while( textures[nextAvailable] != NULL ){
    nextAvailable = (nextAvailable + 1) % MAX_TEXTURES;
  }

  return res;  
}

int TextureTable::createTextureCube( int width, int height, int format, int pixelFormat, 
				     bool filterLinear, bool repeat, byte* data[6] )
{
  Texture* faces[6];

  for( int i=0; i<6; i++ ){
    // Switch format to choose normal or compressed texture
    switch( format ){
    case GL_ETC1_RGB8_OES:
    case GL_ATC_RGB_AMD:
    case GL_ATC_RGBA_EXPLICIT_ALPHA_AMD:
    case GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD:
    case GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG:
    case GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG:
    case GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG:
    case GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG:
      faces[i] = new TextureCompressed( width, height, format, filterLinear, 
					  repeat, data[i], false, -1);
      break;
      
    default:
      faces[i] = new Texture( width, height, format, pixelFormat, 
				filterLinear, repeat, data[i], false, -1);
      break;
    }
    
    // Check if ok
    if( faces[i] == NULL ) {
      delete faces[i];
      return -1;
    }
  }
  
  // Load cube
  TextureCube* newTexture = new TextureCube(faces);
  newTexture->initRenderEnv();

  // Delete textures
  for( int i=0; i<6; i++ ){
    delete faces[i];
  }

  // Register
  int res = nextAvailable;
  textures[res] = newTexture;
  nextAvailable++;
  
  while( textures[nextAvailable] != NULL ){
    nextAvailable = (nextAvailable + 1) % MAX_TEXTURES;
  }
  
  return res;
}


bool TextureTable::bind(int textureID)
{
  Texture* texture = textures[textureID];
  if( texture == NULL ) return false;
  
  return texture->bind();
}

/**
 * 指定のテクスチャーを取得する
 * @return 指定のテクスチャーが存在する場合は、そのテクスチャ、存在しない場合はNULL
 **/
Texture* TextureTable::getTexture(int textureID)
{
    return textures[textureID];
}

/**
 * 指定テクスチャの指定位置にサブ画像をコピー
 * @return true-成功 false-テクスチャが存在しないか、コピーに失敗
 */
bool TextureTable::copySubImage(int texture, int offsetX, int offsetY, int width, int height, int format, int pixelFormat, byte *subData) {
	Texture *tex = getTexture( texture );

	if( tex == NULL ) {
		etrace( "TextureTable::copySubImage - texture %d not found", texture );
		return false;
	}

	return tex->copySubImage( offsetX, offsetY, width, height, format, pixelFormat, subData );
}
