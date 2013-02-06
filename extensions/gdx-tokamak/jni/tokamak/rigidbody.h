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

#ifndef RIGIDBODY_H
#define RIGIDBODY_H

#pragma inline_depth( 8 )

#define MAX_RB_STATES 1
#define MAX_RB_IMPULSE_RECORDS 4
#define MAX_RB_CONTROLLERS 5

class neRigidBodyBase;

class neRigidBody_;

class neFixedTimeStepSimulator;

class neConstraintSolver;

class neRestRecord;

///////////////////////////////////////////////////////////////////
//
//	neControllerCallback
//
///////////////////////////////////////////////////////////////////
class neController
{
PLACEMENT_MAGIC
public:
	s32 period;
	s32 count;
	neRigidBodyControllerCallback * rbc;
	neJointControllerCallback * jc;
	_neConstraint * constraint;
	neRigidBody_ * rb;
	neV3 forceA;
	neV3 torqueA;
	neV3 forceB;
	neV3 torqueB;

public:
	neController()
	{
		period = 0;
		count = 0;
		rbc = NULL;
		jc = NULL;
		constraint = NULL;
		rb = NULL;
	}
};

//typedef neFreeListItem<neController> neControllerItem;

//coordinate list

class CCoordListEntry
{
PLACEMENT_MAGIC
public:
	enum
	{
		LowEnd = 0,
		HighEnd = 1,
	};
	neByte flag;
	f32 value;
	neRigidBodyBase * bb;
};

typedef CCoordListEntry* PCCoordListEntry;

typedef neDLinkList<CCoordListEntry>::listItem CCoordListEntryItem;

///////////////////////////////////////////////////////////////////
//
//	neRigidBodyBase
//
///////////////////////////////////////////////////////////////////
class _neConstraint;

typedef neFreeListItem<neSensor_> neSensorItem;

class neCollisionBody_;

typedef neCollection<neRigidBodyBase>::itemType neBodyHandle;

//neCollection<_neConstraint>::itemType neConstraintItem;

typedef enum
{
	NE_OBJECT_COLISION,
	NE_OBJECT_RIGID,
}neObjectType;

typedef enum
{
	NE_RIGID_NORMAL,
	NE_RIGID_PARTICLE,
}neRigidType;

class neRigidBodyBase
{
public:
	neRigidBodyBase(){ 
		//isAnimated = true; 
		btype = NE_OBJECT_COLISION;
		col.convex = NULL;
		col.convexCount = 0;
		col.obb.Initialise();
		sim = NULL;
		cid = 0;
		isCollideConnected = false;
		isCollideDirectlyConnected = false;
		sensors = NULL;

		geometryCursor = NULL;
		sensorCursor = NULL;

		constraintHeaderItem.thing = this;
		isActive = true;
		regionHandle = NULL;

		_constraintHeader = NULL;

		isCustomCD = false;

		for (s32 i = 0; i < 3; i++)
		{
			maxCoord[i] = NULL;
			minCoord[i] = NULL;
		}

		backupVector.SetZero();

		pendingAddToRegion = 0;
	}
	~neRigidBodyBase()
	{
		//if (col.convex)
		//	delete [] col.convex;
	};
	void RecalcBB();
	
	//TConvex * GetConvex(s32 index);

	void Free();

	NEINLINE neByte IsAABOverlapped(neRigidBodyBase * b)
	{
		neByte ret = 0;

		for (s32 i = 0; i < 3; i++)
		{
			if (minCoord[i])
			{
				if (!(minCoord[i]->value >= b->maxCoord[i]->value || 
					maxCoord[i]->value <= b->minCoord[i]->value))
					ret |= (1 << i);
			}
		}

		return ret;
	}

	neBool IsValid();

	neV3 VelocityAtPoint(const neV3 & pt);

	NEINLINE neV3 GetLinearVelocity();

	NEINLINE neV3 GetAngularVelocity();

	void CollideConnected(neBool yes);

	neBool CollideConnected();

	neSensor_ * AddSensor();

	void BeginIterateSensor();

	neSensor_ * GetNextSensor();

	void ClearSensor();

	TConvex * AddGeometry();

	void BeginIterateGeometry();

	TConvex * GetNextGeometry();

	void Active(neBool yes, neRigidBodyBase * hint);

	NEINLINE neCollisionBody_ * AsCollisionBody() 
	{
		if (btype != NE_OBJECT_COLISION)
			return NULL;

		return (neCollisionBody_ *)this;
	}

	NEINLINE neRigidBody_ * AsRigidBody()
	{
		if (btype != NE_OBJECT_RIGID)
			return NULL;
			
		return (neRigidBody_ *)this;
	}
	
	neT3 & GetB2W();

	NEINLINE void SetConstraintHeader(neConstraintHeader * cheader)
	{
		_constraintHeader = cheader;
	}
	NEINLINE neConstraintHeader * GetConstraintHeader()
	{
		return _constraintHeader;
	}
	void RemoveConstraintHeader();

	NEINLINE neBool IsInRegion()
	{
		return (regionHandle != NULL);
	}

	neBodyHandle constraintHeaderItem;

	//neBodyHandle activeHandle;

	neCollection<_neConstraint> constraintCollection;

	TConvexItem * geometryCursor;

	neSensorItem * sensorCursor;

	u32 cookies;
	
	neCollision col;
	
	u32 id;

	u32 cid;

	neConstraintHeader * _constraintHeader;

	//bool isAnimated;
	neObjectType btype;

	neBool isActive;

	neBool isCollideConnected;

	neBool isCollideDirectlyConnected;

	neSensor_ * sensors;

	neFreeListItem<neRigidBodyBase *> * regionHandle;

	neFixedTimeStepSimulator * sim;

	neT3 obb;

	neBool isCustomCD;

	neV3 minBound;
	neV3 maxBound;
	CCoordListEntry * maxCoord[3];
	CCoordListEntry * minCoord[3];	

	neV3 backupVector;

	neCollection<neRestRecord> rbRestingOnMe;

	s32 pendingAddToRegion;
	
//	neV3 debugMinBound;
//	neM3 dobb;
};

///////////////////////////////////////////////////////////////////
//
//	neRigidBody_
//
///////////////////////////////////////////////////////////////////

class neRigidBodyState
{
public:
	neRigidBodyState();
	neQ q;
	neV3 linearMom; // P
	neV3 angularMom; // L

//	NEINLINE neV3 &pos() const{ 
//		return b2w.pos;}

//	NEINLINE void SetPos(const neV3 & pos){ 
//		b2w.pos = pos;}


	NEINLINE neM3 & rot() { 
		return b2w.rot;}

//private:
	neT3 b2w;
};

class neRigidBodyDerive
{
public:
	neRigidBodyDerive()
	{
		linearVel.SetZero();
		angularVel.SetZero();
		qDot.Zero();
		Iinv.SetZero();
	}
	neV3 linearVel; // v
	neV3 angularVel; // w
	neQ qDot; 
	neM3 Iinv; // R * IbodyInv * Rtrans
	f32 speed;
};

typedef struct neImpulseRecord neImpulseRecord;

struct neImpulseRecord
{
	neV3 point;
	u32 stepCount;
};

///////////////////////////////////////////////////////////////////
//
//	neCollisionBody_
//
///////////////////////////////////////////////////////////////////

class neCollisionBody_: public neRigidBodyBase
{
public:
PLACEMENT_MAGIC

	neCollisionBody_()
	{
		moved = false;
	}
	void UpdateAABB();

	void Free();

public:
	neT3 b2w;
	
	neBool moved;
};

class neStackInfo;
class neStackHeader;

/////////////////////////////////////////////////////////////////

typedef neCollection<neRestRecord>::itemType neRestRecordHandle;

class neRestRecord
{
public:
	typedef enum
	{
		REST_ON_NOT_VALID,
		REST_ON_RIGID_BODY,
		REST_ON_COLLISION_BODY,
		REST_ON_WORLD,

	} RestOnType;

	neV3 bodyPoint;
	neV3 otherBodyPoint;
	neV3 worldThisBody;
	neV3 worldOtherBody;
	neV3 worldDiff;
	neV3 normalBody; //normal define in the body space of otherBody
	neV3 normalWorld; //normal define in the world space
	f32 depth;
	f32 normalDiff;
	f32 tangentialDiffSq;
	s32 material;
	s32 otherMaterial;

private:
	RestOnType rtype;
	neRigidBodyBase * otherBody;
	neRestRecordHandle restOnHandle;
	neRigidBody_ * body;
	s32 counter;

public:	
	neRestRecord()
	{
		restOnHandle.thing = this;
	}
	void Init()
	{
		rtype = REST_ON_NOT_VALID;
		counter = 0;
		otherBody = NULL;
		body = NULL;
		restOnHandle.Remove();
	}
	neBool IsValid()
	{
		return rtype != REST_ON_NOT_VALID;
	}
	void Update();

	neRigidBodyBase * GetOtherBody() const
	{
		return otherBody;
	}
	neRigidBody_ * GetOtherRigidBody() const
	{
		if (!otherBody)
			return NULL;

		return otherBody->AsRigidBody();
	}
	neCollisionBody_ * GetOtherCollisionBody() const
	{
		if (!otherBody)
			return NULL;

		return otherBody->AsCollisionBody();
	}
	neBool CanConsiderOtherBodyIdle();

	neBool CheckOtherBody(neFixedTimeStepSimulator * sim);

	void SetInvalid();

	neV3 GetOtherBodyPoint();

	void Set(neRigidBody_* thisBody, const neRestRecord & rc);

	void SetTmp(neRigidBodyBase * otherb, const neV3 & contactA, const neV3 & contactB, const neV3 & normalBody, s32 matA, s32 matB);
};

class neRestHull
{
public:
	typedef enum
	{
		NONE,
		POINT,
		LINE,
		TRIANGLE,
		QUAD,
	}neRestHullType;

	s32 htype;
	s32 indices[4];
	neV3 normal;
};

enum
{
	NE_RB_MAX_PAST_RECORDS = 10,
	NE_RB_MAX_RESTON_RECORDS = 3,
};	

class neRBExtra
{
public:
	neRestRecord restOnRecord[NE_RB_MAX_RESTON_RECORDS];
	
	neV3 velRecords[NE_RB_MAX_PAST_RECORDS];

	neV3 angVelRecords[NE_RB_MAX_PAST_RECORDS];

	neRestHull restHull;
};

class neMotionCorrectionInfo
{
public:
	neQ  lastQuat;
	neV3 lastPos;
	neV3 lastAM;
	neV3 lastW;
	neM3 lastIinv;
	neV3 lastVel;
	neV3 lastAVel;
};

class neRigidBody_: public neRigidBodyBase
{
PLACEMENT_MAGIC

friend class neFixedTimeStepSimulator;
public:

	enum
	{
		NE_RBSTATUS_NORMAL = 0,
		NE_RBSTATUS_IDLE,
		NE_RBSTATUS_ANIMATED,
	};
	f32		mass;
	f32		oneOnMass;
	neM3	IbodyInv;
	neM3	Ibody;
	neV3	force;
	neV3	torque;
	s32		status;
	neBool	gravityOn;
	neV3	gforce;
	neV3	cforce;
	neV3	ctorque;
	neV3	totalTorque;
	neV3	totalForce;
	neV3	acc;

	f32		linearDamp;
	f32		angularDamp;

	u32 curState;
	
	neRigidBodyState stateBuffer[MAX_RB_STATES];
	
	neRigidBodyDerive derive;	
	
	s32 lowEnergyCounter;

	// constraints
	neStackInfo * stackInfo;
	
	neBool isShifted;

	neBool isShifted2;

	neController * controllers;

	neControllerItem * controllerCursor;

	neBool needRecalc;

	neBool isAddedToSolver;

	neRBExtra * rbExtra;

	neRBExtra eggs;

	neRigidType subType;

	neQ totalRot;

	neV3 totalTrans;

	s32 rotCount;

	s32 transCount;

	neQ totalLastRot;

	s32 lastRotCount;

	neV3 totalLastTrans;

	s32 lastTransCount;

	neBool needSolveContactDynamic;

	neV3 totalDV;

	neV3 totalDA;

	s32 impulseCount;

	s32 twistCount;

	neV3 dvRecord[NE_RB_MAX_PAST_RECORDS];
	neV3 davRecord[NE_RB_MAX_PAST_RECORDS];

	neCollisionResult * maxErrCResult;

	f32 sleepingParam;

	neV3 oldPosition;

	neQ oldRotation;

	neV3 oldVelocity;

	neV3 oldAngularVelocity;

	s32 oldCounter;

	//////////////////////////////////////////////////

	NEINLINE neBool IsParticle()
	{
		return (subType == NE_RIGID_PARTICLE);
	}

	NEINLINE neRestRecord & GetRestRecord(s32 index)
	{
		ASSERT(rbExtra);
		return rbExtra->restOnRecord[index];
	}

	NEINLINE neRestHull & GetRestHull()
	{
		ASSERT(rbExtra);
		return rbExtra->restHull;
	}

	NEINLINE neV3 & GetVelRecord(s32 index)
	{
		ASSERT(rbExtra);
		return rbExtra->velRecords[index];
	}

	NEINLINE neV3 & GetAngVelRecord(s32 index)
	{
		ASSERT(rbExtra);
		return rbExtra->angVelRecords[index];
	}

public:
	neRigidBody_();

	~neRigidBody_();
	
	NEINLINE neRigidBodyState & State() 
	{
		return stateBuffer[curState];
	};
	NEINLINE void SetPos(const neV3 & newPos)
	{
		State().b2w.pos = newPos;
	}
	NEINLINE neV3 GetPos()
	{
		return State().b2w.pos;
	}
	NEINLINE neRigidBodyDerive & Derive(){
		return derive;
	}
	void RecalcInertiaTensor();

	void SetAngMom(const neV3 & am);

	void GravityEnable(neBool yes);

	void CheckForIdle();

	void CheckForIdleNonJoint();

	void CheckForIdleJoint();

	void BecomeIdle();

	void ZeroMotion();

	void WakeUp();

	neBool AddStackInfo(neRestRecord & rc);

	void  FreeStackInfo();

	neBool NewStackInfo(neRestRecord & rc);

	void MigrateNewHeader(neStackHeader * newHeader, neStackHeader * curHeader);

	void ResetRestOnRecords();

	void RemoveStackInfo();

	neBool NewStackInfoTerminator(neStackHeader * header);

	s32 AddContactImpulseRecord(neBool withConstraint);

	neBool IsRestPointStillValid();

	void AddRestContact(neRestRecord & rc);

	neBool CheckContactValidity();

	void ResolveRestingPenetration();

	neBool IsConstraintNeighbour(neRigidBodyBase * otherBody);

	neController * AddController(neRigidBodyControllerCallback * rbc, s32 period);

	void BeginIterateController();

	neController * GetNextController();

	void Free();

	void DrawCPointLine();

	void SetAngMomComponent(const neV3 & angMom, const neV3 & dir);

	void ShiftPosition(const neV3 & delta);

	void UpdateAABB();

	s32 CheckRestHull();

	neBool ApplyCollisionImpulse(const neV3 & impulse, const neV3 & contactPoint, neImpulseType itype);

	neV3 GetCorrectRotation(neRigidBody_ * otherBody, f32 massOther, neV3 & pointThis, neV3 & pointOther);

	void CorrectPosition(neV3 & pointThis, neV3 & pointDest, s32 flag, s32 changeLast);

	void CorrectRotation(f32 massOther, neV3 & pointThis, neV3 & pointDest, neV3 & pointDest2, s32 flag, s32 changeLast);

	void CorrectPenetrationDrift();

	void CorrectPenetrationDrift2(s32 index, neBool slide, s32 flag);

	f32 TestImpulse(neV3 & dir, neV3 & pt, f32 & linear, f32 & angular);

	void UpdateDerive();

	void AddContactConstraint();

	void CorrectPenetrationRotation();

	void CorrectPenetrationTranslation();

	void CorrectPenetrationRotation2(s32 index, neBool slide);

	void CorrectPenetrationTranslation2(s32 index, neBool slide);

	neBool CheckStillIdle();

	neBool CheckHighEnergy();

	neBool TestWakeUpImpulse(const neV3 & impulse);

	void MidPointIntegration(const neV3 & totalTorque, f32 tStep);

	void ImprovedEulerIntegration(const neV3 & totalTorque, f32 tStep);

	void RungeKutta4Integration(const neV3 & totalTorque, f32 tStep);

	void WakeUpAllJoint();

	void ApplyLinearConstraint();

	void ApplyAngularConstraint();

	void ConstraintDoSleepCheck();

	neBool CheckStationary();

	void SyncOldState();

	neBool AllRestRecordInvalid()	;

protected:
	
	void AdvanceDynamic(f32 tStep);
	void AdvancePosition(f32 tStep);
	void IsCollideWith(neRigidBody_& rb);
	void UpdateController();
};

NEINLINE neV3 neRigidBodyBase::GetLinearVelocity()
{
	if (AsCollisionBody())
	{
		neV3 v;

		v.SetZero();

		return v;
	}
	else
	{
		return AsRigidBody()->Derive().linearVel;
	}
}

NEINLINE neV3 neRigidBodyBase::GetAngularVelocity()
{
	if (AsCollisionBody())
	{
		neV3 v;

		v.SetZero();

		return v;
	}
	else
	{
		return ((neRigidBody_*)this)->Derive().angularVel;
	}
}
NEINLINE neV3 neRigidBodyBase::VelocityAtPoint(const neV3 & pt)
{
	neV3 ret;

	if (AsCollisionBody())
	{
		ret.SetZero();

		return ret;
	}
	else
	{
		ret = ((neRigidBody_*)this)->Derive().linearVel;

		ret += ((neRigidBody_*)this)->Derive().angularVel.Cross(pt);

		return ret;
	}
}

/*
NEINLINE neV3 neRigidBodyBase::VelocityAtPoint(const neV3 & pt)
{
	neV3 ret;

	if (AsCollisionBody())
	{
		ret.SetZero();

		return ret;
	}
	else
	{
		//ret = ((neRigidBody_*)this)->Derive().linearVel;

		ret = (((neRigidBody_*)this)->State().pos() - ((neRigidBody_*)this)->correctionInfo.lastPos);

		ret *= (1.0f / sim->currentTimeStep);

		ret += ((neRigidBody_*)this)->Derive().angularVel.Cross(pt);

		return ret;
	}
}
*/
#define TILT_TOLERANCE 0.9f

#endif
