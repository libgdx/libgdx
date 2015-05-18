#include "types.h"


class Vectors
{
 public:
  static void normalize(float* v);
  static void cross(float* v1, float* v2, float* res);
  static float length(float* v);
};
