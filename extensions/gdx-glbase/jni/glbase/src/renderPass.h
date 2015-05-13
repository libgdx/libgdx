/*!
 * @file renderPass.h
 *
 * @brief 描画パス.
 *
 */

#ifndef __RENDER_PASS_H__
#define __RENDER_PASS_H__


#include "IrenderPass.h"
#include "arrayList.h"
#include "renderEnums.h"
#include "uniform.h"


#define DEFAULT_LAYER_NAME "__all_layers"  //!< デフォルトレイヤー名（全レイヤー表示）.


class JObj;

/*!
 * @brief描画パス.
 */
class RenderPass : public IRenderPass {
 private:

  char* id;//!< Id.
  int target;//!< ターゲット.
  int clearMode;//!< クリアモード.
  float clearColor[ 4 ];//!< クリア色.
  int modelType;//!< モデルの種類.
  int particleCount;//!< パーティクルの数.
  int cullingMode;//!< カリングモード.
  int textureType;//!< テクスチャの種類.
  int blendMode;//!< ブレンドモード.
  bool depthMask;//!< デプスマスク.
  int depthFunc;//!< デプス比較関数.
  bool colorMask[ 4 ];//!< カラーマスク.
  char* shaderName;//!< シェーダ名.
  char* layerName;//!< レイヤー名.
  char* mum;//!< MuM
  ArrayList uniforms;//!< Uniform配列.

 public:

  /*!
   * @briefコンストラクタ.
   */
  RenderPass();

  /*!
   * @briefデストラクタ.
   */
  ~RenderPass();

  
  //ゲッタ.
  char *getId();
  int getTarget();
  int getClearMode();
  float *getClearColor();
  int getModelType();
  int getParticleCount();
  int getCullingMode();
  int getTextureType();
  int getBlendMode();
  bool getDepthMask();
  int getDepthFunc();
  bool *getColorMask();
  char* getShaderName();
  char* getLayerName();
  char* getMuM();
  ArrayList* getUniforms();
  int getUniformsNum();
  char *getUniformName( int uniformIndex );
  int getUniformSize( int uniformIndex );
  float *getUniformValues( int uniformIndex );

  //TODO: これ大丈夫かな
  void setTextureType( int textureType ) { this->textureType = textureType; }
  void setTarget( int target ) { this->target = target; }

  /*!
   * @briefセットアップ.
   *
   * @paramoオブジェクト.
   * @paramleftShiftBitsビットシフト数.
   *
   * @return成功フラグ.
   */
  bool setUp( JObj* p, int leftShiftBits );
};


#endif //__RENDER_PASS_H__

