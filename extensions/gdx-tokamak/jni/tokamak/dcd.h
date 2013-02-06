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

#ifndef DCD_H
#define DCD_H

struct DCDFace
{
	neByte *neighbourFaces;
	neByte *neighbourVerts;
	neByte *neighbourEdges;
};

struct DCDVert
{
	neByte * neighbourEdges;
};

struct DCDEdge
{
	neByte f1;
	neByte f2;
	neByte v1;
	neByte v2;
};

struct DCDMesh
{
	s32 numFaces;
	s32 numVerts;
	s32 numEdges;
	neByte pad0;

	s32 numNeighbour;

	neV3 * normals;
	neV3 * vertices;

	DCDFace * faces;
	DCDVert * verts;
	DCDEdge * edges;

	void SetConvex(const TConvex & convex, neV3 * vertArray);
	neV3 GetVertOnFace(s32 faceIndex, s32 vertIndex);
	neV3 GetVert(s32 vertIndex);
	neV3  GetNormal(s32 faceIndex);
//	neByte FaceGetNumFaceNeighbour(s32 faceIndex);
	neByte FaceGetFaceNeighbour(s32 faceIndex, s32 neighbourIndex);
	neByte FaceGetEdgeNeighbour(s32 faceIndex, s32 neighbourIndex);
//	neByte VertGetNumEdgeNeighbour(s32 vertIndex);
	neByte VertGetEdgeNeighbour(s32 vertIndex, s32 neighbourIndex);
	neByte EdgeGetVert1(s32 edgeIndex);
	neByte EdgeGetVert2(s32 edgeIndex);
};

bool TestDCD(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector);

void Convex2TerrainTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB);

#endif