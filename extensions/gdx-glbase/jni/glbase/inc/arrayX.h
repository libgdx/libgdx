/**
 * @file arrayX.h
 * @brief Dynamic array utility
 **/
#include <stdio.h>


// Dynamic Names
#define TOOLS_NAME2_DUMMY(a,b) a ## b
#define name2(a,b) TOOLS_NAME2_DUMMY(a,b)

#undef Array
#define Array(suffix) name2(Array,suffix)

class Array(SUFFIX) {
  int is_no_delete;
 public:
  int len;
  TYPE* el;

  /* 1. constructors and destructor */
  Array(SUFFIX)();
  Array(SUFFIX)(const Array(SUFFIX)& v,int start=0,int length=-1);
  Array(SUFFIX)(const TYPE* p,int length);
  Array(SUFFIX)(int length,TYPE q=0);
  ~Array(SUFFIX)();
  void setup(){ is_no_delete = false; }

  /* 2. converter */
  operator TYPE* (){ return el; }
  operator TYPE* () const { return el; }

  /* 3. methods */
  int getLen() const{ return len; }
  const TYPE* getEl() const{ return el; }

  Array(SUFFIX)& replace(int start,int length,const Array(SUFFIX)& v);
  Array(SUFFIX)& append(const Array(SUFFIX)& v){ return replace(len,0,v); }
  Array(SUFFIX)& set(const Array(SUFFIX)& v){ return replace(0,len,v); }
  Array(SUFFIX)& erase(int pos,int n=1){ return replace(pos,n,Array(SUFFIX)()); }

  Array(SUFFIX)  subarray(int start,int length) const;
  Array(SUFFIX)& resize(int length,TYPE q=0);

  Array(SUFFIX)& set(const TYPE* p,int length); //copy
  Array(SUFFIX)& setEl(TYPE*& p,int length,int no_delete=true);

  int isEqual(const Array(SUFFIX)& v) const;

  int find(TYPE q,int start=0) const;  //return -1 if not exist;
  const char* str(char* buf, int bufsize) const;            //string representation of array;

  void print() const {
    char buf[1024];
    str(buf, 1024);
    puts(buf);
  }

  /* 4. operators */
  TYPE& operator [] (int n);
  TYPE  operator [] (int n) const;
  TYPE& operator () (int n);
  TYPE  operator () (int n) const;

  Array(SUFFIX)& operator =  (const Array(SUFFIX)& v);

  friend int operator != (const Array(SUFFIX)&,const Array(SUFFIX)&);
  friend int operator == (const Array(SUFFIX)&,const Array(SUFFIX)&);
};

#undef TOOLS_NAME2_DUMMY
#undef name2
