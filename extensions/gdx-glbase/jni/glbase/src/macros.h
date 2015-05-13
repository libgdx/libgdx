/**
 * @file macros.h
 * @brief Set of common usage macros and utilities
 *
 **/
#pragma once

#include <string>
#include <vector>

//BS_ERROR_LOGはビルド時にマクロを定義

#ifdef BS_ERROR_LOG

// error trace
#define trace(...) GLBase::get()->dotrace(__VA_ARGS__)
#define etrace(...) do{ GLBase::get()->doetrace("ERROR at %s(%d): ", __FILE__, __LINE__);GLBase::get()->doetrace(__VA_ARGS__); } while(false)
#define ErrFunc(isok, ...) {if( !(isok) ) etrace(__VA_ARGS__);}

// Standard procedure for GL operations
#define GLOP(x) \
  x; \
  { \
    GLenum glError = glGetError(); \
    if(glError != GL_NO_ERROR) { \
      etrace("glGetError() = %i (0x%.8x) at line %i\n", glError, glError, __LINE__); \
    } \
  }

#else //BS_ERROR_LOG

// error trace
#define trace(...) {}
#define etrace(...) {}
#define ErrFunc(isok, ...) {}

// Standard procedure for GL operations
#define GLOP(x) x

#endif //BS_ERROR_LOG

// Float to fixed precision decimals integer
#define FTOX(X)            ((int) ((X) * 65536))
#define XTOF( _x_ )(0.0000152587890625f * (_x_))

// Integer to fixed precision decimals integer
#define ITOX(i)((i) << 16)

// Max
#define MAX2(a, b) (a>b?a:b)

// Load buffer to VBO
#define LOAD_BUFFER_VBO( buffer, length, glid, type )		\
  if( length > 0 ){							\
    GLOP( glGenBuffers( 1, &glid ) );					\
    GLOP( glBindBuffer( type, glid ) );					\
    GLOP( glBufferData( type, length, buffer, GL_STATIC_DRAW ) ); \
    GLOP( glBindBuffer( type, 0 ) );					\
  }

#define UNLOAD_BUFFER_VBO( glid )		\
  {						\
    GLOP( glDeleteBuffers(1, &glid) );		\
    glid = -1;					\
  }


// メモリ
#define ARR_RELEASE(p) {delete[] p; p=NULL;}
#define SAFE_RELEASE(p) {delete p; p=NULL;}
#define SFREE0(p) {free(p); p=NULL;}
#define MEM_ALLOC(p,type_save,type,size) {p=(type_save)new type[size];}
#define SALLOC(p, len){ if(len>=0){ MEM_ALLOC(p,char*,char,len+1); p[len]='\0'; } else p=NULL; }
#define MEM_REALLOC(p1,type_save,size1,p2,size2,type){			\
    ErrFunc(size1>size2,"memreall(): size1(%d) must be larger than size2(%d)",size1,size2); \
    type_save _pt=(type_save)(new type[size1]);				\
    for(int i=0;i<size2;i++){						\
      ((type*)_pt)[i]=((type*)p2)[i];					\
      }									\
    ARR_RELEASE(p1);							\
    p1=_pt;								\
  }
#define VALLOC(p,len){ if(len>0){ MEM_ALLOC(p,TYPE*,TYPE,len); }else p=NULL; }
#define VFREE(p){ if(p!=NULL){ free(p); p=NULL; }}

// Max matrix palettes
#define MAX_MATRIX_PALETTES 128

// String
#define DBGPR trace
#define SNPRINTF snprintf
#define STRLEN strlen
#define STRCPY strcpy
#define STRNCPY strncpy
#define STRCMP strcmp
#define STRTOUL strtoul
#define MEMCPY memcpy
#define ATOI atoi
#define MEMSET memset
#define STRCHR strchr

// Dynamic Names
#define TOOLS_NAME2_DUMMY(a,b) a ## b
#define name2(a,b) TOOLS_NAME2_DUMMY(a,b)

/**
 * STL 文字列をスプリット
 **/
std::vector<std::string> split(const std::string &str, const std::string &delim);

/**
 * strdup with "new" operator
 **/
char* strdup2(const char* string);
