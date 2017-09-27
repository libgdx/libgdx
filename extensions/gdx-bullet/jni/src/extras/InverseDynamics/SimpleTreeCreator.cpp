#include "SimpleTreeCreator.hpp"

#include <cstdio>

namespace btInverseDynamics {
/// minimal "tree" (chain)
SimpleTreeCreator::SimpleTreeCreator(int dim) : m_num_bodies(dim) {
    m_mass = 1.0;
    m_body_T_parent_ref(0, 0) = 1;
    m_body_T_parent_ref(0, 1) = 0;
    m_body_T_parent_ref(0, 2) = 0;
    m_body_T_parent_ref(1, 0) = 0;
    m_body_T_parent_ref(1, 1) = 1;
    m_body_T_parent_ref(1, 2) = 0;
    m_body_T_parent_ref(2, 0) = 0;
    m_body_T_parent_ref(2, 1) = 0;
    m_body_T_parent_ref(2, 2) = 1;

    m_parent_r_parent_body_ref(0) = 1.0;
    m_parent_r_parent_body_ref(1) = 0.0;
    m_parent_r_parent_body_ref(2) = 0.0;

    m_body_r_body_com(0) = 0.5;
    m_body_r_body_com(1) = 0.0;
    m_body_r_body_com(2) = 0.0;

    m_body_I_body(0, 0) = 1;
    m_body_I_body(0, 1) = 0;
    m_body_I_body(0, 2) = 0;
    m_body_I_body(1, 0) = 0;
    m_body_I_body(1, 1) = 1;
    m_body_I_body(1, 2) = 0;
    m_body_I_body(2, 0) = 0;
    m_body_I_body(2, 1) = 0;
    m_body_I_body(2, 2) = 1;

    m_axis(0) = 0;
    m_axis(1) = 0;
    m_axis(2) = 1;
}
int SimpleTreeCreator::getNumBodies(int* num_bodies) const {
    *num_bodies = m_num_bodies;
    return 0;
}

int SimpleTreeCreator::getBody(const int body_index, int* parent_index, JointType* joint_type,
                               vec3* parent_r_parent_body_ref, mat33* body_T_parent_ref,
                               vec3* body_axis_of_motion, idScalar* mass, vec3* body_r_body_com,
                               mat33* body_I_body, int* user_int, void** user_ptr) const {
    *parent_index = body_index - 1;
    if (body_index % 2) {
        *joint_type = PRISMATIC;
    } else {
        *joint_type = REVOLUTE;
    }
    *parent_r_parent_body_ref = m_parent_r_parent_body_ref;
    if (0 == body_index) {
        (*parent_r_parent_body_ref)(2) = 1.0;
    }
    *body_T_parent_ref = m_body_T_parent_ref;
    *body_axis_of_motion = m_axis;
    *mass = m_mass;
    *body_r_body_com = m_body_r_body_com;
    *body_I_body = m_body_I_body;
    *user_int = 0;
    *user_ptr = 0;
    return 0;
}
}
