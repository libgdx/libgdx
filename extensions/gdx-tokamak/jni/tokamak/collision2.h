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

class TriangleParam
{
public:
	neV3 vert[3];
	neV3 edges[3];
	neV3 normal;
	f32 d;

	//extra info for cylinder

//	neV3 edgeUnits[3];
	neV3 edgeNormals[3]; // the normal for the edges
	neV3 vertNormals[3];

	void ConputeExtraInfo();

	void Transform(const TriangleParam & from, neT3 & trans);

	NEINLINE bool PointInYProjection(neV3 & point);

	s32 IsPointInside(const neV3 & point);
};

typedef struct ConvexTestResult ConvexTestResult;

struct ConvexTestResult
{
	neV3 contactA;
	neV3 contactB;
	neV3 contactNormal;
	neV3 contactX;
	neV3 contactY;
	neV3 edgeA[2];
	neV3 edgeB[2];
	f32 depth;
	bool valid;
	bool isEdgeEdge;
	bool needTransform;
	neBool ComputerEdgeContactPoint(ConvexTestResult & res);
	neBool ComputerEdgeContactPoint2(f32 & au, f32 & bu);
	void Reverse()
	{
		neSwap(contactA, contactB);
		contactNormal *= -1.0f;
	}
};

class BoxTestParam
{
public:
	BoxTestParam()
	{
		isVertCalc = false;
	}
	TConvex * convex;
	neT3 * trans;
	neM3 radii;
	
	bool isVertCalc;
	neV3 verts[8];

	void CalcVertInWorld();
	bool BoxTest(ConvexTestResult & result, BoxTestParam & otherBox);
	bool MeasureVertexFacePeneration(ConvexTestResult & result, BoxTestParam & otherBox, s32 whichFace);
	neBool MeasureEdgePeneration(ConvexTestResult & result, BoxTestParam & otherBox, s32 dim1, s32 dim2);
	bool TriTest(ConvexTestResult & result, TriangleParam & tri);
	bool TriHeightTest(ConvexTestResult & result, TriangleParam & tri);
	NEINLINE bool MeasurePlanePenetration(ConvexTestResult & result, const neV3 & normal, f32 d);
	bool MeasureBoxFaceTrianglePenetration(ConvexTestResult & result, TriangleParam & tri, s32 whichFace);
	bool MeasureBoxEdgeTriangleEdgePenetration(ConvexTestResult & result, TriangleParam & tri, s32 dim1, s32 dim2);

	//cylinder functions

	//neBool CylinderEndVertexTest(ConvexTestResult & res, TConvex & cylinder);

	//neBool CylinderRimFaceTest(ConvexTestResult & res, TConvex & cylinder, s32 whichFace);

	neBool CylinderFaceTest(ConvexTestResult & res, TConvex & cylinderB, neT3 & transB, s32 whichFace);

	//neBool CylinderEdgeTest(ConvexTestResult & res, TConvex & cylinder, s32 whichEdge);

	neBool CylinderEdgeTest(ConvexTestResult & res, TConvex & cylinder, neT3 & transB, s32 whichEdge);

	neBool LineTest(ConvexTestResult & res, neV3 & point1, neV3 & point2);
};

void ChooseAxis(neV3 & x, neV3 & y, const neV3 & normal);

neBool SphereTriTest(const neV3 & center, f32 radius, ConvexTestResult & result, TriangleParam & tri);

neBool CylinderTriTest(TConvex & sphere, neT3 & trans, ConvexTestResult & result, TriangleParam & tri);

neBool CylinderTriTest_Line(TConvex & cylinder, neT3 & trans, ConvexTestResult & result, neV3 & point1, neV3 & point2);

