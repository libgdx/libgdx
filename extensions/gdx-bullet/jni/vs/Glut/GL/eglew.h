/*
** The OpenGL Extension Wrangler Library
** Copyright (C) 2008-2015, Nigel Stewart <nigels[]users sourceforge net>
** Copyright (C) 2002-2008, Milan Ikits <milan ikits[]ieee org>
** Copyright (C) 2002-2008, Marcelo E. Magallon <mmagallo[]debian org>
** Copyright (C) 2002, Lev Povalahev
** All rights reserved.
** 
** Redistribution and use in source and binary forms, with or without 
** modification, are permitted provided that the following conditions are met:
** 
** * Redistributions of source code must retain the above copyright notice, 
**   this list of conditions and the following disclaimer.
** * Redistributions in binary form must reproduce the above copyright notice, 
**   this list of conditions and the following disclaimer in the documentation 
**   and/or other materials provided with the distribution.
** * The name of the author may be used to endorse or promote products 
**   derived from this software without specific prior written permission.
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
** AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
** IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
** ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
** LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
** CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
** SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
** INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
** CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
** ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
** THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
 * Mesa 3-D graphics library
 * Version:  7.0
 *
 * Copyright (C) 1999-2007  Brian Paul   All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * BRIAN PAUL BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
** Copyright (c) 2007 The Khronos Group Inc.
** 
** Permission is hereby granted, free of charge, to any person obtaining a
** copy of this software and/or associated documentation files (the
** "Materials"), to deal in the Materials without restriction, including
** without limitation the rights to use, copy, modify, merge, publish,
** distribute, sublicense, and/or sell copies of the Materials, and to
** permit persons to whom the Materials are furnished to do so, subject to
** the following conditions:
** 
** The above copyright notice and this permission notice shall be included
** in all copies or substantial portions of the Materials.
** 
** THE MATERIALS ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
** EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
** MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
** IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
** CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
** TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
** MATERIALS OR THE USE OR OTHER DEALINGS IN THE MATERIALS.
*/

#ifndef __eglew_h__
#define __eglew_h__
#define __EGLEW_H__

#ifdef __eglext_h_
#error eglext.h included before eglew.h
#endif

#if defined(__egl_h_)
#error egl.h included before eglew.h
#endif

#define __eglext_h_

#define __egl_h_

#ifndef EGLAPIENTRY
#define EGLAPIENTRY
#endif
#ifndef EGLAPI
#define EGLAPI extern
#endif

/* EGL Types */
#include <sys/types.h>

#include <KHR/khrplatform.h>
#include <EGL/eglplatform.h>

#include <GL/glew.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef int32_t EGLint;

typedef unsigned int EGLBoolean;
typedef void *EGLDisplay;
typedef void *EGLConfig;
typedef void *EGLSurface;
typedef void *EGLContext;
typedef void (*__eglMustCastToProperFunctionPointerType)(void);

typedef unsigned int EGLenum;
typedef void *EGLClientBuffer;

typedef void *EGLSync;
typedef intptr_t EGLAttrib;
typedef khronos_utime_nanoseconds_t EGLTime;
typedef void *EGLImage;

typedef void *EGLSyncKHR;
typedef intptr_t EGLAttribKHR;
typedef void *EGLLabelKHR;
typedef void *EGLObjectKHR;
typedef void (EGLAPIENTRY  *EGLDEBUGPROCKHR)(EGLenum error,const char *command,EGLint messageType,EGLLabelKHR threadLabel,EGLLabelKHR objectLabel,const char* message);
typedef khronos_utime_nanoseconds_t EGLTimeKHR;
typedef void *EGLImageKHR;
typedef void *EGLStreamKHR;
typedef khronos_uint64_t EGLuint64KHR;
typedef int EGLNativeFileDescriptorKHR;
typedef khronos_ssize_t EGLsizeiANDROID;
typedef void (*EGLSetBlobFuncANDROID) (const void *key, EGLsizeiANDROID keySize, const void *value, EGLsizeiANDROID valueSize);
typedef EGLsizeiANDROID (*EGLGetBlobFuncANDROID) (const void *key, EGLsizeiANDROID keySize, void *value, EGLsizeiANDROID valueSize);
typedef void *EGLDeviceEXT;
typedef void *EGLOutputLayerEXT;
typedef void *EGLOutputPortEXT;
typedef void *EGLSyncNV;
typedef khronos_utime_nanoseconds_t EGLTimeNV;
typedef khronos_utime_nanoseconds_t EGLuint64NV;
typedef khronos_stime_nanoseconds_t EGLnsecsANDROID;

struct EGLClientPixmapHI;

#define EGL_DONT_CARE                     ((EGLint)-1)

#define EGL_NO_CONTEXT                    ((EGLContext)0)
#define EGL_NO_DISPLAY                    ((EGLDisplay)0)
#define EGL_NO_IMAGE                      ((EGLImage)0)
#define EGL_NO_SURFACE                    ((EGLSurface)0)
#define EGL_NO_SYNC                       ((EGLSync)0)

#define EGL_UNKNOWN                       ((EGLint)-1)

#define EGL_DEFAULT_DISPLAY               ((EGLNativeDisplayType)0)

EGLAPI __eglMustCastToProperFunctionPointerType EGLAPIENTRY eglGetProcAddress (const char *procname);
/* ---------------------------- EGL_VERSION_1_0 ---------------------------- */

#ifndef EGL_VERSION_1_0
#define EGL_VERSION_1_0 1

#define EGL_FALSE 0
#define EGL_PBUFFER_BIT 0x0001
#define EGL_TRUE 1
#define EGL_PIXMAP_BIT 0x0002
#define EGL_WINDOW_BIT 0x0004
#define EGL_SUCCESS 0x3000
#define EGL_NOT_INITIALIZED 0x3001
#define EGL_BAD_ACCESS 0x3002
#define EGL_BAD_ALLOC 0x3003
#define EGL_BAD_ATTRIBUTE 0x3004
#define EGL_BAD_CONFIG 0x3005
#define EGL_BAD_CONTEXT 0x3006
#define EGL_BAD_CURRENT_SURFACE 0x3007
#define EGL_BAD_DISPLAY 0x3008
#define EGL_BAD_MATCH 0x3009
#define EGL_BAD_NATIVE_PIXMAP 0x300A
#define EGL_BAD_NATIVE_WINDOW 0x300B
#define EGL_BAD_PARAMETER 0x300C
#define EGL_BAD_SURFACE 0x300D
#define EGL_BUFFER_SIZE 0x3020
#define EGL_ALPHA_SIZE 0x3021
#define EGL_BLUE_SIZE 0x3022
#define EGL_GREEN_SIZE 0x3023
#define EGL_RED_SIZE 0x3024
#define EGL_DEPTH_SIZE 0x3025
#define EGL_STENCIL_SIZE 0x3026
#define EGL_CONFIG_CAVEAT 0x3027
#define EGL_CONFIG_ID 0x3028
#define EGL_LEVEL 0x3029
#define EGL_MAX_PBUFFER_HEIGHT 0x302A
#define EGL_MAX_PBUFFER_PIXELS 0x302B
#define EGL_MAX_PBUFFER_WIDTH 0x302C
#define EGL_NATIVE_RENDERABLE 0x302D
#define EGL_NATIVE_VISUAL_ID 0x302E
#define EGL_NATIVE_VISUAL_TYPE 0x302F
#define EGL_SAMPLES 0x3031
#define EGL_SAMPLE_BUFFERS 0x3032
#define EGL_SURFACE_TYPE 0x3033
#define EGL_TRANSPARENT_TYPE 0x3034
#define EGL_TRANSPARENT_BLUE_VALUE 0x3035
#define EGL_TRANSPARENT_GREEN_VALUE 0x3036
#define EGL_TRANSPARENT_RED_VALUE 0x3037
#define EGL_NONE 0x3038
#define EGL_SLOW_CONFIG 0x3050
#define EGL_NON_CONFORMANT_CONFIG 0x3051
#define EGL_TRANSPARENT_RGB 0x3052
#define EGL_VENDOR 0x3053
#define EGL_VERSION 0x3054
#define EGL_EXTENSIONS 0x3055
#define EGL_HEIGHT 0x3056
#define EGL_WIDTH 0x3057
#define EGL_LARGEST_PBUFFER 0x3058
#define EGL_DRAW 0x3059
#define EGL_READ 0x305A
#define EGL_CORE_NATIVE_ENGINE 0x305B

typedef EGLBoolean  ( * PFNEGLCHOOSECONFIGPROC) (EGLDisplay  dpy, const EGLint * attrib_list, EGLConfig * configs, EGLint  config_size, EGLint * num_config);
typedef EGLBoolean  ( * PFNEGLCOPYBUFFERSPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLNativePixmapType  target);
typedef EGLContext  ( * PFNEGLCREATECONTEXTPROC) (EGLDisplay  dpy, EGLConfig  config, EGLContext  share_context, const EGLint * attrib_list);
typedef EGLSurface  ( * PFNEGLCREATEPBUFFERSURFACEPROC) (EGLDisplay  dpy, EGLConfig  config, const EGLint * attrib_list);
typedef EGLSurface  ( * PFNEGLCREATEPIXMAPSURFACEPROC) (EGLDisplay  dpy, EGLConfig  config, EGLNativePixmapType  pixmap, const EGLint * attrib_list);
typedef EGLSurface  ( * PFNEGLCREATEWINDOWSURFACEPROC) (EGLDisplay  dpy, EGLConfig  config, EGLNativeWindowType  win, const EGLint * attrib_list);
typedef EGLBoolean  ( * PFNEGLDESTROYCONTEXTPROC) (EGLDisplay  dpy, EGLContext  ctx);
typedef EGLBoolean  ( * PFNEGLDESTROYSURFACEPROC) (EGLDisplay  dpy, EGLSurface  surface);
typedef EGLBoolean  ( * PFNEGLGETCONFIGATTRIBPROC) (EGLDisplay  dpy, EGLConfig  config, EGLint  attribute, EGLint * value);
typedef EGLBoolean  ( * PFNEGLGETCONFIGSPROC) (EGLDisplay  dpy, EGLConfig * configs, EGLint  config_size, EGLint * num_config);
typedef EGLDisplay  ( * PFNEGLGETCURRENTDISPLAYPROC) ( void );
typedef EGLSurface  ( * PFNEGLGETCURRENTSURFACEPROC) (EGLint  readdraw);
typedef EGLDisplay  ( * PFNEGLGETDISPLAYPROC) (EGLNativeDisplayType  display_id);
typedef EGLint  ( * PFNEGLGETERRORPROC) ( void );
typedef EGLBoolean  ( * PFNEGLINITIALIZEPROC) (EGLDisplay  dpy, EGLint * major, EGLint * minor);
typedef EGLBoolean  ( * PFNEGLMAKECURRENTPROC) (EGLDisplay  dpy, EGLSurface  draw, EGLSurface  read, EGLContext  ctx);
typedef EGLBoolean  ( * PFNEGLQUERYCONTEXTPROC) (EGLDisplay  dpy, EGLContext  ctx, EGLint  attribute, EGLint * value);
typedef const char * ( * PFNEGLQUERYSTRINGPROC) (EGLDisplay  dpy, EGLint  name);
typedef EGLBoolean  ( * PFNEGLQUERYSURFACEPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  attribute, EGLint * value);
typedef EGLBoolean  ( * PFNEGLSWAPBUFFERSPROC) (EGLDisplay  dpy, EGLSurface  surface);
typedef EGLBoolean  ( * PFNEGLTERMINATEPROC) (EGLDisplay  dpy);
typedef EGLBoolean  ( * PFNEGLWAITGLPROC) ( void );
typedef EGLBoolean  ( * PFNEGLWAITNATIVEPROC) (EGLint  engine);

#define eglChooseConfig EGLEW_GET_FUN(__eglewChooseConfig)
#define eglCopyBuffers EGLEW_GET_FUN(__eglewCopyBuffers)
#define eglCreateContext EGLEW_GET_FUN(__eglewCreateContext)
#define eglCreatePbufferSurface EGLEW_GET_FUN(__eglewCreatePbufferSurface)
#define eglCreatePixmapSurface EGLEW_GET_FUN(__eglewCreatePixmapSurface)
#define eglCreateWindowSurface EGLEW_GET_FUN(__eglewCreateWindowSurface)
#define eglDestroyContext EGLEW_GET_FUN(__eglewDestroyContext)
#define eglDestroySurface EGLEW_GET_FUN(__eglewDestroySurface)
#define eglGetConfigAttrib EGLEW_GET_FUN(__eglewGetConfigAttrib)
#define eglGetConfigs EGLEW_GET_FUN(__eglewGetConfigs)
#define eglGetCurrentDisplay EGLEW_GET_FUN(__eglewGetCurrentDisplay)
#define eglGetCurrentSurface EGLEW_GET_FUN(__eglewGetCurrentSurface)
#define eglGetDisplay EGLEW_GET_FUN(__eglewGetDisplay)
#define eglGetError EGLEW_GET_FUN(__eglewGetError)
#define eglInitialize EGLEW_GET_FUN(__eglewInitialize)
#define eglMakeCurrent EGLEW_GET_FUN(__eglewMakeCurrent)
#define eglQueryContext EGLEW_GET_FUN(__eglewQueryContext)
#define eglQueryString EGLEW_GET_FUN(__eglewQueryString)
#define eglQuerySurface EGLEW_GET_FUN(__eglewQuerySurface)
#define eglSwapBuffers EGLEW_GET_FUN(__eglewSwapBuffers)
#define eglTerminate EGLEW_GET_FUN(__eglewTerminate)
#define eglWaitGL EGLEW_GET_FUN(__eglewWaitGL)
#define eglWaitNative EGLEW_GET_FUN(__eglewWaitNative)

#define EGLEW_VERSION_1_0 EGLEW_GET_VAR(__EGLEW_VERSION_1_0)

#endif /* EGL_VERSION_1_0 */

/* ---------------------------- EGL_VERSION_1_1 ---------------------------- */

#ifndef EGL_VERSION_1_1
#define EGL_VERSION_1_1 1

#define EGL_CONTEXT_LOST 0x300E
#define EGL_BIND_TO_TEXTURE_RGB 0x3039
#define EGL_BIND_TO_TEXTURE_RGBA 0x303A
#define EGL_MIN_SWAP_INTERVAL 0x303B
#define EGL_MAX_SWAP_INTERVAL 0x303C
#define EGL_NO_TEXTURE 0x305C
#define EGL_TEXTURE_RGB 0x305D
#define EGL_TEXTURE_RGBA 0x305E
#define EGL_TEXTURE_2D 0x305F
#define EGL_TEXTURE_FORMAT 0x3080
#define EGL_TEXTURE_TARGET 0x3081
#define EGL_MIPMAP_TEXTURE 0x3082
#define EGL_MIPMAP_LEVEL 0x3083
#define EGL_BACK_BUFFER 0x3084

typedef EGLBoolean  ( * PFNEGLBINDTEXIMAGEPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  buffer);
typedef EGLBoolean  ( * PFNEGLRELEASETEXIMAGEPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  buffer);
typedef EGLBoolean  ( * PFNEGLSURFACEATTRIBPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  attribute, EGLint  value);
typedef EGLBoolean  ( * PFNEGLSWAPINTERVALPROC) (EGLDisplay  dpy, EGLint  interval);

#define eglBindTexImage EGLEW_GET_FUN(__eglewBindTexImage)
#define eglReleaseTexImage EGLEW_GET_FUN(__eglewReleaseTexImage)
#define eglSurfaceAttrib EGLEW_GET_FUN(__eglewSurfaceAttrib)
#define eglSwapInterval EGLEW_GET_FUN(__eglewSwapInterval)

#define EGLEW_VERSION_1_1 EGLEW_GET_VAR(__EGLEW_VERSION_1_1)

#endif /* EGL_VERSION_1_1 */

/* ---------------------------- EGL_VERSION_1_2 ---------------------------- */

#ifndef EGL_VERSION_1_2
#define EGL_VERSION_1_2 1

#define EGL_OPENGL_ES_BIT 0x0001
#define EGL_OPENVG_BIT 0x0002
#define EGL_LUMINANCE_SIZE 0x303D
#define EGL_ALPHA_MASK_SIZE 0x303E
#define EGL_COLOR_BUFFER_TYPE 0x303F
#define EGL_RENDERABLE_TYPE 0x3040
#define EGL_SINGLE_BUFFER 0x3085
#define EGL_RENDER_BUFFER 0x3086
#define EGL_COLORSPACE 0x3087
#define EGL_ALPHA_FORMAT 0x3088
#define EGL_COLORSPACE_LINEAR 0x308A
#define EGL_ALPHA_FORMAT_NONPRE 0x308B
#define EGL_ALPHA_FORMAT_PRE 0x308C
#define EGL_CLIENT_APIS 0x308D
#define EGL_RGB_BUFFER 0x308E
#define EGL_LUMINANCE_BUFFER 0x308F
#define EGL_HORIZONTAL_RESOLUTION 0x3090
#define EGL_VERTICAL_RESOLUTION 0x3091
#define EGL_PIXEL_ASPECT_RATIO 0x3092
#define EGL_SWAP_BEHAVIOR 0x3093
#define EGL_BUFFER_PRESERVED 0x3094
#define EGL_BUFFER_DESTROYED 0x3095
#define EGL_OPENVG_IMAGE 0x3096
#define EGL_CONTEXT_CLIENT_TYPE 0x3097
#define EGL_OPENGL_ES_API 0x30A0
#define EGL_OPENVG_API 0x30A1
#define EGL_DISPLAY_SCALING 10000

typedef EGLBoolean  ( * PFNEGLBINDAPIPROC) (EGLenum  api);
typedef EGLSurface  ( * PFNEGLCREATEPBUFFERFROMCLIENTBUFFERPROC) (EGLDisplay  dpy, EGLenum  buftype, EGLClientBuffer  buffer, EGLConfig  config, const EGLint * attrib_list);
typedef EGLenum  ( * PFNEGLQUERYAPIPROC) ( void );
typedef EGLBoolean  ( * PFNEGLRELEASETHREADPROC) ( void );
typedef EGLBoolean  ( * PFNEGLWAITCLIENTPROC) ( void );

#define eglBindAPI EGLEW_GET_FUN(__eglewBindAPI)
#define eglCreatePbufferFromClientBuffer EGLEW_GET_FUN(__eglewCreatePbufferFromClientBuffer)
#define eglQueryAPI EGLEW_GET_FUN(__eglewQueryAPI)
#define eglReleaseThread EGLEW_GET_FUN(__eglewReleaseThread)
#define eglWaitClient EGLEW_GET_FUN(__eglewWaitClient)

#define EGLEW_VERSION_1_2 EGLEW_GET_VAR(__EGLEW_VERSION_1_2)

#endif /* EGL_VERSION_1_2 */

/* ---------------------------- EGL_VERSION_1_3 ---------------------------- */

#ifndef EGL_VERSION_1_3
#define EGL_VERSION_1_3 1

#define EGL_OPENGL_ES2_BIT 0x0004
#define EGL_VG_COLORSPACE_LINEAR_BIT 0x0020
#define EGL_VG_ALPHA_FORMAT_PRE_BIT 0x0040
#define EGL_MATCH_NATIVE_PIXMAP 0x3041
#define EGL_CONFORMANT 0x3042
#define EGL_VG_COLORSPACE 0x3087
#define EGL_VG_ALPHA_FORMAT 0x3088
#define EGL_VG_COLORSPACE_LINEAR 0x308A
#define EGL_VG_ALPHA_FORMAT_NONPRE 0x308B
#define EGL_VG_ALPHA_FORMAT_PRE 0x308C
#define EGL_CONTEXT_CLIENT_VERSION 0x3098

#define EGLEW_VERSION_1_3 EGLEW_GET_VAR(__EGLEW_VERSION_1_3)

#endif /* EGL_VERSION_1_3 */

/* ---------------------------- EGL_VERSION_1_4 ---------------------------- */

#ifndef EGL_VERSION_1_4
#define EGL_VERSION_1_4 1

#define EGL_OPENGL_BIT 0x0008
#define EGL_MULTISAMPLE_RESOLVE_BOX_BIT 0x0200
#define EGL_SWAP_BEHAVIOR_PRESERVED_BIT 0x0400
#define EGL_MULTISAMPLE_RESOLVE 0x3099
#define EGL_MULTISAMPLE_RESOLVE_DEFAULT 0x309A
#define EGL_MULTISAMPLE_RESOLVE_BOX 0x309B
#define EGL_OPENGL_API 0x30A2

typedef EGLContext  ( * PFNEGLGETCURRENTCONTEXTPROC) ( void );

#define eglGetCurrentContext EGLEW_GET_FUN(__eglewGetCurrentContext)

#define EGLEW_VERSION_1_4 EGLEW_GET_VAR(__EGLEW_VERSION_1_4)

#endif /* EGL_VERSION_1_4 */

/* ---------------------------- EGL_VERSION_1_5 ---------------------------- */

#ifndef EGL_VERSION_1_5
#define EGL_VERSION_1_5 1

#define EGL_CONTEXT_OPENGL_CORE_PROFILE_BIT 0x00000001
#define EGL_SYNC_FLUSH_COMMANDS_BIT 0x0001
#define EGL_CONTEXT_OPENGL_COMPATIBILITY_PROFILE_BIT 0x00000002
#define EGL_OPENGL_ES3_BIT 0x00000040
#define EGL_GL_COLORSPACE_SRGB 0x3089
#define EGL_GL_COLORSPACE_LINEAR 0x308A
#define EGL_CONTEXT_MAJOR_VERSION 0x3098
#define EGL_CL_EVENT_HANDLE 0x309C
#define EGL_GL_COLORSPACE 0x309D
#define EGL_GL_TEXTURE_2D 0x30B1
#define EGL_GL_TEXTURE_3D 0x30B2
#define EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_X 0x30B3
#define EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_X 0x30B4
#define EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Y 0x30B5
#define EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Y 0x30B6
#define EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Z 0x30B7
#define EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Z 0x30B8
#define EGL_GL_RENDERBUFFER 0x30B9
#define EGL_GL_TEXTURE_LEVEL 0x30BC
#define EGL_GL_TEXTURE_ZOFFSET 0x30BD
#define EGL_IMAGE_PRESERVED 0x30D2
#define EGL_SYNC_PRIOR_COMMANDS_COMPLETE 0x30F0
#define EGL_SYNC_STATUS 0x30F1
#define EGL_SIGNALED 0x30F2
#define EGL_UNSIGNALED 0x30F3
#define EGL_TIMEOUT_EXPIRED 0x30F5
#define EGL_CONDITION_SATISFIED 0x30F6
#define EGL_SYNC_TYPE 0x30F7
#define EGL_SYNC_CONDITION 0x30F8
#define EGL_SYNC_FENCE 0x30F9
#define EGL_CONTEXT_MINOR_VERSION 0x30FB
#define EGL_CONTEXT_OPENGL_PROFILE_MASK 0x30FD
#define EGL_SYNC_CL_EVENT 0x30FE
#define EGL_SYNC_CL_EVENT_COMPLETE 0x30FF
#define EGL_CONTEXT_OPENGL_DEBUG 0x31B0
#define EGL_CONTEXT_OPENGL_FORWARD_COMPATIBLE 0x31B1
#define EGL_CONTEXT_OPENGL_ROBUST_ACCESS 0x31B2
#define EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY 0x31BD
#define EGL_NO_RESET_NOTIFICATION 0x31BE
#define EGL_LOSE_CONTEXT_ON_RESET 0x31BF
#define EGL_FOREVER 0xFFFFFFFFFFFFFFFF

typedef EGLint  ( * PFNEGLCLIENTWAITSYNCPROC) (EGLDisplay  dpy, EGLSync  sync, EGLint  flags, EGLTime  timeout);
typedef EGLImage  ( * PFNEGLCREATEIMAGEPROC) (EGLDisplay  dpy, EGLContext  ctx, EGLenum  target, EGLClientBuffer  buffer, const EGLAttrib * attrib_list);
typedef EGLSurface  ( * PFNEGLCREATEPLATFORMPIXMAPSURFACEPROC) (EGLDisplay  dpy, EGLConfig  config, void * native_pixmap, const EGLAttrib * attrib_list);
typedef EGLSurface  ( * PFNEGLCREATEPLATFORMWINDOWSURFACEPROC) (EGLDisplay  dpy, EGLConfig  config, void * native_window, const EGLAttrib * attrib_list);
typedef EGLSync  ( * PFNEGLCREATESYNCPROC) (EGLDisplay  dpy, EGLenum  type, const EGLAttrib * attrib_list);
typedef EGLBoolean  ( * PFNEGLDESTROYIMAGEPROC) (EGLDisplay  dpy, EGLImage  image);
typedef EGLBoolean  ( * PFNEGLDESTROYSYNCPROC) (EGLDisplay  dpy, EGLSync  sync);
typedef EGLDisplay  ( * PFNEGLGETPLATFORMDISPLAYPROC) (EGLenum  platform, void * native_display, const EGLAttrib * attrib_list);
typedef EGLBoolean  ( * PFNEGLGETSYNCATTRIBPROC) (EGLDisplay  dpy, EGLSync  sync, EGLint  attribute, EGLAttrib * value);
typedef EGLBoolean  ( * PFNEGLWAITSYNCPROC) (EGLDisplay  dpy, EGLSync  sync, EGLint  flags);

#define eglClientWaitSync EGLEW_GET_FUN(__eglewClientWaitSync)
#define eglCreateImage EGLEW_GET_FUN(__eglewCreateImage)
#define eglCreatePlatformPixmapSurface EGLEW_GET_FUN(__eglewCreatePlatformPixmapSurface)
#define eglCreatePlatformWindowSurface EGLEW_GET_FUN(__eglewCreatePlatformWindowSurface)
#define eglCreateSync EGLEW_GET_FUN(__eglewCreateSync)
#define eglDestroyImage EGLEW_GET_FUN(__eglewDestroyImage)
#define eglDestroySync EGLEW_GET_FUN(__eglewDestroySync)
#define eglGetPlatformDisplay EGLEW_GET_FUN(__eglewGetPlatformDisplay)
#define eglGetSyncAttrib EGLEW_GET_FUN(__eglewGetSyncAttrib)
#define eglWaitSync EGLEW_GET_FUN(__eglewWaitSync)

#define EGLEW_VERSION_1_5 EGLEW_GET_VAR(__EGLEW_VERSION_1_5)

#endif /* EGL_VERSION_1_5 */

/* ------------------------- EGL_ANDROID_blob_cache ------------------------ */

#ifndef EGL_ANDROID_blob_cache
#define EGL_ANDROID_blob_cache 1

typedef void  ( * PFNEGLSETBLOBCACHEFUNCSANDROIDPROC) (EGLDisplay  dpy, EGLSetBlobFuncANDROID  set, EGLGetBlobFuncANDROID  get);

#define eglSetBlobCacheFuncsANDROID EGLEW_GET_FUN(__eglewSetBlobCacheFuncsANDROID)

#define EGLEW_ANDROID_blob_cache EGLEW_GET_VAR(__EGLEW_ANDROID_blob_cache)

#endif /* EGL_ANDROID_blob_cache */

/* ---------------- EGL_ANDROID_create_native_client_buffer ---------------- */

#ifndef EGL_ANDROID_create_native_client_buffer
#define EGL_ANDROID_create_native_client_buffer 1

#define EGL_NATIVE_BUFFER_USAGE_PROTECTED_BIT_ANDROID 0x00000001
#define EGL_NATIVE_BUFFER_USAGE_RENDERBUFFER_BIT_ANDROID 0x00000002
#define EGL_NATIVE_BUFFER_USAGE_TEXTURE_BIT_ANDROID 0x00000004
#define EGL_NATIVE_BUFFER_USAGE_ANDROID 0x3143

typedef EGLClientBuffer  ( * PFNEGLCREATENATIVECLIENTBUFFERANDROIDPROC) (const EGLint * attrib_list);

#define eglCreateNativeClientBufferANDROID EGLEW_GET_FUN(__eglewCreateNativeClientBufferANDROID)

#define EGLEW_ANDROID_create_native_client_buffer EGLEW_GET_VAR(__EGLEW_ANDROID_create_native_client_buffer)

#endif /* EGL_ANDROID_create_native_client_buffer */

/* --------------------- EGL_ANDROID_framebuffer_target -------------------- */

#ifndef EGL_ANDROID_framebuffer_target
#define EGL_ANDROID_framebuffer_target 1

#define EGL_FRAMEBUFFER_TARGET_ANDROID 0x3147

#define EGLEW_ANDROID_framebuffer_target EGLEW_GET_VAR(__EGLEW_ANDROID_framebuffer_target)

#endif /* EGL_ANDROID_framebuffer_target */

/* ----------------- EGL_ANDROID_front_buffer_auto_refresh ----------------- */

#ifndef EGL_ANDROID_front_buffer_auto_refresh
#define EGL_ANDROID_front_buffer_auto_refresh 1

#define EGL_FRONT_BUFFER_AUTO_REFRESH_ANDROID 0x314C

#define EGLEW_ANDROID_front_buffer_auto_refresh EGLEW_GET_VAR(__EGLEW_ANDROID_front_buffer_auto_refresh)

#endif /* EGL_ANDROID_front_buffer_auto_refresh */

/* -------------------- EGL_ANDROID_image_native_buffer -------------------- */

#ifndef EGL_ANDROID_image_native_buffer
#define EGL_ANDROID_image_native_buffer 1

#define EGL_NATIVE_BUFFER_ANDROID 0x3140

#define EGLEW_ANDROID_image_native_buffer EGLEW_GET_VAR(__EGLEW_ANDROID_image_native_buffer)

#endif /* EGL_ANDROID_image_native_buffer */

/* --------------------- EGL_ANDROID_native_fence_sync --------------------- */

#ifndef EGL_ANDROID_native_fence_sync
#define EGL_ANDROID_native_fence_sync 1

#define EGL_SYNC_NATIVE_FENCE_ANDROID 0x3144
#define EGL_SYNC_NATIVE_FENCE_FD_ANDROID 0x3145
#define EGL_SYNC_NATIVE_FENCE_SIGNALED_ANDROID 0x3146

typedef EGLint  ( * PFNEGLDUPNATIVEFENCEFDANDROIDPROC) (EGLDisplay  dpy, EGLSyncKHR  sync);

#define eglDupNativeFenceFDANDROID EGLEW_GET_FUN(__eglewDupNativeFenceFDANDROID)

#define EGLEW_ANDROID_native_fence_sync EGLEW_GET_VAR(__EGLEW_ANDROID_native_fence_sync)

#endif /* EGL_ANDROID_native_fence_sync */

/* --------------------- EGL_ANDROID_presentation_time --------------------- */

#ifndef EGL_ANDROID_presentation_time
#define EGL_ANDROID_presentation_time 1

typedef EGLBoolean  ( * PFNEGLPRESENTATIONTIMEANDROIDPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLnsecsANDROID  time);

#define eglPresentationTimeANDROID EGLEW_GET_FUN(__eglewPresentationTimeANDROID)

#define EGLEW_ANDROID_presentation_time EGLEW_GET_VAR(__EGLEW_ANDROID_presentation_time)

#endif /* EGL_ANDROID_presentation_time */

/* ------------------------- EGL_ANDROID_recordable ------------------------ */

#ifndef EGL_ANDROID_recordable
#define EGL_ANDROID_recordable 1

#define EGL_RECORDABLE_ANDROID 0x3142

#define EGLEW_ANDROID_recordable EGLEW_GET_VAR(__EGLEW_ANDROID_recordable)

#endif /* EGL_ANDROID_recordable */

/* ---------------- EGL_ANGLE_d3d_share_handle_client_buffer --------------- */

#ifndef EGL_ANGLE_d3d_share_handle_client_buffer
#define EGL_ANGLE_d3d_share_handle_client_buffer 1

#define EGL_D3D_TEXTURE_2D_SHARE_HANDLE_ANGLE 0x3200

#define EGLEW_ANGLE_d3d_share_handle_client_buffer EGLEW_GET_VAR(__EGLEW_ANGLE_d3d_share_handle_client_buffer)

#endif /* EGL_ANGLE_d3d_share_handle_client_buffer */

/* -------------------------- EGL_ANGLE_device_d3d ------------------------- */

#ifndef EGL_ANGLE_device_d3d
#define EGL_ANGLE_device_d3d 1

#define EGL_D3D9_DEVICE_ANGLE 0x33A0
#define EGL_D3D11_DEVICE_ANGLE 0x33A1

#define EGLEW_ANGLE_device_d3d EGLEW_GET_VAR(__EGLEW_ANGLE_device_d3d)

#endif /* EGL_ANGLE_device_d3d */

/* -------------------- EGL_ANGLE_query_surface_pointer -------------------- */

#ifndef EGL_ANGLE_query_surface_pointer
#define EGL_ANGLE_query_surface_pointer 1

typedef EGLBoolean  ( * PFNEGLQUERYSURFACEPOINTERANGLEPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  attribute, void ** value);

#define eglQuerySurfacePointerANGLE EGLEW_GET_FUN(__eglewQuerySurfacePointerANGLE)

#define EGLEW_ANGLE_query_surface_pointer EGLEW_GET_VAR(__EGLEW_ANGLE_query_surface_pointer)

#endif /* EGL_ANGLE_query_surface_pointer */

/* ------------- EGL_ANGLE_surface_d3d_texture_2d_share_handle ------------- */

#ifndef EGL_ANGLE_surface_d3d_texture_2d_share_handle
#define EGL_ANGLE_surface_d3d_texture_2d_share_handle 1

#define EGL_D3D_TEXTURE_2D_SHARE_HANDLE_ANGLE 0x3200

#define EGLEW_ANGLE_surface_d3d_texture_2d_share_handle EGLEW_GET_VAR(__EGLEW_ANGLE_surface_d3d_texture_2d_share_handle)

#endif /* EGL_ANGLE_surface_d3d_texture_2d_share_handle */

/* ---------------------- EGL_ANGLE_window_fixed_size ---------------------- */

#ifndef EGL_ANGLE_window_fixed_size
#define EGL_ANGLE_window_fixed_size 1

#define EGL_FIXED_SIZE_ANGLE 0x3201

#define EGLEW_ANGLE_window_fixed_size EGLEW_GET_VAR(__EGLEW_ANGLE_window_fixed_size)

#endif /* EGL_ANGLE_window_fixed_size */

/* ------------------- EGL_ARM_pixmap_multisample_discard ------------------ */

#ifndef EGL_ARM_pixmap_multisample_discard
#define EGL_ARM_pixmap_multisample_discard 1

#define EGL_DISCARD_SAMPLES_ARM 0x3286

#define EGLEW_ARM_pixmap_multisample_discard EGLEW_GET_VAR(__EGLEW_ARM_pixmap_multisample_discard)

#endif /* EGL_ARM_pixmap_multisample_discard */

/* --------------------------- EGL_EXT_buffer_age -------------------------- */

#ifndef EGL_EXT_buffer_age
#define EGL_EXT_buffer_age 1

#define EGL_BUFFER_AGE_EXT 0x313D

#define EGLEW_EXT_buffer_age EGLEW_GET_VAR(__EGLEW_EXT_buffer_age)

#endif /* EGL_EXT_buffer_age */

/* ----------------------- EGL_EXT_client_extensions ----------------------- */

#ifndef EGL_EXT_client_extensions
#define EGL_EXT_client_extensions 1

#define EGLEW_EXT_client_extensions EGLEW_GET_VAR(__EGLEW_EXT_client_extensions)

#endif /* EGL_EXT_client_extensions */

/* ------------------- EGL_EXT_create_context_robustness ------------------- */

#ifndef EGL_EXT_create_context_robustness
#define EGL_EXT_create_context_robustness 1

#define EGL_CONTEXT_OPENGL_ROBUST_ACCESS_EXT 0x30BF
#define EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY_EXT 0x3138
#define EGL_NO_RESET_NOTIFICATION_EXT 0x31BE
#define EGL_LOSE_CONTEXT_ON_RESET_EXT 0x31BF

#define EGLEW_EXT_create_context_robustness EGLEW_GET_VAR(__EGLEW_EXT_create_context_robustness)

#endif /* EGL_EXT_create_context_robustness */

/* -------------------------- EGL_EXT_device_base -------------------------- */

#ifndef EGL_EXT_device_base
#define EGL_EXT_device_base 1

#define EGL_BAD_DEVICE_EXT 0x322B
#define EGL_DEVICE_EXT 0x322C

#define EGLEW_EXT_device_base EGLEW_GET_VAR(__EGLEW_EXT_device_base)

#endif /* EGL_EXT_device_base */

/* --------------------------- EGL_EXT_device_drm -------------------------- */

#ifndef EGL_EXT_device_drm
#define EGL_EXT_device_drm 1

#define EGL_DRM_DEVICE_FILE_EXT 0x3233

#define EGLEW_EXT_device_drm EGLEW_GET_VAR(__EGLEW_EXT_device_drm)

#endif /* EGL_EXT_device_drm */

/* ----------------------- EGL_EXT_device_enumeration ---------------------- */

#ifndef EGL_EXT_device_enumeration
#define EGL_EXT_device_enumeration 1

typedef EGLBoolean  ( * PFNEGLQUERYDEVICESEXTPROC) (EGLint  max_devices, EGLDeviceEXT * devices, EGLint * num_devices);

#define eglQueryDevicesEXT EGLEW_GET_FUN(__eglewQueryDevicesEXT)

#define EGLEW_EXT_device_enumeration EGLEW_GET_VAR(__EGLEW_EXT_device_enumeration)

#endif /* EGL_EXT_device_enumeration */

/* ------------------------- EGL_EXT_device_openwf ------------------------- */

#ifndef EGL_EXT_device_openwf
#define EGL_EXT_device_openwf 1

#define EGL_OPENWF_DEVICE_ID_EXT 0x3237

#define EGLEW_EXT_device_openwf EGLEW_GET_VAR(__EGLEW_EXT_device_openwf)

#endif /* EGL_EXT_device_openwf */

/* -------------------------- EGL_EXT_device_query ------------------------- */

#ifndef EGL_EXT_device_query
#define EGL_EXT_device_query 1

#define EGL_BAD_DEVICE_EXT 0x322B
#define EGL_DEVICE_EXT 0x322C

typedef EGLBoolean  ( * PFNEGLQUERYDEVICEATTRIBEXTPROC) (EGLDeviceEXT  device, EGLint  attribute, EGLAttrib * value);
typedef const char * ( * PFNEGLQUERYDEVICESTRINGEXTPROC) (EGLDeviceEXT  device, EGLint  name);
typedef EGLBoolean  ( * PFNEGLQUERYDISPLAYATTRIBEXTPROC) (EGLDisplay  dpy, EGLint  attribute, EGLAttrib * value);

#define eglQueryDeviceAttribEXT EGLEW_GET_FUN(__eglewQueryDeviceAttribEXT)
#define eglQueryDeviceStringEXT EGLEW_GET_FUN(__eglewQueryDeviceStringEXT)
#define eglQueryDisplayAttribEXT EGLEW_GET_FUN(__eglewQueryDisplayAttribEXT)

#define EGLEW_EXT_device_query EGLEW_GET_VAR(__EGLEW_EXT_device_query)

#endif /* EGL_EXT_device_query */

/* ---------------------- EGL_EXT_image_dma_buf_import --------------------- */

#ifndef EGL_EXT_image_dma_buf_import
#define EGL_EXT_image_dma_buf_import 1

#define EGL_LINUX_DMA_BUF_EXT 0x3270
#define EGL_LINUX_DRM_FOURCC_EXT 0x3271
#define EGL_DMA_BUF_PLANE0_FD_EXT 0x3272
#define EGL_DMA_BUF_PLANE0_OFFSET_EXT 0x3273
#define EGL_DMA_BUF_PLANE0_PITCH_EXT 0x3274
#define EGL_DMA_BUF_PLANE1_FD_EXT 0x3275
#define EGL_DMA_BUF_PLANE1_OFFSET_EXT 0x3276
#define EGL_DMA_BUF_PLANE1_PITCH_EXT 0x3277
#define EGL_DMA_BUF_PLANE2_FD_EXT 0x3278
#define EGL_DMA_BUF_PLANE2_OFFSET_EXT 0x3279
#define EGL_DMA_BUF_PLANE2_PITCH_EXT 0x327A
#define EGL_YUV_COLOR_SPACE_HINT_EXT 0x327B
#define EGL_SAMPLE_RANGE_HINT_EXT 0x327C
#define EGL_YUV_CHROMA_HORIZONTAL_SITING_HINT_EXT 0x327D
#define EGL_YUV_CHROMA_VERTICAL_SITING_HINT_EXT 0x327E
#define EGL_ITU_REC601_EXT 0x327F
#define EGL_ITU_REC709_EXT 0x3280
#define EGL_ITU_REC2020_EXT 0x3281
#define EGL_YUV_FULL_RANGE_EXT 0x3282
#define EGL_YUV_NARROW_RANGE_EXT 0x3283
#define EGL_YUV_CHROMA_SITING_0_EXT 0x3284
#define EGL_YUV_CHROMA_SITING_0_5_EXT 0x3285

#define EGLEW_EXT_image_dma_buf_import EGLEW_GET_VAR(__EGLEW_EXT_image_dma_buf_import)

#endif /* EGL_EXT_image_dma_buf_import */

/* ------------------------ EGL_EXT_multiview_window ----------------------- */

#ifndef EGL_EXT_multiview_window
#define EGL_EXT_multiview_window 1

#define EGL_MULTIVIEW_VIEW_COUNT_EXT 0x3134

#define EGLEW_EXT_multiview_window EGLEW_GET_VAR(__EGLEW_EXT_multiview_window)

#endif /* EGL_EXT_multiview_window */

/* -------------------------- EGL_EXT_output_base -------------------------- */

#ifndef EGL_EXT_output_base
#define EGL_EXT_output_base 1

#define EGL_BAD_OUTPUT_LAYER_EXT 0x322D
#define EGL_BAD_OUTPUT_PORT_EXT 0x322E
#define EGL_SWAP_INTERVAL_EXT 0x322F

typedef EGLBoolean  ( * PFNEGLGETOUTPUTLAYERSEXTPROC) (EGLDisplay  dpy, const EGLAttrib * attrib_list, EGLOutputLayerEXT * layers, EGLint  max_layers, EGLint * num_layers);
typedef EGLBoolean  ( * PFNEGLGETOUTPUTPORTSEXTPROC) (EGLDisplay  dpy, const EGLAttrib * attrib_list, EGLOutputPortEXT * ports, EGLint  max_ports, EGLint * num_ports);
typedef EGLBoolean  ( * PFNEGLOUTPUTLAYERATTRIBEXTPROC) (EGLDisplay  dpy, EGLOutputLayerEXT  layer, EGLint  attribute, EGLAttrib  value);
typedef EGLBoolean  ( * PFNEGLOUTPUTPORTATTRIBEXTPROC) (EGLDisplay  dpy, EGLOutputPortEXT  port, EGLint  attribute, EGLAttrib  value);
typedef EGLBoolean  ( * PFNEGLQUERYOUTPUTLAYERATTRIBEXTPROC) (EGLDisplay  dpy, EGLOutputLayerEXT  layer, EGLint  attribute, EGLAttrib * value);
typedef const char * ( * PFNEGLQUERYOUTPUTLAYERSTRINGEXTPROC) (EGLDisplay  dpy, EGLOutputLayerEXT  layer, EGLint  name);
typedef EGLBoolean  ( * PFNEGLQUERYOUTPUTPORTATTRIBEXTPROC) (EGLDisplay  dpy, EGLOutputPortEXT  port, EGLint  attribute, EGLAttrib * value);
typedef const char * ( * PFNEGLQUERYOUTPUTPORTSTRINGEXTPROC) (EGLDisplay  dpy, EGLOutputPortEXT  port, EGLint  name);

#define eglGetOutputLayersEXT EGLEW_GET_FUN(__eglewGetOutputLayersEXT)
#define eglGetOutputPortsEXT EGLEW_GET_FUN(__eglewGetOutputPortsEXT)
#define eglOutputLayerAttribEXT EGLEW_GET_FUN(__eglewOutputLayerAttribEXT)
#define eglOutputPortAttribEXT EGLEW_GET_FUN(__eglewOutputPortAttribEXT)
#define eglQueryOutputLayerAttribEXT EGLEW_GET_FUN(__eglewQueryOutputLayerAttribEXT)
#define eglQueryOutputLayerStringEXT EGLEW_GET_FUN(__eglewQueryOutputLayerStringEXT)
#define eglQueryOutputPortAttribEXT EGLEW_GET_FUN(__eglewQueryOutputPortAttribEXT)
#define eglQueryOutputPortStringEXT EGLEW_GET_FUN(__eglewQueryOutputPortStringEXT)

#define EGLEW_EXT_output_base EGLEW_GET_VAR(__EGLEW_EXT_output_base)

#endif /* EGL_EXT_output_base */

/* --------------------------- EGL_EXT_output_drm -------------------------- */

#ifndef EGL_EXT_output_drm
#define EGL_EXT_output_drm 1

#define EGL_DRM_CRTC_EXT 0x3234
#define EGL_DRM_PLANE_EXT 0x3235
#define EGL_DRM_CONNECTOR_EXT 0x3236

#define EGLEW_EXT_output_drm EGLEW_GET_VAR(__EGLEW_EXT_output_drm)

#endif /* EGL_EXT_output_drm */

/* ------------------------- EGL_EXT_output_openwf ------------------------- */

#ifndef EGL_EXT_output_openwf
#define EGL_EXT_output_openwf 1

#define EGL_OPENWF_PIPELINE_ID_EXT 0x3238
#define EGL_OPENWF_PORT_ID_EXT 0x3239

#define EGLEW_EXT_output_openwf EGLEW_GET_VAR(__EGLEW_EXT_output_openwf)

#endif /* EGL_EXT_output_openwf */

/* ------------------------- EGL_EXT_platform_base ------------------------- */

#ifndef EGL_EXT_platform_base
#define EGL_EXT_platform_base 1

typedef EGLSurface  ( * PFNEGLCREATEPLATFORMPIXMAPSURFACEEXTPROC) (EGLDisplay  dpy, EGLConfig  config, void * native_pixmap, const EGLint * attrib_list);
typedef EGLSurface  ( * PFNEGLCREATEPLATFORMWINDOWSURFACEEXTPROC) (EGLDisplay  dpy, EGLConfig  config, void * native_window, const EGLint * attrib_list);
typedef EGLDisplay  ( * PFNEGLGETPLATFORMDISPLAYEXTPROC) (EGLenum  platform, void * native_display, const EGLint * attrib_list);

#define eglCreatePlatformPixmapSurfaceEXT EGLEW_GET_FUN(__eglewCreatePlatformPixmapSurfaceEXT)
#define eglCreatePlatformWindowSurfaceEXT EGLEW_GET_FUN(__eglewCreatePlatformWindowSurfaceEXT)
#define eglGetPlatformDisplayEXT EGLEW_GET_FUN(__eglewGetPlatformDisplayEXT)

#define EGLEW_EXT_platform_base EGLEW_GET_VAR(__EGLEW_EXT_platform_base)

#endif /* EGL_EXT_platform_base */

/* ------------------------ EGL_EXT_platform_device ------------------------ */

#ifndef EGL_EXT_platform_device
#define EGL_EXT_platform_device 1

#define EGL_PLATFORM_DEVICE_EXT 0x313F

#define EGLEW_EXT_platform_device EGLEW_GET_VAR(__EGLEW_EXT_platform_device)

#endif /* EGL_EXT_platform_device */

/* ------------------------ EGL_EXT_platform_wayland ----------------------- */

#ifndef EGL_EXT_platform_wayland
#define EGL_EXT_platform_wayland 1

#define EGL_PLATFORM_WAYLAND_EXT 0x31D8

#define EGLEW_EXT_platform_wayland EGLEW_GET_VAR(__EGLEW_EXT_platform_wayland)

#endif /* EGL_EXT_platform_wayland */

/* -------------------------- EGL_EXT_platform_x11 ------------------------- */

#ifndef EGL_EXT_platform_x11
#define EGL_EXT_platform_x11 1

#define EGL_PLATFORM_X11_EXT 0x31D5
#define EGL_PLATFORM_X11_SCREEN_EXT 0x31D6

#define EGLEW_EXT_platform_x11 EGLEW_GET_VAR(__EGLEW_EXT_platform_x11)

#endif /* EGL_EXT_platform_x11 */

/* ----------------------- EGL_EXT_protected_content ----------------------- */

#ifndef EGL_EXT_protected_content
#define EGL_EXT_protected_content 1

#define EGL_PROTECTED_CONTENT_EXT 0x32C0

#define EGLEW_EXT_protected_content EGLEW_GET_VAR(__EGLEW_EXT_protected_content)

#endif /* EGL_EXT_protected_content */

/* ----------------------- EGL_EXT_protected_surface ----------------------- */

#ifndef EGL_EXT_protected_surface
#define EGL_EXT_protected_surface 1

#define EGL_PROTECTED_CONTENT_EXT 0x32C0

#define EGLEW_EXT_protected_surface EGLEW_GET_VAR(__EGLEW_EXT_protected_surface)

#endif /* EGL_EXT_protected_surface */

/* ------------------- EGL_EXT_stream_consumer_egloutput ------------------- */

#ifndef EGL_EXT_stream_consumer_egloutput
#define EGL_EXT_stream_consumer_egloutput 1

typedef EGLBoolean  ( * PFNEGLSTREAMCONSUMEROUTPUTEXTPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLOutputLayerEXT  layer);

#define eglStreamConsumerOutputEXT EGLEW_GET_FUN(__eglewStreamConsumerOutputEXT)

#define EGLEW_EXT_stream_consumer_egloutput EGLEW_GET_VAR(__EGLEW_EXT_stream_consumer_egloutput)

#endif /* EGL_EXT_stream_consumer_egloutput */

/* -------------------- EGL_EXT_swap_buffers_with_damage ------------------- */

#ifndef EGL_EXT_swap_buffers_with_damage
#define EGL_EXT_swap_buffers_with_damage 1

typedef EGLBoolean  ( * PFNEGLSWAPBUFFERSWITHDAMAGEEXTPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint * rects, EGLint  n_rects);

#define eglSwapBuffersWithDamageEXT EGLEW_GET_FUN(__eglewSwapBuffersWithDamageEXT)

#define EGLEW_EXT_swap_buffers_with_damage EGLEW_GET_VAR(__EGLEW_EXT_swap_buffers_with_damage)

#endif /* EGL_EXT_swap_buffers_with_damage */

/* -------------------------- EGL_EXT_yuv_surface -------------------------- */

#ifndef EGL_EXT_yuv_surface
#define EGL_EXT_yuv_surface 1

#define EGL_YUV_BUFFER_EXT 0x3300
#define EGL_YUV_ORDER_EXT 0x3301
#define EGL_YUV_ORDER_YUV_EXT 0x3302
#define EGL_YUV_ORDER_YVU_EXT 0x3303
#define EGL_YUV_ORDER_YUYV_EXT 0x3304
#define EGL_YUV_ORDER_UYVY_EXT 0x3305
#define EGL_YUV_ORDER_YVYU_EXT 0x3306
#define EGL_YUV_ORDER_VYUY_EXT 0x3307
#define EGL_YUV_ORDER_AYUV_EXT 0x3308
#define EGL_YUV_CSC_STANDARD_EXT 0x330A
#define EGL_YUV_CSC_STANDARD_601_EXT 0x330B
#define EGL_YUV_CSC_STANDARD_709_EXT 0x330C
#define EGL_YUV_CSC_STANDARD_2020_EXT 0x330D
#define EGL_YUV_NUMBER_OF_PLANES_EXT 0x3311
#define EGL_YUV_SUBSAMPLE_EXT 0x3312
#define EGL_YUV_SUBSAMPLE_4_2_0_EXT 0x3313
#define EGL_YUV_SUBSAMPLE_4_2_2_EXT 0x3314
#define EGL_YUV_SUBSAMPLE_4_4_4_EXT 0x3315
#define EGL_YUV_DEPTH_RANGE_EXT 0x3317
#define EGL_YUV_DEPTH_RANGE_LIMITED_EXT 0x3318
#define EGL_YUV_DEPTH_RANGE_FULL_EXT 0x3319
#define EGL_YUV_PLANE_BPP_EXT 0x331A
#define EGL_YUV_PLANE_BPP_0_EXT 0x331B
#define EGL_YUV_PLANE_BPP_8_EXT 0x331C
#define EGL_YUV_PLANE_BPP_10_EXT 0x331D

#define EGLEW_EXT_yuv_surface EGLEW_GET_VAR(__EGLEW_EXT_yuv_surface)

#endif /* EGL_EXT_yuv_surface */

/* -------------------------- EGL_HI_clientpixmap -------------------------- */

#ifndef EGL_HI_clientpixmap
#define EGL_HI_clientpixmap 1

#define EGL_CLIENT_PIXMAP_POINTER_HI 0x8F74

typedef EGLSurface  ( * PFNEGLCREATEPIXMAPSURFACEHIPROC) (EGLDisplay  dpy, EGLConfig  config, struct EGLClientPixmapHI * pixmap);

#define eglCreatePixmapSurfaceHI EGLEW_GET_FUN(__eglewCreatePixmapSurfaceHI)

#define EGLEW_HI_clientpixmap EGLEW_GET_VAR(__EGLEW_HI_clientpixmap)

#endif /* EGL_HI_clientpixmap */

/* -------------------------- EGL_HI_colorformats -------------------------- */

#ifndef EGL_HI_colorformats
#define EGL_HI_colorformats 1

#define EGL_COLOR_FORMAT_HI 0x8F70
#define EGL_COLOR_RGB_HI 0x8F71
#define EGL_COLOR_RGBA_HI 0x8F72
#define EGL_COLOR_ARGB_HI 0x8F73

#define EGLEW_HI_colorformats EGLEW_GET_VAR(__EGLEW_HI_colorformats)

#endif /* EGL_HI_colorformats */

/* ------------------------ EGL_IMG_context_priority ----------------------- */

#ifndef EGL_IMG_context_priority
#define EGL_IMG_context_priority 1

#define EGL_CONTEXT_PRIORITY_LEVEL_IMG 0x3100
#define EGL_CONTEXT_PRIORITY_HIGH_IMG 0x3101
#define EGL_CONTEXT_PRIORITY_MEDIUM_IMG 0x3102
#define EGL_CONTEXT_PRIORITY_LOW_IMG 0x3103

#define EGLEW_IMG_context_priority EGLEW_GET_VAR(__EGLEW_IMG_context_priority)

#endif /* EGL_IMG_context_priority */

/* ---------------------- EGL_IMG_image_plane_attribs ---------------------- */

#ifndef EGL_IMG_image_plane_attribs
#define EGL_IMG_image_plane_attribs 1

#define EGL_NATIVE_BUFFER_MULTIPLANE_SEPARATE_IMG 0x3105
#define EGL_NATIVE_BUFFER_PLANE_OFFSET_IMG 0x3106

#define EGLEW_IMG_image_plane_attribs EGLEW_GET_VAR(__EGLEW_IMG_image_plane_attribs)

#endif /* EGL_IMG_image_plane_attribs */

/* ---------------------------- EGL_KHR_cl_event --------------------------- */

#ifndef EGL_KHR_cl_event
#define EGL_KHR_cl_event 1

#define EGL_CL_EVENT_HANDLE_KHR 0x309C
#define EGL_SYNC_CL_EVENT_KHR 0x30FE
#define EGL_SYNC_CL_EVENT_COMPLETE_KHR 0x30FF

#define EGLEW_KHR_cl_event EGLEW_GET_VAR(__EGLEW_KHR_cl_event)

#endif /* EGL_KHR_cl_event */

/* --------------------------- EGL_KHR_cl_event2 --------------------------- */

#ifndef EGL_KHR_cl_event2
#define EGL_KHR_cl_event2 1

#define EGL_CL_EVENT_HANDLE_KHR 0x309C
#define EGL_SYNC_CL_EVENT_KHR 0x30FE
#define EGL_SYNC_CL_EVENT_COMPLETE_KHR 0x30FF

typedef EGLSyncKHR  ( * PFNEGLCREATESYNC64KHRPROC) (EGLDisplay  dpy, EGLenum  type, const EGLAttribKHR * attrib_list);

#define eglCreateSync64KHR EGLEW_GET_FUN(__eglewCreateSync64KHR)

#define EGLEW_KHR_cl_event2 EGLEW_GET_VAR(__EGLEW_KHR_cl_event2)

#endif /* EGL_KHR_cl_event2 */

/* ----------------- EGL_KHR_client_get_all_proc_addresses ----------------- */

#ifndef EGL_KHR_client_get_all_proc_addresses
#define EGL_KHR_client_get_all_proc_addresses 1

#define EGLEW_KHR_client_get_all_proc_addresses EGLEW_GET_VAR(__EGLEW_KHR_client_get_all_proc_addresses)

#endif /* EGL_KHR_client_get_all_proc_addresses */

/* ------------------------- EGL_KHR_config_attribs ------------------------ */

#ifndef EGL_KHR_config_attribs
#define EGL_KHR_config_attribs 1

#define EGL_VG_COLORSPACE_LINEAR_BIT_KHR 0x0020
#define EGL_VG_ALPHA_FORMAT_PRE_BIT_KHR 0x0040
#define EGL_CONFORMANT_KHR 0x3042

#define EGLEW_KHR_config_attribs EGLEW_GET_VAR(__EGLEW_KHR_config_attribs)

#endif /* EGL_KHR_config_attribs */

/* ------------------------- EGL_KHR_create_context ------------------------ */

#ifndef EGL_KHR_create_context
#define EGL_KHR_create_context 1

#define EGL_CONTEXT_OPENGL_CORE_PROFILE_BIT_KHR 0x00000001
#define EGL_CONTEXT_OPENGL_DEBUG_BIT_KHR 0x00000001
#define EGL_CONTEXT_OPENGL_COMPATIBILITY_PROFILE_BIT_KHR 0x00000002
#define EGL_CONTEXT_OPENGL_FORWARD_COMPATIBLE_BIT_KHR 0x00000002
#define EGL_CONTEXT_OPENGL_ROBUST_ACCESS_BIT_KHR 0x00000004
#define EGL_OPENGL_ES3_BIT 0x00000040
#define EGL_OPENGL_ES3_BIT_KHR 0x00000040
#define EGL_CONTEXT_MAJOR_VERSION_KHR 0x3098
#define EGL_CONTEXT_MINOR_VERSION_KHR 0x30FB
#define EGL_CONTEXT_FLAGS_KHR 0x30FC
#define EGL_CONTEXT_OPENGL_PROFILE_MASK_KHR 0x30FD
#define EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY_KHR 0x31BD
#define EGL_NO_RESET_NOTIFICATION_KHR 0x31BE
#define EGL_LOSE_CONTEXT_ON_RESET_KHR 0x31BF

#define EGLEW_KHR_create_context EGLEW_GET_VAR(__EGLEW_KHR_create_context)

#endif /* EGL_KHR_create_context */

/* -------------------- EGL_KHR_create_context_no_error -------------------- */

#ifndef EGL_KHR_create_context_no_error
#define EGL_KHR_create_context_no_error 1

#define EGL_CONTEXT_OPENGL_NO_ERROR_KHR 0x31B3

#define EGLEW_KHR_create_context_no_error EGLEW_GET_VAR(__EGLEW_KHR_create_context_no_error)

#endif /* EGL_KHR_create_context_no_error */

/* ----------------------------- EGL_KHR_debug ----------------------------- */

#ifndef EGL_KHR_debug
#define EGL_KHR_debug 1

#define EGL_OBJECT_THREAD_KHR 0x33B0
#define EGL_OBJECT_DISPLAY_KHR 0x33B1
#define EGL_OBJECT_CONTEXT_KHR 0x33B2
#define EGL_OBJECT_SURFACE_KHR 0x33B3
#define EGL_OBJECT_IMAGE_KHR 0x33B4
#define EGL_OBJECT_SYNC_KHR 0x33B5
#define EGL_OBJECT_STREAM_KHR 0x33B6
#define EGL_DEBUG_CALLBACK_KHR 0x33B8
#define EGL_DEBUG_MSG_CRITICAL_KHR 0x33B9
#define EGL_DEBUG_MSG_ERROR_KHR 0x33BA
#define EGL_DEBUG_MSG_WARN_KHR 0x33BB
#define EGL_DEBUG_MSG_INFO_KHR 0x33BC

typedef EGLint  ( * PFNEGLDEBUGMESSAGECONTROLKHRPROC) (EGLDEBUGPROCKHR  callback, const EGLAttrib * attrib_list);
typedef EGLint  ( * PFNEGLLABELOBJECTKHRPROC) (EGLDisplay  display, EGLenum  objectType, EGLObjectKHR  object, EGLLabelKHR  label);
typedef EGLBoolean  ( * PFNEGLQUERYDEBUGKHRPROC) (EGLint  attribute, EGLAttrib * value);

#define eglDebugMessageControlKHR EGLEW_GET_FUN(__eglewDebugMessageControlKHR)
#define eglLabelObjectKHR EGLEW_GET_FUN(__eglewLabelObjectKHR)
#define eglQueryDebugKHR EGLEW_GET_FUN(__eglewQueryDebugKHR)

#define EGLEW_KHR_debug EGLEW_GET_VAR(__EGLEW_KHR_debug)

#endif /* EGL_KHR_debug */

/* --------------------------- EGL_KHR_fence_sync -------------------------- */

#ifndef EGL_KHR_fence_sync
#define EGL_KHR_fence_sync 1

#define EGL_SYNC_PRIOR_COMMANDS_COMPLETE_KHR 0x30F0
#define EGL_SYNC_CONDITION_KHR 0x30F8
#define EGL_SYNC_FENCE_KHR 0x30F9

#define EGLEW_KHR_fence_sync EGLEW_GET_VAR(__EGLEW_KHR_fence_sync)

#endif /* EGL_KHR_fence_sync */

/* --------------------- EGL_KHR_get_all_proc_addresses -------------------- */

#ifndef EGL_KHR_get_all_proc_addresses
#define EGL_KHR_get_all_proc_addresses 1

#define EGLEW_KHR_get_all_proc_addresses EGLEW_GET_VAR(__EGLEW_KHR_get_all_proc_addresses)

#endif /* EGL_KHR_get_all_proc_addresses */

/* ------------------------- EGL_KHR_gl_colorspace ------------------------- */

#ifndef EGL_KHR_gl_colorspace
#define EGL_KHR_gl_colorspace 1

#define EGL_GL_COLORSPACE_SRGB_KHR 0x3089
#define EGL_GL_COLORSPACE_LINEAR_KHR 0x308A
#define EGL_GL_COLORSPACE_KHR 0x309D

#define EGLEW_KHR_gl_colorspace EGLEW_GET_VAR(__EGLEW_KHR_gl_colorspace)

#endif /* EGL_KHR_gl_colorspace */

/* --------------------- EGL_KHR_gl_renderbuffer_image --------------------- */

#ifndef EGL_KHR_gl_renderbuffer_image
#define EGL_KHR_gl_renderbuffer_image 1

#define EGL_GL_RENDERBUFFER_KHR 0x30B9

#define EGLEW_KHR_gl_renderbuffer_image EGLEW_GET_VAR(__EGLEW_KHR_gl_renderbuffer_image)

#endif /* EGL_KHR_gl_renderbuffer_image */

/* ---------------------- EGL_KHR_gl_texture_2D_image ---------------------- */

#ifndef EGL_KHR_gl_texture_2D_image
#define EGL_KHR_gl_texture_2D_image 1

#define EGL_GL_TEXTURE_2D_KHR 0x30B1
#define EGL_GL_TEXTURE_LEVEL_KHR 0x30BC

#define EGLEW_KHR_gl_texture_2D_image EGLEW_GET_VAR(__EGLEW_KHR_gl_texture_2D_image)

#endif /* EGL_KHR_gl_texture_2D_image */

/* ---------------------- EGL_KHR_gl_texture_3D_image ---------------------- */

#ifndef EGL_KHR_gl_texture_3D_image
#define EGL_KHR_gl_texture_3D_image 1

#define EGL_GL_TEXTURE_3D_KHR 0x30B2
#define EGL_GL_TEXTURE_ZOFFSET_KHR 0x30BD

#define EGLEW_KHR_gl_texture_3D_image EGLEW_GET_VAR(__EGLEW_KHR_gl_texture_3D_image)

#endif /* EGL_KHR_gl_texture_3D_image */

/* -------------------- EGL_KHR_gl_texture_cubemap_image ------------------- */

#ifndef EGL_KHR_gl_texture_cubemap_image
#define EGL_KHR_gl_texture_cubemap_image 1

#define EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_X_KHR 0x30B3
#define EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_X_KHR 0x30B4
#define EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Y_KHR 0x30B5
#define EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Y_KHR 0x30B6
#define EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Z_KHR 0x30B7
#define EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Z_KHR 0x30B8

#define EGLEW_KHR_gl_texture_cubemap_image EGLEW_GET_VAR(__EGLEW_KHR_gl_texture_cubemap_image)

#endif /* EGL_KHR_gl_texture_cubemap_image */

/* ----------------------------- EGL_KHR_image ----------------------------- */

#ifndef EGL_KHR_image
#define EGL_KHR_image 1

#define EGL_NATIVE_PIXMAP_KHR 0x30B0

typedef EGLImageKHR  ( * PFNEGLCREATEIMAGEKHRPROC) (EGLDisplay  dpy, EGLContext  ctx, EGLenum  target, EGLClientBuffer  buffer, const EGLint * attrib_list);
typedef EGLBoolean  ( * PFNEGLDESTROYIMAGEKHRPROC) (EGLDisplay  dpy, EGLImageKHR  image);

#define eglCreateImageKHR EGLEW_GET_FUN(__eglewCreateImageKHR)
#define eglDestroyImageKHR EGLEW_GET_FUN(__eglewDestroyImageKHR)

#define EGLEW_KHR_image EGLEW_GET_VAR(__EGLEW_KHR_image)

#endif /* EGL_KHR_image */

/* --------------------------- EGL_KHR_image_base -------------------------- */

#ifndef EGL_KHR_image_base
#define EGL_KHR_image_base 1

#define EGL_IMAGE_PRESERVED_KHR 0x30D2

#define EGLEW_KHR_image_base EGLEW_GET_VAR(__EGLEW_KHR_image_base)

#endif /* EGL_KHR_image_base */

/* -------------------------- EGL_KHR_image_pixmap ------------------------- */

#ifndef EGL_KHR_image_pixmap
#define EGL_KHR_image_pixmap 1

#define EGL_NATIVE_PIXMAP_KHR 0x30B0

#define EGLEW_KHR_image_pixmap EGLEW_GET_VAR(__EGLEW_KHR_image_pixmap)

#endif /* EGL_KHR_image_pixmap */

/* -------------------------- EGL_KHR_lock_surface ------------------------- */

#ifndef EGL_KHR_lock_surface
#define EGL_KHR_lock_surface 1

#define EGL_READ_SURFACE_BIT_KHR 0x0001
#define EGL_WRITE_SURFACE_BIT_KHR 0x0002
#define EGL_LOCK_SURFACE_BIT_KHR 0x0080
#define EGL_OPTIMAL_FORMAT_BIT_KHR 0x0100
#define EGL_MATCH_FORMAT_KHR 0x3043
#define EGL_FORMAT_RGB_565_EXACT_KHR 0x30C0
#define EGL_FORMAT_RGB_565_KHR 0x30C1
#define EGL_FORMAT_RGBA_8888_EXACT_KHR 0x30C2
#define EGL_FORMAT_RGBA_8888_KHR 0x30C3
#define EGL_MAP_PRESERVE_PIXELS_KHR 0x30C4
#define EGL_LOCK_USAGE_HINT_KHR 0x30C5
#define EGL_BITMAP_POINTER_KHR 0x30C6
#define EGL_BITMAP_PITCH_KHR 0x30C7
#define EGL_BITMAP_ORIGIN_KHR 0x30C8
#define EGL_BITMAP_PIXEL_RED_OFFSET_KHR 0x30C9
#define EGL_BITMAP_PIXEL_GREEN_OFFSET_KHR 0x30CA
#define EGL_BITMAP_PIXEL_BLUE_OFFSET_KHR 0x30CB
#define EGL_BITMAP_PIXEL_ALPHA_OFFSET_KHR 0x30CC
#define EGL_BITMAP_PIXEL_LUMINANCE_OFFSET_KHR 0x30CD
#define EGL_LOWER_LEFT_KHR 0x30CE
#define EGL_UPPER_LEFT_KHR 0x30CF

typedef EGLBoolean  ( * PFNEGLLOCKSURFACEKHRPROC) (EGLDisplay  dpy, EGLSurface  surface, const EGLint * attrib_list);
typedef EGLBoolean  ( * PFNEGLUNLOCKSURFACEKHRPROC) (EGLDisplay  dpy, EGLSurface  surface);

#define eglLockSurfaceKHR EGLEW_GET_FUN(__eglewLockSurfaceKHR)
#define eglUnlockSurfaceKHR EGLEW_GET_FUN(__eglewUnlockSurfaceKHR)

#define EGLEW_KHR_lock_surface EGLEW_GET_VAR(__EGLEW_KHR_lock_surface)

#endif /* EGL_KHR_lock_surface */

/* ------------------------- EGL_KHR_lock_surface2 ------------------------- */

#ifndef EGL_KHR_lock_surface2
#define EGL_KHR_lock_surface2 1

#define EGL_BITMAP_PIXEL_SIZE_KHR 0x3110

#define EGLEW_KHR_lock_surface2 EGLEW_GET_VAR(__EGLEW_KHR_lock_surface2)

#endif /* EGL_KHR_lock_surface2 */

/* ------------------------- EGL_KHR_lock_surface3 ------------------------- */

#ifndef EGL_KHR_lock_surface3
#define EGL_KHR_lock_surface3 1

#define EGL_READ_SURFACE_BIT_KHR 0x0001
#define EGL_WRITE_SURFACE_BIT_KHR 0x0002
#define EGL_LOCK_SURFACE_BIT_KHR 0x0080
#define EGL_OPTIMAL_FORMAT_BIT_KHR 0x0100
#define EGL_MATCH_FORMAT_KHR 0x3043
#define EGL_FORMAT_RGB_565_EXACT_KHR 0x30C0
#define EGL_FORMAT_RGB_565_KHR 0x30C1
#define EGL_FORMAT_RGBA_8888_EXACT_KHR 0x30C2
#define EGL_FORMAT_RGBA_8888_KHR 0x30C3
#define EGL_MAP_PRESERVE_PIXELS_KHR 0x30C4
#define EGL_LOCK_USAGE_HINT_KHR 0x30C5
#define EGL_BITMAP_POINTER_KHR 0x30C6
#define EGL_BITMAP_PITCH_KHR 0x30C7
#define EGL_BITMAP_ORIGIN_KHR 0x30C8
#define EGL_BITMAP_PIXEL_RED_OFFSET_KHR 0x30C9
#define EGL_BITMAP_PIXEL_GREEN_OFFSET_KHR 0x30CA
#define EGL_BITMAP_PIXEL_BLUE_OFFSET_KHR 0x30CB
#define EGL_BITMAP_PIXEL_ALPHA_OFFSET_KHR 0x30CC
#define EGL_BITMAP_PIXEL_LUMINANCE_OFFSET_KHR 0x30CD
#define EGL_LOWER_LEFT_KHR 0x30CE
#define EGL_UPPER_LEFT_KHR 0x30CF
#define EGL_BITMAP_PIXEL_SIZE_KHR 0x3110

typedef EGLBoolean  ( * PFNEGLQUERYSURFACE64KHRPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  attribute, EGLAttribKHR * value);

#define eglQuerySurface64KHR EGLEW_GET_FUN(__eglewQuerySurface64KHR)

#define EGLEW_KHR_lock_surface3 EGLEW_GET_VAR(__EGLEW_KHR_lock_surface3)

#endif /* EGL_KHR_lock_surface3 */

/* --------------------- EGL_KHR_mutable_render_buffer --------------------- */

#ifndef EGL_KHR_mutable_render_buffer
#define EGL_KHR_mutable_render_buffer 1

#define EGL_MUTABLE_RENDER_BUFFER_BIT_KHR 0x1000

#define EGLEW_KHR_mutable_render_buffer EGLEW_GET_VAR(__EGLEW_KHR_mutable_render_buffer)

#endif /* EGL_KHR_mutable_render_buffer */

/* ------------------------- EGL_KHR_partial_update ------------------------ */

#ifndef EGL_KHR_partial_update
#define EGL_KHR_partial_update 1

#define EGL_BUFFER_AGE_KHR 0x313D

typedef EGLBoolean  ( * PFNEGLSETDAMAGEREGIONKHRPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint * rects, EGLint  n_rects);

#define eglSetDamageRegionKHR EGLEW_GET_FUN(__eglewSetDamageRegionKHR)

#define EGLEW_KHR_partial_update EGLEW_GET_VAR(__EGLEW_KHR_partial_update)

#endif /* EGL_KHR_partial_update */

/* ------------------------ EGL_KHR_platform_android ----------------------- */

#ifndef EGL_KHR_platform_android
#define EGL_KHR_platform_android 1

#define EGL_PLATFORM_ANDROID_KHR 0x3141

#define EGLEW_KHR_platform_android EGLEW_GET_VAR(__EGLEW_KHR_platform_android)

#endif /* EGL_KHR_platform_android */

/* -------------------------- EGL_KHR_platform_gbm ------------------------- */

#ifndef EGL_KHR_platform_gbm
#define EGL_KHR_platform_gbm 1

#define EGL_PLATFORM_GBM_KHR 0x31D7

#define EGLEW_KHR_platform_gbm EGLEW_GET_VAR(__EGLEW_KHR_platform_gbm)

#endif /* EGL_KHR_platform_gbm */

/* ------------------------ EGL_KHR_platform_wayland ----------------------- */

#ifndef EGL_KHR_platform_wayland
#define EGL_KHR_platform_wayland 1

#define EGL_PLATFORM_WAYLAND_KHR 0x31D8

#define EGLEW_KHR_platform_wayland EGLEW_GET_VAR(__EGLEW_KHR_platform_wayland)

#endif /* EGL_KHR_platform_wayland */

/* -------------------------- EGL_KHR_platform_x11 ------------------------- */

#ifndef EGL_KHR_platform_x11
#define EGL_KHR_platform_x11 1

#define EGL_PLATFORM_X11_KHR 0x31D5
#define EGL_PLATFORM_X11_SCREEN_KHR 0x31D6

#define EGLEW_KHR_platform_x11 EGLEW_GET_VAR(__EGLEW_KHR_platform_x11)

#endif /* EGL_KHR_platform_x11 */

/* ------------------------- EGL_KHR_reusable_sync ------------------------- */

#ifndef EGL_KHR_reusable_sync
#define EGL_KHR_reusable_sync 1

#define EGL_SYNC_FLUSH_COMMANDS_BIT_KHR 0x0001
#define EGL_SYNC_STATUS_KHR 0x30F1
#define EGL_SIGNALED_KHR 0x30F2
#define EGL_UNSIGNALED_KHR 0x30F3
#define EGL_TIMEOUT_EXPIRED_KHR 0x30F5
#define EGL_CONDITION_SATISFIED_KHR 0x30F6
#define EGL_SYNC_TYPE_KHR 0x30F7
#define EGL_SYNC_REUSABLE_KHR 0x30FA
#define EGL_FOREVER_KHR 0xFFFFFFFFFFFFFFFF

typedef EGLint  ( * PFNEGLCLIENTWAITSYNCKHRPROC) (EGLDisplay  dpy, EGLSyncKHR  sync, EGLint  flags, EGLTimeKHR  timeout);
typedef EGLSyncKHR  ( * PFNEGLCREATESYNCKHRPROC) (EGLDisplay  dpy, EGLenum  type, const EGLint * attrib_list);
typedef EGLBoolean  ( * PFNEGLDESTROYSYNCKHRPROC) (EGLDisplay  dpy, EGLSyncKHR  sync);
typedef EGLBoolean  ( * PFNEGLGETSYNCATTRIBKHRPROC) (EGLDisplay  dpy, EGLSyncKHR  sync, EGLint  attribute, EGLint * value);
typedef EGLBoolean  ( * PFNEGLSIGNALSYNCKHRPROC) (EGLDisplay  dpy, EGLSyncKHR  sync, EGLenum  mode);

#define eglClientWaitSyncKHR EGLEW_GET_FUN(__eglewClientWaitSyncKHR)
#define eglCreateSyncKHR EGLEW_GET_FUN(__eglewCreateSyncKHR)
#define eglDestroySyncKHR EGLEW_GET_FUN(__eglewDestroySyncKHR)
#define eglGetSyncAttribKHR EGLEW_GET_FUN(__eglewGetSyncAttribKHR)
#define eglSignalSyncKHR EGLEW_GET_FUN(__eglewSignalSyncKHR)

#define EGLEW_KHR_reusable_sync EGLEW_GET_VAR(__EGLEW_KHR_reusable_sync)

#endif /* EGL_KHR_reusable_sync */

/* ----------------------------- EGL_KHR_stream ---------------------------- */

#ifndef EGL_KHR_stream
#define EGL_KHR_stream 1

#define EGL_CONSUMER_LATENCY_USEC_KHR 0x3210
#define EGL_PRODUCER_FRAME_KHR 0x3212
#define EGL_CONSUMER_FRAME_KHR 0x3213
#define EGL_STREAM_STATE_KHR 0x3214
#define EGL_STREAM_STATE_CREATED_KHR 0x3215
#define EGL_STREAM_STATE_CONNECTING_KHR 0x3216
#define EGL_STREAM_STATE_EMPTY_KHR 0x3217
#define EGL_STREAM_STATE_NEW_FRAME_AVAILABLE_KHR 0x3218
#define EGL_STREAM_STATE_OLD_FRAME_AVAILABLE_KHR 0x3219
#define EGL_STREAM_STATE_DISCONNECTED_KHR 0x321A
#define EGL_BAD_STREAM_KHR 0x321B
#define EGL_BAD_STATE_KHR 0x321C

typedef EGLStreamKHR  ( * PFNEGLCREATESTREAMKHRPROC) (EGLDisplay  dpy, const EGLint * attrib_list);
typedef EGLBoolean  ( * PFNEGLDESTROYSTREAMKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream);
typedef EGLBoolean  ( * PFNEGLQUERYSTREAMKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLenum  attribute, EGLint * value);
typedef EGLBoolean  ( * PFNEGLQUERYSTREAMU64KHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLenum  attribute, EGLuint64KHR * value);
typedef EGLBoolean  ( * PFNEGLSTREAMATTRIBKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLenum  attribute, EGLint  value);

#define eglCreateStreamKHR EGLEW_GET_FUN(__eglewCreateStreamKHR)
#define eglDestroyStreamKHR EGLEW_GET_FUN(__eglewDestroyStreamKHR)
#define eglQueryStreamKHR EGLEW_GET_FUN(__eglewQueryStreamKHR)
#define eglQueryStreamu64KHR EGLEW_GET_FUN(__eglewQueryStreamu64KHR)
#define eglStreamAttribKHR EGLEW_GET_FUN(__eglewStreamAttribKHR)

#define EGLEW_KHR_stream EGLEW_GET_VAR(__EGLEW_KHR_stream)

#endif /* EGL_KHR_stream */

/* ------------------- EGL_KHR_stream_consumer_gltexture ------------------- */

#ifndef EGL_KHR_stream_consumer_gltexture
#define EGL_KHR_stream_consumer_gltexture 1

#define EGL_CONSUMER_ACQUIRE_TIMEOUT_USEC_KHR 0x321E

typedef EGLBoolean  ( * PFNEGLSTREAMCONSUMERACQUIREKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream);
typedef EGLBoolean  ( * PFNEGLSTREAMCONSUMERGLTEXTUREEXTERNALKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream);
typedef EGLBoolean  ( * PFNEGLSTREAMCONSUMERRELEASEKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream);

#define eglStreamConsumerAcquireKHR EGLEW_GET_FUN(__eglewStreamConsumerAcquireKHR)
#define eglStreamConsumerGLTextureExternalKHR EGLEW_GET_FUN(__eglewStreamConsumerGLTextureExternalKHR)
#define eglStreamConsumerReleaseKHR EGLEW_GET_FUN(__eglewStreamConsumerReleaseKHR)

#define EGLEW_KHR_stream_consumer_gltexture EGLEW_GET_VAR(__EGLEW_KHR_stream_consumer_gltexture)

#endif /* EGL_KHR_stream_consumer_gltexture */

/* -------------------- EGL_KHR_stream_cross_process_fd -------------------- */

#ifndef EGL_KHR_stream_cross_process_fd
#define EGL_KHR_stream_cross_process_fd 1

typedef EGLStreamKHR  ( * PFNEGLCREATESTREAMFROMFILEDESCRIPTORKHRPROC) (EGLDisplay  dpy, EGLNativeFileDescriptorKHR  file_descriptor);
typedef EGLNativeFileDescriptorKHR  ( * PFNEGLGETSTREAMFILEDESCRIPTORKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream);

#define eglCreateStreamFromFileDescriptorKHR EGLEW_GET_FUN(__eglewCreateStreamFromFileDescriptorKHR)
#define eglGetStreamFileDescriptorKHR EGLEW_GET_FUN(__eglewGetStreamFileDescriptorKHR)

#define EGLEW_KHR_stream_cross_process_fd EGLEW_GET_VAR(__EGLEW_KHR_stream_cross_process_fd)

#endif /* EGL_KHR_stream_cross_process_fd */

/* -------------------------- EGL_KHR_stream_fifo -------------------------- */

#ifndef EGL_KHR_stream_fifo
#define EGL_KHR_stream_fifo 1

#define EGL_STREAM_FIFO_LENGTH_KHR 0x31FC
#define EGL_STREAM_TIME_NOW_KHR 0x31FD
#define EGL_STREAM_TIME_CONSUMER_KHR 0x31FE
#define EGL_STREAM_TIME_PRODUCER_KHR 0x31FF

typedef EGLBoolean  ( * PFNEGLQUERYSTREAMTIMEKHRPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLenum  attribute, EGLTimeKHR * value);

#define eglQueryStreamTimeKHR EGLEW_GET_FUN(__eglewQueryStreamTimeKHR)

#define EGLEW_KHR_stream_fifo EGLEW_GET_VAR(__EGLEW_KHR_stream_fifo)

#endif /* EGL_KHR_stream_fifo */

/* ----------------- EGL_KHR_stream_producer_aldatalocator ----------------- */

#ifndef EGL_KHR_stream_producer_aldatalocator
#define EGL_KHR_stream_producer_aldatalocator 1

#define EGLEW_KHR_stream_producer_aldatalocator EGLEW_GET_VAR(__EGLEW_KHR_stream_producer_aldatalocator)

#endif /* EGL_KHR_stream_producer_aldatalocator */

/* ------------------- EGL_KHR_stream_producer_eglsurface ------------------ */

#ifndef EGL_KHR_stream_producer_eglsurface
#define EGL_KHR_stream_producer_eglsurface 1

#define EGL_STREAM_BIT_KHR 0x0800

typedef EGLSurface  ( * PFNEGLCREATESTREAMPRODUCERSURFACEKHRPROC) (EGLDisplay  dpy, EGLConfig  config, EGLStreamKHR  stream, const EGLint * attrib_list);

#define eglCreateStreamProducerSurfaceKHR EGLEW_GET_FUN(__eglewCreateStreamProducerSurfaceKHR)

#define EGLEW_KHR_stream_producer_eglsurface EGLEW_GET_VAR(__EGLEW_KHR_stream_producer_eglsurface)

#endif /* EGL_KHR_stream_producer_eglsurface */

/* ---------------------- EGL_KHR_surfaceless_context ---------------------- */

#ifndef EGL_KHR_surfaceless_context
#define EGL_KHR_surfaceless_context 1

#define EGLEW_KHR_surfaceless_context EGLEW_GET_VAR(__EGLEW_KHR_surfaceless_context)

#endif /* EGL_KHR_surfaceless_context */

/* -------------------- EGL_KHR_swap_buffers_with_damage ------------------- */

#ifndef EGL_KHR_swap_buffers_with_damage
#define EGL_KHR_swap_buffers_with_damage 1

typedef EGLBoolean  ( * PFNEGLSWAPBUFFERSWITHDAMAGEKHRPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint * rects, EGLint  n_rects);

#define eglSwapBuffersWithDamageKHR EGLEW_GET_FUN(__eglewSwapBuffersWithDamageKHR)

#define EGLEW_KHR_swap_buffers_with_damage EGLEW_GET_VAR(__EGLEW_KHR_swap_buffers_with_damage)

#endif /* EGL_KHR_swap_buffers_with_damage */

/* ------------------------ EGL_KHR_vg_parent_image ------------------------ */

#ifndef EGL_KHR_vg_parent_image
#define EGL_KHR_vg_parent_image 1

#define EGL_VG_PARENT_IMAGE_KHR 0x30BA

#define EGLEW_KHR_vg_parent_image EGLEW_GET_VAR(__EGLEW_KHR_vg_parent_image)

#endif /* EGL_KHR_vg_parent_image */

/* --------------------------- EGL_KHR_wait_sync --------------------------- */

#ifndef EGL_KHR_wait_sync
#define EGL_KHR_wait_sync 1

typedef EGLint  ( * PFNEGLWAITSYNCKHRPROC) (EGLDisplay  dpy, EGLSyncKHR  sync, EGLint  flags);

#define eglWaitSyncKHR EGLEW_GET_FUN(__eglewWaitSyncKHR)

#define EGLEW_KHR_wait_sync EGLEW_GET_VAR(__EGLEW_KHR_wait_sync)

#endif /* EGL_KHR_wait_sync */

/* --------------------------- EGL_MESA_drm_image -------------------------- */

#ifndef EGL_MESA_drm_image
#define EGL_MESA_drm_image 1

#define EGL_DRM_BUFFER_USE_SCANOUT_MESA 0x00000001
#define EGL_DRM_BUFFER_USE_SHARE_MESA 0x00000002
#define EGL_DRM_BUFFER_FORMAT_MESA 0x31D0
#define EGL_DRM_BUFFER_USE_MESA 0x31D1
#define EGL_DRM_BUFFER_FORMAT_ARGB32_MESA 0x31D2
#define EGL_DRM_BUFFER_MESA 0x31D3
#define EGL_DRM_BUFFER_STRIDE_MESA 0x31D4

typedef EGLImageKHR  ( * PFNEGLCREATEDRMIMAGEMESAPROC) (EGLDisplay  dpy, const EGLint * attrib_list);
typedef EGLBoolean  ( * PFNEGLEXPORTDRMIMAGEMESAPROC) (EGLDisplay  dpy, EGLImageKHR  image, EGLint * name, EGLint * handle, EGLint * stride);

#define eglCreateDRMImageMESA EGLEW_GET_FUN(__eglewCreateDRMImageMESA)
#define eglExportDRMImageMESA EGLEW_GET_FUN(__eglewExportDRMImageMESA)

#define EGLEW_MESA_drm_image EGLEW_GET_VAR(__EGLEW_MESA_drm_image)

#endif /* EGL_MESA_drm_image */

/* --------------------- EGL_MESA_image_dma_buf_export --------------------- */

#ifndef EGL_MESA_image_dma_buf_export
#define EGL_MESA_image_dma_buf_export 1

typedef EGLBoolean  ( * PFNEGLEXPORTDMABUFIMAGEMESAPROC) (EGLDisplay  dpy, EGLImageKHR  image, int * fds, EGLint * strides, EGLint * offsets);
typedef EGLBoolean  ( * PFNEGLEXPORTDMABUFIMAGEQUERYMESAPROC) (EGLDisplay  dpy, EGLImageKHR  image, int * fourcc, int * num_planes, EGLuint64KHR * modifiers);

#define eglExportDMABUFImageMESA EGLEW_GET_FUN(__eglewExportDMABUFImageMESA)
#define eglExportDMABUFImageQueryMESA EGLEW_GET_FUN(__eglewExportDMABUFImageQueryMESA)

#define EGLEW_MESA_image_dma_buf_export EGLEW_GET_VAR(__EGLEW_MESA_image_dma_buf_export)

#endif /* EGL_MESA_image_dma_buf_export */

/* ------------------------- EGL_MESA_platform_gbm ------------------------- */

#ifndef EGL_MESA_platform_gbm
#define EGL_MESA_platform_gbm 1

#define EGL_PLATFORM_GBM_MESA 0x31D7

#define EGLEW_MESA_platform_gbm EGLEW_GET_VAR(__EGLEW_MESA_platform_gbm)

#endif /* EGL_MESA_platform_gbm */

/* -------------------------- EGL_NOK_swap_region -------------------------- */

#ifndef EGL_NOK_swap_region
#define EGL_NOK_swap_region 1

typedef EGLBoolean  ( * PFNEGLSWAPBUFFERSREGIONNOKPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  numRects, const EGLint * rects);

#define eglSwapBuffersRegionNOK EGLEW_GET_FUN(__eglewSwapBuffersRegionNOK)

#define EGLEW_NOK_swap_region EGLEW_GET_VAR(__EGLEW_NOK_swap_region)

#endif /* EGL_NOK_swap_region */

/* -------------------------- EGL_NOK_swap_region2 ------------------------- */

#ifndef EGL_NOK_swap_region2
#define EGL_NOK_swap_region2 1

typedef EGLBoolean  ( * PFNEGLSWAPBUFFERSREGION2NOKPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  numRects, const EGLint * rects);

#define eglSwapBuffersRegion2NOK EGLEW_GET_FUN(__eglewSwapBuffersRegion2NOK)

#define EGLEW_NOK_swap_region2 EGLEW_GET_VAR(__EGLEW_NOK_swap_region2)

#endif /* EGL_NOK_swap_region2 */

/* ---------------------- EGL_NOK_texture_from_pixmap ---------------------- */

#ifndef EGL_NOK_texture_from_pixmap
#define EGL_NOK_texture_from_pixmap 1

#define EGL_Y_INVERTED_NOK 0x307F

#define EGLEW_NOK_texture_from_pixmap EGLEW_GET_VAR(__EGLEW_NOK_texture_from_pixmap)

#endif /* EGL_NOK_texture_from_pixmap */

/* ------------------------ EGL_NV_3dvision_surface ------------------------ */

#ifndef EGL_NV_3dvision_surface
#define EGL_NV_3dvision_surface 1

#define EGL_AUTO_STEREO_NV 0x3136

#define EGLEW_NV_3dvision_surface EGLEW_GET_VAR(__EGLEW_NV_3dvision_surface)

#endif /* EGL_NV_3dvision_surface */

/* ------------------------- EGL_NV_coverage_sample ------------------------ */

#ifndef EGL_NV_coverage_sample
#define EGL_NV_coverage_sample 1

#define EGL_COVERAGE_BUFFERS_NV 0x30E0
#define EGL_COVERAGE_SAMPLES_NV 0x30E1

#define EGLEW_NV_coverage_sample EGLEW_GET_VAR(__EGLEW_NV_coverage_sample)

#endif /* EGL_NV_coverage_sample */

/* --------------------- EGL_NV_coverage_sample_resolve -------------------- */

#ifndef EGL_NV_coverage_sample_resolve
#define EGL_NV_coverage_sample_resolve 1

#define EGL_COVERAGE_SAMPLE_RESOLVE_NV 0x3131
#define EGL_COVERAGE_SAMPLE_RESOLVE_DEFAULT_NV 0x3132
#define EGL_COVERAGE_SAMPLE_RESOLVE_NONE_NV 0x3133

#define EGLEW_NV_coverage_sample_resolve EGLEW_GET_VAR(__EGLEW_NV_coverage_sample_resolve)

#endif /* EGL_NV_coverage_sample_resolve */

/* --------------------------- EGL_NV_cuda_event --------------------------- */

#ifndef EGL_NV_cuda_event
#define EGL_NV_cuda_event 1

#define EGL_CUDA_EVENT_HANDLE_NV 0x323B
#define EGL_SYNC_CUDA_EVENT_NV 0x323C
#define EGL_SYNC_CUDA_EVENT_COMPLETE_NV 0x323D

#define EGLEW_NV_cuda_event EGLEW_GET_VAR(__EGLEW_NV_cuda_event)

#endif /* EGL_NV_cuda_event */

/* ------------------------- EGL_NV_depth_nonlinear ------------------------ */

#ifndef EGL_NV_depth_nonlinear
#define EGL_NV_depth_nonlinear 1

#define EGL_DEPTH_ENCODING_NONE_NV 0
#define EGL_DEPTH_ENCODING_NV 0x30E2
#define EGL_DEPTH_ENCODING_NONLINEAR_NV 0x30E3

#define EGLEW_NV_depth_nonlinear EGLEW_GET_VAR(__EGLEW_NV_depth_nonlinear)

#endif /* EGL_NV_depth_nonlinear */

/* --------------------------- EGL_NV_device_cuda -------------------------- */

#ifndef EGL_NV_device_cuda
#define EGL_NV_device_cuda 1

#define EGL_CUDA_DEVICE_NV 0x323A

#define EGLEW_NV_device_cuda EGLEW_GET_VAR(__EGLEW_NV_device_cuda)

#endif /* EGL_NV_device_cuda */

/* -------------------------- EGL_NV_native_query -------------------------- */

#ifndef EGL_NV_native_query
#define EGL_NV_native_query 1

typedef EGLBoolean  ( * PFNEGLQUERYNATIVEDISPLAYNVPROC) (EGLDisplay  dpy, EGLNativeDisplayType * display_id);
typedef EGLBoolean  ( * PFNEGLQUERYNATIVEPIXMAPNVPROC) (EGLDisplay  dpy, EGLSurface  surf, EGLNativePixmapType * pixmap);
typedef EGLBoolean  ( * PFNEGLQUERYNATIVEWINDOWNVPROC) (EGLDisplay  dpy, EGLSurface  surf, EGLNativeWindowType * window);

#define eglQueryNativeDisplayNV EGLEW_GET_FUN(__eglewQueryNativeDisplayNV)
#define eglQueryNativePixmapNV EGLEW_GET_FUN(__eglewQueryNativePixmapNV)
#define eglQueryNativeWindowNV EGLEW_GET_FUN(__eglewQueryNativeWindowNV)

#define EGLEW_NV_native_query EGLEW_GET_VAR(__EGLEW_NV_native_query)

#endif /* EGL_NV_native_query */

/* ---------------------- EGL_NV_post_convert_rounding --------------------- */

#ifndef EGL_NV_post_convert_rounding
#define EGL_NV_post_convert_rounding 1

#define EGLEW_NV_post_convert_rounding EGLEW_GET_VAR(__EGLEW_NV_post_convert_rounding)

#endif /* EGL_NV_post_convert_rounding */

/* ------------------------- EGL_NV_post_sub_buffer ------------------------ */

#ifndef EGL_NV_post_sub_buffer
#define EGL_NV_post_sub_buffer 1

#define EGL_POST_SUB_BUFFER_SUPPORTED_NV 0x30BE

typedef EGLBoolean  ( * PFNEGLPOSTSUBBUFFERNVPROC) (EGLDisplay  dpy, EGLSurface  surface, EGLint  x, EGLint  y, EGLint  width, EGLint  height);

#define eglPostSubBufferNV EGLEW_GET_FUN(__eglewPostSubBufferNV)

#define EGLEW_NV_post_sub_buffer EGLEW_GET_VAR(__EGLEW_NV_post_sub_buffer)

#endif /* EGL_NV_post_sub_buffer */

/* ------------------ EGL_NV_robustness_video_memory_purge ----------------- */

#ifndef EGL_NV_robustness_video_memory_purge
#define EGL_NV_robustness_video_memory_purge 1

#define EGL_GENERATE_RESET_ON_VIDEO_MEMORY_PURGE_NV 0x334C

#define EGLEW_NV_robustness_video_memory_purge EGLEW_GET_VAR(__EGLEW_NV_robustness_video_memory_purge)

#endif /* EGL_NV_robustness_video_memory_purge */

/* ------------------ EGL_NV_stream_consumer_gltexture_yuv ----------------- */

#ifndef EGL_NV_stream_consumer_gltexture_yuv
#define EGL_NV_stream_consumer_gltexture_yuv 1

#define EGL_YUV_BUFFER_EXT 0x3300
#define EGL_YUV_NUMBER_OF_PLANES_EXT 0x3311
#define EGL_YUV_PLANE0_TEXTURE_UNIT_NV 0x332C
#define EGL_YUV_PLANE1_TEXTURE_UNIT_NV 0x332D
#define EGL_YUV_PLANE2_TEXTURE_UNIT_NV 0x332E

typedef EGLBoolean  ( * PFNEGLSTREAMCONSUMERGLTEXTUREEXTERNALATTRIBSNVPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLAttrib  *attrib_list);

#define eglStreamConsumerGLTextureExternalAttribsNV EGLEW_GET_FUN(__eglewStreamConsumerGLTextureExternalAttribsNV)

#define EGLEW_NV_stream_consumer_gltexture_yuv EGLEW_GET_VAR(__EGLEW_NV_stream_consumer_gltexture_yuv)

#endif /* EGL_NV_stream_consumer_gltexture_yuv */

/* ------------------------- EGL_NV_stream_metadata ------------------------ */

#ifndef EGL_NV_stream_metadata
#define EGL_NV_stream_metadata 1

#define EGL_MAX_STREAM_METADATA_BLOCKS_NV 0x3250
#define EGL_MAX_STREAM_METADATA_BLOCK_SIZE_NV 0x3251
#define EGL_MAX_STREAM_METADATA_TOTAL_SIZE_NV 0x3252
#define EGL_PRODUCER_METADATA_NV 0x3253
#define EGL_CONSUMER_METADATA_NV 0x3254
#define EGL_METADATA0_SIZE_NV 0x3255
#define EGL_METADATA1_SIZE_NV 0x3256
#define EGL_METADATA2_SIZE_NV 0x3257
#define EGL_METADATA3_SIZE_NV 0x3258
#define EGL_METADATA0_TYPE_NV 0x3259
#define EGL_METADATA1_TYPE_NV 0x325A
#define EGL_METADATA2_TYPE_NV 0x325B
#define EGL_METADATA3_TYPE_NV 0x325C
#define EGL_PENDING_METADATA_NV 0x3328

typedef EGLBoolean  ( * PFNEGLQUERYDISPLAYATTRIBNVPROC) (EGLDisplay  dpy, EGLint  attribute, EGLAttrib * value);
typedef EGLBoolean  ( * PFNEGLQUERYSTREAMMETADATANVPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLenum  name, EGLint  n, EGLint  offset, EGLint  size, void * data);
typedef EGLBoolean  ( * PFNEGLSETSTREAMMETADATANVPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLint  n, EGLint  offset, EGLint  size, const void * data);

#define eglQueryDisplayAttribNV EGLEW_GET_FUN(__eglewQueryDisplayAttribNV)
#define eglQueryStreamMetadataNV EGLEW_GET_FUN(__eglewQueryStreamMetadataNV)
#define eglSetStreamMetadataNV EGLEW_GET_FUN(__eglewSetStreamMetadataNV)

#define EGLEW_NV_stream_metadata EGLEW_GET_VAR(__EGLEW_NV_stream_metadata)

#endif /* EGL_NV_stream_metadata */

/* --------------------------- EGL_NV_stream_sync -------------------------- */

#ifndef EGL_NV_stream_sync
#define EGL_NV_stream_sync 1

#define EGL_SYNC_TYPE_KHR 0x30F7
#define EGL_SYNC_NEW_FRAME_NV 0x321F

typedef EGLSyncKHR  ( * PFNEGLCREATESTREAMSYNCNVPROC) (EGLDisplay  dpy, EGLStreamKHR  stream, EGLenum  type, const EGLint * attrib_list);

#define eglCreateStreamSyncNV EGLEW_GET_FUN(__eglewCreateStreamSyncNV)

#define EGLEW_NV_stream_sync EGLEW_GET_VAR(__EGLEW_NV_stream_sync)

#endif /* EGL_NV_stream_sync */

/* ------------------------------ EGL_NV_sync ------------------------------ */

#ifndef EGL_NV_sync
#define EGL_NV_sync 1

#define EGL_SYNC_FLUSH_COMMANDS_BIT_NV 0x0001
#define EGL_SYNC_PRIOR_COMMANDS_COMPLETE_NV 0x30E6
#define EGL_SYNC_STATUS_NV 0x30E7
#define EGL_SIGNALED_NV 0x30E8
#define EGL_UNSIGNALED_NV 0x30E9
#define EGL_ALREADY_SIGNALED_NV 0x30EA
#define EGL_TIMEOUT_EXPIRED_NV 0x30EB
#define EGL_CONDITION_SATISFIED_NV 0x30EC
#define EGL_SYNC_TYPE_NV 0x30ED
#define EGL_SYNC_CONDITION_NV 0x30EE
#define EGL_SYNC_FENCE_NV 0x30EF
#define EGL_FOREVER_NV 0xFFFFFFFFFFFFFFFF

typedef EGLint  ( * PFNEGLCLIENTWAITSYNCNVPROC) (EGLSyncNV  sync, EGLint  flags, EGLTimeNV  timeout);
typedef EGLSyncNV  ( * PFNEGLCREATEFENCESYNCNVPROC) (EGLDisplay  dpy, EGLenum  condition, const EGLint * attrib_list);
typedef EGLBoolean  ( * PFNEGLDESTROYSYNCNVPROC) (EGLSyncNV  sync);
typedef EGLBoolean  ( * PFNEGLFENCENVPROC) (EGLSyncNV  sync);
typedef EGLBoolean  ( * PFNEGLGETSYNCATTRIBNVPROC) (EGLSyncNV  sync, EGLint  attribute, EGLint * value);
typedef EGLBoolean  ( * PFNEGLSIGNALSYNCNVPROC) (EGLSyncNV  sync, EGLenum  mode);

#define eglClientWaitSyncNV EGLEW_GET_FUN(__eglewClientWaitSyncNV)
#define eglCreateFenceSyncNV EGLEW_GET_FUN(__eglewCreateFenceSyncNV)
#define eglDestroySyncNV EGLEW_GET_FUN(__eglewDestroySyncNV)
#define eglFenceNV EGLEW_GET_FUN(__eglewFenceNV)
#define eglGetSyncAttribNV EGLEW_GET_FUN(__eglewGetSyncAttribNV)
#define eglSignalSyncNV EGLEW_GET_FUN(__eglewSignalSyncNV)

#define EGLEW_NV_sync EGLEW_GET_VAR(__EGLEW_NV_sync)

#endif /* EGL_NV_sync */

/* --------------------------- EGL_NV_system_time -------------------------- */

#ifndef EGL_NV_system_time
#define EGL_NV_system_time 1

typedef EGLuint64NV  ( * PFNEGLGETSYSTEMTIMEFREQUENCYNVPROC) ( void );
typedef EGLuint64NV  ( * PFNEGLGETSYSTEMTIMENVPROC) ( void );

#define eglGetSystemTimeFrequencyNV EGLEW_GET_FUN(__eglewGetSystemTimeFrequencyNV)
#define eglGetSystemTimeNV EGLEW_GET_FUN(__eglewGetSystemTimeNV)

#define EGLEW_NV_system_time EGLEW_GET_VAR(__EGLEW_NV_system_time)

#endif /* EGL_NV_system_time */

/* --------------------- EGL_TIZEN_image_native_buffer --------------------- */

#ifndef EGL_TIZEN_image_native_buffer
#define EGL_TIZEN_image_native_buffer 1

#define EGL_NATIVE_BUFFER_TIZEN 0x32A0

#define EGLEW_TIZEN_image_native_buffer EGLEW_GET_VAR(__EGLEW_TIZEN_image_native_buffer)

#endif /* EGL_TIZEN_image_native_buffer */

/* --------------------- EGL_TIZEN_image_native_surface -------------------- */

#ifndef EGL_TIZEN_image_native_surface
#define EGL_TIZEN_image_native_surface 1

#define EGL_NATIVE_SURFACE_TIZEN 0x32A1

#define EGLEW_TIZEN_image_native_surface EGLEW_GET_VAR(__EGLEW_TIZEN_image_native_surface)

#endif /* EGL_TIZEN_image_native_surface */

/* ------------------------------------------------------------------------- */

#define EGLEW_FUN_EXPORT GLEW_FUN_EXPORT
#define EGLEW_VAR_EXPORT GLEW_VAR_EXPORT

EGLEW_FUN_EXPORT PFNEGLCHOOSECONFIGPROC __eglewChooseConfig;
EGLEW_FUN_EXPORT PFNEGLCOPYBUFFERSPROC __eglewCopyBuffers;
EGLEW_FUN_EXPORT PFNEGLCREATECONTEXTPROC __eglewCreateContext;
EGLEW_FUN_EXPORT PFNEGLCREATEPBUFFERSURFACEPROC __eglewCreatePbufferSurface;
EGLEW_FUN_EXPORT PFNEGLCREATEPIXMAPSURFACEPROC __eglewCreatePixmapSurface;
EGLEW_FUN_EXPORT PFNEGLCREATEWINDOWSURFACEPROC __eglewCreateWindowSurface;
EGLEW_FUN_EXPORT PFNEGLDESTROYCONTEXTPROC __eglewDestroyContext;
EGLEW_FUN_EXPORT PFNEGLDESTROYSURFACEPROC __eglewDestroySurface;
EGLEW_FUN_EXPORT PFNEGLGETCONFIGATTRIBPROC __eglewGetConfigAttrib;
EGLEW_FUN_EXPORT PFNEGLGETCONFIGSPROC __eglewGetConfigs;
EGLEW_FUN_EXPORT PFNEGLGETCURRENTDISPLAYPROC __eglewGetCurrentDisplay;
EGLEW_FUN_EXPORT PFNEGLGETCURRENTSURFACEPROC __eglewGetCurrentSurface;
EGLEW_FUN_EXPORT PFNEGLGETDISPLAYPROC __eglewGetDisplay;
EGLEW_FUN_EXPORT PFNEGLGETERRORPROC __eglewGetError;
EGLEW_FUN_EXPORT PFNEGLINITIALIZEPROC __eglewInitialize;
EGLEW_FUN_EXPORT PFNEGLMAKECURRENTPROC __eglewMakeCurrent;
EGLEW_FUN_EXPORT PFNEGLQUERYCONTEXTPROC __eglewQueryContext;
EGLEW_FUN_EXPORT PFNEGLQUERYSTRINGPROC __eglewQueryString;
EGLEW_FUN_EXPORT PFNEGLQUERYSURFACEPROC __eglewQuerySurface;
EGLEW_FUN_EXPORT PFNEGLSWAPBUFFERSPROC __eglewSwapBuffers;
EGLEW_FUN_EXPORT PFNEGLTERMINATEPROC __eglewTerminate;
EGLEW_FUN_EXPORT PFNEGLWAITGLPROC __eglewWaitGL;
EGLEW_FUN_EXPORT PFNEGLWAITNATIVEPROC __eglewWaitNative;

EGLEW_FUN_EXPORT PFNEGLBINDTEXIMAGEPROC __eglewBindTexImage;
EGLEW_FUN_EXPORT PFNEGLRELEASETEXIMAGEPROC __eglewReleaseTexImage;
EGLEW_FUN_EXPORT PFNEGLSURFACEATTRIBPROC __eglewSurfaceAttrib;
EGLEW_FUN_EXPORT PFNEGLSWAPINTERVALPROC __eglewSwapInterval;

EGLEW_FUN_EXPORT PFNEGLBINDAPIPROC __eglewBindAPI;
EGLEW_FUN_EXPORT PFNEGLCREATEPBUFFERFROMCLIENTBUFFERPROC __eglewCreatePbufferFromClientBuffer;
EGLEW_FUN_EXPORT PFNEGLQUERYAPIPROC __eglewQueryAPI;
EGLEW_FUN_EXPORT PFNEGLRELEASETHREADPROC __eglewReleaseThread;
EGLEW_FUN_EXPORT PFNEGLWAITCLIENTPROC __eglewWaitClient;

EGLEW_FUN_EXPORT PFNEGLGETCURRENTCONTEXTPROC __eglewGetCurrentContext;

EGLEW_FUN_EXPORT PFNEGLCLIENTWAITSYNCPROC __eglewClientWaitSync;
EGLEW_FUN_EXPORT PFNEGLCREATEIMAGEPROC __eglewCreateImage;
EGLEW_FUN_EXPORT PFNEGLCREATEPLATFORMPIXMAPSURFACEPROC __eglewCreatePlatformPixmapSurface;
EGLEW_FUN_EXPORT PFNEGLCREATEPLATFORMWINDOWSURFACEPROC __eglewCreatePlatformWindowSurface;
EGLEW_FUN_EXPORT PFNEGLCREATESYNCPROC __eglewCreateSync;
EGLEW_FUN_EXPORT PFNEGLDESTROYIMAGEPROC __eglewDestroyImage;
EGLEW_FUN_EXPORT PFNEGLDESTROYSYNCPROC __eglewDestroySync;
EGLEW_FUN_EXPORT PFNEGLGETPLATFORMDISPLAYPROC __eglewGetPlatformDisplay;
EGLEW_FUN_EXPORT PFNEGLGETSYNCATTRIBPROC __eglewGetSyncAttrib;
EGLEW_FUN_EXPORT PFNEGLWAITSYNCPROC __eglewWaitSync;

EGLEW_FUN_EXPORT PFNEGLSETBLOBCACHEFUNCSANDROIDPROC __eglewSetBlobCacheFuncsANDROID;

EGLEW_FUN_EXPORT PFNEGLCREATENATIVECLIENTBUFFERANDROIDPROC __eglewCreateNativeClientBufferANDROID;

EGLEW_FUN_EXPORT PFNEGLDUPNATIVEFENCEFDANDROIDPROC __eglewDupNativeFenceFDANDROID;

EGLEW_FUN_EXPORT PFNEGLPRESENTATIONTIMEANDROIDPROC __eglewPresentationTimeANDROID;

EGLEW_FUN_EXPORT PFNEGLQUERYSURFACEPOINTERANGLEPROC __eglewQuerySurfacePointerANGLE;

EGLEW_FUN_EXPORT PFNEGLQUERYDEVICESEXTPROC __eglewQueryDevicesEXT;

EGLEW_FUN_EXPORT PFNEGLQUERYDEVICEATTRIBEXTPROC __eglewQueryDeviceAttribEXT;
EGLEW_FUN_EXPORT PFNEGLQUERYDEVICESTRINGEXTPROC __eglewQueryDeviceStringEXT;
EGLEW_FUN_EXPORT PFNEGLQUERYDISPLAYATTRIBEXTPROC __eglewQueryDisplayAttribEXT;

EGLEW_FUN_EXPORT PFNEGLGETOUTPUTLAYERSEXTPROC __eglewGetOutputLayersEXT;
EGLEW_FUN_EXPORT PFNEGLGETOUTPUTPORTSEXTPROC __eglewGetOutputPortsEXT;
EGLEW_FUN_EXPORT PFNEGLOUTPUTLAYERATTRIBEXTPROC __eglewOutputLayerAttribEXT;
EGLEW_FUN_EXPORT PFNEGLOUTPUTPORTATTRIBEXTPROC __eglewOutputPortAttribEXT;
EGLEW_FUN_EXPORT PFNEGLQUERYOUTPUTLAYERATTRIBEXTPROC __eglewQueryOutputLayerAttribEXT;
EGLEW_FUN_EXPORT PFNEGLQUERYOUTPUTLAYERSTRINGEXTPROC __eglewQueryOutputLayerStringEXT;
EGLEW_FUN_EXPORT PFNEGLQUERYOUTPUTPORTATTRIBEXTPROC __eglewQueryOutputPortAttribEXT;
EGLEW_FUN_EXPORT PFNEGLQUERYOUTPUTPORTSTRINGEXTPROC __eglewQueryOutputPortStringEXT;

EGLEW_FUN_EXPORT PFNEGLCREATEPLATFORMPIXMAPSURFACEEXTPROC __eglewCreatePlatformPixmapSurfaceEXT;
EGLEW_FUN_EXPORT PFNEGLCREATEPLATFORMWINDOWSURFACEEXTPROC __eglewCreatePlatformWindowSurfaceEXT;
EGLEW_FUN_EXPORT PFNEGLGETPLATFORMDISPLAYEXTPROC __eglewGetPlatformDisplayEXT;

EGLEW_FUN_EXPORT PFNEGLSTREAMCONSUMEROUTPUTEXTPROC __eglewStreamConsumerOutputEXT;

EGLEW_FUN_EXPORT PFNEGLSWAPBUFFERSWITHDAMAGEEXTPROC __eglewSwapBuffersWithDamageEXT;

EGLEW_FUN_EXPORT PFNEGLCREATEPIXMAPSURFACEHIPROC __eglewCreatePixmapSurfaceHI;

EGLEW_FUN_EXPORT PFNEGLCREATESYNC64KHRPROC __eglewCreateSync64KHR;

EGLEW_FUN_EXPORT PFNEGLDEBUGMESSAGECONTROLKHRPROC __eglewDebugMessageControlKHR;
EGLEW_FUN_EXPORT PFNEGLLABELOBJECTKHRPROC __eglewLabelObjectKHR;
EGLEW_FUN_EXPORT PFNEGLQUERYDEBUGKHRPROC __eglewQueryDebugKHR;

EGLEW_FUN_EXPORT PFNEGLCREATEIMAGEKHRPROC __eglewCreateImageKHR;
EGLEW_FUN_EXPORT PFNEGLDESTROYIMAGEKHRPROC __eglewDestroyImageKHR;

EGLEW_FUN_EXPORT PFNEGLLOCKSURFACEKHRPROC __eglewLockSurfaceKHR;
EGLEW_FUN_EXPORT PFNEGLUNLOCKSURFACEKHRPROC __eglewUnlockSurfaceKHR;

EGLEW_FUN_EXPORT PFNEGLQUERYSURFACE64KHRPROC __eglewQuerySurface64KHR;

EGLEW_FUN_EXPORT PFNEGLSETDAMAGEREGIONKHRPROC __eglewSetDamageRegionKHR;

EGLEW_FUN_EXPORT PFNEGLCLIENTWAITSYNCKHRPROC __eglewClientWaitSyncKHR;
EGLEW_FUN_EXPORT PFNEGLCREATESYNCKHRPROC __eglewCreateSyncKHR;
EGLEW_FUN_EXPORT PFNEGLDESTROYSYNCKHRPROC __eglewDestroySyncKHR;
EGLEW_FUN_EXPORT PFNEGLGETSYNCATTRIBKHRPROC __eglewGetSyncAttribKHR;
EGLEW_FUN_EXPORT PFNEGLSIGNALSYNCKHRPROC __eglewSignalSyncKHR;

EGLEW_FUN_EXPORT PFNEGLCREATESTREAMKHRPROC __eglewCreateStreamKHR;
EGLEW_FUN_EXPORT PFNEGLDESTROYSTREAMKHRPROC __eglewDestroyStreamKHR;
EGLEW_FUN_EXPORT PFNEGLQUERYSTREAMKHRPROC __eglewQueryStreamKHR;
EGLEW_FUN_EXPORT PFNEGLQUERYSTREAMU64KHRPROC __eglewQueryStreamu64KHR;
EGLEW_FUN_EXPORT PFNEGLSTREAMATTRIBKHRPROC __eglewStreamAttribKHR;

EGLEW_FUN_EXPORT PFNEGLSTREAMCONSUMERACQUIREKHRPROC __eglewStreamConsumerAcquireKHR;
EGLEW_FUN_EXPORT PFNEGLSTREAMCONSUMERGLTEXTUREEXTERNALKHRPROC __eglewStreamConsumerGLTextureExternalKHR;
EGLEW_FUN_EXPORT PFNEGLSTREAMCONSUMERRELEASEKHRPROC __eglewStreamConsumerReleaseKHR;

EGLEW_FUN_EXPORT PFNEGLCREATESTREAMFROMFILEDESCRIPTORKHRPROC __eglewCreateStreamFromFileDescriptorKHR;
EGLEW_FUN_EXPORT PFNEGLGETSTREAMFILEDESCRIPTORKHRPROC __eglewGetStreamFileDescriptorKHR;

EGLEW_FUN_EXPORT PFNEGLQUERYSTREAMTIMEKHRPROC __eglewQueryStreamTimeKHR;

EGLEW_FUN_EXPORT PFNEGLCREATESTREAMPRODUCERSURFACEKHRPROC __eglewCreateStreamProducerSurfaceKHR;

EGLEW_FUN_EXPORT PFNEGLSWAPBUFFERSWITHDAMAGEKHRPROC __eglewSwapBuffersWithDamageKHR;

EGLEW_FUN_EXPORT PFNEGLWAITSYNCKHRPROC __eglewWaitSyncKHR;

EGLEW_FUN_EXPORT PFNEGLCREATEDRMIMAGEMESAPROC __eglewCreateDRMImageMESA;
EGLEW_FUN_EXPORT PFNEGLEXPORTDRMIMAGEMESAPROC __eglewExportDRMImageMESA;

EGLEW_FUN_EXPORT PFNEGLEXPORTDMABUFIMAGEMESAPROC __eglewExportDMABUFImageMESA;
EGLEW_FUN_EXPORT PFNEGLEXPORTDMABUFIMAGEQUERYMESAPROC __eglewExportDMABUFImageQueryMESA;

EGLEW_FUN_EXPORT PFNEGLSWAPBUFFERSREGIONNOKPROC __eglewSwapBuffersRegionNOK;

EGLEW_FUN_EXPORT PFNEGLSWAPBUFFERSREGION2NOKPROC __eglewSwapBuffersRegion2NOK;

EGLEW_FUN_EXPORT PFNEGLQUERYNATIVEDISPLAYNVPROC __eglewQueryNativeDisplayNV;
EGLEW_FUN_EXPORT PFNEGLQUERYNATIVEPIXMAPNVPROC __eglewQueryNativePixmapNV;
EGLEW_FUN_EXPORT PFNEGLQUERYNATIVEWINDOWNVPROC __eglewQueryNativeWindowNV;

EGLEW_FUN_EXPORT PFNEGLPOSTSUBBUFFERNVPROC __eglewPostSubBufferNV;

EGLEW_FUN_EXPORT PFNEGLSTREAMCONSUMERGLTEXTUREEXTERNALATTRIBSNVPROC __eglewStreamConsumerGLTextureExternalAttribsNV;

EGLEW_FUN_EXPORT PFNEGLQUERYDISPLAYATTRIBNVPROC __eglewQueryDisplayAttribNV;
EGLEW_FUN_EXPORT PFNEGLQUERYSTREAMMETADATANVPROC __eglewQueryStreamMetadataNV;
EGLEW_FUN_EXPORT PFNEGLSETSTREAMMETADATANVPROC __eglewSetStreamMetadataNV;

EGLEW_FUN_EXPORT PFNEGLCREATESTREAMSYNCNVPROC __eglewCreateStreamSyncNV;

EGLEW_FUN_EXPORT PFNEGLCLIENTWAITSYNCNVPROC __eglewClientWaitSyncNV;
EGLEW_FUN_EXPORT PFNEGLCREATEFENCESYNCNVPROC __eglewCreateFenceSyncNV;
EGLEW_FUN_EXPORT PFNEGLDESTROYSYNCNVPROC __eglewDestroySyncNV;
EGLEW_FUN_EXPORT PFNEGLFENCENVPROC __eglewFenceNV;
EGLEW_FUN_EXPORT PFNEGLGETSYNCATTRIBNVPROC __eglewGetSyncAttribNV;
EGLEW_FUN_EXPORT PFNEGLSIGNALSYNCNVPROC __eglewSignalSyncNV;

EGLEW_FUN_EXPORT PFNEGLGETSYSTEMTIMEFREQUENCYNVPROC __eglewGetSystemTimeFrequencyNV;
EGLEW_FUN_EXPORT PFNEGLGETSYSTEMTIMENVPROC __eglewGetSystemTimeNV;
EGLEW_VAR_EXPORT GLboolean __EGLEW_VERSION_1_0;
EGLEW_VAR_EXPORT GLboolean __EGLEW_VERSION_1_1;
EGLEW_VAR_EXPORT GLboolean __EGLEW_VERSION_1_2;
EGLEW_VAR_EXPORT GLboolean __EGLEW_VERSION_1_3;
EGLEW_VAR_EXPORT GLboolean __EGLEW_VERSION_1_4;
EGLEW_VAR_EXPORT GLboolean __EGLEW_VERSION_1_5;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_blob_cache;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_create_native_client_buffer;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_framebuffer_target;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_front_buffer_auto_refresh;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_image_native_buffer;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_native_fence_sync;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_presentation_time;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANDROID_recordable;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANGLE_d3d_share_handle_client_buffer;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANGLE_device_d3d;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANGLE_query_surface_pointer;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANGLE_surface_d3d_texture_2d_share_handle;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ANGLE_window_fixed_size;
EGLEW_VAR_EXPORT GLboolean __EGLEW_ARM_pixmap_multisample_discard;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_buffer_age;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_client_extensions;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_create_context_robustness;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_device_base;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_device_drm;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_device_enumeration;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_device_openwf;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_device_query;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_image_dma_buf_import;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_multiview_window;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_output_base;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_output_drm;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_output_openwf;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_platform_base;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_platform_device;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_platform_wayland;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_platform_x11;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_protected_content;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_protected_surface;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_stream_consumer_egloutput;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_swap_buffers_with_damage;
EGLEW_VAR_EXPORT GLboolean __EGLEW_EXT_yuv_surface;
EGLEW_VAR_EXPORT GLboolean __EGLEW_HI_clientpixmap;
EGLEW_VAR_EXPORT GLboolean __EGLEW_HI_colorformats;
EGLEW_VAR_EXPORT GLboolean __EGLEW_IMG_context_priority;
EGLEW_VAR_EXPORT GLboolean __EGLEW_IMG_image_plane_attribs;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_cl_event;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_cl_event2;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_client_get_all_proc_addresses;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_config_attribs;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_create_context;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_create_context_no_error;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_debug;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_fence_sync;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_get_all_proc_addresses;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_gl_colorspace;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_gl_renderbuffer_image;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_gl_texture_2D_image;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_gl_texture_3D_image;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_gl_texture_cubemap_image;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_image;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_image_base;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_image_pixmap;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_lock_surface;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_lock_surface2;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_lock_surface3;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_mutable_render_buffer;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_partial_update;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_platform_android;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_platform_gbm;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_platform_wayland;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_platform_x11;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_reusable_sync;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_stream;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_stream_consumer_gltexture;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_stream_cross_process_fd;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_stream_fifo;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_stream_producer_aldatalocator;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_stream_producer_eglsurface;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_surfaceless_context;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_swap_buffers_with_damage;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_vg_parent_image;
EGLEW_VAR_EXPORT GLboolean __EGLEW_KHR_wait_sync;
EGLEW_VAR_EXPORT GLboolean __EGLEW_MESA_drm_image;
EGLEW_VAR_EXPORT GLboolean __EGLEW_MESA_image_dma_buf_export;
EGLEW_VAR_EXPORT GLboolean __EGLEW_MESA_platform_gbm;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NOK_swap_region;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NOK_swap_region2;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NOK_texture_from_pixmap;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_3dvision_surface;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_coverage_sample;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_coverage_sample_resolve;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_cuda_event;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_depth_nonlinear;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_device_cuda;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_native_query;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_post_convert_rounding;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_post_sub_buffer;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_robustness_video_memory_purge;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_stream_consumer_gltexture_yuv;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_stream_metadata;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_stream_sync;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_sync;
EGLEW_VAR_EXPORT GLboolean __EGLEW_NV_system_time;
EGLEW_VAR_EXPORT GLboolean __EGLEW_TIZEN_image_native_buffer;
EGLEW_VAR_EXPORT GLboolean __EGLEW_TIZEN_image_native_surface;
/* ------------------------------------------------------------------------ */

GLEWAPI GLenum GLEWAPIENTRY eglewInit (EGLDisplay display);
GLEWAPI GLboolean GLEWAPIENTRY eglewIsSupported (const char *name);

#define EGLEW_GET_VAR(x) (*(const GLboolean*)&x)
#define EGLEW_GET_FUN(x) x

GLEWAPI GLboolean GLEWAPIENTRY eglewGetExtension (const char *name);

#ifdef __cplusplus
}
#endif

#endif /* __eglew_h__ */
