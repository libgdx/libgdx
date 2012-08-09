dnl SoundTouch configure.ac, by David W. Durham
dnl
dnl $Id: configure.ac 141 2012-04-04 19:47:28Z oparviai $
dnl
dnl This file is part of SoundTouch, an audio processing library for pitch/time adjustments
dnl 
dnl SoundTouch is free software; you can redistribute it and/or modify it under the
dnl terms of the GNU General Public License as published by the Free Software
dnl Foundation; either version 2 of the License, or (at your option) any later
dnl version.
dnl 
dnl SoundTouch is distributed in the hope that it will be useful, but WITHOUT ANY
dnl WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
dnl FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
dnl details.
dnl 
dnl You should have received a copy of the GNU General Public License along with
dnl this program; if not, write to the Free Software Foundation, Inc., 59 Temple
dnl Place - Suite 330, Boston, MA  02111-1307, USA
# Process this file with autoconf to produce a configure script.

AC_INIT(SoundTouch, 1.7.0, [http://www.surina.net/soundtouch])
AC_CONFIG_AUX_DIR(config)
AM_CONFIG_HEADER([include/soundtouch_config.h])
AM_INIT_AUTOMAKE
#AC_DISABLE_SHARED	dnl This makes libtool only build static libs 
AC_DISABLE_STATIC	dnl This makes libtool only build shared libs
#AC_GNU_SOURCE 		dnl enable posix extensions in glibc

AC_LANG(C++)





dnl ############################################################################
dnl # Checks for programs                                                      #
dnl ############################################################################
AC_PROG_CXX
#AC_PROG_AWK
AC_PROG_CC
AC_PROG_CPP
AC_PROG_CXXCPP
AC_PROG_INSTALL
#AC_PROG_LN_S
AC_PROG_MAKE_SET

AM_PROG_LIBTOOL dnl turn on using libtool




dnl ############################################################################
dnl # Checks for header files                                                  #
dnl ############################################################################
AC_HEADER_STDC
#AC_HEADER_SYS_WAIT
#	add any others you want to check for here
AC_CHECK_HEADERS([cpuid.h])

if test "x$ac_cv_header_cpuid_h" = "xno"; then
	AC_MSG_WARN([The cpuid.h file was not found therefore the x86 optimizations will be disabled.])
	AC_MSG_WARN([If using a x86 architecture and optimizations are desired then please install gcc (>= 4.3).])
	AC_MSG_WARN([If using a non-x86 architecture then this is expected and can be ignored.])
fi
 

dnl ############################################################################
dnl # Checks for typedefs, structures, and compiler characteristics            $
dnl ############################################################################
AC_C_CONST
AC_C_INLINE
#AC_TYPE_OFF_T
#AC_TYPE_SIZE_T


AC_ARG_ENABLE(integer-samples,
              [AC_HELP_STRING([--enable-integer-samples],
                              [use integer samples instead of floats
[default=yes]])],,
              [enable_integer_samples=no])


# Let the user enable/disable the x86 optimizations.
# Useful when compiling on non-x86 architectures.
AC_ARG_ENABLE([x86-optimizations],
              [AS_HELP_STRING([--enable-x86-optimizations],
                              [use MMX or SSE optimization
[default=yes]])],[enable_x86_optimizations="${enableval}"],
              [enable_x86_optimizations=yes])

# Tell the Makefile.am if the user wants to disable optimizations.
# Makefile.am will enable them by default if support is available.
# Note: We check if optimizations are supported a few lines down.
AM_CONDITIONAL([X86_OPTIMIZATIONS], [test "x$enable_x86_optimizations" = "xyes"])


if test "x$enable_integer_samples" = "xyes"; then
        echo "****** Integer sample type enabled ******"
        AC_DEFINE(SOUNDTOUCH_INTEGER_SAMPLES,1,[Use Integer as Sample type])
else
        echo "****** Float sample type enabled ******"
        AC_DEFINE(SOUNDTOUCH_FLOAT_SAMPLES,1,[Use Float as Sample type])
fi


# Check if optimizations are supported in the system at build time.
if test "x$enable_x86_optimizations" = "xyes" -a "x$ac_cv_header_cpuid_h" = "xyes"; then
        echo "****** x86 optimizations enabled ******"

	original_saved_CXXFLAGS=$CXXFLAGS
	have_mmx_intrinsics=no
	OPT_CXXFLAGS="-mmmx -Winline"
	CXXFLAGS="$OPT_CXXFLAGS $CXXFLAGS"

	# Check if the user can compile MMX code using intrinsics.
	# GCC supports MMX intrinsics since version 3.3
	# A more recent GCC (>= 4.3) is recommended.
	AC_COMPILE_IFELSE([AC_LANG_SOURCE([[
	#if defined(__GNUC__) && (__GNUC__ < 3 || (__GNUC__ == 3 && __GNUC_MINOR__ < 3))
	#error "Need GCC >= 3.3 for MMX intrinsics"
	#endif
	#include <mmintrin.h>
	int main () {
	    __m64 loop = _mm_cvtsi32_si64 (1);
	    return _mm_cvtsi64_si32 (loop);
	}]])],[have_mmx_intrinsics=yes])
	CXXFLAGS=$original_saved_CXXFLAGS

	# Inform the user if we did or did not find MMX support.
	#
	# If we enable optimization and integer samples we only require MMX.
	# Disable optimizations in the SSTypes.h file if this is not the case.
	if test "x$have_mmx_intrinsics" = "xyes" ; then
		echo "****** MMX support found ******"
	else
		echo "****** No MMX support found ******"
		if test "x$enable_integer_samples" = "xyes"; then
		        echo "****** Disabling optimizations. Using integer samples with no MMX support ******"
		        AC_DEFINE([SOUNDTOUCH_DISABLE_X86_OPTIMIZATIONS],[1],[Do not use x86 optimizations])
		fi
	fi


	have_sse_intrinsics=no
	OPT_CXXFLAGS="-msse -Winline"
	CXXFLAGS="$OPT_CXXFLAGS $CXXFLAGS"

	# Check if the user can compile SSE code using intrinsics.
	# GCC supports SSE intrinsics since version 3.3
	# A more recent GCC (>= 4.3) is recommended.
	AC_COMPILE_IFELSE([AC_LANG_SOURCE([[
	#if defined(__GNUC__) && (__GNUC__ < 3 || (__GNUC__ == 3 && __GNUC_MINOR__ < 3))
	#error "Need GCC >= 3.3 for SSE intrinsics"
	#endif
	#include <xmmintrin.h>
	int main () {
	    _mm_setzero_ps();
	    return 0;
	}]])],[have_sse_intrinsics=yes])
	CXXFLAGS=$original_saved_CXXFLAGS

	# Inform the user if we did or did not find SSE support.
	#
	# If we enable optimization and float samples we only require SSE.
	# Disable optimizations in the SSTypes.h file if this is not the case.
	if test "x$have_sse_intrinsics" = "xyes" ; then
		echo "****** SSE support found ******"
	else
		echo "****** No SSE support found ******"
		if test "x$enable_integer_samples" != "xyes"; then
		        echo "****** Disabling optimizations. Using float samples with no SSE support ******"
		        AC_DEFINE([SOUNDTOUCH_DISABLE_X86_OPTIMIZATIONS],[1],[Do not use x86 optimizations])
		fi
	fi

else
	# Disable optimizations in SSTypes.h since the user requested it.
        echo "****** x86 optimizations disabled ******"
        AC_DEFINE([SOUNDTOUCH_DISABLE_X86_OPTIMIZATIONS],[1],[Do not use x86 optimizations])
fi

# SSTypes.h by default enables optimizations. Those already got disabled if
# the user requested for it or if the system does not support them.
#
# Now tell the Makefile.am the optimizations that are supported.
# Note:
# Makefile.am already knows if the user asked for optimizations. We apply
# optimizations by default (if support is available) and then disable all of
# them if the user requested it.
AM_CONDITIONAL([HAVE_MMX], [test "x$have_mmx_intrinsics" = "xyes"])
AM_CONDITIONAL([HAVE_SSE], [test "x$have_sse_intrinsics" = "xyes"])


dnl ############################################################################
dnl # Checks for library functions/classes                                     #
dnl ############################################################################
AC_FUNC_MALLOC
AC_TYPE_SIGNAL

dnl make -lm get added to the LIBS
AC_CHECK_LIB(m, sqrt,,AC_MSG_ERROR([compatible libc math library not found])) 

dnl add whatever functions you might want to check for here
#AC_CHECK_FUNCS([floor ftruncate memmove memset mkdir modf pow realpath sqrt strchr strdup strerror strrchr strstr strtol])







dnl ############################################################################
dnl # Internationaliation and Localiation                                    #
dnl ############################################################################
#AM_GNU_GETTEXT_VERSION([0.11.5])
#AM_GNU_GETTEXT([external])





dnl ############################################################################
dnl # Final                                                                    #
dnl ############################################################################

AC_CONFIG_FILES([
	Makefile
	source/Makefile
		source/SoundTouch/Makefile
		source/SoundStretch/Makefile
	include/Makefile
])

AC_OUTPUT(
	soundtouch.pc
)

dnl use 'echo' to put stuff here if you want a message to the builder
