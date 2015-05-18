/**
 * @file binder.cpp
 * @brief State machine that mantains current bind informations (vbufs, textures, shaders...)
 **/

#include "binder.h"
#include "glbase.h"
#include "macros.h"
#include "polygonBuffer.h"
#include "renderQueue.h"
#include "vertexBuffer.h"

#include <string.h>

Binder::Binder()
{
  unbindAllBuffers();

  curTexture = -1;
  curFBO = NULL;
  screenFBO = -1;
  bindedProgram = -1;

  useBlend = false;
  blendSrcAlpha = false;
  blendFunc = RenderEnums::BLENDFUNC_ALPHA_BLEND;
  blendEq = RenderEnums::BLENDEQ_ADD;

  cullingMode = RenderEnums::CULLING_OFF;

  useDepthTest = true;
  depthFunc = RenderEnums::DEPTHFUNC_LEQUAL;

  useDepthMask = true;

  colorMask[0] = true;
  colorMask[1] = true;
  colorMask[2] = true;
  colorMask[3] = true;

  memset( viewport, 0, sizeof( viewport ) );

  setForceSetFlags();
}

void Binder::bindBuffer(VertexBuffer* buffer)
{
  ShaderProgram::INDEX_ATTRIBUTES att = buffer->getShaderAttribute();
  if( curVertexBuffers[att] == buffer ) return;

  if( buffer->bind() ) {
    curVertexBuffers[att] = buffer;
  }
}

void Binder::unbindBuffer(ShaderProgram::INDEX_ATTRIBUTES attribute)
{
  if( curVertexBuffers[attribute] != NULL ){
    curVertexBuffers[attribute]->unbind();
    curVertexBuffers[attribute] = NULL;
  }
}

void Binder::bindPolygons( PolygonBuffer *buffer ) {
  if( curPolygonBuffer != buffer )  {
    curPolygonBuffer = buffer;
    buffer->bind();
  }
}

void Binder::unbindPolygons() {
  if( curPolygonBuffer != NULL ) {
    curPolygonBuffer = NULL;
    GLOP( glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 ) );
  }
}

void Binder::unbindAllBuffers()
{
  for( int i=0; i<ShaderProgram::IATTRIBUTE_MAX; i++) {
	  curVertexBuffers[i] = NULL;
  }

  curPolygonBuffer = NULL;
}

void Binder::bindTexture(int textureID)
{
  if( curTexture == textureID ) return;

  if( GLBase::get()->getTextureTable()->bind(textureID) ) {
    curTexture = textureID;
  }
}

void Binder::unbindTexture()
{
  if( curTexture != -1 ){
    // テクスチャー0を有効
    GLOP( glActiveTexture( GL_TEXTURE0 ) );

    //バインド解除
    GLOP( glBindTexture(GL_TEXTURE_2D, 0 ) );
    curTexture = -1;
  }
}

void Binder::resetTexture() {
  curTexture = -1;
}

void Binder::bindFBO( int framebufferID ) {
  if( framebufferID >= 0 ) {
    Framebuffer *fbo = GLBase::get()->getFBOTable()->getFramebuffer( framebufferID );

    if( fbo != NULL ) {
      bindFBO( fbo );
    }
  }
}

void Binder::bindFBO( Framebuffer *fbo ) {
  if( fbo == curFBO ) {
    // Set viewport
    setViewport( fbo->getViewport() );
    return;
  }

  // Save screen!
  if( screenFBO == -1 ) {
    GLOP( glGetIntegerv( GL_FRAMEBUFFER_BINDING, &screenFBO ) );
  }
  
  // Bind fbo
  fbo->bind();
  curFBO = fbo;

  // Set viewport
  setViewport( fbo->getViewport() );
}

void Binder::unbindFBO(RenderQueue* queue)
{
  if( curFBO != NULL ){
    GLOP( glBindFramebuffer( GL_FRAMEBUFFER, screenFBO ) );
    curFBO = NULL;

    // Set viewport
    setViewport( GLBase::get()->getViewport( -1 ) );
  }
}

bool Binder::bindProgram(int shaderId)
{
  // 同じシェーダーを2回バインドしない
  if( shaderId == bindedProgram ) return true;

  ShaderProgram* program = GLBase::get()->getShaderTable()->getShaderProgram(shaderId);
  if( program == NULL ) return false;

  // 新しいプログラムをバインド
  program->bind();
  bindedProgram = shaderId;
  return true;
}

void Binder::resetProgram()
{
	bindedProgram = -1;
}


void Binder::initialize()
{
  // デプス
  GLOP( glEnable(GL_DEPTH_TEST) );
  GLOP( glDepthFunc(GL_LEQUAL) );
  
  // ブレンド
  GLOP( glDisable(GL_BLEND) );
  GLOP( glBlendFunc( GL_ONE, GL_ZERO ) );

  // Culling
  GLOP( glDisable(GL_CULL_FACE) );
  
  // デプスマスク
  GLOP( glDepthMask(GL_TRUE) );

  // Init viewport
  setViewport( GLBase::get()->getViewport( -1 ) );
}

void Binder::setBlendMode(bool blendSrcAlpha, RenderEnums::BlendMode blendMode)
{
  if( (blendMode >= RenderEnums::BLENDMODE_DONOTHING)
      && (blendMode < RenderEnums::BLENDMODE_MAX) ) {
    switch( blendMode ) {
    case RenderEnums::BLENDMODE_DONOTHING:		
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_DONOTHING ); break;
    case RenderEnums::BLENDMODE_ALPHA_BLEND:	
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_ALPHA_BLEND ); break;
    case RenderEnums::BLENDMODE_ADD:			
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_ADD ); break;
    case RenderEnums::BLENDMODE_ADDNALPHA:		
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_ADDNALPHA ); break;
    case RenderEnums::BLENDMODE_MULTIPLY:		
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_MULTIPLY ); break;
    case RenderEnums::BLENDMODE_NEGATION:		
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_NEGATION ); break;
    case RenderEnums::BLENDMODE_SCREEN:			
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_SCREEN ); break;
    case RenderEnums::BLENDMODE_SUBTRACT:		
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_SUBTRACT ); break;
    case RenderEnums::BLENDMODE_SUBTRACTNALPHA:	
      setBlendFunc( blendSrcAlpha, RenderEnums::BLENDFUNC_SUBTRACTNALPHA ); break;
    default:	break;
    }
    
    if( (blendMode == RenderEnums::BLENDMODE_SUBTRACT) 
	|| (blendMode == RenderEnums::BLENDMODE_SUBTRACTNALPHA) ) {
      setBlendEq( RenderEnums::BLENDEQ_REV_SUBTRACT );
    } else {
      setBlendEq( RenderEnums::BLENDEQ_ADD );
    }
    
    setUseBlend( true );
  } else {
    setUseBlend( false );
  }
}

/*!
 * @brief	ブレンド設定.
 */
void Binder::setUseBlend( bool useBlend ) {
  if( (this->useBlend != useBlend) || forceSetUseBlend ) {
    if( useBlend ) {
      GLOP( glEnable( GL_BLEND ) );
    } else {
      GLOP( glDisable( GL_BLEND ) );
    }
    
    this->useBlend = useBlend;
	forceSetUseBlend = false;
  }
}

/*!
 * @brief	ブレンド関数設定.
 */
void Binder::setBlendFunc( bool blendSrcAlpha, RenderEnums::BlendFunc blendFunc ) {
  if( (this->blendSrcAlpha != blendSrcAlpha) || (this->blendFunc != blendFunc) || !this->useBlend || forceSetBlendFunc ) {
    GLenum srcRGB;
    GLenum dstRGB;
    
    switch( blendFunc ) {
    case RenderEnums::BLENDFUNC_DONOTHING:		
      srcRGB = GL_ZERO; dstRGB = GL_ONE; break;
    case RenderEnums::BLENDFUNC_ALPHA_BLEND:	
      srcRGB = GL_SRC_ALPHA; dstRGB = GL_ONE_MINUS_SRC_ALPHA; break;
    case RenderEnums::BLENDFUNC_ADD:			
      srcRGB = GL_ONE; dstRGB = GL_ONE; break;
    case RenderEnums::BLENDFUNC_ADDNALPHA:		
      srcRGB = GL_SRC_ALPHA; dstRGB = GL_ONE; break;
    case RenderEnums::BLENDFUNC_MULTIPLY:		
      srcRGB = GL_DST_COLOR; dstRGB = GL_ZERO; break;
    case RenderEnums::BLENDFUNC_NEGATION:		
      srcRGB = GL_ONE_MINUS_DST_COLOR; dstRGB = GL_ZERO; break;
    case RenderEnums::BLENDFUNC_SCREEN:			
      srcRGB = GL_ONE_MINUS_DST_COLOR; dstRGB = GL_ONE; break;
    case RenderEnums::BLENDFUNC_SUBTRACT:		
      srcRGB = GL_ONE; dstRGB = GL_ONE; break;
    case RenderEnums::BLENDFUNC_SUBTRACTNALPHA:	
      srcRGB = GL_SRC_ALPHA; dstRGB = GL_ONE; break;
    default:	break;
    }
    
    if( blendSrcAlpha ) {
      GLOP( glBlendFuncSeparate( srcRGB, dstRGB, GL_ONE, GL_ONE ) );
    } else {
      GLOP( glBlendFuncSeparate( srcRGB, dstRGB, GL_ZERO, GL_ONE ) );
    }
    
    this->blendSrcAlpha = blendSrcAlpha;
    this->blendFunc = blendFunc;
	forceSetBlendFunc = false;
  }
}

/*!
 * @brief	ブレンド式設定.
 */
void Binder::setBlendEq( RenderEnums::BlendEq blendEq ) {
  if( (this->blendEq != blendEq) || !this->useBlend || forceSetBlendEq ) {
    switch( blendEq ) {
    case RenderEnums::BLENDEQ_ADD:	        GLOP( glBlendEquation( GL_FUNC_ADD ) ); break;
    case RenderEnums::BLENDEQ_SUBTRACT:		GLOP( glBlendEquation( GL_FUNC_SUBTRACT ) ); break;
    case RenderEnums::BLENDEQ_REV_SUBTRACT:	GLOP( glBlendEquation( GL_FUNC_REVERSE_SUBTRACT ) ); break;
    default:	break;
    }
    
    this->blendEq = blendEq;
	forceSetBlendEq = false;
  }
}

/**
 * ビューポート設定
 */
void Binder::setViewport( int viewport[ 4 ] ) {
	if(
			(this->viewport[ 0 ] != viewport[ 0 ]) ||
			(this->viewport[ 1 ] != viewport[ 1 ]) ||
			(this->viewport[ 2 ] != viewport[ 2 ]) ||
			(this->viewport[ 3 ] != viewport[ 3 ]) ||
			forceSetViewport ) {
		GLOP( glViewport( viewport[ 0 ], viewport[ 1 ], viewport[ 2 ], viewport[ 3 ] ) );
		this->viewport[ 0 ] = viewport[ 0 ];
		this->viewport[ 1 ] = viewport[ 1 ];
		this->viewport[ 2 ] = viewport[ 2 ];
		this->viewport[ 3 ] = viewport[ 3 ];
		forceSetViewport = false;
	}
}

/**
 * Cullingモードを設定
 **/
void Binder::setCullingMode(RenderEnums::CullingMode cullingMode)
{
  if( cullingMode == RenderEnums::CULLING_DEFAULT ){
    cullingMode = RenderEnums::CULLING_BACK;
  }

  if( (cullingMode != this->cullingMode) || forceSetCullingMode ) {
    
    // on/off
    if( cullingMode != RenderEnums::CULLING_OFF ){
      GLOP( glEnable( GL_CULL_FACE ) );
      switch( cullingMode ) {
      case RenderEnums::CULLING_BACK:           GLOP( glCullFace( GL_BACK ) ); break;
      case RenderEnums::CULLING_FRONT:          GLOP( glCullFace( GL_FRONT ) );break;
      case RenderEnums::CULLING_FRONT_AND_BACK: GLOP( glCullFace( GL_FRONT_AND_BACK ) ); break;
      default: break;
      }
    }
    else{
      GLOP( glDisable( GL_CULL_FACE ) );
    }

    this->cullingMode = cullingMode;
	forceSetCullingMode = false;
  }
}

void Binder::setDepthTest(bool useDepthTest, RenderEnums::DepthFunc depthFunc)
{
  // デプステスト
      // デプス関数
  if( (depthFunc != this->depthFunc) || (useDepthTest && !this->useDepthTest) || forceSetDepthFunc ) {
    GLenum func;

    switch( depthFunc ){
    case RenderEnums::DEPTHFUNC_NEVER:    func = GL_NEVER; break;
    case RenderEnums::DEPTHFUNC_LESS:     func = GL_LESS; break;
    case RenderEnums::DEPTHFUNC_EQUAL:    func = GL_EQUAL; break;
    case RenderEnums::DEPTHFUNC_LEQUAL:   func = GL_LEQUAL; break;
    case RenderEnums::DEPTHFUNC_GREATER:  func = GL_GREATER; break;
    case RenderEnums::DEPTHFUNC_NOTEQUAL: func = GL_NOTEQUAL; break;
    case RenderEnums::DEPTHFUNC_GEQUAL:   func = GL_GEQUAL; break;
    case RenderEnums::DEPTHFUNC_ALWAYS:   func = GL_ALWAYS; break;
    default: break;
    }

    GLOP( glDepthFunc( func ) );
    this->depthFunc = depthFunc;
	forceSetDepthFunc = false;
  }

  if( (useDepthTest != this->useDepthTest) || forceSetUseDepthTest ) {
    if( useDepthTest ){
      GLOP( glEnable(GL_DEPTH_TEST) );
    }
    else {
      GLOP( glDisable(GL_DEPTH_TEST) );
    }
    this->useDepthTest = useDepthTest;
	forceSetUseDepthTest = false;
  }
}

void Binder::setDepthMask(bool useDepthMask)
{
  if( (useDepthMask != this->useDepthMask) || forceSetDepthMask ) {
    if( useDepthMask ){
      GLOP( glDepthMask( GL_TRUE ) );
    }
    else{
      GLOP( glDepthMask( GL_FALSE ) );
    }

    this->useDepthMask = useDepthMask;
	forceSetDepthMask = false;
  }
}

void Binder::setColorMask(bool* colorMask)
{
  if( (colorMask[ 0 ] != this->colorMask[ 0 ]) ||
      (colorMask[ 1 ] != this->colorMask[ 1 ]) ||
      (colorMask[ 2 ] != this->colorMask[ 2 ]) ||
      (colorMask[ 3 ] != this->colorMask[ 3 ]) ||
      forceSetColorMask ) {
    for( int i = 0; i < 4; i++ ) {	
      this->colorMask[ i ] = colorMask[ i ];
    }
  
    GLOP( glColorMask(
      (colorMask[ 0 ] ? GL_TRUE : GL_FALSE),
      (colorMask[ 1 ] ? GL_TRUE : GL_FALSE),
      (colorMask[ 2 ] ? GL_TRUE : GL_FALSE),
      (colorMask[ 3 ] ? GL_TRUE : GL_FALSE) ) );
	forceSetColorMask = false;
  }
}


int Binder::getCurrentProgram()
{
  return bindedProgram;
}

void Binder::setForceSetFlags() {
  forceSetUseBlend = true;
  forceSetBlendFunc = true;
  forceSetBlendEq = true;
  forceSetCullingMode = true;
  forceSetUseDepthTest = true;
  forceSetDepthFunc = true;
  forceSetDepthMask = true;
  forceSetColorMask = true;
  forceSetViewport = true;
}
