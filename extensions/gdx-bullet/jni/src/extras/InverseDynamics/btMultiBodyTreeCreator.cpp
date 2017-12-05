#include "btMultiBodyTreeCreator.hpp"

namespace btInverseDynamics {

btMultiBodyTreeCreator::btMultiBodyTreeCreator() : m_initialized(false) {}

int btMultiBodyTreeCreator::createFromBtMultiBody(const btMultiBody *btmb, const bool verbose) {
    if (0x0 == btmb) {
        error_message("cannot create MultiBodyTree from null pointer\n");
        return -1;
    }

    // in case this is a second call, discard old data
    m_data.clear();
    m_initialized = false;

    // btMultiBody treats base link separately
    m_data.resize(1 + btmb->getNumLinks());

    // add base link data
    {
        LinkData &link = m_data[0];

        link.parent_index = -1;
        if (btmb->hasFixedBase()) {
            link.joint_type = FIXED;
        } else {
            link.joint_type = FLOATING;
        }
        btTransform transform(btmb->getBaseWorldTransform());

        link.parent_r_parent_body_ref(0) = transform.getOrigin()[0];
        link.parent_r_parent_body_ref(1) = transform.getOrigin()[1];
        link.parent_r_parent_body_ref(2) = transform.getOrigin()[2];

        link.body_T_parent_ref(0, 0) = transform.getBasis()[0][0];
        link.body_T_parent_ref(0, 1) = transform.getBasis()[0][1];
        link.body_T_parent_ref(0, 2) = transform.getBasis()[0][2];

        link.body_T_parent_ref(1, 0) = transform.getBasis()[1][0];
        link.body_T_parent_ref(1, 1) = transform.getBasis()[1][1];
        link.body_T_parent_ref(1, 2) = transform.getBasis()[1][2];

        link.body_T_parent_ref(2, 0) = transform.getBasis()[2][0];
        link.body_T_parent_ref(2, 1) = transform.getBasis()[2][1];
        link.body_T_parent_ref(2, 2) = transform.getBasis()[2][2];

        // random unit vector. value not used for fixed or floating joints.
        link.body_axis_of_motion(0) = 0;
        link.body_axis_of_motion(1) = 0;
        link.body_axis_of_motion(2) = 1;

        link.mass = btmb->getBaseMass();
        // link frame in the center of mass
        link.body_r_body_com(0) = 0;
        link.body_r_body_com(1) = 0;
        link.body_r_body_com(2) = 0;
        // BulletDynamics uses body-fixed frame in the cog, aligned with principal axes
        link.body_I_body(0, 0) = btmb->getBaseInertia()[0];
        link.body_I_body(0, 1) = 0.0;
        link.body_I_body(0, 2) = 0.0;
        link.body_I_body(1, 0) = 0.0;
        link.body_I_body(1, 1) = btmb->getBaseInertia()[1];
        link.body_I_body(1, 2) = 0.0;
        link.body_I_body(2, 0) = 0.0;
        link.body_I_body(2, 1) = 0.0;
        link.body_I_body(2, 2) = btmb->getBaseInertia()[2];
        // shift reference point to link origin (in joint axis)
        mat33 tilde_r_com = tildeOperator(link.body_r_body_com);
        link.body_I_body = link.body_I_body - link.mass * tilde_r_com * tilde_r_com;
        if (verbose) {
            id_printf("base: mass= %f, bt_inertia= [%f %f %f]\n"
                      "Io= [%f %f %f;\n"
                      "    %f %f %f;\n"
                      "    %f %f %f]\n",
                      link.mass, btmb->getBaseInertia()[0], btmb->getBaseInertia()[1],
                      btmb->getBaseInertia()[2], link.body_I_body(0, 0), link.body_I_body(0, 1),
                      link.body_I_body(0, 2), link.body_I_body(1, 0), link.body_I_body(1, 1),
                      link.body_I_body(1, 2), link.body_I_body(2, 0), link.body_I_body(2, 1),
                      link.body_I_body(2, 2));
        }
    }

    for (int bt_index = 0; bt_index < btmb->getNumLinks(); bt_index++) {
        if (verbose) {
            id_printf("bt->id: converting link %d\n", bt_index);
        }
        const btMultibodyLink &bt_link = btmb->getLink(bt_index);
        LinkData &link = m_data[bt_index + 1];

        link.parent_index = bt_link.m_parent + 1;

        link.mass = bt_link.m_mass;
        if (verbose) {
            id_printf("mass= %f\n", link.mass);
        }
        // from this body's pivot to this body's com in this body's frame
        link.body_r_body_com[0] = bt_link.m_dVector[0];
        link.body_r_body_com[1] = bt_link.m_dVector[1];
        link.body_r_body_com[2] = bt_link.m_dVector[2];
        if (verbose) {
            id_printf("com= %f %f %f\n", link.body_r_body_com[0], link.body_r_body_com[1],
                      link.body_r_body_com[2]);
        }
        // BulletDynamics uses a body-fixed frame in the CoM, aligned with principal axes
        link.body_I_body(0, 0) = bt_link.m_inertiaLocal[0];
        link.body_I_body(0, 1) = 0.0;
        link.body_I_body(0, 2) = 0.0;
        link.body_I_body(1, 0) = 0.0;
        link.body_I_body(1, 1) = bt_link.m_inertiaLocal[1];
        link.body_I_body(1, 2) = 0.0;
        link.body_I_body(2, 0) = 0.0;
        link.body_I_body(2, 1) = 0.0;
        link.body_I_body(2, 2) = bt_link.m_inertiaLocal[2];
        // shift reference point to link origin (in joint axis)
        mat33 tilde_r_com = tildeOperator(link.body_r_body_com);
        link.body_I_body = link.body_I_body - link.mass * tilde_r_com * tilde_r_com;

        if (verbose) {
            id_printf("link %d: mass= %f, bt_inertia= [%f %f %f]\n"
                      "Io= [%f %f %f;\n"
                      "    %f %f %f;\n"
                      "    %f %f %f]\n",
                      bt_index, link.mass, bt_link.m_inertiaLocal[0], bt_link.m_inertiaLocal[1],
                      bt_link.m_inertiaLocal[2], link.body_I_body(0, 0), link.body_I_body(0, 1),
                      link.body_I_body(0, 2), link.body_I_body(1, 0), link.body_I_body(1, 1),
                      link.body_I_body(1, 2), link.body_I_body(2, 0), link.body_I_body(2, 1),
                      link.body_I_body(2, 2));
        }
        // transform for vectors written in parent frame to this link's body-fixed frame
        btMatrix3x3 basis = btTransform(bt_link.m_zeroRotParentToThis).getBasis();
        link.body_T_parent_ref(0, 0) = basis[0][0];
        link.body_T_parent_ref(0, 1) = basis[0][1];
        link.body_T_parent_ref(0, 2) = basis[0][2];
        link.body_T_parent_ref(1, 0) = basis[1][0];
        link.body_T_parent_ref(1, 1) = basis[1][1];
        link.body_T_parent_ref(1, 2) = basis[1][2];
        link.body_T_parent_ref(2, 0) = basis[2][0];
        link.body_T_parent_ref(2, 1) = basis[2][1];
        link.body_T_parent_ref(2, 2) = basis[2][2];
        if (verbose) {
            id_printf("body_T_parent_ref= %f %f %f\n"
                      "                   %f %f %f\n"
                      "                   %f %f %f\n",
                      basis[0][0], basis[0][1], basis[0][2], basis[1][0], basis[1][1], basis[1][2],
                      basis[2][0], basis[2][1], basis[2][2]);
        }
        switch (bt_link.m_jointType) {
            case btMultibodyLink::eRevolute:
                link.joint_type = REVOLUTE;
                if (verbose) {
                    id_printf("type= revolute\n");
                }
                link.body_axis_of_motion(0) = bt_link.m_axes[0].m_topVec[0];
                link.body_axis_of_motion(1) = bt_link.m_axes[0].m_topVec[1];
                link.body_axis_of_motion(2) = bt_link.m_axes[0].m_topVec[2];

                // for revolute joints, m_eVector = parentComToThisPivotOffset
                //                      m_dVector = thisPivotToThisComOffset
                // from parent com to pivot, in parent frame
                link.parent_r_parent_body_ref(0) = bt_link.m_eVector[0];
                link.parent_r_parent_body_ref(1) = bt_link.m_eVector[1];
                link.parent_r_parent_body_ref(2) = bt_link.m_eVector[2];
                break;
            case btMultibodyLink::ePrismatic:
                link.joint_type = PRISMATIC;
                if (verbose) {
                    id_printf("type= prismatic\n");
                }
                link.body_axis_of_motion(0) = bt_link.m_axes[0].m_bottomVec[0];
                link.body_axis_of_motion(1) = bt_link.m_axes[0].m_bottomVec[1];
                link.body_axis_of_motion(2) = bt_link.m_axes[0].m_bottomVec[2];

                // for prismatic joints, eVector
                //                                according to documentation :
                //                                parentComToThisComOffset
                //                                but seems to be: from parent's com to parent's
                //                                pivot ??
                //                       m_dVector = thisPivotToThisComOffset
                link.parent_r_parent_body_ref(0) = bt_link.m_eVector[0];
                link.parent_r_parent_body_ref(1) = bt_link.m_eVector[1];
                link.parent_r_parent_body_ref(2) = bt_link.m_eVector[2];
                break;
            case btMultibodyLink::eSpherical:
                error_message("spherical joints not implemented\n");
                return -1;
            case btMultibodyLink::ePlanar:
                error_message("planar joints not implemented\n");
                return -1;
            case btMultibodyLink::eFixed:
                link.joint_type = FIXED;
                // random unit vector
                link.body_axis_of_motion(0) = 0;
                link.body_axis_of_motion(1) = 0;
                link.body_axis_of_motion(2) = 1;

                // for fixed joints, m_dVector = thisPivotToThisComOffset;
                //                   m_eVector = parentComToThisPivotOffset;
                link.parent_r_parent_body_ref(0) = bt_link.m_eVector[0];
                link.parent_r_parent_body_ref(1) = bt_link.m_eVector[1];
                link.parent_r_parent_body_ref(2) = bt_link.m_eVector[2];
                break;
            default:
                error_message("unknown btMultiBody::eFeatherstoneJointType %d\n",
                              bt_link.m_jointType);
                return -1;
        }
        if (link.parent_index > 0) {  // parent body isn't the root
            const btMultibodyLink &bt_parent_link = btmb->getLink(link.parent_index - 1);
            // from parent pivot to parent com, in parent frame
            link.parent_r_parent_body_ref(0) += bt_parent_link.m_dVector[0];
            link.parent_r_parent_body_ref(1) += bt_parent_link.m_dVector[1];
            link.parent_r_parent_body_ref(2) += bt_parent_link.m_dVector[2];
        } else {
            // parent is root body. btMultiBody only knows 6-DoF or 0-DoF root bodies,
            // whose link frame is in the CoM (ie, no notion of a pivot point)
        }

        if (verbose) {
            id_printf("parent_r_parent_body_ref= %f %f %f\n", link.parent_r_parent_body_ref[0],
                      link.parent_r_parent_body_ref[1], link.parent_r_parent_body_ref[2]);
        }
    }

    m_initialized = true;

    return 0;
}

int btMultiBodyTreeCreator::getNumBodies(int *num_bodies) const {
    if (false == m_initialized) {
        error_message("btMultiBody not converted yet\n");
        return -1;
    }

    *num_bodies = static_cast<int>(m_data.size());
    return 0;
}

int btMultiBodyTreeCreator::getBody(const int body_index, int *parent_index, JointType *joint_type,
                                    vec3 *parent_r_parent_body_ref, mat33 *body_T_parent_ref,
                                    vec3 *body_axis_of_motion, idScalar *mass,
                                    vec3 *body_r_body_com, mat33 *body_I_body, int *user_int,
                                    void **user_ptr) const {
    if (false == m_initialized) {
        error_message("MultiBodyTree not created yet\n");
        return -1;
    }

    if (body_index < 0 || body_index >= static_cast<int>(m_data.size())) {
        error_message("index out of range (got %d but only %zu bodies)\n", body_index,
                      m_data.size());
        return -1;
    }

    *parent_index = m_data[body_index].parent_index;
    *joint_type = m_data[body_index].joint_type;
    *parent_r_parent_body_ref = m_data[body_index].parent_r_parent_body_ref;
    *body_T_parent_ref = m_data[body_index].body_T_parent_ref;
    *body_axis_of_motion = m_data[body_index].body_axis_of_motion;
    *mass = m_data[body_index].mass;
    *body_r_body_com = m_data[body_index].body_r_body_com;
    *body_I_body = m_data[body_index].body_I_body;

    *user_int = -1;
    *user_ptr = 0x0;

    return 0;
}
}
