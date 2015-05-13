/*!
 * @file renderSettings.cpp
 *
 * @brief 描画設定クラス.
 *
 */

#include "json.h"
#include "renderPass.h"
#include "renderSettings.h"
#include "glbase.h"
#include "macros.h"

RenderSettings::RenderSettings() : passes( 128 ){

  reset();
}

char* RenderSettings::getId() {
  return id;
}

char* RenderSettings::getVersion() {
  return version;
}

ArrayList* RenderSettings::getPasses() {
  return &passes;
}

void RenderSettings::reset() {

  id = NULL;
  toolName = NULL;
  version = NULL;
  parsed = false;
  leftShiftBits = 16;
  passes.clear();
}

bool RenderSettings::setUp( const char *data ) {

  reset();

  if( data == NULL ) {
    etrace( "invalid param" );
    return false;
  }

  JObj* p = JObj::parse(data, strlen(data));

  if( p == NULL ) {
    etrace( "cannot parse" );
    return false;
  }

  JObj* passObjects;

  OBJ_SETSTR(p, id, "Id");
  OBJ_SETSTR(p, toolName, "ToolName");
  OBJ_SETSTR(p, version, "Version");
  OBJ_SETINT(p, leftShiftBits, "LeftShiftBits");
  passObjects = p->getHashPVal("Passes");

  if(
     (strcmp( toolName, "MultipassRenderManager" ) != 0) ||
     (leftShiftBits <1) ||
     (leftShiftBits > 30) ||
     (passObjects == NULL) ||
     (passObjects->getLen() < 1) ||
     (!passObjects->isArray())
     ) {

    etrace( "invalid format" );
    return false;
  }

  for( int i = 0; i < passObjects->getLen(); i++ ) {
    JObj *curPassObject = passObjects->getArrayPVal( i );

    RenderPass* renderPass = new RenderPass();
    if( renderPass->setUp( curPassObject, leftShiftBits ) ){
      passes.add( renderPass );
    } else {
      etrace("Error in render pass");
      return false;
    }
  }

  parsed = true;
  return true;
}

/**
 *float uniformの値を更新する
 **/
void RenderSettings::updateFloatUniform( int passIdx, int uniformIdx, float value )
{
  ArrayF &values = *((Uniform*)((RenderPass*)passes.get(passIdx))->getUniforms()->get(uniformIdx))->getValues();
  values[0] = value;
}

/**
 *vec2 uniformの値を更新する
 **/
void RenderSettings::updateVec2Uniform( int passIdx, int uniformIdx, float x, float y )
{
  ArrayF &values = *((Uniform*)((RenderPass*)passes.get(passIdx))->getUniforms()->get(uniformIdx))->getValues();
  values[0] = x;
  values[1] = y;
}

/**
 *vec3 uniformの値を更新する
 **/
void RenderSettings::updateVec3Uniform( int passIdx, int uniformIdx, float x, float y, float z )
{
  ArrayF &values = *((Uniform*)((RenderPass*)passes.get(passIdx))->getUniforms()->get(uniformIdx))->getValues();
  values[0] = x;
  values[1] = y;
  values[2] = z;
}

/**
 *vec4 uniformの値を更新する
 **/
void RenderSettings::updateVec4Uniform( int passIdx, int uniformIdx, float x, float y, float z, float w )
{
  ArrayF &values = *((Uniform*)((RenderPass*)passes.get(passIdx))->getUniforms()->get(uniformIdx))->getValues();
  values[0] = x;
  values[1] = y;
  values[2] = z;
  values[3] = w;
}

/**
 *uniformの一括更新
 **/
void RenderSettings::updateUniforms( int uniformsNum, int *passUniformIndices, float *values ) {
  for( int u = 0; u < uniformsNum; u++ ) {
    int passUniformOff = u * 2;
    int valueOff = u * 4;
    int passIdx = passUniformIndices[ passUniformOff ];
    int uniformIdx = passUniformIndices[ passUniformOff + 1 ];

    if( passIdx < passes.getSize() ) {
      RenderPass *pass = (RenderPass*)passes.get( passIdx );

      if( uniformIdx < pass->getUniforms()->getSize() ) {
	ArrayF &valuesDst = *((Uniform*)pass->getUniforms()->get(uniformIdx))->getValues();

	memcpy( valuesDst.el, &values[ valueOff ], sizeof( float ) * valuesDst.len );
      } else {
	etrace( "invalid uniform index %d at pass index %d for render settings: %s", uniformIdx, passIdx, id );
      }
    } else {
      etrace( "invalid pass index %d for render settings: %s", passIdx, id );
    }
  }
}

/**
 *  uniformの一括取得
 **/
void RenderSettings::getUniformSizesAndValues( int uniformsNum, int *passUniformIndices, int *sizes, float *values ) {
  for( int u = 0; u < uniformsNum; u++ ) {
    int passUniformOff = u * 2;
    int valueOff = u * 4;
    RenderPass *pass = (RenderPass*)passes.get( passUniformIndices[ passUniformOff ] );
    ArrayF &valuesDst = *((Uniform*)pass->getUniforms()->get(passUniformIndices[ passUniformOff + 1 ] ))->getValues();

    sizes[ u ] = valuesDst.len;
    memcpy( &values[ valueOff ], valuesDst.el, sizeof( float ) * valuesDst.len );
  }
}

/**
 *既にﾊﾟｰｽ済みなのか
 **/
bool RenderSettings::isAlreadyParsed(){
  return parsed;
}


/*!
 * @briefデストラクタ.
 */
RenderSettings::~RenderSettings() {
  delete[] id;
  delete[] toolName;
  delete[] version;
}

bool RenderSettings::containsOffscreenPasses()
{
  RenderPass* it;
  foreach_element( getPasses(), it, RenderPass* ){
    if( it->getTarget() != RenderEnums::TEXTYPE_DEFAULT ) return true;
  }

  return false;
}
