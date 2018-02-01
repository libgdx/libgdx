#include "MultiBodyTreeDebugGraph.hpp"

#include <cstdio>

namespace btInverseDynamics {

int writeGraphvizDotFile(const MultiBodyTree* tree, const MultiBodyNameMap* map,
                         const char* filename) {
    if (0x0 == tree) {
        error_message("tree pointer is null\n");
        return -1;
    }
    if (0x0 == filename) {
        error_message("filename is null\n");
        return -1;
    }

    FILE* fp = fopen(filename, "w");
    if (NULL == fp) {
        error_message("cannot open file %s for writing\n", filename);
        return -1;
    }
    fprintf(fp, "// to generate postscript file, run dot -Tps %s -o %s.ps\n"
                "// details see graphviz documentation at http://graphviz.org\n"
                "digraph tree {\n",
            filename, filename);

    for (int body = 0; body < tree->numBodies(); body++) {
        std::string name;
        if (0x0 != map) {
            if (-1 == map->getBodyName(body, &name)) {
                error_message("can't get name of body %d\n", body);
                return -1;
            }
            fprintf(fp, "              %d [label=\"%d/%s\"];\n", body, body, name.c_str());
        }
    }
    for (int body = 0; body < tree->numBodies(); body++) {
        int parent;
        const char* joint_type;
        int qi;
        if (-1 == tree->getParentIndex(body, &parent)) {
            error_message("indexing error\n");
            return -1;
        }
        if (-1 == tree->getJointTypeStr(body, &joint_type)) {
            error_message("indexing error\n");
            return -1;
        }
        if (-1 == tree->getDoFOffset(body, &qi)) {
            error_message("indexing error\n");
            return -1;
        }
        if (-1 != parent) {
            fprintf(fp, "              %d -> %d [label= \"type:%s, q=%d\"];\n", parent, body,
                    joint_type, qi);
        }
    }

    fprintf(fp, "}\n");
    fclose(fp);
    return 0;
}
}
