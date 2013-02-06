/*************************************************************************
 *                                                                       *
 * Tokamak Physics Engine, Copyright (C) 2002-2007 David Lam.            *
 * All rights reserved.  Email: david@tokamakphysics.com                 *
 *                       Web: www.tokamakphysics.com                     *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * LICENSE.TXT for more details.                                         *
 *                                                                       *
 *************************************************************************/

#ifndef NE_SMATH_H
#define NE_SMATH_H

#include <math.h>
#include "ne_type.h"

#define NE_PI	(3.141592653589793238462643f)

#define NE_RAD_TO_DEG(A) ((f32)(((A) * (180.0f / NE_PI))))
#define NE_DEG_TO_RAD(A) ((f32)(((A) * (NE_PI / 180.0f))))
#define NE_RI			  NE_DEG_TO_RAD(1) 	
#define NE_ZERO (1.0e-6f)

typedef f32 neRadian;

///////////////////////////////////////////////////////////////////////////
//
// GENERAL
//
///////////////////////////////////////////////////////////////////////////

f32			neFRand      ( f32 Min, f32 Max );
f32			neSin        ( neRadian S );
neRadian    neASin       ( f32 S );
f32         neCos        ( neRadian C );
neRadian    neACos       ( f32 C );
f32         neTan        ( neRadian T );
neRadian    neATan       ( f32 T );
neRadian    neATan2      ( f32 y, f32 x );
neBool		neRealsEqual	( f32 s1, f32 s2);
neBool		neIsConsiderZero(f32 f);
neBool		neIsFinite	(f32);

//template< class ta >                     NEINLINE ta      neAbs     ( const ta&  A )                               { return ( A < 0 ) ? -A : A;   }

NEINLINE f32 neAbs(f32 f)
{
	return (f32)fabs(f);
}

template< class ta, class tb, class tc > NEINLINE neBool  neInRange ( const ta&  X, const tb& Min, const tc& Max ) { return (Min <= X) && (X <= Max);}
template< class ta, class tb, class tc > NEINLINE ta      neRange   ( const ta&  X, const tb& Min, const tc& Max ) { if( X < Min ) return Min; return(X > Max) ? Max : X; }
template< class ta>						 NEINLINE void    neSwap    ( ta &  X, ta & Y) { ta tmp = X; X = Y; Y = tmp; }
template< class ta >                     NEINLINE ta      neSqr     ( const ta&  A )                               { return A * A; }
template< class ta >                     NEINLINE ta      neMin     ( const ta&  A, const ta& B )                  { return ( A < B ) ?  A : B;   }
template< class ta >                     NEINLINE ta      neMax     ( const ta&  A, const ta& B )                  { return ( A > B ) ?  A : B;   }
NEINLINE f32     neMin     ( const s32& A, const f32& B )                 { return ( A < B ) ?  A : B;   }
NEINLINE f32     neMax     ( const s32& A, const f32& B )                 { return ( A > B ) ?  A : B;   }
NEINLINE f32     neMin     ( const f32& A, const s32& B )                 { return ( A < B ) ?  A : B;   }
NEINLINE f32     neMax     ( const f32& A, const s32& B )                 { return ( A > B ) ?  A : B;   }
NEINLINE neBool	   neIsFinite	(f32 n) {return neFinite((double)n);} 

#endif
