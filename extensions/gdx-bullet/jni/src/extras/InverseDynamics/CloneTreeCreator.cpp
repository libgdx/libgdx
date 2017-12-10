#include "CloneTreeCreator.hpp"

#include <cstdio>

namespace btInverseDynamics {
#define CHECK_NULLPTR()                                                                            \
    do {                                                                                           \
        if (m_reference == 0x0) {                                                                      \
            error_message("m_reference == 0x0\n");                                                     \
            return -1;                                                                             \
        }                                                                                          \
    } while (0)

#define TRY(x)                                                                                     \
    do {                                                                                           \
        if (x == -1) {                                                                             \
            error_message("error calling " #x "\n");                                               \
            return -1;                                                                             \
        }                                                                                          \
    } while (0)
CloneTreeCreator::CloneTreeCreator(const MultiBodyTree* reference) { m_reference = reference; }

CloneTreeCreator::~CloneTreeCreator() {}

int CloneTreeCreator::getNumBodies(int* num_bodies) const {
    CHECK_NULLPTR();
    *num_bodies = m_reference->numBodies();
    return 0;
}

int CloneTreeCreator::getBody(const int body_index, int* parent_index, JointType* joint_type,
                              vec3* parent_r_parent_body_ref, mat33* body_T_parent_ref,
                              vec3* body_axis_of_motion, idScalar* mass, vec3* body_r_body_com,
                              mat33* body_I_body, int* user_int, void** user_ptr) const {
    CHECK_NULLPTR();
    TRY(m_reference->getParentIndex(body_index, parent_index));
    TRY(m_reference->getJointType(body_index, joint_type));
    TRY(m_reference->getParentRParentBodyRef(body_index, parent_r_parent_body_ref));
    TRY(m_reference->getBodyTParentRef(body_index, body_T_parent_ref));
    TRY(m_reference->getBodyAxisOfMotion(body_index, body_axis_of_motion));
    TRY(m_reference->getBodyMass(body_index, mass));
    TRY(m_reference->getBodyFirstMassMoment(body_index, body_r_body_com));
    TRY(m_reference->getBodySecondMassMoment(body_index, body_I_body));
    TRY(m_reference->getUserInt(body_index, user_int));
    TRY(m_reference->getUserPtr(body_index, user_ptr));

    return 0;
}
}
