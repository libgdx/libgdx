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

#ifndef TOKAMAK_H
#define TOKAMAK_H

#define TOKAMAK_VERSION_MAJOR 1  
#define TOKAMAK_VERSION_MINOR 0  
#define TOKAMAK_VERSION_BUGFIX 5 
#define TOKAMAK_VERSION  (( TOKAMAK_VERSION_MAJOR <<24)+(TOKAMAK_VERSION_MINOR <<16)+(TOKAMAK_VERSION_BUGFIX <<8) + 0)
#include <malloc.h>

#include "math/ne_math.h"

#ifdef TOKAMAK_USE_DLL
#ifdef TOKAMAK_DLL_EXPORTS
#define TOKAMAK_API __declspec(dllexport)
#else
#define TOKAMAK_API __declspec(dllimport)
#endif
#else
#define TOKAMAK_API 
#endif

#define NE_INTERFACE(n) protected: n(){}; n& operator = (const n & e){return (*this);}

class TOKAMAK_API neRigidBody;

typedef enum
{
	NE_TERRAIN = 0, 
	NE_RIGID_BODY,
	NE_ANIMATED_BODY,
} neBodyType;

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neAllocatorAbstract
*
*	Desc:
*
****************************************************************************/ 

class neAllocatorAbstract
{
public:
	virtual neByte * Alloc(s32 size, s32 alignment = 0) = 0;

	virtual void   Free(neByte *) = 0;
};


/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neAllocatorDefault
*
*	Desc:
*
****************************************************************************/ 

class neAllocatorDefault: public neAllocatorAbstract
{
public:
	neAllocatorDefault()
	{
		usedMem = 0;
	}
	neByte * Alloc(s32 size, s32 alignment = 0) {
		
		usedMem += size;

		return (neByte *)malloc(size);
	}
	void Free(neByte * ptr) {

		free(ptr);
	}
public:
	s32 usedMem;
};


/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: nePerformanceReport
*
*	Desc:
*
****************************************************************************/ 

class nePerformanceReport
{
public:
	enum{
		NE_PERF_TOTAL_TIME = 0,
		NE_PERF_DYNAMIC,
		NE_PERF_POSITION,
		NE_PERF_CONTRAIN_SOLVING_1,
		NE_PERF_CONTRAIN_SOLVING_2,
		NE_PERF_COLLISION_DETECTION,
		NE_PERF_COLLISION_CULLING,
		NE_PERF_TERRAIN_CULLING,
		NE_PERF_TERRAIN,
		NE_PERF_CONTROLLER_CALLBACK,
		NE_PERF_LAST,
	};
	enum
	{
		NE_PERF_RUNNING_AVERAGE = 0,
		NE_PERF_SAMPLE,
	};
	f32 time[NE_PERF_LAST];
	f32 accTime[NE_PERF_LAST];

	void Reset()
	{
		for (s32 i = 0; i < NE_PERF_LAST; i++)
		{
			time[i] = 0.0f;
			accTime[i] = 0.0f;
		}
		numSample = 0;
	}
	void SetReportType(s32 type)
	{
		reportType = type;
	}
	s32 reportType;
	s32 numSample;
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neGeometry
*
*	Desc:
*
****************************************************************************/ 

class TOKAMAK_API neGeometry;

typedef void (neBreakageCallback)(neByte * originalBody, neBodyType bodyType, neGeometry * brokenGeometry, neRigidBody * newBody);

class TOKAMAK_API neGeometry
{
NE_INTERFACE(neGeometry)

public:

typedef enum
{
	NE_BREAK_DISABLE,
	NE_BREAK_NORMAL,
	NE_BREAK_ALL,
	NE_BREAK_NEIGHBOUR,
	
	/*	the following are the same as above, 
		except it create a rigid particle instead of a rigid body 
	*/

	NE_BREAK_NORMAL_PARTICLE, 
	NE_BREAK_ALL_PARTICLE,
	NE_BREAK_NEIGHBOUR_PARTICLE,
} neBreakFlag;

public:
	void	SetTransform(neT3 & t);
	
	void	SetMaterialIndex(s32 index);

	s32		GetMaterialIndex();

	neT3	GetTransform();

	void	SetUserData(u32 userData);

	u32		GetUserData();

	/*
		Box
	*/
	void	SetBoxSize(f32 width, f32 height, f32 depth);

	void	SetBoxSize(const neV3 & boxSize);

	neBool	GetBoxSize(neV3 & boxSize); // return false if geometry is not a box

	/*
		Sphere
	*/
	void	SetSphereDiameter(f32 diameter);

	neBool	GetSphereDiameter(f32 & diameter); // return false if geometry is not a sphere

	/*
		Cylinder
	*/
	void	SetCylinder(f32 diameter, f32 height);

	neBool	GetCylinder(f32 & diameter, f32 & height); // return false if geometry is not a cylinder

	/*
		Convex
	*/
	void	SetConvexMesh(neByte * convexData);

	neBool	GetConvexMesh(neByte *& convexData);

	/*
		Breakage functions
	*/
	void	SetBreakageFlag(neBreakFlag flag);

	neBreakFlag GetBreakageFlag();

	void	SetBreakageMass(f32 mass);

	f32		GetBreakageMass();
	
	void	SetBreakageInertiaTensor(const neV3 & tensor);

	neV3	GetBreakageInertiaTensor();

	void	SetBreakageMagnitude(f32 mag);

	f32		GetBreakageMagnitude();

	void	SetBreakageAbsorption(f32 absorb);

	f32		GetBreakageAbsorption();

	void	SetBreakagePlane(const neV3 & planeNormal);

	neV3	GetBreakagePlane();

	void	SetBreakageNeighbourRadius(f32 radius);

	f32		GetBreakageNeighbourRadius();
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neTriangleMesh
*
*	Desc:
*
****************************************************************************/ 
class neTriangle
{
public:
	neTriangle()
	{
		flag = NE_TRI_TRIANGLE;
		materialID = 0;
		indices[0] = -1;
		indices[1] = -1;
		indices[2] = -1;
		userData = 0;
	}

	enum
	{
		NE_TRI_TRIANGLE = 0,
		NE_TRI_HEIGHT_MAP,
	};
	s32 indices[3];
	s32 materialID;
	u32 flag;
	u32 userData;
};

class neTriangleMesh
{
public:
	neV3 * vertices;

	s32 vertexCount;

	neTriangle * triangles;

	s32 triangleCount;
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neSensor
*
*	Desc:
*
****************************************************************************/ 

class TOKAMAK_API neRigidBody;

class TOKAMAK_API neAnimatedBody;

class TOKAMAK_API neSensor
{
NE_INTERFACE(neSensor)

public:
	void	SetLineSensor(const neV3 & pos, const neV3 & lineVector);

	void	SetUserData(u32 userData);

	u32		GetUserData();

	neV3	GetLineVector();

	neV3	GetLineUnitVector();

	neV3	GetLinePos();

	f32		GetDetectDepth();

	neV3	GetDetectNormal();

	neV3	GetDetectContactPoint();

	neRigidBody * GetDetectRigidBody();

	neAnimatedBody * GetDetectAnimatedBody();

	s32		GetDetectMaterial();
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neAnimatedBody
*
*	Desc:
*
****************************************************************************/ 

class TOKAMAK_API neAnimatedBody
{
NE_INTERFACE(neAnimatedBody)

public:

//spatial states
	neV3	GetPos();
	
	void	SetPos(const neV3 & p);
	
	neM3	GetRotationM3();
	
	neQ		GetRotationQ();
	
	void	SetRotation(const neM3 & m);
	
	void	SetRotation(const neQ & q);
	
	neT3	GetTransform();

//collision related
	void	SetCollisionID(s32 cid);
	
	s32		GetCollisionID();
	
	void	SetUserData(u32 userData);
	
	u32		GetUserData();
	
	s32		GetGeometryCount();
	
//collision geometries and sensors

	neGeometry *	AddGeometry();

	neBool			RemoveGeometry(neGeometry * g);

	void			BeginIterateGeometry();

	neGeometry *	GetNextGeometry();

	neRigidBody *	BreakGeometry(neGeometry * g);

	neSensor *		AddSensor();

	neBool			RemoveSensor(neSensor * s);

	void			BeginIterateSensor();

	neSensor *		GetNextSensor();

	void			UseCustomCollisionDetection(neBool yes, const neT3 * obb, f32 boundingRadius);

	neBool			UseCustomCollisionDetection();

//functions
	void	UpdateBoundingInfo();

	// collide with any body which connected to this body indirectly

	void	CollideConnected(neBool yes);

	neBool	CollideConnected();

	// collide with any body which connected to this body directly

	void	CollideDirectlyConnected(neBool yes);

	neBool	CollideDirectlyConnected();

	void	Active(neBool yes, neRigidBody * hint = NULL);

	void	Active(neBool yes, neAnimatedBody * hint = NULL);

	neBool	Active();
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neRigidBody
*
*	Desc:
*
****************************************************************************/ 

class TOKAMAK_API neRigidBodyController;

class TOKAMAK_API neJointController;

class neRigidBodyControllerCallback
{
public:
	virtual void RigidBodyControllerCallback(neRigidBodyController * controller, float timeStep) = 0;
};

class neJointControllerCallback
{
public:
	virtual void ConstraintControllerCallback(neJointController * controller, float timeStep) = 0;
};

class TOKAMAK_API neRigidBody
{
NE_INTERFACE(neRigidBody)

public:

//physical properties
	f32		GetMass();
	
	void	SetMass(f32 mass);
	
	void	SetInertiaTensor(const neM3 & tensor);

	void	SetInertiaTensor(const neV3 & tensor);

//other properties
	void	SetCollisionID(s32 cid);
	
	s32		GetCollisionID();
	
	void	SetUserData(u32 userData);
	
	u32		GetUserData();
	
	s32		GetGeometryCount();

	void	SetLinearDamping(f32 damp);	

	f32		GetLinearDamping();

	void	SetAngularDamping(f32 damp);	

	f32		GetAngularDamping();

	void	SetSleepingParameter(f32 sleepingParam);

	f32		GetSleepingParameter();

//collision geometries, sensors and controllers
	
	neGeometry *	AddGeometry();

	neBool			RemoveGeometry(neGeometry * g);

	void			BeginIterateGeometry();

	neGeometry *	GetNextGeometry();

	neRigidBody *	BreakGeometry(neGeometry * g);

	void			UseCustomCollisionDetection(neBool yes,  const neT3 * obb, f32 boundingRadius);
	
	neBool			UseCustomCollisionDetection();

	neSensor *		AddSensor();

	neBool			RemoveSensor(neSensor * s);
	
	void			BeginIterateSensor();

	neSensor *		GetNextSensor();

	neRigidBodyController * AddController(neRigidBodyControllerCallback * controller, s32 period);

	neBool			RemoveController(neRigidBodyController * rbController);

	void			BeginIterateController();

	neRigidBodyController * GetNextController();

//spatial states
	neV3	GetPos();
	
	void	SetPos(const neV3 & p);
	
	neM3	GetRotationM3();
	
	neQ		GetRotationQ();
	
	void	SetRotation(const neM3 & m);
	
	void	SetRotation(const neQ & q);
	
	neT3	GetTransform();

//dynamic states
	neV3	GetVelocity();
	
	void	SetVelocity(const neV3 & v);
	
	neV3	GetAngularVelocity();
	
	neV3	GetAngularMomentum();
	
	void	SetAngularMomentum(const neV3& am);

	neV3	GetVelocityAtPoint(const neV3 & pt);

//functions
	void	UpdateBoundingInfo();
	
	void	UpdateInertiaTensor();
	
	void	SetForce(const neV3 & force);

	void	SetTorque(const neV3 & torque);
	
	void	SetForce(const neV3 & force, const neV3 & pos);

	neV3	GetForce();

	neV3	GetTorque();

	void	ApplyImpulse(const neV3 & impulse);

	void	ApplyImpulse(const neV3 & impulse, const neV3 & pos);

	void	ApplyTwist(const neV3 & twist);

	void	GravityEnable(neBool yes);

	neBool	GravityEnable();

	// collide with any body which connected to this body indirectly
	
	void	CollideConnected(neBool yes); 

	neBool	CollideConnected();

	// collide with any body which connected to this body directly

	void	CollideDirectlyConnected(neBool yes);

	neBool	CollideDirectlyConnected();

	void	Active(neBool yes, neRigidBody * hint = NULL);

	void	Active(neBool yes, neAnimatedBody * hint = NULL);

	neBool	Active();

	neBool	IsIdle();
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neJoint
*
*	Desc:
*
****************************************************************************/ 

class TOKAMAK_API neJoint
{
NE_INTERFACE(neJoint)

public:
	typedef enum
	{
		NE_JOINT_BALLSOCKET,
		NE_JOINT_BALLSOCKET2,
		NE_JOINT_HINGE,
		NE_JOINT_SLIDE,

	}ConstraintType;

	void SetType(ConstraintType t);

	ConstraintType GetType();

	void SetJointFrameA(const neT3 & frameA);

	void SetJointFrameB(const neT3 & frameB);

	void SetJointFrameWorld(const neT3 & frame);

	neT3 GetJointFrameA();

	neT3 GetJointFrameB();

	void SetJointLength(f32 length);

	f32 GetJointLength();

	neRigidBody * GetRigidBodyA();

	neRigidBody * GetRigidBodyB();

	neAnimatedBody * GetAnimatedBodyB();

	void Enable(neBool yes);

	neBool Enable();

	void SetDampingFactor(f32 damp);

	f32 GetDampingFactor();

	/*
		Query Joint position
	*/

	f32 GetPosition();

	f32 GetPosition2();

	/*
		Constraint primary limit functions
	*/
	neBool EnableLimit();

	void EnableLimit(neBool yes);

	f32 GetUpperLimit();

	void SetUpperLimit(f32 upperLimit);

	f32 GetLowerLimit();

	void SetLowerLimit(f32 lowerLimit);

	/*
		Constraint secondary limit functions (only apply to some Constraint types)
	*/

	neBool EnableLimit2();

	void EnableLimit2(neBool yes);

	f32 GetUpperLimit2();

	void SetUpperLimit2(f32 upperLimit);

	f32 GetLowerLimit2();

	void SetLowerLimit2(f32 lowerLimit);

	/*
		relates to accuracy and speed of the joint solver
	*/
	void SetEpsilon(f32 e);

	f32 GetEpsilon();

	void SetIteration(s32 i);

	s32 GetIteration();

	/*
		Constraint controller functions
	*/

	neJointController * AddController(neJointControllerCallback * controller, s32 period);

	neBool	RemoveController(neJointController * rbController);

	void	BeginIterateController();

	neJointController * GetNextController();
	
	/*
	 	Constraint primary motor function, currently only implemented for hinge Constraint
	 */
	enum MotorType
	{
		NE_MOTOR_SPEED,
		NE_MOTOR_POSITION, //not implemented
	};
	neBool EnableMotor();

	void EnableMotor(neBool yes);

	void SetMotor(MotorType motorType, f32 desireValue, f32 maxForce);

	void GetMotor(MotorType & motorType, f32 & desireValue, f32 & maxForce);

	neBool EnableMotor2();

	void EnableMotor2(neBool yes);

	void SetMotor2(MotorType motorType, f32 desireValue, f32 maxForce);

	void GetMotor2(MotorType & motorType, f32 & desireValue, f32 & maxForce);
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: 
*
*	Desc:
*
****************************************************************************/ 

class TOKAMAK_API neRigidBodyController
{
NE_INTERFACE(neRigidBodyController);

public:
	neRigidBody * GetRigidBody();

	neV3 GetControllerForce();

	neV3 GetControllerTorque();
	
	void SetControllerForce(const neV3 & force);

	void SetControllerTorque(const neV3 & torque);

	void SetControllerForceWithTorque(const neV3 & force, const neV3 & pos);
};

class TOKAMAK_API neJointController
{
NE_INTERFACE(neJointController);

public:
	neJoint * GetJoint();

	neV3 GetControllerForceBodyA();

	neV3 GetControllerForceBodyB();

	neV3 GetControllerTorqueBodyA();

	neV3 GetControllerTorqueBodyB();

	void SetControllerForceBodyA(const neV3 & force);

	void SetControllerForceBodyB(const neV3 & force);

	void SetControllerForceWithTorqueBodyA(const neV3 & force, const neV3 & pos);

	void SetControllerForceWithTorqueBodyB(const neV3 & force, const neV3 & pos);

	void SetControllerTorqueBodyA(const neV3 & torque);

	void SetControllerTorqueBodyB(const neV3 & torque);
};
/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neCollisionTable
*
*	Desc:
*
****************************************************************************/ 

class TOKAMAK_API neCollisionTable
{
NE_INTERFACE(neCollisionTable)

public:
	enum neReponseBitFlag
	{
		RESPONSE_IGNORE = 0,
		RESPONSE_IMPULSE = 1,
		RESPONSE_CALLBACK = 2,
		RESPONSE_IMPULSE_CALLBACK = 3,
	};

	enum
	{
		NE_COLLISION_TABLE_MAX = 64,
	};
	void Set(s32 collisionID1, s32 collisionID2, neReponseBitFlag response = RESPONSE_IMPULSE);

	neReponseBitFlag Get(s32 collisionID1, s32 collisionID2);

	s32 GetMaxCollisionID();
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neSimulatorSizeInfo
*
*	Desc:
*
****************************************************************************/ 


class neSimulatorSizeInfo
{
public:
	enum
	{
		DEFAULT_RIGIDBODIES_COUNT = 50,
		DEFAULT_ANIMATEDBODIES_COUNT = 50, 
		DEFAULT_RIGIDPARTICLES_COUNT = 50,

		DEFAULT_CONTROLLERS_COUNT = 50,
		DEFAULT_OVERLAPPED_PAIRS_COUNT = 1225,

		DEFAULT_GEOMETRIES_COUNT = 50,
		
		DEFAULT_CONSTRAINTS_COUNT = 100,
		DEFAULT_CONTRAINT_SETS_COUNT = 100,
		DEFAULT_SOLVER_BUFFER_SIZE = 2000,
		DEFAULT_SENSORS_COUNT = 100,

		DEFAULT_TERRAIN_NODES_START_COUNT = 200,
		DEFAULT_TERRAIN_NODES_GROWBY_COUNT = -1,
	};

public:
	
	s32 rigidBodiesCount;		/* Number of rigid bodies in the simulation */
	s32 animatedBodiesCount;	/* Number of animated bodies in the simulation */
	s32 rigidParticleCount;		/* Number of rigid particles in the simulation */

	s32 controllersCount;		/* Number of controller instances in the simulation */
	
	s32 overlappedPairsCount;	/* Number of possible overlapping pairs.
								   This has the maximum value of (n x (n - 1)) / 2,
								   where n = rigidBodyCount + animatedBodyCount.
								   But in practice it rarely reach that high.
								   You can try to specify a smaller number to save memory.
								*/
	s32 geometriesCount;		/* Number of collision geometries in the simulator*/


	s32 constraintsCount;		/* Number of joints in the simulation */
	s32 constraintSetsCount;	/* Number of joint Sets in the simulation */
	s32 constraintBufferSize;	/* Size of the buffer use to solve joints */
	s32 sensorsCount;

	s32 terrainNodesStartCount;	/* Number of nodes use to store terrain triangles */
	s32 terrainNodesGrowByCount;/* Grow by this size if run out of nodes */

public:
	
	neSimulatorSizeInfo()		/* Fill with default size values */
	{
		rigidBodiesCount = DEFAULT_RIGIDBODIES_COUNT;
		animatedBodiesCount = DEFAULT_ANIMATEDBODIES_COUNT;
		rigidParticleCount = DEFAULT_RIGIDPARTICLES_COUNT;

		controllersCount = DEFAULT_CONTROLLERS_COUNT;

		overlappedPairsCount = DEFAULT_OVERLAPPED_PAIRS_COUNT;

		geometriesCount = DEFAULT_GEOMETRIES_COUNT;

		constraintsCount = DEFAULT_CONSTRAINTS_COUNT;
		constraintSetsCount = DEFAULT_CONTRAINT_SETS_COUNT;
		constraintBufferSize = DEFAULT_SOLVER_BUFFER_SIZE;
		sensorsCount = DEFAULT_SENSORS_COUNT;

		terrainNodesStartCount = DEFAULT_TERRAIN_NODES_START_COUNT;
		terrainNodesGrowByCount = DEFAULT_TERRAIN_NODES_GROWBY_COUNT;	
										/* -1 signify double the number of terrainNode, whenever the 
										   it reach full capacity.
										*/
	}
};

/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Class: neSimulator
*
*	Desc:
*
****************************************************************************/ 
typedef struct neCollisionInfo neCollisionInfo;

struct neCollisionInfo
{
	neByte * bodyA;
	neByte * bodyB;
	neBodyType typeA;
	neBodyType typeB;
	neGeometry * geometryA;
	neGeometry * geometryB;
	s32 materialIdA;
	s32 materialIdB;
	neV3 bodyContactPointA;		// contact point A in body space of A
	neV3 bodyContactPointB;		// contact point B in body space of B
	neV3 worldContactPointA;	// contact point A in world space
	neV3 worldContactPointB;	// contact point B in world space
	neV3 relativeVelocity;
	neV3 collisionNormal;
};

typedef void (neLogOutputCallback)(char * logString);

typedef void (neCollisionCallback)(neCollisionInfo & collisionInfo);

typedef void (neTerrainTriangleQueryCallback)(const neV3 & minBound, const neV3 & maxBound, 
											  s32 ** candidateTriangles,
												neTriangle ** triangles,
												neV3 ** vertices,
												s32 * candidateCount,
												s32 * triangleCount,
												neRigidBody * body);

typedef struct neCustomCDInfo neCustomCDInfo;

struct neCustomCDInfo
{
	neV3 collisionNormal;
	neV3 worldContactPointA;
	neV3 worldContactPointB;
	f32 penetrationDepth;
	s32 materialIdA;
	s32 materialIdB;
};

typedef neBool (neCustomCDRB2RBCallback)(neRigidBody * bodyA, neRigidBody * bodyB, neCustomCDInfo & cdInfo);

typedef neBool (neCustomCDRB2ABCallback)(neRigidBody * bodyA, neAnimatedBody * bodyB, neCustomCDInfo & cdInfo);

class TOKAMAK_API neSimulator
{
NE_INTERFACE(neSimulator)

public:
	typedef enum
	{
		LOG_OUTPUT_LEVEL_NONE = 0,
		LOG_OUTPUT_LEVEL_ONE,
		LOG_OUTPUT_LEVEL_FULL,
	} LOG_OUTPUT_LEVEL;
	
public:
	/* 
		Static factory functions  
	*/

	static neSimulator * CreateSimulator(const neSimulatorSizeInfo & sizeInfo, 
										neAllocatorAbstract * alloc = NULL, 
										const neV3 * gravity = NULL);
	
	static void DestroySimulator(neSimulator * sim);
	
	/* 
		Rigid body managment functions 
	*/

	neRigidBody * CreateRigidBody();

	neRigidBody * CreateRigidParticle();
	
	neAnimatedBody * CreateAnimatedBody();
	
	void FreeRigidBody(neRigidBody * body);

	void FreeAnimatedBody(neAnimatedBody * body);

	neCollisionTable * GetCollisionTable();

	/*
		Material managment functions
	*/

	bool SetMaterial(s32 index, f32 friction, f32 restitution);

	bool GetMaterial(s32 index, f32& friction, f32& restitution);

	/*
		Advancing the simulation
	*/

	void Advance(f32 sec, s32 nSteps = 1, nePerformanceReport * perfReport = NULL);

	void Advance(f32 sec, f32 minTimeStep, f32 maxTimeStep, nePerformanceReport * perfReport = NULL);

	/*
		Terrain setup function
	*/

	void SetTerrainMesh(neTriangleMesh * tris);

	void FreeTerrainMesh();

	/*
		Constraint related
	*/

	neJoint * CreateJoint(neRigidBody * bodyA);

	neJoint * CreateJoint(neRigidBody * bodyA, neRigidBody * bodyB);

	neJoint * CreateJoint(neRigidBody * bodyA, neAnimatedBody * bodyB);

	void FreeJoint(neJoint * joint);

	/*
		Others
	*/
	neV3 Gravity();

	void Gravity(const neV3 & gravity);

	void SetBreakageCallback(neBreakageCallback * cb);

	neBreakageCallback * GetBreakageCallback();

	void SetCollisionCallback(neCollisionCallback * cb);

	neCollisionCallback * GetCollisionCallback();

	void SetTerrainTriangleQueryCallback(neTerrainTriangleQueryCallback * cb);
		
	neTerrainTriangleQueryCallback * GetTerrainTriangleQueryCallback();

	void SetCustomCDRB2RBCallback(neCustomCDRB2RBCallback * cb);

	neCustomCDRB2RBCallback * GetCustomCDRB2RBCallback();

	void SetCustomCDRB2ABCallback(neCustomCDRB2ABCallback * cb);

	neCustomCDRB2ABCallback * GetCustomCDRB2ABCallback();

	void SetLogOutputCallback(neLogOutputCallback * cb);

	neLogOutputCallback * GetLogOutputCallback();

	void SetLogOutputLevel(LOG_OUTPUT_LEVEL lvl = LOG_OUTPUT_LEVEL_FULL);

	neSimulatorSizeInfo GetCurrentSizeInfo();

	neSimulatorSizeInfo GetStartSizeInfo();

	void GetMemoryAllocated(s32 & memoryAllocated);
};


/****************************************************************************
*
*	Tokamak Game Physics SDK 
*
*	Misc. helper functions
*
*	
*
****************************************************************************/ 

neV3 TOKAMAK_API neBoxInertiaTensor(f32 width, f32 height, f32 depth, f32 mass);

neV3 TOKAMAK_API neBoxInertiaTensor(const neV3 & boxSize, f32 mass);

neV3 TOKAMAK_API neSphereInertiaTensor(f32 diameter, f32 mass);

neV3 TOKAMAK_API neCylinderInertiaTensor(f32 diameter, f32 height, f32 mass);

#endif//TOKAMAK_H
