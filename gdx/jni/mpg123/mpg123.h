/*
	libmpg123: MPEG Audio Decoder library (version 20090712000000)

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
*/

#ifndef MPG123_LIB_H
#define MPG123_LIB_H

/** \file mpg123.h The header file for the libmpg123 MPEG Audio decoder */

#include <stdlib.h>
#include <sys/types.h>

#ifdef __cplusplus
extern "C" {
#endif
/** \defgroup mpg123_init mpg123 library and handle setup
 *
 * Functions to initialise and shutdown the mpg123 library and handles.
 * The parameters of handles have workable defaults, you only have to tune them when you want to tune something;-)
 * Tip: Use a RVA setting...
 *
 * @{
 */

/** Opaque structure for the libmpg123 decoder handle. */
struct mpg123_handle_struct;

/** Opaque structure for the libmpg123 decoder handle.
 *  Most functions take a pointer to a mpg123_handle as first argument and operate on its data in an object-oriented manner.
 */
typedef struct mpg123_handle_struct mpg123_handle;

/** Function to initialise the mpg123 library. 
 *	This function is not thread-safe. Call it exactly once per process, before any other (possibly threaded) work with the library.
 *
 *	\return MPG123_OK if successful, otherwise an error number.
 */
int  mpg123_init(void);

/** Function to close down the mpg123 library. 
 *	This function is not thread-safe. Call it exactly once per process, before any other (possibly threaded) work with the library. */
void mpg123_exit(void);

/** Create a handle with optional choice of decoder (named by a string, see mpg123_decoders() or mpg123_supported_decoders()).
 *  and optional retrieval of an error code to feed to mpg123_plain_strerror().
 *  Optional means: Any of or both the parameters may be NULL.
 *
 *  \return Non-NULL pointer when successful.
 */
mpg123_handle *mpg123_new(const char* decoder, int *error);

/** Delete handle, mh is either a valid mpg123 handle or NULL. */
void mpg123_delete(mpg123_handle *mh);

/** Enumeration of the parameters types that it is possible to set/get. */
enum mpg123_parms {
	MPG123_VERBOSE,        /**< set verbosity value for enabling messages to stderr, >= 0 makes sense (integer) */
	MPG123_FLAGS,          /**< set all flags, p.ex val = MPG123_GAPLESS|MPG123_MONO_MIX (integer) */
	MPG123_ADD_FLAGS,      /**< add some flags (integer) */
	MPG123_FORCE_RATE,     /**< when value > 0, force output rate to that value (integer) */
	MPG123_DOWN_SAMPLE,    /**< 0=native rate, 1=half rate, 2=quarter rate (integer) */
	MPG123_DOWNSPEED,      /**< play a frame N times (integer) */
	MPG123_UPSPEED,        /**< play every Nth frame (integer) */
	MPG123_START_FRAME,    /**< start with this frame (skip frames before that, integer) */ 
	MPG123_DECODE_FRAMES,  /**< decode only this number of frames (integer) */
	MPG123_OUTSCALE,       /**< the scale for output samples (amplitude - integer or float according to mpg123 output format, normally integer) */
	MPG123_TIMEOUT,        /**< timeout for reading from a stream (not supported on win32, integer) */
	MPG123_REMOVE_FLAGS,   /**< remove some flags (inverse of MPG123_ADD_FLAGS, integer) */
	MPG123_RESYNC_LIMIT,   /**< Try resync on frame parsing for that many bytes or until end of stream (<0 ... integer). */
	MPG123_INDEX_SIZE      /**< Set the frame index size (if supported). Values <0 mean that the index is allowed to grow dynamically in these steps (in positive direction, of course) -- Use this when you really want a full index with every individual frame. */
	,MPG123_PREFRAMES /**< Decode/ignore that many frames in advance for layer 3. This is needed to fill bit reservoir after seeking, for example (but also at least one frame in advance is needed to have all "normal" data for layer 3). Give a positive integer value, please.*/
};

/** Flag bits for MPG123_FLAGS, use the usual binary or to combine. */
enum mpg123_param_flags {
	 MPG123_FORCE_MONO   = 0x7  /**<     0111 Force some mono mode: This is a test bitmask for seeing if any mono forcing is active. */
	,MPG123_MONO_LEFT    = 0x1  /**<     0001 Force playback of left channel only.  */
	,MPG123_MONO_RIGHT   = 0x2  /**<     0010 Force playback of right channel only. */
	,MPG123_MONO_MIX     = 0x4  /**<     0100 Force playback of mixed mono.         */
	,MPG123_FORCE_STEREO = 0x8  /**<     1000 Force stereo output.                  */
	,MPG123_FORCE_8BIT   = 0x10 /**< 00010000 Force 8bit formats.                   */
	,MPG123_QUIET        = 0x20 /**< 00100000 Suppress any printouts (overrules verbose).                    */
	,MPG123_GAPLESS      = 0x40 /**< 01000000 Enable gapless decoding (default on if libmpg123 has support). */
	,MPG123_NO_RESYNC    = 0x80 /**< 10000000 Disable resync stream after error.                             */
	,MPG123_SEEKBUFFER   = 0x100 /**< 000100000000 Enable small buffer on non-seekable streams to allow some peek-ahead (for better MPEG sync). */
	,MPG123_FUZZY        = 0x200 /**< 001000000000 Enable fuzzy seeks (guessing byte offsets or using approximate seek points from Xing TOC) */
	,MPG123_FORCE_FLOAT  = 0x400 /**< 010000000000 Force floating point output (32 or 64 bits depends on mpg123 internal precision). */
};

/* TODO: Assess the possibilities and troubles of changing parameters during playback. */

/** Set a specific parameter, for a specific mpg123_handle, using a parameter 
 *  type key chosen from the mpg123_parms enumeration, to the specified value. */
int mpg123_param(mpg123_handle *mh, enum mpg123_parms type, long value, double fvalue);

/** Get a specific parameter, for a specific mpg123_handle. 
 *  See the mpg123_parms enumeration for a list of available parameters. */
int mpg123_getparam(mpg123_handle *mh, enum mpg123_parms type, long *val, double *fval);

/* @} */


/** \defgroup mpg123_error mpg123 error handling
 *
 * Functions to get text version of the error numbers and an enumeration
 * of the error codes returned by libmpg123.
 *
 * Most functions operating on a mpg123_handle simply return MPG123_OK on success and MPG123_ERR on failure (setting the internal error variable of the handle to the specific error code).
 * Decoding/seek functions may also return message codes MPG123_DONE, MPG123_NEW_FORMAT and MPG123_NEED_MORE (please read up on these on how to react!).
 * The positive range of return values is used for "useful" values when appropriate.
 *
 * @{
 */

/** Enumeration of the message and error codes and returned by libmpg123 functions. */
enum mpg123_errors
{
	MPG123_DONE=-12,	/**< Message: Track ended. Stop decoding. */
	MPG123_NEW_FORMAT=-11,	/**< Message: Output format will be different on next call. Note that some libmpg123 versions between 1.4.3 and 1.8.0 insist on you calling mpg123_getformat() after getting this message code. Newer verisons behave like advertised: You have the chance to call mpg123_getformat(), but you can also just continue decoding and get your data. */
	MPG123_NEED_MORE=-10,	/**< Message: For feed reader: "Feed me more!" (call mpg123_feed() or mpg123_decode() with some new input data). */
	MPG123_ERR=-1,			/**< Generic Error */
	MPG123_OK=0, 			/**< Success */
	MPG123_BAD_OUTFORMAT, 	/**< Unable to set up output format! */
	MPG123_BAD_CHANNEL,		/**< Invalid channel number specified. */
	MPG123_BAD_RATE,		/**< Invalid sample rate specified.  */
	MPG123_ERR_16TO8TABLE,	/**< Unable to allocate memory for 16 to 8 converter table! */
	MPG123_BAD_PARAM,		/**< Bad parameter id! */
	MPG123_BAD_BUFFER,		/**< Bad buffer given -- invalid pointer or too small size. */
	MPG123_OUT_OF_MEM,		/**< Out of memory -- some malloc() failed. */
	MPG123_NOT_INITIALIZED,	/**< You didn't initialize the library! */
	MPG123_BAD_DECODER,		/**< Invalid decoder choice. */
	MPG123_BAD_HANDLE,		/**< Invalid mpg123 handle. */
	MPG123_NO_BUFFERS,		/**< Unable to initialize frame buffers (out of memory?). */
	MPG123_BAD_RVA,			/**< Invalid RVA mode. */
	MPG123_NO_GAPLESS,		/**< This build doesn't support gapless decoding. */
	MPG123_NO_SPACE,		/**< Not enough buffer space. */
	MPG123_BAD_TYPES,		/**< Incompatible numeric data types. */
	MPG123_BAD_BAND,		/**< Bad equalizer band. */
	MPG123_ERR_NULL,		/**< Null pointer given where valid storage address needed. */
	MPG123_ERR_READER,		/**< Error reading the stream. */
	MPG123_NO_SEEK_FROM_END,/**< Cannot seek from end (end is not known). */
	MPG123_BAD_WHENCE,		/**< Invalid 'whence' for seek function.*/
	MPG123_NO_TIMEOUT,		/**< Build does not support stream timeouts. */
	MPG123_BAD_FILE,		/**< File access error. */
	MPG123_NO_SEEK,			/**< Seek not supported by stream. */
	MPG123_NO_READER,		/**< No stream opened. */
	MPG123_BAD_PARS,		/**< Bad parameter handle. */
	MPG123_BAD_INDEX_PAR,	/**< Bad parameters to mpg123_index() */
	MPG123_OUT_OF_SYNC,	/**< Lost track in bytestream and did not try to resync. */
	MPG123_RESYNC_FAIL,	/**< Resync failed to find valid MPEG data. */
	MPG123_NO_8BIT,	/**< No 8bit encoding possible. */
	MPG123_BAD_ALIGN,	/**< Stack aligmnent error */
	MPG123_NULL_BUFFER,	/**< NULL input buffer with non-zero size... */
	MPG123_NO_RELSEEK,	/**< Relative seek not possible (screwed up file offset) */
	MPG123_NULL_POINTER, /**< You gave a null pointer somewhere where you shouldn't have. */
	MPG123_BAD_KEY,	/**< Bad key value given. */
	MPG123_NO_INDEX,	/**< No frame index in this build. */
	MPG123_INDEX_FAIL,	/**< Something with frame index went wrong. */
	MPG123_BAD_DECODER_SETUP,	/**< Something prevents a proper decoder setup */
	MPG123_MISSING_FEATURE  /**< This feature has not been built into libmpg123. */
	,MPG123_BAD_VALUE /**< A bad value has been given, somewhere. */
	,MPG123_LSEEK_FAILED /**< Low-level seek failed. */
};

/** Return a string describing that error errcode means. */
const char* mpg123_plain_strerror(int errcode);

/** Give string describing what error has occured in the context of handle mh.
 *  When a function operating on an mpg123 handle returns MPG123_ERR, you should check for the actual reason via
 *  char *errmsg = mpg123_strerror(mh)
 *  This function will catch mh == NULL and return the message for MPG123_BAD_HANDLE. */
const char* mpg123_strerror(mpg123_handle *mh);

/** Return the plain errcode intead of a string. */
int mpg123_errcode(mpg123_handle *mh);

/*@}*/


/** \defgroup mpg123_output mpg123 output audio format 
 *
 * Functions to get and select the format of the decoded audio.
 *
 * @{
 */

/** An enum over all sample types possibly known to mpg123.
 *  The values are designed as bit flags to allow bitmasking for encoding families.
 *
 *  Note that (your build of) libmpg123 does not necessarily support all these.
 *  Usually, you can expect the 8bit encodings and signed 16 bit.
 *  Also 32bit float will be usual beginning with mpg123-1.7.0 .
 *  What you should bear in mind is that (SSE, etc) optimized routines are just for
 *  signed 16bit (and 8bit derived from that). Other formats use plain C code.
 *
 *  All formats are in native byte order. On a little endian machine this should mean
 *  that you can just feed the MPG123_ENC_SIGNED_32 data to common 24bit hardware that
 *  ignores the lowest byte (or you could choose to do rounding with these lower bits).
 */
enum mpg123_enc_enum
{
	 MPG123_ENC_8      = 0x00f  /**< 0000 0000 1111 Some 8 bit  integer encoding. */ 
	,MPG123_ENC_16     = 0x040  /**< 0000 0100 0000 Some 16 bit integer encoding. */
	,MPG123_ENC_32     = 0x100  /**< 0001 0000 0000 Some 32 bit integer encoding. */
	,MPG123_ENC_SIGNED = 0x080  /**< 0000 1000 0000 Some signed integer encoding. */
	,MPG123_ENC_FLOAT  = 0xe00  /**< 1110 0000 0000 Some float encoding. */
	,MPG123_ENC_SIGNED_16   = (MPG123_ENC_16|MPG123_ENC_SIGNED|0x10) /**<           1101 0000 signed 16 bit */
	,MPG123_ENC_UNSIGNED_16 = (MPG123_ENC_16|0x20)                   /**<           0110 0000 unsigned 16 bit */
	,MPG123_ENC_UNSIGNED_8  = 0x01                                   /**<           0000 0001 unsigned 8 bit */
	,MPG123_ENC_SIGNED_8    = (MPG123_ENC_SIGNED|0x02)               /**<           1000 0010 signed 8 bit */
	,MPG123_ENC_ULAW_8      = 0x04                                   /**<           0000 0100 ulaw 8 bit */
	,MPG123_ENC_ALAW_8      = 0x08                                   /**<           0000 1000 alaw 8 bit */
	,MPG123_ENC_SIGNED_32   = MPG123_ENC_32|MPG123_ENC_SIGNED|0x1000 /**< 0001 0001 1000 0000 signed 32 bit */
	,MPG123_ENC_UNSIGNED_32 = MPG123_ENC_32|0x2000                   /**< 0010 0001 0000 0000 unsigned 32 bit */
	,MPG123_ENC_FLOAT_32    = 0x200                                  /**<      0010 0000 0000 32bit float */
	,MPG123_ENC_FLOAT_64    = 0x400                                  /**<      0100 0000 0000 64bit float */
	,MPG123_ENC_ANY = ( MPG123_ENC_SIGNED_16  | MPG123_ENC_UNSIGNED_16 | MPG123_ENC_UNSIGNED_8 
	                  | MPG123_ENC_SIGNED_8   | MPG123_ENC_ULAW_8      | MPG123_ENC_ALAW_8
	                  | MPG123_ENC_SIGNED_32  | MPG123_ENC_UNSIGNED_32
	                  | MPG123_ENC_FLOAT_32   | MPG123_ENC_FLOAT_64 ) /**< any encoding */
};

/** They can be combined into one number (3) to indicate mono and stereo... */
enum mpg123_channelcount
{
	 MPG123_MONO   = 1
	,MPG123_STEREO = 2
};

/** An array of supported standard sample rates
 *  These are possible native sample rates of MPEG audio files.
 *  You can still force mpg123 to resample to a different one, but by default you will only get audio in one of these samplings.
 *  \param list Store a pointer to the sample rates array there.
 *  \param number Store the number of sample rates there. */
void mpg123_rates(const long **list, size_t *number);

/** An array of supported audio encodings.
 *  An audio encoding is one of the fully qualified members of mpg123_enc_enum (MPG123_ENC_SIGNED_16, not MPG123_SIGNED).
 *  \param list Store a pointer to the encodings array there.
 *  \param number Store the number of encodings there. */
void mpg123_encodings(const int **list, size_t *number);

/** Configure a mpg123 handle to accept no output format at all, 
 *  use before specifying supported formats with mpg123_format */
int mpg123_format_none(mpg123_handle *mh);

/** Configure mpg123 handle to accept all formats 
 *  (also any custom rate you may set) -- this is default. */
int mpg123_format_all(mpg123_handle *mh);

/** Set the audio format support of a mpg123_handle in detail:
 *  \param mh audio decoder handle
 *  \param rate The sample rate value (in Hertz).
 *  \param channels A combination of MPG123_STEREO and MPG123_MONO.
 *  \param encodings A combination of accepted encodings for rate and channels, p.ex MPG123_ENC_SIGNED16 | MPG123_ENC_ULAW_8 (or 0 for no support). Please note that some encodings may not be supported in the library build and thus will be ignored here.
 *  \return MPG123_OK on success, MPG123_ERR if there was an error. */
int mpg123_format(mpg123_handle *mh, long rate, int channels, int encodings);

/** Check to see if a specific format at a specific rate is supported 
 *  by mpg123_handle.
 *  \return 0 for no support (that includes invalid parameters), MPG123_STEREO, 
 *          MPG123_MONO or MPG123_STEREO|MPG123_MONO. */
int mpg123_format_support(mpg123_handle *mh, long rate, int encoding);

/** Get the current output format written to the addresses givenr. */
int mpg123_getformat(mpg123_handle *mh, long *rate, int *channels, int *encoding);

/*@}*/


/** \defgroup mpg123_input mpg123 file input and decoding
 *
 * Functions for input bitstream and decoding operations.
 * Decoding/seek functions may also return message codes MPG123_DONE, MPG123_NEW_FORMAT and MPG123_NEED_MORE (please read up on these on how to react!).
 * @{
 */

/* reading samples / triggering decoding, possible return values: */
/** Enumeration of the error codes returned by libmpg123 functions. */

/** Open and prepare to decode the specified file by filesystem path.
 *  This does not open HTTP urls; libmpg123 contains no networking code.
 *  If you want to decode internet streams, use mpg123_open_fd() or mpg123_open_feed().
 */
int mpg123_open(mpg123_handle *mh, const char *path);

/** Use an already opened file descriptor as the bitstream input
 *  mpg123_close() will _not_ close the file descriptor.
 */
int mpg123_open_fd(mpg123_handle *mh, int fd);

/** Closes the source, if libmpg123 opened it. */
int mpg123_close(mpg123_handle *mh);

/** Read from stream and decode up to outmemsize bytes.
 *  \param outmemory address of output buffer to write to
 *  \param outmemsize maximum number of bytes to write
 *  \param done address to store the number of actually decoded bytes to
 *  \return error/message code (watch out for MPG123_DONE and friends!) */
int mpg123_read(mpg123_handle *mh, unsigned char *outmemory, size_t outmemsize, size_t *done);

/** Decode MPEG Audio from inmemory to outmemory. 
 *  This is very close to a drop-in replacement for old mpglib.
 *  When you give zero-sized output buffer the input will be parsed until 
 *  decoded data is available. This enables you to get MPG123_NEW_FORMAT (and query it) 
 *  without taking decoded data.
 *  Think of this function being the union of mpg123_read() and mpg123_feed() (which it actually is, sort of;-).
 *  You can actually always decide if you want those specialized functions in separate steps or one call this one here.
 *  \param inmemory input buffer
 *  \param inmemsize number of input bytes
 *  \param outmemory output buffer
 *  \param outmemsize maximum number of output bytes
 *  \param done address to store the number of actually decoded bytes to
 *  \return error/message code (watch out especially for MPG123_NEED_MORE)
 */
int mpg123_decode(mpg123_handle *mh, const unsigned char *inmemory, size_t inmemsize, unsigned char *outmemory, size_t outmemsize, size_t *done);

/*@}*/


/** \defgroup mpg123_seek mpg123 position and seeking
 *
 * Functions querying and manipulating position in the decoded audio bitstream.
 * The position is measured in decoded audio samples, or MPEG frame offset for the specific functions.
 * If gapless code is in effect, the positions are adjusted to compensate the skipped padding/delay - meaning, you should not care about that at all and just use the position defined for the samples you get out of the decoder;-)
 * The general usage is modelled after stdlib's ftell() and fseek().
 * Especially, the whence parameter for the seek functions has the same meaning as the one for fseek() and needs the same constants from stdlib.h: 
 * - SEEK_SET: set position to (or near to) specified offset
 * - SEEK_CUR: change position by offset from now
 * - SEEK_END: set position to offset from end
 *
 * Note that sample-accurate seek only works when gapless support has been enabled at compile time; seek is frame-accurate otherwise.
 * Also, really sample-accurate seeking (meaning that you get the identical sample value after seeking compared to plain decoding up to the position) is only guaranteed when you do not mess with the position code by using MPG123_UPSPEED, MPG123_DOWNSPEED or MPG123_START_FRAME. The first two mainly should cause trouble with NtoM resampling, but in any case with these options in effect, you have to keep in mind that the sample offset is not the same as counting the samples you get from decoding since mpg123 counts the skipped samples, too (or the samples played twice only once)!
 * Short: When you care about the sample position, don't mess with those parameters;-)
 * Also, seeking is not guaranteed to work for all streams (underlying stream may not support it).
 *
 * @{
 */

/** Returns the current position in samples.
 *  On the next read, you'd get that sample. */
off_t mpg123_tell(mpg123_handle *mh);

/** Seek to a desired sample offset. 
 *  Set whence to SEEK_SET, SEEK_CUR or SEEK_END.
 *  \return The resulting offset >= 0 or error/message code */
off_t mpg123_seek(mpg123_handle *mh, off_t sampleoff, int whence);
/*@}*/


// TODO: own
/** \defgroup mpg123_voleq mpg123 volume and equalizer
 *
 * @{
 */

enum mpg123_channels
{
	 MPG123_LEFT=0x1	/**< The Left Channel. */
	,MPG123_RIGHT=0x2	/**< The Right Channel. */
	,MPG123_LR=0x3	/**< Both left and right channel; same as MPG123_LEFT|MPG123_RIGHT */
};

/** Set the 32 Band Audio Equalizer settings.
 *  \param channel Can be MPG123_LEFT, MPG123_RIGHT or MPG123_LEFT|MPG123_RIGHT for both.
 *  \param band The equalizer band to change (from 0 to 31)
 *  \param val The (linear) adjustment factor. */
int mpg123_eq(mpg123_handle *mh, enum mpg123_channels channel, int band, double val);

/** Reset the 32 Band Audio Equalizer settings to flat */
int mpg123_reset_eq(mpg123_handle *mh);


/** \defgroup mpg123_status mpg123 status and information
 *
 * @{
 */

/** Enumeration of the mode types of Variable Bitrate */
enum mpg123_vbr {
	MPG123_CBR=0,	/**< Constant Bitrate Mode (default) */
	MPG123_VBR,		/**< Variable Bitrate Mode */
	MPG123_ABR		/**< Average Bitrate Mode */
};

/** Enumeration of the MPEG Versions */
enum mpg123_version {
	MPG123_1_0=0,	/**< MPEG Version 1.0 */
	MPG123_2_0,		/**< MPEG Version 2.0 */
	MPG123_2_5		/**< MPEG Version 2.5 */
};


/** Enumeration of the MPEG Audio mode.
 *  Only the mono mode has 1 channel, the others have 2 channels. */
enum mpg123_mode {
	MPG123_M_STEREO=0,	/**< Standard Stereo. */
	MPG123_M_JOINT,		/**< Joint Stereo. */
	MPG123_M_DUAL,		/**< Dual Channel. */
	MPG123_M_MONO		/**< Single Channel. */
};


/** Enumeration of the MPEG Audio flag bits */
enum mpg123_flags {
	MPG123_CRC=0x1,			/**< The bitstream is error protected using 16-bit CRC. */
	MPG123_COPYRIGHT=0x2,	/**< The bitstream is copyrighted. */
	MPG123_PRIVATE=0x4,		/**< The private bit has been set. */
	MPG123_ORIGINAL=0x8	/**< The bitstream is an original, not a copy. */
};

/** Data structure for storing information about a frame of MPEG Audio */
struct mpg123_frameinfo {
	enum mpg123_version version;	/**< The MPEG version (1.0/2.0/2.5). */
	int layer;						/**< The MPEG Audio Layer (MP1/MP2/MP3). */
	long rate; 						/**< The sampling rate in Hz. */
	enum mpg123_mode mode;			/**< The audio mode (Mono, Stereo, Joint-stero, Dual Channel). */
	int mode_ext;					/**< The mode extension bit flag. */
	int framesize;					/**< The size of the frame (in bytes). */
	enum mpg123_flags flags;		/**< MPEG Audio flag bits. */
	int emphasis;					/**< The emphasis type. */
	int bitrate;					/**< Bitrate of the frame (kbps). */
	int abr_rate;					/**< The target average bitrate. */
	enum mpg123_vbr vbr;			/**< The VBR mode. */
};

/** Get frame information about the MPEG audio bitstream and store it in a mpg123_frameinfo structure. */
int mpg123_info(mpg123_handle *mh, struct mpg123_frameinfo *mi);

/** Get the safe output buffer size for all cases (when you want to replace the internal buffer) */
size_t mpg123_safe_buffer(); 

/** Return, if possible, the full (expected) length of current track in samples.
  * \return length >= 0 or MPG123_ERR if there is no length guess possible. */
off_t mpg123_length(mpg123_handle *mh);

/** The key values for state information from mpg123_getstate(). */
enum mpg123_state {
	 MPG123_ACCURATE = 1 /**< Query if positons are currently accurate (integer value, 0 if false, 1 if true) */
};

/*@}*/
/** \defgroup mpg123_advpar mpg123 advanced parameter API
 *
 *  Direct access to a parameter set without full handle around it.
 *	Possible uses:
 *    - Influence behaviour of library _during_ initialization of handle (MPG123_VERBOSE).
 *    - Use one set of parameters for multiple handles.
 *
 *	The functions for handling mpg123_pars (mpg123_par() and mpg123_fmt() 
 *  family) directly return a fully qualified mpg123 error code, the ones 
 *  operating on full handles normally MPG123_OK or MPG123_ERR, storing the 
 *  specific error code itseld inside the handle. 
 *
 * @{
 */

/** Opaque structure for the libmpg123 decoder parameters. */
struct mpg123_pars_struct;

/** Opaque structure for the libmpg123 decoder parameters. */
typedef struct mpg123_pars_struct   mpg123_pars;

/** Create a handle with preset parameters. */
mpg123_handle *mpg123_parnew(mpg123_pars *mp, const char* decoder, int *error);

/** Allocate memory for and return a pointer to a new mpg123_pars */
mpg123_pars *mpg123_new_pars(int *error);

/** Delete and free up memory used by a mpg123_pars data structure */
void mpg123_delete_pars(mpg123_pars* mp);

/** Configure mpg123 parameters to accept no output format at all, 
 * use before specifying supported formats with mpg123_format */
int mpg123_fmt_none(mpg123_pars *mp);

/** Configure mpg123 parameters to accept all formats 
 *  (also any custom rate you may set) -- this is default. */
int mpg123_fmt_all(mpg123_pars *mp);

/** Set the audio format support of a mpg123_pars in detail:
	\param rate The sample rate value (in Hertz).
	\param channels A combination of MPG123_STEREO and MPG123_MONO.
	\param encodings A combination of accepted encodings for rate and channels, p.ex MPG123_ENC_SIGNED16|MPG123_ENC_ULAW_8 (or 0 for no support).
	\return 0 on success, -1 if there was an error. /
*/
int mpg123_fmt(mpg123_pars *mh, long rate, int channels, int encodings); /* 0 is good, -1 is error */

/** Check to see if a specific format at a specific rate is supported 
 *  by mpg123_pars.
 *  \return 0 for no support (that includes invalid parameters), MPG123_STEREO, 
 *          MPG123_MONO or MPG123_STEREO|MPG123_MONO. */
int mpg123_fmt_support(mpg123_pars *mh,   long rate, int encoding);

/** Set a specific parameter, for a specific mpg123_pars, using a parameter 
 *  type key chosen from the mpg123_parms enumeration, to the specified value. */
int mpg123_par(mpg123_pars *mp, enum mpg123_parms type, long value, double fvalue);

/** Get a specific parameter, for a specific mpg123_pars. 
 *  See the mpg123_parms enumeration for a list of available parameters. */
int mpg123_getpar(mpg123_pars *mp, enum mpg123_parms type, long *val, double *fval);

/* @} */

/** The max size of one frame's decoded output with current settings.
 *  Use that to determine an appropriate minimum buffer size for decoding one frame. */
size_t mpg123_outblock(mpg123_handle *mh);

#ifdef __cplusplus
}
#endif

#endif
