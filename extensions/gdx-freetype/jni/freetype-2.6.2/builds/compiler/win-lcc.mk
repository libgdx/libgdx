#
# FreeType 2 Win32-LCC specific definitions
#


# Copyright 1996-2015 by
# David Turner, Robert Wilhelm, and Werner Lemberg.
#
# This file is part of the FreeType project, and may only be used, modified,
# and distributed under the terms of the FreeType project license,
# LICENSE.TXT.  By continuing to use, modify, or distribute this file you
# indicate that you have read the license and understand and accept it
# fully.


# Command line name
#
CC           := lcc
COMPILER_SEP := $(SEP)


# The object file extension (for standard and static libraries).  This can be
# .o, .tco, .obj, etc., depending on the platform.
#
O  := obj
SO := obj


# The library file extension (for standard and static libraries).  This can
# be .a, .lib, etc., depending on the platform.
#
A  := lib
SA := lib


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
L := -Fl


# Target flag.
#
T := -Fo


# C flags
#
#   These should concern: debug output, optimization & warnings.
#
#   Use the ANSIFLAGS variable to define the compiler flags used to enfore
#   ANSI compliance.
#
CFLAGS ?= -c -g2 -O

# ANSIFLAGS: Put there the flags used to make your compiler ANSI-compliant.
#
#  LCC is pure ANSI anyway!
#
ANSIFLAGS :=


# library linking
#
#CLEAN_LIBRARY :=
LINK_LIBRARY = lcclib /out:$(subst /,$(COMPILER_SEP),$@ $(OBJECTS_LIST))


# EOF
