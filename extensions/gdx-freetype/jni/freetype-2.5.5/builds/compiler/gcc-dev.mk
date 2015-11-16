#
# FreeType 2 gcc-specific with NO OPTIMIZATIONS + DEBUGGING
#


# Copyright 1996-2000, 2003, 2004, 2005, 2006 by
# David Turner, Robert Wilhelm, and Werner Lemberg.
#
# This file is part of the FreeType project, and may only be used, modified,
# and distributed under the terms of the FreeType project license,
# LICENSE.TXT.  By continuing to use, modify, or distribute this file you
# indicate that you have read the license and understand and accept it
# fully.


# Compiler command line name
#
CC           := gcc
COMPILER_SEP := /


# The object file extension (for standard and static libraries).  This can be
# .o, .tco, .obj, etc., depending on the platform.
#
O  := o
SO := o

# The library file extension (for standard and static libraries).  This can
# be .a, .lib, etc., depending on the platform.
#
A  := a
SA := a


# Path inclusion flag.  Some compilers use a different flag than `-I' to
# specify an additional include path.  Examples are `/i=' or `-J'.
#
I := -I


# C flag used to define a macro before the compilation of a given source
# object.  Usually it is `-D' like in `-DDEBUG'.
#
D := -D


# The link flag used to specify a given library file on link.  Note that
# this is only used to compile the demo programs, not the library itself.
#
L := -l


# Target flag.
#
T := -o$(space)


# C flags
#
#   These should concern: debug output, optimization & warnings.
#
#   Use the ANSIFLAGS variable to define the compiler flags used to enfore
#   ANSI compliance.
#
ifndef CFLAGS
  ifeq ($(findstring g++,$(CC)),)
    nested_externs    := -Wnested-externs
    strict_prototypes := -Wstrict-prototypes
  endif

  CFLAGS := -c -g -O0 \
            -Wall \
            -W \
            -Wundef \
            -Wshadow \
            -Wpointer-arith \
            -Wwrite-strings \
            -Wredundant-decls \
            -Wno-long-long \
            $(nested_externs) \
            $(strict_prototypes)
endif

# ANSIFLAGS: Put there the flags used to make your compiler ANSI-compliant.
#
ANSIFLAGS := -ansi -pedantic


# Library linking
#
CLEAN_LIBRARY ?= $(DELETE) $(subst /,$(SEP),$(PROJECT_LIBRARY))
LINK_LIBRARY   = $(AR) -r $@ $(OBJECTS_LIST)


# EOF
