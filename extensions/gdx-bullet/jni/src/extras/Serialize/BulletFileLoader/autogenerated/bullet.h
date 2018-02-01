/* Copyright (C) 2011 Erwin Coumans & Charlie C
*
* This software is provided 'as-is', without any express or implied
* warranty.  In no event will the authors be held liable for any damages
* arising from the use of this software.
*
* Permission is granted to anyone to use this software for any purpose,
* including commercial applications, and to alter it and redistribute it
* freely, subject to the following restrictions:
*
* 1. The origin of this software must not be misrepresented; you must not
*    claim that you wrote the original software. If you use this software
*    in a product, an acknowledgment in the product documentation would be
*    appreciated but is not required.
* 2. Altered source versions must be plainly marked as such, and must not be
*    misrepresented as being the original software.
* 3. This notice may not be removed or altered from any source distribution.
*/
// Auto generated from Bullet/Extras/HeaderGenerator/bulletGenerate.py
#ifndef __BULLET_H__
#define __BULLET_H__
namespace Bullet {

// put an empty struct in the case
typedef struct bInvalidHandle {
	int unused;
}bInvalidHandle;

    class PointerArray;
    class btPhysicsSystem;
    class ListBase;
    class btVector3FloatData;
    class btVector3DoubleData;
    class btQuaternionFloatData;
    class btQuaternionDoubleData;
    class btMatrix3x3FloatData;
    class btMatrix3x3DoubleData;
    class btTransformFloatData;
    class btTransformDoubleData;
    class btBvhSubtreeInfoData;
    class btOptimizedBvhNodeFloatData;
    class btOptimizedBvhNodeDoubleData;
    class btQuantizedBvhNodeData;
    class btQuantizedBvhFloatData;
    class btQuantizedBvhDoubleData;
    class btCollisionShapeData;
    class btStaticPlaneShapeData;
    class btConvexInternalShapeData;
    class btPositionAndRadius;
    class btMultiSphereShapeData;
    class btIntIndexData;
    class btShortIntIndexData;
    class btShortIntIndexTripletData;
    class btCharIndexTripletData;
    class btMeshPartData;
    class btStridingMeshInterfaceData;
    class btTriangleMeshShapeData;
    class btScaledTriangleMeshShapeData;
    class btCompoundShapeChildData;
    class btCompoundShapeData;
    class btCylinderShapeData;
    class btConeShapeData;
    class btCapsuleShapeData;
    class btTriangleInfoData;
    class btTriangleInfoMapData;
    class btGImpactMeshShapeData;
    class btConvexHullShapeData;
    class btCollisionObjectDoubleData;
    class btCollisionObjectFloatData;
    class btContactSolverInfoDoubleData;
    class btContactSolverInfoFloatData;
    class btDynamicsWorldDoubleData;
    class btDynamicsWorldFloatData;
    class btRigidBodyFloatData;
    class btRigidBodyDoubleData;
    class btConstraintInfo1;
    class btTypedConstraintFloatData;
    class btTypedConstraintData;
    class btTypedConstraintDoubleData;
    class btPoint2PointConstraintFloatData;
    class btPoint2PointConstraintDoubleData2;
    class btPoint2PointConstraintDoubleData;
    class btHingeConstraintDoubleData;
    class btHingeConstraintFloatData;
    class btHingeConstraintDoubleData2;
    class btConeTwistConstraintDoubleData;
    class btConeTwistConstraintData;
    class btGeneric6DofConstraintData;
    class btGeneric6DofConstraintDoubleData2;
    class btGeneric6DofSpringConstraintData;
    class btGeneric6DofSpringConstraintDoubleData2;
    class btGeneric6DofSpring2ConstraintData;
    class btGeneric6DofSpring2ConstraintDoubleData2;
    class btSliderConstraintData;
    class btSliderConstraintDoubleData;
    class btGearConstraintFloatData;
    class btGearConstraintDoubleData;
    class SoftBodyMaterialData;
    class SoftBodyNodeData;
    class SoftBodyLinkData;
    class SoftBodyFaceData;
    class SoftBodyTetraData;
    class SoftRigidAnchorData;
    class SoftBodyConfigData;
    class SoftBodyPoseData;
    class SoftBodyClusterData;
    class btSoftBodyJointData;
    class btSoftBodyFloatData;
    class btMultiBodyLinkDoubleData;
    class btMultiBodyLinkFloatData;
    class btMultiBodyDoubleData;
    class btMultiBodyFloatData;
// -------------------------------------------------- //
    class PointerArray
    {
    public:
        int m_size;
        int m_capacity;
        void *m_data;
    };


// -------------------------------------------------- //
    class btPhysicsSystem
    {
    public:
        PointerArray m_collisionShapes;
        PointerArray m_collisionObjects;
        PointerArray m_constraints;
    };


// -------------------------------------------------- //
    class ListBase
    {
    public:
        void *first;
        void *last;
    };


// -------------------------------------------------- //
    class btVector3FloatData
    {
    public:
        float m_floats[4];
    };


// -------------------------------------------------- //
    class btVector3DoubleData
    {
    public:
        double m_floats[4];
    };


// -------------------------------------------------- //
    class btQuaternionFloatData
    {
    public:
        float m_floats[4];
    };


// -------------------------------------------------- //
    class btQuaternionDoubleData
    {
    public:
        double m_floats[4];
    };


// -------------------------------------------------- //
    class btMatrix3x3FloatData
    {
    public:
        btVector3FloatData m_el[3];
    };


// -------------------------------------------------- //
    class btMatrix3x3DoubleData
    {
    public:
        btVector3DoubleData m_el[3];
    };


// -------------------------------------------------- //
    class btTransformFloatData
    {
    public:
        btMatrix3x3FloatData m_basis;
        btVector3FloatData m_origin;
    };


// -------------------------------------------------- //
    class btTransformDoubleData
    {
    public:
        btMatrix3x3DoubleData m_basis;
        btVector3DoubleData m_origin;
    };


// -------------------------------------------------- //
    class btBvhSubtreeInfoData
    {
    public:
        int m_rootNodeIndex;
        int m_subtreeSize;
        short m_quantizedAabbMin[3];
        short m_quantizedAabbMax[3];
    };


// -------------------------------------------------- //
    class btOptimizedBvhNodeFloatData
    {
    public:
        btVector3FloatData m_aabbMinOrg;
        btVector3FloatData m_aabbMaxOrg;
        int m_escapeIndex;
        int m_subPart;
        int m_triangleIndex;
        char m_pad[4];
    };


// -------------------------------------------------- //
    class btOptimizedBvhNodeDoubleData
    {
    public:
        btVector3DoubleData m_aabbMinOrg;
        btVector3DoubleData m_aabbMaxOrg;
        int m_escapeIndex;
        int m_subPart;
        int m_triangleIndex;
        char m_pad[4];
    };


// -------------------------------------------------- //
    class btQuantizedBvhNodeData
    {
    public:
        short m_quantizedAabbMin[3];
        short m_quantizedAabbMax[3];
        int m_escapeIndexOrTriangleIndex;
    };


// -------------------------------------------------- //
    class btQuantizedBvhFloatData
    {
    public:
        btVector3FloatData m_bvhAabbMin;
        btVector3FloatData m_bvhAabbMax;
        btVector3FloatData m_bvhQuantization;
        int m_curNodeIndex;
        int m_useQuantization;
        int m_numContiguousLeafNodes;
        int m_numQuantizedContiguousNodes;
        btOptimizedBvhNodeFloatData *m_contiguousNodesPtr;
        btQuantizedBvhNodeData *m_quantizedContiguousNodesPtr;
        btBvhSubtreeInfoData *m_subTreeInfoPtr;
        int m_traversalMode;
        int m_numSubtreeHeaders;
    };


// -------------------------------------------------- //
    class btQuantizedBvhDoubleData
    {
    public:
        btVector3DoubleData m_bvhAabbMin;
        btVector3DoubleData m_bvhAabbMax;
        btVector3DoubleData m_bvhQuantization;
        int m_curNodeIndex;
        int m_useQuantization;
        int m_numContiguousLeafNodes;
        int m_numQuantizedContiguousNodes;
        btOptimizedBvhNodeDoubleData *m_contiguousNodesPtr;
        btQuantizedBvhNodeData *m_quantizedContiguousNodesPtr;
        int m_traversalMode;
        int m_numSubtreeHeaders;
        btBvhSubtreeInfoData *m_subTreeInfoPtr;
    };


// -------------------------------------------------- //
    class btCollisionShapeData
    {
    public:
        char *m_name;
        int m_shapeType;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btStaticPlaneShapeData
    {
    public:
        btCollisionShapeData m_collisionShapeData;
        btVector3FloatData m_localScaling;
        btVector3FloatData m_planeNormal;
        float m_planeConstant;
        char m_pad[4];
    };


// -------------------------------------------------- //
    class btConvexInternalShapeData
    {
    public:
        btCollisionShapeData m_collisionShapeData;
        btVector3FloatData m_localScaling;
        btVector3FloatData m_implicitShapeDimensions;
        float m_collisionMargin;
        int m_padding;
    };


// -------------------------------------------------- //
    class btPositionAndRadius
    {
    public:
        btVector3FloatData m_pos;
        float m_radius;
    };


// -------------------------------------------------- //
    class btMultiSphereShapeData
    {
    public:
        btConvexInternalShapeData m_convexInternalShapeData;
        btPositionAndRadius *m_localPositionArrayPtr;
        int m_localPositionArraySize;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btIntIndexData
    {
    public:
        int m_value;
    };


// -------------------------------------------------- //
    class btShortIntIndexData
    {
    public:
        short m_value;
        char m_pad[2];
    };


// -------------------------------------------------- //
    class btShortIntIndexTripletData
    {
    public:
        short m_values[3];
        char m_pad[2];
    };


// -------------------------------------------------- //
    class btCharIndexTripletData
    {
    public:
        char m_values[3];
        char m_pad;
    };


// -------------------------------------------------- //
    class btMeshPartData
    {
    public:
        btVector3FloatData *m_vertices3f;
        btVector3DoubleData *m_vertices3d;
        btIntIndexData *m_indices32;
        btShortIntIndexTripletData *m_3indices16;
        btCharIndexTripletData *m_3indices8;
        btShortIntIndexData *m_indices16;
        int m_numTriangles;
        int m_numVertices;
    };


// -------------------------------------------------- //
    class btStridingMeshInterfaceData
    {
    public:
        btMeshPartData *m_meshPartsPtr;
        btVector3FloatData m_scaling;
        int m_numMeshParts;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btTriangleMeshShapeData
    {
    public:
        btCollisionShapeData m_collisionShapeData;
        btStridingMeshInterfaceData m_meshInterface;
        btQuantizedBvhFloatData *m_quantizedFloatBvh;
        btQuantizedBvhDoubleData *m_quantizedDoubleBvh;
        btTriangleInfoMapData *m_triangleInfoMap;
        float m_collisionMargin;
        char m_pad3[4];
    };


// -------------------------------------------------- //
    class btScaledTriangleMeshShapeData
    {
    public:
        btTriangleMeshShapeData m_trimeshShapeData;
        btVector3FloatData m_localScaling;
    };


// -------------------------------------------------- //
    class btCompoundShapeChildData
    {
    public:
        btTransformFloatData m_transform;
        btCollisionShapeData *m_childShape;
        int m_childShapeType;
        float m_childMargin;
    };


// -------------------------------------------------- //
    class btCompoundShapeData
    {
    public:
        btCollisionShapeData m_collisionShapeData;
        btCompoundShapeChildData *m_childShapePtr;
        int m_numChildShapes;
        float m_collisionMargin;
    };


// -------------------------------------------------- //
    class btCylinderShapeData
    {
    public:
        btConvexInternalShapeData m_convexInternalShapeData;
        int m_upAxis;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btConeShapeData
    {
    public:
        btConvexInternalShapeData m_convexInternalShapeData;
        int m_upIndex;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btCapsuleShapeData
    {
    public:
        btConvexInternalShapeData m_convexInternalShapeData;
        int m_upAxis;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btTriangleInfoData
    {
    public:
        int m_flags;
        float m_edgeV0V1Angle;
        float m_edgeV1V2Angle;
        float m_edgeV2V0Angle;
    };


// -------------------------------------------------- //
    class btTriangleInfoMapData
    {
    public:
        int *m_hashTablePtr;
        int *m_nextPtr;
        btTriangleInfoData *m_valueArrayPtr;
        int *m_keyArrayPtr;
        float m_convexEpsilon;
        float m_planarEpsilon;
        float m_equalVertexThreshold;
        float m_edgeDistanceThreshold;
        float m_zeroAreaThreshold;
        int m_nextSize;
        int m_hashTableSize;
        int m_numValues;
        int m_numKeys;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btGImpactMeshShapeData
    {
    public:
        btCollisionShapeData m_collisionShapeData;
        btStridingMeshInterfaceData m_meshInterface;
        btVector3FloatData m_localScaling;
        float m_collisionMargin;
        int m_gimpactSubType;
    };


// -------------------------------------------------- //
    class btConvexHullShapeData
    {
    public:
        btConvexInternalShapeData m_convexInternalShapeData;
        btVector3FloatData *m_unscaledPointsFloatPtr;
        btVector3DoubleData *m_unscaledPointsDoublePtr;
        int m_numUnscaledPoints;
        char m_padding3[4];
    };


// -------------------------------------------------- //
    class btCollisionObjectDoubleData
    {
    public:
        void *m_broadphaseHandle;
        void *m_collisionShape;
        btCollisionShapeData *m_rootCollisionShape;
        char *m_name;
        btTransformDoubleData m_worldTransform;
        btTransformDoubleData m_interpolationWorldTransform;
        btVector3DoubleData m_interpolationLinearVelocity;
        btVector3DoubleData m_interpolationAngularVelocity;
        btVector3DoubleData m_anisotropicFriction;
        double m_contactProcessingThreshold;
        double m_deactivationTime;
        double m_friction;
        double m_rollingFriction;
        double m_contactDamping;
        double m_contactStiffness;
        double m_restitution;
        double m_hitFraction;
        double m_ccdSweptSphereRadius;
        double m_ccdMotionThreshold;
        int m_hasAnisotropicFriction;
        int m_collisionFlags;
        int m_islandTag1;
        int m_companionId;
        int m_activationState1;
        int m_internalType;
        int m_checkCollideWith;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btCollisionObjectFloatData
    {
    public:
        void *m_broadphaseHandle;
        void *m_collisionShape;
        btCollisionShapeData *m_rootCollisionShape;
        char *m_name;
        btTransformFloatData m_worldTransform;
        btTransformFloatData m_interpolationWorldTransform;
        btVector3FloatData m_interpolationLinearVelocity;
        btVector3FloatData m_interpolationAngularVelocity;
        btVector3FloatData m_anisotropicFriction;
        float m_contactProcessingThreshold;
        float m_deactivationTime;
        float m_friction;
        float m_rollingFriction;
        float m_contactDamping;
        float m_contactStiffness;
        float m_restitution;
        float m_hitFraction;
        float m_ccdSweptSphereRadius;
        float m_ccdMotionThreshold;
        int m_hasAnisotropicFriction;
        int m_collisionFlags;
        int m_islandTag1;
        int m_companionId;
        int m_activationState1;
        int m_internalType;
        int m_checkCollideWith;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btContactSolverInfoDoubleData
    {
    public:
        double m_tau;
        double m_damping;
        double m_friction;
        double m_timeStep;
        double m_restitution;
        double m_maxErrorReduction;
        double m_sor;
        double m_erp;
        double m_erp2;
        double m_globalCfm;
        double m_splitImpulsePenetrationThreshold;
        double m_splitImpulseTurnErp;
        double m_linearSlop;
        double m_warmstartingFactor;
        double m_maxGyroscopicForce;
        double m_singleAxisRollingFrictionThreshold;
        int m_numIterations;
        int m_solverMode;
        int m_restingContactRestitutionThreshold;
        int m_minimumSolverBatchSize;
        int m_splitImpulse;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btContactSolverInfoFloatData
    {
    public:
        float m_tau;
        float m_damping;
        float m_friction;
        float m_timeStep;
        float m_restitution;
        float m_maxErrorReduction;
        float m_sor;
        float m_erp;
        float m_erp2;
        float m_globalCfm;
        float m_splitImpulsePenetrationThreshold;
        float m_splitImpulseTurnErp;
        float m_linearSlop;
        float m_warmstartingFactor;
        float m_maxGyroscopicForce;
        float m_singleAxisRollingFrictionThreshold;
        int m_numIterations;
        int m_solverMode;
        int m_restingContactRestitutionThreshold;
        int m_minimumSolverBatchSize;
        int m_splitImpulse;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btDynamicsWorldDoubleData
    {
    public:
        btContactSolverInfoDoubleData m_solverInfo;
        btVector3DoubleData m_gravity;
    };


// -------------------------------------------------- //
    class btDynamicsWorldFloatData
    {
    public:
        btContactSolverInfoFloatData m_solverInfo;
        btVector3FloatData m_gravity;
    };


// -------------------------------------------------- //
    class btRigidBodyFloatData
    {
    public:
        btCollisionObjectFloatData m_collisionObjectData;
        btMatrix3x3FloatData m_invInertiaTensorWorld;
        btVector3FloatData m_linearVelocity;
        btVector3FloatData m_angularVelocity;
        btVector3FloatData m_angularFactor;
        btVector3FloatData m_linearFactor;
        btVector3FloatData m_gravity;
        btVector3FloatData m_gravity_acceleration;
        btVector3FloatData m_invInertiaLocal;
        btVector3FloatData m_totalForce;
        btVector3FloatData m_totalTorque;
        float m_inverseMass;
        float m_linearDamping;
        float m_angularDamping;
        float m_additionalDampingFactor;
        float m_additionalLinearDampingThresholdSqr;
        float m_additionalAngularDampingThresholdSqr;
        float m_additionalAngularDampingFactor;
        float m_linearSleepingThreshold;
        float m_angularSleepingThreshold;
        int m_additionalDamping;
    };


// -------------------------------------------------- //
    class btRigidBodyDoubleData
    {
    public:
        btCollisionObjectDoubleData m_collisionObjectData;
        btMatrix3x3DoubleData m_invInertiaTensorWorld;
        btVector3DoubleData m_linearVelocity;
        btVector3DoubleData m_angularVelocity;
        btVector3DoubleData m_angularFactor;
        btVector3DoubleData m_linearFactor;
        btVector3DoubleData m_gravity;
        btVector3DoubleData m_gravity_acceleration;
        btVector3DoubleData m_invInertiaLocal;
        btVector3DoubleData m_totalForce;
        btVector3DoubleData m_totalTorque;
        double m_inverseMass;
        double m_linearDamping;
        double m_angularDamping;
        double m_additionalDampingFactor;
        double m_additionalLinearDampingThresholdSqr;
        double m_additionalAngularDampingThresholdSqr;
        double m_additionalAngularDampingFactor;
        double m_linearSleepingThreshold;
        double m_angularSleepingThreshold;
        int m_additionalDamping;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btConstraintInfo1
    {
    public:
        int m_numConstraintRows;
        int nub;
    };


// -------------------------------------------------- //
    class btTypedConstraintFloatData
    {
    public:
        btRigidBodyFloatData *m_rbA;
        btRigidBodyFloatData *m_rbB;
        char *m_name;
        int m_objectType;
        int m_userConstraintType;
        int m_userConstraintId;
        int m_needsFeedback;
        float m_appliedImpulse;
        float m_dbgDrawSize;
        int m_disableCollisionsBetweenLinkedBodies;
        int m_overrideNumSolverIterations;
        float m_breakingImpulseThreshold;
        int m_isEnabled;
    };


// -------------------------------------------------- //
    class btTypedConstraintData
    {
    public:
        bInvalidHandle *m_rbA;
        bInvalidHandle *m_rbB;
        char *m_name;
        int m_objectType;
        int m_userConstraintType;
        int m_userConstraintId;
        int m_needsFeedback;
        float m_appliedImpulse;
        float m_dbgDrawSize;
        int m_disableCollisionsBetweenLinkedBodies;
        int m_overrideNumSolverIterations;
        float m_breakingImpulseThreshold;
        int m_isEnabled;
    };


// -------------------------------------------------- //
    class btTypedConstraintDoubleData
    {
    public:
        btRigidBodyDoubleData *m_rbA;
        btRigidBodyDoubleData *m_rbB;
        char *m_name;
        int m_objectType;
        int m_userConstraintType;
        int m_userConstraintId;
        int m_needsFeedback;
        double m_appliedImpulse;
        double m_dbgDrawSize;
        int m_disableCollisionsBetweenLinkedBodies;
        int m_overrideNumSolverIterations;
        double m_breakingImpulseThreshold;
        int m_isEnabled;
        char padding[4];
    };


// -------------------------------------------------- //
    class btPoint2PointConstraintFloatData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btVector3FloatData m_pivotInA;
        btVector3FloatData m_pivotInB;
    };


// -------------------------------------------------- //
    class btPoint2PointConstraintDoubleData2
    {
    public:
        btTypedConstraintDoubleData m_typeConstraintData;
        btVector3DoubleData m_pivotInA;
        btVector3DoubleData m_pivotInB;
    };


// -------------------------------------------------- //
    class btPoint2PointConstraintDoubleData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btVector3DoubleData m_pivotInA;
        btVector3DoubleData m_pivotInB;
    };


// -------------------------------------------------- //
    class btHingeConstraintDoubleData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btTransformDoubleData m_rbAFrame;
        btTransformDoubleData m_rbBFrame;
        int m_useReferenceFrameA;
        int m_angularOnly;
        int m_enableAngularMotor;
        float m_motorTargetVelocity;
        float m_maxMotorImpulse;
        float m_lowerLimit;
        float m_upperLimit;
        float m_limitSoftness;
        float m_biasFactor;
        float m_relaxationFactor;
    };


// -------------------------------------------------- //
    class btHingeConstraintFloatData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btTransformFloatData m_rbAFrame;
        btTransformFloatData m_rbBFrame;
        int m_useReferenceFrameA;
        int m_angularOnly;
        int m_enableAngularMotor;
        float m_motorTargetVelocity;
        float m_maxMotorImpulse;
        float m_lowerLimit;
        float m_upperLimit;
        float m_limitSoftness;
        float m_biasFactor;
        float m_relaxationFactor;
    };


// -------------------------------------------------- //
    class btHingeConstraintDoubleData2
    {
    public:
        btTypedConstraintDoubleData m_typeConstraintData;
        btTransformDoubleData m_rbAFrame;
        btTransformDoubleData m_rbBFrame;
        int m_useReferenceFrameA;
        int m_angularOnly;
        int m_enableAngularMotor;
        double m_motorTargetVelocity;
        double m_maxMotorImpulse;
        double m_lowerLimit;
        double m_upperLimit;
        double m_limitSoftness;
        double m_biasFactor;
        double m_relaxationFactor;
        char m_padding1[4];
    };


// -------------------------------------------------- //
    class btConeTwistConstraintDoubleData
    {
    public:
        btTypedConstraintDoubleData m_typeConstraintData;
        btTransformDoubleData m_rbAFrame;
        btTransformDoubleData m_rbBFrame;
        double m_swingSpan1;
        double m_swingSpan2;
        double m_twistSpan;
        double m_limitSoftness;
        double m_biasFactor;
        double m_relaxationFactor;
        double m_damping;
    };


// -------------------------------------------------- //
    class btConeTwistConstraintData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btTransformFloatData m_rbAFrame;
        btTransformFloatData m_rbBFrame;
        float m_swingSpan1;
        float m_swingSpan2;
        float m_twistSpan;
        float m_limitSoftness;
        float m_biasFactor;
        float m_relaxationFactor;
        float m_damping;
        char m_pad[4];
    };


// -------------------------------------------------- //
    class btGeneric6DofConstraintData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btTransformFloatData m_rbAFrame;
        btTransformFloatData m_rbBFrame;
        btVector3FloatData m_linearUpperLimit;
        btVector3FloatData m_linearLowerLimit;
        btVector3FloatData m_angularUpperLimit;
        btVector3FloatData m_angularLowerLimit;
        int m_useLinearReferenceFrameA;
        int m_useOffsetForConstraintFrame;
    };


// -------------------------------------------------- //
    class btGeneric6DofConstraintDoubleData2
    {
    public:
        btTypedConstraintDoubleData m_typeConstraintData;
        btTransformDoubleData m_rbAFrame;
        btTransformDoubleData m_rbBFrame;
        btVector3DoubleData m_linearUpperLimit;
        btVector3DoubleData m_linearLowerLimit;
        btVector3DoubleData m_angularUpperLimit;
        btVector3DoubleData m_angularLowerLimit;
        int m_useLinearReferenceFrameA;
        int m_useOffsetForConstraintFrame;
    };


// -------------------------------------------------- //
    class btGeneric6DofSpringConstraintData
    {
    public:
        btGeneric6DofConstraintData m_6dofData;
        int m_springEnabled[6];
        float m_equilibriumPoint[6];
        float m_springStiffness[6];
        float m_springDamping[6];
    };


// -------------------------------------------------- //
    class btGeneric6DofSpringConstraintDoubleData2
    {
    public:
        btGeneric6DofConstraintDoubleData2 m_6dofData;
        int m_springEnabled[6];
        double m_equilibriumPoint[6];
        double m_springStiffness[6];
        double m_springDamping[6];
    };


// -------------------------------------------------- //
    class btGeneric6DofSpring2ConstraintData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btTransformFloatData m_rbAFrame;
        btTransformFloatData m_rbBFrame;
        btVector3FloatData m_linearUpperLimit;
        btVector3FloatData m_linearLowerLimit;
        btVector3FloatData m_linearBounce;
        btVector3FloatData m_linearStopERP;
        btVector3FloatData m_linearStopCFM;
        btVector3FloatData m_linearMotorERP;
        btVector3FloatData m_linearMotorCFM;
        btVector3FloatData m_linearTargetVelocity;
        btVector3FloatData m_linearMaxMotorForce;
        btVector3FloatData m_linearServoTarget;
        btVector3FloatData m_linearSpringStiffness;
        btVector3FloatData m_linearSpringDamping;
        btVector3FloatData m_linearEquilibriumPoint;
        char m_linearEnableMotor[4];
        char m_linearServoMotor[4];
        char m_linearEnableSpring[4];
        char m_linearSpringStiffnessLimited[4];
        char m_linearSpringDampingLimited[4];
        char m_padding1[4];
        btVector3FloatData m_angularUpperLimit;
        btVector3FloatData m_angularLowerLimit;
        btVector3FloatData m_angularBounce;
        btVector3FloatData m_angularStopERP;
        btVector3FloatData m_angularStopCFM;
        btVector3FloatData m_angularMotorERP;
        btVector3FloatData m_angularMotorCFM;
        btVector3FloatData m_angularTargetVelocity;
        btVector3FloatData m_angularMaxMotorForce;
        btVector3FloatData m_angularServoTarget;
        btVector3FloatData m_angularSpringStiffness;
        btVector3FloatData m_angularSpringDamping;
        btVector3FloatData m_angularEquilibriumPoint;
        char m_angularEnableMotor[4];
        char m_angularServoMotor[4];
        char m_angularEnableSpring[4];
        char m_angularSpringStiffnessLimited[4];
        char m_angularSpringDampingLimited[4];
        int m_rotateOrder;
    };


// -------------------------------------------------- //
    class btGeneric6DofSpring2ConstraintDoubleData2
    {
    public:
        btTypedConstraintDoubleData m_typeConstraintData;
        btTransformDoubleData m_rbAFrame;
        btTransformDoubleData m_rbBFrame;
        btVector3DoubleData m_linearUpperLimit;
        btVector3DoubleData m_linearLowerLimit;
        btVector3DoubleData m_linearBounce;
        btVector3DoubleData m_linearStopERP;
        btVector3DoubleData m_linearStopCFM;
        btVector3DoubleData m_linearMotorERP;
        btVector3DoubleData m_linearMotorCFM;
        btVector3DoubleData m_linearTargetVelocity;
        btVector3DoubleData m_linearMaxMotorForce;
        btVector3DoubleData m_linearServoTarget;
        btVector3DoubleData m_linearSpringStiffness;
        btVector3DoubleData m_linearSpringDamping;
        btVector3DoubleData m_linearEquilibriumPoint;
        char m_linearEnableMotor[4];
        char m_linearServoMotor[4];
        char m_linearEnableSpring[4];
        char m_linearSpringStiffnessLimited[4];
        char m_linearSpringDampingLimited[4];
        char m_padding1[4];
        btVector3DoubleData m_angularUpperLimit;
        btVector3DoubleData m_angularLowerLimit;
        btVector3DoubleData m_angularBounce;
        btVector3DoubleData m_angularStopERP;
        btVector3DoubleData m_angularStopCFM;
        btVector3DoubleData m_angularMotorERP;
        btVector3DoubleData m_angularMotorCFM;
        btVector3DoubleData m_angularTargetVelocity;
        btVector3DoubleData m_angularMaxMotorForce;
        btVector3DoubleData m_angularServoTarget;
        btVector3DoubleData m_angularSpringStiffness;
        btVector3DoubleData m_angularSpringDamping;
        btVector3DoubleData m_angularEquilibriumPoint;
        char m_angularEnableMotor[4];
        char m_angularServoMotor[4];
        char m_angularEnableSpring[4];
        char m_angularSpringStiffnessLimited[4];
        char m_angularSpringDampingLimited[4];
        int m_rotateOrder;
    };


// -------------------------------------------------- //
    class btSliderConstraintData
    {
    public:
        btTypedConstraintData m_typeConstraintData;
        btTransformFloatData m_rbAFrame;
        btTransformFloatData m_rbBFrame;
        float m_linearUpperLimit;
        float m_linearLowerLimit;
        float m_angularUpperLimit;
        float m_angularLowerLimit;
        int m_useLinearReferenceFrameA;
        int m_useOffsetForConstraintFrame;
    };


// -------------------------------------------------- //
    class btSliderConstraintDoubleData
    {
    public:
        btTypedConstraintDoubleData m_typeConstraintData;
        btTransformDoubleData m_rbAFrame;
        btTransformDoubleData m_rbBFrame;
        double m_linearUpperLimit;
        double m_linearLowerLimit;
        double m_angularUpperLimit;
        double m_angularLowerLimit;
        int m_useLinearReferenceFrameA;
        int m_useOffsetForConstraintFrame;
    };


// -------------------------------------------------- //
    class btGearConstraintFloatData
    {
    public:
        btTypedConstraintFloatData m_typeConstraintData;
        btVector3FloatData m_axisInA;
        btVector3FloatData m_axisInB;
        float m_ratio;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btGearConstraintDoubleData
    {
    public:
        btTypedConstraintDoubleData m_typeConstraintData;
        btVector3DoubleData m_axisInA;
        btVector3DoubleData m_axisInB;
        double m_ratio;
    };


// -------------------------------------------------- //
    class SoftBodyMaterialData
    {
    public:
        float m_linearStiffness;
        float m_angularStiffness;
        float m_volumeStiffness;
        int m_flags;
    };


// -------------------------------------------------- //
    class SoftBodyNodeData
    {
    public:
        SoftBodyMaterialData *m_material;
        btVector3FloatData m_position;
        btVector3FloatData m_previousPosition;
        btVector3FloatData m_velocity;
        btVector3FloatData m_accumulatedForce;
        btVector3FloatData m_normal;
        float m_inverseMass;
        float m_area;
        int m_attach;
        int m_pad;
    };


// -------------------------------------------------- //
    class SoftBodyLinkData
    {
    public:
        SoftBodyMaterialData *m_material;
        int m_nodeIndices[2];
        float m_restLength;
        int m_bbending;
    };


// -------------------------------------------------- //
    class SoftBodyFaceData
    {
    public:
        btVector3FloatData m_normal;
        SoftBodyMaterialData *m_material;
        int m_nodeIndices[3];
        float m_restArea;
    };


// -------------------------------------------------- //
    class SoftBodyTetraData
    {
    public:
        btVector3FloatData m_c0[4];
        SoftBodyMaterialData *m_material;
        int m_nodeIndices[4];
        float m_restVolume;
        float m_c1;
        float m_c2;
        int m_pad;
    };


// -------------------------------------------------- //
    class SoftRigidAnchorData
    {
    public:
        btMatrix3x3FloatData m_c0;
        btVector3FloatData m_c1;
        btVector3FloatData m_localFrame;
        bInvalidHandle *m_rigidBody;
        int m_nodeIndex;
        float m_c2;
    };


// -------------------------------------------------- //
    class SoftBodyConfigData
    {
    public:
        int m_aeroModel;
        float m_baumgarte;
        float m_damping;
        float m_drag;
        float m_lift;
        float m_pressure;
        float m_volume;
        float m_dynamicFriction;
        float m_poseMatch;
        float m_rigidContactHardness;
        float m_kineticContactHardness;
        float m_softContactHardness;
        float m_anchorHardness;
        float m_softRigidClusterHardness;
        float m_softKineticClusterHardness;
        float m_softSoftClusterHardness;
        float m_softRigidClusterImpulseSplit;
        float m_softKineticClusterImpulseSplit;
        float m_softSoftClusterImpulseSplit;
        float m_maxVolume;
        float m_timeScale;
        int m_velocityIterations;
        int m_positionIterations;
        int m_driftIterations;
        int m_clusterIterations;
        int m_collisionFlags;
    };


// -------------------------------------------------- //
    class SoftBodyPoseData
    {
    public:
        btMatrix3x3FloatData m_rot;
        btMatrix3x3FloatData m_scale;
        btMatrix3x3FloatData m_aqq;
        btVector3FloatData m_com;
        btVector3FloatData *m_positions;
        float *m_weights;
        int m_numPositions;
        int m_numWeigts;
        int m_bvolume;
        int m_bframe;
        float m_restVolume;
        int m_pad;
    };


// -------------------------------------------------- //
    class SoftBodyClusterData
    {
    public:
        btTransformFloatData m_framexform;
        btMatrix3x3FloatData m_locii;
        btMatrix3x3FloatData m_invwi;
        btVector3FloatData m_com;
        btVector3FloatData m_vimpulses[2];
        btVector3FloatData m_dimpulses[2];
        btVector3FloatData m_lv;
        btVector3FloatData m_av;
        btVector3FloatData *m_framerefs;
        int *m_nodeIndices;
        float *m_masses;
        int m_numFrameRefs;
        int m_numNodes;
        int m_numMasses;
        float m_idmass;
        float m_imass;
        int m_nvimpulses;
        int m_ndimpulses;
        float m_ndamping;
        float m_ldamping;
        float m_adamping;
        float m_matching;
        float m_maxSelfCollisionImpulse;
        float m_selfCollisionImpulseFactor;
        int m_containsAnchor;
        int m_collide;
        int m_clusterIndex;
    };


// -------------------------------------------------- //
    class btSoftBodyJointData
    {
    public:
        void *m_bodyA;
        void *m_bodyB;
        btVector3FloatData m_refs[2];
        float m_cfm;
        float m_erp;
        float m_split;
        int m_delete;
        btVector3FloatData m_relPosition[2];
        int m_bodyAtype;
        int m_bodyBtype;
        int m_jointType;
        int m_pad;
    };


// -------------------------------------------------- //
    class btSoftBodyFloatData
    {
    public:
        btCollisionObjectFloatData m_collisionObjectData;
        SoftBodyPoseData *m_pose;
        SoftBodyMaterialData **m_materials;
        SoftBodyNodeData *m_nodes;
        SoftBodyLinkData *m_links;
        SoftBodyFaceData *m_faces;
        SoftBodyTetraData *m_tetrahedra;
        SoftRigidAnchorData *m_anchors;
        SoftBodyClusterData *m_clusters;
        btSoftBodyJointData *m_joints;
        int m_numMaterials;
        int m_numNodes;
        int m_numLinks;
        int m_numFaces;
        int m_numTetrahedra;
        int m_numAnchors;
        int m_numClusters;
        int m_numJoints;
        SoftBodyConfigData m_config;
    };


// -------------------------------------------------- //
    class btMultiBodyLinkDoubleData
    {
    public:
        btQuaternionDoubleData m_zeroRotParentToThis;
        btVector3DoubleData m_parentComToThisComOffset;
        btVector3DoubleData m_thisPivotToThisComOffset;
        btVector3DoubleData m_jointAxisTop[6];
        btVector3DoubleData m_jointAxisBottom[6];
        btVector3DoubleData m_linkInertia;
        double m_linkMass;
        int m_parentIndex;
        int m_jointType;
        int m_dofCount;
        int m_posVarCount;
        double m_jointPos[7];
        double m_jointVel[6];
        double m_jointTorque[6];
        double m_jointDamping;
        double m_jointFriction;
        double m_jointLowerLimit;
        double m_jointUpperLimit;
        double m_jointMaxForce;
        double m_jointMaxVelocity;
        char *m_linkName;
        char *m_jointName;
        btCollisionObjectDoubleData *m_linkCollider;
        char *m_paddingPtr;
    };


// -------------------------------------------------- //
    class btMultiBodyLinkFloatData
    {
    public:
        btQuaternionFloatData m_zeroRotParentToThis;
        btVector3FloatData m_parentComToThisComOffset;
        btVector3FloatData m_thisPivotToThisComOffset;
        btVector3FloatData m_jointAxisTop[6];
        btVector3FloatData m_jointAxisBottom[6];
        btVector3FloatData m_linkInertia;
        int m_dofCount;
        float m_linkMass;
        int m_parentIndex;
        int m_jointType;
        float m_jointPos[7];
        float m_jointVel[6];
        float m_jointTorque[6];
        int m_posVarCount;
        float m_jointDamping;
        float m_jointFriction;
        float m_jointLowerLimit;
        float m_jointUpperLimit;
        float m_jointMaxForce;
        float m_jointMaxVelocity;
        char *m_linkName;
        char *m_jointName;
        btCollisionObjectFloatData *m_linkCollider;
        char *m_paddingPtr;
    };


// -------------------------------------------------- //
    class btMultiBodyDoubleData
    {
    public:
        btTransformDoubleData m_baseWorldTransform;
        btVector3DoubleData m_baseInertia;
        double m_baseMass;
        char *m_baseName;
        btMultiBodyLinkDoubleData *m_links;
        btCollisionObjectDoubleData *m_baseCollider;
        char *m_paddingPtr;
        int m_numLinks;
        char m_padding[4];
    };


// -------------------------------------------------- //
    class btMultiBodyFloatData
    {
    public:
        char *m_baseName;
        btMultiBodyLinkFloatData *m_links;
        btCollisionObjectFloatData *m_baseCollider;
        btTransformFloatData m_baseWorldTransform;
        btVector3FloatData m_baseInertia;
        float m_baseMass;
        int m_numLinks;
    };


}
#endif//__BULLET_H__