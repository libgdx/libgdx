/**
 * @file mrf.cpp
 * @brief Mrf resource implementation
 **/

#include "glbase.h"
#include "mrf.h"
#include "fileUtils.h"
#include "macros.h"
#include "drawCall.h"
#include "object.h"
#include "layer.h"
#include "polygonMap.h"
#include "renderPass.h"
#include "renderQueue.h"

#include <string.h>

using namespace std;

// Global mrf system fbos
#define MAX_OFFSCREEN_TEXTURES 15
#define OFFSCREEN_FBO_SIZE_DIV 4
#define MAX_POSTPROCESS_TEXTURES 4

const int POSTPROCESS_FBO_SIZE_DIV[ MAX_POSTPROCESS_TEXTURES ] = { 8, 8, 2, 2 };
const bool POSTPROCESS_FBO_DEPTH[ MAX_POSTPROCESS_TEXTURES ] = { true, false, true, false };

// Global frambuffer/textures
int framebuffers[MAX_OFFSCREEN_TEXTURES];
int offscreen_texs[MAX_OFFSCREEN_TEXTURES];
int framebuffersPP[MAX_POSTPROCESS_TEXTURES];
int offscreen_texsPP[MAX_POSTPROCESS_TEXTURES];

BasicRenderParameters::BasicRenderParameters()
{
  modelTransform.setIdentity();
  texture = -1;
  framebuffer = -1;
  animationPlayer = NULL;
}

Mrf::Mrf(const char* mrfFile, bool skipSetUniforms)
{
  renderSettings.setUp(mrfFile);

  if( !skipSetUniforms ) {
    setUniforms();
  }
}

void Mrf::setUniforms() {
  mrfUniforms.clear();

  if( renderSettings.getPasses()->getSize() > 0 ) {
    ArrayList* passes = renderSettings.getPasses();

    for( int j = 0; j < passes->getSize(); j++ ) {
      RenderPass *curPass = (RenderPass*)passes->get( j );
      ArrayList* uniforms = curPass->getUniforms();
      int shaderid = GLBase::get()->getShaderTable()->getShaderProgramID(curPass->getShaderName());
      ShaderProgram* program = GLBase::get()->getShaderTable()->getShaderProgram(shaderid);
      trace("SHADER %s %d", curPass->getShaderName(), shaderid);

      for( int i = 0; i < uniforms->getSize(); i++ ) {
  	    Uniform *uniform = (Uniform*)uniforms->get( i );
        char *uniformName = uniform->getName();
        int id = program->getMyUniformID (uniformName);

        // If myUniform is not loaded by user, load id!
        if( id < 0 ){
          id = program->getAvailableMyUniformID();
		  program->setMyUniform( id, uniformName );
        }

        trace("UNIFORM %s %d", uniformName, id);

        // Save the uniform id and value for god's sake
		MrfUniform mrfUniform;

        mrfUniform.pass = curPass;
        mrfUniform.id = id;
        mrfUniform.u = uniform;
		mrfUniforms.push_back( mrfUniform );
      }
    }
  }
}

Mrf::~Mrf()
{
}

void IMrf::initialize( int screenWidth, int screenHeight )
{
  // Create all fbos and textures
  for( int i=0; i<MAX_OFFSCREEN_TEXTURES; i++ ){
    framebuffers[i] = GLBase::get()->createFBO();
    offscreen_texs[i] = 
      GLBase::get()->createTexture(screenWidth/OFFSCREEN_FBO_SIZE_DIV,
        screenHeight/OFFSCREEN_FBO_SIZE_DIV,
        GL_RGBA,
        GL_UNSIGNED_SHORT_5_5_5_1,
        false);
    if( offscreen_texs[i] == -1 ){
      GLBase::get()->doetrace("Could not create new texture.  Bad format?");
    }

    // Link
    GLBase::get()->setFBOTexture(framebuffers[i], offscreen_texs[i], false);
  }

  for( int i=0; i<MAX_POSTPROCESS_TEXTURES; i++ ){
    framebuffersPP[i] = GLBase::get()->createFBO();
    offscreen_texsPP[i] = 
      GLBase::get()->createTexture(screenWidth/POSTPROCESS_FBO_SIZE_DIV[i],
        screenHeight/POSTPROCESS_FBO_SIZE_DIV[i],
        GL_RGBA,
        GL_UNSIGNED_SHORT_5_5_5_1,
		false);
    if( offscreen_texsPP[i] == -1 ){
      GLBase::get()->doetrace("Could not create new texture.  Bad format?");
    }

    // Link
    GLBase::get()->setFBOTexture(framebuffersPP[i], offscreen_texsPP[i], POSTPROCESS_FBO_DEPTH[i]);
  }
}

 int IMrf::getFramebuffer( int renderTarget ) {
    if( renderTarget >= RenderEnums::TEXTYPE_PP_DIV8_WDEPTH_FBO ) {
      return framebuffersPP[renderTarget - RenderEnums::TEXTYPE_PP_DIV8_WDEPTH_FBO];
	}

    return framebuffers[renderTarget - RenderEnums::TEXTYPE_FBO_1];
 }

Mrf* Mrf::load(const char* mrfFile, bool skipSetUniforms)
{
  return new Mrf(mrfFile, skipSetUniforms);
}

void Mrf::registerDrawCalls(IRenderQueue* queue,
			    BasicRenderParameters* params,
			    IObject* object)
{
  DrawCall* result;
  int calls = prepareDrawCalls(result, params, object);

  for( int i=0; i<calls; i++ ){
    queue->registerDrawCall(&result[i]);
  }
}


int Mrf::prepareDrawCalls(DrawCall* &result, 
			  BasicRenderParameters* params,
			  IObject* object)
{
  result = NULL;
  
  // Calculate target-move map
  MovableFBO movemap[16];
  int movemapNum = 0;
  RenderPass* curPass;
  foreach_element(renderSettings.getPasses(), curPass, RenderPass*){
    
    // If this pass FBO has already been used
    RenderEnums::TextureType target = (RenderEnums::TextureType)curPass->getTarget();
    if( (target > RenderEnums::TEXTYPE_FBO_1) && (target < RenderEnums::TEXTYPE_FBO_15) ){
      int passFBO = framebuffers[target - RenderEnums::TEXTYPE_FBO_1];

      if( GLBase::get()->getFboUsed()[passFBO] ){
        // Register the move
        movemap[movemapNum].from = target;
        movemap[movemapNum++].to = findUnusedFBO(target);
      }
    }
  }
  
  // Register calls
  int totCalls = 0;
  foreach_element(renderSettings.getPasses(), curPass, RenderPass*){
    registerCalls(result, totCalls, params, (Object*)object, curPass,
		  movemap, movemapNum);
  }

  return totCalls;
}

RenderEnums::TextureType Mrf::findUnusedFBO(RenderEnums::TextureType def)
{
  for( int i=def - RenderEnums::TEXTYPE_FBO_1; i<MAX_OFFSCREEN_TEXTURES; i++ ){
    if( !GLBase::get()->getFboUsed()[framebuffers[i]] ) 
		return (RenderEnums::TextureType)(RenderEnums::TEXTYPE_FBO_1 + i);
  }

  return def;
}

void Mrf::registerCalls(DrawCall* &result, int &curPos,
			BasicRenderParameters* params, Object* object, 
			RenderPass* pass, MovableFBO* movemap, int movemapNum)
{
  // Add clear call if needed
  if( pass->getClearMode() != (int)RenderEnums::CLEARMODE_NONE 
      && pass->getTarget() != (int)RenderEnums::TEXTYPE_DEFAULT){
    
    DrawCall* dc = GLBase::get()->getDrawCallsPool()->acquireDrawCall
      ((RenderEnums::ClearMode)pass->getClearMode(), pass->getClearColor());
    if( result == NULL ) result = dc;
    curPos++;
    setupDrawCall(dc, params, pass, movemap, movemapNum);
  }

  // Polygon maps
  switch(pass->getModelType()){
  case RenderEnums::MODELTYPE_DEFAULT: {
    Layer* lay;
    PolygonMap* pm;
    foreach_element( object->getLayers(), lay, Layer* ){
      
      // レイヤー互換性をチェック
      if( strcmp( pass->getLayerName(), "__all_layers" ) != 0 
	  && strcmp( pass->getLayerName(), lay->getName() ) != 0 ) continue;
      
      foreach_element( lay->getPolygonMaps(), pm, PolygonMap* ){
	DrawCall* dc = GLBase::get()->getDrawCallsPool()->acquireDrawCall(pm);
	if( result == NULL ) result = dc;
	curPos++;
	setupDrawCall(dc, params, pass, movemap, movemapNum);
      }
    }
    break;
    }

  case RenderEnums::MODELTYPE_TEXTURE_FULL: {
    DrawCall* dc = GLBase::get()->getDrawCallsPool()->acquireDrawCall();
    if( result == NULL ) result = dc;
    curPos++;
    setupDrawCall(dc, params, pass, movemap, movemapNum);
    break;
  }

  case RenderEnums::MODELTYPE_BBOX: {
    DrawCall* dc = GLBase::get()->getDrawCallsPool()->acquireDrawCall(object->getBoundingBox());
    if( result == NULL ) result = dc;
    curPos++;
    setupDrawCall(dc, params, pass, movemap, movemapNum);
    break;
  }
    
  case RenderEnums::MODELTYPE_PARTICLE: {
    DrawCall* dc = GLBase::get()->getDrawCallsPool()->acquireDrawCall(pass->getParticleCount());
    if( result == NULL ) result = dc;
    curPos++;

	//パーティクルテクスチャはポリゴンマップにて指定されている可能性あり
	ArrayList *layers = object->getLayers();
	Layer *layer;

    foreach_element( layers, layer, Layer * ) {   
      if( strcmp( pass->getLayerName(), layer->getName() ) == 0 ) {
        ArrayList *polygonMaps = layer->getPolygonMaps();

		if( polygonMaps->getSize() > 0 ) {
          PolygonMap *polygonMap = (PolygonMap *)polygonMaps->get( 0 );

		  dc->texture = polygonMap->getSurface()->getTexture();
		}

        break;
	  }
	}

    setupDrawCall(dc, params, pass, movemap, movemapNum);
    break;
  }
  }
}

void Mrf::setupDrawCall(DrawCall* dc, BasicRenderParameters* params, RenderPass* pass,
			MovableFBO* movemap, int movemapNum)
{
  // Calculate the target-shift where needed
  int curFBO = pass->getTarget();
  int curTEX = pass->getTextureType();
  // Search map
  if( (curFBO > RenderEnums::TEXTYPE_FBO_1) && (curFBO < RenderEnums::TEXTYPE_FBO_15) ){
    for( int i=0; i<movemapNum; i++ ){
      if( movemap[i].from == curFBO ){
        curFBO = movemap[i].to;
		break;
      }
    }
  }

  if( (curTEX > RenderEnums::TEXTYPE_FBO_1) && (curTEX < RenderEnums::TEXTYPE_FBO_15) ){
    for( int i=0; i<movemapNum; i++ ){
      if( movemap[i].from == curTEX ){
        curTEX = movemap[i].to;
		break;
      }
    }
  }


  // FBO
  if( curFBO != RenderEnums::TEXTYPE_DEFAULT ){
    dc->framebuffer = getFramebuffer( curFBO );
  } else {
    dc->framebuffer = params->framebuffer;
  }

  // Texture
  if( curTEX != RenderEnums::TEXTYPE_DEFAULT ){
    if( curTEX >= RenderEnums::TEXTYPE_PP_DIV8_WDEPTH_FBO ) {
      dc->texture = offscreen_texsPP[curTEX - RenderEnums::TEXTYPE_PP_DIV8_WDEPTH_FBO];
	} else {
      dc->texture = offscreen_texs[curTEX - RenderEnums::TEXTYPE_FBO_1];
	}
  } else {
	if( params->texture >= 0 ) {
      dc->texture = params->texture;
	} else {
      // If no texture, use pmp texture
      if( dc->renderType == TYPE_PMP ) {
        dc->texture = ((PolygonMap*)dc->renderTarget.polygonMap)->getSurface()->getTexture();
      }
    }
  }
  
  // Other stuffs
  memcpy(dc->modelTransform.getMatrixPointer(), 
	 params->modelTransform.getMatrixPointer(), 
	 sizeof(float)*16);
  dc->shader = GLBase::get()->getShaderTable()->getShaderProgramID( pass->getShaderName() );
  dc->animationPlayer = params->animationPlayer;
  dc->blendSrcAlpha = false;
  dc->blendMode = (RenderEnums::BlendMode)pass->getBlendMode();
  dc->cullingMode = (RenderEnums::CullingMode)pass->getCullingMode();
  dc->useDepthTest = true;
  dc->depthFunc = (RenderEnums::DepthFunc)pass->getDepthFunc();
  dc->depthMask = pass->getDepthMask();
  memcpy(dc->colorMask, pass->getColorMask(), sizeof(bool)*4);

  // My Uniforms
  for( int i=0, len = mrfUniforms.size(); i<len; i++ ){
    if( mrfUniforms[i].pass != pass ) continue;

    // Add the uniform to the map
    dc->myUniforms[mrfUniforms[i].id] = 
      GLBase::get()->acquireMyUniform(mrfUniforms[i].u->getValues()->getEl(),
				      mrfUniforms[i].u->getValues()->getLen());
  }
  
  // Register
  if( pass->getTarget() != RenderEnums::TEXTYPE_DEFAULT ){   
    // Set FBO usage
    GLBase::get()->getFboUsed()[dc->framebuffer] = true;
  } 
}

int Mrf::getPassesNum() {
  return renderSettings.getPasses()->getSize();
}

IRenderPass *Mrf::getPass( int passIndex ) {
  return (IRenderPass *)renderSettings.getPasses()->get( passIndex );
}

char *Mrf::getId() {
  return renderSettings.getId();
}
