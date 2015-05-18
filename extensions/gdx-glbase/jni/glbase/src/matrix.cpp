/**
 * @file matrix.cpp
 * @brief 4x4 Matrix implementation
 **/

#include "matrix.h"
#include "string.h"
#include "stdio.h"
#include "math.h"
#include "macros.h"
#include "types.h"
#include "vectors.h"

void Matrix::setIdentity()
{
  data[4] = data[ 8] = data[12] = 0.0f;
  data[1] = data[ 9] = data[13] = 0.0f;
  data[2] = data[ 6] = data[14] = 0.0f;
  data[3] = data[ 7] = data[11] = 0.0f;
  data[0] = data[ 5] = data[10] = data[15] = 1.0f;
}

void Matrix::setZero()
{
  memset( data, 0, sizeof(data) );
}

void Matrix::setTranslation( float tx, float ty, float tz )
{
  data[3] = data[ 7] = data[11] = 0.0f;
  data[4] = data[ 8] = 0.0f; data[12] = tx;
  data[1] = data[ 9] = 0.0f; data[13] = ty;
  data[2] = data[ 6] = 0.0f; data[14] = tz;
  data[0] = data[ 5] = data[10] = data[15] = 1.0f;
}

void Matrix::setScale( float sx, float sy, float sz )
{
  data[0] = sx; data[ 5] = sy; data[10] = sz; data[15] = 1.0f;
  data[4] = data[ 8] = data[12] = 0.0f;
  data[1] = data[ 9] = data[13] = 0.0f;
  data[2] = data[ 6] = data[14] = 0.0f;
  data[3] = data[ 7] = data[11] = 0.0f;

}

void Matrix::setRotationX( float angle )
{
  float sinVal=sinf( DEG2RADF( angle ) );
  float cosVal=cosf( DEG2RADF( angle ) );

  data[0] = data[15] = 1.0f;
  data[5] = data[10] = cosVal;
  data[6] = sinVal;
  data[9] = -sinVal;

  data[1] = data[2] = data[3] = data[4] = 0.0f;
  data[7] = data[8] = data[11] = data[12] = 0.0f;
  data[13] = data[14] = 0.0f;
  
}

void Matrix::setRotationY( float angle )
{
  float sinVal=sinf( DEG2RADF( angle ) );
  float cosVal=cosf( DEG2RADF( angle ) );

  data[0] = data[10] = cosVal;
  data[2] = -sinVal;
  data[5] = data[15] = 1.0f;
  data[8] = sinVal;

  data[1] = data[3] = data[4] = data[6] = 0.0f;
  data[7] = data[9] = data[11] = data[12] = 0.0f;
  data[13] = data[14] = 0.0f;
}

void Matrix::setRotationZ( float angle )
{
  float sinVal=sinf( DEG2RADF( angle ) );
  float cosVal=cosf( DEG2RADF( angle ) );

  data[0]=data[5]=cosVal;
  data[1]=sinVal;
  data[4]=-sinVal;
  data[10]=data[15]=1.0f;

  data[2] = data[3] = data[6] = data[7] = 0.0f;
  data[8] = data[9] = data[11] = data[12] = 0.0f;
  data[13] = data[14] = 0.0f;
}


void Matrix::setFrustum( float l, float r, float b, float t, float n, float f )
{
  // パラメターエラー
  if( (l == r) || (b == t) || (n == f) ) return;
  
  data[ 0] = (2 * n) / (r - l); data[ 4] = 0.0f;              data[ 8] = (r + l) / (r - l);  data[12] = 0.0f;
  data[ 1] = 0.0f;              data[ 5] = (2 * n) / (t - b); data[ 9] = (t + b) / (t - b);  data[13] = 0.0f;
  data[ 2] = 0.0f;              data[ 6] = 0.0f;              data[10] = -(f + n) / (f - n); data[14] = -(2 * f * n) / (f - n);
  data[ 3] = 0.0f;              data[ 7] = 0.0f;              data[11] = -1.0f;              data[15] = 0.0f;
}
  
void Matrix::setOrtho( float l, float r, float b, float t, float n, float f )
{
  // パラメターエラー
  if( (l == r) || (b == t) || (n == f) ) return;
  
  data[ 0] = (2.0f) / (r - l); data[ 4] = 0.0f;              data[ 8] = 0.0f;            data[12] = -(r + l) / (r - l);
  data[ 1] = 0.0f;             data[ 5] = (2.0f) / (t - b);  data[ 9] = 0.0f;            data[13] = -(t + b) / (t - b);
  data[ 2] = 0.0f;             data[ 6] = 0.0f;              data[10] = -2.0f / (f - n); data[14] = -(f + n) / (f - n);
  data[ 3] = 0.0f;             data[ 7] = 0.0f;              data[11] = 0.0f;            data[15] = 1.0f;
}

void Matrix::setPerspective( float fovy, float aspectRatio, float near, float far )
{
  float ymax = near * tanf( fovy * M_PI / 360.0f );
  float xmax = ymax * aspectRatio;

  setFrustum( -xmax, xmax, -ymax, ymax, near, far );
}

void Matrix::translate( float tx, float ty, float tz )
{
  data[12] += (data[ 0] * tx) + (data[ 4] * ty) + (data[ 8] * tz);
  data[13] += (data[ 1] * tx) + (data[ 5] * ty) + (data[ 9] * tz);
  data[14] += (data[ 2] * tx) + (data[ 6] * ty) + (data[10] * tz);
}

void Matrix::scale( float sx, float sy, float sz )
{
  data[ 0] *= sx;
  data[ 4] *= sy;
  data[ 8] *= sz;

  data[ 1] *= sx;
  data[ 5] *= sy;
  data[ 9] *= sz;
  
  data[ 2] *= sx;
  data[ 6] *= sy;
  data[10] *= sz;
}

void Matrix::rotateX( float angle )
{
  float sinVal=sinf( DEG2RADF( angle ) );
  float cosVal=cosf( DEG2RADF( angle ) );

  float data4 = (data[ 4] *  cosVal) + (data[ 8] *  sinVal);
  data[8] = (data[ 4] * -sinVal) + (data[ 8] *  cosVal);
  
  float data5 = (data[ 5] *  cosVal) + (data[ 9] *  sinVal);
  data[9] = (data[ 5] * -sinVal) + (data[ 9] *  cosVal);
  
  float data6 = (data[ 6] *  cosVal) + (data[10] *  sinVal);
  data[10] = (data[ 6] * -sinVal) + (data[10] *  cosVal);

  data[4] = data4;
  data[5] = data5;
  data[6] = data6;
}

void Matrix::rotateY( float angle )
{
  float sinVal=sinf( DEG2RADF( angle ) );
  float cosVal=cosf( DEG2RADF( angle ) );

  float data0 = (data[ 0] *  cosVal) + (data[ 8] * -sinVal);
  data[ 8] = (data[ 0] *  sinVal) + (data[ 8] *  cosVal);
  
  float data1 = (data[ 1] *  cosVal) + (data[ 9] * -sinVal);
  data[ 9] = (data[ 1] *  sinVal) + (data[ 9] *  cosVal);
  
  float data2 = (data[ 2] *  cosVal) + (data[10] * -sinVal);
  data[10] = (data[ 2] *  sinVal) + (data[10] *  cosVal);

  data[0] = data0;
  data[1] = data1;
  data[2] = data2;
}

void Matrix::rotateZ( float angle )
{
  float sinVal=sinf( DEG2RADF( angle ) );
  float cosVal=cosf( DEG2RADF( angle ) );
  
  float data0 = (data[ 0] *  cosVal) + (data[ 4] *  sinVal);
  data[ 4] = (data[ 0] * -sinVal) + (data[ 4] *  cosVal);
  
  float data1 = (data[ 1] *  cosVal) + (data[ 5] *  sinVal);
  data[ 5] = (data[ 1] * -sinVal) + (data[ 5] *  cosVal);
  
  float data2 = (data[ 2] *  cosVal) + (data[ 6] *  sinVal);
  data[ 6] = (data[ 2] * -sinVal) + (data[ 6] *  cosVal);

  data[0] = data0;
  data[1] = data1;
  data[2] = data2;
}

void Matrix::lookAt( float* v3eye, float* v3center, float* v3up )
{
  float forward[3] = { v3center[0] - v3eye[0], v3center[1] - v3eye[1], v3center[2] - v3eye[2] };
  Vectors::normalize(forward);

  float side[3];
  Vectors::cross(forward, v3up, side);
  Vectors::normalize(side);

  float up[3];
  Vectors::cross(side, forward, up);
 
  setIdentity();
  data[0] = side[0];     data[4] = side[1];     data[8] = side[2];
  data[1] = up[0];       data[5] = up[1];       data[9] = up[2];
  data[2] = -forward[0]; data[6] = -forward[1]; data[10] = -forward[2];

  Matrix t;
  t.setTranslation( -v3eye[0], -v3eye[1], -v3eye[2] );
  
  multiply(&t);
}
  
void Matrix::transform3( float* vec3 )
{
  float tmp[ 3 ];
  float *m = data;

  memcpy( tmp, vec3, sizeof( tmp ) );
  vec3[ 0 ] = ( tmp[ 0 ] * m[ 0] ) + ( tmp[ 1 ] * m[ 4] ) + ( tmp[ 2 ] * m[ 8] ) + m[12];
  vec3[ 1 ] = ( tmp[ 0 ] * m[ 1] ) + ( tmp[ 1 ] * m[ 5] ) + ( tmp[ 2 ] * m[ 9] ) + m[13];
  vec3[ 2 ] = ( tmp[ 0 ] * m[ 2] ) + ( tmp[ 1 ] * m[ 6] ) + ( tmp[ 2 ] * m[10] ) + m[14];
}

void Matrix::transform4( float* vec4 )
{
  float tmp[ 4 ];
  float *m = data;

  memcpy( tmp, vec4, sizeof( tmp ) );
  vec4[ 0 ] = ( tmp[ 0 ] * m[ 0] ) + ( tmp[ 1 ] * m[ 4] ) + ( tmp[ 2 ] * m[ 8] ) + m[12];
  vec4[ 1 ] = ( tmp[ 0 ] * m[ 1] ) + ( tmp[ 1 ] * m[ 5] ) + ( tmp[ 2 ] * m[ 9] ) + m[13];
  vec4[ 2 ] = ( tmp[ 0 ] * m[ 2] ) + ( tmp[ 1 ] * m[ 6] ) + ( tmp[ 2 ] * m[10] ) + m[14];
  vec4[ 3 ] = ( tmp[ 0 ] * m[ 3] ) + ( tmp[ 1 ] * m[ 7] ) + ( tmp[ 2 ] * m[11] ) + m[15];
}

void Matrix::invert()
{
  float p[16];
  memcpy( p, data, 16 * sizeof(float) );

  data[ 0] = p[ 0];
  data[ 4] = p[ 1];
  data[ 8] = p[ 2];
  data[12] = -((p[12] * p[ 0]) + (p[13] * p[ 1]) + (p[14] * p[ 2]));
  
  data[ 1] = p[ 4];
  data[ 5] = p[ 5];
  data[ 9] = p[ 6];
  data[13] = -((p[12] * p[ 4]) + (p[13] * p[ 5]) + (p[14] * p[ 6]));
  
  data[ 2] = p[ 8];
  data[ 6] = p[ 9];
  data[10] = p[10];
  data[14] = -((p[12] * p[ 8]) + (p[13] * p[ 9]) + (p[14] * p[10]));

  data[ 3] = 0.0f;
  data[ 7] = 0.0f;
  data[11] = 0.0f;
  data[15] = 1.0f;
}

void Matrix::multiply(Matrix* operand)
{
  Matrix tmp;
  float * ret = tmp.data;
  const float * mult = operand->data;
  float* fMatrix = data;
  
  ret[ 0] = (fMatrix[ 0] * mult[ 0]) + (fMatrix[ 4] * mult[ 1]) + (fMatrix[ 8] * mult[ 2]) + (fMatrix[12] * mult[ 3]);
  ret[ 4] = (fMatrix[ 0] * mult[ 4]) + (fMatrix[ 4] * mult[ 5]) + (fMatrix[ 8] * mult[ 6]) + (fMatrix[12] * mult[ 7]);
  ret[ 8] = (fMatrix[ 0] * mult[ 8]) + (fMatrix[ 4] * mult[ 9]) + (fMatrix[ 8] * mult[10]) + (fMatrix[12] * mult[11]);
  ret[12] = (fMatrix[ 0] * mult[12]) + (fMatrix[ 4] * mult[13]) + (fMatrix[ 8] * mult[14]) + (fMatrix[12] * mult[15]);
  
  ret[ 1] = (fMatrix[ 1] * mult[ 0]) + (fMatrix[ 5] * mult[ 1]) + (fMatrix[ 9] * mult[ 2]) + (fMatrix[13] * mult[ 3]);
  ret[ 5] = (fMatrix[ 1] * mult[ 4]) + (fMatrix[ 5] * mult[ 5]) + (fMatrix[ 9] * mult[ 6]) + (fMatrix[13] * mult[ 7]);
  ret[ 9] = (fMatrix[ 1] * mult[ 8]) + (fMatrix[ 5] * mult[ 9]) + (fMatrix[ 9] * mult[10]) + (fMatrix[13] * mult[11]);
  ret[13] = (fMatrix[ 1] * mult[12]) + (fMatrix[ 5] * mult[13]) + (fMatrix[ 9] * mult[14]) + (fMatrix[13] * mult[15]);
  
  ret[ 2] = (fMatrix[ 2] * mult[ 0]) + (fMatrix[ 6] * mult[ 1]) + (fMatrix[10] * mult[ 2]) + (fMatrix[14] * mult[ 3]);
  ret[ 6] = (fMatrix[ 2] * mult[ 4]) + (fMatrix[ 6] * mult[ 5]) + (fMatrix[10] * mult[ 6]) + (fMatrix[14] * mult[ 7]);
  ret[10] = (fMatrix[ 2] * mult[ 8]) + (fMatrix[ 6] * mult[ 9]) + (fMatrix[10] * mult[10]) + (fMatrix[14] * mult[11]);
  ret[14] = (fMatrix[ 2] * mult[12]) + (fMatrix[ 6] * mult[13]) + (fMatrix[10] * mult[14]) + (fMatrix[14] * mult[15]);
  
  ret[ 3] = (fMatrix[ 3] * mult[ 0]) + (fMatrix[ 7] * mult[ 1]) + (fMatrix[11] * mult[ 2]) + (fMatrix[15] * mult[ 3]);
  ret[ 7] = (fMatrix[ 3] * mult[ 4]) + (fMatrix[ 7] * mult[ 5]) + (fMatrix[11] * mult[ 6]) + (fMatrix[15] * mult[ 7]);
  ret[11] = (fMatrix[ 3] * mult[ 8]) + (fMatrix[ 7] * mult[ 9]) + (fMatrix[11] * mult[10]) + (fMatrix[15] * mult[11]);
  ret[15] = (fMatrix[ 3] * mult[12]) + (fMatrix[ 7] * mult[13]) + (fMatrix[11] * mult[14]) + (fMatrix[15] * mult[15]);

  memcpy( data, ret, sizeof(data) );
}

void Matrix::premultiply(Matrix* operand)
{
  Matrix tmp;
  float * ret = tmp.data;
  const float * mult = data;
  float* fMatrix = operand->data;
  
  ret[ 0] = (fMatrix[ 0] * mult[ 0]) + (fMatrix[ 4] * mult[ 1]) + (fMatrix[ 8] * mult[ 2]) + (fMatrix[12] * mult[ 3]);
  ret[ 4] = (fMatrix[ 0] * mult[ 4]) + (fMatrix[ 4] * mult[ 5]) + (fMatrix[ 8] * mult[ 6]) + (fMatrix[12] * mult[ 7]);
  ret[ 8] = (fMatrix[ 0] * mult[ 8]) + (fMatrix[ 4] * mult[ 9]) + (fMatrix[ 8] * mult[10]) + (fMatrix[12] * mult[11]);
  ret[12] = (fMatrix[ 0] * mult[12]) + (fMatrix[ 4] * mult[13]) + (fMatrix[ 8] * mult[14]) + (fMatrix[12] * mult[15]);
  
  ret[ 1] = (fMatrix[ 1] * mult[ 0]) + (fMatrix[ 5] * mult[ 1]) + (fMatrix[ 9] * mult[ 2]) + (fMatrix[13] * mult[ 3]);
  ret[ 5] = (fMatrix[ 1] * mult[ 4]) + (fMatrix[ 5] * mult[ 5]) + (fMatrix[ 9] * mult[ 6]) + (fMatrix[13] * mult[ 7]);
  ret[ 9] = (fMatrix[ 1] * mult[ 8]) + (fMatrix[ 5] * mult[ 9]) + (fMatrix[ 9] * mult[10]) + (fMatrix[13] * mult[11]);
  ret[13] = (fMatrix[ 1] * mult[12]) + (fMatrix[ 5] * mult[13]) + (fMatrix[ 9] * mult[14]) + (fMatrix[13] * mult[15]);
  
  ret[ 2] = (fMatrix[ 2] * mult[ 0]) + (fMatrix[ 6] * mult[ 1]) + (fMatrix[10] * mult[ 2]) + (fMatrix[14] * mult[ 3]);
  ret[ 6] = (fMatrix[ 2] * mult[ 4]) + (fMatrix[ 6] * mult[ 5]) + (fMatrix[10] * mult[ 6]) + (fMatrix[14] * mult[ 7]);
  ret[10] = (fMatrix[ 2] * mult[ 8]) + (fMatrix[ 6] * mult[ 9]) + (fMatrix[10] * mult[10]) + (fMatrix[14] * mult[11]);
  ret[14] = (fMatrix[ 2] * mult[12]) + (fMatrix[ 6] * mult[13]) + (fMatrix[10] * mult[14]) + (fMatrix[14] * mult[15]);
  
  ret[ 3] = (fMatrix[ 3] * mult[ 0]) + (fMatrix[ 7] * mult[ 1]) + (fMatrix[11] * mult[ 2]) + (fMatrix[15] * mult[ 3]);
  ret[ 7] = (fMatrix[ 3] * mult[ 4]) + (fMatrix[ 7] * mult[ 5]) + (fMatrix[11] * mult[ 6]) + (fMatrix[15] * mult[ 7]);
  ret[11] = (fMatrix[ 3] * mult[ 8]) + (fMatrix[ 7] * mult[ 9]) + (fMatrix[11] * mult[10]) + (fMatrix[15] * mult[11]);
  ret[15] = (fMatrix[ 3] * mult[12]) + (fMatrix[ 7] * mult[13]) + (fMatrix[11] * mult[14]) + (fMatrix[15] * mult[15]);

  memcpy( data, ret, sizeof(data) );  
}

void Matrix::copyFrom(Matrix const* src)
{
  memcpy( data, src, sizeof(data) );
}

void Matrix::to3x4(float* out)
{
  out[ 0] = data[ 0]; out[ 3] = data[ 4]; out[ 6] = data[ 8]; out[ 9] = data[12];
  out[ 1] = data[ 1]; out[ 4] = data[ 5]; out[ 7] = data[ 9]; out[10] = data[13];
  out[ 2] = data[ 2]; out[ 5] = data[ 6]; out[ 8] = data[10]; out[11] = data[14];
}

float* Matrix::getMatrixPointer()
{
  return data;
}

void Matrix::toString(char* outBuffer)
{
  for( int i=0; i<4; i++ ){
    for( int j=0; j<4; j++ ){
      char buf[32];
	  memset( buf, 0, sizeof( buf ) );
      sprintf(buf, "%.2f  ", data[i*4+j]);
      strcat(outBuffer, buf);
    }
    strcat(outBuffer, "\n");
  }
}
