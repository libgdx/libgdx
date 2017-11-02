#ifndef MULTIBODYTREEDEBUGGRAPH_HPP_
#define MULTIBODYTREEDEBUGGRAPH_HPP_
#include "BulletInverseDynamics/IDConfig.hpp"
#include "BulletInverseDynamics/MultiBodyTree.hpp"
#include "MultiBodyNameMap.hpp"

namespace btInverseDynamics {
/// generate a dot-file of the multibody tree for generating a graph using graphviz' dot tool
/// @param tree the multibody tree
/// @param map to add names of links (if 0x0, no names will be added)
/// @param filename name for the output file
/// @return 0 on success, -1 on error
int writeGraphvizDotFile(const MultiBodyTree* tree, const MultiBodyNameMap* map,
                         const char* filename);
}

#endif  // MULTIBODYTREEDEBUGGRAPH_HPP
