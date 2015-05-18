#include "Lerp.h"

float Lerp::value(float start, float end, float t)
{
  float width = end-start;
  return start + t*width;
}
