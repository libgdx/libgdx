/**
 * @file polygonBuffer.cpp
 * @brief Represents a vbo element array buffer
 **/

#include "polygonBuffer.h"
#include "glbase.h"
#include "macros.h"

#include <string.h>

using namespace std;


const float PolygonBuffer::kGrowth = 0.75f;

PolygonBuffer::PolygonBuffer( unsigned short *data, int dataLength, int elementsCnt ) {
  this->data.shorts = data;
  this->dataLength = dataLength;
  this->elementsCnt = elementsCnt;
  this->type = GL_UNSIGNED_SHORT;
  this->vboId = -1;
  this->sizeOfType = sizeof( unsigned short );
}

PolygonBuffer::PolygonBuffer( byte *data, int dataLength, int elementsCnt ) {
  this->data.bytes = data;
  this->dataLength = dataLength;
  this->elementsCnt = elementsCnt;
  this->type = GL_UNSIGNED_BYTE;
  this->vboId = -1;
  this->sizeOfType = sizeof( byte );
}

PolygonBuffer::~PolygonBuffer() {
  if( vboId != -1 ){
    unloadVbo();
  }

  releaseOriginalData();
}

bool PolygonBuffer::loadVbo() {
  // すでにロード済であれば、スキップ
  if( vboId != -1 ) return true;

  GLOP( glGenBuffers( 1, &vboId ) );

  if( (dataLength > 0) && (elementsCnt > 0) ) {
    //処理効率化のため、GLBaseのBinder経由でバインド
    GLBase::get()->getBinder()->bindPolygons( this );

	//転送
    GLOP( glBufferData( GL_ELEMENT_ARRAY_BUFFER, elementsCnt * sizeOfType, data.bytes, GL_STATIC_DRAW ) );
  }

  return true;
}

void PolygonBuffer::unloadVbo() {
  if( vboId == -1 ) return;
  
  UNLOAD_BUFFER_VBO( vboId );
  vboId = -1;
}

void PolygonBuffer::bind() {
  GLOP( glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, vboId ) );
}

void PolygonBuffer::draw( int drawCnt ) {
  if( vboId != -1 ) {
    //処理効率化のため、GLBaseのBinder経由でバインド
    GLBase::get()->getBinder()->bindPolygons( this );

    //描画処理は三角のみ対応
    GLOP( glDrawElements( GL_TRIANGLES, (drawCnt < 0) ? elementsCnt : drawCnt, type, 0 ) );
  }
}

void PolygonBuffer::clear() {
  elementsCnt = 0;
}

void PolygonBuffer::adjustData( int addDataLength ) {
  int length = elementsCnt * sizeOfType;
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

void PolygonBuffer::append( PolygonBuffer *polygonBuffer, int vertexOffset ) {
  if( type == GL_UNSIGNED_SHORT ) {
    append( polygonBuffer->getShorts(), polygonBuffer->getElementsCnt(), vertexOffset );
  } else if( type == GL_UNSIGNED_BYTE ) {
    append( polygonBuffer->getBytes(), polygonBuffer->getElementsCnt(), vertexOffset );
  }
}

void PolygonBuffer::append( const unsigned short *appendData, int appendElementsCnt, int vertexOffset ) {
  adjustData( appendElementsCnt * sizeof( short ) );

  int startPos = max( elementsCnt, 0 );
  int addShortsCnt = min( appendElementsCnt, elementsCnt + appendElementsCnt );
  unsigned short *dstData = data.shorts + startPos;

  for( int i = 0; i < addShortsCnt; i++ ) {
    dstData[ i ] = appendData[ i ] + vertexOffset;
  }

  elementsCnt += appendElementsCnt;
}

void PolygonBuffer::append( const byte *appendData, int appendElementsCnt, int vertexOffset ) {
  adjustData( appendElementsCnt * sizeof( byte ) );

  int startPos = max( elementsCnt, 0 );
  int addBytesCnt = min( appendElementsCnt, elementsCnt + appendElementsCnt );
  byte *dstData = data.bytes + startPos;

  for( int i = 0; i < addBytesCnt; i++ ) {
    dstData[ i ] = appendData[ i ] + vertexOffset;
  }

  elementsCnt += appendElementsCnt;
}

void PolygonBuffer::commitGroup() {
  if( vboId != -1 ) {
    //処理効率化のため、GLBaseのBinder経由でバインド
    GLBase::get()->getBinder()->bindPolygons( this );

	//転送
	int length = elementsCnt * sizeOfType;

	GLOP( glBufferData( GL_ELEMENT_ARRAY_BUFFER, length, NULL, GL_STREAM_DRAW ) );
	GLOP( glBufferSubData( GL_ELEMENT_ARRAY_BUFFER, 0, length, data.bytes ) );
  }
}

unsigned short *PolygonBuffer::getShorts() {
  return data.shorts;
}

byte *PolygonBuffer::getBytes() {
  return data.bytes;
}

int PolygonBuffer::getElementsCnt() {
	return elementsCnt;
}

/**
 * @brief	オリジナルデータを解放
 */
void PolygonBuffer::releaseOriginalData() {
  ARR_RELEASE( data.bytes );
  dataLength = 0;
}
