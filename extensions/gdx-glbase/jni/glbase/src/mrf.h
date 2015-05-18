/**
 * @file mrf.h
 * @brief Private interface for mrf type
 **/
#pragma once 

#include <vector>

#include "Imrf.h"
#include "arrayList.h"
#include "types.h"
#include "renderSettings.h"
#include "renderEnums.h"
#include "uniform.h"

// Fwd
class DrawCall;
class IRenderPass;
class RenderPass;
class RenderQueue;
class Object;

/**
 * @brief レンダーメッシュを象徴するクラス
 **/
class Mrf : public IMrf
{
public:
  ~Mrf();

  /**
   * Load an mrf
   **/
  static Mrf* load(const char* mrfFile, bool skipSetUniforms);

  /**
   * Register all needed draw calls to queue
   **/
  void registerDrawCalls(IRenderQueue* queue,
			 BasicRenderParameters* params,
			 IObject* object);
  
  /**
   * Obtain all produces draw calls
   **/
  int prepareDrawCalls(DrawCall* &result, 
		       BasicRenderParameters* params,
		       IObject* object);

  /**
   * @return パスの数
   */
  int getPassesNum();

  /**
   * @param passIndex パスのインデックス
   * @return パス情報
   */
  IRenderPass *getPass( int passIndex );

  /**
   * @return ID
   */
  char *getId();

  /**
   * ロード時にskipSetUniformsをtrueにしていた場合は、使用前にこれを一度呼ぶ
   */
  void setUniforms();

 private:

  // Internal
  struct MovableFBO
  {
    RenderEnums::TextureType from;
    RenderEnums::TextureType to;
  };

  // Mrf uniforms
  typedef struct tagMrfUniform{
    int id;
    Uniform* u;
    RenderPass* pass;
  } MrfUniform;
  
  std::vector<MrfUniform> mrfUniforms;

  RenderSettings renderSettings;

  Mrf(const char* mrfFile, bool skipSetUniforms);

  RenderEnums::TextureType findUnusedFBO(RenderEnums::TextureType def);
  // Register all the needed call for the specified pass
  void registerCalls(DrawCall* &result, int &curPos, 
		     BasicRenderParameters* params, Object* object, RenderPass* pass, 
		     MovableFBO* movemap, int movemapNum);
  // Setup a draw call for specified pass and command
  void setupDrawCall(DrawCall* dc, BasicRenderParameters* params,
		     RenderPass* pass, MovableFBO* movemap, int movemapNum);
};
