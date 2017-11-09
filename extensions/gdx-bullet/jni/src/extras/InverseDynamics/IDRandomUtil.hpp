#ifndef ID_RANDOM_UTIL_HPP_
#define ID_RANDOM_UTIL_HPP_
#include "BulletInverseDynamics/IDConfig.hpp"
namespace btInverseDynamics {
/// seed random number generator using time()
void randomInit();
/// seed random number generator with identical value to get repeatable results
void randomInit(unsigned seed);
/// Generate (not quite) uniformly distributed random integers in [low, high]
/// Note: this is a low-quality implementation using only rand(), as
/// C++11 <random> is not supported in bullet.
/// The results will *not* be perfectly uniform.
/// \param low is the lower bound (inclusive)
/// \param high is the lower bound (inclusive)
/// \return a random number within [\param low, \param high]
int randomInt(int low, int high);
/// Generate a (not quite) uniformly distributed random floats in [low, high]
/// Note: this is a low-quality implementation using only rand(), as
/// C++11 <random> is not supported in bullet.
/// The results will *not* be perfectly uniform.
/// \param low is the lower bound (inclusive)
/// \param high is the lower bound (inclusive)
/// \return a random number within [\param low, \param high]
float randomFloat(float low, float high);

/// generate a random valid mass value
/// \returns random mass
float randomMass();
/// generate a random valid vector of principal moments of inertia
vec3 randomInertiaPrincipal();
/// generate a random valid moment of inertia matrix
mat33 randomInertiaMatrix();
/// generate a random unit vector
vec3 randomAxis();
}
#endif
