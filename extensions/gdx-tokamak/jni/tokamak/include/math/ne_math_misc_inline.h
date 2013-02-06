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

#include <stdlib.h>

//=========================================================================

NEINLINE f32 neFRand( f32 Min, f32 Max )
{
    ASSERT( Max >= Min );
    return( (((f32)rand() / (f32)RAND_MAX) * (Max-Min)) + Min );
}

//=========================================================================

NEINLINE f32 neSin( neRadian S )
{
    return (f32)sin( S );
}

//=========================================================================

NEINLINE f32 neCos( neRadian C )
{
    return (f32)cos( C );
}

//=========================================================================

NEINLINE neRadian neASin( f32 S )
{
    return (f32)asin( S );
}

//=========================================================================

NEINLINE neRadian neACos( f32 C )
{
    return (f32)acos( C );
}

//=========================================================================

NEINLINE f32 neTan( neRadian T )
{
    return (f32)tan( T );
}

//=========================================================================

NEINLINE neRadian neATan( f32 T )
{
    return (f32)atan( T );
}

//=========================================================================

NEINLINE neRadian neATan2( f32 y, f32 x )
{
    return (f32)atan2( y, x );
}

//=========================================================================

NEINLINE neBool neRealsEqual(f32 s1, f32 s2)
{
	if ( (2.0f * neAbs( s1 - s2 ) / ( s1 + s2 )) < NE_ZERO )
	{
		return true;
	}

	return false;
}

//=========================================================================

NEINLINE neBool neIsConsiderZero(f32 f)
{
	return (neAbs(f) < NE_ZERO);
}