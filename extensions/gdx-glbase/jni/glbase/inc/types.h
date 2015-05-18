/**
 * @file types.h
 * @brief Define standard types used in the glbase interface
 *
 **/
#pragma once

#include <stdarg.h>

// byte type
typedef unsigned char byte;
typedef unsigned short ushort;
typedef int (*LOGFUNC)(char const* fmt, va_list arglist);

#ifdef IOS
#define GL2_H <OpenGLES/ES2/gl.h>
#define GL2EXT_H <OpenGLES/ES2/glext.h>

#define GL_ATC_RGB_AMD 0x8C92
#define GL_ATC_RGBA_EXPLICIT_ALPHA_AMD 0x8C93
#define GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD 0x87EE
#define GL_ETC1_RGB8_OES 0x8D64

#elif defined(GLCORE20)
#define GL2_H "glCore20/gl_core_2_0.h"
#define GL2EXT_H "glCore20/gl_core_2_0.h"

#define GL_ATC_RGB_AMD 0x8C92
#define GL_ATC_RGBA_EXPLICIT_ALPHA_AMD 0x8C93
#define GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD 0x87EE
#define GL_ETC1_RGB8_OES 0x8D64
#define GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG 0x8C00
#define GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG 0x8C01
#define GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG 0x8C02
#define GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG 0x8C03

#else
#define GL2_H "GLES2/gl2.h"
#define GL2EXT_H "GLES2/gl2ext.h"
#endif


// Unit conversions
#define DEG2RADF( _d_ ) ((_d_) * M_PI / 180.0f)
#define RAD2DEGF( _d_ ) ((_d_) * 180.0f / M_PI)

/**
 * ƒeƒNƒXƒ`ƒƒ�î•ñ
 */
typedef struct tagTextureInfo {
  /** ‰¡•� */
  int width;
  /** �‚‚³ */
  int height;
  /** ƒtƒH�[ƒ}ƒbƒg(GL_RGBA“™) */
  int format;
  /** ƒsƒNƒZƒ‹ƒtƒH�[ƒ}ƒbƒg(GL_UNSIGNED_SHORT_4_4_4_4“™) */
  int pixelFormat;
  /** ‰æ‘œƒf�[ƒ^ */
  byte *imageData;
} TextureInfo;

/**
 * 1’¸“_‚Ì�î•ñ�iQuad’Ç‰ÁŽž“™‚ÉŽg—p�j
 */
typedef struct tagSimpleVertex {
	float x;
	float y;
	float u;
	float v;
} SimpleVertex;
