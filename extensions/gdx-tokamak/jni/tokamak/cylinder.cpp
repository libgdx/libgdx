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

//extern void DrawLine(const neV3 & colour, neV3 * startpoint, s32 count);

void Cylinder2TerrainTest(neCollisionResult & result, TConvex & cylinderA, neT3 & transA, TConvex & terrainB)
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
			if (CylinderTriTest(cylinderA, transA, res[testRes], triParam))
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
/*		{
			neV3 points[4];
			neV3 red;

			neTriangle_ * t =  &triangleArray[finalTriIndex];

			points[0] = terrainB.vertices[t->indices[0]];
			points[1] = terrainB.vertices[t->indices[1]];
			points[2] = terrainB.vertices[t->indices[2]];
			points[3] = terrainB.vertices[t->indices[0]];

			DrawLine(red, points, 4);
		}
*/		result.penetrate = true;

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

neBool CylinderTriTest_PlaneEnd(TConvex & cylinder, neT3 & trans, ConvexTestResult & result, TriangleParam & tri)
{
	f32 dist = trans.pos.Dot(tri.normal) - tri.d;

	neV3 dir;

	if (dist >= 0.0f)
	{
		dir = tri.normal * -1.0f;

		dist *= -1.0f;
	}
	else
	{
		dir = tri.normal;
	}

	if (neAbs(dist) >= ( cylinder.CylinderHalfHeight() + cylinder.CylinderRadius()))
		return false;

	neV3 contactPoint = trans.pos;

	neV3 l = trans.rot[1] * cylinder.CylinderHalfHeight();

	f32 dot = l.Dot(dir);

	if (dot > 0.0f)
	{
		contactPoint += l;

		dist += dot;
	}
	else
	{
		contactPoint -= l;

		dist -= dot;
	}
	contactPoint += dir * cylinder.CylinderRadius();

	dist += cylinder.CylinderRadius();

	if (dist <= 0.0f)
		return false;

	if (dist >= result.depth)
		return true;

	neV3 project = contactPoint - dir * dist;

	if (tri.IsPointInside(project) != -1)
		return true;

	neV3 project2; 
	
	project2.GetIntersectPlane(dir, tri.vert[0], trans.pos, contactPoint);

	s32 region = tri.IsPointInside(project2);
	
	if (region != -1)
		return true;

	result.valid = true;
	result.depth = dist;
	result.contactA = contactPoint; // on the cylinder
	result.contactB = project; // on the triangle
	result.contactNormal = dir * -1.0f;

	return true;
}

neBool CylinderTriTest_Line(TConvex & cylinder, neT3 & trans, ConvexTestResult & result, neV3 & point1, neV3 & point2)
{
	ConvexTestResult cr;

	cr.edgeA[0] = trans.pos + trans.rot[1] * cylinder.CylinderHalfHeight();
	cr.edgeA[1] = trans.pos - trans.rot[1] * cylinder.CylinderHalfHeight();
	cr.edgeB[0] = point1;
	cr.edgeB[1] = point2;

	f32 au, bu;

	if (!cr.ComputerEdgeContactPoint2(au, bu))
		return true;

	f32 depth = cylinder.CylinderRadius() - cr.depth;

	if (depth <= 0.0f)
		return false;

	if (depth >= result.depth)
		return true;

	if (cr.valid)
	{
		result.depth = depth;
		result.valid = true;
		result.contactA = cr.contactA;
		result.contactB = cr.contactB;
		result.contactNormal = trans.rot[1].Cross(point1 - point2);
		result.contactNormal.Normalize();

		neV3 diff = cr.contactA - cr.contactB;

		if (diff.Dot(result.contactNormal) < 0.0f)
		{
			result.contactNormal *= -1.0f;
		}
		result.contactA -= result.contactNormal * cylinder.CylinderRadius();
	}
	else
	{
		if (au > 0.0f && au < 1.0f)
		{
			// vertex of line and trunk of cylinder

			neV3 vert;

			if (bu <= 0.0f)
			{
				//point1

				vert = point1;
			}
			else
			{
				//point2

				vert = point2;
			}
			neV3 project;

			f32 depth = vert.GetDistanceFromLine2(project, cr.edgeA[0], cr.edgeA[1]);

			depth = cylinder.CylinderRadius() - depth;

			if (depth <= 0.0f)
				return false;

			if (depth >= result.depth)
				return true;

			result.depth = depth;
			result.valid = true;
			result.contactB = vert;
			result.contactNormal = project - vert;
			result.contactNormal.Normalize();
			result.contactA = project - result.contactNormal * cylinder.CylinderRadius();
		}
		else
		{
			neV3 cylinderVert;

			if (au <= 0.0f)
			{
				cylinderVert = cr.edgeA[0];
			}
			else // au >= 1.0f
			{
				cylinderVert = cr.edgeA[1];
			}
			if (bu > 0.0f && bu < 1.0f)
			{
				// cylinderVert and edge

				neV3 project;

				f32 depth = cylinderVert.GetDistanceFromLine2(project, cr.edgeB[0], cr.edgeB[1]);

				depth = cylinder.CylinderRadius() - depth;

				if (depth <= 0.0f)
					return false;

				if (depth >= result.depth)
					return true;

				result.depth = depth;
				result.valid = true;
				result.contactB = project;
				result.contactNormal = cylinderVert - project;
				result.contactNormal.Normalize();
				result.contactA = cylinderVert - result.contactNormal * cylinder.CylinderRadius();
			}
			else
			{
				neV3 lineVert;

				if (bu <= 0.0f)
				{
					//point1
					lineVert = point1;
				}
				else
				{
					//point2
					lineVert = point2;
				}
				neV3 diff = cylinderVert - lineVert;

				f32 depth = diff.Dot(diff);

				if (depth >= cylinder.CylinderRadiusSq())
					return false;

				depth = sqrtf(depth);

				depth = cylinder.CylinderRadius() - depth;

				if (depth >= result.depth)
					return true;

				result.depth = depth;
				result.valid = true;
				result.contactB = lineVert;
				result.contactNormal = diff;
				result.contactNormal.Normalize();
				result.contactA = cylinderVert - result.contactNormal * cylinder.CylinderRadius();
			}
		}
	}
	return true;
}

neBool CylinderTriTest(TConvex & cylinder, neT3 & trans, ConvexTestResult & result, TriangleParam & tri)
{
	// test plane of triangle and rim of cylinder
	result.valid = false;
	result.depth = 1.0e6f;
	result.needTransform = false;

	if (!CylinderTriTest_PlaneEnd(cylinder, trans, result, tri))
		return false;
	
	if (!result.valid)
	{
		result.valid = false;
		result.depth = 1.0e6f;
		result.needTransform = false;
		for (s32 i = 0; i < 3; i++)
		{
			CylinderTriTest_Line(cylinder, trans, result, tri.vert[i], tri.vert[neNextDim1[i]]);
		}
	}
	return result.valid;
}

void TestCylinderVertEdge(neCollisionResult & result, neV3 & edgeA1, neV3 & edgeA2, neV3 & vertB, 
						  TConvex & cA, TConvex & cB, neT3 & transA, neT3 & transB, neBool flip)
{
	neV3 project;

	f32 dist = vertB.GetDistanceFromLine2(project, edgeA1, edgeA2);

	f32 depth = cA.CylinderRadius() + cB.CylinderRadius() - dist;

	if (depth <= 0.0f)
		return;

	if (depth <= result.depth)
		return;

	result.penetrate = true;

	result.depth = depth;

	if (!flip)
	{
		result.collisionFrame[2] = project - vertB;

		result.collisionFrame[2].Normalize();

		result.contactA = project - result.collisionFrame[2] * cA.CylinderRadius();

		result.contactB = vertB + result.collisionFrame[2] * cB.CylinderRadius();
	}
	else
	{
		result.collisionFrame[2] = vertB - project;

		result.collisionFrame[2].Normalize();

		result.contactA = vertB - result.collisionFrame[2] * cB.CylinderRadius();

		result.contactB = project + result.collisionFrame[2] * cA.CylinderRadius();
	}
}

void TestCylinderVertVert(neCollisionResult & result, neV3 & vertA, neV3 & vertB, 
						  TConvex & cA, TConvex & cB, neT3 & transA, neT3 & transB)
{
	neV3 diff = vertA - vertB;

	f32 dist = diff.Length();

	f32 depth = cA.CylinderRadius() + cB.CylinderRadius() - dist;

	if (depth <= 0.0f)
		return;

	if (depth <= result.depth)
		return;

	result.penetrate = true;

	result.depth = depth;

	result.collisionFrame[2] = diff * (1.0f / dist);

	result.contactA = vertA - result.collisionFrame[2] * cA.CylinderRadius();

	result.contactB = vertB + result.collisionFrame[2] * cB.CylinderRadius();
}

void Cylinder2CylinderTest(neCollisionResult & result, TConvex & cA, neT3 & transA, TConvex & cB, neT3 & transB)
{
	result.penetrate = false;

	neV3 dir = transA.rot[1].Cross(transB.rot[1]);

	f32 len = dir.Length();

	neBool isParallel = neIsConsiderZero(len);

	s32 doVertCheck = 0;

	ConvexTestResult cr;

	cr.edgeA[0] = transA.pos + transA.rot[1] * cA.CylinderHalfHeight();
	cr.edgeA[1] = transA.pos - transA.rot[1] * cA.CylinderHalfHeight();
	cr.edgeB[0] = transB.pos + transB.rot[1] * cB.CylinderHalfHeight();
	cr.edgeB[1] = transB.pos - transB.rot[1] * cB.CylinderHalfHeight();

	f32 dot = transA.rot[1].Dot(transB.rot[1]);

	if (!neIsConsiderZero(len))
	{
		f32 au, bu;

		cr.ComputerEdgeContactPoint2(au, bu);

		if (cr.valid)
		{
			f32 depth = cA.CylinderRadius() + cB.CylinderRadius() - cr.depth;

			if (depth <= 0.0f)
				return;

			result.depth = depth;

			result.penetrate = true;

			result.collisionFrame[2] = cr.contactA - cr.contactB;
			
			result.collisionFrame[2] *= (1.0f / cr.depth);

			result.contactA = cr.contactA - result.collisionFrame[2] * cA.CylinderRadius();

			result.contactB = cr.contactB + result.collisionFrame[2] * cB.CylinderRadius();

			return;
		}
	}
	result.depth = -1.0e6f;

	s32 i;

	for (i = 0; i < 2; i++)
	{
		//project onto edge b

		neV3 diff = cr.edgeA[i] - cr.edgeB[1];

		f32 dot = diff.Dot(transB.rot[1]);

		if (dot < 0.0f)
		{
			TestCylinderVertVert(result, cr.edgeA[i], cr.edgeB[1], cA, cB, transA, transB);
		}
		else if (dot > (2.0f * cB.CylinderHalfHeight()))
		{
			TestCylinderVertVert(result, cr.edgeA[i], cr.edgeB[0], cA, cB, transA, transB);
		}
		else
		{
			TestCylinderVertEdge(result, cr.edgeB[0], cr.edgeB[1], cr.edgeA[i], cB, cA, transB, transA, true);
		}
	}
	for (i = 0; i < 2; i++)
	{
		//project onto edge b

		neV3 diff = cr.edgeB[i] - cr.edgeA[1];

		f32 dot = diff.Dot(transA.rot[1]);

		if (dot < 0.0f)
		{
			TestCylinderVertVert(result, cr.edgeB[i], cr.edgeA[1], cA, cB, transA, transB);
		}
		else if (dot > (2.0f * cB.CylinderHalfHeight()))
		{
			TestCylinderVertVert(result, cr.edgeB[i], cr.edgeA[0], cA, cB, transA, transB);
		}
		else
		{
			TestCylinderVertEdge(result, cr.edgeA[0], cr.edgeA[1], cr.edgeB[i], cA, cB, transA, transB, false);
		}
	}
}

void Cylinder2SphereTest(neCollisionResult & result, TConvex & cylinderA, neT3 & transA, TConvex & sphereB, neT3 & transB)
{
	result.penetrate = false;

	neV3 cylinderTop = transA.pos + transA.rot[1] * cylinderA.CylinderHalfHeight();

	neV3 cylinderBottom = transA.pos - transA.rot[1] * cylinderA.CylinderHalfHeight();

	neV3 diff0 = transB.pos - cylinderBottom;

	f32 k = diff0.Dot(transA.rot[1]);

	if (k >= (2.0f * cylinderA.CylinderHalfHeight() + cylinderA.CylinderRadius() + sphereB.Radius()))
		return;

	if (k <= -(cylinderA.CylinderRadius() + sphereB.Radius()))
		return;

	neV3 project;

	if (k > 2.0f * cylinderA.CylinderHalfHeight()) //cylinderTop
	{
		project = cylinderTop;
	}
	else if (k < 0.0f)
	{
		project = cylinderBottom;
	}
	else
	{
		project = cylinderBottom + k * transA.rot[1];
	}

	neV3 diff1 = project - transB.pos;

	f32 dist = diff1.Dot(diff1);
	
	dist = sqrtf(dist);

	if (dist >= (cylinderA.CylinderRadius() + sphereB.Radius()))
		return;

	f32 depth = cylinderA.CylinderRadius() + sphereB.Radius() - dist;

	ASSERT(depth > 0.0f);

	result.penetrate = true;

	result.depth = depth;

	if (!neIsConsiderZero(dist))
		result.collisionFrame[2] = diff1 * (1.0f / dist);
	else
		result.collisionFrame[2] = transA.rot[0];

	result.contactA = project - result.collisionFrame[2] * cylinderA.CylinderRadius();

	result.contactB = transB.pos + result.collisionFrame[2] * cylinderA.CylinderRadius();
}
/*
void Box2CylinderTest(neCollisionResult & result, TConvex & boxA, neT3 & transA, TConvex & cylinderB, neT3 & transB)
{
	result.penetrate = false;

	ConvexTestResult res;

	res.valid = false;

	res.depth = 1.0e6f;

	BoxTestParam boxParamA;

	boxParamA.convex = &boxA;
	boxParamA.trans = &transA;
	boxParamA.radii[0] = transA.rot[0] * boxA.BoxSize(0);
	boxParamA.radii[1] = transA.rot[1] * boxA.BoxSize(1);
	boxParamA.radii[2] = transA.rot[2] * boxA.BoxSize(2);

	for (s32 i = 0; i < 3; i++)
	{
		if (!boxParamA.CylinderFaceTest(res, cylinderB, transB, i))
			return;
	}

	if (!res.valid)
	{
		for (s32 i = 0; i < 3; i++)
		{
			if (!boxParamA.CylinderEdgeTest(res, cylinderB, transB, i))
				return;
		}
	}
	if (res.valid)
	{
		result.penetrate = true;

		result.depth = res.depth;

		result.collisionFrame[2] = res.contactNormal;

		result.contactA = res.contactA;

		result.contactB = res.contactB;
	}
}
*/
void ClosestLine2Box(const neV3 & p1, const neV3 & p2, const neV3 & boxSize, neV3 & lret, neV3 & bret)
{
	neV3 sign, s, v;

	s = p1;
	v = p2 - p1;

	s32 i;
	for (i = 0; i < 3; i++)
	{
		if (v[i] < 0.0f)
		{
			s[i] = -s[i];
			v[i] = -v[i];
			sign[i] = -1.0f;
		}
		else
		{
			sign[i] = 1.0f;
		}
	}

	neV3 v2; v2 = v * v;

	neV3 h; h = boxSize;

	int region[3];
	neV3 tanchor;

	for (i = 0; i < 3; i++)
	{
		if (v[i] > 0.0f)
		{
			if (s[i] < -h[i])
			{
				region[i] = -1;
				tanchor[i] = (-h[i] - s[i]) / v[i];
			}
			else
			{
				region[i] = (s[i] > h[i]);
				tanchor[i] = (h[i] - s[i]) / v[i];
			}
		}
		else
		{
			region[i] = 0;
			tanchor[i] = 2;
		}
	}

	f32 t = 0;
	f32 dd2dt = 0;

	for (i = 0; i < 3; i++)
	{
		dd2dt -= (region[i] ? v2[i] : 0) * tanchor[i];
	}
	if (dd2dt >= 0.0f)
		goto got_answer;

	do {
		f32 next_t = 1;

		for (i = 0; i < 3; i++)
		{
			if (tanchor[i] > t && tanchor[i] < 1 && tanchor[i] < next_t)
				next_t = tanchor[i];
		}
		f32 next_dd2dt = 0;

	    for (i=0; i<3; i++) 
		{
			next_dd2dt += (region[i] ? v2[i] : 0) * (next_t - tanchor[i]);
		}
	    if (next_dd2dt >= 0) 
		{
			f32 m = (next_dd2dt-dd2dt)/(next_t - t);
			t -= dd2dt/m;
			goto got_answer;
		}
	    for (i=0; i<3; i++) {
			if (tanchor[i] == next_t) 
			{
				tanchor[i] = (h[i]-s[i])/v[i];
				region[i]++;
			}
		}
		t = next_t;
		dd2dt = next_dd2dt;
	} while(t < 1);
	
	t = 1;

got_answer:

	neV3 diff = p2 - p1;

	lret = p1 + diff * t;

    for (i=0; i<3; i++) 
	{
		bret[i] = sign[i] * (s[i] + t*v[i]);
		if (bret[i] < -h[i]) 
			bret[i] = -h[i];
		else if (bret[i] > h[i]) 
			bret[i] = h[i];
	}
}

void Box2CylinderTest(neCollisionResult & result, TConvex & boxA, neT3 & transA, TConvex & cylinderB, neT3 & transB)
{
	result.penetrate = false;

	ConvexTestResult res;

	res.valid = false;

	res.depth = 1.0e6f;

	neT3 cylinder2box;

	cylinder2box = transA.FastInverse();

	cylinder2box = cylinder2box * transB;

	neV3 c1, c2;

	c1 = cylinder2box.pos - cylinder2box.rot[1] * cylinderB.as.cylinder.halfHeight;

	c2 = cylinder2box.pos + cylinder2box.rot[1] * cylinderB.as.cylinder.halfHeight;

	neV3 lret, bret;

	ClosestLine2Box(c1, c2, boxA.as.box.boxSize, lret, bret);

	bret = transA * bret;

	lret = transA * lret;

	neV3 diff; 
	
	diff = bret - lret;

	f32 dist = diff.Length();

	if (dist > cylinderB.as.cylinder.radius)
	{
		return;
	}
	result.depth = cylinderB.as.cylinder.radius - dist;

	result.penetrate = true;

	f32 d1 = 1.0f / dist;

	result.contactA = bret;

	result.collisionFrame[2] = diff * d1;

	result.contactB = (lret + result.collisionFrame[2] * cylinderB.as.cylinder.radius) ;
}