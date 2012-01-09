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
#include "dcd.h"
//#include "rigidbody.h"

#include <assert.h>
#include <stdio.h>

extern s32 currentMicroStep;

//extern void DrawLine(const neV3 & colour, neV3 * startpoint, s32 count);

/****************************************************************************
*
*	Box2TriangleTest
*
****************************************************************************/ 

void Box2TriangleTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB)
{
	ConvexTestResult res;

	BoxTestParam boxParamA;

	boxParamA.convex = &convexA;
	boxParamA.trans = &transA;
	boxParamA.radii[0] = transA.rot[0] * convexA.as.box.boxSize[0];
	boxParamA.radii[1] = transA.rot[1] * convexA.as.box.boxSize[1];
	boxParamA.radii[2] = transA.rot[2] * convexA.as.box.boxSize[2];

	TriangleParam triParam;

	triParam.vert[0] = transB * convexB.vertices[convexB.as.tri.indices[0]];
	triParam.vert[1] = transB * convexB.vertices[convexB.as.tri.indices[1]];
	triParam.vert[2] = transB * convexB.vertices[convexB.as.tri.indices[2]];

	triParam.edges[0] = triParam.vert[1] - triParam.vert[0];
	triParam.edges[1] = triParam.vert[2] - triParam.vert[1];
	triParam.edges[2] = triParam.vert[0] - triParam.vert[2];
	triParam.normal = triParam.edges[1].Cross(triParam.edges[0]);
	triParam.normal.Normalize();
	triParam.d = triParam.normal.Dot(triParam.vert[0]);

	if (boxParamA.TriTest(res, triParam))
	{
		result.penetrate = true;

		result.depth = res.depth;

//		result.collisionFrame[0] = res.contactX;
//		result.collisionFrame[1] = res.contactY;
		result.collisionFrame[2] = res.contactNormal;

		if (res.isEdgeEdge)
		{
			result.contactA = res.contactA;

			result.contactB = res.contactB;
		}
		else
		{
			result.contactA = res.contactA;
			
			result.contactB = res.contactA;
		}
	}
	else
	{
		result.penetrate = false;
	}
}

NEINLINE bool TriangleParam::PointInYProjection(neV3 & point)
{
	f32 sign1, sign2;

	neV3 line1 = point - vert[0];

	neV3 line2 = point - vert[1];

	sign1 = line1[2] * edges[2][0] - line1[0] * edges[2][2];

	sign2 = line2[2] * edges[1][0] - line2[0] * edges[1][2];

	f32 mul = sign1 * sign2;

	if (mul < 0.0f)
		return false;
	
	f32 sign3 = line1[2] * edges[0][0] - line1[0] * edges[0][2];

	mul = sign1 * sign3;

	if (mul < 0.0f)
		return false;

	return true;
/*	
	if (normal[1] > 0.0f)
	{
		return (sign1 < 0.0f);
	}
	else
	{
		return (sign1 > 0.0f);
	}
*/
}

s32 TriangleParam::IsPointInside(const neV3 & point)
{
	//select coordinate
	s32 dim0, dim1, plane;
	f32 clockness; // 1.0 counter clockwise, -1.0 clockwise

	if (neAbs(normal[1]) > neAbs(normal[2]))
	{
		if (neAbs(normal[1]) > neAbs(normal[0])) //use y plane
		{
			plane = 1;
			dim0 = 2;//0;
			dim1 = 0;//2;
		}
		else //use x plane
		{
			plane = 0;
			dim0 = 1;
			dim1 = 2;
		}
	}
	else if (neAbs(normal[2]) > neAbs(normal[0])) //use z plane
	{
		plane = 2;
		dim0 = 0;
		dim1 = 1;
	}
	else //use x plane
	{
		plane = 0;
		dim0 = 1;
		dim1 = 2;
	}

	clockness = normal[plane] > 0.0f ? 1.0f : -1.0f;

	f32 det0, det1, det2;

#define pointA (vert[0])
#define pointB (vert[1])
#define pointC (vert[2])

	det0 = (point[dim0] - pointA[dim0]) * (pointA[dim1] - pointB[dim1]) + 
			(pointA[dim1] - point[dim1]) * (pointA[dim0] - pointB[dim0]);
	
	det1 = (point[dim0] - pointB[dim0]) * (pointB[dim1] - pointC[dim1]) + 
			(pointB[dim1] - point[dim1]) * (pointB[dim0] - pointC[dim0]);
	
	det2 = (point[dim0] - pointC[dim0]) * (pointC[dim1] - pointA[dim1]) + 
			(pointC[dim1] - point[dim1]) * (pointC[dim0] - pointA[dim0]);

	s32 ret;

	if (det0 > 0.0f)
	{
		if (det1 > 0.0f)
		{
			if (det2 > 0.0f)
			{
				ret = -1; // inside
			}
			else
			{
				ret = 5; // outside edge 2
			}
		}
		else 
		{
			if (det2 > 0.0f)
			{
				ret = 3; // outside edge 1
			}
			else
			{
				ret = 4; // outside vertex 2
			}
		}
	}
	else 
	{
		if (det1 > 0.0f)
		{
			if (det2 > 0.0f)
			{
				ret = 1; // outside edge 0
			}
			else
			{
				ret = 0; // outside vertex 0
			}
		}
		else
		{
			if (det2 > 0.0f)
			{
				ret = 2; // outside vertex 1
			}
			else
			{
				ret = -1; // inside
			}
		}
	}

	if (ret == -1)
		return ret;

	if (clockness == -1.0f)
	{
		ret = (ret + 3) % 6;
	}

	return ret;

/*
	if (det0 > 0.0f && det1 > 0.0f && det2 > 0.0f)
		return true;

	if (det0 < 0.0f && det1 < 0.0f && det2 < 0.0f)
		return true;

	return false;
*/
}

void TriangleParam::ConputeExtraInfo()
{
	s32 i;

	for (i = 0; i < 3; i++)
	{
		edgeNormals[i] = normal.Cross(edges[i]);

		edgeNormals[i].Normalize();

		neV3 diff = vert[neNextDim2[i]] - vert[i];

		if (diff.Dot(edgeNormals[i]) > 0.0f)
		{
			edgeNormals[i] *= -1.0f;
		}
	}
	for (i = 0; i < 3; i++)
	{
		vertNormals[i] = edgeNormals[i] + edgeNormals[neNextDim2[i]];

		vertNormals[i].Normalize();
	}
}

void TriangleParam::Transform(const TriangleParam & from, neT3 & trans)
{
	s32 i;

	for (i = 0; i < 3; i++)
	{
		vert[i] = trans * from.vert[i];
	}
	for (i = 0; i < 3; i++)
	{
		edges[i] = vert[neNextDim1[i]] - vert[i];
	}
	normal = trans.rot * from.normal;

	d = normal.Dot(vert[i]);
}

bool BoxTestParam::TriHeightTest(ConvexTestResult & result, TriangleParam & tri)
{
	if (!isVertCalc)
		CalcVertInWorld();

	f32 deepest = 0.0f;

	bool found = false;

	for (s32 i = 0; i < 8; i++)
	{
		if (!tri.PointInYProjection(verts[i])) // vert in tri projection
			continue;

		f32 height = tri.d - tri.normal[0] * verts[i][0] - tri.normal[2] * verts[i][2];

		height /= tri.normal[1];

		f32 penetrate = height - verts[i][1];

		if  (penetrate > deepest)
		{
			deepest = penetrate;

			result.depth = penetrate;

			result.contactA = verts[i];

			result.contactB = verts[i];

			result.contactB[1] = height;//verts[i][1] + penetrate;

			result.valid = true;

			result.contactNormal = tri.normal;

			found = true;
		}
	}
	return found;
}

bool BoxTestParam::TriTest(ConvexTestResult & result, TriangleParam & tri)
{
	result.depth = 1.e5f;
	result.isEdgeEdge = false;
	result.valid = false;

	if (!MeasurePlanePenetration(result, tri.normal, tri.d))
		return false;

	ConvexTestResult result0;

	result0.valid = false;
	result0.depth = result.depth;

	if (MeasureBoxFaceTrianglePenetration(result0, tri, 0) &&
		MeasureBoxFaceTrianglePenetration(result0, tri, 1) &&
		MeasureBoxFaceTrianglePenetration(result0, tri, 2)
		)
	{
		if (result0.valid)
		{
			result = result0;
		}
	}
	else
		return false;

	ConvexTestResult result2;

	result2.valid = false;

	result2.depth = result.depth;

	bool edgeCollided = false;

	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 0, 0))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 0, 1))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 0, 2))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 1, 0))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 1, 1))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 1, 2))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 2, 0))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 2, 1))
		return false;
	if (!MeasureBoxEdgeTriangleEdgePenetration(result2, tri, 2, 2))
		return false;

	if (result2.valid)
		edgeCollided = true;

	if (edgeCollided)
	{
		ConvexTestResult result3;

		if (result2.ComputerEdgeContactPoint(result3))
		{
			result.isEdgeEdge = true;
			result.contactA = result3.contactA;
			result.contactB = result3.contactB;
			result.depth = result2.depth;
			//result.contactX = result2.contactX;// * -1.0f;
			//result.contactY = result2.contactY;
			result.contactNormal = result2.contactNormal;// * -1.0f;
		}
		else
		{
			return result.valid;
		}
	}
	else
	{
		return result.valid;
	}

	return true;
}

NEINLINE bool BoxTestParam::MeasurePlanePenetration(ConvexTestResult & result, const neV3 & normal, f32 d)
{
	f32 dot = normal.Dot(trans->pos);

	f32 penetrated = dot - d;

	neV3 contactPoint = trans->pos;

	neV3 contactNormal;

	if (penetrated < 0.0f)
	{	
		contactNormal = normal * -1.0f;
	}
	else 
	{	
		contactNormal = normal;
		penetrated *= -1.0f;
	}
	neV3 progression = contactNormal * radii;

	neV3 sign;

	sign[0] = progression[0] > 0.0f ? 1.0f: -1.0f;
	sign[1] = progression[1] > 0.0f ? 1.0f: -1.0f;
	sign[2] = progression[2] > 0.0f ? 1.0f: -1.0f;

	penetrated += (progression[0] * sign[0]);
	penetrated += (progression[1] * sign[1]);
	penetrated += (progression[2] * sign[2]);

	contactPoint -= (radii[0] * sign[0]);
	contactPoint -= (radii[1] * sign[1]);
	contactPoint -= (radii[2] * sign[2]);
	
	if (penetrated < 0.0f)
		return false;

	if (penetrated < result.depth)
	{
		result.depth = penetrated;
		result.contactA = contactPoint; 
		result.contactB = contactPoint + contactNormal * penetrated;//need to project point onto triangle face
		result.valid = true;
		result.contactNormal = contactNormal;
		//ChooseAxis(result.contactX, result.contactY, result.contactNormal);
	}
	return true;
}

bool BoxTestParam::MeasureBoxFaceTrianglePenetration(ConvexTestResult & result, TriangleParam & tri, s32 whichFace)
{
	neV3 contactNormal = trans->rot[whichFace];

	f32 triMin;
	f32 triMax;
	s32 minVert = 0;
	s32 maxVert = 0;

	triMin = triMax = contactNormal.Dot(tri.vert[0]);

	f32 dot = contactNormal.Dot(tri.vert[1]);

	if (dot < triMin)
	{
		triMin = dot;
		minVert = 1;
	}
	else if (dot > triMax)
	{
		triMax = dot;
		maxVert = 1;
	}
	dot = contactNormal.Dot(tri.vert[2]);

	if (dot < triMin)
	{
		triMin = dot;
		minVert = 2;
	}
	else if (dot > triMax)
	{
		triMax = dot;
		maxVert = 2;
	}
	f32 p = trans->pos.Dot(contactNormal);
	f32 boxMin = p - convex->as.box.boxSize[whichFace];
	f32 boxMax = p + convex->as.box.boxSize[whichFace];

	if (triMin >= boxMax)
		return false;

	if (triMax <= boxMin)
		return false;

	f32 d1 = boxMax - triMin;
	f32 d2 = triMax - boxMin;

	f32 penetrated;
	neV3 contactPoint;
	bool reverse = false;

	if (d1 < d2)
	{
		penetrated = d1;
		contactNormal *= -1.0f;
		contactPoint = tri.vert[minVert];
		reverse = true;
	}
	else
	{
		penetrated = d2;
		contactPoint = tri.vert[maxVert];
	}
	if (penetrated < result.depth)
	{
		s32 otherAxis1 = (whichFace + 1) % 3;
		
		s32 otherAxis2 = (whichFace + 2) % 3;

		result.depth = penetrated;
		result.contactA = contactPoint;
		result.contactB = contactPoint + contactNormal * penetrated;
		result.valid = true;
		result.contactNormal = contactNormal;
		if (reverse)
			result.contactX = trans->rot[otherAxis1] * -1.0f;
		else
			result.contactX = trans->rot[otherAxis1];

		result.contactY = trans->rot[otherAxis2];
	}
	return true;
}

bool BoxTestParam::MeasureBoxEdgeTriangleEdgePenetration(ConvexTestResult & result, TriangleParam & tri, s32 dim1, s32 dim2)
{
	neV3 edgeNormal = tri.edges[dim2];

	edgeNormal.Normalize();

	if (edgeNormal.IsConsiderZero())
		return true;

	neV3 contactNormal = trans->rot[dim1].Cross(edgeNormal);

	if (contactNormal.IsConsiderZero())
		return true;

	contactNormal.Normalize(); // do we need this?

	if (contactNormal.IsConsiderZero())
		return true;

	neV3 contactPoint = trans->pos;

	s32 otherAxis1 = (dim1 + 1) % 3;
	s32 otherAxis2 = (dim1 + 2) % 3;

	f32 p = contactNormal.Dot(contactPoint);

	f32 dot1,dot2;
	f32 sign1, sign2;

	dot1 = contactNormal.Dot(radii[otherAxis1]);
	dot2 = contactNormal.Dot(radii[otherAxis2]);

	f32 boxMin, boxMax;

	sign1 = dot1 < 0.0f ? -1.0f : 1.0f;
	sign2 = dot2 < 0.0f ? -1.0f : 1.0f;

	boxMax = p + dot1 * sign1;
	boxMax += dot2 * sign2;

	boxMin = p - dot1 * sign1;
	boxMin -= dot2 * sign2;

	f32 triMin;
	f32 triMax;
	
	f32 q = contactNormal.Dot(tri.vert[dim2]);
	f32 r = contactNormal.Dot(tri.vert[(dim2+2)%3]);

	if (q < r)
	{
		triMin = q;
		triMax = r;
	}
	else
	{
		triMin = r;
		triMax = q;
	}

	if (triMin >= boxMax || triMax <= boxMin)
		return false;

	f32 penetrated;

	if (triMin == q)
	{
		contactNormal = contactNormal * -1.0f;
		penetrated = boxMax - triMin;

		contactPoint += (radii[otherAxis1] * sign1);
		contactPoint += (radii[otherAxis2] * sign2);
	}
	else
	{
		penetrated = triMax - boxMin;
		
		contactPoint -= (radii[otherAxis1] * sign1);
		contactPoint -= (radii[otherAxis2] * sign2);
	}
	if (penetrated < result.depth)
	{
		result.depth = penetrated;
		result.contactA = contactPoint;
		result.contactB = contactPoint;
		result.valid = true;
		result.contactNormal = contactNormal;
		//ChooseAxis(result.contactX, result.contactY, contactNormal);

		result.edgeA[0] = contactPoint + radii[dim1];
		result.edgeA[1] = contactPoint - radii[dim1];

		result.edgeB[0] = tri.vert[dim2];
		result.edgeB[1] = tri.vert[(dim2+1)%3];
	}
	return true;
}

/****************************************************************************
*
*	Box2TerrainTest
*
****************************************************************************/ 

//static s32 callCnt = 0;

void Box2TerrainTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB)
{
//	Convex2TerrainTest(result, convexA, transA, convexB);

//	return;

	neSimpleArray<s32> & _triIndex = *convexB.as.terrain.triIndex;

	s32 triangleCount = _triIndex.GetUsedCount();

	neArray<neTriangle_> & triangleArray = *convexB.as.terrain.triangles;

	ConvexTestResult res[2];

	BoxTestParam boxParamA;

	boxParamA.convex = &convexA;
	boxParamA.trans = &transA;
	boxParamA.radii[0] = transA.rot[0] * convexA.as.box.boxSize[0];
	boxParamA.radii[1] = transA.rot[1] * convexA.as.box.boxSize[1];
	boxParamA.radii[2] = transA.rot[2] * convexA.as.box.boxSize[2];

	s32 finalTriIndex = -1;
	s32 currentRes = 1;
	s32 testRes = 0;

	res[currentRes].depth = -1.0e6f;
	res[currentRes].valid = false;
	res[testRes].depth = 1.0e6f;
	
	s32 terrainMatID = 0;

	u32 userData = 0;
/*
	callCnt++;

	if (callCnt == 21)
		ASSERT(0);
*/
	for (s32 i = 0; i < triangleCount; i++)
	{
		s32 test = _triIndex[i];

		neTriangle_ * t = &triangleArray[_triIndex[i]];

		TriangleParam triParam;

		triParam.vert[0] = convexB.vertices[t->indices[0]];
		triParam.vert[1] = convexB.vertices[t->indices[1]];
		triParam.vert[2] = convexB.vertices[t->indices[2]];

		triParam.edges[0] = triParam.vert[1] - triParam.vert[0];
		triParam.edges[1] = triParam.vert[2] - triParam.vert[1];
		triParam.edges[2] = triParam.vert[0] - triParam.vert[2];
		triParam.normal = triParam.edges[0].Cross(triParam.edges[1]);
		triParam.normal.Normalize();
		triParam.d = triParam.normal.Dot(triParam.vert[0]);

		if (t->flag == neTriangle::NE_TRI_TRIANGLE)
		{
			if (boxParamA.TriTest(res[testRes], triParam))
			{
				if (res[testRes].depth > res[currentRes].depth)
				{
					s32 tmp = testRes;	

					testRes = currentRes;

					currentRes = tmp;

					terrainMatID = t->materialID;

					finalTriIndex = _triIndex[i];

					userData = t->userData;
				}
			}
		}
		else if (t->flag == neTriangle::NE_TRI_HEIGHT_MAP)
		{
			if (boxParamA.TriHeightTest(res[testRes], triParam))
			{
				if (res[testRes].depth > res[currentRes].depth)
				{
					s32 tmp = testRes;	

					testRes = currentRes;

					currentRes = tmp;

					terrainMatID = t->materialID;

					finalTriIndex = _triIndex[i];

					userData = t->userData;
				}
			}
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

			points[0] = convexB.vertices[t->indices[0]];
			points[1] = convexB.vertices[t->indices[1]];
			points[2] = convexB.vertices[t->indices[2]];
			points[3] = convexB.vertices[t->indices[0]];

			DrawLine(red, points, 4);
		}
*/		result.penetrate = true;

		result.depth = res[currentRes].depth;

		result.convexB = (TConvex*)userData;

		//result.collisionFrame[0] = res[currentRes].contactX;
		//result.collisionFrame[1] = res[currentRes].contactY;
		result.collisionFrame[2] = res[currentRes].contactNormal;

		result.materialIdB = terrainMatID;

		//if (res[currentRes].isEdgeEdge)
		{
			result.contactA = res[currentRes].contactA;

			result.contactB = res[currentRes].contactB;
		}
		//else
		//{
		//	result.contactA = res[currentRes].contactA;
		//	
		//	result.contactB = res[currentRes].contactB;
		//}
	}
	else
	{
		result.penetrate = false;
	}
}

