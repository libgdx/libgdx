/*
 * PURPOSE:
 *   Class representing an articulated rigid body. Stores the body's
 *   current state, allows forces and torques to be set, handles
 *   timestepping and implements Featherstone's algorithm.
 *   
 * COPYRIGHT:
 *   Copyright (C) Stephen Thompson, <stephen@solarflare.org.uk>, 2011-2013
 *   Portions written By Erwin Coumans: replacing Eigen math library by Bullet LinearMath and a dedicated 6x6 matrix inverse (solveImatrix)

 This software is provided 'as-is', without any express or implied warranty.
 In no event will the authors be held liable for any damages arising from the use of this software.
 Permission is granted to anyone to use this software for any purpose,
 including commercial applications, and to alter it and redistribute it freely,
 subject to the following restrictions:
 
 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
 3. This notice may not be removed or altered from any source distribution.
 
 */


#include "btMultiBody.h"
#include "btMultiBodyLink.h"
#include "btMultiBodyLinkCollider.h"

// #define INCLUDE_GYRO_TERM 

namespace {
    const btScalar SLEEP_EPSILON = btScalar(0.05);  // this is a squared velocity (m^2 s^-2)
    const btScalar SLEEP_TIMEOUT = btScalar(2);     // in seconds
}




//
// Various spatial helper functions
//

namespace {
    void SpatialTransform(const btMatrix3x3 &rotation_matrix,  // rotates vectors in 'from' frame to vectors in 'to' frame
                          const btVector3 &displacement,     // vector from origin of 'from' frame to origin of 'to' frame, in 'to' coordinates
                          const btVector3 &top_in,       // top part of input vector
                          const btVector3 &bottom_in,    // bottom part of input vector
                          btVector3 &top_out,         // top part of output vector
                          btVector3 &bottom_out)      // bottom part of output vector
    {
        top_out = rotation_matrix * top_in;
        bottom_out = -displacement.cross(top_out) + rotation_matrix * bottom_in;
    }

    void InverseSpatialTransform(const btMatrix3x3 &rotation_matrix,
                                 const btVector3 &displacement,
                                 const btVector3 &top_in,
                                 const btVector3 &bottom_in,
                                 btVector3 &top_out,
                                 btVector3 &bottom_out)
    {
        top_out = rotation_matrix.transpose() * top_in;
        bottom_out = rotation_matrix.transpose() * (bottom_in + displacement.cross(top_in));
    }

    btScalar SpatialDotProduct(const btVector3 &a_top,
                            const btVector3 &a_bottom,
                            const btVector3 &b_top,
                            const btVector3 &b_bottom)
    {
        return a_bottom.dot(b_top) + a_top.dot(b_bottom);
    }
}


//
// Implementation of class btMultiBody
//

btMultiBody::btMultiBody(int n_links,
                     btScalar mass,
                     const btVector3 &inertia,
                     bool fixed_base_,
                     bool can_sleep_)
    : base_quat(0, 0, 0, 1),
      base_mass(mass),
      base_inertia(inertia),
    
		fixed_base(fixed_base_),
		awake(true),
		can_sleep(can_sleep_),
		sleep_timer(0),
		m_baseCollider(0),
		m_linearDamping(0.04f),
		m_angularDamping(0.04f),
		m_useGyroTerm(true),
		m_maxAppliedImpulse(1000.f),
		m_hasSelfCollision(true)
{
	 links.resize(n_links);

	vector_buf.resize(2*n_links);
    matrix_buf.resize(n_links + 1);
	m_real_buf.resize(6 + 2*n_links);
    base_pos.setValue(0, 0, 0);
    base_force.setValue(0, 0, 0);
    base_torque.setValue(0, 0, 0);
}

btMultiBody::~btMultiBody()
{
}

void btMultiBody::setupPrismatic(int i,
                               btScalar mass,
                               const btVector3 &inertia,
                               int parent,
                               const btQuaternion &rot_parent_to_this,
                               const btVector3 &joint_axis,
                               const btVector3 &r_vector_when_q_zero,
							   bool disableParentCollision)
{
    links[i].mass = mass;
    links[i].inertia = inertia;
    links[i].parent = parent;
    links[i].zero_rot_parent_to_this = rot_parent_to_this;
    links[i].axis_top.setValue(0,0,0);
    links[i].axis_bottom = joint_axis;
    links[i].e_vector = r_vector_when_q_zero;
    links[i].is_revolute = false;
    links[i].cached_rot_parent_to_this = rot_parent_to_this;
	if (disableParentCollision)
		links[i].m_flags |=BT_MULTIBODYLINKFLAGS_DISABLE_PARENT_COLLISION;

    links[i].updateCache();
}

void btMultiBody::setupRevolute(int i,
                              btScalar mass,
                              const btVector3 &inertia,
                              int parent,
                              const btQuaternion &zero_rot_parent_to_this,
                              const btVector3 &joint_axis,
                              const btVector3 &parent_axis_position,
                              const btVector3 &my_axis_position,
							  bool disableParentCollision)
{
    links[i].mass = mass;
    links[i].inertia = inertia;
    links[i].parent = parent;
    links[i].zero_rot_parent_to_this = zero_rot_parent_to_this;
    links[i].axis_top = joint_axis;
    links[i].axis_bottom = joint_axis.cross(my_axis_position);
    links[i].d_vector = my_axis_position;
    links[i].e_vector = parent_axis_position;
    links[i].is_revolute = true;
	if (disableParentCollision)
		links[i].m_flags |=BT_MULTIBODYLINKFLAGS_DISABLE_PARENT_COLLISION;
    links[i].updateCache();
}




	
int btMultiBody::getParent(int i) const
{
    return links[i].parent;
}

btScalar btMultiBody::getLinkMass(int i) const
{
    return links[i].mass;
}

const btVector3 & btMultiBody::getLinkInertia(int i) const
{
    return links[i].inertia;
}

btScalar btMultiBody::getJointPos(int i) const
{
    return links[i].joint_pos;
}

btScalar btMultiBody::getJointVel(int i) const
{
    return m_real_buf[6 + i];
}

void btMultiBody::setJointPos(int i, btScalar q)
{
    links[i].joint_pos = q;
    links[i].updateCache();
}

void btMultiBody::setJointVel(int i, btScalar qdot)
{
    m_real_buf[6 + i] = qdot;
}

const btVector3 & btMultiBody::getRVector(int i) const
{
    return links[i].cached_r_vector;
}

const btQuaternion & btMultiBody::getParentToLocalRot(int i) const
{
    return links[i].cached_rot_parent_to_this;
}

btVector3 btMultiBody::localPosToWorld(int i, const btVector3 &local_pos) const
{
    btVector3 result = local_pos;
    while (i != -1) {
        // 'result' is in frame i. transform it to frame parent(i)
        result += getRVector(i);
        result = quatRotate(getParentToLocalRot(i).inverse(),result);
        i = getParent(i);
    }

    // 'result' is now in the base frame. transform it to world frame
    result = quatRotate(getWorldToBaseRot().inverse() ,result);
    result += getBasePos();

    return result;
}

btVector3 btMultiBody::worldPosToLocal(int i, const btVector3 &world_pos) const
{
    if (i == -1) {
        // world to base
        return quatRotate(getWorldToBaseRot(),(world_pos - getBasePos()));
    } else {
        // find position in parent frame, then transform to current frame
        return quatRotate(getParentToLocalRot(i),worldPosToLocal(getParent(i), world_pos)) - getRVector(i);
    }
}

btVector3 btMultiBody::localDirToWorld(int i, const btVector3 &local_dir) const
{
    btVector3 result = local_dir;
    while (i != -1) {
        result = quatRotate(getParentToLocalRot(i).inverse() , result);
        i = getParent(i);
    }
    result = quatRotate(getWorldToBaseRot().inverse() , result);
    return result;
}

btVector3 btMultiBody::worldDirToLocal(int i, const btVector3 &world_dir) const
{
    if (i == -1) {
        return quatRotate(getWorldToBaseRot(), world_dir);
    } else {
        return quatRotate(getParentToLocalRot(i) ,worldDirToLocal(getParent(i), world_dir));
    }
}

void btMultiBody::compTreeLinkVelocities(btVector3 *omega, btVector3 *vel) const
{
	int num_links = getNumLinks();
    // Calculates the velocities of each link (and the base) in its local frame
    omega[0] = quatRotate(base_quat ,getBaseOmega());
    vel[0] = quatRotate(base_quat ,getBaseVel());
    
    for (int i = 0; i < num_links; ++i) {
        const int parent = links[i].parent;

        // transform parent vel into this frame, store in omega[i+1], vel[i+1]
        SpatialTransform(btMatrix3x3(links[i].cached_rot_parent_to_this), links[i].cached_r_vector,
                         omega[parent+1], vel[parent+1],
                         omega[i+1], vel[i+1]);

        // now add qidot * shat_i
        omega[i+1] += getJointVel(i) * links[i].axis_top;
        vel[i+1] += getJointVel(i) * links[i].axis_bottom;
    }
}

btScalar btMultiBody::getKineticEnergy() const
{
	int num_links = getNumLinks();
    // TODO: would be better not to allocate memory here
    btAlignedObjectArray<btVector3> omega;omega.resize(num_links+1);
	btAlignedObjectArray<btVector3> vel;vel.resize(num_links+1);
    compTreeLinkVelocities(&omega[0], &vel[0]);

    // we will do the factor of 0.5 at the end
    btScalar result = base_mass * vel[0].dot(vel[0]);
    result += omega[0].dot(base_inertia * omega[0]);
    
    for (int i = 0; i < num_links; ++i) {
        result += links[i].mass * vel[i+1].dot(vel[i+1]);
        result += omega[i+1].dot(links[i].inertia * omega[i+1]);
    }

    return 0.5f * result;
}

btVector3 btMultiBody::getAngularMomentum() const
{
	int num_links = getNumLinks();
    // TODO: would be better not to allocate memory here
    btAlignedObjectArray<btVector3> omega;omega.resize(num_links+1);
	btAlignedObjectArray<btVector3> vel;vel.resize(num_links+1);
    btAlignedObjectArray<btQuaternion> rot_from_world;rot_from_world.resize(num_links+1);
    compTreeLinkVelocities(&omega[0], &vel[0]);

    rot_from_world[0] = base_quat;
    btVector3 result = quatRotate(rot_from_world[0].inverse() , (base_inertia * omega[0]));

    for (int i = 0; i < num_links; ++i) {
        rot_from_world[i+1] = links[i].cached_rot_parent_to_this * rot_from_world[links[i].parent+1];
        result += (quatRotate(rot_from_world[i+1].inverse() , (links[i].inertia * omega[i+1])));
    }

    return result;
}


void btMultiBody::clearForcesAndTorques()
{
    base_force.setValue(0, 0, 0);
    base_torque.setValue(0, 0, 0);

    for (int i = 0; i < getNumLinks(); ++i) {
        links[i].applied_force.setValue(0, 0, 0);
        links[i].applied_torque.setValue(0, 0, 0);
        links[i].joint_torque = 0;
    }
}

void btMultiBody::clearVelocities()
{
	for (int i = 0; i < 6 + getNumLinks(); ++i) 
	{
		m_real_buf[i] = 0.f;
	}
}
void btMultiBody::addLinkForce(int i, const btVector3 &f)
{
    links[i].applied_force += f;
}

void btMultiBody::addLinkTorque(int i, const btVector3 &t)
{
    links[i].applied_torque += t;
}

void btMultiBody::addJointTorque(int i, btScalar Q)
{
    links[i].joint_torque += Q;
}

const btVector3 & btMultiBody::getLinkForce(int i) const
{
    return links[i].applied_force;
}

const btVector3 & btMultiBody::getLinkTorque(int i) const
{
    return links[i].applied_torque;
}

btScalar btMultiBody::getJointTorque(int i) const
{
    return links[i].joint_torque;
}


inline btMatrix3x3 vecMulVecTranspose(const btVector3& v0, const btVector3& v1Transposed)
{
		btVector3 row0 = btVector3( 
			v0.x() * v1Transposed.x(),
			v0.x() * v1Transposed.y(),
			v0.x() * v1Transposed.z());
		btVector3 row1 = btVector3( 
			v0.y() * v1Transposed.x(),
			v0.y() * v1Transposed.y(),
			v0.y() * v1Transposed.z());
		btVector3 row2 = btVector3( 
			v0.z() * v1Transposed.x(),
			v0.z() * v1Transposed.y(),
			v0.z() * v1Transposed.z());

        btMatrix3x3 m(row0[0],row0[1],row0[2],
						row1[0],row1[1],row1[2],
						row2[0],row2[1],row2[2]);
		return m;
}


void btMultiBody::stepVelocities(btScalar dt,
                               btAlignedObjectArray<btScalar> &scratch_r,
                               btAlignedObjectArray<btVector3> &scratch_v,
                               btAlignedObjectArray<btMatrix3x3> &scratch_m)
{
    // Implement Featherstone's algorithm to calculate joint accelerations (q_double_dot)
    // and the base linear & angular accelerations.

    // We apply damping forces in this routine as well as any external forces specified by the 
    // caller (via addBaseForce etc).

    // output should point to an array of 6 + num_links reals.
    // Format is: 3 angular accelerations (in world frame), 3 linear accelerations (in world frame),
    // num_links joint acceleration values.
    
	int num_links = getNumLinks();

    const btScalar DAMPING_K1_LINEAR = m_linearDamping;
	const btScalar DAMPING_K2_LINEAR = m_linearDamping;

	const btScalar DAMPING_K1_ANGULAR = m_angularDamping;
	const btScalar DAMPING_K2_ANGULAR= m_angularDamping;

    btVector3 base_vel = getBaseVel();
    btVector3 base_omega = getBaseOmega();

    // Temporary matrices/vectors -- use scratch space from caller
    // so that we don't have to keep reallocating every frame

    scratch_r.resize(2*num_links + 6);
    scratch_v.resize(8*num_links + 6);
    scratch_m.resize(4*num_links + 4);

    btScalar * r_ptr = &scratch_r[0];
    btScalar * output = &scratch_r[num_links];  // "output" holds the q_double_dot results
    btVector3 * v_ptr = &scratch_v[0];
    
    // vhat_i  (top = angular, bottom = linear part)
    btVector3 * vel_top_angular = v_ptr; v_ptr += num_links + 1;
    btVector3 * vel_bottom_linear = v_ptr; v_ptr += num_links + 1;

    // zhat_i^A
    btVector3 * zero_acc_top_angular = v_ptr; v_ptr += num_links + 1;
    btVector3 * zero_acc_bottom_linear = v_ptr; v_ptr += num_links + 1;

    // chat_i  (note NOT defined for the base)
    btVector3 * coriolis_top_angular = v_ptr; v_ptr += num_links;
    btVector3 * coriolis_bottom_linear = v_ptr; v_ptr += num_links;

    // top left, top right and bottom left blocks of Ihat_i^A.
    // bottom right block = transpose of top left block and is not stored.
    // Note: the top right and bottom left blocks are always symmetric matrices, but we don't make use of this fact currently.
    btMatrix3x3 * inertia_top_left = &scratch_m[num_links + 1];
    btMatrix3x3 * inertia_top_right = &scratch_m[2*num_links + 2];
    btMatrix3x3 * inertia_bottom_left = &scratch_m[3*num_links + 3];

    // Cached 3x3 rotation matrices from parent frame to this frame.
    btMatrix3x3 * rot_from_parent = &matrix_buf[0];
    btMatrix3x3 * rot_from_world = &scratch_m[0];

    // hhat_i, ahat_i
    // hhat is NOT stored for the base (but ahat is)
    btVector3 * h_top = num_links > 0 ? &vector_buf[0] : 0;
    btVector3 * h_bottom = num_links > 0 ? &vector_buf[num_links] : 0;
    btVector3 * accel_top = v_ptr; v_ptr += num_links + 1;
    btVector3 * accel_bottom = v_ptr; v_ptr += num_links + 1;

    // Y_i, D_i
    btScalar * Y = r_ptr; r_ptr += num_links;
    btScalar * D = num_links > 0 ? &m_real_buf[6 + num_links] : 0;

    // ptr to the joint accel part of the output
    btScalar * joint_accel = output + 6;


    // Start of the algorithm proper.
    
    // First 'upward' loop.
    // Combines CompTreeLinkVelocities and InitTreeLinks from Mirtich.

    rot_from_parent[0] = btMatrix3x3(base_quat);

    vel_top_angular[0] = rot_from_parent[0] * base_omega;
    vel_bottom_linear[0] = rot_from_parent[0] * base_vel;

    if (fixed_base) {
        zero_acc_top_angular[0] = zero_acc_bottom_linear[0] = btVector3(0,0,0);
    } else {
        zero_acc_top_angular[0] = - (rot_from_parent[0] * (base_force 
                                                   - base_mass*(DAMPING_K1_LINEAR+DAMPING_K2_LINEAR*base_vel.norm())*base_vel));
		
        zero_acc_bottom_linear[0] =
            - (rot_from_parent[0] * base_torque);

		if (m_useGyroTerm)
			zero_acc_bottom_linear[0]+=vel_top_angular[0].cross( base_inertia * vel_top_angular[0] );

        zero_acc_bottom_linear[0] += base_inertia * vel_top_angular[0] * (DAMPING_K1_ANGULAR + DAMPING_K2_ANGULAR*vel_top_angular[0].norm());

    }



    inertia_top_left[0] = btMatrix3x3(0,0,0,0,0,0,0,0,0);//::Zero();
	
	
    inertia_top_right[0].setValue(base_mass, 0, 0,
                            0, base_mass, 0,
                            0, 0, base_mass);
    inertia_bottom_left[0].setValue(base_inertia[0], 0, 0,
                              0, base_inertia[1], 0,
                              0, 0, base_inertia[2]);

    rot_from_world[0] = rot_from_parent[0];

    for (int i = 0; i < num_links; ++i) {
        const int parent = links[i].parent;
        rot_from_parent[i+1] = btMatrix3x3(links[i].cached_rot_parent_to_this);
		

        rot_from_world[i+1] = rot_from_parent[i+1] * rot_from_world[parent+1];
        
        // vhat_i = i_xhat_p(i) * vhat_p(i)
        SpatialTransform(rot_from_parent[i+1], links[i].cached_r_vector,
                         vel_top_angular[parent+1], vel_bottom_linear[parent+1],
                         vel_top_angular[i+1], vel_bottom_linear[i+1]);

        // we can now calculate chat_i
        // remember vhat_i is really vhat_p(i) (but in current frame) at this point
        coriolis_bottom_linear[i] = vel_top_angular[i+1].cross(vel_top_angular[i+1].cross(links[i].cached_r_vector))
            + 2 * vel_top_angular[i+1].cross(links[i].axis_bottom) * getJointVel(i);
        if (links[i].is_revolute) {
            coriolis_top_angular[i] = vel_top_angular[i+1].cross(links[i].axis_top) * getJointVel(i);
            coriolis_bottom_linear[i] += (getJointVel(i) * getJointVel(i)) * links[i].axis_top.cross(links[i].axis_bottom);
        } else {
            coriolis_top_angular[i] = btVector3(0,0,0);
        }
        
        // now set vhat_i to its true value by doing
        // vhat_i += qidot * shat_i
        vel_top_angular[i+1] += getJointVel(i) * links[i].axis_top;
        vel_bottom_linear[i+1] += getJointVel(i) * links[i].axis_bottom;

        // calculate zhat_i^A
        zero_acc_top_angular[i+1] = - (rot_from_world[i+1] * (links[i].applied_force));
        zero_acc_top_angular[i+1] += links[i].mass * (DAMPING_K1_LINEAR + DAMPING_K2_LINEAR*vel_bottom_linear[i+1].norm()) * vel_bottom_linear[i+1];

        zero_acc_bottom_linear[i+1] =
            - (rot_from_world[i+1] * links[i].applied_torque);
		if (m_useGyroTerm)
		{
			zero_acc_bottom_linear[i+1] += vel_top_angular[i+1].cross( links[i].inertia * vel_top_angular[i+1] );
		}

        zero_acc_bottom_linear[i+1] += links[i].inertia * vel_top_angular[i+1] * (DAMPING_K1_ANGULAR + DAMPING_K2_ANGULAR*vel_top_angular[i+1].norm());

        // calculate Ihat_i^A
        inertia_top_left[i+1] = btMatrix3x3(0,0,0,0,0,0,0,0,0);//::Zero();
        inertia_top_right[i+1].setValue(links[i].mass, 0, 0,
                                  0, links[i].mass, 0,
                                  0, 0, links[i].mass);
        inertia_bottom_left[i+1].setValue(links[i].inertia[0], 0, 0,
                                    0, links[i].inertia[1], 0,
                                    0, 0, links[i].inertia[2]);
    }


    // 'Downward' loop.
    // (part of TreeForwardDynamics in Mirtich.)
    for (int i = num_links - 1; i >= 0; --i) {

        h_top[i] = inertia_top_left[i+1] * links[i].axis_top + inertia_top_right[i+1] * links[i].axis_bottom;
        h_bottom[i] = inertia_bottom_left[i+1] * links[i].axis_top + inertia_top_left[i+1].transpose() * links[i].axis_bottom;
		btScalar val = SpatialDotProduct(links[i].axis_top, links[i].axis_bottom, h_top[i], h_bottom[i]);
        D[i] = val;
		Y[i] = links[i].joint_torque
            - SpatialDotProduct(links[i].axis_top, links[i].axis_bottom, zero_acc_top_angular[i+1], zero_acc_bottom_linear[i+1])
            - SpatialDotProduct(h_top[i], h_bottom[i], coriolis_top_angular[i], coriolis_bottom_linear[i]);

        const int parent = links[i].parent;

        
        // Ip += pXi * (Ii - hi hi' / Di) * iXp
        const btScalar one_over_di = 1.0f / D[i];

		


		const btMatrix3x3 TL = inertia_top_left[i+1]   - vecMulVecTranspose(one_over_di * h_top[i] , h_bottom[i]);
        const btMatrix3x3 TR = inertia_top_right[i+1]  - vecMulVecTranspose(one_over_di * h_top[i] , h_top[i]);
        const btMatrix3x3 BL = inertia_bottom_left[i+1]- vecMulVecTranspose(one_over_di * h_bottom[i] , h_bottom[i]);


        btMatrix3x3 r_cross;
        r_cross.setValue(
            0, -links[i].cached_r_vector[2], links[i].cached_r_vector[1],
            links[i].cached_r_vector[2], 0, -links[i].cached_r_vector[0],
            -links[i].cached_r_vector[1], links[i].cached_r_vector[0], 0);
        
        inertia_top_left[parent+1] += rot_from_parent[i+1].transpose() * ( TL - TR * r_cross ) * rot_from_parent[i+1];
        inertia_top_right[parent+1] += rot_from_parent[i+1].transpose() * TR * rot_from_parent[i+1];
        inertia_bottom_left[parent+1] += rot_from_parent[i+1].transpose() *
            (r_cross * (TL - TR * r_cross) + BL - TL.transpose() * r_cross) * rot_from_parent[i+1];
        
        
        // Zp += pXi * (Zi + Ii*ci + hi*Yi/Di)
        btVector3 in_top, in_bottom, out_top, out_bottom;
        const btScalar Y_over_D = Y[i] * one_over_di;
        in_top = zero_acc_top_angular[i+1]
            + inertia_top_left[i+1] * coriolis_top_angular[i]
            + inertia_top_right[i+1] * coriolis_bottom_linear[i]
            + Y_over_D * h_top[i];
        in_bottom = zero_acc_bottom_linear[i+1]
            + inertia_bottom_left[i+1] * coriolis_top_angular[i]
            + inertia_top_left[i+1].transpose() * coriolis_bottom_linear[i]
            + Y_over_D * h_bottom[i];
        InverseSpatialTransform(rot_from_parent[i+1], links[i].cached_r_vector,
                                in_top, in_bottom, out_top, out_bottom);
        zero_acc_top_angular[parent+1] += out_top;
        zero_acc_bottom_linear[parent+1] += out_bottom;
    }


    // Second 'upward' loop
    // (part of TreeForwardDynamics in Mirtich)

    if (fixed_base) 
	{
        accel_top[0] = accel_bottom[0] = btVector3(0,0,0);
    } 
	else 
	{
        if (num_links > 0) 
		{
            //Matrix<btScalar, 6, 6> Imatrix;
            //Imatrix.block<3,3>(0,0) = inertia_top_left[0];
            //Imatrix.block<3,3>(3,0) = inertia_bottom_left[0];
            //Imatrix.block<3,3>(0,3) = inertia_top_right[0];
            //Imatrix.block<3,3>(3,3) = inertia_top_left[0].transpose();
            //cached_imatrix_lu.reset(new Eigen::LU<Matrix<btScalar, 6, 6> >(Imatrix));  // TODO: Avoid memory allocation here?

			cached_inertia_top_left = inertia_top_left[0];
			cached_inertia_top_right = inertia_top_right[0];
			cached_inertia_lower_left = inertia_bottom_left[0];
			cached_inertia_lower_right= inertia_top_left[0].transpose();

        }
		btVector3 rhs_top (zero_acc_top_angular[0][0], zero_acc_top_angular[0][1], zero_acc_top_angular[0][2]);
		btVector3 rhs_bot (zero_acc_bottom_linear[0][0], zero_acc_bottom_linear[0][1], zero_acc_bottom_linear[0][2]);
        float result[6];

		solveImatrix(rhs_top, rhs_bot, result);
//		printf("result=%f,%f,%f,%f,%f,%f\n",result[0],result[0],result[0],result[0],result[0],result[0]);
        for (int i = 0; i < 3; ++i) {
            accel_top[0][i] = -result[i];
            accel_bottom[0][i] = -result[i+3];
        }

    }

    // now do the loop over the links
    for (int i = 0; i < num_links; ++i) {
        const int parent = links[i].parent;
        SpatialTransform(rot_from_parent[i+1], links[i].cached_r_vector,
                         accel_top[parent+1], accel_bottom[parent+1],
                         accel_top[i+1], accel_bottom[i+1]);
        joint_accel[i] = (Y[i] - SpatialDotProduct(h_top[i], h_bottom[i], accel_top[i+1], accel_bottom[i+1])) / D[i];
        accel_top[i+1] += coriolis_top_angular[i] + joint_accel[i] * links[i].axis_top;
        accel_bottom[i+1] += coriolis_bottom_linear[i] + joint_accel[i] * links[i].axis_bottom;
    }

    // transform base accelerations back to the world frame.
    btVector3 omegadot_out = rot_from_parent[0].transpose() * accel_top[0];
	output[0] = omegadot_out[0];
	output[1] = omegadot_out[1];
	output[2] = omegadot_out[2];

    btVector3 vdot_out = rot_from_parent[0].transpose() * accel_bottom[0];
	output[3] = vdot_out[0];
	output[4] = vdot_out[1];
	output[5] = vdot_out[2];
    // Final step: add the accelerations (times dt) to the velocities.
    applyDeltaVee(output, dt);

	
}



void btMultiBody::solveImatrix(const btVector3& rhs_top, const btVector3& rhs_bot, float result[6]) const
{
	int num_links = getNumLinks();
	///solve I * x = rhs, so the result = invI * rhs
    if (num_links == 0) 
	{
		// in the case of 0 links (i.e. a plain rigid body, not a multibody) rhs * invI is easier
        result[0] = rhs_bot[0] / base_inertia[0];
        result[1] = rhs_bot[1] / base_inertia[1];
        result[2] = rhs_bot[2] / base_inertia[2];
        result[3] = rhs_top[0] / base_mass;
        result[4] = rhs_top[1] / base_mass;
        result[5] = rhs_top[2] / base_mass;
    } else 
	{
		/// Special routine for calculating the inverse of a spatial inertia matrix
		///the 6x6 matrix is stored as 4 blocks of 3x3 matrices
		btMatrix3x3 Binv = cached_inertia_top_right.inverse()*-1.f;
		btMatrix3x3 tmp = cached_inertia_lower_right * Binv;
		btMatrix3x3 invIupper_right = (tmp * cached_inertia_top_left + cached_inertia_lower_left).inverse();
		tmp = invIupper_right * cached_inertia_lower_right;
		btMatrix3x3 invI_upper_left = (tmp * Binv);
		btMatrix3x3 invI_lower_right = (invI_upper_left).transpose();
		tmp = cached_inertia_top_left  * invI_upper_left;
		tmp[0][0]-= 1.0;
		tmp[1][1]-= 1.0;
		tmp[2][2]-= 1.0;
		btMatrix3x3 invI_lower_left = (Binv * tmp);

		//multiply result = invI * rhs
		{
		  btVector3 vtop = invI_upper_left*rhs_top;
		  btVector3 tmp;
		  tmp = invIupper_right * rhs_bot;
		  vtop += tmp;
		  btVector3 vbot = invI_lower_left*rhs_top;
		  tmp = invI_lower_right * rhs_bot;
		  vbot += tmp;
		  result[0] = vtop[0];
		  result[1] = vtop[1];
		  result[2] = vtop[2];
		  result[3] = vbot[0];
		  result[4] = vbot[1];
		  result[5] = vbot[2];
		}
		
    }
}


void btMultiBody::calcAccelerationDeltas(const btScalar *force, btScalar *output,
                                       btAlignedObjectArray<btScalar> &scratch_r, btAlignedObjectArray<btVector3> &scratch_v) const
{
    // Temporary matrices/vectors -- use scratch space from caller
    // so that we don't have to keep reallocating every frame
	int num_links = getNumLinks();
    scratch_r.resize(num_links);
    scratch_v.resize(4*num_links + 4);

    btScalar * r_ptr = num_links == 0 ? 0 : &scratch_r[0];
    btVector3 * v_ptr = &scratch_v[0];
    
    // zhat_i^A (scratch space)
    btVector3 * zero_acc_top_angular = v_ptr; v_ptr += num_links + 1;
    btVector3 * zero_acc_bottom_linear = v_ptr; v_ptr += num_links + 1;

    // rot_from_parent (cached from calcAccelerations)
    const btMatrix3x3 * rot_from_parent = &matrix_buf[0];

    // hhat (cached), accel (scratch)
    const btVector3 * h_top = num_links > 0 ? &vector_buf[0] : 0;
    const btVector3 * h_bottom = num_links > 0 ? &vector_buf[num_links] : 0;
    btVector3 * accel_top = v_ptr; v_ptr += num_links + 1;
    btVector3 * accel_bottom = v_ptr; v_ptr += num_links + 1;

    // Y_i (scratch), D_i (cached)
    btScalar * Y = r_ptr; r_ptr += num_links;
    const btScalar * D = num_links > 0 ? &m_real_buf[6 + num_links] : 0;

    btAssert(num_links == 0 || r_ptr - &scratch_r[0] == scratch_r.size());
    btAssert(v_ptr - &scratch_v[0] == scratch_v.size());


    
    // First 'upward' loop.
    // Combines CompTreeLinkVelocities and InitTreeLinks from Mirtich.

    btVector3 input_force(force[3],force[4],force[5]);
    btVector3 input_torque(force[0],force[1],force[2]);
    
    // Fill in zero_acc
    // -- set to force/torque on the base, zero otherwise
    if (fixed_base) 
	{
        zero_acc_top_angular[0] = zero_acc_bottom_linear[0] = btVector3(0,0,0);
    } else 
	{
        zero_acc_top_angular[0] = - (rot_from_parent[0] * input_force);
        zero_acc_bottom_linear[0] =  - (rot_from_parent[0] * input_torque);
    }
    for (int i = 0; i < num_links; ++i) 
	{
        zero_acc_top_angular[i+1] = zero_acc_bottom_linear[i+1] = btVector3(0,0,0);
    }

    // 'Downward' loop.
    for (int i = num_links - 1; i >= 0; --i) 
	{

        Y[i] = - SpatialDotProduct(links[i].axis_top, links[i].axis_bottom, zero_acc_top_angular[i+1], zero_acc_bottom_linear[i+1]);
        Y[i] += force[6 + i];  // add joint torque
        
        const int parent = links[i].parent;
        
        // Zp += pXi * (Zi + hi*Yi/Di)
        btVector3 in_top, in_bottom, out_top, out_bottom;
        const btScalar Y_over_D = Y[i] / D[i];
        in_top = zero_acc_top_angular[i+1] + Y_over_D * h_top[i];
        in_bottom = zero_acc_bottom_linear[i+1] + Y_over_D * h_bottom[i];
        InverseSpatialTransform(rot_from_parent[i+1], links[i].cached_r_vector,
                                in_top, in_bottom, out_top, out_bottom);
        zero_acc_top_angular[parent+1] += out_top;
        zero_acc_bottom_linear[parent+1] += out_bottom;
    }

    // ptr to the joint accel part of the output
    btScalar * joint_accel = output + 6;

    // Second 'upward' loop
    if (fixed_base) 
	{
        accel_top[0] = accel_bottom[0] = btVector3(0,0,0);
    } else 
	{
		btVector3 rhs_top (zero_acc_top_angular[0][0], zero_acc_top_angular[0][1], zero_acc_top_angular[0][2]);
		btVector3 rhs_bot (zero_acc_bottom_linear[0][0], zero_acc_bottom_linear[0][1], zero_acc_bottom_linear[0][2]);
		
		float result[6];
        solveImatrix(rhs_top,rhs_bot, result);
	//	printf("result=%f,%f,%f,%f,%f,%f\n",result[0],result[0],result[0],result[0],result[0],result[0]);

        for (int i = 0; i < 3; ++i) {
            accel_top[0][i] = -result[i];
            accel_bottom[0][i] = -result[i+3];
        }

    }
    
    // now do the loop over the links
    for (int i = 0; i < num_links; ++i) {
        const int parent = links[i].parent;
        SpatialTransform(rot_from_parent[i+1], links[i].cached_r_vector,
                         accel_top[parent+1], accel_bottom[parent+1],
                         accel_top[i+1], accel_bottom[i+1]);
        joint_accel[i] = (Y[i] - SpatialDotProduct(h_top[i], h_bottom[i], accel_top[i+1], accel_bottom[i+1])) / D[i];
        accel_top[i+1] += joint_accel[i] * links[i].axis_top;
        accel_bottom[i+1] += joint_accel[i] * links[i].axis_bottom;
    }

    // transform base accelerations back to the world frame.
    btVector3 omegadot_out;
    omegadot_out = rot_from_parent[0].transpose() * accel_top[0];
	output[0] = omegadot_out[0];
	output[1] = omegadot_out[1];
	output[2] = omegadot_out[2];

    btVector3 vdot_out;
    vdot_out = rot_from_parent[0].transpose() * accel_bottom[0];

	output[3] = vdot_out[0];
	output[4] = vdot_out[1];
	output[5] = vdot_out[2];
}

void btMultiBody::stepPositions(btScalar dt)
{
	int num_links = getNumLinks();
    // step position by adding dt * velocity
	btVector3 v = getBaseVel();
    base_pos += dt * v;

    // "exponential map" method for the rotation
    btVector3 base_omega = getBaseOmega();
    const btScalar omega_norm = base_omega.norm();
    const btScalar omega_times_dt = omega_norm * dt;
    const btScalar SMALL_ROTATION_ANGLE = 0.02f; // Theoretically this should be ~ pow(FLT_EPSILON,0.25) which is ~ 0.0156
    if (fabs(omega_times_dt) < SMALL_ROTATION_ANGLE) 
	{
        const btScalar xsq = omega_times_dt * omega_times_dt;     // |omega|^2 * dt^2
        const btScalar sin_term = dt * (xsq / 48.0f - 0.5f);      // -sin(0.5*dt*|omega|) / |omega|
        const btScalar cos_term = 1.0f - xsq / 8.0f;              // cos(0.5*dt*|omega|)
        base_quat = base_quat * btQuaternion(sin_term * base_omega[0],sin_term * base_omega[1],sin_term * base_omega[2],cos_term);
    } else 
	{
        base_quat = base_quat * btQuaternion(base_omega / omega_norm,-omega_times_dt);
    }

    // Make sure the quaternion represents a valid rotation.
    // (Not strictly necessary, but helps prevent any round-off errors from building up.)
    base_quat.normalize();

    // Finally we can update joint_pos for each of the links
    for (int i = 0; i < num_links; ++i) 
	{
		float jointVel = getJointVel(i);
        links[i].joint_pos += dt * jointVel;
        links[i].updateCache();
    }
}

void btMultiBody::fillContactJacobian(int link,
                                    const btVector3 &contact_point,
                                    const btVector3 &normal,
                                    btScalar *jac,
                                    btAlignedObjectArray<btScalar> &scratch_r,
                                    btAlignedObjectArray<btVector3> &scratch_v,
                                    btAlignedObjectArray<btMatrix3x3> &scratch_m) const
{
    // temporary space
	int num_links = getNumLinks();
    scratch_v.resize(2*num_links + 2);
    scratch_m.resize(num_links + 1);

    btVector3 * v_ptr = &scratch_v[0];
    btVector3 * p_minus_com = v_ptr; v_ptr += num_links + 1;
    btVector3 * n_local = v_ptr; v_ptr += num_links + 1;
    btAssert(v_ptr - &scratch_v[0] == scratch_v.size());

    scratch_r.resize(num_links);
    btScalar * results = num_links > 0 ? &scratch_r[0] : 0;

    btMatrix3x3 * rot_from_world = &scratch_m[0];

    const btVector3 p_minus_com_world = contact_point - base_pos;

    rot_from_world[0] = btMatrix3x3(base_quat);

    p_minus_com[0] = rot_from_world[0] * p_minus_com_world;
    n_local[0] = rot_from_world[0] * normal;
    
    // omega coeffients first.
    btVector3 omega_coeffs;
    omega_coeffs = p_minus_com_world.cross(normal);
	jac[0] = omega_coeffs[0];
	jac[1] = omega_coeffs[1];
	jac[2] = omega_coeffs[2];
    // then v coefficients
    jac[3] = normal[0];
    jac[4] = normal[1];
    jac[5] = normal[2];

    // Set remaining jac values to zero for now.
    for (int i = 6; i < 6 + num_links; ++i) {
        jac[i] = 0;
    }

    // Qdot coefficients, if necessary.
    if (num_links > 0 && link > -1) {

        // TODO: speed this up -- don't calculate for links we don't need.
        // (Also, we are making 3 separate calls to this function, for the normal & the 2 friction directions,
        // which is resulting in repeated work being done...)

        // calculate required normals & positions in the local frames.
        for (int i = 0; i < num_links; ++i) {

            // transform to local frame
            const int parent = links[i].parent;
            const btMatrix3x3 mtx(links[i].cached_rot_parent_to_this);
            rot_from_world[i+1] = mtx * rot_from_world[parent+1];

            n_local[i+1] = mtx * n_local[parent+1];
            p_minus_com[i+1] = mtx * p_minus_com[parent+1] - links[i].cached_r_vector;

            // calculate the jacobian entry
            if (links[i].is_revolute) {
                results[i] = n_local[i+1].dot( links[i].axis_top.cross(p_minus_com[i+1]) + links[i].axis_bottom );
            } else {
                results[i] = n_local[i+1].dot( links[i].axis_bottom );
            }
        }

        // Now copy through to output.
        while (link != -1) {
            jac[6 + link] = results[link];
            link = links[link].parent;
        }
    }
}

void btMultiBody::wakeUp()
{
    awake = true;
}

void btMultiBody::goToSleep()
{
    awake = false;
}

void btMultiBody::checkMotionAndSleepIfRequired(btScalar timestep)
{
	int num_links = getNumLinks();
	extern bool gDisableDeactivation;
    if (!can_sleep || gDisableDeactivation) 
	{
		awake = true;
		sleep_timer = 0;
		return;
	}

    // motion is computed as omega^2 + v^2 + (sum of squares of joint velocities)
    btScalar motion = 0;
    for (int i = 0; i < 6 + num_links; ++i) {
        motion += m_real_buf[i] * m_real_buf[i];
    }

    if (motion < SLEEP_EPSILON) {
        sleep_timer += timestep;
        if (sleep_timer > SLEEP_TIMEOUT) {
            goToSleep();
        }
    } else {
        sleep_timer = 0;
		if (!awake)
			wakeUp();
    }
}
