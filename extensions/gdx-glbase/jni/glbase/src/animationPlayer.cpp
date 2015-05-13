/**
 * @file animationPlayer.cpp
 * @brief Utility to play an animation on an render target (Polygon map)
 **/

#include <string.h>

#include "glbase.h"
#include "macros.h"
#include "animationPlayer.h"
#include "bone.h"
#include "animation.h"
#include "renderQueue.h"

float *AnimationPlayer::matrixPalette = new float[ MAX_MATRIX_PALETTES * 16 ];
Bone *AnimationPlayer::nullBone;

AnimationPlayer::AnimationPlayer( Animation* animation, ArrayList* matrixIndicesNames )
  : sortedBones( GLBase::get()->getMaxPaletteMatrices() )
{
  this->animation = animation;
  this->playMode = LOOP;
  if( matrixIndicesNames != NULL ) {
    sortBones( matrixIndicesNames );
  }
}

void AnimationPlayer::sortBones( ArrayList* matrixIndicesNames ) {
  sortedBones.clear();

  if( animation == NULL ) {
    return;
  }

  // 指定通りにソートされたボーン配列を作る
  for( int i=0; i<matrixIndicesNames->getSize(); i++ ){
    char* boneName = (char*) matrixIndicesNames->get(i);

    // ボーン配列に検索
    Bone* foundBone = NULL;
    for( int b=0; b<animation->getBones()->getSize(); b++ ){
      Bone* bone = (Bone*)animation->getBones()->get(b);
      if( strcmp( bone->WeightMapName, boneName ) == 0 ){
        foundBone = bone;
        break;
      }
    }

    if( foundBone == NULL ) {
      etrace("Warning: .bo3 bone not present in .bs: %s", boneName);

	  if( nullBone == NULL ) {
		  nullBone = new Bone();
		  nullBone->Name = strdup2( boneName );
		  nullBone->WeightMapName = strdup2( boneName );
		  nullBone->Root = nullBone;
	  }

	  sortedBones.add( nullBone );
    }
    else{
      trace("Sorted bone: %s", foundBone->WeightMapName);
      sortedBones.add(foundBone);
    }
  }
}

ArrayList* AnimationPlayer::getSortedBones()
{
  return &sortedBones;
}

void AnimationPlayer::bind(RenderQueue* queue)
{
  if( animation == NULL ) {
    bindNullAnimation( queue );
    return;
  }

  // フレームを断定
  int fps = animation->getFps();
  int length = animation->getLength();
  int framePosition = (int)(timer.getTime() * fps / 1000);
  int animFrame = 0;

  switch( playMode ){
  case LOOP: 
    animFrame = animation->getFrameOffset() + (framePosition % length);
    break;

  case PLAY_ONCE:
    if( framePosition >= length ) animFrame = 0;
    else animFrame = animation->getFrameOffset() + framePosition;
    break;

  case KEEP_END:
    if( framePosition >= length ) animFrame = animation->getFrameOffset() + animation->getLength()-1;
    else animFrame = animation->getFrameOffset() + framePosition;
    break;
  }

  // ボーン行列計算
  animation->setBonesToTime(animFrame);
  
  // シェーダーUniform行列パレットを設定
  if( sortedBones.getSize() <= 0 ) {
    bindNullAnimation( queue );
  } else {
    setMatrixPalette( &sortedBones );
    setRootMatrixPalette( &sortedBones );
    setBMVPMatrixPalette( &sortedBones, queue );
    setBMVMatrixPalette( &sortedBones, queue );
  }
}

void AnimationPlayer::bindNullAnimation(RenderQueue* queue)
{
  setMatrixPalette(NULL);
  setRootMatrixPalette(NULL);
  setBMVPMatrixPalette(NULL, queue);
  setBMVMatrixPalette(NULL, queue);
}

void AnimationPlayer::setBMVPMatrixPalette(ArrayList* sortedBones, RenderQueue* queue)
{
  int nUniBMVPMatrixPalette = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_BMVP_MATRIX_PALETTE );

  if( nUniBMVPMatrixPalette >= 0 ) {
    Matrix mvp;

	queue->getModelViewProjection( &mvp );
    
    if( !sortedBones){
      // MVPを全ボーン行列に設定する
      for( int j = 0, arrlen = GLBase::get()->getMaxPaletteMatrices() * 16; j < arrlen; j += 16 ) {
	    memcpy( &matrixPalette[ j ], mvp.getMatrixPointer(), sizeof( float ) * 16 );
      }
    }

    else {
      // ソート済ボーン行列を投げる
      for( int b=0; b<sortedBones->getSize(); b++ ){
        Bone* bone = (Bone*) sortedBones->get(b);

        // BMVPを計算
        Matrix bmvp;
        bmvp.copyFrom(&mvp);
        if( bone ) bmvp.multiply(bone->getFinalMatrix());

        memcpy( &matrixPalette[ b * 16 ], bmvp.getMatrixPointer(), sizeof( float ) * 16 );
      }
    }

    // シェーダUniformのサイズ取得.
    int uniSize = GLBase::get()->getShaderTable()->getCPUniformSize( ShaderProgram::IUNIFORM_BMVP_MATRIX_PALETTE );

    if( (uniSize > 0) && (uniSize <= GLBase::get()->getMaxPaletteMatrices()) ) {
      GLOP( glUniformMatrix4fv( nUniBMVPMatrixPalette, uniSize, false, matrixPalette ) );
    }
  }
}

void AnimationPlayer::setRootMatrixPalette(ArrayList* sortedBones)
{
  int nUniRootMatrixPalette = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_ROOT_MATRIX_PALETTE );

  if( nUniRootMatrixPalette >= 0 ) {
    if( !sortedBones ) {
      // Identityを全ボーン行列に設定する
      Matrix identity;
      identity.setIdentity();

      for( int j = 0, arrlen = GLBase::get()->getMaxPaletteMatrices() * 12; j < arrlen; j += 12 ) {
        memcpy( &matrixPalette[ j ], identity.getMatrixPointer(), sizeof( float ) * 12 );
      }
    }

    else {
      // ソート済ボーン行列を投げる
      for( int b=0; b<sortedBones->getSize(); b++ ){
        Bone* bone = (Bone*) sortedBones->get(b);

        bone->getFinalMatrix()->to3x4(&matrixPalette[ b * 12 ]);
      }
    }

    // シェーダUniformのサイズ取得.
    int uniSize = GLBase::get()->getShaderTable()->getCPUniformSize( ShaderProgram::IUNIFORM_ROOT_MATRIX_PALETTE );

    if( (uniSize > 0) && (uniSize <= GLBase::get()->getMaxPaletteMatrices()*3) ) {
      GLOP( glUniform4fv( nUniRootMatrixPalette, uniSize, matrixPalette ) );
    }
  }
}

void AnimationPlayer::setMatrixPalette(ArrayList* sortedBones)
{
  int nUniMatrixPalette = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_MATRIX_PALETTE );

  if( nUniMatrixPalette >= 0 ) {
    if( !sortedBones ) {
      // Identityを全ボーン行列に設定する
      Matrix identity;
      identity.setIdentity();

      for( int j = 0, arrlen = GLBase::get()->getMaxPaletteMatrices() * 12; j < arrlen; j += 12 ) {
        memcpy( &matrixPalette[ j ], identity.getMatrixPointer(), sizeof( float ) * 12 );
      }
    }

    else {
      // ソート済ボーン行列を投げる
      for( int b=0; b<sortedBones->getSize(); b++ ){
        Bone* bone = (Bone*) sortedBones->get(b);

        bone->getFinalMatrix()->to3x4(&matrixPalette[ b * 12 ]);
      }
    }

    // シェーダUniformのサイズ取得.
    int uniSize = GLBase::get()->getShaderTable()->getCPUniformSize( ShaderProgram::IUNIFORM_MATRIX_PALETTE );

    if( (uniSize > 0) && (uniSize <= GLBase::get()->getMaxPaletteMatrices()*3) ) {
      GLOP( glUniform4fv( nUniMatrixPalette, uniSize, matrixPalette ) );
    }
  }
}

void AnimationPlayer::setBMVMatrixPalette(ArrayList* sortedBones, RenderQueue* queue)
{
  int nUniBMVMatrixPalette = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_BMV_MATRIX_PALETTE );
  Matrix *mv = queue->getModelView();

  if( nUniBMVMatrixPalette >= 0 ) {
    if( !sortedBones){
      // MVPを全ボーン行列に設定する
      for( int j = 0, arrlen = GLBase::get()->getMaxPaletteMatrices() * 12; j < arrlen; j += 12 ) {
        memcpy( &matrixPalette[ j ], mv->getMatrixPointer(), sizeof( float ) * 12 );
      }
    }

    else {
      // ソート済ボーン行列を投げる
      for( int b=0; b<sortedBones->getSize(); b++ ){
        Bone* bone = (Bone*) sortedBones->get(b);

        // BMVPを計算
        Matrix bmv;
        bmv.copyFrom(mv);
        if( bone ) bmv.multiply(bone->getFinalMatrix());

        memcpy( &matrixPalette[ b * 12 ], bmv.getMatrixPointer(), sizeof( float ) * 12 );
      }
    }

    // シェーダUniformのサイズ取得.
    int uniSize = GLBase::get()->getShaderTable()->getCPUniformSize( ShaderProgram::IUNIFORM_BMV_MATRIX_PALETTE );

    if( (uniSize > 0) && (uniSize <= GLBase::get()->getMaxPaletteMatrices()*3) ) {
      GLOP( glUniform4fv( nUniBMVMatrixPalette, uniSize, matrixPalette ) );
    }
  }
}

void AnimationPlayer::play( PlayMode playMode, int addStartTimeMS )
{
  this->playMode = playMode;
  timer.start( addStartTimeMS );
}

void AnimationPlayer::replay()
{
  timer.start();
}

void AnimationPlayer::stop()
{
  timer.stop();
}

void AnimationPlayer::rewind()
{
  timer.stop();
  timer.reset();
}

 /**
  * @brief アニメーションを無設定状態にする
  */
void AnimationPlayer::unsetAnimation() {
  this->animation = NULL;
}

/**
 * @brief アニメーションが再生中か
 */
bool AnimationPlayer::isPlaying() {
  if( !timer.isRunning() || (animation == NULL) ) {
    return false;
  }

  if( playMode == LOOP ) {
    return true;
  }

  int framePosition = (int)(timer.getTime() * animation->getFps() / 1000);

  return (framePosition < animation->getLength());
}

/**
  * 更新
  */
void AnimationPlayer::update( struct timeval &currentTimev ) {
	timer.update( currentTimev );
}

/**
 * 親プレイヤーと同じタイミングで再生を開始する
 */
void AnimationPlayer::playSync( PlayMode playMode, IAnimationPlayer *parent ) {
  AnimationPlayer *p = (AnimationPlayer *)parent;

  this->playMode = playMode;
  timer.startSync( &p->timer );
}

/**
 * 親プレイヤーと同じタイミングで再生を停止する
 */
void AnimationPlayer::stopSync( IAnimationPlayer *parent ) {
  AnimationPlayer *p = (AnimationPlayer *)parent;

  timer.stopSync( &p->timer );
}

/**
 * 親プレイヤーと同じタイミングで再開する
 */
void AnimationPlayer::replaySync( IAnimationPlayer *parent ) {
  AnimationPlayer *p = (AnimationPlayer *)parent;

  timer.startSync( &p->timer );
}
