#ifndef	__EGL_H
#define	__EGL_H

// include types and defines

#include	<GL/egl_defs.h>

// include log tokens

#include	<GL/egl_tokens.h>

// include simple void functions we ignore

#include	<GL/egl_void.h>

// include functions that need a bit of work, but we don't log

#include	<GL/egl_cpx.h>

// include functions we log

#ifdef		EGL_LOG_PTR

extern		unsigned int	*EGL_LOG_PTR;

inline	void	xGLL(int a) 		 	 {	*EGL_LOG_PTR=(unsigned int)a; EGL_LOG_PTR++;		};
inline	void	xGLL(unsigned int a) 	 {	*EGL_LOG_PTR=a; EGL_LOG_PTR++;					};
inline	void	xGLL(float a) 			 {	*(float *)EGL_LOG_PTR=a; EGL_LOG_PTR++;			};
inline	void	xGLL(double a) 			 {	*(float *)EGL_LOG_PTR=(float)a; EGL_LOG_PTR++;	};
inline	void	xGLL(const float *a)	 {  for(int t=0;t!=16;t++) xGLL(a[t]);					};
inline	void	xGLL(const double *a)	 {  for(int t=0;t!=16;t++) xGLL(a[t]);					};
#else

inline	void	xGLL(int a)			 	{};
inline	void	xGLL(unsigned int a) 	{};
inline	void	xGLL(float a) 		 	{};
inline	void	xGLL(double  a) 	 	{};
inline	void	xGLL(const float *a)    {};
inline	void	xGLL(const double  *a) 	{};

#endif

// functions we might log

#include	<GL/egl_logged.h>

#endif
