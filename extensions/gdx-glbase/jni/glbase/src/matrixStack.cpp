/**
 * @file matrixStack.cpp
 * @brief Matrix stack implementation
 **/

#include "matrixStack.h"
#include <stdio.h>

MatrixStack::MatrixStack()
{
  clear();
}
  
void MatrixStack::push(Matrix* matrix)
{
  Matrix* newTop = &stack[size];

  // スタックが空の場合
  if( size == 0 ){
    newTop->copyFrom(matrix);
  }

  // スタックが空じゃない場合
  else{
    newTop->copyFrom(&stack[size-1]); 
    newTop->multiply(matrix);
  }

  size++;
}

void MatrixStack::pop()
{
  size--;
}

Matrix* MatrixStack::top()
{
  if( size == 0 ) return NULL;
  return &stack[size-1];
}

void MatrixStack::clear()
{
  size = 0;
}

Matrix* MatrixStack::get(int stackIndex)
{
  return &stack[stackIndex];
}
