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
#include "scenery.h"
#include "stack.h"
#include "simulator.h"
#include "message.h"
#include "dcd.h"

const s32 BOX_NUM_FACES = 6;

const s32 BOX_NUM_VERTS = 8;

const s32 BOX_NUM_EDGES = 12;

const s32 TRI_NUM_FACES = 2;

const s32 TRI_NUM_VERTS = 3;

const s32 TRI_NUM_EDGES = 3;


s32 _num_edge_test;

s32 _num_face_test;

static neByte _boxNeighbourFaces[][4] = {{2,3,4,5},{2,3,4,5},{0,1,4,5},{0,1,4,5},{0,1,2,3},{0,1,2,3}};
static neByte _boxNeighbourVerts[][4] = {{2,3,6,7},{0,1,4,5},{4,5,6,7},{0,1,2,3},{1,3,5,7},{0,2,4,6}};
static neByte _boxNeighbourEdges[][4] = {{0,1,2,3},{4,5,6,7},{0,4,8,9},{1,5,10,11},{2,8,6,10},{3,7,9,11}};
static neByte _boxVertNeighbourEdges[][4] = {{5,7,11,0xff},{5,6,10,0xff},{1,3,11,0xff},{1,2,10,0xff},{4,7,9,0xff},{4,6,8,0xff},{0,3,9,0xff},{0,2,8,0xff}};
static neV3 _boxNormals[BOX_NUM_FACES] = {{0,1,0,0},{0,-1,0,0},{1,0,0,0},{-1,0,0,0},{0,0,1,0},{0,0,-1,0}};
static neV3 _boxVertexPos0[BOX_NUM_VERTS] = {{-1,-1,-1,0},{-1,-1,1,0},{-1,1,-1,0},{-1,1,1,0},{1,-1,-1,0},{1,-1,1,0},{1,1,-1,0},{1,1,1,0}};
static neV3 _boxVertexPosP[BOX_NUM_VERTS];
static neV3 _boxVertexPosQ[BOX_NUM_VERTS];
static neBool _visited[100];

DCDFace BoxFaces[BOX_NUM_FACES] =
{
	{_boxNeighbourFaces[0],_boxNeighbourVerts[0],_boxNeighbourEdges[0]}, //0
	{_boxNeighbourFaces[1],_boxNeighbourVerts[1],_boxNeighbourEdges[1]}, //1
	{_boxNeighbourFaces[2],_boxNeighbourVerts[2],_boxNeighbourEdges[2]}, //2
	{_boxNeighbourFaces[3],_boxNeighbourVerts[3],_boxNeighbourEdges[3]}, //3
	{_boxNeighbourFaces[4],_boxNeighbourVerts[4],_boxNeighbourEdges[4]}, //4
	{_boxNeighbourFaces[5],_boxNeighbourVerts[5],_boxNeighbourEdges[5]}, //5
};

DCDVert BoxVertices[BOX_NUM_VERTS] =
{
	{_boxVertNeighbourEdges[0],}, //0
	{_boxVertNeighbourEdges[1],}, //1
	{_boxVertNeighbourEdges[2],}, //2
	{_boxVertNeighbourEdges[3],}, //3
	{_boxVertNeighbourEdges[4],}, //4
	{_boxVertNeighbourEdges[5],}, //5
	{_boxVertNeighbourEdges[6],}, //6
	{_boxVertNeighbourEdges[7],}, //7
};

DCDEdge BoxEdges[BOX_NUM_EDGES] = 
{
	{0,2,6,7}, //0
	{0,3,2,3}, //1
	{0,4,3,7}, //2
	{0,5,2,6}, //3
	{1,2,4,5}, //4
	{1,3,0,1}, //5
	{1,4,1,5}, //6
	{1,5,0,4}, //7
	{2,4,5,7}, //8
	{2,5,4,6}, //9
	{3,4,1,3}, //10
	{3,5,0,2}, //11
};

static neByte _triNeigbhourFaces[TRI_NUM_FACES][1] = {{0}, {1}};
static neByte _triNeighbourVerts[TRI_NUM_FACES][3] = {{0,1,2},{0,1,2}};
static neByte _triNeighbourEdges[TRI_NUM_FACES][3] = {{0,1,2},{0,1,2}};
static neByte _triVertNeighbourEdges[TRI_NUM_VERTS][3] = {{0,2, 0xff},{0, 1, 0xff},{1, 2, 0xff}};

static neV3 _triNormals[TRI_NUM_FACES];
static neV3 _triVertexPos[3];

DCDFace TriFaces[TRI_NUM_FACES] = 
{
	{_triNeigbhourFaces[0],_triNeighbourVerts[0],_triNeighbourEdges[0]},
	{_triNeigbhourFaces[1],_triNeighbourVerts[1],_triNeighbourEdges[1]},
};

DCDVert TriVertices[TRI_NUM_VERTS] = 
{
	{_triVertNeighbourEdges[0]},
	{_triVertNeighbourEdges[1]},
	{_triVertNeighbourEdges[2]},
};

DCDEdge TriEdges[TRI_NUM_EDGES] =
{
	{0,1,0,1},
	{0,1,1,2},
	{0,1,2,0},
};

neV3 TriEdgeDir[TRI_NUM_EDGES];

void DCDMesh::SetConvex(const TConvex & convex, neV3 * vertArray)
{
	if (convex.type == TConvex::BOX)
	{
		numFaces = BOX_NUM_FACES;
		numVerts = BOX_NUM_VERTS;
		//numEdges = BOX_NUM_EDGES;
		normals = _boxNormals;
		faces = BoxFaces;
		verts = BoxVertices;
		edges = BoxEdges;
		if (vertArray)
			vertices = vertArray;

		numNeighbour = 4;
	}
	else if (convex.type == TConvex::CONVEXDCD)
	{
		numFaces = *(int*)convex.as.convexDCD.convexData;
		numVerts = *((int*)convex.as.convexDCD.convexData+1);
		//numEdges = *((int*)convex.as.convexDCD.convexData+2);

		f32 * np = (f32 *)(convex.as.convexDCD.convexData + 4 * sizeof(int));
		normals = (neV3*)np;

		vertices = (neV3*)(np + 4 * numFaces);
		faces = (DCDFace*)(vertices + numVerts);
		verts = (DCDVert*)((neByte*)faces + sizeof(DCDFace) * numFaces);
		edges = (DCDEdge*)((neByte*)verts + sizeof(DCDVert) * numVerts);

		numNeighbour = 3;
	}
	else if (convex.type == TConvex::TRIANGLE)
	{
		numFaces = TRI_NUM_FACES;
		numVerts = TRI_NUM_VERTS;
		normals = _triNormals;
		vertices = _triVertexPos;
		faces = TriFaces;
		verts = TriVertices;
		edges = TriEdges;
		numNeighbour = 3;
	}
}
neV3 DCDMesh::GetVertOnFace(s32 faceIndex, s32 vertIndex)
{
	return vertices[faces[faceIndex].neighbourVerts[vertIndex]];
}
neV3 DCDMesh::GetVert(s32 vertIndex)
{
	return vertices[vertIndex];
}
neV3  DCDMesh::GetNormal(s32 faceIndex)
{
	return normals[faceIndex];
}
/*neByte DCDMesh::FaceGetNumFaceNeighbour(s32 faceIndex)
{
	return faces[faceIndex].numberFaceNeighbour;
}
*/neByte DCDMesh::FaceGetFaceNeighbour(s32 faceIndex, s32 neighbourIndex)
{
	return faces[faceIndex].neighbourFaces[neighbourIndex];
}
neByte DCDMesh::FaceGetEdgeNeighbour(s32 faceIndex, s32 neighbourIndex)
{
	return faces[faceIndex].neighbourEdges[neighbourIndex];
}
/*neByte DCDMesh::VertGetNumEdgeNeighbour(s32 vertIndex)
{
	return verts[vertIndex].numberEdgeNeighbour;
}
*/neByte DCDMesh::VertGetEdgeNeighbour(s32 vertIndex, s32 neighbourIndex)
{
	return verts[vertIndex].neighbourEdges[neighbourIndex];
}
neByte DCDMesh::EdgeGetVert1(s32 edgeIndex)
{
	return edges[edgeIndex].v1;
}
neByte DCDMesh::EdgeGetVert2(s32 edgeIndex)
{
	return edges[edgeIndex].v2;
}

const s32 NUM_STACK_SIZE = 200;

bool CalcContactEE(const neV3 & edgeA0, 
					const neV3 & edgeA1, 
					const neV3 & edgeB0, 
					const neV3 & edgeB1, neV3 & contactA, neV3 & contactB);

struct EdgeStackRecord
{
	s32 edgeP;
	s32 edgeQ;
};

class EdgeStack
{
public:
	void Init()
	{
		tos = 0;
	}
	void Push(s32 edgeP, s32 edgeQ)
	{
		ASSERT(tos < NUM_STACK_SIZE);

		for (s32 i = 0; i < tos; i++)
		{
			if ((eStack[i].edgeP == edgeP && eStack[i].edgeQ == edgeQ) ||
				(eStack[i].edgeP == edgeQ && eStack[i].edgeQ == edgeP))
			return;
		}
		eStack[tos].edgeP = edgeP;
		eStack[tos].edgeQ = edgeQ;
		tos++;
	}

	bool Pop(s32 & edgeP, s32 & edgeQ)
	{
		ASSERT(tos > 0);

		tos--;
		edgeP = eStack[tos].edgeP;
		edgeQ = eStack[tos].edgeQ;
		return true;
	}
	neBool IsEmpty()
	{
		return tos == 0;
	}
private:
	s32 tos;
	EdgeStackRecord eStack[NUM_STACK_SIZE];
};

EdgeStack gEdgeStack;

neV3 BigC;

f32 BigCLength;

class Face
{
public:
	// face is defined as normal.Dot(p) = k
	neV3 normal;
	f32 k;
};

class DCDObj
{
public:
	//TConvex * convex;
	neBool isBox;

	DCDMesh mesh;

	neT3 * trans;

	Face GetFace(s32 faceIndex)
	{
		Face face0;

		face0.normal = trans->rot * mesh.normals[faceIndex];

		neV3 tmp1 = (*trans) * mesh.GetVertOnFace(faceIndex, 0);

		//face0.k = face0.normal.Dot(trans->pos) + mesh.normals[faceIndex].v[3] * -1.0f;
		
		face0.k = face0.normal.Dot(tmp1);

		return face0;
	}
	neV3 GetVertWorld(s32 vertIndex)
	{
		neV3 vert = (*trans) * mesh.vertices[vertIndex];

		return vert;
	}
	neV3 GetNegVertWorld(s32 vertIndex)
	{
		neV3 vert = GetVertWorld(vertIndex) * -1.0f;

		return vert;
	}
	NEINLINE neV3 GetWorldNormalByEdge1(s32 edgeIndex)
	{
		neV3 ret; ret = trans->rot * mesh.normals[mesh.edges[edgeIndex].f1];

		return ret;
	}
	NEINLINE neV3 GetWorldNormalByEdge2(s32 edgeIndex)
	{
		neV3 ret; ret = trans->rot * mesh.normals[mesh.edges[edgeIndex].f2];

		return ret;
	}
	NEINLINE s32 GetSupportPoint(const neV3 & norm)
	{
		if (isBox)
			return GetSupportPointBox(norm);

		else
			return GetSupportPointMesh(norm);

		return 0;
	}
	NEINLINE void GetWorldEdgeVerts(s32 edgeIndex, neV3 & av, neV3 & bv)
	{
		neV3 tmp;
		
		tmp = mesh.vertices[mesh.edges[edgeIndex].v1];

		av = (*trans) * tmp;

		tmp = mesh.vertices[mesh.edges[edgeIndex].v2];

		bv = (*trans) * tmp;
	}
	neV3 FaceGetWorldNormal(s32 faceIndex)
	{
		return trans->rot * mesh.GetNormal(faceIndex);
	}
private:
	s32 GetSupportPointBox(const neV3 & norm)
	{
		neV3 localNorm = trans->rot.TransposeMulV3(norm);

		localNorm *= -1.0f;

		s32 ret = 0;

		if (localNorm[0] >= 0.0f)
		{
			if (localNorm[1] >= 0.0f)
			{
				if (localNorm[2] >= 0.0f)
					ret = 7;
				else
					ret = 6;
			}
			else
			{
				if (localNorm[2] >= 0.0f)
					ret = 5;
				else
					ret = 4;
			}
		}
		else
		{
			if (localNorm[1] >= 0.0f)
			{
				if (localNorm[2] >= 0.0f)
					ret = 3;
				else
					ret = 2;
			}
			else
			{
				if (localNorm[2] >= 0.0f)
					ret = 1;
				else
					ret = 0;
			}
		}
		return ret;
	}
	s32 GetSupportPointMesh(const neV3 & norm)
	{
		neV3 localNorm = trans->rot.TransposeMulV3(norm);

		localNorm *= -1.0f;

		s32 ret = 0;

		f32 maxd = -1.0e6f;

		neByte neighbourEdge;

		neBool moving;

		do {
			moving = false;
			
			s32 i = 0;

			//while (i < mesh.verts[ret].numberEdgeNeighbour)
			do 
			{
				s32 currentVert;

				neighbourEdge = mesh.verts[ret].neighbourEdges[i];

				if (neighbourEdge == 0xff)
					break;

				if (mesh.edges[neighbourEdge].v1 == ret)
					currentVert = mesh.edges[neighbourEdge].v2;
				else
					currentVert = mesh.edges[neighbourEdge].v1;

				//if (currentVert > 10)
				//	ASSERT(0);
				
				f32 dot = mesh.vertices[currentVert].Dot(localNorm);

				if (dot > maxd)
				{
					maxd = dot;

					ret = currentVert;

					moving = 1;
					
					break;
				}

				i++;
			} while (true);
		} while(moving);

		return ret;
/*
		for (s32 i = 0; i < mesh.numVerts; i++)
		{
			f32 dot = mesh.vertices[i].Dot(localNorm);

			if (dot > maxd)
			{
				maxd = dot;

				ret = i;
			}
		}
		return ret;
*/
	}

};

f32 funcD(const Face & face)
{
	f32 k = face.k;
	
	neV3 N; 
	
	N = face.normal;

	if (face.k < 0.0f)
	{
		k = -face.k;
		N *= -1.0f;
	}

	f32 den = k - N.Dot(BigC);

	den = BigCLength * den;

	ASSERT(!neIsConsiderZero(den));

	f32 ret = -k / den;

	return ret;
}

f32 SignedDistance(const Face & faceP, const neV3 & vertQ , Face & faceM)
{
	faceM = faceP;

	f32 dot = faceP.normal.Dot(vertQ);

	faceM.k += (dot);

	return funcD(faceM);
}

class SearchResult
{
public:
	enum Type
	{
		FACE,
		VERTEX,
		EDGE,
	} ;
	
	SearchResult(const TConvex & convexA, neT3 * transA, const TConvex & convexB, neT3 * transB, neV3 * vertArrayA, neV3 * vertArrayB)
	{
		objA.mesh.SetConvex(convexA, vertArrayA);

		objA.isBox = (convexA.type == TConvex::BOX);

		objA.trans = transA;

		objB.mesh.SetConvex(convexB, vertArrayB);

		objB.isBox = (convexB.type == TConvex::BOX);

		objB.trans = transB;

		dMax = -1.0e6f;
	}
	neBool TestFace(s32 face0Index, neBool & assigned)
	{
		assigned = false;

		_visited[face0Index] = true;

		Face face0 = objA.GetFace(face0Index);

		neByte _indexB = objB.GetSupportPoint(face0.normal);

		neV3 vertB;

		vertB = objB.GetNegVertWorld(_indexB);

		Face newFace;

		f32 d = SignedDistance(face0, vertB, newFace);

		if (d >= 0.0f)
			return false;

		if (d <= dMax)
			return true;

		dMax = d;

		typeA = SearchResult::FACE;

		typeB = SearchResult::VERTEX;

		indexA = face0Index;

		indexB = _indexB;

		face = newFace;

		assigned = true;

		_num_face_test++;

		return true;
	}
	neBool SearchFV(s32 initialFace, neBool & assigned);

	neBool SearchEE(s32 flag /*0 or 1*/, s32 aIndex, s32 bIndex, neBool & assigned);

	neBool SearchEETri(s32 flag /*0 or 1*/, s32 aIndex, s32 bIndex, neBool & assigned);

	DCDObj objA;

	Type typeA;

	s32 indexA;

	DCDObj objB;

	Type typeB;

	s32 indexB;

	Face face;

	f32 dMax;
};

neBool SearchResult::SearchFV(s32 initialFace, neBool & assigned)
{
	for (s32 i = 0; i < objA.mesh.numFaces; i++)
	{
		_visited[i] = false;

		if (objA.isBox)
		{
			objA.mesh.normals[i].v[3] = objA.mesh.vertices[objA.mesh.faces[i].neighbourVerts[0]].Dot(objA.mesh.normals[i]) * -1.0f;
		}
	}
	
	if (!TestFace(initialFace, assigned))
		return false;

	//ASSERT(assigned);

	neBool found = true;

	s32 currentFace = initialFace;

	while (found)
	{
		found = false;

		for (s32 ii = 0; ii < objA.mesh.numNeighbour; ii++)
		{
			s32 i = objA.mesh.FaceGetFaceNeighbour(currentFace, ii);

			if (_visited[i])
				continue;

			neBool _assigned;

			if (!TestFace(i, _assigned))
				return false;

			if (_assigned)
				found = true;
		}
		if (found)
		{
			currentFace = indexA;
		}
	}
	return true;
}

f32 Determinant(const neV3 & a, const neV3 & b, const neV3 & c)
{
	f32 t1 = a[0] * b[1] * c[2];

	f32 t2 = a[1] * b[0] * c[2];

	f32 t3 = a[0] * b[2] * c[1];

	f32 t4 = a[2] * b[1] * c[0];

	f32 t5 = a[1] * b[2] * c[0];

	f32 t6 = a[2] * b[0] * c[1];

	f32 ret = t1 - t2 - t3 - t4 + t5 + t6;
	
	return ret;
}

neBool SearchResult::SearchEE(s32 flag, s32 aIndex, s32 bIndex, neBool & assigned)
{
	assigned = false;

	gEdgeStack.Init();

	neByte edgeIndex;

	if (flag == 0) //fv
	{
		for (s32 i = 0; i < objA.mesh.numNeighbour; i++)
		{
			int j = 0;

			while ((edgeIndex = objB.mesh.VertGetEdgeNeighbour(bIndex, j)) != 0xff)
			{
				gEdgeStack.Push(objA.mesh.FaceGetEdgeNeighbour(aIndex, i),
								objB.mesh.VertGetEdgeNeighbour(bIndex, j));

				j++;
			}
		}
	}
	else //vf
	{
		s32 i = 0;

		while ((edgeIndex = objA.mesh.VertGetEdgeNeighbour(aIndex, i)) != 0xff)
		{
			for (s32 j = 0; j < objB.mesh.numNeighbour; j++)
			{
				gEdgeStack.Push(objA.mesh.VertGetEdgeNeighbour(aIndex, i),
								objB.mesh.FaceGetEdgeNeighbour(bIndex, j));
			}			
			i++;
		}
	}
	while (!gEdgeStack.IsEmpty())
	{
		_num_edge_test++;

		s32 edgeP, edgeQ;

		gEdgeStack.Pop(edgeP, edgeQ);

		// does the edge form a face
		neV3 a = objA.GetWorldNormalByEdge1(edgeP);

		neV3 b = objA.GetWorldNormalByEdge2(edgeP);

		neV3 c = objB.GetWorldNormalByEdge1(edgeQ) * -1.0f;

		neV3 d = objB.GetWorldNormalByEdge2(edgeQ) * -1.0f;

		f32 cba = Determinant(c,b,a);

		f32 dba = Determinant(d,b,a);

		f32 prod0 = cba * dba;

		if (prod0 >= 0.0f/*-1.0e-6f*/)
		{
			continue;
		}

		f32 adc = Determinant(a,d,c);

		f32 bdc = Determinant(b,d,c);

		f32 prod1 = adc * bdc;

		if (prod1 >= 0.0f/*-1.0e-6f*/)
		{
			continue;
		}
		f32 prod2 = cba * bdc;

		if (prod2 <= 0.0f/*1.0e-6f*/)
		{
			continue;
		}
		neV3 ai, bi;
		neV3 naj, nbj;

		objA.GetWorldEdgeVerts(edgeP, ai, bi);

		objB.GetWorldEdgeVerts(edgeQ, naj, nbj);

		naj *= -1.0f; nbj *= -1.0f;

		neV3 ainaj = ai + naj;
		neV3 ainbj = ai + nbj;
		neV3 binaj = bi + naj;
		//neV3 binbj = bi + nbj;

		neV3 diff1 = ainaj - ainbj;
		neV3 diff2 = ainaj - binaj ;

		Face testFace;

		testFace.normal = diff1.Cross(diff2);

		f32 len = testFace.normal.Length();

		if (neIsConsiderZero(len))
		{
			continue;
		}
		testFace.normal *= (1.0f / len);

		testFace.k = testFace.normal.Dot(ainaj);

		f32 testD = funcD(testFace);

		if (testD >= 0)
			return false;


		if (testD <= dMax)
			continue;

		assigned = true;
		dMax = testD;
		face = testFace;
		indexA = edgeP;
		indexB = edgeQ;
		typeA = SearchResult::EDGE; 
		typeB = SearchResult::EDGE;

		// push
		s32 i, j;

		s32 vindex;

		vindex = objB.mesh.EdgeGetVert1(edgeQ);

		i = 0;

		while ((j = objB.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeQ)
				gEdgeStack.Push(edgeP, j);

			i++;
		}

		vindex = objB.mesh.EdgeGetVert2(edgeQ);

		i = 0;

		while ((j = objB.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeQ)
				gEdgeStack.Push(edgeP, j);

			i++;
		}

		vindex = objA.mesh.EdgeGetVert1(edgeP);

		i = 0;

		while((j = objA.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeP)
				gEdgeStack.Push(j, edgeQ);

			i++;
		}

		vindex = objA.mesh.EdgeGetVert2(edgeP);

		//for (i = 0; i < objA.mesh.VertGetNumEdgeNeighbour(vindex); i++)
		i = 0;

		while ((j = objA.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeP)
				gEdgeStack.Push(j, edgeQ);

			i++;
		}
/*
		if (testD <= dMax)
			continue;

		assigned = true;
		dMax = testD;
		face = testFace;
		indexA = edgeP;
		indexB = edgeQ;
		typeA = SearchResult::EDGE; 
		typeB = SearchResult::EDGE;
*/	}
	return true;
}

neBool SearchResult::SearchEETri(s32 flag, s32 aIndex, s32 bIndex, neBool & assigned)
{
	assigned = false;

	gEdgeStack.Init();

	neByte edgeIndex;

	if (flag == 0) //fv
	{
		// face of convex A
		// vertex of triangle B
		for (s32 i = 0; i < objA.mesh.numNeighbour; i++) // for each edge neighbour of Face aIndex
		{
			int j = 0;

			while ((edgeIndex = objB.mesh.VertGetEdgeNeighbour(bIndex, j)) != 0xff)
			{
				gEdgeStack.Push(objA.mesh.FaceGetEdgeNeighbour(aIndex, i),
								objB.mesh.VertGetEdgeNeighbour(bIndex, j));
				
				j++;
			}
		}
	}
	else //vf
	{
		//vertex of convex A
		//face of triangle B
		s32 i = 0;

		//for each edge neighbour incident to Vertex aIndex
		
		while ((edgeIndex = objA.mesh.VertGetEdgeNeighbour(aIndex, i)) != 0xff)
		{
			for (s32 j = 0; j < objB.mesh.numNeighbour; j++)
			{
				gEdgeStack.Push(objA.mesh.VertGetEdgeNeighbour(aIndex, i),
								objB.mesh.FaceGetEdgeNeighbour(bIndex, j));
			}			
			i++;
		}
	}
	while (!gEdgeStack.IsEmpty())
	{
		_num_edge_test++;

		s32 edgeP, edgeQ;

		gEdgeStack.Pop(edgeP, edgeQ);

		// does the edge form a face
		neV3 a = objA.GetWorldNormalByEdge1(edgeP);

		neV3 b = objA.GetWorldNormalByEdge2(edgeP);

		neV3 c = objB.GetWorldNormalByEdge1(edgeQ) * -1.0f;

		neV3 d = objB.GetWorldNormalByEdge2(edgeQ) * -1.0f;

		c += (TriEdgeDir[edgeQ] * 0.01f);

		d += (TriEdgeDir[edgeQ] * 0.01f);

		c.Normalize();

		d.Normalize();

		f32 cba = Determinant(c,b,a);

		f32 dba = Determinant(d,b,a);

		f32 prod0 = cba * dba;

		if (prod0 >= -1.0e-6f)
		{
			continue;
		}

		f32 adc = Determinant(a,d,c);

		f32 bdc = Determinant(b,d,c);

		f32 prod1 = adc * bdc;

		if (prod1 >= -1.0e-6f)
		{
			continue;
		}
		f32 prod2 = cba * bdc;

		if (prod2 <= 1.0e-6f)
		{
			continue;
		}


		neV3 ai, bi;
		neV3 naj, nbj;

		objA.GetWorldEdgeVerts(edgeP, ai, bi);

		objB.GetWorldEdgeVerts(edgeQ, naj, nbj);
		
		naj *= -1.0f; nbj *= -1.0f;

		neV3 ainaj = ai + naj;
		neV3 ainbj = ai + nbj;
		neV3 binaj = bi + naj;
		//neV3 binbj = bi + nbj;

		neV3 diff1 = ainaj - ainbj;
		neV3 diff2 = ainaj - binaj ;

		Face testFace;

		testFace.normal = diff1.Cross(diff2);

		f32 len = testFace.normal.Length();

		if (neIsConsiderZero(len))
		{
			continue;
		}
		testFace.normal *= (1.0f / len);

		testFace.k = testFace.normal.Dot(ainaj);

		f32 testD = funcD(testFace);

		if (testD >= 0)
			return false;

		if (testD <= dMax)
			continue;

		assigned = true;
		dMax = testD;
		face = testFace;
		indexA = edgeP;
		indexB = edgeQ;
		typeA = SearchResult::EDGE; 
		typeB = SearchResult::EDGE;

		// push
		s32 i, j;

		s32 vindex;

		vindex = objB.mesh.EdgeGetVert1(edgeQ);

		i = 0;

		while ((j = objB.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeQ)
				gEdgeStack.Push(edgeP, j);

			i++;
		}

		vindex = objB.mesh.EdgeGetVert2(edgeQ);

		i = 0;

		while ((j = objB.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeQ)
				gEdgeStack.Push(edgeP, j);

			i++;
		}

		vindex = objA.mesh.EdgeGetVert1(edgeP);

		i = 0;
		
		while((j = objA.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeP)
				gEdgeStack.Push(j, edgeQ);

			i++;
		}

		vindex = objA.mesh.EdgeGetVert2(edgeP);

		//for (i = 0; i < objA.mesh.VertGetNumEdgeNeighbour(vindex); i++)
		i = 0;

		while ((j = objA.mesh.VertGetEdgeNeighbour(vindex, i)) != 0xff)
		{
			if (j != edgeP)
				gEdgeStack.Push(j, edgeQ);

			i++;
		}
	}
	return true;
}

bool TestDCD(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector)
{
	_num_edge_test = 0;

	_num_face_test = 0;

	result.penetrate = false;
	
	neV3 aPoint = transA.pos;

	neV3 av; av.Set(0.1f);

	aPoint += av;

	neV3 bPoint = transB.pos;

	av.Set(0.2f);

	bPoint += av;

	BigC = aPoint - bPoint;

	BigCLength = BigC.Length();

	neV3 * aVertArray, * bVertArray;

	if (convexA.type == TConvex::BOX)
	{
		for (s32 i = 0; i < BOX_NUM_VERTS; i++)
		{
			_boxVertexPosP[i] = _boxVertexPos0[i] * convexA.as.box.boxSize;
		}
		aVertArray = _boxVertexPosP;
	}
	if (convexB.type == TConvex::BOX)
	{
		for (s32 i = 0; i < BOX_NUM_VERTS; i++)
		{
			_boxVertexPosQ[i] = _boxVertexPos0[i] * convexB.as.box.boxSize;
		}
		bVertArray = _boxVertexPosQ;
	}

	SearchResult srFV(convexA, &transA, convexB, &transB, aVertArray, bVertArray);

	neBool showDebug = 0;
	
	neBool showDebug2  = (srFV.objA.mesh.numVerts > 8 && srFV.objB.mesh.numVerts > 8);
	
	neBool assigned;

	neBool res = srFV.SearchFV(0, assigned);

	if (!res)
	{
		if (showDebug)
		{TOKAMAK_OUTPUT_2("%d, %d \n", _num_face_test, _num_edge_test);}

		return false;
	}
	SearchResult srVF(convexB, &transB, convexA, &transA, bVertArray, aVertArray);

	srVF.dMax = srFV.dMax;

	BigC *= -1.0f;

	s32 whichF = srFV.objB.mesh.edges[srFV.objB.mesh.verts[srFV.indexB].neighbourEdges[0]].f1;

	res = srVF.SearchFV(whichF, assigned);
	
	if (!res)
	{
		if (showDebug)
			{TOKAMAK_OUTPUT_2("%d, %d \n", _num_face_test, _num_edge_test);}

		return false;
	}

	bool need2Swap = false;

	SearchResult srEE(convexA, &transA, convexB, &transB, aVertArray, bVertArray);

	s32 eeflag = 0;
	
	s32 pindex, qindex;

	if (srVF.dMax > srFV.dMax)
	{
		need2Swap = true;

		srEE.dMax = srVF.dMax;

		eeflag = 1;

		pindex = srVF.indexB;

		qindex = srVF.indexA;
	}
	else
	{
		srEE.dMax = srFV.dMax;

		pindex = srFV.indexA;

		qindex = srFV.indexB;
	}
	BigC *= -1.0f;

	if (!srEE.SearchEE(eeflag, pindex, qindex, assigned))
	{
		if (showDebug)
			{TOKAMAK_OUTPUT_2("%d, %d \n", _num_face_test, _num_edge_test);}

		return false;
	}
	if (showDebug2)
	{
		TOKAMAK_OUTPUT_2("%d, %d \n", _num_face_test, _num_edge_test);
	}
	if (!assigned)
	{
		if (!need2Swap)
		{
			ASSERT(srFV.typeA == SearchResult::FACE && srFV.typeB == SearchResult::VERTEX);

			result.penetrate = true;

			result.collisionFrame[2] = srFV.face.normal * -1.0f;

			result.depth = srFV.face.k;

			result.contactB = srFV.objB.GetVertWorld(srFV.indexB);

			result.contactA = result.contactB + srFV.face.normal * srFV.face.k;
		}
		else
		{
			ASSERT(srVF.typeA == SearchResult::FACE && srVF.typeB == SearchResult::VERTEX);

			result.penetrate = true;

			result.collisionFrame[2] = srVF.face.normal;

			result.depth = srVF.face.k;

			result.contactA = srVF.objB.GetVertWorld(srVF.indexB);

			result.contactB = result.contactA + srVF.face.normal * srVF.face.k;
		}
	}
	else
	{
		ASSERT(srEE.typeA == SearchResult::EDGE &&
				srEE.typeB == SearchResult::EDGE);

		neV3 edgeA[2];
		neV3 edgeB[2];

		srEE.objA.GetWorldEdgeVerts(srEE.indexA, edgeA[0], edgeA[1]);

		srEE.objB.GetWorldEdgeVerts(srEE.indexB, edgeB[0], edgeB[1]);

		bool r = CalcContactEE(edgeA[0], edgeA[1], edgeB[0], edgeB[1], result.contactA, result.contactB);

		if (r)
		{
			if (srEE.face.k > 0.0f)
			{
				result.collisionFrame[2] = srEE.face.normal * -1.0f;

				result.depth = srEE.face.k;
			}
			else
			{
				result.collisionFrame[2] = srEE.face.normal;

				result.depth = srEE.face.k * -1.0f;
			}

			result.penetrate = true;
		}
		else
		{
			return false;
		}
	}

	return true;
}

bool TestDCDTri(ConvexTestResult & res, TConvex & convexA, neT3 & transA, const neV3 & insidePoint)
{
	res.valid = false;

	neV3 aPoint = transA.pos;

	neV3 av; av.Set(0.1f);

	aPoint += av;

	neV3 bPoint =  insidePoint;

	BigC = aPoint - bPoint;

	BigCLength = BigC.Length();

	neV3 * aVertArray = NULL, * bVertArray = NULL;

	TConvex dummyB; dummyB.type = TConvex::TRIANGLE;

	if (convexA.type == TConvex::BOX)
	{
		for (s32 i = 0; i < BOX_NUM_VERTS; i++)
		{
			_boxVertexPosP[i] = _boxVertexPos0[i] * convexA.as.box.boxSize;
		}
		aVertArray = _boxVertexPosP;
	}
	neT3 transB; transB.SetIdentity();

	SearchResult srBoxFaceTriVert(convexA, &transA, dummyB, &transB, aVertArray, bVertArray);

	neBool assigned;

	neBool r = srBoxFaceTriVert.SearchFV(0, assigned);

	if (!r)
		{return false;}

/*	bPoint = insidePoint;// + _triNormals[1];
	BigC = bPoint - aPoint;
	BigCLength = BigC.Length();
*/
	BigC *= -1.0f;

	SearchResult srBoxVertTriFace(dummyB, &transB, convexA, &transA, bVertArray, aVertArray);

	if (!(r = srBoxVertTriFace.TestFace(0, assigned)))
		return false;

	//BigC *= -1.0f;
/*
	bPoint = insidePoint + _triNormals[0];
	BigC = bPoint - aPoint;
	BigCLength = BigC.Length();
*/
	neBool assigned2;

	if (!(r = srBoxVertTriFace.TestFace(1, assigned2)))
		return false;

	assigned |= assigned2;

	BigC *= -1.0f;
/*
	bPoint = insidePoint;
	BigC = aPoint - bPoint;
	BigCLength = BigC.Length();
*/

	neBool need2Swap = false;

	SearchResult srBoxTriEE(convexA, &transA, dummyB, &transB, aVertArray, bVertArray);

	s32 eeflag = 0;

	s32 pindex, qindex;

	if (srBoxVertTriFace.dMax > srBoxFaceTriVert.dMax)
	{
		need2Swap = true;

		srBoxTriEE.dMax = srBoxVertTriFace.dMax;

		eeflag = 1;

		pindex = srBoxVertTriFace.indexB; // vertex of Convex

		qindex = srBoxVertTriFace.indexA; // face of Triangle
	}
	else
	{
		srBoxTriEE.dMax = srBoxFaceTriVert.dMax;

		pindex = srBoxFaceTriVert.indexA; // face of Convex

		qindex = srBoxFaceTriVert.indexB; // vertex of Triangle
	}
	//BigC *= -1.0f;

	if (!srBoxTriEE.SearchEETri(eeflag, pindex, qindex, assigned))
	{
		return false;
	}
	if (!assigned)
	{
FV_Backup:
		if (!need2Swap)
		{
			ASSERT(srBoxFaceTriVert.typeA == SearchResult::FACE && srBoxFaceTriVert.typeB == SearchResult::VERTEX);

			res.valid = true;

			res.contactNormal = srBoxFaceTriVert.face.normal * -1.0f;

			res.depth = srBoxFaceTriVert.face.k;

			res.contactB = srBoxFaceTriVert.objB.GetVertWorld(srBoxFaceTriVert.indexB);

			res.contactA = res.contactB + srBoxFaceTriVert.face.normal * srBoxFaceTriVert.face.k;
		}
		else
		{
			ASSERT(srBoxVertTriFace.typeA == SearchResult::FACE && srBoxVertTriFace.typeB == SearchResult::VERTEX);

			res.valid = true;

			res.contactNormal = srBoxVertTriFace.face.normal;

			res.depth = srBoxVertTriFace.face.k;

			res.contactA = srBoxVertTriFace.objB.GetVertWorld(srBoxVertTriFace.indexB);

			res.contactB = res.contactA + srBoxVertTriFace.face.normal * srBoxVertTriFace.face.k;
		}
	}
	else
	{
		ASSERT(srBoxTriEE.typeA == SearchResult::EDGE &&
				srBoxTriEE.typeB == SearchResult::EDGE);

		neV3 edgeA[2];
		neV3 edgeB[2];

		srBoxTriEE.objA.GetWorldEdgeVerts(srBoxTriEE.indexA, edgeA[0], edgeA[1]);

		srBoxTriEE.objB.GetWorldEdgeVerts(srBoxTriEE.indexB, edgeB[0], edgeB[1]);

		bool r = CalcContactEE(edgeA[0], edgeA[1], edgeB[0], edgeB[1], res.contactA, res.contactB);

		if (r)
		{
			if (srBoxTriEE.face.k > 0.0f)
			{
				res.contactNormal = srBoxTriEE.face.normal * -1.0f;

				res.depth = srBoxTriEE.face.k;
			}
			else
			{
				res.contactNormal = srBoxTriEE.face.normal;

				res.depth = srBoxTriEE.face.k * -1.0f;
			}

			res.valid = true;
		}
		else
		{
			//return false;
			goto FV_Backup;
		}
	}
	return true;
}

void Convex2TerrainTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB)
{
	neSimpleArray<s32> & _triIndex = *convexB.as.terrain.triIndex;

	s32 triangleCount = _triIndex.GetUsedCount();

	neArray<neTriangle_> & triangleArray = *convexB.as.terrain.triangles;

	ConvexTestResult res[2];

	s32 finalTriIndex = -1;
	s32 currentRes = 1;
	s32 testRes = 0;

	res[currentRes].depth = -1.0e6f;
	res[currentRes].valid = false;
	res[testRes].depth = 1.0e6f;
	
	s32 terrainMatID = 0;

	neBool found = false;
#if 0
	for (s32 j = 0/*triangleCount-1*/; j < triangleCount; j++)
	//int j = 12;
	{
		neV3 points[4];
		neV3 red;red.Set(1.0f);

		neTriangle_ * t =  &triangleArray[_triIndex[j]];

		points[0] = convexB.vertices[t->indices[0]];
		points[1] = convexB.vertices[t->indices[1]];
		points[2] = convexB.vertices[t->indices[2]];
		points[3] = convexB.vertices[t->indices[0]];
		extern void DrawLine(const neV3 & colour, neV3 * startpoint, s32 count);
		DrawLine(red, points, 4);
	}
#endif
	for (s32 i = 0; i < triangleCount; i++)
	{
		s32 test = _triIndex[i];

		neTriangle_ * t = &triangleArray[_triIndex[i]];

		_triVertexPos[0] = convexB.vertices[t->indices[0]];
		_triVertexPos[1] = convexB.vertices[t->indices[1]];
		_triVertexPos[2] = convexB.vertices[t->indices[2]];

		neV3 diff1 = _triVertexPos[1] - _triVertexPos[0];
		neV3 diff2 = _triVertexPos[2] - _triVertexPos[1];
		neV3 diff3 = _triVertexPos[0] - _triVertexPos[2];

		_triNormals[0] = diff1.Cross(diff2);

		_triNormals[0].Normalize();

		_triNormals[1] = -_triNormals[0];

		_triNormals[0].v[3] = _triNormals[0].Dot(_triVertexPos[0]);

		_triNormals[1].v[3] = -_triNormals[0].v[3];

		TriEdgeDir[0] = _triNormals[0].Cross(diff1);
		TriEdgeDir[1] = _triNormals[0].Cross(diff2);
		TriEdgeDir[2] = _triNormals[0].Cross(diff3);
		TriEdgeDir[0].Normalize();
		TriEdgeDir[1].Normalize();
		TriEdgeDir[2].Normalize();

		neV3 insidePoint = _triVertexPos[0] + _triVertexPos[1] + _triVertexPos[2];

		insidePoint *= (1.0f / 3.0f);

		//insidePoint += (_triNormals[1] * 0.1f);

		if (TestDCDTri(res[testRes], convexA, transA, insidePoint))
		{
			if (res[testRes].depth > res[currentRes].depth)
			{
				s32 tmp = testRes;	

				testRes = currentRes;

				currentRes = tmp;

				terrainMatID = t->materialID;

				finalTriIndex = _triIndex[i];

				found = true;
			}
		}
	}
	if (found)
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

bool CalcContactEE(const neV3 & edgeA0, 
					const neV3 & edgeA1, 
					const neV3 & edgeB0, 
					const neV3 & edgeB1, neV3 & contactA, neV3 & contactB)
{
	f32 d1343, d4321, d1321, d4343, d2121;
	f32 numer, denom, au, bu;
	
	neV3 p13;
	neV3 p43;
	neV3 p21;

	p13 = (edgeA0) - (edgeB0);
	p43 = (edgeB1) - (edgeB0);

	if ( p43.IsConsiderZero() )
	{
		goto CalcContactEE_Exit;
	}
	
	p21 = (edgeA1) - (edgeA0);

	if ( p21.IsConsiderZero() )
	{
		goto CalcContactEE_Exit;
	}
	
	d1343 = p13.Dot(p43);
	d4321 = p43.Dot(p21);
	d1321 = p13.Dot(p21);
	d4343 = p43.Dot(p43);
	d2121 = p21.Dot(p21);

	denom = d2121 * d4343 - d4321 * d4321;   

	if (neAbs(denom) < NE_ZERO) 
		goto CalcContactEE_Exit;

	numer = d1343 * d4321 - d1321 * d4343;
	au = numer / denom;   
	bu = (d1343 + d4321 * (au)) / d4343;

	if (au < 0.0f || au >= 1.0f)
		goto CalcContactEE_Exit;
	
	if (bu < 0.0f || bu >= 1.0f)
		goto CalcContactEE_Exit;

	{
		neV3 tmpv;

		tmpv = p21 * au;
		contactA = (edgeA0) + tmpv;

		tmpv = p43 * bu;
		contactB = (edgeB0) + tmpv;
	}

	return true;

CalcContactEE_Exit:

	return false;
}