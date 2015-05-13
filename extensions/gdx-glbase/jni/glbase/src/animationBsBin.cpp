/**
 * @file animationBsBin.cpp
 * @brief Represents an animation resource composed of a bs/bin file pair
 **/
#include <string.h>
#include "animationBsBin.h"
#include "scene.h"
#include "bone.h"
#include "glbase.h"
#include "macros.h"

AnimationBsBin::AnimationBsBin(char* bsFile, int bsFileLength, byte* binFile, int binFileLength)
{
  this->bsFile = bsFile;
  this->bsFileLength = bsFileLength;
  this->binFile = binFile;
  this->binFileLength = binFileLength;
  this->dataMode = RESTMOVE_ONLY;
}

AnimationBsBin::~AnimationBsBin()
{
}

bool AnimationBsBin::load()
{
  // Sceneをbsファイルからロードする
  Scene* scene = new Scene(bsFile, bsFileLength);
  if( scene == NULL ) return false;
  trace("Scene loaded.");

  // アニメーションの情報をコピーする
  this->length = (scene->LastFrame - scene->FirstFrame) + 1;
  this->fps = scene->FramesPerSecond >> 16;
  this->frameOffset = scene->FirstFrame;

  // Load matrix data
  trace("Loading matrices... ");
  loadMatrices(binFile);
  trace("Matrices loaded.");

  // ボーンを作成する
  createBones(scene);
  trace("Bones created.");
  
  // シーンを開放
  delete scene;

  return true;
}

Matrix *AnimationBsBin::getLayerMatrix() {
  return &layerMatrix;
}

void AnimationBsBin::loadMatrices(byte* binFile)
{
  int * num = (int*)&(binFile[0]);
  int bonesNum = num[0];

  // Copy rest matrices
  int offset = (4+bonesNum)*sizeof(int);
  restMatrices = new Matrix[bonesNum];
  copyMatrices(restMatrices, binFile + offset, bonesNum, FIXED);
  offset += bonesNum * MATRIX_LENGTH;

  moveMatrices = new Matrix[bonesNum*length];
  copyMatrices(moveMatrices, binFile + offset, bonesNum*length, FIXED);
  offset += length * bonesNum * MATRIX_LENGTH;
}

void AnimationBsBin::createBones(Scene* scene)
{
  ArrayList* coms = &scene->Commands;

  // 全てのコマンドを回し、LoadObjectLayerとAddBoneコマンドだけを処理する
  for(int i=0;i<coms->getSize();i++){
    Command* cmd = (Command*)coms->get(i);

	//一度でもLoadObjectLayerの処理に成功した場合は、他のLoadObjectLayerは無視
	if( (this->dataMode == RESTMOVE_ONLY) && (strcmp( cmd->CommandName, "LoadObjectLayer" ) == 0) ) {
		if( getFirtLayerMatrix( cmd ) ) {
		  this->dataMode = LAYERRESTMOVE_ONLY;
		}

		continue;
	}

    // 正常なAddBoneだけ
    if( strcmp(cmd->CommandName, "AddBone") != 0 ) continue;
    if( cmd->Channels.getSize() != 9 ) continue;

    // 新しいボーン生成
    Bone* bone = new Bone();
    bone->ItemNo = cmd->ItemNo;
    bone->ParentNo = cmd->ParentNo;

    bone->Name = strdup2(cmd->BoneName);
    bone->WeightMapName = strdup2(cmd->BoneWeightMapName);

    // Set REST matrix once and for all
    bone->getRestMatrix()->copyFrom(&restMatrices[bones.getSize()]);

    bones.add(bone);
    // Channels非対応
  }

  // 親設定
  for(int i=0; i<bones.getSize(); i++){
    Bone* child = (Bone*)bones.get(i);
    unsigned int n = child->ParentNo;
    if(n<0x40000000) continue;
    
    for(int j=0; j<bones.getSize(); j++){
      Bone* parent = (Bone*)bones.get(j);
      if( parent->ItemNo == n ){
	child->Parent = parent;
	break;
      }
    }
  }
  
  // ルートボーン設定
  setRootBones();
}

#define PI2DEG1 5730
#define PI2DEG2 100

bool AnimationBsBin::getFirtLayerMatrix( Command *cmd ) {
  if( cmd->Channels.getSize() < 9 ) {
    return false;
  }

  int pos[ 3 ];
  int rot[ 3 ];
  int scl[ 3 ];
  Channel *channel;

  //移動
  for( int i = 0; i < 3; i++ ) {
    channel = (Channel *)cmd->Channels.get( i );

    if( channel->Keys.getSize() <= 0 ) {
      return false;
    }

	ArrayI *key = (ArrayI *)channel->Keys.get( 0 );

	if( key->len <= 0 ) {
      return false;
	}

	pos[ i ] = key->el[ 0 ];
  }

  //回転
  for( int i = 0; i < 3; i++ ) {
    channel = (Channel *)cmd->Channels.get( i + 3 );

    if( channel->Keys.getSize() <= 0 ) {
      return false;
    }

	ArrayI *key = (ArrayI *)channel->Keys.get( 0 );

	if( key->len <= 0 ) {
      return false;
	}

	rot[ i ] = key->el[ 0 ];
  }

  //拡縮
  for( int i = 0; i < 3; i++ ) {
    channel = (Channel *)cmd->Channels.get( i + 6 );

    if( channel->Keys.getSize() <= 0 ) {
      return false;
    }

	ArrayI *key = (ArrayI *)channel->Keys.get( 0 );

	if( key->len <= 0 ) {
      return false;
	}

	scl[ i ] = key->el[ 0 ];
  }

  //初期値なら計算しない
  if(
      (pos[ 0 ] == 0) &&
      (pos[ 1 ] == 0) &&
      (pos[ 2 ] == 0) &&
      (rot[ 0 ] == 0) &&
      (rot[ 1 ] == 0) &&
      (rot[ 2 ] == 0) &&
      (scl[ 0 ] == 65536) &&
      (scl[ 1 ] == 65536) &&
      (scl[ 2 ] == 65536) ) {
    return false;
  }

  layerMatrix.setIdentity();
  layerMatrix.translate( XTOF( pos[ 0 ] ), XTOF( pos[ 1 ] ), XTOF( pos[ 2 ] ) );
  
  layerMatrix.rotateY( XTOF( -PI2DEG1 * ((rot[ 0 ] + PI2DEG2 / 2) / PI2DEG2) ) );
  layerMatrix.rotateX( XTOF( -PI2DEG1 * ((rot[ 1 ] + PI2DEG2 / 2) / PI2DEG2) ) );
  layerMatrix.rotateZ( XTOF( PI2DEG1 * ((rot[ 2 ] + PI2DEG2 / 2) / PI2DEG2) ) );
  
  layerMatrix.scale( XTOF( scl[ 0 ] ), XTOF( scl[ 1 ] ), XTOF( scl[ 2 ] ) );
  return true;
}
