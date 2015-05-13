/**
 * @file shaderTable.h
 * @brief Shader table and management
 **/
#pragma once

#include "shaderProgram.h"
#include <map>
#include <string>

/**
 * Shader table and management
 **/
class ShaderTable
{
public:

  /**
   * 初期化
   **/
  ShaderTable();

  /**
   * シェダーをロードし、テーブルに追加する
   * 
   * @return 成功の場合、ロードされたプログラムの架空番号(!=GLid)
   * 失敗の場合は-1
   **/
  int loadShader(const char* vertexCode, const char* fragmentCode, const char* vShaderName, const char* fShaderName);

  /**
   * シェダーをロードし、テーブルに追加する
   * 
   * @return 成功の場合、ロードされたプログラムの架空番号(!=GLid)
   * 失敗の場合は-1
   **/
  int loadShader( const char* vertexCode, const char* fragmentCode, const char *programName );

  /**
   * 以前ロードされたシェダーを取得する
   *
   * @return 成功の場合はシェダープログラム、存在しない場合はNULL
   **/
  ShaderProgram* getShaderProgram(int shaderId);

  /**
   * @return シェーダー名に相当するシェーダーのリソースID (存在しなかった場合は-1)
   **/
  int getShaderProgramID(const char* shaderName);

  /**
   * 現在バインドされているプログラムに対して(Current Program)指定の
   * attributeのlocationを取得。 attributeが現在のプログラムに入っていなければ、
   * -1 が返されます。
   **/
  int getCPAttributeLocation( ShaderProgram::INDEX_ATTRIBUTES attribute );

  /**
   * 現在バインドされているプログラムに対して(Current Program)指定の
   * uniformのlocationを取得。 uniformが現在のプログラムに入っていなければ、
   * -1 が返されます。
   **/
  int getCPUniformLocation( ShaderProgram::INDEX_UNIFORMS uniform );

  /**
   * 現在バインドされているプログラムに対して(Current Program)指定の
   * myUniformのlocationを取得。 uniformが現在のプログラムに入っていなければ、
   * -1 が返されます。
   **/
  int getCPMyUniformLocation( int index );

  /**
   * 現在バインドされているプログラムに対して(Current Program)指定の
   * uniformのsizeを取得。 uniformが現在のプログラムに入っていなければ、
   * -1 が返されます。
   **/
  GLint getCPUniformSize( ShaderProgram::INDEX_UNIFORMS uniform );
  
private:
  // 最大プログラム数
  static const int MAX_PROGRAMS = 1024;
  
  // シェダープログラムテーブル
  ShaderProgram* shaderPrograms[MAX_PROGRAMS];
  // シェダープログラム数
  int numPrograms;

  // 名前→ IDマップ
  std::map<std::string, int> nameToId;
};
