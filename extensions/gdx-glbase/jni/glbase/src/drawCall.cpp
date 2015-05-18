/**
 * @file drawCall.cpp
 * @brief A wrapper class for rendering options
 **/

#include <stdio.h>
#include <string.h>

#include "drawCall.h"
#include "polygonMap.h"
#include "glbase.h"
#include "macros.h"
#include "animationPlayer.h"
#include "arrayList.h"
#include "renderQueue.h"
#include "object.h"
#include "polygonMap.h"
#include "layer.h"

void DrawCall::set( IPolygonMap* polygonMap )
{
  renderTarget.polygonMap = polygonMap;
  renderType = TYPE_PMP;
  
  setDefault();
}

void DrawCall::set( RenderEnums::ClearMode mode, float* clearColor )
{
  renderTarget.clear.mode = mode;
  memcpy(renderTarget.clear.color, clearColor, sizeof(float)*4);
  renderType = TYPE_CLR;

  setDefault();
}

void DrawCall::set( float* box )
{
  memcpy(renderTarget.box, box, sizeof(renderTarget.box));
  renderType = TYPE_BOX;

  setDefault();
}

void DrawCall::set( int numParticles )
{
  renderTarget.numParticles = numParticles;
  renderType = TYPE_PRT;

  setDefault();
}

void DrawCall::setFullScreen()
{
  renderType = TYPE_FUL;

  setDefault();
}

void DrawCall::setDefault()
{
  modelTransform.setIdentity();
  shader = 0;
  texture = -1;
  framebuffer = -1;
  animationPlayer = NULL;
  blendSrcAlpha = false;
  blendMode = RenderEnums::BLENDMODE_DEFAULT;
  cullingMode = RenderEnums::CULLING_DEFAULT;
  useDepthTest = true;
  depthFunc = RenderEnums::DEPTHFUNC_LEQUAL;
  depthMask = true;
  for( int i=0; i<4; i++ ) colorMask[i] = true;
  myUniforms.clear();
}

void DrawCall::set( DrawCall *drawCall ) {
  //コピー
  renderType = drawCall->renderType;

  switch( renderType ) {
  case TYPE_PMP:
    renderTarget.polygonMap = drawCall->renderTarget.polygonMap;
    break;
  case TYPE_BOX:
    memcpy( renderTarget.box, drawCall->renderTarget.box, sizeof( float ) * 6 );
    break;
  case TYPE_PRT:
    renderTarget.numParticles = drawCall->renderTarget.numParticles;
	break;
  case TYPE_CLR:
    memcpy( renderTarget.clear.color, drawCall->renderTarget.clear.color, sizeof( float ) * 4 );
    renderTarget.clear.mode = drawCall->renderTarget.clear.mode;
	break;
  default:
	  ;
  }

  modelTransform.copyFrom( &drawCall->modelTransform );
  shader = drawCall->shader;
  texture = drawCall->texture;
  framebuffer = drawCall->framebuffer;
  animationPlayer = drawCall->animationPlayer;
  blendSrcAlpha = drawCall->blendSrcAlpha;
  blendMode = drawCall->blendMode;
  cullingMode = drawCall->cullingMode;
  useDepthTest = drawCall->useDepthTest;
  depthFunc = drawCall->depthFunc;
  depthMask = drawCall->depthMask;
  memcpy( colorMask, drawCall->colorMask, sizeof( bool ) * 4 );
  myUniforms = drawCall->myUniforms;
}

void DrawCall::clear()
{
  if( renderTarget.clear.mode != RenderEnums::CLEARMODE_NONE ) {
    // デプスマスク
    GLBase::get()->getBinder()->setDepthMask( depthMask );

    // カラーマスク
    GLBase::get()->getBinder()->setColorMask( colorMask );

    // クリア
    if( renderTarget.clear.mode == RenderEnums::CLEARMODE_DEPTH_ONLY ) { 
      GLOP( glClear( GL_DEPTH_BUFFER_BIT ) );
    }
    else {
      float *color = renderTarget.clear.color;
      GLOP( glClearColor( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] ) );

      if( renderTarget.clear.mode == RenderEnums::CLEARMODE_COLOR_ONLY ) {
	GLOP( glClear( GL_COLOR_BUFFER_BIT ) );
      }
      else {
	GLOP( glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT ) );
      }
    }
  }
}

void DrawCall::execRender(RenderQueue* queue)
{
  // Bind target render surface
  if( framebuffer >= 0 ) {
    GLBase::get()->getBinder()->bindFBO(framebuffer);
  } else {
    GLBase::get()->getBinder()->unbindFBO(queue);
  }

  // Clear?
  if( renderType == TYPE_CLR ){
    clear();
    return;
  }

  // シェーダー
  GLBase::get()->getBinder()->bindProgram(shader);

  // ブレンドモード
  GLBase::get()->getBinder()->setBlendMode( blendSrcAlpha, blendMode );

  // カーリング
  GLBase::get()->getBinder()->setCullingMode( cullingMode );

  // デプス関数
  GLBase::get()->getBinder()->setDepthTest( useDepthTest, depthFunc );

  // デプスマスク
  GLBase::get()->getBinder()->setDepthMask( depthMask );

  // カラーマスク
  GLBase::get()->getBinder()->setColorMask( colorMask );

  // モデル変換を掛け合わせる
  queue->getMVStack()->push( &modelTransform );

  // Bind texture
  bindTexture();

  // Bind model transform
  bindModelTransform();

  // Bind uniforms
  queue->bindUniforms( &myUniforms, GLBase::get()->getViewport(framebuffer) );

  // ターゲットモデルのレンダー
  switch( renderType ) {
  case TYPE_PMP:      execRenderPolygonMap(queue, (PolygonMap*)renderTarget.polygonMap); break;
  case TYPE_BOX:      execRenderBBox(queue, renderTarget.box); break;
  case TYPE_PRT:      execRenderParticle(queue, renderTarget.numParticles); break;
  case TYPE_FUL:      execRenderFullScreen(queue); break;
  }

  queue->getMVStack()->pop();
}

void DrawCall::execRenderPolygonMap(RenderQueue* queue, PolygonMap* pm)
{
  // アニメーションをバインド
  AnimationPlayer* ap = (AnimationPlayer*)animationPlayer;

  if( ap != NULL ){
    ap->bind(queue);
  }
  else{
    AnimationPlayer::bindNullAnimation(queue);
  }
  
  // Surface
  pm->bindSurface();
  pm->execRender();
}

void DrawCall::execRenderFullScreen(RenderQueue* queue)
{
  // アニメなし
  AnimationPlayer::bindNullAnimation(queue);
  
  // レンダー
  GLBase::get()->getShapeRenderer()->renderFullScreen();
}

void DrawCall::execRenderBBox(RenderQueue* queue, float* box)
{
  // アニメなし
  AnimationPlayer::bindNullAnimation(queue);

  // レンダー
  GLBase::get()->getShapeRenderer()->renderBBox( box );
}

void DrawCall::execRenderParticle(RenderQueue* queue, int numParticles)
{
  // アニメなし
  AnimationPlayer::bindNullAnimation(queue);
  
  // レンダー
  GLBase::get()->getShapeRenderer()->renderParticles( numParticles );
}

void DrawCall::bindTexture()
{
  // 直接指定が最優先
  if( texture != -1 ){
    GLBase::get()->getBinder()->bindTexture( texture );
  }
  else {
    GLBase::get()->getBinder()->unbindTexture();
  }
}

void DrawCall::bindModelTransform() {
	int nUniModelMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation( ShaderProgram::IUNIFORM_MODEL_MATRIX );

	if( nUniModelMatrix >= 0 ) {
		float* safModelMatrix = modelTransform.getMatrixPointer();

		GLOP( glUniformMatrix4fv( nUniModelMatrix, 1, GL_FALSE, safModelMatrix ) );
	}
}

