#if defined(lightingFlag) && defined(numPointLights) && defined(normalFlag)
#if (numPointLights > 0)
#define pointLightsFlag
#endif
#endif

#ifdef pointLightsFlag
struct PointLight
{
	vec3 color;
	vec3 position;
	float intensity;
};
uniform PointLight u_pointLights[numPointLights];

void calculatePointLights(inout vec3 lightDiffuse, inout vec3 lightSpecular, in vec3 normal, in vec3 viewVec, in vec4 pos) {
	for (int i = 0; i < numPointLights; i++) {
		vec3 lightDir = u_pointLights[i].position - pos.xyz;
		float dist2 = dot(lightDir, lightDir);
		lightDir *= inversesqrt(dist2);
		float NdotL = clamp(dot(normal, lightDir), 0.0, 2.0);
		float falloff = clamp(u_pointLights[i].intensity / (1.0 + dist2), 0.0, 2.0); // FIXME mul intensity on cpu
		lightDiffuse += u_pointLights[i].color * (NdotL * falloff);
		#ifdef specularFlag
			float halfDotView = clamp(dot(normal, normalize(lightDir + viewVec)), 0.0, 2.0);
			lightSpecular += u_pointLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess) * falloff, 0.0, 2.0);
		#endif // specularFlag
	}
}
#endif // pointLightsFlag