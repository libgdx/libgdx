#ifdef sphericalHarmonicsFlag
uniform vec3 u_sphericalHarmonics[9];

void calculateSphericalHarmonics(inout vec3 lightDiffuse, in vec3 normal) {
	lightDiffuse += u_sphericalHarmonics[0];
	lightDiffuse += u_sphericalHarmonics[1] * normal.x;
	lightDiffuse += u_sphericalHarmonics[2] * normal.y;
	lightDiffuse += u_sphericalHarmonics[3] * normal.z;
	lightDiffuse += u_sphericalHarmonics[4] * (normal.x * normal.z);
	lightDiffuse += u_sphericalHarmonics[5] * (normal.z * normal.y);
	lightDiffuse += u_sphericalHarmonics[6] * (normal.y * normal.x);
	lightDiffuse += u_sphericalHarmonics[7] * (3.0 * normal.z * normal.z - 1.0);
	lightDiffuse += u_sphericalHarmonics[8] * (normal.x * normal.x - normal.y * normal.y);
}
#endif // sphericalHarmonicsFlag