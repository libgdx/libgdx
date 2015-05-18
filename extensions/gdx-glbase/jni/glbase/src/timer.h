/**
 * @file timer.h
 * @brief Timer type
 **/
#pragma once

#include <sys/time.h>

/**
 * タイマークラス
 **/
class Timer
{
public:
  
  /**
   * 初期化
   **/
  Timer();

  /**
   * タイマーをスタート
   **/ 
  void start( double addStartTime = 0.0 );

  /**
   * タイマーをストップ
   **/
  void stop();

  /**
   * リセット
   **/
  void reset();

  /**
   * 時間取得
   **/
  double getTime();

  /**
   * 計測中か
   */
  bool isRunning() {
    return running;
  }

  /**
   * 親タイマーと同じタイミングで開始する
   **/ 
  void startSync( Timer *parent );

  /**
   * 親タイマーと同じタイミングで停止する
   **/
  void stopSync( Timer *parent );

  /**
   * 更新
   */
  void update( struct timeval &currentTimev );

private:
  double time;
  double accumulatedTime;
  struct timeval timev;
  bool running;
};
