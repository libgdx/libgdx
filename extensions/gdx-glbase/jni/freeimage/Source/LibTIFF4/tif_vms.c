/* $Id: tif_vms.c,v 1.8 2013/11/29 22:22:01 drolon Exp $ */

/*
 * Copyright (c) 1988-1997 Sam Leffler
 * Copyright (c) 1991-1997 Silicon Graphics, Inc.
 *
 * Permission to use, copy, modify, distribute, and sell this software and 
 * its documentation for any purpose is hereby granted without fee, provided
 * that (i) the above copyright notices and this permission notice appear in
 * all copies of the software and related documentation, and (ii) the names of
 * Sam Leffler and Silicon Graphics may not be used in any advertising or
 * publicity relating to the software without the specific, prior written
 * permission of Sam Leffler and Silicon Graphics.
 * 
 * THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY 
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * IN NO EVENT SHALL SAM LEFFLER OR SILICON GRAPHICS BE LIABLE FOR
 * ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF 
 * LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 
 * OF THIS SOFTWARE.
 */

/*
 * TIFF Library VMS-specific Routines.
 */

#include <stdlib.h>
#include <unixio.h>
#include "tiffiop.h"
#if !HAVE_IEEEFP
#include <math.h>
#endif

#ifdef VAXC
#define	NOSHARE	noshare
#else
#define	NOSHARE
#endif

COMPILATION SHOULD FAIL
This file is not yet updated to reflect changes in LibTiff 4.0. If you have
the opportunity to update and test this file, please contact LibTiff folks
for all assistance you may require and contribute the results

#ifdef __alpha
/* Dummy entry point for backwards compatibility */
void TIFFModeCCITTFax3(void){}
#endif

static tsize_t
_tiffReadProc(thandle_t fd, tdata_t buf, tsize_t size)
{
	return (read((int) fd, buf, size));
}

static tsize_t
_tiffWriteProc(thandle_t fd, tdata_t buf, tsize_t size)
{
	return (write((int) fd, buf, size));
}

static toff_t
_tiffSeekProc(thandle_t fd, toff_t off, int whence)
{
	return ((toff_t) lseek((int) fd, (off_t) off, whence));
}

static int
_tiffCloseProc(thandle_t fd)
{
	return (close((int) fd));
}

#include <sys/stat.h>

static toff_t
_tiffSizeProc(thandle_t fd)
{
	struct stat sb;
	return (toff_t) (fstat((int) fd, &sb) < 0 ? 0 : sb.st_size);
}

#ifdef HAVE_MMAP
#include <starlet.h>
#include <fab.h>
#include <secdef.h>

/*
 * Table for storing information on current open sections. 
 * (Should really be a linked list)
 */
#define MAX_MAPPED 100
static int no_mapped = 0;
static struct {
	char *base;
	char *top;
	unsigned short channel;
} map_table[MAX_MAPPED];

/* 
 * This routine maps a file into a private section. Note that this 
 * method of accessing a file is by far the fastest under VMS.
 * The routine may fail (i.e. return 0) for several reasons, for
 * example:
 * - There is no more room for storing the info on sections.
 * - The process is out of open file quota, channels, ...
 * - fd does not describe an opened file.
 * - The file is already opened for write access by this process
 *   or another process
 * - There is no free "hole" in virtual memory that fits the
 *   size of the file
 */
static int
_tiffMapProc(thandle_t fd, tdata_t* pbase, toff_t* psize)
{
	char name[256];
	struct FAB fab;
	unsigned short channel;
	char *inadr[2], *retadr[2];
	unsigned long status;
	long size;
	
	if (no_mapped >= MAX_MAPPED)
		return(0);
	/*
	 * We cannot use a file descriptor, we
	 * must open the file once more.
	 */
	if (getname((int)fd, name, 1) == NULL)
		return(0);
	/* prepare the FAB for a user file open */
	fab = cc$rms_fab;
	fab.fab$l_fop |= FAB$V_UFO;
	fab.fab$b_fac = FAB$M_GET;
	fab.fab$b_shr = FAB$M_SHRGET;
	fab.fab$l_fna = name;
	fab.fab$b_fns = strlen(name);
	status = sys$open(&fab);	/* open file & get channel number */
	if ((status&1) == 0)
		return(0);
	channel = (unsigned short)fab.fab$l_stv;
	inadr[0] = inadr[1] = (char *)0; /* just an address in P0 space */
	/*
	 * Map the blocks of the file up to
	 * the EOF block into virtual memory.
	 */
	size = _tiffSizeProc(fd);
	status = sys$crmpsc(inadr, retadr, 0, SEC$M_EXPREG, 0,0,0, channel,
		TIFFhowmany(size,512), 0,0,0);  ddd
	if ((status&1) == 0){
		sys$dassgn(channel);
		return(0);
	}
	*pbase = (tdata_t) retadr[0];	/* starting virtual address */
	/*
	 * Use the size of the file up to the
	 * EOF mark for UNIX compatibility.
	 */
	*psize = (toff_t) size;
	/* Record the section in the table */
	map_table[no_mapped].base = retadr[0];
	map_table[no_mapped].top = retadr[1];
	map_table[no_mapped].channel = channel;
	no_mapped++;

        return(1);
}

/*
 * This routine unmaps a section from the virtual address space of 
 * the process, but only if the base was the one returned from a
 * call to TIFFMapFileContents.
 */
static void
_tiffUnmapProc(thandle_t fd, tdata_t base, toff_t size)
{
	char *inadr[2];
	int i, j;
	
	/* Find the section in the table */
	for (i = 0;i < no_mapped; i++) {
		if (map_table[i].base == (char *) base) {
			/* Unmap the section */
			inadr[0] = (char *) base;
			inadr[1] = map_table[i].top;
			sys$deltva(inadr, 0, 0);
			sys$dassgn(map_table[i].channel);
			/* Remove this section from the list */
			for (j = i+1; j < no_mapped; j++)
				map_table[j-1] = map_table[j];
			no_mapped--;
			return;
		}
	}
}
#else /* !HAVE_MMAP */
static int
_tiffMapProc(thandle_t fd, tdata_t* pbase, toff_t* psize)
{
	return (0);
}

static void
_tiffUnmapProc(thandle_t fd, tdata_t base, toff_t size)
{
}
#endif /* !HAVE_MMAP */

/*
 * Open a TIFF file descriptor for read/writing.
 */
TIFF*
TIFFFdOpen(int fd, const char* name, const char* mode)
{
	TIFF* tif;

	tif = TIFFClientOpen(name, mode,  ddd
	    (thandle_t) fd,
	    _tiffReadProc, _tiffWriteProc, _tiffSeekProc, _tiffCloseProc,
	    _tiffSizeProc, _tiffMapProc, _tiffUnmapProc);
	if (tif)
		tif->tif_fd = fd;
	return (tif);
}

/*
 * Open a TIFF file for read/writing.
 */
TIFF*
TIFFOpen(const char* name, const char* mode)
{
	static const char module[] = "TIFFOpen";
	int m, fd;

	m = _TIFFgetMode(mode, module);
	if (m == -1)
		return ((TIFF*)0);
        if (m&O_TRUNC){
                /*
		 * There is a bug in open in VAXC. If you use
		 * open w/ m=O_RDWR|O_CREAT|O_TRUNC the
		 * wrong thing happens.  On the other hand
		 * creat does the right thing.
                 */
                fd = creat((char *) /* bug in stdio.h */ name, 0666,
		    "alq = 128", "deq = 64", "mbc = 32",
		    "fop = tef");
	} else if (m&O_RDWR) {
		fd = open(name, m, 0666,
		    "deq = 64", "mbc = 32", "fop = tef", "ctx = stm");
	} else
		fd = open(name, m, 0666, "mbc = 32", "ctx = stm");
	if (fd < 0) {
		TIFFErrorExt(0, module, "%s: Cannot open", name);
		return ((TIFF*)0);
	}
	return (TIFFFdOpen(fd, name, mode));
}

tdata_t
_TIFFmalloc(tsize_t s)
{
        if (s == 0)
                return ((void *) NULL);

	return (malloc((size_t) s));
}

void
_TIFFfree(tdata_t p)
{
	free(p);
}

tdata_t
_TIFFrealloc(tdata_t p, tsize_t s)
{
	return (realloc(p, (size_t) s));
}

void
_TIFFmemset(tdata_t p, int v, tsize_t c)
{
	memset(p, v, (size_t) c);
}

void
_TIFFmemcpy(tdata_t d, const tdata_t s, tsize_t c)
{
	memcpy(d, s, (size_t) c);
}

int
_TIFFmemcmp(const tdata_t p1, const tdata_t p2, tsize_t c)
{
	return (memcmp(p1, p2, (size_t) c));
}

/*
 * On the VAX, we need to make those global, writable pointers
 * non-shareable, otherwise they would be made shareable by default.
 * On the AXP, this brain damage has been corrected. 
 * 
 * I (Karsten Spang, krs@kampsax.dk) have dug around in the GCC
 * manual and the GAS code and have come up with the following
 * construct, but I don't have GCC on my VAX, so it is untested.
 * Please tell me if it does not work.
 */

static void
vmsWarningHandler(const char* module, const char* fmt, va_list ap)
{
	if (module != NULL)
		fprintf(stderr, "%s: ", module);
	fprintf(stderr, "Warning, ");
	vfprintf(stderr, fmt, ap);
	fprintf(stderr, ".\n");
}

NOSHARE TIFFErrorHandler _TIFFwarningHandler = vmsWarningHandler
#if defined(VAX) && defined(__GNUC__)
asm("_$$PsectAttributes_NOSHR$$_TIFFwarningHandler")
#endif
;

static void
vmsErrorHandler(const char* module, const char* fmt, va_list ap)
{
	if (module != NULL)
		fprintf(stderr, "%s: ", module);
	vfprintf(stderr, fmt, ap);
	fprintf(stderr, ".\n");
}

NOSHARE TIFFErrorHandler _TIFFerrorHandler = vmsErrorHandler
#if defined(VAX) && defined(__GNUC__)
asm("_$$PsectAttributes_NOSHR$$_TIFFerrorHandler")
#endif
;


#if !HAVE_IEEEFP
/* IEEE floting point handling */

typedef	struct ieeedouble {
	unsigned long	mant2;          /* fix NDR: full 8-byte swap */
	unsigned long	mant	: 20,
		exp	: 11,
		sign	: 1;
} ieeedouble;
typedef	struct ieeefloat {
	unsigned long   mant	: 23,
		exp	: 8,
		sign	: 1;
} ieeefloat;

/* 
 * NB: These are D_FLOAT's, not G_FLOAT's. A G_FLOAT is
 *  simply a reverse-IEEE float/double.
 */

typedef	struct {
	unsigned long	mant1	: 7,
		exp	: 8,
		sign	: 1,
		mant2	: 16,
		mant3   : 16,
		mant4   : 16;
} nativedouble;
typedef	struct {
	unsigned long	mant1	: 7,
		exp	: 8,
		sign	: 1,
		mant2	: 16;
} nativefloat;

typedef	union {
	ieeedouble	ieee;
	nativedouble	native;
	char		b[8];
	uint32		l[2];
	double		d;
} double_t;

typedef	union {
	ieeefloat	ieee;
	nativefloat	native;
	char		b[4];
	uint32		l;
	float		f;
} float_t;

#if defined(VAXC) || defined(DECC)
#pragma inline(ieeetod,dtoieee)
#endif

/*
 * Convert an IEEE double precision number to native double precision.
 * The source is contained in two longwords, the second holding the sign,
 * exponent and the higher order bits of the mantissa, and the first
 * holding the rest of the mantissa as follows:
 * (Note: It is assumed that the number has been eight-byte swapped to
 * LSB first.)
 * 
 * First longword:
 *	32 least significant bits of mantissa
 * Second longword:
 *	0-19:	20 most significant bits of mantissa
 *	20-30:	exponent
 *	31:	sign
 * The exponent is stored as excess 1023.
 * The most significant bit of the mantissa is implied 1, and not stored.
 * If the exponent and mantissa are zero, the number is zero.
 * If the exponent is 0 (i.e. -1023) and the mantissa is non-zero, it is an
 * unnormalized number with the most significant bit NOT implied.
 * If the exponent is 2047, the number is invalid, in case the mantissa is zero,
 * this means overflow (+/- depending of the sign bit), otherwise
 * it simply means invalid number.
 * 
 * If the number is too large for the machine or was specified as overflow, 
 * +/-HUGE_VAL is returned.
 */
INLINE static void
ieeetod(double *dp)
{
	double_t source;
	long sign,exp,mant;
	double dmant;

	source.ieee = ((double_t*)dp)->ieee;
	sign = source.ieee.sign;
	exp = source.ieee.exp;
	mant = source.ieee.mant;

	if (exp == 2047) {
		if (mant)			/* Not a Number (NAN) */
			*dp = HUGE_VAL;
		else				/* +/- infinity */
			*dp = (sign ? -HUGE_VAL : HUGE_VAL);
		return;
	}
	if (!exp) {
		if (!(mant || source.ieee.mant2)) {	/* zero */
			*dp=0;
			return;
		} else {			/* Unnormalized number */
			/* NB: not -1023, the 1 bit is not implied */
			exp= -1022;
		}
	} else {
		mant |= 1<<20;
		exp -= 1023;
	}
	dmant = (((double) mant) +
		((double) source.ieee.mant2) / (((double) (1<<16)) *
		((double) (1<<16)))) / (double) (1<<20);
	dmant = ldexp(dmant, exp);
	if (sign)
		dmant= -dmant;
	*dp = dmant;
}

INLINE static void
dtoieee(double *dp)
{
	double_t num;
	double x;
	int exp;

	num.d = *dp;
	if (!num.d) {			/* Zero is just binary all zeros */
		num.l[0] = num.l[1] = 0;
		return;
	}

	if (num.d < 0) {		/* Sign is encoded separately */
		num.d = -num.d;
		num.ieee.sign = 1;
	} else {
		num.ieee.sign = 0;
	}

	/* Now separate the absolute value into mantissa and exponent */
	x = frexp(num.d, &exp);

	/*
	 * Handle cases where the value is outside the
	 * range for IEEE floating point numbers. 
	 * (Overflow cannot happen on a VAX, but underflow
	 * can happen for G float.)
	 */
	if (exp < -1022) {		/* Unnormalized number */
		x = ldexp(x, -1023-exp);
		exp = 0;
	} else if (exp > 1023) {	/* +/- infinity */
		x = 0;
		exp = 2047;
	} else {			/* Get rid of most significant bit */
		x *= 2;
		x -= 1;
		exp += 1022; /* fix NDR: 1.0 -> x=0.5, exp=1 -> ieee.exp = 1023 */
	}
	num.ieee.exp = exp;

	x *= (double) (1<<20);
	num.ieee.mant = (long) x;
	x -= (double) num.ieee.mant;
	num.ieee.mant2 = (long) (x*((double) (1<<16)*(double) (1<<16)));

	if (!(num.ieee.mant || num.ieee.exp || num.ieee.mant2)) {
		/* Avoid negative zero */
		num.ieee.sign = 0;
	}
	((double_t*)dp)->ieee = num.ieee;
}

/*
 * Beware, these do not handle over/under-flow
 * during conversion from ieee to native format.
 */
#define	NATIVE2IEEEFLOAT(fp) { \
    float_t t; \
    if (t.ieee.exp = (fp)->native.exp) \
	t.ieee.exp += -129 + 127; \
    t.ieee.sign = (fp)->native.sign; \
    t.ieee.mant = ((fp)->native.mant1<<16)|(fp)->native.mant2; \
    *(fp) = t; \
}
#define	IEEEFLOAT2NATIVE(fp) { \
    float_t t; int v = (fp)->ieee.exp; \
    if (v) v += -127 + 129;		/* alter bias of exponent */\
    t.native.exp = v;			/* implicit truncation of exponent */\
    t.native.sign = (fp)->ieee.sign; \
    v = (fp)->ieee.mant; \
    t.native.mant1 = v >> 16; \
    t.native.mant2 = v;\
    *(fp) = t; \
}

#define IEEEDOUBLE2NATIVE(dp) ieeetod(dp)

#define NATIVE2IEEEDOUBLE(dp) dtoieee(dp)


/*
 * These unions are used during floating point
 * conversions.  The above macros define the
 * conversion operations.
 */
void
TIFFCvtIEEEFloatToNative(TIFF* tif, u_int n, float* f)
{
	float_t* fp = (float_t*) f;

	while (n-- > 0) {
		IEEEFLOAT2NATIVE(fp);
		fp++;
	}
}

void
TIFFCvtNativeToIEEEFloat(TIFF* tif, u_int n, float* f)
{
	float_t* fp = (float_t*) f;

	while (n-- > 0) {
		NATIVE2IEEEFLOAT(fp);
		fp++;
	}
}
void
TIFFCvtIEEEDoubleToNative(TIFF* tif, u_int n, double* f)
{
	double_t* fp = (double_t*) f;

	while (n-- > 0) {
		IEEEDOUBLE2NATIVE(fp);
		fp++;
	}
}

void
TIFFCvtNativeToIEEEDouble(TIFF* tif, u_int n, double* f)
{
	double_t* fp = (double_t*) f;

	while (n-- > 0) {
		NATIVE2IEEEDOUBLE(fp);
		fp++;
	}
}
#endif
/*
 * Local Variables:
 * mode: c
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */
