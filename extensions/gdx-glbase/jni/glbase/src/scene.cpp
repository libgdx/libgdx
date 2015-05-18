/**
 * @file scene.cpp
 * @brief wrap class for .bs file format
 **/

#include "scene.h"
#include "glbase.h"
#include "macros.h"

Command::Command() : 
  Channels(0), MorphEnvelopes(0), ZoomFactorEnvelopes(0), Keys(0)
{ 
  LayerNo=-1; 
  ItemNo=0; 
  ParentNo=0;

  CommandName = NULL;
  Id = NULL;
  FileName = NULL;
  BoneName = NULL;
  BoneWeightMapName = NULL;
  LightName = NULL;
  CameraName = NULL;
  SurfaceName = NULL;
  TextureName = NULL;
}

Command::~Command()
{
  for(int i=0;i<Channels.getSize();i++){
    delete( (Channel*)Channels.get(i) );
  }
  for(int i=0;i<MorphEnvelopes.getSize();i++){
    delete( (Channel*)MorphEnvelopes.get(i) );
  }
  for( int i = 0 ; i < ZoomFactorEnvelopes.getSize() ; i++ ){
    delete( (Channel*)ZoomFactorEnvelopes.get( i ) );
  }
  for( int i = 0; i < Keys.getSize(); i++ ){
    delete((ArrayI*)Keys.get(i));
  }

  delete[] CommandName;
  delete[] Id;
  delete[] FileName;
  delete[] BoneName;
  delete[] BoneWeightMapName;
  delete[] LightName;
  delete[] CameraName;
  delete[] SurfaceName;
  delete[] TextureName;
}

Scene::Scene() : 
  Commands(0)
{
  Id = NULL;
  Version = NULL;
  MetaInfo = NULL;
}

Scene::Scene(const char* buff,int len) :
  Commands(0)
{
  Id = NULL;
  Version = NULL;
  MetaInfo = NULL; 
  setSceneDat(buff,len); 
}

Scene::~Scene()
{
  for(int i=0;i<Commands.getSize();i++){
    delete( (Command*)Commands.get(i) );
  }
  
  delete[] Id;
  delete[] Version;
  delete[] MetaInfo;
}

int Scene::setSceneDat(const char* buff,int len)
{
  
  if(len<0){
    len=strlen(buff);
  }

  // JSONをパース
  JObj* p=JObj::parse(buff,len);

  if(!p){
    etrace("Scene::setSceneDat() failed to parse input string");
    return 0;
  }
  trace("Scene::setSceneDat() parsing done. (%d Bytes)",len);

  int retval=setUp(p);

  delete( p );
  return 1;
}

int Scene::setUp(JObj* p)
{  
  JObj *q,*r,*t;
  OBJ_SETSTR(p,Id,"Id");
  OBJ_SETSTR(p,Version,"Version");
  OBJ_SETSTR(p,MetaInfo,"MetaInfo");
  OBJ_SETINT(p,FirstFrame,"FirstFrame");
  OBJ_SETINT(p,LastFrame,"LastFrame");
  OBJ_SETINT(p,CurrentFrame,"CurrentFrame");
  OBJ_SETINT(p,FramesPerSecond,"FramesPerSecond");
  OBJ_SETINT(p,CurrentCamera,"CurrentCamera");

  if(p == NULL){ etrace("Scene::setUp() p is NULL"); return 0; };

  p=p->getHashPVal("Commands");
  if(p && p->isArray()){
    Commands.resize( p->getLen() );
    for(int i=0;i<p->getLen();i++){
      Command* com=new Command();
      Commands.add(com);
      
      q=p->getArrayPVal(i);
      OBJ_SETSTR(q,com->CommandName,"CommandName");
      OBJ_SETSTR(q,com->Id,"Id");
      OBJ_SETINT(q,com->LayerNo,"LayerNo");
      OBJ_SETUINT(q,com->ItemNo,"ItemNo");
      OBJ_SETUINT(q,com->ParentNo,"ParentNo");
      OBJ_SETSTR(q,com->FileName,"FileName");
        
      if(q == NULL){ etrace("Scene::setUp() q is NULL"); return 0; };

      r=q->getHashPVal("Channels");
      if(r && r->isArray()){
        com->Channels.resize( r->getLen() );
        for(int j=0;j<r->getLen();j++){
          Channel* cnl=new Channel();
          com->Channels.add(cnl);

          t=r->getArrayPVal(j);
          OBJ_SETSTR(t,cnl->Id,"Id");
          OBJ_SETARRAY2(t,cnl->Keys,"Keys");
          OBJ_SETARRAY(t,cnl->Behaviors,"Behaviors");
        }
      }
      OBJ_SETINT(q,com->MorphTarget, "MorphTarget");
      OBJ_SETINT(q,com->MorphSurface,"MorphSurface");
      OBJ_SETINT(q,com->MTSEMorphing,"MTSEMorphing");

      r=q->getHashPVal("MorphEnvelopes");
      if(r && r->isArray()){
        com->MorphEnvelopes.resize( r->getLen() );
        for(int j=0;j<r->getLen();j++){
          Channel* cnl=new Channel();
          com->MorphEnvelopes.add(cnl);

          t=r->getArrayPVal(j);
          OBJ_SETSTR(t,cnl->Id,"Id");
          OBJ_SETARRAY2(t,cnl->Keys,"Keys");
          OBJ_SETARRAY(t,cnl->Behaviors,"Behaviors");
        }
      }

      OBJ_SETSTR(q,com->BoneName,"BoneName");
      OBJ_SETSTR(q,com->BoneWeightMapName,"BoneWeightMapName");

      for( int i = 0; com->BoneWeightMapName && i < strlen(com->BoneWeightMapName); i++ ) {
        if( (com->BoneWeightMapName[ i ] >= 'A') && (com->BoneWeightMapName[ i ] <= 'Z') ) {
          com->BoneWeightMapName[ i ] += 'a' - 'A';
        }
      }

      OBJ_SETINT(q,com->BoneRestLength,"BoneRestLength");
      OBJ_SETARRAY(q,com->BoneRestPosition,"BoneRestPosition");
      OBJ_SETARRAY(q,com->BoneRestDirection,"BoneRestDirection");
      OBJ_SETARRAY(q,com->BoneHLimits,"HLimits");
      OBJ_SETARRAY(q,com->BonePLimits,"PLimits");
      OBJ_SETARRAY(q,com->BoneBLimits,"BLimits");

      OBJ_SETARRAY(q,com->PivotPosition,"PivotPosition");
      OBJ_SETARRAY(q,com->PivotRotation,"PivotRotation");

      OBJ_SETSTR(q,com->LightName,"LightName");
      OBJ_SETARRAY(q,com->LightColor,"LightColor");

      OBJ_SETSTR(q,com->CameraName,"CameraName");

      r=q->getHashPVal("ZoomFactorEnvelope");
      if(r && r->isArray()){
        com->ZoomFactorEnvelopes.resize( r->getLen() );
        for(int j=0;j<r->getLen();j++){
          Channel* cnl=new Channel();
          com->ZoomFactorEnvelopes.add(cnl);

          t=r->getArrayPVal(j);
          OBJ_SETSTR(t,cnl->Id,"Id");
          OBJ_SETARRAY2(t,cnl->Keys,"Keys");
          OBJ_SETARRAY(t,cnl->Behaviors,"Behaviors");
        }
      }
      else
      {
        OBJ_SETINT(q,com->ZoomFactor,"ZoomFactor");
      }

      OBJ_SETINT(q,com->FocalDistance,"FocalDistance");

      OBJ_SETINT(q,com->FogType,     "FogType");
      OBJ_SETINT(q,com->FogMinDist,  "FogMinDist");
      OBJ_SETINT(q,com->FogMaxDist,  "FogMaxDist");
      OBJ_SETINT(q,com->FogMinAmount,"FogMinAmount");
      OBJ_SETINT(q,com->FogMaxAmount,"FogMaxAmount");
      OBJ_SETARRAY(q,com->FogColor,  "FogColor");
      OBJ_SETINT(q,com->BackdropFog, "BackdropFog");
        
      OBJ_SETSTR(q,com->SurfaceName, "SurfaceName");
      OBJ_SETSTR(q,com->TextureName, "TextureName");
      OBJ_SETINT(q,com->Mode,        "Mode");
      OBJ_SETARRAY2(q,com->Keys,     "Keys");
    }
  }
  return 1;
}

void Scene::print(){
  char buf[1024];

  trace("------------------Scene Information : %s------------------",(char*)_loadFile_FileName);
  trace("Id='%s'   Version='%s'",(char*)Id,(char*)Version);
  trace("MetaInfo='%s'",(char*)MetaInfo);
  trace("FirstFrame=%d",     FirstFrame);
  trace("LastFrame=%d",      LastFrame);
  trace("CurrentFrame=%d",   CurrentFrame);
  trace("FramesPerSecond=%d",FramesPerSecond);
  trace("CurrentCamera=%d",  CurrentCamera);

  trace("Commands.len=%d",Commands.getSize());
  for(int i=0;i<Commands.getSize();i++){
    Command* com = (Command*)Commands.get(i);
    trace("------Commands[%d]",i);
    trace("CommandName='%s'   Id='%s'",(char*)com->CommandName,(char*)com->Id);
    trace("LayerNo=%d   ItemNo=0x%X   ParentNo=0x%X",com->LayerNo,com->ItemNo,com->ParentNo);
    trace("FileName='%s'",(char*)com->FileName);
    trace("------Commands[%d]->Channels.len=%d",i,com->Channels.getSize());

    for(int j=0;j<com->Channels.getSize();j++){
      trace("------Commands[%d]->Channels[%d]",i,j);
      Channel* cn = (Channel*)com->Channels.get(j);

      trace("Id='%s'",(char*)cn->Id);
      trace("Keys(len=%d):[",cn->Keys.getSize());
      for(int k=0;k<cn->Keys.getSize();k++){
	trace("    %s",((ArrayI*)cn->Keys.get(k))->str(buf, 1024));
      }
      trace("]");
     
      trace("Behaviors=%s", cn->Behaviors.str(buf, 1024));
    }
    trace("MorphTarget=%d",com->MorphTarget);
    trace("MorphSurface=%d",com->MorphSurface);
    trace("MTSEMorphing=%d",com->MTSEMorphing);

    trace("------Commands[%d]->MorphEnvelopes.len=%d",i,com->MorphEnvelopes.getSize());
    for(int j=0;j<com->MorphEnvelopes.getSize();j++){
      trace("------Commands[%d]->MorphEnvelopes[%d]",i,j);
      Channel* cn = (Channel*)com->MorphEnvelopes.get(j);
      
      trace("Id='%s'",(char*)cn->Id);
      trace("Keys(len=%d):[",cn->Keys.getSize());
      for(int k=0;k<cn->Keys.getSize();k++){
	trace("    %s",	((ArrayI*)cn->Keys.get(k))->str(buf, 1024));
      }
      trace("]");
      trace("Behaviors=%s", cn->Behaviors.str(buf, 1024));
    }
    
    trace("BoneName='%s'",(char*)com->BoneName);
    trace("BoneWeightMapName='%s'",(char*)com->BoneWeightMapName);
    trace("BoneRestLength='%d'",com->BoneRestLength);

    trace("BoneRestPosition='%s'",(char*)com->BoneRestPosition.str(buf, 1024));
    trace("BoneRestDirection='%s'",(char*)com->BoneRestDirection.str(buf, 1024));
    trace("BoneHLimits='%s'",(char*)com->BoneHLimits.str(buf, 1024));
    trace("BonePLimits='%s'",(char*)com->BonePLimits.str(buf, 1024));
    trace("BoneBLimits='%s'",(char*)com->BoneBLimits.str(buf, 1024));

    trace("LightName='%s'",(char*)com->LightName);
    trace("LightColor=%s",(char*)com->LightColor.str(buf, 1024));

    trace("CameraName='%s'",(char*)com->CameraName);
    trace("ZoomFactor=%d",com->ZoomFactor);
    trace("FocalDistance=%d",com->FocalDistance);

    trace("FogType=%d",com->FogType);
    trace("FogMinDist=%d",com->FogMinDist);
    trace("FogMaxDist=%d",com->FogMaxDist);
    trace("FogMinAmount=%d",com->FogMinAmount);
    trace("FogMaxAmount=%d",com->FogMaxAmount);
    trace("FogColor=%s",(char*)com->FogColor.str(buf, 1024));
    trace("BackdropFog=%d",com->BackdropFog);
      
    trace("SurfaceName=%s",(char*)com->SurfaceName);
    trace("TextureName=%s",(char*)com->TextureName);
    trace("Mode=%d",com->Mode);
    trace("Keys(len=%d):[",com->Keys.getSize());
    for(int k=0;k<com->Keys.getSize();k++){
      trace("  %s", ((ArrayI*)com->Keys.get(k))->str(buf, 1024));
    }
  }
}
