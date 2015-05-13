/**
 * @file animationPlayer.h
 * @brief Utility to play an animation on an render target (Polygon map)
 **/
#pragma once

#include "arrayList.h"
#include "timer.h"
#include "IanimationPlayer.h"

// Forward declarations
class Animation;
class Bone;
class RenderQueue;

/**
 * アニメーション再生・キャッシュクラス
 **/
class AnimationPlayer : public IAnimationPlayer
{
public:
  
  /**
   * インスタンス生成
   **/
  AnimationPlayer( Animation* animation, ArrayList* matrixIndicesNames = NULL );

  /**
   * ボーンを指定ウエイト名配列の順番にソート
   */
  void sortBones( ArrayList* matrixIndicesNames );

  /**
   * @return ソート済ボーンリスト
   **/
  ArrayList* getSortedBones();

  /**
   * アニメーションを現在位置の設定で、バインドする
   **/
  void bind(RenderQueue* queue);

  /**
   * アニメーションが設定されていない時用の、NULLアニメーションをバインド
   * 全てのボーン関連行列パレットに、MVPがそのまま投げられます。
   **/
  static void bindNullAnimation(RenderQueue* queue);

  void play( PlayMode playMode, int addStartTimeMS = 0 );

  void replay();

  void stop();

  void rewind();

  /**
   * @brief アニメーションを無設定状態にする
   */
  void unsetAnimation();

  /**
   * @brief アニメーションが再生中か
   */
  bool isPlaying();

  /**
   * 更新
   */
  void update( struct timeval &currentTimev );

  /**
   * 親プレイヤーと同じタイミングで再生を開始する
   */
  void playSync( PlayMode playMode, IAnimationPlayer *parent );

  /**
   * 親プレイヤーと同じタイミングで再生を停止する
   */
  void stopSync( IAnimationPlayer *parent );

  /**
   * 親プレイヤーと同じタイミングで再開する
   */
  void replaySync( IAnimationPlayer *parent );
  
private:

  //行列情報をGLに転送する際に使用するバッファ
  static float *matrixPalette;

  //ボーンが見つからなかった時に使用する仮ボーン
  static Bone *nullBone;

  // アニメーションリソース
  Animation* animation;

  // ソート済ボーンリスト
  ArrayList sortedBones;

  // アニメーションタイマー
  Timer timer;

  // プレーモード
  PlayMode playMode;


  // private methods
  static void setMatrixPalette(ArrayList* sortedBones);
  static void setRootMatrixPalette(ArrayList* sortedBones);
  static void setBMVPMatrixPalette(ArrayList* sortedBones, RenderQueue* queue);
  static void setBMVMatrixPalette(ArrayList* sortedBones, RenderQueue* queue);
};
