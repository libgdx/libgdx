/**
 * @file IrenderQueue.h
 * @brief Public interface for RenderQueue
 **/
#pragma once

#include <stdarg.h>
#include "types.h"

// Forwards
class DrawCall;
class Matrix;

class IRenderQueue
{
  
public:
  virtual ~IRenderQueue() = 0;

  /**
   * @brief プロジェクション行列を設定する
   * モデル、ビュー、プロジェクション行列を書き合わせた結果が
   * m4ModelViewProjectionMatrixのシェーダーuniform に設定されます。
   *
   * @param projection プロジェクション行列
   **/
  virtual void setProjection(Matrix const* projection) = 0;

  /**
   * @brief ビュー行列を設定する
   * モデル、ビュー、プロジェクション行列を書き合わせた結果が
   * m4ModelViewProjectionMatrixのシェーダーuniform に設定されます。
   *
   * @param view ビュー行列
   **/
  virtual void setView(Matrix const* view) = 0;

  /**
   * @return プロジェクション行列
   **/
  virtual Matrix* getProjection() = 0;

  /**
   * @return ビュー行列
   **/
  virtual Matrix* getView() = 0;

  /**
   * @brief フォグを設定する
   * シェーダーでフォグの計算処理が入っていなければ、フォグが表示されません。
   * shaderNames.hをご参照ください。
   *
   * @param fogColor フォグの色
   * @param fogNear フォグが始まる距離
   * @param fogFar フォグが完全にfogColorになる距離
   *
   **/
  virtual void setFog(float fogColor[3], float fogNear, float fogFar) = 0;

  /**
   * @brief 全ての描画登録を実際にレンダリングします。
   * このメソッドが呼び出されるタイミングで、全ての登録リストが
   * からの状態に戻ります。
   **/
  virtual void execRender() = 0;

  /**
   * @brief 描画登録を行います。 登録された内容が、IGLBase::execRender()が
   * 呼び出されるタイミングで、実際にレンダリングされます。
   *
   * @param drawCall レンダリング内容を格納する構造体。レンダリングの対象
   * となるpolygonMapが必ず設定されていなければなりません
   **/
  virtual void registerDrawCall(DrawCall* drawCall) = 0;
};
