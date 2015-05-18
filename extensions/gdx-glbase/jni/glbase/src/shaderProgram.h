/**
 * @file shaderProgram.h
 * @brief A GLSL shader program
 **/
#pragma once

#include <map>
#include <string>

#include "types.h"
#include GL2_H
#include "shaderNames.h"

/*!
 * @briefシェーダのメンバ変数.
 */
class tagShaderMember {
public:  
  GLint nLocation;  //シェーダ内の位置.
  const char* strName;    //メンバの名前.
  GLint nSize;      //メンバのサイズ（nSize×eTypeのサイズ＝全体のサイズ）.
  GLenum eType;     //メンバの型.
  
  /*!
   * @brief初期化.
   */
  void Reset();
  
  /*!
   * @briefコンストラクタ.
   */
  tagShaderMember();
  
  /*!
   * @briefAttributeの名前とロケーションの設定関数.
   *
   * @param programIDシェーダプログラムのID.
   * @param bLoadedロード済みフラグ.
   * @param defStrデフォルトのAttribute名.
   */
  void SetAttribute( GLuint programID, const char *defStr );
  
  /*!
   * @briefUniformの名前とロケーションの設定関数.
   *
   * @param programIDシェーダプログラムのID.
   * @param bLoadedロード済みフラグ.
   * @param defStrデフォルトのAttribute名.
   */
  void SetUniform( GLuint programID, const char *defStr );
};


/**
 * シェーダープログラム
 **/
class ShaderProgram
{
public:

  /*!
   * @briefAttributeメンバインデックス.
   */
  typedef enum {
  
    IATTRIBUTE_VERTEX = 0,//!< 頂点座標Attribute.
    IATTRIBUTE_TEXTURE_COORD0,//!< UVAttribute.
    IATTRIBUTE_COLOR,//!< 頂点カラーAttribute.
    IATTRIBUTE_NORMAL,//!< 頂点法線Attribute.
    IATTRIBUTE_MATRIX_INDICES,//!< 行列インデックスAttribute.
    IATTRIBUTE_WEIGHTS,//!< ウエイトAttribute.

    IATTRIBUTE_MAX,
  } INDEX_ATTRIBUTES;

  /*!
   * @briefUniformメンバインデックス.
   */
  typedef enum {
  
    //行列.
    IUNIFORM_MODEL_VIEW_PROJECTION_MATRIX = 0,//!< ModelViewProjection行列Uniform.
    IUNIFORM_NORMAL_MATRIX,//!< Normal行列Uniform(3X3のModelView).
    IUNIFORM_VIEW_ROTATION_MATRIX,//!< ビュー回転行列.
    IUNIFORM_MODEL_VIEW_MATRIX,//!< ModelView行列Uniform.
    IUNIFORM_MODEL_VIEW_MATRIX_3VEC4,//!< ModelView行列Uniform(行順で3行X4列).
    IUNIFORM_PROJECTION_MATRIX,//!< Projection行列Uniform.
    IUNIFORM_INVERSE_MODEL_VIEW_MATRIX,//!< 逆ModelView行列Uniform.
    IUNIFORM_MODEL_MATRIX,//!< Model行列Uniform.
    IUNIFORM_MODEL_LIGHT_PROJECTION_MATRIX,//!< Projection(Light)×Light0×Model行列Uniform.
    IUNIFORM_MODEL_LIGHT_MATRIX,//!< Light0×Model行列Uniform.
    IUNIFORM_LIGHT_PROJECTION_MATRIX,//!< LightProjection行列Uniform.
    IUNIFORM_SHADOW_MAP_MATRIX,//!< Texture×Projection(Light)×Light0×InverseView行列Uniform.
    IUNIFORM_SPRITE_MATRIX,//!< Sprite行列１
      
    //その他.
    IUNIFORM_MATRIX_PALETTE,//!< 行列パレットUniform(行順で3行X4列).
    IUNIFORM_ROOT_MATRIX_PALETTE,//!< ルート行列パレットUniform(行順で3行X4列).
    IUNIFORM_BMV_MATRIX_PALETTE,//!< Projection×View×Model×ボーン行列パレットUniform(行順で3行X4列).
    IUNIFORM_BMVP_MATRIX_PALETTE,//!< Projection×View×Model×ボーン行列パレットUniform.
    IUNIFORM_BMLP_MATRIX_PALETTE,//!< Projection×Light0×Model×ボーン行列パレットUniform.

    IUNIFORM_TEXTURE0,//!< GL_TEXTURE0Uniform.

    IUNIFORM_TIME_SECONDS,//!< 現在の時間（秒）.
    IUNIFORM_VIEWPORT_RECT,//!< 現在のビューポート.
    IUNIFORM_VIEWPORT_RECIPROCAL,//!< 現在のビューポートサイズの逆数.
    IUNIFORM_SCREEN2DISPLAY,//!< ディスプレイサイズ：描画先サイズ.
    IUNIFORM_DISPLAY2SCREEN,//!< 描画先サイズ：ディスプレイサイズ.
  
    IUNIFORM_MATERIAL_DIFFUSE,//!< マテリアルの拡散色.
    IUNIFORM_MATERIAL_AMBIENT,//!< マテリアルの環境色.
    IUNIFORM_MATERIAL_SPECULAR,//!< マテリアルの鏡面反射色.
    IUNIFORM_MATERIAL_EMISSIVE,//!< マテリアルの発光色.
    IUNIFORM_MATERIAL_SHININESS,//!< マテリアルの鏡面反射の強度.
    IUNIFORM_MATERIAL_GLOSSINESS,//!< マテリアルの鏡面反射の広さ.
  
    IUNIFORM_LIGHT_POSITION,//!< 各光源の位置(複数).
    IUNIFORM_LIGHT_DIFFUSE,//!< 各光源の拡散光.
    IUNIFORM_LIGHT_AMBIENT,//!< 各光源の環境光.
    IUNIFORM_LIGHT_STRENGTH,//!< 各光源の強さ.
  
    IUNIFORM_ADJACENT_UV_OFFSET_H,//!< 隣接する4ピクセルのUVオフセット値（横）.
    IUNIFORM_ADJACENT_UV_OFFSET_V,//!< 隣接する4ピクセルのUVオフセット値（縦）.
    IUNIFORM_SHADOW_MAP_INFO,//!< シャドウマップのテクスチャ情報{ 1.0f * TexSize, 0.5f * TexSize, 1.0f / TexSize, 0.5f / TexSize }.
    IUNIFORM_SHADOW_DARKNESS,//!< シャドウの濃さ.

    IUNIFORM_FOG_COLOR,//!< フォグの色
    IUNIFORM_FOG_RANGE,//!< （フォグがかかる最短の距離、フォグが完全にかかりきる距離、値2－値1）

    IUNIFORM_DEPTH_RANGE,//!< （Near値、Far値、Far値－Near値）

    IUNIFORM_MAX,
  } INDEX_UNIFORMS;
  

  ShaderProgram();

  /**
   * プログラムをソースからコンパイル+リンクをする
   **/
  bool loadFromSource(char const* vertexCode, char const* fragmentCode);

  /**
   * attributeのlocationを取得
   **/
  int getAttributeLocation( int attributeIndex );


  /**
   * uniformのlocationを取得
   **/
  int getUniformLocation( int uniformIndex );

  /**
   * uniformのsize取得
   **/
  GLint getUniformSize( int uniformIndex );

  /**
   * MyUniformを追加する
   **/
  bool setMyUniform( int id, const char* uniformName );

  /**
   * Get the ID of a uniform by name
   **/
  int getMyUniformID( const char* uniformName );

  /**
   * @return Whether uniform with specified id has been loaded
   **/
  bool hasMyUniform( int id );

  /**
   * ユーザ定義Uniformの取得.
   */
  GLint getMyUniformLocation( int idx );

  /**
   * @return first abailable ID
   **/
  int getAvailableMyUniformID();

  /**
   * シェーダープログラムをglUseします
   **/
  void bind();

  bool updateMemberSizeAndType();
  
private:

  // Attributes
  tagShaderMember attributes[ IATTRIBUTE_MAX ];
  // Uniforms
  tagShaderMember uniforms[ IUNIFORM_MAX ];

  // My Uniforms
  std::map<int, tagShaderMember> myUniforms;
  std::map<std::string, int> myUniformsNames;
  
  // シェーダープログラムID
  GLint vshaderGlId;
  GLint fshaderGlId;
  GLint programGlId;
  
  // Private methods
  bool checkCompileStatus(int shaderGlId);
  bool checkLinkStatus();

  void bindAttributeNames(int programGlId);
};
