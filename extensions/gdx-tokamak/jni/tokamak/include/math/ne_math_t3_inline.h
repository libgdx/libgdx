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
// neT3 
///////////////////////////////////////////////////////////////////////////

//=========================================================================

NEINLINE neT3 neT3::operator * (const neT3 & t)
{
	neT3 ret;

	ret.rot.M[0][0] = rot.M[0][0] * t.rot.M[0][0] + rot.M[1][0] * t.rot.M[0][1] + rot.M[2][0] * t.rot.M[0][2];
	ret.rot.M[0][1] = rot.M[0][1] * t.rot.M[0][0] + rot.M[1][1] * t.rot.M[0][1] + rot.M[2][1] * t.rot.M[0][2];
	ret.rot.M[0][2] = rot.M[0][2] * t.rot.M[0][0] + rot.M[1][2] * t.rot.M[0][1] + rot.M[2][2] * t.rot.M[0][2];

	ret.rot.M[1][0] = rot.M[0][0] * t.rot.M[1][0] + rot.M[1][0] * t.rot.M[1][1] + rot.M[2][0] * t.rot.M[1][2];
	ret.rot.M[1][1] = rot.M[0][1] * t.rot.M[1][0] + rot.M[1][1] * t.rot.M[1][1] + rot.M[2][1] * t.rot.M[1][2];
	ret.rot.M[1][2] = rot.M[0][2] * t.rot.M[1][0] + rot.M[1][2] * t.rot.M[1][1] + rot.M[2][2] * t.rot.M[1][2];

	ret.rot.M[2][0] = rot.M[0][0] * t.rot.M[2][0] + rot.M[1][0] * t.rot.M[2][1] + rot.M[2][0] * t.rot.M[2][2];
	ret.rot.M[2][1] = rot.M[0][1] * t.rot.M[2][0] + rot.M[1][1] * t.rot.M[2][1] + rot.M[2][1] * t.rot.M[2][2];
	ret.rot.M[2][2] = rot.M[0][2] * t.rot.M[2][0] + rot.M[1][2] * t.rot.M[2][1] + rot.M[2][2] * t.rot.M[2][2];

	ret.pos.v[0] = rot.M[0][0] * t.pos.v[0] + rot.M[1][0] * t.pos.v[1] + rot.M[2][0] * t.pos.v[2] + pos.v[0];
	ret.pos.v[1] = rot.M[0][1] * t.pos.v[0] + rot.M[1][1] * t.pos.v[1] + rot.M[2][1] * t.pos.v[2] + pos.v[1];
	ret.pos.v[2] = rot.M[0][2] * t.pos.v[0] + rot.M[1][2] * t.pos.v[1] + rot.M[2][2] * t.pos.v[2] + pos.v[2];

/*
	ret.rot[0] = rot[0] * t.rot.M[0][0] + rot[1] * t.rot.M[0][1] + rot[2] * t.rot.M[0][2];
	ret.rot[1] = rot[0] * t.rot.M[1][0] + rot[1] * t.rot.M[1][1] + rot[2] * t.rot.M[1][2];
	ret.rot[2] = rot[0] * t.rot.M[2][0] + rot[1] * t.rot.M[2][1] + rot[2] * t.rot.M[2][2];
	
	ret.pos = rot[0] * t.pos[0] + rot[1] * t.pos[1] + rot[2] * t.pos[2] + pos;
*/
	return ret;
}

NEINLINE neV3 neT3::operator * (const neV3 & v)
{
	return rot * v + pos;
}

NEINLINE neT3 neT3::FastInverse()
{
	neT3 ret;

	ret.rot.SetTranspose(rot);

	neV3 tpos = ret.rot * pos;

	ret.pos = -tpos;

	return ret;
}
	
#ifdef USE_OPCODE

NEINLINE neT3 & neT3::operator = (const IceMaths::Matrix4x4 & mat)
{
	(*this).rot[0][0] = mat.m[0][0];
	(*this).rot[0][1] = mat.m[0][1];
	(*this).rot[0][2] = mat.m[0][2];

	(*this).rot[1][0] = mat.m[1][0];
	(*this).rot[1][1] = mat.m[1][1];
	(*this).rot[1][2] = mat.m[1][2];

	(*this).rot[2][0] = mat.m[2][0];
	(*this).rot[2][1] = mat.m[2][1];
	(*this).rot[2][2] = mat.m[2][2];

	(*this).pos[0] = mat.m[3][0];
	(*this).pos[1] = mat.m[3][1];
	(*this).pos[2] = mat.m[3][2];


	return (*this);
}

NEINLINE IceMaths::Matrix4x4 & neT3::AssignIceMatrix(IceMaths::Matrix4x4 & mat) const
{
	mat.m[0][0] = (*this).rot[0][0];
	mat.m[0][1] = (*this).rot[0][1];
	mat.m[0][2] = (*this).rot[0][2];
	mat.m[0][3] = 0.0f;

	mat.m[1][0] = (*this).rot[1][0];
	mat.m[1][1] = (*this).rot[1][1];
	mat.m[1][2] = (*this).rot[1][2];
	mat.m[1][3] = 0.0f;

	mat.m[2][0] = (*this).rot[2][0];
	mat.m[2][1] = (*this).rot[2][1];
	mat.m[2][2] = (*this).rot[2][2];
	mat.m[2][3] = 0.0f;

	mat.m[3][0] = (*this).pos[0];
	mat.m[3][1] = (*this).pos[1];
	mat.m[3][2] = (*this).pos[2];
	mat.m[3][3] = 1.0f;

	return mat;
}

neBool neT3::IsFinite()
{
	return ((*this).rot.IsFinite() && (*this).pos.IsFinite());
}

#endif //USE_OPCODE