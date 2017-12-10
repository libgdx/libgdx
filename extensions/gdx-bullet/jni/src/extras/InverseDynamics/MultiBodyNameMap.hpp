#ifndef MULTIBODYNAMEMAP_HPP_
#define MULTIBODYNAMEMAP_HPP_

#include "BulletInverseDynamics/IDConfig.hpp"
#include <string>
#include <map>

namespace btInverseDynamics {

/// \brief The MultiBodyNameMap class
/// Utility class that stores a maps from body/joint indices to/from body and joint names
class MultiBodyNameMap {
public:
    MultiBodyNameMap();
    /// add a body to the map
    /// @param index of the body
    /// @param name name of the body
    /// @return 0 on success, -1 on failure
    int addBody(const int index, const std::string& name);
    /// add a joint to the map
    /// @param index of the joint
    /// @param name name of the joint
    /// @return 0 on success, -1 on failure
    int addJoint(const int index, const std::string& name);
    /// get body name from index
    /// @param index of the body
    /// @param body_name name of the body
    /// @return 0 on success, -1 on failure
    int getBodyName(const int index, std::string* name) const;
    /// get joint name from index
    /// @param index of the joint
    /// @param joint_name name of the joint
    /// @return 0 on success, -1 on failure
    int getJointName(const int index, std::string* name) const;
    /// get body index from name
    /// @param index of the body
    /// @param name name of the body
    /// @return 0 on success, -1 on failure
    int getBodyIndex(const std::string& name, int* index) const;
    /// get joint index from name
    /// @param index of the joint
    /// @param name name of the joint
    /// @return 0 on success, -1 on failure
    int getJointIndex(const std::string& name, int* index) const;

private:
    std::map<int, std::string> m_index_to_joint_name;
    std::map<int, std::string> m_index_to_body_name;

    std::map<std::string, int> m_joint_name_to_index;
    std::map<std::string, int> m_body_name_to_index;
};
}
#endif  // MULTIBODYNAMEMAP_HPP_
