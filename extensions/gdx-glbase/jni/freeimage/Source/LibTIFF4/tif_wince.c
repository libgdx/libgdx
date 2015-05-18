/* $Id: tif_wince.c,v 1.8 2013/11/29 22:22:01 drolon Exp $ */

/*
 * Copyright (c) 1988-1997 Sam Leffler
 * Copyright (c) 1991-1997 Silicon Graphics, Inc.
 *
 * Permission to use, copy, modify, distribute, and sell this software and 
 * its documentation for any purpose is hereby granted without fee, provided
 * that (i) the above copyright notices and this permission notice appear in
 * all copies of the software and related documentation, and (ii) the names of
 * Sam Leffler and Silicon Graphics may not be used in any advertising or
 * publicity relating to the software without the specific, prior written
 * permission of Sam Leffler and Silicon Graphics.
 * 
 * THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY 
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * IN NO EVENT SHALL SAM LEFFLER OR SILICON GRAPHICS BE LIABLE FOR
 * ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF 
 * LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 
 * OF THIS SOFTWARE.
 */

/*
 * Windows CE-specific routines for TIFF Library.
 * Adapted from tif_win32.c 01/10/2006 by Mateusz Loskot (mateusz@loskot.net)
 */

#ifndef _WIN32_WCE
# error "Only Windows CE target is supported!"
#endif

#include "tiffiop.h"
#include <windows.h>

/* Turn off console support on Windows CE. */
#undef TIF_PLATFORM_CONSOLE

COMPILATION SHOULD FAIL
This file is not yet updated to reflect changes in LibTiff 4.0. If you have
the opportunity to update and test this file, please contact LibTiff folks
for all assistance you may require and contribute the results


/*
 * Open a TIFF file for read/writing.
 */
TIFF*
TIFFOpen(const char* name, const char* mode)
{
	static const char module[] = "TIFFOpen";
	thandle_t fd;
	int m;
	DWORD dwMode;
	TIFF* tif;
    size_t nLen;
    size_t nWideLen;
    wchar_t* wchName;

	m = _TIFFgetMode(mode, module);

	switch(m)
	{
	case O_RDONLY:
		dwMode = OPEN_EXISTING;
		break;
	case O_RDWR:
		dwMode = OPEN_ALWAYS;
		break;
	case O_RDWR|O_CREAT:
		dwMode = OPEN_ALWAYS;
		break;
	case O_RDWR|O_TRUNC:
		dwMode = CREATE_ALWAYS;
		break;
	case O_RDWR|O_CREAT|O_TRUNC:
		dwMode = CREATE_ALWAYS;
		break;
	default:
		return ((TIFF*)0);
	}

    /* On Windows CE, CreateFile is mapped to CreateFileW,
     * but file path is passed as char-based string,
     * so the path has to be converted to wchar_t.
     */

    nWideLen = 0;
    wchName = NULL;
    nLen = strlen(name) + 1;
    
    nWideLen = MultiByteToWideChar(CP_ACP, 0, name, nLen, NULL, 0);
    wchName = (wchar_t*)malloc(sizeof(wchar_t) * nWideLen);
    if (NULL == wchName)
    {
        TIFFErrorExt(0, module, "Memory allocation error!");
		return ((TIFF *)0);
    }
    memset(wchName, 0, sizeof(wchar_t) * nWideLen);
    MultiByteToWideChar(CP_ACP, 0, name, nLen, wchName, nWideLen);

	fd = (thandle_t)CreateFile(wchName,
		(m == O_RDONLY)?GENERIC_READ:(GENERIC_READ | GENERIC_WRITE),
		FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, dwMode,
		(m == O_RDONLY)?FILE_ATTRIBUTE_READONLY:FILE_ATTRIBUTE_NORMAL,
		NULL);

    free(wchName);

    if (fd == INVALID_HANDLE_VALUE) {
		TIFFErrorExt(0, module, "%s: Cannot open", name);
		return ((TIFF *)0);
	}

    /* TODO - mloskot: change to TIFFdOpenW and pass wchar path */

	tif = TIFFFdOpen((int)fd, name, mode);
	if(!tif)
		CloseHandle(fd);
	return tif;
}

/*
 * Open a TIFF file with a Unicode filename, for read/writing.
 */
TIFF*
TIFFOpenW(const wchar_t* name, const char* mode)
{
	static const char module[] = "TIFFOpenW";
	thandle_t fd;
	int m;
	DWORD dwMode;
	int mbsize;
	char *mbname;
	TIFF *tif;

	m = _TIFFgetMode(mode, module);

	switch(m) {
		case O_RDONLY:			dwMode = OPEN_EXISTING; break;
		case O_RDWR:			dwMode = OPEN_ALWAYS;   break;
		case O_RDWR|O_CREAT:		dwMode = OPEN_ALWAYS;   break;
		case O_RDWR|O_TRUNC:		dwMode = CREATE_ALWAYS; break;
		case O_RDWR|O_CREAT|O_TRUNC:	dwMode = CREATE_ALWAYS; break;
		default:			return ((TIFF*)0);
	}

    /* On Windows CE, CreateFile is mapped to CreateFileW,
     * so no conversion of wchar_t to char is required.
     */

	fd = (thandle_t)CreateFile(name,
		(m == O_RDONLY)?GENERIC_READ:(GENERIC_READ|GENERIC_WRITE),
		FILE_SHARE_READ, NULL, dwMode,
		(m == O_RDONLY)?FILE_ATTRIBUTE_READONLY:FILE_ATTRIBUTE_NORMAL,
		NULL);
	if (fd == INVALID_HANDLE_VALUE) {
		TIFFErrorExt(0, module, "%S: Cannot open", name);
		return ((TIFF *)0);
	}

	mbname = NULL;
	mbsize = WideCharToMultiByte(CP_ACP, 0, name, -1, NULL, 0, NULL, NULL);
	if (mbsize > 0) {
		mbname = (char *)_TIFFmalloc(mbsize);
		if (!mbname) {
			TIFFErrorExt(0, module,
			"Can't allocate space for filename conversion buffer");
			return ((TIFF*)0);
		}

		WideCharToMultiByte(CP_ACP, 0, name, -1, mbname, mbsize,
				    NULL, NULL);
	}

	tif = TIFFFdOpen((int)fd,
			 (mbname != NULL) ? mbname : "<unknown>", mode);
	if(!tif)
		CloseHandle(fd);

	_TIFFfree(mbname);

	return tif;
}

static void
Win32WarningHandler(const char* module, const char* fmt, va_list ap)
{
    /* On Windows CE, MessageBox is mapped to wide-char based MessageBoxW. */

    size_t nWideLen = 0;
    LPTSTR szWideTitle = NULL;
    LPTSTR szWideMsg = NULL;

	LPSTR szTitle;
	LPSTR szTmp;
	LPCSTR szTitleText = "%s Warning";
	LPCSTR szDefaultModule = "LIBTIFF";
	LPCSTR szTmpModule;

	szTmpModule = (module == NULL) ? szDefaultModule : module;
	if ((szTitle = (LPSTR)LocalAlloc(LMEM_FIXED,
        (strlen(szTmpModule) + strlen(szTitleText)
        + strlen(fmt) + 128) * sizeof(char))) == NULL)
		return;

	sprintf(szTitle, szTitleText, szTmpModule);
	szTmp = szTitle + (strlen(szTitle) + 2) * sizeof(char);
	vsprintf(szTmp, fmt, ap);

    /* Convert error message to Unicode. */

    nWideLen = MultiByteToWideChar(CP_ACP, 0, szTitle, -1, NULL, 0);
    szWideTitle = (wchar_t*)malloc(sizeof(wchar_t) * nWideLen);
    MultiByteToWideChar(CP_ACP, 0, szTitle, -1, szWideTitle, nWideLen);

    nWideLen = MultiByteToWideChar(CP_ACP, 0, szTmp, -1, NULL, 0);
    szWideMsg = (wchar_t*)malloc(sizeof(wchar_t) * nWideLen);
    MultiByteToWideChar(CP_ACP, 0, szTmp, -1, szWideMsg, nWideLen);

    /* Display message */
	
    MessageBox(GetFocus(), szWideMsg, szWideTitle, MB_OK | MB_ICONEXCLAMATION);
    
    /* Free resources */

    LocalFree(szTitle);
    free(szWideMsg);
    free(szWideTitle);
}

TIFFErrorHandler _TIFFwarningHandler = Win32WarningHandler;

static void
Win32ErrorHandler(const char* module, const char* fmt, va_list ap)
{
    /* On Windows CE, MessageBox is mapped to wide-char based MessageBoxW. */

    size_t nWideLen = 0;
    LPTSTR szWideTitle = NULL;
    LPTSTR szWideMsg = NULL;

    LPSTR szTitle;
	LPSTR szTmp;
	LPCSTR szTitleText = "%s Error";
	LPCSTR szDefaultModule = "LIBTIFF";
	LPCSTR szTmpModule;

	szTmpModule = (module == NULL) ? szDefaultModule : module;
	if ((szTitle = (LPSTR)LocalAlloc(LMEM_FIXED,
        (strlen(szTmpModule) + strlen(szTitleText)
        + strlen(fmt) + 128) * sizeof(char))) == NULL)
		return;

	sprintf(szTitle, szTitleText, szTmpModule);
	szTmp = szTitle + (strlen(szTitle) + 2) * sizeof(char);
	vsprintf(szTmp, fmt, ap);

    /* Convert error message to Unicode. */

    nWideLen = MultiByteToWideChar(CP_ACP, 0, szTitle, -1, NULL, 0);
    szWideTitle = (wchar_t*)malloc(sizeof(wchar_t) * nWideLen);
    MultiByteToWideChar(CP_ACP, 0, szTitle, -1, szWideTitle, nWideLen);

    nWideLen = MultiByteToWideChar(CP_ACP, 0, szTmp, -1, NULL, 0);
    szWideMsg = (wchar_t*)malloc(sizeof(wchar_t) * nWideLen);
    MultiByteToWideChar(CP_ACP, 0, szTmp, -1, szWideMsg, nWideLen);

    /* Display message */

	MessageBox(GetFocus(), szWideMsg, szWideTitle, MB_OK | MB_ICONEXCLAMATION);

    /* Free resources */

    LocalFree(szTitle);
    free(szWideMsg);
    free(szWideTitle);
}

TIFFErrorHandler _TIFFerrorHandler = Win32ErrorHandler;


/* vim: set ts=8 sts=8 sw=8 noet: */
/*
 * Local Variables:
 * mode: c
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */
