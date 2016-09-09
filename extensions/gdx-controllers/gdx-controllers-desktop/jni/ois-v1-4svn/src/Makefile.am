INCLUDES = $(STLPORT_CFLAGS) -I$(top_srcdir)/includes $(CFLAGS) -I/usr/X11R6/include



lib_LTLIBRARIES=libOIS.la
libOIS_la_SOURCES = OISInputManager.cpp \
	OISObject.cpp \
	OISEffect.cpp \
	OISJoyStick.cpp \
	OISKeyboard.cpp \
	OISForceFeedback.cpp \
	OISException.cpp \
	./linux/EventHelpers.cpp \
	./linux/LinuxInputManager.cpp \
        ./linux/LinuxJoyStickEvents.cpp \
        ./linux/LinuxForceFeedback.cpp \
	./linux/LinuxKeyboard.cpp \
	./linux/LinuxMouse.cpp

libOIS_la_LDFLAGS = -release @PACKAGE_VERSION@
#libOIS_la_LDFLAGS = -version-info $(shell echo "@PACKAGE_VERSION@" | tr '.' ':')

libOIS_la_LIBADD = $(STLPORT_LIBS) -L/usr/X11R6/lib -lX11

#eof "$Id: Makefile.am,v 1.15.2.1 2008/02/14 03:33:36 pjcast Exp $"
