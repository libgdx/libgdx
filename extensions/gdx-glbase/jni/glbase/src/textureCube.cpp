/**
 * @file textureCube.cpp
 * @brief Cube texture module implementation
 **/
#include "textureCube.h"
#include "glbase.h"
#include "macros.h"

TextureCube::TextureCube(Texture* faces[6])
  : Texture( faces[0]->getWidth(), faces[0]->getHeight(), faces[0]->getFormat(), 
	     faces[0]->getPixelFormat(), faces[0]->getFilterLinear(), faces[0]->getRepeat(), 
	     NULL, faces[0]->getMipMap(), faces[0]->getBpp() )
{
  // Set faces
  for( int i=0; i<6; i++ ){
    this->faces[i] = faces[i];
  }

  // Change texture mode
  textureMode = GL_TEXTURE_CUBE_MAP;
}

TextureCube::~TextureCube()
{
  
}

bool TextureCube::bind()
{
  // テクスチャーをバインド
  GLOP( glBindTexture(GL_TEXTURE_CUBE_MAP, glid ) );
  return true;
}

void TextureCube::initRenderEnv() {  
  // Create VBO
  createVBO();

  // Load data
  GLuint textureModes[6] = {
    GL_TEXTURE_CUBE_MAP_POSITIVE_X,
    GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
    GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
    GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
    GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
    GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
  };

  for( int i=0; i<6; i++ ){
    faces[i]->loadDataAndMipMaps(textureModes[i]);
  }
  
  // Unbind
  GLOP( glBindTexture( GL_TEXTURE_CUBE_MAP, 0 ) );
}


  
bool TextureCube::copySubImage(int offsetX, int offsetY, int width, int height, 
				     int format, int pixelFormat, byte *subData)
{
  // TODO
}
