#include "User2InternalIndex.hpp"

namespace btInverseDynamics {
User2InternalIndex::User2InternalIndex() : m_map_built(false) {}

void User2InternalIndex::addBody(const int body, const int parent) {
    m_user_parent_index_map[body] = parent;
}

int User2InternalIndex::findRoot(int index) {
    if (0 == m_user_parent_index_map.count(index)) {
        return index;
    }
    return findRoot(m_user_parent_index_map[index]);
}

// modelled after URDF2Bullet.cpp:void ComputeParentIndices(const
// URDFImporterInterface& u2b, URDF2BulletCachedData& cache, int urdfLinkIndex,
// int urdfParentIndex)
void User2InternalIndex::recurseIndexSets(const int user_body_index) {
    m_user_to_internal[user_body_index] = m_current_index;
    m_current_index++;
    for (size_t i = 0; i < m_user_child_indices[user_body_index].size(); i++) {
        recurseIndexSets(m_user_child_indices[user_body_index][i]);
    }
}

int User2InternalIndex::buildMapping() {
    // find root index
    int user_root_index = -1;
    for (std::map<int, int>::iterator it = m_user_parent_index_map.begin();
         it != m_user_parent_index_map.end(); it++) {
        int current_root_index = findRoot(it->second);
        if (it == m_user_parent_index_map.begin()) {
            user_root_index = current_root_index;
        } else {
            if (user_root_index != current_root_index) {
                error_message("multiple roots (at least) %d and %d\n", user_root_index,
                              current_root_index);
                return -1;
            }
        }
    }

    // build child index map
    for (std::map<int, int>::iterator it = m_user_parent_index_map.begin();
         it != m_user_parent_index_map.end(); it++) {
        m_user_child_indices[it->second].push_back(it->first);
    }

    m_current_index = -1;
    // build internal index set
    m_user_to_internal[user_root_index] = -1;  // add map for root link
    recurseIndexSets(user_root_index);

    // reverse mapping
    for (std::map<int, int>::iterator it = m_user_to_internal.begin();
         it != m_user_to_internal.end(); it++) {
        m_internal_to_user[it->second] = it->first;
    }

    m_map_built = true;
    return 0;
}

int User2InternalIndex::user2internal(const int user, int *internal) const {

    if (!m_map_built) {
        return -1;
    }

    std::map<int, int>::const_iterator it;
    it = m_user_to_internal.find(user);
    if (it != m_user_to_internal.end()) {
        *internal = it->second;
        return 0;
    } else {
        error_message("no user index %d\n", user);
        return -1;
    }
}

int User2InternalIndex::internal2user(const int internal, int *user) const {

    if (!m_map_built) {
        return -1;
    }

    std::map<int, int>::const_iterator it;
    it = m_internal_to_user.find(internal);
    if (it != m_internal_to_user.end()) {
        *user = it->second;
        return 0;
    } else {
        error_message("no internal index %d\n", internal);
        return -1;
    }
}
}
