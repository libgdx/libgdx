#ifndef CLONETREE_CREATOR_HPP_
#define CLONETREE_CREATOR_HPP_

#include "BulletInverseDynamics/IDConfig.hpp"
#include "MultiBodyTreeCreator.hpp"

namespace btInverseDynamics {
/// Generate an identical multibody tree from a reference system.
class CloneTreeCreator : public MultiBodyTreeCreator {
public:
    /// ctor
    /// @param reference the MultiBodyTree to clone
    CloneTreeCreator(const MultiBodyTree*reference);
    ~CloneTreeCreator();
    ///\copydoc MultiBodyTreeCreator::getNumBodies
    int getNumBodies(int* num_bodies) const;
    ///\copydoc MultiBodyTreeCreator::getBody
    int getBody(const int body_index, int* parent_index, JointType* joint_type,
                vec3* parent_r_parent_body_ref, mat33* body_T_parent_ref, vec3* body_axis_of_motion,
                idScalar* mass, vec3* body_r_body_com, mat33* body_I_body, int* user_int,
                void** user_ptr) const;

private:
    const MultiBodyTree *m_reference;
};
}
#endif  // CLONETREE_CREATOR_HPP_
