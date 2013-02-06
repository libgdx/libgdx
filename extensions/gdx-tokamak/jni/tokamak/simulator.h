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

#ifndef SIMULATOR_H
#define SIMULATOR_H

#define DEFAULT_CONSTRAINT_EPSILON 0.001f

#define DEFAULT_CONSTRAINT_ITERATION 4

//#define _DEBUG_REGION


///////////////////////////////////////////////////////////////////
//
//	Simulator
//
//
///////////////////////////////////////////////////////////////////
/*
class neGravityController: public neController
{
public:
	neV3 gravity;

	void ControllerUpdate(neRigidBody & rb)
	{
		neV3 gforce = gravity;

		gforce *= rb.GetMass();

		rb.ApplyForceCOG(gforce);
	}
};
*/

class neFastImpulse
{
public:
	neRigidBody_ * bodyA;
	neRigidBody_ * bodyB;
	neV3 contactA;
	neV3 contactB;
	neM3 k;
	neM3 kInv;
	neV3 initRelVel;
	neM3 collisionFrame;
	neM3 w2c;
	f32 relativeSpeedSq;
	
public:
	void Init();
	void Update();
	void Apply(f32 scale);
};


struct nePhysicsMaterial
{
	f32 friction;
	f32 resititution;
	f32 density;
};


/****************************************************************************
*
*	NE Physics Engine 
*
*	Class: neRegion
*
*	Desc:
*
****************************************************************************/ 
class neRegion;

class neCoordList
{
public:
	neDLinkList<CCoordListEntry> coordList;

	void Add(neRigidBodyBase * bb, neRigidBodyBase * hint, s32 hintCoord);

	bool Reserve(s32 size, neAllocatorAbstract * all = NULL)
	{
		return coordList.Reserve(size, all);
	}

	void Sort(bool sortOnly);

#ifdef _DEBUG

	void OuputDebug();

#endif

	s32 dim;

	neByte dimPower2;

	neRegion * region;
};

typedef struct neOverlappedPair neOverlappedPair;

struct neOverlappedPair
{
PLACEMENT_MAGIC
	neRigidBodyBase * bodyA;
	neRigidBodyBase * bodyB;
};

typedef struct neOverlapped neOverlapped;

struct neOverlapped
{
	neByte status;
	neOverlappedPair * pairItem;
};

struct neAddBodyInfo
{
	neRigidBodyBase * body;

	neRigidBodyBase * hint;
};

class neRegion 
{
public:
	neRegion() {}

//	enum {
//		MAX_OVERLAPPED = 100000,
//	};
	enum {
		SORT_DIMENSION_X = 1,
		SORT_DIMENSION_Y = 2,
		SORT_DIMENSION_Z = 4,
	};
	void Initialise(neFixedTimeStepSimulator * s, neByte sortD = (SORT_DIMENSION_X | SORT_DIMENSION_Y));

	bool AddBody(neRigidBodyBase * bb, neRigidBodyBase * hint);

	void InsertCoordList(neRigidBodyBase * bb, neRigidBodyBase * hint);

	void RemoveBody(neRigidBodyBase * bb);

	void Update();

	void Rebuild();

	neOverlapped * GetOverlappedStatus(neRigidBodyBase * a, neRigidBodyBase * b);

	void ToggleOverlapStatus(neRigidBodyBase * a, neRigidBodyBase * b, neByte dimp2);

	void ResetOverlapStatus(neRigidBodyBase * a, neRigidBodyBase * b);

	void MakeTerrain(neTriangleMesh * tris);

	void FreeTerrain();

	neTriangleTree & GetTriangleTree() {return terrainTree;}

	~neRegion();

public:
	neByte sortDimension;
		
	neFixedTimeStepSimulator * sim;

	s32 maxRigidBodies;

	s32 maxAnimBodies;

	s32 totalBodies;

	s32 maxParticle;

//	neArray<neOverlapped> rb2rb;

//	neArray<neOverlapped> rb2ab;

	neArray<neOverlapped> b2b;

	neArray<neOverlapped> b2p;

	neSimpleArray<neAddBodyInfo> newBodies;

	neDLinkList<neRigidBodyBase *> bodies;

	neDLinkList<neOverlappedPair> overlappedPairs;

	neCoordList coordLists[3];

	neTriangleTree terrainTree;

#ifdef _DEBUG_REGION
	bool debugOn;
#endif
};


/****************************************************************************
*
*	NE Physics Engine 
*
*	Class: neCollisionTable_
*
*	Desc:
*
****************************************************************************/ 

class neCollisionTable_
{
public:
	enum
	{
		NE_COLLISION_TABLE_MAX = neCollisionTable::NE_COLLISION_TABLE_MAX,
	};

	neCollisionTable_();

	~neCollisionTable_();

	void Set(s32 collisionID1, s32 collisionID2, neCollisionTable::neReponseBitFlag flag);

	neCollisionTable::neReponseBitFlag Get(s32 collisionID1, s32 collisionID2);

	s32 GetMaxCollisionID() {
		return NE_COLLISION_TABLE_MAX;
	};

public:
	neCollisionTable::neReponseBitFlag table[NE_COLLISION_TABLE_MAX][NE_COLLISION_TABLE_MAX];

	neCollisionTable::neReponseBitFlag terrainTable[NE_COLLISION_TABLE_MAX];
};

class nePerformanceData
{
public:
	static nePerformanceData * Create();

	nePerformanceData()
	{
		Reset();
	}
	void Reset()
	{
		dynamic = 0.0f;
		position = 0.0f;
		constrain_1 = 0.0f;
		constrain_2 = 0.0f;
		cd = 0.0f;
		cdCulling = 0.0f;
		terrain = 0.0f;
		terrainCulling = 0.0f;
		controllerCallback = 0.0f;
	}
	void Start();
	void Init();
	f32 GetCount();
	f32 GetTotalTime()
	{
		return dynamic + 
				position +
				constrain_1 +
				constrain_2 +
				cd +
				cdCulling +
				terrain +
				terrainCulling +
				controllerCallback;
	};

	void UpdateDynamic();
	void UpdatePosition();
	void UpdateConstrain1();
	void UpdateConstrain2();
	void UpdateCD();
	void UpdateCDCulling();
	void UpdateTerrain();
	void UpdateTerrainCulling();
	void UpdateControllerCallback();

	f32 dynamic;

	f32 position;

	f32 controllerCallback;

	f32 constrain_1;

	f32 constrain_2;

	f32 cdCulling;

	f32 cd;

	f32 terrain;

	f32 terrainCulling;

	s32 perfFreqAdjust; // in case Freq is too big

	s32 overheadTicks;   // overhead  in calling timer
};

class neFixedTimeStepSimulator
{
public:
	friend class neRegion;

	enum {MAX_MATERIAL = 256,};

	neFixedTimeStepSimulator(const neSimulatorSizeInfo & _sizeInfo, neAllocatorAbstract * alloc = NULL, const neV3 * grav = NULL);
	
	~neFixedTimeStepSimulator();

	void Initialise(const neV3& gravity);
	
	neRigidBody_* CreateRigidBody(neBool isParticle = false);

	neRigidBody_ * CreateRigidBodyFromConvex(TConvex * convex, neRigidBodyBase * originalBody);
	
	neCollisionBody_* CreateCollisionBody();

	void Free(neRigidBodyBase * bb);
	
	void Advance(f32 time, u32 nStep, nePerformanceReport * _perfReport = NULL);

	void Advance(f32 time, f32 minTimeStep, f32 maxTimeStep, nePerformanceReport * _perfReport = NULL);

	void Advance(nePerformanceReport * _perfReport = NULL);

	bool SetMaterial(s32 index, f32 friction, f32 restitution, f32 density);

	bool GetMaterial(s32 index, f32& friction, f32& restitution, f32& density);

	f32 HandleCollision(neRigidBodyBase * bodyA, neRigidBodyBase * bodyB, neCollisionResult & cresult, neImpulseType impulseType, f32 scale = 0.0f);

	void CollisionRigidParticle(neRigidBody_ * ba, neRigidBody_ * bb, neCollisionResult & cresult);

	void SimpleShift(const neCollisionResult & cresult);

	void RegisterPenetration(neRigidBodyBase * bodyA, neRigidBodyBase * bodyB, neCollisionResult & cresult);

	void SetTerrainMesh(neTriangleMesh * tris);

	void FreeTerrainMesh();

	void CreatePoint2PointConstraint(neRigidBodyBase * bodyA, const neV3 & pointA, neRigidBodyBase * bodyB, const neV3 & pointB);

	neStackHeader * NewStackHeader(neStackInfo *);

	neConstraintHeader * NewConstraintHeader();

	void CheckStackHeader();

	neLogOutputCallback * SetLogOutputCallback(neLogOutputCallback * fn);

	neCollisionCallback * SetCollisionCallback(neCollisionCallback * fn);

	void LogOutput(neSimulator::LOG_OUTPUT_LEVEL);

	void SetLogOutputLevel(neSimulator::LOG_OUTPUT_LEVEL lvl);

	void UpdateConstraintControllers();

	void FreeAllBodies();

	void GetMemoryAllocated(s32 & memoryAllocated);

	neBool CheckBreakage(neRigidBodyBase * originalBody, TConvex * convex, const neV3 & contactPoint, neV3 & impulse);

	void ResetTotalForce();
	
	void AdvanceDynamicRigidBodies();
		
	void AdvanceDynamicParticles();

	void AdvancePositionRigidBodies();
		
	void AdvancePositionParticles();

	void ApplyJointDamping();

	void ClearCollisionBodySensors();

	void UpdateAABB();

	//f32 SolveDynamicLocal(neCollisionResult * cr);

	f32 SolveLocal(neCollisionResult * cr);

	void AddContactConstraint(f32 & epsilon, s32 & iteration);

	void SetGravity(const neV3 & g);

	neV3 CalcNormalImpulse(neCollisionResult & cresult, neBool isContact);

	void ResetStackHeaderFlag();

	void AddCollisionResult(neCollisionResult & cresult);

	neCollisionBody_ * GetTerrainBody()
	{
		return &fakeCollisionBody;
	}
	
public:
	neSimulatorSizeInfo sizeInfo;

	nePerformanceReport * perfReport;

	nePerformanceData * perf;

	neV3 gravity;

	neV3 gravityVector;

	f32 gravityMag;

	f32 restingSpeed;

	s32 stepSoFar;

	f32 _currentTimeStep;

	f32 oneOnCurrentTimeStep;

	f32 highEnergy;

//	neConstraintSolver solver;

	neDLinkList<neConstraintHeader> constraintHeaders;

	neDLinkList<_neConstraint> constraintHeap;

//	neDLinkList<neMiniConstraint> miniConstraintHeap;

	neDLinkList<neController> controllerHeap;

	neStackInfoHeap stackInfoHeap;

	neStackHeaderHeap stackHeaderHeap;

	neStackHeader stackHeaderX;

	neDLinkList<neSensor_> sensorHeap;

	neDLinkList<TConvex> geometryHeap;

	neSimpleArray<neByte *> pointerBuffer1;

	neSimpleArray<neByte *> pointerBuffer2;

	neSimulator::LOG_OUTPUT_LEVEL logLevel;

	s32 solverStage;

	bool solverLastIteration;

	static char logBuffer[256];

	neSimpleArray<neCollisionResult> cresultHeap;

	neSimpleArray<neCollisionResult> cresultHeap2;

	neConstraintHeader contactConstraintHeader;

	f32 magicNumber;

	s32 currentRecord;

	f32 timeFromLastFrame;

	f32 lastTimeStep;

protected:
	void CheckCollision();

	void CheckTerrainCollision();

	void SolveAllConstrain();

	void SolveOneConstrainChain(f32 epsilon, s32 iteration);

	void ResolvePenetration();

	void SolveContactConstrain();

	void CheckIfStationary();

	nePhysicsMaterial materials[MAX_MATERIAL];

public:

	neAllocatorAbstract * allocator;

	neAllocatorDefault allocDef;

//data

	u32 maxRigidBodies;

	u32 maxAnimBodies;

	u32 maxParticles;

	neDLinkList<neRigidBody_> rigidBodyHeap;

	neDLinkList<neCollisionBody_> collisionBodyHeap;

	neDLinkList<neRigidBody_> rigidParticleHeap;

	neList<neRigidBody_> activeRB;

	neList<neRigidBody_> inactiveRB;

	neList<neCollisionBody_> activeCB;

	neList<neCollisionBody_> inactiveCB;

	neList<neRigidBody_> activeRP;

	neList<neRigidBody_> inactiveRP;

	neList<neCollisionResult> colResults;

	neRegion region;

	neCollisionTable_ colTable;

	neSimpleArray<neTreeNode*> treeNodes;

	neSimpleArray<s32> triangleIndex;

	neCollisionBody_ fakeCollisionBody;

//state
	bool buildCoordList;

//others
	neCollisionCallback * collisionCallback;
	
	neLogOutputCallback * logCallback;	

	neBreakageCallback * breakageCallback;

	neTerrainTriangleQueryCallback * terrainQueryCallback;

	neCustomCDRB2RBCallback * customCDRB2RBCallback;

	neCustomCDRB2ABCallback * customCDRB2ABCallback;

	s32 idleBodyCount;
};

#endif
