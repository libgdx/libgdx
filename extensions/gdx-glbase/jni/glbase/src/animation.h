/**
 * @file animation.h
 * @brief Represents an animation resource 
 * loaded in a way specified by subclasses of this class which 
 * is defined by method load().
 **/
#pragma once
#include "types.h"
#include "arrayList.h"

// Forward declarations
class Bone;
class Matrix;

// Matrix length in bytes
#define MATRIX_LENGTH 12*sizeof( float )

/**
 * アニメーション
 **/
class Animation
{
public:

  /**
   * デストラクタ
   **/
  virtual ~Animation();

  /**
   * 派生クラスのコンストラクターで指定されたデータで、
   * ボーンを構成し、アニメーション情報を読み取ります。
   * 
   * @return true-成功 false-失敗
   **/
  virtual bool load() = 0;

  /**
   * Prepend parent animation to all matrices
   **/
  void prependParent(Animation* parent);

  /**
   * 指定のフレームでボーン行列を計算する
   **/
  void setBonesToTime(int frame);

  /**
   * @return ボーン配列
   **/
  ArrayList* getBones();

  /**
   * @return アニメーションの長さ(フレーム数)
   **/
  int getLength();

  /**
   * @return アニメーションのFPS
   **/
  int getFps();
  
  /**
   * @return フレームオフセット
   **/
  int getFrameOffset();

protected:

  /**
   * データモード
   **/
  typedef enum DataMode{
    RESTMOVE_ONLY,
    FINAL_ONLY,
    RESTMOVEFINAL,
	LAYERRESTMOVE_ONLY,
  } DataMode;

  /**
   * 値形式
   **/
  typedef enum ValueType{
    FIXED = 0, // 固定少数
    FLOAT = 1  // 浮動少数
  } ValueType;


  /**
   * 初期化
   **/
  Animation();

  /**
   * Utility for extracting matrices
   **/
  void copyMatrices(Matrix* to, byte* from, int num, ValueType valueType);

  /**
   * Utility for prepending matrices to child array
   **/
  void prependMatrices(Matrix* child, Animation* parent, int length);

  /**
   * Get final matrices to out array
   **/
  void getFinalMatrix(int bone, int frame, Matrix* out);

  /**
   * レイヤー行列（最初のフレームだけ）
   */
  virtual Matrix *getLayerMatrix();

  /**
   * Change dataMode of this animation to FINAL_ONLY or RESTMOVEFINAL,
   * and produce final matrices if needed
   **/
  void makeFinal();

  /**
   * ルートボーン設定
   **/
  void setRootBones();

  // 骨！
  ArrayList bones;

  // Anime data
  Matrix* restMatrices;
  Matrix* moveMatrices;
  Matrix* finalMatrices;

  // Data mode
  DataMode dataMode;

  // アニメーションフレームオフセット(再生されない頭フレーム)
  int frameOffset;

  // アニメーション長さ
  int length;

  // FPS
  int fps;

private:

  /**
   * @return 指定のボーンのルート、stopBoneまでの範囲
   **/
  Bone* getRootBone( Bone* bone, Bone* stopBone = NULL );
};
