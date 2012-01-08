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

enum synth_channel  { c_plain=0, c_stereo, c_mono2stereo, c_mono, c_limit };
enum synth_resample { r_none=-1, r_1to1=0, r_2to1, r_4to1, r_ntom, r_limit };

struct synth_s {
	func_synth              plain[r_limit];
	func_synth_stereo      stereo[r_limit];
	func_synth_mono   mono2stereo[r_limit];
	func_synth_mono          mono[r_limit];
};

#endif
