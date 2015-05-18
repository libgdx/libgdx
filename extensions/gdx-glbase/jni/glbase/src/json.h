/**
 * @file json.h
 * @brief json format parse module
 **/
#pragma once

#include <string.h>
#include <stdlib.h>
#include "types.h"
#include "macros.h"

// マクロ定義
#define OBJ_SETSTR(p,attr,name) {		\
    if(p){					\
      JObj* tt=p->getHashPVal(name);		\
      if(tt && tt->isString()) {		\
	attr = strdup2(tt->getString());		\
      }						\
    }						\
  }

#define OBJ_SETINT(p,attr,name){     \
    if(p){			     \
      JObj* tt=p->getHashPVal(name); \
      if(tt && tt->isInt()){	     \
	attr=tt->getInt();	     \
      }				     \
    }				     \
  }

#define OBJ_SETINTARRAY(p,attr,name){		\
    if(p){					\
      JObj* tt=p->getHashPVal(name);		\
      if(tt && tt->isArray()){			\
	attr.resize(tt->getLen());		\
	if( tt->getIntArray( attr.el ) == -1){	\
	  tt->copyIntArray(attr.el);		\
	}					\
      }						\
    }						\
  }


#define OBJ_SETUINT(p,attr,name){    \
    if(p){			     \
      JObj* tt=p->getHashPVal(name); \
      if(tt && tt->isUInt()){	     \
	attr=tt->getUInt();	     \
      }				     \
    }				     \
  }

#define OBJ_SETARRAY(p,attr,name){			 \
    if(p){						 \
      JObj* tt=p->getHashPVal(name);			 \
      if(tt && tt->isArray()){				 \
	attr.resize(tt->getLen());			 \
	tt->copyIntArray(attr.el);			 \
      }							 \
    }							 \
  }

#define OBJ_SETARRAY2(p,attr,name){				 \
    if(p){							 \
      JObj* tt=p->getHashPVal(name);				 \
      if(tt && tt->isArray()){					 \
    attr.resize( tt->getLen() );				 \
	for(int ii=0;ii<tt->getLen();ii++){			 \
	  JObj* uu=tt->getArrayPVal(ii);			 \
	  if(uu && uu->isArray()){				 \
	    ArrayI* newarray = new ArrayI(uu->getLen());	 \
	    uu->copyIntArray(newarray->el);			 \
	    attr.add(newarray);					 \
	  }							 \
	}							 \
      }								 \
    }								 \
  }



// JSon Types
#define JOBJ_NONE   0x00 //not supported
#define JOBJ_BOOL   0x01 //not supported
#define JOBJ_INT    0x02
#define JOBJ_UINT   0x03
#define JOBJ_FLOAT  0x04 //not supported
#define JOBJ_STRING 0x05
#define JOBJ_ARRAY  0x06
#define JOBJ_HASH   0x07

#define JOBJ_STR_INDENT 4
#define JOBJ_SPACES " \n\t\r\v\f"

/**
 * JSonオブジェトタイプ
 **/
class JObj {
  int type;
  int len;
  int len0;
  char** key;

  JObj** val;
  int   iData;
  unsigned int uData;
  float fData;
  char* sData;

  int * piData;
  unsigned short * psData;

 public:
  // Constructors, Destructor, setUp function
  JObj(){ setUp(); }
  JObj(const int   v){ setUp(); setInt(v); }
  JObj(const unsigned int v){ setUp(); setUInt(v); }
  JObj(const float v){ setUp(); setFloat(v); }
  JObj(const char* v){ setUp(); setString(v); }
  
  JObj(const int* v , int srclen ){ setUp();setIntArray(v , srclen ); }
  JObj(const unsigned short* v , int srclen ){ setUp(); setShortArray(v , srclen ); }


  /**
   * 初期化
   **/
  void setUp(int t=JOBJ_NONE){
    type=t; len=0; len0=0; key=NULL; val=NULL; sData=NULL;
    piData = NULL;
    psData = NULL;
  }


  ~JObj();
  
  /**
   * Prints the object
   **/
  void print();
  
  /**
   * Obtains a string representation of the object
   **/
  char* str(int depth=0,int is_simple=0);
  
  // Judging type
  int isBool()   const{ return (type==JOBJ_BOOL); }
  int isInt()    const{ return (type==JOBJ_INT); }
  int isUInt()   const{ return (type==JOBJ_UINT); }
  int isFloat()  const{ return (type==JOBJ_FLOAT); }
  int isString() const{ return (type==JOBJ_STRING); }
  int isArray()  const{ return (type==JOBJ_ARRAY); }
  int isHash()   const{ return (type==JOBJ_HASH); }
  
  // Getting actual value
  int getBool() const;
  int getInt() const;
  unsigned int getUInt() const;

  int getIntArray(int * v ) const;
  int getShortArray( unsigned short * v ) const;
  int copyFixedToFloatArray2(float * v ) const;
  int copyFixedToFloatArrayWithIdx2(float * v,int colnum,int idxcnt) const;

  float getFloat() const;
  const char* getString() const;
  int getLen() const{ return len; }
  JObj* getPVal (int i);
  JObj* getArrayPVal (const int i);
  char** getHashKeys(){ return key; }
  char*  getHashKey (const int i);
  JObj*  getHashPVal (const char* k);
  
  void setBool  (const int   v);
  void setInt   (const int   v);
  
  void setIntArray( const int * v , int len );
  void setShortArray( const unsigned short * v , int len );

  void setUInt  (const unsigned int   v);
  void setFloat (const float v);
  void setString(const char* v);
  void setArray (){ type=JOBJ_ARRAY; }
  void setHash  (){ type=JOBJ_HASH; }
  void addHashPVal(const char* k,JObj*& pv);
  void addArrayPVal(JObj*& pv);
  int copyIntArray(int*& v,int offset=0) const;
  int copyIntArray(unsigned short*& v,int offset=0) const;
  
  int copyFixedToFloatArray(float*& v,int offset=0) const;
  int copyFixedToFloatArrayWithIdx(float*& v,int colnum,int idxcnt,int offset=0) const;
  
  static JObj* parse(const char* s){ int n=0; return JObj::parse(s,n,STRLEN(s)); }
  static JObj* parse(const char* s,int len_s){ int n=0; return JObj::parse(s,n,len_s); }
  static JObj* parse(const char* s,int& n,int len_s,bool is_utf8 = false);
  static JObj* parseEx(const byte* s,int len_s){ int n = 0; return JObj::parseEx(s,n,len_s); };
  static JObj* parseEx(const byte* s,int& n,int len_s,bool is_utf8 = false);

  // Miscellaneous functions
  static void skipSpaces(int&i,const char* s,int len_s);
  static int isAlnumChar(char c);
};
