#include "invdyn_bullet_comparison.hpp"
#include <cmath>
#include "BulletInverseDynamics/IDConfig.hpp"
#include "BulletInverseDynamics/MultiBodyTree.hpp"
#include "btBulletDynamicsCommon.h"
#include "BulletDynamics/Featherstone/btMultiBodyConstraintSolver.h"
#include "BulletDynamics/Featherstone/btMultiBodyDynamicsWorld.h"
#include "BulletDynamics/Featherstone/btMultiBodyLinkCollider.h"
#include "BulletDynamics/Featherstone/btMultiBodyPoint2Point.h"

namespace btInverseDynamics {
int compareInverseAndForwardDynamics(vecx &q, vecx &u, vecx &dot_u, btVector3 &gravity, bool verbose,
                                     btMultiBody *btmb, MultiBodyTree *id_tree, double *pos_error,
                                     double *acc_error) {
// call function and return -1 if it does, printing an error_message
#define RETURN_ON_FAILURE(x)                                                                       \
    do {                                                                                           \
        if (-1 == x) {                                                                             \
            error_message("calling " #x "\n");                                                     \
            return -1;                                                                             \
        }                                                                                          \
    } while (0)

    if (verbose) {
        printf("\n ===================================== \n");
    }
    vecx joint_forces(q.size());

    // set positions and velocities for btMultiBody
    // base link
    mat33 world_T_base;
    vec3 world_pos_base;
    btTransform base_transform;
    vec3 base_velocity;
    vec3 base_angular_velocity;

    RETURN_ON_FAILURE(id_tree->setGravityInWorldFrame(gravity));
    RETURN_ON_FAILURE(id_tree->getBodyOrigin(0, &world_pos_base));
    RETURN_ON_FAILURE(id_tree->getBodyTransform(0, &world_T_base));
    RETURN_ON_FAILURE(id_tree->getBodyAngularVelocity(0, &base_angular_velocity));
    RETURN_ON_FAILURE(id_tree->getBodyLinearVelocityCoM(0, &base_velocity));

    base_transform.setBasis(world_T_base);
    base_transform.setOrigin(world_pos_base);
    btmb->setBaseWorldTransform(base_transform);
    btmb->setBaseOmega(base_angular_velocity);
    btmb->setBaseVel(base_velocity);
    btmb->setLinearDamping(0);
    btmb->setAngularDamping(0);

    // remaining links
    int q_index;
    if (btmb->hasFixedBase()) {
        q_index = 0;
    } else {
        q_index = 6;
    }
    if (verbose) {
        printf("bt:num_links= %d, num_dofs= %d\n", btmb->getNumLinks(), btmb->getNumDofs());
    }
    for (int l = 0; l < btmb->getNumLinks(); l++) {
        const btMultibodyLink &link = btmb->getLink(l);
        if (verbose) {
            printf("link %d, pos_var_count= %d, dof_count= %d\n", l, link.m_posVarCount,
                   link.m_dofCount);
        }
        if (link.m_posVarCount == 1) {
            btmb->setJointPosMultiDof(l, &q(q_index));
            btmb->setJointVelMultiDof(l, &u(q_index));
            if (verbose) {
                printf("set q[%d]= %f, u[%d]= %f\n", q_index, q(q_index), q_index, u(q_index));
            }
            q_index++;
        }
    }
    // sanity check
    if (q_index != q.size()) {
        error_message("error in number of dofs for btMultibody and MultiBodyTree\n");
        return -1;
    }

    // run inverse dynamics to determine joint_forces for given q, u, dot_u
    if (-1 == id_tree->calculateInverseDynamics(q, u, dot_u, &joint_forces)) {
        error_message("calculating inverse dynamics\n");
        return -1;
    }

    // set up bullet forward dynamics model
    btScalar dt = 0;
    btAlignedObjectArray<btScalar> scratch_r;
    btAlignedObjectArray<btVector3> scratch_v;
    btAlignedObjectArray<btMatrix3x3> scratch_m;
    // this triggers switch between using either appliedConstraintForce or appliedForce
    bool isConstraintPass = false;
    // apply gravity forces for btMultiBody model. Must be done manually.
    btmb->addBaseForce(btmb->getBaseMass() * gravity);

    for (int link = 0; link < btmb->getNumLinks(); link++) {
        btmb->addLinkForce(link, gravity * btmb->getLinkMass(link));
        if (verbose) {
            printf("link %d, applying gravity %f %f %f\n", link,
                   gravity[0] * btmb->getLinkMass(link), gravity[1] * btmb->getLinkMass(link),
                   gravity[2] * btmb->getLinkMass(link));
        }
    }

    // apply generalized forces
    if (btmb->hasFixedBase()) {
        q_index = 0;
    } else {
        vec3 base_force;
        base_force(0) = joint_forces(3);
        base_force(1) = joint_forces(4);
        base_force(2) = joint_forces(5);

        vec3 base_moment;
        base_moment(0) = joint_forces(0);
        base_moment(1) = joint_forces(1);
        base_moment(2) = joint_forces(2);

        btmb->addBaseForce(world_T_base * base_force);
        btmb->addBaseTorque(world_T_base * base_moment);
        if (verbose) {
            printf("base force from id: %f %f %f\n", joint_forces(3), joint_forces(4),
                   joint_forces(5));
            printf("base moment from id: %f %f %f\n", joint_forces(0), joint_forces(1),
                   joint_forces(2));
        }
        q_index = 6;
    }

    for (int l = 0; l < btmb->getNumLinks(); l++) {
        const btMultibodyLink &link = btmb->getLink(l);
        if (link.m_posVarCount == 1) {
            if (verbose) {
                printf("id:joint_force[%d]= %f, applied to link %d\n", q_index,
                       joint_forces(q_index), l);
            }
            btmb->addJointTorque(l, joint_forces(q_index));
            q_index++;
        }
    }

    // sanity check
    if (q_index != q.size()) {
        error_message("error in number of dofs for btMultibody and MultiBodyTree\n");
        return -1;
    }

    // run forward kinematics & forward dynamics
    btAlignedObjectArray<btQuaternion> world_to_local;
    btAlignedObjectArray<btVector3> local_origin;
    btmb->forwardKinematics(world_to_local, local_origin);
    btmb->computeAccelerationsArticulatedBodyAlgorithmMultiDof(dt, scratch_r, scratch_v, scratch_m, isConstraintPass);

    // read generalized accelerations back from btMultiBody
    // the mapping from scratch variables to accelerations is taken from the implementation
    // of stepVelocitiesMultiDof
    btScalar *base_accel = &scratch_r[btmb->getNumDofs()];
    btScalar *joint_accel = base_accel + 6;
    *acc_error = 0;
    int dot_u_offset = 0;
    if (btmb->hasFixedBase()) {
        dot_u_offset = 0;
    } else {
        dot_u_offset = 6;
    }

    if (true == btmb->hasFixedBase()) {
        for (int i = 0; i < btmb->getNumDofs(); i++) {
            if (verbose) {
                printf("bt:ddot_q[%d]= %f, id:ddot_q= %e, diff= %e\n", i, joint_accel[i],
                       dot_u(i + dot_u_offset), joint_accel[i] - dot_u(i));
            }
            *acc_error += BT_ID_POW(joint_accel[i] - dot_u(i + dot_u_offset), 2);
        }
    } else {
        vec3 base_dot_omega;
        vec3 world_dot_omega;
        world_dot_omega(0) = base_accel[0];
        world_dot_omega(1) = base_accel[1];
        world_dot_omega(2) = base_accel[2];
        base_dot_omega = world_T_base.transpose() * world_dot_omega;

        // com happens to coincide with link origin here. If that changes, we need to calculate
        // ddot_com
        vec3 base_ddot_com;
        vec3 world_ddot_com;
        world_ddot_com(0) = base_accel[3];
        world_ddot_com(1) = base_accel[4];
        world_ddot_com(2) = base_accel[5];
        base_ddot_com = world_T_base.transpose()*world_ddot_com;

        for (int i = 0; i < 3; i++) {
            if (verbose) {
                printf("bt::base_dot_omega(%d)= %e dot_u[%d]= %e, diff= %e\n", i, base_dot_omega(i),
                       i, dot_u[i], base_dot_omega(i) - dot_u[i]);
            }
            *acc_error += BT_ID_POW(base_dot_omega(i) - dot_u(i), 2);
        }
        for (int i = 0; i < 3; i++) {
            if (verbose) {
                printf("bt::base_ddot_com(%d)= %e dot_u[%d]= %e, diff= %e\n", i, base_ddot_com(i),
                       i, dot_u[i + 3], base_ddot_com(i) - dot_u[i + 3]);
            }
            *acc_error += BT_ID_POW(base_ddot_com(i) - dot_u(i + 3), 2);
        }

        for (int i = 0; i < btmb->getNumDofs(); i++) {
            if (verbose) {
                printf("bt:ddot_q[%d]= %f, id:ddot_q= %e, diff= %e\n", i, joint_accel[i],
                       dot_u(i + 6), joint_accel[i] - dot_u(i + 6));
            }
            *acc_error += BT_ID_POW(joint_accel[i] - dot_u(i + 6), 2);
        }
    }
    *acc_error = std::sqrt(*acc_error);
    if (verbose) {
        printf("======dynamics-err: %e\n", *acc_error);
    }
    *pos_error = 0.0;

    {
        mat33 world_T_body;
        if (-1 == id_tree->getBodyTransform(0, &world_T_body)) {
            error_message("getting transform for body %d\n", 0);
            return -1;
        }
        vec3 world_com;
        if (-1 == id_tree->getBodyCoM(0, &world_com)) {
            error_message("getting com for body %d\n", 0);
            return -1;
        }
        if (verbose) {
            printf("id:com:       %f %f %f\n", world_com(0), world_com(1), world_com(2));

            printf("id:transform: %f %f %f\n"
                   "              %f %f %f\n"
                   "              %f %f %f\n",
                   world_T_body(0, 0), world_T_body(0, 1), world_T_body(0, 2), world_T_body(1, 0),
                   world_T_body(1, 1), world_T_body(1, 2), world_T_body(2, 0), world_T_body(2, 1),
                   world_T_body(2, 2));
        }
    }

    for (int l = 0; l < btmb->getNumLinks(); l++) {
        const btMultibodyLink &bt_link = btmb->getLink(l);

        vec3 bt_origin = bt_link.m_cachedWorldTransform.getOrigin();
        mat33 bt_basis = bt_link.m_cachedWorldTransform.getBasis();
        if (verbose) {
            printf("------------- link %d\n", l + 1);
            printf("bt:com:       %f %f %f\n", bt_origin(0), bt_origin(1), bt_origin(2));
            printf("bt:transform: %f %f %f\n"
                   "              %f %f %f\n"
                   "              %f %f %f\n",
                   bt_basis(0, 0), bt_basis(0, 1), bt_basis(0, 2), bt_basis(1, 0), bt_basis(1, 1),
                   bt_basis(1, 2), bt_basis(2, 0), bt_basis(2, 1), bt_basis(2, 2));
        }
        mat33 id_world_T_body;
        vec3 id_world_com;

        if (-1 == id_tree->getBodyTransform(l + 1, &id_world_T_body)) {
            error_message("getting transform for body %d\n", l);
            return -1;
        }
        if (-1 == id_tree->getBodyCoM(l + 1, &id_world_com)) {
            error_message("getting com for body %d\n", l);
            return -1;
        }
        if (verbose) {
            printf("id:com:       %f %f %f\n", id_world_com(0), id_world_com(1), id_world_com(2));

            printf("id:transform: %f %f %f\n"
                   "              %f %f %f\n"
                   "              %f %f %f\n",
                   id_world_T_body(0, 0), id_world_T_body(0, 1), id_world_T_body(0, 2),
                   id_world_T_body(1, 0), id_world_T_body(1, 1), id_world_T_body(1, 2),
                   id_world_T_body(2, 0), id_world_T_body(2, 1), id_world_T_body(2, 2));
        }
        vec3 diff_com = bt_origin - id_world_com;
        mat33 diff_basis = bt_basis - id_world_T_body;
        if (verbose) {
            printf("diff-com:    %e %e %e\n", diff_com(0), diff_com(1), diff_com(2));

            printf("diff-transform: %e %e %e %e %e %e %e %e %e\n", diff_basis(0, 0),
                   diff_basis(0, 1), diff_basis(0, 2), diff_basis(1, 0), diff_basis(1, 1),
                   diff_basis(1, 2), diff_basis(2, 0), diff_basis(2, 1), diff_basis(2, 2));
        }
        double total_pos_err =
            BT_ID_SQRT(BT_ID_POW(diff_com(0), 2) + BT_ID_POW(diff_com(1), 2) +
                       BT_ID_POW(diff_com(2), 2) + BT_ID_POW(diff_basis(0, 0), 2) +
                       BT_ID_POW(diff_basis(0, 1), 2) + BT_ID_POW(diff_basis(0, 2), 2) +
                       BT_ID_POW(diff_basis(1, 0), 2) + BT_ID_POW(diff_basis(1, 1), 2) +
                       BT_ID_POW(diff_basis(1, 2), 2) + BT_ID_POW(diff_basis(2, 0), 2) +
                       BT_ID_POW(diff_basis(2, 1), 2) + BT_ID_POW(diff_basis(2, 2), 2));
        if (verbose) {
            printf("======kin-pos-err: %e\n", total_pos_err);
        }
        if (total_pos_err > *pos_error) {
            *pos_error = total_pos_err;
        }
    }

    return 0;
}
}
