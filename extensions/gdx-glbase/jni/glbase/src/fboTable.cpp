/**
 * @file fboTable.cpp
 * @brief Module for management of fbo resources implementation
 **/

#include <stdio.h>
#include <string.h>

#include "glbase.h"
#include "fboTable.h"
#include "framebuffer.h"

FBOTable::FBOTable()
{
  nextAvailable = 0;

  memset(fbos, 0, sizeof(fbos));
}

int FBOTable::createFBO()
{
  Framebuffer* newFbo = new Framebuffer();
  if( newFbo == NULL || !newFbo->initRenderEnv() ) {
    GLBase::get()->doetrace("Could not initialize FBO");
    delete newFbo;
    return -1;
  }

  int res = nextAvailable;
  fbos[res] = newFbo;

  while( fbos[++nextAvailable] != NULL ){
    nextAvailable = (nextAvailable + 1) % MAX_FBO;
  }

  return res;  
}

/**
 * 指定のFBO取得する
 * @return 指定のFBOが存在する場合はそのFBO、存在しない場合はNULL
 **/
Framebuffer* FBOTable::getFramebuffer(int fboID)
{
    return fbos[fboID];
}
