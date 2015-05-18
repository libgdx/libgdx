/**
 * @file fileUtils.cpp
 * @brief fileUtils implementation
 **/

#include "fileUtils.h"
#include <string.h>

byte FileUtils::readByte( const byte *buffer, int &offset ) {
  byte data = buffer[ offset ];

  offset++;

  return data;
}

char FileUtils::readChar( const byte *buffer, int &offset ) {
  char data = (char)buffer[ offset ];

  offset += sizeof( char );

  return data;
}

int FileUtils::readInt( const byte *buffer, int &offset ) {
  int data = *((int *)&buffer[ offset ]);

  offset += sizeof( int );

  return data;
}

unsigned short FileUtils::readUshort( const byte *buffer, int &offset )
{
  unsigned short data = *((unsigned short*)&buffer[ offset ]);

  offset += sizeof( unsigned short );

  return data;
}

byte* FileUtils::readBytes( const byte *buffer, int &offset, int length ) {
  if( length <= 0 ) {
    return NULL;
  } else {
    byte* outdata = new byte[ length ];

    memcpy( outdata, &buffer[ offset ], length );
    offset += length;
    return outdata;
  }
}

char* FileUtils::readString( const byte *buffer, int &offset, int length ) {
  if( length <= 0 ) {
    return NULL;
  } else {
    char* outdata = new char[ length + 1 ];
    int bytesLength = sizeof( char ) * length;

    memcpy( outdata, &buffer[ offset ], bytesLength );
    outdata[ length ] = '\0';
    offset += bytesLength;

    return outdata;
  }
}

/**
 * ushort配列を読み込む
 */
unsigned short* FileUtils::readUshorts( const byte *buffer, int &offset, int length ) {
  if( length <= 0 ) {
    return NULL;
  } else {
    unsigned short* outdata = new unsigned short[ length ];
    int bytesLength = sizeof( unsigned short ) * length;

    memcpy( outdata, &buffer[ offset ], bytesLength );
    offset += bytesLength;
    return outdata;
  }
}

/**
 * int配列を読み込む
 */
int* FileUtils::readInts( const byte *buffer, int &offset, int length ) {
  if( length <= 0 ) {
    return NULL;
  } else {
    int* outdata = new int[ length ];
    int bytesLength = sizeof( int ) * length;

    memcpy( outdata, &buffer[ offset ], bytesLength );
    offset += bytesLength;
    return outdata;
  }
}

/**
 * float配列を読み込む
 */
float* FileUtils::readFloats( const byte *buffer, int &offset, int length ) {
  if( length <= 0 ) {
    return NULL;
  } else {
    float* outdata = new float[ length ];
    int bytesLength = sizeof( float ) * length;

    memcpy( outdata, &buffer[ offset ], bytesLength );
    offset += bytesLength;
    return outdata;
  }
}

void FileUtils::readFloats( const byte *buffer, int &offset, int length, float* outData ) {
  if( length <= 0 ) return;

  memcpy( outData, &buffer [ offset ], sizeof(float) * length);
  offset += sizeof(float) * length;
}
