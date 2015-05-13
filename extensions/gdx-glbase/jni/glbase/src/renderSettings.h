/*!
 * @file renderSettings.h
 *
 * @brief 描画設定クラス.
 *
 */

#ifndef __RENDER_SETTINGS_H__
#define __RENDER_SETTINGS_H__


#include "arrayList.h"
#include "renderEnums.h"


/*!
 * @brief描画設定クラス.
 */
class RenderSettings {

 private:

  char* id;           //!< Id.
  char* toolName;     //!< ツール名.
  char* version;      //!< バージョン.
  int  leftShiftBits; //!< ビットシフト数.
  ArrayList passes;   //!< パス配列.

  bool parsed;        //!< ﾊﾟｰｽされているか

 public:

  /*!
   * @brief コンストラクタ.
   */
  RenderSettings();

  /*!
   * @briefデストラクタ.
   */
  ~RenderSettings();


  //ゲッタ.
  char* getId();
  char* getVersion();
  ArrayList* getPasses();

  /*!
   * @brief リセット.
   */
  void reset();

  /*!
   * @brief セットアップ.
   *
   * @paramdata MRMデータ.
   *
   * @return 成功フラグ.
   */
  bool setUp( const char *data );

  /**
   *float uniformの値を更新する
   **/
  void updateFloatUniform( int passIdx, int uniformIdx, float value );

  /**
   *vec2 uniformの値を更新する
   **/
  void updateVec2Uniform( int passIdx, int uniformIdx, float x, float y );

  /**
   *vec3 uniformの値を更新する
   **/
  void updateVec3Uniform( int passIdx, int uniformIdx, float x, float y, float z );

  /**
   *vec4 uniformの値を更新する
   **/
  void updateVec4Uniform( int passIdx, int uniformIdx, float x, float y, float z, float w );

  /**
   *uniformの一括更新
   **/
  void updateUniforms( int uniformsNum, int *passUniformIndices, float *values );

  /**
   *  uniformの一括取得
   **/
  void getUniformSizesAndValues( int uniformsNum, int *passUniformIndices, int *sizes, float *values );
  
  /**
   *既にﾊﾟｰｽ済みなのか
   **/
  bool isAlreadyParsed();

  /**
   * 最低ひとつのオフスクリーンパスが存在するか否か
   **/
  bool containsOffscreenPasses();

};


#endif
