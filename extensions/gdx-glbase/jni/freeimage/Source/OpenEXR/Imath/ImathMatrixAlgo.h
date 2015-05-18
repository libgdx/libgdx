///////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2002, Industrial Light & Magic, a division of Lucas
// Digital Ltd. LLC
// 
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
// *       Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// *       Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
// *       Neither the name of Industrial Light & Magic nor the names of
// its contributors may be used to endorse or promote products derived
// from this software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
///////////////////////////////////////////////////////////////////////////


#ifndef INCLUDED_IMATHMATRIXALGO_H
#define INCLUDED_IMATHMATRIXALGO_H

//-------------------------------------------------------------------------
//
//      This file contains algorithms applied to or in conjunction with
//	transformation matrices (Imath::Matrix33 and Imath::Matrix44).
//	The assumption made is that these functions are called much less
//	often than the basic point functions or these functions require
//	more support classes.
//
//	This file also defines a few predefined constant matrices.
//
//-------------------------------------------------------------------------

#include "ImathMatrix.h"
#include "ImathQuat.h"
#include "ImathEuler.h"
#include "ImathExc.h"
#include "ImathVec.h"
#include "ImathLimits.h"
#include <math.h>


#ifdef OPENEXR_DLL
    #ifdef IMATH_EXPORTS
        #define IMATH_EXPORT_CONST extern __declspec(dllexport)
    #else
	#define IMATH_EXPORT_CONST extern __declspec(dllimport)
    #endif
#else
    #define IMATH_EXPORT_CONST extern const
#endif


namespace Imath {

//------------------
// Identity matrices
//------------------

IMATH_EXPORT_CONST M33f identity33f;
IMATH_EXPORT_CONST M44f identity44f;
IMATH_EXPORT_CONST M33d identity33d;
IMATH_EXPORT_CONST M44d identity44d;

//----------------------------------------------------------------------
// Extract scale, shear, rotation, and translation values from a matrix:
// 
// Notes:
//
// This implementation follows the technique described in the paper by
// Spencer W. Thomas in the Graphics Gems II article: "Decomposing a 
// Matrix into Simple Transformations", p. 320.
//
// - Some of the functions below have an optional exc parameter
//   that determines the functions' behavior when the matrix'
//   scaling is very close to zero:
//
//   If exc is true, the functions throw an Imath::ZeroScale exception.
//
//   If exc is false:
//
//      extractScaling (m, s)            returns false, s is invalid
//	sansScaling (m)		         returns m
//	removeScaling (m)	         returns false, m is unchanged
//      sansScalingAndShear (m)          returns m
//      removeScalingAndShear (m)        returns false, m is unchanged
//      extractAndRemoveScalingAndShear (m, s, h)  
//                                       returns false, m is unchanged, 
//                                                      (sh) are invalid
//      checkForZeroScaleInRow ()        returns false
//	extractSHRT (m, s, h, r, t)      returns false, (shrt) are invalid
//
// - Functions extractEuler(), extractEulerXYZ() and extractEulerZYX() 
//   assume that the matrix does not include shear or non-uniform scaling, 
//   but they do not examine the matrix to verify this assumption.  
//   Matrices with shear or non-uniform scaling are likely to produce 
//   meaningless results.  Therefore, you should use the 
//   removeScalingAndShear() routine, if necessary, prior to calling
//   extractEuler...() .
//
// - All functions assume that the matrix does not include perspective
//   transformation(s), but they do not examine the matrix to verify 
//   this assumption.  Matrices with perspective transformations are 
//   likely to produce meaningless results.
//
//----------------------------------------------------------------------


//
// Declarations for 4x4 matrix.
//

template <class T>  bool        extractScaling 
                                            (const Matrix44<T> &mat,
					     Vec3<T> &scl,
					     bool exc = true);
  
template <class T>  Matrix44<T> sansScaling (const Matrix44<T> &mat, 
					     bool exc = true);

template <class T>  bool        removeScaling 
                                            (Matrix44<T> &mat, 
					     bool exc = true);

template <class T>  bool        extractScalingAndShear 
                                            (const Matrix44<T> &mat,
					     Vec3<T> &scl,
					     Vec3<T> &shr,
					     bool exc = true);
  
template <class T>  Matrix44<T> sansScalingAndShear 
                                            (const Matrix44<T> &mat, 
					     bool exc = true);

template <class T>  void        sansScalingAndShear 
                                            (Matrix44<T> &result,
                                             const Matrix44<T> &mat, 
					     bool exc = true);

template <class T>  bool        removeScalingAndShear 
                                            (Matrix44<T> &mat,
					     bool exc = true);

template <class T>  bool        extractAndRemoveScalingAndShear
                                            (Matrix44<T> &mat,
					     Vec3<T>     &scl,
					     Vec3<T>     &shr,
					     bool exc = true);

template <class T>  void	extractEulerXYZ 
                                            (const Matrix44<T> &mat,
					     Vec3<T> &rot);

template <class T>  void	extractEulerZYX 
                                            (const Matrix44<T> &mat,
					     Vec3<T> &rot);

template <class T>  Quat<T>	extractQuat (const Matrix44<T> &mat);

template <class T>  bool	extractSHRT 
                                    (const Matrix44<T> &mat,
				     Vec3<T> &s,
				     Vec3<T> &h,
				     Vec3<T> &r,
				     Vec3<T> &t,
				     bool exc /*= true*/,
				     typename Euler<T>::Order rOrder);

template <class T>  bool	extractSHRT 
                                    (const Matrix44<T> &mat,
				     Vec3<T> &s,
				     Vec3<T> &h,
				     Vec3<T> &r,
				     Vec3<T> &t,
				     bool exc = true);

template <class T>  bool	extractSHRT 
                                    (const Matrix44<T> &mat,
				     Vec3<T> &s,
				     Vec3<T> &h,
				     Euler<T> &r,
				     Vec3<T> &t,
				     bool exc = true);

//
// Internal utility function.
//

template <class T>  bool	checkForZeroScaleInRow
                                            (const T       &scl, 
					     const Vec3<T> &row,
					     bool exc = true);

template <class T>  Matrix44<T> outerProduct
                                            ( const Vec4<T> &a,
                                              const Vec4<T> &b);


//
// Returns a matrix that rotates "fromDirection" vector to "toDirection"
// vector.
//

template <class T> Matrix44<T>	rotationMatrix (const Vec3<T> &fromDirection,
						const Vec3<T> &toDirection);



//
// Returns a matrix that rotates the "fromDir" vector 
// so that it points towards "toDir".  You may also 
// specify that you want the up vector to be pointing 
// in a certain direction "upDir".
//

template <class T> Matrix44<T>	rotationMatrixWithUpDir 
                                            (const Vec3<T> &fromDir,
					     const Vec3<T> &toDir,
					     const Vec3<T> &upDir);


//
// Constructs a matrix that rotates the z-axis so that it 
// points towards "targetDir".  You must also specify 
// that you want the up vector to be pointing in a 
// certain direction "upDir".
//
// Notes: The following degenerate cases are handled:
//        (a) when the directions given by "toDir" and "upDir" 
//            are parallel or opposite;
//            (the direction vectors must have a non-zero cross product)
//        (b) when any of the given direction vectors have zero length
//

template <class T> void	alignZAxisWithTargetDir 
                                            (Matrix44<T> &result,
                                             Vec3<T>      targetDir, 
					     Vec3<T>      upDir);


// Compute an orthonormal direct frame from : a position, an x axis direction and a normal to the y axis
// If the x axis and normal are perpendicular, then the normal will have the same direction as the z axis.
// Inputs are : 
//     -the position of the frame
//     -the x axis direction of the frame
//     -a normal to the y axis of the frame
// Return is the orthonormal frame
template <class T> Matrix44<T> computeLocalFrame( const Vec3<T>& p,
                                                  const Vec3<T>& xDir,
                                                  const Vec3<T>& normal);

// Add a translate/rotate/scale offset to an input frame
// and put it in another frame of reference
// Inputs are :
//     - input frame
//     - translate offset
//     - rotate    offset in degrees
//     - scale     offset
//     - frame of reference
// Output is the offsetted frame
template <class T> Matrix44<T> addOffset( const Matrix44<T>& inMat,
                                          const Vec3<T>&     tOffset,
                                          const Vec3<T>&     rOffset,
                                          const Vec3<T>&     sOffset,
                                          const Vec3<T>&     ref);

// Compute Translate/Rotate/Scale matrix from matrix A with the Rotate/Scale of Matrix B
// Inputs are :
//      -keepRotateA : if true keep rotate from matrix A, use B otherwise
//      -keepScaleA  : if true keep scale  from matrix A, use B otherwise
//      -Matrix A
//      -Matrix B
// Return Matrix A with tweaked rotation/scale
template <class T> Matrix44<T> computeRSMatrix( bool               keepRotateA,
                                                bool               keepScaleA, 
                                                const Matrix44<T>& A,
                                                const Matrix44<T>& B);


//----------------------------------------------------------------------


// 
// Declarations for 3x3 matrix.
//

 
template <class T>  bool        extractScaling 
                                            (const Matrix33<T> &mat,
					     Vec2<T> &scl,
					     bool exc = true);
  
template <class T>  Matrix33<T> sansScaling (const Matrix33<T> &mat, 
					     bool exc = true);

template <class T>  bool        removeScaling 
                                            (Matrix33<T> &mat, 
					     bool exc = true);

template <class T>  bool        extractScalingAndShear 
                                            (const Matrix33<T> &mat,
					     Vec2<T> &scl,
					     T &h,
					     bool exc = true);
  
template <class T>  Matrix33<T> sansScalingAndShear 
                                            (const Matrix33<T> &mat, 
					     bool exc = true);

template <class T>  bool        removeScalingAndShear 
                                            (Matrix33<T> &mat,
					     bool exc = true);

template <class T>  bool        extractAndRemoveScalingAndShear
                                            (Matrix33<T> &mat,
					     Vec2<T>     &scl,
					     T           &shr,
					     bool exc = true);

template <class T>  void	extractEuler
                                            (const Matrix33<T> &mat,
					     T       &rot);

template <class T>  bool	extractSHRT (const Matrix33<T> &mat,
					     Vec2<T> &s,
					     T       &h,
					     T       &r,
					     Vec2<T> &t,
					     bool exc = true);

template <class T>  bool	checkForZeroScaleInRow
                                            (const T       &scl, 
					     const Vec2<T> &row,
					     bool exc = true);

template <class T>  Matrix33<T> outerProduct
                                            ( const Vec3<T> &a,
                                              const Vec3<T> &b);


//-----------------------------------------------------------------------------
// Implementation for 4x4 Matrix
//------------------------------


template <class T>
bool
extractScaling (const Matrix44<T> &mat, Vec3<T> &scl, bool exc)
{
    Vec3<T> shr;
    Matrix44<T> M (mat);

    if (! extractAndRemoveScalingAndShear (M, scl, shr, exc))
	return false;
    
    return true;
}


template <class T>
Matrix44<T>
sansScaling (const Matrix44<T> &mat, bool exc)
{
    Vec3<T> scl;
    Vec3<T> shr;
    Vec3<T> rot;
    Vec3<T> tran;

    if (! extractSHRT (mat, scl, shr, rot, tran, exc))
	return mat;

    Matrix44<T> M;
    
    M.translate (tran);
    M.rotate (rot);
    M.shear (shr);

    return M;
}


template <class T>
bool
removeScaling (Matrix44<T> &mat, bool exc)
{
    Vec3<T> scl;
    Vec3<T> shr;
    Vec3<T> rot;
    Vec3<T> tran;

    if (! extractSHRT (mat, scl, shr, rot, tran, exc))
	return false;

    mat.makeIdentity ();
    mat.translate (tran);
    mat.rotate (rot);
    mat.shear (shr);

    return true;
}


template <class T>
bool
extractScalingAndShear (const Matrix44<T> &mat, 
			Vec3<T> &scl, Vec3<T> &shr, bool exc)
{
    Matrix44<T> M (mat);

    if (! extractAndRemoveScalingAndShear (M, scl, shr, exc))
	return false;
    
    return true;
}


template <class T>
Matrix44<T>
sansScalingAndShear (const Matrix44<T> &mat, bool exc)
{
    Vec3<T> scl;
    Vec3<T> shr;
    Matrix44<T> M (mat);

    if (! extractAndRemoveScalingAndShear (M, scl, shr, exc))
	return mat;
    
    return M;
}


template <class T>
void
sansScalingAndShear (Matrix44<T> &result, const Matrix44<T> &mat, bool exc)
{
    Vec3<T> scl;
    Vec3<T> shr;

    if (! extractAndRemoveScalingAndShear (result, scl, shr, exc))
	result = mat;
}


template <class T>
bool
removeScalingAndShear (Matrix44<T> &mat, bool exc)
{
    Vec3<T> scl;
    Vec3<T> shr;

    if (! extractAndRemoveScalingAndShear (mat, scl, shr, exc))
	return false;
    
    return true;
}


template <class T>
bool
extractAndRemoveScalingAndShear (Matrix44<T> &mat, 
				 Vec3<T> &scl, Vec3<T> &shr, bool exc)
{
    //
    // This implementation follows the technique described in the paper by
    // Spencer W. Thomas in the Graphics Gems II article: "Decomposing a 
    // Matrix into Simple Transformations", p. 320.
    //

    Vec3<T> row[3];

    row[0] = Vec3<T> (mat[0][0], mat[0][1], mat[0][2]);
    row[1] = Vec3<T> (mat[1][0], mat[1][1], mat[1][2]);
    row[2] = Vec3<T> (mat[2][0], mat[2][1], mat[2][2]);
    
    T maxVal = 0;
    for (int i=0; i < 3; i++)
	for (int j=0; j < 3; j++)
	    if (Imath::abs (row[i][j]) > maxVal)
		maxVal = Imath::abs (row[i][j]);

    //
    // We normalize the 3x3 matrix here.
    // It was noticed that this can improve numerical stability significantly,
    // especially when many of the upper 3x3 matrix's coefficients are very
    // close to zero; we correct for this step at the end by multiplying the 
    // scaling factors by maxVal at the end (shear and rotation are not 
    // affected by the normalization).

    if (maxVal != 0)
    {
	for (int i=0; i < 3; i++)
	    if (! checkForZeroScaleInRow (maxVal, row[i], exc))
		return false;
	    else
		row[i] /= maxVal;
    }

    // Compute X scale factor. 
    scl.x = row[0].length ();
    if (! checkForZeroScaleInRow (scl.x, row[0], exc))
	return false;

    // Normalize first row.
    row[0] /= scl.x;

    // An XY shear factor will shear the X coord. as the Y coord. changes.
    // There are 6 combinations (XY, XZ, YZ, YX, ZX, ZY), although we only
    // extract the first 3 because we can effect the last 3 by shearing in
    // XY, XZ, YZ combined rotations and scales.
    //
    // shear matrix <   1,  YX,  ZX,  0,
    //                 XY,   1,  ZY,  0,
    //                 XZ,  YZ,   1,  0,
    //                  0,   0,   0,  1 >

    // Compute XY shear factor and make 2nd row orthogonal to 1st.
    shr[0]  = row[0].dot (row[1]);
    row[1] -= shr[0] * row[0];

    // Now, compute Y scale.
    scl.y = row[1].length ();
    if (! checkForZeroScaleInRow (scl.y, row[1], exc))
	return false;

    // Normalize 2nd row and correct the XY shear factor for Y scaling.
    row[1] /= scl.y; 
    shr[0] /= scl.y;

    // Compute XZ and YZ shears, orthogonalize 3rd row.
    shr[1]  = row[0].dot (row[2]);
    row[2] -= shr[1] * row[0];
    shr[2]  = row[1].dot (row[2]);
    row[2] -= shr[2] * row[1];

    // Next, get Z scale.
    scl.z = row[2].length ();
    if (! checkForZeroScaleInRow (scl.z, row[2], exc))
	return false;

    // Normalize 3rd row and correct the XZ and YZ shear factors for Z scaling.
    row[2] /= scl.z;
    shr[1] /= scl.z;
    shr[2] /= scl.z;

    // At this point, the upper 3x3 matrix in mat is orthonormal.
    // Check for a coordinate system flip. If the determinant
    // is less than zero, then negate the matrix and the scaling factors.
    if (row[0].dot (row[1].cross (row[2])) < 0)
	for (int  i=0; i < 3; i++)
	{
	    scl[i] *= -1;
	    row[i] *= -1;
	}

    // Copy over the orthonormal rows into the returned matrix.
    // The upper 3x3 matrix in mat is now a rotation matrix.
    for (int i=0; i < 3; i++)
    {
	mat[i][0] = row[i][0]; 
	mat[i][1] = row[i][1]; 
	mat[i][2] = row[i][2];
    }

    // Correct the scaling factors for the normalization step that we 
    // performed above; shear and rotation are not affected by the 
    // normalization.
    scl *= maxVal;

    return true;
}


template <class T>
void
extractEulerXYZ (const Matrix44<T> &mat, Vec3<T> &rot)
{
    //
    // Normalize the local x, y and z axes to remove scaling.
    //

    Vec3<T> i (mat[0][0], mat[0][1], mat[0][2]);
    Vec3<T> j (mat[1][0], mat[1][1], mat[1][2]);
    Vec3<T> k (mat[2][0], mat[2][1], mat[2][2]);

    i.normalize();
    j.normalize();
    k.normalize();

    Matrix44<T> M (i[0], i[1], i[2], 0, 
		   j[0], j[1], j[2], 0, 
		   k[0], k[1], k[2], 0, 
		   0,    0,    0,    1);

    //
    // Extract the first angle, rot.x.
    // 

    rot.x = Math<T>::atan2 (M[1][2], M[2][2]);

    //
    // Remove the rot.x rotation from M, so that the remaining
    // rotation, N, is only around two axes, and gimbal lock
    // cannot occur.
    //

    Matrix44<T> N;
    N.rotate (Vec3<T> (-rot.x, 0, 0));
    N = N * M;

    //
    // Extract the other two angles, rot.y and rot.z, from N.
    //

    T cy = Math<T>::sqrt (N[0][0]*N[0][0] + N[0][1]*N[0][1]);
    rot.y = Math<T>::atan2 (-N[0][2], cy);
    rot.z = Math<T>::atan2 (-N[1][0], N[1][1]);
}


template <class T>
void
extractEulerZYX (const Matrix44<T> &mat, Vec3<T> &rot)
{
    //
    // Normalize the local x, y and z axes to remove scaling.
    //

    Vec3<T> i (mat[0][0], mat[0][1], mat[0][2]);
    Vec3<T> j (mat[1][0], mat[1][1], mat[1][2]);
    Vec3<T> k (mat[2][0], mat[2][1], mat[2][2]);

    i.normalize();
    j.normalize();
    k.normalize();

    Matrix44<T> M (i[0], i[1], i[2], 0, 
		   j[0], j[1], j[2], 0, 
		   k[0], k[1], k[2], 0, 
		   0,    0,    0,    1);

    //
    // Extract the first angle, rot.x.
    // 

    rot.x = -Math<T>::atan2 (M[1][0], M[0][0]);

    //
    // Remove the x rotation from M, so that the remaining
    // rotation, N, is only around two axes, and gimbal lock
    // cannot occur.
    //

    Matrix44<T> N;
    N.rotate (Vec3<T> (0, 0, -rot.x));
    N = N * M;

    //
    // Extract the other two angles, rot.y and rot.z, from N.
    //

    T cy = Math<T>::sqrt (N[2][2]*N[2][2] + N[2][1]*N[2][1]);
    rot.y = -Math<T>::atan2 (-N[2][0], cy);
    rot.z = -Math<T>::atan2 (-N[1][2], N[1][1]);
}


template <class T>
Quat<T>
extractQuat (const Matrix44<T> &mat)
{
  Matrix44<T> rot;

  T        tr, s;
  T         q[4];
  int    i, j, k;
  Quat<T>   quat;

  int nxt[3] = {1, 2, 0};
  tr = mat[0][0] + mat[1][1] + mat[2][2];

  // check the diagonal
  if (tr > 0.0) {
     s = Math<T>::sqrt (tr + T(1.0));
     quat.r = s / T(2.0);
     s = T(0.5) / s;

     quat.v.x = (mat[1][2] - mat[2][1]) * s;
     quat.v.y = (mat[2][0] - mat[0][2]) * s;
     quat.v.z = (mat[0][1] - mat[1][0]) * s;
  } 
  else {      
     // diagonal is negative
     i = 0;
     if (mat[1][1] > mat[0][0]) 
        i=1;
     if (mat[2][2] > mat[i][i]) 
        i=2;
    
     j = nxt[i];
     k = nxt[j];
     s = Math<T>::sqrt ((mat[i][i] - (mat[j][j] + mat[k][k])) + T(1.0));
      
     q[i] = s * T(0.5);
     if (s != T(0.0)) 
        s = T(0.5) / s;

     q[3] = (mat[j][k] - mat[k][j]) * s;
     q[j] = (mat[i][j] + mat[j][i]) * s;
     q[k] = (mat[i][k] + mat[k][i]) * s;

     quat.v.x = q[0];
     quat.v.y = q[1];
     quat.v.z = q[2];
     quat.r = q[3];
 }

  return quat;
}

template <class T>
bool 
extractSHRT (const Matrix44<T> &mat,
	     Vec3<T> &s,
	     Vec3<T> &h,
	     Vec3<T> &r,
	     Vec3<T> &t,
	     bool exc /* = true */ ,
	     typename Euler<T>::Order rOrder /* = Euler<T>::XYZ */ )
{
    Matrix44<T> rot;

    rot = mat;
    if (! extractAndRemoveScalingAndShear (rot, s, h, exc))
	return false;

    extractEulerXYZ (rot, r);

    t.x = mat[3][0];
    t.y = mat[3][1];
    t.z = mat[3][2];

    if (rOrder != Euler<T>::XYZ)
    {
	Imath::Euler<T> eXYZ (r, Imath::Euler<T>::XYZ);
	Imath::Euler<T> e (eXYZ, rOrder);
	r = e.toXYZVector ();
    }

    return true;
}

template <class T>
bool 
extractSHRT (const Matrix44<T> &mat,
	     Vec3<T> &s,
	     Vec3<T> &h,
	     Vec3<T> &r,
	     Vec3<T> &t,
	     bool exc)
{
    return extractSHRT(mat, s, h, r, t, exc, Imath::Euler<T>::XYZ);
}

template <class T>
bool 
extractSHRT (const Matrix44<T> &mat,
	     Vec3<T> &s,
	     Vec3<T> &h,
	     Euler<T> &r,
	     Vec3<T> &t,
	     bool exc /* = true */)
{
    return extractSHRT (mat, s, h, r, t, exc, r.order ());
}


template <class T> 
bool		
checkForZeroScaleInRow (const T& scl, 
			const Vec3<T> &row,
			bool exc /* = true */ )
{
    for (int i = 0; i < 3; i++)
    {
	if ((abs (scl) < 1 && abs (row[i]) >= limits<T>::max() * abs (scl)))
	{
	    if (exc)
		throw Imath::ZeroScaleExc ("Cannot remove zero scaling "
					   "from matrix.");
	    else
		return false;
	}
    }

    return true;
}

template <class T>
Matrix44<T>
outerProduct (const Vec4<T> &a, const Vec4<T> &b )
{
    return Matrix44<T> (a.x*b.x, a.x*b.y, a.x*b.z, a.x*b.w,
                        a.y*b.x, a.y*b.y, a.y*b.z, a.x*b.w,
                        a.z*b.x, a.z*b.y, a.z*b.z, a.x*b.w,
                        a.w*b.x, a.w*b.y, a.w*b.z, a.w*b.w);
}

template <class T>
Matrix44<T>
rotationMatrix (const Vec3<T> &from, const Vec3<T> &to)
{
    Quat<T> q;
    q.setRotation(from, to);
    return q.toMatrix44();
}


template <class T>
Matrix44<T>	
rotationMatrixWithUpDir (const Vec3<T> &fromDir,
			 const Vec3<T> &toDir,
			 const Vec3<T> &upDir)
{
    //
    // The goal is to obtain a rotation matrix that takes 
    // "fromDir" to "toDir".  We do this in two steps and 
    // compose the resulting rotation matrices; 
    //    (a) rotate "fromDir" into the z-axis
    //    (b) rotate the z-axis into "toDir"
    //

    // The from direction must be non-zero; but we allow zero to and up dirs.
    if (fromDir.length () == 0)
	return Matrix44<T> ();

    else
    {
	Matrix44<T> zAxis2FromDir( Imath::UNINITIALIZED );
	alignZAxisWithTargetDir (zAxis2FromDir, fromDir, Vec3<T> (0, 1, 0));

	Matrix44<T> fromDir2zAxis  = zAxis2FromDir.transposed ();
	
	Matrix44<T> zAxis2ToDir( Imath::UNINITIALIZED );
	alignZAxisWithTargetDir (zAxis2ToDir, toDir, upDir);

	return fromDir2zAxis * zAxis2ToDir;
    }
}


template <class T>
void
alignZAxisWithTargetDir (Matrix44<T> &result, Vec3<T> targetDir, Vec3<T> upDir)
{
    //
    // Ensure that the target direction is non-zero.
    //

    if ( targetDir.length () == 0 )
	targetDir = Vec3<T> (0, 0, 1);

    //
    // Ensure that the up direction is non-zero.
    //

    if ( upDir.length () == 0 )
	upDir = Vec3<T> (0, 1, 0);

    //
    // Check for degeneracies.  If the upDir and targetDir are parallel 
    // or opposite, then compute a new, arbitrary up direction that is
    // not parallel or opposite to the targetDir.
    //

    if (upDir.cross (targetDir).length () == 0)
    {
	upDir = targetDir.cross (Vec3<T> (1, 0, 0));
	if (upDir.length() == 0)
	    upDir = targetDir.cross(Vec3<T> (0, 0, 1));
    }

    //
    // Compute the x-, y-, and z-axis vectors of the new coordinate system.
    //

    Vec3<T> targetPerpDir = upDir.cross (targetDir);    
    Vec3<T> targetUpDir   = targetDir.cross (targetPerpDir);
    
    //
    // Rotate the x-axis into targetPerpDir (row 0),
    // rotate the y-axis into targetUpDir   (row 1),
    // rotate the z-axis into targetDir     (row 2).
    //
    
    Vec3<T> row[3];
    row[0] = targetPerpDir.normalized ();
    row[1] = targetUpDir  .normalized ();
    row[2] = targetDir    .normalized ();
    
    result.x[0][0] = row[0][0];
    result.x[0][1] = row[0][1];
    result.x[0][2] = row[0][2];
    result.x[0][3] = (T)0;
 
    result.x[1][0] = row[1][0];
    result.x[1][1] = row[1][1];
    result.x[1][2] = row[1][2];
    result.x[1][3] = (T)0;
 
    result.x[2][0] = row[2][0];
    result.x[2][1] = row[2][1];
    result.x[2][2] = row[2][2];
    result.x[2][3] = (T)0;
 
    result.x[3][0] = (T)0;
    result.x[3][1] = (T)0;
    result.x[3][2] = (T)0;
    result.x[3][3] = (T)1;
}


// Compute an orthonormal direct frame from : a position, an x axis direction and a normal to the y axis
// If the x axis and normal are perpendicular, then the normal will have the same direction as the z axis.
// Inputs are : 
//     -the position of the frame
//     -the x axis direction of the frame
//     -a normal to the y axis of the frame
// Return is the orthonormal frame
template <class T>
Matrix44<T>
computeLocalFrame( const Vec3<T>& p,
                   const Vec3<T>& xDir,
                   const Vec3<T>& normal)
{
    Vec3<T> _xDir(xDir);
    Vec3<T> x = _xDir.normalize();
    Vec3<T> y = (normal % x).normalize();
    Vec3<T> z = (x % y).normalize();

    Matrix44<T> L;
    L[0][0] = x[0];
    L[0][1] = x[1];
    L[0][2] = x[2];
    L[0][3] = 0.0;

    L[1][0] = y[0];
    L[1][1] = y[1];
    L[1][2] = y[2];
    L[1][3] = 0.0;

    L[2][0] = z[0];
    L[2][1] = z[1];
    L[2][2] = z[2];
    L[2][3] = 0.0;

    L[3][0] = p[0];
    L[3][1] = p[1];
    L[3][2] = p[2];
    L[3][3] = 1.0;
    
    return L;
}

// Add a translate/rotate/scale offset to an input frame
// and put it in another frame of reference
// Inputs are :
//     - input frame
//     - translate offset
//     - rotate    offset in degrees
//     - scale     offset
//     - frame of reference
// Output is the offsetted frame
template <class T>
Matrix44<T>
addOffset( const Matrix44<T>& inMat,
           const Vec3<T>&     tOffset,
           const Vec3<T>&     rOffset,
           const Vec3<T>&     sOffset,
           const Matrix44<T>& ref)
{
    Matrix44<T> O;

    Vec3<T> _rOffset(rOffset);
    _rOffset *= M_PI / 180.0;
    O.rotate (_rOffset);

    O[3][0] = tOffset[0];
    O[3][1] = tOffset[1];
    O[3][2] = tOffset[2];

    Matrix44<T> S;
    S.scale (sOffset);

    Matrix44<T> X = S * O * inMat * ref;

    return X;
}

// Compute Translate/Rotate/Scale matrix from matrix A with the Rotate/Scale of Matrix B
// Inputs are :
//      -keepRotateA : if true keep rotate from matrix A, use B otherwise
//      -keepScaleA  : if true keep scale  from matrix A, use B otherwise
//      -Matrix A
//      -Matrix B
// Return Matrix A with tweaked rotation/scale
template <class T>
Matrix44<T>
computeRSMatrix( bool               keepRotateA,
                 bool               keepScaleA, 
                 const Matrix44<T>& A, 
                 const Matrix44<T>& B)
{
    Vec3<T> as, ah, ar, at;
    extractSHRT (A, as, ah, ar, at);
    
    Vec3<T> bs, bh, br, bt;
    extractSHRT (B, bs, bh, br, bt);

    if (!keepRotateA)
        ar = br;

    if (!keepScaleA)
        as = bs;

    Matrix44<T> mat;
    mat.makeIdentity();
    mat.translate (at);
    mat.rotate (ar);
    mat.scale (as);
    
    return mat;
}



//-----------------------------------------------------------------------------
// Implementation for 3x3 Matrix
//------------------------------


template <class T>
bool
extractScaling (const Matrix33<T> &mat, Vec2<T> &scl, bool exc)
{
    T shr;
    Matrix33<T> M (mat);

    if (! extractAndRemoveScalingAndShear (M, scl, shr, exc))
	return false;

    return true;
}


template <class T>
Matrix33<T>
sansScaling (const Matrix33<T> &mat, bool exc)
{
    Vec2<T> scl;
    T shr;
    T rot;
    Vec2<T> tran;

    if (! extractSHRT (mat, scl, shr, rot, tran, exc))
	return mat;

    Matrix33<T> M;
    
    M.translate (tran);
    M.rotate (rot);
    M.shear (shr);

    return M;
}


template <class T>
bool
removeScaling (Matrix33<T> &mat, bool exc)
{
    Vec2<T> scl;
    T shr;
    T rot;
    Vec2<T> tran;

    if (! extractSHRT (mat, scl, shr, rot, tran, exc))
	return false;

    mat.makeIdentity ();
    mat.translate (tran);
    mat.rotate (rot);
    mat.shear (shr);

    return true;
}


template <class T>
bool
extractScalingAndShear (const Matrix33<T> &mat, Vec2<T> &scl, T &shr, bool exc)
{
    Matrix33<T> M (mat);

    if (! extractAndRemoveScalingAndShear (M, scl, shr, exc))
	return false;

    return true;
}


template <class T>
Matrix33<T>
sansScalingAndShear (const Matrix33<T> &mat, bool exc)
{
    Vec2<T> scl;
    T shr;
    Matrix33<T> M (mat);

    if (! extractAndRemoveScalingAndShear (M, scl, shr, exc))
	return mat;
    
    return M;
}


template <class T>
bool
removeScalingAndShear (Matrix33<T> &mat, bool exc)
{
    Vec2<T> scl;
    T shr;

    if (! extractAndRemoveScalingAndShear (mat, scl, shr, exc))
	return false;
    
    return true;
}

template <class T>
bool
extractAndRemoveScalingAndShear (Matrix33<T> &mat, 
				 Vec2<T> &scl, T &shr, bool exc)
{
    Vec2<T> row[2];

    row[0] = Vec2<T> (mat[0][0], mat[0][1]);
    row[1] = Vec2<T> (mat[1][0], mat[1][1]);
    
    T maxVal = 0;
    for (int i=0; i < 2; i++)
	for (int j=0; j < 2; j++)
	    if (Imath::abs (row[i][j]) > maxVal)
		maxVal = Imath::abs (row[i][j]);

    //
    // We normalize the 2x2 matrix here.
    // It was noticed that this can improve numerical stability significantly,
    // especially when many of the upper 2x2 matrix's coefficients are very
    // close to zero; we correct for this step at the end by multiplying the 
    // scaling factors by maxVal at the end (shear and rotation are not 
    // affected by the normalization).

    if (maxVal != 0)
    {
	for (int i=0; i < 2; i++)
	    if (! checkForZeroScaleInRow (maxVal, row[i], exc))
		return false;
	    else
		row[i] /= maxVal;
    }

    // Compute X scale factor. 
    scl.x = row[0].length ();
    if (! checkForZeroScaleInRow (scl.x, row[0], exc))
	return false;

    // Normalize first row.
    row[0] /= scl.x;

    // An XY shear factor will shear the X coord. as the Y coord. changes.
    // There are 2 combinations (XY, YX), although we only extract the XY 
    // shear factor because we can effect the an YX shear factor by 
    // shearing in XY combined with rotations and scales.
    //
    // shear matrix <   1,  YX,  0,
    //                 XY,   1,  0,
    //                  0,   0,  1 >

    // Compute XY shear factor and make 2nd row orthogonal to 1st.
    shr     = row[0].dot (row[1]);
    row[1] -= shr * row[0];

    // Now, compute Y scale.
    scl.y = row[1].length ();
    if (! checkForZeroScaleInRow (scl.y, row[1], exc))
	return false;

    // Normalize 2nd row and correct the XY shear factor for Y scaling.
    row[1] /= scl.y; 
    shr    /= scl.y;

    // At this point, the upper 2x2 matrix in mat is orthonormal.
    // Check for a coordinate system flip. If the determinant
    // is -1, then flip the rotation matrix and adjust the scale(Y) 
    // and shear(XY) factors to compensate.
    if (row[0][0] * row[1][1] - row[0][1] * row[1][0] < 0)
    {
	row[1][0] *= -1;
	row[1][1] *= -1;
	scl[1] *= -1;
	shr *= -1;
    }

    // Copy over the orthonormal rows into the returned matrix.
    // The upper 2x2 matrix in mat is now a rotation matrix.
    for (int i=0; i < 2; i++)
    {
	mat[i][0] = row[i][0]; 
	mat[i][1] = row[i][1]; 
    }

    scl *= maxVal;

    return true;
}


template <class T>
void
extractEuler (const Matrix33<T> &mat, T &rot)
{
    //
    // Normalize the local x and y axes to remove scaling.
    //

    Vec2<T> i (mat[0][0], mat[0][1]);
    Vec2<T> j (mat[1][0], mat[1][1]);

    i.normalize();
    j.normalize();

    //
    // Extract the angle, rot.
    // 

    rot = - Math<T>::atan2 (j[0], i[0]);
}


template <class T>
bool 
extractSHRT (const Matrix33<T> &mat,
	     Vec2<T> &s,
	     T       &h,
	     T       &r,
	     Vec2<T> &t,
	     bool    exc)
{
    Matrix33<T> rot;

    rot = mat;
    if (! extractAndRemoveScalingAndShear (rot, s, h, exc))
	return false;

    extractEuler (rot, r);

    t.x = mat[2][0];
    t.y = mat[2][1];

    return true;
}


template <class T> 
bool		
checkForZeroScaleInRow (const T& scl, 
			const Vec2<T> &row,
			bool exc /* = true */ )
{
    for (int i = 0; i < 2; i++)
    {
	if ((abs (scl) < 1 && abs (row[i]) >= limits<T>::max() * abs (scl)))
	{
	    if (exc)
		throw Imath::ZeroScaleExc ("Cannot remove zero scaling "
					   "from matrix.");
	    else
		return false;
	}
    }

    return true;
}


template <class T>
Matrix33<T>
outerProduct (const Vec3<T> &a, const Vec3<T> &b )
{
    return Matrix33<T> (a.x*b.x, a.x*b.y, a.x*b.z,
                        a.y*b.x, a.y*b.y, a.y*b.z,
                        a.z*b.x, a.z*b.y, a.z*b.z );
}


// Computes the translation and rotation that brings the 'from' points
// as close as possible to the 'to' points under the Frobenius norm.  
// To be more specific, let x be the matrix of 'from' points and y be
// the matrix of 'to' points, we want to find the matrix A of the form
//    [ R t ]
//    [ 0 1 ]
// that minimizes
//     || (A*x - y)^T * W * (A*x - y) ||_F
// If doScaling is true, then a uniform scale is allowed also.
template <typename T>
Imath::M44d
procrustesRotationAndTranslation (const Imath::Vec3<T>* A,  // From these
                                  const Imath::Vec3<T>* B,  // To these
                                  const T* weights, 
                                  const size_t numPoints,
                                  const bool doScaling = false);

// Unweighted:
template <typename T>
Imath::M44d
procrustesRotationAndTranslation (const Imath::Vec3<T>* A, 
                                  const Imath::Vec3<T>* B, 
                                  const size_t numPoints,
                                  const bool doScaling = false);

// Compute the SVD of a 3x3 matrix using Jacobi transformations.  This method
// should be quite accurate (competitive with LAPACK) even for poorly
// conditioned matrices, and because it has been written specifically for the
// 3x3/4x4 case it is much faster than calling out to LAPACK.  
//
// The SVD of a 3x3/4x4 matrix A is defined as follows:
//     A = U * S * V^T
// where S is the diagonal matrix of singular values and both U and V are
// orthonormal.  By convention, the entries S are all positive and sorted from
// the largest to the smallest.  However, some uses of this function may
// require that the matrix U*V^T have positive determinant; in this case, we
// may make the smallest singular value negative to ensure that this is
// satisfied.  
//
// Currently only available for single- and double-precision matrices.
template <typename T>
void
jacobiSVD (const Imath::Matrix33<T>& A,
           Imath::Matrix33<T>& U,
           Imath::Vec3<T>& S,
           Imath::Matrix33<T>& V,
           const T tol = Imath::limits<T>::epsilon(),
           const bool forcePositiveDeterminant = false);

template <typename T>
void
jacobiSVD (const Imath::Matrix44<T>& A,
           Imath::Matrix44<T>& U,
           Imath::Vec4<T>& S,
           Imath::Matrix44<T>& V,
           const T tol = Imath::limits<T>::epsilon(),
           const bool forcePositiveDeterminant = false);

// Compute the eigenvalues (S) and the eigenvectors (V) of
// a real symmetric matrix using Jacobi transformation.
//
// Jacobi transformation of a 3x3/4x4 matrix A outputs S and V:
// 	A = V * S * V^T
// where V is orthonormal and S is the diagonal matrix of eigenvalues.
// Input matrix A must be symmetric. A is also modified during
// the computation so that upper diagonal entries of A become zero. 
//
template <typename T>
void
jacobiEigenSolver (Matrix33<T>& A,
                   Vec3<T>& S,
                   Matrix33<T>& V,
                   const T tol);

template <typename T>
inline
void
jacobiEigenSolver (Matrix33<T>& A,
                   Vec3<T>& S,
                   Matrix33<T>& V)
{
    jacobiEigenSolver(A,S,V,limits<T>::epsilon());
}

template <typename T>
void
jacobiEigenSolver (Matrix44<T>& A,
                   Vec4<T>& S,
                   Matrix44<T>& V,
                   const T tol);

template <typename T>
inline
void
jacobiEigenSolver (Matrix44<T>& A,
                   Vec4<T>& S,
                   Matrix44<T>& V)
{
    jacobiEigenSolver(A,S,V,limits<T>::epsilon());
}

// Compute a eigenvector corresponding to the abs max/min eigenvalue
// of a real symmetric matrix using Jacobi transformation.
template <typename TM, typename TV>
void
maxEigenVector (TM& A, TV& S);
template <typename TM, typename TV>
void
minEigenVector (TM& A, TV& S);

} // namespace Imath

#endif
