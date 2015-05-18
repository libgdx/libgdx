/**
 * @file polygonMap.cpp
 * @brief polygonMap implementation
 **/

#include "polygonMap.h"
#include "animationPlayer.h"
#include "arrayList.h"
#include "fileUtils.h"
#include "glbase.h"
#include "layer.h"
#include "macros.h"
#include "polygonBuffer.h"
#include "shaderProgram.h"
#include "surface.h"
#include "vertexBuffer.h"

#include <string.h>

PolygonMap::PolygonMap()
{
  surfaceName = NULL;
  uvName = NULL;
  
  polygons = NULL;

  uvs = NULL;

  points = NULL;
  vcolors = NULL;
  normals = NULL;
  matrixIndices = NULL;
  matrixWeights = NULL;
  matrixIndicesNames = NULL;
}

PolygonMap::~PolygonMap()
{
  delete[] surfaceName;
  delete[] uvName;
  
  delete polygons;
  delete uvs;
}

bool PolygonMap::load( const byte *data, int infoOffset, int uvsOffset, int vertexNum,
		       VertexBuffer* points, VertexBuffer* vcolors, VertexBuffer* normals, 
		       VertexBuffer* matrixIndices, VertexBuffer* matrixWeights, 
		       ArrayList* matrixIndicesNames)
{
  this->points = points;
  this->vcolors = vcolors;
  this->normals = normals;
  this->matrixIndices = matrixIndices;
  this->matrixWeights = matrixWeights;
  this->matrixIndicesNames = matrixIndicesNames;

  //レイヤー情報取得
  if( !loadInfo( data, infoOffset ) ) {
    return false;
  }

  //UV配列取得
  if( !loadUVs( data, uvsOffset, vertexNum ) ) {
    return false;
  }

  return true;
}

void PolygonMap::loadFrom( PolygonMap* pm, int triangleOffset, int triangleLength, int vertexOffset, int vertexLength,
			   VertexBuffer* points, VertexBuffer* vcolors, VertexBuffer* normals, 
			   VertexBuffer* matrixIndices, VertexBuffer* matrixWeights, ArrayList* matrixIndicesNames)
{
  this->points = points;
  this->vcolors = vcolors;
  this->normals = normals;
  this->matrixIndices = matrixIndices;
  this->matrixWeights = matrixWeights;
  this->matrixIndicesNames = matrixIndicesNames;

  // Surface
  surfaceName = strdup2(pm->surfaceName);
  surface.loadFrom((Surface *)pm->getSurface());

  int polygonsCnt = triangleLength;

  //ポリゴン配列を設定
  if( vertexOffset != -1 ){
    int elementsCnt = polygonsCnt * 3;

    polygons = new PolygonBuffer( new unsigned short[ elementsCnt ], elementsCnt * sizeof( unsigned short ), 0 );
    polygons->append( pm->polygons->getShorts() + (triangleOffset * 3), elementsCnt, -vertexOffset );

    // UV
    if( pm->uvs != NULL ){
      uvName = strdup2(pm->uvName);
      int uvsLength = vertexLength * 2;
      float* uvsBuf = new float[uvsLength];
	  int uvsBufLength = uvsLength*sizeof(float);
      memcpy(uvsBuf, pm->uvs->getFloats() + vertexOffset*2, uvsBufLength);
      uvs = new VertexBuffer( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0, uvsBuf, uvsBufLength, uvsBufLength, 2 );
    }
  }
}

void PolygonMap::loadForSpriteMergeGroup()
{
  surfaceName = strdup2("mergeGroupSurface");
}

void PolygonMap::loadMergeGroup( PolygonMap* pm, int maxVertices, VertexBuffer* points, 
				 VertexBuffer* vcolors, VertexBuffer* normals, 
				 VertexBuffer* matrixIndices, VertexBuffer* matrixWeights )
{
  this->points = points;
  this->vcolors = vcolors;
  this->normals = normals;
  this->matrixIndices = matrixIndices;
  this->matrixWeights = matrixWeights;

  // Triangles always
  int elementsCnt = maxVertices * 3;
  polygons = new PolygonBuffer( new unsigned short[ elementsCnt ], elementsCnt * sizeof( unsigned short ), 0 );
  
  if( pm == NULL || pm->uvs != NULL ){
    if( pm == NULL ){
      uvName = strdup2("DefaultMergeGroupUV");
    } else {
      uvName = strdup2(pm->uvName);
    }
    int uvsLength = maxVertices * 2;
    float* uvsBuf = new float[uvsLength];
    uvs = new VertexBuffer( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0, uvsBuf, uvsLength * sizeof( float ), 0 , 2 );
  } 
}

void PolygonMap::appendUvs( PolygonMap* pm )
{
  if( uvs ){
    uvs->append(pm->uvs);
  }
}

void PolygonMap::appendPolygonMap( PolygonMap* pm, int voffset ) {
  polygons->append( pm->polygons, voffset );
}

void PolygonMap::appendTriangles( int voffset, int vertices, int triangles,
				  const unsigned short* indices, const float* uvsData ) {
  polygons->append( indices, triangles * 3, voffset );

  if( uvs ){
    uvs->append(uvsData, vertices*2);
  }
}

void PolygonMap::startGroup()
{
  polygons->clear();

  if( uvs ) uvs->clear();
}

void PolygonMap::endGroup()
{
  commitPolygonsChanges();

  if( uvs ) uvs->commitGroup();
}

bool PolygonMap::loadInfo( const byte *data, int offset )
{
  //ヘッダ部分
  int surfaceNameLength = FileUtils::readByte( data, offset );//サーフェイス文字列の長さ
  int polygonsCnt = FileUtils::readInt( data, offset );//ポリゴンの数
  int polygonVertices = FileUtils::readByte( data, offset );//ポリゴンの頂点数
  int weightSort = FileUtils::readByte( data, offset );//ウエイトソート
  
  //未使用領域をスキップ
  offset += 4;

  //ポリゴンの頂点数が3以外ならエラー
  if( polygonVertices != 3 ) {
    etrace( "polygonVertices is not 3" );
    return false;
  }
  
  //データ部分
  surfaceName = FileUtils::readString( data, offset, surfaceNameLength );//サーフェイス名

  //ウエイト数ソート情報をスキップ
  if( weightSort > 0 ) {
    offset += MAX_VERTEX_UNITS * 2 * sizeof( int );
  }

  //ポリゴン配列を設定
  int polygonsLength = polygonsCnt * polygonVertices;

  polygons = new PolygonBuffer( FileUtils::readUshorts( data, offset, polygonsLength ), polygonsLength * sizeof( unsigned short ), polygonsLength );
  
  // ???

  return true;
}

bool PolygonMap::loadUVs( const byte *data, int offset, int vertexNum )
{
  int uvsCount = FileUtils::readByte( data, offset );
  
  //UV数が0か1じゃなければエラー
  if( uvsCount > 1 ) {
    etrace( "multi-uv unsupported" );

    return false;
  }

  int* offsets;
  offsets = FileUtils::readInts( data, offset, 1 );
  delete[] offsets;

  //UVを読み込む
  if( uvsCount == 1 ) {
    //ヘッダ部分
    int uvNameLength = FileUtils::readByte( data, offset );
    int texCoordComponents = FileUtils::readByte( data, offset );

    //未使用領域をスキップ
    offset += 4;

    //テクスチャ座標の要素数が2じゃなければエラー
    if( texCoordComponents != 2 ) {
      etrace( "only u and v texture coordinates are supported" );

      return false;
    }

    //データ部分
    uvName = FileUtils::readString( data, offset, uvNameLength );
    int uvsLength = vertexNum * texCoordComponents;
    float* uvsBuf = FileUtils::readFloats( data, offset, uvsLength );
	int uvsBufLength = uvsLength * sizeof( float );
    uvs = new VertexBuffer( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0, uvsBuf, uvsBufLength, uvsBufLength, 2 );
  }

  return true;
}

void PolygonMap::linkSurface(ArrayList* surfaces)
{
  Surface* surf;
  foreach_element(surfaces, surf, Surface*){
    if( strcmp( surfaceName, surf->getName() ) == 0 ){
      this->surface.loadFrom(surf);
      break;
    }
  }
}

bool PolygonMap::initRenderEnv()
{
  polygons->loadVbo();

  if( uvs ) uvs->loadVbo();

  return true;
}

void PolygonMap::uninitRenderEnv()
{
  polygons->unloadVbo();

  if( uvs ) uvs->unloadVbo();
}

void PolygonMap::bindSurface()
{
  // サーフェースをバインド
  surface.bind();
}

void PolygonMap::execRender()
{
  // 頂点バッファーをバインド
  GLBase::get()->getBinder()->bindBuffer( points );
  
  if( vcolors ) GLBase::get()->getBinder()->bindBuffer( vcolors );
  if( normals ) GLBase::get()->getBinder()->bindBuffer( normals );
  if( uvs ) GLBase::get()->getBinder()->bindBuffer( uvs );
  if( matrixIndices ) GLBase::get()->getBinder()->bindBuffer( matrixIndices );
  if( matrixWeights ) GLBase::get()->getBinder()->bindBuffer( matrixWeights );

  // レンダリング
  polygons->draw();

  GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_VERTEX );
  if( vcolors ) GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_COLOR );
  if( normals ) GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_NORMAL );
  if( uvs ) GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0 );
  if( matrixIndices ) GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_MATRIX_INDICES );
  if( matrixWeights ) GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_WEIGHTS );

  // NOTE: Probably no need to unbind the shader,
  // since it will be re-binded at next rendering 100% times  
}


ArrayList* PolygonMap::getMatrixIndicesNames()
{
  return matrixIndicesNames;
}

/**
 * @brief	オリジナルデータを解放
 */
void PolygonMap::releaseOriginalData() {
  polygons->releaseOriginalData();

  if( uvs != NULL ) {
    uvs->releaseOriginalData();
  }
}

float* PolygonMap::getUvsBuffer()
{
  return uvs->getFloats();
}

int PolygonMap::getUvsBufferLength()
{
  return uvs->getLength() / (2*sizeof(float));
}

unsigned short* PolygonMap::getPolygonsBuffer()
{
  return polygons->getShorts();
}

int PolygonMap::getPolygonsBufferLength()
{
  return polygons->getElementsCnt();
}

void PolygonMap::commitUvsChanges()
{
  uvs->commitGroup();
}

void PolygonMap::commitPolygonsChanges()
{
  polygons->commitGroup();
}
