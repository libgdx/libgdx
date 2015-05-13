## Process this file with automake to produce Makefile.in

INCLUDES = -I$(top_srcdir)/config

lib_LTLIBRARIES = libIex.la

libIex_la_SOURCES = IexThrowErrnoExc.cpp IexBaseExc.cpp IexBaseExc.h \
		    IexErrnoExc.h Iex.h IexMacros.h IexMathExc.h \
		    IexThrowErrnoExc.h

libIex_la_LDFLAGS = -version-info @LIBTOOL_VERSION@ -no-undefined

libIexincludedir = $(includedir)/OpenEXR

libIexinclude_HEADERS = IexBaseExc.h IexMathExc.h IexThrowErrnoExc.h \
			IexErrnoExc.h IexMacros.h Iex.h
