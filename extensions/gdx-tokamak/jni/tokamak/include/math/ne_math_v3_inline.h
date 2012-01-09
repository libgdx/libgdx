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

//=========================================================================

NEINLINE f32& neV3::operator[]( s32 I )
{
    ASSERT( I >= 0 );
    ASSERT( I <= 2 );
   // return ((f32*)this)[I];
	return v[I];
}

//=========================================================================

NEINLINE f32 neV3::operator [] ( s32 I ) const
{
    ASSERT( I >= 0 );
    ASSERT( I <= 2 );
	return v[I];
}

//=========================================================================

NEINLINE neV3 & neV3::Set( f32 x, f32 y, f32 z )
{
    this->v[0] = x; this->v[1] = y; this->v[2] = z;
	//m = _mm_set_ps(x, y, z, 0);
	return (*this);
}
/*
NEINLINE neV3 & neV3::operator =(const neV3 & V)
{ 
	_mm_store_ps(&v[0], V.m); 
	return (*this);
};
*/
NEINLINE neV3 & neV3::Set (const neV3& V)
{
	(*this) = V;
	return (*this);
}

NEINLINE neV3 & neV3::Set(const neQ& Q)
{
	v[0] = Q.X;
	v[1] = Q.Y;
	v[2] = Q.Z;

	return (*this);
}

NEINLINE void neV3::SetAbs(const neV3 & a)
{
	v[0] = neAbs(a[0]);
	v[1] = neAbs(a[1]);
	v[2] = neAbs(a[2]);
}

//=========================================================================

NEINLINE void neV3::Set( f32 val[3])
{
    this->v[0] = val[0]; this->v[1] = val[1]; this->v[2] = val[2];
}

//=========================================================================

NEINLINE void neV3::Get( f32 val[3])
{
    val[0] = X(); val[1] = Y(); val[2] = Z();
}

//=========================================================================

NEINLINE f32 neV3::Length( void ) const
{
	f32 dot = this->Dot( *this );

	if (neIsConsiderZero(dot))
		return 0.0f;

    return (f32)sqrtf( dot );
}

//=========================================================================

NEINLINE void neV3::Normalize( void )
{
	f32 l = Length();

	if (l == 0.0f)
	{
		SetZero();
	}
	else
	{
		*this *= 1.0f / Length();
	}
}

//=========================================================================

NEINLINE void neV3::RotateX( neRadian Rx )
{
    f32 s = (f32)sinf( Rx );
    f32 c = (f32)cosf( Rx );
    f32 y = v[1];
    f32 z = v[2];

    v[1]  = (c * y) - (s * z);
    v[2]  = (c * z) + (s * y);
}

//=========================================================================

NEINLINE void neV3::RotateY( neRadian Ry )
{
    f32 s = (f32)sinf( Ry );
    f32 c = (f32)cosf( Ry );
    f32 x = X();
    f32 z = Z();

    this->v[0] = (c * x) + (s * z);
    this->v[2] = (c * z) - (s * x);
}

//=========================================================================

NEINLINE void neV3::RotateZ( neRadian Rz )
{
    f32 s = (f32)sinf( Rz );
    f32 c = (f32)cosf( Rz );
    f32 x = X();
    f32 y = Y();

    this->v[0]  = (c * x) - (s * y);
    this->v[1]  = (c * y) + (s * x);
}

//=========================================================================

NEINLINE neV3 & neV3::SetZero( void )
{
    this->v[0] = this->v[1] = this->v[2] = 0.0f;

	return (*this);
}

NEINLINE neV3 & neV3::SetOne(void)
{
    this->v[0] = this->v[1] = this->v[2] = 1.0f;
	return (*this);
}

NEINLINE neV3 & neV3::SetHalf(void)
{
    this->v[0] = this->v[1] = this->v[2] = 0.5f;
	return (*this);
}

NEINLINE neV3 & neV3::Set(f32 value)
{
    this->v[0] = this->v[1] = this->v[2] = value;
	return (*this);
}

//=========================================================================

NEINLINE f32 neV3::Dot( const neV3& V ) const
{
    return  X() * V.X() + Y() * V.Y() + Z() * V.Z();
}

//=========================================================================

NEINLINE neV3 neV3::Cross( const neV3& V ) const
{
	neV3 tmp;

	tmp.v[0] = Y() * V.Z() - Z() * V.Y();
	tmp.v[1] = Z() * V.X() - X() * V.Z();
	tmp.v[2] = X() * V.Y() - Y() * V.X();

	return tmp;
}

//=========================================================================

NEINLINE neV3& neV3::operator += ( const neV3& V )
{
    v[0] += V.X(); v[1] += V.Y(); v[2] += V.Z();
    return *this;
}

//=========================================================================

NEINLINE neV3& neV3::operator -= ( const neV3& V )
{
    v[0] -= V.X(); 
	v[1] -= V.Y(); 
	v[2] -= V.Z();
    return *this;
}

//=========================================================================

NEINLINE neV3& neV3::operator /= ( f32 S )
{
    *this = *this / S;
    return *this;
}

//=========================================================================

NEINLINE neV3& neV3::operator *= ( f32 S )
{
    *this = *this * S;
    return *this;
}

//=========================================================================

NEINLINE neRadian neV3::GetPitch( void ) const
{
    return(f32) -atan2( Y(), (f32)sqrt( X()*X() + Z()*Z() ) );
}

//=========================================================================

NEINLINE neRadian neV3::GetYaw( void ) const
{
    return (f32)atan2( X(), Z() );
}

///////////////////////////////////////////////////////////////////////////
// VECTOR3 FRIEND FUNCTIONS
///////////////////////////////////////////////////////////////////////////

//=========================================================================

NEINLINE neV3 operator - ( const neV3& V )
{
	neV3 tmp;
    return tmp.Set( -V.X(), -V.Y(), -V.Z() );
}

//=========================================================================

NEINLINE neV3 operator + ( const neV3& V1, const neV3& V2 )
{
	neV3 tmp;

	//tmp.m = _mm_add_ps(V1.m, V2.m);

	//return tmp;
	
    return tmp.Set( V1.X() + V2.X(), V1.Y() + V2.Y(), V1.Z() + V2.Z() );
}

//=========================================================================

NEINLINE neV3 operator - ( const neV3& V1, const neV3& V2 )
{
	neV3 tmp;

	//tmp.m = _mm_sub_ps(V1.m, V2.m);

	//return tmp;
    return tmp.Set( V1.X() - V2.X(), V1.Y() - V2.Y(), V1.Z() - V2.Z() );
}

//=========================================================================

NEINLINE neV3 operator / ( const neV3& V, f32 S )
{
    return V * (1.0f/S);
}

//=========================================================================

NEINLINE neV3 operator * ( const neV3& V, const f32 S )
{
	neV3 tmp;
    return tmp.Set( V.X() * S, V.Y() * S, V.Z() * S );
}

//=========================================================================

NEINLINE neV3 operator * ( f32 S,  const neV3& V )
{
    return V * S;
}

//=========================================================================
NEINLINE void neV3::SetBoxTensor(f32 width, f32 height, f32 length, f32 mass)
{
	v[0] = mass * (length * length + height * height) / 12.0f;
	v[1] = mass * (width * width + height * height) / 12.0f;
	v[2] = mass * (length * length + width * width) / 12.0f;
}

//=========================================================================
NEINLINE void neV3::SetMin(const neV3& V1, const neV3& V2)
{
	(*this)[0] = (V1.X() < V2.X()) ? V1.X() : V2.X();
	(*this)[1] = (V1.Y() < V2.Y()) ? V1.Y() : V2.Y();
	(*this)[2] = (V1.Z() < V2.Z()) ? V1.Z() : V2.Z();
}
//=========================================================================
NEINLINE void neV3::SetMax(const neV3& V1, const neV3& V2)
{
	(*this)[0] = (V1.X() > V2.X()) ? V1.X() : V2.X();
	(*this)[1] = (V1.Y() > V2.Y()) ? V1.Y() : V2.Y();
	(*this)[2] = (V1.Z() > V2.Z()) ? V1.Z() : V2.Z();
}
//=========================================================================
NEINLINE neV3 operator *      ( const neV3& V,  const neM3&     M  )
{
	neV3 ret;

	ret[0] = V.Dot(M[0]);
	ret[1] = V.Dot(M[1]);
	ret[2] = V.Dot(M[2]);
	return ret;
}
//=========================================================================
NEINLINE neV3 operator * ( const neV3& V1, const neV3& V2 )
{
	neV3 ret;

	//ret.m = _mm_mul_ps(V1.m, V2.m);

	//return ret;

	ret[0] = V1[0] * V2[0];
	ret[1] = V1[1] * V2[1];
	ret[2] = V1[2] * V2[2];

	return ret;
}

NEINLINE bool neV3::IsConsiderZero() const
{
	return (neIsConsiderZero(v[0]) &&
			neIsConsiderZero(v[1]) &&
			neIsConsiderZero(v[2]));
}

NEINLINE bool neV3::IsFinite() const
{
	if (neFinite((double)v[0]) &&
		neFinite((double)v[1]) &&
		neFinite((double)v[2]))
		return true;
	return false;
}

NEINLINE f32 neV3::GetDistanceFromLine(const neV3 & pointA, const neV3 & pointB)
{
	neV3 ba = pointB - pointA;

	f32 len = ba.Length();

	if (neIsConsiderZero(len))
		ba.SetZero();
	else
		ba *= 1.0f / len;

	neV3 pa = (*this) - pointA;

	f32 k = pa.Dot(ba);

	neV3 q = pointA + k * ba;

	neV3 diff = (*this) - q;

	return diff.Length();
}

NEINLINE f32 neV3::GetDistanceFromLine2(neV3 & project, const neV3 & pointA, const neV3 & pointB)
{
	neV3 ba = pointB - pointA;

	f32 len = ba.Length();

	if (neIsConsiderZero(len))
		ba.SetZero();
	else
		ba *= 1.0f / len;

	neV3 pa = (*this) - pointA;

	f32 k = pa.Dot(ba);

	project = pointA + k * ba;

	neV3 diff = (*this) - project;

	return diff.Length();
}

NEINLINE f32 neV3::GetDistanceFromLineAndProject(neV3 & result, const neV3 & startPoint, const neV3 & dir)
{
	neV3 pa = (*this) - startPoint;

	f32 k = pa.Dot(dir);
	
	result = startPoint + k * dir;

	neV3 diff = (*this) - result;

	return diff.Length();
}

NEINLINE void neV3::RemoveComponent	(const neV3& V)
{
	f32 dot = (*this).Dot(V);

	(*this) = (*this) - V * dot;
}

NEINLINE neV3 neV3::Project(const neV3 & v) const
{
	f32 dot = (*this).Dot(v);

	return (v * dot);
}

NEINLINE neBool neV3::GetIntersectPlane(neV3 & normal, neV3 & pointOnPlane, neV3 & point1, neV3 & point2)
{
	neV3 diff = point2 - point1;

	f32 d2 = normal.Dot(diff);

	if (neIsConsiderZero(d2))
		return false;

	f32 d1 = normal.Dot(pointOnPlane - point1);

	f32 u = d1 / d2;

	*this = point1 + diff * u;

	return true;
}

NEINLINE f32 neV3::X() const
{
	return v[0];
}

NEINLINE f32 neV3::Y() const
{
	return v[1];
}

NEINLINE f32 neV3::Z() const
{
	return v[2];
}

NEINLINE f32 neV3::W() const
{
	return v[3];
}

///////////////////////////////////////////////////////////////////////////
// END
///////////////////////////////////////////////////////////////////////////

