/**
 * @file mergeGroup.h
 * @brief Private interface for mergeGroup type
 **/
#pragma once 

#include "ImergeGroup.h"
#include "object.h"
#include "polygonMap.h"

/**
 * @brief レンダーメッシュを象徴するクラス
 **/
class MergeGroup : public IMergeGroup, public Object
{
 public: 
  /**
   * からのオブジェクトを生成します
   **/
  MergeGroup();

  /**
   * すべての所属リソースを解放する
   **/
  ~MergeGroup();

  void createEmptyGroup(Object* base, int maxVertices, int polygonMaps);

  void startGroup();

  void endGroup();

  void addPolygonMap(IPolygonMap* polygonMap, int targetPmId, float x, float y, float z);

  void addPolygonMap(IPolygonMap* polygonMap, int targetPmId, Matrix* transform);

  void addObject(IObject* object, int targetPmId, float x, float y, float z);

  void addObject(IObject* object, int targetPmId, Matrix* transform);

  void addTriangles(int vertices, int triangles, const float* points, 
		    const unsigned short* indices, const float* uvs);

  void addSprite(float x, float y, float z, int texture);

  void addSprite(float x, float y, float z, float w, float h, int texture,
		 float sx, float sy, float sw, float sh, bool flipV=false);

  void addSprite(int texture, float sx, float sy, float sw, float sh, 
		 Matrix* transform, bool flipV=false);

  void addSpriteUV(float x, float y, float z, float w, float h, 
		   float u, float v, float u2, float v2);

  void addQuad(SimpleVertex *vertex1, SimpleVertex *vertex2, SimpleVertex *vertex3, SimpleVertex *vertex4, float z);

  

  // Stub
  char* getId() {return Object::getId();}
  char* getName(){return Object::getName();}
  char* getVersion(){return Object::getVersion();}
  char* getMetainfo(){return Object::getMetainfo();}
  char* getFilename(){return Object::getFilename();}
  float* getBoundingBox(){return Object::getBoundingBox();}
  int getLeftBitShift(){return Object::getLeftBitShift();}
  char* getShadeModel(){return Object::getShadeModel();}
  char* getShadeValue(){return Object::getShadeValue();}
  ArrayList* getLayers(){return Object::getLayers();}
  bool initRenderEnv(){return Object::initRenderEnv();}
  void releaseOriginalData() { Object::releaseOriginalData(); }
  void setTexture(int l, int pm, int t) { Object::setTexture(l, pm, t); }
  void setTexture(int pm, int t) { Object::setTexture(pm, t); }
  void addDrawCalls( IRenderQueue* queue, RenderParameters* params ) 
  { Object::addDrawCalls(queue, params); }
  int prepareDrawCalls( DrawCall* &result, RenderParameters* params )
  { return Object::prepareDrawCalls(result, params); }
  void setMatrixIndicesNames( ArrayList* names, int layerIdx ) 
  { Object::setMatrixIndicesNames(names, layerIdx) ;}

 private:
  static const unsigned short quad[6];
};
