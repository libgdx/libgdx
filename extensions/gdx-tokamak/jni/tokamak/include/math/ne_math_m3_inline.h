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

NEINLINE neV3&   neM3::operator   []       ( s32 I )
{
	ASSERT(I >= 0);
	ASSERT(I < 3);

	return M[I];
}

NEINLINE neV3	neM3::operator   []		( s32 I ) const
{
	ASSERT(I >= 0);
	ASSERT(I < 3);

	return M[I];
}

NEINLINE void neM3::SetZero( void )
{
    M[0][0] = 0.0f; 
	M[1][0] = 0.0f; 
	M[2][0] = 0.0f;
	M[0][1] = 0.0f;
	M[1][1] = 0.0f;
	M[2][1] = 0.0f;
	M[0][2] = 0.0f;
	M[1][2] = 0.0f;
	M[2][2] = 0.0f;
}

NEINLINE void neM3::SetIdentity( void )
{
    M[0][1] = M[1][0] = M[2][0] = 0.0f;
    M[0][2] = M[1][2] = M[2][1] = 0.0f;    
    M[0][0] = M[1][1] = M[2][2] = 1.0f;
}

NEINLINE neM3 & neM3::SetTranspose( neM3 & m )
{
	if (this == &m)
	{
		neSwap(M[1].v[0], M[0].v[1]);  
		neSwap(M[2].v[0], M[0].v[2]); 

		//neSwap(M[0].Y, M[1].X);  
		neSwap(M[2].v[1], M[1].v[2]); 

		//neSwap(M[0].Z, M[2].X);  
		//neSwap(M[1].Z, M[2].Y);  
	}
	else
	{
		M[0].v[0] = m.M[0].v[0];  
		M[1].v[0] = m.M[0].v[1];  
		M[2].v[0] = m.M[0].v[2]; 

		M[0].v[1] = m.M[1].v[0];  
		M[1].v[1] = m.M[1].v[1];  
		M[2].v[1] = m.M[1].v[2]; 

		M[0].v[2] = m.M[2].v[0];  
		M[1].v[2] = m.M[2].v[1];  
		M[2].v[2] = m.M[2].v[2];
	}
    return (*this);
}

NEINLINE neV3 neM3::TransposeMulV3(const neV3 & V)
{
	neV3 tmp;
	
	tmp[0] = M[0].v[0] * V.X() + M[0].v[1] * V.Y() + M[0].v[2] * V.Z();
	tmp[1] = M[1].v[0] * V.X() + M[1].v[1] * V.Y() + M[1].v[2] * V.Z();
	tmp[2] = M[2].v[0] * V.X() + M[2].v[1] * V.Y() + M[2].v[2] * V.Z();
	
	return tmp;
}

NEINLINE neM3 operator * ( const neM3& m1, const neM3& m2 )
{
	neM3 tmp;

	tmp.M[0].v[0] = m2.M[0].v[0] * m1.M[0].v[0] + m2.M[0].v[1] * m1.M[1].v[0] + m2.M[0].v[2] * m1.M[2].v[0];
	tmp.M[0].v[1] = m2.M[0].v[0] * m1.M[0].v[1] + m2.M[0].v[1] * m1.M[1].v[1] + m2.M[0].v[2] * m1.M[2].v[1];
	tmp.M[0].v[2] = m2.M[0].v[0] * m1.M[0].v[2] + m2.M[0].v[1] * m1.M[1].v[2] + m2.M[0].v[2] * m1.M[2].v[2];
	tmp.M[1].v[0] = m2.M[1].v[0] * m1.M[0].v[0] + m2.M[1].v[1] * m1.M[1].v[0] + m2.M[1].v[2] * m1.M[2].v[0];
	tmp.M[1].v[1] = m2.M[1].v[0] * m1.M[0].v[1] + m2.M[1].v[1] * m1.M[1].v[1] + m2.M[1].v[2] * m1.M[2].v[1];
	tmp.M[1].v[2] = m2.M[1].v[0] * m1.M[0].v[2] + m2.M[1].v[1] * m1.M[1].v[2] + m2.M[1].v[2] * m1.M[2].v[2];
	tmp.M[2].v[0] = m2.M[2].v[0] * m1.M[0].v[0] + m2.M[2].v[1] * m1.M[1].v[0] + m2.M[2].v[2] * m1.M[2].v[0];
	tmp.M[2].v[1] = m2.M[2].v[0] * m1.M[0].v[1] + m2.M[2].v[1] * m1.M[1].v[1] + m2.M[2].v[2] * m1.M[2].v[1];
	tmp.M[2].v[2] = m2.M[2].v[0] * m1.M[0].v[2] + m2.M[2].v[1] * m1.M[1].v[2] + m2.M[2].v[2] * m1.M[2].v[2];
/*	
	tmp.M[0][0] = m1[0][0] * m2[0][0] + m1[1][0] * m2[0][1] + m1[2][0] * m2[0][2];
	tmp.M[0][1] = m1[0][1] * m2[0][0] + m1[1][1] * m2[0][1] + m1[2][1] * m2[0][2];
	tmp.M[0][2] = m1[0][2] * m2[0][0] + m1[1][2] * m2[0][1] + m1[2][2] * m2[0][2];

	tmp.M[1][0] = m1[0][0] * m2[1][0] + m1[1][0] * m2[1][1] + m1[2][0] * m2[1][2];
	tmp.M[1][1] = m1[0][1] * m2[1][0] + m1[1][1] * m2[1][1] + m1[2][1] * m2[1][2];
	tmp.M[1][2] = m1[0][2] * m2[1][0] + m1[1][2] * m2[1][1] + m1[2][2] * m2[1][2];

	tmp.M[2][0] = m1[0][0] * m2[2][0] + m1[1][0] * m2[2][1] + m1[2][0] * m2[2][2];
	tmp.M[2][1] = m1[0][1] * m2[2][0] + m1[1][1] * m2[2][1] + m1[2][1] * m2[2][2];
	tmp.M[2][2] = m1[0][2] * m2[2][0] + m1[1][2] * m2[2][1] + m1[2][2] * m2[2][2];
*/
	return tmp;
}

NEINLINE neV3 operator * ( const neM3& m1, const neV3& v)
{
	neV3 tmp;
	
	tmp[0] = m1.M[0].v[0] * v.X() + m1.M[1].v[0] * v.Y() + m1.M[2].v[0] * v.Z();
	tmp[1] = m1.M[0].v[1] * v.X() + m1.M[1].v[1] * v.Y() + m1.M[2].v[1] * v.Z();
	tmp[2] = m1.M[0].v[2] * v.X() + m1.M[1].v[2] * v.Y() + m1.M[2].v[2] * v.Z();
	
	return tmp;
}

NEINLINE neM3& operator *= ( neM3& M1, const f32 f  )
{
	M1.M[0].v[0] *= f;
	M1.M[0].v[1] *= f;
	M1.M[0].v[2] *= f;
	M1.M[1].v[0] *= f;
	M1.M[1].v[1] *= f;
	M1.M[1].v[2] *= f;
	M1.M[2].v[0] *= f;
	M1.M[2].v[1] *= f;
	M1.M[2].v[2] *= f;
	
	return M1;
}

NEINLINE neM3& operator /= ( neM3& M1, const f32 f  )
{
	M1.M[0].v[0] /= f;
	M1.M[0].v[1] /= f;
	M1.M[0].v[2] /= f;
	M1.M[1].v[0] /= f;
	M1.M[1].v[1] /= f;
	M1.M[1].v[2] /= f;
	M1.M[2].v[0] /= f;
	M1.M[2].v[1] /= f;
	M1.M[2].v[2] /= f;
	
	return M1;
}

NEINLINE neM3& operator -= ( neM3& M1, const neM3& M2)
{
	M1.M[0].v[0] -= M2.M[0].v[0];
	M1.M[0].v[1] -= M2.M[0].v[1];
	M1.M[0].v[2] -= M2.M[0].v[2];
	M1.M[1].v[0] -= M2.M[1].v[0];
	M1.M[1].v[1] -= M2.M[1].v[1];
	M1.M[1].v[2] -= M2.M[1].v[2];
	M1.M[2].v[0] -= M2.M[2].v[0];
	M1.M[2].v[1] -= M2.M[2].v[1];
	M1.M[2].v[2] -= M2.M[2].v[2];
	
	return M1;
}

NEINLINE void neM3::GetColumns( neV3& V1, neV3& V2, neV3& V3 ) const
{
    V1.Set( M[0][0], M[0][1], M[0][2] );
    V2.Set( M[1][0], M[1][1], M[1][2] );
    V3.Set( M[2][0], M[2][1], M[2][2] ); 
}

NEINLINE void neM3::SetColumns( const neV3& V1, const neV3& V2, const neV3& V3 )
{
    M[0][0] = V1.X();  M[0][1] = V1.Y();  M[0][2] = V1.Z();
    M[1][0] = V2.X();  M[1][1] = V2.Y();  M[1][2] = V2.Z();
    M[2][0] = V3.X();  M[2][1] = V3.Y();  M[2][2] = V3.Z();
}

NEINLINE neBool neM3::IsIdentity() const
{
	if (!(neIsConsiderZero(M[0][0] - 1.0f) && 
		neIsConsiderZero(M[1][1] - 1.0f) &&
		neIsConsiderZero(M[2][2] - 1.0f)))
		return false;

	return (neIsConsiderZero(M[0][1]) && 
			neIsConsiderZero(M[0][2]) &&
			neIsConsiderZero(M[1][0]) &&
			neIsConsiderZero(M[1][2]) &&
			neIsConsiderZero(M[2][0]) &&
			neIsConsiderZero(M[2][1]));
}

NEINLINE neBool neM3::SetInvert(const neM3 & rhs)
{
	f32		det, invDet;

	M[2] = rhs.M[0].Cross ( rhs.M[1] );

	det = rhs.M[2].Dot(M[2]);

	if ( neAbs(det) < 1.0e-17f )
	{
		return false;
	}

	M[0] = rhs.M[1].Cross( rhs.M[2] );
	
	M[1] = rhs.M[2].Cross( rhs.M[0] );
	
	invDet = 1.0f / det;
	
	M[0] = M[0] * invDet;
	
	M[1] = M[1] * invDet;
	
	M[2] = M[2] * invDet;
	
	SetTranspose(*this);

	return true;
}

NEINLINE neM3 operator +	( const neM3& add1, const neM3& add2)
{
	neM3 sum;

	sum[0] = add1[0] + add2[0];
	sum[1] = add1[1] + add2[1];
	sum[2] = add1[2] + add2[2];

	return sum;
}

NEINLINE neM3 operator -	( const neM3& add1, const neM3& add2)
{
	neM3 sum;

	sum[0] = add1[0] - add2[0];
	sum[1] = add1[1] - add2[1];
	sum[2] = add1[2] - add2[2];

	return sum;
}

NEINLINE neM3 & neM3::operator +=	( const neM3& add)
{
	(*this)[0] += add[0];
	(*this)[1] += add[1];
	(*this)[2] += add[2];

	return (*this);
}

NEINLINE neV3 neM3::GetDiagonal()
{
	neV3 ret;

	ret[0] = M[0][0];
	ret[1] = M[1][1];
	ret[2] = M[2][2];

	return ret;
}

NEINLINE neM3 neM3::operator ^ (const neV3 & v) const
{
	neM3 ret;

	ret[0][0] = M[1][0] * v.Z() - M[2][0] * v.Y();
	ret[0][1] = M[1][1] * v.Z() - M[2][1] * v.Y();
	ret[0][2] = M[1][2] * v.Z() - M[2][2] * v.Y();

	ret[1][0] = M[0][0] * -v.Z() + M[2][0] * v.X();
	ret[1][1] = M[0][1] * -v.Z() + M[2][1] * v.X();
	ret[1][2] = M[0][2] * -v.Z() + M[2][2] * v.X(); 
	
	ret[2][0] = M[0][0] * v.Y() - M[1][0] * v.X();
	ret[2][1] = M[0][1] * v.Y() - M[1][1] * v.X();
	ret[2][2] = M[0][2] * v.Y() - M[1][2] * v.X();

	return ret;
}

NEINLINE neM3 operator ^ (const neV3 & v, const neM3 & matrix)
{
	neM3 ret;

	ret[0][0] = -v.Z() * matrix[0][1] + v.Y() * matrix[0][2];
	ret[0][1] =  v.Z() * matrix[0][0] - v.X() * matrix[0][2];
	ret[0][2] = -v.Y() * matrix[0][0] + v.X() * matrix[0][1];

	ret[1][0] = -v.Z() * matrix[1][1] + v.Y() * matrix[1][2]; 
	ret[1][1] =  v.Z() * matrix[1][0] - v.X() * matrix[1][2]; 
	ret[1][2] = -v.Y() * matrix[1][0] + v.X() * matrix[1][1]; 
	
	ret[2][0] = -v.Z() * matrix[2][1] + v.Y() * matrix[2][2]; 
	ret[2][1] =  v.Z() * matrix[2][0] - v.X() * matrix[2][2]; 
	ret[2][2] = -v.Y() * matrix[2][0] + v.X() * matrix[2][1]; 

	return ret;
}

NEINLINE neBool neM3::IsOrthogonalNormal() const
{
	neV3 cross;

	const neM3 & me = (*this);

	f32 m = me[0].Length();

	if (!neIsConsiderZero(m - 1.0f))
		return false;

	m = me[1].Length();

	if (!neIsConsiderZero(m - 1.0f))
		return false;

	m = me[2].Length();

	if (!neIsConsiderZero(m - 1.0f))
		return false;

	cross = me[0].Cross(me[1]);

	f32 dot = cross.Dot(me[2]);

	if (!neIsConsiderZero(dot - 1.0f))
		return false;

	return true;
}

NEINLINE void neM3::RotateXYZ(const neV3 & rotate)
{
	neM3 rx, ry, rz;

	f32 sintheta, costheta;

	sintheta = sinf(rotate[0]);
	costheta = cosf(rotate[0]);
	
	rx[0].Set(1.0f, 0.0f, 0.0f);
	rx[1].Set(0.0f, costheta, sintheta);
	rx[2].Set(0.0f,-sintheta, costheta);

	sintheta = sinf(rotate[1]);
	costheta = cosf(rotate[1]);

	ry[0].Set(costheta, 0.0f, -sintheta);
	ry[1].Set(0.0f, 1.0f, 0.0f);
	ry[2].Set(sintheta, 0.0f, costheta);

	sintheta = sinf(rotate[2]);
	costheta = cosf(rotate[2]);

	rz[0].Set(costheta, sintheta, 0.0f);
	rz[1].Set(-sintheta, costheta, 0.0f);
	rz[2].Set(0.0f, 0.0f, 1.0f);

	(*this) = rz * ry * rx;
}

NEINLINE neM3 neM3::operator * (f32 scalar) const
{
	neM3 ret;

	ret[0] = (*this)[0] * scalar;
	ret[1] = (*this)[1] * scalar;
	ret[2] = (*this)[2] * scalar;
	return ret;
}

NEINLINE neM3 operator * ( f32 scalar, const neM3& M )
{
	neM3 ret;

	ret[0] = M[0] * scalar;
	ret[1] = M[1] * scalar;
	ret[2] = M[2] * scalar;
	return ret;
}

NEINLINE neM3 & neM3::SkewSymmetric(const neV3 & V)
{
	(*this)[0].Set(0.0f, V[2], -V[1]);
	
	(*this)[1].Set(-V[2], 0.0f, V[0]);
	
	(*this)[2].Set(V[1], -V[0], 0.0f);

	return (*this);
}

neBool neM3::IsFinite() const
{
	return ((*this)[0].IsFinite() &&
			(*this)[1].IsFinite() &&
			(*this)[2].IsFinite() );
}

#ifdef USE_OPCODE

NEINLINE neV3 & neV3::operator = (const IceMaths::Point & pt)
{
	(*this)[0] = pt[0];
	(*this)[1] = pt[1];
	(*this)[2] = pt[2];

	return (*this);
}

NEINLINE IceMaths::Point& neV3::AssignIcePoint(IceMaths::Point & pt) const
{
	pt[0] = (*this)[0];
	pt[1] = (*this)[1];
	pt[2] = (*this)[2];
	return pt;
}

#endif //USE_OPCODE