#include <cmath>
#include <cstdlib>
#include <ctime>

#include "BulletInverseDynamics/IDConfig.hpp"
#include "BulletInverseDynamics/IDMath.hpp"
#include "IDRandomUtil.hpp"


namespace btInverseDynamics {

// constants for random mass and inertia generation
// these are arbitrary positive values.
static const float mass_min = 0.001;
static const float mass_max = 1.0;

void randomInit() { srand(time(NULL)); }
void randomInit(unsigned seed) { srand(seed); }

int randomInt(int low, int high) { return rand() % (high + 1 - low) + low; }

float randomFloat(float low, float high) {
    return low + static_cast<float>(rand()) / RAND_MAX * (high - low);
}

float randomMass() { return randomFloat(mass_min, mass_max); }

vec3 randomInertiaPrincipal() {
    vec3 inertia;
    do {
        inertia(0) = randomFloat(mass_min, mass_max);
        inertia(1) = randomFloat(mass_min, mass_max);
        inertia(2) = randomFloat(mass_min, mass_max);
    } while (inertia(0) + inertia(1) < inertia(2) || inertia(0) + inertia(2) < inertia(1) ||
             inertia(1) + inertia(2) < inertia(0));
    return inertia;
}

mat33 randomInertiaMatrix() {
    // generate random valid inertia matrix by first getting valid components
    // along major axes and then rotating by random amount
    vec3 principal = randomInertiaPrincipal();
    mat33 rot(transformX(randomFloat(-BT_ID_PI, BT_ID_PI)) * transformY(randomFloat(-BT_ID_PI, BT_ID_PI)) *
              transformZ(randomFloat(-BT_ID_PI, BT_ID_PI)));
    mat33 inertia;
    inertia(0, 0) = principal(0);
    inertia(0, 1) = 0;
    inertia(0, 2) = 0;
    inertia(1, 0) = 0;
    inertia(1, 1) = principal(1);
    inertia(1, 2) = 0;
    inertia(2, 0) = 0;
    inertia(2, 1) = 0;
    inertia(2, 2) = principal(2);
    return rot * inertia * rot.transpose();
}

vec3 randomAxis() {
    vec3 axis;
    idScalar length;
    do {
        axis(0) = randomFloat(-1.0, 1.0);
        axis(1) = randomFloat(-1.0, 1.0);
        axis(2) = randomFloat(-1.0, 1.0);

        length = BT_ID_SQRT(BT_ID_POW(axis(0), 2) + BT_ID_POW(axis(1), 2) + BT_ID_POW(axis(2), 2));
    } while (length < 0.01);

    return axis / length;
}
}
