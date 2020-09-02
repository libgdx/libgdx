#ifndef DILLCREATOR_HPP_
#define DILLCREATOR_HPP_

#include "MultiBodyTreeCreator.hpp"

namespace btInverseDynamics {


/// Creator class for building a "Dill" system as intruduced as benchmark example in
/// Featherstone (1999), "A Divide-and-Conquer Articulated-Body Algorithm for Parallel O(log(n))
/// Calculation of Rigid-Body Dynamics. Part 2: Trees, Loops, and Accuracy.",  The International
/// Journal of Robotics Research 18 (9): 876–892. doi : 10.1177 / 02783649922066628.
///
/// This is a self-similar branched tree, somewhat resembling a dill plant
class DillCreator : public MultiBodyTreeCreator {
public:
    /// ctor
    /// @param levels the number of dill levels
    DillCreator(int levels);
    /// dtor
    ~DillCreator();
    ///\copydoc MultiBodyTreeCreator::getNumBodies
    int getNumBodies(int* num_bodies) const;
    ///\copydoc MultiBodyTreeCreator::getBody
    int getBody(const int body_index, int* parent_index, JointType* joint_type,
                vec3* parent_r_parent_body_ref, mat33* body_T_parent_ref, vec3* body_axis_of_motion,
                idScalar* mass, vec3* body_r_body_com, mat33* body_I_body, int* user_int,
                void** user_ptr) const;

private:
    /// recursively generate dill bodies.
    /// TODO better documentation
    int recurseDill(const int levels, const int parent, const idScalar d_DH_in,
                    const idScalar a_DH_in, const idScalar alpha_DH_in);
    int m_level;
    int m_num_bodies;
    idArray<int>::type m_parent;
    idArray<vec3>::type m_parent_r_parent_body_ref;
    idArray<mat33>::type m_body_T_parent_ref;
    idArray<vec3>::type m_body_axis_of_motion;
    idArray<idScalar>::type m_mass;
    idArray<vec3>::type m_body_r_body_com;
    idArray<mat33>::type m_body_I_body;
    int m_current_body;
};
}
#endif
