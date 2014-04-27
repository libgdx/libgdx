#include "Utilities.h"

#include <stdio.h>
#include <cstdarg>

using namespace std;

//Default no debug logging
bool debugLoggingActive = false;

void logDebug(const char* format, ...)
{
	if(debugLoggingActive)
	{
		va_list argptr;
		va_start(argptr, format);
		vfprintf(stderr, format, argptr);
		va_end(argptr);
	}
}

void logError(const char* format, ...)
{
    va_list argptr;
    va_start(argptr, format);
    vfprintf(stderr, format, argptr);
    va_end(argptr);
}

msec_t currentTimeMillis() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (msec_t)tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

void debug(bool debug) {
    debugLoggingActive = debug;
}
