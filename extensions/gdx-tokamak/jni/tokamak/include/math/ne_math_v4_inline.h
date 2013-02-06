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

///////////////////////////////////////////////////////////////////////////
// VECTOR4 MEMBER FUNCTIONS
///////////////////////////////////////////////////////////////////////////

//=========================================================================

NEINLINE neV4::neV4( void ){}

//=========================================================================

NEINLINE neV4::neV4( f32 x, f32 y, f32 z, f32 w )
{
    X = x; Y = y; Z = z; W = w;
}

//=========================================================================

NEINLINE neV4::neV4( const neV4& V )
{
    (*this) = V;
}

//=========================================================================

NEINLINE neV4::neV4( const neV3& V, f32 w )
{
    X = V.X(); Y = V.Y(); Z = V.Z(); W = w;    
}

//=========================================================================

NEINLINE f32 neV4::Length( void ) const
{
    return (f32)sqrt( this->Dot( *this ) );
}

//=========================================================================

NEINLINE neV4& neV4::Normalize( void )
{
    *this *= 1 / Length();
    return *this;
}

//=========================================================================

NEINLINE void neV4::SetZero( void )
{
    X = Y = Z = W = 0;
}

//=========================================================================

NEINLINE void neV4::Set( f32 x, f32 y, f32 z, f32 w )
{
    X = x; Y = y; Z = z; W = w;
}

//=========================================================================

NEINLINE f32 neV4::Dot( const neV4& V ) const
{
    return  X * V.X + Y * V.Y + Z * V.Z + W * V.W;
}

//=========================================================================

NEINLINE f32& neV4::operator[]( s32 I )
{
    ASSERT( I >= 0 );
    ASSERT( I <= 3 );
    return ((f32*)this)[I];
}

//=========================================================================

NEINLINE neV4& neV4::operator += ( const neV4& V )
{
    X += V.X; Y += V.Y; Z += V.Z; W += V.W;
    return *this;
}

//=========================================================================

NEINLINE neV4& neV4::operator -= ( const neV4& V )
{
    X -= V.X; Y -= V.Y; Z -= V.Z;  W -= V.W;
    return *this;
}

//=========================================================================

NEINLINE neV4& neV4::operator /= ( f32 S )
{
    *this = *this / S;
    return *this;
}

//=========================================================================

NEINLINE neV4& neV4::operator *= ( f32 S )
{
    *this = *this * S;
    return *this;
}


///////////////////////////////////////////////////////////////////////////
// VECTOR4 FRIEND FUNCTIONS
///////////////////////////////////////////////////////////////////////////

//=========================================================================

NEINLINE neV4 operator - ( const neV4& V )
{
    return neV4( -V.X, -V.Y, -V.Z, -V.W );
}

//=========================================================================

NEINLINE neV4 operator + ( const neV4& V1, const neV4& V2 )
{
    return neV4( V1.X + V2.X, V1.Y + V2.Y, V1.Z + V2.Z, V1.W + V2.W );
}

//=========================================================================

NEINLINE neV4 operator - ( const neV4& V1, const neV4& V2 )
{
    return neV4( V1.X - V2.X, V1.Y - V2.Y, V1.Z - V2.Z, V1.W - V2.W );
}

//=========================================================================

NEINLINE neV4 operator / ( const neV4& V, f32 S )
{
    return V * (1/S);
}

//=========================================================================

NEINLINE neV4 operator * ( const neV4& V, const f32 S )
{
    return neV4( V.X * S, V.Y * S, V.Z * S, V.W * S );
}

//=========================================================================

NEINLINE neV4 operator * ( f32 S,  const neV4& V )
{
    return V * S;
}

///////////////////////////////////////////////////////////////////////////
// END
///////////////////////////////////////////////////////////////////////////

