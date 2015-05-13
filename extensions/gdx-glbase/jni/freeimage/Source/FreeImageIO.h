// ==========================================================
// Input/Output functions
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
//
// This file is part of FreeImage 3
//
// COVERED CODE IS PROVIDED UNDER THIS LICENSE ON AN "AS IS" BASIS, WITHOUT WARRANTY
// OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
// THAT THE COVERED CODE IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE
// OR NON-INFRINGING. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE COVERED
// CODE IS WITH YOU. SHOULD ANY COVERED CODE PROVE DEFECTIVE IN ANY RESPECT, YOU (NOT
// THE INITIAL DEVELOPER OR ANY OTHER CONTRIBUTOR) ASSUME THE COST OF ANY NECESSARY
// SERVICING, REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL
// PART OF THIS LICENSE. NO USE OF ANY COVERED CODE IS AUTHORIZED HEREUNDER EXCEPT UNDER
// THIS DISCLAIMER.
//
// Use at your own risk!
// ==========================================================

#ifndef FREEIMAGEIO_H
#define FREEIMAGEIO_H

#ifndef FREEIMAGE_H
#include "FreeImage.h"
#endif

// ----------------------------------------------------------

FI_STRUCT (FIMEMORYHEADER) {
	/// remember to delete the buffer
	BOOL delete_me;
	/// file length
	long filelen;
	/// buffer size
	long datalen;
	/// current position
	long curpos;
	/// start buffer address
	void *data;
};

void SetDefaultIO(FreeImageIO *io);

void SetMemoryIO(FreeImageIO *io);

#endif // !FREEIMAGEIO_H
