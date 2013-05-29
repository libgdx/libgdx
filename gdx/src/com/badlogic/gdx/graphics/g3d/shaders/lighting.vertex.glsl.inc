#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif // ambientLightFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
uniform vec3 u_cameraPosition;
#endif // specularFlag

#endif // lightingFlag

#ifdef lightingFlag
void calculateLighting(in vec3 normal, in vec4 pos) {
	vec3 lightDiffuse = vec3(0.0);
	vec3 lightSpecular = vec3(0.0);
	
	#ifdef ambientLightFlag
		lightDiffuse = u_ambientLight;
	#endif // ambientLightFlag
		
	#ifdef ambientCubemapFlag
		calculateAmbientCube(lightDiffuse, normal);
	#endif // ambientCubemapFlag

	#ifdef sphericalHarmonicsFlag
		calculateSphericalHarmonics(lightDiffuse, normal);			
	#endif // sphericalHarmonicsFlag
		
	#ifdef specularFlag
		vec3 viewVec = normalize(u_cameraPosition - pos.xyz);
	#endif // specularFlag
		
	#ifdef directionalLightsFlag
		calculateDirectionLights(lightDiffuse, lightSpecular, normal, viewVec);
	#endif //directionalLightsFlag

	#ifdef pointLightsFlag
		calculatePointLights(lightDiffuse, lightSpecular, normal, viewVec, pos);
	#endif // pointLightsFlag
	
	v_lightDiffuse = lightDiffuse;
	#ifdef specularFlag
		v_lightSpecular = lightSpecular;
	#endif
}
#endif // lightingFlag