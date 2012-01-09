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

/****************************************************************************
*
*	neM4
*
****************************************************************************/ 

NEINLINE void	neM4::Set(float row00, float row01, float row02, float row03
					, float row10, float row11, float row12, float row13
					, float row20, float row21, float row22, float row23
					, float row30, float row31, float row32, float row33)
{
    M[0][0] = row00; M[1][0] = row01; M[2][0] = row02; M[3][0] = row03;
    M[0][1] = row10; M[1][1] = row11; M[2][1] = row12; M[3][1] = row13;
    M[0][2] = row20; M[1][2] = row21; M[2][2] = row22; M[3][2] = row23;
    M[0][3] = row30; M[1][3] = row31; M[2][3] = row32; M[3][3] = row33;
}

//=========================================================================

NEINLINE void neM4::SetZero( void )
{
    M[0][0] = M[1][0] = M[2][0] = M[3][0] = 0;
    M[0][1] = M[1][2] = M[2][1] = M[3][1] = 0;
    M[0][2] = M[1][3] = M[2][3] = M[3][2] = 0;
    M[0][3] = M[1][1] = M[2][2] = M[3][3] = 0;
}

//=========================================================================

NEINLINE f32& neM4::operator[]( s32 I )
{
    ASSERT( I >= 0  );
    ASSERT( I < 4*4 );
    return ((f32*)this)[I];
}

//=========================================================================

NEINLINE void neM4::SetIdentity( void )
{
    M[0][1] = M[1][0] = M[2][0] = M[3][0] = 0;
    M[0][2] = M[1][2] = M[2][1] = M[3][1] = 0;
    M[0][3] = M[1][3] = M[2][3] = M[3][2] = 0;
    M[0][0] = M[1][1] = M[2][2] = M[3][3] = 1;
}

//=========================================================================

NEINLINE neV3 neM4::GetScale( void ) const
{
	neV3 tmp;
    return tmp.Set( M[0][0], M[1][1], M[2][2] );
}

//=========================================================================

NEINLINE void neM4::SetTranslation( const neV3& V )
{
    M[3][0] = V.X(); M[3][1] = V.Y(); M[3][2] = V.Z();
}

//=========================================================================

NEINLINE void neM4::SetScale( const neV3& V )
{
    M[0][0] = V.X(); M[1][1] = V.Y(); M[2][2] = V.Z();
}

//=========================================================================

NEINLINE neM4& neM4::operator *= ( const neM4& M )
{
    return (*this) = (*this) * M;
}

//=========================================================================

NEINLINE neV3 neM4::GetTranslation( void ) const
{
	neV3 tmp;
    return tmp.Set( M[3][0], M[3][1], M[3][2] );
}

//==============================================================================

NEINLINE void neM4::GetRows( neV3& V1, neV3& V2, neV3& V3 ) const
{
    V1.Set( M[0][0], M[1][0], M[2][0] );
    V2.Set( M[0][1], M[1][1], M[2][1] );
    V3.Set( M[0][2], M[1][2], M[2][2] ); 
}

//==============================================================================

NEINLINE void neM4::GetColumn(neV3& V1, u32 col) const
{
    V1.Set( M[col][0], M[col][1], M[col][2] );
	//V1.Set( M[0][row], M[1][row], M[2][row] );
}

//==============================================================================

NEINLINE void neM4::SetRows( const neV3& V1, const neV3& V2, const neV3& V3 )
{
    M[0][0] = V1.X();  M[1][0] = V1.Y();  M[2][0] = V1.Z();
    M[0][1] = V2.X();  M[1][1] = V2.Y();  M[2][1] = V2.Z();
    M[0][2] = V3.X();  M[1][2] = V3.Y();  M[2][2] = V3.Z();
}

//==============================================================================

NEINLINE void neM4::GetColumns( neV3& V1, neV3& V2, neV3& V3 ) const
{
    V1.Set( M[0][0], M[0][1], M[0][2] );
    V2.Set( M[1][0], M[1][1], M[1][2] );
    V3.Set( M[2][0], M[2][1], M[2][2] ); 
}

//==============================================================================

NEINLINE void neM4::SetColumns( const neV3& V1, const neV3& V2, const neV3& V3 )
{
    M[0][0] = V1.X();  M[0][1] = V1.Y();  M[0][2] = V1.Z();
    M[1][0] = V2.X();  M[1][1] = V2.Y();  M[1][2] = V2.Z();
    M[2][0] = V3.X();  M[2][1] = V3.Y();  M[2][2] = V3.Z();
}

//=========================================================================

NEINLINE neV3 neM4::TransformAs3x3( const neV3& V ) const
{
	neV3 tmp;
    return tmp.Set( M[0][0] * V.X() + M[1][0] * V.Y() + M[2][0] * V.Z(),
                     M[0][1] * V.X() + M[1][1] * V.Y() + M[2][1] * V.Z(),
                     M[0][2] * V.X() + M[1][2] * V.Y() + M[2][2] * V.Z() );
}

/****************************************************************************
*
*	friend functions
*
****************************************************************************/ 

//=========================================================================

NEINLINE neM4 operator * ( const neM4& M1, const neM4& M2 )
{
    neM4 Temp;
    f32*  L;
    f32*  R;

    L = (f32*)M1.M;
    R = (f32*)M2.M;

    for( f32* D = (f32*)&Temp.M[0]; D < (f32*)&Temp.M[4]; D+=4, R+=4 )
    {
        D[0] = L[0] * R[0] + L[4] * R[1] + L[8]  * R[2] + L[12] * R[3];
        D[1] = L[1] * R[0] + L[5] * R[1] + L[9]  * R[2] + L[13] * R[3];
        D[2] = L[2] * R[0] + L[6] * R[1] + L[10] * R[2] + L[14] * R[3];
        D[3] = L[3] * R[0] + L[7] * R[1] + L[11] * R[2] + L[15] * R[3];
    }

    return Temp;
}

//=========================================================================

NEINLINE neV3 operator * ( const neM4& M, const neV3& V )
{
	neV3 tmp;
    return tmp.Set( M.M[0][0] * V.X() + M.M[1][0] * V.Y() + M.M[2][0] * V.Z() + M.M[3][0],
                     M.M[0][1] * V.X() + M.M[1][1] * V.Y() + M.M[2][1] * V.Z() + M.M[3][1],
                     M.M[0][2] * V.X() + M.M[1][2] * V.Y() + M.M[2][2] * V.Z() + M.M[3][2] );
}

//=========================================================================

NEINLINE void neM4::SetFastInvert( const neM4& Src )
{
    f32     Determinant;
    neM4    & Matrix = (*this);

    //
    // Calculate the determinant.
    //
    Determinant = ( Src.M[0][0] * ( Src.M[1][1] * Src.M[2][2] - Src.M[1][2] * Src.M[2][1] ) -
                    Src.M[0][1] * ( Src.M[1][0] * Src.M[2][2] - Src.M[1][2] * Src.M[2][0] ) +
                    Src.M[0][2] * ( Src.M[1][0] * Src.M[2][1] - Src.M[1][1] * Src.M[2][0] ) );

    if( fabs( Determinant ) < 0.0001f ) 
    {
        Matrix.SetIdentity();
        //return Matrix; 
    }

    Determinant = 1.0f / Determinant;

    //
    // Find the inverse of the matrix.
    //
    Matrix.M[0][0] =  Determinant * ( Src.M[1][1] * Src.M[2][2] - Src.M[1][2] * Src.M[2][1] );
    Matrix.M[0][1] = -Determinant * ( Src.M[0][1] * Src.M[2][2] - Src.M[0][2] * Src.M[2][1] );
    Matrix.M[0][2] =  Determinant * ( Src.M[0][1] * Src.M[1][2] - Src.M[0][2] * Src.M[1][1] );
    Matrix.M[0][3] = 0.0f;

    Matrix.M[1][0] = -Determinant * ( Src.M[1][0] * Src.M[2][2] - Src.M[1][2] * Src.M[2][0] );
    Matrix.M[1][1] =  Determinant * ( Src.M[0][0] * Src.M[2][2] - Src.M[0][2] * Src.M[2][0] );
    Matrix.M[1][2] = -Determinant * ( Src.M[0][0] * Src.M[1][2] - Src.M[0][2] * Src.M[1][0] );
    Matrix.M[1][3] = 0.0f;

    Matrix.M[2][0] =  Determinant * ( Src.M[1][0] * Src.M[2][1] - Src.M[1][1] * Src.M[2][0] );
    Matrix.M[2][1] = -Determinant * ( Src.M[0][0] * Src.M[2][1] - Src.M[0][1] * Src.M[2][0] );
    Matrix.M[2][2] =  Determinant * ( Src.M[0][0] * Src.M[1][1] - Src.M[0][1] * Src.M[1][0] );
    Matrix.M[2][3] = 0.0f;

    Matrix.M[3][0] = -( Src.M[3][0] * Matrix.M[0][0] + Src.M[3][1] * Matrix.M[1][0] + Src.M[3][2] * Matrix.M[2][0] );
    Matrix.M[3][1] = -( Src.M[3][0] * Matrix.M[0][1] + Src.M[3][1] * Matrix.M[1][1] + Src.M[3][2] * Matrix.M[2][1] );
    Matrix.M[3][2] = -( Src.M[3][0] * Matrix.M[0][2] + Src.M[3][1] * Matrix.M[1][2] + Src.M[3][2] * Matrix.M[2][2] );
    Matrix.M[3][3] = 1.0f;

    //return Matrix;
}

//=========================================================================

NEINLINE neM4 neM4_BuildRotX( neRadian Rx )
{
    f32 s = (f32)sin( Rx );
    f32 c = (f32)cos( Rx );
    neM4 Temp;

    Temp.SetIdentity();

    Temp.M[1][1] = Temp.M[2][2] = c;
    Temp.M[2][1] = -s;
    Temp.M[1][2] =  s;

    return Temp;
}
    
//=========================================================================

NEINLINE neM4 neM4_BuildRotY( neRadian Ry )
{
    f32 s = (f32)sin( Ry );
    f32 c = (f32)cos( Ry );
    neM4 Temp;

    Temp.SetIdentity();
    Temp.M[0][0] = Temp.M[2][2] = c;
    Temp.M[0][2] = s;
    Temp.M[2][0] = - s;

    return Temp;
}

//=========================================================================

NEINLINE neM4 neM4_BuildRotZ( neRadian Rz )
{
    f32 s = (f32)sin( Rz );
    f32 c = (f32)cos( Rz );
    neM4 Temp;

    Temp.SetIdentity();
    Temp.M[0][0] = Temp.M[1][1] = c;
    Temp.M[0][1] =  s;
    Temp.M[1][0] = -s;

    return Temp;
}

//=========================================================================

NEINLINE neM4 neM4_BuildScale( const neV3 & Scale )
{
    neM4 Temp;

    Temp.SetIdentity();
    Temp.M[0][0] = Scale[0]; 
    Temp.M[1][1] = Scale[1]; 
    Temp.M[2][2] = Scale[2];

    return Temp;
}

//=========================================================================

NEINLINE neM4 neM4_BuildTranslate( const neV3 & Trans )
{
    neM4 Temp;

    Temp.SetIdentity();
    Temp.SetTranslation( Trans );

    return Temp;
}

//=========================================================================

NEINLINE neM4 neM4_BuildSkewSymmetric( const neV3& Vector )
{
    neM4 m;

    m.M[0][0] =  0;        m.M[0][1] =  Vector.Z(); m.M[0][2] = -Vector.Y();
    m.M[1][0] = -Vector.Z(); m.M[1][1] = 0;         m.M[1][2] =  Vector.X();
    m.M[2][0] =  Vector.Y(); m.M[2][1] = -Vector.X(); m.M[2][2] = 0;

    m.M[3][0] = m.M[3][1] = m.M[3][2] = 
    m.M[0][3] = m.M[1][3] = m.M[2][3] = 0;
    m.M[3][3] = 1;

    return m;
}

NEINLINE neM4& neM4::operator =( const neM3& m3 )
{
	int i, j;
	for (i = 0; i < 3; i++)
		for (j = 0; j < 3; j++)
			M[i][j] = m3.M[i][j];
	return *this;
}

NEINLINE void neM4::SetTranspose(const neM4 & M)
{
    neM4 m;

    m.M[0][0] = M.M[0][0];  
    m.M[1][0] = M.M[0][1];  
    m.M[2][0] = M.M[0][2]; 
    m.M[3][0] = M.M[0][3];

    m.M[0][1] = M.M[1][0];  
    m.M[1][1] = M.M[1][1];  
    m.M[2][1] = M.M[1][2]; 
    m.M[3][1] = M.M[1][3];

    m.M[0][2] = M.M[2][0];  
    m.M[1][2] = M.M[2][1];  
    m.M[2][2] = M.M[2][2];
    m.M[3][2] = M.M[2][3];

    m.M[0][3] = M.M[3][0];  
    m.M[1][3] = M.M[3][1];  
    m.M[2][3] = M.M[3][2];  
    m.M[3][3] = M.M[3][3];

	(*this) = m;
}
