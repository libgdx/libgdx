/**
 * @file renderQueue.h
 * @brief Private interface for RenderQueue
 **/
#pragma once

#include <map>

#include "IrenderQueue.h"
#include "renderList.h"
#include "matrixStack.h"

class Matrix;
class MyUniformValue;

/**
 * @brief 
 **/
class RenderQueue : public IRenderQueue
{
 public:
  // Construct
  RenderQueue();
  // Desctruct
  ~RenderQueue();

  void execRender();
  void registerDrawCall(DrawCall* drawCall);

  void setProjection(Matrix const* projection);
  void setView(Matrix const* view);
  void setFog(float fogColor[3], float fogNear, float fogFar);


  /**
   * 行列スタックを取得
   **/
  MatrixStack* getMVStack();

  /**
   * プロジェクション行列を取得
   **/
  Matrix* getProjection();

  /**
   * ビュー行列を取得
   **/
  Matrix* getView();

  /**
   * MVP行列を取得
   **/
  void getModelViewProjection( Matrix *out );

  /**
   * VP行列を取得
   **/
  void getViewProjection( Matrix *out );
  
  /**
   * MV両列を取得
   **/
  Matrix *getModelView();
    
  /**
   * レンダーリストを取得
   **/
  RenderList* getRenderList();

  /**
   * シェーダーで有効な行列uniformを更新する
   **/
  void bindUniforms( std::map<int, MyUniformValue*>* myUniforms, int* viewport );

  /**
   * 全てのuniformをオフにする
   **/
  void unbindUniforms();
    
 private:
  // バッチレンダリング
  RenderList renderList;
  
  // 行列スタック
  MatrixStack mvStack;

  // PROJECTION行列
  Matrix projection;
  // VIEW行列
  Matrix view;

  // frustum設定
  float frustumNear;
  float frustumFar;

  // フォグ
  float fogColor[3];
  float fogNear;
  float fogFar;
};
