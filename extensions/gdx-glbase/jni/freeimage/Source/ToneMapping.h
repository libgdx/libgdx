// ==========================================================
// High Dynamic Range bitmap conversion routines
//
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
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

#ifndef TONE_MAPPING_H
#define TONE_MAPPING_H

#ifdef __cplusplus
extern "C" {
#endif

BOOL ConvertInPlaceRGBFToYxy(FIBITMAP *dib);
BOOL ConvertInPlaceYxyToRGBF(FIBITMAP *dib);
FIBITMAP* ConvertRGBFToY(FIBITMAP *src);

BOOL LuminanceFromYxy(FIBITMAP *dib, float *maxLum, float *minLum, float *worldLum);
BOOL LuminanceFromY(FIBITMAP *dib, float *maxLum, float *minLum, float *Lav, float *Llav);

void NormalizeY(FIBITMAP *Y, float minPrct, float maxPrct);

FIBITMAP* ClampConvertRGBFTo24(FIBITMAP *src);

#ifdef __cplusplus
}
#endif

#endif // TONE_MAPPING_H
