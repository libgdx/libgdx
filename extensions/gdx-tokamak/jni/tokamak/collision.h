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

#ifndef COLLISION_H
#define COLLISION_H

typedef enum
{
	IMPULSE_IGNORE,
	IMPULSE_NORMAL,
	IMPULSE_CONTACT,
	IMPULSE_CONSTRAINT,
	IMPULSE_SLIDER,
	IMPULSE_SLIDER_LIMIT_PRIMARY,
//	IMPULSE_LIMIT,
	IMPULSE_ANGULAR_LIMIT_PRIMARY,
	IMPULSE_ANGULAR_LIMIT_SECONDARY,
	IMPULSE_ANGULAR_MOTOR_PRIMARY,
	IMPULSE_ANGULAR_MOTOR_SECONDARY,
	IMPULSE_RELATIVE_LINEAR_VELOCITY,
}neImpulseType;

class neRigidBodyBase;

class neRigidBody_;

///////////////////////////////////////////////////////////////////
//
//	Collision Model
//
//
///////////////////////////////////////////////////////////////////

typedef struct
{
	neV3	point[2];		// closest point in world space, but relative to object centre
	neV3	normal;			// toward feature on body A

	f32		distance;
	bool	penetrated;		
	
	int		matIndex[2];
}TCollisionResult;

///////////////////////////////////////////////////////////////////

//typedef struct neBox neBox; 

typedef struct _neBox
{
	neV3	boxSize; //half of dimensions
	//f32 boxSize[4];
}neBox;

struct neTri
{
	s32 indices[3];
};

struct neTriangleTerrain
{
	neSimpleArray<s32> * triIndex;
	neArray<neTriangle_> * triangles;
};

struct neSphere
{
	f32 radius;
	f32 radiusSq;
};

struct neCylinder
{
	f32 radius;
	f32 radiusSq;
	f32 halfHeight;
};

struct neConvexMesh
{
	neV3 * vertices;
	s32 * neighbours;
	s32 vertexCount;
};

struct neConvexDCD
{
	neByte * convexData;
	neV3 * vertices;
	s32 numVerts;
};

#ifdef USE_OPCODE

struct neOPCMesh
{
	Opcode::OPCODE_Model * opmodel;
	IceMaths::Point * vertices;
	u32 vertCount;
	IndexedTriangle * triIndices;
	u32 triCount;
};

#endif

typedef struct neBreakInfo neBreakInfo;

struct neBreakInfo
{
	neV3 inertiaTensor;
	neV3 breakPlane;
	f32 mass;
	f32 breakMagnitude;
	f32 breakAbsorb;
	f32 neighbourRadius;
	neGeometry::neBreakFlag flag; //break all,
};

typedef struct TConvex TConvex;

struct TConvex
{
	enum
	{
		POINT,
		LINE,
		TRIANGLE,
		BOX,
		SPHERE,
		CYLINDER,
		TERRAIN,
		CONVEXITY,
		CONVEXDCD,
		OPCODE_MESH,
	};

	union
	{
		neBox box;
		neTri tri;
		neTriangleTerrain terrain;
		neSphere sphere;
		neCylinder cylinder;
		neConvexMesh convexMesh;
		neConvexDCD convexDCD;
#ifdef USE_OPCODE
		neOPCMesh opcodeMesh;
#endif
	}as;

	neT3	c2p;	// convex to physics object
	f32		boundingRadius;
	f32		envelope;
	u32		type;
	s32		matIndex;
	u32		userData;
	neBreakInfo breakInfo;
	neV3 *	vertices;

	void	SetBoxSize(f32 width, f32 height, f32 depth);
	void	SetSphere(f32 radius);
	void	SetTriangle(s32 a, s32 b, s32 c, neV3 * vertices);
	void	SetTerrain(neSimpleArray<s32> & triangleIndex, neArray<neTriangle_> & triangles, neV3 * vertices);
	void	SetConvexMesh(neByte * convexData);

#ifdef USE_OPCODE

	void	SetOpcodeMesh(IndexedTriangle * triIndex, u32 triCount, IceMaths::Point * vertArray, u32 vertCount);

#endif

	void	SetTransform(neT3 & t3);
	neT3	GetTransform();
	void	SetUserData(u32 ud)
	{
			userData = ud;
	}
	u32		GetUserData()
	{
			return userData;
	}
	void	SetMaterialId(s32 id);
	s32		GetMaterialId();
	f32		GetBoundRadius();
	u32		GetType();
	void	Initialise();
	neM3	CalcInertiaTensor(f32 density, f32 & mass);
	void	GetExtend(neV3 & minExt, neV3 & maxEnt);

	//quick access functions
	NEINLINE f32 BoxSize(s32 dir)
	{
		ASSERT(type == BOX);
		
		return as.box.boxSize[dir];
	}
	NEINLINE f32 Radius()
	{
		ASSERT(type == SPHERE);
		
		return as.sphere.radius;
	}
	NEINLINE f32 RadiusSq()
	{
		ASSERT(type == SPHERE);
		
		return as.sphere.radiusSq;
	}
	NEINLINE f32 CylinderRadius()
	{
		ASSERT(type == CYLINDER);
		
		return as.cylinder.radius;
	}
	NEINLINE f32 CylinderRadiusSq()
	{
		ASSERT(type == CYLINDER);
		
		return as.cylinder.radiusSq;
	}
	NEINLINE f32 CylinderHalfHeight()
	{
		ASSERT(type == CYLINDER);
		
		return as.cylinder.halfHeight;
	}
};

class neSensor_
{
public:
	neV3 pos;

	neV3 dir;

	neV3 dirNormal;

	f32 length;

	u32 cookies;

	//results
	
	neV3 normal;

	neV3 contactPoint;

	f32 depth;

	s32 materialID;

	neRigidBodyBase * body;

public:

	neSensor_()
	{
		pos.SetZero();
		dir.SetZero();
		cookies = 0;
		normal.SetZero();
		depth = 0;
		materialID = 0;
		body = NULL;
	}
};
/****************************************************************************
*
*	NE Physics Engine 
*
*	Class: neCollision
*
*	Desc:
*
****************************************************************************/ 

typedef neFreeListItem<TConvex> TConvexItem;

class neCollision
{
public:
	neCollision()
	{
		convex = NULL;
		convexCount = 0;
		boundingRadius = 0.0f;
		obb.SetBoxSize(1.0f, 1.0f, 1.0f);
	}

//	void		SeTConvex(TConvex * con, s32 count)
//	{
//		convex = con;
//		convexCount = count+1;
//	}

	void		CalcBB();

public:
	TConvex		obb;
	TConvex *	convex;
	s32			convexCount;
	f32			boundingRadius;
};

class neCollisionResult;

typedef neCollection<neCollisionResult>::itemType neCollisionResultHandle;

class neCollisionResult
{
PLACEMENT_MAGIC
public:
	//neCollisionResultHandle bodyAHandle;
	//neCollisionResultHandle bodyBHandle;

	neV3 contactA;
	neV3 contactB;

	neV3 contactABody; // or relative tangential velocity
	neV3 contactBBody; // or angular limit axis

	neV3 contactAWorld;
	neV3 contactBWorld;

	neM3 collisionFrame;

	neM3 w2c;

	neV3 initRelVelWorld;
	neV3 initRelVel;
	neV3 finaltRelVel;
	
	s32	 materialIdA;
	s32	 materialIdB;
	f32	 depth; //+ve
	neBool penetrate;

	neRigidBodyBase * bodyA;
	neRigidBodyBase * bodyB;

	f32 relativeSpeed;
	f32 finalRelativeSpeed;

	TConvex * convexA;
	TConvex * convexB;

	neM3 k;

	neM3 kInv;

	f32 impulseScale;

	neImpulseType impulseType;

	neBool flag;

	void UpdateConstraintRelativeSpeed();
	void StartStage2();
	void PrepareForSolver(neBool aIdle = false, neBool bIdle = false);
	void CalcCollisionMatrix(neRigidBody_* ba, neRigidBody_ * bb, neBool isWorld);
	void CalcCollisionMatrix2(neRigidBody_* ba, neRigidBody_ * bb);
	void CalcCollisionMatrix3(neRigidBody_* ba, neRigidBody_ * bb);
	
	f32 SolveContact(neFixedTimeStepSimulator * sim);
	f32 SolveConstraint(neFixedTimeStepSimulator * sim);
	f32 SolveSlider(neFixedTimeStepSimulator * sim);
	f32 SolveSliderLimit(neFixedTimeStepSimulator * sim);
	f32 SolveAngularPrimary(neFixedTimeStepSimulator * sim);
	f32 SolveAngularSecondary(neFixedTimeStepSimulator * sim);
	f32 SolveAngularMotorPrimary(neFixedTimeStepSimulator * sim);
	f32 SolveRelativeLinear(neFixedTimeStepSimulator * sim);
	
	f32 SolveAngular(f32 depth, const neV3 & axis, f32 relAV, neFixedTimeStepSimulator * sim);
	f32 SolveAngular2(const neV3 & axisA, const neV3 & axisB, f32 relAV, f32 desireAV, f32 depth, neFixedTimeStepSimulator * sim);
	f32 SolveAngular3(f32 depth, const neV3 & axis, f32 relAV, neFixedTimeStepSimulator * sim);
	
	void CalcError(neFixedTimeStepSimulator * sim);
//	void AddToBodies();
	
	f32 Value(){
		return relativeSpeed;};

	neBool CheckIdle();

	void Swap()
	{
		collisionFrame[2] *=  -1.0f;

		neSwap(contactA, contactB);

		neSwap(convexA, convexB);
	}
};

void CollisionTest(neCollisionResult & result, neCollision & colA, neT3 & transA, neCollision & colB, neT3 & transB, const neV3 & backupVector);

void CollisionTestSensor(TConvex * obbA, neSensor_ * sensorsA, neT3 & transA, neCollision & colB, neT3 & transB, neRigidBodyBase * body);

void ConvexCollisionTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector);

void Box2BoxTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector);

void Box2TriangleTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB);

void Box2TerrainTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB);

void Box2SphereTest(neCollisionResult & result, TConvex & boxA, neT3 & transA, TConvex & sphereB, neT3 & transB);

void Box2CylinderTest(neCollisionResult & result, TConvex & boxA, neT3 & transA, TConvex & sphereB, neT3 & transB);

void Sphere2TerrainTest(neCollisionResult & result, TConvex & sphereA, neT3 & transA, TConvex & terrainB);

void Sphere2SphereTest(neCollisionResult & result, TConvex & sphereA, neT3 & transA, TConvex & sphereB, neT3 & transB);

void Cylinder2CylinderTest(neCollisionResult & result, TConvex & cA, neT3 & transA, TConvex & cB, neT3 & transB);

void Cylinder2TerrainTest(neCollisionResult & result, TConvex & cylinderA, neT3 & transA, TConvex & terrainB);

void Cylinder2SphereTest(neCollisionResult & result, TConvex & cylinderA, neT3 & transA, TConvex & sphereB, neT3 & transB);

void Box2ConvexTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector);

void Convex2ConvexTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB, const neV3 & backupVector);

void TranslateCOM(neM3 & I, neV3 &translate, f32 mass, f32 factor);

void DiagonalizeMassTensor(neM3 & I, neV3 & diagonal, neM3 & eigenVectors);

void SensorTest(neSensor_ & sensorA, TConvex & convexB, neT3 & transB);

#ifdef USE_OPCODE

void Box2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB);

void Sphere2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB);

void Cylinder2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB);

void Opcode2TerrainTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB);

void Opcode2OpcodeTest(neCollisionResult & result, TConvex & convexA, neT3 & transA, TConvex & convexB, neT3 & transB);

#endif //USE_OPCODE

#endif




















