#ifndef __glxext_h_
#define __glxext_h_

#ifdef __cplusplus
extern "C" {
#endif

/*
** License Applicability. Except to the extent portions of this file are
** made subject to an alternative license as permitted in the SGI Free
** Software License B, Version 1.1 (the "License"), the contents of this
** file are subject only to the provisions of the License. You may not use
** this file except in compliance with the License. You may obtain a copy
** of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
** Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
** 
** http://oss.sgi.com/projects/FreeB
** 
** Note that, as provided in the License, the Software is distributed on an
** "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
** DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
** CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
** PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
** 
** Original Code. The Original Code is: OpenGL Sample Implementation,
** Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
** Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
** Copyright in any portions created by third parties is as indicated
** elsewhere herein. All Rights Reserved.
** 
** Additional Notice Provisions: This software was created using the
** OpenGL(R) version 1.2.1 Sample Implementation published by SGI, but has
** not been independently verified as being compliant with the OpenGL(R)
** version 1.2.1 Specification.
*/

#if defined(_WIN32) && !defined(APIENTRY) && !defined(__CYGWIN__)
#define WIN32_LEAN_AND_MEAN 1
#include <windows.h>
#else
#include <GL/glx.h>
#endif

#ifndef APIENTRY
#define APIENTRY
#endif


/*************************************************************/

/* Header file version number, required by OpenGL ABI for Linux */
#define GLX_GLXEXT_VERSION 2

#ifndef GLX_VERSION_1_3
#define GLX_WINDOW_BIT                     0x00000001
#define GLX_PIXMAP_BIT                     0x00000002
#define GLX_PBUFFER_BIT                    0x00000004
#define GLX_RGBA_BIT                       0x00000001
#define GLX_COLOR_INDEX_BIT                0x00000002
#define GLX_PBUFFER_CLOBBER_MASK           0x08000000
#define GLX_FRONT_LEFT_BUFFER_BIT          0x00000001
#define GLX_FRONT_RIGHT_BUFFER_BIT         0x00000002
#define GLX_BACK_LEFT_BUFFER_BIT           0x00000004
#define GLX_BACK_RIGHT_BUFFER_BIT          0x00000008
#define GLX_AUX_BUFFERS_BIT                0x00000010
#define GLX_DEPTH_BUFFER_BIT               0x00000020
#define GLX_STENCIL_BUFFER_BIT             0x00000040
#define GLX_ACCUM_BUFFER_BIT               0x00000080
#define GLX_CONFIG_CAVEAT                  0x20
#define GLX_X_VISUAL_TYPE                  0x22
#define GLX_TRANSPARENT_TYPE               0x23
#define GLX_TRANSPARENT_INDEX_VALUE        0x24
#define GLX_TRANSPARENT_RED_VALUE          0x25
#define GLX_TRANSPARENT_GREEN_VALUE        0x26
#define GLX_TRANSPARENT_BLUE_VALUE         0x27
#define GLX_TRANSPARENT_ALPHA_VALUE        0x28
#define GLX_DONT_CARE                      0xFFFFFFFF
#define GLX_NONE                           0x8000
#define GLX_SLOW_CONFIG                    0x8001
#define GLX_TRUE_COLOR                     0x8002
#define GLX_DIRECT_COLOR                   0x8003
#define GLX_PSEUDO_COLOR                   0x8004
#define GLX_STATIC_COLOR                   0x8005
#define GLX_GRAY_SCALE                     0x8006
#define GLX_STATIC_GRAY                    0x8007
#define GLX_TRANSPARENT_RGB                0x8008
#define GLX_TRANSPARENT_INDEX              0x8009
#define GLX_VISUAL_ID                      0x800B
#define GLX_SCREEN                         0x800C
#define GLX_NON_CONFORMANT_CONFIG          0x800D
#define GLX_DRAWABLE_TYPE                  0x8010
#define GLX_RENDER_TYPE                    0x8011
#define GLX_X_RENDERABLE                   0x8012
#define GLX_FBCONFIG_ID                    0x8013
#define GLX_RGBA_TYPE                      0x8014
#define GLX_COLOR_INDEX_TYPE               0x8015
#define GLX_MAX_PBUFFER_WIDTH              0x8016
#define GLX_MAX_PBUFFER_HEIGHT             0x8017
#define GLX_MAX_PBUFFER_PIXELS             0x8018
#define GLX_PRESERVED_CONTENTS             0x801B
#define GLX_LARGEST_PBUFFER                0x801C
#define GLX_WIDTH                          0x801D
#define GLX_HEIGHT                         0x801E
#define GLX_EVENT_MASK                     0x801F
#define GLX_DAMAGED                        0x8020
#define GLX_SAVED                          0x8021
#define GLX_WINDOW                         0x8022
#define GLX_PBUFFER                        0x8023
#define GLX_PBUFFER_HEIGHT                 0x8040
#define GLX_PBUFFER_WIDTH                  0x8041
#endif

#ifndef GLX_EXT_visual_info
#define GLX_X_VISUAL_TYPE_EXT              0x22
#define GLX_TRANSPARENT_TYPE_EXT           0x23
#define GLX_TRANSPARENT_INDEX_VALUE_EXT    0x24
#define GLX_TRANSPARENT_RED_VALUE_EXT      0x25
#define GLX_TRANSPARENT_GREEN_VALUE_EXT    0x26
#define GLX_TRANSPARENT_BLUE_VALUE_EXT     0x27
#define GLX_TRANSPARENT_ALPHA_VALUE_EXT    0x28
#define GLX_NONE_EXT                       0x8000
#define GLX_TRUE_COLOR_EXT                 0x8002
#define GLX_DIRECT_COLOR_EXT               0x8003
#define GLX_PSEUDO_COLOR_EXT               0x8004
#define GLX_STATIC_COLOR_EXT               0x8005
#define GLX_GRAY_SCALE_EXT                 0x8006
#define GLX_STATIC_GRAY_EXT                0x8007
#define GLX_TRANSPARENT_RGB_EXT            0x8008
#define GLX_TRANSPARENT_INDEX_EXT          0x8009
#endif

#ifndef GLX_SGI_swap_control
#endif

#ifndef GLX_SGI_video_sync
#endif

#ifndef GLX_SGI_make_current_read
#endif

#ifndef GLX_SGIX_video_source
#endif

#ifndef GLX_EXT_visual_rating
#define GLX_VISUAL_CAVEAT_EXT              0x20
#define GLX_SLOW_VISUAL_EXT                0x8001
#define GLX_NON_CONFORMANT_VISUAL_EXT      0x800D
/* reuse GLX_NONE_EXT */
#endif

#ifndef GLX_EXT_import_context
#define GLX_SHARE_CONTEXT_EXT              0x800A
#define GLX_VISUAL_ID_EXT                  0x800B
#define GLX_SCREEN_EXT                     0x800C
#endif

#ifndef GLX_SGIX_fbconfig
#define GLX_WINDOW_BIT_SGIX                0x00000001
#define GLX_PIXMAP_BIT_SGIX                0x00000002
#define GLX_RGBA_BIT_SGIX                  0x00000001
#define GLX_COLOR_INDEX_BIT_SGIX           0x00000002
#define GLX_DRAWABLE_TYPE_SGIX             0x8010
#define GLX_RENDER_TYPE_SGIX               0x8011
#define GLX_X_RENDERABLE_SGIX              0x8012
#define GLX_FBCONFIG_ID_SGIX               0x8013
#define GLX_RGBA_TYPE_SGIX                 0x8014
#define GLX_COLOR_INDEX_TYPE_SGIX          0x8015
/* reuse GLX_SCREEN_EXT */
#endif

#ifndef GLX_SGIX_pbuffer
#define GLX_PBUFFER_BIT_SGIX               0x00000004
#define GLX_BUFFER_CLOBBER_MASK_SGIX       0x08000000
#define GLX_FRONT_LEFT_BUFFER_BIT_SGIX     0x00000001
#define GLX_FRONT_RIGHT_BUFFER_BIT_SGIX    0x00000002
#define GLX_BACK_LEFT_BUFFER_BIT_SGIX      0x00000004
#define GLX_BACK_RIGHT_BUFFER_BIT_SGIX     0x00000008
#define GLX_AUX_BUFFERS_BIT_SGIX           0x00000010
#define GLX_DEPTH_BUFFER_BIT_SGIX          0x00000020
#define GLX_STENCIL_BUFFER_BIT_SGIX        0x00000040
#define GLX_ACCUM_BUFFER_BIT_SGIX          0x00000080
#define GLX_SAMPLE_BUFFERS_BIT_SGIX        0x00000100
#define GLX_MAX_PBUFFER_WIDTH_SGIX         0x8016
#define GLX_MAX_PBUFFER_HEIGHT_SGIX        0x8017
#define GLX_MAX_PBUFFER_PIXELS_SGIX        0x8018
#define GLX_OPTIMAL_PBUFFER_WIDTH_SGIX     0x8019
#define GLX_OPTIMAL_PBUFFER_HEIGHT_SGIX    0x801A
#define GLX_PRESERVED_CONTENTS_SGIX        0x801B
#define GLX_LARGEST_PBUFFER_SGIX           0x801C
#define GLX_WIDTH_SGIX                     0x801D
#define GLX_HEIGHT_SGIX                    0x801E
#define GLX_EVENT_MASK_SGIX                0x801F
#define GLX_DAMAGED_SGIX                   0x8020
#define GLX_SAVED_SGIX                     0x8021
#define GLX_WINDOW_SGIX                    0x8022
#define GLX_PBUFFER_SGIX                   0x8023
#endif

#ifndef GLX_SGI_cushion
#endif

#ifndef GLX_SGIX_video_resize
#define GLX_SYNC_FRAME_SGIX                0x00000000
#define GLX_SYNC_SWAP_SGIX                 0x00000001
#endif

#ifndef GLX_SGIX_dmbuffer
#define GLX_DIGITAL_MEDIA_PBUFFER_SGIX     0x8024
#endif

#ifndef GLX_SGIX_swap_group
#endif

#ifndef GLX_SGIX_swap_barrier
#endif

#ifndef GLX_SGIS_blended_overlay
#define GLX_BLENDED_RGBA_SGIS              0x8025
#endif

#ifndef GLX_SGIS_shared_multisample
#define GLX_MULTISAMPLE_SUB_RECT_WIDTH_SGIS 0x8026
#define GLX_MULTISAMPLE_SUB_RECT_HEIGHT_SGIS 0x8027
#endif

#ifndef GLX_SUN_get_transparent_index
#endif

#ifndef GLX_3DFX_multisample
#define GLX_SAMPLE_BUFFERS_3DFX            0x8050
#define GLX_SAMPLES_3DFX                   0x8051
#endif

#ifndef GLX_MESA_copy_sub_buffer
#endif

#ifndef GLX_MESA_pixmap_colormap
#endif

#ifndef GLX_MESA_release_buffers
#endif

#ifndef GLX_MESA_set_3dfx_mode
#define GLX_3DFX_WINDOW_MODE_MESA          0x1
#define GLX_3DFX_FULLSCREEN_MODE_MESA      0x2
#endif


/*************************************************************/

#ifndef GLX_ARB_get_proc_address
typedef void (*__GLXextFuncPtr)();
#endif

#ifndef GLX_SGIX_video_source
typedef XID GLXVideoSourceSGIX;
#endif

#ifndef GLX_SGIX_fbconfig
typedef XID GLXFBConfigIDSGIX;
typedef struct __GLXFBConfigRec *GLXFBConfigSGIX;
#endif

#ifndef GLX_SGIX_pbuffer
typedef XID GLXPbufferSGIX;
typedef struct {
    int type;
    unsigned long serial;	  /* # of last request processed by server */
    Bool send_event;		  /* true if this came for SendEvent request */
    Display *display;		  /* display the event was read from */
    GLXDrawable drawable;	  /* i.d. of Drawable */
    int event_type;		  /* GLX_DAMAGED_SGIX or GLX_SAVED_SGIX */
    int draw_type;		  /* GLX_WINDOW_SGIX or GLX_PBUFFER_SGIX */
    unsigned int mask;	  /* mask indicating which buffers are affected*/
    int x, y;
    int width, height;
    int count;		  /* if nonzero, at least this many more */
} GLXBufferClobberEventSGIX;
#endif

#ifdef GL_NV_vertex_array_range
#ifndef PFNGLXALLOCATEMEMORYNVPROC
#ifdef GLX_GLXEXT_PROTOTYPES
extern void *glXAllocateMemoryNV (GLsizei, GLfloat, GLfloat, GLfloat);
#endif
typedef void * ( * PFNGLXALLOCATEMEMORYNVPROC) (GLsizei, GLfloat, GLfloat, GLfloat);
#endif
#ifndef PFNGLXFREEMEMORYNVPROC
#ifdef GLX_GLXEXT_PROTOTYPES
extern void glXFreeMemoryNV (void *);
#endif
typedef void ( * PFNGLXFREEMEMORYNVPROC) (void *);
#endif
#endif

#ifndef GLX_VERSION_1_3
#define GLX_VERSION_1_3 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern GLXFBConfig * glXGetFBConfigs (Display *, int, int *);
extern GLXFBConfig * glXChooseFBConfig (Display *, int, const int *, int *);
extern int glXGetFBConfigAttrib (Display *, GLXFBConfig, int, int *);
extern XVisualInfo * glXGetVisualFromFBConfig (Display *, GLXFBConfig);
extern GLXWindow glXCreateWindow (Display *, GLXFBConfig, Window, const int *);
extern void glXDestroyWindow (Display *, GLXWindow);
extern GLXPixmap glXCreatePixmap (Display *, GLXFBConfig, Pixmap, const int *);
extern void glXDestroyPixmap (Display *, GLXPixmap);
extern GLXPbuffer glXCreatePbuffer (Display *, GLXFBConfig, const int *);
extern void glXDestroyPbuffer (Display *, GLXPbuffer);
extern void glXQueryDrawable (Display *, GLXDrawable, int, unsigned int *);
extern GLXContext glXCreateNewContext (Display *, GLXFBConfig, int, GLXContext, Bool);
extern Bool glXMakeContextCurrent (Display *, GLXDrawable, GLXDrawable, GLXContext);
extern GLXDrawable glXGetCurrentReadDrawable (void);
extern Display * glXGetCurrentDisplay (void);
extern int glXQueryContext (Display *, GLXContext, int, int *);
extern void glXSelectEvent (Display *, GLXDrawable, unsigned long);
extern void glXGetSelectedEvent (Display *, GLXDrawable, unsigned long *);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef GLXFBConfig * ( * PFNGLXGETFBCONFIGSPROC) (Display *dpy, int screen, int *nelements);
typedef GLXFBConfig * ( * PFNGLXCHOOSEFBCONFIGPROC) (Display *dpy, int screen, const int *attrib_list, int *nelements);
typedef int ( * PFNGLXGETFBCONFIGATTRIBPROC) (Display *dpy, GLXFBConfig config, int attribute, int *value);
typedef XVisualInfo * ( * PFNGLXGETVISUALFROMFBCONFIGPROC) (Display *dpy, GLXFBConfig config);
typedef GLXWindow ( * PFNGLXCREATEWINDOWPROC) (Display *dpy, GLXFBConfig config, Window win, const int *attrib_list);
typedef void ( * PFNGLXDESTROYWINDOWPROC) (Display *dpy, GLXWindow win);
typedef GLXPixmap ( * PFNGLXCREATEPIXMAPPROC) (Display *dpy, GLXFBConfig config, Pixmap pixmap, const int *attrib_list);
typedef void ( * PFNGLXDESTROYPIXMAPPROC) (Display *dpy, GLXPixmap pixmap);
typedef GLXPbuffer ( * PFNGLXCREATEPBUFFERPROC) (Display *dpy, GLXFBConfig config, const int *attrib_list);
typedef void ( * PFNGLXDESTROYPBUFFERPROC) (Display *dpy, GLXPbuffer pbuf);
typedef void ( * PFNGLXQUERYDRAWABLEPROC) (Display *dpy, GLXDrawable draw, int attribute, unsigned int *value);
typedef GLXContext ( * PFNGLXCREATENEWCONTEXTPROC) (Display *dpy, GLXFBConfig config, int render_type, GLXContext share_list, Bool direct);
typedef Bool ( * PFNGLXMAKECONTEXTCURRENTPROC) (Display *dpy, GLXDrawable draw, GLXDrawable read, GLXContext ctx);
typedef GLXDrawable ( * PFNGLXGETCURRENTREADDRAWABLEPROC) (void);
typedef Display * ( * PFNGLXGETCURRENTDISPLAYPROC) (void);
typedef int ( * PFNGLXQUERYCONTEXTPROC) (Display *dpy, GLXContext ctx, int attribute, int *value);
typedef void ( * PFNGLXSELECTEVENTPROC) (Display *dpy, GLXDrawable draw, unsigned long event_mask);
typedef void ( * PFNGLXGETSELECTEDEVENTPROC) (Display *dpy, GLXDrawable draw, unsigned long *event_mask);
#endif

#ifndef GLX_ARB_get_proc_address
#define GLX_ARB_get_proc_address 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern __GLXextFuncPtr glXGetProcAddressARB (const GLubyte *);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef __GLXextFuncPtr ( * PFNGLXGETPROCADDRESSARBPROC) (const GLubyte *procName);
#endif

#ifndef GLX_SGIS_multisample
#define GLX_SGIS_multisample 1
#endif

#ifndef GLX_EXT_visual_info
#define GLX_EXT_visual_info 1
#endif

#ifndef GLX_SGI_swap_control
#define GLX_SGI_swap_control 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern int glXSwapIntervalSGI (int);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef int ( * PFNGLXSWAPINTERVALSGIPROC) (int interval);
#endif

#ifndef GLX_SGI_video_sync
#define GLX_SGI_video_sync 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern int glXGetVideoSyncSGI (unsigned int *);
extern int glXWaitVideoSyncSGI (int, int, unsigned int *);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef int ( * PFNGLXGETVIDEOSYNCSGIPROC) (unsigned int *count);
typedef int ( * PFNGLXWAITVIDEOSYNCSGIPROC) (int divisor, int remainder, unsigned int *count);
#endif

#ifndef GLX_SGI_make_current_read
#define GLX_SGI_make_current_read 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern Bool glXMakeCurrentReadSGI (Display *, GLXDrawable, GLXDrawable, GLXContext);
extern GLXDrawable glXGetCurrentReadDrawableSGI (void);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef Bool ( * PFNGLXMAKECURRENTREADSGIPROC) (Display *dpy, GLXDrawable draw, GLXDrawable read, GLXContext ctx);
typedef GLXDrawable ( * PFNGLXGETCURRENTREADDRAWABLESGIPROC) (void);
#endif

#ifdef _VL_H
#ifndef GLX_SGIX_video_source
#define GLX_SGIX_video_source 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern GLXVideoSourceSGIX glXCreateGLXVideoSourceSGIX (Display *, int, VLServer, VLPath, int, VLNode);
extern void glXDestroyGLXVideoSourceSGIX (Display *, GLXVideoSourceSGIX);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef GLXVideoSourceSGIX ( * PFNGLXCREATEGLXVIDEOSOURCESGIXPROC) (Display *display, int screen, VLServer server, VLPath path, int nodeClass, VLNode drainNode);
typedef void ( * PFNGLXDESTROYGLXVIDEOSOURCESGIXPROC) (Display *dpy, GLXVideoSourceSGIX glxvideosource);
#endif

#endif /* _VL_H */
#ifndef GLX_EXT_visual_rating
#define GLX_EXT_visual_rating 1
#endif

#ifndef GLX_EXT_import_context
#define GLX_EXT_import_context 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern Display * glXGetCurrentDisplayEXT (void);
extern int glXQueryContextInfoEXT (Display *, GLXContext, int, int *);
extern GLXContextID glXGetContextIDEXT (GLXContext);
extern GLXContext glXImportContextEXT (Display *, GLXContextID);
extern void glXFreeContextEXT (Display *, GLXContext);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef Display * ( * PFNGLXGETCURRENTDISPLAYEXTPROC) (void);
typedef int ( * PFNGLXQUERYCONTEXTINFOEXTPROC) (Display *dpy, GLXContext context, int attribute, int *value);
typedef GLXContextID ( * PFNGLXGETCONTEXTIDEXTPROC) (GLXContext context);
typedef GLXContext ( * PFNGLXIMPORTCONTEXTEXTPROC) (Display *dpy, GLXContextID contextID);
typedef void ( * PFNGLXFREECONTEXTEXTPROC) (Display *dpy, GLXContext context);
#endif

#ifndef GLX_SGIX_fbconfig
#define GLX_SGIX_fbconfig 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern int glXGetFBConfigAttribSGIX (Display *, GLXFBConfigSGIX, int, int *);
extern GLXFBConfigSGIX * glXChooseFBConfigSGIX (Display *, int, int *, int *);
extern GLXPixmap glXCreateGLXPixmapWithConfigSGIX (Display *, GLXFBConfigSGIX, Pixmap);
extern GLXContext glXCreateContextWithConfigSGIX (Display *, GLXFBConfigSGIX, int, GLXContext, Bool);
extern XVisualInfo * glXGetVisualFromFBConfigSGIX (Display *, GLXFBConfigSGIX);
extern GLXFBConfigSGIX glXGetFBConfigFromVisualSGIX (Display *, XVisualInfo *);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef int ( * PFNGLXGETFBCONFIGATTRIBSGIXPROC) (Display *dpy, GLXFBConfigSGIX config, int attribute, int *value);
typedef GLXFBConfigSGIX * ( * PFNGLXCHOOSEFBCONFIGSGIXPROC) (Display *dpy, int screen, int *attrib_list, int *nelements);
typedef GLXPixmap ( * PFNGLXCREATEGLXPIXMAPWITHCONFIGSGIXPROC) (Display *dpy, GLXFBConfigSGIX config, Pixmap pixmap);
typedef GLXContext ( * PFNGLXCREATECONTEXTWITHCONFIGSGIXPROC) (Display *dpy, GLXFBConfigSGIX config, int render_type, GLXContext share_list, Bool direct);
typedef XVisualInfo * ( * PFNGLXGETVISUALFROMFBCONFIGSGIXPROC) (Display *dpy, GLXFBConfigSGIX config);
typedef GLXFBConfigSGIX ( * PFNGLXGETFBCONFIGFROMVISUALSGIXPROC) (Display *dpy, XVisualInfo *vis);
#endif

#ifndef GLX_SGIX_pbuffer
#define GLX_SGIX_pbuffer 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern GLXPbufferSGIX glXCreateGLXPbufferSGIX (Display *, GLXFBConfigSGIX, unsigned int, unsigned int, int *);
extern void glXDestroyGLXPbufferSGIX (Display *, GLXPbufferSGIX);
extern int glXQueryGLXPbufferSGIX (Display *, GLXPbufferSGIX, int, unsigned int *);
extern void glXSelectEventSGIX (Display *, GLXDrawable, unsigned long);
extern void glXGetSelectedEventSGIX (Display *, GLXDrawable, unsigned long *);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef GLXPbufferSGIX ( * PFNGLXCREATEGLXPBUFFERSGIXPROC) (Display *dpy, GLXFBConfigSGIX config, unsigned int width, unsigned int height, int *attrib_list);
typedef void ( * PFNGLXDESTROYGLXPBUFFERSGIXPROC) (Display *dpy, GLXPbufferSGIX pbuf);
typedef int ( * PFNGLXQUERYGLXPBUFFERSGIXPROC) (Display *dpy, GLXPbufferSGIX pbuf, int attribute, unsigned int *value);
typedef void ( * PFNGLXSELECTEVENTSGIXPROC) (Display *dpy, GLXDrawable drawable, unsigned long mask);
typedef void ( * PFNGLXGETSELECTEDEVENTSGIXPROC) (Display *dpy, GLXDrawable drawable, unsigned long *mask);
#endif

#ifndef GLX_SGI_cushion
#define GLX_SGI_cushion 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern void glXCushionSGI (Display *, Window, float);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef void ( * PFNGLXCUSHIONSGIPROC) (Display *dpy, Window window, float cushion);
#endif

#ifndef GLX_SGIX_video_resize
#define GLX_SGIX_video_resize 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern int glXBindChannelToWindowSGIX (Display *, int, int, Window);
extern int glXChannelRectSGIX (Display *, int, int, int, int, int, int);
extern int glXQueryChannelRectSGIX (Display *, int, int, int *, int *, int *, int *);
extern int glXQueryChannelDeltasSGIX (Display *, int, int, int *, int *, int *, int *);
extern int glXChannelRectSyncSGIX (Display *, int, int, GLenum);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef int ( * PFNGLXBINDCHANNELTOWINDOWSGIXPROC) (Display *display, int screen, int channel, Window window);
typedef int ( * PFNGLXCHANNELRECTSGIXPROC) (Display *display, int screen, int channel, int x, int y, int w, int h);
typedef int ( * PFNGLXQUERYCHANNELRECTSGIXPROC) (Display *display, int screen, int channel, int *dx, int *dy, int *dw, int *dh);
typedef int ( * PFNGLXQUERYCHANNELDELTASSGIXPROC) (Display *display, int screen, int channel, int *x, int *y, int *w, int *h);
typedef int ( * PFNGLXCHANNELRECTSYNCSGIXPROC) (Display *display, int screen, int channel, GLenum synctype);
#endif

#ifdef _DM_BUFFER_H_
#ifndef GLX_SGIX_dmbuffer
#define GLX_SGIX_dmbuffer 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern Bool glXAssociateDMPbufferSGIX (Display *, GLXPbufferSGIX, DMparams *, DMbuffer);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef Bool ( * PFNGLXASSOCIATEDMPBUFFERSGIXPROC) (Display *dpy, GLXPbufferSGIX pbuffer, DMparams *params, DMbuffer dmbuffer);
#endif

#endif /* _DM_BUFFER_H_ */
#ifndef GLX_SGIX_swap_group
#define GLX_SGIX_swap_group 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern void glXJoinSwapGroupSGIX (Display *, GLXDrawable, GLXDrawable);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef void ( * PFNGLXJOINSWAPGROUPSGIXPROC) (Display *dpy, GLXDrawable drawable, GLXDrawable member);
#endif

#ifndef GLX_SGIX_swap_barrier
#define GLX_SGIX_swap_barrier 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern void glXBindSwapBarrierSGIX (Display *, GLXDrawable, int);
extern Bool glXQueryMaxSwapBarriersSGIX (Display *, int, int *);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef void ( * PFNGLXBINDSWAPBARRIERSGIXPROC) (Display *dpy, GLXDrawable drawable, int barrier);
typedef Bool ( * PFNGLXQUERYMAXSWAPBARRIERSSGIXPROC) (Display *dpy, int screen, int *max);
#endif

#ifndef GLX_SUN_get_transparent_index
#define GLX_SUN_get_transparent_index 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern Status glXGetTransparentIndexSUN (Display *, Window, Window, long *);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef Status ( * PFNGLXGETTRANSPARENTINDEXSUNPROC) (Display *dpy, Window overlay, Window underlay, long *pTransparentIndex);
#endif

#ifndef GLX_MESA_copy_sub_buffer
#define GLX_MESA_copy_sub_buffer 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern void glXCopySubBufferMESA (Display *, GLXDrawable, int, int, int, int);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef void ( * PFNGLXCOPYSUBBUFFERMESAPROC) (Display *dpy, GLXDrawable drawable, int x, int y, int width, int height);
#endif

#ifndef GLX_MESA_pixmap_colormap
#define GLX_MESA_pixmap_colormap 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern GLXPixmap glXCreateGLXPixmapMESA (Display *, XVisualInfo *, Pixmap, Colormap);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef GLXPixmap ( * PFNGLXCREATEGLXPIXMAPMESAPROC) (Display *dpy, XVisualInfo *visual, Pixmap pixmap, Colormap cmap);
#endif

#ifndef GLX_MESA_release_buffers
#define GLX_MESA_release_buffers 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern Bool glXReleaseBuffersMESA (Display *, GLXDrawable);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef Bool ( * PFNGLXRELEASEBUFFERSMESAPROC) (Display *dpy, GLXDrawable drawable);
#endif

#ifndef GLX_MESA_set_3dfx_mode
#define GLX_MESA_set_3dfx_mode 1
#ifdef GLX_GLXEXT_PROTOTYPES
extern Bool glXSet3DfxModeMESA (int);
#endif /* GLX_GLXEXT_PROTOTYPES */
typedef Bool ( * PFNGLXSET3DFXMODEMESAPROC) (int mode);
#endif


#ifdef __cplusplus
}
#endif

#endif

