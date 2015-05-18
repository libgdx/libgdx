/**
 * @file matrixStack.h
 * @brief Matrix stack
 **/
#pragma once

#include "matrix.h"

/**
 * @brief push() pop()で行列のかけあわせと巻き戻しが
 * 高速にできるためのモジュール
 **/
class MatrixStack
{
public:

  /**
   * 初期化
   **/
  MatrixStack();
  
  /**
   * 行列をスタックに掛け合わせる
   **/
  void push(Matrix* matrix);

  /**
   * 最後の掛け合わせた行列をundoする
   **/
  void pop();

  /**
   * @return 現在のトップ行列(pushされた行列を全部掛け合わせた結果)
   **/
  Matrix* top();

  /**
   * スタックを空にする
   **/
  void clear();

  /**
   * スタック上の任意位置の行列取得
   *
   **/
  Matrix* get(int stackIndex);

private:
  // 最大対応行列数
  static const int MAX_STACKSIZE = 128;

  // スタックメモリー
  Matrix stack[MAX_STACKSIZE];
  int size;
};
