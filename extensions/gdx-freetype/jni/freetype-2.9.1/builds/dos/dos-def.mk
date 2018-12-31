#
# FreeType 2 DOS specific definitions
#


# Copyright 1996-2018 by
# David Turner, Robert Wilhelm, and Werner Lemberg.
#
# This file is part of the FreeType project, and may only be used, modified,
# and distributed under the terms of the FreeType project license,
# LICENSE.TXT.  By continuing to use, modify, or distribute this file you
# indicate that you have read the license and understand and accept it
# fully.


DELETE    := del
CAT       := type
SEP       := $(strip \ )
BUILD_DIR := $(TOP_DIR)/builds/dos
PLATFORM  := dos


# The executable file extension (for tools), *with* leading dot.
#
E := .exe

# The directory where all library files are placed.
#
# By default, this is the same as $(OBJ_DIR); however, this can be changed
# to suit particular needs.
#
LIB_DIR := $(OBJ_DIR)

# The name of the final library file.  Note that the DOS-specific Makefile
# uses a shorter (8.3) name.
#
LIBRARY := $(PROJECT)


# The NO_OUTPUT macro is used to ignore the output of commands.
#
NO_OUTPUT = > nul


# EOF
