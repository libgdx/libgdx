#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
AC_PREREQ(2.50)
AC_INIT( [OIS], 1.4.0 )

AC_CANONICAL_TARGET
AM_INIT_AUTOMAKE( [OIS], 1.4.0 )
AM_CONFIG_HEADER([includes/config.h])

dnl Check for programs
AC_PROG_CC
AC_PROG_CXX
AM_PROG_CC_C_O
AC_PROG_INSTALL
AM_PROG_LIBTOOL

dnl Checking for STLPort
OIS_USE_STLPORT

CFLAGS="$CFLAGS"
CXXFLAGS="$CXXFLAGS"
LIBS="$LIBS"

dnl Detect X11
AC_CHECK_HEADERS([X11/Xlib.h],, [AC_MSG_ERROR("Xlib.h not found - libx11-dev")])

dnl Added for BSD's
AC_PROG_LIBTOOL

AC_CONFIG_FILES([Makefile
                 includes/Makefile
                 src/Makefile
                 demos/Makefile
                 OIS.pc])
AC_OUTPUT
