/*!
 * @file uniform.h
 *
 * @brief Uniformクラス.
 *
 */

#ifndef __UNIFORM_H__
#define __UNIFORM_H__


#include "arrays.h"
#include "renderEnums.h"

class JObj;


/*!
 * @briefUniformクラス.
 */
class Uniform {

 private:

  char* name;      //!< Uniform名.
  int priority;    //!< Uniform設定の優先度.
  ArrayF *values;   //!< Uniform値.

 public:

  /*!
   * @briefコンストラクタ.
   */
  Uniform();
  
  //ゲッタ.
  char* getName();
  int getPriority();
  ArrayF *getValues();
  
  //セッタ.
  void setName( char *name );
  void setPriority( int priority );

  /*!
   * @briefセットアップ.
   *
   * @paramoオブジェクト.
   * @paramleftShiftBitsビットシフト数.
   *
   * @return成功フラグ.
   */
  bool setUp( JObj* o, int leftShiftBits );

  /*!
   * @briefデストラクタ.
   */
  ~Uniform();
};


#endif //__UNIFORM_H__

