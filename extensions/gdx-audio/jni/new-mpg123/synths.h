#ifndef MPG123_SYNTH_H
#define MPG123_SYNTH_H

/* This is included inside frame.h, which is included in mpg123lib_intern.h,
   at the appropriate place.
   Explicit header inclusions here would cause circular dependencies. */

/* The handle needs these types for selecting the decoding routine at runtime.
   Not just for optimization, mainly for XtoY, mono/stereo. */
typedef int (*func_synth)(real *,int, mpg123_handle *,int );
typedef int (*func_synth_mono)(real *, mpg123_handle *);
typedef int (*func_synth_stereo)(real *, real *, mpg123_handle *);
enum synth_channel  { c_plain=0, c_stereo, c_m2s, c_mono, c_limit };
enum synth_resample
{
	 r_none=-1
	,r_1to1=0
#	ifndef NO_DOWNSAMPLE
	,r_2to1
	,r_4to1
#	endif
#	ifndef NO_NTOM
	,r_ntom
#	endif
	,r_limit
};
enum synth_format
{
	 f_none=-1
#	ifndef NO_16BIT
	,f_16
#	endif
#	ifndef NO_8BIT
	,f_8
#	endif
#	ifndef NO_REAL
	,f_real
#	endif
#	ifndef NO_32BIT
	,f_32
#	endif
	,f_limit
};
struct synth_s
{
	func_synth              plain[r_limit][f_limit];
	func_synth_stereo      stereo[r_limit][f_limit];
	func_synth_mono   mono2stereo[r_limit][f_limit];
	func_synth_mono          mono[r_limit][f_limit];
};

#endif
