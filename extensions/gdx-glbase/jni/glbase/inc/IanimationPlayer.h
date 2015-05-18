/**
 * @file IAnimationPlayer.h
 * @brief Utility to play an animation on a render target (Polygon map)
 **/
#pragma once

#include <sys/time.h>


/**
 * アニメーションを操作するためのインターフェース
 **/
class IAnimationPlayer
{
 public:

  /**
   * @brief 再生モードを定義する
   **/
  enum PlayMode {
    /**
     * @brief ループ再生
     **/
    LOOP,

    /**
     * @brief 最初から最後まで一度だけ再生を行い、再生終了の時に1フレーム目に戻り、
     * それ以降はずっと1フレーム目が適応されます。
     **/
    PLAY_ONCE,

    /**
     * @brief 最初から最後まで一度だけ再生を行い、再生終了の時に最後のフレームのままで、
     * それ以降も最後のフレームが適応されます。
     **/
    KEEP_END

  };
  
  /**
   * @brief アニメーション再生を開始する
   **/
  virtual void play( PlayMode playMode, int addStartTimeMS = 0 ) = 0;

  /**
   * @brief アニメーションを再開する
   */
  virtual void replay() = 0;

  /**
   * @brief アニメーション再生を停止する
   **/
  virtual void stop() = 0;

  /**
   * @brief アニメーションの再生位置を初期の状態に戻す
   * このメソッドはアニメーションが停止している時のみに有効です
   **/
  virtual void rewind() = 0;

  /**
   * @brief アニメーションを無設定状態にする
   */
  virtual void unsetAnimation() = 0;

  /**
   * @brief アニメーションが再生中か
   */
  virtual bool isPlaying() = 0;

  /**
   * 更新
   */
  virtual void update( struct timeval &currentTimev ) = 0;

  /**
   * 親プレイヤーと同じタイミングで再生を開始する
   */
  virtual void playSync( PlayMode playMode, IAnimationPlayer *parent ) = 0;

  /**
   * 親プレイヤーと同じタイミングで再生を停止する
   */
  virtual void stopSync( IAnimationPlayer *parent ) = 0;

  /**
   * 親プレイヤーと同じタイミングで再開する
   */
  virtual void replaySync( IAnimationPlayer *parent ) = 0;
};
