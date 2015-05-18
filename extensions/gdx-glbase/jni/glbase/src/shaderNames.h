/**
 * @file shaderNames.h
 * @brief Name definitions for shader attributes and uniforms
 **/
#pragma once

/*--------------------------------------------------
----------------------------------------------------

                      定数定義                      

----------------------------------------------------
--------------------------------------------------*/

#define NAME_ATTRIBUTE_VERTEX                     "v3Vertex"                 //!< 頂点座標Attribute.
#define NAME_ATTRIBUTE_TEXTURE_COORD0             "v2TextureCoord0"          //!< UVAttribute.
#define NAME_ATTRIBUTE_COLOR                      "v4Color"                  //!< 頂点カラーAttribute.
#define NAME_ATTRIBUTE_NORMAL                     "v3Normal"                 //!< 頂点法線Attribute.
#define NAME_ATTRIBUTE_MATRIX_INDICES             "v4MatrixIndices"          //!< 行列インデックスAttribute.
#define NAME_ATTRIBUTE_WEIGHTS                    "v4Weights"                //!< ウエイトAttribute.

#define NAME_UNIFORM_MODEL_VIEW_PROJECTION_MATRIX "m4ModelViewProjectionMatrix"//!< ModelViewProjection行列Uniform.
#define NAME_UNIFORM_NORMAL_MATRIX "m3NormalMatrix"//!< Normal行列Uniform(3X3のModelView).
#define NAME_UNIFORM_VIEW_ROTATION_MATRIX "m3ViewRotationMatrix"//!< ビュー回転行列.
#define NAME_UNIFORM_MODEL_VIEW_MATRIX "m4ModelViewMatrix"//!< ModelView行列Uniform.
#define NAME_UNIFORM_MODEL_VIEW_MATRIX_3VEC4 "av4ModelViewMatrix[0]"//!< ModelView行列Uniform(行順で3行X4列).
#define NAME_UNIFORM_PROJECTION_MATRIX "m4ProjectionMatrix"//!< Projection行列Uniform.
#define NAME_UNIFORM_INVERSE_MODEL_VIEW_MATRIX "m4InverseModelViewMatrix"//!< 逆ModelView行列Uniform.
#define NAME_UNIFORM_MODEL_MATRIX "m4ModelMatrix"//!< Model行列Uniform.
#define NAME_UNIFORM_MODEL_LIGHT_PROJECTION_MATRIX "m4ModelLightProjectionMatrix"//!< Projection(Light)×Light0×Model行列Uniform.
#define NAME_UNIFORM_MODEL_LIGHT_MATRIX "m4ModelLightMatrix"//!< Light0×Model行列Uniform.
#define NAME_UNIFORM_LIGHT_PROJECTION_MATRIX "m4LightProjectionMatrix"//!< LightProjection行列Uniform.
#define NAME_UNIFORM_SHADOW_MAP_MATRIX "m4ShadowMapMatrix"//!< Texture×Projection(Light)×Light0×InverseView行列Uniform.

#define NAME_UNIFORM_MATRIX_PALETTE "av4MatrixPalette[0]"//!< 行列パレットUniform(行順で3行X4列).
#define NAME_UNIFORM_ROOT_MATRIX_PALETTE "av4RootMatrixPalette[0]"//!< ルート行列パレットUniform(行順で3行X4列).
#define NAME_UNIFORM_BMV_MATRIX_PALETTE "av4BMVMatrixPalette[0]"//!< Projection×View×Model×ボーン行列パレットUniform(行順で3行X4列).
#define NAME_UNIFORM_BMVP_MATRIX_PALETTE "am4BMVPMatrixPalette[0]"//!< Projection×View×Model×ボーン行列パレットUniform.
#define NAME_UNIFORM_BMLP_MATRIX_PALETTE "am4BMLPMatrixPalette[0]"//!< Projection×Light0×Model×ボーン行列パレットUniform.
#define NAME_UNIFORM_SPRITE_MATRIX "m4SpriteMatrix" //!< Sprite変換行列

#define NAME_UNIFORM_TEXTURE0 "s2DTexture0"//!< GL_TEXTURE0Uniform.
#define NAME_UNIFORM_SHADOW_MAP_TEXTURE "s2DShadowMapTexture"//!< シャドウマップテクスチャ（デプス、GL_TEXTURE1）Uniform.
#define NAME_UNIFORM_SHADOW_COLOR_TEXTURE "s2DShadowColorTexture"//!< シャドウマップテクスチャ（カラー、GL_TEXTURE2）Uniform.

#define NAME_UNIFORM_TIME_SECONDS "fTimeSeconds"//!< 現在の時間（秒）.
#define NAME_UNIFORM_VIEWPORT_RECT "v4ViewportRect"//!< 現在のビューポート.
#define NAME_UNIFORM_VIEWPORT_RECIPROCAL "v2ViewportSizeReciprocal"//!< 現在のビューポートサイズの逆数.
#define NAME_UNIFORM_SCREEN2DISPLAY "v2Screen2Display"//!< ディスプレイサイズ：描画先サイズ.
#define NAME_UNIFORM_DISPLAY2SCREEN "v2Display2Screen"//!< 描画先サイズ：ディスプレイサイズ.

#define NAME_UNIFORM_MATERIAL_DIFFUSE "v3MaterialDiffuse"//!< マテリアルの拡散色.
#define NAME_UNIFORM_MATERIAL_AMBIENT "v3MaterialAmbient"//!< マテリアルの環境色.
#define NAME_UNIFORM_MATERIAL_SPECULAR "v3MaterialSpecular"//!< マテリアルの鏡面反射色.
#define NAME_UNIFORM_MATERIAL_EMISSIVE "v3MaterialEmissive"//!< マテリアルの発光色.
#define NAME_UNIFORM_MATERIAL_SHININESS "fMaterialShininess"//!< マテリアルの鏡面反射の強度.
#define NAME_UNIFORM_MATERIAL_GLOSSINESS "fMaterialGlossiness"//!< マテリアルの鏡面反射の広さ.

#define NAME_UNIFORM_LIGHT_POSITION "v3LightPosition[0]"//!< 各光源の位置(複数).
#define NAME_UNIFORM_LIGHT_DIFFUSE "v3LightDiffuseCol[0]"//!< 各光源の拡散光.
#define NAME_UNIFORM_LIGHT_AMBIENT "v3LightAmbientCol[0]"//!< 各光源の環境光.
#define NAME_UNIFORM_LIGHT_STRENGTH "v3LightStrength[0]"//!< 各光源の強さ.

#define NAME_UNIFORM_ADJACENT_UV_OFFSET_H "v4AdjacentUVOffsetH"//!< 隣接する4ピクセルのUVオフセット値（横）.
#define NAME_UNIFORM_ADJACENT_UV_OFFSET_V "v4AdjacentUVOffsetV"//!< 隣接する4ピクセルのUVオフセット値（縦）.
#define NAME_UNIFORM_SHADOW_MAP_INFO "v4ShadowMapInfo"//!< シャドウマップのテクスチャ情報{ 1.0f * TexSize, 0.5f * TexSize, 1.0f / TexSize, 0.5f / TexSize }.
#define NAME_UNIFORM_SHADOW_DARKNESS "fShadowDarkness"//!< シャドウの濃さ.

// フォグ
#define NAME_UNIFORM_FOG_COLOR "v3FogColor"//!< フォグの色
#define NAME_UNIFORM_FOG_RANGE "v3FogRange"//!< （フォグがかかる最短の距離、フォグが完全にかかりきる距離、値2－値1）

//カメラ
#define NAME_UNIFORM_CAMERA_POSITION "v3CameraPosition"//!< カメラの座標
#define NAME_UNIFORM_DEPTH_RANGE "v3DepthRange"//!< （Near値、Far値、Far値－Near値）
