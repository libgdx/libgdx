#ifndef INVDYN_BULLET_COMPARISON_HPP
#define INVDYN_BULLET_COMPARISON_HPP

#include "BulletInverseDynamics/IDConfig.hpp"

class btMultiBody;
class btVector3;

namespace btInverseDynamics {
class MultiBodyTree;

/// this function compares the forward dynamics computations implemented in btMultiBody to
/// the inverse dynamics implementation in MultiBodyTree. This is done in three steps
/// 1. run inverse dynamics for (q, u, dot_u) to obtain joint forces f
/// 2. run forward dynamics (btMultiBody) for (q,u,f) to obtain dot_u_bullet
/// 3. compare dot_u with dot_u_bullet for cross check of forward and inverse dynamics computations
/// @param btmb the bullet forward dynamics model
/// @param id_tree the inverse dynamics model
/// @param q vector of generalized coordinates (matches id_tree)
/// @param u vector of generalized speeds (matches id_tree)
/// @param gravity gravitational acceleration in world frame
/// @param dot_u vector of generalized accelerations (matches id_tree)
/// @param gravity gravitational acceleration in world frame
/// @param base_fixed set base joint to fixed or
/// @param pos_error is set to the maximum of the euclidean norm of position+rotation errors of all
///        center of gravity positions and link frames
/// @param acc_error is set to the square root of the sum of squared differences of generalized
/// accelerations
///        computed in step 3 relative to dot_u
/// @return -1 on error, 0 on success
int compareInverseAndForwardDynamics(vecx &q, vecx &u, vecx &dot_u, btVector3 &gravity, bool verbose,
                                     btMultiBody *btmb, MultiBodyTree *id_tree, double *pos_error,
                                     double *acc_error);
}
#endif  // INVDYN_BULLET_COMPARISON_HPP
