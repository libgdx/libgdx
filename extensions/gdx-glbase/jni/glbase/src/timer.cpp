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

  //�J�n���Ԃ����炷
  if( addStartTime > 0.0 ) {
    //�ʕb�ɕϊ�
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
  * �X�V
  */
void Timer::update( struct timeval &currentTimev ) {
  if( !running ) {
    return;
  }

  time = (currentTimev.tv_sec - timev.tv_sec) * 1000.0;      // sec to ms
  time += (currentTimev.tv_usec - timev.tv_usec) / 1000.0;   // us to ms
}

/**
 * �e�^�C�}�[�Ɠ����^�C�~���O�ŊJ�n����
 **/ 
void Timer::startSync( Timer *parent ) {
  if( running ) {
    return;
  }

  timev = parent->timev;
  running = true;
}

/**
 * �e�^�C�}�[�Ɠ����^�C�~���O�Œ�~����
 **/
void Timer::stopSync( Timer *parent ) {
  if( !running ) {
    return;
  }

  accumulatedTime = parent->accumulatedTime;
  time = 0.0;
  running = false;
}
