/**
 * @file shapeRenderer.cpp
 * @brief Methods for rendering simple geometry
 **/
#include "shapeRenderer.h"
#include "binder.h"
#include "glbase.h"
#include "macros.h"
#include "polygonBuffer.h"
#include "vertexBuffer.h"

void ShapeRenderer::initialize()
{
  // パーティクルのためのバッファーを準備
  loadParticleSystem();
}

void ShapeRenderer::loadParticleSystem()
{
  const int pointsCnt = PARTICLE_MAX * 4 * 3;
  const int trianglesCnt = PARTICLE_MAX * 2 * 3;
  const int uvsCnt = PARTICLE_MAX * 4 * 2;

  // 頂点
  float* pointsBuf = new float[pointsCnt];
  float* uvsBuf = new float[uvsCnt];
  unsigned short *trianglesBuf = new unsigned short[ trianglesCnt ];

  const GLfloat o = 1.0f / 128.0f;
  float z = o;

  const GLfloat points[] = {
    -1.0f, -1.0f, 0.0f,
    -1.0f,  1.0f, 0.0f,
    1.0f, -1.0f, 0.0f,
    1.0f,  1.0f, 0.0f };

  for( int i = 0; i < pointsCnt; i += 12 ) {
    for( int j = 0; j < 12; j += 3 ) {
      pointsBuf[ i + j     ] = points[ j     ];
      pointsBuf[ i + j + 1 ] = points[ j + 1 ];
      pointsBuf[ i + j + 2 ] = z;
    }
    z += o;
  }

  //triangles
  const unsigned short triangles[] = { 0, 2, 1, 2, 3, 1 };
  unsigned short n = 0;

  for( int i = 0; i < trianglesCnt; i += 6 ) {
    for( int j = 0; j < 6; j++ ) {
      trianglesBuf[ i + j ] = triangles[ j ] + n;
    }
    n += 4;
  }

  // uvs
  const GLfloat uvs[] = {
    0.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 0.0f,
    1.0f, 1.0f };

  for( int i = 0; i < uvsCnt; i += 8 ) {
    for( int j = 0; j < 8; j++ ) {
      uvsBuf[ i + j ] = uvs[ j ];
    }
  }


  // VBO登録
  int pointsBufLength = pointsCnt * sizeof( float );
  int uvsBufLength = uvsCnt * sizeof( float );

  particlePoints = new VertexBuffer( ShaderProgram::IATTRIBUTE_VERTEX, pointsBuf, pointsBufLength, pointsBufLength, 3 );
  particleUvs = new VertexBuffer( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0, uvsBuf, uvsBufLength, uvsBufLength, 2 );
  particlePoints->loadVbo();
  particleUvs->loadVbo();

  //particlePolygonsVBO登録
  particlePolygons = new PolygonBuffer( trianglesBuf, trianglesCnt * sizeof( unsigned short ), trianglesCnt );
  particlePolygons->loadVbo();
}

void ShapeRenderer::renderBBox( float* bbox )
{
  GLBase::get()->getBinder()->unbindPolygons();

  GLfloat vertices[ 24 ] = {
    
    bbox[ 0 ], bbox[ 1 ], bbox[ 2 ],//0.
    bbox[ 0 ], bbox[ 4 ], bbox[ 5 ],//1.
    bbox[ 0 ], bbox[ 4 ], bbox[ 2 ],//2.
    bbox[ 3 ], bbox[ 1 ], bbox[ 5 ],//3.
    bbox[ 0 ], bbox[ 1 ], bbox[ 5 ],//4.
    bbox[ 3 ], bbox[ 4 ], bbox[ 5 ],//5.
    bbox[ 3 ], bbox[ 4 ], bbox[ 2 ],//6.
    bbox[ 3 ], bbox[ 1 ], bbox[ 2 ],//7.
  };
  
  static const GLfloat normals[ 24 ] = {
    
    -0.577350f, -0.577350f, -0.577350f,
    -0.577350f,  0.577350f,  0.577350f,
    -0.577350f,  0.577350f, -0.577350f,
    0.577350f, -0.577350f,  0.577350f,
    -0.577350f, -0.577350f,  0.577350f,
    0.577350f,  0.577350f,  0.577350f,
    0.577350f,  0.577350f, -0.577350f,
    0.577350f, -0.577350f, -0.577350f,
  };
  
  static const GLubyte indices[ 36 ] = {

    0, 2, 1,
    0, 4, 3,
    4, 1, 5,
    2, 6, 5,
    0, 7, 6,
    7, 3, 5,
    0, 1, 4,
    0, 3, 7,
    4, 5, 3,
    2, 5, 1,
    0, 6, 2,
    7, 5, 6,
  };

  int nAttVertex = GLBase::get()->getShaderTable()->getCPAttributeLocation( ShaderProgram::IATTRIBUTE_VERTEX );
  int nAttNormal = GLBase::get()->getShaderTable()->getCPAttributeLocation( ShaderProgram::IATTRIBUTE_NORMAL );

  if( nAttVertex >= 0 ) {
    GLOP( glEnableVertexAttribArray( nAttVertex ) );
    GLOP( glVertexAttribPointer( nAttVertex, 3, GL_FLOAT, GL_FALSE, 0, vertices ) );
  }

  if( nAttNormal >= 0 ) {
    GLOP( glEnableVertexAttribArray( nAttNormal ) );
    GLOP( glVertexAttribPointer( nAttNormal, 3, GL_FLOAT, GL_FALSE, 0, normals ) );
  }

  GLOP( glDrawElements( GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, indices ) );

  if( nAttVertex >= 0 ) {
    GLOP( glDisableVertexAttribArray( nAttVertex ) );
  }

  if( nAttNormal >= 0 ) {
    GLOP( glDisableVertexAttribArray( nAttNormal ) );
  }
}

void ShapeRenderer::renderParticles( int numParticles )
{
  GLBase::get()->getBinder()->bindBuffer( particlePoints );
  GLBase::get()->getBinder()->bindBuffer( particleUvs );

  particlePolygons->draw( 6 * numParticles );

  GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_VERTEX );
  GLBase::get()->getBinder()->unbindBuffer( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0 );
}

void ShapeRenderer::renderFullScreen()
{
  renderRectangle( 1.0f, -1.0f, -1.0f, 1.0f );
}

void ShapeRenderer::renderRectangle( float top, float left, float bottom, float right )
{
  GLBase::get()->getBinder()->unbindPolygons();
  
  const GLfloat vertices[ 12 ] = {
    left, bottom, 0.0f,// 0.
    right, bottom, 0.0f,// 1.
    right,  top, 0.0f,// 2.
    left,  top, 0.0f,// 3.
  };

  float umax = 1.0f;
  float vmax = 1.0f;

  GLfloat texcoords[ 8 ] = {
    0.0f, 0.0f,
    umax, 0.0f,
    umax, vmax,
    0.0f, vmax,
  };

  const GLubyte indices[ 6 ] ={ 2, 0, 1, 3, 0, 2 };

  int nAttVertex = GLBase::get()->getShaderTable()->getCPAttributeLocation( ShaderProgram::IATTRIBUTE_VERTEX );
  int nAttTextureCoord0 = GLBase::get()->getShaderTable()->getCPAttributeLocation( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0 );

  if( nAttVertex >= 0 ) {
    GLOP( glEnableVertexAttribArray( nAttVertex ) );
    GLOP( glVertexAttribPointer( nAttVertex, 3, GL_FLOAT, GL_FALSE, 0, vertices ) );
  }

  if( nAttTextureCoord0 >= 0 ) {
    GLOP( glEnableVertexAttribArray( nAttTextureCoord0 ) );
    GLOP( glVertexAttribPointer( nAttTextureCoord0, 2, GL_FLOAT, GL_FALSE, 0, texcoords ) );
  }

  GLOP( glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, indices ) );

  if( nAttVertex >= 0 ) {
    GLOP( glDisableVertexAttribArray( nAttVertex ) );
  }
  
  if( nAttTextureCoord0 >= 0 ) {
    GLOP( glDisableVertexAttribArray( nAttTextureCoord0 ) );
  }
}

/**
 * 長方形スプライトを描画
 **/
void ShapeRenderer::renderSprite( float left, float top, float right, float bottom,
                                 float srcLeft, float srcTop, float srcRight, float srcBottom,
                                 Matrix* spriteTransform)
{
    GLBase::get()->getBinder()->unbindPolygons();
    
    const GLfloat vertices[ 12 ] = {
        left, bottom, 0.0f,// 0.
        right, bottom, 0.0f,// 1.
        right,  top, 0.0f,// 2.
        left,  top, 0.0f,// 3.
    };
    
    GLfloat texcoords[ 8 ] = {
        srcLeft, srcBottom,
        srcRight, srcBottom,
        srcRight, srcTop,
        srcLeft, srcTop,
    };
    
    const GLubyte indices[ 6 ] ={ 2, 0, 1, 3, 0, 2 };
    
    // Transform
    int nUnSpriteMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_SPRITE_MATRIX);
    if( nUnSpriteMatrix >= 0 ){
        float *pMvp = spriteTransform->getMatrixPointer();
        GLOP( glUniformMatrix4fv( nUnSpriteMatrix, 1, GL_FALSE, pMvp ) );
    }
    
    int nAttVertex = GLBase::get()->getShaderTable()->getCPAttributeLocation( ShaderProgram::IATTRIBUTE_VERTEX );
    int nAttTextureCoord0 = GLBase::get()->getShaderTable()->getCPAttributeLocation( ShaderProgram::IATTRIBUTE_TEXTURE_COORD0 );
    
    if( nAttVertex >= 0 ) {
        GLOP( glEnableVertexAttribArray( nAttVertex ) );
        GLOP( glVertexAttribPointer( nAttVertex, 3, GL_FLOAT, GL_FALSE, 0, vertices ) );
    }
    
    if( nAttTextureCoord0 >= 0 ) {
        GLOP( glEnableVertexAttribArray( nAttTextureCoord0 ) );
        GLOP( glVertexAttribPointer( nAttTextureCoord0, 2, GL_FLOAT, GL_FALSE, 0, texcoords ) );
    }

    GLOP( glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, indices ) );
    
    if( nAttVertex >= 0 ) {
        GLOP( glDisableVertexAttribArray( nAttVertex ) );
    }
    
    if( nAttTextureCoord0 >= 0 ) {
        GLOP( glDisableVertexAttribArray( nAttTextureCoord0 ) );
    }
}
