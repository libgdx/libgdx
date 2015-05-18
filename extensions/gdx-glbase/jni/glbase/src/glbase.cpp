/**
 * @file glhq.cpp
 * @brief GLHeadQuarter library implementation
 **/

#include <stdio.h>
#include "mrf.h"
#include "glbase.h"
#include "object.h"
#include "mergeGroup.h"
#include "layer.h"
#include "macros.h"
#include "FreeImage.h"
#include "animationPlayer.h"
#include "IpolygonMap.h"
#include "polygonMap.h"
#include "shaderProgram.h"
#include "surface.h"
#include "renderQueue.h"
#include "animation.h"
#include GL2_H

#include <math.h>
#include <string.h>

/**
   FreeImage error handler
   @param fif Format / Plugin responsible for the error 
   @param message Error message
*/
void FreeImageErrorHandler(FREE_IMAGE_FORMAT fif, const char *message) {
  etrace("\n*** "); 
  if(fif != FIF_UNKNOWN) {
    etrace("%s Format\n", FreeImage_GetFormatFromFIF(fif));
  }
  etrace(message);
  etrace(" ***\n");
}


GLBase GLBase::self;


GLBase::GLBase()
{
  tracef = &vprintf;
  etracef = &vprintf;

  maxPaletteMatrices = 30;
}

GLBase::~GLBase() {
}

GLBase* GLBase::get()
{
  return &self;
}

void GLBase::setTraceFunction(LOGFUNC f)
{
  tracef = f;
}

void GLBase::setEtraceFunction(LOGFUNC f)
{
  etracef = f;
}

void GLBase::dotrace(char const* fmt, ...)
{
  va_list arglist;
  va_start(arglist, fmt);
  
  (*tracef)(fmt, arglist);

  va_end(arglist);
}

void GLBase::doetrace(char const* fmt, ...)
{
  va_list arglist;
  va_start(arglist, fmt);
  
  (*etracef)(fmt, arglist);

  va_end(arglist);
}

void GLBase::initialize(int screenWidth, int screenHeight)
{
  trace("sizeof(byte): %d", sizeof(byte));
  trace("sizeof(int): %d", sizeof(int));
  trace("sizeof(float): %d", sizeof(float));
  trace("sizeof(long): %d", sizeof(long));
  trace("sizeof(long long): %d", sizeof(long long));
  trace("sizeof(void*): %d", sizeof(void*));

  // 画面サイズ
  this->screenWidth = screenWidth;
  this->screenHeight = screenHeight;

  // Inits fbo stats
  memset(fboUsed, 0, sizeof(fboUsed));

  // Set default viewport
  trace("Default viewport: %dx%d", screenWidth, screenHeight);
  setViewport(-1, 0, 0, screenWidth, screenHeight);

  // バインダーの初期化
  getBinder()->initialize();
  getBinder()->setBlendMode(false, RenderEnums::BLENDMODE_ALPHA_BLEND);
  
  // シェープレンデラーの初期化
  shapeRenderer.initialize();

  // Free imageの初期化
  FreeImage_Initialise(false);
  FreeImage_SetOutputMessage(FreeImageErrorHandler);

  // 時間開始
  timer.start();
  
  // Check for extensions
  const char* extensions = (const char*)glGetString(GL_EXTENSIONS);
  ETC1 = (strstr(extensions, "GL_OES_compressed_ETC1_RGB8_texture") != NULL);

  trace("Extensions: %s", extensions);
}

IObject* GLBase::loadBo3(byte* data, int length, bool gpuOnly)
{
  Object* object = new Object();

  if( !object->loadFromBo3(data, length, gpuOnly) ){
    delete object;
    return NULL;
  }

  return object;
}

IObject* GLBase::subObject(IObject* object, int vertexOffset, int triangleOffset,
		   int vertexLength, int triangleLength, int layer, int polygonMap)
{
  Object* newobject = new Object();
  newobject->loadFromObjectPart((Object*)object, vertexOffset, triangleOffset,
				vertexLength, triangleLength, layer, polygonMap);

  return newobject;
}

IMergeGroup* GLBase::createMergeGroup(IObject* base, int maxVertices, int polygonMaps)
{
  MergeGroup* mergegroup = new MergeGroup();
  mergegroup->loadFromObject((Object*)base, polygonMaps);
  mergegroup->createEmptyGroup((Object*)base, maxVertices, polygonMaps);

  return mergegroup;
}

IMergeGroup* GLBase::createMergeGroup(int maxVertices)
{
  MergeGroup* mergegroup = new MergeGroup();
  mergegroup->loadForSpriteMergeGroup();
  mergegroup->createEmptyGroup(NULL, maxVertices, 1);

  return mergegroup;
}

IRenderQueue* GLBase::createRenderQueue()
{
  return new RenderQueue();
}

int GLBase::loadShader(const char* vertexCode, const char* fragmentCode, const char* vShaderName, const char* fShaderName)
{
  return shaderTable.loadShader(vertexCode, fragmentCode, vShaderName, fShaderName);
}

int GLBase::loadShader( const char* vertexCode, const char* fragmentCode, const char* programName ) {
	return shaderTable.loadShader( vertexCode, fragmentCode, programName );
}

void GLBase::addShaderUniform(int shaderId, int uniformPos, const char *uniformName) {
	ShaderProgram *program = shaderTable.getShaderProgram( shaderId );

	if( program != NULL ) {
		program->setMyUniform( uniformPos, uniformName );
	}
}

int GLBase::getShaderProgramID( const char* shaderName ) {
	return shaderTable.getShaderProgramID( shaderName );
}

bool GLBase::hasMyUniform( int shaderID, int uniformPos ) {
	ShaderProgram *program = shaderTable.getShaderProgram( shaderID );

	if( program == NULL ) {
		return false;
	}

	return program->hasMyUniform( uniformPos );
}

int GLBase::loadTexture( byte* data, int length, bool repeat, bool mipmap, bool gpuOnly ) {
	return textureTable.loadTexture( data, length, repeat, mipmap, gpuOnly );
}

void GLBase::deleteTexture(int texture) {
  textureTable.deleteTexture(texture);
}

TextureInfo GLBase::getTextureInfo(int texture) {
  return textureTable.getTextureInfo(texture);
}


int GLBase::loadTextureCube(byte* data[6], int length[6], bool mipmap)
{
  return textureTable.loadTextureCube(data, length, mipmap);
}

int GLBase::createTexture( int width, int height, int format, int pixelFormat, bool repeat, byte* data, bool filterLinear ) {
  return textureTable.createTexture( width, height, format, pixelFormat, 
				     filterLinear, repeat, data );
}

int GLBase::createTextureCube( int width, int height, int format, int pixelFormat, 
			       bool repeat, byte* data[6], bool filterLinear )
{
  return textureTable.createTextureCube( width, height, format, pixelFormat,
					 filterLinear, repeat, data );
}


bool GLBase::copySubImage(int texture, int offsetX, int offsetY, int width, int height, int format, int pixelFormat, byte *subData) {
	return textureTable.copySubImage( texture, offsetX, offsetY, width, height, format, pixelFormat, subData );
}

int GLBase::createFBO()
{
  return fboTable.createFBO();
}

void GLBase::setFBOTexture(int fboID, int textureID, bool createDepth)
{
  Framebuffer* fbo = fboTable.getFramebuffer(fboID);
  if( fbo == NULL ) return;

  Texture* tex = textureTable.getTexture(textureID);
  if( tex == NULL ) return;

  fbo->setTexture(tex, createDepth);
}

int GLBase::loadAnimation(char* bsFile, int bsFileLength, byte* binFile, int binFileLength)
{
  return animationTable.loadAnimation( bsFile, bsFileLength, binFile, binFileLength );
}

int GLBase::loadAnimation(byte* bbmFile, int bbmFileLength)
{
  return animationTable.loadAnimation( bbmFile, bbmFileLength );
}

void GLBase::setAnimationParent(int child, int parent)
{
  Animation* childAnime = animationTable.getAnimation(child);
  Animation* parentAnime = animationTable.getAnimation(parent);

  if( childAnime == NULL ) {
    etrace("Animation not found: %d", child);
    return;
  }
  if( parentAnime == NULL ) {
    etrace("Animation not found: %d", parent);
    return;
  }

  childAnime->prependParent(parentAnime);
}

bool GLBase::deleteAnimation(int animationID) {
  return animationTable.deleteAnimation( animationID );
}

IMrf* GLBase::loadMrf(const char* mrfFile, bool skipSetUniforms)
{
  return Mrf::load( mrfFile, skipSetUniforms );
}

IAnimationPlayer* GLBase::loadAnimationPlayer( int animationID, IObject* object)
{
  // ボーン名を特定
  Object* obj = (Object*) object;
  if( obj == NULL ) {
    return loadAnimationPlayer( animationID, (IPolygonMap *)NULL );
  }

  if(
     (obj->getLayers()->getSize() < 1 )  ||
     (((Layer*)obj->getLayers()->get(0))->getPolygonMaps()->getSize() < 1) ) {
    return NULL;
  }

  // 最初のポリゴンマップのボーン名を採用する
  PolygonMap* firstPm = (PolygonMap*) ((Layer*)obj->getLayers()->get(0))->getPolygonMaps()->get(0);

  return loadAnimationPlayer( animationID, firstPm );
}

IAnimationPlayer* GLBase::loadAnimationPlayer( int animationID, IPolygonMap* polygonMap)
{
  // アニメーションを特定
  Animation* animation = animationTable.getAnimation( animationID );
  if( animation == NULL ) return NULL;

  if( polygonMap == NULL ) {
    return new AnimationPlayer( animation );
  }

  return new AnimationPlayer( animation, ((PolygonMap*)polygonMap)->getMatrixIndicesNames() );
}

void GLBase::adaptTo( IAnimationPlayer *animationPlayer, IObject *object ) {
  // ボーン名を特定
  Object* obj = (Object*) object;

  if(
     (obj == NULL) ||
     (obj->getLayers()->getSize() < 1 )  ||
     (((Layer*)obj->getLayers()->get(0))->getPolygonMaps()->getSize() < 1) ) {
    return;
  }
  
  // 最初のポリゴンマップのボーン名を採用する
  PolygonMap* firstPm = (PolygonMap*) ((Layer*)obj->getLayers()->get(0))->getPolygonMaps()->get(0);
  
  adaptTo( animationPlayer, firstPm );
}

void GLBase::adaptTo( IAnimationPlayer *animationPlayer, IPolygonMap *polygonMap ) {
  if( (animationPlayer == NULL) || (polygonMap == NULL) ) {
    return;
  }
  
  ((AnimationPlayer *)animationPlayer)->sortBones( ((PolygonMap *)polygonMap)->getMatrixIndicesNames() );
}


ShaderTable* GLBase::getShaderTable()
{
  return &shaderTable;
}

TextureTable* GLBase::getTextureTable()
{
  return &textureTable;
}

FBOTable* GLBase::getFBOTable()
{
  return &fboTable;
}

AnimationTable* GLBase::getAnimationTable()
{
  return &animationTable;
}

MyUniformsBuffer* GLBase::getMyUniformsBuffer()
{
  return &myUniformsBuffer;
}

DrawCallsPool* GLBase::getDrawCallsPool()
{
  return &drawCallsPool;
}

Binder* GLBase::getBinder()
{
  return &binder;
}

ShapeRenderer* GLBase::getShapeRenderer()
{
  return &shapeRenderer;
}

int GLBase::getMaxPaletteMatrices()
{
  return maxPaletteMatrices;
}

int GLBase::getScreenWidth()
{
    return screenWidth;
}

int GLBase::getScreenHeight()
{
    return screenHeight;
}

double GLBase::getTime()
{
  return timer.getTime();
}

void GLBase::resetGLState()
{
	getBinder()->setForceSetFlags();
	getBinder()->unbindAllBuffers();
	getBinder()->resetTexture();
	getBinder()->resetProgram();
}

void GLBase::setViewport(int framebuffer, int l, int t, int r, int b)
{
  if( framebuffer == -1 ){
    viewport[0] = l;
    viewport[1] = t;
    viewport[2] = r;
    viewport[3] = b;

    GLOP( glViewport(viewport[0], viewport[1], viewport[2], viewport[3]) );
  } else {
    Framebuffer* fbo = getFBOTable()->getFramebuffer(framebuffer);
    if( fbo == NULL ){
      doetrace("FBO %d not loaded", framebuffer);
      return;
    }
    
    fbo->setViewport(l, t, r, b);
  }
}

int* GLBase::getViewport(int framebuffer)
{
  if( framebuffer < 0 ){
    return viewport;
  } else {
    Framebuffer* fbo = getFBOTable()->getFramebuffer(framebuffer);
    if( fbo == NULL ){
      doetrace("FBO %d not loaded", framebuffer);
      return NULL;
    }
    
    return fbo->getViewport();
  }
}

bool* GLBase::getFboUsed()
{
  return fboUsed;
}

MyUniformValue* GLBase::acquireMyUniform(const float* vector, int numComponents)
{
  return myUniformsBuffer.acquire(vector, numComponents);
}

MyUniformValue* GLBase::acquireMyUniform(int texture, int glactive)
{
  return myUniformsBuffer.acquire(texture, glactive);
}

DrawCall* GLBase::acquireDrawCall()
{
  return drawCallsPool.acquireDrawCall();
}

DrawCall* GLBase::acquireDrawCall(IPolygonMap* polygonMap)
{
  return drawCallsPool.acquireDrawCall(polygonMap);
}

DrawCall* GLBase::acquireDrawCall(float* bbox)
{
  return drawCallsPool.acquireDrawCall(bbox);
}

DrawCall* GLBase::acquireDrawCall(int numParticles)
{
  return drawCallsPool.acquireDrawCall(numParticles);
}

DrawCall* GLBase::acquireDrawCall(RenderEnums::ClearMode mode, float* color)
{
  return drawCallsPool.acquireDrawCall(mode, color);
}

void GLBase::flush()
{
  // Reset my uniforms
  myUniformsBuffer.reset();

  // Reset draw calls pool
  drawCallsPool.reset();

  // Reset fbo usage stats
  memset(fboUsed, 0, sizeof(fboUsed));
}

void GLBase::onResume() {
  //アプリサスペンド中に他のアプリが起動されると、
  //GLステートが書き換えられ、
  //レジューム時に表示が崩れることがあったため、
  //GLステート強制設定フラグを立てておく
  binder.setForceSetFlags();
}

/**
 * fTimeSecondsを使用している場合は、これを各フレームの先頭で呼ぶ
 */
void GLBase::updateTimer( struct timeval &currentTimev ) {
	timer.update( currentTimev );
}

/**
 * @return Whether extension GL_OES_compressed_ETC1_RGB8_texture is present
 */
bool GLBase::hasETC1() {
	return ETC1;
}
