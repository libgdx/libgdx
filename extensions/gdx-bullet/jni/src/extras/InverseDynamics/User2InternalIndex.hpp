#ifndef USER2INTERNALINDEX_HPP
#define USER2INTERNALINDEX_HPP
#include <map>
#include <vector>

#include "BulletInverseDynamics/IDConfig.hpp"

namespace btInverseDynamics {

/// Convert arbitrary indexing scheme to internal indexing
/// used for MultiBodyTree
class User2InternalIndex {
public:
    /// Ctor
    User2InternalIndex();
    /// add body to index maps
    /// @param body index of body to add (external)
    /// @param parent index of parent body (external)
    void addBody(const int body, const int parent);
    /// build mapping from external to internal indexing
    /// @return 0 on success, -1 on failure
    int buildMapping();
    /// get internal index from external index
    /// @param user external (user) index
    /// @param internal pointer for storage of corresponding internal index
    /// @return 0 on success, -1 on failure
    int user2internal(const int user, int *internal) const;
    /// get internal index from external index
    /// @param user external (user) index
    /// @param internal pointer for storage of corresponding internal index
    /// @return 0 on success, -1 on failure
    int internal2user(const int internal, int *user) const;

private:
    int findRoot(int index);
    void recurseIndexSets(const int user_body_index);
    bool m_map_built;
    std::map<int, int> m_user_parent_index_map;
    std::map<int, int> m_user_to_internal;
    std::map<int, int> m_internal_to_user;
    std::map<int, std::vector<int> > m_user_child_indices;
    int m_current_index;
};
}

#endif  // USER2INTERNALINDEX_HPP
