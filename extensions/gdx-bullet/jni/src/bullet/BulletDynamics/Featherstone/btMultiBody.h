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


#ifndef BT_MULTIBODY_H
#define BT_MULTIBODY_H

#include "LinearMath/btScalar.h"
#include "LinearMath/btVector3.h"
#include "LinearMath/btQuaternion.h"
#include "LinearMath/btMatrix3x3.h"
#include "LinearMath/btAlignedObjectArray.h"


#include "btMultiBodyLink.h"
class btMultiBodyLinkCollider;

class btMultiBody 
{
public:

    
	BT_DECLARE_ALIGNED_ALLOCATOR();

    //
    // initialization
    //
    
    btMultiBody(int n_links,                // NOT including the base
              btScalar mass,                // mass of base
              const btVector3 &inertia,    // inertia of base, in base frame; assumed diagonal
              bool fixed_base_,           // whether the base is fixed (true) or can move (false)
              bool can_sleep_);

    ~btMultiBody();
    
    void setupPrismatic(int i,             // 0 to num_links-1
                        btScalar mass,
                        const btVector3 &inertia,       // in my frame; assumed diagonal
                        int parent,
                        const btQuaternion &rot_parent_to_this,  // rotate points in parent frame to my frame.
                        const btVector3 &joint_axis,             // in my frame
                        const btVector3 &r_vector_when_q_zero,  // vector from parent COM to my COM, in my frame, when q = 0.
						bool disableParentCollision=false
						);

    void setupRevolute(int i,            // 0 to num_links-1
                       btScalar mass,
                       const btVector3 &inertia,
                       int parent,
                       const btQuaternion &zero_rot_parent_to_this,  // rotate points in parent frame to this frame, when q = 0
                       const btVector3 &joint_axis,    // in my frame
                       const btVector3 &parent_axis_position,    // vector from parent COM to joint axis, in PARENT frame
                       const btVector3 &my_axis_position,       // vector from joint axis to my COM, in MY frame
					   bool disableParentCollision=false);
	
	const btMultibodyLink& getLink(int index) const
	{
		return links[index];
	}

	btMultibodyLink& getLink(int index)
	{
		return links[index];
	}


	void setBaseCollider(btMultiBodyLinkCollider* collider)//collider can be NULL to disable collision for the base
	{
		m_baseCollider = collider;
	}
	const btMultiBodyLinkCollider* getBaseCollider() const
	{
		return m_baseCollider;
	}
	btMultiBodyLinkCollider* getBaseCollider()
	{
		return m_baseCollider;
	}

    //
    // get parent
    // input: link num from 0 to num_links-1
    // output: link num from 0 to num_links-1, OR -1 to mean the base.
    //
    int getParent(int link_num) const;
    
    
    //
    // get number of links, masses, moments of inertia
    //

    int getNumLinks() const { return links.size(); }
    btScalar getBaseMass() const { return base_mass; }
    const btVector3 & getBaseInertia() const { return base_inertia; }
    btScalar getLinkMass(int i) const;
    const btVector3 & getLinkInertia(int i) const;
    

    //
    // change mass (incomplete: can only change base mass and inertia at present)
    //

    void setBaseMass(btScalar mass) { base_mass = mass; }
    void setBaseInertia(const btVector3 &inertia) { base_inertia = inertia; }


    //
    // get/set pos/vel/rot/omega for the base link
    //

    const btVector3 & getBasePos() const { return base_pos; }    // in world frame
    const btVector3 getBaseVel() const 
	{ 
		return btVector3(m_real_buf[3],m_real_buf[4],m_real_buf[5]); 
	}     // in world frame
    const btQuaternion & getWorldToBaseRot() const 
	{ 
		return base_quat; 
	}     // rotates world vectors into base frame
    btVector3 getBaseOmega() const { return btVector3(m_real_buf[0],m_real_buf[1],m_real_buf[2]); }   // in world frame

    void setBasePos(const btVector3 &pos) 
	{ 
		base_pos = pos; 
	}
    void setBaseVel(const btVector3 &vel) 
	{ 

		m_real_buf[3]=vel[0]; m_real_buf[4]=vel[1]; m_real_buf[5]=vel[2]; 
	}
    void setWorldToBaseRot(const btQuaternion &rot) 
	{ 
		base_quat = rot; 
	}
    void setBaseOmega(const btVector3 &omega) 
	{ 
		m_real_buf[0]=omega[0]; 
		m_real_buf[1]=omega[1]; 
		m_real_buf[2]=omega[2]; 
	}


    //
    // get/set pos/vel for child links (i = 0 to num_links-1)
    //

    btScalar getJointPos(int i) const;
    btScalar getJointVel(int i) const;

    void setJointPos(int i, btScalar q);
    void setJointVel(int i, btScalar qdot);

    //
    // direct access to velocities as a vector of 6 + num_links elements.
    // (omega first, then v, then joint velocities.)
    //
    const btScalar * getVelocityVector() const 
	{ 
		return &m_real_buf[0]; 
	}
/*    btScalar * getVelocityVector() 
	{ 
		return &real_buf[0]; 
	}
  */  

    //
    // get the frames of reference (positions and orientations) of the child links
    // (i = 0 to num_links-1)
    //

    const btVector3 & getRVector(int i) const;   // vector from COM(parent(i)) to COM(i), in frame i's coords
    const btQuaternion & getParentToLocalRot(int i) const;   // rotates vectors in frame parent(i) to vectors in frame i.


    //
    // transform vectors in local frame of link i to world frame (or vice versa)
    //
    btVector3 localPosToWorld(int i, const btVector3 &vec) const;
    btVector3 localDirToWorld(int i, const btVector3 &vec) const;
    btVector3 worldPosToLocal(int i, const btVector3 &vec) const;
    btVector3 worldDirToLocal(int i, const btVector3 &vec) const;
    

    //
    // calculate kinetic energy and angular momentum
    // useful for debugging.
    //

    btScalar getKineticEnergy() const;
    btVector3 getAngularMomentum() const;
    

    //
    // set external forces and torques. Note all external forces/torques are given in the WORLD frame.
    //

    void clearForcesAndTorques();
	void clearVelocities();

    void addBaseForce(const btVector3 &f) 
	{ 
		base_force += f; 
	}
    void addBaseTorque(const btVector3 &t) { base_torque += t; }
    void addLinkForce(int i, const btVector3 &f);
    void addLinkTorque(int i, const btVector3 &t);
    void addJointTorque(int i, btScalar Q);

    const btVector3 & getBaseForce() const { return base_force; }
    const btVector3 & getBaseTorque() const { return base_torque; }
    const btVector3 & getLinkForce(int i) const;
    const btVector3 & getLinkTorque(int i) const;
    btScalar getJointTorque(int i) const;


    //
    // dynamics routines.
    //

    // timestep the velocities (given the external forces/torques set using addBaseForce etc).
    // also sets up caches for calcAccelerationDeltas.
    //
    // Note: the caller must provide three vectors which are used as
    // temporary scratch space. The idea here is to reduce dynamic
    // memory allocation: the same scratch vectors can be re-used
    // again and again for different Multibodies, instead of each
    // btMultiBody allocating (and then deallocating) their own
    // individual scratch buffers. This gives a considerable speed
    // improvement, at least on Windows (where dynamic memory
    // allocation appears to be fairly slow).
    //
    void stepVelocities(btScalar dt,
                        btAlignedObjectArray<btScalar> &scratch_r,
                        btAlignedObjectArray<btVector3> &scratch_v,
                        btAlignedObjectArray<btMatrix3x3> &scratch_m);

    // calcAccelerationDeltas
    // input: force vector (in same format as jacobian, i.e.:
    //                      3 torque values, 3 force values, num_links joint torque values)
    // output: 3 omegadot values, 3 vdot values, num_links q_double_dot values
    // (existing contents of output array are replaced)
    // stepVelocities must have been called first.
    void calcAccelerationDeltas(const btScalar *force, btScalar *output,
                                btAlignedObjectArray<btScalar> &scratch_r,
                                btAlignedObjectArray<btVector3> &scratch_v) const;

    // apply a delta-vee directly. used in sequential impulses code.
    void applyDeltaVee(const btScalar * delta_vee) 
	{

        for (int i = 0; i < 6 + getNumLinks(); ++i) 
		{
			m_real_buf[i] += delta_vee[i];
		}
		
    }
    void applyDeltaVee(const btScalar * delta_vee, btScalar multiplier) 
	{
		btScalar sum = 0;
        for (int i = 0; i < 6 + getNumLinks(); ++i)
		{
			sum += delta_vee[i]*multiplier*delta_vee[i]*multiplier;
		}
		btScalar l = btSqrt(sum);
		/*
		static btScalar maxl = -1e30f;
		if (l>maxl)
		{
			maxl=l;
	//		printf("maxl=%f\n",maxl);
		}
		*/
		if (l>m_maxAppliedImpulse)
		{
//			printf("exceeds 100: l=%f\n",maxl);
			multiplier *= m_maxAppliedImpulse/l;
		}

        for (int i = 0; i < 6 + getNumLinks(); ++i)
		{
			sum += delta_vee[i]*multiplier*delta_vee[i]*multiplier;
			m_real_buf[i] += delta_vee[i] * multiplier;
		}
    }

    // timestep the positions (given current velocities).
    void stepPositions(btScalar dt);


    //
    // contacts
    //

    // This routine fills out a contact constraint jacobian for this body.
    // the 'normal' supplied must be -n for body1 or +n for body2 of the contact.
    // 'normal' & 'contact_point' are both given in world coordinates.
    void fillContactJacobian(int link,
                             const btVector3 &contact_point,
                             const btVector3 &normal,
                             btScalar *jac,
                             btAlignedObjectArray<btScalar> &scratch_r,
                             btAlignedObjectArray<btVector3> &scratch_v,
                             btAlignedObjectArray<btMatrix3x3> &scratch_m) const;


    //
    // sleeping
    //
	void	setCanSleep(bool canSleep)
	{
		can_sleep = canSleep;
	}

    bool isAwake() const { return awake; }
    void wakeUp();
    void goToSleep();
    void checkMotionAndSleepIfRequired(btScalar timestep);
    
	bool hasFixedBase() const
	{
		    return fixed_base;
	}

	int getCompanionId() const
	{
		return m_companionId;
	}
	void setCompanionId(int id)
	{
		//printf("for %p setCompanionId(%d)\n",this, id);
		m_companionId = id;
	}

	void setNumLinks(int numLinks)//careful: when changing the number of links, make sure to re-initialize or update existing links
	{
		links.resize(numLinks);
	}

	btScalar getLinearDamping() const
	{
			return m_linearDamping;
	}
	void setLinearDamping( btScalar damp)
	{
		m_linearDamping = damp;
	}
	btScalar getAngularDamping() const
	{
		return m_angularDamping;
	}
		
	bool getUseGyroTerm() const
	{
		return m_useGyroTerm;
	}
	void setUseGyroTerm(bool useGyro)
	{
		m_useGyroTerm = useGyro;
	}
	btScalar	getMaxAppliedImpulse() const
	{
		return m_maxAppliedImpulse;
	}
	void	setMaxAppliedImpulse(btScalar maxImp)
	{
		m_maxAppliedImpulse = maxImp;
	}

	void	setHasSelfCollision(bool hasSelfCollision)
	{
		m_hasSelfCollision = hasSelfCollision;
	}
	bool hasSelfCollision() const
	{
		return m_hasSelfCollision;
	}

private:
    btMultiBody(const btMultiBody &);  // not implemented
    void operator=(const btMultiBody &);  // not implemented

    void compTreeLinkVelocities(btVector3 *omega, btVector3 *vel) const;

	void solveImatrix(const btVector3& rhs_top, const btVector3& rhs_bot, float result[6]) const;
    
	
private:

	btMultiBodyLinkCollider* m_baseCollider;//can be NULL

    btVector3 base_pos;       // position of COM of base (world frame)
    btQuaternion base_quat;   // rotates world points into base frame

    btScalar base_mass;         // mass of the base
    btVector3 base_inertia;   // inertia of the base (in local frame; diagonal)

    btVector3 base_force;     // external force applied to base. World frame.
    btVector3 base_torque;    // external torque applied to base. World frame.
    
    btAlignedObjectArray<btMultibodyLink> links;    // array of links, excluding the base. index from 0 to num_links-1.
	btAlignedObjectArray<btMultiBodyLinkCollider*> m_colliders;
    
    //
    // real_buf:
    //  offset         size            array
    //   0              6 + num_links   v (base_omega; base_vel; joint_vels)
    //   6+num_links    num_links       D
    //
    // vector_buf:
    //  offset         size         array
    //   0              num_links    h_top
    //   num_links      num_links    h_bottom
    //
    // matrix_buf:
    //  offset         size         array
    //   0              num_links+1  rot_from_parent
    //
    
    btAlignedObjectArray<btScalar> m_real_buf;
    btAlignedObjectArray<btVector3> vector_buf;
    btAlignedObjectArray<btMatrix3x3> matrix_buf;

    //std::auto_ptr<Eigen::LU<Eigen::Matrix<btScalar, 6, 6> > > cached_imatrix_lu;

	btMatrix3x3 cached_inertia_top_left;
	btMatrix3x3 cached_inertia_top_right;
	btMatrix3x3 cached_inertia_lower_left;
	btMatrix3x3 cached_inertia_lower_right;

    bool fixed_base;

    // Sleep parameters.
    bool awake;
    bool can_sleep;
    btScalar sleep_timer;

	int	m_companionId;
	btScalar	m_linearDamping;
	btScalar	m_angularDamping;
	bool	m_useGyroTerm;
	btScalar	m_maxAppliedImpulse;
	bool		m_hasSelfCollision;
};

#endif
