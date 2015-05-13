/**
 * @file renderQueue.cpp
 * @brief renderQueue implementation
 **/

#include <math.h>
#include <string.h>

#include "arrayList.h"
#include "glbase.h"
#include "macros.h"
#include "matrix.h"
#include "myUniforms.h"
#include "renderQueue.h"
#include "shaderProgram.h"
#include "texture.h"

using namespace std;


RenderQueue::RenderQueue()
{
  // 初期フォグ
  fogColor[0] = 1.0f;
  fogColor[1] = 1.0f;
  fogColor[2] = 1.0f;
  fogNear = 5.0f;
  fogFar = 50.0f;

  // View/projection
  view.setIdentity();
  projection.setIdentity();
}

RenderQueue::~RenderQueue()
{
}

void RenderQueue::execRender()
{
  // VP行列を準備する
  getMVStack()->clear();
  getMVStack()->push( getView() );

  // 全ての登録レンダーを実行する
  renderList.execRender( this );
}

void RenderQueue::registerDrawCall(DrawCall* drawCall)
{
  renderList.registerDrawCall(drawCall);
}


MatrixStack* RenderQueue::getMVStack()
{
  return &mvStack;
}

void RenderQueue::setProjection(Matrix const* projection)
{
  this->projection.copyFrom(projection);

  // Near とFarを逆算して、保存する
  float A = ((Matrix*)projection)->getMatrixPointer()[14];
  float B = ((Matrix*)projection)->getMatrixPointer()[10];
  frustumFar = B / (A+1);
  frustumNear = B / (A-1);
}
 
void RenderQueue::setView(Matrix const* view)
{
  this->view.copyFrom(view);
}

void RenderQueue::setFog(float fogColor[3], float fogNear, float fogFar)
{
  memcpy( this->fogColor, fogColor, sizeof( float ) * 3 );
  this->fogNear = fogNear;
  this->fogFar = fogFar;
}

void RenderQueue::bindUniforms( map<int, MyUniformValue*>* myUniforms, int* viewport )
{
  int nUniModelViewProjectionMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_MODEL_VIEW_PROJECTION_MATRIX);
  int nUniNormalMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_NORMAL_MATRIX);
  int nUniViewRotationMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_VIEW_ROTATION_MATRIX);
  int nUniModelViewMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_MODEL_VIEW_MATRIX);
  int nUniModelViewMatrix3Vec4 = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_MODEL_VIEW_MATRIX_3VEC4);
  int nUniProjectionMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_PROJECTION_MATRIX);
  int nUniInverseModelViewMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_INVERSE_MODEL_VIEW_MATRIX);
  int nUniModelLightProjectionMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_MODEL_LIGHT_PROJECTION_MATRIX);
  int nUniModelLightMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_MODEL_LIGHT_MATRIX);
  int nUniLightProjectionMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_LIGHT_PROJECTION_MATRIX);
  int nUniShadowMapMatrix = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_SHADOW_MAP_MATRIX);
  int nUniTimeSeconds = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_TIME_SECONDS);
  int nUniViewportRect = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_VIEWPORT_RECT);
  int nUniViewportReciprocal = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_VIEWPORT_RECIPROCAL);
  int nUniShadowMapInfo = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_SHADOW_MAP_INFO);
  int nUniShadowDarkness = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_SHADOW_DARKNESS);
  int nUniFogColor = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_FOG_COLOR);
  int nUniFogRange = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_FOG_RANGE);
  int nUniDepthRange = GLBase::get()->getShaderTable()->getCPUniformLocation(ShaderProgram::IUNIFORM_DEPTH_RANGE);

  if( nUniModelViewProjectionMatrix >= 0 ) {
    Matrix mvp;

    getModelViewProjection( &mvp );
    GLOP( glUniformMatrix4fv( nUniModelViewProjectionMatrix, 1, GL_FALSE, mvp.getMatrixPointer() ) );
  }

  if( nUniNormalMatrix >= 0 ) {
    float safNormalMatrix[ 12 ];

	getModelView()->to3x4( safNormalMatrix );
    GLOP( glUniformMatrix3fv( nUniNormalMatrix, 1, GL_FALSE, safNormalMatrix ) );
  }

  if( nUniViewRotationMatrix >= 0 ) {
    float safViewRotationMatrix[ 12 ];

    getView()->to3x4( safViewRotationMatrix );
    GLOP( glUniformMatrix3fv( nUniViewRotationMatrix, 1, GL_FALSE, safViewRotationMatrix ) );
  }

  if( nUniModelViewMatrix >= 0 ) {
	float* safModelViewMatrix = getModelView()->getMatrixPointer();

    GLOP( glUniformMatrix4fv( nUniModelViewMatrix, 1, GL_FALSE, safModelViewMatrix ) );
  }

  if( nUniModelViewMatrix3Vec4 >= 0 ) {
    float safModelViewMatrix[ 12 ];
    int uniSize = GLBase::get()->getShaderTable()->getCPUniformSize(ShaderProgram::IUNIFORM_MODEL_VIEW_MATRIX_3VEC4);

    getModelView()->to3x4( safModelViewMatrix );
    GLOP( glUniform4fv( nUniModelViewMatrix3Vec4, uniSize, safModelViewMatrix ) );
  }

  if( nUniProjectionMatrix >= 0 ){
    float *pProjectionMatrix = getProjection()->getMatrixPointer();

    GLOP( glUniformMatrix4fv( nUniProjectionMatrix, 1, GL_FALSE, pProjectionMatrix ) );
  }

  if( nUniInverseModelViewMatrix >= 0 ) {
    Matrix modelViewInverted;

	modelViewInverted.copyFrom( getModelView() );
    modelViewInverted.invert();

    float* safInverseModelViewMatrix = modelViewInverted.getMatrixPointer();
    
    GLOP( glUniformMatrix4fv( nUniInverseModelViewMatrix, 1, GL_FALSE, safInverseModelViewMatrix ) );
  }
  
  if( nUniTimeSeconds >= 0 ) {
    double time = GLBase::get()->getTime() / 1000;
    time = time - floor( time / 60.0 ) * 60.0;

    GLOP( glUniform1f( nUniTimeSeconds, (float)time ) );
  }

  if( nUniViewportRect >= 0 ) {
    GLOP( glUniform4f( nUniViewportRect, (float)viewport[ 0 ], (float)viewport[ 1 ], (float)viewport[ 2 ], (float)viewport[ 3 ] ) );
  }

  if( nUniViewportReciprocal >= 0 ) {
    GLOP( glUniform2f( nUniViewportReciprocal, 1.0f / viewport[ 2 ], 1.0f / viewport[ 3 ] ) );
  }
  
  if( nUniFogColor >= 0 ){
    GLOP( glUniform3fv( nUniFogColor, 1, fogColor ) );
  }

  if( nUniFogRange >= 0 ) {
    GLOP( glUniform3f( nUniFogRange, fogNear, fogFar, fogFar - fogNear ) );
  }

  if( nUniDepthRange >= 0 ) {
    GLOP( glUniform3f( nUniDepthRange, frustumNear, frustumFar, frustumFar - frustumNear ) );
  }

  // MyUniforms
  if( myUniforms != NULL ) {
    for (map<int,MyUniformValue*>::iterator it=myUniforms->begin(); it!=myUniforms->end(); ++it){
      int id = it->first;
      MyUniformValue* val = it->second;
      
      switch( val->type ){
      case UTYPE_FLOAT: {
        float* values = val->value.vector.data;
		int uniformId = GLBase::get()->getShaderTable()->getCPMyUniformLocation(id);

		if( uniformId >= 0 ) {
          switch( val->value.vector.length ) {
          case 1: GLOP( glUniform1fv( uniformId, 1, values ) ); break;
          case 2: GLOP( glUniform2fv( uniformId, 1, values ) ); break;
          case 3: GLOP( glUniform3fv( uniformId, 1, values ) ); break;
          case 4: GLOP( glUniform4fv( uniformId, 1, values ) ); break;
          }
		}
        break;
      }

      case UTYPE_TEXTURE:
        Texture* texture = GLBase::get()->getTextureTable()->getTexture(val->value.texture.id);
        int samplerId = GLBase::get()->getShaderTable()->getCPMyUniformLocation(id);

		if( samplerId >= 0 ) {
          int glid = texture->glid;
          GLenum textureMode = texture->getTextureMode();
          int glactive = val->value.texture.glactive;

          // 2nd texture
          GLOP( glActiveTexture( glactive ) );
          // テクスチャーをバインド
          GLOP( glBindTexture(textureMode, glid) );
          // サンプラーをバインド
          GLOP( glUniform1i(samplerId, glactive - GL_TEXTURE0 ) );
		}
        break;
      }
    }
  }
}

void RenderQueue::unbindUniforms()
{
  // TODO
}

Matrix* RenderQueue::getProjection()
{
  return &projection;
}

Matrix* RenderQueue::getView()
{
  return &view;
}

/**
 * MVP行列を取得
 **/
void RenderQueue::getModelViewProjection( Matrix *out ) {
  out->copyFrom( getProjection() );
  out->multiply( getModelView() );
}

/**
 * VP行列を取得
 **/
void RenderQueue::getViewProjection( Matrix *out ) {
  out->copyFrom( getProjection() );
  out->multiply( getView() );
}

/**
 * MV両列を取得
 **/
Matrix *RenderQueue::getModelView() {
  return getMVStack()->top();
}

RenderList* RenderQueue::getRenderList()
{
  return &renderList;
}
