#
# FreeType 2 installation instructions for Unix systems
#


# Copyright 1996-2000, 2002, 2003, 2006, 2013, 2014 by
# David Turner, Robert Wilhelm, and Werner Lemberg.
#
# This file is part of the FreeType project, and may only be used, modified,
# and distributed under the terms of the FreeType project license,
# LICENSE.TXT.  By continuing to use, modify, or distribute this file you
# indicate that you have read the license and understand and accept it
# fully.

# If you say
#
#   make install DESTDIR=/tmp/somewhere/
#
# don't forget the final backslash (this command is mainly for package
# maintainers).


.PHONY: install uninstall check

# Unix installation and deinstallation targets.
#
# Note that we remove any data in the `freetype' subdirectory found in
# `$(includedir)/freetype2', which was the previous location of the header
# files up to version 2.5.0.
#
install: $(PROJECT_LIBRARY)
	$(MKINSTALLDIRS) $(DESTDIR)$(libdir)                      \
                         $(DESTDIR)$(libdir)/pkgconfig            \
                         $(DESTDIR)$(includedir)/freetype2/config \
                         $(DESTDIR)$(bindir)                      \
                         $(DESTDIR)$(datadir)/aclocal             \
                         $(DESTDIR)$(mandir)/man1
	$(LIBTOOL) --mode=install $(INSTALL)                             \
                                  $(PROJECT_LIBRARY) $(DESTDIR)$(libdir)
	-for P in $(PUBLIC_H) ; do                  \
          $(INSTALL_DATA)                           \
            $$P $(DESTDIR)$(includedir)/freetype2 ; \
        done
	-for P in $(CONFIG_H) ; do                         \
          $(INSTALL_DATA)                                  \
            $$P $(DESTDIR)$(includedir)/freetype2/config ; \
        done
	-$(DELETE) $(DESTDIR)$(includedir)/freetype2/freetype/config/*
	-$(DELDIR) $(DESTDIR)$(includedir)/freetype2/freetype/config
	-$(DELETE) $(DESTDIR)$(includedir)/freetype2/freetype/*
	-$(DELDIR) $(DESTDIR)$(includedir)/freetype2/freetype
	$(INSTALL_DATA) $(OBJ_BUILD)/ftconfig.h               \
          $(DESTDIR)$(includedir)/freetype2/config/ftconfig.h
	$(INSTALL_DATA) $(OBJ_DIR)/ftmodule.h                 \
          $(DESTDIR)$(includedir)/freetype2/config/ftmodule.h
	$(INSTALL_SCRIPT) -m 755 $(OBJ_BUILD)/freetype-config \
          $(DESTDIR)$(bindir)/freetype-config
	$(INSTALL_SCRIPT) -m 644 $(BUILD_DIR)/freetype2.m4 \
          $(DESTDIR)$(datadir)/aclocal/freetype2.m4
	$(INSTALL_SCRIPT) -m 644 $(OBJ_BUILD)/freetype2.pc \
          $(DESTDIR)$(libdir)/pkgconfig/freetype2.pc
	$(INSTALL_DATA) $(TOP_DIR)/docs/freetype-config.1 \
          $(DESTDIR)$(mandir)/man1/freetype-config.1


uninstall:
	-$(LIBTOOL) --mode=uninstall $(RM) $(DESTDIR)$(libdir)/$(LIBRARY).$A
	-$(DELETE) $(DESTDIR)$(includedir)/freetype2/config/*
	-$(DELDIR) $(DESTDIR)$(includedir)/freetype2/config
	-$(DELETE) $(DESTDIR)$(includedir)/freetype2/*
	-$(DELDIR) $(DESTDIR)$(includedir)/freetype2
	-$(DELETE) $(DESTDIR)$(bindir)/freetype-config
	-$(DELETE) $(DESTDIR)$(datadir)/aclocal/freetype2.m4
	-$(DELETE) $(DESTDIR)$(libdir)/pkgconfig/freetype2.pc
	-$(DELETE) $(DESTDIR)$(mandir)/man1/freetype-config.1


check:
	@echo There is no validation suite for this package.


.PHONY: clean_project_unix distclean_project_unix

# Unix cleaning and distclean rules.
#
clean_project_unix:
	-$(DELETE) $(BASE_OBJECTS) $(OBJ_M) $(OBJ_S)
	-$(DELETE) $(patsubst %.$O,%.$(SO),$(BASE_OBJECTS) $(OBJ_M) $(OBJ_S)) \
                   $(CLEAN)

distclean_project_unix: clean_project_unix
	-$(DELETE) $(PROJECT_LIBRARY)
	-$(DELETE) $(OBJ_DIR)/.libs/*
	-$(DELDIR) $(OBJ_DIR)/.libs
	-$(DELETE) *.orig *~ core *.core $(DISTCLEAN)

# EOF
