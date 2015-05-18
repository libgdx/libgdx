/**
 * @file vertexBuffer.cpp
 * @brief Represents a vbo vertex buffer of some shader attribute
 **/

#include <string.h>
#include "vertexBuffer.h"
#include "glbase.h"
#include "macros.h"
#include "matrix.h"

using namespace std;


const float VertexBuffer::kGrowth = 0.75f;

VertexBuffer::VertexBuffer( ShaderProgram::INDEX_ATTRIBUTES attribute, float* data, int dataLength, int length, int componentsNum )
{
  this->attribute = attribute;
  this->data.floats = data;
  this->dataLength = dataLength;
  this->length = length;
  this->type = GL_FLOAT;
  this->vboId = -1;
  this->componentsNum = componentsNum;
}

VertexBuffer::VertexBuffer( ShaderProgram::INDEX_ATTRIBUTES attribute, byte* data, int dataLength, int length, int componentsNum )
{
  this->attribute = attribute;
  this->data.bytes = data;
  this->dataLength = dataLength;
  this->length = length;
  this->type = GL_UNSIGNED_BYTE;
  this->vboId = -1;
  this->componentsNum = componentsNum;
}

VertexBuffer::~VertexBuffer()
{
  if( vboId != -1 ){
    unloadVbo();
  }

  releaseOriginalData();
}

bool VertexBuffer::loadVbo()
{
  // すでにロード済であれば、スキップ
  if( vboId != -1 ) return true;

  // If buffer is empty, generate only (merge groups)
  if( (dataLength <= 0) || (length <= 0) ) {
    GLOP( glGenBuffers( 1, &vboId ) );
  } else {
    LOAD_BUFFER_VBO(data.bytes, length, vboId, GL_ARRAY_BUFFER);
  }

  return true;
}

void VertexBuffer::unloadVbo()
{
  if( vboId == -1 ) return;
  
  UNLOAD_BUFFER_VBO(vboId);
  vboId = -1;
}

bool VertexBuffer::bind()
{
  int nAtt = GLBase::get()->getShaderTable()->getCPAttributeLocation(attribute);

  if( nAtt >= 0 && vboId != -1 ){
    GLOP( glEnableVertexAttribArray( nAtt ) );
    GLOP( glBindBuffer( GL_ARRAY_BUFFER, vboId ) );
    GLOP( glVertexAttribPointer( nAtt, componentsNum, type, GL_FALSE, 0, 0 ) );
    GLOP( glBindBuffer( GL_ARRAY_BUFFER, 0 ) );
	return true;
  }

  return false;
}

void VertexBuffer::unbind()
{
  int nAtt = GLBase::get()->getShaderTable()->getCPAttributeLocation(attribute);

  if( nAtt >= 0  ) {
    GLOP( glDisableVertexAttribArray( nAtt ) );
  }
}

void VertexBuffer::clear()
{
  length = 0;
}

void VertexBuffer::adjustData( int addDataLength ) {
  int minNewDataLength = length + addDataLength;

  if( (minNewDataLength <= 0) || (dataLength >= minNewDataLength) ) {
    return;
  }

  int newDataLength;

  if( dataLength <= 0 ) {
    newDataLength = addDataLength;
  } else {
    newDataLength = dataLength;

    do {
      newDataLength += max( (int)(newDataLength * kGrowth), 1 );
    } while( newDataLength < minNewDataLength );
  }

  byte *oldData = data.bytes;

  data.bytes = new byte[ newDataLength ];

  if( (dataLength > 0) && (length > 0) ) {
    memcpy( data.bytes, oldData, length );
  }

  dataLength = newDataLength;
  ARR_RELEASE( oldData );
}

void VertexBuffer::append(VertexBuffer* vbuffer)
{
  append( vbuffer->data.bytes, vbuffer->length );
}

void VertexBuffer::append(VertexBuffer* vbuffer, float x, float y, float z)
{
  adjustData( vbuffer->length );

  // Must be 3 float components
  int startPos = max( length, 0 ) / sizeof( float );
  int addFloatsCnt = min( vbuffer->length, length + vbuffer->length ) / sizeof( float );
  float *srcData = vbuffer->data.floats;
  float *dstData = data.floats + startPos;

  for( int i = 0, last = addFloatsCnt - 3; i <= last; i += 3 ) {
    dstData[ i ] = srcData[ i ] + x;
    dstData[ i + 1 ] = srcData[ i + 1 ] + y;
    dstData[ i + 2 ] = srcData[ i + 2 ] + z;
  }

  length += vbuffer->length;
}

void VertexBuffer::append(VertexBuffer* vbuffer, Matrix* transform)
{
  adjustData( vbuffer->length );

  // Must be 3 float components
  const int copyLength = sizeof( float ) * 3;
  int startPos = max( length, 0 ) / sizeof( float );
  int addFloatsCnt = min( vbuffer->length, length + vbuffer->length ) / sizeof( float );
  float *srcData = vbuffer->data.floats;
  float *dstData = data.floats + startPos;

  for( int i = 0, last = addFloatsCnt - 3; i <= last; i += 3 ) {
	memcpy( &dstData[ i ], &srcData[ i ], copyLength );
	transform->transform3( &dstData[ i ] );
  }

  length += vbuffer->length;
}

void VertexBuffer::append(const float* appendData, int numComponents)
{
  append( (const byte *)appendData, numComponents * sizeof( float ) );
}

void VertexBuffer::append(const byte* appendData, int numComponents)
{
  adjustData( numComponents );
  memcpy( data.bytes + max( length, 0 ), appendData, min( numComponents, length + numComponents ) );
  length += numComponents;
}

void VertexBuffer::commitGroup()
{
  GLOP( glBindBuffer( GL_ARRAY_BUFFER, vboId ) );
  GLOP( glBufferData( GL_ARRAY_BUFFER, length, NULL, GL_STREAM_DRAW ) );
  GLOP( glBufferSubData( GL_ARRAY_BUFFER, 0, length, data.bytes ) );
  GLOP( glBindBuffer( GL_ARRAY_BUFFER, 0 ) );
}


float* VertexBuffer::getFloats()
{
  return data.floats;
}

byte* VertexBuffer::getBytes()
{
  return data.bytes;
}

int VertexBuffer::getLength()
{
  return length;
}

ShaderProgram::INDEX_ATTRIBUTES VertexBuffer::getShaderAttribute()
{
  return attribute;
}

/**
 * @brief	オリジナルデータを解放
 */
void VertexBuffer::releaseOriginalData() {
  ARR_RELEASE( data.bytes );
  dataLength = 0;
}
