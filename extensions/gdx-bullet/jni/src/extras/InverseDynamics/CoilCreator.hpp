#ifndef COILCREATOR_HPP_
#define COILCREATOR_HPP_

#include "MultiBodyTreeCreator.hpp"

namespace btInverseDynamics {

/// Creator class for building a "coil" system as intruduced as benchmark example in
/// Featherstone (1999), "A Divide-and-Conquer Articulated-Body Algorithm for Parallel O(log(n))
/// Calculation of Rigid-Body Dynamics. Part 2: Trees, Loops, and Accuracy.",  The International
/// Journal of Robotics Research 18 (9): 876–892. doi : 10.1177 / 02783649922066628.
///
/// This is a serial chain, with an initial configuration resembling a coil.
class CoilCreator : public MultiBodyTreeCreator {
public:
    /// ctor.
    /// @param n the number of bodies in the system
    CoilCreator(int n);
    /// dtor
    ~CoilCreator();
    // \copydoc MultiBodyTreeCreator::getNumBodies
    int getNumBodies(int* num_bodies) const;
    // \copydoc MultiBodyTreeCreator::getBody
    int getBody(const int body_index, int* parent_index, JointType* joint_type,
                vec3* parent_r_parent_body_ref, mat33* body_T_parent_ref, vec3* body_axis_of_motion,
                idScalar* mass, vec3* body_r_body_com, mat33* body_I_body, int* user_int,
                void** user_ptr) const;

private:
    int m_num_bodies;
    std::vector<int> m_parent;
    vec3 m_parent_r_parent_body_ref;
    mat33 m_body_T_parent_ref;
    vec3 m_body_axis_of_motion;
    idScalar m_mass;
    vec3 m_body_r_body_com;
    mat33 m_body_I_body;
};
}
#endif
