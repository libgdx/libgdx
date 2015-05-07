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

#include "btWorldImporter.h"
#include "btBulletDynamicsCommon.h"
#include "BulletCollision/Gimpact/btGImpactShape.h"

btWorldImporter::btWorldImporter(btDynamicsWorld* world)
:m_dynamicsWorld(world),
m_verboseMode(0)
{

}

btWorldImporter::~btWorldImporter()
{
}

void btWorldImporter::deleteAllData()
{
	int i;
	for (i=0;i<m_allocatedConstraints.size();i++)
	{
		if(m_dynamicsWorld)
			m_dynamicsWorld->removeConstraint(m_allocatedConstraints[i]);
		delete m_allocatedConstraints[i];
	}
	m_allocatedConstraints.clear();

	
	for (i=0;i<m_allocatedRigidBodies.size();i++)
	{
		if(m_dynamicsWorld)
			m_dynamicsWorld->removeRigidBody(btRigidBody::upcast(m_allocatedRigidBodies[i]));
		delete m_allocatedRigidBodies[i];
	}
	
	m_allocatedRigidBodies.clear();


	for (i=0;i<m_allocatedCollisionShapes.size();i++)
	{
		delete m_allocatedCollisionShapes[i];
	}
	m_allocatedCollisionShapes.clear();

	
	for (i=0;i<m_allocatedBvhs.size();i++)
	{
		delete m_allocatedBvhs[i];
	}
	m_allocatedBvhs.clear();
	
	for (i=0;i<m_allocatedTriangleInfoMaps.size();i++)
	{
		delete m_allocatedTriangleInfoMaps[i];
	}
	m_allocatedTriangleInfoMaps.clear();
	for (i=0;i<m_allocatedTriangleIndexArrays.size();i++)
	{
		delete m_allocatedTriangleIndexArrays[i];
	}
	m_allocatedTriangleIndexArrays.clear();
	for (i=0;i<m_allocatedNames.size();i++)
	{
		delete[] m_allocatedNames[i];
	}
	m_allocatedNames.clear();

	for (i=0;i<m_allocatedbtStridingMeshInterfaceDatas.size();i++)
	{
		btStridingMeshInterfaceData* curData = m_allocatedbtStridingMeshInterfaceDatas[i];

		for(int a = 0;a < curData->m_numMeshParts;a++)
		{
			btMeshPartData* curPart = &curData->m_meshPartsPtr[a];
			if(curPart->m_vertices3f)
				delete [] curPart->m_vertices3f;

			if(curPart->m_vertices3d)
				delete [] curPart->m_vertices3d;

			if(curPart->m_indices32)
				delete [] curPart->m_indices32;

			if(curPart->m_3indices16)
				delete [] curPart->m_3indices16;

			if(curPart->m_indices16)
				delete [] curPart->m_indices16;
			
			if (curPart->m_3indices8)
				delete [] curPart->m_3indices8;

		}
		delete [] curData->m_meshPartsPtr;
		delete curData;
	}
	m_allocatedbtStridingMeshInterfaceDatas.clear();

	for (i=0;i<m_indexArrays.size();i++)
	{
		btAlignedFree(m_indexArrays[i]);
	}
  m_indexArrays.clear();

	for (i=0;i<m_shortIndexArrays.size();i++)
	{
		btAlignedFree(m_shortIndexArrays[i]);
	}
  m_shortIndexArrays.clear();

	for (i=0;i<m_charIndexArrays.size();i++)
	{
		btAlignedFree(m_charIndexArrays[i]);
	}
  m_charIndexArrays.clear();
  
	for (i=0;i<m_floatVertexArrays.size();i++)
	{
		btAlignedFree(m_floatVertexArrays[i]);
	}
  m_floatVertexArrays.clear();

	for (i=0;i<m_doubleVertexArrays.size();i++)
	{
		btAlignedFree(m_doubleVertexArrays[i]);
	}
   m_doubleVertexArrays.clear();
   

}



btCollisionShape* btWorldImporter::convertCollisionShape(  btCollisionShapeData* shapeData  )
{
	btCollisionShape* shape = 0;

	switch (shapeData->m_shapeType)
		{
	case STATIC_PLANE_PROXYTYPE:
		{
			btStaticPlaneShapeData* planeData = (btStaticPlaneShapeData*)shapeData;
			btVector3 planeNormal,localScaling;
			planeNormal.deSerializeFloat(planeData->m_planeNormal);
			localScaling.deSerializeFloat(planeData->m_localScaling);
			shape = createPlaneShape(planeNormal,planeData->m_planeConstant);
			shape->setLocalScaling(localScaling);

			break;
		}
	case SCALED_TRIANGLE_MESH_SHAPE_PROXYTYPE:
		{
			btScaledTriangleMeshShapeData* scaledMesh = (btScaledTriangleMeshShapeData*) shapeData;
			btCollisionShapeData* colShapeData = (btCollisionShapeData*) &scaledMesh->m_trimeshShapeData;
			colShapeData->m_shapeType = TRIANGLE_MESH_SHAPE_PROXYTYPE;
			btCollisionShape* childShape = convertCollisionShape(colShapeData);
			btBvhTriangleMeshShape* meshShape = (btBvhTriangleMeshShape*)childShape;
			btVector3 localScaling;
			localScaling.deSerializeFloat(scaledMesh->m_localScaling);

			shape = createScaledTrangleMeshShape(meshShape, localScaling);
			break;
		}
	case GIMPACT_SHAPE_PROXYTYPE:
		{
			btGImpactMeshShapeData* gimpactData = (btGImpactMeshShapeData*) shapeData;
			if (gimpactData->m_gimpactSubType == CONST_GIMPACT_TRIMESH_SHAPE)
			{
				btStridingMeshInterfaceData* interfaceData = createStridingMeshInterfaceData(&gimpactData->m_meshInterface);
				btTriangleIndexVertexArray* meshInterface = createMeshInterface(*interfaceData);
				

				btGImpactMeshShape* gimpactShape = createGimpactShape(meshInterface);
				btVector3 localScaling;
				localScaling.deSerializeFloat(gimpactData->m_localScaling);
				gimpactShape->setLocalScaling(localScaling);
				gimpactShape->setMargin(btScalar(gimpactData->m_collisionMargin));
				gimpactShape->updateBound();
				shape = gimpactShape;
			} else
			{
				printf("unsupported gimpact sub type\n");
			}
			break;
		}
	//The btCapsuleShape* API has issue passing the margin/scaling/halfextents unmodified through the API
	//so deal with this
		case CAPSULE_SHAPE_PROXYTYPE:
		{
			btCapsuleShapeData* capData = (btCapsuleShapeData*)shapeData;
			

			switch (capData->m_upAxis)
			{
			case 0:
				{
					shape = createCapsuleShapeX(1,1);
					break;
				}
			case 1:
				{
					shape = createCapsuleShapeY(1,1);
					break;
				}
			case 2:
				{
					shape = createCapsuleShapeZ(1,1);
					break;
				}
			default:
				{
					printf("error: wrong up axis for btCapsuleShape\n");
				}


			};
			if (shape)
			{
				btCapsuleShape* cap = (btCapsuleShape*) shape;
				cap->deSerializeFloat(capData);
			}
			break;
		}
		case CYLINDER_SHAPE_PROXYTYPE:
		case CONE_SHAPE_PROXYTYPE:
		case BOX_SHAPE_PROXYTYPE:
		case SPHERE_SHAPE_PROXYTYPE:
		case MULTI_SPHERE_SHAPE_PROXYTYPE:
		case CONVEX_HULL_SHAPE_PROXYTYPE:
			{
				btConvexInternalShapeData* bsd = (btConvexInternalShapeData*)shapeData;
				btVector3 implicitShapeDimensions;
				implicitShapeDimensions.deSerializeFloat(bsd->m_implicitShapeDimensions);
				btVector3 localScaling;
				localScaling.deSerializeFloat(bsd->m_localScaling);
				btVector3 margin(bsd->m_collisionMargin,bsd->m_collisionMargin,bsd->m_collisionMargin);
				switch (shapeData->m_shapeType)
				{
					case BOX_SHAPE_PROXYTYPE:
						{
							btBoxShape* box= (btBoxShape*)createBoxShape(implicitShapeDimensions/localScaling+margin);
							//box->initializePolyhedralFeatures();
							shape = box;
							
							break;
						}
					case SPHERE_SHAPE_PROXYTYPE:
						{
							shape = createSphereShape(implicitShapeDimensions.getX());
							break;
						}
					
					case CYLINDER_SHAPE_PROXYTYPE:
						{
							btCylinderShapeData* cylData = (btCylinderShapeData*) shapeData;
							btVector3 halfExtents = implicitShapeDimensions+margin;
							switch (cylData->m_upAxis)
							{
							case 0:
								{
									shape = createCylinderShapeX(halfExtents.getY(),halfExtents.getX());
									break;
								}
							case 1:
								{
									shape = createCylinderShapeY(halfExtents.getX(),halfExtents.getY());
									break;
								}
							case 2:
								{
									shape = createCylinderShapeZ(halfExtents.getX(),halfExtents.getZ());
									break;
								}
							default:
								{
									printf("unknown Cylinder up axis\n");
								}

							};
							

							
							break;
						}
					case CONE_SHAPE_PROXYTYPE:
						{
							btConeShapeData* conData = (btConeShapeData*) shapeData;
							btVector3 halfExtents = implicitShapeDimensions;//+margin;
							switch (conData->m_upIndex)
							{
							case 0:
								{
									shape = createConeShapeX(halfExtents.getY(),halfExtents.getX());
									break;
								}
							case 1:
								{
									shape = createConeShapeY(halfExtents.getX(),halfExtents.getY());
									break;
								}
							case 2:
								{
									shape = createConeShapeZ(halfExtents.getX(),halfExtents.getZ());
									break;
								}
							default:
								{
									printf("unknown Cone up axis\n");
								}
									
							};
							
							
							
							break;
						}
					case MULTI_SPHERE_SHAPE_PROXYTYPE:
						{
							btMultiSphereShapeData* mss = (btMultiSphereShapeData*)bsd;
							int numSpheres = mss->m_localPositionArraySize;

							btAlignedObjectArray<btVector3> tmpPos;
							btAlignedObjectArray<btScalar> radii;
							radii.resize(numSpheres);
							tmpPos.resize(numSpheres);
							int i;
							for ( i=0;i<numSpheres;i++)
							{
								tmpPos[i].deSerializeFloat(mss->m_localPositionArrayPtr[i].m_pos);
								radii[i] = mss->m_localPositionArrayPtr[i].m_radius;
							}
							shape = createMultiSphereShape(&tmpPos[0],&radii[0],numSpheres);
							break;
						}
					case CONVEX_HULL_SHAPE_PROXYTYPE:
						{
						//	int sz = sizeof(btConvexHullShapeData);
						//	int sz2 = sizeof(btConvexInternalShapeData);
						//	int sz3 = sizeof(btCollisionShapeData);
							btConvexHullShapeData* convexData = (btConvexHullShapeData*)bsd;
							int numPoints = convexData->m_numUnscaledPoints;

							btAlignedObjectArray<btVector3> tmpPoints;
							tmpPoints.resize(numPoints);
							int i;
							for ( i=0;i<numPoints;i++)
							{
#ifdef BT_USE_DOUBLE_PRECISION
							if (convexData->m_unscaledPointsDoublePtr)
								tmpPoints[i].deSerialize(convexData->m_unscaledPointsDoublePtr[i]);
							if (convexData->m_unscaledPointsFloatPtr)
								tmpPoints[i].deSerializeFloat(convexData->m_unscaledPointsFloatPtr[i]);
#else
							if (convexData->m_unscaledPointsFloatPtr)
								tmpPoints[i].deSerialize(convexData->m_unscaledPointsFloatPtr[i]);
							if (convexData->m_unscaledPointsDoublePtr)
								tmpPoints[i].deSerializeDouble(convexData->m_unscaledPointsDoublePtr[i]);
#endif //BT_USE_DOUBLE_PRECISION
							}
							btConvexHullShape* hullShape = createConvexHullShape();
							for (i=0;i<numPoints;i++)
							{
								hullShape->addPoint(tmpPoints[i]);
							}
							hullShape->setMargin(bsd->m_collisionMargin);
							//hullShape->initializePolyhedralFeatures();
							shape = hullShape;
							break;
						}
					default:
						{
							printf("error: cannot create shape type (%d)\n",shapeData->m_shapeType);
						}
				}

				if (shape)
				{
					shape->setMargin(bsd->m_collisionMargin);
					
					btVector3 localScaling;
					localScaling.deSerializeFloat(bsd->m_localScaling);
					shape->setLocalScaling(localScaling);
					
				}
				break;
			}
		case TRIANGLE_MESH_SHAPE_PROXYTYPE:
		{
			btTriangleMeshShapeData* trimesh = (btTriangleMeshShapeData*)shapeData;
			btStridingMeshInterfaceData* interfaceData = createStridingMeshInterfaceData(&trimesh->m_meshInterface);
			btTriangleIndexVertexArray* meshInterface = createMeshInterface(*interfaceData);
			if (!meshInterface->getNumSubParts())
			{
				return 0;
			}

			btVector3 scaling; scaling.deSerializeFloat(trimesh->m_meshInterface.m_scaling);
			meshInterface->setScaling(scaling);


			btOptimizedBvh* bvh = 0;
#if 1
			if (trimesh->m_quantizedFloatBvh)
			{
				btOptimizedBvh** bvhPtr = m_bvhMap.find(trimesh->m_quantizedFloatBvh);
				if (bvhPtr && *bvhPtr)
				{
					bvh = *bvhPtr;
				} else
				{
					bvh = createOptimizedBvh();
					bvh->deSerializeFloat(*trimesh->m_quantizedFloatBvh);
				}
			}
			if (trimesh->m_quantizedDoubleBvh)
			{
				btOptimizedBvh** bvhPtr = m_bvhMap.find(trimesh->m_quantizedDoubleBvh);
				if (bvhPtr && *bvhPtr)
				{
					bvh = *bvhPtr;
				} else
				{
					bvh = createOptimizedBvh();
					bvh->deSerializeDouble(*trimesh->m_quantizedDoubleBvh);
				}
			}
#endif


			btBvhTriangleMeshShape* trimeshShape = createBvhTriangleMeshShape(meshInterface,bvh);
			trimeshShape->setMargin(trimesh->m_collisionMargin);
			shape = trimeshShape;

			if (trimesh->m_triangleInfoMap)
			{
				btTriangleInfoMap* map = createTriangleInfoMap();
				map->deSerialize(*trimesh->m_triangleInfoMap);
				trimeshShape->setTriangleInfoMap(map);

#ifdef USE_INTERNAL_EDGE_UTILITY
				gContactAddedCallback = btAdjustInternalEdgeContactsCallback;
#endif //USE_INTERNAL_EDGE_UTILITY

			}

			//printf("trimesh->m_collisionMargin=%f\n",trimesh->m_collisionMargin);
			break;
		}
		case COMPOUND_SHAPE_PROXYTYPE:
			{
				btCompoundShapeData* compoundData = (btCompoundShapeData*)shapeData;
				btCompoundShape* compoundShape = createCompoundShape();

				btCompoundShapeChildData* childShapeDataArray = &compoundData->m_childShapePtr[0];
				

				btAlignedObjectArray<btCollisionShape*> childShapes;
				for (int i=0;i<compoundData->m_numChildShapes;i++)
				{
					btCompoundShapeChildData* ptr = &compoundData->m_childShapePtr[i];

					btCollisionShapeData* cd = compoundData->m_childShapePtr[i].m_childShape;

					btCollisionShape* childShape = convertCollisionShape(cd);
					if (childShape)
					{
						btTransform localTransform;
						localTransform.deSerializeFloat(compoundData->m_childShapePtr[i].m_transform);
						compoundShape->addChildShape(localTransform,childShape);
					} else
					{
#ifdef _DEBUG
						printf("error: couldn't create childShape for compoundShape\n");
#endif
					}
					
				}
				shape = compoundShape;

				break;
			}
		case SOFTBODY_SHAPE_PROXYTYPE:
			{
				return 0;
			}
		default:
			{
#ifdef _DEBUG
				printf("unsupported shape type (%d)\n",shapeData->m_shapeType);
#endif
			}
		}

		return shape;
	
}



char* btWorldImporter::duplicateName(const char* name)
{
	if (name)
	{
		int l = (int)strlen(name);
		char* newName = new char[l+1];
		memcpy(newName,name,l);
		newName[l] = 0;
		m_allocatedNames.push_back(newName);
		return newName;
	}
	return 0;
}

void	btWorldImporter::convertConstraintBackwardsCompatible281(btTypedConstraintData* constraintData, btRigidBody* rbA, btRigidBody* rbB, int fileVersion)
{

	btTypedConstraint* constraint = 0;

		switch (constraintData->m_objectType)
		{
		case POINT2POINT_CONSTRAINT_TYPE:
			{
				btPoint2PointConstraintDoubleData* p2pData = (btPoint2PointConstraintDoubleData*)constraintData;
				if (rbA && rbB)
				{					
					btVector3 pivotInA,pivotInB;
					pivotInA.deSerializeDouble(p2pData->m_pivotInA);
					pivotInB.deSerializeDouble(p2pData->m_pivotInB);
					constraint = createPoint2PointConstraint(*rbA,*rbB,pivotInA,pivotInB);
				} else
				{
					btVector3 pivotInA;
					pivotInA.deSerializeDouble(p2pData->m_pivotInA);
					constraint = createPoint2PointConstraint(*rbA,pivotInA);
				}
				break;
			}
		case HINGE_CONSTRAINT_TYPE:
			{
				btHingeConstraint* hinge = 0;

				btHingeConstraintDoubleData* hingeData = (btHingeConstraintDoubleData*)constraintData;
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeDouble(hingeData->m_rbAFrame);
					rbBFrame.deSerializeDouble(hingeData->m_rbBFrame);
					hinge = createHingeConstraint(*rbA,*rbB,rbAFrame,rbBFrame,hingeData->m_useReferenceFrameA!=0);
				} else
				{
					btTransform rbAFrame;
					rbAFrame.deSerializeDouble(hingeData->m_rbAFrame);
					hinge = createHingeConstraint(*rbA,rbAFrame,hingeData->m_useReferenceFrameA!=0);
				}
				if (hingeData->m_enableAngularMotor)
				{
					hinge->enableAngularMotor(true,(btScalar)hingeData->m_motorTargetVelocity,(btScalar)hingeData->m_maxMotorImpulse);
				}
				hinge->setAngularOnly(hingeData->m_angularOnly!=0);
				hinge->setLimit(btScalar(hingeData->m_lowerLimit),btScalar(hingeData->m_upperLimit),btScalar(hingeData->m_limitSoftness),btScalar(hingeData->m_biasFactor),btScalar(hingeData->m_relaxationFactor));

				constraint = hinge;
				break;

			}
		case CONETWIST_CONSTRAINT_TYPE:
			{
				btConeTwistConstraintData* coneData = (btConeTwistConstraintData*)constraintData;
				btConeTwistConstraint* coneTwist = 0;
				
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(coneData->m_rbAFrame);
					rbBFrame.deSerializeFloat(coneData->m_rbBFrame);
					coneTwist = createConeTwistConstraint(*rbA,*rbB,rbAFrame,rbBFrame);
				} else
				{
					btTransform rbAFrame;
					rbAFrame.deSerializeFloat(coneData->m_rbAFrame);
					coneTwist = createConeTwistConstraint(*rbA,rbAFrame);
				}
				coneTwist->setLimit((btScalar)coneData->m_swingSpan1,(btScalar)coneData->m_swingSpan2,(btScalar)coneData->m_twistSpan,(btScalar)coneData->m_limitSoftness,
					(btScalar)coneData->m_biasFactor,(btScalar)coneData->m_relaxationFactor);
				coneTwist->setDamping((btScalar)coneData->m_damping);
				
				constraint = coneTwist;
				break;
			}

		case D6_SPRING_CONSTRAINT_TYPE:
			{

				btGeneric6DofSpringConstraintData* dofData = (btGeneric6DofSpringConstraintData*)constraintData;
			//	int sz = sizeof(btGeneric6DofSpringConstraintData);
				btGeneric6DofSpringConstraint* dof = 0;

				if (rbA && rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(dofData->m_6dofData.m_rbAFrame);
					rbBFrame.deSerializeFloat(dofData->m_6dofData.m_rbBFrame);
					dof = createGeneric6DofSpringConstraint(*rbA,*rbB,rbAFrame,rbBFrame,dofData->m_6dofData.m_useLinearReferenceFrameA!=0);
				} else
				{
					printf("Error in btWorldImporter::createGeneric6DofSpringConstraint: requires rbA && rbB\n");
				}

				if (dof)
				{
					btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
					angLowerLimit.deSerializeFloat(dofData->m_6dofData.m_angularLowerLimit);
					angUpperLimit.deSerializeFloat(dofData->m_6dofData.m_angularUpperLimit);
					linLowerLimit.deSerializeFloat(dofData->m_6dofData.m_linearLowerLimit);
					linUpperlimit.deSerializeFloat(dofData->m_6dofData.m_linearUpperLimit);
					
					angLowerLimit.setW(0.f);
					dof->setAngularLowerLimit(angLowerLimit);
					dof->setAngularUpperLimit(angUpperLimit);
					dof->setLinearLowerLimit(linLowerLimit);
					dof->setLinearUpperLimit(linUpperlimit);

					int i;
					if (fileVersion>280)
					{
						for (i=0;i<6;i++)
						{
							dof->setStiffness(i,(btScalar)dofData->m_springStiffness[i]);
							dof->setEquilibriumPoint(i,(btScalar)dofData->m_equilibriumPoint[i]);
							dof->enableSpring(i,dofData->m_springEnabled[i]!=0);
							dof->setDamping(i,(btScalar)dofData->m_springDamping[i]);
						}
					}
				}

				constraint = dof;
				break;

			}
		case D6_CONSTRAINT_TYPE:
			{
				btGeneric6DofConstraintData* dofData = (btGeneric6DofConstraintData*)constraintData;
				btGeneric6DofConstraint* dof = 0;

				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(dofData->m_rbAFrame);
					rbBFrame.deSerializeFloat(dofData->m_rbBFrame);
					dof = createGeneric6DofConstraint(*rbA,*rbB,rbAFrame,rbBFrame,dofData->m_useLinearReferenceFrameA!=0);
				} else
				{
					if (rbB)
					{
						btTransform rbBFrame;
						rbBFrame.deSerializeFloat(dofData->m_rbBFrame);
						dof = createGeneric6DofConstraint(*rbB,rbBFrame,dofData->m_useLinearReferenceFrameA!=0);
					} else
					{
						printf("Error in btWorldImporter::createGeneric6DofConstraint: missing rbB\n");
					}
				}

				if (dof)
				{
					btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
					angLowerLimit.deSerializeFloat(dofData->m_angularLowerLimit);
					angUpperLimit.deSerializeFloat(dofData->m_angularUpperLimit);
					linLowerLimit.deSerializeFloat(dofData->m_linearLowerLimit);
					linUpperlimit.deSerializeFloat(dofData->m_linearUpperLimit);
					
					dof->setAngularLowerLimit(angLowerLimit);
					dof->setAngularUpperLimit(angUpperLimit);
					dof->setLinearLowerLimit(linLowerLimit);
					dof->setLinearUpperLimit(linUpperlimit);
				}

				constraint = dof;
				break;
			}
		case SLIDER_CONSTRAINT_TYPE:
			{
				btSliderConstraintData* sliderData = (btSliderConstraintData*)constraintData;
				btSliderConstraint* slider = 0;
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(sliderData->m_rbAFrame);
					rbBFrame.deSerializeFloat(sliderData->m_rbBFrame);
					slider = createSliderConstraint(*rbA,*rbB,rbAFrame,rbBFrame,sliderData->m_useLinearReferenceFrameA!=0);
				} else
				{
					btTransform rbBFrame;
					rbBFrame.deSerializeFloat(sliderData->m_rbBFrame);
					slider = createSliderConstraint(*rbB,rbBFrame,sliderData->m_useLinearReferenceFrameA!=0);
				}
				slider->setLowerLinLimit((btScalar)sliderData->m_linearLowerLimit);
				slider->setUpperLinLimit((btScalar)sliderData->m_linearUpperLimit);
				slider->setLowerAngLimit((btScalar)sliderData->m_angularLowerLimit);
				slider->setUpperAngLimit((btScalar)sliderData->m_angularUpperLimit);
				slider->setUseFrameOffset(sliderData->m_useOffsetForConstraintFrame!=0);
				constraint = slider;
				break;
			}
		
		default:
			{
				printf("unknown constraint type\n");
			}
		};

		if (constraint)
		{
			constraint->setDbgDrawSize((btScalar)constraintData->m_dbgDrawSize);
			///those fields didn't exist and set to zero for pre-280 versions, so do a check here
			if (fileVersion>=280)
			{
				constraint->setBreakingImpulseThreshold((btScalar)constraintData->m_breakingImpulseThreshold);
				constraint->setEnabled(constraintData->m_isEnabled!=0);
				constraint->setOverrideNumSolverIterations(constraintData->m_overrideNumSolverIterations);
			}

			if (constraintData->m_name)
			{
				char* newname = duplicateName(constraintData->m_name);
				m_nameConstraintMap.insert(newname,constraint);
				m_objectNameMap.insert(constraint,newname);
			}
			if(m_dynamicsWorld)
				m_dynamicsWorld->addConstraint(constraint,constraintData->m_disableCollisionsBetweenLinkedBodies!=0);
		}

}

void	btWorldImporter::convertConstraintFloat(btTypedConstraintFloatData* constraintData, btRigidBody* rbA, btRigidBody* rbB, int fileVersion)
{
	btTypedConstraint* constraint = 0;

		switch (constraintData->m_objectType)
		{
		case POINT2POINT_CONSTRAINT_TYPE:
			{
				btPoint2PointConstraintFloatData* p2pData = (btPoint2PointConstraintFloatData*)constraintData;
				if (rbA&& rbB)
				{					
					btVector3 pivotInA,pivotInB;
					pivotInA.deSerializeFloat(p2pData->m_pivotInA);
					pivotInB.deSerializeFloat(p2pData->m_pivotInB);
					constraint = createPoint2PointConstraint(*rbA,*rbB,pivotInA,pivotInB);
					
				} else
				{
					btVector3 pivotInA;
					pivotInA.deSerializeFloat(p2pData->m_pivotInA);
					constraint = createPoint2PointConstraint(*rbA,pivotInA);
				}
				break;
			}
		case HINGE_CONSTRAINT_TYPE:
			{
				btHingeConstraint* hinge = 0;
				btHingeConstraintFloatData* hingeData = (btHingeConstraintFloatData*)constraintData;
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(hingeData->m_rbAFrame);
					rbBFrame.deSerializeFloat(hingeData->m_rbBFrame);
					hinge = createHingeConstraint(*rbA,*rbB,rbAFrame,rbBFrame,hingeData->m_useReferenceFrameA!=0);
				} else
				{
					btTransform rbAFrame;
					rbAFrame.deSerializeFloat(hingeData->m_rbAFrame);
					hinge = createHingeConstraint(*rbA,rbAFrame,hingeData->m_useReferenceFrameA!=0);
				}
				if (hingeData->m_enableAngularMotor)
				{
					hinge->enableAngularMotor(true,hingeData->m_motorTargetVelocity,hingeData->m_maxMotorImpulse);
				}
				hinge->setAngularOnly(hingeData->m_angularOnly!=0);
				hinge->setLimit(btScalar(hingeData->m_lowerLimit),btScalar(hingeData->m_upperLimit),btScalar(hingeData->m_limitSoftness),btScalar(hingeData->m_biasFactor),btScalar(hingeData->m_relaxationFactor));

				constraint = hinge;
				break;

			}
		case CONETWIST_CONSTRAINT_TYPE:
			{
				btConeTwistConstraintData* coneData = (btConeTwistConstraintData*)constraintData;
				btConeTwistConstraint* coneTwist = 0;
				
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(coneData->m_rbAFrame);
					rbBFrame.deSerializeFloat(coneData->m_rbBFrame);
					coneTwist = createConeTwistConstraint(*rbA,*rbB,rbAFrame,rbBFrame);
				} else
				{
					btTransform rbAFrame;
					rbAFrame.deSerializeFloat(coneData->m_rbAFrame);
					coneTwist = createConeTwistConstraint(*rbA,rbAFrame);
				}
				coneTwist->setLimit(coneData->m_swingSpan1,coneData->m_swingSpan2,coneData->m_twistSpan,coneData->m_limitSoftness,coneData->m_biasFactor,coneData->m_relaxationFactor);
				coneTwist->setDamping(coneData->m_damping);
				
				constraint = coneTwist;
				break;
			}

		case D6_SPRING_CONSTRAINT_TYPE:
			{
				
				btGeneric6DofSpringConstraintData* dofData = (btGeneric6DofSpringConstraintData*)constraintData;
			//	int sz = sizeof(btGeneric6DofSpringConstraintData);
				btGeneric6DofSpringConstraint* dof = 0;

				if (rbA && rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(dofData->m_6dofData.m_rbAFrame);
					rbBFrame.deSerializeFloat(dofData->m_6dofData.m_rbBFrame);
					dof = createGeneric6DofSpringConstraint(*rbA,*rbB,rbAFrame,rbBFrame,dofData->m_6dofData.m_useLinearReferenceFrameA!=0);
				} else
				{
					printf("Error in btWorldImporter::createGeneric6DofSpringConstraint: requires rbA && rbB\n");
				}

				if (dof)
				{
					btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
					angLowerLimit.deSerializeFloat(dofData->m_6dofData.m_angularLowerLimit);
					angUpperLimit.deSerializeFloat(dofData->m_6dofData.m_angularUpperLimit);
					linLowerLimit.deSerializeFloat(dofData->m_6dofData.m_linearLowerLimit);
					linUpperlimit.deSerializeFloat(dofData->m_6dofData.m_linearUpperLimit);
					
					angLowerLimit.setW(0.f);
					dof->setAngularLowerLimit(angLowerLimit);
					dof->setAngularUpperLimit(angUpperLimit);
					dof->setLinearLowerLimit(linLowerLimit);
					dof->setLinearUpperLimit(linUpperlimit);

					int i;
					if (fileVersion>280)
					{
						for (i=0;i<6;i++)
						{
							dof->setStiffness(i,dofData->m_springStiffness[i]);
							dof->setEquilibriumPoint(i,dofData->m_equilibriumPoint[i]);
							dof->enableSpring(i,dofData->m_springEnabled[i]!=0);
							dof->setDamping(i,dofData->m_springDamping[i]);
						}
					}
				}

				constraint = dof;
				break;
			}
		case D6_CONSTRAINT_TYPE:
			{
				btGeneric6DofConstraintData* dofData = (btGeneric6DofConstraintData*)constraintData;
				btGeneric6DofConstraint* dof = 0;

				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(dofData->m_rbAFrame);
					rbBFrame.deSerializeFloat(dofData->m_rbBFrame);
					dof = createGeneric6DofConstraint(*rbA,*rbB,rbAFrame,rbBFrame,dofData->m_useLinearReferenceFrameA!=0);
				} else
				{
					if (rbB)
					{
						btTransform rbBFrame;
						rbBFrame.deSerializeFloat(dofData->m_rbBFrame);
						dof = createGeneric6DofConstraint(*rbB,rbBFrame,dofData->m_useLinearReferenceFrameA!=0);
					} else
					{
						printf("Error in btWorldImporter::createGeneric6DofConstraint: missing rbB\n");
					}
				}

				if (dof)
				{
					btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
					angLowerLimit.deSerializeFloat(dofData->m_angularLowerLimit);
					angUpperLimit.deSerializeFloat(dofData->m_angularUpperLimit);
					linLowerLimit.deSerializeFloat(dofData->m_linearLowerLimit);
					linUpperlimit.deSerializeFloat(dofData->m_linearUpperLimit);
					
					dof->setAngularLowerLimit(angLowerLimit);
					dof->setAngularUpperLimit(angUpperLimit);
					dof->setLinearLowerLimit(linLowerLimit);
					dof->setLinearUpperLimit(linUpperlimit);
				}

				constraint = dof;
				break;
			}
		case SLIDER_CONSTRAINT_TYPE:
			{
				btSliderConstraintData* sliderData = (btSliderConstraintData*)constraintData;
				btSliderConstraint* slider = 0;
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeFloat(sliderData->m_rbAFrame);
					rbBFrame.deSerializeFloat(sliderData->m_rbBFrame);
					slider = createSliderConstraint(*rbA,*rbB,rbAFrame,rbBFrame,sliderData->m_useLinearReferenceFrameA!=0);
				} else
				{
					btTransform rbBFrame;
					rbBFrame.deSerializeFloat(sliderData->m_rbBFrame);
					slider = createSliderConstraint(*rbB,rbBFrame,sliderData->m_useLinearReferenceFrameA!=0);
				}
				slider->setLowerLinLimit(sliderData->m_linearLowerLimit);
				slider->setUpperLinLimit(sliderData->m_linearUpperLimit);
				slider->setLowerAngLimit(sliderData->m_angularLowerLimit);
				slider->setUpperAngLimit(sliderData->m_angularUpperLimit);
				slider->setUseFrameOffset(sliderData->m_useOffsetForConstraintFrame!=0);
				constraint = slider;
				break;
			}
		case GEAR_CONSTRAINT_TYPE:
			{
				btGearConstraintFloatData* gearData = (btGearConstraintFloatData*) constraintData;
				btGearConstraint* gear = 0;
				if (rbA&&rbB)
				{
					btVector3 axisInA,axisInB;
					axisInA.deSerializeFloat(gearData->m_axisInA);
					axisInB.deSerializeFloat(gearData->m_axisInB);
					gear = createGearConstraint(*rbA, *rbB, axisInA,axisInB, gearData->m_ratio);
				} else
				{
					btAssert(0);
					//perhaps a gear against a 'fixed' body, while the 'fixed' body is not serialized?
					//btGearConstraint(btRigidBody& rbA, btRigidBody& rbB, const btVector3& axisInA,const btVector3& axisInB, btScalar ratio=1.f);
				}
				constraint = gear;
				break;
			}
		case D6_SPRING_2_CONSTRAINT_TYPE:
			{
							
			btGeneric6DofSpring2ConstraintData* dofData = (btGeneric6DofSpring2ConstraintData*)constraintData;
		
			btGeneric6DofSpring2Constraint* dof = 0;
						
			if (rbA && rbB)
			{
				btTransform rbAFrame,rbBFrame;
				rbAFrame.deSerializeFloat(dofData->m_rbAFrame);
				rbBFrame.deSerializeFloat(dofData->m_rbBFrame);
				dof = createGeneric6DofSpring2Constraint(*rbA,*rbB,rbAFrame,rbBFrame, dofData->m_rotateOrder);
			} else
			{
				printf("Error in btWorldImporter::createGeneric6DofSpring2Constraint: requires rbA && rbB\n");
			}

			if (dof)
			{
				btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
				angLowerLimit.deSerializeFloat(dofData->m_angularLowerLimit);
				angUpperLimit.deSerializeFloat(dofData->m_angularUpperLimit);
				linLowerLimit.deSerializeFloat(dofData->m_linearLowerLimit);
				linUpperlimit.deSerializeFloat(dofData->m_linearUpperLimit);
					
				angLowerLimit.setW(0.f);
				dof->setAngularLowerLimit(angLowerLimit);
				dof->setAngularUpperLimit(angUpperLimit);
				dof->setLinearLowerLimit(linLowerLimit);
				dof->setLinearUpperLimit(linUpperlimit);

				int i;
				if (fileVersion>280)
				{
					//6-dof: 3 linear followed by 3 angular
					for (i=0;i<3;i++)
					{
						dof->setStiffness(i,dofData->m_linearSpringStiffness.m_floats[i]);
						dof->setEquilibriumPoint(i,dofData->m_linearEquilibriumPoint.m_floats[i]);
						dof->enableSpring(i,dofData->m_linearEnableSpring[i]!=0);
						dof->setDamping(i,dofData->m_linearSpringDamping.m_floats[i]);
					}
					for (i=0;i<3;i++)
					{
						dof->setStiffness(i+3,dofData->m_angularSpringStiffness.m_floats[i]);
						dof->setEquilibriumPoint(i+3,dofData->m_angularEquilibriumPoint.m_floats[i]);
						dof->enableSpring(i+3,dofData->m_angularEnableSpring[i]!=0);
						dof->setDamping(i+3,dofData->m_angularSpringDamping.m_floats[i]);
					}

				}
			}

			constraint = dof;
			break;
			
			}
			case FIXED_CONSTRAINT_TYPE:
			{
				
				btGeneric6DofSpring2Constraint* dof = 0;
				if (rbA && rbB)
				{
					btTransform rbAFrame,rbBFrame;
					//compute a shared world frame, and compute frameInA, frameInB relative to this
					btTransform sharedFrame;
					sharedFrame.setIdentity();
					btVector3 centerPos = btScalar(0.5)*(rbA->getWorldTransform().getOrigin()+
											rbB->getWorldTransform().getOrigin());
					sharedFrame.setOrigin(centerPos);
					rbAFrame = rbA->getWorldTransform().inverse()*sharedFrame;
					rbBFrame = rbB->getWorldTransform().inverse()*sharedFrame;
					
					
					dof = createGeneric6DofSpring2Constraint(*rbA,*rbB,rbAFrame,rbBFrame, RO_XYZ);
					dof->setLinearUpperLimit(btVector3(0,0,0));
					dof->setLinearLowerLimit(btVector3(0,0,0));
					dof->setAngularUpperLimit(btVector3(0,0,0));
					dof->setAngularLowerLimit(btVector3(0,0,0));
					
				} else
				{
					printf("Error in btWorldImporter::createGeneric6DofSpring2Constraint: requires rbA && rbB\n");
				}
				
				constraint = dof;
				break;
			}
		default:
			{
				printf("unknown constraint type\n");
			}
		};

		if (constraint)
		{
			constraint->setDbgDrawSize(constraintData->m_dbgDrawSize);
			///those fields didn't exist and set to zero for pre-280 versions, so do a check here
			if (fileVersion>=280)
			{
				constraint->setBreakingImpulseThreshold(constraintData->m_breakingImpulseThreshold);
				constraint->setEnabled(constraintData->m_isEnabled!=0);
				constraint->setOverrideNumSolverIterations(constraintData->m_overrideNumSolverIterations);
			}

			if (constraintData->m_name)
			{
				char* newname = duplicateName(constraintData->m_name);
				m_nameConstraintMap.insert(newname,constraint);
				m_objectNameMap.insert(constraint,newname);
			}
			if(m_dynamicsWorld)
				m_dynamicsWorld->addConstraint(constraint,constraintData->m_disableCollisionsBetweenLinkedBodies!=0);
		}
		

}



void	btWorldImporter::convertConstraintDouble(btTypedConstraintDoubleData* constraintData, btRigidBody* rbA, btRigidBody* rbB, int fileVersion)
{
	btTypedConstraint* constraint = 0;

		switch (constraintData->m_objectType)
		{
		case POINT2POINT_CONSTRAINT_TYPE:
			{
				btPoint2PointConstraintDoubleData2* p2pData = (btPoint2PointConstraintDoubleData2*)constraintData;
				if (rbA && rbB)
				{					
					btVector3 pivotInA,pivotInB;
					pivotInA.deSerializeDouble(p2pData->m_pivotInA);
					pivotInB.deSerializeDouble(p2pData->m_pivotInB);
					constraint = createPoint2PointConstraint(*rbA,*rbB,pivotInA,pivotInB);
				} else
				{
					btVector3 pivotInA;
					pivotInA.deSerializeDouble(p2pData->m_pivotInA);
					constraint = createPoint2PointConstraint(*rbA,pivotInA);
				}
				break;
			}
		case HINGE_CONSTRAINT_TYPE:
			{
				btHingeConstraint* hinge = 0;

				btHingeConstraintDoubleData2* hingeData = (btHingeConstraintDoubleData2*)constraintData;
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeDouble(hingeData->m_rbAFrame);
					rbBFrame.deSerializeDouble(hingeData->m_rbBFrame);
					hinge = createHingeConstraint(*rbA,*rbB,rbAFrame,rbBFrame,hingeData->m_useReferenceFrameA!=0);
				} else
				{
					btTransform rbAFrame;
					rbAFrame.deSerializeDouble(hingeData->m_rbAFrame);
					hinge = createHingeConstraint(*rbA,rbAFrame,hingeData->m_useReferenceFrameA!=0);
				}
				if (hingeData->m_enableAngularMotor)
				{
					hinge->enableAngularMotor(true,(btScalar)hingeData->m_motorTargetVelocity,(btScalar)hingeData->m_maxMotorImpulse);
				}
				hinge->setAngularOnly(hingeData->m_angularOnly!=0);
				hinge->setLimit(btScalar(hingeData->m_lowerLimit),btScalar(hingeData->m_upperLimit),btScalar(hingeData->m_limitSoftness),btScalar(hingeData->m_biasFactor),btScalar(hingeData->m_relaxationFactor));

				constraint = hinge;
				break;

			}
		case CONETWIST_CONSTRAINT_TYPE:
			{
				btConeTwistConstraintDoubleData* coneData = (btConeTwistConstraintDoubleData*)constraintData;
				btConeTwistConstraint* coneTwist = 0;
				
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeDouble(coneData->m_rbAFrame);
					rbBFrame.deSerializeDouble(coneData->m_rbBFrame);
					coneTwist = createConeTwistConstraint(*rbA,*rbB,rbAFrame,rbBFrame);
				} else
				{
					btTransform rbAFrame;
					rbAFrame.deSerializeDouble(coneData->m_rbAFrame);
					coneTwist = createConeTwistConstraint(*rbA,rbAFrame);
				}
				coneTwist->setLimit((btScalar)coneData->m_swingSpan1,(btScalar)coneData->m_swingSpan2,(btScalar)coneData->m_twistSpan,(btScalar)coneData->m_limitSoftness,
					(btScalar)coneData->m_biasFactor,(btScalar)coneData->m_relaxationFactor);
				coneTwist->setDamping((btScalar)coneData->m_damping);
				
				constraint = coneTwist;
				break;
			}

		case D6_SPRING_CONSTRAINT_TYPE:
			{
				
				btGeneric6DofSpringConstraintDoubleData2* dofData = (btGeneric6DofSpringConstraintDoubleData2*)constraintData;
			//	int sz = sizeof(btGeneric6DofSpringConstraintData);
				btGeneric6DofSpringConstraint* dof = 0;

				if (rbA && rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeDouble(dofData->m_6dofData.m_rbAFrame);
					rbBFrame.deSerializeDouble(dofData->m_6dofData.m_rbBFrame);
					dof = createGeneric6DofSpringConstraint(*rbA,*rbB,rbAFrame,rbBFrame,dofData->m_6dofData.m_useLinearReferenceFrameA!=0);
				} else
				{
					printf("Error in btWorldImporter::createGeneric6DofSpringConstraint: requires rbA && rbB\n");
				}

				if (dof)
				{
					btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
					angLowerLimit.deSerializeDouble(dofData->m_6dofData.m_angularLowerLimit);
					angUpperLimit.deSerializeDouble(dofData->m_6dofData.m_angularUpperLimit);
					linLowerLimit.deSerializeDouble(dofData->m_6dofData.m_linearLowerLimit);
					linUpperlimit.deSerializeDouble(dofData->m_6dofData.m_linearUpperLimit);
					
					angLowerLimit.setW(0.f);
					dof->setAngularLowerLimit(angLowerLimit);
					dof->setAngularUpperLimit(angUpperLimit);
					dof->setLinearLowerLimit(linLowerLimit);
					dof->setLinearUpperLimit(linUpperlimit);

					int i;
					if (fileVersion>280)
					{
						for (i=0;i<6;i++)
						{
							dof->setStiffness(i,(btScalar)dofData->m_springStiffness[i]);
							dof->setEquilibriumPoint(i,(btScalar)dofData->m_equilibriumPoint[i]);
							dof->enableSpring(i,dofData->m_springEnabled[i]!=0);
							dof->setDamping(i,(btScalar)dofData->m_springDamping[i]);
						}
					}
				}

				constraint = dof;
				break;
			}
		case D6_CONSTRAINT_TYPE:
			{
				btGeneric6DofConstraintDoubleData2* dofData = (btGeneric6DofConstraintDoubleData2*)constraintData;
				btGeneric6DofConstraint* dof = 0;

				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeDouble(dofData->m_rbAFrame);
					rbBFrame.deSerializeDouble(dofData->m_rbBFrame);
					dof = createGeneric6DofConstraint(*rbA,*rbB,rbAFrame,rbBFrame,dofData->m_useLinearReferenceFrameA!=0);
				} else
				{
					if (rbB)
					{
						btTransform rbBFrame;
						rbBFrame.deSerializeDouble(dofData->m_rbBFrame);
						dof = createGeneric6DofConstraint(*rbB,rbBFrame,dofData->m_useLinearReferenceFrameA!=0);
					} else
					{
						printf("Error in btWorldImporter::createGeneric6DofConstraint: missing rbB\n");
					}
				}

				if (dof)
				{
					btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
					angLowerLimit.deSerializeDouble(dofData->m_angularLowerLimit);
					angUpperLimit.deSerializeDouble(dofData->m_angularUpperLimit);
					linLowerLimit.deSerializeDouble(dofData->m_linearLowerLimit);
					linUpperlimit.deSerializeDouble(dofData->m_linearUpperLimit);
					
					dof->setAngularLowerLimit(angLowerLimit);
					dof->setAngularUpperLimit(angUpperLimit);
					dof->setLinearLowerLimit(linLowerLimit);
					dof->setLinearUpperLimit(linUpperlimit);
				}

				constraint = dof;
				break;
			}
		case SLIDER_CONSTRAINT_TYPE:
			{
				btSliderConstraintDoubleData* sliderData = (btSliderConstraintDoubleData*)constraintData;
				btSliderConstraint* slider = 0;
				if (rbA&& rbB)
				{
					btTransform rbAFrame,rbBFrame;
					rbAFrame.deSerializeDouble(sliderData->m_rbAFrame);
					rbBFrame.deSerializeDouble(sliderData->m_rbBFrame);
					slider = createSliderConstraint(*rbA,*rbB,rbAFrame,rbBFrame,sliderData->m_useLinearReferenceFrameA!=0);
				} else
				{
					btTransform rbBFrame;
					rbBFrame.deSerializeDouble(sliderData->m_rbBFrame);
					slider = createSliderConstraint(*rbB,rbBFrame,sliderData->m_useLinearReferenceFrameA!=0);
				}
				slider->setLowerLinLimit((btScalar)sliderData->m_linearLowerLimit);
				slider->setUpperLinLimit((btScalar)sliderData->m_linearUpperLimit);
				slider->setLowerAngLimit((btScalar)sliderData->m_angularLowerLimit);
				slider->setUpperAngLimit((btScalar)sliderData->m_angularUpperLimit);
				slider->setUseFrameOffset(sliderData->m_useOffsetForConstraintFrame!=0);
				constraint = slider;
				break;
			}
		case GEAR_CONSTRAINT_TYPE:
			{
				btGearConstraintDoubleData* gearData = (btGearConstraintDoubleData*) constraintData;
				btGearConstraint* gear = 0;
				if (rbA&&rbB)
				{
					btVector3 axisInA,axisInB;
					axisInA.deSerializeDouble(gearData->m_axisInA);
					axisInB.deSerializeDouble(gearData->m_axisInB);
					gear = createGearConstraint(*rbA, *rbB, axisInA,axisInB, gearData->m_ratio);
				} else
				{
					btAssert(0);
					//perhaps a gear against a 'fixed' body, while the 'fixed' body is not serialized?
					//btGearConstraint(btRigidBody& rbA, btRigidBody& rbB, const btVector3& axisInA,const btVector3& axisInB, btScalar ratio=1.f);
				}
				constraint = gear;
				break;
			}

		case D6_SPRING_2_CONSTRAINT_TYPE:
			{
							
			btGeneric6DofSpring2ConstraintDoubleData2* dofData = (btGeneric6DofSpring2ConstraintDoubleData2*)constraintData;
		
			btGeneric6DofSpring2Constraint* dof = 0;
						
			if (rbA && rbB)
			{
				btTransform rbAFrame,rbBFrame;
				rbAFrame.deSerializeDouble(dofData->m_rbAFrame);
				rbBFrame.deSerializeDouble(dofData->m_rbBFrame);
				dof = createGeneric6DofSpring2Constraint(*rbA,*rbB,rbAFrame,rbBFrame, dofData->m_rotateOrder);
			} else
			{
				printf("Error in btWorldImporter::createGeneric6DofSpring2Constraint: requires rbA && rbB\n");
			}

			if (dof)
			{
				btVector3 angLowerLimit,angUpperLimit, linLowerLimit,linUpperlimit;
				angLowerLimit.deSerializeDouble(dofData->m_angularLowerLimit);
				angUpperLimit.deSerializeDouble(dofData->m_angularUpperLimit);
				linLowerLimit.deSerializeDouble(dofData->m_linearLowerLimit);
				linUpperlimit.deSerializeDouble(dofData->m_linearUpperLimit);
					
				angLowerLimit.setW(0.f);
				dof->setAngularLowerLimit(angLowerLimit);
				dof->setAngularUpperLimit(angUpperLimit);
				dof->setLinearLowerLimit(linLowerLimit);
				dof->setLinearUpperLimit(linUpperlimit);

				int i;
				if (fileVersion>280)
				{
					//6-dof: 3 linear followed by 3 angular
					for (i=0;i<3;i++)
					{
						dof->setStiffness(i,dofData->m_linearSpringStiffness.m_floats[i]);
						dof->setEquilibriumPoint(i,dofData->m_linearEquilibriumPoint.m_floats[i]);
						dof->enableSpring(i,dofData->m_linearEnableSpring[i]!=0);
						dof->setDamping(i,dofData->m_linearSpringDamping.m_floats[i]);
					}
					for (i=0;i<3;i++)
					{
						dof->setStiffness(i+3,dofData->m_angularSpringStiffness.m_floats[i]);
						dof->setEquilibriumPoint(i+3,dofData->m_angularEquilibriumPoint.m_floats[i]);
						dof->enableSpring(i+3,dofData->m_angularEnableSpring[i]!=0);
						dof->setDamping(i+3,dofData->m_angularSpringDamping.m_floats[i]);
					}

				}
			}

			constraint = dof;
			break;
			
			}
			case FIXED_CONSTRAINT_TYPE:
			{
				
				btGeneric6DofSpring2Constraint* dof = 0;
				if (rbA && rbB)
				{
					btTransform rbAFrame,rbBFrame;
					//compute a shared world frame, and compute frameInA, frameInB relative to this
					btTransform sharedFrame;
					sharedFrame.setIdentity();
					btVector3 centerPos = btScalar(0.5)*(rbA->getWorldTransform().getOrigin()+
														 rbB->getWorldTransform().getOrigin());
					sharedFrame.setOrigin(centerPos);
					rbAFrame = rbA->getWorldTransform().inverse()*sharedFrame;
					rbBFrame = rbB->getWorldTransform().inverse()*sharedFrame;
					
					
					dof = createGeneric6DofSpring2Constraint(*rbA,*rbB,rbAFrame,rbBFrame, RO_XYZ);
					dof->setLinearUpperLimit(btVector3(0,0,0));
					dof->setLinearLowerLimit(btVector3(0,0,0));
					dof->setAngularUpperLimit(btVector3(0,0,0));
					dof->setAngularLowerLimit(btVector3(0,0,0));
					
				} else
				{
					printf("Error in btWorldImporter::createGeneric6DofSpring2Constraint: requires rbA && rbB\n");
				}
				
				constraint = dof;
				break;
			}
				
		default:
			{
				printf("unknown constraint type\n");
			}
		};

		if (constraint)
		{
			constraint->setDbgDrawSize((btScalar)constraintData->m_dbgDrawSize);
			///those fields didn't exist and set to zero for pre-280 versions, so do a check here
			if (fileVersion>=280)
			{
				constraint->setBreakingImpulseThreshold((btScalar)constraintData->m_breakingImpulseThreshold);
				constraint->setEnabled(constraintData->m_isEnabled!=0);
				constraint->setOverrideNumSolverIterations(constraintData->m_overrideNumSolverIterations);
			}

			if (constraintData->m_name)
			{
				char* newname = duplicateName(constraintData->m_name);
				m_nameConstraintMap.insert(newname,constraint);
				m_objectNameMap.insert(constraint,newname);
			}
			if(m_dynamicsWorld)
				m_dynamicsWorld->addConstraint(constraint,constraintData->m_disableCollisionsBetweenLinkedBodies!=0);
		}
		

}










btTriangleIndexVertexArray* btWorldImporter::createMeshInterface(btStridingMeshInterfaceData&  meshData)
{
	btTriangleIndexVertexArray* meshInterface = createTriangleMeshContainer();

	for (int i=0;i<meshData.m_numMeshParts;i++)
	{
		btIndexedMesh meshPart;
		meshPart.m_numTriangles = meshData.m_meshPartsPtr[i].m_numTriangles;
		meshPart.m_numVertices = meshData.m_meshPartsPtr[i].m_numVertices;
		

		if (meshData.m_meshPartsPtr[i].m_indices32)
		{
			meshPart.m_indexType = PHY_INTEGER;
			meshPart.m_triangleIndexStride = 3*sizeof(int);
			int* indexArray = (int*)btAlignedAlloc(sizeof(int)*3*meshPart.m_numTriangles,16);
			m_indexArrays.push_back(indexArray);
			for (int j=0;j<3*meshPart.m_numTriangles;j++)
			{
				indexArray[j] = meshData.m_meshPartsPtr[i].m_indices32[j].m_value;
			}
			meshPart.m_triangleIndexBase = (const unsigned char*)indexArray;
		} else
		{
			if (meshData.m_meshPartsPtr[i].m_3indices16)
			{
				meshPart.m_indexType = PHY_SHORT;
				meshPart.m_triangleIndexStride = sizeof(short int)*3;//sizeof(btShortIntIndexTripletData);

				short int* indexArray = (short int*)btAlignedAlloc(sizeof(short int)*3*meshPart.m_numTriangles,16);
				m_shortIndexArrays.push_back(indexArray);

				for (int j=0;j<meshPart.m_numTriangles;j++)
				{
					indexArray[3*j] = meshData.m_meshPartsPtr[i].m_3indices16[j].m_values[0];
					indexArray[3*j+1] = meshData.m_meshPartsPtr[i].m_3indices16[j].m_values[1];
					indexArray[3*j+2] = meshData.m_meshPartsPtr[i].m_3indices16[j].m_values[2];
				}

				meshPart.m_triangleIndexBase = (const unsigned char*)indexArray;
			}
			if (meshData.m_meshPartsPtr[i].m_indices16)
			{
				meshPart.m_indexType = PHY_SHORT;
				meshPart.m_triangleIndexStride = 3*sizeof(short int);
				short int* indexArray = (short int*)btAlignedAlloc(sizeof(short int)*3*meshPart.m_numTriangles,16);
				m_shortIndexArrays.push_back(indexArray);
				for (int j=0;j<3*meshPart.m_numTriangles;j++)
				{
					indexArray[j] = meshData.m_meshPartsPtr[i].m_indices16[j].m_value;
				}

				meshPart.m_triangleIndexBase = (const unsigned char*)indexArray;
			}

			if (meshData.m_meshPartsPtr[i].m_3indices8)
			{
				meshPart.m_indexType = PHY_UCHAR;
				meshPart.m_triangleIndexStride = sizeof(unsigned char)*3;

				unsigned char* indexArray = (unsigned char*)btAlignedAlloc(sizeof(unsigned char)*3*meshPart.m_numTriangles,16);
				m_charIndexArrays.push_back(indexArray);

				for (int j=0;j<meshPart.m_numTriangles;j++)
				{
					indexArray[3*j] = meshData.m_meshPartsPtr[i].m_3indices8[j].m_values[0];
					indexArray[3*j+1] = meshData.m_meshPartsPtr[i].m_3indices8[j].m_values[1];
					indexArray[3*j+2] = meshData.m_meshPartsPtr[i].m_3indices8[j].m_values[2];
				}

				meshPart.m_triangleIndexBase = (const unsigned char*)indexArray;
			}
		}

		if (meshData.m_meshPartsPtr[i].m_vertices3f)
		{
			meshPart.m_vertexType = PHY_FLOAT;
			meshPart.m_vertexStride = sizeof(btVector3FloatData);
			btVector3FloatData* vertices = (btVector3FloatData*) btAlignedAlloc(sizeof(btVector3FloatData)*meshPart.m_numVertices,16);
			m_floatVertexArrays.push_back(vertices);

			for (int j=0;j<meshPart.m_numVertices;j++)
			{
				vertices[j].m_floats[0] = meshData.m_meshPartsPtr[i].m_vertices3f[j].m_floats[0];
				vertices[j].m_floats[1] = meshData.m_meshPartsPtr[i].m_vertices3f[j].m_floats[1];
				vertices[j].m_floats[2] = meshData.m_meshPartsPtr[i].m_vertices3f[j].m_floats[2];
				vertices[j].m_floats[3] = meshData.m_meshPartsPtr[i].m_vertices3f[j].m_floats[3];
			}
			meshPart.m_vertexBase = (const unsigned char*)vertices;
		} else
		{
			meshPart.m_vertexType = PHY_DOUBLE;
			meshPart.m_vertexStride = sizeof(btVector3DoubleData);


			btVector3DoubleData* vertices = (btVector3DoubleData*) btAlignedAlloc(sizeof(btVector3DoubleData)*meshPart.m_numVertices,16);
			m_doubleVertexArrays.push_back(vertices);

			for (int j=0;j<meshPart.m_numVertices;j++)
			{
				vertices[j].m_floats[0] = meshData.m_meshPartsPtr[i].m_vertices3d[j].m_floats[0];
				vertices[j].m_floats[1] = meshData.m_meshPartsPtr[i].m_vertices3d[j].m_floats[1];
				vertices[j].m_floats[2] = meshData.m_meshPartsPtr[i].m_vertices3d[j].m_floats[2];
				vertices[j].m_floats[3] = meshData.m_meshPartsPtr[i].m_vertices3d[j].m_floats[3];
			}
			meshPart.m_vertexBase = (const unsigned char*)vertices;
		}
		
		if (meshPart.m_triangleIndexBase && meshPart.m_vertexBase)
		{
			meshInterface->addIndexedMesh(meshPart,meshPart.m_indexType);
		}
	}

	return meshInterface;
}


btStridingMeshInterfaceData* btWorldImporter::createStridingMeshInterfaceData(btStridingMeshInterfaceData* interfaceData)
{
	//create a new btStridingMeshInterfaceData that is an exact copy of shapedata and store it in the WorldImporter
	btStridingMeshInterfaceData* newData = new btStridingMeshInterfaceData;

	newData->m_scaling = interfaceData->m_scaling;
	newData->m_numMeshParts = interfaceData->m_numMeshParts;
	newData->m_meshPartsPtr = new btMeshPartData[newData->m_numMeshParts];

	for(int i = 0;i < newData->m_numMeshParts;i++)
	{
		btMeshPartData* curPart = &interfaceData->m_meshPartsPtr[i];
		btMeshPartData* curNewPart = &newData->m_meshPartsPtr[i];

		curNewPart->m_numTriangles = curPart->m_numTriangles;
		curNewPart->m_numVertices = curPart->m_numVertices;
		
		if(curPart->m_vertices3f)
		{
			curNewPart->m_vertices3f = new btVector3FloatData[curNewPart->m_numVertices];
			memcpy(curNewPart->m_vertices3f,curPart->m_vertices3f,sizeof(btVector3FloatData) * curNewPart->m_numVertices);
		}
		else
			curNewPart->m_vertices3f = NULL;

		if(curPart->m_vertices3d)
		{
			curNewPart->m_vertices3d = new btVector3DoubleData[curNewPart->m_numVertices];
			memcpy(curNewPart->m_vertices3d,curPart->m_vertices3d,sizeof(btVector3DoubleData) * curNewPart->m_numVertices);
		}
		else
			curNewPart->m_vertices3d = NULL;

		int numIndices = curNewPart->m_numTriangles * 3;
		///the m_3indices8 was not initialized in some Bullet versions, this can cause crashes at loading time
		///we catch it by only dealing with m_3indices8 if none of the other indices are initialized
		bool uninitialized3indices8Workaround =false;

		if(curPart->m_indices32)
		{
			uninitialized3indices8Workaround=true;
			curNewPart->m_indices32 = new btIntIndexData[numIndices];
			memcpy(curNewPart->m_indices32,curPart->m_indices32,sizeof(btIntIndexData) * numIndices);
		}
		else
			curNewPart->m_indices32 = NULL;

		if(curPart->m_3indices16)
		{
			uninitialized3indices8Workaround=true;
			curNewPart->m_3indices16 = new btShortIntIndexTripletData[curNewPart->m_numTriangles];
			memcpy(curNewPart->m_3indices16,curPart->m_3indices16,sizeof(btShortIntIndexTripletData) * curNewPart->m_numTriangles);
		}
		else
			curNewPart->m_3indices16 = NULL;

		if(curPart->m_indices16)
		{
			uninitialized3indices8Workaround=true;
			curNewPart->m_indices16 = new btShortIntIndexData[numIndices];
			memcpy(curNewPart->m_indices16,curPart->m_indices16,sizeof(btShortIntIndexData) * numIndices);
		}
		else
			curNewPart->m_indices16 = NULL;

		if(!uninitialized3indices8Workaround && curPart->m_3indices8)
		{
			curNewPart->m_3indices8 = new btCharIndexTripletData[curNewPart->m_numTriangles];
			memcpy(curNewPart->m_3indices8,curPart->m_3indices8,sizeof(btCharIndexTripletData) * curNewPart->m_numTriangles);
		}
		else
			curNewPart->m_3indices8 = NULL;

	}

	m_allocatedbtStridingMeshInterfaceDatas.push_back(newData);

	return(newData);
}

#ifdef USE_INTERNAL_EDGE_UTILITY
extern ContactAddedCallback		gContactAddedCallback;

static bool btAdjustInternalEdgeContactsCallback(btManifoldPoint& cp,	const btCollisionObject* colObj0,int partId0,int index0,const btCollisionObject* colObj1,int partId1,int index1)
{

	btAdjustInternalEdgeContacts(cp,colObj1,colObj0, partId1,index1);
		//btAdjustInternalEdgeContacts(cp,colObj1,colObj0, partId1,index1, BT_TRIANGLE_CONVEX_BACKFACE_MODE);
		//btAdjustInternalEdgeContacts(cp,colObj1,colObj0, partId1,index1, BT_TRIANGLE_CONVEX_DOUBLE_SIDED+BT_TRIANGLE_CONCAVE_DOUBLE_SIDED);
	return true;
}
#endif //USE_INTERNAL_EDGE_UTILITY




btCollisionObject* btWorldImporter::createCollisionObject(const btTransform& startTransform,btCollisionShape* shape, const char* bodyName)
{
	return createRigidBody(false,0,startTransform,shape,bodyName);
}

void	btWorldImporter::setDynamicsWorldInfo(const btVector3& gravity, const btContactSolverInfo& solverInfo)
{
	if (m_dynamicsWorld)
	{
		m_dynamicsWorld->setGravity(gravity);
		m_dynamicsWorld->getSolverInfo() = solverInfo;
	}

}

btRigidBody*  btWorldImporter::createRigidBody(bool isDynamic, btScalar mass, const btTransform& startTransform,btCollisionShape* shape,const char* bodyName)
{
	btVector3 localInertia;
	localInertia.setZero();

	if (mass)
		shape->calculateLocalInertia(mass,localInertia);
	
	btRigidBody* body = new btRigidBody(mass,0,shape,localInertia);	
	body->setWorldTransform(startTransform);

	if (m_dynamicsWorld)
		m_dynamicsWorld->addRigidBody(body);
	
	if (bodyName)
	{
		char* newname = duplicateName(bodyName);
		m_objectNameMap.insert(body,newname);
		m_nameBodyMap.insert(newname,body);
	}
	m_allocatedRigidBodies.push_back(body);
	return body;

}

btCollisionShape* btWorldImporter::createPlaneShape(const btVector3& planeNormal,btScalar planeConstant)
{
	btStaticPlaneShape* shape = new btStaticPlaneShape(planeNormal,planeConstant);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}
btCollisionShape* btWorldImporter::createBoxShape(const btVector3& halfExtents)
{
	btBoxShape* shape = new btBoxShape(halfExtents);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}
btCollisionShape* btWorldImporter::createSphereShape(btScalar radius)
{
	btSphereShape* shape = new btSphereShape(radius);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}


btCollisionShape* btWorldImporter::createCapsuleShapeX(btScalar radius, btScalar height)
{
	btCapsuleShapeX* shape = new btCapsuleShapeX(radius,height);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createCapsuleShapeY(btScalar radius, btScalar height)
{
	btCapsuleShape* shape = new btCapsuleShape(radius,height);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createCapsuleShapeZ(btScalar radius, btScalar height)
{
	btCapsuleShapeZ* shape = new btCapsuleShapeZ(radius,height);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createCylinderShapeX(btScalar radius,btScalar height)
{
	btCylinderShapeX* shape = new btCylinderShapeX(btVector3(height,radius,radius));
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createCylinderShapeY(btScalar radius,btScalar height)
{
	btCylinderShape* shape = new btCylinderShape(btVector3(radius,height,radius));
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createCylinderShapeZ(btScalar radius,btScalar height)
{
	btCylinderShapeZ* shape = new btCylinderShapeZ(btVector3(radius,radius,height));
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createConeShapeX(btScalar radius,btScalar height)
{
	btConeShapeX* shape = new btConeShapeX(radius,height);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createConeShapeY(btScalar radius,btScalar height)
{
	btConeShape* shape = new btConeShape(radius,height);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCollisionShape* btWorldImporter::createConeShapeZ(btScalar radius,btScalar height)
{
	btConeShapeZ* shape = new btConeShapeZ(radius,height);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btTriangleIndexVertexArray*	btWorldImporter::createTriangleMeshContainer()
{
	btTriangleIndexVertexArray* in = new btTriangleIndexVertexArray();
	m_allocatedTriangleIndexArrays.push_back(in);
	return in;
}

btOptimizedBvh*	btWorldImporter::createOptimizedBvh()
{
	btOptimizedBvh* bvh = new btOptimizedBvh();
	m_allocatedBvhs.push_back(bvh);
	return bvh;
}


btTriangleInfoMap* btWorldImporter::createTriangleInfoMap()
{
	btTriangleInfoMap* tim = new btTriangleInfoMap();
	m_allocatedTriangleInfoMaps.push_back(tim);
	return tim;
}

btBvhTriangleMeshShape* btWorldImporter::createBvhTriangleMeshShape(btStridingMeshInterface* trimesh, btOptimizedBvh* bvh)
{
	if (bvh)
	{
		btBvhTriangleMeshShape* bvhTriMesh = new btBvhTriangleMeshShape(trimesh,bvh->isQuantized(), false);
		bvhTriMesh->setOptimizedBvh(bvh);
		m_allocatedCollisionShapes.push_back(bvhTriMesh);
		return bvhTriMesh;
	}

	btBvhTriangleMeshShape* ts = new btBvhTriangleMeshShape(trimesh,true);
	m_allocatedCollisionShapes.push_back(ts);
	return ts;

}
btCollisionShape* btWorldImporter::createConvexTriangleMeshShape(btStridingMeshInterface* trimesh)
{
	return 0;
}
btGImpactMeshShape* btWorldImporter::createGimpactShape(btStridingMeshInterface* trimesh)
{
	btGImpactMeshShape* shape = new btGImpactMeshShape(trimesh);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
	
}
btConvexHullShape* btWorldImporter::createConvexHullShape()
{
	btConvexHullShape* shape = new btConvexHullShape();
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btCompoundShape* btWorldImporter::createCompoundShape()
{
	btCompoundShape* shape = new btCompoundShape();
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

	
btScaledBvhTriangleMeshShape* btWorldImporter::createScaledTrangleMeshShape(btBvhTriangleMeshShape* meshShape,const btVector3& localScaling)
{
	btScaledBvhTriangleMeshShape* shape = new btScaledBvhTriangleMeshShape(meshShape,localScaling);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btMultiSphereShape* btWorldImporter::createMultiSphereShape(const btVector3* positions,const btScalar* radi,int numSpheres)
{
	btMultiSphereShape* shape = new btMultiSphereShape(positions, radi, numSpheres);
	m_allocatedCollisionShapes.push_back(shape);
	return shape;
}

btRigidBody& btWorldImporter::getFixedBody()
{
	static btRigidBody s_fixed(0, 0,0);
	s_fixed.setMassProps(btScalar(0.),btVector3(btScalar(0.),btScalar(0.),btScalar(0.)));
	return s_fixed;
}

btPoint2PointConstraint* btWorldImporter::createPoint2PointConstraint(btRigidBody& rbA,btRigidBody& rbB, const btVector3& pivotInA,const btVector3& pivotInB)
{
	btPoint2PointConstraint* p2p = new btPoint2PointConstraint(rbA,rbB,pivotInA,pivotInB);
	m_allocatedConstraints.push_back(p2p);
	return p2p;
}

btPoint2PointConstraint* btWorldImporter::createPoint2PointConstraint(btRigidBody& rbA,const btVector3& pivotInA)
{
	btPoint2PointConstraint* p2p = new btPoint2PointConstraint(rbA,pivotInA);
	m_allocatedConstraints.push_back(p2p);
	return p2p;
}


btHingeConstraint* btWorldImporter::createHingeConstraint(btRigidBody& rbA,btRigidBody& rbB, const btTransform& rbAFrame, const btTransform& rbBFrame, bool useReferenceFrameA)
{
	btHingeConstraint* hinge = new btHingeConstraint(rbA,rbB,rbAFrame,rbBFrame,useReferenceFrameA);
	m_allocatedConstraints.push_back(hinge);
	return hinge;
}

btHingeConstraint* btWorldImporter::createHingeConstraint(btRigidBody& rbA,const btTransform& rbAFrame, bool useReferenceFrameA)
{
	btHingeConstraint* hinge = new btHingeConstraint(rbA,rbAFrame,useReferenceFrameA);
	m_allocatedConstraints.push_back(hinge);
	return hinge;
}

btConeTwistConstraint* btWorldImporter::createConeTwistConstraint(btRigidBody& rbA,btRigidBody& rbB,const btTransform& rbAFrame, const btTransform& rbBFrame)
{
	btConeTwistConstraint* cone = new btConeTwistConstraint(rbA,rbB,rbAFrame,rbBFrame);
	m_allocatedConstraints.push_back(cone);
	return cone;
}

btConeTwistConstraint* btWorldImporter::createConeTwistConstraint(btRigidBody& rbA,const btTransform& rbAFrame)
{
	btConeTwistConstraint* cone = new btConeTwistConstraint(rbA,rbAFrame);
	m_allocatedConstraints.push_back(cone);
	return cone;
}


btGeneric6DofConstraint* btWorldImporter::createGeneric6DofConstraint(btRigidBody& rbA, btRigidBody& rbB, const btTransform& frameInA, const btTransform& frameInB ,bool useLinearReferenceFrameA)
{
	btGeneric6DofConstraint* dof = new btGeneric6DofConstraint(rbA,rbB,frameInA,frameInB,useLinearReferenceFrameA);
	m_allocatedConstraints.push_back(dof);
	return dof;
}

btGeneric6DofConstraint* btWorldImporter::createGeneric6DofConstraint(btRigidBody& rbB, const btTransform& frameInB, bool useLinearReferenceFrameB)
{
	btGeneric6DofConstraint* dof =  new btGeneric6DofConstraint(rbB,frameInB,useLinearReferenceFrameB);
	m_allocatedConstraints.push_back(dof);
	return dof;
}

btGeneric6DofSpring2Constraint* btWorldImporter::createGeneric6DofSpring2Constraint(btRigidBody& rbA, btRigidBody& rbB, const btTransform& frameInA, const btTransform& frameInB, int rotateOrder)
{
	btGeneric6DofSpring2Constraint* dof = new btGeneric6DofSpring2Constraint(rbA,rbB,frameInA,frameInB, (RotateOrder)rotateOrder);
	m_allocatedConstraints.push_back(dof);
	return dof;
}



btGeneric6DofSpringConstraint* btWorldImporter::createGeneric6DofSpringConstraint(btRigidBody& rbA, btRigidBody& rbB, const btTransform& frameInA, const btTransform& frameInB ,bool useLinearReferenceFrameA)
{
	btGeneric6DofSpringConstraint* dof = new btGeneric6DofSpringConstraint(rbA,rbB,frameInA,frameInB,useLinearReferenceFrameA);
	m_allocatedConstraints.push_back(dof);
	return dof;
}


btSliderConstraint* btWorldImporter::createSliderConstraint(btRigidBody& rbA, btRigidBody& rbB, const btTransform& frameInA, const btTransform& frameInB ,bool useLinearReferenceFrameA)
{
	btSliderConstraint* slider = new btSliderConstraint(rbA,rbB,frameInA,frameInB,useLinearReferenceFrameA);
	m_allocatedConstraints.push_back(slider);
	return slider;
}

btSliderConstraint* btWorldImporter::createSliderConstraint(btRigidBody& rbB, const btTransform& frameInB, bool useLinearReferenceFrameA)
{
	btSliderConstraint* slider = new btSliderConstraint(rbB,frameInB,useLinearReferenceFrameA);
	m_allocatedConstraints.push_back(slider);
	return slider;
}

btGearConstraint* btWorldImporter::createGearConstraint(btRigidBody& rbA, btRigidBody& rbB, const btVector3& axisInA,const btVector3& axisInB, btScalar ratio)
{
	btGearConstraint* gear = new btGearConstraint(rbA,rbB,axisInA,axisInB,ratio);
	m_allocatedConstraints.push_back(gear);
	return gear;
}

	// query for data
int	btWorldImporter::getNumCollisionShapes() const
{
	return m_allocatedCollisionShapes.size();
}

btCollisionShape* btWorldImporter::getCollisionShapeByIndex(int index)
{
	return m_allocatedCollisionShapes[index];
}

btCollisionShape* btWorldImporter::getCollisionShapeByName(const char* name)
{
	btCollisionShape** shapePtr = m_nameShapeMap.find(name);
	if (shapePtr&& *shapePtr)
	{
		return *shapePtr;
	}
	return 0;
}

btRigidBody* btWorldImporter::getRigidBodyByName(const char* name)
{
	btRigidBody** bodyPtr = m_nameBodyMap.find(name);
	if (bodyPtr && *bodyPtr)
	{
		return *bodyPtr;
	}
	return 0;
}

btTypedConstraint* btWorldImporter::getConstraintByName(const char* name)
{
	btTypedConstraint** constraintPtr = m_nameConstraintMap.find(name);
	if (constraintPtr && *constraintPtr)
	{
		return *constraintPtr;
	}
	return 0;
}

const char*	btWorldImporter::getNameForPointer(const void* ptr) const
{
	const char*const * namePtr = m_objectNameMap.find(ptr);
	if (namePtr && *namePtr)
		return *namePtr;
	return 0;
}


int btWorldImporter::getNumRigidBodies() const
{
	return m_allocatedRigidBodies.size();
}

btCollisionObject* btWorldImporter::getRigidBodyByIndex(int index) const
{
	return m_allocatedRigidBodies[index];
}
int btWorldImporter::getNumConstraints() const
{
	return m_allocatedConstraints.size();
}

btTypedConstraint* btWorldImporter::getConstraintByIndex(int index) const
{
	return m_allocatedConstraints[index];
}

int btWorldImporter::getNumBvhs() const
{
	return m_allocatedBvhs.size();
}
 btOptimizedBvh* btWorldImporter::getBvhByIndex(int index) const
{
	return m_allocatedBvhs[index];
}

int btWorldImporter::getNumTriangleInfoMaps() const
{
	return m_allocatedTriangleInfoMaps.size();
}

btTriangleInfoMap* btWorldImporter::getTriangleInfoMapByIndex(int index) const
{
	return m_allocatedTriangleInfoMaps[index];
}


void	btWorldImporter::convertRigidBodyFloat( btRigidBodyFloatData* colObjData)
{
	btScalar mass = btScalar(colObjData->m_inverseMass? 1.f/colObjData->m_inverseMass : 0.f);
	btVector3 localInertia;
	localInertia.setZero();
	btCollisionShape** shapePtr = m_shapeMap.find(colObjData->m_collisionObjectData.m_collisionShape);
	if (shapePtr && *shapePtr)
	{
		btTransform startTransform;
		colObjData->m_collisionObjectData.m_worldTransform.m_origin.m_floats[3] = 0.f;
		startTransform.deSerializeFloat(colObjData->m_collisionObjectData.m_worldTransform);
				
	//	startTransform.setBasis(btMatrix3x3::getIdentity());
		btCollisionShape* shape = (btCollisionShape*)*shapePtr;
		if (shape->isNonMoving())
		{
			mass = 0.f;
		}
		if (mass)
		{
			shape->calculateLocalInertia(mass,localInertia);
		}
		bool isDynamic = mass!=0.f;
		btRigidBody* body = createRigidBody(isDynamic,mass,startTransform,shape,colObjData->m_collisionObjectData.m_name);
		body->setFriction(colObjData->m_collisionObjectData.m_friction);
		body->setRestitution(colObjData->m_collisionObjectData.m_restitution);
		btVector3 linearFactor,angularFactor;
		linearFactor.deSerializeFloat(colObjData->m_linearFactor);
		angularFactor.deSerializeFloat(colObjData->m_angularFactor);
		body->setLinearFactor(linearFactor);
		body->setAngularFactor(angularFactor);

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

void	btWorldImporter::convertRigidBodyDouble( btRigidBodyDoubleData* colObjData)
{
	btScalar mass = btScalar(colObjData->m_inverseMass? 1.f/colObjData->m_inverseMass : 0.f);
	btVector3 localInertia;
	localInertia.setZero();
	btCollisionShape** shapePtr = m_shapeMap.find(colObjData->m_collisionObjectData.m_collisionShape);
	if (shapePtr && *shapePtr)
	{
		btTransform startTransform;
		colObjData->m_collisionObjectData.m_worldTransform.m_origin.m_floats[3] = 0.f;
		startTransform.deSerializeDouble(colObjData->m_collisionObjectData.m_worldTransform);
				
	//	startTransform.setBasis(btMatrix3x3::getIdentity());
		btCollisionShape* shape = (btCollisionShape*)*shapePtr;
		if (shape->isNonMoving())
		{
			mass = 0.f;
		}
		if (mass)
		{
			shape->calculateLocalInertia(mass,localInertia);
		}
		bool isDynamic = mass!=0.f;
		btRigidBody* body = createRigidBody(isDynamic,mass,startTransform,shape,colObjData->m_collisionObjectData.m_name);
		body->setFriction(btScalar(colObjData->m_collisionObjectData.m_friction));
		body->setRestitution(btScalar(colObjData->m_collisionObjectData.m_restitution));
		btVector3 linearFactor,angularFactor;
		linearFactor.deSerializeDouble(colObjData->m_linearFactor);
		angularFactor.deSerializeDouble(colObjData->m_angularFactor);
		body->setLinearFactor(linearFactor);
		body->setAngularFactor(angularFactor);
				

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
