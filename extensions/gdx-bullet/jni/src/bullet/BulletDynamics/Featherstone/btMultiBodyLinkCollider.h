/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2013 Erwin Coumans  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

#ifndef BT_FEATHERSTONE_LINK_COLLIDER_H
#define BT_FEATHERSTONE_LINK_COLLIDER_H

#include "BulletCollision/CollisionDispatch/btCollisionObject.h"

#include "btMultiBody.h"

class btMultiBodyLinkCollider : public btCollisionObject
{
//protected:
public:

	btMultiBody* m_multiBody;
	int m_link;


	btMultiBodyLinkCollider (btMultiBody* multiBody,int link)
		:m_multiBody(multiBody),
		m_link(link)
	{
		m_checkCollideWith =  true;
		//we need to remove the 'CF_STATIC_OBJECT' flag, otherwise links/base doesn't merge islands
		//this means that some constraints might point to bodies that are not in the islands, causing crashes
		//if (link>=0 || (multiBody && !multiBody->hasFixedBase()))
		{
			m_collisionFlags &= (~btCollisionObject::CF_STATIC_OBJECT);
		}
		// else
		//{
		//	m_collisionFlags |= (btCollisionObject::CF_STATIC_OBJECT);
		//}

		m_internalType = CO_FEATHERSTONE_LINK;
	}
	static btMultiBodyLinkCollider* upcast(btCollisionObject* colObj)
	{
		if (colObj->getInternalType()&btCollisionObject::CO_FEATHERSTONE_LINK)
			return (btMultiBodyLinkCollider*)colObj;
		return 0;
	}
	static const btMultiBodyLinkCollider* upcast(const btCollisionObject* colObj)
	{
		if (colObj->getInternalType()&btCollisionObject::CO_FEATHERSTONE_LINK)
			return (btMultiBodyLinkCollider*)colObj;
		return 0;
	}

	virtual bool checkCollideWithOverride(const  btCollisionObject* co) const
	{
		const btMultiBodyLinkCollider* other = btMultiBodyLinkCollider::upcast(co);
		if (!other)
			return true;
		if (other->m_multiBody != this->m_multiBody)
			return true;
		if (!m_multiBody->hasSelfCollision())
			return false;

		//check if 'link' has collision disabled
		if (m_link>=0)
		{
			const btMultibodyLink& link = m_multiBody->getLink(this->m_link);
			if ((link.m_flags&BT_MULTIBODYLINKFLAGS_DISABLE_PARENT_COLLISION) && link.m_parent == other->m_link)
				return false;
		}
		
		if (other->m_link>=0)
		{
			const btMultibodyLink& otherLink = other->m_multiBody->getLink(other->m_link);
			if ((otherLink.m_flags& BT_MULTIBODYLINKFLAGS_DISABLE_PARENT_COLLISION) && otherLink.m_parent == this->m_link)
				return false;
		}
		return true;
	}
};

#endif //BT_FEATHERSTONE_LINK_COLLIDER_H

