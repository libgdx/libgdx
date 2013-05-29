#if defined(lightingFlag) && defined(numDirectionalLights) && defined(normalFlag)
#if (numDirectionalLights > 0) 
#define directionalLightsFlag
#endif
#endif

#ifdef directionalLightsFlag
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];

void calculateDirectionLights(inout vec3 lightDiffuse, inout vec3 lightSpecular, in vec3 normal, in vec3 viewVec) {
	for (int i = 0; i < numDirectionalLights; i++) {
		vec3 lightDir = -u_dirLights[i].direction;
		float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
		lightDiffuse += u_dirLights[i].color * NdotL;
		#ifdef specularFlag
			float halfDotView = dot(normal, normalize(lightDir + viewVec));
			lightSpecular += u_dirLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess), 0.0, 1.0);
		#endif // specularFlag
	}
}
#endif //directionalLightsFlag