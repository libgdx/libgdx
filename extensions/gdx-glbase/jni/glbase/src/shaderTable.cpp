/**
 * @file shaderTable.cpp
 * @brief Shader table implementation
 **/

#include "shaderTable.h"
#include "shaderProgram.h"
#include "glbase.h"
#include "macros.h"

using namespace std;


ShaderTable::ShaderTable() {
  numPrograms = 0;

  for( int i = 0; i < MAX_PROGRAMS; i++ ) {
    shaderPrograms[ i ] = NULL;
  }
}

int ShaderTable::loadShader( const char* vertexCode, const char* fragmentCode, const char* vShaderName, const char* fShaderName ) {
	string programName = string( vShaderName ).append( "_" ).append( fShaderName );

	return loadShader( vertexCode, fragmentCode, (char *)programName.c_str() );
}

int ShaderTable::loadShader( const char* vertexCode, const char* fragmentCode, const char *programName ) {
	// バウンドチェック
	if( numPrograms >= MAX_PROGRAMS - 1 ) {
		return -1;
	}

	// Load the shader to GL ES
	ShaderProgram* newProgram = new ShaderProgram();

	if( !newProgram->loadFromSource( vertexCode, fragmentCode ) ) {
		// エラー
		delete newProgram;
		return -1;
	}

	// 成功、テーブルに追加
	shaderPrograms[ numPrograms++ ] = newProgram;

	// Name → ID マップ
	nameToId[ programName ] = numPrograms - 1;
	return numPrograms - 1;
}

ShaderProgram *ShaderTable::getShaderProgram( int shaderId ) {
	if( (shaderId < 0) || (shaderId >= numPrograms) ) {
		etrace( "shaderId out of bounds: %d", shaderId );
		return NULL;
	}

	return shaderPrograms[ shaderId ];
}

int ShaderTable::getShaderProgramID( const char* shaderName ) {
	if( nameToId.count( shaderName ) == 0 ){
		return -1;
	}

	return nameToId[ shaderName ];
}

int ShaderTable::getCPAttributeLocation( ShaderProgram::INDEX_ATTRIBUTES attribute )
{
  int bindedProgram = GLBase::get()->getBinder()->getCurrentProgram();

  // プログラムチェック
  if( bindedProgram == -1 ) {
    etrace("Call to getCPAttributeLocation with no binded shader program. Did you(glbase developer) call ShaderTabke::bind()?");
    return -1;
  }

  // パラメターバウンドチェック
  if( (attribute < 0) || (attribute >= ShaderProgram::IATTRIBUTE_MAX) ) {
    etrace("getCPAttributeLocation: Attribute index out of bounds: %d", attribute);
    return -1;
  }

  return shaderPrograms[bindedProgram]->getAttributeLocation(attribute);
}

int ShaderTable::getCPUniformLocation( ShaderProgram::INDEX_UNIFORMS uniform )
{
  int bindedProgram = GLBase::get()->getBinder()->getCurrentProgram();

  // プログラムチェック
  if( bindedProgram == -1 ) {
    etrace("Call to getCPUniformLocation with no binded shader program. Did you(glbase developer) call ShaderTable::bind()?");
    return -1;
  }

  // パラメターバウンドチェック
  if( (uniform < 0) || (uniform >= ShaderProgram::IUNIFORM_MAX) ) {
    etrace("getCPUniformLocation: Uniform index out of bounds: %d", uniform);
    return -1;
  }

  return shaderPrograms[bindedProgram]->getUniformLocation(uniform);
}

int ShaderTable::getCPMyUniformLocation( int index )
{
  int bindedProgram = GLBase::get()->getBinder()->getCurrentProgram();

  // プログラムチェック
  if( bindedProgram == -1 ) {
    etrace("Call to getCPMyUniformLocation with no binded shader program. Did you(glbase developer) call ShaderTable::bind()?");
    return -1;
  }

  // パラメターバウンドチェック
  if( index < 0 ) {
    etrace("getCPMyUniformLocation: Uniform index out of bounds: %d", index);
    return -1;
  }

  return shaderPrograms[bindedProgram]->getMyUniformLocation(index);
}

GLint ShaderTable::getCPUniformSize( ShaderProgram::INDEX_UNIFORMS uniform )
{
  int bindedProgram = GLBase::get()->getBinder()->getCurrentProgram();

  // プログラムチェック
  if( bindedProgram == -1 ) {
    etrace("Call to getCPAttributeSize with no binded shader program. Did you(glbase developer) call ShaderTable::bind()?");
    return -1;
  }

  // パラメターバウンドチェック
  if( (uniform < 0) || (uniform >= ShaderProgram::IUNIFORM_MAX) ) {
    etrace("getCPUniformSize: Uniform index out of bounds: %d", uniform);
    return -1;
  }

  return shaderPrograms[bindedProgram]->getUniformSize(uniform);
}
