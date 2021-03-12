#ifndef BTMULTIBODYTREECREATOR_HPP_
#define BTMULTIBODYTREECREATOR_HPP_

#include <vector>

#include "BulletInverseDynamics/IDConfig.hpp"
#include "MultiBodyTreeCreator.hpp"
#include "BulletDynamics/Featherstone/btMultiBody.h"

namespace btInverseDynamics {

/// MultiBodyTreeCreator implementation for converting
/// a btMultiBody forward dynamics model into a MultiBodyTree inverse dynamics model
class btMultiBodyTreeCreator : public MultiBodyTreeCreator {
public:
    /// ctor
    btMultiBodyTreeCreator();
    /// dtor
    ~btMultiBodyTreeCreator() {}
    /// extract model data from a btMultiBody
    /// @param btmb pointer to btMultiBody to convert
    /// @param verbose if true, some information is printed
    /// @return -1 on error, 0 on success
    int createFromBtMultiBody(const btMultiBody *btmb, const bool verbose = false);
    /// \copydoc MultiBodyTreeCreator::getNumBodies
    int getNumBodies(int *num_bodies) const;
    ///\copydoc MultiBodyTreeCreator::getBody
	int getBody(const int body_index, int *parent_index, JointType *joint_type,
		vec3 *parent_r_parent_body_ref, mat33 *body_T_parent_ref,
		vec3 *body_axis_of_motion, idScalar *mass, vec3 *body_r_body_com,
		mat33 *body_I_body, int *user_int, void **user_ptr) const;

private:
    // internal struct holding data extracted from btMultiBody
    struct LinkData {
        int parent_index;
        JointType joint_type;
        vec3 parent_r_parent_body_ref;
        mat33 body_T_parent_ref;
        vec3 body_axis_of_motion;
        idScalar mass;
        vec3 body_r_body_com;
        mat33 body_I_body;
    };
    idArray<LinkData>::type m_data;
    bool m_initialized;
};
}

#endif  // BTMULTIBODYTREECREATOR_HPP_
