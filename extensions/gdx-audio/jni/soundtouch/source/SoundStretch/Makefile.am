## Process this file with automake to create Makefile.in
##
## $Id: Makefile.am 128 2011-07-17 10:59:56Z oparviai $
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

include $(top_srcdir)/config/am_include.mk


## bin_PROGRAMS is the macro that tells automake the name of the programs to
## install in the bin directory (/usr/local/bin) by default. By setting
## --prefix= at configure time the user can change this (eg: ./configure
## --prefix=/usr will install soundstretch under /usr/bin/soundstretch )
bin_PROGRAMS=soundstretch

noinst_HEADERS=RunParameters.h WavFile.h

# extra files to include in distrubution tarball
EXTRA_DIST=soundstretch.dsp soundstretch.dsw soundstretch.sln soundstretch.vcproj

## for every name listed under bin_PROGRAMS, you have a <prog>_SOURCES. This lists
## all the sources in the current directory that are used to build soundstretch.
soundstretch_SOURCES=main.cpp RunParameters.cpp WavFile.cpp 

## soundstretch_LDADD is a list of extras to pass at link time. All the objects
## created by the above soundstretch_SOURCES are automatically linked in, so here I
## list object files from other directories as well as flags passed to the
## linker. 
soundstretch_LDADD=../SoundTouch/libSoundTouch.la -lm

## linker flags. 
# OP 2011-7-17 Linker flags disabled to prevent stripping symbols by default
# soundstretch_LDFLAGS=-s

## additional compiler flags
soundstretch_CXXFLAGS=-O3

#clean-local: 
#	-rm -f additional-files-to-remove-on-make-clean
