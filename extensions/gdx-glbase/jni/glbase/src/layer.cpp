/**
 * @file layer.cpp
 * @brief Layer implementation
 **/

#include "glbase.h"
#include "layer.h"
#include "fileUtils.h"
#include "macros.h"
#include "polygonMap.h"
#include "vertexBuffer.h"

#include <vector>
#include <string.h>

using namespace std;

Layer::Layer() :
		polygonMaps(128),
		matrixIndicesName(128) {
  id = NULL;
  name = NULL;

  for( int i = 0; i < 6; i++ ) {
    boundingBox[ i ] = 0.0f;
  }

  pivot = NULL;

  points = NULL;
  vcolors = NULL;
  normals = NULL;
  matrixIndices = NULL;
  matrixWeights = NULL;
}

Layer::~Layer()
{
  delete[] id;
  delete[] name;
  delete[] pivot;

  delete points;
  delete vcolors;
  delete normals;
  delete matrixIndices;
  delete matrixWeights;

  // ポリゴンマップを削除
  PolygonMap* pm = NULL;
  foreach_element(getPolygonMaps(), pm, PolygonMap*){
    delete pm;
  }

  // ボーン名リストを削除
  char* miName = NULL;
  foreach_element(&matrixIndicesName, miName, char*){
    delete miName;
  }
}

bool Layer::load(const byte *data, int infoOffset, int weightNamesOffset, int polygonMapsOffset, Object* parent)
{
  int pointsCnt = 0;

  //レイヤー情報取得
  if( !loadInfo( data, infoOffset, pointsCnt ) ) {
    return false;
  }

  //ウエイト名配列取得
  if( !loadWeightNames( data, weightNamesOffset ) ) {
    return false;
  }

  //ポリゴンマップ配列
  if( !loadPolygonMaps( data, polygonMapsOffset, parent, pointsCnt ) ) {
    return false;
  }

  return true;
}

void Layer::loadFrom(Layer* layer, int vertexOffset, int triangleOffset,
		     int vertexLength, int triangleLength, int polygonMap, int polygonMapsNum)
{
  int pointsCnt = vertexLength;
  id = NULL;

  //レイヤー名
  if( layer->name != NULL ){
    name = strdup2(layer->name);
  }
  
  //ピボット座標
  pivot = new float[3];
  memcpy(pivot, layer->pivot, sizeof(float)*3);

  //バウンディングボックス（浮動小数→固定小数）
  memcpy(boundingBox, layer->boundingBox, sizeof(float)*6);

  //座標
  if( vertexOffset != -1 ){
    int pointsLength = pointsCnt * 3;
    float* pointsBuf = new float[pointsLength];
	int pointsBufLength = pointsLength * sizeof( float );
    memcpy(pointsBuf, layer->points->getFloats() + vertexOffset*3, pointsBufLength);
    points = new VertexBuffer( ShaderProgram::IATTRIBUTE_VERTEX, pointsBuf, pointsBufLength, pointsBufLength, 3 );
  }

  //カラー
  if( layer->vcolors != NULL && vertexOffset != -1) {
    int vcolorsLength = pointsCnt * 4;
    float* vcolorsBuf = new float[vcolorsLength];
	int vcolorsBufLength = vcolorsLength * sizeof( float );
    memcpy(vcolorsBuf, layer->vcolors->getFloats() + vertexOffset*4, vcolorsBufLength);
    vcolors = new VertexBuffer( ShaderProgram::IATTRIBUTE_COLOR, vcolorsBuf, vcolorsBufLength, vcolorsBufLength, 4 );
  }
  
  //法線
  if( layer->normals != NULL && vertexOffset != -1) {
    int normalsLength = pointsCnt * 3;
    float* normalsBuf = new float[normalsLength];
	int normalsBufLength = normalsLength * sizeof( float );
    memcpy(normalsBuf, layer->normals->getFloats() + vertexOffset*3, normalsBufLength);
    normals = new VertexBuffer( ShaderProgram::IATTRIBUTE_NORMAL, normalsBuf, normalsBufLength, normalsBufLength, 3 );
  }
  
 
  if( layer->matrixIndices != NULL && vertexOffset != -1) {
    //ウエイトのインデックス配列
    int matrixIndicesLength = pointsCnt * 4;
    byte* matrixIndicesBuf = new byte[matrixIndicesLength];
	int matrixIndicesBufLength = matrixIndicesLength * sizeof( byte );
    memcpy(matrixIndicesBuf, layer->matrixIndices->getBytes() + vertexOffset*4, matrixIndicesBufLength);
    matrixIndices = new VertexBuffer( ShaderProgram::IATTRIBUTE_MATRIX_INDICES, matrixIndicesBuf, matrixIndicesBufLength, matrixIndicesBufLength, 4 );

    //ウエイト値配列
    int matrixWeightsLength = pointsCnt * 4;
    float* matrixWeightsBuf = new float[matrixWeightsLength];
	int matrixWeightsBufLength = matrixWeightsLength * sizeof( float );
    memcpy(matrixWeightsBuf, layer->matrixWeights->getFloats() + vertexOffset*4, matrixWeightsBufLength);
    matrixWeights = new VertexBuffer( ShaderProgram::IATTRIBUTE_WEIGHTS, matrixWeightsBuf, matrixWeightsBufLength, matrixWeightsBufLength, 4 );
  }  

  // Weight names
  char* it;
  ArrayList* list = &layer->matrixIndicesName;
  foreach_element(list, it, char*){
    matrixIndicesName.add(strdup2(it));
  }

  // Polygon map
  for( int i=0; i<polygonMapsNum; i++ ){
    PolygonMap* pm = new PolygonMap();
    pm->loadFrom((PolygonMap*)layer->getPolygonMaps()->get(polygonMap), triangleOffset, triangleLength, vertexOffset, vertexLength,
      points, vcolors, normals, matrixIndices, matrixWeights, &matrixIndicesName);
    polygonMaps.add(pm);
  }
}

void Layer::loadForSpriteMergeGroup()
{
  id = NULL;

  //ピボット座標
  pivot = new float[3];
  memset(pivot, 0, sizeof(float)*3);
  
  //バウンディングボックス（浮動小数→固定小数）
  memset(boundingBox, 0, sizeof(float)*6);
  
  PolygonMap* pm = new PolygonMap();
  pm->loadForSpriteMergeGroup();
  polygonMaps.add(pm);
}

void Layer::createMergeBuffers(Layer* layer, int maxVertices, int polygonMapsNum)
{
  // Must have points
  int pointsLength = 3 * maxVertices;
  float* pointsBuf = new float[pointsLength];
  points = new VertexBuffer( ShaderProgram::IATTRIBUTE_VERTEX, pointsBuf, pointsLength * sizeof( float ), 0 , 3 );

  if( layer != NULL && layer->vcolors != NULL ){
    int vcolorsLength = 4 * maxVertices;
    float* vcolorsBuf = new float[vcolorsLength];
    vcolors = new VertexBuffer( ShaderProgram::IATTRIBUTE_COLOR, vcolorsBuf, vcolorsLength * sizeof( float ), 0 , 4 );
  }
  if( layer != NULL && layer->normals != NULL ){
    int normalsLength = 3 * maxVertices;
    float* normalsBuf = new float[normalsLength];
    normals = new VertexBuffer( ShaderProgram::IATTRIBUTE_NORMAL, normalsBuf, normalsLength * sizeof( float ), 0 , 3 );
  }
  if( layer != NULL && layer->matrixIndices != NULL ){
    int matrixIndicesLength = 4 * maxVertices;
    byte* matrixIndicesBuf = new byte[matrixIndicesLength];
    matrixIndices = new VertexBuffer( ShaderProgram::IATTRIBUTE_MATRIX_INDICES, matrixIndicesBuf, matrixIndicesLength * sizeof( byte ), 0 , 4 );
  }
  if( layer != NULL && layer->matrixWeights != NULL ){
    int matrixWeightsLength = 4 * maxVertices;
    float* matrixWeightsBuf = new float[matrixWeightsLength];
    matrixWeights = new VertexBuffer( ShaderProgram::IATTRIBUTE_WEIGHTS, matrixWeightsBuf, matrixWeightsLength * sizeof( float ), 0 , 4 );
  }
 
  // Create the polygonmap
  for( int i=0; i<polygonMapsNum; i++ ){
    PolygonMap* pm = (PolygonMap*)polygonMaps.get(i);
    if( layer == NULL ){
      pm->loadMergeGroup(NULL, maxVertices, 
			 points, vcolors, normals, matrixIndices, matrixWeights);
    } else {
      pm->loadMergeGroup((PolygonMap*)layer->polygonMaps.get(0), maxVertices, 
			 points, vcolors, normals, matrixIndices, matrixWeights);
    }
  }
}

void Layer::addPolygonMap(PolygonMap* pm, int targetPmId, Matrix* transform)
{
  //All except points
  appendAllButPoints(pm, targetPmId);

  points->append(pm->getPoints(), transform);
}

void Layer::addPolygonMap(PolygonMap* pm, int targetPmId, 
			  float x, float y, float z)
{
  //All except points
  appendAllButPoints(pm, targetPmId);

  points->append(pm->getPoints(), x, y, z);
}

void Layer::appendAllButPoints(PolygonMap* pm, int targetPmId)
{
  int voffset = points->getLength() / ( 3 * sizeof( float ) );

  if( vcolors ){
    vcolors->append(pm->getVcolors());
  }
  if( normals ){
    normals->append(pm->getNormals());
  }
  if( matrixIndices ){
    matrixIndices->append(pm->getMatrixIndices());
  }
  if( matrixWeights ){
    matrixWeights->append(pm->getMatrixWeights());
  }

  // Append uv to ALL pms
  PolygonMap* pit;
  foreach_element( getPolygonMaps(), pit, PolygonMap * ) {
    pit->appendUvs(pm);
  }

  // Append polygon info
  ((PolygonMap*)polygonMaps.get(targetPmId))->appendPolygonMap(pm, voffset);  
}

void Layer::addTriangles(int vertices, int triangles, const float* pointsData, 
			 const unsigned short* indices, const float* uvs)
{
  int voffset = points->getLength() / ( 3 * sizeof( float ) );

  ((PolygonMap*)polygonMaps.get(0))->
    appendTriangles(voffset, vertices, triangles, indices, uvs);

  points->append(pointsData, vertices*3);
}

void Layer::startGroup()
{
  points->clear();
  if( vcolors) vcolors->clear();
  if( normals ) normals->clear();
  if( matrixIndices ) matrixIndices->clear();
  if( matrixWeights ) matrixWeights->clear();

  PolygonMap* pit;
  foreach_element( getPolygonMaps(), pit, PolygonMap * ) {
    pit->startGroup();
  }
}

void Layer::endGroup()
{
  points->commitGroup();
  if( vcolors ) vcolors->commitGroup();
  if( normals ) normals->commitGroup();
  if( matrixIndices ) matrixIndices->commitGroup();
  if( matrixWeights ) matrixWeights->commitGroup();

  PolygonMap* pit;
  foreach_element( getPolygonMaps(), pit, PolygonMap * ) {
    pit->endGroup();
  }
}

bool Layer::loadInfo( const byte *data, int offset, int &outPointsCnt )
{
  //ヘッダ部分
  int nameLength = FileUtils::readByte( data, offset );            //レイヤー名文字列の長さ
  
  ItemNo = 0;
  ParentNo = FileUtils::readChar( data, offset );//親レイヤーのインデックス

  outPointsCnt = FileUtils::readInt( data, offset );//頂点の数
  int coordComponents = FileUtils::readByte( data, offset );//頂点座標の要素数
  int colorComponents = FileUtils::readByte( data, offset );//頂点カラーの要素数
  int normalComponents = FileUtils::readByte( data, offset );//頂点法線の要素数
  int tangentComponents = FileUtils::readByte( data, offset );//頂点接線の要素数
  int binormalComponents = FileUtils::readByte( data, offset );//頂点従接線の要素数
  int weightComponents = FileUtils::readByte( data, offset );//頂点ウエイトの要素数

  //未使用領域をスキップ
  offset += 4;

  //頂点座標の要素数が3じゃなければエラー
  if( coordComponents != 3 ) {
    etrace( "coordComponents is not 3" );

    return false;
  }

  //頂点カラーの要素数が4じゃなくてもエラー
  if( (colorComponents != 0) && (colorComponents != 4) ) {
    etrace( "colorComponents is not 4" );

    return false;
  }

  //頂点ウエイトの要素数が定数と一致してなくてもエラー
  if( (weightComponents != 0) && (weightComponents != MAX_VERTEX_UNITS) ) {
    etrace( "weightComponents is not %d", MAX_VERTEX_UNITS );

    return false;
  }
  
  //データ部分
  id = NULL;

  //レイヤー名
  name = FileUtils::readString( data, offset, nameLength );

  //ピボット座標
  pivot = FileUtils::readFloats( data, offset, coordComponents );

  //バウンディングボックス（浮動小数→固定小数）
  FileUtils::readFloats( data, offset, coordComponents * 2, boundingBox );

  //座標
  int pointsLength = outPointsCnt * coordComponents;
  float* pointsBuf = FileUtils::readFloats( data, offset, pointsLength );
  int pointsBufLength = pointsLength * sizeof( float );
  points = new VertexBuffer( ShaderProgram::IATTRIBUTE_VERTEX, pointsBuf, pointsBufLength, pointsBufLength, 3 );

  //カラー
  if( colorComponents > 0 ) {
    int vcolorsLength = outPointsCnt * colorComponents;
    float* vcolorsBuf = FileUtils::readFloats( data, offset, vcolorsLength );
	int vcolorsBufLength = vcolorsLength * sizeof ( float );
    vcolors = new VertexBuffer( ShaderProgram::IATTRIBUTE_COLOR, vcolorsBuf, vcolorsBufLength, vcolorsBufLength, 4 );
  }
  
  //法線
  if( normalComponents > 0 ) {
    int normalsLength = outPointsCnt * normalComponents;
    float* normalsBuf = FileUtils::readFloats( data, offset, normalsLength );
	int normalsBufLength = normalsLength * sizeof ( float );
    normals = new VertexBuffer( ShaderProgram::IATTRIBUTE_NORMAL, normalsBuf, normalsBufLength, normalsBufLength, 3 );
  }
  
  //接線をスキップ
  if( tangentComponents > 0 ) {
    offset += outPointsCnt * tangentComponents * sizeof( float );
  }
  
  //従接線をスキップ
  if( binormalComponents > 0 ) {
    offset += outPointsCnt * binormalComponents * sizeof( float );
  }
  
  if( weightComponents > 0 ) {
    //ウエイトのインデックス配列
    int matrixIndicesLength = outPointsCnt * weightComponents;
    byte* matrixIndicesBuf = FileUtils::readBytes( data, offset, matrixIndicesLength );
	int matrixIndicesBufLength = matrixIndicesLength * sizeof ( byte );
    matrixIndices = new VertexBuffer( ShaderProgram::IATTRIBUTE_MATRIX_INDICES, matrixIndicesBuf, matrixIndicesBufLength, matrixIndicesBufLength, 4 );

    //ウエイト値配列
    int matrixWeightsLength = outPointsCnt * weightComponents;
    float* matrixWeightsBuf = FileUtils::readFloats( data, offset, matrixWeightsLength );
	int matrixWeightsBufLength = matrixWeightsLength * sizeof ( float );
    matrixWeights = new VertexBuffer( ShaderProgram::IATTRIBUTE_WEIGHTS, matrixWeightsBuf, matrixWeightsBufLength, matrixWeightsBufLength, 4 );
  }

  return true;
}

bool Layer::loadWeightNames( const byte *data, int offset )
{
  //開始位置を保持
  int startPos = offset;

  //ウエイト名配列情報を取得
  int weightNum = FileUtils::readByte( data, offset );
  byte* nameLengths = FileUtils::readBytes( data, offset, weightNum ); //各ウエイト名文字列の長さ

  //全ウエイト名を取得（全部小文字に変換）
  for( int i = 0; i < weightNum; i++ ) {
    char* name = FileUtils::readString( data, offset, nameLengths[ i ] );
    matrixIndicesName.add( name );

    for( int j = 0; j < strlen(name); j++ ) {
      if( ( name[ j ] >= 'A') && (name[ j ] <= 'Z') ) {
	name[ j ] += 'a' - 'A';
      }
    }
  }

  delete[] nameLengths;
  return true;
}

bool Layer::loadPolygonMaps( const byte *data, int offset, Object *parent, int pointsCnt )
{
  int startPos = offset;

  int polygonMapsNum = FileUtils::readByte( data, offset );
  int* offsets;
  
  offsets = FileUtils::readInts( data, offset, polygonMapsNum * 2 );

  //全ポリゴンマップをロード
  for( int i = 0, j = 0; i < polygonMapsNum; i++, j += 2 ) {
    PolygonMap* newPolygonMap = new PolygonMap();
        
    if( !newPolygonMap->load( data, startPos + offsets[ j ], startPos + offsets[ j + 1 ], pointsCnt,
			      points, vcolors, normals, matrixIndices, matrixWeights, &matrixIndicesName) ) {
      delete[] offsets;
      return false;
    }

    polygonMaps.add(newPolygonMap);
  }

  delete[] offsets;
  return true;
}

ArrayList* Layer::getPolygonMaps()
{
  return &polygonMaps;
}

bool Layer::initRenderEnv()
{
  // 全バッファーをロード (複数回ロードが呼ばれてもOK!)
  points->loadVbo();
  if( vcolors ) vcolors->loadVbo();
  if( normals ) normals->loadVbo();
  if( matrixIndices ) matrixIndices->loadVbo();
  if( matrixWeights ) matrixWeights->loadVbo();
  
  // 各ポリゴンマップをロード
  PolygonMap* pm;
  foreach_element(getPolygonMaps(), pm, PolygonMap*){
    if( !pm->initRenderEnv() ){
      return false;
    }
  }

  return true;
}

void Layer::uninitRenderEnv()
{
  // 全てのバッファーをアンロード
  points->unloadVbo();
  if( vcolors ) vcolors->unloadVbo();
  if( normals ) normals->unloadVbo();
  if( matrixIndices ) matrixIndices->unloadVbo();
  if( matrixWeights ) matrixWeights->unloadVbo();
  
  // 全てのポリゴンマップを開放
  PolygonMap* pm;
  foreach_element(getPolygonMaps(), pm, PolygonMap*){
    pm->uninitRenderEnv();
  }
}

char* Layer::getName()
{
  return name;
}

float* Layer::getBoundingBox()
{
  return boundingBox;
}

/**
 * @brief	オリジナルデータを解放
 */
void Layer::releaseOriginalData() {
  points->releaseOriginalData();
  if( vcolors ) vcolors->releaseOriginalData();
  if( normals ) normals->releaseOriginalData();
  if( matrixIndices ) matrixIndices->releaseOriginalData();
  if( matrixWeights ) matrixWeights->releaseOriginalData();

  PolygonMap *pm;

  foreach_element( getPolygonMaps(), pm, PolygonMap * ) {
    pm->releaseOriginalData();
  }
}

const int pointComponentSize = 3 * sizeof( float );

int Layer::getPointsNum()
{
  return (points != NULL) ? points->getLength() / pointComponentSize : 0;
}

float* Layer::getPointsBuffer()
{
  return points->getFloats();
}

float* Layer::getVcolorsBuffer()
{
  return vcolors->getFloats();
}

float* Layer::getNormalsBuffer()
{
  return normals->getFloats();
}

byte* Layer::getMatrixIndicesBuffer()
{
  return matrixIndices->getBytes();
}

float* Layer::getMatrixWeightsBuffer()
{
  return matrixWeights->getFloats();
}

void Layer::setMatrixIndicesNames( ArrayList* names )
{
  // Release all current names
  char* miName = NULL;
  foreach_element(&matrixIndicesName, miName, char*){
    delete miName;
  }

  matrixIndicesName.clear();

  // Add all names
  foreach_element(names, miName, char*){
    matrixIndicesName.add(strdup2(miName));
  }
  
}

void Layer::commitPointsChanges()
{
  points->commitGroup();
}

void Layer::commitVcolorsChanges()
{
  vcolors->commitGroup();
}

void Layer::commitNormalsChanges()
{
  normals->commitGroup();
}

void Layer::commitMatrixIndicesChanges()
{
  matrixIndices->commitGroup();
}

void Layer::commitMatrixWeightsChanges()
{
  matrixWeights->commitGroup();
}
