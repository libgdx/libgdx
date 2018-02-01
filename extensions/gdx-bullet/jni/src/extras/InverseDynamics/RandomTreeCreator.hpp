#ifndef RANDOMTREE_CREATOR_HPP_
#define RANDOMTREE_CREATOR_HPP_

#include "BulletInverseDynamics/IDConfig.hpp"
#include "MultiBodyTreeCreator.hpp"

namespace btInverseDynamics {
/// Generate a random MultiBodyTree with fixed or floating base and fixed, prismatic or revolute
/// joints
/// Uses a pseudo random number generator seeded from a random device.
class RandomTreeCreator : public MultiBodyTreeCreator {
public:
    /// ctor
    /// @param max_bodies maximum number of bodies
    /// @param gravity gravitational acceleration
    /// @param use_seed if true, seed random number generator
    RandomTreeCreator(const int max_bodies, bool use_seed=false);
    ~RandomTreeCreator();
    ///\copydoc MultiBodyTreeCreator::getNumBodies
    int getNumBodies(int* num_bodies) const;
    ///\copydoc MultiBodyTreeCreator::getBody
    int getBody(const int body_index, int* parent_index, JointType* joint_type,
                vec3* parent_r_parent_body_ref, mat33* body_T_parent_ref, vec3* body_axis_of_motion,
                idScalar* mass, vec3* body_r_body_com, mat33* body_I_body, int* user_int,
                void** user_ptr) const;

private:
    int m_num_bodies;
};
}
#endif  // RANDOMTREE_CREATOR_HPP_
