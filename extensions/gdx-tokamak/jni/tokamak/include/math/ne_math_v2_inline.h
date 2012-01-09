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
// VECTOR2 MEMBER FUNCTIONS
///////////////////////////////////////////////////////////////////////////

//=========================================================================

NEINLINE neV2::neV2( void ){}

//=========================================================================

NEINLINE neV2::neV2( f32 x, f32 y ) 
{ 
    X=x; Y=y; 
}

//=========================================================================

NEINLINE neV2::neV2( const neV2& V )
{
    *this = V;
}

//==============================================================================

NEINLINE neV2::neV2( const f32& K )
{
    X = K;//(f32)cos( R );
    Y = K;//(f32)sin( R );
}

//=========================================================================

NEINLINE void neV2::Zero( void )
{
    X=Y=0;
}

//=========================================================================

NEINLINE f32& neV2::operator[]( s32 I )
{
    ASSERT( I >= 0 );
    ASSERT( I <= 1 );
    return ((f32*)(this))[I];
}

//=========================================================================

NEINLINE neV2& neV2::operator += ( const neV2& V )
{
    X += V.X; Y += V.Y; 
    return *this;
}

//=========================================================================

NEINLINE neV2& neV2::operator -= ( const neV2& V )
{
    X -= V.X; Y -= V.Y; 
    return *this;
}

//=========================================================================

NEINLINE neV2& neV2::operator /= ( f32 S )
{
    *this = *this / S;
    return *this;
}

//=========================================================================

NEINLINE neV2& neV2::operator *= ( f32 S )
{
    *this = *this * S;
    return *this;
}

//=========================================================================

NEINLINE void neV2::Set( f32 x, f32 y )
{
    X = x; Y = y; 
}

//=========================================================================

NEINLINE f32 neV2::Length( void ) const
{
    return (f32)sqrt( this->Dot( *this ) );
}

//=========================================================================

NEINLINE neV2& neV2::Normalize( void )
{
    *this *= 1 / Length();
    return *this;
}

//=========================================================================

NEINLINE f32 neV2::Dot( const neV2& V ) const
{
    return X * V.X + Y * V.Y;
}

//==============================================================================

NEINLINE neV2 neV2::Cross( const neV2& V ) const
{
    return neV2( (X * V.Y) - (Y * V.X) );
}

//==============================================================================

NEINLINE f32 neV2::GetAngle( void ) const
{
    return (f32)atan2( Y, X );
}

//==============================================================================

NEINLINE neV2& neV2::Rotate( neRadian R )
{
    f32 s = (f32)sin( R );
    f32 c = (f32)cos( R );
    f32 x = X;
    f32 y = Y;

    X  = (c * x) - (s * y);
    Y  = (c * y) + (s * x);

    return *this;
}

///////////////////////////////////////////////////////////////////////////
// VECTOR2 FRIEND FUNCTIONS
///////////////////////////////////////////////////////////////////////////

//=========================================================================

NEINLINE neV2 operator - ( const neV2& V )
{
    return neV2( -V.X, -V.Y );
}

//=========================================================================

NEINLINE neV2 operator + ( const neV2& V1, const neV2& V2 )
{
    return neV2( V1.X + V2.X, V1.Y + V2.Y  );
}

//=========================================================================

NEINLINE neV2 operator - ( const neV2& V1, const neV2& V2 )
{
    return neV2( V1.X - V2.X, V1.Y - V2.Y  );
}

//=========================================================================

NEINLINE neV2 operator / ( const neV2& V, f32 S )
{
    return V * (1/S);
}

//=========================================================================

NEINLINE neV2 operator * ( const neV2& V, const f32 S )
{
    return neV2( V.X * S, V.Y * S );
}

//=========================================================================

NEINLINE neV2 operator * ( f32 S,  const neV2& V )
{
    return V * S;
}

//=========================================================================

NEINLINE neRadian neV2_AngleBetween( const neV2& V1, const neV2& V2 )
{
    f32 D, C;
    
    D = V1.Length() * V2.Length();
    
    if( D == 0.0f ) return 0;
    
    C = V1.Dot( V2 ) / D;
    
    if     ( C >  1.0f )  C =  1.0f;
    else if( C < -1.0f )  C = -1.0f;
    
    return (f32)acos( C );
}

///////////////////////////////////////////////////////////////////////////
// END
///////////////////////////////////////////////////////////////////////////
