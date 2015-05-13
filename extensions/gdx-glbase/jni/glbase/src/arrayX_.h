/**
 * @file arrayX_.h
 * @brief Pseudo-template implementation of some array type
 *
 **/
#include "glbase.h"
#include "macros.h"
#include <stdlib.h>
#include <string.h>

#define VSETNODELETE(flag){ is_no_delete=flag; }


Array(SUFFIX)::Array(SUFFIX)(){
  setup(); len=0; el=NULL;
}

Array(SUFFIX)::Array(SUFFIX)(const Array(SUFFIX)& v,int start,int length){
  setup();
  ErrFunc(start>=0,"Array(SUFFIX)(): start(%d) must be >=0",start);
  ErrFunc(start+length<v.len,
	  "Array(SUFFIX)(): start(%d)+length(%d) must be < v.len(%d)",
	  start,length,v.len);
  len=(length <0 ? v.len-start : length);
  VALLOC(el,len);
  for(int i=0;i<len;i++){
    el[i]=v.el[i+start];
  }
}

Array(SUFFIX)::Array(SUFFIX)(const TYPE* p,int length){
  setup();
  ErrFunc(length>=0,"Array(SUFFIX)(): length(%d) must be >=0.",length);
  len=length;
  VALLOC(el,len);
  for(int i=0;i<len;i++) el[i]=p[i];
}
Array(SUFFIX)::Array(SUFFIX)(int length,TYPE q){
  setup();
  ErrFunc(length>=0,"Array(SUFFIX)(): length(%d) must be >=0.",length);
  len=length; VALLOC(el,len);
  for(int i=0;i<len;i++) el[i]=q;
}
Array(SUFFIX)::~Array(SUFFIX)(){
  len=0;
  VFREE(el);
}

Array(SUFFIX)& Array(SUFFIX)::replace(int start,int length,const Array(SUFFIX)& v){
  ErrFunc(start>=0 && start<=len,
	  "Array(SUFFIX)::replace() start(%d) out of range[0:%d]",start,len);
  ErrFunc(start+length>=0 && start+length<=len,
	  "Array(SUFFIX)::replace() start+length(%d) out of range[0:%d].",
	  start+length,len);
  int s1,s2;
  if(length>0){ s1=start; s2=start+length; } else{ s1=start+length; s2=start; }

  int new_len=len-(s2-s1)+v.len;

  TYPE* temp=NULL; VALLOC(temp,new_len);

  if(new_len>0 && temp==NULL){ ErrFunc(0,"Array(SUFFIX)::replace() array memory for %p not allocated",&el); return *this; };

  int i;
  for(i=0;i<s1;i++) temp[i]=el[i];
  for(i=s1;i<s1+v.len;i++) temp[i]=v.el[i-s1];
  for(i=s1+v.len;i<new_len;i++) temp[i]=el[i-s1-v.len+s2];

  VFREE(el);

  len=new_len;
  el=temp;
  VSETNODELETE(false);

  return *this;
}
Array(SUFFIX) Array(SUFFIX)::subarray(int start,int length) const {
  ErrFunc(length>=0,"Array(SUFFIX)::subarray() length(%d) must be >=0.",length);
  ErrFunc(start>=0 && start<len,
	  "Array(SUFFIX)::subarray() start(%d) out of range[0:%d]",start,len-1);
  ErrFunc(start+length>=0 && start+length<len,
	  "Array(SUFFIX)::subarray() start+length(%d) out of range[0:%d]",start+length,len-1);
  Array(SUFFIX) a(length);
  for(int i=0;i<length;i++) a.el[i]=el[start+i];
  return a;
}
Array(SUFFIX)& Array(SUFFIX)::resize(int length,TYPE q){
  if(length==len){
    return *this;
  }else if(length>len){
    Array(SUFFIX) a(length-len,q);
    this->replace(len,0,a);
  }else{
    this->replace(length,len-length,Array(SUFFIX)());
  }
  return *this;
}
Array(SUFFIX)& Array(SUFFIX)::set(const TYPE* p,int length){
  ErrFunc(length>=0,"Array(SUFFIX)::set() length(%d) must be >=0.",length);
  VFREE(el);
  len=length;
  VALLOC(el,len);
  for(int i=0;i<len;i++) el[i]=p[i];
  VSETNODELETE(false);

  return *this;
}
Array(SUFFIX)& Array(SUFFIX)::setEl(TYPE*& p,int length,int no_delete){
  ErrFunc(length>=0,"Array(SUFFIX)::setEl() length(%d) must be >=0.",length);
  VFREE(el);

  len=length;
  el=p;
  VSETNODELETE(no_delete);

  return *this;
}
int Array(SUFFIX)::isEqual(const Array(SUFFIX)& v) const{
  if(len!=v.len) return 0;
  for(int i=0;i<len;i++){
    if(el[i]!=v.el[i]) return 0;
  }
  return 1;
}
int Array(SUFFIX)::find(TYPE q,int start) const{
  ErrFunc(start>=0,"Array(SUFFIX)::find() start(%d) must be >=0.",start);
  for(int i=start;i<len;i++){
    if(el[i]==q) return i;
  }
  return -1;
}
const char* Array(SUFFIX)::str(char* s, int bufsize) const {

#ifdef IS_FLOATING_POINT
  char t[64];
  s[0]='['; int count=1;
  for(int i=0;i<len;i++){
    SNPRINTF(t,63,"%f",el[i]);

    // bound check 
    if( count + strlen(t) >= bufsize-1 ) {
      s[bufsize-1] = '\0';
      return s;
    }

    STRCPY(s+count,t);
    count+=STRLEN(t);
    if(i<len-1){
      STRCPY(s+count,", ");
      count+=2;
    }
  }
  
  // bound check 
  if( count + 2 >= bufsize ) {
    s[bufsize-1] = '\0';
    return s;
  }
#else
  char t[12];
  s[0]='['; int count=1;
  for(int i=0;i<len;i++){
    SNPRINTF(t,11,"%d",el[i]);
    
    // bound check 
    if( count + strlen(t) >= bufsize-1 ) {
      s[bufsize-1] = '\0';
      return s;
    }

    STRCPY(s+count,t);
    count+=STRLEN(t);
    if(i<len-1){
      STRCPY(s+count,", ");
      count+=2;
    }
  }
#endif
  
  s[count]=']'; count+=1;
  s[count]='\0';

  return s;
}


TYPE& Array(SUFFIX)::operator [] (int n){
  ErrFunc(n>=0 && n<len,
	  "Array(SUFFIX)::operator [](%d) out of range [0:%d]",n,len-1);
  return el[n];
}
TYPE Array(SUFFIX)::operator [] (int n) const{
  ErrFunc(n>=0 && n<len,
	  "Array(SUFFIX)::operator [](%d) out of range [0:%d]",n,len-1);
  return el[n];
}
TYPE& Array(SUFFIX)::operator () (int n){
  ErrFunc(n>=1 && n<=len,
	  "Array(SUFFIX)::operator ()(%d) out of range [1:%d]",n,len);
  return el[n-1];
}
TYPE Array(SUFFIX)::operator () (int n) const{
  ErrFunc(n>=1 && n<=len,
	  "Array(SUFFIX)::operator ()(%d) out of range [1:%d]",n,len);
  return el[n-1];
}
Array(SUFFIX)& Array(SUFFIX)::operator = (const Array(SUFFIX)& v){
  return (this->replace(0,len,v));
}
  
  int operator != (const Array(SUFFIX)& a,const Array(SUFFIX)& b){
    return (!a.isEqual(b));
  }
int operator == (const Array(SUFFIX)& a,const Array(SUFFIX)& b){
  return (a.isEqual(b));
}
