/**
 * @file textureLoader.cpp
 * @brief TextureLoader module implementation
 **/

#include "glbase.h"
#include "texture.h"
#include "textureLoader.h"
#include "textureLoaderPKM.h"
#include "textureLoaderPKMBMP.h"
#include "textureLoaderATI.h"
#include "textureLoaderCTES.h"
#include "textureLoaderPVR.h"
#include "textureLoaderFI.h"

TextureLoader* TextureLoader::createLoaderFor( byte* data )
{
  // PKMテクスチャーの場合はPKMでロードを試す
  if( isPkm(data) ){
	if( GLBase::get()->hasETC1() ){
	  return new TextureLoaderPKM();
	} else {
	  return new TextureLoaderPKMBMP();
	}
  }

  else if( isATI(data) ){
    return new TextureLoaderATI();
  }

  else if( isCTES(data) ){
    return new TextureLoaderCTES();
  }

  else if( isPVR(data) ){
    return new TextureLoaderPVR();
  }

  // 圧縮テクスチャーじゃない場合、FreeImageで画像ロードを試す
  else{
    return new TextureLoaderFI();
  }
}

bool TextureLoader::isPkm(byte* data)
{
  return data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x4D && data[3] == 0x20;
}

bool TextureLoader::isATI(byte* data)
{
  return data[0] == 0x44 && data[1] == 0x44 && data[2] == 0x53 && data[3] == 0x20;
}

bool TextureLoader::isCTES(byte* data)
{
  return data[0] == 0x02 && data[1] == 0x00 && data[2] == 0xC4 && data[3] == 0xCC;
}

bool TextureLoader::isPVR(byte* data)
{
  return data[0] == 0x50 && data[1] == 0x56 && data[2] == 0x52 && data[3] == 0x3;
}
