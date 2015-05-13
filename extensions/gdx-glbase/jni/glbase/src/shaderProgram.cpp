/**
 * @file shaderProgram.cpp
 * @brief Shader program implementation
 **/

using namespace std;

#include <string.h>
#include <string>
#include "shaderProgram.h"
#include "glbase.h"
#include "macros.h"

char attributeNames[ ShaderProgram::IATTRIBUTE_MAX ][ 64 ] = {
  NAME_ATTRIBUTE_VERTEX,
  NAME_ATTRIBUTE_TEXTURE_COORD0,
  NAME_ATTRIBUTE_COLOR,
  NAME_ATTRIBUTE_NORMAL,
  NAME_ATTRIBUTE_MATRIX_INDICES,
  NAME_ATTRIBUTE_WEIGHTS,
};

char uniformNames[ ShaderProgram::IUNIFORM_MAX ][ 64 ] = {
  NAME_UNIFORM_MODEL_VIEW_PROJECTION_MATRIX,
  NAME_UNIFORM_NORMAL_MATRIX,
  NAME_UNIFORM_VIEW_ROTATION_MATRIX,
  NAME_UNIFORM_MODEL_VIEW_MATRIX,
  NAME_UNIFORM_MODEL_VIEW_MATRIX_3VEC4,
  NAME_UNIFORM_PROJECTION_MATRIX,
  NAME_UNIFORM_INVERSE_MODEL_VIEW_MATRIX,
  NAME_UNIFORM_MODEL_MATRIX,
  NAME_UNIFORM_MODEL_LIGHT_PROJECTION_MATRIX,
  NAME_UNIFORM_MODEL_LIGHT_MATRIX,
  NAME_UNIFORM_LIGHT_PROJECTION_MATRIX,
  NAME_UNIFORM_SHADOW_MAP_MATRIX,
  NAME_UNIFORM_SPRITE_MATRIX,
    
  NAME_UNIFORM_MATRIX_PALETTE,
  NAME_UNIFORM_ROOT_MATRIX_PALETTE,
  NAME_UNIFORM_BMV_MATRIX_PALETTE,
  NAME_UNIFORM_BMVP_MATRIX_PALETTE,
  NAME_UNIFORM_BMLP_MATRIX_PALETTE,

  NAME_UNIFORM_TEXTURE0,

  NAME_UNIFORM_TIME_SECONDS,
  NAME_UNIFORM_VIEWPORT_RECT,
  NAME_UNIFORM_VIEWPORT_RECIPROCAL,
  NAME_UNIFORM_SCREEN2DISPLAY,
  NAME_UNIFORM_DISPLAY2SCREEN,

  NAME_UNIFORM_MATERIAL_DIFFUSE,
  NAME_UNIFORM_MATERIAL_AMBIENT,
  NAME_UNIFORM_MATERIAL_SPECULAR,
  NAME_UNIFORM_MATERIAL_EMISSIVE,
  NAME_UNIFORM_MATERIAL_SHININESS,
  NAME_UNIFORM_MATERIAL_GLOSSINESS,

  NAME_UNIFORM_LIGHT_POSITION,
  NAME_UNIFORM_LIGHT_DIFFUSE,
  NAME_UNIFORM_LIGHT_AMBIENT,
  NAME_UNIFORM_LIGHT_STRENGTH,
  
  NAME_UNIFORM_ADJACENT_UV_OFFSET_H,
  NAME_UNIFORM_ADJACENT_UV_OFFSET_V,
  NAME_UNIFORM_SHADOW_MAP_INFO,
  NAME_UNIFORM_SHADOW_DARKNESS,

  NAME_UNIFORM_FOG_COLOR,
  NAME_UNIFORM_FOG_RANGE,

  NAME_UNIFORM_DEPTH_RANGE,
};

void tagShaderMember::Reset() { 
  nLocation = -1;
  strName = NULL;
  nSize = -1;
  eType = 0;
}

tagShaderMember::tagShaderMember() { 
  Reset();
}

void tagShaderMember::SetAttribute( GLuint programID, const char *defStr ) {
  
  if( strName == NULL ) {
    strName = defStr;
  }
  
  GLOP( nLocation = glGetAttribLocation( programID, strName ) );
}

void tagShaderMember::SetUniform( GLuint programID, const char *defStr ) {
  if( strName == NULL ) { 
    strName = defStr;
  }
      
  GLOP( nLocation = glGetUniformLocation( programID, strName ) );
  
  if(
     (nLocation < 0) &&
     (strName[ strlen(strName) - 3 ] == '[') &&
     (strName[ strlen(strName) - 2 ] == '0') &&
     (strName[ strlen(strName) - 1 ] == ']')
     ) {
    
      char name[ 65 ];
      
      name[0] = '\0';
      strncpy( name, strName, strlen(strName) - 3 );
      GLOP( nLocation = glGetUniformLocation( programID, name ) );
  }
}

ShaderProgram::ShaderProgram()
{
}

bool ShaderProgram::loadFromSource(char const* vertexCode, char const* fragmentCode)
{
  // 頂点シェダーをコンパイル
  vshaderGlId = glCreateShader(GL_VERTEX_SHADER);
  if( vshaderGlId == 0 ) {
    etrace("Cannot create vertex shader");
    return false;
  }

  GLOP( glShaderSource(vshaderGlId, 1, &vertexCode, NULL) );
  GLOP( glCompileShader(vshaderGlId) );

  // Check the compile status
  if( !checkCompileStatus(vshaderGlId) ){
    etrace("Error in vertex shader");

    const int logSize = 1024 * 16;
    char log[ logSize ];
    GLint logLen;

    memset( log, 0, sizeof( char ) * logSize );
    GLOP( glGetShaderiv( vshaderGlId, GL_INFO_LOG_LENGTH, &logLen ) );
    GLOP( glGetShaderInfoLog( vshaderGlId, logLen, &logLen, log ) );

    GLBase::get()->doetrace( "------------------------------" );
    GLBase::get()->doetrace( "%s", log );
    GLBase::get()->doetrace( "------------------------------" );
    GLOP( glDeleteShader(vshaderGlId) );
    return false;
  }

  
  // フラグメントシェダーをコンパイル
  GLOP( fshaderGlId = glCreateShader(GL_FRAGMENT_SHADER) );
  if( fshaderGlId == 0 ) {
    etrace("Cannot create fragment shader");
    return false;
  }

  GLOP( glShaderSource(fshaderGlId, 1, &fragmentCode, NULL) );
  GLOP( glCompileShader(fshaderGlId) );

  // Check the compile status
  if( !checkCompileStatus(fshaderGlId) ){
    etrace("Error in fragment shader");

    const int logSize = 1024 * 16;
    char log[ logSize ];
    GLint logLen;

    memset( log, 0, sizeof( char ) * logSize );
    GLOP( glGetShaderiv( fshaderGlId, GL_INFO_LOG_LENGTH, &logLen ) );
    GLOP( glGetShaderInfoLog( fshaderGlId, logLen, &logLen, log ) );

    GLBase::get()->doetrace( "------------------------------" );
    GLBase::get()->doetrace( "%s", log );
    GLBase::get()->doetrace( "------------------------------" );
    GLOP( glDeleteShader(vshaderGlId) );
    GLOP( glDeleteShader(fshaderGlId) );
    return false;
  }


  // 頂点とフラグメントシェダーをLinkさせる
  GLOP( programGlId = glCreateProgram() );

  if( programGlId == 0 ) {
    etrace("Cannot create shader program");
    GLOP( glDeleteShader(vshaderGlId) );
    GLOP( glDeleteShader(fshaderGlId) );
    return false;
  }

  // Attribute名をバインドしておく！
  bindAttributeNames( programGlId );

  GLOP( glAttachShader(programGlId, vshaderGlId) );
  GLOP( glAttachShader(programGlId, fshaderGlId) );
  GLOP( glLinkProgram(programGlId) );
  
  // Check the link status
  if( !checkLinkStatus() ){
    GLOP( glDeleteShader(vshaderGlId) );
    GLOP( glDeleteShader(fshaderGlId) );
    GLOP( glDeleteProgram(programGlId) );
    etrace("Error linking shader");
    return false;
  }

  // AttributesとUniform情報を回収する
  for( int i = 0; i < IATTRIBUTE_MAX; i++ ) {
    attributes[ i ].SetAttribute( programGlId, attributeNames[ i ] );
  }
  
  for( int i = 0; i < IUNIFORM_MAX; i++ ) {
    uniforms[ i ].SetUniform( programGlId, uniformNames[ i ] );
  }

  //サイズ、型を取得.
  if( !updateMemberSizeAndType() ) {
    etrace("Error int updating member sizes and types");
    return false;
  }

  trace( "shader program loaded with glid %d", programGlId );
  return true;
}

bool ShaderProgram::updateMemberSizeAndType()
{
  const int bufSize = 65;
  GLint num = 0;
  GLint maxLength = 0;
  GLint size = -1;
  GLenum type = 0;
  GLsizei length = 0;
  char name[ bufSize ];
  bool bFound = false;

  //Attribute.
  GLOP( glGetProgramiv( programGlId, GL_ACTIVE_ATTRIBUTES, &num ) );
  GLOP( glGetProgramiv( programGlId, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, &maxLength ) );

  //数と名前のMAXの長さをエラーチェック.
  if(
     (num > 0) &&
     (maxLength > 0) &&
     (maxLength <= bufSize)
     ) {

    //シェーダ内の全Attributeのサイズと型を取得.
    for ( GLint i = 0; i < num; i++ ) {

      name[0] = '\0';
      GLOP( glGetActiveAttrib( programGlId, i, maxLength, &length, &size, &type, name ) );
      
      //サイズと型を保持.
      bFound = false;
      for( int j = 0; (j < IATTRIBUTE_MAX) && (bFound == false); j++ ) {

	if( strncmp( name, attributes[ j ].strName, bufSize - 1 ) == 0 ) {

	  bFound = true;
	  attributes[ j ].nSize = size;
	  attributes[ j ].eType = type;
	}
      }
    }
  }

  //Uniform.
  GLOP( glGetProgramiv( programGlId, GL_ACTIVE_UNIFORMS, &num ) );
  GLOP( glGetProgramiv( programGlId, GL_ACTIVE_UNIFORM_MAX_LENGTH, &maxLength ) );

  //数と名前のMAXの長さをエラーチェック.
  if(
     (num > 0) &&
     (maxLength > 0) &&
     (maxLength <= bufSize)
     ) {

    //シェーダ内の全Uniformのサイズと型を取得.
    for ( GLint i = 0; i < num; i++ ) {

      name[0] = '\0';
      GLOP( glGetActiveUniform( programGlId, i, maxLength, &length, &size, &type, name ) );
      
      //サイズと型を保持.
      bFound = false;
      for( int j = 0; (j < IUNIFORM_MAX) && (bFound == false); j++ ) {

	if( strncmp( name, uniforms[ j ].strName, bufSize - 1 ) == 0 ) {

	  bFound = true;
	  uniforms[ j ].nSize = size;
	  uniforms[ j ].eType = type;
	}
	else {

	  const char* uniName = uniforms[ j ].strName;

	  if(
	     (uniName[ strlen(uniName) - 3 ] == '[') &&
	     (uniName[ strlen(uniName) - 2 ] == '0') &&
	     (uniName[ strlen(uniName) - 1 ] == ']')
	     ) {

	    if( strncmp( name, uniName, strlen(uniName) - 3 ) == 0 ) {

	      bFound = true;
	      uniforms[ j ].nSize = size;
	      uniforms[ j ].eType = type;
	    }
	  }
	}
      }
    }
  }
  
  return true;
}

void ShaderProgram::bindAttributeNames(int programGlId)
{
  GLOP( glBindAttribLocation( programGlId, IATTRIBUTE_VERTEX + 1, NAME_ATTRIBUTE_VERTEX ) );
  GLOP( glBindAttribLocation( programGlId, IATTRIBUTE_TEXTURE_COORD0 + 1, NAME_ATTRIBUTE_TEXTURE_COORD0 ) );
  GLOP( glBindAttribLocation( programGlId, IATTRIBUTE_COLOR + 1, NAME_ATTRIBUTE_COLOR ) );
  GLOP( glBindAttribLocation( programGlId, IATTRIBUTE_NORMAL + 1, NAME_ATTRIBUTE_NORMAL ) );
  GLOP( glBindAttribLocation( programGlId, IATTRIBUTE_MATRIX_INDICES + 1, NAME_ATTRIBUTE_MATRIX_INDICES ) );
  GLOP( glBindAttribLocation( programGlId, IATTRIBUTE_WEIGHTS + 1, NAME_ATTRIBUTE_WEIGHTS ) );
}

bool ShaderProgram::checkCompileStatus(int shaderGlId)
{
  GLint compiled;
  GLOP( glGetShaderiv(shaderGlId, GL_COMPILE_STATUS, &compiled) );
  
  if( !compiled ){
    GLint infoLen = 0;
    GLOP( glGetShaderiv(shaderGlId, GL_INFO_LOG_LENGTH, &infoLen) );

    if(infoLen > 1)
      {
        char* infoLog = new char[infoLen];
	
        GLOP( glGetShaderInfoLog(shaderGlId, infoLen, NULL, infoLog) );
        etrace("Error compiling shader:\n%s\n", infoLog);
	
        delete[] infoLog;
      }
    return false;
  }

  return true;
}

bool ShaderProgram::checkLinkStatus()
{
  GLint linked;
  GLOP( glGetProgramiv(programGlId, GL_LINK_STATUS, &linked) );

  if(!linked)
    {
      GLint infoLen = 0;
      GLOP( glGetProgramiv(programGlId, GL_INFO_LOG_LENGTH, &infoLen) );

      if(infoLen > 1)
	{
	  char* infoLog = new char[infoLen];

	  GLOP( glGetProgramInfoLog(programGlId, infoLen, NULL, infoLog) );
	  etrace("Error linking program:\n%s\n", infoLog);

	  delete[] infoLog;
	}
      
      return false;
    }

  return true;
}

int ShaderProgram::getAttributeLocation( int attributeIndex )
{
  return attributes[ attributeIndex ].nLocation;
}

int ShaderProgram::getUniformLocation( int uniformIndex )
{
  return uniforms[ uniformIndex ].nLocation;
}

GLint ShaderProgram::getUniformSize( int uniformIndex )
{
  return uniforms[ uniformIndex ].nSize;
}

bool ShaderProgram::setMyUniform( int id, const char* uniformName )
{ 
  myUniforms[ id ].strName = uniformName;
  myUniforms[ id ].SetUniform( programGlId, "" );
  
  myUniformsNames[ uniformName ] = id;
  return true;
}

int ShaderProgram::getMyUniformID( const char* uniformName )
{
  if( myUniformsNames.count(uniformName) == 0 ) return -1;
  return myUniformsNames[uniformName];
}

bool ShaderProgram::hasMyUniform( int id )
{
  return (myUniforms.count(id) > 0);
}

int ShaderProgram::getAvailableMyUniformID()
{
  for( int i=0; true; i++ ){
    if( myUniforms.count(i) == 0 ) return i;
  }
}

GLint ShaderProgram::getMyUniformLocation( int idx ) {
  
  if( myUniforms.count(idx) == 0 ) {
    
    etrace( "uniform id not found: %d", idx );
    return -1;
  }
  
  return myUniforms[ idx ].nLocation;
}

void ShaderProgram::bind()
{
  GLOP( glUseProgram( programGlId ) );
}
