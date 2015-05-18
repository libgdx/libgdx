/**
 * @file timer.cpp
 * @brief Timer type
 **/

#include "timer.h"
#include <time.h>

Timer::Timer() {
  time = 0.0;
  accumulatedTime = 0.0;
  running = false;
}

void Timer::start( double addStartTime ) {
  if( running ) {
    return;
  }
  
  gettimeofday( &timev, NULL );

  //開始時間をずらす
  if( addStartTime > 0.0 ) {
    //μ秒に変換
    addStartTime *= 1000.0;

    while( addStartTime > timev.tv_usec ) {
      timev.tv_sec -= 1.0;
      addStartTime -= 1000000.0;
    }

	timev.tv_usec -= addStartTime;
  }

  running = true;
}

void Timer::reset() {
  if( running ) {
	  return;
  }

  time = 0.0f;
  accumulatedTime = 0.0f;
}

void Timer::stop() {
  if( !running ) {
    return;
  }

  accumulatedTime += time;
  time = 0.0;
  running = false;
}

double Timer::getTime() {
  return time + accumulatedTime;
}

/**
  * 更新
  */
void Timer::update( struct timeval &currentTimev ) {
  if( !running ) {
    return;
  }

  time = (currentTimev.tv_sec - timev.tv_sec) * 1000.0;      // sec to ms
  time += (currentTimev.tv_usec - timev.tv_usec) / 1000.0;   // us to ms
}

/**
 * 親タイマーと同じタイミングで開始する
 **/ 
void Timer::startSync( Timer *parent ) {
  if( running ) {
    return;
  }

  timev = parent->timev;
  running = true;
}

/**
 * 親タイマーと同じタイミングで停止する
 **/
void Timer::stopSync( Timer *parent ) {
  if( !running ) {
    return;
  }

  accumulatedTime = parent->accumulatedTime;
  time = 0.0;
  running = false;
}
