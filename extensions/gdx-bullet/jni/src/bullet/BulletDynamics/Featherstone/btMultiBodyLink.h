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

#ifndef BT_MULTIBODY_LINK_H
#define BT_MULTIBODY_LINK_H

#include "LinearMath/btQuaternion.h"
#include "LinearMath/btVector3.h"
#include "BulletCollision/CollisionDispatch/btCollisionObject.h"

enum	btMultiBodyLinkFlags
{
	BT_MULTIBODYLINKFLAGS_DISABLE_PARENT_COLLISION = 1
};
//
// Link struct
//

struct btMultibodyLink 
{

	BT_DECLARE_ALIGNED_ALLOCATOR();

    btScalar joint_pos;    // qi

    btScalar mass;         // mass of link
    btVector3 inertia;   // inertia of link (local frame; diagonal)

    int parent;         // index of the parent link (assumed to be < index of this link), or -1 if parent is the base link.

    btQuaternion zero_rot_parent_to_this;    // rotates vectors in parent-frame to vectors in local-frame (when q=0). constant.

    // "axis" = spatial joint axis (Mirtich Defn 9 p104). (expressed in local frame.) constant.
    // for prismatic: axis_top = zero;
    //                axis_bottom = unit vector along the joint axis.
    // for revolute: axis_top = unit vector along the rotation axis (u);
    //               axis_bottom = u cross d_vector.
    btVector3 axis_top;
    btVector3 axis_bottom;

    btVector3 d_vector;   // vector from the inboard joint pos to this link's COM. (local frame.) constant. set for revolute joints only.

    // e_vector is constant, but depends on the joint type
    // prismatic: vector from COM of parent to COM of this link, WHEN Q = 0. (local frame.)
    // revolute: vector from parent's COM to the pivot point, in PARENT's frame.
    btVector3 e_vector;

    bool is_revolute;   // true = revolute, false = prismatic

    btQuaternion cached_rot_parent_to_this;   // rotates vectors in parent frame to vectors in local frame
    btVector3 cached_r_vector;                // vector from COM of parent to COM of this link, in local frame.

    btVector3 applied_force;    // In WORLD frame
    btVector3 applied_torque;   // In WORLD frame
    btScalar joint_torque;

	class btMultiBodyLinkCollider* m_collider;
	int m_flags;

    // ctor: set some sensible defaults
	btMultibodyLink()
		: joint_pos(0),
			mass(1),
			parent(-1),
			zero_rot_parent_to_this(1, 0, 0, 0),
			is_revolute(false),
			cached_rot_parent_to_this(1, 0, 0, 0),
			joint_torque(0),
			m_collider(0),
			m_flags(0)
	{
		inertia.setValue(1, 1, 1);
		axis_top.setValue(0, 0, 0);
		axis_bottom.setValue(1, 0, 0);
		d_vector.setValue(0, 0, 0);
		e_vector.setValue(0, 0, 0);
		cached_r_vector.setValue(0, 0, 0);
		applied_force.setValue( 0, 0, 0);
		applied_torque.setValue(0, 0, 0);
	}

    // routine to update cached_rot_parent_to_this and cached_r_vector
    void updateCache()
	{
		if (is_revolute) 
		{
			cached_rot_parent_to_this = btQuaternion(axis_top,-joint_pos) * zero_rot_parent_to_this;
			cached_r_vector = d_vector + quatRotate(cached_rot_parent_to_this,e_vector);
		} else 
		{
			// cached_rot_parent_to_this never changes, so no need to update
			cached_r_vector = e_vector + joint_pos * axis_bottom;
		}
	}
};


#endif //BT_MULTIBODY_LINK_H
