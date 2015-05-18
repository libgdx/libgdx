/**
 * @file json.cpp
 * @brief json format parse module implementation
 **/
#include "json.h"
#include "glbase.h"
#include "macros.h"

JObj::~JObj(){
  type = JOBJ_NONE;
  if( len )
    {
      if( key )
	{
	  for( int i = len - 1 ; i >= 0 ; i-- )
	    {
	      ARR_RELEASE( key[ i ] );
	      //ARR_RELEASE( val [ i ] );

	      if( val != NULL )
		{
		  //20100409cabansag追加.
		  SAFE_RELEASE( val[ i ] );
		}
	    }
	}
      else
	{
	  for( int i = len - 1 ; i >= 0 ; i-- )
	    {
	      if( val != NULL )
		{
		  //20100409cabansag追加.
		  SAFE_RELEASE( val[ i ] );
		}
	    }
	}
    }

  if( sData )
    {
      SFREE0( sData );
    }
  ARR_RELEASE( key );
  ARR_RELEASE( val );
  
  ARR_RELEASE( piData );
  ARR_RELEASE( psData );
  len = 0;
}


void JObj::print(){

  char* s=str();

  if(s == NULL){ ErrFunc(0,"JObj::print() str() returned NULL"); return; };

  int len=STRLEN(s); int ns=0;

  for(int i=0;i<len;i++){
    if(s[i]=='\n' || s[i]=='\r'){
      s[i]='\0';
      DBGPR(s+ns);
      ns=i+1;
    }
  }
  if(ns<len){
    DBGPR(s+ns);
  }
  ARR_RELEASE(s);
}

char* JObj::str(int depth,int is_simple){
  char* retval;
  int count,retlen;
  int len; 
  char** ss;
  
  switch(type){
  case JOBJ_NONE:
    MEM_ALLOC(retval,char*,char,7);
    if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
    STRCPY(retval,"(None)");
    return retval;
  case JOBJ_BOOL:
    MEM_ALLOC(retval,char*,char,2);
    if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
    retval[0]=(getBool() ? '1' : '0');
    retval[1]='\0';
    return retval;
  case JOBJ_INT:
    MEM_ALLOC(retval,char*,char,12);
    if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
    SNPRINTF(retval,11,"%d",getInt());
    return retval;
  case JOBJ_UINT:
    MEM_ALLOC(retval,char*,char,12);
    if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
    SNPRINTF(retval,11,"0x%X",getUInt());
    return retval;
  case JOBJ_FLOAT:
    MEM_ALLOC(retval,char*,char,128);
    if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
    SNPRINTF(retval,127,"%f",getFloat());
    return retval;
  case JOBJ_STRING:
    char c,cpre;
    const char* s; s=getString();
    if(s == NULL){ ErrFunc(0,"JObj::print() getString() returned NULL"); return NULL; };
    MEM_ALLOC(retval,char*,char,STRLEN(s)*2+2+1);
    if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
    count=0;
    retval[0]='"'; count+=1;
    for(int i=0;i<STRLEN(s);i++){
      c=s[i]; cpre=0;
      switch(c){
      case '"':
      case '\\':
	cpre='\\';
	break;
      case '\n':
	cpre='\\'; c='n';
	break;
      case '\r':
	cpre='\\'; c='r';
	break;
      };
      if(cpre){
	retval[count]=cpre;
	count+=1;
      }
      retval[count]=c;
      count+=1;
    }
    retval[count]='"'; count+=1;
    retval[count]='\0';
    return retval;
  case JOBJ_ARRAY:
  case JOBJ_HASH:
    len=getLen();
    if(is_simple || len==0){
      MEM_ALLOC(retval,char*,char,3);
      if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
      retval[0]=(type==JOBJ_ARRAY ? '[' : '{');
      retval[1]=(type==JOBJ_ARRAY ? ']' : '}');
      retval[2]='\0';
      return retval;
    }
    //ss=new char* [len];
    MEM_ALLOC(ss,char**,char*,len);
    if(ss == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); return NULL; };
    retlen=0;
    for(int i=0;i<len;i++){
      ss[i]=getPVal(i)->str(depth+1);
      retlen+=STRLEN(ss[i])+JOBJ_STR_INDENT*(depth+1)+2;
      if(type==JOBJ_HASH){
	retlen+=STRLEN(getHashKey(i))+3;
      }
    }
    retlen+=JOBJ_STR_INDENT*depth;
    retlen+=4;
    MEM_ALLOC(retval,char*,char,retlen+1);
    if(retval == NULL){ ErrFunc(0,"JObj::str() string memory not allocated"); ARR_RELEASE(ss); return NULL; };
    count=0;
    retval[0]=(type==JOBJ_ARRAY ? '[' : '{'); count+=1;
    retval[1]='\n'; count+=1;
    for(int i=0;i<len;i++){
      for(int j=0;j<=depth;j++){
	for(int k=0;k<JOBJ_STR_INDENT;k++){
	  retval[count]=' '; count+=1;
	}
      }
      if(type==JOBJ_HASH){
	STRCPY(retval+count,getHashKey(i)); count+=STRLEN(getHashKey(i));
	STRCPY(retval+count," : "); count+=3;
      }
      STRCPY(retval+count,ss[i]); count+=STRLEN(ss[i]);
      if(i<len-1){
	retval[count]=','; count+=1;
      }
      retval[count]='\n'; count+=1;
    }
    for(int j=0;j<depth;j++){
      for(int k=0;k<JOBJ_STR_INDENT;k++){
	retval[count]=' '; count+=1;
      }
    }
    retval[count]=(type==JOBJ_ARRAY ? ']' : '}'); count+=1;
    retval[count]='\0'; count+=1;
    for(int i=0;i<len;i++){
      ARR_RELEASE(ss[i]);
    }
    ARR_RELEASE(ss);
    return retval;
  }
  return NULL;
}

int JObj::getBool() const{
  ErrFunc(isBool(),"getBool() invalid type(%d)",type);
  return iData;
}
int JObj::getInt() const{
  ErrFunc(isInt(),"getInt() invalid type(%d)",type);
  return iData;
}
unsigned int JObj::getUInt() const{
  ErrFunc(isUInt(),"getUInt() invalid type(%d)",type);
  return uData;
}

int JObj::getIntArray( int * v ) const{
  ErrFunc(isArray(),"getIntArray() invalid type(%d)",type);

  if( piData != NULL )
    {
      MEMCPY( v , piData , len<<2 );
      return 0;
    }
  
  return -1;
}

int JObj::getShortArray( unsigned short * v ) const{
  ErrFunc(isArray(),"getShortArray() invalid type(%d)",type);
  if( psData != NULL )
    {
      MEMCPY( v , psData , len<<1 );
      return 0;
    }

  return -1;
}

int JObj::copyFixedToFloatArray2( float *v ) const {
  
  ErrFunc(isArray(),"copyFixedToFloatArray2() invalid type(%d)", type );
  
  if( piData != NULL )
    {
      for( int i = 0; i < len; i++ ) {	
	v[ i ] = XTOF( piData[ i ] );
      }
      return 0;
    }
  
  return -1;
}

int JObj::copyFixedToFloatArrayWithIdx2(float * v,int colnum,int idxcnt) const {
  
  ErrFunc(isArray(),"copyFixedToFloatArray2() invalid type(%d)", type );
  
  if( piData != NULL )
    {
      int ctr = 0;
      
      for( int i = 0; i < len; i++ ) {
	
	if( ctr < idxcnt ) {
	  
	  v[ i ] = (float)piData[ i ];
	}
	else {
	  
	  v[ i ] = XTOF( piData[ i ] );
	}
	
	ctr++;
	
	if( ctr >= colnum ) {
	  
	  ctr = 0;
	}
      }
      
      return 0;
    }
  
  return -1;
}

float JObj::getFloat() const{
  ErrFunc(isFloat(),"getFloat() invalid type(%d)",type);
  return fData;
}
const char* JObj::getString() const{
  ErrFunc(isString(),"getString() invalid type(%d)",type);
  return sData;
}
JObj* JObj::getPVal(const int i){
  ErrFunc(i>=0 && i<len,"getPVal() index(%d) out of range [%d:%d].",i,0,len-1);
  return val[i];
}
JObj* JObj::getArrayPVal(int i){
  return getPVal(i);
}
char* JObj::getHashKey(const int i){
  ErrFunc(i>=0 && i<len,"getHashKey() index(%d) out of range [%d:%d].",i,0,len-1);
  return key[i];
}
JObj* JObj::getHashPVal(const char* k){
  for(int i=0;i<len;i++){
    if(STRCMP(key[i],k)==0){
      return val[i];
    }
  }
  return NULL;
}

void JObj::setBool  (const int   v){ type=JOBJ_BOOL;  iData=v; }
void JObj::setInt   (const int   v){ type=JOBJ_INT;   iData=v; }

void JObj::setIntArray( const int * v , int srclen )
{ 
  type = JOBJ_ARRAY;
  
  MEM_ALLOC( piData , int * , int , srclen>>2 );
  if( piData == NULL )return;
  MEMCPY( piData , v , srclen );
  len = srclen>>2;

}

void JObj::setShortArray( const unsigned short * v , int srclen )
{
  type = JOBJ_ARRAY;
  
  MEM_ALLOC( psData , unsigned short * , unsigned short , srclen>>1 );
  if( psData == NULL )return;
  MEMCPY( psData , v , srclen );
  len = srclen>>1;
}

void JObj::setUInt  (const unsigned int v){ type=JOBJ_UINT; uData=v; }
void JObj::setFloat (const float v){ type=JOBJ_FLOAT; fData=v; }
void JObj::setString(const char* v){
  type=JOBJ_STRING;
  SALLOC(sData,(int)STRLEN(v));
  STRCPY(sData,v);
}
void JObj::addHashPVal(const char* k,JObj*& pv){
  if(len>=len0){
    len0=MAX2(2*len0,1);
    MEM_REALLOC(key,char**,len0,key,len,char*);
    if(key == NULL){ ErrFunc(0,"JObj::addHashPVal() array memory not allocated"); return; };
    MEM_REALLOC(val,JObj**,len0,val,len,JObj*);
    if(val == NULL){ ErrFunc(0,"JObj::addHashPVal() array memory not allocated"); ARR_RELEASE(key); return; };
  }
  MEM_ALLOC(key[len],char*,char,STRLEN(k)+1);
  if(key[len] == NULL){ ErrFunc(0,"JObj::addHashPVal() string memory not allocated"); ARR_RELEASE(key); ARR_RELEASE(val); return; };

  STRCPY(key[len],k);
  val[len]=pv;
  len+=1;
}
void JObj::addArrayPVal(JObj*& pv){
  if(len>=len0){
    len0=MAX2(4*len0,1);

    MEM_REALLOC(val,JObj**,len0,val,len,JObj*);

    if(val == NULL){ ErrFunc(0,"JObj::addArrayPVal() array memory not allocated"); return; };
  }
  val[len]=pv;
  len+=1;
}

#define _COPY_INT_ARRAY_PROC                            \
  ErrFunc(isArray(),"JObj::copyIntArray() not array!"); \
  for(int i=0;i<len-offset;i++){                        \
  v[i]=val[i+offset]->getInt();                       \
  }                                                     \
  return len;

int JObj::copyIntArray(int*& v,int offset) const{
  _COPY_INT_ARRAY_PROC;
}
int JObj::copyIntArray(unsigned short*& v,int offset) const{
  _COPY_INT_ARRAY_PROC;
}

int JObj::copyFixedToFloatArray( float *&v, int offset ) const {
  
  ErrFunc( isArray(), "JObj::copyFixedToFloatArray() not array!") ;
  
  for( int i = 0; i < len - offset; i++ ) {
    
    v[ i ] = XTOF( val[ i + offset ]->getInt() );
  }
  
  return len;
}

int JObj::copyFixedToFloatArrayWithIdx( float *&v, int colnum, int idxcnt, int offset ) const {

  ErrFunc( isArray(), "JObj::copyFixedToFloatArrayVMax() not array!") ;
  int ctr = 0;

  for( int i = 0; i < len - offset; i++ ) {
    
    if( ctr < idxcnt ) {
      
      v[ i ] = (float)val[ i + offset ]->getInt();
    }
    else {
      
      v[ i ] = XTOF( val[ i + offset ]->getInt() );
    }
    
    ctr++;
    
    if( ctr >= colnum ) {
      
      ctr = 0;
    }
  }

  return len;
}

JObj* JObj::parse(const char* s,int& n,int len_s,bool is_utf8){
  if(!s){
    return NULL;
  }
  int i=n;
  
  //1. skip preceding spaces
  JObj::skipSpaces(i,s,len_s);

  if( (i == 0) && (s[ 0 ] == 0xEF) && (s[ 1 ] == 0xBB) && (s[ 2 ] == 0xBF) ) {

    is_utf8 = true;
    i += 3;
  }

  if(i>=len_s){
    n=len_s;
    return NULL;
  }
  
  int count,is_bslash,is_ok,i0;
  char c=s[i];
  
  //2. case A: number character found : int or float
  if(('0'<=c && c<='9') || c=='-'){
    char ss[12];
    count=0;
    ss[0]=c; count+=1; i+=1;
    int dot_count=0;
    int x_count=0;
    while(i<len_s){
      c=s[i];
      if('0'<=c && c<='9' || c=='.' || c=='x' || c=='X' ||
	 ('a'<=c && c<='f') || ('A' <=c && c<='F')){
	ss[count]=c; count+=1; i+=1;
	if(c=='.'){
	  dot_count+=1;
	}else if(c=='x' || c=='X'){
	  x_count+=1;
	}
	if(count>=12) break;
      }else{
	break;
      }
    }
    //check error 1 : int
    if(dot_count<=0){
      if(count>10+(ss[0]=='-' ? 1 : 0)){
	ErrFunc(0,"JObj::parse() int value overflow for \"%s\".",s+n);
	n=i;
	return NULL;
      }
    }
    //check error 2 : float
    if(dot_count>0){
      if(dot_count>=2 || count<=1 || x_count>=1){
	ErrFunc(0,"JObj::parse() invalid float for \"%s\".",s+n);
	n=i;
	return NULL;
      }
    }
    //check error 3 : hex int
    else if(x_count>0){
      if(x_count>=2 || count<=2 || ss[0]!='0' || (ss[1]!='x' && ss[1]!='X')){
	ErrFunc(0,"JObj::parse() hex value invalid for \"%s\".",s+n);
	n=i;
	return NULL;
      }
    }

    if(count>=12) count=11;

    ss[count]='\0'; count+=1;

    n=i;

    if(dot_count){
      float f =(float)atof( ss );
      return new JObj(f);
    }else if(x_count){
      unsigned int v=STRTOUL(ss,NULL,16);
      return new JObj(v);
    }else {
      int v=ATOI(ss);
      return new JObj(v);
    }
    
    //3. case B: string character found
  }else if(c=='"'){
    i+=1; count=0; is_bslash=0; is_ok=0; int sjis_found=0;
    i0=i;
    while(i<len_s){
      c=s[i];
      if(sjis_found){
	sjis_found=0;
      }
      else if( (is_utf8 == false) && ((0x81<=c && c<=0x9F) || (0xE0<=c && c<=0xEF)) ) {
	sjis_found=1;
	is_bslash=0;
      }else if(c=='"' && !is_bslash){
	is_ok=1;
	break;
      }else if(c=='\\'){
	is_bslash=(is_bslash ? 0 : 1);
      }else{
	is_bslash=0;
      }
      count+=1;
      i+=1;
    }
    ErrFunc(is_ok,
	    "JObj::parse() invalid string value for \"%s\".",s+n);
    ErrFunc(!sjis_found,
	    "JObj::parse() sjis character not closed for \"%s\".",s+n);
    char* ss;
    MEM_ALLOC(ss,char*,char,count+1);
    if(ss == NULL){ ErrFunc(0,"JObj::parse() string memory not allocated"); return NULL; };
    i=i0; is_bslash=0; count=0; sjis_found=0;
    while(i<len_s){
      c=s[i];
      if(sjis_found){
	sjis_found=0;
	ss[count++]=c;
      }
      else if( (is_utf8 == false) && ((0x81<=c && c<=0x9F) || (0xE0<=c && c<=0xEF)) ) {
	sjis_found=1;
	is_bslash=0;
	ss[count++]=c;
      }else if(c=='"' && !is_bslash){
	i+=1;
	break;
      }else if(c=='\\'){
	if(is_bslash){
	  ss[count++]='\\'; is_bslash=0;
	}else{
	  is_bslash=1;
	}
      }else{
	if(is_bslash){
	  switch(c){
	  case '"': ss[count++]='"';  break;
	  case 'n': ss[count++]='\n'; break;
	  case 't': ss[count++]='\t'; break;
	  case 'r': ss[count++]='\r'; break;
	  case 'v': ss[count++]='\v'; break;
	  case 'f': ss[count++]='\f'; break;
	  default:  ErrFunc(0,"JObj::parse(): invalid back slash for \"%s\".",s+n);
	  };
	  is_bslash=0;
	}else{
	  ss[count++]=c;
	}
      }
      i+=1;
    }
    ss[count]='\0';
    n=i;
    JObj* p=new JObj(ss);
    ARR_RELEASE(ss);
    return p;

    //4. case C: array character found
  }else if(c=='['){
    i+=1; is_ok=0;


    bool is_int = ((char)s[i] == 'i');
    bool is_fix = ((char)s[i] == 'x' );
    bool is_short = ((char)s[i] == 's');
    char binsize[10] = {0};
    int binstart = 0;
    int binlen = 0;
    JObj* retarrayval=NULL;

    if( i < len_s-4 && (is_int || is_fix || is_short) )
      {
	//サイズ検索
	i+=2;
	binstart = i;
	while( 1 )
	  {
	    if( !((char)s[i] >= '0' && (char)s[i] <= '9') )
	      {
		MEMCPY( binsize , &s[binstart] , binlen );
		break;
	      }

	    i++;
	    binlen++;
	  }

	//バイナリの読み込み前の準備
	binstart = 0;
	binlen = 0;

	binstart = i+1;

	binlen = ATOI( binsize );
	if( is_short )binlen = binlen << 1;
	else
	  if( is_int || is_fix )binlen = binlen << 2;

	n = binstart + binlen;

	//バイナリの読み込み
	if( binlen <= 0 )return NULL;

	if(is_int ||
	   is_fix )
	  {
	    int * retidat = NULL;
	    MEM_ALLOC( retidat , int * , int , binlen>>2 );

	    if( retidat == NULL )return NULL;

	    MEMSET( retidat , 0 , binlen );

	    n += 1;

	    retarrayval= new JObj( retidat , binlen );

	    ARR_RELEASE( retidat );

	    return retarrayval;
	  }
	else
	  if(is_short)
	    {
	      unsigned short * retsdat = NULL;

	      MEM_ALLOC( retsdat , unsigned short * , unsigned short , binlen>>1 );

	      if( retsdat == NULL )return NULL;

	      MEMSET( retsdat , 0 , binlen );

	      n += 1;

	      retarrayval= new JObj( retsdat , binlen );

	      ARR_RELEASE( retsdat );

	      return retarrayval;
	    }

      }


    JObj* retval=new JObj(); retval->setArray();
    while(i<len_s){
      JObj::skipSpaces(i,s,len_s);
      ErrFunc(i<len_s,
	      "JObj::parse(): invalid array string(1) in \"%s\".",s+n);
      c=s[i];
      if(c==']'){
	is_ok=1; i+=1; break;
      }

      JObj* child=JObj::parse( s, i, len_s, is_utf8 );

      if(child!=NULL) retval->addArrayPVal(child);
      JObj::skipSpaces(i,s,len_s);
      ErrFunc(i<len_s,
	      "JObj::parse(): invalid array string(2) in \"%s\".",s+n);
      if(s[i]==','){
	i+=1;
      }
    }
    ErrFunc(is_ok,"JObj::parse(): closing brace for array not found in \"%s\".",s+n);
    n=i;
    return retval;
    
    //5. case D: hash character found
  }else if(c=='{'){
    i+=1; is_ok=0;
    JObj* retval=new JObj(); retval->setHash();
    char* key; int keylen=0;
    
    while(i<len_s){
      JObj::skipSpaces(i,s,len_s);
      c=s[i];
      if(c=='}'){
	is_ok=1; i+=1; break;
      }
      keylen=0; i0=i;
      while(i<len_s){
	if(JObj::isAlnumChar(s[i])){
	  keylen+=1; i+=1; continue;
	}
	break;
      }
      if(keylen<=0){
	ErrFunc(0,"JObj::parse(): cannot get hash key for \"%s\".",s+n);
	break;
      }
      MEM_ALLOC(key,char*,char,keylen+1);
      if(key == NULL){ ErrFunc(0,"JObj::parse() string memory not allocated"); return NULL; };
      STRNCPY(key,s+i0,keylen);
      key[keylen]='\0';
      JObj::skipSpaces(i,s,len_s);
      ErrFunc(i<len_s && s[i]==':',
	      "JObj::parse(): cannot get hash colon(%d-%d, %c) for \"%s\".",i, len_s,s[i],s+n);
      i+=1;

      JObj* child=JObj::parse( s, i, len_s, is_utf8 );

      JObj::skipSpaces(i,s,len_s);
      ErrFunc((child!=NULL) && (i<len_s),
	      "JObj::parse(): cannot get hash value for \"%s\".",s+n);
      if(child!=NULL){
	retval->addHashPVal(key,child);
      }
      ARR_RELEASE(key);
      if(s[i]==','){
	i+=1;
      }
    }
    ErrFunc(is_ok,"JObj::parse(): closing brace for hash not found in \"%s\".",s+n);
    n=i;
    return retval;
    
    //6. case E: error (unexpected character)
  }else{
    ErrFunc(0,"JObj::parse(): cannot process input string \"%s\".",s+n);
    n=len_s;        //finish parsing input string once illegal char found ...
    return NULL;
  }
}

JObj* JObj::parseEx(const byte* s,int& n,int len_s,bool is_utf8){
  if(!s){
    return NULL;
  }
  int i=n;
  
  //1. skip preceding spaces
  JObj::skipSpaces(i,(char *)s,len_s);

  if( (i == 0) && (s[ 0 ] == 0xEF) && (s[ 1 ] == 0xBB) && (s[ 2 ] == 0xBF) ) {

    is_utf8 = true;
    i += 3;
  }
  if(i>=len_s){
    n=len_s;
    return NULL;
  }
  
  int count,is_bslash,is_ok,i0;
  char c=s[i];
  
  //2. case A: number character found : int or float
  if(('0'<=c && c<='9') || c=='-'){
    char ss[12];
    count=0;
    ss[0]=c; count+=1; i+=1;
    int dot_count=0;
    int x_count=0;
    while(i<len_s){
      c=s[i];
      if('0'<=c && c<='9' || c=='.' || c=='x' || c=='X' ||
	 ('a'<=c && c<='f') || ('A' <=c && c<='F')){
	ss[count]=c; count+=1; i+=1;
	if(c=='.'){
	  dot_count+=1;
	}else if(c=='x' || c=='X'){
	  x_count+=1;
	}
	if(count>=12) break;
      }else{
	break;
      }
    }
    //check error 1 : int
    if(dot_count<=0){
      if(count>10+(ss[0]=='-' ? 1 : 0)){
	ErrFunc(0,"JObj::parseEx() int value overflow for \"%s\".",s+n);
	n=i;
	return NULL;
      }
    }
    //check error 2 : float
    if(dot_count>0){
      if(dot_count>=2 || count<=1 || x_count>=1){
	ErrFunc(0,"JObj::parseEx() invalid float for \"%s\".",s+n);
	n=i;
	return NULL;
      }
    }
    //check error 3 : hex int
    else if(x_count>0){
      if(x_count>=2 || count<=2 || ss[0]!='0' || (ss[1]!='x' && ss[1]!='X')){
	ErrFunc(0,"JObj::parseEx() hex value invalid for \"%s\".",s+n);
	n=i;
	return NULL;
      }
    }

    if(count>=12) count=11;

    ss[count]='\0'; count+=1;

    n=i;

    if(dot_count){
      float f=strtof(ss,NULL);
      /*
	JObj* qf=new JObj(f);
	DBGPR("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<%s",ss);
	//以下Float値の文字列出力で文字化けする模様。
	//SPRINTF(retval,"%f",getFloat());でも同様
	DBGPR("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<%f",f);
	DBGPR("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<%f",qf->getFloat());
      */
      return new JObj(f);
    }else if(x_count){
      unsigned int v=STRTOUL(ss,NULL,16);
      return new JObj(v);
    }else {
      int v=ATOI(ss);
      return new JObj(v);
    }
    
    //3. case B: string character found
  }else if(c=='"'){
    i+=1; count=0; is_bslash=0; is_ok=0; int sjis_found=0;
    i0=i;
    while(i<len_s){
      c=s[i];
      if(sjis_found){
	sjis_found=0;
      }
      else if( (is_utf8 == false) && ((0x81<=c && c<=0x9F) || (0xE0<=c && c<=0xEF)) ) {
	sjis_found=1;
	is_bslash=0;
      }else if(c=='"' && !is_bslash){
	is_ok=1;
	break;
      }else if(c=='\\'){
	is_bslash=(is_bslash ? 0 : 1);
      }else{
	is_bslash=0;
      }
      count+=1;
      i+=1;
    }
    ErrFunc(is_ok,
	    "JObj::parseEx() invalid string value for \"%s\".",s+n);
    ErrFunc(!sjis_found,
	    "JObj::parseEx() sjis character not closed for \"%s\".",s+n);
    char* ss;
    MEM_ALLOC(ss,char*,char,count+1);
    if(ss == NULL){ ErrFunc(0,"JObj::parseEx() string memory not allocated"); return NULL; };
    i=i0; is_bslash=0; count=0; sjis_found=0;
    while(i<len_s){
      c=s[i];
      if(sjis_found){
	sjis_found=0;
	ss[count++]=c;
      }
      else if( (is_utf8 == false) && ((0x81<=c && c<=0x9F) || (0xE0<=c && c<=0xEF)) ) {
	sjis_found=1;
	is_bslash=0;
	ss[count++]=c;
      }else if(c=='"' && !is_bslash){
	i+=1;
	break;
      }else if(c=='\\'){
	if(is_bslash){
	  ss[count++]='\\'; is_bslash=0;
	}else{
	  is_bslash=1;
	}
      }else{
	if(is_bslash){
	  switch(c){
	  case '"': ss[count++]='"';  break;
	  case 'n': ss[count++]='\n'; break;
	  case 't': ss[count++]='\t'; break;
	  case 'r': ss[count++]='\r'; break;
	  case 'v': ss[count++]='\v'; break;
	  case 'f': ss[count++]='\f'; break;
	  default:  ErrFunc(0,"JObj::parseEx(): invalid back slash for \"%s\".",s+n);
	  };
	  is_bslash=0;
	}else{
	  ss[count++]=c;
	}
      }
      i+=1;
    }
    ss[count]='\0';
    n=i;
    JObj* p=new JObj(ss);
    ARR_RELEASE(ss);
    return p;

    //4. case C: array character found
  }else if(c=='['){
    i+=1; is_ok=0;

    bool is_int = ((char)s[i] == 'i');
    bool is_fix = ((char)s[i] == 'x' );
    bool is_short = ((char)s[i] == 's');
    char binsize[10] = {0};
    int binstart = 0;
    int binlen = 0;
    JObj* retarrayval=NULL;

    if( i < len_s-4 && (is_int || is_fix || is_short) )
      {
	//サイズ検索
	i+=2;
	binstart = i;
	while( 1 )
	  {
	    if( !((char)s[i] >= '0' && (char)s[i] <= '9') )
	      {
		MEMCPY( binsize , &s[binstart] , binlen );
		break;
	      }

	    i++;
	    binlen++;
	  }

	//バイナリの読み込み前の準備
	binstart = 0;
	binlen = 0;

	binstart = i+1;

	binlen = ATOI( binsize );
	if( is_short )binlen = binlen << 1;
	else
	  if( is_int || is_fix )binlen = binlen << 2;

	n = binstart + binlen;

	//バイナリの読み込み
	if( binlen < 0 )return NULL;


	if( binlen == 0 ) {

	  n += 1;
	  return NULL;
	}

	//原因２：文字コードに置き換わってしまっている
	if(is_int ||
	   is_fix )
	  {
	    int * retidat = NULL;
	    MEM_ALLOC( retidat , int * , int , binlen>>2 );

	    if( retidat == NULL )return NULL;

	    MEMSET( retidat , 0 , binlen );

	    for( int loopcnt = 0; loopcnt < binlen; loopcnt+=4 )
	      {
		retidat[loopcnt>>2] = (s[binstart + loopcnt + 3]<<24) | (s[binstart + loopcnt + 2]<<16) | (s[binstart + loopcnt + 1]<<8) | s[binstart + loopcnt];
	      }

	    n += 1;

	    retarrayval= new JObj( retidat , binlen );

	    ARR_RELEASE( retidat );

	    return retarrayval;
	  }
	else
	  if(is_short)
	    {
	      unsigned short * retsdat = NULL;

	      MEM_ALLOC( retsdat , unsigned short * , unsigned short , binlen>>1 );

	      if( retsdat == NULL )return NULL;

	      MEMSET( retsdat , 0 , binlen );

	      for( int loopcnt = 0; loopcnt < binlen; loopcnt+=2 )
		{
		  retsdat[loopcnt>>1] = (s[binstart + loopcnt + 1]<<8) | s[binstart + loopcnt];
		}

	      n += 1;

	      retarrayval= new JObj( retsdat , binlen );

	      ARR_RELEASE( retsdat );

	      return retarrayval;
	    }
      }

    JObj* retval=new JObj(); retval->setArray();
    while(i<len_s){
      JObj::skipSpaces(i,(char *)s,len_s);
      ErrFunc(i<len_s,
	      "JObj::parseEx(): invalid array string(1) in \"%s\".",s+n);
      c=s[i];
      if(c==']'){
	is_ok=1; i+=1; break;
      }

      JObj* child=JObj::parseEx( s, i, len_s, is_utf8 );

      if(child!=NULL) retval->addArrayPVal(child);
      JObj::skipSpaces(i,(char *)s,len_s);
      ErrFunc(i<len_s,
	      "JObj::parseEx(): invalid array string(2) in \"%s\".",s+n);
      if(s[i]==','){
	i+=1;
      }
    }
    ErrFunc(is_ok,"JObj::parseEx(): closing brace for array not found in \"%s\".",s+n);
    n=i;
    return retval;
    
    //5. case D: hash character found
  }else if(c=='{'){
    i+=1; is_ok=0;
    JObj* retval=new JObj(); retval->setHash();
    char* key; int keylen=0;
    
    while(i<len_s){
      JObj::skipSpaces(i,(char *)s,len_s);
      c=s[i];
      if(c=='}'){
	is_ok=1; i+=1; break;
      }
      keylen=0; i0=i;
      while(i<len_s){
	if(JObj::isAlnumChar(s[i])){
	  keylen+=1; i+=1; continue;
	}
	break;
      }

      if(keylen<=0){
	ErrFunc(0,"JObj::parseEx(): cannot get hash key for \"%s\".",s+n);
	break;
      }

      MEM_ALLOC(key,char*,char,keylen+1);
      if(key == NULL){ ErrFunc(0,"JObj::parseEx() string memory not allocated"); return NULL; };
      STRNCPY(key,((char *)s)+i0,keylen);
      key[keylen]='\0';
      JObj::skipSpaces(i,(char *)s,len_s);

      ErrFunc(i<len_s && s[i]==':',
	      "JObj::parseEx(): cannot get hash colon for \"%s\".",s+n);
      i+=1;


      int oldI = i;

      JObj* child=JObj::parseEx( s, i, len_s, is_utf8 );

      JObj::skipSpaces(i,(char *)s,len_s);

      ErrFunc(
	      (i < len_s) && (oldI != i) && ((i == oldI + 7) || (child != NULL)),
	      "JObj::parseEx(): cannot get hash value for \"%s\".",
	      s+n
	      );


      if(child!=NULL){
	retval->addHashPVal(key,child);
      }
      ARR_RELEASE(key);
      if(s[i]==','){
	i+=1;
      }
    }
    n=i;
    return retval;
    
    //6. case E: error (unexpected character)
  }else{
	
    ErrFunc(0,"JObj::parseEx(): cannot process input string \"%s\".",s+n);
    n=len_s;        //finish parsing input string once illegal char found ...
    return NULL;
  }
}

void JObj::skipSpaces(int&i,const char* s,int len_s){
  while(i<len_s){
    if(STRCHR(JOBJ_SPACES,s[i])!=NULL){
      i+=1; continue;
    }
    break;
  }
  return;
}

int JObj::isAlnumChar(char c){
  return (
	  c=='_' ||
	  ('0'<=c && c<='9') ||
	  ('a'<=c && c<='z') ||
	  ('A'<=c && c<='Z')
	  );
}
