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

#ifndef BT_BULLET_XML_WORLD_IMPORTER_H
#define BT_BULLET_XML_WORLD_IMPORTER_H

#include "LinearMath/btScalar.h"

class btDynamicsWorld;
class TiXmlNode;
struct btConvexInternalShapeData;
struct btCollisionShapeData;
#ifdef BT_USE_DOUBLE_PRECISION
struct btRigidBodyDoubleData;
#define btRigidBodyData btRigidBodyDoubleData
#else
struct btRigidBodyFloatData;
#define btRigidBodyData btRigidBodyFloatData
#endif//BT_USE_DOUBLE_PRECISION
struct btTypedConstraintData;
struct btCompoundShapeChildData;

#include "LinearMath/btAlignedObjectArray.h"
#include "BulletWorldImporter/btWorldImporter.h"

class btBulletXmlWorldImporter : public btWorldImporter
{

protected:
	btAlignedObjectArray<btCollisionShapeData*>			m_collisionShapeData;
	btAlignedObjectArray<btAlignedObjectArray<btCompoundShapeChildData>* >		m_compoundShapeChildDataArrays;
	btAlignedObjectArray<btRigidBodyData*>				m_rigidBodyData;
	btAlignedObjectArray<btTypedConstraintData*>		m_constraintData;
	btHashMap<btHashPtr,void*>							m_pointerLookup;
	int													m_fileVersion;
	bool												m_fileOk;

	void auto_serialize_root_level_children(TiXmlNode* pParent);
	void auto_serialize(TiXmlNode* pParent);

	void deSerializeVector3FloatData(TiXmlNode* pParent,btAlignedObjectArray<btVector3FloatData>& vectors);

	void	fixupCollisionDataPointers(btCollisionShapeData* shapeData);
	void	fixupConstraintData(btTypedConstraintData* tcd);

	//collision shapes data
	void deSerializeCollisionShapeData(TiXmlNode* pParent,btCollisionShapeData* colShapeData);
	void deSerializeConvexInternalShapeData(TiXmlNode* pParent);
	void deSerializeStaticPlaneShapeData(TiXmlNode* pParent);
	void deSerializeCompoundShapeData(TiXmlNode* pParent);
	void deSerializeCompoundShapeChildData(TiXmlNode* pParent);
	void deSerializeConvexHullShapeData(TiXmlNode* pParent);
	void deSerializeDynamicsWorldData(TiXmlNode* parent);
	
	///bodies
	void deSerializeRigidBodyFloatData(TiXmlNode* pParent);

	///constraints
	void deSerializeGeneric6DofConstraintData(TiXmlNode* pParent);

	public:
		btBulletXmlWorldImporter(btDynamicsWorld* world);

		virtual ~btBulletXmlWorldImporter();
		
		bool loadFile(const char* fileName);

};

#endif //BT_BULLET_XML_WORLD_IMPORTER_H
