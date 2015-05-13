/**
 * @file surface.cpp
 * @brief Surface implementation
 **/

#include "surface.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"

#include <string.h>

Surface::Surface()
{
  name = NULL;
  memset(ambient, 0, 4*sizeof(float));
  memset(diffuse, 0, 4*sizeof(float));
  memset(specular, 0, 4*sizeof(float));
  memset(shininess, 0, 4*sizeof(float));
  memset(emission, 0, 4*sizeof(float));
  texture = -1;
}

Surface::~Surface()
{
  delete[] name;
}

bool Surface::load(const byte *data, int offset, int texturesOffset)
{
  //ヘッダ部分
  int nameLength = FileUtils::readByte( data, offset );//サーフェイス名文字列の長さ
  
  FileUtils::readFloats( data, offset, 4, ambient );//マテリアルカラー（環境色）
  FileUtils::readFloats( data, offset, 4, diffuse );//減衰色
  FileUtils::readFloats( data, offset, 4, specular );//反射色
  FileUtils::readFloats( data, offset, 4, shininess );//反射係数
  FileUtils::readFloats( data, offset, 4, emission );//発光係数

  trace("ambient: %f %f %f %f", ambient[0], ambient[1], ambient[2], ambient[3] );

  //未使用領域をスキップ
  offset += 4;

  //データ部分
  name = FileUtils::readString( data, offset, nameLength );//サーフェイス名

  // テクスチャー無視
  (void)texturesOffset;

  return true;
}

void Surface::loadFrom(Surface* surface)
{
  memcpy(ambient, surface->ambient, 4*sizeof(float));
  memcpy(diffuse, surface->diffuse, 4*sizeof(float));
  memcpy(specular, surface->specular, 4*sizeof(float));
  memcpy(shininess, surface->shininess, 4*sizeof(float));
  memcpy(emission, surface->emission, 4*sizeof(float));
  
  name = strdup2(surface->name);
}

bool Surface::loadForSpriteMergeGroup()
{
  name = strdup2("mergeGroupSurface");
  return true;
}

char* Surface::getName()
{
  return name;
}

void Surface::bind()
{
  int nUniDiffuse = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_MATERIAL_DIFFUSE );
  int nUniAmbientMaterial = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_MATERIAL_AMBIENT );
  int nUniSpecularMaterial = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_MATERIAL_SPECULAR );
  int nUniShininess = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_MATERIAL_SHININESS );
  int nUniEmissionMaterial = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_MATERIAL_GLOSSINESS );

  if( nUniDiffuse >= 0 ) 
    GLOP( glUniform3fv( nUniDiffuse, 1, diffuse ) );
  if( nUniAmbientMaterial >= 0) 
    GLOP( glUniform3fv( nUniAmbientMaterial, 1, ambient ) );
  if( nUniSpecularMaterial >= 0 )
    GLOP( glUniform3fv( nUniSpecularMaterial, 1, specular ) );
  if( nUniEmissionMaterial >= 0 ) 
    GLOP( glUniform3fv( nUniEmissionMaterial, 1, emission ) );
  if( nUniShininess >= 0 )
    GLOP( glUniform1f( nUniShininess, shininess[0] ) );

}

void Surface::setTexture( int texture )
{
  this->texture = texture;
}

int Surface::getTexture(){
  return texture;
}
