#ifdef ambientCubemapFlag
uniform vec3 u_ambientCubemap[6];

void calculateAmbientCube(inout vec3 lightDiffuse, in vec3 normal) {
	vec3 squaredNormal = normal * normal;
	vec3 isPositive  = step(0.0, normal);
	lightDiffuse += squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +
			squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +
			squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);
}
#endif //ambientCubemapFlag