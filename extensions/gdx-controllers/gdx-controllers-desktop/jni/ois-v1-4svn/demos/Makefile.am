INCLUDES = $(STLPORT_CFLAGS) -I$(top_srcdir)/includes $(CFLAGS) -I/usr/X11R6/include

noinst_PROGRAMS = ConsoleApp FFConsoleTest

ConsoleApp_SOURCES = OISConsole.cpp
ConsoleApp_LDFLAGS = -L$(top_builddir)/src
ConsoleApp_LDADD = -lOIS

FFConsoleTest_SOURCES = FFConsoleDemo.cpp
FFConsoleTest_LDFLAGS = -L$(top_builddir)/src
FFConsoleTest_LDADD = -lOIS
