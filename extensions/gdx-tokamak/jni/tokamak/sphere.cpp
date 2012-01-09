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

#include "tokamak.h"
#include "containers.h"
#include "scenery.h"
#include "collision.h"
#include "collision2.h"
#include "constraint.h"
#include "rigidbody.h"

#include <assert.h>
#include <stdio.h>

#define INSIDE_BOX_BOUNDARY(_dir) (flag2[_dir] < 0.0f)

#define BOX_SPHERE_DO_TEST(whichCase, _dir) {configuration = whichCase; dir = _dir;}

void Box2SphereTest(neCollisionResult & result, TConvex & boxA, neT3 & transA, TConvex & sphereB, neT3 & transB)
{
	f32 penetration;
	
	result.penetrate = false;

	neV3 sphereCenter;
	
	sphereCenter = (transA.FastInverse() * transB).pos;

	neV3 flag1, flag2;

	for (s32 i = 0; i < 3; i++)
		flag1[i] = sphereCenter[i] < 0.0f ? -1.0f: 1.0f;

	neV3 sphereCenterAbs;

	sphereCenterAbs = sphereCenter * flag1;

	flag2 = sphereCenterAbs - boxA.as.box.boxSize;

	s32 configuration, dir;

	if (INSIDE_BOX_BOUNDARY(1))
	{
		if (INSIDE_BOX_BOUNDARY(2))
			if (INSIDE_BOX_BOUNDARY(0))
				configuration = -1; //center inside the box
			else
				BOX_SPHERE_DO_TEST(0, 0)
		else
			if (INSIDE_BOX_BOUNDARY(0))
				BOX_SPHERE_DO_TEST(0, 2)
			else
				BOX_SPHERE_DO_TEST(1, 1)
	}	
	else if (INSIDE_BOX_BOUNDARY(2))
	{
		if (INSIDE_BOX_BOUNDARY(0))
			BOX_SPHERE_DO_TEST(0, 1)
		else
			BOX_SPHERE_DO_TEST(1, 2)
	}
	else if (INSIDE_BOX_BOUNDARY(0))
	{
		BOX_SPHERE_DO_TEST(1, 0)
	}
	else
	{
		BOX_SPHERE_DO_TEST(2, 0)
	}

	neV3 contactA;

	if (configuration == -1)
	{
		//find the shallowest penetration
		neV3 depth; depth = boxA.as.box.boxSize - flag2;
		s32 k;

		if (depth[0] < depth[1])
		{
			if (depth[0] < depth[2])
			{ //x
				k = 0;
			}
			else
			{ //z
				k = 2;
			}
		}
		else if (depth[1] < depth[2])
		{ //y
			k = 1;
		}
		else
		{ //z
			k = 2;
		}
		ASSERT(depth[k] >= 0.0f);

		result.depth = depth[k] + sphereB.Radius();

		result.penetrate = true;

		result.collisionFrame[2] = transA.rot[k] * flag1[k] * -1.0f;

		result.contactB = transB.pos + result.collisionFrame[2] * sphereB.Radius();

		result.contactA = result.contactB - result.collisionFrame[2] * result.depth;
	}
	else if (configuration == 0)
	{
		penetration = sphereB.Radius() + boxA.BoxSize(dir) - sphereCenterAbs[dir];

		if (penetration > 0.0f)
		{
			result.depth = penetration;

			result.penetrate = true;

			result.collisionFrame[2] = transA.rot[dir] * flag1[dir] * -1.0f;

			result.contactB = transB.pos + result.collisionFrame[2] * sphereB.Radius();

			result.contactA = result.contactB - result.collisionFrame[2] * penetration;
		}
	}
	else if (configuration == 1)
	{
		s32 dir1, dir2;

		dir1 = neNextDim1[dir];

		dir2 = neNextDim2[dir];

		contactA[dir] = sphereCenter[dir];

		contactA[dir1] = flag1[dir1] * boxA.BoxSize(dir1);

		contactA[dir2] = flag1[dir2] * boxA.BoxSize(dir2);

		neV3 sub = contactA - sphereCenter;

		f32 lenSq = sub[dir1] * sub[dir1] + 
					sub[dir2] * sub[dir2];

		if (lenSq > sphereB.RadiusSq())
			return;
		
		f32 len = sqrtf(lenSq);

		sub *= 1.0f / len;

		penetration = sphereB.Radius() - len;

		ASSERT(penetration > 0.0f);

		result.depth = penetration;

		result.penetrate = true;

		result.collisionFrame[2] = transA.rot * sub;

		result.contactA = transA * contactA;

		result.contactB = transB.pos + result.collisionFrame[2] * sphereB.Radius();
	}
	else if (configuration == 2)
	{
		contactA.SetZero();

		for (s32 i = 0; i < 3; i++)
			contactA[i] += flag1[i] * boxA.BoxSize(i);

		neV3 sub = contactA - sphereCenter;

		f32 lenSq = sub.Dot(sub);

		if (lenSq > sphereB.RadiusSq())
			return;
		
		f32 len = sqrtf(lenSq);

		penetration = sphereB.Radius() - len;

		sub *= 1.0f / len;

		ASSERT(penetration > 0.0f);

		result.depth = penetration;

		result.penetrate = true;

		result.collisionFrame[2] = transA.rot * sub;

		result.contactA = transA * contactA;

		result.contactB = transB.pos + result.collisionFrame[2] * sphereB.Radius();
	}
	return;
}

void Sphere2TerrainTest(neCollisionResult & result, TConvex & sphereA, neT3 & transA, TConvex & terrainB)
{
	neSimpleArray<s32> & _triIndex = *terrainB.as.terrain.triIndex;

	s32 triangleCount = _triIndex.GetUsedCount();

	neArray<neTriangle_> & triangleArray = *terrainB.as.terrain.triangles;

	ConvexTestResult res[2];

	s32 finalTriIndex = -1;
	s32 currentRes = 1;
	s32 testRes = 0;

	res[currentRes].depth = -1.0e6f;
	res[currentRes].valid = false;
	res[testRes].depth = 1.0e6f;
	
	s32 terrainMatID = 0;

	for (s32 i = 0; i < triangleCount; i++)
	{
		s32 test = _triIndex[i];

		neTriangle_ * t = &triangleArray[_triIndex[i]];

		TriangleParam triParam;

		triParam.vert[0] = terrainB.vertices[t->indices[0]];
		triParam.vert[1] = terrainB.vertices[t->indices[1]];
		triParam.vert[2] = terrainB.vertices[t->indices[2]];

		triParam.edges[0] = triParam.vert[1] - triParam.vert[0];
		triParam.edges[1] = triParam.vert[2] - triParam.vert[1];
		triParam.edges[2] = triParam.vert[0] - triParam.vert[2];
		triParam.normal = triParam.edges[0].Cross(triParam.edges[1]);
		triParam.normal.Normalize();
		triParam.d = triParam.normal.Dot(triParam.vert[0]);

		if (t->flag == neTriangle::NE_TRI_TRIANGLE)
		{
			if (SphereTriTest(transA.pos, sphereA.Radius(), res[testRes], triParam))
			{
				if (res[testRes].depth > res[currentRes].depth)
				{
					s32 tmp = testRes;	

					testRes = currentRes;

					currentRes = tmp;

					terrainMatID = t->materialID;

					finalTriIndex = _triIndex[i];
				}
			}
		}
		else if (t->flag == neTriangle::NE_TRI_HEIGHT_MAP)
		{
		}
		else
		{
			ASSERT(0);
		}
	}
	if (res[currentRes].valid)
	{
		result.penetrate = true;

		result.depth = res[currentRes].depth;

		result.collisionFrame[2] = res[currentRes].contactNormal;

		result.materialIdB = terrainMatID;

		result.contactA = res[currentRes].contactA;

		result.contactB = res[currentRes].contactB;
	}
	else
	{
		result.penetrate = false;
	}
}

void MeasureSphereAndTriEdge(const neV3 & center, f32 radius, ConvexTestResult & result, TriangleParam & tri, s32 whichEdge)
{
	s32 whichVert0, whichVert1;

	whichVert0 = whichEdge;

	whichVert1 = neNextDim1[whichEdge];

	f32 penetrate;
	
	neV3 dir = tri.edges[whichEdge];

	f32 edgeLen = dir.Length();

	if (neIsConsiderZero(edgeLen))
	{
		dir.SetZero();
	}
	else
	{
		dir *= (1.0f / edgeLen);
	}
	neV3 vert2Point = center - tri.vert[whichVert0];
	
	f32 dot = dir.Dot(vert2Point);

	neV3 project = tri.vert[whichVert0] + dot * dir;

	if (dot > 0.0f && dot < edgeLen)
	{
		neV3 diff = center - project;
			
		f32 len = diff.Length();
		
		penetrate = radius - len;

		if (penetrate > 0.0f && penetrate < result.depth && penetrate < radius)
		{
			result.valid = true;

			result.depth = penetrate;

			result.contactNormal = diff * (1.0f / len);

			result.contactA = center - result.contactNormal * radius;

			result.contactB = project;
		}
	}
}

void MeasureSphereAndTriVert(const neV3 & center, f32 radius, ConvexTestResult & result, TriangleParam & tri, s32 whichVert)
{
	neV3 diff = center - tri.vert[whichVert];
		
	f32 len = diff.Length();
	
	f32 penetrate = radius - len;

	if (penetrate > 0.0f)
	{
		result.valid = true;

		result.depth = penetrate;

		result.contactNormal = diff * (1.0f / len);

		result.contactA = center - result.contactNormal * radius;

		result.contactB = tri.vert[whichVert];
	}
}

neBool SphereTriTest(const neV3 & center, f32 radius, ConvexTestResult & result, TriangleParam & tri)
{
	//check sphere and triangle plane
	result.depth = 1.e5f;
	result.valid = false;

	f32 distFromPlane = tri.normal.Dot(center) - tri.d;

	f32 factor = 1.0f;

	if (distFromPlane < 0.0f)
		factor = -1.0f;

	f32 penetrated = radius - distFromPlane * factor;

	if (penetrated <= 0.0f)
		return false;

	neV3 contactB = center - tri.normal * distFromPlane;

	s32 pointInside = tri.IsPointInside(contactB);

	if (pointInside == -1) // inside the triangle
	{
		result.depth = penetrated;

		result.contactA = center - tri.normal * factor * radius; //on the sphere

		result.contactB = contactB;

		result.valid = true;

		result.contactNormal = tri.normal * factor;

		return true;
	}

	switch (pointInside)
	{
	case 0:
		MeasureSphereAndTriVert(center, radius, result, tri, 0);
		break;

	case 1:
		MeasureSphereAndTriEdge(center, radius, result, tri, 0);
		break;

	case 2:
		MeasureSphereAndTriVert(center, radius, result, tri, 1);
		break;

	case 3:
		MeasureSphereAndTriEdge(center, radius, result, tri, 1);
		break;

	case 4:
		MeasureSphereAndTriVert(center, radius, result, tri, 2);
		break;

	case 5:
		MeasureSphereAndTriEdge(center, radius, result, tri, 2);
		break;
	}
	
	return result.valid;
}

void Sphere2SphereTest(neCollisionResult & result, TConvex & sphereA, neT3 & transA, TConvex & sphereB, neT3 & transB)
{
	neV3 sub = transA.pos - transB.pos;

	f32 dot = sub.Dot(sub);

	f32 totalLen = sphereA.Radius() + sphereB.Radius();

	totalLen *= totalLen;

	if (dot >= totalLen)
	{
		result.penetrate = false;

		return;
	}

	if (neIsConsiderZero(dot))
	{
		result.penetrate = false;

		return;
	}
	f32 len = sub.Length();

	sub *= 1.0f / len;

	result.depth = sphereA.Radius() + sphereB.Radius() - len;

	ASSERT(result.depth > 0.0f);

	result.penetrate = true;

	result.collisionFrame[2] = sub;

	result.contactA = transA.pos - sub * sphereA.Radius();

	result.contactB = transB.pos + sub * sphereB.Radius();
}