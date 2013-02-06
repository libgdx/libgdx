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

#include "math/ne_type.h"
#include "math/ne_debug.h"
#include "tokamak.h"
#include "containers.h"
#include "scenery.h"
#include "collision.h"
#include "constraint.h"
#include "rigidbody.h"
#include "scenery.h"
/*
#ifdef _WIN32
#include <windows.h>
#endif
*/
#include "stack.h"
#include "simulator.h"

/********************************************************/
/* AABB-triangle overlap test code                      */
/* by Tomas Akenine-Möller                              */
/* Function: int triBoxOverlap(float boxcenter[3],      */
/*          float boxhalfsize[3],float triverts[3][3]); */
/* History:                                             */
/*   2001-03-05: released the code in its first version */
/*   2001-06-18: changed the order of the tests, faster */
/*                                                      */
/* Acknowledgement: Many thanks to Pierre Terdiman for  */
/* suggestions and discussions on how to optimize code. */
/* Thanks to David Hunt for finding a ">="-bug!         */
/********************************************************/
#include <math.h>
#include <stdio.h>

#define X 0
#define Y 1
#define Z 2

#define CROSS(dest,v1,v2) \
          dest[0]=v1[1]*v2[2]-v1[2]*v2[1]; \
          dest[1]=v1[2]*v2[0]-v1[0]*v2[2]; \
          dest[2]=v1[0]*v2[1]-v1[1]*v2[0];

#define DOT(v1,v2) (v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2])

#define SUB(dest,v1,v2) \
          dest[0]=v1[0]-v2[0]; \
          dest[1]=v1[1]-v2[1]; \
          dest[2]=v1[2]-v2[2];

#define FINDMINMAX(x0,x1,x2,min,max) \
  min = max = x0;   \
  if(x1<min) min=x1;\
  if(x1>max) max=x1;\
  if(x2<min) min=x2;\
  if(x2>max) max=x2;

int planeBoxOverlap(float normal[3],float d, float maxbox[3])
{
  int q;
  float vmin[3],vmax[3];
  for(q=X;q<=Z;q++)
  {
    if(normal[q]>0.0f)
    {
      vmin[q]=-maxbox[q];
      vmax[q]=maxbox[q];
    }
    else
    {
      vmin[q]=maxbox[q];
      vmax[q]=-maxbox[q];
    }
  }
  if(DOT(normal,vmin)+d>0.0f) return 0;
  if(DOT(normal,vmax)+d>=0.0f) return 1;

  return 0;
}


/*======================== X-tests ========================*/
#define AXISTEST_X01(a, b, fa, fb)             \
    p0 = a*v0[Y] - b*v0[Z];                    \
    p2 = a*v2[Y] - b*v2[Z];                    \
        if(p0<p2) {min=p0; max=p2;} else {min=p2; max=p0;} \
    rad = fa * boxhalfsize[Y] + fb * boxhalfsize[Z];   \
    if(min>rad || max<-rad) return 0;

#define AXISTEST_X2(a, b, fa, fb)              \
    p0 = a*v0[Y] - b*v0[Z];                    \
    p1 = a*v1[Y] - b*v1[Z];                    \
        if(p0<p1) {min=p0; max=p1;} else {min=p1; max=p0;} \
    rad = fa * boxhalfsize[Y] + fb * boxhalfsize[Z];   \
    if(min>rad || max<-rad) return 0;

/*======================== Y-tests ========================*/
#define AXISTEST_Y02(a, b, fa, fb)             \
    p0 = -a*v0[X] + b*v0[Z];                   \
    p2 = -a*v2[X] + b*v2[Z];                       \
        if(p0<p2) {min=p0; max=p2;} else {min=p2; max=p0;} \
    rad = fa * boxhalfsize[X] + fb * boxhalfsize[Z];   \
    if(min>rad || max<-rad) return 0;

#define AXISTEST_Y1(a, b, fa, fb)              \
    p0 = -a*v0[X] + b*v0[Z];                   \
    p1 = -a*v1[X] + b*v1[Z];                       \
        if(p0<p1) {min=p0; max=p1;} else {min=p1; max=p0;} \
    rad = fa * boxhalfsize[X] + fb * boxhalfsize[Z];   \
    if(min>rad || max<-rad) return 0;

/*======================== Z-tests ========================*/

#define AXISTEST_Z12(a, b, fa, fb)             \
    p1 = a*v1[X] - b*v1[Y];                    \
    p2 = a*v2[X] - b*v2[Y];                    \
        if(p2<p1) {min=p2; max=p1;} else {min=p1; max=p2;} \
    rad = fa * boxhalfsize[X] + fb * boxhalfsize[Y];   \
    if(min>rad || max<-rad) return 0;

#define AXISTEST_Z0(a, b, fa, fb)              \
    p0 = a*v0[X] - b*v0[Y];                \
    p1 = a*v1[X] - b*v1[Y];                    \
        if(p0<p1) {min=p0; max=p1;} else {min=p1; max=p0;} \
    rad = fa * boxhalfsize[X] + fb * boxhalfsize[Y];   \
    if(min>rad || max<-rad) return 0;

int _triBoxOverlap_(float boxcenter[3],float boxhalfsize[3],float triverts[3][3])
{

  /*    use separating axis theorem to test overlap between triangle and box */
  /*    need to test for overlap in these directions: */
  /*    1) the {x,y,z}-directions (actually, since we use the AABB of the triangle */
  /*       we do not even need to test these) */
  /*    2) normal of the triangle */
  /*    3) crossproduct(edge from tri, {x,y,z}-directin) */
  /*       this gives 3x3=9 more tests */
   float v0[3],v1[3],v2[3];
   float min,max,d,p0,p1,p2,rad,fex,fey,fez;
   float normal[3],e0[3],e1[3],e2[3];

   /* This is the fastest branch on Sun */
   /* move everything so that the boxcenter is in (0,0,0) */
   SUB(v0,triverts[0],boxcenter);
   SUB(v1,triverts[1],boxcenter);
   SUB(v2,triverts[2],boxcenter);

   /* compute triangle edges */
   SUB(e0,v1,v0);      /* tri edge 0 */
   SUB(e1,v2,v1);      /* tri edge 1 */
   SUB(e2,v0,v2);      /* tri edge 2 */

   /* Bullet 3:  */
   /*  test the 9 tests first (this was faster) */
   fex = fabs(e0[X]);
   fey = fabs(e0[Y]);
   fez = fabs(e0[Z]);
   AXISTEST_X01(e0[Z], e0[Y], fez, fey);
   AXISTEST_Y02(e0[Z], e0[X], fez, fex);
   AXISTEST_Z12(e0[Y], e0[X], fey, fex);

   fex = fabs(e1[X]);
   fey = fabs(e1[Y]);
   fez = fabs(e1[Z]);
   AXISTEST_X01(e1[Z], e1[Y], fez, fey);
   AXISTEST_Y02(e1[Z], e1[X], fez, fex);
   AXISTEST_Z0(e1[Y], e1[X], fey, fex);

   fex = fabs(e2[X]);
   fey = fabs(e2[Y]);
   fez = fabs(e2[Z]);
   AXISTEST_X2(e2[Z], e2[Y], fez, fey);
   AXISTEST_Y1(e2[Z], e2[X], fez, fex);
   AXISTEST_Z12(e2[Y], e2[X], fey, fex);

   /* Bullet 1: */
   /*  first test overlap in the {x,y,z}-directions */
   /*  find min, max of the triangle each direction, and test for overlap in */
   /*  that direction -- this is equivalent to testing a minimal AABB around */
   /*  the triangle against the AABB */

   /* test in X-direction */
   FINDMINMAX(v0[X],v1[X],v2[X],min,max);
   if(min>boxhalfsize[X] || max<-boxhalfsize[X]) return 0;

   /* test in Y-direction */
   FINDMINMAX(v0[Y],v1[Y],v2[Y],min,max);
   if(min>boxhalfsize[Y] || max<-boxhalfsize[Y]) return 0;

   /* test in Z-direction */
   FINDMINMAX(v0[Z],v1[Z],v2[Z],min,max);
   if(min>boxhalfsize[Z] || max<-boxhalfsize[Z]) return 0;

   /* Bullet 2: */
   /*  test if the box intersects the plane of the triangle */
   /*  compute plane equation of triangle: normal*x+d=0 */
   CROSS(normal,e0,e1);
   d=-DOT(normal,v0);  /* plane eq: normal.x+d=0 */
   if(!planeBoxOverlap(normal,d,boxhalfsize)) return 0;

   return 1;   /* box and triangle overlaps */
}


//extern void DrawLine(const neV3 & colour, neV3 * startpoint, s32 count);

s32 neTreeNode::numOfChildren = 4;//NE_TREE_DIM * NE_TREE_DIM;

void FindCenterOfMass(neTriangleTree * tree, neSimpleArray<s32>& triIndex, neV3 * com)
{
	s32 i;
	
	neV3 ret;

	ret.SetZero();
	
	for(i = 0; i < triIndex.GetUsedCount(); i++)
	{
		neTriangle_ & t = tree->triangles[triIndex[i]];

		for (s32 j = 0; j < 3; j++)
		{
			ret += tree->vertices[t.indices[j]];
		}
	}

	f32 div = (f32)triIndex.GetUsedCount() * 3;

	ret /= div;

	*com = ret;
	//_mm_store_ps(&com->v[0], ret.m);
}

void FindMinMaxBound(neTriangleTree * tree, neSimpleArray<s32>& triIndex, neV3 & minBound, neV3 & maxBound)
{
	s32 i;
	
	minBound.Set(1.0e6f, 1.0e6f, 1.0e6f);

	maxBound.Set(-1.0e6f, -1.0e6f, -1.0e6f);

	for(i = 0; i < triIndex.GetUsedCount(); i++)
	{
		neTriangle_ & t = tree->triangles[triIndex[i]];

		for (s32 j = 0; j < 3; j++)
		{
			minBound.SetMin(minBound, tree->vertices[t.indices[j]]);

			maxBound.SetMax(maxBound, tree->vertices[t.indices[j]]);
		}
	}
}

neBool IntersectAABBTriangle(neTriangleTree * tree, const neV3 & minBound, const neV3 & maxBound, const neTriangle & triangle)
{
	neCollision box;
	neCollision tri;
//	neT3 boxt3;
	neT3 trit3;

	trit3.SetIdentity();
	tri.obb.SetTriangle(triangle.indices[0], triangle.indices[1], triangle.indices[2], tree->vertices);
	tri.obb.SetTransform(trit3);
	tri.convexCount = 1;
	tri.convex = &tri.obb;
/*
	if (triangle.indices[0] == 1312 &&
		triangle.indices[1] == 1363 &&
		triangle.indices[2] == 1364)
	{
		ASSERT(0);
	}
*/

	neV3 edge0 = tree->vertices[triangle.indices[1]] - tree->vertices[triangle.indices[0]];
	neV3 edge1 = tree->vertices[triangle.indices[2]] - tree->vertices[triangle.indices[1]];

	neV3 normal = edge0.Cross(edge1);

	normal.Normalize();

	f32 xfactor = 0.0f;
	f32 yfactor = 0.0f;
	f32 zfactor = 0.0f;

	if (neIsConsiderZero(neAbs(normal[0]) - 1.0f))
	{
		xfactor = 0.01f;
	}
	else if (neIsConsiderZero(neAbs(normal[1]) - 1.0f))
	{
		yfactor = 0.01f;
	}
	else if (neIsConsiderZero(neAbs(normal[2]) - 1.0f))
	{
		zfactor = 0.01f;
	}

	float boxpos[3];
	float boxhalfsize[3];
	float triverts[3][3];
	int i;

	boxhalfsize[0] = ((maxBound[0] - minBound[0]) * 0.5f) + xfactor;
	boxhalfsize[1] = ((maxBound[1] - minBound[1]) * 0.5f) + yfactor;
	boxhalfsize[2] = ((maxBound[2] - minBound[2]) * 0.5f) + zfactor;

	for (i = 0; i < 3; i++)
		boxpos[i] = (maxBound[i] + minBound[i]) * 0.5f;

	for (i = 0; i < 3; i++)
		for (int j = 0; j < 3; j++)
			triverts[i][j] = tree->vertices[triangle.indices[i]][j];

	int ret = _triBoxOverlap_(boxpos, boxhalfsize, triverts);

	return ret;

/*
	boxt3.SetIdentity();
	box.obb.SetBoxSize((maxBound[0] - minBound[0]) + xfactor, 
						(maxBound[1] - minBound[1]) + yfactor, 
						(maxBound[2] - minBound[2]) + zfactor);
	box.obb.SetTransform(boxt3);
	box.convexCount = 1;
	box.convex = &box.obb;

	neCollisionResult res;

	boxt3.pos = ( maxBound + minBound ) * 0.5f;

	CollisionTest(res, box, boxt3, tri, trit3);

	return res.penetrate;
*/
}

/****************************************************************************
*
*	neTreeNode::neTreeNode
*
****************************************************************************/ 

neTreeNode::neTreeNode()
{
	//triangleIndices = NULL; //leaf only
	neV3 minBound, maxBound;

	minBound.SetZero();

	maxBound.SetZero();

	Initialise(NULL, 0, minBound, maxBound);
}

void neTreeNode::Initialise(neTriangleTree * _tree, s32 _parent, const neV3 & minBound, const neV3 & maxBound)
{
	s32 i;

	for (i = 0; i < 3; i++)
	{
		bounds[i][0] = minBound[i];
		bounds[i][1] = maxBound[i];
	}

	tree = _tree;

	parent = _parent;

	for (i = 0; i < numOfChildren; i++)
		children[i] = -1;
}

/****************************************************************************
*
*	neTreeNode::Build
*
****************************************************************************/ 

void neTreeNode::SelectBound(const neV3 & com, neV3 & minBound, neV3 & maxBound, s32 sector)
{
	switch (sector)
	{
	case 0:
		minBound.Set(bounds[0][0], bounds[1][0], bounds[2][0]);
		maxBound.Set(com[0], bounds[1][1], com[2]);
		break;
	case 1:
		minBound.Set(com[0], bounds[1][0], bounds[2][0]);
		maxBound.Set(bounds[0][1], bounds[1][1], com[2]);
		break;
	case 2:
		minBound.Set(com[0], bounds[1][0], com[2]);
		maxBound.Set(bounds[0][1], bounds[1][1], bounds[2][1]);
		break;
	case 3:
		minBound.Set(bounds[0][0], bounds[1][0], com[2]);
		maxBound.Set(com[0], bounds[1][1], bounds[2][1]);
		break;
	}
}

void neTreeNode::CountTriangleInSector(neSimpleArray<s32> &tris, neSimpleArray<s32> &sectorTris, const neV3 & com, s32 sector)
{
	neV3 maxBound;

	neV3 minBound;

	SelectBound(com, minBound, maxBound, sector);

	neSimpleArray<s32> triSet;

	triSet.Reserve(2000 , tree->alloc, 2000);

	s32 i;

	for (i = 0; i < tris.GetUsedCount(); i++)
	{
		neTriangle * triangle = &tree->triangles[tris[i]];

		ASSERT(triangle);

		// check triangle collide with this AABB
		if (IntersectAABBTriangle(tree, minBound, maxBound, *triangle))
		{
			s32 * n = triSet.Alloc();

			ASSERT(n);

			*n = tris[i];
		}
	}
	if (triSet.GetUsedCount() > 0)
	{
		sectorTris.Reserve(triSet.GetUsedCount(), tree->alloc);
		
		for (i = 0; i < triSet.GetUsedCount(); i++)
		{
			s32 * j = sectorTris.Alloc();
			ASSERT(j);
			*j = triSet[i];
		}
	}
}

s32 neTreeNode::CountTriangleInSector2(neSimpleArray<s32> &tris, const neV3 & com, s32 sector)
{
	s32 ret = 0;

	neV3 maxBound;

	neV3 minBound;

	SelectBound(com, minBound, maxBound, sector);

	neSimpleArray<s32> triSet;

	s32 i;

	for (i = 0; i < tris.GetUsedCount(); i++)
	{
		neTriangle * triangle = &tree->triangles[tris[i]];

		ASSERT(triangle);

		// check triangle collide with this AABB
		if (IntersectAABBTriangle(tree, minBound, maxBound, *triangle))
		{
			ret++;
		}
	}
	return ret;
}

#pragma optimize( "", off )

void neTreeNode::Build(neSimpleArray<s32> & triIndex, s32 level)
{
	neV3 maxBound, minBound, com;

	FindCenterOfMass(tree, triIndex, &com);

	s32 i;

	if (level > 10)
	{
		MakeLeaf(triIndex);

		return;
	}
	if (triIndex.GetUsedCount() < 4)
	{
		MakeLeaf(triIndex);

		return;
	}

	for (i = 0; i < 4; i++)
	{
		neSimpleArray<s32> sectorTris;

		CountTriangleInSector(triIndex, sectorTris, com, i);

		if (sectorTris.GetUsedCount())
		{
			if (sectorTris.GetUsedCount() == triIndex.GetUsedCount())
			{
				MakeLeaf(triIndex);	

				return;
			}
			else
			{
				FindMinMaxBound(tree, sectorTris, minBound, maxBound);

				f32 yMin = minBound[1];

				f32 yMax = maxBound[1];

				neTreeNode * node = tree->nodes.Alloc();

				ASSERT(node);

				SelectBound(com, minBound, maxBound, i);

				minBound[1] = yMin;

				maxBound[1] = yMax;

				if (this == &tree->root)
					node->Initialise(tree, -1, minBound, maxBound);
				else
					node->Initialise(tree, tree->nodes.GetIndex(this), minBound, maxBound);

				children[i] = tree->nodes.GetIndex(node);

				//ASSERT(children[i] != 159);

				tree->nodes[children[i]].Build(sectorTris, level + 1);
			}
		}
		else
		{
			children[i] = -1;
		}
	}
}
#pragma optimize( "", on )
void neTreeNode::MakeLeaf(neSimpleArray<s32> &tris)
{
	triangleIndices.Reserve(tris.GetUsedCount(), tree->alloc);

	s32 i;

	for (i = 0; i < tris.GetUsedCount(); i++)
	{
		s32 * n = triangleIndices.Alloc();

		ASSERT(n);

		*n = tris[i];
	}

	for (i = 0; i < 4; i++)
	{
		children[i] = -1;
	}
}

bool neTreeNode::IsOverlapped(const neV3 & minBound, const neV3 & maxBound)
{
	const s32 _min = 0;
	const s32 _max = 1;

	if (minBound[1] > bounds[1][_max])
		return false;

	if (maxBound[1] < bounds[1][_min])
		return false;

	if (minBound[0] > bounds[0][_max])
		return false;

	if (maxBound[0] < bounds[0][_min])
		return false;

	if (minBound[2] > bounds[2][_max])
		return false;

	if (maxBound[2] < bounds[2][_min])
		return false;

	return true;
}

void neTreeNode::DrawTriangles()
{
#if 0
	s32 i;

	neV3 red;

	red.Set(1.0f, 0.2f, 0.2f);

	for (i = 0; i < triangleIndices.GetUsedCount(); i++)
	{
		neTriangle_ * tri = &tree->triangles[triangleIndices[i]];

		neV3 points[4];

		points[0] = tree->vertices[tri->indices[0]];
		points[1] = tree->vertices[tri->indices[1]];
		points[2] = tree->vertices[tri->indices[2]];
		points[3] = tree->vertices[tri->indices[0]];

		DrawLine(red, points, 4);
	}
#endif
}

void neTreeNode::DrawBounds()
{
#if 0
	neV3 points[5];

	neV3 white;

	white.Set(1.0f, 1.0f, 1.0f);

	points[0].Set(bounds[0][0], bounds[1][0], bounds[2][0]);
	points[1].Set(bounds[0][0], bounds[1][0], bounds[2][1]);
	points[2].Set(bounds[0][1], bounds[1][0], bounds[2][1]);
	points[3].Set(bounds[0][1], bounds[1][0], bounds[2][0]);
	points[4].Set(bounds[0][0], bounds[1][0], bounds[2][0]);
	DrawLine(white, points, 5);
	
	points[0].Set(bounds[0][0], bounds[1][1], bounds[2][0]);
	points[1].Set(bounds[0][0], bounds[1][1], bounds[2][1]);
	points[2].Set(bounds[0][1], bounds[1][1], bounds[2][1]);
	points[3].Set(bounds[0][1], bounds[1][1], bounds[2][0]);
	points[4].Set(bounds[0][0], bounds[1][1], bounds[2][0]);
	DrawLine(white, points, 5);
	
	
	points[0].Set(bounds[0][0], bounds[1][0], bounds[2][0]);
	points[1].Set(bounds[0][0], bounds[1][1], bounds[2][0]);
	DrawLine(white, points, 2);
	points[0].Set(bounds[0][0], bounds[1][0], bounds[2][1]);
	points[1].Set(bounds[0][0], bounds[1][1], bounds[2][1]);
	DrawLine(white, points, 2);
	points[0].Set(bounds[0][1], bounds[1][0], bounds[2][0]);
	points[1].Set(bounds[0][1], bounds[1][1], bounds[2][0]);
	DrawLine(white, points, 2);
	points[0].Set(bounds[0][1], bounds[1][0], bounds[2][1]);
	points[1].Set(bounds[0][1], bounds[1][1], bounds[2][1]);
	DrawLine(white, points, 2);
#endif
}

void neTreeNode::GetCandidateNodes(neSimpleArray<neTreeNode*> & nodes, const neV3 & minBound, const neV3 & maxBound, s32 level)
{
	if (!IsOverlapped(minBound, maxBound))
	
		return;
/*	
	if (level >= 3)
	{
		DrawBounds();
		//DrawTriangles();
	}
*/	if (this->triangleIndices.GetUsedCount() > 0)
	{
		neTreeNode ** n = nodes.Alloc();

		ASSERT(n);

		*n = this;
/*
		if (level >= 4)
		{
			DrawBounds();
			DrawTriangles();
		}
*/
		return;
	}

	s32 i;

	for (i = 0; i < 4; i++)
	{
		if (children[i] != -1)
		{
			neTreeNode * c = &tree->nodes[children[i]];

			c->GetCandidateNodes(nodes, minBound, maxBound, level + 1);
		}
	}
}

/****************************************************************************
*
*	neTriangleTree::neTriangleTree()
*
****************************************************************************/ 

neTriangleTree::neTriangleTree()
{
	alloc = NULL;

	vertexCount = 0;

	vertices = NULL;

	sim = NULL;
}
#pragma optimize( "", off )
void TreeBuild(neTriangleTree * tree, s32 nodeIndex, neSimpleArray<s32> & triIndex, s32 level)
{
//	if (nodeIndex == 769)
//		ASSERT(0);

	neV3 maxBound, minBound;

	neV3 com;
	
	FindCenterOfMass(tree, triIndex, &com);

	s32 i;

	if (level > 4)
	{
		tree->GetNode(nodeIndex).MakeLeaf(triIndex);

		return;
	}
	if (triIndex.GetUsedCount() < 10)
	{
		tree->GetNode(nodeIndex).MakeLeaf(triIndex);

		return;
	}

	for (i = 0; i < 4; i++)
	{
		neSimpleArray<s32> sectorTris;

		tree->GetNode(nodeIndex).CountTriangleInSector(triIndex, sectorTris, com, i);

		if (sectorTris.GetUsedCount())
		{
			if (sectorTris.GetUsedCount() == triIndex.GetUsedCount())
			{
				tree->GetNode(nodeIndex).MakeLeaf(triIndex);

				return;
			}
			else
			{
				FindMinMaxBound(tree, sectorTris, minBound, maxBound);

				f32 yMin = minBound[1];

				f32 yMax = maxBound[1];

				neTreeNode * node = tree->nodes.Alloc();

				ASSERT(node);

				tree->GetNode(nodeIndex).SelectBound(com, minBound, maxBound, i);

				minBound[1] = yMin;

				maxBound[1] = yMax;

				node->Initialise(tree, nodeIndex, minBound, maxBound);

				tree->GetNode(nodeIndex).children[i] = tree->nodes.GetIndex(node);
			
				TreeBuild(tree, tree->GetNode(nodeIndex).children[i], sectorTris, level + 1);
			}
		}
		else
		{
			tree->GetNode(nodeIndex).children[i] = -1;
		}
	}
}
#pragma optimize( "", on )
neTreeNode & neTriangleTree::GetNode(s32 nodeIndex)
{
	if (nodeIndex == -1)
		return root;
	else
	{
		ASSERT(nodeIndex >= 0 && nodeIndex < nodes.GetUsedCount());
		
		return nodes[nodeIndex];
	}
}

/****************************************************************************
*
*	neTriangleTree::BuildTree
*
****************************************************************************/ 

neBool neTriangleTree::BuildTree(neV3 * _vertices, s32 _vertexCount, neTriangle * tris, s32 triCount, neAllocatorAbstract * _alloc)
{
	if (!_vertices || _vertexCount <= 0)
		return false;

	if (!tris || triCount <= 0)
		return false;

	if (_alloc)
		alloc = _alloc;
	else
		alloc = &allocDef;

	if (triangles.GetTotalSize() > 0)
	{
		FreeTree();
	}
	triangles.Reserve(triCount, alloc);

	vertices = (neV3*)alloc->Alloc(sizeof(neV3) * _vertexCount);

	vertexCount = _vertexCount;
	
	s32 i;

	for (i = 0; i < vertexCount; i++)
	{
		vertices[i] = _vertices[i];
	}


	for (i = 0; i < triCount; i++)
	{
		neTriangle * t = triangles.Alloc();

		ASSERT(t);

		*t = tris[i];
	}

	nodes.Reserve(sim->sizeInfo.terrainNodesStartCount, alloc, sim->sizeInfo.terrainNodesGrowByCount);

	neSimpleArray<s32> triIndex;

	triIndex.Reserve(triCount, alloc);

	for (i = 0; i < triCount; i++)
	{
		s32 * j = triIndex.Alloc();

		*j = i;
	}

	neV3 minBound, maxBound;

	FindMinMaxBound(this, triIndex, minBound, maxBound);

	root.Initialise(this, -2, minBound, maxBound);

	//root.Build(triIndex, 0);

	TreeBuild(this, -1, triIndex, 0);

	s32 nodeUsed = nodes.GetUsedCount();

	triIndex.Free();

	return true;
}

void neTriangleTree::FreeTree()
{
	triangles.Free();

	if (vertices)
	{
		alloc->Free((neByte*)vertices);

		vertices = NULL;
	}	

	nodes.Free();

	neV3 minBound, maxBound;
	minBound.SetZero();
	maxBound.SetZero();
	root.Initialise(NULL, 0, minBound, maxBound);

	vertexCount = 0;
}

/****************************************************************************
*
*	neTriangleTree::~neTriangleTree
*
****************************************************************************/ 

neTriangleTree::~neTriangleTree()
{
	FreeTree();
}