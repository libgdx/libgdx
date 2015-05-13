/**
 * @file renderList.cpp
 * @brief Render priority list implementation
 **/

#include "renderList.h"
#include "glbase.h"
#include "object.h"
#include "layer.h"
#include "polygonMap.h"
#include "macros.h"
#include "arrayList.h"
#include "drawCall.h"
#include "renderQueue.h"
#include "surface.h"
#include "drawCallsPool.h"

//#define DEBUG_OFFSCREEN

RenderList::RenderList()
{
  // Render listsを作成
  renderList = new ArrayList(DrawCallsPool::MAX_DRAW_CALLS);
}

RenderList::~RenderList()
{
  delete renderList;
}

void RenderList::registerDrawCall(DrawCall* drawCall)
{
  renderList->add(drawCall);
}

/**
 * すべての登録されたレンダリングを行う
 * @param	clearScreen	glClear()を呼ぶか
 **/
void RenderList::execRender( RenderQueue* queue )
{
  // 優先通りでレンダリングを行う
  DrawCall* dc;
  foreach_element(renderList, dc, DrawCall*){
    dc->execRender(queue);
  }
  
  // リストをクリアする
  renderList->clear();
}
