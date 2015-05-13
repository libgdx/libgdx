/**
 * @file arrayList.h
 * @brief Array based list utility
 **/
#pragma once

#include <stdio.h>


/**
 * @brief すべての項目を回すためのforeach
 **/
#define foreach_element(list, it, T) if( ((list) != NULL) && !(list)->isEmpty() ) it=(T)(list)->get(0); for( int i=0; i<(list)->getSize(); (++i<(list)->getSize()) && (it=(T)(list)->get(i)) )

/**
 * @brief パフォーマンス重視のリスト機能。
 * 自動拡張はないため、コンストラクターで指定のサイズが
 * 個数の上限となります。
 *
 **/
class ArrayList
{
 public:
  
  /**
   * @param maxsize 最大の個数
   **/
  ArrayList(int maxsize);

  /**
   * Destructor
   **/
  ~ArrayList();

  /**
   * リストないの指定の位置の個を取得する
   *
   * @param position 位置
   * @param outElement 取得したエレメントへのポインター(出力)
   * 
   * @return 成功の場合は取得した項目、失敗の場合はNULL
   **/
  void* get(int position);

  /**
   * 指定のエレメントをリストに追加する
   *
   * @param element 追加される項目
   * @return 正常の場合はtrue、エラーの場合はfalse
   **/
  bool add(void* element);

  /**
   * @return リストのサイズ
   **/
  int getSize();

  /**
   * @return リストが空か否か
   **/
  bool isEmpty();
  
  /**
   * リストを空にする
   **/
  void clear();

  /**
   * リストの最大個数を変更
   * 変更前のデータは維持されます
   * @param maxsize 最大の個数
   **/
  void resize( int maxsize );
 
 private:
  void** elements;
  int size;
  int maxsize;
};
