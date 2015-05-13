/**
 * @file scene.h
 * @brief wrap class for .bs file format
 **/
#pragma once

#include "json.h"
#include "arrayList.h"
#include "arrays.h"

/**
 * アニメーションチャンネル (移動x,y,z,回転x,y,zスケールx,y,z情報)
 **/
class Channel {
public:
  Channel() : Keys(0){ Id = NULL; }
  ~Channel(){
    for( int i=0; i<Keys.getSize(); i++ ){
      delete (ArrayI*)Keys.get(i);
    }
    delete[] Id;
  }
  char* Id;
  ArrayList Keys;
  ArrayI Behaviors;
};

/**
 * JSON　コマンドオブジェクトタイプ
 **/
class Command
{
 public:
  Command();
  ~Command();
  
  // Info
  char* CommandName;
  char* Id;
  int    LayerNo;
  int    ItemNo;
  int    ParentNo;
  char* FileName;
  ArrayList Channels;

  int    MorphTarget;
  int    MorphSurface;
  int    MTSEMorphing;
  ArrayList MorphEnvelopes;

  // Bones
  char* BoneName;
  char* BoneWeightMapName;
  int   BoneRestLength;
  ArrayI  BoneRestPosition;
  ArrayI  BoneRestDirection;
  ArrayI  BoneHLimits;
  ArrayI  BonePLimits;
  ArrayI  BoneBLimits;

  // Pivot
  ArrayI   PivotPosition;
  ArrayI   PivotRotation;

  // Light
  char*  LightName;
  ArrayI   LightColor;

  char*  CameraName;
  int    ZoomFactor;

  ArrayList ZoomFactorEnvelopes;

  int    FocalDistance;

  // Fog
  int     FogType;
  int     FogMinDist;
  int     FogMaxDist;
  int     FogMinAmount;
  int     FogMaxAmount;
  ArrayI    FogColor;
  int     BackdropFog;


  // UV Anime
  char*   SurfaceName;
  char*   TextureName;
  int     Mode;
  ArrayList Keys;

  Command& operator = (const Command& v);
  Command(const Command& v);
};


/**
 * .bsファイルラップクラス
 **/
class Scene
{
  char* _loadFile_FileName;

public:
  Scene();
  Scene(const char* buff,int len=-1);

  ~Scene();

  int setUp(JObj* p);
  int setSceneDat(const char* buff,int len=-1);

  // Info
  char* Id;
  char* Version;
  char* MetaInfo;
  int   CurrentCamera;
  ArrayList Commands;

  Scene& operator = (const Scene& v);
  Scene(const Scene& v);
  void print();

  int FirstFrame;
  int LastFrame;
  int CurrentFrame;
  int FramesPerSecond;
};
