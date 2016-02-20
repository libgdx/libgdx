#
# FreeType 2 configuration rules for the OS/2 + gcc
#


# Copyright 1996-2015 by
# David Turner, Robert Wilhelm, and Werner Lemberg.
#
# This file is part of the FreeType project, and may only be used, modified,
# and distributed under the terms of the FreeType project license,
# LICENSE.TXT.  By continuing to use, modify, or distribute this file you
# indicate that you have read the license and understand and accept it
# fully.


# include OS/2-specific definitions
include $(TOP_DIR)/builds/os2/os2-def.mk

# include gcc-specific definitions
include $(TOP_DIR)/builds/compiler/gcc.mk

# include linking instructions
include $(TOP_DIR)/builds/link_dos.mk


# EOF
