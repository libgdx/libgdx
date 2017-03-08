///@file error message utility functions
#ifndef IDUTILS_HPP_
#define IDUTILS_HPP_
#include <cstring>
/// name of file being compiled, without leading path components
#define __INVDYN_FILE_WO_DIR__ (strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 : __FILE__)

#ifndef BT_ID_WO_BULLET
#include "Bullet3Common/b3Logging.h"
#define error_message(...) b3Error(__VA_ARGS__)
#define warning_message(...) b3Warning(__VA_ARGS__)
#define id_printf(...) b3Printf(__VA_ARGS__)
#else  // BT_ID_WO_BULLET
#include <cstdio>
/// print error message with file/line information
#define error_message(...)																		 \
	do {																						   \
		fprintf(stderr, "[Error:%s:%d] ", __INVDYN_FILE_WO_DIR__, __LINE__);					   \
		fprintf(stderr, __VA_ARGS__);															  \
	} while (0)
/// print warning message with file/line information
#define warning_message(...)																	   \
	do {																						   \
		fprintf(stderr, "[Warning:%s:%d] ", __INVDYN_FILE_WO_DIR__, __LINE__);					 \
		fprintf(stderr, __VA_ARGS__);															  \
	} while (0)
#define warning_message(...)																		 \
	do {																						   \
		fprintf(stderr, "[Warning:%s:%d] ", __INVDYN_FILE_WO_DIR__, __LINE__);					   \
		fprintf(stderr, __VA_ARGS__);															\
	} while (0)
#define id_printf(...) printf(__VA_ARGS__)
#endif  // BT_ID_WO_BULLET
#endif
