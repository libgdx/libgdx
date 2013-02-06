/*************************************************************************
 *                                                                       *
 * Tokamak Physics Engine, Copyright (C) 2002-2007 David Lam.            *
 * All rights reserved.  Email: david@tokamakphysics.com                 *
 *                       Web: www.tokamakphysics.com                     *
 *                                                                       *
 * This library is free software; you can redistribute it and/or         *
 * modify it under the terms of EITHER:                                  *
 *   (1) The GNU Lesser General Public License as published by the Free  *
 *       Software Foundation; either version 2.1 of the License, or (at  *
 *       your option) any later version. The text of the GNU Lesser      *
 *       General Public License is included with this library in the     *
 *       file LICENSE.TXT.                                               *
 *   (2) The BSD-style license that is included with this library in     *
 *       the file LICENSE-BSD.TXT.                                       *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * LICENSE.TXT and LICENSE-BSD.TXT for more details.                     *
 *                                                                       *
 *************************************************************************/

#include "stdio.h"
/*
#ifdef _WIN32
#include <windows.h>
#endif
*/
#include "tokamak.h"
#include "containers.h"
#include "scenery.h"
#include "collision.h"
#include "collision2.h"
#include "constraint.h"
#include "rigidbody.h"
#include "scenery.h"
#include "stack.h"
#include "simulator.h"
#include "message.h"
#include "dcd.h"

s32 currentMicroStep = 0;

extern f32 CONSTRAINT_THESHOLD_JOINT;

extern f32 CONSTRAINT_THESHOLD_CONTACT;

extern f32 CONSTRAINT_THESHOLD_LIMIT;

extern f32 CONSTRAINT_CONVERGE_FACTOR_JOINT;

extern f32 CONSTRAINT_CONVERGE_FACTOR_CONTACT;

extern f32 CONSTRAINT_CONVERGE_FACTOR_LIMIT;

s32 magicN;

//extern void DrawLine(const neV3 & colour, neV3 * startpoint, s32 count);

//#pragma inline_recursion( on )
//#pragma inline_depth( 50 )

neBool neCollisionResult::CheckIdle()
{
	f32 theshold = 1.0f;

	if (relativeSpeed > theshold)
		return false;

	neRigidBody_* ba = bodyA->AsRigidBody();

	neRigidBody_* bb = NULL;
	
	if (bodyB)
	{
		bb = bodyB->AsRigidBody();
	}

	if (ba && ba->status == neRigidBody_::NE_RBSTATUS_IDLE)
	{
		if (bb)
		{
			if (bb->status == neRigidBody_::NE_RBSTATUS_IDLE)
			{
				return true;
			}
			else
			{
				bodyA = NULL;

				return false;
			}
		}
		else
		{
			return true;
		}
	}
	else if (bb && bb->status == neRigidBody_::NE_RBSTATUS_IDLE)
	{
		if (ba)
		{
			bodyB = NULL;

			return false;
		}
	}
	return false;
}

void neCollisionResult::StartStage2()
{
	if (impulseType != IMPULSE_CONTACT)
		return;

	//f32 timeStep;

	neV3 relVel;

	if (bodyA)
	{
		relVel = bodyA->VelocityAtPoint(contactA) * 1.0f;

		//timeStep = bodyA->sim->currentTimeStep;

		if (bodyA->AsRigidBody())
			bodyA->AsRigidBody()->needRecalc = true;
	}

	if (bodyB)
	{
		relVel -= bodyB->VelocityAtPoint(contactB);

		//timeStep = bodyB->sim->currentTimeStep;

		if (bodyB->AsRigidBody())
			bodyB->AsRigidBody()->needRecalc = true;
	}
	contactBWorld = w2c * relVel;

	contactBWorld[2] = 0.0f;
	
	//contactBWorld.RemoveComponent(collisionFrame[2]);

	//contactBWorld.SetZero();
}

void neCollisionResult::UpdateConstraintRelativeSpeed()
{
	ASSERT(impulseType == IMPULSE_CONTACT);

	neV3 relVel;

	relVel.SetZero();

	s32 solverStage;

	if (bodyA)
	{
		relVel = bodyA->VelocityAtPoint(contactA) * -1.0f;

		solverStage = bodyA->sim->solverStage;
	}

	if (bodyB)
	{
		relVel += bodyB->VelocityAtPoint(contactB);

		solverStage = bodyB->sim->solverStage;
	}
	if (solverStage != 2)
	{
		ASSERT(FALSE);

		return;
	}
	relativeSpeed = relVel.Dot(collisionFrame[2]);

	if (relativeSpeed < 0.0f)
	{
		relativeSpeed = 0.0f;
	}
}

void neCollisionResult::CalcCollisionMatrix(neRigidBody_* ba, neRigidBody_ * bb, neBool isWorld)
{
	neM3 zero;

	zero.SetZero();

	neM3 * IinvAW; 
	
	f32 oneOnMassA;

	if (!ba)
	{
		oneOnMassA = 0.0f;

		IinvAW = &zero;//.SetZero();
	}
	else
	{
		IinvAW = &ba->Derive().Iinv;

		oneOnMassA = ba->oneOnMass;
	}
	neM3 * IinvBW;

	f32 oneOnMassB;

	if (!bb)
	{
		oneOnMassB = 0.0f;

		IinvBW = &zero;//.SetZero();
	}
	else
	{
		IinvBW = &bb->Derive().Iinv;

		oneOnMassB = bb->oneOnMass;
	}

	k.SetIdentity();

	//k *= (oneOnMassA + oneOnMassB);
	f32 oom = oneOnMassA + oneOnMassB;

	k[0][0] = oom;
	k[1][1] = oom;
	k[2][2] = oom;

	if (isWorld)
	{
		neM3 tmp = contactA ^ (*IinvAW) ^ contactA;

		k = k - tmp;

		tmp = contactB ^ (*IinvBW) ^ contactB;

		k = k - tmp;
	}
	else
	{
		//neM3 w2c; 

		//w2c.SetTranspose(collisionFrame);

		neV3 pointA; pointA = w2c * contactA;

		neV3 pointB; pointB = w2c * contactB;

		neM3 IinvAC; IinvAC = w2c * (*IinvAW) * collisionFrame;

		neM3 IinvBC; IinvBC = w2c * (*IinvBW) * collisionFrame;

		neM3 tmp = pointA ^ IinvAC ^ pointA;

		k = k - tmp;

		tmp = pointB ^ IinvBC ^ pointB;

		k = k - tmp;
	}
	kInv.SetInvert(k);

	ASSERT(kInv.IsFinite());
}

void neCollisionResult::CalcCollisionMatrix2(neRigidBody_* ba, neRigidBody_ * bb)
{
	k.SetZero();

	if (ba)
		k = ba->Derive().Iinv;

	if (bb)
		k += bb->Derive().Iinv;

	kInv.SetInvert(k);
}

void neCollisionResult::CalcCollisionMatrix3(neRigidBody_* ba, neRigidBody_ * bb)
{
	neM3 kk;

	kk.SetZero();

	neM3 ii;

	ii = bb->GetB2W().rot * k;

	neM3 kTrans; kTrans.SetTranspose(ii);

	if (ba)
		kk = ii * ba->Derive().Iinv * kTrans;

	if (bb)
		kk += bb->Derive().Iinv;

	kInv.SetInvert(kk);
}

void neCollisionResult::PrepareForSolver(neBool aIdle, neBool bIdle)
{
	neRigidBody_ * ba = NULL;

	neRigidBody_ * bb = NULL;

	if (bodyA && bodyA->AsRigidBody() && !aIdle)
	{
		ba = bodyA->AsRigidBody();
	}
	if (bodyB && bodyB->AsRigidBody() && !bIdle)
	{
		bb = bodyB->AsRigidBody();
	}

	switch (impulseType)
	{
	case IMPULSE_NORMAL:
	case IMPULSE_CONTACT:
		{
			ChooseAxis(collisionFrame[0], collisionFrame[1], collisionFrame[2]);

			ASSERT(collisionFrame.IsFinite());

			w2c.SetTranspose(collisionFrame);

			CalcCollisionMatrix(ba, bb, false);
		}
		break;

	case IMPULSE_CONSTRAINT:
	case IMPULSE_SLIDER:
	case IMPULSE_SLIDER_LIMIT_PRIMARY:
		{
			CalcCollisionMatrix(ba, bb, true);
		}
		
		break;
	case IMPULSE_ANGULAR_LIMIT_PRIMARY:
	case IMPULSE_ANGULAR_MOTOR_PRIMARY:
		{
			CalcCollisionMatrix2(ba, bb);
		}
		break;
	
	case IMPULSE_ANGULAR_LIMIT_SECONDARY:
		{
			CalcCollisionMatrix3(ba, bb);
		}
		break;

	case IMPULSE_RELATIVE_LINEAR_VELOCITY:
		{
			f32 oneOnMassA, oneOnMassB;

			if (!ba)
			{
				oneOnMassA = 0.0f;
			}
			else
			{
				oneOnMassA = ba->oneOnMass;
			}
			if (!bb)
			{
				oneOnMassB = 0.0f;
			}
			else
			{
				oneOnMassB = bb->oneOnMass;
			}
			kInv[0][0] = 1.0f / (oneOnMassA + oneOnMassB);
		}	
		break;
	}
}

void neCollision::CalcBB()
{
	s32 i;
	
	boundingRadius = 0.0f;	

	if (convexCount == 0)
		return;

	TConvexItem * gi = (TConvexItem *) convex;

	while (gi)
	{
		TConvex * g = (TConvex *)gi;

		gi = gi->next;

		f32 r = g->GetBoundRadius();

		if (r > boundingRadius)
			boundingRadius = r;
	}

	if (convexCount == 1 && (convex->type == TConvex::BOX))
	{
		obb = *convex;
		//obb.as.box.boxSize *= 1.5f;
	}
	else
	{
		neV3 maxExt, minExt;

		maxExt.Set(-1.0e6f, -1.0e6f, -1.0e6f);
		minExt.Set(1.0e6f, 1.0e6f, 1.0e6f);

		neV3 _maxExt, _minExt;

		gi = (TConvexItem *) convex;
		
		while (gi)
		{
			TConvex * g = (TConvex *)gi;

			gi = gi->next;

			g->GetExtend(_minExt, _maxExt);

			for (s32 j = 0; j < 3; j++)
			{
				maxExt[j] = neMax(maxExt[j], _maxExt[j]);
				minExt[j] = neMin(minExt[j], _minExt[j]);
			}
		}

		obb.c2p.rot.SetIdentity();

		for (i = 0; i < 3; i++)
		{
			obb.as.box.boxSize[i] = ( maxExt[i] - minExt[i] ) * 0.5f;
			obb.c2p.pos[i] = minExt[i] + obb.as.box.boxSize[i];
		}
	}
}

void TConvex::GetExtend(neV3 & minExt, neV3 & maxExt)
{
	s32 i;

	switch (GetType())
	{
	case TConvex::BOX:
		for (i = 0; i < 3; i++)
		{
			maxExt[i] = neAbs(c2p.rot[0][i]) * as.box.boxSize[0] +
							neAbs(c2p.rot[1][i]) * as.box.boxSize[1] +
							neAbs(c2p.rot[2][i]) * as.box.boxSize[2] +
							c2p.pos[i];
			minExt[i] = -neAbs(c2p.rot[0][i]) * as.box.boxSize[0] +
							-neAbs(c2p.rot[1][i]) * as.box.boxSize[1] +
							-neAbs(c2p.rot[2][i]) * as.box.boxSize[2] +
							c2p.pos[i];
		}
		break;
	
	case TConvex::SPHERE:
		{
			neV3 rad;

			rad.Set(Radius());

			maxExt = c2p.pos + rad;

			minExt = c2p.pos - rad;
		}
		break;
	
	case TConvex::CYLINDER:
		for (i = 0; i < 3; i++)
		{
			maxExt[i] = neAbs(c2p.rot[0][i]) * CylinderRadius() +
							neAbs(c2p.rot[1][i]) * (CylinderHalfHeight() + CylinderRadius()) +
							neAbs(c2p.rot[2][i]) * CylinderRadius() +
							c2p.pos[i];
			minExt[i] = -neAbs(c2p.rot[0][i]) * CylinderRadius() +
							-neAbs(c2p.rot[1][i]) * (CylinderHalfHeight() + CylinderRadius()) +
							-neAbs(c2p.rot[2][i]) * CylinderRadius() +
							c2p.pos[i];
		}
		break;

#ifdef USE_OPCODE
	
	case TConvex::OPCODE_MESH:
	{
		IceMaths::Point minex, maxex;

		minex.Set(1.0e6f);

		maxex.Set(-1.0e6f);

		for (u32 kk = 0; kk < as.opcodeMesh.vertCount; kk++)
		{
			minex = minex.Min(as.opcodeMesh.vertices[kk]);

			maxex = maxex.Max(as.opcodeMesh.vertices[kk]);
		}
		minExt = minex;

		maxExt = maxex;
	}
	break;

#endif //USE_OPCODE

	case TConvex::CONVEXITY:
	{
		neV3 minex; minex.Set(1.0e6f);

		neV3 maxex; maxex.Set(-1.0e6f);

		for (s32 kk = 0; kk < as.convexMesh.vertexCount; kk++)
		{
			minex.SetMin(minex, as.convexMesh.vertices[kk]);

			maxex.SetMax(maxex, as.convexMesh.vertices[kk]);
		}
		minExt = minex * (2.0f + envelope);

		maxExt = maxex * (2.0f + envelope);
	}
	break;

	case TConvex::CONVEXDCD:
	{
		neV3 minex; minex.Set(1.0e6f);

		neV3 maxex; maxex.Set(-1.0e6f);

		for (s32 kk = 0; kk < as.convexDCD.numVerts; kk++)
		{
			minex.SetMin(minex, as.convexDCD.vertices[kk]);

			maxex.SetMax(maxex, as.convexDCD.vertices[kk]);
		}
		minExt = minex * (2.0f);

		maxExt = maxex * (2.0f);
	}
	break;

	default:
		ASSERT(0);
	}
}

f32 TConvex::GetBoundRadius()
{
	f32 extend = 0.0f;

	switch (GetType())
	{
	case TConvex::BOX:
		{
			neV3 v3;
			
			v3.Set(as.box.boxSize[0], as.box.boxSize[1], as.box.boxSize[2]);
			
			extend = v3.Length();

			extend += c2p.pos.Length();
		}
		break;
	case TConvex::SPHERE:
		extend = c2p.pos.Length() + as.sphere.radius;
		break;

	case TConvex::CYLINDER:
		//extend = c2p.pos.Length() + sqrtf(CylinderRadiusSq() + CylinderHalfHeight() * CylinderHalfHeight());
		{
			f32 r = CylinderRadiusSq() + CylinderHalfHeight();
			
			extend = c2p.pos.Length() + r;
		}
		break;

	case TConvex::CONVEXITY:
		{
			for (s32 i = 0; i < as.convexMesh.vertexCount; i++)
			{
				f32 l = as.convexMesh.vertices[i].Length();

				if (l > extend)
				{
					extend = l;
				}
			}
			extend *= (1.0f + envelope);
		}
		break;

	case TConvex::CONVEXDCD:
		{
			for (s32 i = 0; i < as.convexMesh.vertexCount; i++)
			{
				f32 l = as.convexMesh.vertices[i].Length();

				if (l > extend)
				{
					extend = l;
				}
			}
		}
		break;

#ifdef USE_OPCODE
		
	case TConvex::OPCODE_MESH:
	{		
		for (u32 kk = 0; kk < as.opcodeMesh.vertCount; kk++)
		{
			f32 tmp = as.opcodeMesh.vertices[kk].Magnitude();

			if (tmp > extend)
				extend = tmp;
		}
	}
	break;

#endif //USE_OPCODE

	default:
		//fprintf(stderr, "TConvex::GetExtend - error: unrecongised primitive type\n");
		ASSERT(0);
		break;
	}
	return extend;
}

void TConvex::SetBoxSize(f32 width, f32 height, f32 depth)
{
	type = TConvex::BOX;
	as.box.boxSize[0] = width / 2.0f;
	as.box.boxSize[1] = height / 2.0f;
	as.box.boxSize[2] = depth / 2.0f;

	boundingRadius = as.box.boxSize.Length();

	envelope = 0.0f;
}

void TConvex::SetSphere(f32 radius)
{
	type = TConvex::SPHERE;

	as.sphere.radius = radius;

	as.sphere.radiusSq = radius * radius;

	boundingRadius = radius;

	envelope = 0.0f;
}

void TConvex::SetConvexMesh(neByte * convexData)
{
	type = TConvex::CONVEXDCD;
	
	as.convexDCD.convexData = convexData;

	s32 numFace = *((s32*)convexData);

	as.convexDCD.numVerts =  *((s32*)convexData + 1);

	as.convexDCD.vertices = (neV3 *)(convexData + numFace * sizeof(f32) * 4);
}

void TConvex::SetTriangle(s32 a, s32 b, s32 c, neV3 * _vertices)
{
	type = TConvex::TRIANGLE;

	as.tri.indices[0] = a;
	as.tri.indices[1] = b;
	as.tri.indices[2] = c;
	vertices = _vertices;
}

void TConvex::SetTerrain(neSimpleArray<s32> & triangleIndex, neArray<neTriangle_> & triangles, neV3 * _vertices)
{
	type = TConvex::TERRAIN;

	as.terrain.triangles = &triangles;
	as.terrain.triIndex = &triangleIndex;
	vertices = _vertices;
}


#ifdef USE_OPCODE

void TConvex::SetOpcodeMesh(IndexedTriangle * triIndex, u32 triCount, IceMaths::Point * vertArray, u32 vertCount)
{
	type = TConvex::OPCODE_MESH;
	
	as.opcodeMesh.triIndices = triIndex;
	as.opcodeMesh.triCount = triCount;
	as.opcodeMesh.vertices = vertArray;
	as.opcodeMesh.vertCount = vertCount;
}

#endif //USE_OPCODE

void TConvex::SetMaterialId(s32 index)
{
	matIndex = index;
}
/*
void TConvex::SetId(s32 _id)
{
	id = _id;
}

s32	 TConvex::GetId()
{
	return id;
}
*/
s32	 TConvex::GetMaterialId()
{
	return matIndex;
}

u32	TConvex::GetType()
{
	return type;
}

void TConvex::SetTransform(neT3 & t3)
{
	c2p = t3;
}

neT3 TConvex::GetTransform()
{
	switch (GetType())
	{
	case TConvex::BOX: 
	case TConvex::CYLINDER: 
	case TConvex::SPHERE: 

#ifdef USE_OPCODE
		
	case TConvex::OPCODE_MESH:

#endif //USE_OPCODE

		return c2p;
		break;
	default:
		ASSERT(1);
	}

	neT3 ret;

	ret.SetIdentity();
	
	return ret;
}

void TConvex::Initialise()
{
	SetBoxSize(1.0f, 1.0f, 1.0f);
	neT3 t;
	t.SetIdentity();
	SetTransform(t);
	matIndex = 0;
	//id = 0;
	userData = 0;

	breakInfo.mass = 1.0f;
	breakInfo.inertiaTensor = neBoxInertiaTensor(1.0f, 1.0f, 1.0f, 1.0f);
	breakInfo.breakMagnitude = 0.0f;
	breakInfo.breakAbsorb = 0.5f;
	breakInfo.neighbourRadius = 0.0f;
	breakInfo.flag = neGeometry::NE_BREAK_DISABLE; //break all,
	breakInfo.breakPlane.SetZero();
}

/****************************************************************************
*
*	TConvex::CalcInertiaTensor
*
****************************************************************************/ 
void TranslateCOM(neM3 & I, neV3 &translate, f32 mass, f32 factor)
{
	s32	i,j,k;
	f32	change;

	for(i=0;i<3;i++)
	{
		for(j=i;j<3;j++)
		{
			if(i==j)
			{
				change = 0.0f;
				for(k=0;k<3;k++)

				{
					if(k!=i)
					{
						change += (translate[k] * translate[k]);
					}
				}
			}
			else
			{
				change += (translate[i] * translate[j]);
			}
			change *= mass;

			change *= factor;

			I[j][i] += change;
			
			if (i != j)
				I[i][j] += change;
		}
	}
	return;
}

neM3 TConvex::CalcInertiaTensor(f32 density, f32 & mass)
{
	neM3 ret;

	ret.SetZero();

	switch (GetType())
	{
	case TConvex::BOX:
	{
		f32	xsq = as.box.boxSize[0];
		f32	ysq = as.box.boxSize[1];
		f32	zsq = as.box.boxSize[2];

		xsq *= xsq;
		ysq *= ysq;
		zsq *= zsq;

		mass = as.box.boxSize[0] * as.box.boxSize[1] * as.box.boxSize[2] * 8.0f * density;

		ret[0][0] = (ysq + zsq) * mass / 3.0f;
		ret[1][1] = (xsq + zsq) * mass / 3.0f;
		ret[2][2] = (xsq + ysq) * mass / 3.0f;

		break;
	}
	default:
		ASSERT(1);
	}

	neM3 invrotation;
	
	//invrotation.SetInvert(c2p.rot);

	invrotation.SetTranspose(c2p.rot);

	ret = ret * invrotation;

	ret = c2p.rot * ret;

	TranslateCOM(ret, c2p.pos, mass, 1.0f);
	
	return ret;
}

/****************************************************************************
*
*	CollisionModelTest
*
****************************************************************************/ 

void CollisionTest(neCollisionResult & result, neCollision & colA, neT3 & transA, neCollision & colB, neT3 & transB, const neV3 & backupVector)
{
	result.penetrate = false;

	if (colA.convexCount == 0 || colB.convexCount == 0)
	{
		return;
	}

	neCollisionResult candidate[2];

	s32 cur = 0;
	s32 res = 1;
	s32 tmp;

	candidate[res].depth = 0.0f;

	neT3 convex2WorldA;
	neT3 convex2WorldB;

	if (colA.convexCount == 1 && colB.convexCount == 1)
	{
		convex2WorldA = transA * colA.convex->c2p;

		if (colB.convex->type != TConvex::TERRAIN)
			convex2WorldB = transB * colB.convex->c2p;

		ConvexCollisionTest(candidate[res], *colA.convex, convex2WorldA, *colB.convex, convex2WorldB, backupVector);

		if (candidate[res].penetrate)
		{
			candidate[res].convexA = colA.convex;

			candidate[res].convexB = colB.convex;
			
			goto CollisionTest_Exit;
		}
		else
		{
			return;
		}
	}

	convex2WorldA = transA * colA.obb.c2p;

	if (colB.obb.type != TConvex::TERRAIN)
		convex2WorldB = transB * colB.obb.c2p;

	ConvexCollisionTest(candidate[res], colA.obb, convex2WorldA, colB.obb, convex2WorldB, backupVector);

	if (candidate[res].penetrate == false)
	{
		return; //no more to do
	}

	candidate[res].depth = 0.0f;

	candidate[res].penetrate = false;

	if (colA.convexCount == 1 && colB.convexCount > 1)
	{
		TConvexItem * gi = (TConvexItem *)colB.convex;

		while (gi)
		{
			TConvex * g = (TConvex *) gi;

			gi = gi->next;

			convex2WorldB = transB * g->c2p;

			ConvexCollisionTest(candidate[cur], *colA.convex, convex2WorldA, *g, convex2WorldB, backupVector);

			if (candidate[cur].penetrate && (candidate[cur].depth > candidate[res].depth))
			{
				candidate[cur].convexA = colA.convex;
				candidate[cur].convexB = g;
				tmp = res;
				res = cur;
				cur = tmp;
			}
		}
	}
	else if (colA.convexCount > 1 && colB.convexCount == 1)
	{
		TConvexItem * gi = (TConvexItem *)colA.convex;

		while (gi)
		{
			TConvex * g = (TConvex *) gi;

			gi = gi->next;

			convex2WorldA = transA * g->c2p;

			ConvexCollisionTest(candidate[cur], *g, convex2WorldA, *colB.convex, convex2WorldB, backupVector);

			if (candidate[cur].penetrate && (candidate[cur].depth > candidate[res].depth))
			{
				candidate[cur].convexA = g;
				candidate[cur].convexB = colB.convex;
				tmp = res;
				res = cur;
				cur = tmp;
			}
		}
	}
	else //colA.convexCount > 1 && colB.convexCount > 1
	{
		const s32 totalPotentials = 100;

		static TConvex * potentialsA[totalPotentials];
		static TConvex * potentialsB[totalPotentials];

		s32 potentialsACount = 0;
		s32 potentialsBCount = 0;

		TConvexItem * giA = (TConvexItem *)colA.convex;

		convex2WorldB = transB * colB.obb.c2p;

		while (giA)
		{
			TConvex * gA = (TConvex *) giA;

			giA = giA->next;

			convex2WorldA = transA * gA->c2p;

			ConvexCollisionTest(candidate[0], *gA, convex2WorldA, colB.obb, convex2WorldB, backupVector);

			if (!candidate[0].penetrate)
				continue;
			
			potentialsA[potentialsACount++] = gA;
		}

		TConvexItem * giB = (TConvexItem *)colB.convex;

		convex2WorldA = transA * colA.obb.c2p;

		while (giB)
		{
			TConvex * gB = (TConvex *) giB;

			giB = giB->next;

			convex2WorldB = transB * gB->c2p;

			ConvexCollisionTest(candidate[0], colA.obb, convex2WorldA, *gB, convex2WorldB, backupVector);

			if (!candidate[0].penetrate)
				continue;
			
			potentialsB[potentialsBCount++] = gB;
		}

		
		cur = 0;

		res = 1;

		candidate[res].depth = 0.0f;

		candidate[res].penetrate = false;

		for (s32 i = 0; i < potentialsACount; i++)
		{
			convex2WorldA = transA * potentialsA[i]->c2p;

			for (s32 j = 0; j < potentialsBCount; j++)
			{
				convex2WorldB = transB * potentialsB[j]->c2p;

				ConvexCollisionTest(candidate[cur], *potentialsA[i], convex2WorldA, 
													*potentialsB[j], convex2WorldB, backupVector);	

				if (candidate[cur].penetrate && (candidate[cur].depth > candidate[res].depth))
				{
					candidate[cur].convexA = potentialsA[i];
					candidate[cur].convexB = potentialsB[j];
					tmp = res;
					res = cur;
					cur = tmp;
				}
			}
		}
	}

	if (!candidate[res].penetrate)
		return;

CollisionTest_Exit:

	result = candidate[res];

	result.contactAWorld = result.contactA;

	result.contactBWorld = result.contactB;

	result.contactA = result.contactA - transA.pos;

	result.contactB = result.contactB - transB.pos;

	result.contactABody = transA.rot.TransposeMulV3(result.contactA);

	result.contactBBody = transB.rot.TransposeMulV3(result.contactB);
	
	result.materialIdA = result.convexA->GetMaterialId(); 

	if (colB.obb.GetType() != TConvex::TERRAIN && colB.obb.GetType() != TConvex::TRIANGLE)
	{
		result.materialIdB = result.convexB->GetMaterialId();
	}
}

/****************************************************************************
*
*	ConvexCollisionTest
*
****************************************************************************/ 

void ConvexCollisionTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector)
{
	switch (convexA.type)
	{
	case TConvex::BOX:
		switch (convexB.type)
		{
		case TConvex::BOX:
			Box2BoxTest(result, convexA, transA, convexB, transB, backupVector);
			break;
		
		case TConvex::SPHERE:
			Box2SphereTest(result, convexA, transA, convexB, transB);
			break;

		case TConvex::CYLINDER:
			Box2CylinderTest(result, convexA, transA, convexB, transB);
			break;

		case TConvex::TRIANGLE:
			Box2TriangleTest(result, convexA, transA, convexB, transB);
			break;
		case TConvex::TERRAIN:
			Box2TerrainTest(result, convexA, transA, convexB);
			break;
		case TConvex::CONVEXDCD:
			Box2ConvexTest(result, convexA, transA, convexB, transB, backupVector);
			break;

#ifdef USE_OPCODE

		case TConvex::OPCODE_MESH:
			Box2OpcodeTest(result, convexA, transA, convexB, transB);
			break;
				
#endif //USE_OPCODE

		default:
			ASSERT(0);
			break;
		}
		break;
	
	case TConvex::SPHERE:
		switch (convexB.type)
		{
			case TConvex::BOX:
				Box2SphereTest(result, convexB, transB, convexA, transA);
				
				result.Swap();
				
				break;
			
			case TConvex::SPHERE:
				Sphere2SphereTest(result, convexA, transA, convexB, transB);

				break;

			case TConvex::CYLINDER:
				Cylinder2SphereTest(result, convexB, transB, convexA, transA);

				result.Swap();

				break;

			case TConvex::TRIANGLE:
				//Sphere2TriangleTest(result, convexA, transA, convexB, transB);
				break;
			case TConvex::TERRAIN:
				Sphere2TerrainTest(result, convexA, transA, convexB);
				break;

#ifdef USE_OPCODE

			case TConvex::OPCODE_MESH:
				Sphere2OpcodeTest(result, convexA, transA, convexB, transB);
				break;

#endif //USE_OPCODE

			default:
				ASSERT(0);
				break;
		}
		break;
	case TConvex::CYLINDER:
		switch (convexB.type)
		{
			case TConvex::BOX:
				Box2CylinderTest(result, convexB, transB, convexA, transA);
				
				result.Swap();
				
				break;
			
			case TConvex::CYLINDER:
				Cylinder2CylinderTest(result, convexA, transA, convexB, transB);

				break;

			case TConvex::SPHERE:
				Cylinder2SphereTest(result, convexA, transA, convexB, transB);

				break;

			case TConvex::TRIANGLE:
				//Sphere2TriangleTest(result, convexA, transA, convexB, transB);
				break;
			case TConvex::TERRAIN:
				Cylinder2TerrainTest(result, convexA, transA, convexB);
				break;

#ifdef USE_OPCODE

			case TConvex::OPCODE_MESH:
				Cylinder2OpcodeTest(result, convexA, transA, convexB, transB);
				break;
				
#endif //USE_OPCODE

			default:
				ASSERT(0);
				break;
		}
		break;

#ifdef USE_OPCODE

	case TConvex::OPCODE_MESH:

		switch(convexB.type)
		{
		case TConvex::BOX:
			Box2OpcodeTest(result, convexB, transB, convexA, transA);
			result.Swap();

			break;
		case TConvex::SPHERE:
			Sphere2OpcodeTest(result, convexB, transB, convexA, transA);
			result.Swap();

			break;
		case TConvex::CYLINDER:
			Cylinder2OpcodeTest(result, convexB, transB, convexA, transA);
			result.Swap();

			break;
		case TConvex::TERRAIN:
			Opcode2TerrainTest(result, convexA, transA, convexB);
			break;

		case TConvex::OPCODE_MESH:
			Opcode2OpcodeTest(result, convexA, transA, convexB, transB);

			break;
		default:
			ASSERT(0);
			break;
		};
		break;
		
#endif //USE_OPCODE

	case TConvex::CONVEXDCD:
	{
		switch (convexB.type)
		{
		case TConvex::CONVEXDCD:

			Convex2ConvexTest(result, convexA, transA, convexB, transB, backupVector);
			
			break;

		case TConvex::BOX:
			
			Box2ConvexTest(result, convexB, transB, convexA, transA, -backupVector);

			result.Swap();

			break;
		
		case TConvex::TERRAIN:

			Convex2TerrainTest(result, convexA, transA, convexB);
			
			break;
		}
		break;

	}
	default:
		ASSERT(0);
		break;
	}
}

/****************************************************************************
*
*	Box2BoxTest
*
****************************************************************************/ 
#if 1
void Box2BoxTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector)
{
/*	neCollisionResult dcdresult;

	TestDCD(dcdresult, convexA, transA, convexB, transB, backupVector);


	if (dcdresult.penetrate)
	{
		result.penetrate = true;

		result.depth = dcdresult.depth;

		result.collisionFrame[2] = dcdresult.collisionFrame[2];

		result.contactA = dcdresult.contactA;

		result.contactB = dcdresult.contactB;
	}
	else
	{
		result.penetrate = false;
	}
	
	return;
*/
	ConvexTestResult res;

	BoxTestParam boxParamA;
	BoxTestParam boxParamB;

	boxParamA.convex = &convexA;
	boxParamA.trans = &transA;
	boxParamA.radii[0] = transA.rot[0] * convexA.as.box.boxSize[0];
	boxParamA.radii[1] = transA.rot[1] * convexA.as.box.boxSize[1];
	boxParamA.radii[2] = transA.rot[2] * convexA.as.box.boxSize[2];

	boxParamB.convex = &convexB;
	boxParamB.trans = &transB;
	boxParamB.radii[0] = transB.rot[0] * convexB.as.box.boxSize[0];
	boxParamB.radii[1] = transB.rot[1] * convexB.as.box.boxSize[1];
	boxParamB.radii[2] = transB.rot[2] * convexB.as.box.boxSize[2];

	if (boxParamA.BoxTest(res, boxParamB))
	{
		//return;
		result.penetrate = true;
		
		result.depth = res.depth;

//		result.collisionFrame[0] = res.contactX;
//		result.collisionFrame[1] = res.contactY;
		result.collisionFrame[2] = res.contactNormal;

//		if (res.isEdgeEdge)
		{
			result.contactA = res.contactA;

			result.contactB = res.contactB;
		}
//		else
//		{
//			result.contactA = res.contactA;
//			
//			result.contactB = res.contactB;
//		}
	}
	else
	{
		result.penetrate = false;
	}
//	ASSERT(result.penetrate == dcdresult.penetrate);
}

#else

void Box2BoxTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, neV3 & backupVector)
{
	Simplex simplex;

	GJKObj gjkObjA, gjkObjB;

	simplex.cache_valid = false;

	simplex.epsilon = 1.0e-6f;

	gjkObjA.half_box.Set(convexA.as.box.boxSize);

	gjkObjB.half_box.Set(convexB.as.box.boxSize);

	gjkObjA.scaleFactor = 1.0f;

	gjkObjB.scaleFactor = 1.0f;

	gjkObjA.type = GJK_BOX;

	gjkObjB.type = GJK_BOX;

	gjkObjA.lw = &transA;

	gjkObjB.lw = &transB;

	gjkObjA.lwrot = &transA.rot;

	gjkObjB.lwrot = &transB.rot;

	f32 envelopeA = convexA.envelope = 0.1f;

	f32 envelopeB = convexB.envelope = 0.1f;

	f32 envelope = envelopeA + envelopeB;

	f32 dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

	if (dist > 0)
	{
		dist = sqrtf(dist);

		ASSERT(dist > 0.0f);

		if (dist < envelope)
		{
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			diff.Normalize();

			result.collisionFrame[2] = diff;

			result.depth = envelope - dist;

			result.contactA = pa - diff * envelopeA;

			result.contactB = pb + diff * envelopeB;

			result.penetrate = true;
		}
		else
			result.penetrate = false;
	}
	else
	{
		neV3 posA, posB;

		posA = transA.pos;

		posB = transB.pos;

		f32 dist = 0.0f;

		//simplex.cache_valid = false;

		neV3 bv = backupVector * 10.0f;

		transA.pos += bv;
		
		dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

		if (dist > 0.0f)
		{
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			f32 d = diff.Length();

			result.collisionFrame[2] = diff * (1.0f / d);

			transA.pos = posA;

			pa = transA * simplex.closest_pointA;

			diff = pb - pa;

			d = diff.Length();

			result.depth = d + envelope;

			result.penetrate = true;
			
			result.contactA = pa;

			result.contactB = pb;
		}
		else
		{
			//result.penetrate = false;

			//return;
			
			f32 shrink = 0.8f;

			transA.pos = posA;

			simplex.cache_valid = false;

			for (s32 i = 0; i < 5; i++)
			{
				simplex.cache_valid = false;

				gjkObjA.scaleFactor = shrink;

				gjkObjB.scaleFactor = shrink;

				dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

				if (dist > 0.0f)
					break;
				
				shrink *= 0.8f;
			}
			if (dist == 0.0f)
			{
				result.penetrate = false;

				return;
			}
			if (!simplex.closest_pointA.IsFinite() ||
				!simplex.closest_pointB.IsFinite() )
			{
				result.penetrate = false;

				return;
			}
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			diff.Normalize();

			f32 factor;

			if (convexA.boundingRadius > convexB.boundingRadius)
				factor = convexA.boundingRadius;
			else
				factor = convexB.boundingRadius;

			transA.pos += (diff * factor);

			simplex.cache_valid = false;

			ASSERT(transA.pos.IsFinite());

			gjkObjA.scaleFactor = 1.0f;

			gjkObjB.scaleFactor = 1.0f;

			dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

			ASSERT(dist > 0.0f);

			transA.pos = posA;

			result.contactA = transA * simplex.closest_pointA;

			result.contactB = transB * simplex.closest_pointB;

			result.penetrate = true;

			result.collisionFrame[2] = result.contactB - result.contactA;

			result.depth = result.collisionFrame[2].Length();

			result.collisionFrame[2] *= (1.0f / result.depth);

			result.depth += envelope;
		}
	}
}

#endif
/*
void Convex2ConvexTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, neV3 & backupVector)
{
	Simplex simplex;

	GJKObj gjkObjA, gjkObjB;

	simplex.cache_valid = false;

	simplex.epsilon = 1.0e-6f;

	//gjkObjA.half_box.Set(convexA.as.box.boxSize);

	//gjkObjB.half_box.Set(convexB.as.box.boxSize);

	gjkObjA.vp = convexA.as.convexMesh.vertices;

	gjkObjA.neighbors = convexA.as.convexMesh.neighbours;

	gjkObjB.vp = convexB.as.convexMesh.vertices;

	gjkObjB.neighbors = convexB.as.convexMesh.neighbours;

	gjkObjA.scaleFactor = 1.0f;

	gjkObjB.scaleFactor = 1.0f;

	gjkObjA.type = GJK_MESH;

	gjkObjB.type = GJK_MESH;

	simplex.hintA = 0;

	simplex.hintB = 0;

	gjkObjA.lw = &transA;

	gjkObjB.lw = &transB;

	gjkObjA.lwrot = &transA.rot;

	gjkObjB.lwrot = &transB.rot;

	f32 envelopeA = convexA.envelope;

	f32 envelopeB = convexB.envelope;

	f32 envelope = envelopeA + envelopeB;

	f32 dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

	if (dist > 0)
	{
		dist = sqrtf(dist);

		ASSERT(dist > 0.0f);

		if (dist < envelope)
		{
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			diff.Normalize();

			result.collisionFrame[2] = diff;

			result.depth = envelope - dist;

			result.contactA = pa - diff * envelopeA;

			result.contactB = pb + diff * envelopeB;

			result.penetrate = true;
		}
		else
			result.penetrate = false;
	}
	else
	{
		neV3 posA, posB;

		posA = transA.pos;

		posB = transB.pos;

		f32 dist = 0.0f;

		simplex.cache_valid = false;

		//neV3 bv = posA - posB;
		
		neV3 bv = backupVector;

		bv.Normalize();

		bv *= (convexA.boundingRadius * 100.0f);

		transA.pos += bv;
		
		dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

		if (0)//dist > 0.0f)
		{
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			f32 d = diff.Length();

			result.collisionFrame[2] = diff * (1.0f / d);

			transA.pos = posA;

			pa = transA * simplex.closest_pointA;

			diff = pb - pa;

			d = diff.Length();

			result.depth = d + envelope;

			result.penetrate = true;
			
			result.contactA = pa;

			result.contactB = pb;
		}
		else
		{
			//result.penetrate = false;

			//return;
			
			f32 shrink = 0.8f;

			transA.pos = posA;

			simplex.cache_valid = false;

			for (s32 i = 0; i < 5; i++)
			{
				simplex.cache_valid = false;

				gjkObjA.scaleFactor = shrink;

				gjkObjB.scaleFactor = shrink;

				dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

				if (dist > 0.0f)
					break;
				
				shrink *= 0.8f;
			}
			if (dist == 0.0f)
			{
				result.penetrate = false;

				return;
			}
			if (!simplex.closest_pointA.IsFinite() ||
				!simplex.closest_pointB.IsFinite() )
			{
				result.penetrate = false;

				return;
			}
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			diff.Normalize();

			f32 factor;

			if (convexA.boundingRadius > convexB.boundingRadius)
				factor = convexA.boundingRadius;
			else
				factor = convexB.boundingRadius;

			transA.pos = posA + (diff * factor * 10.0f);

			simplex.cache_valid = false;

			ASSERT(transA.pos.IsFinite());

			gjkObjA.scaleFactor = 1.0f;

			gjkObjB.scaleFactor = 1.0f;

			dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

			ASSERT(dist > 0.0f);

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			diff = pa - pb;

			diff.Normalize();

			result.collisionFrame[2] = diff;

			transA.pos = posA;

			result.contactA = transA * simplex.closest_pointA;

			result.contactB = pb;//transB * simplex.closest_pointB;

			result.penetrate = true;

			diff = result.contactB - result.contactA;

			result.depth = diff.Length();

			result.depth += envelope;
		}
	}
}

*/

void Convex2ConvexTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector)
{
	neCollisionResult dcdresult;

	TestDCD(dcdresult, convexA, transA, convexB, transB, backupVector);


	if (dcdresult.penetrate)
	{
		result.penetrate = true;

		result.depth = dcdresult.depth;

		result.collisionFrame[2] = dcdresult.collisionFrame[2];

		result.contactA = dcdresult.contactA;

		result.contactB = dcdresult.contactB;
	}
	else
	{
		result.penetrate = false;
	}
	
	return;
}
void Box2ConvexTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector)
{
	neCollisionResult dcdresult;

	TestDCD(dcdresult, convexA, transA, convexB, transB, backupVector);


	if (dcdresult.penetrate)
	{
		result.penetrate = true;

		result.depth = dcdresult.depth;

		result.collisionFrame[2] = dcdresult.collisionFrame[2];

		result.contactA = dcdresult.contactA;

		result.contactB = dcdresult.contactB;
	}
	else
	{
		result.penetrate = false;
	}
	
	return;
}

/*
void Box2ConvexTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, neV3 & backupVector)
{
	Simplex simplex;

	GJKObj gjkObjA, gjkObjB;

	simplex.cache_valid = false;

	simplex.epsilon = 1.0e-6f;

	gjkObjA.half_box.Set(convexA.as.box.boxSize);

	//gjkObjB.half_box.Set(convexB.as.box.boxSize);

	gjkObjB.vp = convexB.as.convexMesh.vertices;

	gjkObjB.neighbors = convexB.as.convexMesh.neighbours;

	gjkObjA.scaleFactor = 1.0f;

	gjkObjB.scaleFactor = 1.0f;

	gjkObjA.type = GJK_BOX;

	gjkObjB.type = GJK_MESH;

	simplex.hintB = 0;

	gjkObjA.lw = &transA;

	gjkObjB.lw = &transB;

	gjkObjA.lwrot = &transA.rot;

	gjkObjB.lwrot = &transB.rot;

	f32 envelopeA = 0.0f;//convexA.envelope = 0.05f;

	f32 envelopeB = convexB.envelope;

	f32 envelope = envelopeA + envelopeB;

	f32 dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

	if (dist > 0)
	{
		dist = sqrtf(dist);

		ASSERT(dist > 0.0f);

		if (dist < envelope)
		{
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			diff.Normalize();

			result.collisionFrame[2] = diff;

			result.depth = envelope - dist;

			result.contactA = pa - diff * envelopeA;

			result.contactB = pb + diff * envelopeB;

			result.penetrate = true;
		}
		else
			result.penetrate = false;
	}
	else
	{
		neV3 posA, posB;

		posA = transA.pos;

		posB = transB.pos;

		f32 dist = 0.0f;

		//simplex.cache_valid = false;

		neV3 bv = backupVector * 10.0f;

		transA.pos += bv;
		
		dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

		if (dist > 0.0f)
		{
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			f32 d = diff.Length();

			result.collisionFrame[2] = diff * (1.0f / d);

			transA.pos = posA;

			pa = transA * simplex.closest_pointA;

			diff = pb - pa;

			d = diff.Length();

			result.depth = d + envelope;

			result.penetrate = true;
			
			result.contactA = pa;

			result.contactB = pb;
		}
		else
		{
			//result.penetrate = false;

			//return;
			
			f32 shrink = 0.8f;

			//transA.pos = posA;

			simplex.cache_valid = false;

			for (s32 i = 0; i < 5; i++)
			{
				simplex.cache_valid = false;

				gjkObjA.scaleFactor = shrink;

				gjkObjB.scaleFactor = shrink;

				dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

				if (dist > 0.0f)
					break;
				
				shrink *= 0.8f;
			}
			if (dist == 0.0f)
			{
				result.penetrate = false;

				return;
			}
			if (!simplex.closest_pointA.IsFinite() ||
				!simplex.closest_pointB.IsFinite() )
			{
				result.penetrate = false;

				return;
			}
			neV3 pa, pb;

			pa = transA * simplex.closest_pointA;

			pb = transB * simplex.closest_pointB;

			neV3 diff = pa - pb;

			diff.Normalize();

			f32 factor;

			if (convexA.boundingRadius > convexB.boundingRadius)
				factor = convexA.boundingRadius;
			else
				factor = convexB.boundingRadius;

			transA.pos += (diff * factor);

			simplex.cache_valid = false;

			ASSERT(transA.pos.IsFinite());

			gjkObjA.scaleFactor = 1.0f;

			gjkObjB.scaleFactor = 1.0f;

			dist = calc_dist(&simplex, &gjkObjA, &gjkObjB, 1);

			ASSERT(dist > 0.0f);

			transA.pos = posA;

			result.contactA = transA * simplex.closest_pointA;

			result.contactB = transB * simplex.closest_pointB;

			result.penetrate = true;

			result.collisionFrame[2] = result.contactB - result.contactA;

			result.depth = result.collisionFrame[2].Length();

			result.collisionFrame[2] *= (1.0f / result.depth);

			result.depth += envelope;
		}
	}
}
*/
void BoxTestParam::CalcVertInWorld()
{
	isVertCalc = true;
	
	verts[0] = trans->pos + radii[0] + radii[1] + radii[2];
	verts[1] = trans->pos + radii[0] + radii[1] - radii[2];
	verts[2] = trans->pos + radii[0] - radii[1] + radii[2];
	verts[3] = trans->pos + radii[0] - radii[1] - radii[2];
	verts[4] = trans->pos - radii[0] + radii[1] + radii[2];
	verts[5] = trans->pos - radii[0] + radii[1] - radii[2];
	verts[6] = trans->pos - radii[0] - radii[1] + radii[2];
	verts[7] = trans->pos - radii[0] - radii[1] - radii[2];
}

bool BoxTestParam::BoxTest(ConvexTestResult & result, BoxTestParam & otherBox)
{
	result.depth = 1.e5f;
	result.isEdgeEdge = false;
	result.valid = false;

	if (MeasureVertexFacePeneration(result, otherBox, 0) && //vertex of B with face of A
		MeasureVertexFacePeneration(result, otherBox, 1) &&
		MeasureVertexFacePeneration(result, otherBox, 2))
	{
		result.contactNormal *= -1.0f; // normal points toward A (this)
		neV3 tmp = result.contactA;
		result.contactA = result.contactB;
		result.contactB = tmp;
	}
	else
	{
		return false;
	}

	if (otherBox.MeasureVertexFacePeneration(result, *this, 0) && //vertex of A with face of B
		otherBox.MeasureVertexFacePeneration(result, *this, 1) &&
		otherBox.MeasureVertexFacePeneration(result, *this, 2))
	{
		
	}
	else
	{
		return false;
	}
	
	ConvexTestResult result2;

	result2.valid = false;

	result2.depth = result.depth;

	bool edgeCollided = false;

	if (MeasureEdgePeneration(result2, otherBox, 0, 0) &&
		MeasureEdgePeneration(result2, otherBox, 0, 1) &&
		MeasureEdgePeneration(result2, otherBox, 0, 2) &&
		MeasureEdgePeneration(result2, otherBox, 1, 0) &&
		MeasureEdgePeneration(result2, otherBox, 1, 1) &&
		MeasureEdgePeneration(result2, otherBox, 1, 2) &&
		MeasureEdgePeneration(result2, otherBox, 2, 0) &&
		MeasureEdgePeneration(result2, otherBox, 2, 1) &&
		MeasureEdgePeneration(result2, otherBox, 2, 2) )
	{
		if (result2.valid)
			edgeCollided = true;
	}
	else
	{
		return false;
	}

	if (edgeCollided)
	{
		result2.ComputerEdgeContactPoint(result);
		result.isEdgeEdge = true;
		result.contactNormal = result2.contactNormal * -1.0f;
	}
	else
	{
		return result.valid;
	}
	return result.valid;
}

bool BoxTestParam::MeasureVertexFacePeneration(ConvexTestResult & result, BoxTestParam & otherBox, s32 whichFace)
{
	neV3 me2otherBox;

	me2otherBox = otherBox.trans->pos - trans->pos;

	neV3 direction;

	direction = trans->rot[whichFace];

	neV3 contactPoint;

	contactPoint = otherBox.trans->pos;

	f32 penetrated;

	bool reverse = false;
	
	if ((penetrated = me2otherBox.Dot(direction)) < 0.0f)
	{
		direction = direction * -1.0f;

		reverse = true;
	}
	else
	{
		penetrated = penetrated * -1.0f;
	}

	penetrated += convex->as.box.boxSize[whichFace];

	neV3 progression;

	progression = direction * otherBox.radii;

	neV3 sign;

	sign[0] = progression[0] > 0.0f ? 1.0f: -1.0f;
	sign[1] = progression[1] > 0.0f ? 1.0f: -1.0f;
	sign[2] = progression[2] > 0.0f ? 1.0f: -1.0f;

	penetrated += (progression[0] * sign[0]);
	penetrated += (progression[1] * sign[1]);
	penetrated += (progression[2] * sign[2]);

	contactPoint -= (otherBox.radii[0] * sign[0]);
	contactPoint -= (otherBox.radii[1] * sign[1]);
	contactPoint -= (otherBox.radii[2] * sign[2]);

	if (penetrated <= 0.0f)
		return false;

	if ((penetrated + 0.0001f) < result.depth)
	{
		result.depth = penetrated;
		result.contactA = contactPoint; // contactPoint is vertex of otherBox
		result.contactB = contactPoint + direction * penetrated;
		result.valid = true;
		result.contactNormal = direction;
	}
	else if (neIsConsiderZero(penetrated - result.depth))
	{
		s32 otherAxis1 = neNextDim1[whichFace];
		
		s32 otherAxis2 = neNextDim2[whichFace];

		//check to see if this one fall into the faces
		neV3 sub = contactPoint - trans->pos;

		f32 dot = neAbs(sub.Dot(trans->rot[otherAxis1]));

		if (dot > (convex->as.box.boxSize[otherAxis1] * 1.001f))
			return true;// not false ???? no it is true!!! 

		dot = neAbs(sub.Dot(trans->rot[otherAxis2]));

		if (dot > (convex->as.box.boxSize[otherAxis2] * 1.001f))
			return true;// not false ???? no it is true!!! 

		result.depth = penetrated;
		result.contactA = contactPoint;
		result.contactB = contactPoint + direction * penetrated;
		result.valid = true;
		result.contactNormal = direction;
	}
	return true;
}

neBool BoxTestParam::MeasureEdgePeneration(ConvexTestResult & result, BoxTestParam & otherBox, s32 dim1, s32 dim2)
{
	neV3 contactA = trans->pos;

	neV3 contactB = otherBox.trans->pos;
	
	neV3 contactNormal = trans->rot[dim1].Cross(otherBox.trans->rot[dim2]);

	f32 len = contactNormal.Length();

	if (neIsConsiderZero(len))
		return true;

	contactNormal *= (1.0f / len);

	neV3 me2OtherBox = otherBox.trans->pos - trans->pos;

	f32 penetrated = me2OtherBox.Dot(contactNormal);

	bool reverse = false;

	if (penetrated < 0.0f)
	{
		contactNormal = contactNormal * -1.0f;

		reverse = true;
	}
	else
		penetrated = penetrated * -1.0f;

	f32 progression[4];

	s32 otherAxisA1 = (dim1 + 1) % 3;
	s32 otherAxisA2 = (dim1 + 2) % 3;
	s32 otherAxisB1 = (dim2 + 1) % 3;
	s32 otherAxisB2 = (dim2 + 2) % 3;

	progression[0] = radii[otherAxisA1].Dot(contactNormal);
	progression[1] = radii[otherAxisA2].Dot(contactNormal);
	progression[2] = otherBox.radii[otherAxisB1].Dot(contactNormal);
	progression[3] = otherBox.radii[otherAxisB2].Dot(contactNormal);

	f32 sign[4];

	sign[0] = progression[0] > 0.0f ? 1.0f: -1.0f;
	sign[1] = progression[1] > 0.0f ? 1.0f: -1.0f;
	sign[2] = progression[2] > 0.0f ? 1.0f: -1.0f;
	sign[3] = progression[3] > 0.0f ? 1.0f: -1.0f;

	penetrated += (progression[0] * sign[0]);
	penetrated += (progression[1] * sign[1]);
	penetrated += (progression[2] * sign[2]);
	penetrated += (progression[3] * sign[3]);

	contactA += (radii[otherAxisA1] * sign[0]);
	contactA += (radii[otherAxisA2] * sign[1]);
	contactB -= (otherBox.radii[otherAxisB1] * sign[2]);
	contactB -= (otherBox.radii[otherAxisB2] * sign[3]);

	if(penetrated <= 0.0f)
		return false;

	if (penetrated < result.depth)
	{
		result.depth = penetrated;

		result.valid = true;

		result.edgeA[0] = contactA + (radii[dim1]);
		result.edgeA[1] = contactA - (radii[dim1]);

		result.edgeB[0] = contactB + (otherBox.radii[dim2]);
		result.edgeB[1] = contactB - (otherBox.radii[dim2]);

		result.contactA = contactA;
		result.contactB = contactB;
/*		
		if (reverse)
			result.contactX = trans->rot[dim1];
		else
			result.contactX = trans->rot[dim1];

		result.contactY = contactNormal.Cross(result.contactX);
*/		result.contactNormal = contactNormal;
	}
	return true;
}

neBool ConvexTestResult::ComputerEdgeContactPoint(ConvexTestResult & res)
{
	f32 d1343, d4321, d1321, d4343, d2121;
	f32 numer, denom, au, bu;
	
	neV3 p13;
	neV3 p43;
	neV3 p21;
//	neV3 diff;

	p13 = (edgeA[0]) - (edgeB[0]);
	p43 = (edgeB[1]) - (edgeB[0]);

	if ( p43.IsConsiderZero() )
	{
		goto ComputerEdgeContactPoint_Exit;
	}
	
	p21 = (edgeA[1]) - (edgeA[0]);

	if ( p21.IsConsiderZero() )
	{
		goto ComputerEdgeContactPoint_Exit;
	}
	
	d1343 = p13.Dot(p43);
	d4321 = p43.Dot(p21);
	d1321 = p13.Dot(p21);
	d4343 = p43.Dot(p43);
	d2121 = p21.Dot(p21);

	denom = d2121 * d4343 - d4321 * d4321;   

	if (neAbs(denom) < NE_ZERO) 
		goto ComputerEdgeContactPoint_Exit;

	numer = d1343 * d4321 - d1321 * d4343;
	au = numer / denom;   
	bu = (d1343 + d4321 * (au)) / d4343;

	if (au < 0.0f || au >= 1.0f)
		goto ComputerEdgeContactPoint_Exit;
	
	if (bu < 0.0f || bu >= 1.0f)
		goto ComputerEdgeContactPoint_Exit;

	{
		neV3 tmpv;

		tmpv = p21 * au;
		res.contactA = (edgeA[0]) + tmpv;

		tmpv = p43 * bu;
		res.contactB = (edgeB[0]) + tmpv;
	}

//	diff = (res.contactA) - (res.contactB);
	res.depth = depth;//sqrtf(diff.Dot(diff));

	return true;

ComputerEdgeContactPoint_Exit:
	//res.contactA = contactA;
	//res.contactB = contactB;
	//diff = (res.contactA) - (res.contactB);
	res.depth = depth;//sqrtf(diff.Dot(diff));
	return false;
}

neBool ConvexTestResult::ComputerEdgeContactPoint2(f32 & au, f32 & bu)
{
	f32 d1343, d4321, d1321, d4343, d2121;
	f32 numer, denom;
	
	neV3 p13;
	neV3 p43;
	neV3 p21;
	neV3 diff;

	p13 = (edgeA[0]) - (edgeB[0]);
	p43 = (edgeB[1]) - (edgeB[0]);

	if ( p43.IsConsiderZero() )
	{
		valid = false;
		goto ComputerEdgeContactPoint2_Exit;
	}
	
	p21 = (edgeA[1]) - (edgeA[0]);

	if ( p21.IsConsiderZero() )
	{
		valid = false;
		goto ComputerEdgeContactPoint2_Exit;
	}
	
	d1343 = p13.Dot(p43);
	d4321 = p43.Dot(p21);
	d1321 = p13.Dot(p21);
	d4343 = p43.Dot(p43);
	d2121 = p21.Dot(p21);

	denom = d2121 * d4343 - d4321 * d4321;   

	if (neAbs(denom) < NE_ZERO) 
	{
		valid = false;

		goto ComputerEdgeContactPoint2_Exit;
	}

	numer = d1343 * d4321 - d1321 * d4343;
	au = numer / denom;   
	bu = (d1343 + d4321 * (au)) / d4343;

	if (au < 0.0f || au >= 1.0f)
	{
		valid = false;
	}
	else if (bu < 0.0f || bu >= 1.0f)
	{
		valid = false;
	}
	else
	{
		valid = true;
	}
	{
		neV3 tmpv;

		tmpv = p21 * au;
		contactA = (edgeA[0]) + tmpv;

		tmpv = p43 * bu;
		contactB = (edgeB[0]) + tmpv;
	}

	diff = contactA - contactB;
	
	depth = sqrtf(diff.Dot(diff));

	return true;

ComputerEdgeContactPoint2_Exit:

	return false;
}

neBool BoxTestParam::LineTest(ConvexTestResult & res, neV3 & point1, neV3 & point2)
{
	return false;	
}