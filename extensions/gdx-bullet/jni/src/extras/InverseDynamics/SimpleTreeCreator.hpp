#ifndef SIMPLETREECREATOR_HPP_
#define SIMPLETREECREATOR_HPP_

#include "MultiBodyTreeCreator.hpp"

namespace btInverseDynamics {

/// minimal "tree" (chain)
class SimpleTreeCreator : public MultiBodyTreeCreator {
public:
    /// ctor
    /// @param dim number of bodies
    SimpleTreeCreator(int dim);
    // dtor
    ~SimpleTreeCreator() {}
    ///\copydoc MultiBodyTreeCreator::getNumBodies
    int getNumBodies(int* num_bodies) const;
    ///\copydoc MultiBodyTreeCreator::getBody
    int getBody(const int body_index, int* parent_index, JointType* joint_type,
                vec3* parent_r_parent_body_ref, mat33* body_T_parent_ref, vec3* body_axis_of_motion,
                idScalar* mass, vec3* body_r_body_com, mat33* body_I_body, int* user_int,
                void** user_ptr) const;

private:
    int m_num_bodies;
    idScalar m_mass;
    mat33 m_body_T_parent_ref;
    vec3 m_parent_r_parent_body_ref;
    vec3 m_body_r_body_com;
    mat33 m_body_I_body;
    vec3 m_axis;
};
}
#endif  // SIMPLETREECREATOR_HPP_
