/**
 * @file glbase.h
 * @brief GLBase: wrap library over GL ES 2.0
 **/
#pragma once

#include "Iglbase.h"
#include "types.h"
#include "shaderTable.h"
#include "textureTable.h"
#include "fboTable.h"
#include "animationTable.h"
#include "renderList.h"
#include "matrixStack.h"
#include "binder.h"
#include "timer.h"
#include "shapeRenderer.h"
#include "myUniforms.h"
#include "drawCallsPool.h"

// Forward decs
class IObject;
class IMrf;
class Matrix;
class IRenderQueue;

/**
 * @brief GL ES 2.0でbsリソース(bo3, bs/bin)を使って、
 * レンダリングを行うためのクラス
 *
 **/
class GLBase : public IGLBase
{
 public:
  ~GLBase();

  /**
   * @return GLBaseの実体化
   **/
  static GLBase* get();

  /**
   * ログ出力
   **/
  void dotrace(char const* fmt, ...);
  
  /**
   * エラーログ出力
   **/
  void doetrace(char const* fmt, ...);


  void initialize(int screenWidth, int screenHeight);

  void setTraceFunction(LOGFUNC f);

  void setEtraceFunction(LOGFUNC f);

  IObject* loadBo3(byte* data, int length, bool gpuOnly=false);

  IObject* subObject(IObject* object, int vertexOffset, int triangleOffset,
		     int vertexLength, int triangleLength, int layer, int polygonMap);

  IMergeGroup* createMergeGroup(IObject* base, int maxVertices, int polygonMaps=1);

  IMergeGroup* createMergeGroup(int maxVertices);

  IRenderQueue* createRenderQueue();

  int loadShader(const char* vertexCode, const char* fragmentCode, const char* vShaderName, const char* fShaderName);

  int loadShader(const char* vertexCode, const char* fragmentCode, const char* programName);

  void addShaderUniform(int shaderId, int uniformPos, const char *uniformName);

  int getShaderProgramID(const char* shaderName);

  bool hasMyUniform( int shaderID, int uniformPos );

  int loadTexture(byte* data, int length, bool repeat, bool mipmap, bool gpuOnly = true);

  void deleteTexture(int texture);

  TextureInfo getTextureInfo(int texture);

  int loadTextureCube(byte* data[6], int length[6], bool mipmap);
  
  int createTexture( int width, int height, int format, int pixelFormat, bool repeat, byte* data = NULL, bool filterLinear = true );

  int createTextureCube( int width, int neight, int format, int pixelFormat, 
			 bool repeat, byte* data[6], bool filterLinear = true );

  bool copySubImage(int texture, int offsetX, int offsetY, int width, int height, int format, int pixelFormat, byte *subData);

  int createFBO();

  void setFBOTexture(int fbo, int texture, bool createDepth);

  int loadAnimation(char* bsFile, int bsFileLength, byte* binFile, int binFileLength);

  int loadAnimation(byte* bbmFile, int bbmFileLength);

  void setAnimationParent(int child, int parent);

  bool deleteAnimation(int animationID);

  IMrf* loadMrf(const char* mrfFile, bool skipSetUniforms = false);

  IAnimationPlayer* loadAnimationPlayer( int animation, IObject* object);

  IAnimationPlayer* loadAnimationPlayer( int animation, IPolygonMap* polygonMap);

  void adaptTo( IAnimationPlayer *animationPlayer, IObject *object );

  void adaptTo( IAnimationPlayer *animationPlayer, IPolygonMap *polygonMap );

  int getScreenWidth();
  int getScreenHeight();
  int getDisplayWidth();
  int getDisplayHeight();
  double getTime();
  void resetGLState();

  /**
   * シェーダーテーブル取得
   **/
  ShaderTable* getShaderTable();

  /**
   * テクスチャーテーブルを取得
   **/
  TextureTable* getTextureTable();

  /**
   * Get Fbo table
   **/
  FBOTable* getFBOTable();

  /**
   * アニメーションテーブルを取得
   **/
  AnimationTable* getAnimationTable();

  /**
   * Get myUniform buffer
   **/
  MyUniformsBuffer* getMyUniformsBuffer();

  /**
   * Get draw calls pool
   **/
  DrawCallsPool* getDrawCallsPool();

  /**
   * バインダーを取得
   **/
  Binder* getBinder();

  /**
   * シェープレンデラーを取得
   **/
  ShapeRenderer* getShapeRenderer();

  /**
   * @return 最大のパレット行列
   **/
  int getMaxPaletteMatrices();

  /**
   * Viewport
   **/
  void setViewport(int framebuffer, int l, int t, int r, int b);

  /**
   * Get viewport
   **/
  int* getViewport(int framebuffer);
  
  /**
   * New draw call from pool
   **/
  DrawCall* acquireDrawCall();
  DrawCall* acquireDrawCall(IPolygonMap* polygonMap);
  DrawCall* acquireDrawCall(float* bbox);
  DrawCall* acquireDrawCall(int numParticles);
  DrawCall* acquireDrawCall(RenderEnums::ClearMode mode, float* color);


  /**
   * Get fbo usage stats
   **/
  bool* getFboUsed();

  /**
   * New uniform
   **/
  MyUniformValue* acquireMyUniform(const float* vector, int numComponents);
  MyUniformValue* acquireMyUniform(int texture, int glactive);

  void flush();

  void onResume();

  /**
   * fTimeSecondsを使用している場合は、これを各フレームの先頭で呼ぶ
   */
  void updateTimer( struct timeval &currentTimev );

  /**
   * Checks if this platform has ETC1 support
   */
  bool hasETC1();

 private:
  
  /**
   * 初期化
   **/
  GLBase();

  // シングルトン
  static GLBase self;

  // ログ関数
  LOGFUNC tracef;
  LOGFUNC etracef;

  // シェダーテーブル
  ShaderTable shaderTable;

  // テクスチャーテーブル
  TextureTable textureTable;

  // FBOテーブル
  FBOTable fboTable;

  // アニメーションテーブル
  AnimationTable animationTable;

  // MyUniform buffer
  MyUniformsBuffer myUniformsBuffer;

  // DrawCalls pool
  DrawCallsPool drawCallsPool;

  // バインダー
  Binder binder;

  // シェープレンデラー
  ShapeRenderer shapeRenderer;

  // タイマー
  Timer timer;

  // Viewport設定
  int viewport[4];

  int screenWidth;
  int screenHeight;

  // 行列パレット最大数
  int maxPaletteMatrices;

  // FBO usage stats
  bool fboUsed[128];
  
  // Extension flags
  bool ETC1;

};
