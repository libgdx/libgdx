/**
 * @file framebuffer.cpp
 * @brief Framebuffer module implementation
 **/

#include "glbase.h"
#include "framebuffer.h"
#include "macros.h"
#include "texture.h"

#include <stdlib.h>

Framebuffer::Framebuffer()
{
  glid = -1;
  depthRenderBuffer = -1;
}

Framebuffer::~Framebuffer()
{
  if( glid != -1 ) uninitRenderEnv();
}

bool Framebuffer::initRenderEnv()
{
  GLOP( glGenFramebuffers( 1, &glid ) );
  return true;
}

void Framebuffer::uninitRenderEnv()
{
  GLOP( glDeleteFramebuffers( 1, &glid ) );

  if( depthRenderBuffer != -1 ) {
    GLOP( glDeleteRenderbuffers( 1, &depthRenderBuffer ) );
    depthRenderBuffer = -1;
  }
}

void Framebuffer::bind()
{
  GLOP( glBindFramebuffer( GL_FRAMEBUFFER, glid ) );
}

void Framebuffer::setTexture(Texture* texture, bool createDepth)
{
  // Set the viewport to texture bounds
  setViewport(0, 0, texture->getWidth(), texture->getHeight());

  //処理効率化のため、GLBaseのBinder経由でバインド
  GLBase::get()->getBinder()->bindFBO( this );
  GLBase::get()->getBinder()->resetTexture();

  GLOP( glBindTexture( GL_TEXTURE_2D, texture->glid ) );
  GLOP( glFramebufferTexture2D( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, 
				GL_TEXTURE_2D, texture->glid, 0 ) );

  if( glCheckFramebufferStatus( GL_FRAMEBUFFER ) != GL_FRAMEBUFFER_COMPLETE ) {
    etrace( "failed to make complete framebuffer with texture glid: %d, error: %x", 
		texture->glid, glCheckFramebufferStatus( GL_FRAMEBUFFER ) );
    return;
  }

  if( depthRenderBuffer != -1 ) {
    GLOP( glDeleteRenderbuffers( 1, &depthRenderBuffer ) );
    depthRenderBuffer = -1;
  }

  if( createDepth ) {
    GLOP( glGenRenderbuffers( 1, &depthRenderBuffer ) );
    GLOP( glBindRenderbuffer( GL_RENDERBUFFER, depthRenderBuffer ) );
    GLOP( glRenderbufferStorage( GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, texture->getWidth(), texture->getHeight() ) );
    GLOP( glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBuffer ) );

    if( glCheckFramebufferStatus( GL_FRAMEBUFFER ) != GL_FRAMEBUFFER_COMPLETE ) {
      etrace( "failed to make complete framebuffer object %x", 
		    glCheckFramebufferStatus( GL_FRAMEBUFFER ) );
      return;
    }
  }
  
  // Clear once
  GLOP( glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) );
  GLOP( glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT ) );
  
  // Unbind
  GLOP( glBindTexture( GL_TEXTURE_2D, 0 ) );
}

void Framebuffer::setViewport(int l, int t, int r, int b)
{
  viewport[0] = l;
  viewport[1] = t;
  viewport[2] = r;
  viewport[3] = b;
}

int* Framebuffer::getViewport()
{
  return viewport;
}
