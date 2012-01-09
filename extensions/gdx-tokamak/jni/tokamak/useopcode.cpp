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

#include <stdio.h>
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

#ifdef USE_OPCODE

Opcode::AABBTreeCollider tc;

Opcode::BVTCache ColCache;

IceMaths::Matrix4x4 worldA, worldB;

void GetTriangleOverlap(neCollisionResult & result, s32 i, TConvex & convexA, TConvex & convexB)
{
	const Pair * pair = &tc.GetPairs()[i];

	IceMaths::Point * vertsA = convexA.as.opcodeMesh.vertices;

	IndexedTriangle * trisA = convexA.as.opcodeMesh.triIndices;

	IceMaths::Point * vertsB = convexB.as.opcodeMesh.vertices;

	IndexedTriangle * trisB = convexB.as.opcodeMesh.triIndices;

	IceMaths::Point V0 = worldA * vertsA[trisA[pair->id0].mVRef[0]];

	IceMaths::Point V1 = worldA * vertsA[trisA[pair->id0].mVRef[1]];

	IceMaths::Point V2 = worldA * vertsA[trisA[pair->id0].mVRef[2]];

	IceMaths::Point U0 = worldB * vertsB[trisB[pair->id1].mVRef[0]];

	IceMaths::Point U1 = worldB * vertsB[trisB[pair->id1].mVRef[1]];

	IceMaths::Point U2 = worldB * vertsB[trisB[pair->id1].mVRef[2]];

	if (tc.TriTriOverlap(V0, V1, V2, U0, U1, U2))
	{

	}
}

void Box2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB)
{
	
}

void Sphere2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB)
{
}

void Cylinder2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB)
{
}

void Opcode2TerrainTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB)
{
}

void Opcode2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB)
{
	tc.SetFirstContact(false);
	tc.SetFullBoxBoxTest(true);
	tc.SetFullPrimBoxTest(true);
	tc.SetTemporalCoherence(false);

	tc.SetPointers0(convexA.as.opcodeMesh.triIndices, convexA.as.opcodeMesh.vertices);
	tc.SetPointers1(convexB.as.opcodeMesh.triIndices, convexB.as.opcodeMesh.vertices);

	// Setup cache
	
	ColCache.Model0 = convexA.as.opcodeMesh.opmodel;
	ColCache.Model1 = convexB.as.opcodeMesh.opmodel;
	
	transA.AssignIceMatrix(worldA);
	transB.AssignIceMatrix(worldB);
	// Collision query

	bool IsOk = tc.Collide(ColCache, &worldA, &worldB);

	if (tc.GetContactStatus() == false)
	{
		result.penetrate = false;
		return;
	}

	u32 npairs = tc.GetNbPairs();

	result.penetrate = true;

	result.depth = 0.0f;

	for (u32 i = 0; i < npairs; i++)
	{
		GetTriangleOverlap(result, i, convexA, convexB);
	}
}

#endif //USE_OPCODE