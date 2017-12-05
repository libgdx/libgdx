#include "MultiBodyNameMap.hpp"

namespace btInverseDynamics {

MultiBodyNameMap::MultiBodyNameMap() {}

int MultiBodyNameMap::addBody(const int index, const std::string& name) {
    if (m_index_to_body_name.count(index) > 0) {
        error_message("trying to add index %d again\n", index);
        return -1;
    }
    if (m_body_name_to_index.count(name) > 0) {
        error_message("trying to add name %s again\n", name.c_str());
        return -1;
    }

    m_index_to_body_name[index] = name;
    m_body_name_to_index[name] = index;

    return 0;
}

int MultiBodyNameMap::addJoint(const int index, const std::string& name) {
    if (m_index_to_joint_name.count(index) > 0) {
        error_message("trying to add index %d again\n", index);
        return -1;
    }
    if (m_joint_name_to_index.count(name) > 0) {
        error_message("trying to add name %s again\n", name.c_str());
        return -1;
    }

    m_index_to_joint_name[index] = name;
    m_joint_name_to_index[name] = index;

    return 0;
}

int MultiBodyNameMap::getBodyName(const int index, std::string* name) const {
    std::map<int, std::string>::const_iterator it = m_index_to_body_name.find(index);
    if (it == m_index_to_body_name.end()) {
        error_message("index %d not known\n", index);
        return -1;
    }
    *name = it->second;
    return 0;
}

int MultiBodyNameMap::getJointName(const int index, std::string* name) const {
    std::map<int, std::string>::const_iterator it = m_index_to_joint_name.find(index);
    if (it == m_index_to_joint_name.end()) {
        error_message("index %d not known\n", index);
        return -1;
    }
    *name = it->second;
    return 0;
}

int MultiBodyNameMap::getBodyIndex(const std::string& name, int* index) const {
    std::map<std::string, int>::const_iterator it = m_body_name_to_index.find(name);
    if (it == m_body_name_to_index.end()) {
        error_message("name %s not known\n", name.c_str());
        return -1;
    }
    *index = it->second;
    return 0;
}

int MultiBodyNameMap::getJointIndex(const std::string& name, int* index) const {
    std::map<std::string, int>::const_iterator it = m_joint_name_to_index.find(name);
    if (it == m_joint_name_to_index.end()) {
        error_message("name %s not known\n", name.c_str());
        return -1;
    }
    *index = it->second;
    return 0;
}
}
