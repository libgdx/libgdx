# m4 configure test script for the SoundTouch library
# (c)2003 David W. Durham
#
# $Id: soundtouch.m4 20 2008-02-17 14:20:52Z oparviai $
#
# This file can be included with other packages that need to test
# for libSoundTouch.
#
# It will #define HAVE_LIBSOUNDTOUCH iff the library is found
# It will AC_SUBST SOUNDTOUCH_LIBS and SOUNDTOUCH_CXXFLAGS as well
# It also defines some flags to the configure script for specifying
# the location to search for libSoundTouch
#
# A user of libSoundTouch should add @SOUNDTOUCH_LIBS@ and 
# @SOUNDTOUCH_CXXFLAGS@ to the appropriate variables in his
# Makefile.am files
#
# This script works with autoconf-2.5x and automake-1.6 but I have
# not tested it with older versions.


dnl        min version not supported yet
dnl AM_PATH_SOUNDTOUCH([MINMUM-VERSION, [(additional) ACTION-IF-FOUND] [, ACTION-IF-NOT-FOUND]]])

AH_TEMPLATE([HAVE_LIBSOUNDTOUCH], [defined by $0])
SOUNDTOUCH_CXXFLAGS=""
SOUNDTOUCH_LIBS=""

AC_DEFUN([AM_PATH_SOUNDTOUCH],[
	AC_ARG_WITH(soundtouch-prefix,[  --with-soundtouch-prefix=DIR   Prefix where SoundTouch was installed (optional)], [soundtouch_prefix="$withval"],[soundtouch_prefix=""])

	AC_ARG_ENABLE(soundtouch-check,[  --disable-soundtouch-check   Do not look for the SoundTouch Library],[enable_soundtouch_check="$enableval"],[enable_soundtouch_check="yes"])

	if test "$enable_soundtouch_check" = "yes"
	then
		saved_CPPFLAGS="$CPPFLAGS"
		saved_LDFLAGS="$LDFLAGS"
	   
		CPPFLAGS="$CPPFLAGS -I$soundtouch_prefix/include"
		LDFLAGS="$LDFLAGS -L$soundtouch_prefix/lib"
   
		dnl make sure SoundTouch.h header file exists
			dnl could use AC_CHECK_HEADERS to check for all of them, but the supporting .h file names may change later
		AC_CHECK_HEADER([soundtouch/SoundTouch.h],[
				dnl SoundTouch.h found
				dnl make sure libSoundTouch is linkable
				AC_CHECK_LIB([SoundTouch],[soundtouch_ac_test],[
					dnl libSoundTouch found
					SOUNDTOUCH_CXXFLAGS="-I$soundtouch_prefix/include"
					SOUNDTOUCH_LIBS="-L$soundtouch_prefix/lib -lSoundTouch"
					AC_DEFINE([HAVE_LIBSOUNDTOUCH])

					dnl run action-if-found
					ifelse([$2], , :, [$2])
				],[ 
					dnl run action-if-not-found
					ifelse([$3], , :, [$3])
				])
			],[
				dnl run action-if-not-found
				ifelse([$3], , :, [$3])
			])

		CPPFLAGS="$saved_CPPFLAGS"
		LDFLAGS="$saved_LDFLAGS"
	fi

	AC_SUBST(SOUNDTOUCH_CXXFLAGS)
	AC_SUBST(SOUNDTOUCH_LIBS)
])
