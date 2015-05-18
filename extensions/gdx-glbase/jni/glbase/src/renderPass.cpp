/*!
 * @file renderPass.cpp
 *
 * @brief 描画パス.
 */

#include "renderPass.h"
#include "shaderTable.h"
#include "arrays.h"
#include "glbase.h"
#include "macros.h"
#include "json.h"

#define DEFAULT_SHADER_NAME "__vdefault_shader___fdefault_shader" //!< デフォルトシェーダ名.


/*!
 * @briefコンストラクタ.
 */
RenderPass::RenderPass() : uniforms( 128 ) {

  id = NULL;
  target = RenderEnums::TEXTYPE_DEFAULT;
  clearMode = RenderEnums::CLEARMODE_COLOR_AND_DEPTH;
  modelType = RenderEnums::MODELTYPE_DEFAULT;
  particleCount = 1;
  cullingMode = RenderEnums::CULLING_DEFAULT;
  textureType = RenderEnums::TEXTYPE_DEFAULT;
  blendMode = RenderEnums::BLENDMODE_DEFAULT;
  depthMask = true;
  depthFunc = RenderEnums::DEPTHFUNC_LESS;
  shaderName = (char*)DEFAULT_SHADER_NAME;
  layerName = (char*)DEFAULT_LAYER_NAME;

  for( int i = 0; i < 4; i++ ) {

    clearColor[ i ] = 0;
    colorMask[ i ] = true;
  }
}

//ゲッタ.
char* RenderPass::getId() {

  return id;
}

int RenderPass::getTarget() {

  return target;
}

int RenderPass::getClearMode() {

  return clearMode;
}

float *RenderPass::getClearColor() {

  return clearColor;
}

int RenderPass::getModelType() {

  return modelType;
}

int RenderPass::getParticleCount() {
  return particleCount;
}

int RenderPass::getCullingMode() {

  return cullingMode;
}

int RenderPass::getTextureType() {

  return textureType;
}

int RenderPass::getBlendMode() {

  return blendMode;
}

bool RenderPass::getDepthMask() {

  return depthMask;
}

int RenderPass::getDepthFunc() {

  return depthFunc;
}

bool *RenderPass::getColorMask() {

  return colorMask;
}

char* RenderPass::getShaderName() {

  return shaderName;
}

char* RenderPass::getLayerName() {

  return layerName;
}

char* RenderPass::getMuM() {

  return mum;
}

ArrayList* RenderPass::getUniforms() {

  return &uniforms;
}

/*!
 * @briefセットアップ.
 */
bool RenderPass::setUp( JObj* o, int leftShiftBits ) {

  if( (leftShiftBits < 1) || (leftShiftBits > 30) ) {
    etrace( "invalid param" );
    return false;
  }

  target = RenderEnums::TEXTYPE_DEFAULT;//!< ターゲット.
  clearMode = RenderEnums::CLEARMODE_COLOR_AND_DEPTH;//!< クリアモード.
  modelType = RenderEnums::MODELTYPE_DEFAULT;//!< モデルの種類.
  particleCount = 1;//!< パーティクルの数.
  cullingMode = RenderEnums::CULLING_DEFAULT;//!< カリングモード.
  textureType = RenderEnums::MODELTYPE_DEFAULT;//!< テクスチャの種類.
  blendMode = RenderEnums::BLENDMODE_DEFAULT;//!< ブレンドモード.
  depthMask = 1;//!< デプスマスク.
  depthFunc = RenderEnums::DEPTHFUNC_LESS;//!< デプス比較関数.

  JObj* uniformObjects;
  char* vertexShader, *fragmentShader;
  ArrayI _clearColor, _colorMask;

  OBJ_SETSTR( o, id, "Id" );
  OBJ_SETINT( o, target, "Target" );
  OBJ_SETINT( o, clearMode, "ClearMode" );
  OBJ_SETINTARRAY( o, _clearColor, "ClearColor" );
  OBJ_SETINT( o, modelType, "ModelType" );
  OBJ_SETINT( o, particleCount, "ParticleCount" );
  OBJ_SETINT( o, cullingMode, "CullingMode" );
  OBJ_SETINT( o, textureType, "TextureType" );
  OBJ_SETINT( o, blendMode, "BlendMode" );
  OBJ_SETINT( o, depthMask, "DepthMask" );
  OBJ_SETINT( o, depthFunc, "DepthFunc" );
  OBJ_SETINTARRAY( o, _colorMask, "ColorMask" );
  OBJ_SETSTR( o, vertexShader, "VertexShader" );
  OBJ_SETSTR( o, fragmentShader, "FragmentShader" );
  OBJ_SETSTR( o, layerName, "LayerName" );
  OBJ_SETSTR( o, mum, "MuM" );
  uniformObjects = o->getHashPVal( "Uniforms" );

  shaderName = new char[ strlen(vertexShader) + strlen(fragmentShader) + 2 ];
  sprintf( shaderName, "%s_%s", vertexShader, fragmentShader );
  delete[] vertexShader;
  delete[] fragmentShader;

  if(
     (_clearColor.getLen() != 4) ||
     (_colorMask.getLen() != 4) ||
     (uniformObjects == NULL) ||
     (!uniformObjects->isArray())
     ) {

    etrace( "invalid format" );
    return false;
  }

  for( int i = 0; i < uniformObjects->getLen(); i++ ) {
    JObj *curUniform = uniformObjects->getArrayPVal( i );

    Uniform* uniform = new Uniform();
    if( uniform->setUp( curUniform, leftShiftBits ) ) {
      uniforms.add( uniform );
    } else {
      etrace("Error in uniforms");
      return false;
    }
  }

  clearColor[ 0 ] = (float)_clearColor[ 0 ] / (1 << leftShiftBits);
  clearColor[ 1 ] = (float)_clearColor[ 1 ] / (1 << leftShiftBits);
  clearColor[ 2 ] = (float)_clearColor[ 2 ] / (1 << leftShiftBits);
  clearColor[ 3 ] = (float)_clearColor[ 3 ] / (1 << leftShiftBits);

  colorMask[ 0 ] = _colorMask[ 0 ];
  colorMask[ 1 ] = _colorMask[ 1 ];
  colorMask[ 2 ] = _colorMask[ 2 ];
  colorMask[ 3 ] = _colorMask[ 3 ];

  mum = (mum==NULL) ? strdup2("__no_mum") : mum;

  trace( "Pass ID: %s", id );
  trace( "Pass Shader: %s", shaderName );
  
  return true;
}

/*!
 * @briefデストラクタ.
 */
RenderPass::~RenderPass() {
  delete[] shaderName;
  delete[] mum;
  delete[] layerName;
  delete[] id;
}

int RenderPass::getUniformsNum() {
  return uniforms.getSize();
}

char *RenderPass::getUniformName( int uniformIndex ) {
  return ((Uniform *)uniforms.get( uniformIndex ))->getName();
}

int RenderPass::getUniformSize( int uniformIndex ) {
  return ((Uniform *)uniforms.get( uniformIndex ))->getValues()->len;
}

float *RenderPass::getUniformValues( int uniformIndex ) {
  return ((Uniform *)uniforms.get( uniformIndex ))->getValues()->el;
}

