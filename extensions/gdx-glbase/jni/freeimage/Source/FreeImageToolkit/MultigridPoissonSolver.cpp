// ==========================================================
// Poisson solver based on a full multigrid algorithm
//
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
// Reference:
// PRESS, W. H., TEUKOLSKY, S. A., VETTERLING, W. T., AND FLANNERY, B. P.
// 1992. Numerical Recipes in C: The Art of Scientific Computing, 2nd ed. Cambridge University Press.
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

#include "FreeImage.h"
#include "Utilities.h"
#include "ToneMapping.h"

static const int NPRE	= 1;		// Number of relaxation sweeps before ...
static const int NPOST	= 1;		// ... and after the coarse-grid correction is computed
static const int NGMAX	= 15;		// Maximum number of grids

/**
Copy src into dst
*/
static inline void fmg_copyArray(FIBITMAP *dst, FIBITMAP *src) {
	memcpy(FreeImage_GetBits(dst), FreeImage_GetBits(src), FreeImage_GetHeight(dst) * FreeImage_GetPitch(dst));
}

/**
Fills src with zeros
*/
static inline void fmg_fillArrayWithZeros(FIBITMAP *src) {
	memset(FreeImage_GetBits(src), 0, FreeImage_GetHeight(src) * FreeImage_GetPitch(src));
}

/**
Half-weighting restriction. nc is the coarse-grid dimension. The fine-grid solution is input in
uf[0..2*nc-2][0..2*nc-2], the coarse-grid solution is returned in uc[0..nc-1][0..nc-1].
*/
static void fmg_restrict(FIBITMAP *UC, FIBITMAP *UF, int nc) {
	int row_uc, row_uf, col_uc, col_uf;

	const int uc_pitch  = FreeImage_GetPitch(UC) / sizeof(float);
	const int uf_pitch  = FreeImage_GetPitch(UF) / sizeof(float);
	
	float *uc_bits = (float*)FreeImage_GetBits(UC);
	const float *uf_bits = (float*)FreeImage_GetBits(UF);

	// interior points
	{
		float *uc_scan = uc_bits + uc_pitch;
		for (row_uc = 1, row_uf = 2; row_uc < nc-1; row_uc++, row_uf += 2) {
			const float *uf_scan = uf_bits + row_uf * uf_pitch;
			for (col_uc = 1, col_uf = 2; col_uc < nc-1; col_uc++, col_uf += 2) { 
				// calculate 
				// UC(row_uc, col_uc) = 
				// 0.5 * UF(row_uf, col_uf) + 0.125 * [ UF(row_uf+1, col_uf) + UF(row_uf-1, col_uf) + UF(row_uf, col_uf+1) + UF(row_uf, col_uf-1) ]
				float *uc_pixel = uc_scan + col_uc;
				const float *uf_center = uf_scan + col_uf;
				*uc_pixel = 0.5F * *uf_center + 0.125F * ( *(uf_center + uf_pitch) + *(uf_center - uf_pitch) + *(uf_center + 1) + *(uf_center - 1) );
			}
			uc_scan += uc_pitch;
		}
	}
	// boundary points
	const int ncc = 2*nc-1;
	{
		/*
		calculate the following: 
		for (row_uc = 0, row_uf = 0; row_uc < nc; row_uc++, row_uf += 2) { 
			UC(row_uc, 0) = UF(row_uf, 0);		
			UC(row_uc, nc-1) = UF(row_uf, ncc-1);
		}
		*/
		float *uc_scan = uc_bits;
		for (row_uc = 0, row_uf = 0; row_uc < nc; row_uc++, row_uf += 2) { 
			const float *uf_scan = uf_bits + row_uf * uf_pitch;
			uc_scan[0] = uf_scan[0];
			uc_scan[nc-1] = uf_scan[ncc-1];
			uc_scan += uc_pitch;
		}
	}
	{
		/*
		calculate the following: 
		for (col_uc = 0, col_uf = 0; col_uc < nc; col_uc++, col_uf += 2) {
			UC(0, col_uc) = UF(0, col_uf);
			UC(nc-1, col_uc) = UF(ncc-1, col_uf);
		}
		*/
		float *uc_scan_top = uc_bits;
		float *uc_scan_bottom = uc_bits + (nc-1)*uc_pitch;
		const float *uf_scan_top = uf_bits + (ncc-1)*uf_pitch;
		const float *uf_scan_bottom = uf_bits;
		for (col_uc = 0, col_uf = 0; col_uc < nc; col_uc++, col_uf += 2) {
			uc_scan_top[col_uc] = uf_scan_top[col_uf];
			uc_scan_bottom[col_uc] = uf_scan_bottom[col_uf];
		}
	}
}

/**
Solution of the model problem on the coarsest grid, where h = 1/2 . 
The right-hand side is input
in rhs[0..2][0..2] and the solution is returned in u[0..2][0..2].
*/
static void fmg_solve(FIBITMAP *U, FIBITMAP *RHS) {
	// fill U with zeros
	fmg_fillArrayWithZeros(U);
	// calculate U(1, 1) = -h*h*RHS(1, 1)/4.0 where h = 1/2
	float *u_scan = (float*)FreeImage_GetScanLine(U, 1);
	const float *rhs_scan = (float*)FreeImage_GetScanLine(RHS, 1);
	u_scan[1] = -rhs_scan[1] / 16;
}

/**
Coarse-to-fine prolongation by bilinear interpolation. nf is the fine-grid dimension. The coarsegrid
solution is input as uc[0..nc-1][0..nc-1], where nc = nf/2 + 1. The fine-grid solution is
returned in uf[0..nf-1][0..nf-1].
*/
static void fmg_prolongate(FIBITMAP *UF, FIBITMAP *UC, int nf) {
	int row_uc, row_uf, col_uc, col_uf;

	const int uf_pitch  = FreeImage_GetPitch(UF) / sizeof(float);
	const int uc_pitch  = FreeImage_GetPitch(UC) / sizeof(float);
	
	float *uf_bits = (float*)FreeImage_GetBits(UF);
	const float *uc_bits = (float*)FreeImage_GetBits(UC);
	
	// do elements that are copies
	{
		const int nc = nf/2 + 1;

		float *uf_scan = uf_bits;
		const float *uc_scan = uc_bits;		
		for (row_uc = 0; row_uc < nc; row_uc++) {
			for (col_uc = 0, col_uf = 0; col_uc < nc; col_uc++, col_uf += 2) {
				// calculate UF(2*row_uc, col_uf) = UC(row_uc, col_uc);
				uf_scan[col_uf] = uc_scan[col_uc];
			}
			uc_scan += uc_pitch;
			uf_scan += 2 * uf_pitch;
		}
	}
	// do odd-numbered columns, interpolating vertically
	{		
		for(row_uf = 1; row_uf < nf-1; row_uf += 2) {
			float *uf_scan = uf_bits + row_uf * uf_pitch;
			for (col_uf = 0; col_uf < nf; col_uf += 2) {
				// calculate UF(row_uf, col_uf) = 0.5 * ( UF(row_uf+1, col_uf) + UF(row_uf-1, col_uf) )
				uf_scan[col_uf] = 0.5F * ( *(uf_scan + uf_pitch + col_uf) + *(uf_scan - uf_pitch + col_uf) );
			}
		}
	}
	// do even-numbered columns, interpolating horizontally
	{
		float *uf_scan = uf_bits;
		for(row_uf = 0; row_uf < nf; row_uf++) {
			for (col_uf = 1; col_uf < nf-1; col_uf += 2) {
				// calculate UF(row_uf, col_uf) = 0.5 * ( UF(row_uf, col_uf+1) + UF(row_uf, col_uf-1) )
				uf_scan[col_uf] = 0.5F * ( uf_scan[col_uf + 1] + uf_scan[col_uf - 1] );
			}
			uf_scan += uf_pitch;
		}
	}
}

/**
Red-black Gauss-Seidel relaxation for model problem. Updates the current value of the solution
u[0..n-1][0..n-1], using the right-hand side function rhs[0..n-1][0..n-1].
*/
static void fmg_relaxation(FIBITMAP *U, FIBITMAP *RHS, int n) {
	int row, col, ipass, isw, jsw;
	const float h = 1.0F / (n - 1);
	const float h2 = h*h;

	const int u_pitch  = FreeImage_GetPitch(U) / sizeof(float);
	const int rhs_pitch  = FreeImage_GetPitch(RHS) / sizeof(float);
	
	float *u_bits = (float*)FreeImage_GetBits(U);
	const float *rhs_bits = (float*)FreeImage_GetBits(RHS);

	for (ipass = 0, jsw = 1; ipass < 2; ipass++, jsw = 3-jsw) { // Red and black sweeps
		float *u_scan = u_bits + u_pitch;
		const float *rhs_scan = rhs_bits + rhs_pitch;
		for (row = 1, isw = jsw; row < n-1; row++, isw = 3-isw) {
			for (col = isw; col < n-1; col += 2) { 
				// Gauss-Seidel formula
				// calculate U(row, col) = 
				// 0.25 * [ U(row+1, col) + U(row-1, col) + U(row, col+1) + U(row, col-1) - h2 * RHS(row, col) ]		 
				float *u_center = u_scan + col;
				const float *rhs_center = rhs_scan + col;
				*u_center = *(u_center + u_pitch) + *(u_center - u_pitch) + *(u_center + 1) + *(u_center - 1);
				*u_center -= h2 * *rhs_center;
				*u_center *= 0.25F;
			}
			u_scan += u_pitch;
			rhs_scan += rhs_pitch;
		}
	}
}

/**
Returns minus the residual for the model problem. Input quantities are u[0..n-1][0..n-1] and
rhs[0..n-1][0..n-1], while res[0..n-1][0..n-1] is returned.
*/
static void fmg_residual(FIBITMAP *RES, FIBITMAP *U, FIBITMAP *RHS, int n) {
	int row, col;

	const float h = 1.0F / (n-1);	
	const float h2i = 1.0F / (h*h);

	const int res_pitch  = FreeImage_GetPitch(RES) / sizeof(float);
	const int u_pitch  = FreeImage_GetPitch(U) / sizeof(float);
	const int rhs_pitch  = FreeImage_GetPitch(RHS) / sizeof(float);
	
	float *res_bits = (float*)FreeImage_GetBits(RES);
	const float *u_bits = (float*)FreeImage_GetBits(U);
	const float *rhs_bits = (float*)FreeImage_GetBits(RHS);

	// interior points
	{
		float *res_scan = res_bits + res_pitch;
		const float *u_scan = u_bits + u_pitch;
		const float *rhs_scan = rhs_bits + rhs_pitch;
		for (row = 1; row < n-1; row++) {
			for (col = 1; col < n-1; col++) {
				// calculate RES(row, col) = 
				// -h2i * [ U(row+1, col) + U(row-1, col) + U(row, col+1) + U(row, col-1) - 4 * U(row, col) ] + RHS(row, col);
				float *res_center = res_scan + col;
				const float *u_center = u_scan + col;
				const float *rhs_center = rhs_scan + col;
				*res_center = *(u_center + u_pitch) + *(u_center - u_pitch) + *(u_center + 1) + *(u_center - 1) - 4 * *u_center;
				*res_center *= -h2i;
				*res_center += *rhs_center;
			}
			res_scan += res_pitch;
			u_scan += u_pitch;
			rhs_scan += rhs_pitch;
		}
	}

	// boundary points
	{
		memset(FreeImage_GetScanLine(RES, 0), 0, FreeImage_GetPitch(RES));
		memset(FreeImage_GetScanLine(RES, n-1), 0, FreeImage_GetPitch(RES));
		float *left = res_bits;
		float *right = res_bits + (n-1);
		for(int k = 0; k < n; k++) {
			*left = 0;
			*right = 0;
			left += res_pitch;
			right += res_pitch;
		}
	}
}

/**
Does coarse-to-fine interpolation and adds result to uf. nf is the fine-grid dimension. The
coarse-grid solution is input as uc[0..nc-1][0..nc-1], where nc = nf/2+1. The fine-grid solution
is returned in uf[0..nf-1][0..nf-1]. res[0..nf-1][0..nf-1] is used for temporary storage.
*/
static void fmg_addint(FIBITMAP *UF, FIBITMAP *UC, FIBITMAP *RES, int nf) {
	fmg_prolongate(RES, UC, nf);

	const int uf_pitch  = FreeImage_GetPitch(UF) / sizeof(float);
	const int res_pitch  = FreeImage_GetPitch(RES) / sizeof(float);	

	float *uf_bits = (float*)FreeImage_GetBits(UF);
	const float *res_bits = (float*)FreeImage_GetBits(RES);

	for(int row = 0; row < nf; row++) {
		for(int col = 0; col < nf; col++) {
			// calculate UF(row, col) = UF(row, col) + RES(row, col);
			uf_bits[col] += res_bits[col];
		}
		uf_bits += uf_pitch;
		res_bits += res_pitch;
	}
}

/**
Full Multigrid Algorithm for solution of linear elliptic equation, here the model problem (19.0.6).
On input u[0..n-1][0..n-1] contains the right-hand side ñ, while on output it returns the solution.
The dimension n must be of the form 2^j + 1 for some integer j. (j is actually the number of
grid levels used in the solution, called ng below.) ncycle is the number of V-cycles to be
used at each level.
*/
static BOOL fmg_mglin(FIBITMAP *U, int n, int ncycle) {
	int j, jcycle, jj, jpost, jpre, nf, ngrid;

	FIBITMAP **IRHO = NULL;
	FIBITMAP **IU   = NULL;
	FIBITMAP **IRHS = NULL;
	FIBITMAP **IRES = NULL;
	
	int ng = 0;		// number of allocated grids

// --------------------------------------------------------------------------

#define _CREATE_ARRAY_GRID_(array, array_size) \
	array = (FIBITMAP**)malloc(array_size * sizeof(FIBITMAP*));\
	if(!array) throw(1);\
	memset(array, 0, array_size * sizeof(FIBITMAP*))

#define _FREE_ARRAY_GRID_(array, array_size) \
	if(NULL != array) {\
		for(int k = 0; k < array_size; k++) {\
			if(NULL != array[k]) {\
				FreeImage_Unload(array[k]); array[k] = NULL;\
			}\
		}\
		free(array);\
	}

// --------------------------------------------------------------------------

	try {
		int nn = n;
		// check grid size and grid levels
		while (nn >>= 1) ng++;
		if (n != 1 + (1L << ng)) {
			FreeImage_OutputMessageProc(FIF_UNKNOWN, "Multigrid algorithm: n = %d, while n-1 must be a power of 2.", n);
			throw(1);
		}
		if (ng > NGMAX) {
			FreeImage_OutputMessageProc(FIF_UNKNOWN, "Multigrid algorithm: ng = %d while NGMAX = %d, increase NGMAX.", ng, NGMAX);
			throw(1);
		}
		// allocate grid arrays
		{
			_CREATE_ARRAY_GRID_(IRHO, ng);
			_CREATE_ARRAY_GRID_(IU, ng);
			_CREATE_ARRAY_GRID_(IRHS, ng);
			_CREATE_ARRAY_GRID_(IRES, ng);
		}

		nn = n/2 + 1;
		ngrid = ng - 2;

		// allocate storage for r.h.s. on grid (ng - 2) ...
		IRHO[ngrid] = FreeImage_AllocateT(FIT_FLOAT, nn, nn);
		if(!IRHO[ngrid]) throw(1);

		// ... and fill it by restricting from the fine grid
		fmg_restrict(IRHO[ngrid], U, nn);	

		// similarly allocate storage and fill r.h.s. on all coarse grids.
		while (nn > 3) {
			nn = nn/2 + 1; 
			ngrid--;
			IRHO[ngrid] = FreeImage_AllocateT(FIT_FLOAT, nn, nn);
			if(!IRHO[ngrid]) throw(1);
			fmg_restrict(IRHO[ngrid], IRHO[ngrid+1], nn);
		}

		nn = 3;

		IU[0] = FreeImage_AllocateT(FIT_FLOAT, nn, nn);
		if(!IU[0]) throw(1);
		IRHS[0] = FreeImage_AllocateT(FIT_FLOAT, nn, nn);
		if(!IRHS[0]) throw(1);

		// initial solution on coarsest grid
		fmg_solve(IU[0], IRHO[0]);
		// irho[0] no longer needed ...
		FreeImage_Unload(IRHO[0]); IRHO[0] = NULL;

		ngrid = ng;

		// nested iteration loop
		for (j = 1; j < ngrid; j++) {
			nn = 2*nn - 1;

			IU[j] = FreeImage_AllocateT(FIT_FLOAT, nn, nn);
			if(!IU[j]) throw(1);
			IRHS[j] = FreeImage_AllocateT(FIT_FLOAT, nn, nn);
			if(!IRHS[j]) throw(1);
			IRES[j] = FreeImage_AllocateT(FIT_FLOAT, nn, nn);
			if(!IRES[j]) throw(1);

			fmg_prolongate(IU[j], IU[j-1], nn);
			
			// interpolate from coarse grid to next finer grid

			// set up r.h.s.
			fmg_copyArray(IRHS[j], j != (ngrid - 1) ? IRHO[j] : U);
			
			// V-cycle loop
			for (jcycle = 0; jcycle < ncycle; jcycle++) {
				nf = nn;
				// downward stoke of the V
				for (jj = j; jj >= 1; jj--) {
					// pre-smoothing
					for (jpre = 0; jpre < NPRE; jpre++) {
						fmg_relaxation(IU[jj], IRHS[jj], nf);
					}
					fmg_residual(IRES[jj], IU[jj], IRHS[jj], nf);
					nf = nf/2 + 1;
					// restriction of the residual is the next r.h.s.
					fmg_restrict(IRHS[jj-1], IRES[jj], nf);				
					// zero for initial guess in next relaxation
					fmg_fillArrayWithZeros(IU[jj-1]);
				}
				// bottom of V: solve on coarsest grid
				fmg_solve(IU[0], IRHS[0]); 
				nf = 3; 
				// upward stroke of V.
				for (jj = 1; jj <= j; jj++) { 
					nf = 2*nf - 1;
					// use res for temporary storage inside addint
					fmg_addint(IU[jj], IU[jj-1], IRES[jj], nf);				
					// post-smoothing
					for (jpost = 0; jpost < NPOST; jpost++) {
						fmg_relaxation(IU[jj], IRHS[jj], nf);
					}
				}
			}
		}

		// return solution in U
		fmg_copyArray(U, IU[ngrid-1]);

		// delete allocated arrays
		_FREE_ARRAY_GRID_(IRES, ng);
		_FREE_ARRAY_GRID_(IRHS, ng);
		_FREE_ARRAY_GRID_(IU, ng);
		_FREE_ARRAY_GRID_(IRHO, ng);

		return TRUE;

	} catch(int) {
		// delete allocated arrays
		_FREE_ARRAY_GRID_(IRES, ng);
		_FREE_ARRAY_GRID_(IRHS, ng);
		_FREE_ARRAY_GRID_(IU, ng);
		_FREE_ARRAY_GRID_(IRHO, ng);

		return FALSE;
	}
}

// --------------------------------------------------------------------------

/**
Poisson solver based on a multigrid algorithm. 
This routine solves a Poisson equation, remap result pixels to [0..1] and returns the solution. 
NB: The input image is first stored inside a square image whose size is (2^j + 1)x(2^j + 1) for some integer j, 
where j is such that 2^j is the nearest larger dimension corresponding to MAX(image width, image height). 
@param Laplacian Laplacian image
@param ncycle Number of cycles in the multigrid algorithm (usually 2 or 3)
@return Returns the solved PDE equations if successful, returns NULL otherwise
*/
FIBITMAP* DLL_CALLCONV 
FreeImage_MultigridPoissonSolver(FIBITMAP *Laplacian, int ncycle) {
	if(!FreeImage_HasPixels(Laplacian)) return NULL;

	int width = FreeImage_GetWidth(Laplacian);
	int height = FreeImage_GetHeight(Laplacian);

	// get nearest larger dimension length that is acceptable by the algorithm
	int n = MAX(width, height);
	int size = 0;
	while((n >>= 1) > 0) size++;
	if((1 << size) < MAX(width, height)) {
		size++;
	}
	// size must be of the form 2^j + 1 for some integer j
	size = 1 + (1 << size);

	// allocate a temporary square image I
	FIBITMAP *I = FreeImage_AllocateT(FIT_FLOAT, size, size);
	if(!I) return NULL;

	// copy Laplacian into I and shift pixels to create a boundary
	FreeImage_Paste(I, Laplacian, 1, 1, 255);

	// solve the PDE equation
	fmg_mglin(I, size, ncycle);

	// shift pixels back
	FIBITMAP *U = FreeImage_Copy(I, 1, 1, width + 1, height + 1);
	FreeImage_Unload(I);

	// remap pixels to [0..1]
	NormalizeY(U, 0, 1);

	// copy metadata from src to dst
	FreeImage_CloneMetadata(U, Laplacian);

	// return the integrated image
	return U;
}

