## Process this file with automake to create Makefile.in
##
## $Id: Makefile.am 89 2010-04-03 15:30:08Z oparviai $
##
## Copyright (C) 2003 - David W. Durham
## 
## This file is part of SoundTouch, an audio processing library for pitch/time adjustments
## 
## SoundTouch is free software; you can redistribute it and/or modify it under the
## terms of the GNU General Public License as published by the Free Software
## Foundation; either version 2 of the License, or (at your option) any later
## version.
## 
## SoundTouch is distributed in the hope that it will be useful, but WITHOUT ANY
## WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
## A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
## 
## You should have received a copy of the GNU General Public License along with
## this program; if not, write to the Free Software Foundation, Inc., 59 Temple
## Place - Suite 330, Boston, MA  02111-1307, USA

## I used config/am_include.mk for common definitions
include $(top_srcdir)/config/am_include.mk

## Descend into SUBDIRS and run make. Look at the Makefile.am files in the
## subdirectories Start at frontend_fox/Makefile.am to see how everything works.
SUBDIRS=include source


# list files that are documentation to be packaged in a release tarball and installed
pkgdoc_DATA=COPYING.TXT README.html

# extra data files that are to be pacakged in a release tarball and installed into the data directory
#pkgdata_DATA=

# sets up for soundtouch.m4 to be installed
m4datadir=$(datadir)/aclocal
m4data_DATA=soundtouch.m4

## These extra files and directories will be included in the distribution. by
## using make-dist by default many common filenames are automatically included
## such as AUTHORS, COPYING, etc the bootstrap script really shouldn't be a part
## of a final package, but it is useful for developers who might want to make
## changes to the configure scripts or makefiles.
# NOTE: wouldn't have to list the .TXT file if it were named without the .TXT
EXTRA_DIST=                  \
       soundtouch.m4        \
       config/m4            \
       bootstrap            \
       make-win.bat         \
       COPYING.TXT          \
       README.html

pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = soundtouch.pc

## This removes stuff from the distribution which may be present
## from a cvs checkout or other build reasons
dist-hook:
	rm -rf `find $(distdir) -type d -name CVS`	# remove all CVS directories
	echo rm -rf `find $(distdir) -type f -name \.\#\*`	# CVS revision files left around for some reason

## This happens at 'make distclean'
#distcleancheck:
#	rm -rf files-that-may-also-need-to-be-deleted-on-'make distclean'


# flag to aclocal where to find m4 macros for tests
ACLOCAL_AMFLAGS = -I config/m4
AUTOMAKE_OPTIONS = foreign
