#
# FreeType 2 configuration file to detect an OS/2 host platform.
#


# Copyright 1996-2015 by
# David Turner, Robert Wilhelm, and Werner Lemberg.
#
# This file is part of the FreeType project, and may only be used, modified,
# and distributed under the terms of the FreeType project license,
# LICENSE.TXT.  By continuing to use, modify, or distribute this file you
# indicate that you have read the license and understand and accept it
# fully.


.PHONY: setup


ifeq ($(PLATFORM),ansi)

  ifdef OS2_SHELL

    PLATFORM := os2

  endif # test OS2_SHELL
endif

ifeq ($(PLATFORM),os2)

  COPY   := copy
  DELETE := del
  CAT    := type
  SEP    := $(BACKSLASH)

  # gcc-emx by default
  CONFIG_FILE := os2-gcc.mk

  # additionally, we provide hooks for various other compilers
  #
  ifneq ($(findstring visualage,$(MAKECMDGOALS)),)     # Visual Age C++
    CONFIG_FILE := os2-icc.mk
    CC          := icc
    visualage: setup
    .PHONY: visualage
  endif

  ifneq ($(findstring watcom,$(MAKECMDGOALS)),)        # Watcom C/C++
    CONFIG_FILE := os2-wat.mk
    CC          := wcc386
    watcom: setup
    .PHONY: watcom
  endif

  ifneq ($(findstring borlandc,$(MAKECMDGOALS)),)      # Borland C++ 32-bit
    CONFIG_FILE := os2-bcc.mk
    CC          := bcc32
    borlandc: setup
    .PHONY: borlandc
  endif

  ifneq ($(findstring devel,$(MAKECMDGOALS)),)         # development target
    CONFIG_FILE := os2-dev.mk
    CC          := gcc
    devel: setup
    .PHONY: devel
  endif

  setup: dos_setup

endif   # test PLATFORM os2


# EOF
