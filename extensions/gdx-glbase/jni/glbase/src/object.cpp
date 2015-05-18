/**

 * @file object.cpp

 * @brief Renderable mesh object implementation

 **/



#include "glbase.h"
#include "object.h"
#include "fileUtils.h"
#include "layer.h"
#include "polygonMap.h"
#include "surface.h"
#include "macros.h"
#include "drawCall.h"
#include "renderQueue.h"

#include <string.h>

RenderParameters::RenderParameters()
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
}

Object::Object()

  : layers(128)

{

  loaded = false;

  renderEnvInitialized = false;

  for( int i = 0; i < 6; i++ ) {
    boundingBox[ i ] = 0.0f;
  }

  id = NULL;

  name = NULL;

  version = NULL;

  metainfo = NULL;

  filename = NULL;

  leftBitShift = 0;

  shadeModel = NULL;

  shadeValue = NULL;

  gpuOnly = false;

}



Object::~Object()

{

  delete[] id;

  delete[] name;

  delete[] version;

  delete[] metainfo;

  delete[] filename;

  delete[] shadeModel;

  delete[] shadeValue;



  // ロード済なら、アンロードする

  if( renderEnvInitialized ){

    uninitRenderEnv();

  }



  // 全レイヤーを削除する

  Layer* lay = NULL;

  foreach_element( getLayers(), lay, Layer* ){

    delete lay;

  }

}



bool Object::loadFromBo3(const byte* data, int length, bool gpuOnly)

{

  // すでにロードされていれば、失敗

  if( loaded ) return false;

  this->gpuOnly = gpuOnly;



  //ファイルヘッダからオフセットを取得

  const int infoOffset = 16;

  int layerOffset = *((int *)(&data[ 8 ]));

  int surfaceOffset = *((int *)(&data[ 12 ]));



  version = new char[4];

  version[ 0 ] = '0' + data[ 2 ];

  version[ 1 ] = '.';

  version[ 2 ] = '0' + data[ 3 ];

  version[ 3 ] = '\0';



  //オブジェクト情報取得

  if( !loadObjectInfo( data, infoOffset ) ) {

    return false;

  }



  //レイヤー配列取得

  if( !loadLayers( data, layerOffset ) ) {

    return false;

  }



  //サーフェイス配列取得

  ArrayList surfaces(128);

  if( !loadSurfaces( data, surfaceOffset, &surfaces ) ) {

    return false;

  }



  // 全てのポリゴンマップに相当するサーフェースをリンクする

  Layer* lay = NULL;

  PolygonMap* pm = NULL;

  foreach_element(getLayers(), lay, Layer*){

    foreach_element(lay->getPolygonMaps(), pm, PolygonMap*){

      pm->linkSurface(&surfaces);

    }

  }



  // Delete temp surfaces

  for( int i=0; i<surfaces.getSize(); i++ ){

    Surface* s = (Surface*)surfaces.get(i);

    delete s;

  }





  loaded = true;

  return true;

}



void Object::copyBasicInfo(Object* object)

{

  // Basic info

  version = new char[4];

  memcpy(version, object->version, sizeof(char)*4);

  

  if( object->id != NULL ){

    id = new char[strlen(object->id)+1];

    strcpy(id, object->id);

  }

  if( object->name != NULL ){

    name = new char[strlen(object->name)+1];

    strcpy(name, object->name);

  }

  if( object->metainfo != NULL ){

    metainfo = new char[strlen(object->metainfo)+1];

    strcpy(metainfo, object->metainfo);

  }

  if( object->shadeModel != NULL ){

    shadeModel = new char[strlen(object->shadeModel)+1];

    strcpy(shadeModel, object->shadeModel);

  }

  if( object->shadeValue != NULL ){

    shadeValue = new char[strlen(object->shadeValue)+1];

    strcpy(shadeValue, object->shadeValue);

  }

  filename = NULL;



  trace("object from parts: %s", id);

}



void Object::setDefaultInfo()

{

  version = strdup2("MRG");

  name = strdup2("merge_group");

}



bool Object::loadForSpriteMergeGroup()

{

  setDefaultInfo();



  // Layer

  Layer* newlayer = new Layer();

  newlayer->loadForSpriteMergeGroup();

  layers.add(newlayer);



  loaded = true;

  return true;

}



bool Object::loadFromObject(Object* object, int polygonMaps)

{

  copyBasicInfo(object);

  

  // First layer

  Layer* newlayer = new Layer();

  newlayer->loadFrom((Layer*)object->layers.get(0), -1, -1, -1, -1, 0, polygonMaps);

  layers.add(newlayer);

  

  loaded = true;

  return true;

}



bool Object::loadFromObjectPart(Object* object, int vertexOffset, int triangleOffset,

				int vertexLength, int triangleLength, int layer, int polygonMap)

{

  // Basic structure

  copyBasicInfo(object);



  // Make one layer and one polygonMap

  Layer* newlayer = new Layer();

  newlayer->loadFrom((Layer*)object->layers.get(layer), vertexOffset, triangleOffset, 

		     vertexLength, triangleLength, polygonMap, 1);

  layers.add(newlayer);



  loaded = true;

  return true;

}





bool Object::loadObjectInfo( const byte *data, int offset )

{

  //ヘッダ部分

  int idLength = FileUtils::readByte( data, offset );          //ID文字列の長さ

  int nameLength = FileUtils::readByte( data, offset );        //名前文字列の長さ

  int metaInfoLength = FileUtils::readByte( data, offset );    //メタ情報文字列の長さ

  int shadeModelLength = FileUtils::readByte( data, offset );  //ShadeModel文字列の長さ

  int shadeValueLength = FileUtils::readByte( data, offset );//ShadeValue文字列の長さ

  int coordValueType = FileUtils::readByte( data, offset );//座標値の形式

  int otherValueType = FileUtils::readByte( data, offset );//座標以外の値の形式

  /*int leftBitShift = */FileUtils::readByte( data, offset );//固定小数値のシフトビット数



  //float形式のものしか受け付けない

  if( (coordValueType != 1) || (otherValueType != 1) ) {

    etrace("bo3 file not exported in float mode");



    return false;

  }



  //未使用領域をスキップ

  offset += 4;



  //データ部分

  id = FileUtils::readString( data, offset, idLength );//ID

  name = FileUtils::readString( data, offset, nameLength );//名前

  metainfo = FileUtils::readString( data, offset, metaInfoLength );//メタ情報

  shadeModel = FileUtils::readString( data, offset, shadeModelLength );//ShadeModel

  shadeValue = FileUtils::readString( data, offset, shadeValueLength );//ShadeValue

  filename = NULL;



  return true;

}



bool Object::loadLayers( const byte *data, int offset )

{

  int startPos = offset;



  int layersNum = FileUtils::readByte( data, offset );

  int* offsets;



  offsets = FileUtils::readInts( data, offset, layersNum * 3 );

  

  //全レイヤーをロード

  for( int i = 0, j = 0; i < layersNum; i++, j += 3 ) {

    Layer* newLayer = new Layer();

        

    if( !newLayer->load( data, startPos + offsets[ j ], startPos + offsets[ j + 1 ], startPos + offsets[ j + 2 ], this ) ) {

      delete[] offsets;

      return false;

    }

    

    layers.add(newLayer);

  }



  delete[] offsets;

  return true;

}



bool Object::loadSurfaces( const byte *data, int offset, ArrayList* surfaces )

{

  int startPos = offset;



  int surfacesNum = FileUtils::readByte( data, offset );

  int* offsets;



  offsets = FileUtils::readInts( data, offset, surfacesNum * 2 );

  

  //全レイヤーをロード

  for( int i = 0, j = 0; i < surfacesNum; i++, j += 2 ) {

    Surface* newSurface = new Surface();

        

    if( !newSurface->load( data, startPos + offsets[ j ], startPos + offsets[ j + 1 ] ) ) {

      delete[] offsets;

      return false;

    }

    

    surfaces->add(newSurface);

  }



  delete[] offsets;

  return true;



}





char* Object::getId()

{

  return id;

}



char* Object::getName()

{

  return name;

}



char* Object::getVersion()

{

  return version;

}



char* Object::getMetainfo()

{

  return metainfo;

}



char* Object::getFilename()

{

  return filename;

}



int Object::getLeftBitShift()

{

  return leftBitShift;

}



char* Object::getShadeModel()

{

  return shadeModel;

}



char* Object::getShadeValue()

{

  return shadeValue;

}



ArrayList* Object::getLayers()

{

  return &layers;

}



bool Object::initRenderEnv()

{

  if( !loaded ) {

    etrace("Attempt to initRenderEnv an unloaded object. This should never happen.");

    return false;

  }



  // BBOXも作る！

  bool first = true;



  Layer* lay = NULL;

  foreach_element(getLayers(), lay, Layer*){

    if( !lay->initRenderEnv() ){

      return false;

    }



    // BBOX生成

    float* laybb = lay->getBoundingBox();



    if( first ){

      first = false;

      memcpy( boundingBox, laybb, sizeof(boundingBox) );

    }

    else{

      if( laybb[ 0 ] < boundingBox[ 0 ] ) {

	boundingBox[ 0 ] = laybb[ 0 ];

      }



      if( laybb[ 1 ] < boundingBox[ 1 ] ) {

	boundingBox[ 1 ] = laybb[ 1 ];

      }



      //Zは逆

      if( laybb[ 2 ] > boundingBox[ 2 ] ) {

	boundingBox[ 2 ] = laybb[ 2 ];

      }



      if( laybb[ 3 ] > boundingBox[ 3 ] ) {

	boundingBox[ 3 ] = laybb[ 3 ];

      }



      if( laybb[ 4 ] > boundingBox[ 4 ] ) {

	boundingBox[ 4 ] = laybb[ 4 ];

      }



      //Zは逆

      if( laybb[ 5 ] < boundingBox[ 5 ] ) {

	boundingBox[ 5 ] = laybb[ 5 ];

      }

    }

  }



  trace( "bbox: %f %f %f - %f %f %f", boundingBox[0], boundingBox[1], boundingBox[2],

	 boundingBox[3], boundingBox[4], boundingBox[5] );

  

  renderEnvInitialized = true;

  

  if( gpuOnly ) {

    //VRAMに転送後、オリジナルデータを解放

    releaseOriginalData();

  }


  return true;

}



void Object::uninitRenderEnv()

{

  if( !loaded ) {

    etrace("Attempt to uninitRenderEnv an unloaded object. This should never happen.");

    return;
  }


  Layer* lay = NULL;

  foreach_element(getLayers(), lay, Layer*){

    lay->uninitRenderEnv();

  }

  

  renderEnvInitialized = false;

}



bool Object::isRenderEnvInitialized()

{

  return renderEnvInitialized;

}



float* Object::getBoundingBox()

{

  return boundingBox;

}



/**

 * @brief	オリジナルデータを解放

 */

void Object::releaseOriginalData() {

  Layer *lay = NULL;



  foreach_element( getLayers(), lay, Layer * ) {

    lay->releaseOriginalData();

  }

}



void Object::setTexture( int layerIdx, int polygonMapIdx, int texture )

{

  if( layerIdx < 0 || layerIdx >= getLayers()->getSize() ) {

    etrace("Layer index out of range: %d.", layerIdx);

    return;

  }

  Layer* layer = (Layer*)getLayers()->get(layerIdx);



  if( polygonMapIdx < 0 || polygonMapIdx >= layer->getPolygonMaps()->getSize() ) {

    etrace("PM index out of range: %d.", polygonMapIdx);

    return;

  }

  PolygonMap* pm = (PolygonMap*)layer->getPolygonMaps()->get(polygonMapIdx);

  ((Surface *)pm->getSurface())->setTexture(texture);

}



void Object::setTexture( int polygonMapIdx, int texture )

{

  setTexture(0, polygonMapIdx, texture);

}



void Object::setMatrixIndicesNames( ArrayList* names, int layerIdx )

{

  if( layerIdx < 0 || layerIdx >= getLayers()->getSize() ) {

    etrace("Layer index out of range: %d.", layerIdx);

    return;

  }

  Layer* layer = (Layer*)getLayers()->get(layerIdx);

  

  layer->setMatrixIndicesNames(names);

}


void Object::addDrawCalls( IRenderQueue* queue, RenderParameters* params )
{
  DrawCall* result;
  int calls = prepareDrawCalls(result, params);
  
  for( int i=0; i<calls; i++ ){
    queue->registerDrawCall(&result[i]);
  }
}

int Object::prepareDrawCalls( DrawCall* &result, RenderParameters* params )
{
  int curPos = 0;
  result = NULL;

  Layer* lay;
  PolygonMap* pm;
  foreach_element( getLayers(), lay, Layer* ){
    foreach_element( lay->getPolygonMaps(), pm, PolygonMap* ){
      DrawCall* nDrawCall = GLBase::get()->getDrawCallsPool()->acquireDrawCall(pm);
      curPos++;
      if( result == NULL ) result = nDrawCall;
      
      // Copy contents
      copyParamsToDrawCall(params, nDrawCall);
      
      // If no texture, use pm texture
      if( nDrawCall->texture == -1 ){
	nDrawCall->texture = pm->getSurface()->getTexture();
      }
    }
  }

  return curPos;
}

void Object::copyParamsToDrawCall(RenderParameters* params, DrawCall* drawCall)
{
  memcpy(drawCall->modelTransform.getMatrixPointer(), 
	 params->modelTransform.getMatrixPointer(), 
	 sizeof(float)*16);
  drawCall->shader = params->shader;
  drawCall->texture = params->texture;
  drawCall->framebuffer = params->framebuffer;
  drawCall->animationPlayer = params->animationPlayer;
  drawCall->blendSrcAlpha = params->blendSrcAlpha;
  drawCall->blendMode = params->blendMode;
  drawCall->cullingMode = params->cullingMode;
  drawCall->useDepthTest = params->useDepthTest;
  drawCall->depthFunc = params->depthFunc;
  drawCall->depthMask = params->depthMask;
  memcpy(drawCall->colorMask, params->colorMask, sizeof(bool)*4);
}
