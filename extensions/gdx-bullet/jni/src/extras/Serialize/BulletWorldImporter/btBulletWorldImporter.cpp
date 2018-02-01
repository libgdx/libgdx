/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2012 Erwin Coumans  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/


#include "btBulletWorldImporter.h"
#include "../BulletFileLoader/btBulletFile.h"

#include "btBulletDynamicsCommon.h"
#ifndef USE_GIMPACT
#include "BulletCollision/Gimpact/btGImpactShape.h"
#endif


//#define USE_INTERNAL_EDGE_UTILITY
#ifdef USE_INTERNAL_EDGE_UTILITY
#include "BulletCollision/CollisionDispatch/btInternalEdgeUtility.h"
#endif //USE_INTERNAL_EDGE_UTILITY

btBulletWorldImporter::btBulletWorldImporter(btDynamicsWorld* world)
	:btWorldImporter(world)
{
}

btBulletWorldImporter::~btBulletWorldImporter()
{
}


bool	btBulletWorldImporter::loadFile( const char* fileName, const char* preSwapFilenameOut)
{
	bParse::btBulletFile* bulletFile2 = new bParse::btBulletFile(fileName);

	
	bool result = loadFileFromMemory(bulletFile2);
	//now you could save the file in 'native' format using
	//bulletFile2->writeFile("native.bullet");
	if (result)
	{
		if (preSwapFilenameOut)
		{
			bulletFile2->preSwap();
			bulletFile2->writeFile(preSwapFilenameOut);
		}
		
	}
	delete bulletFile2;
	
	return result;

}



bool	btBulletWorldImporter::loadFileFromMemory( char* memoryBuffer, int len)
{
	bParse::btBulletFile* bulletFile2 = new bParse::btBulletFile(memoryBuffer,len);

	bool result = loadFileFromMemory(bulletFile2);

	delete bulletFile2;

	return result;
}




bool	btBulletWorldImporter::loadFileFromMemory(  bParse::btBulletFile* bulletFile2)
{
	bool ok = (bulletFile2->getFlags()& bParse::FD_OK)!=0;
	
	if (ok)
		bulletFile2->parse(m_verboseMode);
	else 
		return false;
	
	if (m_verboseMode & bParse::FD_VERBOSE_DUMP_CHUNKS)
	{
		bulletFile2->dumpChunks(bulletFile2->getFileDNA());
	}

	return convertAllObjects(bulletFile2);

}

bool	btBulletWorldImporter::convertAllObjects(  bParse::btBulletFile* bulletFile2)
{

	m_shapeMap.clear();
	m_bodyMap.clear();

	int i;
	
	for (i=0;i<bulletFile2->m_bvhs.size();i++)
	{
		btOptimizedBvh* bvh = createOptimizedBvh();

		if (bulletFile2->getFlags() & bParse::FD_DOUBLE_PRECISION)
		{
			btQuantizedBvhDoubleData* bvhData = (btQuantizedBvhDoubleData*)bulletFile2->m_bvhs[i];
			bvh->deSerializeDouble(*bvhData);
		} else
		{
			btQuantizedBvhFloatData* bvhData = (btQuantizedBvhFloatData*)bulletFile2->m_bvhs[i];
			bvh->deSerializeFloat(*bvhData);
		}
		m_bvhMap.insert(bulletFile2->m_bvhs[i],bvh);
	}



	

	for (i=0;i<bulletFile2->m_collisionShapes.size();i++)
	{
		btCollisionShapeData* shapeData = (btCollisionShapeData*)bulletFile2->m_collisionShapes[i];
		btCollisionShape* shape = convertCollisionShape(shapeData);
		if (shape)
		{
	//		printf("shapeMap.insert(%x,%x)\n",shapeData,shape);
			m_shapeMap.insert(shapeData,shape);
		}

		if (shape&& shapeData->m_name)
		{
			char* newname = duplicateName(shapeData->m_name);
			m_objectNameMap.insert(shape,newname);
			m_nameShapeMap.insert(newname,shape);
		}
	}

	


	
	for (int i=0;i<bulletFile2->m_dynamicsWorldInfo.size();i++)
	{
		if (bulletFile2->getFlags() & bParse::FD_DOUBLE_PRECISION)
		{
			btDynamicsWorldDoubleData* solverInfoData = (btDynamicsWorldDoubleData*)bulletFile2->m_dynamicsWorldInfo[i];
			btContactSolverInfo solverInfo;

			btVector3 gravity;
			gravity.deSerializeDouble(solverInfoData->m_gravity);

			solverInfo.m_tau = btScalar(solverInfoData->m_solverInfo.m_tau);
			solverInfo.m_damping = btScalar(solverInfoData->m_solverInfo.m_damping);
			solverInfo.m_friction = btScalar(solverInfoData->m_solverInfo.m_friction);
			solverInfo.m_timeStep = btScalar(solverInfoData->m_solverInfo.m_timeStep);

			solverInfo.m_restitution = btScalar(solverInfoData->m_solverInfo.m_restitution);
			solverInfo.m_maxErrorReduction = btScalar(solverInfoData->m_solverInfo.m_maxErrorReduction);
			solverInfo.m_sor = btScalar(solverInfoData->m_solverInfo.m_sor);
			solverInfo.m_erp = btScalar(solverInfoData->m_solverInfo.m_erp);

			solverInfo.m_erp2 = btScalar(solverInfoData->m_solverInfo.m_erp2);
			solverInfo.m_globalCfm = btScalar(solverInfoData->m_solverInfo.m_globalCfm);
			solverInfo.m_splitImpulsePenetrationThreshold = btScalar(solverInfoData->m_solverInfo.m_splitImpulsePenetrationThreshold);
			solverInfo.m_splitImpulseTurnErp = btScalar(solverInfoData->m_solverInfo.m_splitImpulseTurnErp);
		
			solverInfo.m_linearSlop = btScalar(solverInfoData->m_solverInfo.m_linearSlop);
			solverInfo.m_warmstartingFactor = btScalar(solverInfoData->m_solverInfo.m_warmstartingFactor);
			solverInfo.m_maxGyroscopicForce = btScalar(solverInfoData->m_solverInfo.m_maxGyroscopicForce);
			solverInfo.m_singleAxisRollingFrictionThreshold = btScalar(solverInfoData->m_solverInfo.m_singleAxisRollingFrictionThreshold);
		
			solverInfo.m_numIterations = solverInfoData->m_solverInfo.m_numIterations;
			solverInfo.m_solverMode = solverInfoData->m_solverInfo.m_solverMode;
			solverInfo.m_restingContactRestitutionThreshold = solverInfoData->m_solverInfo.m_restingContactRestitutionThreshold;
			solverInfo.m_minimumSolverBatchSize = solverInfoData->m_solverInfo.m_minimumSolverBatchSize;
		
			solverInfo.m_splitImpulse = solverInfoData->m_solverInfo.m_splitImpulse;

			setDynamicsWorldInfo(gravity,solverInfo);
		} else
		{
			btDynamicsWorldFloatData* solverInfoData = (btDynamicsWorldFloatData*)bulletFile2->m_dynamicsWorldInfo[i];
			btContactSolverInfo solverInfo;

			btVector3 gravity;
			gravity.deSerializeFloat(solverInfoData->m_gravity);

			solverInfo.m_tau = solverInfoData->m_solverInfo.m_tau;
			solverInfo.m_damping = solverInfoData->m_solverInfo.m_damping;
			solverInfo.m_friction = solverInfoData->m_solverInfo.m_friction;
			solverInfo.m_timeStep = solverInfoData->m_solverInfo.m_timeStep;

			solverInfo.m_restitution = solverInfoData->m_solverInfo.m_restitution;
			solverInfo.m_maxErrorReduction = solverInfoData->m_solverInfo.m_maxErrorReduction;
			solverInfo.m_sor = solverInfoData->m_solverInfo.m_sor;
			solverInfo.m_erp = solverInfoData->m_solverInfo.m_erp;

			solverInfo.m_erp2 = solverInfoData->m_solverInfo.m_erp2;
			solverInfo.m_globalCfm = solverInfoData->m_solverInfo.m_globalCfm;
			solverInfo.m_splitImpulsePenetrationThreshold = solverInfoData->m_solverInfo.m_splitImpulsePenetrationThreshold;
			solverInfo.m_splitImpulseTurnErp = solverInfoData->m_solverInfo.m_splitImpulseTurnErp;
		
			solverInfo.m_linearSlop = solverInfoData->m_solverInfo.m_linearSlop;
			solverInfo.m_warmstartingFactor = solverInfoData->m_solverInfo.m_warmstartingFactor;
			solverInfo.m_maxGyroscopicForce = solverInfoData->m_solverInfo.m_maxGyroscopicForce;
			solverInfo.m_singleAxisRollingFrictionThreshold = solverInfoData->m_solverInfo.m_singleAxisRollingFrictionThreshold;
		
			solverInfo.m_numIterations = solverInfoData->m_solverInfo.m_numIterations;
			solverInfo.m_solverMode = solverInfoData->m_solverInfo.m_solverMode;
			solverInfo.m_restingContactRestitutionThreshold = solverInfoData->m_solverInfo.m_restingContactRestitutionThreshold;
			solverInfo.m_minimumSolverBatchSize = solverInfoData->m_solverInfo.m_minimumSolverBatchSize;
		
			solverInfo.m_splitImpulse = solverInfoData->m_solverInfo.m_splitImpulse;

			setDynamicsWorldInfo(gravity,solverInfo);
		}
	}


	for (i=0;i<bulletFile2->m_rigidBodies.size();i++)
	{
		if (bulletFile2->getFlags() & bParse::FD_DOUBLE_PRECISION)
		{
			btRigidBodyDoubleData* colObjData = (btRigidBodyDoubleData*)bulletFile2->m_rigidBodies[i];
			convertRigidBodyDouble(colObjData);
		} else
		{
			btRigidBodyFloatData* colObjData = (btRigidBodyFloatData*)bulletFile2->m_rigidBodies[i];
			convertRigidBodyFloat(colObjData);
		}

		
	}

	for (i=0;i<bulletFile2->m_collisionObjects.size();i++)
	{
		if (bulletFile2->getFlags() & bParse::FD_DOUBLE_PRECISION)
		{
			btCollisionObjectDoubleData* colObjData = (btCollisionObjectDoubleData*)bulletFile2->m_collisionObjects[i];
			btCollisionShape** shapePtr = m_shapeMap.find(colObjData->m_collisionShape);
			if (shapePtr && *shapePtr)
			{
				btTransform startTransform;
				colObjData->m_worldTransform.m_origin.m_floats[3] = 0.f;
				startTransform.deSerializeDouble(colObjData->m_worldTransform);
				
				btCollisionShape* shape = (btCollisionShape*)*shapePtr;
				btCollisionObject* body = createCollisionObject(startTransform,shape,colObjData->m_name);
				body->setFriction(btScalar(colObjData->m_friction));
				body->setRestitution(btScalar(colObjData->m_restitution));
				
#ifdef USE_INTERNAL_EDGE_UTILITY
				if (shape->getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE)
				{
					btBvhTriangleMeshShape* trimesh = (btBvhTriangleMeshShape*)shape;
					if (trimesh->getTriangleInfoMap())
					{
						body->setCollisionFlags(body->getCollisionFlags()  | btCollisionObject::CF_CUSTOM_MATERIAL_CALLBACK);
					}
				}
#endif //USE_INTERNAL_EDGE_UTILITY
				m_bodyMap.insert(colObjData,body);
			} else
			{
				printf("error: no shape found\n");
			}
	
		} else
		{
			btCollisionObjectFloatData* colObjData = (btCollisionObjectFloatData*)bulletFile2->m_collisionObjects[i];
			btCollisionShape** shapePtr = m_shapeMap.find(colObjData->m_collisionShape);
			if (shapePtr && *shapePtr)
			{
				btTransform startTransform;
				colObjData->m_worldTransform.m_origin.m_floats[3] = 0.f;
				startTransform.deSerializeFloat(colObjData->m_worldTransform);
				
				btCollisionShape* shape = (btCollisionShape*)*shapePtr;
				btCollisionObject* body = createCollisionObject(startTransform,shape,colObjData->m_name);

#ifdef USE_INTERNAL_EDGE_UTILITY
				if (shape->getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE)
				{
					btBvhTriangleMeshShape* trimesh = (btBvhTriangleMeshShape*)shape;
					if (trimesh->getTriangleInfoMap())
					{
						body->setCollisionFlags(body->getCollisionFlags()  | btCollisionObject::CF_CUSTOM_MATERIAL_CALLBACK);
					}
				}
#endif //USE_INTERNAL_EDGE_UTILITY
				m_bodyMap.insert(colObjData,body);
			} else
			{
				printf("error: no shape found\n");
			}
		}
		
	}

	
	for (i=0;i<bulletFile2->m_constraints.size();i++)
	{
		btTypedConstraintData2* constraintData = (btTypedConstraintData2*)bulletFile2->m_constraints[i];

		btCollisionObject** colAptr = m_bodyMap.find(constraintData->m_rbA);
		btCollisionObject** colBptr = m_bodyMap.find(constraintData->m_rbB);

		btRigidBody* rbA = 0;
		btRigidBody* rbB = 0;

		if (colAptr)
		{
			rbA = btRigidBody::upcast(*colAptr);
			if (!rbA)
				rbA = &getFixedBody();
		}
		if (colBptr)
		{
			rbB = btRigidBody::upcast(*colBptr);
			if (!rbB)
				rbB = &getFixedBody();
		}
		if (!rbA && !rbB)
			continue;
				
		bool isDoublePrecisionData = (bulletFile2->getFlags() & bParse::FD_DOUBLE_PRECISION)!=0;
		
		if (isDoublePrecisionData)
		{
			if (bulletFile2->getVersion()>=282)
			{
				btTypedConstraintDoubleData* dc = (btTypedConstraintDoubleData*)constraintData;
				convertConstraintDouble(dc, rbA,rbB, bulletFile2->getVersion());
			} else
			{
				//double-precision constraints were messed up until 2.82, try to recover data...
				
				btTypedConstraintData* oldData = (btTypedConstraintData*)constraintData;
				
				convertConstraintBackwardsCompatible281(oldData, rbA,rbB, bulletFile2->getVersion());

			}
		}
		else
		{
			btTypedConstraintFloatData* dc = (btTypedConstraintFloatData*)constraintData;
			convertConstraintFloat(dc, rbA,rbB, bulletFile2->getVersion());
		}
		

	}

	return true;
}

